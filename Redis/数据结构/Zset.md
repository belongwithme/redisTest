# ZSet

# 核心概念与应用场景
## 什么是Zset
Zset 是 Redis 中的有序集合数据结构，它将每个成员与一个分数（score）关联，并根据分数对成员进行排序。
## 请简述一下 Redis ZSet 的核心特性是什么？它与其他集合类型（如 Set、List）最本质的区别在哪里？
Redis ZSet（有序集合）最核心的特性是它集合中的每个元素都关联着一个浮点数类型的分数（score），并且 ZSet 内部是根据这个分数进行排序的。成员（member）是唯一的，但分数（score）可以重复。
这与 Redis 的其他集合类型有着本质区别：
- Set：无序，只存储唯一的成员，没有分数的概念。
- List：有序，但这个“序”是按照元素插入的先后顺序决定的，并且允许成员重复。

ZSet 则结合了 Set 的成员唯一性和基于分数的排序能力。

## 结合你实际使用或了解的场景，具体谈谈在哪些业务需求下 ZSet 是一个非常合适的选择？举例说明 ZSet 如何解决这些问题的。
ZSet 非常适用于需要根据某个权重、分数或时间戳进行排序和范围查找的场景。常见的例子包括：
排行榜（Leaderboards）： 比如游戏积分榜、用户贡献榜。用户的 ID 或昵称作为 member，积分或贡献值作为 score。通过 ZSet 可以轻松实现：
- ZADD user:1 100 John：添加用户 John，分数为 100。
- ZREVRANGE leaderboard 0 9 WITHSCORES：获取排名前 10 的用户及其分数。
- ZRANK leaderboard John：查询用户 John 的排名（从 0 开始）。
- ZSCORE leaderboard John：查询用户 John 的分数。
带权重的任务队列/消息队列： 任务 ID 作为 member，优先级或计划执行时间戳作为 score。可以方便地取出优先级最高或最早需要执行的任务。
- ZRANGEBYSCORE task_queue -inf (current_time：取出所有到期或需要立即执行的任务。
- 时间轴（Timeline）： 比如社交应用中用户关注的人发布的内容，内容 ID 作为 member，发布时间戳作为 score。可以方便地按时间倒序拉取最新的内容。
- 范围查找： 比如查找某个价格区间、年龄段或地理位置范围内的商品或用户。

选择 ZSet 的主要原因是它能高效地支持按分数排序和按分数范围查找 (ZRANGEBYSCORE, ZREVRANGEBYSCORE)，同时也能快速地根据成员查找分数 (ZSCORE) 和获取排名 (ZRANK, ZREVRANK)。这些操作在其他数据结构中要么无法直接实现，要么效率较低。

## 底层实现
ZSet 主要有两种底层编码方式：
- ziplist (压缩列表)：当 ZSet 中存储的元素数量较少，并且每个元素（member 和 score）占用的空间也较小时，Redis 会采用这种编码。ziplist 是一种紧凑的、连续存储的数据结构，非常节省内存。但缺点是，插入或删除元素可能需要移动后续元素，导致时间复杂度在最坏情况下为 O(N)。
- skiplist (跳跃表) + dict (哈希表/字典)：当元素数量或元素大小超过阈值时，Redis 会采用这种编码。
   - skiplist：用于按 score 排序存储所有元素，并提供高效的范围查询（平均 O(log N)）。跳跃表通过多层链表结构，实现了类似二分查找的效率。
   - dict：用于存储从 member到 score 的映射。这使得通过 member 查找 score 的操作 (ZSCORE) 具有 O(1) 的平均时间复杂度。

## 有几种编码格式
- ziplist : 
ziplist编码的Zset使用压缩列表作为底层数据结构。
- skiplist+hashtable:
skiplist编码的Zset使用跳表和哈希表作为底层数据结构。

为了结合两者的优势，优化不同类型的查询：
skiplist 的优势在于它能够高效地（平均 O(log N)）进行基于分数的排序和范围查找（如 ZRANGE, ZRANGEBYSCORE）。它通过维护有序链表和多级索引来实现这一点。
dict (哈希表) 的优势在于它能够以平均 O(1) 的时间复杂度快速查找一个 key 对应的值。在这里，它存储了 member 到 score 的映射。
如果只用 skiplist，虽然也能通过遍历（利用跳表的索引加速）找到指定 member 的 score，但其平均时间复杂度会是 O(log N)，不如 dict 的 O(1) 高效。ZSCORE 是一个常用操作，O(1) 的效率至关重要。

## ziplist和skiplist的编码条件
- 当Zset的元素数量小于128个,且每个元素的键值对长度小于64字节时,使用ziplist编码.
- 当Zset的元素数量大于128个,或每个元素的键值对长度大于64字节时,使用skiplist编码.
这个转换过程需要重新构建 skiplist 和 dict，并将 ziplist 中的数据迁移过去，会消耗一定的 CPU 时间和内存，带来一次性的性能开销。但转换完成后，后续操作将受益于 skiplist 和 dict 带来的更高效率，尤其是在数据量较大时。需要注意的是，编码转换是单向的，一旦转换成 skiplist，即使后来元素数量减少，也不会自动转换回 ziplist。













