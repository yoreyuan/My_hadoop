
**问题1**：编译中如果出现如下错误，可以这样解决：
```bash
Starting a Gradle Daemon (subsequent builds will be faster)
Parallel execution with configuration on demand is an incubating feature.
> Task :az-exec-util:linkMainExecutable FAILED
collect2: fatal error: cannot find 'ld'
compilation terminated.
FAILURE: Build failed with an exception.
* What went wrong:
Execution failed for task ':az-exec-util:linkMainExecutable'.
> A build operation failed.
      Linker failed while linking main.
  See the complete log at: file:///opt/azkaban/az-exec-util/build/tmp/linkMainExecutable/output.txt
* Try:
Run with --stacktrace option to get the stack trace. Run with --info or --debug option to get more log output. Run with --scan to get full insights.
* Get more help at https://help.gradle.org
BUILD FAILED in 11s
41 actionable tasks: 15 executed, 7 from cache, 19 up-to-date
```

查看ld，发现缺少ld命令：
```bash
[root@node1 azkaban]# whereis ld
 ld: /usr/bin/ld.bfd /usr/bin/ld.gold /usr/share/man/man1/ld.1.gz
 
 [root@node1 azkaban]#  ll /usr/bin/ld*
 -rwxr-xr-x 1 root root 1006216 Oct 30  2018 /usr/bin/ld.bfd
 -rwxr-xr-x 1 root root    5302 Jul  3 21:25 /usr/bin/ldd
 -rwxr-xr-x 1 root root 5354296 Oct 30  2018 /usr/bin/ld.gold
```

因此我们修复这个命令，先删除 binutils
```bash
#查看ld RPM信息
[root@node1 azkaban]# rpm -qf /usr/bin/ld
binutils-2.27-34.base.el7.x86_64
#删除这个，然后重新安装
[root@node1 azkaban]# rpm -e binutils --nodeps
admindir /var/lib/alternatives invalid
admindir /var/lib/alternatives invalid
```

再重新安装 binutils 
```bash
#下载。http://rpmfind.net/linux/rpm2html/search.php?query=binutils，例如CentOS7下载搜搜 el7.x86_64 找到对应rpm包下载
wget http://rpmfind.net/linux/centos/7.7.1908/os/x86_64/Packages/binutils-2.27-41.base.el7.x86_64.rpm
# 安装
[root@node1 ~]# rpm -ivh binutils-2.27-41.base.el7.x86_64.rpm
Preparing...                          ################################# [100%]
Updating / installing...
   1:binutils-2.27-34.base.el7        ################################# [100%]
admindir /var/lib/alternatives invalid
admindir /var/lib/alternatives invalid
admindir /var/lib/alternatives invalid
```

再次查看ld，如下：
```bash
[root@node1 ~]#  ll /usr/bin/ld*
-rwxr-xr-x 1 root root 1006216 Oct 30  2018 /usr/bin/ld.bfd
-rwxr-xr-x 1 root root    5302 Jul  3 21:25 /usr/bin/ldd
-rwxr-xr-x 1 root root 5354296 Oct 30  2018 /usr/bin/ld.gold
```

还是没有ld ，但是不要慌，其依赖其实是已经修复，如果没有这一步，直接修复软连是会报如下的错误：
```bash
error while loading shared libraries: libbfd-2.25.1-31.base.el7.so: cannot open shared object file: No such file or directory
```

此时只需要从其他CentOS系统中拷贝ld，并做个软连就可以了
```bash
#从其他节点（cdh3）拷贝 alternatives
[root@cdh3 ~]# scp -r /etc/alternatives/ root@node1:/etc/alternatives

# 软连修复
[root@node1 ~]#  cd /usr/bin
[root@node1 bin]# ln -s /etc/alternatives/ld  ld
# 再次查看。
[root@node1 bin]# ls -l ld*
lrwxrwxrwx 1 root root      20 Aug 19 19:00 ld -> /etc/alternatives/ld
-rwxr-xr-x 1 root root 1006216 Oct 30  2018 ld.bfd
-rwxr-xr-x 1 root root    5302 Jul  3 21:25 ldd
-rwxr-xr-x 1 root root 5354296 Oct 30  2018 ld.gold
```

