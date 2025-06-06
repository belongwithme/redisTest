# RDB日志文件

## 你怎么理解RDB日志文件?
RDB采用的是"快照思维"，它关注的是数据的"是什么"，即在某个时间点上，Redis中的数据状态是什么样的。它通过定期对整个数据集生成二进制快照来保存数据，就像照相机拍下某一瞬间的画面。
而AOF采用的是"日志思维"，它关注的是"发生了什么"，即记录了哪些改变数据的操作。它通过追加写入所有修改数据的命令来重现数据变化的过程，更像是一本详细记录每一步操作的日记。
这种本质差异导致了一些区别:在文件恢复速度,安全性,操作开销等方面有所不同,需要展开说吗?

### 文件恢复速度
RDB的恢复速度明显快于AOF。这是因为RDB文件本质上是一个数据快照，Redis可以直接加载这个二进制结构到内存中，几乎是"照单全收"。而AOF则需要重新执行文件中记录的每一条写命令，这个过程就像"重放"所有的操作，自然会慢很多。

### 安全性
AOF在安全性方面优于RDB。RDB是间歇性快照，两次快照之间的数据变更都存在丢失风险。比如，如果配置为5分钟做一次RDB，那么服务器宕机可能导致最近5分钟内的所有写操作丢失。
而AOF可以配置为每秒同步或每次写操作后立即同步，将数据丢失的风险降到最低。

### 操作开销
RDB在操作开销上通常小于AOF。RDB是周期性的，且通过fork子进程来完成，主进程可以继续处理客户端请求，对性能的影响相对较小。
AOF则需要在每次写操作后追加记录，特别是在同步写入模式下，会导致明显的性能下降。即使在每秒同步模式下，AOF也会比不使用持久化的Redis实例慢约10%-15%。
在我们的系统监控中发现，开启AOF后，Redis的QPS下降了约12%，而RDB的影响则不到5%。

### 文件体积
RDB文件通常比AOF文件小得多。因为RDB是数据的紧凑二进制表示，而AOF记录了所有的写命令及其参数。
举个例子，在我们的用户行为分析系统中，同样100万条记录，RDB文件约为200MB，而AOF文件则达到了1.2GB。这种差异对存储成本和备份传输都有显著影响。


## 如果AOF和RDB只能保留一个,你会保留哪个?
这个需要根据具体业务场景做选择:
如果能接受分钟级别的数据丢失,可以保留RDB,因为它恢复速度快,文件体积小。
如果需要考虑数据安全性,我会选择AOF,同时需要选择每秒刷盘的策略,以将数据丢失的风险降到最低。

当然如果说是我心里倾向性的答案:我会选择AOF
首先:数据安全性在大多数业务场景中是首要考虑因素。
AOF的数据丢失风险明显低于RDB，特别是在配置为每秒同步的情况下，最多只会丢失1秒的数据。而RDB可能会丢失两次快照之间的所有数据变更，这对于许多业务来说是不可接受的风险。

其次，虽然AOF在性能和文件大小方面不如RDB，但这些问题都有相应的解决方案:
例如，Redis提供了AOF重写机制，可以定期压缩AOF文件，减小其体积；而性能问题可以通过硬件升级或优化同步策略来缓解。
相比之下，RDB的数据丢失风险则很难通过技术手段完全规避。
我选择AOF是因为"数据安全大于一切"的原则,但是实际工作中,Redis 4.0以后提供的混合持久化方案，它能够结合两者的优势，我觉得是更为理想的选择.

