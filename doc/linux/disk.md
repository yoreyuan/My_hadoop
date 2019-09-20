磁盘
======

# 压缩某个目录
```bash
# 查看磁盘信息，例如想减小 /dev/mapper/v01-cloudera对应的Mounted on（opt）点的大小
df -h

# 卸载 挂载的分区
umount /home

#检查VolGroup-lv_home文件的错误性，-f 即使文件系统没有错误迹象，仍强制地检查正确性。
e2fsck -f /dev/mapper/VolGroup-lv_home
# 压缩大小，调整home大小为20G
resize2fs -p /dev/mapper/VolGroup-lv_home 20G

# 装载
mount /home
df -h

#划分20G外面的剩余空间
#lvreduce -L 20G /dev/mapper/VolGroup-lv_home

```



# Centos磁盘扩容
```bash
# 可查看可以扩展的目录，ls /dev/mapper
df -h

fdisk -l
fdisk -l /dev/xvdb

```

1. 创建新分区：fdisk /dev/xvdb
```
Command (m for help): n              #n创建新分区
Select (default p): p                #p分区类型
Partition number (3,4, default 3):   #直接回车
First sector (83886080-167772159, default 83886080):    #直接回车
Last sector, +sectors or +size{K,M,G} (83886080-167772159, default 167772159):    #直接回车
t       # 磁盘类型为 ‘Linux LVM’
8e
Command (m for help): w              #w保存并退出

# 如果提示如下则需要重启电脑
WARNING: Re-reading the partition table failed with error 16: Device or resource busy.
The kernel still uses the old table. The new table will be used at
the next reboot or after you run partprobe(8) or kpartx(8)

```

2. 查看分区`fdisk -l`，可以在分区列表中查看到刚才新建的分区

3. 创建物理卷并扩展卷组
```bash
#查看卷组名称及大小，需要记下 VG Name
vgdisplay

# 创建卷。 2步骤查看到的新 Device 
pvcreate /dev/sda3

# 扩展卷组 centos 为上面记录的VG Name
vgextend centos /dev/sda3

# 查看物理卷
pvdisplay

```

4. 扩展逻辑卷
```bash
# 查看磁盘分区大小
df -h

# 查看当前逻辑卷的名称及空间大小
lvdisplay

#扩展分区。数值见查看卷组名称及大小vgdisplay
#lvextend -l +10G /dev/centos/root
lvextend /dev/centos/root /dev/sda3

# 调整文件系统大小 ext2、ext3、ext4文件系统
resize2fs -f /dev/centos/root
#调整文件系统大小 xfs文件系统
xfs_growfs /dev/centos/root

# 查询分区大小
df -h
lvscan

```


# 实战
目标，将新加入的500的磁盘空间，分配42GB到/var，余下全部分配到根节点。解决挂载点使用率过高的警报

