Getting Started (入门)
======

# 1 [Design](https://druid.apache.org/docs/latest/design/index.html) （设计） 
## 1.1 What is Druid?（什么是Druid？）
Apache Druid（孵化）是一个实时分析数据库，专为大型数据集上的快速 slice-and-dice 分析（“OLAP”查询）而设计。 Druid最常被用作数据库，用于为实时摄取、
快速查询性能和很重要的高正常运行时间的用例提供支持。因此Druid通常用于为分析应用程序的GUI（Graphical User Interface）提供动力，或者作为需要快速聚合的高度并发API的后端。Druid最适合面向事件的数据。

Druid的常见应用领域包括：
* 点击流分析（web和移动端分析）
* 网络遥测分析（网络性能监控）
* 服务器指标存储
* 供应链分析（产生指标）
* 应用性能指标
* 数字营销/广告分析
* 商业智能/OLAP

Druid的核心架构结合了数据仓库、时间序列数据库和日志搜索系统的理念。Druid的一些主要特点是：
1. **列式存储格式**： Druid使用面向列的存储，这意味着它只需要加载特定查询所需的精确列。这为仅查看几列的查询提供了巨大的速度提升。此外，每列都针对其特定数据类型进行了优化，支持快速扫描和聚合。
2. **可扩展的分布式系**： Druid通常部署在数十到数百台服务器的集群中，可以提供数百万条记录/秒的摄取率，保留数万亿条记录，以及亚秒级到几秒钟的查询延迟。
3. **大规模并行处理**： Druid可以在整个集群中并行处理查询。
4. **实时或批量摄取**： Druid可以实时摄取数据（摄取的数据可立即用于查询）或批量摄取。
5. **自愈,自平衡,易于操作**： 作为操作者，要将群集扩缩，只需添加或删除服务器，群集将在后台自动重新平衡，无需任何停机时间。如果任何Druid服务器发生故障，系统将自动路由绕过损坏，直到可以更换这些服务器。Druid旨在全天候运行，无需任何原因计划停机，包括配置更改和软件更新。
6. **云本机,容错架构将不会丢失数据**： 一旦Druid摄取了您的数据，副本就会安全地存储在[深存储](https://druid.apache.org/docs/latest/design/index.html#deep-storage)（通常是云存储，HDFS或共享文件系统）中。
即使每个Druid服务器都出现故障，您的数据也可以从外围存储中恢复。对于仅少数Druid服务非常有限的故障影响，复制可确保在系统恢复时仍可进行查询。
7. **用于快速过滤的索引**： Druid使用[CONCISE](https://arxiv.org/pdf/1004.0403)或[Roaring](https://roaringbitmap.org/)压缩位图索引来创建索引，这些索引可以跨多个列进行快速过滤和搜索。
8. **基于时间的分区**： Druid首先按时间划分数据，并且可以基于其他字段进行额外划分。这意味着基于时间的查询将仅访问与查询的时间范围匹配的分区。这导致基于时间的数据的性能显着改进。
9. **近似算法**： Druid包括用于近似去重计数，近似排序以及近似直方图和分位数的计算的算法。这些算法提供有限的内存使用，并且通常比精确计算快得多。对于精度比速度更重要的情况，Druid还提供精确去重计数且精确的排名。
10. **在摄取时自动汇总**： Druid可选择在摄取时支持数据汇总。此概要部分预先汇总了您的数据，可以节省大量成本并提高性能。


## 1.2 When should I use Druid?（什么时候应该使用Druid？）
如果您的用例符合以下几个描述符，Druid可能是一个不错的选择：
* 插入率非常高，但更新不常见。
* 您的大多数查询都是聚合和汇报查询（“group by”查询）。您可能还有搜索和扫描查询。
* 您将查询延迟定位为100毫秒到几秒。
* 您的数据有一个时间组件（Druid包含与时间特别相关的优化和设计选择）。
* 您可能有多个表，但每个查询只能访问一个大的分布式表。查询可能会遇到多个较小的“lookup”表。
* 您有高基数数据列（例如网址，用户ID），需要对它们进行快速计数和排名。
* 您希望从Kafka，HDFS，flat文件或对象存储（如Amazon S3）加载数据。

您可能不想使用Druid的情况包括：
* 您需要使用主键对现有记录进行低延迟更新。Druid支持流式插入，但不支持流式更新（使用后台批处理作业进行更新）。
* 您正在构建一个线下报告系统，其中查询延迟不是很重要。
* 你想做“大”连接（将一个大事实表连接到另一个大事实表），你可以花费几个小时完成这些查询。


## 1.3 Architecture（架构）
Druid拥有一个多进程、分布式架构，旨在实现 cloud-friendly 且易于操作。每个Druid流程类型都可以独立配置和扩展，为您的群集提供最大的灵活性。 
此设计还提供增强的容错能力：一个组件的中断不会立即影响其他组件。

### 1.3.1 Processes and Servers （进程和服务）
Druid有几种类型的进程，简要描述如下：
* [Coordinator](https://druid.apache.org/docs/latest/design/coordinator.html)进程管理群集上的数据可用性。
* [Overlord](https://druid.apache.org/docs/latest/design/overlord.html)进程控制数据提取工作负载的分配。
* [Broker](https://druid.apache.org/docs/latest/design/broker.html)进程处理来自外部客户端的查询。
* [Router](https://druid.apache.org/docs/latest/development/router.html)进程是可选的进程，可以将请求路由到Broker，Coordinator和Overlords。
* [Historical](https://druid.apache.org/docs/latest/design/historical.html)进程存储可查询数据。
* [MiddleManager](https://druid.apache.org/docs/latest/design/middlemanager.html)进程负责提取数据。

Druid进程可以按照您喜欢的方式进行部署，但为了便于部署，我们建议将它们组织为三种服务器类型：Master、Query 和 Data。

* **Master**：运行Coordinator和Overlord进程，管理数据可用性和摄取。
* **Query**：运行Broker和可选的Router进程，处理来自外部客户端的查询。
* **Data**：运行Historical和MiddleManager进程，执行提取工作负载并存储所有可查询数据。

有关进程和服务器组织的更多详细信息，请参阅[Druid进程和服务](https://druid.apache.org/docs/latest/design/processes.html)。

### 1.3.2 External dependencies （外部依赖）
除了内置的进程类型，Druid还有三个外部依赖项。 这些旨在能够利用现有的基础设施。

#### 1.3.2.1 Deep storage（深存储）
每个Druid服务都可以访问共享文件存储。这通常是像S3或HDFS这样的分布式对象存储，或者是网络安装的文件系统。Druid使用它来存储已被摄入系统的任何数据。

Druid仅将深存储用作数据的备份，并将其作为在Druid进程之间在后台传输数据的一种方式。要响应查询，Historical进程不会从深存储读取数据，而是在提供的任何查询服务之前从其本地磁盘读取预取的segments。
这意味着Druid在查询期间永远不需要访问深存储，从而帮助它提供最佳的查询延迟。这也意味着您必须在深存储和跨计划加载的数据的Historical进程中拥有足够的磁盘空间。

更多详细信息，请参阅[Deep storage dependency](https://druid.apache.org/docs/latest/dependencies/deep-storage.html)。

#### 1.3.2.2 Metadata storage（元数据存储）
元数据存储保存各种共享系统元数据，例如segment可用性信息和任务信息。这通常是传统的RDBMS，如PostgreSQL或MySQL。

更多详细信息，请参阅[Metadata storage dependency](https://druid.apache.org/docs/latest/dependencies/metadata-storage.html)。

#### 1.3.2.3 Zookeeper
用于内部服务发现，协调和leader选举。

更多详细信息，请参阅[Zookeeper dependency](https://druid.apache.org/docs/latest/dependencies/zookeeper.html)。

这种架构背后的想法是使Druid集群在生产中大规模运作变得简单。例如，深存储和元数据存储与集群其余部分的分离意味着Druid进程具有极大的容错能力：即使每个Druid服务器都出现故障，
您仍然可以从存储在深存储中的数据和元数据重新启动集群。

### 1.3.3 Architecture diagram （架构图）
下图显示了使用建议的 Master/Query/Data 服务器组织的查询和数据如何流经此体系结构：

![druid-architecture.png](https://druid.apache.org/docs/img/druid-architecture.png)


## 1.4 Datasources and segments（数据源和分片（Segments））
Druid数据存储在“datasources”中，类似于传统RDBMS中的表。每个datasource按时间划分，并可选择进一步按其他属性划分。每个时间范围称为“chunk”（例如，如果您的datasource按天分区，则为一天）。
在chunk内，数据被划分为一个或多个"[segments](https://druid.apache.org/docs/latest/design/segments.html)"。 每个segment都是单个文件，通常包含多达几百万行数据。
由于segments被组织成时间chunks，因此将segments视为如下所示的靠时间轴存活的有时会有所帮助：

![druid-timeline.png](https://druid.apache.org/docs/img/druid-timeline.png)

datasources可能只有几个segments，最多可达数十万甚至数百万个Segments。每个segment都开始在MiddleManager上创建生命，并且在那时，它是可变的和未提交的。
segment构建过程包括以下步骤，旨在生成紧凑的数据文件并支持快速查询：
* 转换为柱状格式
* 使用位图索引进行索引
* 使用各种算法进行压缩
    - 具有String列的id存储最小化的字典编码
    - 位图索引的位图压缩
    - 所有列的类型感知压缩

定期提交和发布segments。此时，它们被写入[深存储](https://druid.apache.org/docs/latest/design/index.html#deep-storage)，变为不可变，
并从MiddleManagers转移到Historical进程（有关详细信息，请参阅上面的[体系架构](https://druid.apache.org/docs/latest/design/index.html#architecture)）。
关于segment的条目也被写入[元数据存储](https://druid.apache.org/docs/latest/design/index.html#metadata-storage)。 此条目是关于segment的自描述元数据，
包括segment的架构，其大小以及它在深存储上的位置。 这些条目是Coordinator用于了解群集上应该有哪些数据可用。

## 1.5 Query processing（查询处理）
查询首先进入[Broker](https://druid.apache.org/docs/latest/design/broker.html)，Broker将识别哪些segments具有可能与该查询相关的数据。segment列表始终按时间进行修剪，
也可能会被其他属性修剪，具体取决于数据源的分区方式。 然后，Broker将识别哪些[Historicals](https://druid.apache.org/docs/latest/design/historical.html)和
[MiddleManagers](https://druid.apache.org/docs/latest/design/middlemanager.html)正在为这些segment提供服务，并向每个进程发送重写的子查询。 
Historical/MiddleManager进程将接受查询，处理它们并返回结果。Broker接收结果并将它们合并在一起以获得最终答案，并将其返回给原始调用者。

Broker修剪是Druid限制每个查询必须扫描的数据量的重要方式，但这不是唯一的方法。 对于比Broker可用于修剪更细粒度的过滤器，每个segment内的索引结构允许Druid在查看任何数据行之前确定哪些（如果有）行匹配过滤器集。
一旦Druid知道哪些行与特定查询匹配，它只访问该查询所需的特定列。在这些列中，Druid可以在行之间跳过，避免读取与查询过滤器不匹配的数据。

所以Druid使用三种不同的技术来最大化查询性能：
* 修剪为每个查询访问哪些segment。
* 在每个segment内，使用索引来标识必须访问的行。
* 在每个segment中，仅读取与特定查询相关的特定行和列。

## 1.6 External dependencies（外部依赖）

## 1.7 Ingestion overview（摄取概述）

<br/>

**********

<br/>

# 2 [Getting Started](https://druid.apache.org/docs/latest/operations/getting-started.html) （入门） 
Apache Druid入门（孵化中）

* Overview (概述)
如果您不熟悉Druid，我们建议您先阅读[Design Overview](https://github.com/yoreyuan/My_hadoop/blob/master/doc/Apache-Druid/Getting-Started.md#getting-started-%E5%85%A5%E9%97%A8)
和[Ingestion Overview](https://github.com/yoreyuan/My_hadoop/blob/master/doc/Apache-Druid/Data-Ingestion.md#1-ingestion-overview-%E6%91%84%E5%8F%96%E6%A6%82%E8%BF%B0)，以便对Druid有基本的了解。

* Single-server Quickstart and Tutorials （单服务快速入门和指南）
要开始运行Druid，最简单、最快捷的方法是尝试[single-server quickstart and tutorials](https://druid.apache.org/docs/latest/tutorials/index.html)。

* Deploying a Druid cluster （部署Druid集群） 
如果您希望直接将Druid部署为集群，或者您希望迁移现有单服务部署到集群部署，请参阅[Clustered Deployment Guide](https://druid.apache.org/docs/latest/tutorials/cluster.md)。

* 操作Druid
    - [Configuration Reference](https://druid.apache.org/docs/latest/configuration/index.html)描述了所有Druid的配置属性。
    - [API Reference](https://druid.apache.org/docs/latest/operations/api-reference.html)描述了每个Druid进程可用的API。
    - [Basic Cluster Tuning Guide](https://druid.apache.org/docs/latest/operations/basic-cluster-tuning.html)是调整Druid群集的入门指南。

* Need help with Druid? （需要Druid的帮助吗？）
如果您对使用Druid有疑问，请联系[Druid user mailing list or other community channels](https://druid.apache.org/community/)！

## 2.1 [Single-server Quickstart](https://druid.apache.org/docs/latest/tutorials/index.html) （单服务快速入门）
在本快速入门中，我们将下载Druid并在一台机器上进行设置。 完成此初始设置后，群集将准备好加载数据。

在开始快速入门之前，阅读[Druid概述](https://github.com/yoreyuan/My_hadoop/blob/master/doc/Apache-Druid/Getting-Started.md#getting-started-%E5%85%A5%E9%97%A8)和
[摄取概述](https://github.com/yoreyuan/My_hadoop/blob/master/doc/Apache-Druid/Data-Ingestion.md#1-ingestion-overview-%E6%91%84%E5%8F%96%E6%A6%82%E8%BF%B0)是有帮助的，
因为教程将参考这些页面上讨论的概念。

### 0 安装
#### 0.1 Prerequisites （先决条件）
##### 0.1.1 Software （软件）
你将需要：
* Java 8 (8u92+)
* Linux, Mac OS X, 或者其他的类Unix OS (Windows是不支持的)

##### 0.1.2 Hardware（硬件）
Druid包括几个[单服务配置](https://github.com/yoreyuan/My_hadoop/blob/master/doc/Apache-Druid/Single-Server-Deployments.md)示例，以及使用这些配置启动Druid进程的脚本。

如果您在笔记本电脑等小型机器上运行以进行快速评估，那么`micro-quickstart`配置是一个不错的选择，适用于 4CPU/16GB RAM环境。

如果您计划在教程之外使用单机部署进行进一步评估，我们建议使用比`micro-quickstart`更大的配置。

#### 0.2 Getting started （入门）
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

#### 0.3 Download Zookeeper （下载Zookeeper）
Druid依赖[Apache ZooKeeper](http://zookeeper.apache.org/)进行分布式协调。您需要下载并运行Zookeeper。

在程序包根目录中，运行以下命令：
```bash
curl https://archive.apache.org/dist/zookeeper/zookeeper-3.4.11/zookeeper-3.4.11.tar.gz -o zookeeper-3.4.11.tar.gz
tar -xzf zookeeper-3.4.11.tar.gz
mv zookeeper-3.4.11 zk
```

指南的启动脚本(bin/run-zk)将期望Zookeeper tarball位于apache-druid-0.15.0-incubating包根目录下。

#### 0.4 Start up Druid services （启动Druid服务）
以下命令将假定您使用的是`micro-quickstart`单机配置。 如果使用其他配置，则bin目录具有每个配置的等效脚本，例如`bin/start-single-server-small`。

从apache-druid-0.15.0-incubating包根目录，运行以下命令：
```bash
./bin/start-micro-quickstart
```

这将启动Zookeeper和Druid服务的实例，所有这些都在本地机器上运行，例如：
```
$ ./bin/start-micro-quickstart 
[Fri May  3 11:40:50 2019] Running command[zk], logging to[/apache-druid-0.15.0-incubating/var/sv/zk.log]: bin/run-zk conf
[Fri May  3 11:40:50 2019] Running command[coordinator-overlord], logging to[/apache-druid-0.15.0-incubating/var/sv/coordinator-overlord.log]: bin/run-druid coordinator-overlord conf/druid/single-server/micro-quickstart
[Fri May  3 11:40:50 2019] Running command[broker], logging to[/apache-druid-0.15.0-incubating/var/sv/broker.log]: bin/run-druid broker conf/druid/single-server/micro-quickstart
[Fri May  3 11:40:50 2019] Running command[router], logging to[/apache-druid-0.15.0-incubating/var/sv/router.log]: bin/run-druid router conf/druid/single-server/micro-quickstart
[Fri May  3 11:40:50 2019] Running command[historical], logging to[/apache-druid-0.15.0-incubating/var/sv/historical.log]: bin/run-druid historical conf/druid/single-server/micro-quickstart
[Fri May  3 11:40:50 2019] Running command[middleManager], logging to[/apache-druid-0.15.0-incubating/var/sv/middleManager.log]: bin/run-druid middleManager conf/druid/single-server/micro-quickstart
```

所有持久状态（如集群元数据存储和segment服务）都将保存在apache-druid-0.15.0-incubating软件包根目录下的`var`目录中。 服务的日志位于`var/sv`。

稍后，如果您想停止服务，请按CTRL-C退出`bin/start-micro-quickstart`脚本，这将终止Druid进程。

群集启动后，您可以导航到[http//localhost:8888](http//localhost:8888)。 服务于Druid控制台的[Druid路由进程](https://druid.apache.org/docs/latest/development/router.html)驻留在此地址。

![tutorial-quickstart-01.png](https://druid.apache.org/docs/latest/tutorials/img/tutorial-quickstart-01.png)

所有Druid进程都需要几秒钟才能完全启动。如果在启动服务后立即打开控制台，您可能会看到一些可以安全忽略的错误。

#### 0.5 Loading Data （加载数据）
##### 0.5.1 Tutorial Dataset （教程数据集）
对于以下数据加载教程，我们已经包含了一个示例数据文件，其中包含2015-09-12发生的Wikipedia页面的编辑事件。

此示例数据位于Druid软件包根目录的`quickstart/tutorial/wikiticker-2015-09-12-sampled.json.gz`中。 页面编辑事件作为JSON对象存储在文本文件中。

示例数据具有以下列，示例事件如下所示：
* added
* channel
* cityName
* comment
* countryIsoCode
* countryName
* deleted
* delta
* isAnonymous
* isMinor
* isNew
* isRobot
* isUnpatrolled
* metroCode
* namespace
* page
* regionIsoCode
* regionName
* user

```json
{
  "timestamp":"2015-09-12T20:03:45.018Z",
  "channel":"#en.wikipedia",
  "namespace":"Main",
  "page":"Spider-Man's powers and equipment",
  "user":"foobar",
  "comment":"/* Artificial web-shooters */",
  "cityName":"New York",
  "regionName":"New York",
  "regionIsoCode":"NY",
  "countryName":"United States",
  "countryIsoCode":"US",
  "isAnonymous":false,
  "isNew":false,
  "isMinor":false,
  "isRobot":false,
  "isUnpatrolled":false,
  "added":99,
  "delta":99,
  "deleted":0
}

```

##### 0.5.2 Data loading tutorials （数据加载教程）
以下教程演示了将数据加载到Druid中的各种方法，包括批处理和流式用例。 所有教程都假设您使用上面提到的`micro-quickstart`单机配置。

* [Loading a file](https://druid.apache.org/docs/latest/tutorials/tutorial-batch.html) - 本教程演示了如何使用Druid的本机批量提取执行批处理文件加载。
* [从Apache Kafka加载流数据](https://druid.apache.org/docs/latest/tutorials/tutorial-kafka.html) - 本教程演示了如何从Kafka topic加载流数据。
* [使用Apache Hadoop加载文件](https://druid.apache.org/docs/latest/tutorials/tutorial-batch-hadoop.html) - 本教程演示了如何使用远程Hadoop集群执行批处理文件加载。
* [使用Tranquility加载数据](https://druid.apache.org/docs/latest/tutorials/tutorial-tranquility.html) - 本教程演示了如何使用Tranquility服务将事件推送到Druid来加载流数据。
* [编写自己的提取规范](https://druid.apache.org/docs/latest/tutorials/tutorial-ingestion-spec.html) - 本教程演示了如何编写新的提取规范并使用它来加载数据。

##### 0.5.3 Resetting cluster state （重置集群状态）
如果要在停止服务后进行干净启动，请删除`var`目录并再次运行`bin/start-micro-quickstart`脚本。

一旦每个服务启动后，您就可以加载数据了。

###### Resetting Kafka （重置Kafka） 
如果您完成了[教程：从Kafka加载流数据](https://druid.apache.org/docs/latest/tutorials/tutorial-kafka.html)并希望重置群集状态，你需要另外清除任何Kafka状态。

在停止Zookeeper和Druid服务之前，使用CTRL-C关闭Kafka代理，然后删除 /tmp/kafka-logs中的Kafka日志目录：

```bash
rm -rf /tmp/kafka-logs
```

<br/>

**********

### 1 [Tutorial: Loading a file](https://druid.apache.org/docs/latest/tutorials/tutorial-batch.html) （教程：加载文件）
本教程演示了如何使用Apache Druid（孵化中）的本机批处理提取来执行批处理文件加载。

对于本教程，我们假设您已经使用`micro-quickstart`单机配置[快速入门](https://github.com/yoreyuan/My_hadoop/blob/master/doc/Apache-Druid/Getting-Started.md#21-single-server-quickstart-%E5%8D%95%E6%9C%8D%E5%8A%A1%E5%BF%AB%E9%80%9F%E5%85%A5%E9%97%A8)
下载了Druid并让它在本地计算机上运行。您还不需要加载任何数据。

通过向Druid Overlord提交摄取任务规范来启动数据加载。在本教程中，我们将加载示例Wikipedia页面编辑数据。

摄取规范可以手写或使用内置于Druid控制台中的“数据加载器”编写。数据加载器可以通过对数据进行采样并迭代配置各种摄取参数来帮助您构建摄取规范。
数据加载器目前仅支持本机批量提取（支持流式传输，包括存储在Apache Kafka和AWS Kinesis中的数据，将在未来的版本中提供）。流式摄取只能通过今天的编写的提取规范获得。

我们从2015年9月12日开始包含维基百科编辑样本，以帮助您入门。

#### 1.1 Loading data with the data loader （使用数据加载器加载数据）
导航到 [localhost:8888](http://localhost:8888)，然后单击控制台标题中的加载数据。 选择本地磁盘。

![tutorial-batch-data-loader-01.png](https://druid.apache.org/docs/latest/tutorials/img/tutorial-batch-data-loader-01.png)

输入`quickstart/tutorial/`的值作为基本目录，输入`wikiticker-2015-09-12-sampled.json.gz`作为过滤器。如果需要从多个文件中提取数据，则会分离基目录和[通配符文件过滤器](https://commons.apache.org/proper/commons-io/apidocs/org/apache/commons/io/filefilter/WildcardFileFilter.html)。

单击“预览”并确保您看到的数据正确无误。

![tutorial-batch-data-loader-02.png](https://druid.apache.org/docs/latest/tutorials/img/tutorial-batch-data-loader-02.png)
找到数据后，可以单击“Nextt: Parse data”转到下一步。 数据加载器将尝试自动确定数据的正确解析器。 在这种情况下，它将成功确定`json`。您可以随意使用不同的解析器选项来预览Druid将如何解析您的数据。

![tutorial-batch-data-loader-03.png](https://druid.apache.org/docs/latest/tutorials/img/tutorial-batch-data-loader-03.png)
选中`json`解析器后，单击`Next: Parse time`以确定主时间戳列为中心的步骤。 Druid的架构需要一个主时间戳列（内部存储在名为`__time`的列中）。 
如果数据中没有时间戳请选择`Constant value`。 在我们的示例中，数据加载器将确定原始数据中的时间列是唯一可用作主时间列的候选列。

![tutorial-batch-data-loader-04.png](https://druid.apache.org/docs/latest/tutorials/img/tutorial-batch-data-loader-04.png)
单击`Next: ...`两次以跳过`Transform`和`Filter`步骤。 您无需在这些步骤中输入任何内容，因为应用摄取时间转换和过滤器超出了本教程的范围。

在`Configure schema`步骤中，您可以配置将哪些维度（和指标）提取到Druid中。 这正是Druid一旦被摄取就会出现的数据。 由于我们的数据集非常小，请通过单击开关并确认更改来关闭`Rollup`。

![tutorial-batch-data-loader-05.png](https://druid.apache.org/docs/latest/tutorials/img/tutorial-batch-data-loader-05.png)
对架构满意后，单击`Next`转到`Partition`步骤，您可以在其中微调将数据划分为segment的方式。 在这里，您可以调整数据在Druid中分割成segment的方式。 由于这是一个小数据集，因此在此步骤中无需进行任何调整。

![tutorial-batch-data-loader-06.png](https://druid.apache.org/docs/latest/tutorials/img/tutorial-batch-data-loader-06.png)
点击`Tune`步骤，我们进入发布步骤，在这里我们可以指定Druid中的数据源名称。 我们将这个数据源命名为`wikipedia`。

![tutorial-batch-data-loader-07.png](https://druid.apache.org/docs/latest/tutorials/img/tutorial-batch-data-loader-07.png)

最后，单击`Next`以查看您的规范。这是您构建的规范。 您可以返回并在之前的步骤中进行更改，以查看更改将如何更新规范。 同样，您也可以直接编辑规范，并在前面的步骤中看到它。

![tutorial-batch-data-loader-08.png](https://druid.apache.org/docs/latest/tutorials/img/tutorial-batch-data-loader-08.png)

对规范满意后，单击`Submit`，将创建一个摄取任务。

您将进入任务视图，重点是新创建的任务。

![tutorial-batch-data-loader-09.png](https://druid.apache.org/docs/latest/tutorials/img/tutorial-batch-data-loader-09.png)
在任务视图中，您可以单击`Refresh`几次，直到您的提取任务（希望）成功。

当任务成功时，意味着它构建了一个或多个现在由数据服务拾取的segment。

导航到`Datasources`视图，然后单击`refresh`，直到出现数据源（`wikipedia`）。 加载segment时可能需要几秒钟。

![tutorial-batch-data-loader-10.png](https://druid.apache.org/docs/latest/tutorials/img/tutorial-batch-data-loader-10.png)

一旦看到绿色（完全可用）圆圈，就可以查询数据源。 此时，您可以转到`Query`视图以对数据源运行SQL查询。

由于这是一个小数据集，您只需运行`SELECT * FROM wikipedia`查询即可查看结果。
![tutorial-batch-data-loader-11.png](https://druid.apache.org/docs/latest/tutorials/img/tutorial-batch-data-loader-11.png)

点击[query tutorial]()，对新加载的数据运行一些示例查询。

#### 1.2 Loading data with a spec (via console) （使用规范加载数据（通过控制台））
Druid包在`quickstart/tutorial/wikipedia-index.json`中包含以下示例本机批量摄取任务规范，为方便起见，此处显示已配置为读取`quickstart/tutorial/wikiticker-2015-09-12-sampled.json.gz`输入文件：
```json
{
  "type" : "index",
  "spec" : {
    "dataSchema" : {
      "dataSource" : "wikipedia",
      "parser" : {
        "type" : "string",
        "parseSpec" : {
          "format" : "json",
          "dimensionsSpec" : {
            "dimensions" : [
              "channel",
              "cityName",
              "comment",
              "countryIsoCode",
              "countryName",
              "isAnonymous",
              "isMinor",
              "isNew",
              "isRobot",
              "isUnpatrolled",
              "metroCode",
              "namespace",
              "page",
              "regionIsoCode",
              "regionName",
              "user",
              { "name": "added", "type": "long" },
              { "name": "deleted", "type": "long" },
              { "name": "delta", "type": "long" }
            ]
          },
          "timestampSpec": {
            "column": "time",
            "format": "iso"
          }
        }
      },
      "metricsSpec" : [],
      "granularitySpec" : {
        "type" : "uniform",
        "segmentGranularity" : "day",
        "queryGranularity" : "none",
        "intervals" : ["2015-09-12/2015-09-13"],
        "rollup" : false
      }
    },
    "ioConfig" : {
      "type" : "index",
      "firehose" : {
        "type" : "local",
        "baseDir" : "quickstart/tutorial/",
        "filter" : "wikiticker-2015-09-12-sampled.json.gz"
      },
      "appendToExisting" : false
    },
    "tuningConfig" : {
      "type" : "index",
      "maxRowsPerSegment" : 5000000,
      "maxRowsInMemory" : 25000
    }
  }
}
```

此规范将创建名为“wikipedia”的数据源。

在任务视图中，单击`Submit task`并选择`Raw JSON task`。
![tutorial-batch-submit-task-01.png](https://druid.apache.org/docs/latest/tutorials/img/tutorial-batch-submit-task-01.png)

这将显示规范提交对话框，您可以在其中粘贴上面的规范。
![tutorial-batch-submit-task-02.png](https://druid.apache.org/docs/latest/tutorials/img/tutorial-batch-submit-task-02.png)

提交规范后，您可以按照上述相同的说明等待数据加载然后进行查询。

#### 1.3 Loading data with a spec (via command line) （使用规范加载数据（通过命令行））
为方便起见，Druid包在`bin/post-index-task`中包含批量摄取助手脚本。

此脚本会POST一个摄取任务给Druid Overlord并轮询Druid，直到数据可用于查询。

从Druid包跟目录运行以下命令：
```bash
bin/post-index-task --file quickstart/tutorial/wikipedia-index.json --url http://localhost:8081
```

您应该看到如下输出：
```
Beginning indexing data for wikipedia
Task started: index_wikipedia_2018-07-27T06:37:44.323Z
Task log:     http://localhost:8081/druid/indexer/v1/task/index_wikipedia_2018-07-27T06:37:44.323Z/log
Task status:  http://localhost:8081/druid/indexer/v1/task/index_wikipedia_2018-07-27T06:37:44.323Z/status
Task index_wikipedia_2018-07-27T06:37:44.323Z still running...
Task index_wikipedia_2018-07-27T06:37:44.323Z still running...
Task finished with status: SUCCESS
Completed indexing data for wikipedia. Now loading indexed data onto the cluster...
wikipedia loading complete! You may now query your data
```

提交规范后，您可以按照上述相同的说明等待数据加载然后进行查询。

#### 1.4 Loading data without the script （不使用脚本加载数据）
让我们简要讨论一下如何在不使用脚本的情况下提交摄取任务。 您不需要运行这些命令。

要提交任务，请在apache-druid-0.15.0-incubating目录的新终端窗口中将其发布到Druid：
```bash
curl -X 'POST' -H 'Content-Type:application/json' -d @quickstart/tutorial/wikipedia-index.json http://localhost:8081/druid/indexer/v1/task
```

如果提交成功，将打印任务的ID：
```json
{"task":"index_wikipedia_2018-06-09T21:30:32.802Z"}
```

您可以如上所述从控制台监视此任务的状态。

#### 1.5 Querying your data （查询你的数据）
当加载数据后，请按照[查询教程](https://druid.apache.org/docs/latest/tutorials/tutorial-query.html)对新加载的数据运行一些示例查询。

#### 1.6 Cleanup （清理）
如果你希望通过任何其他的摄取教程，你需要关闭集群并通过删除druid包下的`var`目录的内容来重置集群状态，因为其他教程将写入相同的`wikipedia`数据源。

#### 1.7 Further reading （进一步阅读）
有关加载批处理数据的更多信息，请参阅[批处理提取文档](https://druid.apache.org/docs/latest/ingestion/batch-ingestion.html)。

<br/>

**********

### 2 [Tutorial: Loading stream data from Apache Kafka]() （教程：从Apache Kafka加载流数据）

<br/>

**********

### 3 [Tutorial: Loading a file using Apache Hadoop]() （教程：使用Apache Hadoop加载文件）

<br/>

**********

### 4 [Tutorial: Loading stream data using HTTP push]() （教程：使用HTTP推送加载流数据）

<br/>

**********

### 5 [Tutorial: Querying data]() （教程：查询数据）

<br/>

**********

### Further tutorials

### 6 [Tutorial: Rollup]() （教程：汇总）


<br/>

**********

### 7 [Tutorial: Configuring retention]() （教程：保留配置）


<br/>

**********

### 8 [Tutorial: Updating existing data]() （教程：更新现有数据）


<br/>

**********

### 9 [Tutorial: Compacting segments]() （教程：segment压缩）


<br/>

**********

### 10 [Tutorial: Deleting data]() （教程：删除数据）

<br/>

**********

### 11 [Tutorial: Writing your own ingestion specs]() （教程：编写你自己的摄取规范）

<br/>

**********

### 12 [Tutorial: Transforming input data]() （教程：转换输入数据）

<br/>

**********

## 2.2 [Clustering](https://druid.apache.org/docs/latest/tutorials/cluster.html) （集群）
## Setting up a Clustered Deployment （集群设置部署）
Apache Druid（孵化中）旨在部署为可扩展的容错集群。

在本文档中，我们将设置一个简单的集群，并讨论如何进一步配置以满足您的需求。

这个简单的集群将具有以下特点： 
- 用于承载Coordinator和Overlord进程的Master服务
- 两个可伸缩的容错数据服务器，Historical和MiddleManager进程 
- 一个查询服务器，托管Druid Broker和Router进程

在生产中，我们建议根据您的特定容错需求在容错配置中部署多个主服务器和多个查询服务器，但您可以使用一个主服务器和一个查询服务器快速入门，并在以后添加更多服务器。

### 1 Select hardware （硬件选择）
#### 1.1 Fresh Deployment （新部署）
如果您没有现有的Druid集群，并希望在集群部署中开始运行Druid，本指南提供了一个带有预制配置的示例集群部署。

#### 1.2 Master Server （Master服务）
Coordinator和Overlord进程负责处理集群的元数据和协调需求。 它们可以在同一服务器上共同放置。

在此示例中，我们将部署相当于一个AWS [m5.2xlarge](https://aws.amazon.com/ec2/instance-types/m5/)实例。

该硬件提供：- 8 vCPUs - 31 GB RAM

示例已为此硬件调整大小的Master服务配置可在`conf/druid/cluster/master`下找到。

#### 1.3 Data Server （数据服务）
可以在同一服务器上并置Historicals和MiddleManagers来处理群集中的实际数据。 这些服务器从CPU，RAM和SSD中受益匪浅。

在此示例中，我们将部署两个AWS [i3.4xlarge](https://aws.amazon.com/ec2/instance-types/i3/)实例的等效项。

这个硬件提供：
* 16 vCPUs
* 122 GB RAM
* 2 * 1.9TB SSD 存储

示例已针对此硬件调整大小的数据服务器配置可在`conf/druid/cluster/data`下找到。

#### 1.4 Query Server （查询服务）
Druid Broker接受查询并将其分配给群集的其余部分。 他们还可以选择维护内存中的查询缓存。 这些服务器受益于CPU和RAM。

在此示例中，我们将部署相当于一个AWS [m5.2xlarge](https://aws.amazon.com/ec2/instance-types/m5/)实例。

该硬件提供：- 8 vCPUs - 31 GB RAM

您可以考虑在运行Broker的同一服务上运行同一位置任何打开source UI或查询库。

示例可以在`conf/druid/cluster/query`下找到已针对此硬件调整大小的查询服务器配置。


#### 1.5 Other Hardware Sizes （其他硬件大小）
上面的示例被选为一个电节点例子的集群，可用于确定Druid集群的大小。

您可以根据具体需求和约束选择较小/较大的硬件或更少/更多的服务器。

如果您的用例具有复杂的扩展要求，您还可以选择不再一起的Druid进程（例如，独立的Historical服务器）。

[basic cluster tuning guide](https://druid.apache.org/docs/latest/operations/basic-cluster-tuning.html)中的信息可以帮助您制定决策流程并调整配置大小。

### 2 Migrating from a Single-Server Deployment （从单服务部署迁移）
如果您具有现有的单服务部署（例如来自[single-server deployment examples](https://github.com/yoreyuan/My_hadoop/blob/master/doc/Apache-Druid/Getting-Started.md#21-single-server-quickstart-%E5%8D%95%E6%9C%8D%E5%8A%A1%E5%BF%AB%E9%80%9F%E5%85%A5%E9%97%A8)），
并且您希望迁移到类似规模的集群部署，则以下部分包含使用 Master/Data/Query 服务组织结构。

#### 2.1  Master Server
Master Server的主要考虑因素是Coordinator和Overlord的可用CPU和RAM。

从单服务部署中总结协调器和宿主的分配堆大小，并选择具有足够RAM的主服务器硬件用于组合堆，并为机器上的其他进程选择一些额外的RAM。

对于CPU内核，您可以选择具有单服务器部署核心大约1/4的硬件。

#### 2.2 Data Server（数据服务）
为群集选择数据服务器硬件时，主要考虑因素是CPU和RAM，如果可行，则使用SSD存储。

在集群部署中，拥有多个数据服务器是出于容错目的的好想法。

选择数据服务器硬件时，可以选择拆分因子N，将单服务器部署的原始 CPU/RAM 除以N，并在新集群中部署N个缩小的数据服务器。

有关调整分割的Historical/MiddleManager 配置的说明，请参阅本指南的后续部分。

#### 2.3 Query Server （查询服务）
查询服务器的主要注意事项是 Broker堆内存 + 直接内存 和 Router堆内存的可用CPU和RAM。

从单服务器部署中总结为Broker和Router分配的内存大小，并选择具有足够RAM的查询服务器硬件来覆盖 Broker/Router，并为机器上的其他进程添加一些额外的RAM。

对于CPU内核，您可以选择具有单服务器部署核心大约1/4的硬件。

[basic cluster tuning guide](https://druid.apache.org/docs/latest/operations/basic-cluster-tuning.html)包含有关如何计算 Broker/Router 内存使用情况的信息。

### 3 Select OS （选择操作系统）
我们建议运行您喜欢的Linux发行版。您还需要：
* Java 8

您的OS包管理器应该能够为两个Java提供帮助。 如果您的基于Ubuntu的操作系统没有最新版本的Java，则WebUpd8会为[这些操作系统提供软件包](http://www.webupd8.org/2012/09/install-oracle-java-8-in-ubuntu-via-ppa.html)。

### 4 Download the distribution （下载发行版）
首先，下载并解压缩发布版存档包。 最好先在一台机器上执行此操作，因为您将编辑配置，然后将修改后的分发包复制到所有服务器。

[下载](https://www.apache.org/dyn/closer.cgi?path=/incubator/druid/0.15.0-incubating/apache-druid-0.15.0-incubating-bin.tar.gz) 0.15.0-incubating  版本。

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

#### 4.1 Migrating from Single-Server Deployments （从单服务部署迁移）
在以下部分中，我们将编辑`conf/druid/cluster`下的配置。

如果您有现有的单服务器部署，请将您现有的配置复制到`conf/druid/cluster`以保留您所做的任何配置更改。

### 5 Configure metadata storage and deep storage （配置元数据存储和深存储）
#### 5.1 Migrating from Single-Server Deployments（从电服务部署迁移）
如果您有现有的单服务器部署，并且希望在迁移过程中保留数据，请在更新 元数据/深存储 配置之前按照[元数据迁移](https://druid.apache.org/docs/latest/operations/metadata-migration.html)和[深存储迁移](https://druid.apache.org/docs/latest/operations/deep-storage-migration.html)的说明进行操作。

这些指南针对使用Derby元数据存储和本地深存储的单服务器部署。 如果您已在单服务器群集中使用非Derby元数据存储，则可以为新群集重用现有元数据存储。

这些指南还提供有关从本地深存储迁移segment的信息。 集群部署需要分布式深存储，如S3或HDFS。 如果您的单服务器部署已使用分布式深存储，则可以将现有深存储重用于新群集。

#### 5.2 Metadata Storage （元数据存储）
在`conf/druid/cluster/_common/common.runtime.properties`中，将"metadata.storage.*"替换为您将用作元数据存储的计算机的地址：
* druid.metadata.storage.connector.connectURI
* druid.metadata.storage.connector.host

在生产部署中，我们建议运行专用的元数据存储，例如带有复制的MySQL或PostgreSQL，与Druid服务器分开部署。

[MySQL extension](https://druid.apache.org/docs/latest/development/extensions-core/mysql.html)和[PostgreSQL extension](https://druid.apache.org/docs/latest/development/extensions-core/postgresql.html)文档具有扩展配置和初始数据库设置的说明。

#### 5.3 Deep Storage （深存储）
Druid依赖于分布式文件系统或大型对象（blob）存储来进行数据存储。 最常用的深存储实现是S3（适用于AWS上的那些）和HDFS（如果您已经有Hadoop部署，那很流行）。

#### 5.4 S3
在`conf/druid/cluster/_common/common.runtime.properties`
* 添加 "druid-s3-extensions" 到 `druid.extensions.loadList`。
* 在"Deep Storage" 和 "Indexing service logs"下注释掉本地存储的配置。
* 取消注释并在 "Deep Storage" 和 "Indexing service logs" 的 "For S3" 部分中配置适当的值。

在此之后，您应该进行以下更改：
```yaml
druid.extensions.loadList=["druid-s3-extensions"]

#druid.storage.type=local
#druid.storage.storageDirectory=var/druid/segments

druid.storage.type=s3
druid.storage.bucket=your-bucket
druid.storage.baseKey=druid/segments
druid.s3.accessKey=...
druid.s3.secretKey=...

#druid.indexer.logs.type=file
#druid.indexer.logs.directory=var/druid/indexing-logs

druid.indexer.logs.type=s3
druid.indexer.logs.s3Bucket=your-bucket
druid.indexer.logs.s3Prefix=druid/indexing-logs
```

更多信息请查看[S3 extension](https://druid.apache.org/docs/latest/development/extensions-core/s3.html)文档。

#### 5.5 HDFS
在`conf/druid/cluster/_common/common.runtime.properties`
* 添加 "druid-hdfs-storage" 到 `druid.extensions.loadList`。
* 在"Deep Storage" 和 "Indexing service logs"下注释掉本地存储配置。
* 取消注释并在"Deep Storage" 和 "Indexing service logs"的"For HDFS"部分中配置适当的值。

在此之后，您应该进行以下更改：
```yaml
druid.extensions.loadList=["druid-hdfs-storage"]

#druid.storage.type=local
#druid.storage.storageDirectory=var/druid/segments

druid.storage.type=hdfs
druid.storage.storageDirectory=/druid/segments

#druid.indexer.logs.type=file
#druid.indexer.logs.directory=var/druid/indexing-logs

druid.indexer.logs.type=hdfs
druid.indexer.logs.directory=/druid/indexing-logs
```

另外也要，
* 将Hadoop配置的XML（core-site.xml, hdfs-site.xml, yarn-site.xml, mapred-site.xml）放在Druid进程的类路径中。 您可以通过将它们复制到`conf/druid/cluster/_common/`来完成此操作。

有关详细信息，请参阅[HDFS扩展文档](https://druid.apache.org/docs/latest/development/extensions-core/hdfs.html)。

### 6 Configure Tranquility Server (optional) （配置Tranquility Server(可选) ）
数据流可以通过由Tranquility Server简单的提供支持HTTP API发送给Druid。如果您将使用此功能，那么此时您应该[配置Tranquility Server](https://druid.apache.org/docs/latest/ingestion/stream-ingestion.html#server)。

### 7 Configure for connecting to Hadoop (optional) （连接Hadoop配置(可选)）
如果您要从Hadoop集群加载数据，那么此时您应该配置Druid以了解您的集群：

* 将`conf/druid/cluster/middleManager/runtime.properties`中的`druid.indexer.task.hadoopWorkingPath`更新为HDFS上的路径，
您希望将其用于索引进程所需的临时文件。 `druid.indexer.task.hadoopWorkingPath=/tmp/druid-indexing`是一种常见的选择。

**注意**： 这里应该是文档未来得及更新， `conf/druid/cluster/middleManager/runtime.properties`应该为`conf/druid/cluster/data/middleManager/runtime.properties`

将Hadoop配置的XML(core-site.xml, hdfs-site.xml, yarn-site.xml, mapred-site.xml) 放在Druid进程的类路径中。 您可以通过将它们复制到`conf/druid/cluster/_common/core-site.xml`，`conf/druid/cluster/_common/hdfs-site.xml`等来完成此操作。

请注意，您不需要使用HDFS深存储来从Hadoop加载数据。 例如，如果您的群集在Amazon Web Services上运行，我们建议您使用S3进行深存储，即使您使用Hadoop或Elastic MapReduce加载数据也是如此。

有关详细信息，请参阅[batch ingestion](https://druid.apache.org/docs/latest/ingestion/batch-ingestion.html)。

### 8 Configure Zookeeper connection （配置Zookeeper连接）
在生产群集中，我们建议在法定人数(quorum)中使用专用ZK群集，与Druid服务器分开部署。

在`conf/druid/cluster/_common/common.runtime.properties`中，将`druid.zk.service.host`设置为包含逗号分隔的列表 host:port对的连接字符串，
每个对应于ZK quorum的ZooKeeper服务。 (流入 "127.0.0.1:4545" 或 "127.0.0.1:3000,127.0.0.1:3001,127.0.0.1:3002")

您还可以选择在主服务器上运行ZK，而不是使用专用的ZK群集。 如果这样做，我们建议部署3个主服务器，以便您具有ZK quorum。

### 9 Configuration Tuning （配置调整）
#### 9.1 Migrating from a Single-Server Deployment （从单服务部署迁移）
##### 9.1.1 Master
如果您使用的是单服务器部署示例中的示例配置，则这些示例将Coordinator和Overlord进程组合到一个组合进程中。

`conf/druid/cluster/master/coordinator-overlord`下的示例配置也结合了Coordinator和Overlord进程。

您可以将现有的`coordinator-overlord`配置从单服务器部署复制到`conf/druid/cluster/master/coordinator-overlord`。

##### 9.1.2 Data
假设我们从具有32个CPU和256GB RAM的单服务器部署进行迁移。 在旧部署中，应用了Historicals和MiddleManagers的以下配置：

Historical (Single-server) 
```yaml
druid.processing.buffer.sizeBytes=500000000 
druid.processing.numMergeBuffers=8 
druid.processing.numThreads=31
``` 

MiddleManager (Single-server) 
```yaml
druid.worker.capacity=8 
druid.indexer.fork.property.druid.processing.numMergeBuffers=2 
druid.indexer.fork.property.druid.processing.buffer.sizeBytes=100000000 
druid.indexer.fork.property.druid.processing.numThreads=1
```

在集群部署中，我们可以选择拆分因子（本例中为2），并部署2个数据服务器，每个服务器具有16CPU和128GB RAM。 规模范围如下：

Historical 
-  `druid.processing.numThreads`：基于新硬件设置为 (num_cores - 1) 
-  `druid.processing.numMergeBuffers`：用分割因子除去单服务器部署中的旧值 
-  `druid.processing.buffer.sizeBytes`： 保持不变

MiddleManager： 
-  `druid.worker.capacity`：用分割因子除以单服务器部署的旧值 
-  `druid.indexer.fork.property.druid.processing.numMergeBuffers`：保持不变 
-  `druid.indexer.fork.property。 druid.processing.buffer.sizeBytes`：保持不变 
-  `druid.indexer.fork.property.druid.processing.numThreads`：保持不变

切分后产生的配置:

新的 Historical (on 2 Data servers) 
```yaml
druid.processing.buffer.sizeBytes=500000000 
druid.processing.numMergeBuffers=8 
druid.processing.numThreads=31
```

新的 MiddleManager (on 2 Data servers) 
```yaml
druid.worker.capacity=4 
druid.indexer.fork.property.druid.processing.numMergeBuffers=2 
druid.indexer.fork.property.druid.processing.buffer.sizeBytes=100000000 
druid.indexer.fork.property.druid.processing.numThreads=1
```

##### 9.1.3 Query
您可以将现有的Broker和Router配置复制到`conf/druid/cluster/query`下的目录，只要新硬件的大小相应其他的不需要修改。

#### 9.2 Fresh deployment （更新部署）
如果您使用上述示例群集： 
-  1个 Master server （m5.2xlarge） 
-  2个Data servers （i3.4xlarge） 
-  1个Query server（m5.2xlarge）

`conf/druid/cluster`下的配置已针对此硬件进行了调整，您无需对一般用例进行进一步修改。

如果您选择了不同的硬件，则[basic cluster tuning guide](https://druid.apache.org/docs/latest/operations/basic-cluster-tuning.html)可帮助您调整配置大小。

### 10 Open ports (if using a firewall) （开放端口(如果使用了防火墙)）
如果您使用的是防火墙或其他系统仅允许其特定端口通过，请允许以下内容的入站：

#### 10.1 Master Server （Master服务）
* 1527 （Derby元数据存储;如果您使用单独的元数据存储，如MySQL或PostgreSQL，则不需要）
* 2181 (ZooKeeper; 如果您使用单独的ZooKeeper集群则不需要)
* 8081 (Coordinator)
* 8090 (Overlord)

#### 10.2 Data Server （数据服务）
* 8083 (Historical)
* 8091, 8100–8199 （Druid Middle Manager;如果你有一个非常高级的`druid.worker.capacity`，你可能需要高于8199的端口号）

#### 10.3 Query Server （查询服务）
* 8082 (Broker)
* 8088 (Router, 如果使用了)

#### 10.4 Other （其他）
* 8200 (Tranquility Server, 如果使用了)

**注意** 在生产中，我们建议在自己的专用硬件上而不是在主服务器上部署ZooKeeper和元数据存储。

### 11 Start Master Server （启动Master服务）
将Druid发行版和您编辑的配置复制到 Master server。

如果您一直在本地计算机上编辑配置，则可以使用rsync复制它们：
```bash
rsync -az apache-druid-0.15.0-incubating/ MASTER_SERVER:apache-druid-0.15.0-incubating/
```

#### 11.1 No Zookeper on Master （在Master上没有Zookeeper）
从发行版根目录，运行以下命令以启动 Master server：

```bash
bin/start-cluster-master-no-zk-server
```

#### 11.2 With Zookeper on Master （在Master上使用Zookeeper）
如果您计划在Master servers上运行ZK，请首先更新`conf/zoo.cfg`以反映您计划如何运行ZK。 然后登录到Master servers并安装Zookeeper：
```bash
curl http://www.gtlib.gatech.edu/pub/apache/zookeeper/zookeeper-3.4.11/zookeeper-3.4.11.tar.gz -o zookeeper-3.4.11.tar.gz
tar -xzf zookeeper-3.4.11.tar.gz
mv zookeeper-3.4.11 zk
```

如果在Master server上运行ZK，则可以使用以下命令与ZK一起启动Master server进程：
```bash
bin/start-cluster-master-with-zk-server
```

**注意** 在生产中，我们还建议在自己的专用硬件上运行ZooKeeper集群。

### 12 Start Data Server （启动数据服务）
将Druid发行版和您编辑的配置复制到 Data Server。

从发行版根目录运行以下命令以启动Data Server：
```bash
bin/start-cluster-data-server
```

您可以根据需要添加更多数据服务器。

**注意** 对于具有复杂资源分配需求的群集，您可以拆分Historicals和MiddleManagers并单独扩展组件。 这也允许您利用Druid内置的MiddleManager自动缩放

#### 12.1 Tranquility 
如果您使用Kafka或HTTP进行基于推送的流提取，您还可以在Data server上启动Tranquility Server。

对于大规模生产，Data server进程和Tranquility Server仍然可以共存。

如果您使用流处理运行Tranquility（而非服务），则可以将Tranquility与流处理共同协作，而不需要Tranquility Server。

首先安装Tranquility：
```bash
curl http://static.druid.io/tranquility/releases/tranquility-distribution-0.8.3.tgz -o tranquility-distribution-0.8.3.tgz
tar -xzf tranquility-distribution-0.8.3.tgz
mv tranquility-distribution-0.8.3 tranquility
```

然后，在`conf/supervise/cluster/data.conf`中，取消注释`tranquility-server`行，然后重新启动 Data server进程。

### 13 Start Query Server （启动查询服务）
将Druid发行版和您编辑的配置复制到Query Server。

从发行版根目录运行以下命令以启动Query Server：
```bash
bin/start-cluster-query-server
```

您可以根据查询负载的需要添加更多Query server。 如果增加Query server的数量，请确保按照[basic cluster tuning guide](https://druid.apache.org/docs/latest/operations/basic-cluster-tuning.html)中的说明调整Historicals和任务上的连接池。

### 14 Loading data （加载数据）
恭喜你，你现在拥有一个Druid集群！ 下一步是了解使用推荐的方式将数据加载到Druid中。 了解更多[loading data](https://druid.apache.org/docs/latest/ingestion/index.html)。


<br/>

**********

### 2 [Migrating from a Single-Server Deployment]()


## 2.3 [Further examples]() （更多案例）