@[TOC](LinkedHashMap)
### 能简单介绍一下LinkedHashMap吗？它与HashMap有什么区别？
LinkedHashMap是HashMap的一个子类，它在保持HashMap高效查找特性的同时，还维护了元素的插入顺序或访问顺序。
简单来说，它结合了HashMap和LinkedList的优点：像HashMap一样能O(1)时间复杂度查找元素，又像LinkedList一样保持元素的有序性。
与HashMap的主要区别在于：
1. HashMap只关心键值映射，不关心元素顺序
2. LinkedHashMap除了键值映射，还维护了元素的顺序
3. 内部通过双向链表实现有序性
可以按插入顺序(默认)或访问顺序排列元素
这个特性使它特别适合实现LRU缓存、保持数据插入顺序等场景。
### LinkedHashMap是如何保证有序性的？能说说它的内部结构吗？
它通过扩展HashMap的节点结构，在每个节点上增加了前后指针，形成了一个贯穿所有节点的双向链表。
具体来说，它定义了自己的Entry类,它有前后指针：
```java
static class Entry<K,V> extends HashMap.Node<K,V> {
    Entry<K,V> before, after; // 双向链表的前后指针
    Entry(int hash, K key, V value, Node<K,V> next) {
        super(hash, key, value, next);
    }
}
```
- 哈希表负责快速查找
- 双向链表负责维护顺序
当我们遍历LinkedHashMap时，实际上是按照这个双向链表的顺序进行的，而不是哈希桶的顺序。这就是为什么LinkedHashMap的遍历顺序是可预测的，而HashMap不是。
在进行插入、删除等操作时，LinkedHashMap除了维护哈希表结构，还会相应地更新这个双向链表，确保链表反映当前的顺序状态。"
### LinkedHashMap支持两种顺序：插入顺序和访问顺序。能详细说说它们的区别和实现原理吗？
LinkedHashMap支持两种排序模式，这是它非常强大的一个特性:
1. 插入顺序(默认)：元素顺序与它们添加到Map中的顺序一致。即使更新已存在的键，顺序也不会改变。
2. 访问顺序：元素会按照最近访问的时间排序，最近访问的元素会移到链表末尾。这个'访问'包括put、get、putAll等操作。
切换到访问顺序模式很简单，只需在构造时指定参与accessOrder为true.
在访问顺序模式下，每次访问元素后，LinkedHashMap会调用afterNodeAccess方法将访问的节点移到链表末尾，这样就实现了按访问时间排序。
这种访问顺序模式是实现LRU缓存的关键，因为最不常用的元素会自然地处于链表头部，而最近使用的元素则位于链表尾部。"
### 如何使用LinkedHashMap实现一个LRU缓存？需要注意什么？
我们可以利用它的访问顺序特性，再加上一点定制，就能轻松实现,这个实现的关键点在于：
1. 使用访问顺序模式(accessOrder=true)
2. 重写removeEldestEntry方法来控制缓存大小
当我们调用get或put时，被访问的元素会自动移到链表末尾，而最少访问的元素始终在链表头部。当缓存超过容量时，removeEldestEntry方法返回true，触发最老元素的移除。
这个方案比手动实现一个LRU缓存要简洁得多，而且性能也不错。不过需要注意，这个实现不是线程安全的，在多线程环境下需要额外的同步机制。"
### 在使用LinkedHashMap时，有哪些需要注意的性能问题？如何优化？
在使用LinkedHashMap时，有几个性能方面的考虑：
1. 内存占用
LinkedHashMap比HashMap消耗更多内存，因为它为每个节点增加了两个引用(before和after)。在大数据量场景下，这个额外开销需要考虑。
2. 操作开销
维护双向链表会带来额外的时间开销。虽然这个开销是常数级的，但在高频操作场景下可能会有影响。特别是在访问顺序模式下，每次get操作都会修改链表结构。
3. 初始容量设置
如果预知数据量，合理设置初始容量可以减少扩容次数。计算公式类似HashMap：initialCapacity = expectedSize / loadFactor + 1。
4. 迭代性能
LinkedHashMap的迭代性能优于HashMap，因为它直接遍历双向链表，而HashMap需要遍历哈希桶和链表/红黑树。
根据我的经验，在处理大约10万级别的数据时，这些性能差异就会变得明显。在一个实际项目中，我们通过预设合理的初始容量，将查询耗时降低了约15%。"
### 能说说LinkedHashMap是如何维护双向链表的吗？特别是在插入、删除、访问元素时？
LinkedHashMap维护双向链表的核心在于它重写了HashMap的几个关键方法，主要是三个回调方法:
1. afterNodeAccess: 当节点被访问后调用，在访问顺序模式下，将节点移到链表末尾
```java
void afterNodeAccess(Node<K,V> e) {
    LinkedHashMap.Entry<K,V> last;
    // 只在访问顺序模式下且节点不是尾节点时处理
    if (accessOrder && (last = tail) != e) {
        // 从链表中移除节点
        // 将节点添加到链表尾部
    }
}
```
2. afterNodeInsertion: 插入节点后调用，可能会删除最老的节点
```java
void afterNodeInsertion(boolean evict) {
    // 判断是否需要删除最老节点
    if (evict && removeEldestEntry(eldest)) {
        // 删除最老节点的操作
    }
}
```
3. afterNodeRemoval: 删除节点后调用，从链表中移除节点
```java
void afterNodeRemoval(Node<K,V> e) {
    // 从双向链表中移除节点
}
```
这些方法在HashMap的put、get等操作的不同阶段被调用，实现了对双向链表的维护。这种设计方式也体现了模板方法模式的应用，HashMap定义了操作框架，而LinkedHashMap通过重写这些回调方法实现自定义行为。"