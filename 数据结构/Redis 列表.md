# Redis 列表
>八股专用 
## 列表
列表是简单的字符串列表，按照插入顺序排序。

### 底层编码
- ziplist：当列表的元素个数小于`list-max-ziplist-entries`配置（默认512）并且列表中每个元素的值都小于`list-max-ziplist-value`配置（默认64字节）时，列表使用ziplist编码。
- linkedlist：当列表的元素不满足ziplist条件时，列表使用linkedlist编码。

#### ziplist编码
ziplist编码的列表使用压缩列表作为底层数据结构。

#### linkedlist编码
linkedlist编码的列表使用双端链表作为底层数据结构。

### 面试版本- ziplist
#### Redis列表的底层实现是什么？
Redis列表有两种底层实现：
当元素较少且较小时使用ziplist(压缩列表)；
当元素较多或较大时使用linkedlist(双端链表)。
具体由list-max-ziplist-entries和list-max-ziplist-value两个配置决定。

进阶:
##### 演进历史
Redis列表的底层实现经历了三个主要阶段：
早期版本：使用双向链表(linkedlist)或压缩列表(ziplist)
Redis 3.2：引入quicklist作为统一实现
Redis 7.0：引入listpack替代ziplist，但整体结构仍是quicklist


#### 为什么Redis要使用压缩列表？
压缩列表是为了节约内存而设计的。通过连续内存存储和特殊编码，减少了内存碎片和指针开销，对于小数据集非常高效。

#### 压缩列表的优缺点是什么？
优点是内存利用率高，缓存友好；缺点是增删操作可能导致连锁更新，时间复杂度在最坏情况下可达O(n²)。

#### 为什么是O(n²)。
单次更新：最坏情况下，一次操作可能导致所有n个节点连锁更新，复杂度为O(n)
累积效应：对列表进行n次操作，每次都可能触发O(n)的连锁更新，总复杂度为O(n²)

#### 压缩列表的内存布局与节点结构
压缩列表是连续内存块，包含以下部分：
zlbytes(4字节)：整个列表占用字节数
zltail(4字节)：尾节点偏移量
zllen(2字节)：节点数量
各个节点entry：每个节点包含三部分
prevlen：前一节点长度，1字节或5字节
encoding：编码方式和当前节点长度
data：实际数据
zlend(1字节)：结束标记0xFF

#### 如何优化压缩列表的连锁更新问题？Redis是如何解决的？
Redis使用惰性删除和内存重分配来优化连锁更新问题。
惰性删除：
在删除节点时，不立即释放内存，避免了因删除操作引发的连锁更新,而是等待后续插入操作时再进行内存重分配。
内存重分配：
在插入节点时，如果空间不足，Redis会重新分配内存，并进行内存重分配。
引入quicklist：Redis 3.2后用quicklist替代纯压缩列表，将数据分散在多个小的ziplist中，限制了连锁更新的范围

#### 为什么会有连锁更新问题？
在压缩列表中，每个节点都存储了前一个节点的长度，当删除或插入节点时，需要更新前一个节点的长度，如果前一个节点的长度小于当前节点的长度，则需要更新前一个节点的长度，如果前一个节点的长度大于当前节点的长度，则需要更新当前节点的长度，这样就会导致连锁更新问题。



### 面试版本- linkedlist

#### Redis列表的linkedlist编码是什么？
linkedlist是Redis列表的一种编码方式，当列表元素较多或较大时使用。
它基于双向链表实现，每个节点都有指向前后节点的指针。

#### linkedlist的内存布局是什么？
链表结构(list)：包含表头、表尾指针和节点数量等信息
链表节点(listNode)：包含前驱指针、后继指针和值对象
值对象通常是简单动态字符串(SDS).



### 面试版本 - quicklist
#### 什么是Redis的quicklist？quicklist的基本结构是什么？它解决了什么问题？
quicklist是Redis 3.2版本引入的一种数据结构，是ziplist和linkedlist的混合体。
quicklist是由多个ziplist节点组成的双向链表。每个quicklist节点包含一个ziplist，ziplist中存储实际数据。这种结构既保留了ziplist的内存效率，又具备了linkedlist的灵活性。
解决了单纯使用ziplist可能导致的连锁更新问题，以及单纯使用linkedlist带来的内存开销大的问题。

#### quicklist的内存布局是什么？
quicklist结构(quicklist)：包含表头、表尾指针和节点数量等信息
quicklist节点(quicklistNode)：包含前驱指针、后继指针和ziplist
ziplist结构(ziplist)：包含节点数量、尾节点偏移量等信息



#### quicklist如何实现压缩以进一步节省内存
quicklist使用LZF算法对中间节点的ziplist进行压缩存储。
通过list-compress-depth参数控制压缩范围，只有距离头尾一定距离以上的节点才会被压缩，这样既节省了内存，又保证了常用数据的快速访问。压缩后的节点在需要访问时会被解压缩，使用完毕后再次压缩。

#### 如何控制每个zipList的大小
通过list-max-ziplist-size参数控制每个ziplist的大小。
quicklist的节点ziplist越小，越有可能造成更多的内存碎片。
极端情况下，一个ziplist只有一个数据entry，也就退化成了linked list.
quicklist的节点ziplist越大，分配给ziplist的连续内存空间越困难。
极端情况下，一个quicklist只有一个ziplist，也就退化成了ziplist.
redis提供list-max-ziplist-size参数进行配置，默认-2，表示每个ziplist节点大小不超过8KB











