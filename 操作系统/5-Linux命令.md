**文件相关的命令**

1.  **Linux查看文件的命令有哪些?**
    *   `ls`: 列出目录内容。
    *   `cat`: 显示文件内容。
    *   `less`: 分页显示文件内容，可上下滚动。
    *   `more`: 与 `less` 类似，但功能较少。
    *   `head`: 显示文件开头部分。
    *   `tail`: 显示文件结尾部分。
    *   `file`: 判断文件类型。

2.  **Linux查看文件大小命令**
    *   `ls -lh`: 以人类可读的格式 (例如 KB, MB, GB) 显示文件大小。
    *   `du -sh <文件名或目录名>`: 显示指定文件或目录的总大小。`d` 表示 disk usage, `s` 表示 summary, `h` 表示 human-readable。

3.  **Linux查询当前所在目录的语句**
    *   `pwd`: Print Working Directory，打印当前工作目录的绝对路径。

4.  **Linux创建文件夹和文件的语句是什么?**
    *   创建文件夹: `mkdir <目录名>`
    *   创建空文件: `touch <文件名>` (如果文件已存在，则更新其时间戳)
    *   创建并编辑文件: 可以使用文本编辑器如 `vi <文件名>` 或 `nano <文件名>`，保存时即创建。

5.  **Linux如何删除一个文件?**
    *   `rm <文件名>`: 删除文件。使用 `-i` 选项会在删除前进行确认。

6.  **Linux如何删除一个目录（文件夹）?**
    *   `rmdir <空目录名>`: 删除空目录。
    *   `rm -r <目录名>`: 递归删除目录及其内容。
    *   `rm -rf <目录名>`: 强制递归删除目录及其内容，不会进行确认提示，请谨慎使用。

7.  **Linux怎么创建、复制、移动一个文件?**
    *   创建: `touch <文件名>` (空文件), `vi <文件名>` (创建并编辑)
    *   复制: `cp <源文件> <目标文件或目录>`
    *   移动/重命名: `mv <源文件或目录> <目标文件或目录>`

8.  **Linux cp 命令怎么复制整个文件夹?**
    *   `cp -r <源目录名> <目标目录名>`: `-r` (或 `-R`) 选项表示递归复制，即复制目录及其所有子目录和文件。

9.  **Linux如何文件重命名**
    *   `mv <旧文件名> <新文件名>`: `mv` 命令也用于重命名文件或目录。

10. **Linux 文件夹中如何查看最近被修改的文件?**
    *   `ls -lt`: `-l` 表示长列表格式，`-t` 表示按修改时间排序 (最新的在前)。
    *   `ls -ltr`: `-r` 表示反向排序，结合 `-t` 即按修改时间排序 (最旧的在前，最新的在后)。

11. **Linux怎么修改文件的权限?**
    *   `chmod <权限模式> <文件名或目录名>`
        *   数字模式: 例如 `chmod 755 <文件名>` (所有者读写执行，同组用户读执行，其他用户读执行)。
        *   符号模式: 例如 `chmod u+x <文件名>` (给文件所有者增加执行权限)，`chmod g-w <文件名>` (移除同组用户的写权限)，`chmod o=r <文件名>` (设置其他用户只有读权限)。

12. **chmod + x是给哪个属性赋予了权限**
    *   `chmod +x` 是给文件的所有者 (user)、所属组 (group) 和其他用户 (others) 都赋予执行权限。如果只想给特定用户类别赋予权限，需要明确指定，例如 `u+x` (用户), `g+x` (组), `o+x` (其他), `a+x` (所有)。

13. **Linux中如何查找一个文件**
    *   `find <路径> -name "<文件名模式>"`: 在指定路径下按文件名查找。例如 `find /home -name "*.txt"` 查找 /home 目录下所有 .txt 文件。
    *   `locate <文件名模式>`: 速度更快，但依赖于一个预先建立的数据库 (通常通过 `updatedb` 命令更新)。可能找不到最新创建的文件。
    *   `which <命令名>`: 查找可执行文件的路径。

14. **Linux 怎么查看实时滚动日志?**
    *   `tail -f <日志文件名>`: `-f` (follow) 选项会持续显示文件尾部的新增内容。
    *   `tail -F <日志文件名>`: 与 `-f` 类似，但在文件被重命名或重新创建后会尝试重新打开文件。

15. **现在有一个txt文件，如何查看后三行**
    *   `tail -n 3 <文件名.txt>`: `-n 3` 表示显示最后3行。

