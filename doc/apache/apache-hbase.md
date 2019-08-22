Apache HBase
---------
[官网](https://hbase.apache.org) &nbsp; | &nbsp; 
[GitHub](https://github.com/apache/hbase) &nbsp; | &nbsp; 
[Download](https://hbase.apache.org/downloads.html) &nbsp; | &nbsp; 
[Documentation](https://hbase.apache.org/2.1/book.html)  and [API](https://hbase.apache.org/2.1/apidocs/index.html)  


# 1 Home
Apache HBase是Hadoop之上的一个分布式、可扩展的大数据存储的数据库。
当我们需要对大数据进行随机实时 读/写 时，可以考虑使用Apache HBase。这个项目的目标是在商用硬件集群上托管非常大的表（数十亿行x百万列）。
Apache HBase是一个开源的、分布式的、版本化的非关系型数据库，模仿于Google的Bigtable： Bigtable: 
[A Distributed Storage System for Structured Data](https://research.google.com/archive/bigtable.html)。
正如Bigtable利用Google文件系统提供的分布式存储一样，Apache HBase在Hadoop的HDFS之上提供类似于Bigtable的功能。

## Download
[https://hbase.apache.org/downloads.html](https://hbase.apache.org/downloads.html)

## 特征
* 线性和模块化可扩展性。
* 严格一致的读写操作。
* 表的自动和可配置分片(configurable sharding)
* 支持RegionServers之间的自动故障转移。
* 方便的基类，用于使用Apache HBase表支持Hadoop MapReduce作业。
* 易于使用的Java API，用于客户端访问。
* 块缓存（Block Cache）和布隆过滤器（Bloom Filters）以进行实时查询。
* 通过服务器端过滤下推查询谓词
* Thrift网关和REST-ful Web服务，支持XML，Protobuf和二进制数据编码选项
* 可扩展的基于jruby（JIRB）的外壳
* 支持通过Hadoop指标子系统将指标导出到文件或Ganglia; 或通过JMX


********

# 2 HBase 安装
HBase同Hadoop的安装基本类似，也是有单机模式、伪分布式模式（Pseudo-Distributed）和完全分布式（Fully Distributed），
第一种适合直接使用的是本地文件系统，安装快速，可用户与测试；
伪分布式模式是在单机上通过多进程模拟的一个分布式，适合在只允许一个节点下运行HBase集群；
完全分布式，在多个节点启动不同的角色进程，构成一个完整的分布式集群，适合机器充足的情况下的集群搭建，一般用于生产环境。

## 2.1 安装之前
* SSH, 可参照 [book.html#quickstart_fully_distributed](https://hbase.apache.org/2.1/book.html#quickstart_fully_distributed)进行配置。
* DNS
* [NTP](https://blog.csdn.net/github_39577257/article/details/92471365#4.8)
* JDK，版本要求可查看 [java support by release line](https://hbase.apache.org/2.1/book.html#java)。例如本次安装`2.2.0`版本，JDK可选择8
* [Hadoop](./apache-hadoop.md)
* [ZooKeeper](apache-zookeeper.md)

规划(node-a.test.com是已经在/etc/hosts应已经配置好的)：  

节点名称 | Master | ZooKeeper | RegionServer
:---- | :----: | :----: | :----:
node-a.test.com  | yes   |   yes |   no
node-b.test.com  | no    |   yes |   yes
node-c.test.com  | no    |   yes |   yes

## 2.2 下载和解压
```bash
 wget https://www-eu.apache.org/dist/hbase/2.2.0/hbase-2.2.0-bin.tar.gz
 #解压
 tar -zxf hbase-2.2.0-bin.tar.gz
 cd hbase-2.2.0/ 

```

## 2.3 添加HBase环境变量
```bash
# 修改用户环境变量文件
vim ~/.bash_profile

#添加如下配置，保存，并生效
export HBASE_HOME=/opt/hbase-2.2.0/
export PATH=$PATH:$HBASE_HOME/bin

```


## 2.4 配置 hbase-env.sh
```bash
vim $HBASE_HOME/conf/hbase-env.sh
# 在文件中添加JAVA路径，保存退出
export JAVA_HOME=/usr/local/zulu8/
export HADOOP_CONF_DIR=/opt/hadoop-3.1.2/etc/hadoop

```

如果HDFS客户端更给了配置，需要将HADOOP_CONF_DIR配置到conf/hbase-env.sh文件中；
或者将hdfs-site.xml直接软连接到$HBASE_HOME/conf/下；
或者如果配置文件不是很多时，可以直接添加到hbase-site.xml中。

## 2.5 配置 hbase-site.xml
```bash
vim $HBASE_HOME/conf/hbase-site.xml

```

配置如下（其中的路径可以不用创建，HBase会帮助我们自动创建）：
```xml
<configuration>
  <property>
    <name>hbase.rootdir</name>
    <value>hdfs://cdh6:8020/hbase</value>
  </property>
  <property>
    <name>hbase.cluster.distributed</name>
    <value>true</value>
  </property>
  <property>
    <name>hbase.zookeeper.quorum</name>
    <value>node-a.test.com,node-b.test.com,node-c.test.com</value>
  </property>
  <property>
    <name>hbase.zookeeper.property.dataDir</name>
    <value>/opt/zookeeper-3.4.14</value>
    <description>指定zk路径</description>
  </property>
</configuration>

```

## 2.6 配置 regionservers
```bash
vim $HBASE_HOME/conf/regionservers

```

将各个regionservers的地址填入文件保存：
```bash
node-a.test.com
node-b.test.com
node-c.test.com

```

## 2.7 分发配置好的包
将前面配置好的HBase的scp到各个节点
```bash
scp -r $HBASE_HOME/ root@node-b.test.com:/opt/
scp -r $HBASE_HOME/ root@node-c.test.com:/opt/

```

## 2.8 启动并确认安装
```bash
#启动,
$HBASE_HOME/bin/start-hbase.sh

```

查看启动的进程，主节点(node-a.test.com)有两个进程`HMaster`和`ResourceManager`，从节点(node-b.test.com、node-c.test.com)只有`ResourceManager`进程。
```bash
1786 HMaster
6666 ResourceManager
```

访问UI：[node-a.test.com:16010](http://node-a.test.com:16010)


## 2.9 关闭
如果需要关闭，则执行如下命令：
```bash
$HBASE_HOME/bin/stop-hbase.sh

```

# Using
在HBase任意一个节点中执行` hbase shell `进入HBase的shell


```bash



```




























