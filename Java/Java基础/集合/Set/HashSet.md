@[TOC](HashSet)
## 能简单介绍一下HashSet吗？它的特点是什么
HashSet是Java集合框架中Set接口的一个重要实现，它基于HashMap实现。作为一个Set集合，它的核心特点是不允许存储重复元素。
HashSet的主要特点有：
- 唯一性：不允许有重复元素，如果尝试添加重复元素，add方法会返回false
- 无序性：不保证元素的存储顺序，也不保证顺序随时间保持不变
- 允许null值：可以包含一个null元素
- 高效操作：基本操作（添加、删除、包含）的平均时间复杂度为O(1)
- 非线程安全：多线程环境下需要外部同步

从内部实现来看，HashSet实际上是一个HashMap的包装器。元素被存储为HashMap的键，而值则是一个常量对象。这种设计充分利用了HashMap键的唯一性特征，同时避免了重复实现类似功能。

## HashSet是如何实现的？它是如何保证元素不重复的
HashSet的实现非常巧妙，它内部完全依赖于HashMap：
```java
// HashSet的核心字段
private transient HashMap<E,Object> map;

// 所有值使用的共享对象
private static final Object PRESENT = new Object();
```
当我们向HashSet添加元素时，实际上是将该元素作为键，一个名为PRESENT的固定对象作为值，添加到内部的HashMap中：
```java
// add方法的实现
public boolean add(E e) {
    return map.put(e, PRESENT) == null;
}
```
HashSet如何保证元素不重复？这个问题的关键在于两点：
hashCode()方法：首先通过元素的hashCode值决定元素在哈希表中的存储位置
equals()方法：当hashCode相同时，通过equals方法判断元素是否真正相等
这个机制直接继承自HashMap的键唯一性机制。当我们调用add方法时：
如果集合中不存在相等的元素（根据hashCode和equals判断），元素被添加，方法返回true
如果集合中已存在相等的元素，则不添加，方法返回false
这就是为什么在使用自定义对象作为HashSet元素时，正确重写hashCode和equals方法非常重要。如果只重写了equals而没有重写hashCode，可能导致相等的对象被重复添加，因为它们可能散列到不同的位置。
这种设计也说明了为什么HashSet的性能如此高效 - 它直接利用了HashMap成熟的哈希表实现。"

## HashSet与其他Set实现(如TreeSet、LinkedHashSet)相比，有什么优缺点？在什么场景下选择HashSet
Java提供了几种不同的Set实现，每种都有其独特的优缺点和适用场景:


| 特性 | HashSet | LinkedHashSet | TreeSet |
|------|---------|---------------|---------|
| 底层实现 | HashMap | LinkedHashMap | TreeMap |
| 元素顺序 | 无序 | 插入顺序 | 自然排序或比较器排序 |
| 性能(添加/删除/查找) | O(1) | O(1) | O(log n) |
| 内存占用 | 低 | 中 | 高 |
| 是否允许null | 允许一个 | 允许一个 | 不允许 |
| 迭代性能 | 较好 | 最好 | 较好 |
| 适用场景 | 快速查找、不关心顺序 | 需要记住插入顺序 | 需要有序数据、范围查询 |
我一般根据这些因素选择合适的Set实现：
1. 是否需要排序：
    - 需要自然排序或自定义排序：TreeSet
    - 需要插入顺序：LinkedHashSet
    - 不需要排序：HashSet
2. 性能优先级：
    - HashSet > LinkedHashSet > TreeSet
3. 操作类型：
    - 范围操作多：TreeSet
    - 插入/删除/查找频繁：HashSet
    
在我的实际项目中，大多数情况下默认使用HashSet，因为它性能最好。只有当明确需要顺序时，才会选择LinkedHashSet或TreeSet。