16. **查找一个字符串是否在文件中**
    *   `grep "<字符串>" <文件名>`: 在文件中搜索包含指定字符串的行。
    *   `grep -q "<字符串>" <文件名>; echo $?`: `-q` (quiet) 模式不输出匹配的行，只通过退出状态码表示是否找到。如果找到，退出状态码为0，否则为1。

17. **Linux怎么查找一个文件里的某一个字符串的位置**
    *   `grep -n "<字符串>" <文件名>`: `-n` 选项会显示匹配行及其行号。
    *   `grep -b "<字符串>" <文件名>`: `-b` (byte offset) 选项会显示匹配字符串在文件中的字节偏移量。

18. **Linux查看文件行数命令**
    *   `wc -l <文件名>`: `wc` (word count) 命令的 `-l` 选项用于统计行数。

19. **在一个目录下寻找含有字符串"admin"的文件**
    *   `grep -r "admin" <目录路径>`: `-r` (recursive) 选项表示递归搜索目录下的所有文件。
    *   `grep -rl "admin" <目录路径>`: `-l` 选项表示只列出包含匹配字符串的文件名，而不是匹配的行。
    *   `find <目录路径> -type f -exec grep -l "admin" {} \;`: `find` 命令找到普通文件 (`-type f`)，然后对每个找到的文件执行 `grep` 命令。

20. **统计一个文件中某一个字的次数**
    *   `grep -o "<字>" <文件名> | wc -l`:
        *   `grep -o "<字>" <文件名>`: `-o` 选项表示只输出匹配到的部分，每个匹配项占一行。
        *   `wc -l`: 统计行数，即字的出现次数。
    *   注意：这统计的是"字"作为一个独立的字符串出现的次数。如果要统计汉字字符，需要更复杂的处理，可能涉及到字符编码。

21. **如何替换一个文件中的字符串?**
    *   `sed -i 's/<旧字符串>/<新字符串>/g' <文件名>`:
        *   `sed`: Stream EDitor，流编辑器。
        *   `-i`: 直接修改文件内容 (in-place)。如果不加 `-i`，则只输出到标准输出。可以加一个后缀，如 `-i.bak`，表示修改前创建备份文件。
        *   `s/.../.../`:替换命令。
        *   `<旧字符串>`: 要被替换的字符串 (可以是正则表达式)。
        *   `<新字符串>`: 用来替换的新字符串。
        *   `g`: 全局替换 (global)，替换行内所有匹配项。如果没有 `g`，则只替换每行第一个匹配项。

**系统命令**

22. **Linux怎么查进程占用的CPU?**
    *   `top`: 实时显示系统中各个进程的资源占用情况，包括 CPU 使用率 ( `%CPU` 列)。按 `P` 可以按 CPU 使用率排序。
    *   `ps aux | sort -k3 -nr | head`:
        *   `ps aux`: 显示所有进程的详细信息。
        *   `sort -k3 -nr`: 按第3列 (CPU 使用率) 进行数字反向排序。
        *   `head`: 显示前几行 (CPU 占用最高的进程)。
    *   `htop`: 一个交互式的进程查看器，比 `top` 更友好。

23. **Linux怎么查看一个进程的进程号?**
    *   `pgrep <进程名>`: 根据进程名查找进程ID (PID)。
    *   `ps aux | grep <进程名>`: `ps` 列出所有进程，然后用 `grep` 过滤。注意这可能会匹配到 `grep` 进程本身。
    *   `pidof <进程名>`: 直接返回指定进程名的所有 PID。

24. **Linux怎么查进程的线程?**
    *   `top -H -p <PID>`: `-H` 选项会显示线程信息，`-p <PID>` 指定要查看的进程ID。
    *   `ps -T -p <PID>`: `-T` 选项会显示与指定 PID 关联的线程。
    *   `htop`: 在 `htop` 中，可以通过设置 (F2 Setup -> Display options -> Hide userland process threads 取消勾选) 来显示线程。

25. **Linux怎么查端口被哪个进程占用了?**
    *   `netstat -tulnp | grep <端口号>`:
        *   `netstat`: 显示网络连接、路由表、接口统计等信息。
        *   `-t`: TCP 协议。
        *   `-u`: UDP 协议。
        *   `-l`: 监听状态的套接字。
        *   `-n`: 以数字形式显示地址和端口号。
        *   `-p`: 显示占用端口的进程PID和名称 (通常需要 root 权限)。
    *   `ss -tulnp | grep <端口号>`: `ss` 是 `netstat` 的替代工具，功能更强大，效率更高。参数类似。
    *   `lsof -i :<端口号>`: `lsof` (List Open Files) 命令，`-i :<端口号>` 用于查找使用指定端口的进程。

