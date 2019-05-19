Presto -- Distributed SQL Query Engine for Big Data
====
[官网](https://prestodb.github.io/)
[项目源码 - GitHub](https://github.com/prestodb/presto)
[可参考官方文档](https://prestodb.github.io/docs/current/)

# 1 Presto 概述

Presto 是一个在 Facebook 主持下运营的开源项目。Presto是一种旨在使用分布式查询有效查询大量数据的工具，Presto是专门为大数据实时查询计算呢而设计和开发的产品，其为基于 Java 开发的，对使用者和开发者而言易于学习。
Presto有时被社区的许多成员称为数据库，官方也强调 Presto 不是通用关系数据库，它不是 MySQL，PostgreSQL 或 Oracle 等数据库的替代品，Presto 不是为处理在线事务处理（OLTP）而设计的。

如果您使用太字节或数PB的数据，您可能会使用与 Hadoop 和 HDFS 交互的工具。Presto 被设计为使用 MapReduce 作业（如Hive或Pig）管道查询 HDFS 的工具的替代方案，但 Presto不限于访问HDFS。
Presto 可以并且已经扩展到可以在不同类型的数据源上运行，包括传统的关系数据库和其他数据源像 Cassandra。Presto旨在处理数据仓库和分析：数据分析，聚合大量数据和生成报告。这些工作负载通常归类为在线分析处理（OLAP）。


Presto的基本概念如下：

1. Coordinator
Presto coordinator 是负责解析语句、查询计划和管理 Presto Worker 节点的服务器。它是 Presto 安装的“大脑”，也是客户端连接以提交语句以供执行的节点。
每个 Presto 安装必须有一个 Presto Coordinator 和一个或多个 Presto Worker。出于开发或测试目的，可以将单个 Presto 实例配置作为这两个角色。
协调器跟踪每个 Worker 的活动并协调查询的执行。Coordinator 创建一个涉及一系列阶段的查询的逻辑模型，然后将其转换为在 Presto 工作集群上运行的一系列连接任务。
Coordinator 使用 REST API 与 Worker 和客户进行通信。
   
2. Worker
Presto worker 是 Presto 安装中的负责执行任务和处理数据服务器。Worker 从连接器获取数据并相互交换中间数据。Coordinator 负责从工人那里获取结果并将最终结果返回给客户。
当Presto工作进程启动时，它会将自己通告给 Coordinator 中的发现服务器，这使 Presto Coordinator可以执行任务。
Worker 使用 REST API 与其他 Worker 和 Presto Coordinator 进行通信。

3. Connector
Connector 将 Presto 适配到数据源，如Hive或关系数据库。您可以将 Connector 视为与数据库驱动程序相同的方式。它是 Presto SPI 的一个实现，它允许 Presto 使用标准 API 与资源进行交互。
Presto 包含几个内置连接器：一个用于 JMX 的连接器 ，一个提供对内置系统表的访问的系统连接器，一个Hive连接器和一个用于提供 TPC-H 基准数据的TPCH连接器。
许多第三方开发人员都提供了连接器，以便 Presto 可以访问各种数据源中的数据。每个 Catalog 都与特定 Connector 相关联。如果检查 Catalog 配置文件，您将看到每个都包含强制属性 connector.name，
Catalog 使用该属性为给定 catalog 创建 Connector。可以让多个 Catalog 使用相同的 Connector 来访问类似数据库的两个不同实例。例如，如果您有两个Hive集群，则可以在单个 Presto 集群中配置两个 Catalog，
这两个 Catalog 都使用 Hive Connector，允许您查询来自两个 Hive 集群的数据（即使在同一SQL查询中）。

4. Catalog
Presto Catalog 包含 Schemas，并通过 Connector 引用数据源。例如，您可以配置 JMX catalog 以通过 JMX Connector 提供对 JMX 信息的访问。
在 Presto 中运行 SQL 语句时，您将针对一个或多个 Catalog 运行它。Catalog 的其他示例包括用于连接到 Hive 数据源的 Hive 目录。
当在 Presto 中寻址一个表时，全限定的表名始终以 Catalog 为根。例如，一个全限定的表名称`hive.test_data.test` 将引用 Hive catalog中 test_data库中 test表。
Catalog 在存储在 Presto 配置目录中的属性文件中定义。

5. Schema
Schema 是组织表的一种方式。Schema 和 Catalog 一起定义了一组可以查询的表。使用 Presto 访问 Hive 或 MySQL 等关系数据库时，Schema 会转换为目标数据库中的相同概念。
其他类型的 Connector 可以选择以对底层数据源有意义的方式将表组织成 Schema。

6. Table
表是一组无序行，它们被组织成具有类型的命名列。这与任何关系数据库中的相同。从源数据到表的映射由 Connector 定义。

7. Statement
Presto 执行与 ANSI 兼容的 SQL 语句。当 Presto 文档引用语句时，它指的是 ANSI SQL 标准中定义的语句，它由子句(Clauses)，表达式(Expression)和断言(Predicate)组成。
Presto 为什么将语句(Statment)和查询(Query)概念分开呢？这是必要的，因为在 Presto 中，Statment 只是引用 SQL 语句的文本表示。执行语句时，Presto 会创建一个查询以及一个查询计划，然后该查询计划将分布在​​一系列 Presto Worker 进程中。

8. Query
当 Presto 解析语句时，它会将其转换为查询并创建分布式查询计划，然后将其实现为在 Presto Worker 进程上运行的一系列互连阶段。在 Presto 中检索有关查询的信息时，您会收到生成结果集以响应语句所涉及的每个组件的快照。
语句和查询之间的区别很简单。语句可以被认为是传递给 Presto 的 SQL 文本，而查询是指为实现该语句而实例化的配置和组件。查询包含 stages, tasks, splits, connectors以及协同工作以生成结果的其他组件和数据源。

9. Stage
当 Presto 执行查询时，它会通过将执行分解为具有层级关系的多个 Stage。例如，如果 Presto 需要聚合存储在 Hive 中的10亿行的数据，则可以通过创建根 State 来聚合其他几个阶段的输出，所有这些阶段都旨在实现分布式查询计划的不同部分。
包含查询的层级关系结构类似于树。每个查询都有一个根 Stage，负责聚合其他 Stage 的输出。阶段是 Coordinator 用于建模分布式查询计划的 Stage，但阶段本身不在 Presto Worker 进程上运行。

10. Task
如上一部分所述，Stage 为分布式查询计划的特定部分建模，但 Stage 本身不在 Presto Worker 进程上执行。要了解 Stage 的执行方式，您需要了解 Stage 是通过 Presto Worker 网络分发的一系列任务来实现的。
任务是 Presto 架构中的“work horse”，因为分布式查询计划被解构为一系列 Stage，然后将这些 Stage 转换为任务，然后执行或处理 split。Presto 任务具有输入和输出，正如一个 Stage 可以通过一系列任务并行执行，任务与一系列驱动程序并行执行。

11. Split
任务对拆分进行操作，拆分是较大数据集的一部分。分布式查询计划的最低级别的 Stage 通过 Connector 的拆分检索数据，而分布式查询计划的更高级别的中间阶段从其他阶段检索数据。
当 Presto 正在安排查询时，Coordinator 将查询 Connector 以获取可用于表的所有拆分的列表。Coordinator 跟踪哪些计算机正在运行哪些任务以及哪些任务正在处理哪些拆分。

12. Driver
任务包含一个或多个并行 Driver。Driver 对数据进行操作并组合运算符以生成输出，然后由任务聚合，然后将其传递到另一个 Stage 中的另一个任务。
Driver 是一系列操作符实例，或者您可以将 Driver 视为内存中的一组物理运算符。它是 Presto 架构中最低级别的并行性。Driver 有一个输入和一个输出。

13. Operator
Operator消费、转换和生成数据。例如，表扫描从 Connector 获取数据并生成可由其他运算符使用的数据，并且过滤器运算符通过在输入数据上应用来使用 Predicate 数据并生成子集。

14. Exchange
交换在 Presto 节点之间传输数据以用于查询的不同阶段。任务使用交换客户端将数据生成到输出缓冲区并使用其他任务中的数据。


# 2. Presto 安装
可参考官方文档：[2.1. Deploying Presto](https://prestodb.github.io/docs/current/)

## 2.1 准备
安装前准备： 
* 保证集群各节点已经建立了 SSH 信任
* 安装 Java (JDK 1.8以上)
* 主节点： 
    - Git：可以访问 https://mirrors.edge.kernel.org/pub/software/scm/git/ 下载安装。
    - Maven
* 必要的数据源库：例如 Mysql、Postgresql、Elasticsearch、Redis、MongoDB、Hive、Kafka、Kudu、Cassandra 等

下面以单节点伪分布式方式安装部署  

Host名 | 部署服务
:----: | ----
cdh6 | Presto - Coordinator
cdh6 | Presto - Worker

## 2.2 安装包的获取
可以直接下载官网提供编译好的包进行安装 [presto-server-0.190.tar.gz](https://repo1.maven.org/maven2/com/facebook/presto/presto-server/0.190/presto-server-0.190.tar.gz)。

本次以编译源码的方式获取需要安装的文件：  
以源码编译方式安装，访问 [presto-releases](https://github.com/prestodb/presto/releases)，现在需要的版本的源码包。  
通过下载release版源码包编译会出现问题，原因是这种方式缺少 `.git目录和文件` 
```
[INFO] BUILD FAILURE
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  02:19 min (Wall Clock)
[INFO] Finished at: 2019-05-12T02:57:56+08:00
[INFO] ------------------------------------------------------------------------
Downloaded from nexus-aliyun: http://maven.aliyun.com/nexus/content/groups/public/io/airlift/units/1.3/units-1.3.jar (18 kB at 129 kB/s)
Downloading from nexus-aliyun: http://maven.aliyun.com/nexus/content/groups/public/org/apache/commons/commons-lang3/3.4/commons-lang3-3.4.jar
[ERROR] Failed to execute goal pl.project13.maven:git-commit-id-plugin:2.1.13:revision (default) on project presto-matching: .git directory could not be found! Please specify a valid [dotGitDirectory] in your pom.xml -> [Help 1]
[ERROR]
[ERROR] To see the full stack trace of the errors, re-run Maven with the -e switch.
[ERROR] Re-run Maven using the -X switch to enable full debug logging.
[ERROR]
[ERROR] For more information about the errors and possible solutions, please read the following articles:
[ERROR] [Help 1] http://cwiki.apache.org/confluence/display/MAVEN/MojoExecutionException
[ERROR]
[ERROR] After correcting the problems, you can resume the build with the command
[ERROR]   mvn <goals> -rf :presto-matching
[root@cdh6 presto-0.219]#
```


因此采用 clone 源码方式安装，这种方式可以切换标签，编译官方发布的任意我们需要的版本。
```bash
git clone https://github.com/prestodb/presto.git
git tag
# 可以看到不同的版本，找到最新的 0.219
git checkout tags/0.219
# 可以看到已经切换到此分支了
git branch

# 编译
mvn -T2C install -DskipTests

```

等待一段时间后，显示如下内容则表示编译成功
```
[INFO] ------------------------------------------------------------------------
[INFO] Reactor Summary for presto-root 0.219:
[INFO]
[INFO] presto-root ........................................ SUCCESS [ 11.575 s]
[INFO] presto-spi ......................................... SUCCESS [ 30.813 s]
[INFO] presto-plugin-toolkit .............................. SUCCESS [  3.953 s]
[INFO] presto-client ...................................... SUCCESS [  8.612 s]
[INFO] presto-parser ...................................... SUCCESS [ 29.083 s]
[INFO] presto-geospatial-toolkit .......................... SUCCESS [ 28.109 s]
[INFO] presto-array ....................................... SUCCESS [  2.433 s]
[INFO] presto-matching .................................... SUCCESS [ 24.025 s]
[INFO] presto-memory-context .............................. SUCCESS [ 24.019 s]
[INFO] presto-tpch ........................................ SUCCESS [  9.667 s]
[INFO] presto-main ........................................ SUCCESS [01:36 min]
[INFO] presto-resource-group-managers ..................... SUCCESS [ 42.605 s]
[INFO] presto-tests ....................................... SUCCESS [ 23.371 s]
[INFO] presto-atop ........................................ SUCCESS [01:03 min]
[INFO] presto-jmx ......................................... SUCCESS [01:03 min]
[INFO] presto-record-decoder .............................. SUCCESS [ 33.159 s]
[INFO] presto-kafka ....................................... SUCCESS [01:13 min]
[INFO] presto-redis ....................................... SUCCESS [01:03 min]
[INFO] presto-accumulo .................................... SUCCESS [01:13 min]
[INFO] presto-cassandra ................................... SUCCESS [01:13 min]
[INFO] presto-blackhole ................................... SUCCESS [01:02 min]
[INFO] presto-memory ...................................... SUCCESS [01:02 min]
[INFO] presto-orc ......................................... SUCCESS [01:03 min]
[INFO] presto-benchmark ................................... SUCCESS [ 14.930 s]
[INFO] presto-parquet ..................................... SUCCESS [  5.787 s]
[INFO] presto-rcfile ...................................... SUCCESS [ 42.634 s]
[INFO] presto-hive ........................................ SUCCESS [ 21.288 s]
[INFO] presto-hive-hadoop2 ................................ SUCCESS [  8.084 s]
[INFO] presto-teradata-functions .......................... SUCCESS [ 47.924 s]
[INFO] presto-example-http ................................ SUCCESS [ 33.494 s]
[INFO] presto-local-file .................................. SUCCESS [ 33.505 s]
[INFO] presto-tpcds ....................................... SUCCESS [01:03 min]
[INFO] presto-raptor ...................................... SUCCESS [ 12.072 s]
[INFO] presto-base-jdbc ................................... SUCCESS [  4.513 s]
[INFO] presto-mysql ....................................... SUCCESS [ 25.919 s]
[INFO] presto-postgresql .................................. SUCCESS [ 15.107 s]
[INFO] presto-redshift .................................... SUCCESS [ 15.102 s]
[INFO] presto-sqlserver ................................... SUCCESS [ 15.086 s]
[INFO] presto-mongodb ..................................... SUCCESS [01:02 min]
[INFO] presto-ml .......................................... SUCCESS [01:02 min]
[INFO] presto-geospatial .................................. SUCCESS [  5.922 s]
[INFO] presto-jdbc ........................................ SUCCESS [ 22.349 s]
[INFO] presto-cli ......................................... SUCCESS [  9.009 s]
[INFO] presto-product-tests ............................... SUCCESS [01:04 min]
[INFO] presto-benchmark-driver ............................ SUCCESS [ 10.863 s]
[INFO] presto-password-authenticators ..................... SUCCESS [ 10.366 s]
[INFO] presto-session-property-managers ................... SUCCESS [ 10.463 s]
[INFO] presto-kudu ........................................ SUCCESS [01:13 min]
[INFO] presto-thrift-connector-api ........................ SUCCESS [ 34.061 s]
[INFO] presto-thrift-testing-server ....................... SUCCESS [01:21 min]
[INFO] presto-thrift-connector ............................ SUCCESS [  3.641 s]
[INFO] presto-elasticsearch ............................... SUCCESS [01:09 min]
[INFO] presto-server ...................................... SUCCESS [ 53.982 s]
[INFO] presto-server-rpm .................................. SUCCESS [01:08 min]
[INFO] presto-docs ........................................ SUCCESS [01:54 min]
[INFO] presto-verifier .................................... SUCCESS [ 31.352 s]
[INFO] presto-testing-server-launcher ..................... SUCCESS [ 25.514 s]
[INFO] presto-benchto-benchmarks .......................... SUCCESS [ 42.340 s]
[INFO] presto-proxy ....................................... SUCCESS [  6.275 s]
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  07:32 min (Wall Clock)
[INFO] Finished at: 2019-05-11T13:40:14+08:00
[INFO] ------------------------------------------------------------------------
```

在 `presto` 目录下 presto/presto-server/target 可看到刚编译好的 `presto-server-0.219.tar.gz`，后续会使用这个包部署 Presto 集群。


## 2.3 Presto 安装与部署
Presto 集群部署分为三个部分：
* ①Presto服务部署；
* ②Presto-cli客户端部署；
* ③Presto JDBC使用

### 2.3.1 将 presto-server-0.219.tar.gz 传输到安装位置
```bash
cp presto/presto-server/target/presto-server-0.219.tar.gz /opt/
tar -zxvf presto-server-0.219.tar.gz

```

### 2.3.2 创建配置文件
配置文件配置可以参考 [presto-deployment](https://prestodb.github.io/docs/current/installation/deployment.html)

解压后 `presto-server-0.219`下没有`etc` 目录，需要手动创建。

可以到前面`clone`的源码目录下cp一份配置文件：
```bash
cp -r presto/presto-main/etc /opt/presto-server-0.219/
cp presto/presto-server-rpm/src/main/resources/dist/config/node.properties /opt/presto-server-0.219/etc/
``` 

配置文件 `presto-server-0.219/etc`  的文件目录结构如下
```
-etc
--catalog
---hive.properties
---jmx.properties
---memory.properties
---mysql.properties
--config.properties
--jvm.config
--log.properties
--node.properties   包含特定于每个节点的配置
```

### 2.3.3 修改配置文件
分别配置如下配置文件：
#### 1. node.properties
```bash
node.environment=test
node.id=db519624-18e8-4ee5-af30-a6545d10fcdb
node.data-dir=/var/lib/presto/data
catalog.config-dir=/etc/presto/catalog
plugin.dir=/usr/lib/presto/lib/plugin
node.server-log-file=/var/log/presto/server.log
node.launcher-log-file=/var/log/presto/launcher.log
```
**node.environment:** Presto运行环境名称。属于同一个Prosto集群的节点必须有相同的环境名称。  
**node.id:** Presto集群中的每个node的唯一标识。属于同一个Presto集群的各个Presto的node节点标识必须不同，可以以uuid的值指定属于该属性的值，linux可以用`uuidgen`生成。  
**node.data-dir:** 在每个Presto Node所在服务器的操作系统中的路径，Presto会在该路径下存放日志和其他的Presto数据。  

#### 2. config.properties
该配置文件的配置项应用于每个Presto的服务进程，每个Presto服务进程既可作为Conrdinator，也可作为Worker.
```bash
# Conrdinator & Wroker
coordinator=true
node-scheduler.include-coordinator=true
http-server.http.port=8080
query.max-memory=2GB
query.max-memory-per-node=2GB
query.max-total-memory-per-node=2GB
discovery-server.enabled=true
discovery.uri=http://cdh6:8080

exchange.http-client.connect-timeout=1m
exchange.http-client.idle-timeout=1m
exchange.http-client.max-connections=1000
exchange.http-client.max-connections-per-server=1000
scheduler.http-client.max-connections=1000
scheduler.http-client.max-connections-per-server=1000
scheduler.http-client.connect-timeout=1m
scheduler.http-client.idle-timeout=1m
query.client.timeout=5m
query.min-expire-age=30m
presto.version=testversion

## You may also wish to set the following properties:
# Specifies the port for the JMX RMI registry. JMX clients should connect to this port.
#jmx.rmiregistry.port: 
# Specifies the port for the JMX RMI server. Presto exports many metrics that are useful for monitoring via JMX.
#jmx.rmiserver.port: 

# distributed-joins-enabled=true
distributed-index-joins-enabled=true
# exchange.min-error.duration=10.00m
exchange.client-threads=8
# node-scheduler.multiple-tasks-per-node-enabled=true
optimizer.optimize-hash-generation=true
# optimizer.optimize-single-distinct=false
# query.remote-task.max-consecutive-error-count=20
# 已过期
# query.remote-task.min-error-duration=10m
# query.initial-hash-partitions=2 已经由query.hash-partition-count=2替换
query.hash-partition-count=2
#query.queue-config-file=etc/queues.json
# task.http-notification-threads=8
```

**coordinator:** Presto集群中的当前节点是否作为Coordinator，即当前节点可以接受来自客户端的查询请求，并且管理查询的执行过程。
**node-scheduler.include-coordinator:** 是否允许在Coordinator上执行计算任务，若允许计算计算任务，则Coordinator节点除了接受客户端的查询请求、管理查询的执行过程，还需要执行普通的计算任务。  
**http-server.http.port:** 指定HTTP Server的端口号，Presot通过HTTP协议进行所有的内部和外部通信。  
**query.max-memory:** 一个单独的计算任务使用的最大内存数。内存的大小直接限制了可以运行Order By 计算的行数。该配置应该基于查询复杂度、查询的数据量，以及查询的并发数进行设置。  
**query.max-memory-per-node:** 查询可在任何一台计算机上使用的最大用户内存量。  
**query.max-total-memory-per-node:** 查询可在任何一台计算机上使用的最大用户和系统内存量，其中系统内存是读取器、写入程序和网络缓冲区等执行期间使用的内存。  
**discovery-server.enabled:**  Presto使用Discover服务来查找集群中的所有节点，每个Presto实例都会在启动时使用Discovery服务注册自己。为了简化部署可以在Coordinator节点启动的时候启动Discover服务。  
**discovery.uri:** Discovery服务器的URI。因为我们已经将Discovery嵌入了Presto Coordinator服务中，因此它应该是Presto Coordinator的URI。注意，改URI不要以`/`结尾。  

#### 3. jvm.config
Presto是Java语言开发的，每个Presto服务进程都是运行在JVM之上的，因此需要在配置文件中指定Presto服务进程的Java运行环境。
改配置文件中包含了一系列启动Java虚拟机时所需要的命令行参数。  
在集群中Coordinator和Worker上的JVM配置文件是一样的。
```bash
#
# WARNING
# ^^^^^^^
# This configuration file is for development only and should NOT be used be
# used in production. For example configuration, see the Presto documentation.
#
-server
-Xmx16G
-XX:+UseG1GC
-XX:+ExplicitGCInvokesConcurrent
-XX:+AggressiveOpts
-XX:+HeapDumpOnOutOfMemoryError
-XX:OnOutOfMemoryError=kill -9 %p
-XX:InitiatingHeapOccupancyPercent=75
-XX:MaxGCPauseMillis=600
-XX:G1HeapRegionSize=32M
-XX:+UseGCOverheadLimit
-XX:+ExitOnOutOfMemoryError
```

#### 4. log.properties
在改配置文件中设置Logger的最小日志级别。 
```bash
com.facebook.presto=INFO
com.sun.jersey.guice.spi.container.GuiceComponentProviderFactory=WARN
com.ning.http.client=WARN
com.facebook.presto.server.PluginManager=DEBUG
```

#### 5.1 Catalog 配置
Presto通过 Connector 来访问数据，一种类型的数据源与一种而类型的Connector对应。
在Presto实际使用过程中，会根据实际的业务需要建立一个或者多个Catalog，而每种Catalog都有一个特定类型的Connector与之对应。
更多的Connector可以访问 [Connectors](http://prestodb.github.io/docs/current/connector.html)

1. 以Hive为例，在`ect/catalog/` 下配置`hive.properties`如下：
```bash
connector.name=hive-hadoop2
# Hive Metastore 服务器端口 
hive.metastore.uri=thrift://cdh3:9083
hive.config.resources=/etc/hadoop/conf/core-site.xml,/etc/hadoop/conf/hdfs-site.xml,/etc/hadoop/conf/mapred-site.xml
# 是否允许在Presto中删除Hive中的表
hive.allow-drop-table=true
# 是否允许在Presto中重命名Hive中的表
hive.allow-rename-table=true

```

2. Mysql配置`mysql.properties`
```bash
connector.name=mysql
connection-url=jdbc:mysql://mysql:13306
connection-user=root
connection-password=swarm
```

2. Kafka配置
...


### 2.3.4 启动和停止服务
到每个节点，通过运行以下命令将Presto作为守护程序启动：
```bash
[root@cdh6 presto-server-0.219]# bin/launcher start
Started as 30065
[root@cdh6 presto-server-0.219]# ./launcher status
Running as 30065
[root@cdh6 presto-server-0.219]# jps
30065 PrestoServer
```

查看日志，进入到`node.properties`配置的`node.data-dir`目录下找到`server.log`查看日志，如下标识启动成功。 
```
2019-05-18T21:41:04.405+0800    INFO    main    com.facebook.presto.security.AccessControlManager       -- Loaded system access control allow-all --
2019-05-18T21:41:04.503+0800    INFO    main    com.facebook.presto.server.PrestoServer ======== SERVER STARTED ========
```

停止
```bash
bin/launcher stop
```

### 2.3.5 客户端部署
前面已经成功完成了Presto集群服务的安装、部署和启动，之后可以通过Presto的客户端访问集群并执行数据查询和计算了。

#### 1. 部署 cli 端
Presto的客户端是由子工程 `presto-cli` 编译得到，编译后的文件为 `presto-cli-0.219-executable.jar` ，
也可以直接下载官网编译好的 [presto-cli-0.219-executable.jar](https://repo1.maven.org/maven2/com/facebook/presto/presto-cli/0.219/presto-cli-0.219-executable.jar)
将此喷溅拷贝到安装的目录，例如拷贝到 `presto-server-0.219/bin/` 下
```bash
[root@cdh6 presto-server-0.219]# cp ../presto/presto-cli/target/presto-cli-0.219-executable.jar bin/
# 修改权限
chmod 755 presto-cli-0.219-executable.jar
# 获取帮助
bin/presto-cli-0.219-executable.jar --help
```

#### 2. 启动 PResto CLI
```bash
bin/presto-cli-0.219-executable.jar --server cdh6:8080 --catalog hive --schema default
```

在hvie中 default 库下执行如下查询
```sql
hive> select count(*) from orders;
Query ID = root_20190519224242_5b036552-9162-4aad-bdcc-6ea75e244985
Total jobs = 1
Launching Job 1 out of 1
Number of reduce tasks determined at compile time: 1
In order to change the average load for a reducer (in bytes):
  set hive.exec.reducers.bytes.per.reducer=<number>
In order to limit the maximum number of reducers:
  set hive.exec.reducers.max=<number>
In order to set a constant number of reducers:
  set mapreduce.job.reduces=<number>
Starting Job = job_1556437415909_0006, Tracking URL = http://cdh3:8088/proxy/application_1556437415909_0006/
Kill Command = /opt/cloudera/parcels/CDH-5.16.1-1.cdh5.16.1.p0.3/lib/hadoop/bin/hadoop job  -kill job_1556437415909_0006
Hadoop job information for Stage-1: number of mappers: 1; number of reducers: 1
2019-05-18 22:42:25,603 Stage-1 map = 0%,  reduce = 0%
2019-05-18 22:42:30,817 Stage-1 map = 100%,  reduce = 0%, Cumulative CPU 1.99 sec
2019-05-18 22:42:41,179 Stage-1 map = 100%,  reduce = 100%, Cumulative CPU 4.12 sec
MapReduce Total cumulative CPU time: 4 seconds 120 msec
Ended Job = job_1556437415909_0006
MapReduce Jobs Launched:
Stage-Stage-1: Map: 1  Reduce: 1   Cumulative CPU: 4.12 sec   HDFS Read: 8143 HDFS Write: 2 SUCCESS
Total MapReduce CPU Time Spent: 4 seconds 120 msec
OK
6
Time taken: 27.128 seconds, Fetched: 1 row(s)
```

在Presto 执行同样的查询
```sql
presto:default> select count(*) from orders;
 _col0
-------
     6
(1 row)
Query 20190519_144201_00004_iy67z, FINISHED, 1 node
Splits: 18 total, 18 done (100.00%)
0:01 [6 rows, 318B] [6 rows/s, 318B/s]
```

### 2.3.6 JDBC 连接 Presto
#### 1.新建项目
在 idea 创建一个maven项目，

#### 2. 在`pom.xml`中引入依赖
```xml
    <dependencies>
        <!-- 0.219 com.facebook.presto已移至 io.prestosql.presto-jdbc -->
        <dependency>
            <groupId>io.prestosql</groupId>
            <artifactId>presto-jdbc</artifactId>
            <version>310</version>
        </dependency>
    </dependencies>
```

#### 3. 编写代码
创建java类，编写Presto jdbc client代码
详细代码查看：[PrestoJDBCClient.java](src/main/java/yore/PrestoJDBCClient.java)