```
# 查看系统磁盘空间的使用情况。
[root@cdh1 ~]# df -h
文件系统                       容量  已用  可用 已用% 挂载点
/dev/mapper/VolGroup-root       25G   16G  9.4G   62% /
devtmpfs                        16G     0   16G    0% /dev
tmpfs                           16G     0   16G    0% /dev/shm
tmpfs                           16G  201M   16G    2% /run
tmpfs                           16G     0   16G    0% /sys/fs/cgroup
/dev/xvda1                     497M  254M  243M   52% /boot
/dev/mapper/VolGroup00-consul 1014M   33M  982M    4% /consul
/dev/mapper/VolGroup-var       8.0G  2.0G  6.1G   25% /var
tmpfs                          3.2G     0  3.2G    0% /run/user/0
tmpfs                          3.2G     0  3.2G    0% /run/user/99
cm_processes                    16G   21M   16G    1% /run/cloudera-scm-agent/process
[root@cdh1 ~]# df -hl
文件系统                       容量  已用  可用 已用% 挂载点
/dev/mapper/VolGroup-root       25G   16G  9.4G   62% /
devtmpfs                        16G     0   16G    0% /dev
tmpfs                           16G     0   16G    0% /dev/shm
tmpfs                           16G  201M   16G    2% /run
tmpfs                           16G     0   16G    0% /sys/fs/cgroup
/dev/xvda1                     497M  254M  243M   52% /boot
/dev/mapper/VolGroup00-consul 1014M   33M  982M    4% /consul
/dev/mapper/VolGroup-var       8.0G  2.0G  6.1G   25% /var
tmpfs                          3.2G     0  3.2G    0% /run/user/0
tmpfs                          3.2G     0  3.2G    0% /run/user/99
cm_processes                    16G   21M   16G    1% /run/cloudera-scm-agent/process


[root@cdh1 ~]# fdisk -l

磁盘 /dev/xvdb：493.9 GB, 493921239040 字节，964689920 个扇区
Units = 扇区 of 1 * 512 = 512 bytes
扇区大小(逻辑/物理)：512 字节 / 512 字节
I/O 大小(最小/最佳)：512 字节 / 512 字节

磁盘 /dev/xvda：42.9 GB, 42949672960 字节，83886080 个扇区
Units = 扇区 of 1 * 512 = 512 bytes
扇区大小(逻辑/物理)：512 字节 / 512 字节
I/O 大小(最小/最佳)：512 字节 / 512 字节
磁盘标签类型：dos
磁盘标识符：0x0003755b

    设备 Boot      Start         End      Blocks   Id  System
/dev/xvda1   *        2048     1026047      512000   83  Linux
/dev/xvda2         1026048     3131391     1052672   8e  Linux LVM
/dev/xvda3         3131392    83886079    40377344   8e  Linux LVM

磁盘 /dev/mapper/VolGroup-root：26.3 GB, 26315063296 字节，51396608 个扇区
Units = 扇区 of 1 * 512 = 512 bytes
扇区大小(逻辑/物理)：512 字节 / 512 字节
I/O 大小(最小/最佳)：512 字节 / 512 字节

磁盘 /dev/mapper/VolGroup-swap：6438 MB, 6438256640 字节，12574720 个扇区
Units = 扇区 of 1 * 512 = 512 bytes
扇区大小(逻辑/物理)：512 字节 / 512 字节
I/O 大小(最小/最佳)：512 字节 / 512 字节

磁盘 /dev/mapper/VolGroup00-consul：1073 MB, 1073741824 字节，2097152 个扇区
Units = 扇区 of 1 * 512 = 512 bytes
扇区大小(逻辑/物理)：512 字节 / 512 字节
I/O 大小(最小/最佳)：512 字节 / 512 字节

磁盘 /dev/mapper/VolGroup-var：8589 MB, 8589934592 字节，16777216 个扇区
Units = 扇区 of 1 * 512 = 512 bytes
扇区大小(逻辑/物理)：512 字节 / 512 字节
I/O 大小(最小/最佳)：512 字节 / 512 字节



# 获取指定的磁盘的信息
[root@cdh1 ~]# fdisk -l /dev/xvdb

磁盘 /dev/xvdb：493.9 GB, 493921239040 字节，964689920 个扇区
Units = 扇区 of 1 * 512 = 512 bytes
扇区大小(逻辑/物理)：512 字节 / 512 字节
I/O 大小(最小/最佳)：512 字节 / 512 字节

```


