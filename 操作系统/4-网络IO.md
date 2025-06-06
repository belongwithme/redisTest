@[TOC](网络IO)

## 说一下Linux五种IO模型（重要）
Linux的五种I/O模型包括：
1. 阻塞I/O（Blocking I/O）：进程发起I/O请求后一直阻塞，直到操作完成
2. 非阻塞I/O（Non-blocking I/O）：进程发起I/O请求后立即返回，需要轮询检查操作是否完成
3. I/O多路复用（I/O Multiplexing）：通过select/poll/epoll监控多个文件描述符，一旦某个描述符就绪就通知进程处理
4. 信号驱动I/O（Signal-driven I/O）：进程注册信号处理函数，当I/O就绪时接收信号通知
5. 异步I/O（Asynchronous I/O）：进程发起I/O请求后立即返回，I/O操作完成后系统通知进程

个人理解版本:
谈到Linux的五种I/O模型，我习惯从"等待"的角度来理解它们的差异。
阻塞I/O就像排队买票，你必须在窗口前一直等待，期间不能做任何其他事情。这是最简单的模型，适合单线程场景，但效率较低。
非阻塞I/O则像每隔几分钟去窗口询问"票准备好了吗"，你可以利用询问之间的时间做其他事。这减少了等待，但增加了轮询开销。
I/O多路复用像是一个人同时排多个队，哪个窗口先准备好就去哪个窗口办理。通过select/poll/epoll机制，一个线程可以监控多个I/O操作，大大提高了效率，这是高性能服务器的标配。
信号驱动I/O是在橱窗留下手机号，票准备好时会收到短信通知。进程可以完全不用关心I/O操作，直到收到系统信号。
异步I/O最为先进，相当于网上订票，你只需提交订单，然后完全专注其他事情，票不仅准备好而且会直接送到你手上。在Linux中，真正的异步I/O直到较新的内核版本才得到完善支持。
这五种模型反映了操作系统I/O设计的演进历程，从简单到高效，从同步到异步。
## 阻塞IO和非阻塞IO的应用场景问题，有一个计算密集型的场景，和一个给用户传视频的场景，分别应该用什么IO?
计算密集型场景：适合非阻塞I/O或I/O多路复用
- 原因：计算任务需要大量CPU资源，阻塞I/O会导致CPU资源浪费
- 实现：使用非阻塞I/O配合事件循环，或多线程处理计算任务
视频传输场景：适合非阻塞I/O结合I/O多路复用
- 原因：视频传输是I/O密集型，需要高并发处理多个客户端连接
- 实现：使用epoll/select等多路复用机制处理多个连接，提高吞吐量
- 还可结合零拷贝技术减少数据复制开销

个人理解版回答:
在这两个截然不同的场景中，I/O模型的选择需要考虑资源利用效率。
对于计算密集型场景，核心是要让CPU尽可能多地用于计算而非I/O等待。在这种情况下，非阻塞I/O是明智之选。假设我们正在处理大规模数据分析，我会选择使用非阻塞I/O模型，结合工作队列的设计，让CPU在等待I/O期间继续处理其他计算任务。实际上，许多高性能计算框架如Spark就采用类似的设计理念。
对于视频传输场景，这是典型的I/O密集型应用，特点是连接数多、数据量大。我会选择I/O多路复用模型，特别是epoll机制，因为它能高效处理大量并发连接。Netflix、Twitch等流媒体平台的服务器架构通常就采用这种设计。此外，我还会结合零拷贝技术进一步优化视频数据传输，减少内核态与用户态之间的数据复制，提高吞吐量。
## 谈谈你对 I/O 多路复用的理解（重要）
I/O多路复用是指单个进程/线程同时监控多个文件描述符，当其中任何一个就绪时进行相应处理的技术。核心特点包括：
1. 使用select/poll/epoll等系统调用实现
2. 可以同时监控多个I/O事件，避免为每个连接创建线程
3. 减少了线程切换开销，提高了系统并发处理能力
4. 分为水平触发(Level-Triggered)和边缘触发(Edge-Triggered)两种模式
5. 是高性能网络服务器的核心技术之一

