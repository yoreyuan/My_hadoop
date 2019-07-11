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

**********

# 2 Getting Started （入门） 
Apache Druid入门（孵化中）

* Overview (概述)
如果您不熟悉Druid，我们建议您先阅读[Design Overview](https://druid.apache.org/docs/latest/design/index.html)
和[Ingestion Overview](https://druid.apache.org/docs/latest/ingestion/index.html)，以便对Druid有基本的了解。

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

## 2.1 Single-server Quickstart （单服务快速入门）
在本快速入门中，我们将下载Druid并在一台机器上进行设置。 完成此初始设置后，群集将准备好加载数据。

在开始快速入门之前，阅读[Druid概述]()和[摄取概述]()是有帮助的，因为教程将参考这些页面上讨论的概念。



## 2.2 Clustering （集群）

## 2.3 Further examples （更多案例）