针对`/dev/xvdb`，将42GB空间扩展到`/var`
```
#主要对磁盘/dev/xvdb进行分区
[root@cdh1 ~]# fdisk  /dev/xvdb
欢迎使用 fdisk (util-linux 2.23.2)。

更改将停留在内存中，直到您决定将更改写入磁盘。
使用写入命令前请三思。

Device does not contain a recognized partition table
使用磁盘标识符 0xc121d7db 创建新的 DOS 磁盘标签。

命令(输入 m 获取帮助)：m
命令操作
   a   toggle a bootable flag
   b   edit bsd disklabel
   c   toggle the dos compatibility flag
   d   delete a partition
   g   create a new empty GPT partition table
   G   create an IRIX (SGI) partition table
   l   list known partition types
   m   print this menu
   n   add a new partition
   o   create a new empty DOS partition table
   p   print the partition table
   q   quit without saving changes
   s   create a new empty Sun disklabel
   t   change a partition's system id
   u   change display/entry units
   v   verify the partition table
   w   write table to disk and exit
   x   extra functionality (experts only)
命令(输入 m 获取帮助)：n
Partition type:
   p   primary (0 primary, 0 extended, 4 free)
   e   extended
Select (default p): p
分区号 (1-4，默认 1)：
起始 扇区 (2048-964689919，默认为 2048)：
将使用默认值 2048
Last 扇区, +扇区 or +size{K,M,G} (2048-964689919，默认为 964689919)：+42G
分区 1 已设置为 Linux 类型，大小设为 42 GiB

命令(输入 m 获取帮助)：t
已选择分区 1
Hex 代码(输入 L 列出所有代码)：L

 0  空              24  NEC DOS         81  Minix / 旧 Linu bf  Solaris
 1  FAT12           27  隐藏的 NTFS Win 82  Linux 交换 / So c1  DRDOS/sec (FAT-
 2  XENIX root      39  Plan 9          83  Linux           c4  DRDOS/sec (FAT-
 3  XENIX usr       3c  PartitionMagic  84  OS/2 隐藏的 C:  c6  DRDOS/sec (FAT-
 4  FAT16 <32M      40  Venix 80286     85  Linux 扩展      c7  Syrinx
 5  扩展            41  PPC PReP Boot   86  NTFS 卷集       da  非文件系统数据
 6  FAT16           42  SFS             87  NTFS 卷集       db  CP/M / CTOS / .
 7  HPFS/NTFS/exFAT 4d  QNX4.x          88  Linux 纯文本    de  Dell 工具
 8  AIX             4e  QNX4.x 第2部分  8e  Linux LVM       df  BootIt
 9  AIX 可启动      4f  QNX4.x 第3部分  93  Amoeba          e1  DOS 访问
 a  OS/2 启动管理器 50  OnTrack DM      94  Amoeba BBT      e3  DOS R/O
 b  W95 FAT32       51  OnTrack DM6 Aux 9f  BSD/OS          e4  SpeedStor
 c  W95 FAT32 (LBA) 52  CP/M            a0  IBM Thinkpad 休 eb  BeOS fs
 e  W95 FAT16 (LBA) 53  OnTrack DM6 Aux a5  FreeBSD         ee  GPT
 f  W95 扩展 (LBA)  54  OnTrackDM6      a6  OpenBSD         ef  EFI (FAT-12/16/
10  OPUS            55  EZ-Drive        a7  NeXTSTEP        f0  Linux/PA-RISC
11  隐藏的 FAT12    56  Golden Bow      a8  Darwin UFS      f1  SpeedStor
12  Compaq 诊断     5c  Priam Edisk     a9  NetBSD          f4  SpeedStor
14  隐藏的 FAT16 <3 61  SpeedStor       ab  Darwin 启动     f2  DOS 次要
16  隐藏的 FAT16    63  GNU HURD or Sys af  HFS / HFS+      fb  VMware VMFS
17  隐藏的 HPFS/NTF 64  Novell Netware  b7  BSDI fs         fc  VMware VMKCORE
18  AST 智能睡眠    65  Novell Netware  b8  BSDI swap       fd  Linux raid 自动
1b  隐藏的 W95 FAT3 70  DiskSecure 多启 bb  Boot Wizard 隐  fe  LANstep
1c  隐藏的 W95 FAT3 75  PC/IX           be  Solaris 启动    ff  BBT
1e  隐藏的 W95 FAT1 80  旧 Minix
Hex 代码(输入 L 列出所有代码)：8e
已将分区“Linux”的类型更改为“Linux LVM”

命令(输入 m 获取帮助)：w
The partition table has been altered!

Calling ioctl() to re-read partition table.
正在同步磁盘。

# 没有提示WARNING: Re-reading the partition table failed with error 16: 设备或资源忙.。则不用重启系统

# 再次查看磁盘信息，可以和前面的对比，很明显多了一行。
[root@cdh1 ~]# fdisk -l

磁盘 /dev/xvdb：493.9 GB, 493921239040 字节，964689920 个扇区
Units = 扇区 of 1 * 512 = 512 bytes
扇区大小(逻辑/物理)：512 字节 / 512 字节
I/O 大小(最小/最佳)：512 字节 / 512 字节
磁盘标签类型：dos
磁盘标识符：0xc121d7db

    设备 Boot      Start         End      Blocks   Id  System
/dev/xvdb1            2048    88082431    44040192   8e  Linux LVM

磁盘 /dev/xvda：42.9 GB, 42949672960 字节，83886080 个扇区
Units = 扇区 of 1 * 512 = 512 bytes
扇区大小(逻辑/物理)：512 字节 / 512 字节
I/O 大小(最小/最佳)：512 字节 / 512 字节
磁盘标签类型：dos
磁盘标识符：0x0003755b

    设备 Boot      Start         End      Blocks   Id  System
/dev/xvda1   *        2048     1026047      512000   83  Linux
/dev/xvda2         1026048     3131391     1052672   8e  Linux LVM
/dev/xvda3         3131392    83886079    40377344   8e  Linux LVM

磁盘 /dev/mapper/VolGroup-root：26.3 GB, 26315063296 字节，51396608 个扇区
Units = 扇区 of 1 * 512 = 512 bytes
扇区大小(逻辑/物理)：512 字节 / 512 字节
I/O 大小(最小/最佳)：512 字节 / 512 字节


磁盘 /dev/mapper/VolGroup-swap：6438 MB, 6438256640 字节，12574720 个扇区
Units = 扇区 of 1 * 512 = 512 bytes
扇区大小(逻辑/物理)：512 字节 / 512 字节
I/O 大小(最小/最佳)：512 字节 / 512 字节


磁盘 /dev/mapper/VolGroup-var：8589 MB, 8589934592 字节，16777216 个扇区
Units = 扇区 of 1 * 512 = 512 bytes
扇区大小(逻辑/物理)：512 字节 / 512 字节
I/O 大小(最小/最佳)：512 字节 / 512 字节


磁盘 /dev/mapper/VolGroup00-consul：1073 MB, 1073741824 字节，2097152 个扇区
Units = 扇区 of 1 * 512 = 512 bytes
扇区大小(逻辑/物理)：512 字节 / 512 字节
I/O 大小(最小/最佳)：512 字节 / 512 字节


#查看卷组名称及大小，需要记下 VG Name。例如这里的是 VolGroup，后面会用到这个卷组名
[root@cdh1 ~]# vgdisplay
  --- Volume group ---
  VG Name               VolGroup
  System ID
  Format                lvm2
  Metadata Areas        1
  Metadata Sequence No  4
  VG Access             read/write
  VG Status             resizable
  MAX LV                0
  Cur LV                3
  Open LV               3
  Max PV                0
  Cur PV                1
  Act PV                1
  VG Size               38.50 GiB
  PE Size               4.00 MiB
  Total PE              9857
  Alloc PE / Size       9857 / 38.50 GiB
  Free  PE / Size       0 / 0
  VG UUID               faVyy5-8PQq-MN9X-RraY-LOgj-g9Iq-KQrMsg

  --- Volume group ---
  VG Name               VolGroup00
  System ID
  Format                lvm2
  Metadata Areas        1
  Metadata Sequence No  2
  VG Access             read/write
  VG Status             resizable
  MAX LV                0
  Cur LV                1
  Open LV               1
  Max PV                0
  Cur PV                1
  Act PV                1
  VG Size               1.00 GiB
  PE Size               4.00 MiB
  Total PE              256
  Alloc PE / Size       256 / 1.00 GiB
  Free  PE / Size       0 / 0
  VG UUID               fo1adj-WN2W-1bqp-6NaD-42Xc-vHeT-6uSPAR

# 创建卷。 从前面我们已经创建了一个新的分区：/dev/xvdb1
# 执行时如果报： Device /dev/xvdb2 not found.。此时需要重启系统：reboot
[root@cdh1 ~]# pvcreate /dev/xvdb1
  Physical volume "/dev/xvdb1" successfully created.
  
# 扩展卷组 VolGroup
[root@cdh1 ~]# vgextend  VolGroup  /dev/xvdb1
  Volume group "VolGroup" successfully extended
  
# 查看物理卷。注意看PV Name为/dev/xvdb1的信息，42GB已经创建出来
[root@cdh1 ~]# pvdisplay
  --- Physical volume ---
  PV Name               /dev/xvda3
  VG Name               VolGroup
  PV Size               <38.51 GiB / not usable 3.00 MiB
  Allocatable           yes (but full)
  PE Size               4.00 MiB
  Total PE              9857
  Free PE               0
  Allocated PE          9857
  PV UUID               uQLUzq-WiZg-YdPp-KNq1-cVdw-aTBc-ey3YWy

  --- Physical volume ---
  PV Name               /dev/xvdb1
  VG Name               VolGroup
  PV Size               42.00 GiB / not usable 4.00 MiB
  Allocatable           yes
  PE Size               4.00 MiB
  Total PE              10751
  Free PE               10751
  Allocated PE          0
  PV UUID               JagRJV-FXfW-BcWx-vweZ-kPQz-7goT-COX2MH

  --- Physical volume ---
  PV Name               /dev/xvda2
  VG Name               VolGroup00
  PV Size               1.00 GiB / not usable 4.00 MiB
  Allocatable           yes (but full)
  PE Size               4.00 MiB
  Total PE              256
  Free PE               0
  Allocated PE          256
  PV UUID               w96LS0-UNYi-57Um-FBIO-U7uv-Csqi-2j91yP


[root@cdh1 ~]# df -h
文件系统                       容量  已用  可用 已用% 挂载点
/dev/mapper/VolGroup-root       25G   16G  9.5G   62% /
devtmpfs                        16G     0   16G    0% /dev
tmpfs                           16G     0   16G    0% /dev/shm
tmpfs                           16G  8.7M   16G    1% /run
tmpfs                           16G     0   16G    0% /sys/fs/cgroup
/dev/mapper/VolGroup-var       8.0G  2.0G  6.1G   25% /var
/dev/xvda1                     497M  267M  231M   54% /boot
/dev/mapper/VolGroup00-consul 1014M   33M  982M    4% /consul
tmpfs                          3.2G     0  3.2G    0% /run/user/1001
cm_processes                    16G     0   16G    0% /run/cloudera-scm-agent/process
tmpfs                          3.2G     0  3.2G    0% /run/user/0

# 查看当前逻辑卷的名称及空间大小。
[root@cdh1 ~]# lvdisplay
  --- Logical volume ---
  LV Path                /dev/VolGroup/root
  LV Name                root
  VG Name                VolGroup
  LV UUID                2f6zN4-wlsI-6JKt-9B5d-fDDm-cudJ-gus6RB
  LV Write Access        read/write
  LV Creation host, time localhost, 2018-03-29 10:32:45 +0800
  LV Status              available
  # open                 1
  LV Size                <24.51 GiB
  Current LE             6274
  Segments               1
  Allocation             inherit
  Read ahead sectors     auto
  - currently set to     8192
  Block device           253:0

  --- Logical volume ---
  LV Path                /dev/VolGroup/var
  LV Name                var
  VG Name                VolGroup
  LV UUID                Dzr2iB-N6f3-Edv0-bXOj-OTDI-pibv-jCTuCY
  LV Write Access        read/write
  LV Creation host, time localhost, 2018-03-29 10:32:46 +0800
  LV Status              available
  # open                 1
  LV Size                8.00 GiB
  Current LE             2048
  Segments               1
  Allocation             inherit
  Read ahead sectors     auto
  - currently set to     8192
  Block device           253:2

  --- Logical volume ---
  LV Path                /dev/VolGroup/swap
  LV Name                swap
  VG Name                VolGroup
  LV UUID                tEJ8sn-RGQr-FSvs-CL9K-b4Qi-OjT2-MeE7Av
  LV Write Access        read/write
  LV Creation host, time localhost, 2018-03-29 10:32:47 +0800
  LV Status              available
  # open                 2
  LV Size                <6.00 GiB
  Current LE             1535
  Segments               1
  Allocation             inherit
  Read ahead sectors     auto
  - currently set to     8192
  Block device           253:1

  --- Logical volume ---
  LV Path                /dev/VolGroup00/consul
  LV Name                consul
  VG Name                VolGroup00
  LV UUID                HicSHS-oJaI-GCdQ-4EUd-pLdN-7rkp-UkwMOE
  LV Write Access        read/write
  LV Creation host, time localhost, 2018-03-29 10:32:51 +0800
  LV Status              available
  # open                 1
  LV Size                1.00 GiB
  Current LE             256
  Segments               1
  Allocation             inherit
  Read ahead sectors     auto
  - currently set to     8192
  Block device           253:3

# 扩展分区
[root@cdh1 ~]# lvextend /dev/VolGroup/var /dev/xvdb1
  Size of logical volume VolGroup/var changed from 8.00 GiB (2048 extents) to <50.00 GiB (12799 extents).
  Logical volume VolGroup/var successfully resized.

# 调整文件系统大小
#[root@cdh1 ~]# resize2fs -f /dev/VolGroup/var
#resize2fs 1.42.9 (28-Dec-2013)
#resize2fs: Bad magic number in super-block 当尝试打开 /dev/VolGroup/var 时
#找不到有效的文件系统超级块.
[root@cdh1 ~]# xfs_growfs /dev/VolGroup/var
meta-data=/dev/mapper/VolGroup-var isize=512    agcount=4, agsize=524288 blks
         =                       sectsz=512   attr=2, projid32bit=1
         =                       crc=1        finobt=0 spinodes=0
data     =                       bsize=4096   blocks=2097152, imaxpct=25
         =                       sunit=0      swidth=0 blks
naming   =version 2              bsize=4096   ascii-ci=0 ftype=1
log      =internal               bsize=4096   blocks=2560, version=2
         =                       sectsz=512   sunit=0 blks, lazy-count=1
realtime =none                   extsz=4096   blocks=0, rtextents=0
data blocks changed from 2097152 to 13106176
您在 /var/spool/mail/root 中有新邮件

# 查看分区
[root@cdh1 ~]# lvscan
  ACTIVE            '/dev/VolGroup/root' [<24.51 GiB] inherit
  ACTIVE            '/dev/VolGroup/var' [<50.00 GiB] inherit
  ACTIVE            '/dev/VolGroup/swap' [<6.00 GiB] inherit
  ACTIVE            '/dev/VolGroup00/consul' [1.00 GiB] inherit

# 用df查看系统磁盘信息
[root@cdh1 ~]# df -h
文件系统                       容量  已用  可用 已用% 挂载点
/dev/mapper/VolGroup-root       25G   16G  9.5G   62% /
devtmpfs                        16G     0   16G    0% /dev
tmpfs                           16G     0   16G    0% /dev/shm
tmpfs                           16G  8.7M   16G    1% /run
tmpfs                           16G     0   16G    0% /sys/fs/cgroup
/dev/mapper/VolGroup-var        50G  2.0G   49G    4% /var
/dev/xvda1                     497M  267M  231M   54% /boot
/dev/mapper/VolGroup00-consul 1014M   33M  982M    4% /consul
tmpfs                          3.2G     0  3.2G    0% /run/user/1001
cm_processes                    16G     0   16G    0% /run/cloudera-scm-agent/process
tmpfs                          3.2G     0  3.2G    0% /run/user/0

```