个人理解版本:
I/O多路复用技术本质上是解决C10K问题（同时处理10000个连接）的关键突破，它彻底改变了服务器架构设计思路。
我将I/O多路复用比喻为大堂经理制度：传统的阻塞I/O模型像是银行为每个客户配备一名专职柜员（每个连接一个线程），当客户数量增加，员工成本和管理复杂度急剧上升；而I/O多路复用则像是设置一名大堂经理，他同时观察所有窗口的状态，一旦发现某个客户办理完材料准备手续，立即安排柜员处理，极大提高了人力资源利用效率。
从技术角度看，I/O多路复用通过单个系统调用监控多个文件描述符的状态变化，将"轮询等待"的工作交给内核完成，避免了用户空间的忙等待或线程阻塞。现代服务器架构如Nginx、Redis等都充分利用了这一机制，才能以极小的资源消耗处理海量并发连接。
在我的实践中，曾经通过将传统多线程服务器改造为基于epoll的事件驱动模型，使系统并发连接数从原来的几百提升到上万，同时CPU和内存占用显著下降。I/O多路复用的价值不仅体现在性能提升上，更在于它带来的系统架构简化和可扩展性增强。
## select, poll, epoll 有什么区别?（重要）

## select, poll, epoll 适合哪些应用场景?
各I/O复用模型适合的场景：
select适用场景：
1. 连接数较少（<1000）的环境
2. 要求跨平台的应用
3. 对实时性要求不高的场景
poll适用场景：
1. 连接数较多但依然可控
2. 需要突破select的1024连接数限制
3. 依然对跨平台有一定要求
epoll适用场景：
1. 大规模高并发服务（>10000连接）
2. 长连接为主的应用（如聊天服务器、推送服务等）
3. 只在Linux平台部署的服务
4. 对系统性能要求较高的场景

个人理解版本:
select虽然看似落伍，但在特定场景下仍有价值。它的优势在于实现简单且可移植性强，几乎所有平台都支持。
poll解决了select的一些限制，特别是描述符数量上限，但本质上仍是遍历式检查。它适合那些连接数中等（数百至数千）且需要比select更好扩展性的系统。例如，一些中等规模的内部服务或管理系统，使用poll能够获得不错的平衡。
epoll是高性能服务器的不二之选，尤其适合长连接、高并发场景。
## epoll ET 模式和 LT 模式有什么区别? 哪一个更高效?
epoll的ET(边缘触发)模式和LT(水平触发)模式区别：
1. LT模式(默认)：
- 只要文件描述符就绪，每次epoll_wait都会通知
- 可以不一次处理完全部数据，下次调用epoll_wait依然会通知
- 编程方式简单，类似于传统的select/poll
2. ET模式：
- 仅在描述符状态发生变化时才通知一次
- 必须一次处理完全部数据，否则后续将不再通知
- 减少了系统调用次数，理论上更高效
- 编程复杂度更高，容易出错
3. 效率比较：
- ET模式在高并发连接下理论上更高效，因为减少了重复通知
- 但实际性能提升取决于应用场景和实现方式
- ET模式必须配合非阻塞I/O使用，否则容易出现死锁

个人理解版回答:
LT模式就像一个不断响起的闹钟：只要有数据可读，它就会不断提醒你，直到你处理完所有数据。这种机制容错性高，即使你一次没读完所有数据，下次epoll_wait依然会通知你。这种特性使得LT模式编程更简单，也更接近传统的select/poll编程模型。
ET模式则像门铃：只在状态变化时响一次。有人按门铃时会响一次，但不会一直响直到你开门。在ET模式下，当文件描述符从未就绪变为就绪时，epoll只会通知一次，即使你没有处理完所有数据，也不会再次提醒。这要求程序必须在接收到通知时尽可能多地读取数据，通常需要循环读取直到返回EAGAIN错误。

ET模式理论上更高效，因为它减少了系统调用和重复通知，特别是在高负载系统中。
然而，这种效率提升伴随着更高的编程复杂度和潜在风险，如果忘记一次性处理完数据，可能导致部分事件丢失。
除非系统真正处于极高并发状态且每微秒的性能都至关重要，否则LT模式的简单性和可靠性通常更有价值。
## 零拷贝技术了解过吗? 说一下原理（重要）
零拷贝(Zero-Copy)是一种I/O优化技术，目标是避免内核和用户空间之间的数据复制，从而减少CPU开销和提高I/O性能。传统I/O至少需要4次拷贝和2次上下文切换，而零拷贝可以显著减少这些开销。
主要实现方式包括：
1. mmap+write：
- 通过内存映射将文件映射到内核缓冲区
- 用户空间和内核空间共享这块内存，避免了一次拷贝
- 依然需要从内核缓冲区拷贝到socket缓冲区
2. sendfile：
- 数据不经过用户空间，直接从内核文件系统缓冲区传输到socket缓冲区
- 在Linux 2.4后的版本中，结合DMA技术，数据甚至不需要完整经过CPU
3. splice：
- 在两个文件描述符之间移动数据，无需经过用户空间
- 使用管道作为中间媒介
4. 直接I/O：
- 绕过操作系统的缓冲区，直接在用户空间与设备传输数据

应用场景：文件服务器、流媒体服务器、大数据传输等。

