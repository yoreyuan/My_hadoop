Apache Druid (incubating) 安装及使用
======
[官网](https://druid.apache.org/) &nbsp; &nbsp; | &nbsp; &nbsp; 
[GitHub](https://github.com/apache/incubator-druid/) &nbsp; &nbsp; | &nbsp; &nbsp;  
[Docs](https://druid.apache.org/docs/latest/design/) &nbsp; &nbsp; | &nbsp; &nbsp; 
[imply.io](https://imply.io/) &nbsp; &nbsp; | &nbsp; &nbsp; 
[druid 论文](http://static.druid.io/docs/druid.pdf)

# 一、Overview
## 1.1 概念
* **Segment**: Druid将索引数据保存到Segment文件中,Segment文件根据时间进行分 片。Segment中会保存维度、指标以及索引信息。
* **roll-up**: 数据预聚合，将一定时间粒度范围内、维度列相同的数据进行聚合

[Druid进程和服务](https://druid.apache.org/docs/latest/design/processes.html)。
* [Coordinator](https://druid.apache.org/docs/latest/design/coordinator.html) 进程管理群集上的数据可用性。从metastore中读取Segment的元数据,并决定哪些Segments需要被加载到集群中。使用ZooKeeper查看已经存在的历史节点都有哪些，了解集群各个节点负载情况。创建一个ZK的条目告诉历史节点加载、删除、或者移动Segments
* [Overlord](https://druid.apache.org/docs/latest/design/overlord.html) 进程控制数据提取工作负载的分配。
* [Broker](https://druid.apache.org/docs/latest/design/broker.html) 进程处理来自外部客户端的查询。负责将查询请求分发到历史节点和实时节点,并聚合这些节点返回的查询结果数据。Broker节点通过zookeeper知道Segment都存放在哪些节点上。
* [Router](https://druid.apache.org/docs/latest/development/router.html) 进程是可选的进程，可以将请求路由到Broker，Coordinator和Overlords。
* [Historical](https://druid.apache.org/docs/latest/design/historical.html) 进程存储可查询数据。提供对Segment的数据查询服务。与ZooKeeper通信，上报节点信息，告知ZK自己拥有哪些Segments。从ZooKeeper中获取执行任务。
* [MiddleManager](https://druid.apache.org/docs/latest/design/middlemanager.html) 进程负责提取数据。

我们常常根据线程的服务类型分为
* **Master**：运行Coordinator和Overlord进程，管理数据可用性和摄取。
* **Query**：运行Broker和可选的Router进程，处理来自外部客户端的查询。
* **Data**：运行Historical和MiddleManager进程，执行提取工作负载并存储所有可查询数据。

## 1.2 架构
![druid-architecture.png](https://druid.apache.org/docs/img/druid-architecture.png)


![druid-architecture2.png](https://github.com/yoreyuan/My_hadoop/blob/master/doc/image/druid-cluster-architecture.png?raw=true)

## 1.3 Druid 能做什么
Druid擅长的部分
* 对于大部分查询场景可以亚秒级响应
* 事件流实时写入与批量数据导入兼备
* 数据写入前预聚合节省存储空间，提升查询效率 • 水平扩容能力强
* 社区活跃

Druid短板的部分
* **不支持Join**
* **大数据量场景下明细查询有瓶颈**

我是否需要使用Druid?
* 处理时间序列事件
* 快速的聚合以及探索式分析
* 近实时分析亚秒级响应
* 存储大量(TB级、PB级)可以预先定义若干维度的事件 • 无单点问题的数据存储


# 二 安装

获取Druid安装包有以下几种方式
* **源代码编译**：[druid/release](https://github.com/apache/drill/releases)，主要用于定制化需求时，比如结合实际环境中的周边依赖，或者是加入支持特定查询的部分的优化必定等。
* **官网安装包下载**：[download](https://druid.apache.org/downloads.html)，包含Druid部署运行的最基本组件
* **Imply组合套件**：[Imply](https://imply.io/get-started)，该套件包含了稳定版本的Druid组件、实时数据写入支持服务、图形化展示查询Web UI和SQL查询支持组件等，目的是为更加方便、快速地部署搭建基于Druid的数据分析应用产品。


## 2.1 单机版安装（Single-Server ）

### 2.1.1 软件要求
* Java 8 (8u92+)
* Linux, Mac OS X, 或者其他的类Unix OS (Windows是不支持的)

### 2.1.2 硬件要求
Druid包括几个[单服务配置](https://github.com/yoreyuan/My_hadoop/blob/master/doc/Apache-Druid/Single-Server-Deployments.md)示例，以及使用这些配置启动Druid进程的脚本。

如果您在笔记本电脑等小型机器上运行以进行快速评估，那么`micro-quickstart`配置是一个不错的选择，适用于 4CPU/16GB RAM环境。

如果您计划在教程之外使用单机部署进行进一步评估，我们建议使用比`micro-quickstart`更大的配置。

### 2.1.3 下载官方安装包
[下载](https://druid.apache.org/downloads.html) [0.15.0-incubating](https://www.apache.org/dyn/closer.cgi?path=/incubator/druid/0.15.0-incubating/apache-druid-0.15.0-incubating-bin.tar.gz)版本。

通过在终端中运行以下命令来解压Druid：
```bash
tar -xzf apache-druid-0.15.0-incubating-bin.tar.gz
cd apache-druid-0.15.0-incubating
```

在包中，您应该找到：
* `DISCLAIMER`, `LICENSE`, 和 `NOTICE` 文件
* `bin/*` - 对快速入门有用的脚本
* `conf/*` - 单服务和群集设置的示例配置
* `extensions/*` - Druid核心扩展
* `hadoop-dependencies/*` - Druid Hadoop 依赖
* `lib/*` - Druid核心的库和依赖
* `quickstart/*` - 快速入门教程的配置文件，示例数据和其他文件

### 2.1.4 外部依赖
* **Deep Storage**（数据文件存储库）：这里使用HDFS，提前安装好[Hadoop](https://github.com/yoreyuan/My_hadoop/blob/master/doc/apache-hadoop.md#1-deploy)
* **Metadata Storage**（元数据库）： MySQL或者PostgreSQL，这里使用MySQL。
* **Zookeeper**（集群状态管理服务）：Druid依赖[Apache ZooKeeper](http://zookeeper.apache.org/)进行分布式协调。您需要下载并运行Zookeeper。

### 2.1.5 配置环境变量
配置如下，保存并推出。并最后使配置生效 `source ~/.bash_profile`
```bash
vim ~/.bash_profile
```

添加如下配置
```yaml
# Druid配置
export DRUID_HOME=/opt/apache-druid-0.15.0-incubating
export PATH=$PATH:$DRUID_HOME/bin
```

### 2.1.6 修改Druid配置文件
这里使用最低配置的配置`micro-quickstart`（Micro-Quickstart: 4 CPU, 16GB RAM）方式启动。启动的时候会加载`$DRUID_HOME/conf/supervise/single-server/micro-quickstart.conf`，配置文件的内容如下：
```yaml
:verify bin/verify-java
:verify bin/verify-default-ports
:kill-timeout 10

# !p数字，表示服务关闭的权重，默认值为50，数值越大越优先关闭。
!p10 zk bin/run-zk conf
coordinator-overlord bin/run-druid coordinator-overlord conf/druid/single-server/micro-quickstart
broker bin/run-druid broker conf/druid/single-server/micro-quickstart
router bin/run-druid router conf/druid/single-server/micro-quickstart
historical bin/run-druid historical conf/druid/single-server/micro-quickstart
!p90 middleManager bin/run-druid middleManager conf/druid/single-server/micro-quickstart

# Uncomment to use Tranquility Server
#!p95 tranquility-server tranquility/bin/tranquility server -configFile conf/tranquility/wikipedia-server.json -Ddruid.extensions.loadList=[]

```

#### Zookeeper(集成)
在程序包根目录中，运行以下命令：
```bash
cd $DRUID_HOME
curl https://archive.apache.org/dist/zookeeper/zookeeper-3.4.11/zookeeper-3.4.11.tar.gz -o zookeeper-3.4.11.tar.gz
tar -xzf zookeeper-3.4.11.tar.gz
mv zookeeper-3.4.11 zk
```

#### Zookeeper(外部)
如果使用外部已经存在的ZK，可以将`$DRUID_HOME/conf/supervise/single-server/micro-quickstart.conf`中`!p10 zk bin/run-zk conf`注释掉，然后修改配置

```bash
vim $DRUID_HOME/conf/druid/single-server/micro-quickstart/_common/common.runtime.properties

#
# Zookeeper，大概在46~55行中间，对zk进行配置
#
druid.zk.service.host=cdh1:2181,cdh2:2181,cdh3:2181
druid.zk.paths.base=/druid
```

#### 配置common.runtime.properties
编辑：`vim $DRUID_HOME/conf/druid/single-server/micro-quickstart/_common/common.runtime.properties`
```yaml
druid.extensions.loadList=["mysql-metadata-storage", "druid-hdfs-storage", "druid-kafka-indexing-service", "druid-datasketches"]

# 元数据Mysql配置
druid.metadata.storage.type=mysql
druid.metadata.storage.connector.connectURI=jdbc:mysql://cdh1:3306/druid?characterEncoding=UTF-8
druid.metadata.storage.connector.user=root
druid.metadata.storage.connector.password=123456

#
# Deep storage
#
druid.extensions.loadList=["druid-hdfs-storage"]

# For HDFS:
druid.storage.type=hdfs
druid.storage.storageDirectory=hdfs://cdh1:8020/druid/segments

#
# Indexing service logs
#
# For HDFS:
druid.indexer.logs.type=hdfs
druid.indexer.logs.directory=hdfs://cdh1:8020/druid/indexing-logs

```

#### 调整JVM（主要修改时区，调整为 UTC+0800 ）
默认`micro-quickstart`启动的JVM大小如下
* broker                512m    
* coordinator-overlord  256m
* historical：          512m
* middleManager         64m
* router                128m

修改 `$DRUID_HOME/conf/druid/single-server/micro-quickstart/` 下各个服务的 `jvm.config`，可以根据情况配置如下几种常用配置项。
```bash
-XX:+UseConcMarkSweepGC
-XX:+PrintGCDetails
-XX:+PrintGCTimeStamps
-XX:+ExitOnOutOfMemoryError
-Duser.timezone=UTC+0800
-Dfile.encoding=UTF-8
```

#### 如果配置的了深存储是HDFS
需要将Hadoop的配置文件：`core-site.xml`、`hdfs-site.xml`、`mapred-site.xml`、`yarn-site.xml`放置到`$DRUID_HOME/conf/druid/single-server/micro-quickstart/_common`目录下

#### 如果[配置了元数据库为MySQL](https://druid.apache.org/docs/latest/development/extensions-core/mysql.html)
需要手动创建出`druid.metadata.storage.connector.connectURI`指定的数据库
```bash
# 在MySQL服务器中执行命令，创建一个 druid 库
mysql -u <username> -e "CREATE DATABASE druid CHARACTER SET utf8 COLLATE utf8_general_ci" -p

```

**同时**，需要在`$DRUID_HOME/conf/druid/single-server/micro-quickstart/_common/common.runtime.properties` 中的配置项`druid.extensions.loadList`添加` ["mysql-metadata-storage"]`。
其他扩展参数可查看[extensions](https://druid.apache.org/docs/latest/development/extensions.html#core-extensions)

下载Mysql驱动(它的名字应该是mysql-connector-java-xxxx.jar)到`$DRUID_HOME/extensions/mysql-metadata-storage`目录下
```bash
wget https://repo1.maven.org/maven2/mysql/mysql-connector-java/5.1.47/mysql-connector-java-5.1.47.jar -P $DRUID_HOME/extensions/mysql-metadata-storage/
```

**注意**，这里如果使用的是MySQL数据库，可能因为版本不同，设置编码时会有问题，官方文档为`CREATE DATABASE druid DEFAULT CHARACTER SET utf8mb4;`，后面发下会报如下错误：
```
Caused by: org.skife.jdbi.v2.exceptions.UnableToExecuteStatementException: java.sql.BatchUpdateException: Specified key was too long; max key length is 767 bytes [statement:"null", located:"null", rewritten:"null", arguments:null]
	at org.skife.jdbi.v2.Batch.execute(Batch.java:131) ~[jdbi-2.63.1.jar:2.63.1]
	at org.apache.druid.metadata.SQLMetadataConnector$2.withHandle(SQLMetadataConnector.java:189) ~[druid-server-0.15.0-incubating.jar:0.15.0-incubating]
	at org.apache.druid.metadata.SQLMetadataConnector$2.withHandle(SQLMetadataConnector.java:179) ~[druid-server-0.15.0-incubating.jar:0.15.0-incubating]
	at org.skife.jdbi.v2.DBI.withHandle(DBI.java:281) ~[jdbi-2.63.1.jar:2.63.1]
	... 15 more
```
因此创建元数据库时，最好设置为`utf8_general_ci`编码。

### 2.1.7 启动Druid服务
启动前请确保如下端口没有占用
服务  |   简介  | 端口
:---- | :---- | :----: 
Zookeeper   | 分布式协调服务   | -- / 2181
Coordinator | 协调节点，管理集群状态   | 8081
broker      | 查询节点，处理查询请求   | 8082
historical  | 历史节点，管理历史数据   | 8083
overlord    | 统治节点，管理数据写入任务 | 8090
middleManager | 中间管理者，负责写数据处理 | --



启动Drui服务的命令如下
```bash
# 创建一个日志的目录，用来保存启动日志
mkdir $DRUID_HOME/logs
nohup $DRUID_HOME/bin/start-micro-quickstart > ${DRUID_HOME}/logs/start-micro-quickstart.log &

```

启动的时候可以查看日志。
```bash
 tail -f ${DRUID_HOME}/logs/start-micro-quickstart.log
```

如果显示如下日志则启动成功
```
[Fri May  3 11:40:50 2019] Running command[coordinator-overlord], logging to[/opt/apache-druid-0.15.0-incubating/var/sv/coordinator-overlord.log]: bin/run-druid coordinator-overlord conf/druid/single-server/micro-quickstart
[Fri May  3 11:40:50 2019] Running command[broker], logging to[/opt/apache-druid-0.15.0-incubating/var/sv/broker.log]: bin/run-druid broker conf/druid/single-server/micro-quickstart
[Fri May  3 11:40:50 2019] Running command[router], logging to[/opt/apache-druid-0.15.0-incubating/var/sv/router.log]: bin/run-druid router conf/druid/single-server/micro-quickstart
[Fri May  3 11:40:50 2019] Running command[historical], logging to[/opt/apache-druid-0.15.0-incubating/var/sv/historical.log]: bin/run-druid historical conf/druid/single-server/micro-quickstart
[Fri May  3 11:40:50 2019] Running command[middleManager], logging to[/opt/apache-druid-0.15.0-incubating/var/sv/middleManager.log]: bin/run-druid middleManager conf/druid/single-server/micro-quickstart
[Fri May  3 11:40:50 2019] Command[coordinator-overlord] exited (pid = 22702, exited = 1)
[Fri May  3 11:40:50 2019] Command[coordinator-overlord] failed, see logfile for more details: /opt/apache-druid-0.15.0-incubating/var/sv/coordinator-overlord.log
[Fri May  3 11:40:50 2019] Running command[coordinator-overlord], logging to[/opt/apache-druid-0.15.0-incubating/var/sv/coordinator-overlord.log]: bin/run-druid coordinator-overlord conf/druid/single-server/micro-quickstart

```


### 2.1.8 启动的错误解决
#### 端口被占用
如果日志中出现一些错误，可以根据提示解决，最常见的错误如下
```
Cannot start up because port[8081] is already in use.
```

请修改 `$DRUID_HOME/conf/druid/single-server/micro-quickstart/` 下的对应服务的`runtime.properties`的端口号，
例如这里8081端口被占用，说明是Coordinator服务端口需要修改，
```bash
$DRUID_HOME/conf/druid/single-server/micro-quickstart/coordinator-overlord/runtime.properties

# 修改如下配置
druid.plaintextPort=8085

```

同时因为`micro-quickstart`启动时会进行端口验证，因此也需要修改`$DRUID_HOME/bin/druid/verify-default-ports`
```yaml
# 将8081改为8085
#my @ports = (1527, 2181, 8081, 8082, 8083, 8090, 8091, 8200, 9095);
my @ports = (1527, 2181, 8085, 8082, 8083, 8090, 8091, 8200, 9095);

```

#### `Command[coordinator-overlord] failed`
此时根据提示查看 `$DRUID_HOME/var/sv/coordinator-overlord.log`，进行异常解决。

### 2.1.9 关闭Druid服务
```bash
$DRUID_HOME/bin/service --down

```

## 2.2 Imply组合套件方式部署

### 2.2.1 下载
浏览器访问 [https://imply.io/get-started](https://imply.io/get-started)，选择右侧的`Download Imply On-prem`方式下载，
可以不用注册账号，填写姓名、组织、**公司邮箱**即可下载，

如果不想输入个人信息，可以使用这个连接下载老版本的 
```bash
curl -O https://static.imply.io/release/imply-2.7.10.tar.gz
```

### 2.2.2 安装
**说明**：这个套件中`pivot`试用期30天。

可以查看官方文档，[quickstart](https://docs.imply.io/on-prem/quickstart)，这个官方文档是比较详细的。

这里推荐[《Druid实时大数据分析原理与实战》](https://item.jd.com/12041307.html)这本书，书中「**第四章：安装与配置**」，介绍的比较详细

因此这里就不详细说明这方方式的安装过程了。


## 3 体验

### 3.1 转到浏览器
群集启动后，所有Druid进程都需要几秒钟才能完全启动。过上几秒后，我们就可以浏览器上转到[http//cdh6:8888](http//localhost:8888)页面。
服务于Druid控制台的[Druid路由进程](https://druid.apache.org/docs/latest/development/router.html)驻留在此地址。

### 3.2 继续
* 导入测试数据时，因为安装包已经有一份demo数据，可以选择本地
    - Firehose type: `local`
    - Base directory: `quickstart/tutorial/`
    - File filter : `wikiticker-2015-09-12-sampled.json.gz`
* 如果提交完了之后Task失败，如果是HDFS，请查看配置的Segment路径的权限，如果没有写的权限，请赋予写的权限：`hadoop fs -chmod +w /druid/segments`


这部分可以查看官网
* [Single-Server Quickstart](https://druid.apache.org/docs/latest/tutorials/index.html) 
* 或者 [tutorials/index.md](https://github.com/apache/incubator-druid/blob/master/docs/content/tutorials/index.md#loading-data)，
* [Tutorial: Loading a file](https://druid.apache.org/docs/latest/tutorials/tutorial-batch.html)

也可以查看我的官网文档的一份译文
* [Getting Started](https://github.com/yoreyuan/My_hadoop/blob/master/doc/Apache-Druid/Getting-Started.md#05-loading-data-%E5%8A%A0%E8%BD%BD%E6%95%B0%E6%8D%AE)


<br/><br/>

my blog [Apache Druid (incubating) 安装及使用](https://blog.csdn.net/github_39577257/article/details/96803834)