继续，将磁盘剩余空间分配给根节点
```
[root@cdh1 lib]# fdisk  /dev/xvdb
欢迎使用 fdisk (util-linux 2.23.2)。

更改将停留在内存中，直到您决定将更改写入磁盘。
使用写入命令前请三思。


命令(输入 m 获取帮助)：n
Partition type:
   p   primary (1 primary, 0 extended, 3 free)
   e   extended
Select (default p): p
分区号 (2-4，默认 2)：
起始 扇区 (88082432-964689919，默认为 88082432)：
将使用默认值 88082432
Last 扇区, +扇区 or +size{K,M,G} (88082432-964689919，默认为 964689919)：
将使用默认值 964689919
分区 2 已设置为 Linux 类型，大小设为 418 GiB

命令(输入 m 获取帮助)：t
分区号 (1,2，默认 2)：2
Hex 代码(输入 L 列出所有代码)：8e
已将分区“Linux”的类型更改为“Linux LVM”

命令(输入 m 获取帮助)：w
The partition table has been altered!

Calling ioctl() to re-read partition table.

WARNING: Re-reading the partition table failed with error 16: 设备或资源忙.
The kernel still uses the old table. The new table will be used at
the next reboot or after you run partprobe(8) or kpartx(8)
正在同步磁盘。


#再次查看磁盘信息。或发现多了/dev/xvdb1、/dev/xvdb2
[root@cdh1 lib]# fdisk -l

磁盘 /dev/xvdb：493.9 GB, 493921239040 字节，964689920 个扇区
Units = 扇区 of 1 * 512 = 512 bytes
扇区大小(逻辑/物理)：512 字节 / 512 字节
I/O 大小(最小/最佳)：512 字节 / 512 字节
磁盘标签类型：dos
磁盘标识符：0xc121d7db

    设备 Boot      Start         End      Blocks   Id  System
/dev/xvdb1            2048    88082431    44040192   8e  Linux LVM
/dev/xvdb2        88082432   964689919   438303744   8e  Linux LVM

磁盘 /dev/xvda：42.9 GB, 42949672960 字节，83886080 个扇区
Units = 扇区 of 1 * 512 = 512 bytes
扇区大小(逻辑/物理)：512 字节 / 512 字节
I/O 大小(最小/最佳)：512 字节 / 512 字节
磁盘标签类型：dos
磁盘标识符：0x0003755b

    设备 Boot      Start         End      Blocks   Id  System
/dev/xvda1   *        2048     1026047      512000   83  Linux
/dev/xvda2         1026048     3131391     1052672   8e  Linux LVM
/dev/xvda3         3131392    83886079    40377344   8e  Linux LVM

磁盘 /dev/mapper/VolGroup-root：26.3 GB, 26315063296 字节，51396608 个扇区
Units = 扇区 of 1 * 512 = 512 bytes
扇区大小(逻辑/物理)：512 字节 / 512 字节
I/O 大小(最小/最佳)：512 字节 / 512 字节


磁盘 /dev/mapper/VolGroup-swap：6438 MB, 6438256640 字节，12574720 个扇区
Units = 扇区 of 1 * 512 = 512 bytes
扇区大小(逻辑/物理)：512 字节 / 512 字节
I/O 大小(最小/最佳)：512 字节 / 512 字节


磁盘 /dev/mapper/VolGroup-var：53.7 GB, 53682896896 字节，104849408 个扇区
Units = 扇区 of 1 * 512 = 512 bytes
扇区大小(逻辑/物理)：512 字节 / 512 字节
I/O 大小(最小/最佳)：512 字节 / 512 字节


磁盘 /dev/mapper/VolGroup00-consul：1073 MB, 1073741824 字节，2097152 个扇区
Units = 扇区 of 1 * 512 = 512 bytes
扇区大小(逻辑/物理)：512 字节 / 512 字节
I/O 大小(最小/最佳)：512 字节 / 512 字节


# 创建卷。发现报无法找到/dev/xvdb2设备，因此需要重启
[root@cdh1 ~]# pvcreate /dev/xvdb2
  Device /dev/xvdb2 not found.
#重启
[root@cdh1 ~]# reboot
Connection to xx.xxx.x.xx closed by remote host.
Connection to xx.xxx.x.xx closed.
#再次创建卷
[root@cdh1 ~]# pvcreate /dev/xvdb2
  Physical volume "/dev/xvdb2" successfully created.

# 扩展卷组 VolGroup
[root@cdh1 ~]# vgextend  VolGroup  /dev/xvdb2
  Volume group "VolGroup" successfully extended

# 查看物理卷
[root@cdh1 ~]# pvdisplay
  --- Physical volume ---
  PV Name               /dev/xvda3
  VG Name               VolGroup
  PV Size               <38.51 GiB / not usable 3.00 MiB
  Allocatable           yes (but full)
  PE Size               4.00 MiB
  Total PE              9857
  Free PE               0
  Allocated PE          9857
  PV UUID               uQLUzq-WiZg-YdPp-KNq1-cVdw-aTBc-ey3YWy

  --- Physical volume ---
  PV Name               /dev/xvdb1
  VG Name               VolGroup
  PV Size               42.00 GiB / not usable 4.00 MiB
  Allocatable           yes (but full)
  PE Size               4.00 MiB
  Total PE              10751
  Free PE               0
  Allocated PE          10751
  PV UUID               JagRJV-FXfW-BcWx-vweZ-kPQz-7goT-COX2MH

  --- Physical volume ---
  PV Name               /dev/xvdb2
  VG Name               VolGroup
  PV Size               <418.00 GiB / not usable 3.00 MiB
  Allocatable           yes
  PE Size               4.00 MiB
  Total PE              107007
  Free PE               107007
  Allocated PE          0
  PV UUID               nf2YjK-Kcp0-hxX3-QeUU-MuX2-EmG5-zzyIQK

  --- Physical volume ---
  PV Name               /dev/xvda2
  VG Name               VolGroup00
  PV Size               1.00 GiB / not usable 4.00 MiB
  Allocatable           yes (but full)
  PE Size               4.00 MiB
  Total PE              256
  Free PE               0
  Allocated PE          256
  PV UUID               w96LS0-UNYi-57Um-FBIO-U7uv-Csqi-2j91yP

[root@cdh1 ~]# df -h
文件系统                       容量  已用  可用 已用% 挂载点
/dev/mapper/VolGroup-root       25G   11G   15G   42% /
devtmpfs                        16G     0   16G    0% /dev
tmpfs                           16G     0   16G    0% /dev/shm
tmpfs                           16G  8.7M   16G    1% /run
tmpfs                           16G     0   16G    0% /sys/fs/cgroup
/dev/xvda1                     497M  267M  231M   54% /boot
/dev/mapper/VolGroup-var        50G  6.8G   44G   14% /var
/dev/mapper/VolGroup00-consul 1014M   33M  982M    4% /consul
tmpfs                          3.2G     0  3.2G    0% /run/user/1001
cm_processes                    16G     0   16G    0% /run/cloudera-scm-agent/process
tmpfs                          3.2G     0  3.2G    0% /run/user/0

#查看当前逻辑卷的名称及空间大小
[root@cdh1 ~]# lvdisplay
  --- Logical volume ---
  LV Path                /dev/VolGroup/root
  LV Name                root
  VG Name                VolGroup
  LV UUID                2f6zN4-wlsI-6JKt-9B5d-fDDm-cudJ-gus6RB
  LV Write Access        read/write
  LV Creation host, time localhost, 2018-03-29 10:32:45 +0800
  LV Status              available
  # open                 1
  LV Size                <24.51 GiB
  Current LE             6274
  Segments               1
  Allocation             inherit
  Read ahead sectors     auto
  - currently set to     8192
  Block device           253:0

  --- Logical volume ---
  LV Path                /dev/VolGroup/var
  LV Name                var
  VG Name                VolGroup
  LV UUID                Dzr2iB-N6f3-Edv0-bXOj-OTDI-pibv-jCTuCY
  LV Write Access        read/write
  LV Creation host, time localhost, 2018-03-29 10:32:46 +0800
  LV Status              available
  # open                 1
  LV Size                <50.00 GiB
  Current LE             12799
  Segments               2
  Allocation             inherit
  Read ahead sectors     auto
  - currently set to     8192
  Block device           253:3

  --- Logical volume ---
  LV Path                /dev/VolGroup/swap
  LV Name                swap
  VG Name                VolGroup
  LV UUID                tEJ8sn-RGQr-FSvs-CL9K-b4Qi-OjT2-MeE7Av
  LV Write Access        read/write
  LV Creation host, time localhost, 2018-03-29 10:32:47 +0800
  LV Status              available
  # open                 2
  LV Size                <6.00 GiB
  Current LE             1535
  Segments               1
  Allocation             inherit
  Read ahead sectors     auto
  - currently set to     8192
  Block device           253:1

  --- Logical volume ---
  LV Path                /dev/VolGroup00/consul
  LV Name                consul
  VG Name                VolGroup00
  LV UUID                HicSHS-oJaI-GCdQ-4EUd-pLdN-7rkp-UkwMOE
  LV Write Access        read/write
  LV Creation host, time localhost, 2018-03-29 10:32:51 +0800
  LV Status              available
  # open                 1
  LV Size                1.00 GiB
  Current LE             256
  Segments               1
  Allocation             inherit
  Read ahead sectors     auto
  - currently set to     8192
  Block device           253:2

#扩展分区
[root@cdh1 ~]# lvextend /dev/VolGroup/root /dev/xvdb2
  Size of logical volume VolGroup/root changed from <24.51 GiB (6274 extents) to 442.50 GiB (113281 extents).
  Logical volume VolGroup/root successfully resized.

#调整文件系统大小
[root@cdh1 ~]# xfs_growfs /dev/VolGroup/root
meta-data=/dev/mapper/VolGroup-root isize=512    agcount=4, agsize=1606144 blks
         =                       sectsz=512   attr=2, projid32bit=1
         =                       crc=1        finobt=0 spinodes=0
data     =                       bsize=4096   blocks=6424576, imaxpct=25
         =                       sunit=0      swidth=0 blks
naming   =version 2              bsize=4096   ascii-ci=0 ftype=1
log      =internal               bsize=4096   blocks=3137, version=2
         =                       sectsz=512   sunit=0 blks, lazy-count=1
realtime =none                   extsz=4096   blocks=0, rtextents=0
data blocks changed from 6424576 to 115999744

#查看分区
[root@cdh1 ~]# lvscan
  ACTIVE            '/dev/VolGroup/root' [442.50 GiB] inherit
  ACTIVE            '/dev/VolGroup/var' [<50.00 GiB] inherit
  ACTIVE            '/dev/VolGroup/swap' [<6.00 GiB] inherit
  ACTIVE            '/dev/VolGroup00/consul' [1.00 GiB] inherit
#用df查看系统磁盘信息
[root@cdh1 ~]# df -h
文件系统                       容量  已用  可用 已用% 挂载点
/dev/mapper/VolGroup-root      443G   11G  433G    3% /
devtmpfs                        16G     0   16G    0% /dev
tmpfs                           16G     0   16G    0% /dev/shm
tmpfs                           16G  8.7M   16G    1% /run
tmpfs                           16G     0   16G    0% /sys/fs/cgroup
/dev/xvda1                     497M  267M  231M   54% /boot
/dev/mapper/VolGroup-var        50G  6.8G   44G   14% /var
/dev/mapper/VolGroup00-consul 1014M   33M  982M    4% /consul
tmpfs                          3.2G     0  3.2G    0% /run/user/1001
cm_processes                    16G     0   16G    0% /run/cloudera-scm-agent/process
tmpfs                          3.2G     0  3.2G    0% /run/user/0
[root@cdh1 ~]#

```