个人理解版回答:
个人理解版回答
零拷贝技术是我认为I/O优化领域最具革命性的进步之一，它彻底改变了数据在计算机系统中的传输方式。
要理解零拷贝的价值，首先需要了解传统I/O路径的低效之处。假设我们要从磁盘读取数据并通过网络发送，传统路径是这样的：磁盘→内核缓冲区→用户缓冲区→socket缓冲区→网卡。这个过程涉及4次数据拷贝(其中2次是CPU参与的)和2次上下文切换，当传输大文件时，这些开销累积起来非常可观。
零拷贝技术本质上是打破了用户空间与内核空间的数据传输壁垒。以Linux的sendfile系统调用为例，它允许数据直接从文件描述符传输到socket描述符，完全在内核空间内完成，无需用户空间参与。这不仅减少了数据复制次数，还避免了上下文切换的开销。
## reactor 模式有哪些方案?
Reactor模式是基于事件驱动的设计模式，主要有三种实现方案：
1. 单Reactor单线程模式：
- 一个Reactor线程负责多路分离和事件分发
- 所有I/O操作和业务处理在同一线程中完成
- 优点：简单，无并发问题
- 缺点：无法充分利用多核CPU，业务处理会阻塞反应堆
2. 单Reactor多线程模式：
- 一个Reactor线程负责监听和分发事件
- 多个工作线程负责处理业务逻辑
- 优点：能处理耗时业务，充分利用多核CPU
- 缺点：Reactor线程仍可能成为瓶颈，多线程引入同步问题
3. 多Reactor多线程模式：
- 主Reactor负责接受连接，然后分发给从Reactor

多个从Reactor线程负责I/O读写事件监听
业务处理由线程池负责
优点：充分利用多核CPU，负载均衡，扩展性好
缺点：实现复杂度高

个人理解版回答:
单Reactor多线程模式则像是一位迎宾员配合多位厨师。迎宾员负责接待顾客并记录点单，然后将订单分发给多位厨师去处理。这种模式解决了业务处理阻塞的问题，常见于一些中小型服务器。但它的瓶颈在于，所有的客户接待和点单工作仍由一人完成，在客流量大时，迎宾员可能应接不暇，成为系统瓶颈。
多Reactor多线程模式是最为成熟的方案，类似于高级餐厅的分工体系：设有门厅经理专门负责迎接客人并安排到不同区域就座，每个区域都有专属服务员负责点单，厨房则有多位厨师分工合作。这种多级分工的模式能够充分利用系统资源，实现最高的并发处理能力。Nginx、Netty等高性能服务器基本都采用这种架构。
## proactor 和 reactor 模式有什么区别?
Proactor和Reactor是两种不同的事件处理模式，主要区别如下：
1. 基本模式：
- Reactor是同步I/O模型，关注的是就绪事件
- Proactor是异步I/O模型，关注的是完成事件
2. 工作流程：
- Reactor：应用程序注册I/O就绪事件，当事件触发后，应用程序负责实际的I/O操作
- Proactor：应用程序提交异步I/O操作请求，当I/O操作完成后，系统通知应用程序
3. 关注点：
- Reactor关注"是否可以开始I/O"
- Proactor关注"I/O是否已完成"
4. 实现难度：
- Reactor较容易实现，且适用性更广
- Proactor实现复杂，对操作系统异步I/O支持要求高
5. 适用场景：
- Reactor适合并发连接数高但流量适中的场景
- Proactor适合I/O密集型且追求极致性能的场景
6. 常见应用：
- Reactor：Nginx、Redis、Netty
- Proactor：Windows的IOCP模型

个人理解版本:
Reactor模式就像传统餐厅：服务员(Reactor)注意到有客人入座(就绪事件)，便上前询问需求，然后根据订单提供服务。服务员在此模式下是"被动响应者"，只负责发现客人需求并提供相应服务。关键是，服务员需要亲自完成记录订单、传递订单等工作(I/O操作由应用处理)。这种模式在客流量大但单客户需求不复杂的场景下效率很高。
Proactor模式则更像外卖平台：客人在平台上直接下单(异步I/O请求)后就可以去做其他事，厨房准备完成后，外卖员会主动将食物送到客人手中(完成通知)。在此模式下，客人(应用程序)只需发出请求并等待结果，整个备餐和配送过程(I/O操作)都由系统完成。这种模式让应用程序能专注于业务逻辑，而非I/O细节。
从实现角度看，Reactor模式更为普及，因为它对操作系统的要求较低，基于select/poll/epoll等机制就能实现。而Proactor依赖于操作系统提供的异步I/O支持，在Linux下的支持一直不够完善，直到较新的io_uring机制出现才有了突破。