## RDB持久化的触发时机
RDB持久化的触发时机主要有三种情况:
### 自动触发
1. 基于时间和修改次数配置的触发:通过修改配置文件中save命令来实现.
2. 主从复制触发: 从服务器连接并要求同步数据时,主服务器就会自动触发生成RDB快照.
3. Redis实例迁移触发: 某些Redis集群管理工具中(如:Redis Cluster)中,需要进行实例迁移,也会自动触发RDB来保存当前状态
### 手动触发
1. SAVE命令: 执行时在主线程中执行RDB持久化,并阻塞所有客户端请求.
2. BGSAVE命令: 执行时会fork一个子进程中执行RDB持久化,不阻塞客户端请求.
### 关闭时触发
关闭时,如果没有开启AOF持久化,Redis会自动执行一次RDB持久化,确保数据安全地保存到磁盘上再关闭服务。
(如果开启了AOF持久化，Redis会确保所有AOF缓冲区中的数据都被写入并同步到磁盘上。
此时Redis不会额外触发RDB持久化，即使RDB也是开启状态。)

## RDB刷盘过程
- 触发RDB生成
首先，当满足RDB触发条件时，Redis会调用rdbSave函数开始生成RDB文件。
- 创建子进程
Redis会调用fork()系统调用创建一个子进程。这个过程利用了操作系统的写时复制（Copy-On-Write）机制，初始时子进程与父进程共享内存空间，不会立即复制所有数据。
注意:所以，COW 在这里的核心作用是保证 RDB 快照的一致性（point-in-time consistency），而不是改变子进程最终要读取的数据内容本身。它隔离了父进程的并发修改，使得子进程可以在一个稳定的数据视图上完成持久化工作。
- 子进程生成RDB文件
子进程会遍历内存中的所有数据，将它们序列化后写入到一个临时文件中
- 文件刷盘
所有数据都写入临时文件后，子进程会调用fsync()系统调用，确保数据从操作系统缓冲区真正写入磁盘。这一步是真正的"刷盘"操作，确保数据持久化。
这一步的耗时与磁盘性能直接相关。使用SSD的实例比使用传统机械硬盘的实例在这一步有显著优势，刷盘时间可以缩短5-10倍。
- 原子性替换文件
当临时文件完全写入并刷盘后，子进程会调用rename()系统调用，将临时文件原子性地重命名为正式的RDB文件（通常是dump.rdb）。
- 通知主进程
子进程完成所有工作后会退出，主进程通过信号处理器捕获到子进程的退出状态，从而知道RDB生成已完成。
- 清理工作
主进程会更新统计信息，如上次RDB保存时间、RDB操作耗时等.

## RDB对主流程有什么影响
如果使用SAVE命令触发RDB，Redis会阻塞所有客户端请求，直到RDB文件生成完成,但是一般不会用这个命令去生成RDB.
一般都是fork一个子进程去执行持久化:
RDB刷盘过程中最耗时的通常是初始fork操作和最终fsync操作。
- fork操作的阻塞
阻塞时间与内存使用量直接相关
在内存较小的实例（几百MB）上，fork通常只需几毫秒
在大内存实例（几十GB）上，fork可能需要几百毫秒甚至更长
由于采用了写时复制技术，fork操作不会阻塞主进程，但会消耗大量内存，如果内存不足，会导致fork失败
-fsync操作
不直接阻塞主进程，但会消耗大量I/O资源，间接影响系统整体性能,尤其是数据量比较大的时候.

## 介绍一下什么是写时复制技术
写时复制的核心思想非常优雅：资源的复制只在需要写入时才进行，在此之前共享同一份资源。
具体来说：
1. 当需要复制一个资源时，系统并不立即创建一个完整的物理副本，而是让父子进程共享同一份资源
2. 当任一方尝试修改共享资源时，系统才会创建该部分资源的实际副本
3. 修改操作作用于新创建的副本上，而不影响原始资源
这种"推迟复制"的策略大大提高了资源利用效率，特别是在复制大型资源但只修改其中小部分的场景下。

## 写时复制的优势是什么
- 资源效率高：避免了不必要的内存复制，只有被修改的部分才会占用额外内存
- 响应速度快：初始复制操作几乎是即时的，不需要等待大量数据复制完成
- 并发友好：允许多个进程或线程并发访问资源，只有在写入冲突时才需要特殊处理
- 实现简单：对于开发者来说，COW机制通常由操作系统透明处理，简化了应用层代码