<br/><br/>

调整之后的集群系统磁盘信息如下：

```bash
# cdh1节点
[root@cdh1 ~]# df -hl
文件系统                       容量  已用  可用 已用% 挂载点
/dev/mapper/VolGroup-root      443G   11G  433G    3% /
devtmpfs                        16G     0   16G    0% /dev
tmpfs                           16G     0   16G    0% /dev/shm
tmpfs                           16G  8.7M   16G    1% /run
tmpfs                           16G     0   16G    0% /sys/fs/cgroup
/dev/xvda1                     497M  267M  231M   54% /boot
/dev/mapper/VolGroup-var        50G  6.8G   44G   14% /var
/dev/mapper/VolGroup00-consul 1014M   33M  982M    4% /consul
tmpfs                          3.2G     0  3.2G    0% /run/user/1001
cm_processes                    16G  3.4M   16G    1% /run/cloudera-scm-agent/process
tmpfs                          3.2G     0  3.2G    0% /run/user/0

# cdh2节点
[root@cdh2 ~]# df -hl
文件系统                       容量  已用  可用 已用% 挂载点
/dev/mapper/VolGroup-root      463G  9.3G  454G    3% /
devtmpfs                        16G     0   16G    0% /dev
tmpfs                           16G     0   16G    0% /dev/shm
tmpfs                           16G  201M   16G    2% /run
tmpfs                           16G     0   16G    0% /sys/fs/cgroup
/dev/xvda1                     497M  254M  243M   52% /boot
/dev/mapper/VolGroup00-consul 1014M   33M  982M    4% /consul
/dev/mapper/VolGroup-var        30G  974M   30G    4% /var
tmpfs                          3.2G     0  3.2G    0% /run/user/0
tmpfs                          3.2G     0  3.2G    0% /run/user/99
cm_processes                    16G   98M   16G    1% /run/cloudera-scm-agent/process

# cdh3节点
[root@cdh3 ~]# df -hl
文件系统                       容量  已用  可用 已用% 挂载点
/dev/mapper/VolGroup-root      463G   12G  452G    3% /
devtmpfs                        16G     0   16G    0% /dev
tmpfs                           16G     0   16G    0% /dev/shm
tmpfs                           16G  201M   16G    2% /run
tmpfs                           16G     0   16G    0% /sys/fs/cgroup
/dev/xvda1                     497M  254M  243M   52% /boot
/dev/mapper/VolGroup00-consul 1014M   33M  982M    4% /consul
/dev/mapper/VolGroup-var        30G  1.1G   29G    4% /var
tmpfs                          3.2G     0  3.2G    0% /run/user/0
tmpfs                          3.2G     0  3.2G    0% /run/user/99
cm_processes                    16G   31M   16G    1% /run/cloudera-scm-agent/process

```

<br/>
<br/>

这部分欢迎访问我的blog [Centos7扩展挂载点的容量](https://blog.csdn.net/github_39577257/article/details/101071050)

