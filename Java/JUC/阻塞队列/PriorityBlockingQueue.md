
# 基础概念
## 请简要介绍PriorityBlockingQueue的特点和主要功能
## PriorityBlockingQueue与普通PriorityQueue有什么区别？
## 它实现了哪些接口？这些接口的意义是什么？

# 内部实现
## PriorityBlockingQueue的内部数据结构是什么？它是如何保证优先级的？
## 它是如何实现线程安全的？用了哪些同步机制？
## 扩容机制是怎样的？与ArrayList等其他集合的扩容有何不同？

# 使用场景
## 什么情况下应该使用PriorityBlockingQueue而不是其他阻塞队列？
## 它适合处理哪类并发问题？能举例说明吗？
## 如何正确自定义元素的优先级排序规则？

# 深入原理
## PriorityBlockingQueue的put操作会阻塞吗？为什么？
## 它与DelayQueue有什么关系？
## 分析一下PriorityBlockingQueue在高并发情况下可能存在的性能瓶颈
## 如何理解它的"无界队列"特性？这会带来什么问题？

# 实际应用
## 如何使用PriorityBlockingQueue实现一个优先级任务调度系统？
## 在使用过程中有哪些常见的陷阱或错误？
## 如果需要实现一个带有超时优先级的队列，你会如何设计？

# 源码分析
## 能分析一下poll()方法的源码实现吗？
## 为什么PriorityBlockingQueue不允许存放null元素？

