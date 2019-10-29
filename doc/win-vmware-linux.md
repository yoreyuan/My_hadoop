Win环境VMware安装以及Centos安装和使用
=========

# 1 系统
* Window系统下载：[MSDN, 我告诉你](https://msdn.itellyou.cn/)
* 系统安装： PE
* Centos7系统下载：[Download CentOS](https://www.centos.org/download/)  
推荐使用阿里镜像下载：[mirrors.aliyu](https://opsx.alibaba.com/mirror)，
centos/7.6.1810/isos/[x86_64](https://mirrors.aliyun.com/centos/7.6.1810/isos/x86_64/)

# 2 Markdown
## 2.1 语法
* 题目
* 标题
* 文字
    * 加粗
    * 倾斜
    * 删除
    * 代码块
    * 代码
    * Emoji
    * keyboard
* 引用
* 超链接
* 图片
* 列表
    * 无序列表
    * 有序列表
* 表格
* 分割线

```
我是题目
===

我也是题目
---

#标题1
##标题2
###标题3
####标题4
#####标题5
######标题6

`突出的文字`
*倾斜的文字*
**加粗的文字**
~~删除的文字~~
***斜体加粗的文字***

<kbd>框中的文字</kbd>

https://www.emojicopy.com/
表情 :表情符号: 

段落：空一行 or 两个空格
> 我是引用


[我是超连接](https://blog.csdn.net/github_39577257/article/details/88776147)
![我是图片](xxx.png)

```flag
代码段
\```

* 无序列表
* 无序列表
    1. 有序列表
    2. 有序列表

默认 | 左对齐 | 居中 | 右对齐
---- | :---- | :----: | ----:
11  | 12    | 13    | 14
21  | 22    | 23    | 24


*********

---------

```


## 2.2 工具
* [DataGrip](https://www.jetbrains.com/datagrip/)。SQL和 Markdown
* [谷歌访问助手](https://github.com/haotian-wang/google-access-helper)。[chrome://extensions](chrome://extensions)，勾选开发者模式。有问题Google搜索
* [Octotree](#)。方便GitHub上查看源码

## 2.3 Markdown文件转换
* [pandoc 官网](https://www.pandoc.org/)。Markdown文件如何转换为其他文件格式。
* 下载：[https://github.com/jgm/pandoc/releases](https://github.com/jgm/pandoc/releases)
* 支持的格式：[README.md](https://github.com/jgm/pandoc/blob/master/README.md)
* 转换：
```bash
pandoc win-vmware-linux.md -o win-vmware-linux.md.docx
```

# 3 SSH工具
* 远程终端：xshell、securecrt、**[MobaXterm](https://mobaxterm.mobatek.net/)**
* (可选，因为mobaxterm已经包含)远程文件传输：[FileZilla](https://filezilla-project.org)  
或者是 [WinSCP](http://www.winscp.net)
* (可选)lz，linux安装一个lz可用用命令进行上传和下载文件
```bash
# 安装
yum install -y lrzsz
# 上传
rz
# 下载
lz
```




# 4 VMware Workstation Pro安装
主要以Win环境下的安装为例

## 4.1 下载VMware
第一种：通过官方网站下载：   
访问[VMware Workstation Pro](https://www.vmware.com/products/workstation-pro/workstation-pro-evaluation.html)，
选择`Workstation 15 Pro for Windows`下载即可。

[VMware-workstation-full-15.1.0-13591040.exe](https://download3.vmware.com/software/wkst/file/VMware-workstation-full-15.1.0-13591040.exe)

Keys:
```bash
#VMware 2019 v15.x 永久许可证激活密钥
YG5H2-ANZ0H-M8ERY-TXZZZ-YKRV8
UG5J2-0ME12-M89WY-NPWXX-WQH88
UA5DR-2ZD4H-089FY-6YQ5T-YPRX6
```

## 4.2 安装
* 双击安装：
* 选择`安装位置`，
* 安装，完成
* 双击打开，提示输入15位许可证密钥。许可证：输入上面的密钥
* 开始使用



## 4.3 创建虚拟机
(可选)创建虚拟机镜像之前，可以先配置一下NAT模式下的网络，可以配置后面自己喜欢的ip网段。编辑 -> 虚拟网络编辑器:
* 子网IP：配置喜欢的网段，例如这里配置为`192.168.33.0`
* 子网掩码：如果子网不多，配置为`255.255.255.0`即可
* 应用。确定

<br/>


* 创建新的虚拟机
* 你希望什么类型的配置？☉ 自定义 （也可以选择典型）。下一步
* 虚拟机兼容性，因为这里没有备份的老的镜像，默认。下一步
* 安装客户端操作系统。安装程序光盘映像文件。选择`CentOS-7-x86_64-DVD-1810.iso`文件。下一步
* 命名虚拟机。虚拟机名称`node1`。位置：`C:\opt\os\node1`。下一步
* 处理器配置。电脑比较好的，处理器数量和内核数给的多一些。这里默认为1也可。下一步
* 此虚拟机内存。最少给512MB吧。剩余内存多的可以多给些，这里后面要启三个，保证空间够即可，后期也可以随时调整。这里选择：`2048MB`。下一步
* **important**。网络类型选择：`使用网络地址转换(NAT)`。下一步
* 选择I/O控制器类型。使用默认推荐的即可。下一步
* 选择磁盘类型。使用默认推荐的即可。下一步
* 选择磁盘。☉ 创建新虚拟磁盘。下一步
* 指定磁盘容量。磁盘大小可以给的大一些，需要选择下面的立即分配就不会马上占用掉系统的磁盘大小，只是说后期虚拟机可以使用这么大磁盘（当然后期不够也可以扩展，但稍微麻烦些），
这里选择：`30`GB。如果不进行跨机拷贝或者移动，可以选择当文件，这里选择 ☉ 将虚拟磁盘存储为单个文件。下一步
* 指定磁盘文件。选择到上面镜像的位置目录：`C:\opt\os\node1\osCentOS 7 64 位.vmdk`
* 完成

## 4.4 开始安装CentOS
* 直接回车安装
* 选择语言。因为主要做服务器来用，语言可以选择 `English`。Continue
* 进入到`安装摘要`部分。
    * DATE & TIME。`Asia/Shanghai`时区，Done
    * SOFTWARE SELECTION。可以选择`Minimal Install`，但系统常用的好多工具都需要手动安装，这里**推荐**基础环境选择`Compute Node`，插件选择`Development Tools`。Done
    * INSTALLATION DESTINATION。选择上一步常见的虚拟30GB的磁盘。Done
    * NETWORK & HOST NAME。Host name: `node1`。Ethernet: `ON`，可以看到ip、掩码、网关、DNS，这不重要，后期还会进行配置。Done
    * Begin Installation
* 配置用户信息
    * 设置root用户的密码。ROOT PASSWORD: 比如`2019`。Done（点击两次）
    * 创建一个普通用户。USER CREATION: 用户名`yore`，密码`yore`。Done
* waiting...
* Reboot



# 5 Centos 设置
先把公共部分配置完毕后，再进行克隆。

## 5.1 静态IP
默认ip是DHCP。过上一段时间后再次重启进入系统后ip可能会都会重新分配。现在需要绑定ip，保证以后都可以使用同一iP访问这个机器。
```bash
cd /etc/sysconfig/network-scripts/
# 查看网卡文件，每个人可能不同，我的是 ens33
vim ifcfg-ens33
```

**修改**或**配置**为如下：
```bash
##IP为静态IP
BOOTPROTO="static"
##开机自启
ONBOOT="yes"
##这里配置国内的DNS地址，是固定的。也可以配置自己公司的，cat /etc/resolv.conf
DNS1=114.114.114.114
DNS2=8.8.8.8
##配置自己本机的IP
IPADDR=192.168.33.3
##子网掩码：执行`route -n `命令，Genmask列就是掩码
NETMASK=255.255.255.0
##配置网关。执行`route -n `命令，Gateway列就是网关
GATEWAY=192.168.33.2
# 添加 MACADDR=xx:xx:xx:xx:xx:xx。如果有HWADDR修改为MACADDR。要保证集群中mac地址唯一
MACADDR=00:0c:29:7b:60:50
```

保存退出：`:wq`，然后重启网络
```bash
# ifconfig ens33 down
# ifconfig ens33 up
service network restart
/etc/init.d/network restart

```

00:50:56:38:72:B8

重启，并查看：
```bash
# init 1 。 /etc/inittab
reboot
```

## 5.2 JDK安装配置
JDK有多种选择，[Oracle JDK](https://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)、
[GraalVM CE JDK](https://www.graalvm.org/)、[Zulu JDK](https://www.azul.com/downloads/zulu/)

阳光这边选择的是OpenJDK是 Zulu JDK。因此下面也以Zulu JDK为例。不过如果没有要求的话还是推荐使用Oracle JDK。版本最好选择**JDK 8**。

### 下载
```bash
wget https://cdn.azul.com/zulu/bin/zulu8.40.0.25-ca-jdk8.0.222-linux_x64.tar.gz
```

### 解压
```bash
# 解压到/usr/local/下
tar -zxvf zulu8.40.0.178-ca-jdk1.8.0_222-linux_aarch64.tar.gz -C /usr/local/
# 重命名
mv /usr/local/zulu8.40.0.178-ca-jdk1.8.0_222-linux_aarch64 /usr/local/zulu8
```

### 配置环境变量
可以配置到系统的环境变量下(vim /etc/profile)。也可以配置到用户的环境变量下（vim ~/.bash_profile），编辑如下，保存退出

```bash
### set java environment
JAVA_HOME=/usr/local/zulu8
JRE_HOME=$JAVA_HOME/jre
CLASS_PATH=.:$JAVA_HOME/lib/dt.jar:$JAVA_HOME/lib/tools.jar:$JRE_HOME/lib
PATH=$PATH:$JAVA_HOME/bin:$JRE_HOME/bin
export JAVA_HOME JRE_HOME CLASS_PATH PATH
```

立即生效
```bash
source /etc/profile
```

## 5.3 关闭防火墙和selinux
集群之间需要通过端口进行通信，如果不开启需要将用到的端口开放` firewall-cmd --zone=public --add-port=22/tcp --permanent`。
一般测试环境防火墙可以关闭，方便开发和测试。

```bash
#查看防火墙状态
firewall-cmd --state

# 关闭防火墙
systemctl stop firewalld.service
# 停止开机自启
systemctl disable firewalld.service 
```

禁止SELinux，将`SELINUX=enforcing`改为`SELINUX=disabled`，保存退出。
```bash
vim /etc/selinux/config
```

## 5.4 clone镜像并修改
关闭系统
```bash
shutdown -h now
```

选择刚我们安装配置的虚拟机镜像，右键 -> 管理 -> 克隆。
* 向导。 下一步
* 克隆源。☉ 虚拟机中的当前状态。下一步
* 克隆类型。☉ 创建完整克隆。下一步
* 新虚拟机名称。虚拟机名称`node2`。配置`C:\opt\os\node2`
* 完成。关闭
* 选中刚克隆的新镜像。右键 -> 设置 ->  网络适配器  ->  高级  ->  MAC地址<kbd>生成</kbd> ，因为node1系统已经绑定mac地址，这一步生成后复制一下后面修改时粘贴为这个值。确定
* 启动
* 修改ip。`vim /etc/sysconfig/network-scripts/ifcfg-ens33`，将`IPADDR`和`MACADDR`修改为新的。
* 重启网络。`service network restart`
* 修改hostname。`hostnamectl set-hostname node2` 。虽然登陆的名字没变化，但实际已经生效，并且重启后也是生效的。

同样的方法，在克隆一个node3。


## 5.5 hosts
编辑`/etc/hosts`，将集群的节点添加进来。
```bash
vim /etc/hosts
```

添加如下配置，保存退出
```bash
# ip hostname1 hostname2 ……
192.168.33.9  node1 node-a.test.com 
192.168.33.10 node2 node-b.test.com 
192.168.33.11 node3 node-c.test.com
```


## 5.6 SSH
有时集群之间会通过SSH进行执行和创建，如果未开启SSH，某些命令执行时会报错。

**在每个节点执行如下命令并将其发送到各节点**。例如在node1上执行如下命令，
```bash
# 有提示，直接回车(有3次)
ssh-keygen -t rsa

# 授权node2、node3节点可以免密访问node1
ssh-copy-id root@node1
ssh-copy-id root@node2
ssh-copy-id root@node3
```

## 5.7 NTP
### 规划
ip | 用途
:---- | :----
node1 | ntpd服务，自己跟自己同步。也可以与公网的时间同步服务器同步时间
node2 | ntpd客户端。与ntpd服务同步时间
node3 | ntpd客户端。与ntpd服务同步时间

### 安装NTP
```bash
yum install -y ntp
```

### NTP服务配置
```bash
vim /etc/ntp.conf
```

配置如下内容
```bash
driftfile /var/lib/ntp/drift
restrict default nomodify notrap nopeer noquery
restrict 127.0.0.1 
restrict ::1
restrict localhost mask 255.255.255.0 nomodify notrap
server 127.127.1.0	 # local clock
fudge  127.127.1.0   stratum 10
includefile /etc/ntp/crypto/pw
keys /etc/ntp/keys
disable monitor
```

开启NTP服务
```bash
systemctl start ntpd.service
```

### NTP客户端节点配置
```bash
vim /etc/ntp.conf
```

配置如下内容
```bash
restrict node1 mask 255.255.255.0 nomodify notrap
server node1
```

开启NTP服务
```bash
#重启服务
systemctl restart ntpd.service
#开机自启
chkconfig ntpd on

```

与NTP服务节点手动同步一次时间
```bash
ntpdate -u node1
```

### 查看同步状态
```bash
ntptime
```

如果显示如下则同步是正常的状态(状态显示 **PLL,NANO**)：
```
[root@node2 ~]# ntptime
ntp_gettime() returns code 0 (OK)
  time e0b2b842.b180f51c  Tue, Jun 18 2019  9:09:22.693, (.693374110),
  maximum error 27426 us, estimated error 0 us, TAI offset 0
ntp_adjtime() returns code 0 (OK)
  modes 0x0 (),
  offset 0.000 us, frequency 3.932 ppm, interval 1 s,
  maximum error 27426 us, estimated error 0 us,
  status 0x2001 (PLL,NANO),
  time constant 6, precision 0.001 us, tolerance 500 ppm,
```

# 6 Apache Hadoop安装
## 6.1 节点规划
节点名 |	HDFS |	YARN
:---- | :---- | :-----
node1 | NameNode 、DataNode |	ResourceManager、NodeManager
node2 | DataNode    |	NodeManager
node3 | DataNode    |	NodeManager

## 6.2 下载并解压
```bash
wget http://archive.apache.org/dist/hadoop/common/hadoop-3.1.2/hadoop-3.1.2.tar.gz
# 解压
tar -zxf hadoop-3.1.2.tar.gz -C /opt/
```

## 6.3 配置Hadoop环境变量
添加如下配置，保存并推出
```bash
vim ~/.bash_profile
```

添加如下配置
```bash
#hadoop配置
export HADOOP_HOME=/opt/hadoop-3.1.2
export PATH=$PATH:$JAVA_HOME/bin:$HADOOP_HOME/bin:$HADOOP_HOME/sbin
```
       
并使配置生效，验证。
```bash
. ~/.bash_profile
# 查看Hadoop版本
hadoop version
```

## 6.4 创建需要需要的文件夹
```bash
mkdir -p /opt/hadoop/dfs/dn
mkdir -p /opt/hadoop/dfs/nn
mkdir -p /opt/hadoop/dfs/snn
mkdir -p /opt/hadoop/yarn/container-logs
```       
       
## 6.5 修改 hadoop-env.sh 配置文件
```bash
vim $HADOOP_HOME/etc/hadoop/hadoop-env.sh
```

大概在`54行`，配置上自己的 JAVA_HOME，保存退出。
```bash
export JAVA_HOME=/usr/local/zulu8
export HADOOP_HOME=/opt/hadoop-3.1.2
export HADOOP_CONF_DIR=${HADOOP_HOME}/etc/hadoop
# 配置用户信息
export HDFS_NAMENODE_USER=root
export HDFS_DATANODE_USER=root
export HDFS_SECONDARYNAMENODE_USER=root
export YARN_RESOURCEMANAGER_USER=root
export YARN_NODEMANAGER_USER=root

```

## 6.6 配置 [core-site.xml](http://hadoop.apache.org/docs/stable/hadoop-project-dist/hadoop-common/core-default.xml)
```bash
vim $HADOOP_HOME/etc/hadoop/core-site.xml
```

添加如下配置内容
```xml
<configuration>
    <property>
        <name>fs.defaultFS</name>
        <value>hdfs://node1:8020</value>
    </property>
    <!-- 设置垃圾回收的时间，0为禁止，单位分钟数 -->
    <property>
      <name>fs.trash.interval</name>
      <value>60</value>
   </property>
   <property>
        <name>fs.trash.checkpoint.interval</name>
        <value>0</value>
   </property>
   <property>
      <name>hadoop.proxyuser.root.groups</name>
      <value>*</value>
   </property>
   <property>
      <name>hadoop.proxyuser.root.hosts</name>
      <value>*</value>
   </property>
</configuration>
```

## 6.7 配置 [hdfs-site.xml](http://hadoop.apache.org/docs/stable/hadoop-project-dist/hadoop-hdfs/hdfs-default.xml)
```bash
vim $HADOOP_HOME/etc/hadoop/hdfs-site.xml
```

添加如下配置内容
```xml
<configuration>
   <property>
      <name>dfs.namenode.name.dir</name>
      <value>file:///opt/hadoop/dfs/nn</value>
   </property>
   <property>
      <name>dfs.datanode.data.dir</name>
      <value>file:///opt/hadoop/dfs/dn</value>
   </property>
   <property>
      <name>dfs.namenode.checkpoint.dir</name>
      <value>file:///opt/hadoop/dfs/snn</value>
   </property>
   <!--block的副本数，默认为3-->
   <property>
      <name>dfs.replication</name>
      <value>1</value>
   </property>
   <property>
      <name>dfs.namenode.http-address</name>
      <value>node1:50070</value>
   </property>
   <property>
      <name>dfs.namenode.secondary.http-address</name>
      <value>node1:50090</value>
   </property>
   <property>
      <name>dfs.permissions</name>
      <value>false</value>
   </property>
</configuration>
```

## 6.8 配置 [mapred-site.xml](http://hadoop.apache.org/docs/stable/hadoop-mapreduce-client/hadoop-mapreduce-client-core/mapred-default.xml)
```bash
vim $HADOOP_HOME/etc/hadoop/mapred-site.xml
```

添加如下配置内容
```xml
<configuration>
    <property>
        <name>mapreduce.framework.name</name>
        <value>yarn</value>
    </property>
    <property>
        <name>mapreduce.jobhistory.address</name>
        <value>node1:10020</value>
    </property>
    <property>
        <name>mapreduce.jobhistory.webapp.address</name>
        <value>node1:19888</value>
    </property>
    <property>
        <name>mapreduce.jobhistory.webapp.https.address</name>
        <value>node1:19890</value>
    </property>
</configuration>
```

## 6.9 配置 [yarn-site.xml](http://hadoop.apache.org/docs/stable/hadoop-yarn/hadoop-yarn-common/yarn-default.xml)
```bash
vim $HADOOP_HOME/etc/hadoop/yarn-site.xml
```

添加如下配置内容
```xml
<configuration>
 
<!-- Site specific YARN configuration properties -->
 
   <property>
        <name>yarn.resourcemanager.hostname</name>
        <value>node1</value>
    </property>
   <property>
        <name>yarn.nodemanager.aux-services</name>
        <value>mapreduce_shuffle</value>
    </property>
   <property>
         <name>yarn.nodemanager.aux-services.mapreduce.shuffle.class</name>
         <value>org.apache.hadoop.mapred.ShuffleHandler</value>
    </property>
 
   <!--NodeManager本地目录-->
   <property>
      <name>yarn.nodemanager.local-dirs</name>
      <value>file:///opt/hadoop/yarn</value>
   </property>
   <!--NodeManager容器日志目录-->
   <property>
      <name>yarn.nodemanager.log-dirs</name>
      <value>file:///opt/hadoop/yarn/container-logs</value>
   </property>
   <property>
      <name>yarn.log-aggregation-enable</name>
      <value>true</value>
   </property>
   <property>
      <name>yarn.log.server.url</name>
      <value>http://node1:19888/jobhistory/logs/</value>
   </property>
   <property>
      <name>yarn.nodemanager.vmem-check-enabled</name>
      <value>false</value>
   </property>
   <property>
         <name>yarn.application.classpath</name>
            <value>
               $HADOOP_HOME/etc/hadoop,
               $HADOOP_HOME/share/hadoop/common/*,
               $HADOOP_HOME/share/hadoop/common/lib/*,
               $HADOOP_HOME/share/hadoop/hdfs/*,
               $HADOOP_HOME/share/hadoop/hdfs/lib/*,
               $HADOOP_HOME/share/hadoop/mapreduce/*,
               $HADOOP_HOME/share/hadoop/mapreduce/lib/*,
               $HADOOP_HOME/share/hadoop/yarn/*,
               $HADOOP_HOME/share/hadoop/yarn/lib/*
            </value>
      </property>
</configuration>
```
       
## 6.10 配置 workers
```bash
vim $HADOOP_HOME/etc/hadoop/workers
```

将集群的所有Worker添加到文件中
```
node1
node2
node3
```

## 6.11 将配置好的文件分发到各个 worker 节点
```bash
scp -r $HADOOP_HOME/ root@node2:/opt/
scp -r $HADOOP_HOME/ root@node3:/opt/
```

## 6.12 格式化 NameNode
```bash
$HADOOP_HOME/bin/hdfs namenode -format
```

## 6.13 启动Hadoop
```bash
$HADOOP_HOME/sbin/start-dfs.sh
$HADOOP_HOME/sbin/start-yarn.sh
```

## 6.14 验证和查看是否启动成功
* jps:  Master节点(node1)的进程有：`NameNode`、`SecondaryNameNode`、`ResourceManager`，在worker节点(node2、node3)的进程有：`DataNode`、`NodeManager`
* HDFS: [http://node1:50070](http://node1:50070)
* YARN: [http://node1:8088](http://node1:8088) 、 `http://node1:8088/stacks`
* 查看hdfs汇总信息： ` hdfs dfsadmin -report `
* HDFS上创建一个文件夹：`  hadoop dfs -mkdir /tmp/input `
* 往HDFS上传一份数据： `  hadoop fs -put $HADOOP_HOME/README.txt /tmp/input ` 
* 提交一个MR测试一下： `  hadoop jar $HADOOP_HOME/share/hadoop/mapreduce/hadoop-mapreduce-examples-3.1.2.jar wordcount  /tmp/input   /tmp/output `
* 查看结果： `  hadoop fs -head /tmp/output/part-r-00000 `cd 

## 6.15 关闭Hadoop
```bash
$HADOOP_HOME/sbin/stop-all.sh
```

**********

# 7 Window环境下Hadoop安装
安装路径最好不要有中文和空格，如果有需要转码或者转义

## 7.1 JDK安装
### 7.1.1 下载
访问 [Download Zulu CommunityTM](https://www.azul.com/downloads/zulu-community/) 下载，这里下载的是 Java 8。

### 7.1.2 安装
如果下载的是`.msi`直接双击安装，如果下载的是`.zip`解压。然后在win10底部任务栏搜索处输入`高级设置`回车，单击`环境变量`进行环境变量配置：
* 在系统变量下`新建`，变量名：`JAVA_HOME`，变量值：`C:\opt\zulu8`。确定
* 在系统变量下`新建`，变量名：`CLASSPATH`，变量值：`.;%JAVA_HOME%\lib;%JAVA_HOME%\lib\dt.jar;%JAVA_HOME%\lib\tools.jar;`。确定
* 修改Path值，双击进入Path进入编辑。新建输入`%JAVA_HOME%\bin`，新建输入`%JAVA_HOME%\jre\bin`。确定
* 确定，退出环境变量设置
* 验证，在cmd中输入：`java -version`，能正常输出Java版本信息，则配置成功。


## 7.2 Hadoop安装（本地伪分布式安装）
Apache的官方Hadoop版本不包含Windows二进制文件，如果要在Window环境下安装并运行，需要手动编译，但编译很无趣，这里网上有很多已经编译好的，可以下载并替换。

编译过程比较麻烦，对环境和依赖插件版本要求比较严，比如cmake最好3.x版本、protobuf必须是2.5.0等，不建议编译。直接到[7.2.2](#7.2.2)步下载解压替换方式安装。

在本地正常开发中，其实只需要配置Hadoop环境变量就行，不过我们这来还是完整安装一下。


### *7.2.1 [编译](https://cwiki.apache.org/confluence/display/HADOOP2/Hadoop2OnWindows) （不推荐）
如果需要编译特定版本，可以如下方式尝试编译安装。

```bash
# 下载源码
wget https://github.com/apache/hadoop/archive/rel/release-3.1.2.tar.gz
# 解压
tar -zxf release-3.1.2.tar.gz
cd hadoop-rel-release-3.1.2/

# cmake一定安装3.x版本。https://cmake.org/download/
#yum install -y gcc gcc-c++ make cmake
wget https://github.com/Kitware/CMake/releases/download/v3.15.2/cmake-3.15.2-Linux-x86_64.tar.gz
tar xzvf cmake-3.15.2-Linux-x86_64.tar.gz
cd cmake-3.15.2-Linux-x86_64
#配置环境变量即可

yum install -y openssl openssl-devel svn ncurses-devel zlib-devel libtool
yum install -y snappy snappy-devel bzip2 bzip2-devel lzo lzo-devel lzop autoconf automake
# 依赖插件下载和安装，这里一定安装protobuf-2.5.0版本，
 wget https://github.com/protocolbuffers/protobuf/releases/download/v2.5.0/protobuf-2.5.0.zip
unzip protobuf-2.5.0.zip
cd protobuf-2.5.0
./configure --prefix=/usr/local/protobuf
make
make check
make install
# 导入如下环境变量
export PATH=$PATH:/usr/local/protobuf/bin/
export PKG_CONFIG_PATH=/usr/local/protobuf/lib/pkgconfig/
#验证
protoc --version

# 编译
#mvn package -Pdist,native-win -DskipTests -Dtar
mvn clean package -DskipTests -Pdist,native -Dtar -Dsnappy.lib=/opt/ -Dbundle.snappy 

```

### 7.2.2 下载和解压
下载[hadoop-3.0.3](http://archive.apache.org/dist/hadoop/common/hadoop-3.0.3/hadoop-3.0.3.tar.gz)。将`hadoop-3.0.3.tar.gz`解压到`C:\opt\`下

### 7.2.3 下载Hadoop的winutils
访问[winutils](https://github.com/steveloughran/winutils/tree/master/hadoop-3.0.0/bin)，使用git下载`git clone https://github.com/steveloughran/winutils.git`，
将`bin`下载后替换上一步解压的`bin`文件

这里主要下载关注如下包(直接替换bin下的所有文件也可)：
* `hadoop.dll`
* `hdfs.dll`
* `winutils.exe`


### 7.2.4 创建需要的文件夹
在`C:\opt\`下分别创建：
```bash
hadoop\dfs\dn
hadoop\dfs\nn
hadoop\dfs\snn
hadoop\yarn\container-logs
```

### 7.2.5 配置环境变量
* 在系统变量下`新建`，变量名：`HADOOP_HOME`，变量值：`C:\opt\hadoop-3.0.3`。确定
* 修改Path值，双击进入Path进入编辑。新建输入`%HADOOP_HOME%\bin`，新建输入`%HADOOP_HOME%\sbin`。确定
* 验证。配置完毕后，在cmd中输入：`hadoop version`可以看到Hadoop的版本信息


### 7.2.6 配置文件
同Centos的安装配置相同，需要注意的是**如果有中文，编码一定统一改为UTF-8**


**hadoop-env.cmd**
```bash
set HADOOP_PREFIX=C:\opt\hadoop-3.0.3
set HADOOP_CONF_DIR=%HADOOP_PREFIX%\etc\hadoop
set YARN_CONF_DIR=%HADOOP_CONF_DIR%
set PATH=%PATH%;%HADOOP_PREFIX%\bin
```

**core-site.xml**
```xml
<configuration>
    <property>
        <name>fs.defaultFS</name>
        <value>hdfs://localhost:8020</value>
    </property>
    <!-- 设置垃圾回收的时间，0为禁止，单位分钟数 -->
    <property>
      <name>fs.trash.interval</name>
      <value>60</value>
   </property>
   <property>
        <name>fs.trash.checkpoint.interval</name>
        <value>0</value>
   </property>
   <property>
      <name>hadoop.proxyuser.Yuan.groups</name>
      <value>*</value>
   </property>
   <property>
      <name>hadoop.proxyuser.Yuan.hosts</name>
      <value>*</value>
   </property>
</configuration>
```

**hdfs-site.xml**
```xml
<configuration>
   <property>
      <name>dfs.namenode.name.dir</name>
      <value>/C:/opt/hadoop/dfs/nn</value>
   </property>
   <property>
      <name>dfs.datanode.data.dir</name>
      <value>/C:/opt/hadoop/dfs/dn</value>
   </property>
   <property>
      <name>dfs.namenode.checkpoint.dir</name>
      <value>/C:/opt/hadoop/dfs/snn</value>
   </property>
   <!--block的副本数，默认为3-->
   <property>
      <name>dfs.replication</name>
      <value>1</value>
   </property>
   <property>
      <name>dfs.namenode.http-address</name>
      <value>localhost:50070</value>
   </property>
   <property>
      <name>dfs.namenode.secondary.http-address</name>
      <value>localhost:50090</value>
   </property>
   <property>
      <name>dfs.permissions</name>
      <value>false</value>
   </property>
</configuration>
```

**mapred-site.xml**
```xml
<configuration>
    <property>
        <name>mapreduce.framework.name</name>
        <value>yarn</value>
    </property>
    <property>
        <name>mapreduce.job.user.name</name>
        <value>%USERNAME%</value>
    </property>
    <property>
        <name>mapreduce.jobhistory.address</name>
        <value>localhost:10020</value>
    </property>
    <property>
        <name>mapreduce.jobhistory.webapp.address</name>
        <value>localhost:19888</value>
    </property>
    <property>
        <name>mapreduce.jobhistory.webapp.https.address</name>
        <value>localhost:19890</value>
    </property>
</configuration>
```

**yarn-site.xml**
```xml
<configuration>
 
<!-- Site specific YARN configuration properties -->
 
   <property>
        <name>yarn.resourcemanager.hostname</name>
        <value>localhost</value>
    </property>
   <property>
        <name>yarn.nodemanager.aux-services</name>
        <value>mapreduce_shuffle</value>
    </property>
   <property>
         <name>yarn.nodemanager.aux-services.mapreduce.shuffle.class</name>
         <value>org.apache.hadoop.mapred.ShuffleHandler</value>
    </property>
 
   <!--NodeManager本地目录-->
   <property>
      <name>yarn.nodemanager.local-dirs</name>
      <value>/C:/opt/hadoop/yarn</value>
   </property>
   <!--NodeManager容器日志目录-->
   <property>
      <name>yarn.nodemanager.log-dirs</name>
      <value>/C:/opt/hadoop/yarn/container-logs</value>
   </property>
   <property>
      <name>yarn.log-aggregation-enable</name>
      <value>true</value>
   </property>
   <property>
      <name>yarn.log.server.url</name>
      <value>http://localhost:19888/jobhistory/logs/</value>
   </property>
   <property>
      <name>yarn.nodemanager.vmem-check-enabled</name>
      <value>false</value>
   </property>
</configuration>
```

### 7.2.7 格式化
在 HADOOP_HOME路径下，Shift + 右键，单击`在此处打开Powershell窗口`，输入
```bash
hdfs namenode -format
```

### 7.2.8 启动
```bash
#%HADOOP_HOME%/sbin/start-dfs.cmd
#%HADOOP_HOME%/sbin/start-yarn.cmd
%HADOOP_HOME%/sbin/start-all.cmd
```

### 7.2.9 查看
* HDFS: [http://localhost:50070](http://localhost:50070)
* YARN: [http://localhost:8088](http://localhost:8088)
* HDFS上创建一个文件夹：`  hadoop dfs -mkdir -p /tmp/input `
* 往HDFS上传一份数据： `  hadoop fs -put %HADOOP_HOME%\README.txt /tmp/input ` 
* 提交一个MR测试一下： `  hadoop jar %HADOOP_HOME%\share\hadoop\mapreduce\hadoop-mapreduce-examples-3.0.3.jar wordcount  /tmp/input   /tmp/output `
* 查看hdfs汇总信息： ` hdfs dfsadmin -report `
* 查看结果： `  hadoop fs -tail /tmp/output/part-r-00000 `


如果中间报如下问题，为权限问题，解决方法[stackoverflow](https://stackoverflow.com/questions/28958999/hdfs-write-resulting-in-createsymboliclink-error-1314-a-required-privilege)：
```
Exception message: CreateSymbolicLink error (1314): ???????????
```

1. win+R gpedit.msc 
2. 计算机配置->windows设置->安全设置->本地策略->用户权限分配->创建符号链接。 
3. 把用户添加进去，重启或者注销。


## 7.2.10 关闭Hadoop
直接关闭弹出的四个窗口也可以。
```bash
%HADOOP_HOME%/sbin/stop-all.sh
```



