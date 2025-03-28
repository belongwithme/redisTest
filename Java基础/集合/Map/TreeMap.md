# 基础概念与特性
## TreeMap是什么？它实现了哪些接口？它在Java集合框架中的位置是什么？
TreeMap是Java集合框架中的一个实现类，它实现了NavigableMap接口，而NavigableMap继承自SortedMap接口。
TreeMap是一个基于红黑树实现的有序映射表，它可以根据键的自然顺序或者提供的Comparator进行排序。
TreeMap对我而言是一种"有记忆的映射"。与HashMap相比，TreeMap不仅记住了键值对，还记住了它们之间的顺序关系。这种顺序关系使得TreeMap能够回答一系列HashMap无法回答的问题，比如"谁是最小的键"、"谁是比X大的最小键"等。特别适合那些需要按顺序处理数据的场景。
## TreeMap与HashMap的主要区别是什么？各自适用于什么场景？
TreeMap与HashMap的主要区别：
1.有序性：TreeMap保持键的有序性，HashMap不保证顺序
2.性能：HashMap基本操作平均O(1)，TreeMap为O(log n)
3.内部实现：HashMap基于哈希表，TreeMap基于红黑树
4.导航能力：TreeMap提供了丰富的导航方法，HashMap没有
5.对键的要求：TreeMap的键必须可比较，HashMap的键需要正确实现hashCode和equals
适用场景：
- HashMap适合：查找速度优先、不关心元素顺序、大量随机访问
- TreeMap适合：需要有序遍历、需要范围查询、需要获取最大/最小键
## TreeMap的核心特性是什么？它如何保证键的有序性？
TreeMap的核心特性是：
- 有序性：按键的顺序存储和访问元素
- 导航能力：提供查找最接近值的方法
- 高效的范围操作：支持子映射视图
- 可靠的时间复杂度：所有操作保证O(log n)性能
TreeMap通过两种方式保证键的有序性：
- 自然排序：键实现Comparable接口，TreeMap使用其compareTo()方法
- 外部比较器：创建TreeMap时提供Comparator实现，TreeMap使用其compare()方法
TreeMap的有序性是它最强大的特性，但这种有序性的价值常被低估。
不能只关注TreeMap比HashMap"慢"，而忽视了有序性带来的算法简化和功能增强。
在处理时间序列数据、区间查询、排名系统等场景时，TreeMap可以用简洁的API解决复杂问题
## TreeMap允许null键吗？为什么？
TreeMap不允许null键，但允许null值。这是因为TreeMap需要比较键来维护顺序，而null不能与任何对象进行比较，尝试这样做会导致NullPointerException。
如果创建TreeMap时提供了显式处理null的自定义Comparator，理论上可以支持null键，但这违背了集合框架的一致性原则.
# 源码分析
## TreeMap中的Entry结构是如何设计的？它与红黑树节点有什么关系？
## TreeMap如何处理键的重复情况？put方法的返回值有什么意义？
## TreeMap的序列化机制是什么？为什么需要特殊处理？
## clear方法是如何实现的？它的时间复杂度是多少？