26. **怎么查看一个进程占用的端口号?**
    *   `netstat -tulnp | grep <PID或进程名>`
    *   `ss -tulnp | grep <PID或进程名>`
    *   `lsof -i -P -n | grep <PID>`

27. **Linux怎么查tcp状态?**
    *   `netstat -ant`: 显示所有 TCP 连接的状态 (`-a` all, `-n` numeric, `-t` tcp)。
    *   `ss -tan`: `ss` 命令查看 TCP 连接状态。
    *   常见的TCP状态有：`LISTEN`, `SYN-SENT`, `SYN-RECEIVED`, `ESTABLISHED`, `FIN-WAIT-1`, `FIN-WAIT-2`, `CLOSE-WAIT`, `CLOSING`, `LAST-ACK`, `TIME-WAIT`, `CLOSED`。

28. **如何判断远端端口是否开启?**
    *   `telnet <远端IP地址> <端口号>`: 如果连接成功 (通常会看到一个空白屏幕或者一些服务信息)，则端口是开放的。如果连接失败或超时，则端口可能未开放或被防火墙阻止。
    *   `nc -zv <远端IP地址> <端口号>` (Netcat):
        *   `-z`: Zero-I/O mode (scanning).
        *   `-v`: Verbose.
    *   `nmap -p <端口号> <远端IP地址>`: `nmap` 是一个强大的网络扫描工具。

29. **Linux查看TCP连接数**
    *   `netstat -ant | grep ESTABLISHED | wc -l`: 统计已建立的 TCP 连接数。
    *   `ss -s`: 显示套接字统计信息，包括 TCP 连接总数。
    *   按状态统计: `netstat -ant | awk '{print $6}' | sort | uniq -c | sort -nr` (统计各种 TCP 状态的数量)

30. **Linux top 命令有哪些信息?**
    *   第一行: 系统时间、运行时间、登录用户数、系统负载 (load average)。
    *   第二行: 任务 (进程) 总数、运行中、睡眠中、停止、僵尸进程数。
    *   第三行 (`%Cpu(s)`): CPU 使用情况，包括用户空间(us)、系统空间(sy)、nice值改变的进程(ni)、空闲(id)、等待I/O(wa)、硬件中断(hi)、软件中断(si)、虚拟机(st)。
    *   第四行 (`KiB Mem`): 物理内存使用情况，包括总量、空闲、已用、缓存。
    *   第五行 (`KiB Swap`): 交换空间 (虚拟内存) 使用情况。
    *   进程列表区: PID (进程ID), USER (用户), PR (优先级), NI (Nice值), VIRT (虚拟内存), RES (常驻内存), SHR (共享内存), S (状态), %CPU (CPU占用率), %MEM (内存占用率), TIME+ (CPU累计时间), COMMAND (命令名)。

31. **CPU使用率达到100%吗? 怎么排查?**
    *   **是否能达到100%**: 单核CPU可以达到100%。对于多核CPU，`top` 命令中 `%Cpu(s)` 行显示的 `id` (空闲百分比) 为 0 时，表示所有CPU核心都被充分利用，总的CPU使用率会接近 `核心数 * 100%` (如果 `top` 显示的是每个核心的平均值则为100%，如果显示的是总和则会超过100%，具体看 `top` 的显示模式，通常按 `1` 可以切换)。
    *   **排查步骤**:
        1.  使用 `top` 命令查看是哪个进程占用了大量 CPU (按 `P` 排序)。
        2.  记录高CPU进程的 PID。
        3.  分析该进程:
            *   如果是应用进程:
                *   查看进程日志，是否有异常或大量请求。
                *   使用 `strace -p <PID>` 跟踪系统调用，看进程在做什么。
                *   使用 `perf top -p <PID>` (如果安装了 perf) 分析函数级别的CPU消耗。
                *   针对特定语言的分析工具 (如 Java 的 `jstack` 分析线程堆栈，Python 的 `py-spy`)。
            *   如果是系统进程: 可能是内核相关的任务，需要更深入分析。
            *   如果是未知进程: 可能是恶意软件。
        4.  查看 `vmstat` 或 `sar` 命令，观察上下文切换 (`cs`)、中断 (`in`) 等指标是否过高。
        5.  检查是否有死循环、资源竞争、低效算法等问题。
        6.  如果 `wa` (I/O wait) 很高，说明 CPU 在等待磁盘或网络 I/O，需要排查 I/O 瓶颈。

