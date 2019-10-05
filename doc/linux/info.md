查看Linux信息
----


# 硬件信息
```bash
# 1 概述计算机硬件信息
lshw -short


# 2 CPU
lscpu
lshw -C cpu
# 只查看CPU品牌和型号
lshw -C cpu | grep -i product
# 仅查看 CPU的速度
lscpu | grep -i mhz
# BogoMips额定功率
lscpu | grep -i bogo


# 3 内存
# 使用dmidecode命令列出每根内存条和其容量
dmidecode -t memory | grep -i size
# 列出内存类型、容量、速度和电压
lshw -short -C memory
# 支持的最大内存
dmidecode -t memory | grep -i max
# 内存插槽使用情况
lshw -short -C memory | grep -i empty
# 内存使用信息
free -h


# 4 磁盘文件系统和设备
# 显示每个磁盘设备的描述信息
lshw -short -C disk
# 列出所有磁盘及其分区大小
lsblk
# 获取更多有关扇区数量、大小、文件系统ID、类型、分区起始和结束扇区
fdisk -l 
# 获取每个分区的唯一标识符（UUID）及其文件系统类型
blkid
# 列出已经挂载的文件系统和它们的挂载点
df -m
# 查看USB、PCI总线、其它设备详细信息
lsusb
lspci


# 5 网络
# 查看网卡信息
lshw -C network
# 显示网络接口信息
ifconfig -a
ip link show
#       lo                  回环接口
#       eth0/enp*           以太网络接口
#       wlan0               无线网络接口
#       ppp*                点对点协议接口
#       vboxnet0/vmnet*     虚拟机网络接口
netstat -i
# 默认网关路由表信息
ip route | column -t
netstat -r

# 6 软件
# 显示 UEFI或BIOS的日期和版本等信息
dmidecode -t bios
# 内核信息
uname -a

```

