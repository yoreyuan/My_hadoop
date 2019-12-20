[CDH 6.3.0](https://www.cloudera.com/downloads/cdh/6-3-0.html)
==========

# 1 下载
* [MySQL](https://dev.mysql.com/get/Downloads/MySQL-5.7/mysql-5.7.27-el7-x86_64.tar.gz)

* [Cloudera Manager 6.3.0](https://www.cloudera.com/documentation/enterprise/6/release-notes/topics/rg_cm_6_version_download.html#CM_630)
* [CDH 6.3.x](https://www.cloudera.com/documentation/enterprise/6/release-notes/topics/rg_cdh_63_download.html#cdh_620-download)



# 2 服务器
IP | hostname | password
:---- | :---- | :----
192.168.3.3 | yr-3-3 cdh1.yore.com cdh1 |   A9******S3 
192.168.3.6 | yr-3-6 cdh2.yore.com cdh2 |   31******EY
192.168.3.9 | yr-3-9 cdh3.yore.com cdh3 |   LP******G5

因为在指定Host时，所有的Host的用户名和密码必须一样，但是这里root的密码都不一样，可以先以一个节点为主，
然后在起一个页面，将其它节点添加的同一个集群中。

* Cloudera Manager -> 主机 -> 所有主机  ->  Add Hosts 
* 选择集群名字，Add hosts to cluster。继续
* 主机名，依次添加，例如添加一个 cdh3.yore.com。继续
* 填入自定义存储库：http://192.168.3.3/cloudera-repos/cm6/6.3.0/
* 填入用户名root和密码，继续


```
已经重装， 数据盘： /dev/xvdb

下面是我们使用  lvm 方式创建分区的方式，请参考
pvcreate /dev/xvdb
vgcreate VolGroup00 /dev/xvdb
lvcreate -l 100%FREE -n   lv_app  VolGroup00
mkfs.xfs /dev/VolGroup00 /dev/VolGroup00/lv_app

vi /etc/fstab      :  /dev/VolGroup00/lv_app   /app   xfs   defaults 0 0
```

# 3 Host
[Configure Network Names](https://www.cloudera.com/documentation/enterprise/6/6.3/topics/configure_network_names.html#configure_network_names)

**注意**：hostname必须是一个FQDN（全限定域名），例如`myhost-1.example.com`，否则后面启动Agent时有个验证时无法通过的。
```bash
# yr-3-3
sudo hostnamectl set-hostname cdh1.yore.com
# yr-3-6
sudo hostnamectl set-hostname cdh2.yore.com
# yr-3-9
sudo hostnamectl set-hostname cdh3.yore.com

#配置 /etc/hosts
192.168.3.3 cdh1.yore.com cdh1 yr-3-3
192.168.3.6 cdh2.yore.com cdh2 yr-3-6
192.168.3.9 cdh3.yore.com cdh3 yr-3-9


# 配置 /etc/sysconfig/network 
# yr-3-3
HOSTNAME=cdh1.yore.com
# yr-3-6
HOSTNAME=cdh2.yore.com
# yr-3-9
HOSTNAME=cdh3.yore.com


```

# 4 防火墙和selinux
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

# 5 SSH
```bash
# 有提示，直接回车(有3次)
ssh-keygen -t rsa

# 授权node2、node3节点可以免密访问node1
ssh-copy-id root@yr-3-3
ssh-copy-id root@yr-3-6
ssh-copy-id root@yr-3-9
```

# 6 JDK
rpm方式安装
```bash
mkdir /usr/java
cp oracle-j2sdk1.8-1.8.0+update181-1.x86_64.rpm /usr/java/
cd /usr/java
chmod +x oracle-j2sdk1.8-1.8.0+update181-1.x86_64.rpm
rpm -ivh oracle-j2sdk1.8-1.8.0+update181-1.x86_64.rpm

```

```bash
### set java environment
#JAVA_HOME=/usr/local/zulu8
JAVA_HOME=/usr/java/jdk1.8.0_181-cloudera
JRE_HOME=$JAVA_HOME/jre
CLASS_PATH=.:$JAVA_HOME/lib/dt.jar:$JAVA_HOME/lib/tools.jar:$JRE_HOME/lib
PATH=$PATH:$JAVA_HOME/bin:$JRE_HOME/bin
export JAVA_HOME JRE_HOME CLASS_PATH PATH

```

# 7 Scala
三个节点都进行如下操作
```bash
# 解压到
tar -zxf  scala-2.11.8.tgz -C /usr/local
mv /usr/local/scala-2.11.8 /usr/local/scala

# 这里可以用scp把/usr/local/scala发送到另外的两个节点
scp -r /usr/local/scala/ root@yr-3-6:/usr/local/
scp -r /usr/local/scala/ root@yr-3-9:/usr/local/

#配置环境变量，在 /etc/profile中添加如下配置，并生效 ` source /etc/proifle `
# set Scala environment
export SCALA_HOME=/usr/local/scala
export PATH=$PATH:$SCALA_HOME/bin

# 验证
scala -version

```


# 8 MySQL
规划：在`192.168.3.9`节点安装

## 配置环境变量
```bash
# 配置Mysql环境变量
export PATH=$PATH:/usr/local/mysql/bin
```

## 创建用户和组
```bash
#①建立一个mysql的组 
groupadd mysql
#②建立mysql用户，并且把用户放到mysql组 
useradd -r -g mysql mysql
#③还可以给mysql用户设置一个密码（mysql）
passwd mysql 回车设置mysql用户的密码
#④修改/usr/local/mysql 所属的组和用户
chown -R mysql:mysql /usr/local/mysql/

```

## 设置MySQL配置文件
vim /etc/my.cnf
```bash
[mysqld]
basedir = /usr/local/mysql
datadir = /usr/local/mysql/data
port = 3306
socket=/var/lib/mysql/mysql.sock
character-set-server=utf8

transaction-isolation = READ-COMMITTED
# Disabling symbolic-links is recommended to prevent assorted security risks;
# to do so, uncomment this line:
symbolic-links = 0
server_id=1
max_connections = 550
log_bin=/var/lib/mysql/mysql_binary_log
binlog_format = mixed
read_buffer_size = 2M
read_rnd_buffer_size = 16M
sort_buffer_size = 8M
join_buffer_size = 8M

# InnoDB settings
innodb_file_per_table = 1
innodb_flush_log_at_trx_commit  = 2
innodb_log_buffer_size = 32M
innodb_buffer_pool_size = 64M
innodb_thread_concurrency = 8
innodb_flush_method = O_DIRECT
innodb_log_file_size = 64M
 
[client]
default-character-set=utf8
socket=/var/lib/mysql/mysql.sock
 
[mysql]
default-character-set=utf8
socket=/var/lib/mysql/mysql.sock

[mysqld_safe]
log-error=/var/log/mysqld.log
pid-file=/var/run/mysqld/mysqld.pid

sql_mode=STRICT_ALL_TABLES

```

## 配置
```bash
# 解压到 /usr/local/ 下
tar -zxf mysql-5.7.27-el7-x86_64.tar.gz -C /usr/local/
# 重命名
mv /usr/local/mysql-5.7.27-el7-x86_64/ /usr/local/mysql

# 实现mysqld -install这样开机自动执行效果 
cp /usr/local/mysql/support-files/mysql.server /etc/init.d/mysql
vim /etc/init.d/mysql
# 添加如下配置
basedir=/usr/local/mysql
datadir=/usr/local/mysql/data

#创建存放socket文件的目录
mkdir -p /var/lib/mysql
chown mysql:mysql /var/lib/mysql
#添加服务mysql 
chkconfig --add mysql 
# 设置mysql服务为自动
chkconfig mysql on 

```

## 开始安装
```bash
#初始化mysql。注意记录下临时密码： ?w=HuL-yV05q
/usr/local/mysql/bin/mysqld --initialize --user=mysql --basedir=/usr/local/mysql --datadir=/usr/local/mysql/data
#给数据库加密
/usr/local/mysql/bin/mysql_ssl_rsa_setup --datadir=/usr/local/mysql/data

# 启动mysql服务。过段时间，当不再刷屏时，按Ctrl + C退出后台进程
/usr/local/mysql/bin/mysqld_safe --user=mysql & 
# 重启MySQL服务
/etc/init.d/mysql restart 
#查看mysql进程 
ps -ef|grep mysql

```

## 登陆MySQL，完成后续设置
```bash
#第一次登陆mysql数据库，输入刚才的那个临时密码
/usr/local/mysql/bin/mysql -uroot -p 
```
```sql
--必须先修改密码
mysql>  set password=password('p8**&***Hv');

--在mysql中添加一个远程访问的用户 
mysql> use mysql; 
mysql> select host,user from user; 
-- 添加一个远程访问用户scm，并设置其密码为 U@*****Qh^
mysql> grant all privileges on *.* to 'scm'@'%' identified by 'U@*****Qh^' with grant option; 
--刷新配置
mysql> flush privileges;

```

# 9 NTP
ip | 用途
:---- | :----
~~ntp2.yore.com~~ | NTP服务
yr-3-3 | ntpd服务，以本地时间为准
yr-3-6 | ntpd客户端。与ntpd服务同步时间
yr-3-9 | ntpd客户端。与ntpd服务同步时间

```bash
# NTP服务，如果没有先安装
systemctl status ntpd.service

```

**非常重要** 硬件时间与系统时间一起同步。修改配置文件`vim /etc/sysconfig/ntpd`。末尾新增代码`SYNC_HWCLOCK=yes`
```bash
# Command line options for ntpd
#OPTIONS="-x -u ntp:ntp -p /var/run/ntpd.pid"
#OPTIONS="-u ntp:ntp -p /var/run/ntpd.pid -g"
OPTIONS="-g"
SYNC_HWCLOCK=yes

```

添加NTP服务列表，编辑`vim /etc/ntp/step-tickers
```bash
# List of NTP servers used by the ntpdate service.

0.centos.pool.ntp.org

```

修改ntp配置文件`vim /etc/ntp.conf`

**NTP服务**配置如下：
```bash
driftfile /var/lib/ntp/drift
logfile /var/log/ntp.log
pidfile   /var/run/ntpd.pid
leapfile  /etc/ntp.leapseconds
includefile /etc/ntp/crypto/pw
keys /etc/ntp/keys

#允许任何IP的客户端进行时间同步，但不允许修改NTP服务端参数，default类似于0.0.0.0
restrict default kod nomodify notrap nopeer noquery
restrict -6 default kod nomodify notrap nopeer noquery
#restrict 192.168.3.3 nomodify notrap nopeer noquery
#允许通过本地回环接口进行所有访问
restrict 127.0.0.1
restrict  -6 ::1
# 允许内网其他机器同步时间。网关和子网掩码。/etc/sysconfig/network-scripts/ifcfg-网卡名；route -n、ip route show  
restrict 192.168.3.254 mask 255.255.255.0 nomodify notrap
restrict 192.168.3.0 mask 255.255.255.0 nomodify notrap
# 允许上层时间服务器主动修改本机时间
#server ntp2.yore.com minpoll 4 maxpoll 4 prefer
server xx.xx.x.xx minpoll 4 maxpoll 4 prefer
fudge xx.xx.x.xx stratum 3
server 192.168.3.3
# 外部时间服务器不可用时，以本地时间作为时间服务
server  127.127.1.0     # local clock
fudge   127.127.1.0 stratum 10
```

**NTP客户端**配置如下：
```bash
driftfile /var/lib/ntp/drift
logfile /var/log/ntp.log
pidfile   /var/run/ntpd.pid
leapfile  /etc/ntp.leapseconds
includefile /etc/ntp/crypto/pw
keys /etc/ntp/keys

restrict default kod nomodify notrap nopeer noquery
restrict -6 default kod nomodify notrap nopeer noquery
restrict 127.0.0.1
restrict -6 ::1
server 192.168.3.3 iburst

```

NTP重启并同步
```bash
# service ntpd status
systemctl restart ntpd
systemctl enable ntpd
ntpq -p
#ntpd -q -g 
#ss -tunlp | grep -w :123
#手动触发同步
ntpdate -uv ntp2.yore.com
#ntpdate yr-3-3
# ntpdate -d  yr-3-3
# ntpdate -u  ntp2.yore.com
ntpdate -u  yr-3-3
# 查看同步状态。需要过一段时间，查看状态会变成synchronised
ntpstat
timedatectl
ntptime

#开机自启
# 2、3、4、5为on，表示开机自启
# chkconfig --list ntpd 
chkconfig ntpd on

```

# 10 Apache HTTP
```bash
# 查看状态
sudo systemctl status httpd
# 启动服务
sudo systemctl start httpd

# 创建资源路径
sudo mkdir -p /var/www/html/cloudera-repos
# 将前面下载的Cloudera Manager 6.3.0的rpm包上传的Apache服务的指定路径下
# Apache 服务器节点执行
mkdir -p /var/www/html/cloudera-repos/cm6/6.3.0/redhat7/yum/RPMS/x86_64
mv rpms/* /var/www/html/cloudera-repos/cm6/6.3.0/redhat7/yum/RPMS/x86_64

#下载 ASC文件
wget https://archive.cloudera.com/cm6/6.3.0/allkeys.asc
# 同时还需要下载一个asc文件，同样保存到cloudera-repos目录下
mv allkeys.asc /var/www/html/cloudera-repos/cm6/6.3.0

# 将parcel包上传到Apache服务的指定路径下
mkdir -p /var/www/html/cloudera-repos/cdh6/6.3.0/parcels
mv parcels/* /var/www/html/cloudera-repos/cdh6/6.3.0/parcels

# 将repo相关文件复制到Apache服务的指定路径下。
mv RPM-GPG-KEY-cloudera /var/www/html/cloudera-repos/cm6/6.3.0/redhat7/yum
mv cloudera-manager.repo /var/www/html/cloudera-repos/cm6/6.3.0/redhat7/yum

# 如果没有repodata，先安装
#wget http://mirror.centos.org/centos/7/os/x86_64/Packages/createrepo-0.9.9-28.el7.noarch.rpm
yum -y install createrepo

# 初始化repodata
cd /var/www/html/cloudera-repos/cm6/6.3.0/redhat7/yum/
# 创建，这一步会在当前路径下创建一个 repodata 文件夹
createrepo .
#yum repolist

# 注意文件的权限
chmod 555 -R /var/www/html/cloudera-repos

```

# 11 cloudera-manager 安装前
三个节点都执行
```bash
wget http://yr-3-3/cloudera-repos/cm6/6.3.0/redhat7/yum/cloudera-manager.repo -P /etc/yum.repos.d/
# 导入存储库签名GPG密钥：
sudo rpm --import http://yr-3-3/cloudera-repos/cm6/6.3.0/redhat7/yum/RPM-GPG-KEY-cloudera
```

修改`cloudera-manager.repo`。执行命令：`vim /etc/yum.repos.d/cloudera-manager.repo`，修改为如下(注意https改为http)
```yaml
[cloudera-manager]
name=Cloudera Manager 6.3.0
baseurl=http://yr-3-3/cloudera-repos/cm6/6.3.0/redhat7/yum/
gpgkey=http://yr-3-3/cloudera-repos/cm6/6.3.0/redhat7/yum/RPM-GPG-KEY-cloudera
gpgcheck=1
enabled=1
autorefresh=0
type=rpm-md
```

更新yum
```bash
#清除 yum 缓存
sudo yum clean all
#更新yum
sudo yum update
```

MySQL驱动包（**注意**一定要将MySQL驱动重名为mysql-connector-java.jar）
```bash
sudo mkdir -p /usr/share/java/
mv mysql-connector-java-5.1.46-bin.jar /usr/share/java/mysql-connector-java.jar
scp /usr/share/java/mysql-connector-java.jar root@yr-3-3:/usr/share/java/
scp /usr/share/java/mysql-connector-java.jar root@yr-3-6:/usr/share/java/
```

关于时区。/opt/cloudera/cm/bin/cm-server文件中，大概第43行添加CMF_OPTS="$CMF_OPTS -Duser.timezone=Asia/Shanghai"
```bash
# ……
44 CMF_OPTS="$CMF_OPTS -Duser.timezone=Asia/Shanghai"
# ……
```



# 12 安装 Cloudera Manager
```bash
#在 Server端 执行
sudo yum install -y cloudera-manager-daemons cloudera-manager-agent cloudera-manager-server

#在 Agent端 执行
sudo yum install -y cloudera-manager-agent cloudera-manager-daemons

```

为了后面安装的更快速，将下载的CDH包裹方到这里(只在Server端)：
```bash
cd /opt/cloudera/parcel-repo/
# ln -s /opt/cloudera/parcel-repo/CDH-6.3.0-1.cdh6.3.0.p0.1279813-el7.parcel /var/www/html/cloudera-repos/cdh6/6.3.0/parcels/CDH-6.3.0-1.cdh6.3.0.p0.1279813-el7.parcel
wget http://yr-3-3/cloudera-repos/cdh6/6.3.0/parcels/CDH-6.3.0-1.cdh6.3.0.p0.1279813-el7.parcel
wget http://yr-3-3/cloudera-repos/cdh6/6.3.0/parcels/CDH-6.3.0-1.cdh6.3.0.p0.1279813-el7.parcel.sha1
wget http://yr-3-3/cloudera-repos/cdh6/6.3.0/parcels/manifest.json



# 在manifest.json中找到对应版本的密钥(大概在755行)，复制到*.sha文件中
# 一般CDH-6.3.0-1.cdh6.3.0.p0.1279813-el7.parcel.sha1文件的内容和parcel密钥是一致的，只需重命名即可
echo "d6e1483e47e3f2b1717db8357409865875dc307e"  > CDH-6.3.0-1.cdh6.3.0.p0.1279813-el7.parcel.sha
#修改属主属组。
chown cloudera-scm.cloudera-scm /opt/cloudera/parcel-repo/*
```

修改agent配置文件，将Cloudera Manager Agent 配置为指向 Cloudera Manager Serve。这里主要是配置 Agent节点的 config.ini 文件。
```bash
vim /etc/cloudera-scm-agent/config.ini
#配置如下项
# Hostname of the CM server. 运行Cloudera Manager Server的主机的名称
server_host=cdh1.yore.com
# Port that the CM server is listening on. 运行Cloudera Manager Server的主机上的端口
server_port=7182
#启用为代理使用 TLS 加密
#use_tls=1

```

# 13 设置 Cloudera Manager 数据库
登陆`192.168.3.9`服务，然后进入MySQL创建库和表
```bash
# 登陆 Mysql后执行如下命令
CREATE DATABASE scm DEFAULT CHARACTER SET utf8 DEFAULT COLLATE utf8_general_ci;

# 顺便把其他的数据库也创建处理
CREATE DATABASE amon DEFAULT CHARACTER SET utf8 DEFAULT COLLATE utf8_general_ci;
CREATE DATABASE rman DEFAULT CHARACTER SET utf8 DEFAULT COLLATE utf8_general_ci;
CREATE DATABASE hue DEFAULT CHARACTER SET utf8 DEFAULT COLLATE utf8_general_ci;
CREATE DATABASE metastore DEFAULT CHARACTER SET utf8 DEFAULT COLLATE utf8_general_ci;
CREATE DATABASE sentry DEFAULT CHARACTER SET utf8 DEFAULT COLLATE utf8_general_ci;
CREATE DATABASE nav DEFAULT CHARACTER SET utf8 DEFAULT COLLATE utf8_general_ci;
CREATE DATABASE navms DEFAULT CHARACTER SET utf8 DEFAULT COLLATE utf8_general_ci;
CREATE DATABASE oozie DEFAULT CHARACTER SET utf8 DEFAULT COLLATE utf8_general_ci;

```

初始化数据库
```bash
sudo /opt/cloudera/cm/schema/scm_prepare_database.sh -h 192.168.3.9  mysql scm scm U@*****Qh^
```

# 14 启动Cloudera Manager Server
```bash
sudo systemctl start cloudera-scm-server
#查看启动结果
sudo systemctl status cloudera-scm-server

#如果要观察启动过程可以在 Cloudera Manager Server 主机上运行以下命令：
sudo tail -f /var/log/cloudera-scm-server/cloudera-scm-server.log
# 当您看到此日志条目时，Cloudera Manager管理控制台已准备就绪：
# INFO WebServerImpl:com.cloudera.server.cmf.WebServerImpl: Started Jetty server.

```

# 15 转到 Web 浏览器
Web浏览器数据 [http://192.168.3.3:7180](http://192.168.3.3:7180)。登录Cloudera Manager Admin Console，默认凭着为
* **Username**: admin
* **Password**: admin

## ⚠️警告1 
```
Cloudera 建议将 /proc/sys/vm/swappiness 设置为最大值 10。当前设置为 30。使用 sysctl 命令在运行时更改该设置并编辑 /etc/sysctl.conf，以在重启后保存该设置。
您可以继续进行安装，但 Cloudera Manager 可能会报告您的主机由于交换而运行状况不良。以下主机将受到影响： 
```
处理：
```bash
sysctl vm.swappiness=10
# 这里我们的修改已经生效，但是如果我们重启了系统，又会变成原先的值
echo 'vm.swappiness=10'>> /etc/sysctl.conf
```

## ⚠️警告2
```
已启用透明大页面压缩，可能会导致重大性能问题。请运行“echo never > /sys/kernel/mm/transparent_hugepage/defrag”和
“echo never > /sys/kernel/mm/transparent_hugepage/enabled”以禁用此设置，
然后将同一命令添加到 /etc/rc.local 等初始化脚本中，以便在系统重启时予以设置。以下主机将受到影响: 
```
处理：
```bash
echo never > /sys/kernel/mm/transparent_hugepage/defrag
echo never > /sys/kernel/mm/transparent_hugepage/enabled

# 然后将命令添加到初始化脚本中
vi /etc/rc.local
# 添加如下
echo never > /sys/kernel/mm/transparent_hugepage/defrag
echo never > /sys/kernel/mm/transparent_hugepage/enabled

```

## 数据库设置
服务 | 主机名称 | 数据库 | 用户名 | 密码
---- | ---- |  ---- | ---- | ----
Hive             | cdh3.yore.com | metastore | scm | U@*****Qh^
Activity Monitor | cdh3.yore.com | amon      | scm | U@*****Qh^
Oozie Server     | cdh3.yore.com | oozie     | scm | U@*****Qh^
Hue              | cdh3.yore.com | hue       | scm | U@*****Qh^

## 审核更改
* **dfs.datanode.data.dir**： /opt/hadoop/dfs/dn
* **dfs.namenode.name.dir**： /opt/hadoop/dfs/nn
* **dfs.namenode.checkpoint.dir**：/opt/hadoop/dfs/snn
* **hive.metastore.warehouse.dir**：/app/hive/warehouse
* **scratch_dirs**：/opt/impala/impalad
* **yarn.nodemanager.local-dirs**：/opt/hadoop/yarn/nm
* **dfs.datanode.failed.volumes.tolerated**：0

* **fs_wal_dir**：/opt/kudu/master/wal、/opt/kudu/server/wal
* **fs_data_dirs**：/opt/kudu/master/data、/opt/kudu/server/data


## Hive启动时报错
```
+ [[ -z /opt/cloudera/cm ]]
+ JDBC_JARS_CLASSPATH='/opt/cloudera/cm/lib/*:/usr/share/java/mysql-connector-java.jar:/opt/cloudera/cm/lib/postgresql-42.1.4.jre7.jar:/usr/share/java/oracle-connector-java.jar'
++ /usr/java/jdk1.8.0_181-cloudera/bin/java -Djava.net.preferIPv4Stack=true -cp '/opt/cloudera/cm/lib/*:/usr/share/java/mysql-connector-java.jar:/opt/cloudera/cm/lib/postgresql-42.1.4.jre7.jar:/usr/share/java/oracle-connector-java.jar' com.cloudera.cmf.service.hive.HiveMetastoreDbUtil /var/run/cloudera-scm-agent/process/32-hive-metastore-create-tables/metastore_db_py.properties unused --printTableCount
Exception in thread "main" java.lang.RuntimeException: java.lang.ClassNotFoundException: com.mysql.jdbc.Driver
	at com.cloudera.cmf.service.hive.HiveMetastoreDbUtil.countTables(HiveMetastoreDbUtil.java:203)
	at com.cloudera.cmf.service.hive.HiveMetastoreDbUtil.printTableCount(HiveMetastoreDbUtil.java:284)
	at com.cloudera.cmf.service.hive.HiveMetastoreDbUtil.main(HiveMetastoreDbUtil.java:334)
Caused by: java.lang.ClassNotFoundException: com.mysql.jdbc.Driver
	at java.net.URLClassLoader.findClass(URLClassLoader.java:381)
	at java.lang.ClassLoader.loadClass(ClassLoader.java:424)
	at sun.misc.Launcher$AppClassLoader.loadClass(Launcher.java:349)
	at java.lang.ClassLoader.loadClass(ClassLoader.java:357)
	at java.lang.Class.forName0(Native Method)
	at java.lang.Class.forName(Class.java:264)
	at com.cloudera.enterprise.dbutil.SqlRunner.open(SqlRunner.java:180)
	at com.cloudera.enterprise.dbutil.SqlRunner.getDatabaseName(SqlRunner.java:264)
	at com.cloudera.cmf.service.hive.HiveMetastoreDbUtil.countTables(HiveMetastoreDbUtil.java:197)
	... 2 more
+ NUM_TABLES='[                          main] SqlRunner                      ERROR Unable to find the MySQL JDBC driver. Please make sure that you have installed it as per instruction in the installation guide.'
+ [[ 1 -ne 0 ]]
+ echo 'Failed to count existing tables.'
+ exit 1
```
把驱动拷贝一份到Hive的lib下
```bash
chmod 755 /usr/share/java/mysql-connector-java.jar
ln -s /usr/share/java/mysql-connector-java.jar /opt/cloudera/parcels/CDH-6.3.0-1.cdh6.3.0.p0.1279813/lib/hive/lib/mysql-connector-java.jar

```

# Hive 中文注释乱码问题

```sql
create table test (  
id bigint comment '主键ID',  
name string comment '名称',
load_time timestamp,
num double comment '数字'  
)ROW FORMAT DELIMITED FIELDS TERMINATED BY '\t'
STORED AS TEXTFILE;

--查看表，发现有中文乱码问题
hive> desc test;
OK
id                      bigint                  ??ID
name                    string                  ??
num                     double                  ??
Time taken: 0.173 seconds, Fetched: 3 row(s)

```

问题解决：
```sql
-- 登陆MySQL元数据库
mysql> use metastore;

--查看COLUMNS_V2表，发现COMMENT列就是中文乱码的
mysql> select * from COLUMNS_V2;
+-------+---------+-------------+-----------+-------------+
| CD_ID | COMMENT | COLUMN_NAME | TYPE_NAME | INTEGER_IDX |
+-------+---------+-------------+-----------+-------------+
|   287 | ??ID    | id          | bigint    |           0 |
|   287 | ??      | name        | string    |           1 |
|   287 | ??      | num         | double    |           2 |
+-------+---------+-------------+-----------+-------------+
3 rows in set (0.00 sec)

--查看COLUMNS_V2建表语句，可以发现  ENGINE=InnoDB DEFAULT CHARSET=latin1 ，使用的是 latin1 字符，
mysql> show create table COLUMNS_V2;
--因此我们将该表的字符改为utf8
alter table COLUMNS_V2 modify column COMMENT varchar(256) character set utf8; 
-- 同时也把如下的表也改为utf8
alter table TABLE_PARAMS modify column PARAM_VALUE varchar(4000) character set utf8; 
alter table PARTITION_KEYS modify column PKEY_COMMENT varchar(4000) character set utf8; 

```

测试
```sql
--Hive中插入一条数据
insert into table test values(1101, "hive组件", "2019-09-06 17:03:22",2.1);

```