32. **怎么用top 命令查看是多少个CPU核心?**
    *   在 `top` 命令的输出界面，直接按数字键 `1`。这会切换显示模式，将 `%Cpu(s)` 行展开，为每个 CPU 核心单独显示一行使用情况 (例如 `Cpu0`, `Cpu1`, `Cpu2` ...)。数一下有多少行 `CpuX` 就知道有多少个核心了。

33. **Linux top结果CPU占用会超过100%吗?**
    *   是的，在多核CPU系统中，`top` 命令默认显示的 `%CPU` 列是单个进程占用的 CPU 百分比，相对于 *一个* CPU核心。如果一个进程是多线程的，并且这些线程并行运行在多个核心上，那么这个进程的 `%CPU` 总和可以超过 100%。
    *   例如，在一个4核CPU上，一个充分利用了2个核心的进程，其 `%CPU` 可能会显示为 200%。
    *   当按 `1` 切换到显示每个核心的模式时，每个核心的占用率最高是100%。

34. **Linux如何查看内存使用情况?**
    *   `free -h`: 以人类可读格式显示内存使用情况，包括物理内存 (Mem) 和交换空间 (Swap) 的总量、已用、空闲、共享、缓冲/缓存等。
        *   `total`: 总内存。
        *   `used`: 已用内存。
        *   `free`: 空闲内存。
        *   `shared`: 共享内存。
        *   `buff/cache`: 缓冲和缓存的内存。Linux 会将空闲内存用作文件系统缓存，以提高性能。这部分内存在需要时可以被回收。
        *   `available`: (较新版本的 `free` 命令提供) 真正可供应用程序使用的内存估算值，通常比 `free` 更能反映实际可用内存。
    *   `top`: 第四行 (`KiB Mem`) 显示内存使用情况。
    *   `vmstat`: 显示虚拟内存统计信息，包括内存使用。
    *   `/proc/meminfo`: 内核提供的内存详细信息文件，可以用 `cat /proc/meminfo` 查看。

35. **Linux怎么查看磁盘剩余空间**
    *   `df -h`: (`disk free`) 以人类可读格式显示文件系统的磁盘空间使用情况，包括总空间、已用空间、可用空间、使用百分比、挂载点。
    *   `df -T`: 同时显示文件系统类型。
    *   `du -sh <目录>`: 查看指定目录占用的磁盘空间。

36. **Linux 服务器当中如何查看负载情况? 通过什么指标进行判断?**
    *   **主要命令**:
        *   `uptime`: 显示系统运行时间、登录用户数以及过去1分钟、5分钟、15分钟的平均负载 (load average)。
        *   `top`: 第一行也会显示 load average。
    *   **核心指标: Load Average (平均负载)**
        *   Load average 表示在特定时间间隔内 (1分钟、5分钟、15分钟) 系统中处于运行队列 (正在运行或等待CPU) 和不可中断睡眠状态 (通常是等待I/O) 的平均进程数。
        *   **判断标准**:
            *   **理想情况**: Load average 应小于 CPU核心数。例如，一个4核CPU，如果load average 持续小于4，通常表示系统运行良好。
            *   **需要关注**: 如果 load average 持续等于或略大于 CPU 核心数，表示系统可能接近饱和，但可能仍可接受。
            *   **过载**: 如果 load average 持续远大于 CPU 核心数 (例如，是核心数的2倍或更多)，表示系统过载，进程需要排队等待CPU，响应会变慢。
    *   **其他辅助指标**:
        *   **CPU 使用率 (`top` 中的 `%Cpu(s)`)**: 查看 `us` (用户态), `sy` (内核态), `wa` (I/O等待)。高 `wa` 意味着I/O瓶颈可能是负载高的原因。
        *   **内存使用 (`free -h`, `top`)**: 内存不足会导致频繁使用交换空间，增加I/O，从而推高负载。
        *   **I/O 状态 (`iostat`, `vmstat`)**: 查看磁盘读写是否繁忙。
        *   **网络状态 (`netstat`, `ss`, `sar -n DEV`)**: 网络瓶颈也可能导致响应慢和负载感观高。
        *   **上下文切换 (`vmstat`, `pidstat -w`)**: 过多的上下文切换会消耗CPU资源。
