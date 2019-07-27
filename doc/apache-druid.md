Apache Druid
======
[官网](https://druid.apache.org/) &nbsp; &nbsp; | &nbsp; &nbsp; [GitHub](https://github.com/apache/incubator-druid/) &nbsp; &nbsp; | &nbsp; &nbsp;  [Docs](https://druid.apache.org/docs/latest/design/)

<br/><br/>

# 目录
* 一、[Home](#1)
    - 1.1 Overview
    - 1.2 Learn more （更多了解）
* 二、[Techology](#2) (技术)
    - 2.1 Integration 
    - 2.2 Ingestion
    - 2.3 Storage
    - 2.4 Querying
    - 2.5 Architecture (架构)
    - 2.6 Operations
* 三、[Use Cases](#3)（用例）
    - 3.1 Streaming and operational data （流式和数据操作）
    - 3.2 User activity and behavior （用户活跃和行为）
    - 3.3 Network flows （网络流量）
    - 3.4 Digital marketing （数字营销）
    - 3.5 Application performance management （应用性能管理）
    - 3.6 IoT and device metrics （Iot和设备指标）
    - 3.7 OLAP and business intelligence （OLAP和商业智能）
* 四、[Docs](#4) （文档）

<br/>

*********

<br/>

# 一、Home
Apache Druid (孵化中) 是一个高性能的实时分析数据库。

## 1.1 Overview
* **分析事件流**： Druid以高并发性为事件驱动数据提供快速分析查询。Druid可以即时摄取流数据并提供亚秒级查询以支持交互式UI。
* **利用重新构想的架构**： Druid是一种新型数据库，它结合了[OLAP/分析数据库](https://en.wikipedia.org/wiki/Online_analytical_processing)，
[时间序列数据库](https://en.wikipedia.org/wiki/Time_series_database)和[搜索系统](https://en.wikipedia.org/wiki/Full-text_search)的思想，以支持流式体系结构中的新用例。
* **构建事件驱动的数据堆栈**： Druid本地集成了消息总线（Kafka，AWS Kinesis等）和数据湖（HDFS，AWS S3等）。 Druid尤其适用于流枢纽和流处理器的查询层。
* **解锁新的工作流程**： Druid旨在对实时数据和历史数据进行快速，即席分析。解释趋势，探索数据，并快速迭代查询以应答问题。
* **任何地方部署**： Druid可以部署在商用硬件上的任何*NIX环境中，无论是在云端还是内部部署。Druid是云原生的：扩缩就像添加和删除进程一样简单。

## 1.2 Learn more （更多了解）
* **Powered By**： Druid已在[世界领先的公司](https://druid.apache.org/druid-powered)大规模生产中得到验证。

* **FAQ**： 了解一些[关于Druid的最常见问题](https://druid.apache.org/faq)。

* **快速开始**： 几分钟即可[开始使用Druid](https://druid.apache.org/docs/latest/tutorials/quickstart)。 加载您自己的数据并进行查询。

* **得到帮助**： 从[广泛的社区成员网络](https://druid.apache.org/community)获取有关使用Druid的帮助。

**********

# 二、[Techology](https://druid.apache.org/technology)
Apache Druid（孵化中）是一个开源的分布式数据存储。Druid的核心设计思想结合了 [OLAP/分析型数据库](https://en.wikipedia.org/wiki/Online_analytical_processing)、
[时间序列数据库](https://en.wikipedia.org/wiki/Time_series_database)和[搜索系统](https://en.wikipedia.org/wiki/Full-text_search)，为广泛的[用例](https://druid.apache.org/use-cases)创建了统一的系统。
Druid将3个系统中每个系统的关键特性合并到其摄入层（ingestion layer）、存储格式（storage format）、查询层（querying layer）和核心架构中。

![diagram-2.png](https://druid.apache.org/img/diagram-2.png)

Druid主要特点包括：
* **面向列的存储**：Druid单独存储和压缩每个列，只需要读取特定查询所需的列，这些查询支持快速扫描（scan）、排序（ranking）和groupBy。
* **原生搜索索引**：Druid为字符串值创建反向索引（inverted indexes），以便快速搜索和过滤。
* **流批摄入**：   使用于Apache Kafka，HDFS，AWS S3，流处理器等的开箱即用连接器。
* **灵活的架构**： Druid优雅地处理不断发展的模式和[嵌套数据](https://druid.apache.org/docs/latest/ingestion/flatten-json)。
* **时间优化的区间**：Druid基于时间智能地分区数据，基于时间的查询比传统数据库快得多。
* **支持SQL**：   除了基于[JSON的原生语言](https://druid.apache.org/docs/latest/querying/querying)外，Druid还不言而喻通过HTTP或JDBC支持了[SQL](https://druid.apache.org/docs/latest/querying/sql)。
* **水平可扩展性**：Druid已经在[生产中用于](https://druid.apache.org/druid-powered)摄入数百万事件/秒，保留多年的数据，并提供亚秒级查询（sub-second querie）。
* **操作简单**：  只需添加或删除服务器即可向上或向下扩展，德鲁伊会自动重新平衡。 容错架构围绕服务器故障进行路由。

## 2.1 Integration 
Druid是[Apache Software Foundation](https://www.apache.org/)中许多开源数据技术的补充，包括[Apache Kafka](https://kafka.apache.org/)，[Apache Hadoop](https://hadoop.apache.org/)，[Apache Flink](https://flink.apache.org/)等。

Druid通常位于存储或处理层与最终用户之间，并充当查询层以服务分析工作负载。
![diagram-3.png](https://druid.apache.org/img/diagram-3.png)


## 2.2 Ingestion
Druid 支持流式和批量摄入。 Druid连接到原始数据源，通常是消息总线，如Apache Kafka（用于流数据加载），或分布式文件系统，如HDFS（用于批量数据加载）。

Druid在一个称为“索引”的过程中将存储在源中的原始数据转换为更加读取优化的格式（称为Druid“segment”）。

![diagram-4.png](https://druid.apache.org/img/diagram-4.png)

更多详细信息，请访问[our docs page](https://druid.apache.org/docs/latest/ingestion/index.html)。

## 2.3 Storage
与许多分析型数据存储一样，Druid将数据存储在列中。根据列的类型（string, number等），应用不同的压缩和编码方法。 Druid还根据列类型构建不同类型的索引。

与搜索系统类似，Druid为字符串列构建反向索引，以便快速搜索和过滤。 与时间序列数据库类似，Druid会按时间智能地对数据进行分区，以实现快速面向时间的查询。

与许多传统系统不同，Druid可以选择预先汇总数据。 此预聚合步骤称为[汇总](https://druid.apache.org/docs/latest/tutorials/tutorial-rollup.html)，
可以节省大量存储空间。

![diagram-5.png](https://druid.apache.org/img/diagram-5.png)

更多详细信息，请访问[our docs page](https://druid.apache.org/docs/latest/ingestion/index.html)。

## 2.4 Querying
Druid支持通过[JSON-over-HTTP](https://druid.apache.org/docs/latest/querying/querying)和[SQL](https://druid.apache.org/docs/latest/querying/sql)查询数据。 
除了标准的SQL运算符之外，Druid还支持使用其近似算法套件的独特运算符，以提供快速统计，排名和分位数。

![diagram-6.png](https://druid.apache.org/img/diagram-6.png)

更多详细信息，请访问[our docs page](https://druid.apache.org/docs/latest/ingestion/index.html)。

## 2.5 Architecture (架构)
Druid可以被认为是一个分散的数据库。 Druid的每个核心流程（摄取，查询和协调）可以单独或联合部署在商品硬件上。

Druid明确命名每个主要流程，以允许运营商根据用例和工作负载微调每个流程。 例如，如果工作负载需要，operator可以为Druid的摄取过程投入更多资源，
同时为Druid的查询过程提供更少的资源。

Druid过程可以独立地失败，而不会影响其他过程的操作。

![diagram-7.png](https://druid.apache.org/img/diagram-7.png)

更多详细信息，请访问[our docs page](https://druid.apache.org/docs/latest/ingestion/index.html)。

## 2.6 Operations
Druid旨在为需要每周7天，每天24小时运行的应用程序提供支持。 因此，Druid拥有多项功能，以确保正常运行时间和没有数据丢失。

* **数据复制**： Druid中的所有数据都被复制了可配置的次数，因此单个服务器故障对查询没有影响。

* **独立流程**： Druid明确指出其所有主要流程，并且每个流程都可以根据用例进行微调。 流程可以独立地失败而不会影响其他流程。 例如，如果摄取过程失败，则系统中不会加载新数据，但现有数据仍然可查询。

* **自动数据备份**： Druid自动将所有索引数据备份到文件系统，如HDFS。 您可能会丢失整个Druid群集，并从此备份数据中快速恢复。

* **滚动更新**： 您可以通过滚动更新方式更新Druid群集，无需停机，也不会对最终用户产生任何影响。 所有Druid版本都向后兼容以前的版本。

更多详细信息，请访问[our docs page](https://druid.apache.org/docs/latest/ingestion/index.html)。

**********

# 三、Use Cases （用例）
## 3.1 Streaming and operational data （流式和数据操作）
Apache Druid（孵化中）通常适用于任何面向事件、点击流、时间序列或遥测数据，尤其是Apache Kafka的流数据集。 Druid提供[Apache Kafka](https://kafka.apache.org/)的
[一次消费语义](https://druid.apache.org/docs/latest/development/extensions-core/kafka-ingestion)，通常用作面向事件的Kafka Topic 接收器。

Druid也适用于批量数据集。 组织机构已经部署了Druid来加速查询并为输入数据是一个或多个静态文件的应用程序提供动力。 如果您正在开发面向用户的应用程序，并希望您的用户能够自行处理他们自己的问题，那么Druid非常适合。

一些常见的德鲁伊高级用例包括：
* **性能分析**：创建具有完全向下钻取功能的交互式仪表板。 分析数字产品的性能，跟踪移动应用程序使用情况或监控站点可靠性。

* **诊断问题**：找出问题的根本原因。 解决netflow瓶颈问题，分析安全威胁或诊断软件崩溃。

* **寻找共性**：查找事件中的常见属性。 识别有缺陷的产品中的共享组件，或确定性能最佳的产品中的模式。

* **提高效率**：改善产品参与度。 优化数字营销广告系列中的广告支出或提高在线产品的用户参与度。

## 3.2 User activity and behavior （用户活跃和行为）
Druid经常用于点击流（clickstreams）、视图流（viewstreams）和活动流。 具体用例包括衡量用户参与度，跟踪产品发布的 A/B测试数据以及理解使用模式。

Druid可以准确计算用户指标，例如精确和近似的[去重计数](https://druid.apache.org/docs/latest/querying/aggregations)。 这意味着诸如每日活跃用户之类的措施
可以在大约一秒钟内（平均准确率为98％）计算以查看一般趋势，或者精确计算以呈现给关键利益相关者。 此外，Druid可以用于[漏斗分析](https://druid.apache.org/docs/latest/development/extensions-core/datasketches-aggregators)，
并测量有多少用户采取一个行动而没有采取其他行动。 这种分析非常有用，可以跟踪产品的用户注册情况。

Druid的搜索和过滤功能可以快速、轻松地沿着任何属性集钻取用户。 按年龄、性别、位置等衡量和比较用户活动。

## 3.3 Network flows （网络流量）
Druid通常用于收集和分析网络流量。 Druid用于沿任何属性集任意切片和切块流数据。

Druid通过能够摄取大量流记录，以及能够以交互速度在查询时对数十个属性进行分组或排序，从而帮助进行网络流分析。这些属性通常包括IP和端口等核心属性，以及通过增强功能添加的属性，
如地理位置、服务、应用程序、设施和ASN。 Druid处理灵活的模式能力意味着您可以添加所需的任何属性。

## 3.4 Digital marketing （数字营销）
Druid通常用于存储和查询在线广告数据。 此数据通常来自广告服务器，对衡量和了解广告系列效果、点击率、转化率（损耗率）等至关重要。

Druid最初旨在为面向用户的数字广告数据分析应用程序提供支持。Druid已经看到这类数据的大量生产用途，并且世界上最大的用户在数千台服务器上存储了数PB的数据。

Druid可用于计算展示次数、点击次数、有效的千次展示费用和关键转化指标、过滤发布商、广告系列、用户信息以及支持完整切片和切块功能的许多其他维度。

## 3.5 Application performance management （应用性能管理）
Druid经常用于跟踪应用程序生成的操作数据。与用户活动用例类似，此数据可以是用户如何与应用程序交互，也可以是应用程序本身发出的度量标准。
Druid可用于深入了解应用程序的不同组件的执行情况，识别瓶颈以及解决问题。

与许多传统解决方案不同，数据的数量、复杂性和吞吐量几乎没有限制。使用数千个属性快速分析应用程序事件，并计算有关负载、性能和使用情况的复杂指标。 
例如，根据95%查询延迟对API端点进行排序，并根据任意一组特殊属性（如时间、用户人口统计或数据中心位置）对这些指标的变化进行切片和切块。

## 3.6 IoT and device metrics （Iot和设备指标）
Druid可用作服务器和设备指标的时间序列解决方案。实时获取机器生成的数据，并执行快速临时分析以测量性能，优化硬件资源或鉴定问题。

与许多传统的时间序列数据库不同，Druid是一个分析引擎。Druid结合了时间序列数据库，面向列的分析数据库和搜索系统的思想。 Druid在单个系统中支持基于时间的分区，
面向列的存储和搜索索引。这意味着基于时间的查询，数字聚合以及搜索和过滤查询都非常快。

您可以在指标中包含数百万个唯一维度值，并对任何维度组合进行任意分组和过滤（Druid中的维度与TSDB中的标记类似）。您可以对标签进行分组和排名，并计算各种复杂的指标。
此外，您可以比传统时间序列数据库更快地搜索和过滤标记值。

## 3.7 OLAP and business intelligence （OLAP和商业智能）
Druid通常用于BI用例。组织机构已经部署了Druid来加速查询和支持应用程序。 与Presto或Hive等SQL-on-Hadoop引擎不同，Druid专为高并发和亚秒查询而设计，
通过UI为交互式数据探索提供动力。 总的来说，这使得Druid更适合真正的交互式视觉分析。

如果您需要面向用户的应用程序并希望用户能够运行自己的自助服务深入查询，那么Druid非常适合。 您可以使用Druid的API开发自己的应用程序，
也可以使用与Druid一起使用的[众多现成应用程序](https://druid.apache.org/libraries)之一。

<br/>

**********

# 四、[Docs](https://druid.apache.org/docs/latest/design/) (文档)

## 目录
### 1 Getting Started
* 1 [Design](https://github.com/yoreyuan/My_hadoop/blob/master/doc/Apache-Druid/Getting-Started.md#1-design-%E8%AE%BE%E8%AE%A1) （设计） 
    + 1.1 [What is Druid?](https://github.com/yoreyuan/My_hadoop/blob/master/doc/Apache-Druid/Getting-Started.md#11-what-is-druid%E4%BB%80%E4%B9%88%E6%98%AFdruid)（什么是Druid？）
    + 1.2 [When should I use Druid?](https://github.com/yoreyuan/My_hadoop/blob/master/doc/Apache-Druid/Getting-Started.md#12-when-should-i-use-druid%E4%BB%80%E4%B9%88%E6%97%B6%E5%80%99%E5%BA%94%E8%AF%A5%E4%BD%BF%E7%94%A8druid)（什么时候应该使用Druid？）
    + 1.3 [Architecture](https://github.com/yoreyuan/My_hadoop/blob/master/doc/Apache-Druid/Getting-Started.md#13-architecture%E6%9E%B6%E6%9E%84)（架构）
        * 1.3.1 [Processes and Servers](https://github.com/yoreyuan/My_hadoop/blob/master/doc/Apache-Druid/Getting-Started.md#131-processes-and-servers-%E8%BF%9B%E7%A8%8B%E5%92%8C%E6%9C%8D%E5%8A%A1) （进程和服务）
        * 1.3.2 [External dependencies](https://github.com/yoreyuan/My_hadoop/blob/master/doc/Apache-Druid/Getting-Started.md#132-external-dependencies-%E5%A4%96%E9%83%A8%E4%BE%9D%E8%B5%96) （外部依赖）
            - 1.3.2.1 [Deep storage](https://github.com/yoreyuan/My_hadoop/blob/master/doc/Apache-Druid/Getting-Started.md#1321-deep-storage%E6%B7%B1%E5%AD%98%E5%82%A8)（深存储）
            - 1.3.2.2 [Metadata storage](https://github.com/yoreyuan/My_hadoop/blob/master/doc/Apache-Druid/Getting-Started.md#1322-metadata-storage%E5%85%83%E6%95%B0%E6%8D%AE%E5%AD%98%E5%82%A8)（元数据存储）
            - 1.3.2.3 [Zookeeper](https://github.com/yoreyuan/My_hadoop/blob/master/doc/Apache-Druid/Getting-Started.md#1323-zookeeper)
        * 1.3.3 [Architecture diagram](https://github.com/yoreyuan/My_hadoop/blob/master/doc/Apache-Druid/Getting-Started.md#133-architecture-diagram-%E6%9E%B6%E6%9E%84%E5%9B%BE) （架构图）
    + 1.4 [Datasources and segments](https://github.com/yoreyuan/My_hadoop/blob/master/doc/Apache-Druid/Getting-Started.md#14-datasources-and-segments%E6%95%B0%E6%8D%AE%E6%BA%90%E5%92%8C%E5%88%86%E7%89%87segments)（数据源和分片（Segments））
    + 1.5 [Query processing](https://github.com/yoreyuan/My_hadoop/blob/master/doc/Apache-Druid/Getting-Started.md#15-query-processing%E6%9F%A5%E8%AF%A2%E5%A4%84%E7%90%86)（查询处理）
    + 1.6 External dependencies（外部依赖）
    + 1.7 Ingestion overview（摄取概述）
* 2 [Getting Started](https://github.com/yoreyuan/My_hadoop/blob/master/doc/Apache-Druid/Getting-Started.md#2-getting-started-%E5%85%A5%E9%97%A8) （入门） 
    + 2.1 [Single-server Quickstart](https://github.com/yoreyuan/My_hadoop/blob/master/doc/Apache-Druid/Getting-Started.md#21-single-server-quickstart-%E5%8D%95%E6%9C%8D%E5%8A%A1%E5%BF%AB%E9%80%9F%E5%85%A5%E9%97%A8) （单服务快速入门）
        * 0 安装
            + 0.1 [Prerequisites](https://github.com/yoreyuan/My_hadoop/blob/master/doc/Apache-Druid/Getting-Started.md#01-prerequisites-%E5%85%88%E5%86%B3%E6%9D%A1%E4%BB%B6) （先决条件）
                * 0.1.1 [Software ](https://github.com/yoreyuan/My_hadoop/blob/master/doc/Apache-Druid/Getting-Started.md#011-software-%E8%BD%AF%E4%BB%B6)（软件）
                * 0.1.2 [Hardware](https://github.com/yoreyuan/My_hadoop/blob/master/doc/Apache-Druid/Getting-Started.md#012-hardware%E7%A1%AC%E4%BB%B6)（硬件）
            + 0.2 [Getting started](https://github.com/yoreyuan/My_hadoop/blob/master/doc/Apache-Druid/Getting-Started.md#02-getting-started-%E5%85%A5%E9%97%A8) （入门）
            + 0.3 [Download Zookeeper](https://github.com/yoreyuan/My_hadoop/blob/master/doc/Apache-Druid/Getting-Started.md#03-download-zookeeper-%E4%B8%8B%E8%BD%BDzookeeper) （下载Zookeeper）
            + 0.4 [Start up Druid services](https://github.com/yoreyuan/My_hadoop/blob/master/doc/Apache-Druid/Getting-Started.md#04-start-up-druid-services-%E5%90%AF%E5%8A%A8druid%E6%9C%8D%E5%8A%A1) （启动Druid服务）
            + 0.5 [Loading Data](https://github.com/yoreyuan/My_hadoop/blob/master/doc/Apache-Druid/Getting-Started.md#05-loading-data-%E5%8A%A0%E8%BD%BD%E6%95%B0%E6%8D%AE) （加载数据）
                * 0.5.1 [Tutorial Dataset](https://github.com/yoreyuan/My_hadoop/blob/master/doc/Apache-Druid/Getting-Started.md#051-tutorial-dataset-%E6%95%99%E7%A8%8B%E6%95%B0%E6%8D%AE%E9%9B%86) （教程数据集）
                * 0.5.2 [Data loading tutorials](https://github.com/yoreyuan/My_hadoop/blob/master/doc/Apache-Druid/Getting-Started.md#052-data-loading-tutorials-%E6%95%B0%E6%8D%AE%E5%8A%A0%E8%BD%BD%E6%95%99%E7%A8%8B) （数据加载教程）
                * 0.5.3 [Resetting cluster state](https://github.com/yoreyuan/My_hadoop/blob/master/doc/Apache-Druid/Getting-Started.md#053-resetting-cluster-state-%E9%87%8D%E7%BD%AE%E9%9B%86%E7%BE%A4%E7%8A%B6%E6%80%81) （重置集群状态）
                    - [Resetting Kafka](https://github.com/yoreyuan/My_hadoop/blob/master/doc/Apache-Druid/Getting-Started.md#resetting-kafka-%E9%87%8D%E7%BD%AEkafka) （重置Kafka） 
        * 1 [Tutorial: Loading a file](https://github.com/yoreyuan/My_hadoop/blob/master/doc/Apache-Druid/Getting-Started.md#1-tutorial-loading-a-file-%E6%95%99%E7%A8%8B%E5%8A%A0%E8%BD%BD%E6%96%87%E4%BB%B6) （教程：加载文件）
            - 1.1 [Loading data with the data loader](https://github.com/yoreyuan/My_hadoop/blob/master/doc/Apache-Druid/Getting-Started.md#11-loading-data-with-the-data-loader-%E4%BD%BF%E7%94%A8%E6%95%B0%E6%8D%AE%E5%8A%A0%E8%BD%BD%E5%99%A8%E5%8A%A0%E8%BD%BD%E6%95%B0%E6%8D%AE) （使用数据加载器加载数据）
            - 1.2 [Loading data with a spec (via console)](https://github.com/yoreyuan/My_hadoop/blob/master/doc/Apache-Druid/Getting-Started.md#12-loading-data-with-a-spec-via-console-%E4%BD%BF%E7%94%A8%E8%A7%84%E8%8C%83%E5%8A%A0%E8%BD%BD%E6%95%B0%E6%8D%AE%E9%80%9A%E8%BF%87%E6%8E%A7%E5%88%B6%E5%8F%B0) （使用规范加载数据（通过控制台））
            - 1.3 [Loading data with a spec (via command line)](https://github.com/yoreyuan/My_hadoop/blob/master/doc/Apache-Druid/Getting-Started.md#13-loading-data-with-a-spec-via-command-line-%E4%BD%BF%E7%94%A8%E8%A7%84%E8%8C%83%E5%8A%A0%E8%BD%BD%E6%95%B0%E6%8D%AE%E9%80%9A%E8%BF%87%E5%91%BD%E4%BB%A4%E8%A1%8C) （使用规范加载数据（通过命令行））
            - 1.4 [Loading data without the script](https://github.com/yoreyuan/My_hadoop/blob/master/doc/Apache-Druid/Getting-Started.md#14-loading-data-without-the-script-%E4%B8%8D%E4%BD%BF%E7%94%A8%E8%84%9A%E6%9C%AC%E5%8A%A0%E8%BD%BD%E6%95%B0%E6%8D%AE) （不使用脚本加载数据）
            - 1.5 [Querying your data](https://github.com/yoreyuan/My_hadoop/blob/master/doc/Apache-Druid/Getting-Started.md#15-querying-your-data-%E6%9F%A5%E8%AF%A2%E4%BD%A0%E7%9A%84%E6%95%B0%E6%8D%AE) （查询你的数据）
            - 1.6 [Cleanup](https://github.com/yoreyuan/My_hadoop/blob/master/doc/Apache-Druid/Getting-Started.md#16-cleanup-%E6%B8%85%E7%90%86) （清理）
            - 1.7 [Further reading](https://github.com/yoreyuan/My_hadoop/blob/master/doc/Apache-Druid/Getting-Started.md#17-further-reading-%E8%BF%9B%E4%B8%80%E6%AD%A5%E9%98%85%E8%AF%BB) （进一步阅读）
        * 2 Tutorial: Loading stream data from Apache Kafka （教程：从Apache Kafka加载流数据）
        * 3 Tutorial: Loading a file using Apache Hadoop （教程：使用Apache Hadoop加载文件）
        * 4 Tutorial: Loading stream data using HTTP push （教程：使用HTTP推送加载流数据）
        * 5 Tutorial: Querying data （教程：查询数据）
        * Further tutorials
        * 6 Tutorial: Rollup （教程：汇总）
        * 7 Tutorial: Configuring retention （教程：保留配置）
        * 8 Tutorial: Updating existing data （教程：更新现有数据）
        * 9 Tutorial: Compacting segments （教程：segment压缩）
        * 10 Tutorial: Deleting data （教程：删除数据）
        * 11 Tutorial: Writing your own ingestion specs （教程：编写你自己的摄取规范）
        * 12 Tutorial: Transforming input data （教程：转换输入数据）
    + 2.2 [Clustering](https://github.com/yoreyuan/My_hadoop/blob/master/doc/Apache-Druid/Getting-Started.md#22-clustering-%E9%9B%86%E7%BE%A4) （集群）
        * [Setting up a Clustered Deployment](https://github.com/yoreyuan/My_hadoop/blob/master/doc/Apache-Druid/Getting-Started.md#setting-up-a-clustered-deployment-%E9%9B%86%E7%BE%A4%E8%AE%BE%E7%BD%AE%E9%83%A8%E7%BD%B2) （集群设置部署）
        * 1 [Select hardware](https://github.com/yoreyuan/My_hadoop/blob/master/doc/Apache-Druid/Getting-Started.md#1-select-hardware-%E7%A1%AC%E4%BB%B6%E9%80%89%E6%8B%A9) （硬件选择）
            + 1.1 [Fresh Deployment](https://github.com/yoreyuan/My_hadoop/blob/master/doc/Apache-Druid/Getting-Started.md#11-fresh-deployment-%E6%96%B0%E9%83%A8%E7%BD%B2)  （新部署）
            + 1.2 [Master Server](https://github.com/yoreyuan/My_hadoop/blob/master/doc/Apache-Druid/Getting-Started.md#12-master-server-master%E6%9C%8D%E5%8A%A1) （Master服务）
            + 1.3 [Data Server](https://github.com/yoreyuan/My_hadoop/blob/master/doc/Apache-Druid/Getting-Started.md#13-data-server-%E6%95%B0%E6%8D%AE%E6%9C%8D%E5%8A%A1) （数据服务）
            + 1.4 [Query Server](https://github.com/yoreyuan/My_hadoop/blob/master/doc/Apache-Druid/Getting-Started.md#14-query-server-%E6%9F%A5%E8%AF%A2%E6%9C%8D%E5%8A%A1) （查询服务）
            + 1.5 [Other Hardware Sizes](https://github.com/yoreyuan/My_hadoop/blob/master/doc/Apache-Druid/Getting-Started.md#15-other-hardware-sizes-%E5%85%B6%E4%BB%96%E7%A1%AC%E4%BB%B6%E5%A4%A7%E5%B0%8F) （其他硬件大小）
        * 2 [Migrating from a Single-Server Deployment](https://github.com/yoreyuan/My_hadoop/blob/master/doc/Apache-Druid/Getting-Started.md#2-migrating-from-a-single-server-deployment-%E4%BB%8E%E5%8D%95%E6%9C%8D%E5%8A%A1%E9%83%A8%E7%BD%B2%E8%BF%81%E7%A7%BB) （从单服务部署迁移）
            + 2.1  [Master Server](https://github.com/yoreyuan/My_hadoop/blob/master/doc/Apache-Druid/Getting-Started.md#21--master-server)
            + 2.2 [Data Server](https://github.com/yoreyuan/My_hadoop/blob/master/doc/Apache-Druid/Getting-Started.md#22-data-server%E6%95%B0%E6%8D%AE%E6%9C%8D%E5%8A%A1)（数据服务）
            + 2.3 [Query Server](https://github.com/yoreyuan/My_hadoop/blob/master/doc/Apache-Druid/Getting-Started.md#23-query-server-%E6%9F%A5%E8%AF%A2%E6%9C%8D%E5%8A%A1) （查询服务）
        * 3 [Select OS](https://github.com/yoreyuan/My_hadoop/blob/master/doc/Apache-Druid/Getting-Started.md#3-select-os-%E9%80%89%E6%8B%A9%E6%93%8D%E4%BD%9C%E7%B3%BB%E7%BB%9F) （选择操作系统）
        * 4 [Download the distribution](https://github.com/yoreyuan/My_hadoop/blob/master/doc/Apache-Druid/Getting-Started.md#4-download-the-distribution-%E4%B8%8B%E8%BD%BD%E5%8F%91%E8%A1%8C%E7%89%88) （下载发行版）
            + 4.1 [Migrating from Single-Server Deployments](https://github.com/yoreyuan/My_hadoop/blob/master/doc/Apache-Druid/Getting-Started.md#41-migrating-from-single-server-deployments-%E4%BB%8E%E5%8D%95%E6%9C%8D%E5%8A%A1%E9%83%A8%E7%BD%B2%E8%BF%81%E7%A7%BB) （从单服务部署迁移）
        * 5 [Configure metadata storage and deep storage](https://github.com/yoreyuan/My_hadoop/blob/master/doc/Apache-Druid/Getting-Started.md#5-configure-metadata-storage-and-deep-storage-%E9%85%8D%E7%BD%AE%E5%85%83%E6%95%B0%E6%8D%AE%E5%AD%98%E5%82%A8%E5%92%8C%E6%B7%B1%E5%AD%98%E5%82%A8) （配置元数据存储和深存储）
            + 5.1 [Migrating from Single-Server Deployments](https://github.com/yoreyuan/My_hadoop/blob/master/doc/Apache-Druid/Getting-Started.md#51-migrating-from-single-server-deployments%E4%BB%8E%E7%94%B5%E6%9C%8D%E5%8A%A1%E9%83%A8%E7%BD%B2%E8%BF%81%E7%A7%BB)（从电服务部署迁移）
            + 5.2 [Metadata Storage](https://github.com/yoreyuan/My_hadoop/blob/master/doc/Apache-Druid/Getting-Started.md#52-metadata-storage-%E5%85%83%E6%95%B0%E6%8D%AE%E5%AD%98%E5%82%A8) （元数据存储）
            + 5.3 [Deep Storage](https://github.com/yoreyuan/My_hadoop/blob/master/doc/Apache-Druid/Getting-Started.md#53-deep-storage-%E6%B7%B1%E5%AD%98%E5%82%A8) （深存储）
            + 5.4 [S3](https://github.com/yoreyuan/My_hadoop/blob/master/doc/Apache-Druid/Getting-Started.md#54-s3)
            + 5.5 [HDFS](https://github.com/yoreyuan/My_hadoop/blob/master/doc/Apache-Druid/Getting-Started.md#55-hdfs)
        * 6 [Configure Tranquility Server (optional)](https://github.com/yoreyuan/My_hadoop/blob/master/doc/Apache-Druid/Getting-Started.md#6-configure-tranquility-server-optional-%E9%85%8D%E7%BD%AEtranquility-server%E5%8F%AF%E9%80%89-) （配置Tranquility Server(可选) ）
        * 7 [Configure for connecting to Hadoop (optional)](https://github.com/yoreyuan/My_hadoop/blob/master/doc/Apache-Druid/Getting-Started.md#7-configure-for-connecting-to-hadoop-optional-%E8%BF%9E%E6%8E%A5hadoop%E9%85%8D%E7%BD%AE%E5%8F%AF%E9%80%89) （连接Hadoop配置(可选)）
        * 8 [Configure Zookeeper connection](https://github.com/yoreyuan/My_hadoop/blob/master/doc/Apache-Druid/Getting-Started.md#8-configure-zookeeper-connection-%E9%85%8D%E7%BD%AEzookeeper%E8%BF%9E%E6%8E%A5) （配置Zookeeper连接）
        * 9 [Configuration Tuning](https://github.com/yoreyuan/My_hadoop/blob/master/doc/Apache-Druid/Getting-Started.md#9-configuration-tuning-%E9%85%8D%E7%BD%AE%E8%B0%83%E6%95%B4) （配置调整）
            + 9.1 [Migrating from a Single-Server Deployment](https://github.com/yoreyuan/My_hadoop/blob/master/doc/Apache-Druid/Getting-Started.md#91-migrating-from-a-single-server-deployment-%E4%BB%8E%E5%8D%95%E6%9C%8D%E5%8A%A1%E9%83%A8%E7%BD%B2%E8%BF%81%E7%A7%BB) （从单服务部署迁移）
                - 9.1.1 [Master](https://github.com/yoreyuan/My_hadoop/blob/master/doc/Apache-Druid/Getting-Started.md#911-master)
                - 9.1.2 [Data](https://github.com/yoreyuan/My_hadoop/blob/master/doc/Apache-Druid/Getting-Started.md#912-data)
                - 9.1.3 [Query](https://github.com/yoreyuan/My_hadoop/blob/master/doc/Apache-Druid/Getting-Started.md#913-query)
            + 9.2 [Fresh deployment](https://github.com/yoreyuan/My_hadoop/blob/master/doc/Apache-Druid/Getting-Started.md#92-fresh-deployment-%E6%9B%B4%E6%96%B0%E9%83%A8%E7%BD%B2) （更新部署）
        * 10 [Open ports (if using a firewall)](https://github.com/yoreyuan/My_hadoop/blob/master/doc/Apache-Druid/Getting-Started.md#10-open-ports-if-using-a-firewall-%E5%BC%80%E6%94%BE%E7%AB%AF%E5%8F%A3%E5%A6%82%E6%9E%9C%E4%BD%BF%E7%94%A8%E4%BA%86%E9%98%B2%E7%81%AB%E5%A2%99) （开放端口(如果使用了防火墙)）
            + 10.1 [Master Server](https://github.com/yoreyuan/My_hadoop/blob/master/doc/Apache-Druid/Getting-Started.md#101-master-server-master%E6%9C%8D%E5%8A%A1) （Master服务）
            + 10.2 [Data Server](https://github.com/yoreyuan/My_hadoop/blob/master/doc/Apache-Druid/Getting-Started.md#102-data-server-%E6%95%B0%E6%8D%AE%E6%9C%8D%E5%8A%A1) （数据服务）
            + 10.3 [Query Server](https://github.com/yoreyuan/My_hadoop/blob/master/doc/Apache-Druid/Getting-Started.md#103-query-server-%E6%9F%A5%E8%AF%A2%E6%9C%8D%E5%8A%A1) （查询服务）
            + 10.4 [Other](https://github.com/yoreyuan/My_hadoop/blob/master/doc/Apache-Druid/Getting-Started.md#104-other-%E5%85%B6%E4%BB%96) （其他）
        * 11 [Start Master Server](https://github.com/yoreyuan/My_hadoop/blob/master/doc/Apache-Druid/Getting-Started.md#11-start-master-server-%E5%90%AF%E5%8A%A8master%E6%9C%8D%E5%8A%A1) （启动Master服务）
            + 11.1 [No Zookeper on Master](https://github.com/yoreyuan/My_hadoop/blob/master/doc/Apache-Druid/Getting-Started.md#111-no-zookeper-on-master-%E5%9C%A8master%E4%B8%8A%E6%B2%A1%E6%9C%89zookeeper) （在Master上没有Zookeeper）
            + 11.2 [With Zookeper on Master](https://github.com/yoreyuan/My_hadoop/blob/master/doc/Apache-Druid/Getting-Started.md#112-with-zookeper-on-master-%E5%9C%A8master%E4%B8%8A%E4%BD%BF%E7%94%A8zookeeper) （在Master上使用Zookeeper）
        * 12 [Start Data Server](https://github.com/yoreyuan/My_hadoop/blob/master/doc/Apache-Druid/Getting-Started.md#12-start-data-server-%E5%90%AF%E5%8A%A8%E6%95%B0%E6%8D%AE%E6%9C%8D%E5%8A%A1) （启动数据服务）
            + 12.1 [Tranquility](https://github.com/yoreyuan/My_hadoop/blob/master/doc/Apache-Druid/Getting-Started.md#121-tranquility) 
        * 13 [Start Query Server ](https://github.com/yoreyuan/My_hadoop/blob/master/doc/Apache-Druid/Getting-Started.md#13-start-query-server-%E5%90%AF%E5%8A%A8%E6%9F%A5%E8%AF%A2%E6%9C%8D%E5%8A%A1)（启动查询服务）
        * 14 [Loading data](https://github.com/yoreyuan/My_hadoop/blob/master/doc/Apache-Druid/Getting-Started.md#14-loading-data-%E5%8A%A0%E8%BD%BD%E6%95%B0%E6%8D%AE) （加载数据）
    + 2.3 [Further examples]()（更多案例）

### 2 Data Ingestion (数据摄取)
* 1 [Data Ingestion](https://github.com/yoreyuan/My_hadoop/blob/master/doc/Apache-Druid/Data-Ingestion.md#data-ingestion-%E6%95%B0%E6%8D%AE%E6%91%84%E5%8F%96) （数据摄取）
    + 1.1 [Datasources and segments](https://github.com/yoreyuan/My_hadoop/blob/master/doc/Apache-Druid/Data-Ingestion.md#11-datasources-and-segments)
        - 1.1.1 [Segment identifiers](https://github.com/yoreyuan/My_hadoop/blob/master/doc/Apache-Druid/Data-Ingestion.md#111-segment-identifiers-segment%E6%A0%87%E8%AF%86%E7%AC%A6) （Segment标识符）
        - 1.1.2 [Segment versioning](https://github.com/yoreyuan/My_hadoop/blob/master/doc/Apache-Druid/Data-Ingestion.md#112-segment-versioning-segment%E7%89%88%E6%9C%AC) （Segment版本）
        - 1.1.3 [Segment states](https://github.com/yoreyuan/My_hadoop/blob/master/doc/Apache-Druid/Data-Ingestion.md#113-segment-states-segment%E7%8A%B6%E6%80%81) （Segment状态）
        - 1.1.4 [Indexing and handoff](https://github.com/yoreyuan/My_hadoop/blob/master/doc/Apache-Druid/Data-Ingestion.md#114-indexing-and-handoff-%E7%B4%A2%E5%BC%95%E5%92%8C%E5%88%87%E6%8D%A2) （索引和切换）
    + 1.2 [Ingestion methods](https://github.com/yoreyuan/My_hadoop/blob/master/doc/Apache-Druid/Data-Ingestion.md#12-ingestion-methods-%E6%91%84%E5%8F%96%E6%96%B9%E6%B3%95) （摄取方法）
    + 1.3 [Partitioning](https://github.com/yoreyuan/My_hadoop/blob/master/doc/Apache-Druid/Data-Ingestion.md#13-partitioning-%E5%88%86%E5%8C%BA) （分区）
    + 1.4 [Rollup](https://github.com/yoreyuan/My_hadoop/blob/master/doc/Apache-Druid/Data-Ingestion.md#14-rollup-%E6%B1%87%E6%80%BB) （汇总）
        - 1.4.1 [Roll-up modes](https://github.com/yoreyuan/My_hadoop/blob/master/doc/Apache-Druid/Data-Ingestion.md#141-roll-up-modes-%E6%B1%87%E6%80%BB%E6%A8%A1%E5%BC%8F) （汇总模式）
    + 1.5 [Data maintenance](https://github.com/yoreyuan/My_hadoop/blob/master/doc/Apache-Druid/Data-Ingestion.md#15-data-maintenance-%E6%95%B0%E6%8D%AE%E7%BB%B4%E6%8A%A4) （数据维护）
        - 1.5.1 [Inserts and overwrites](https://github.com/yoreyuan/My_hadoop/blob/master/doc/Apache-Druid/Data-Ingestion.md#151-inserts-and-overwrites-%E6%91%84%E5%85%A5%E4%B8%8E%E9%87%8D%E5%86%99) （摄入与重写）
        - 1.5.2 [Compaction](https://github.com/yoreyuan/My_hadoop/blob/master/doc/Apache-Druid/Data-Ingestion.md#152-compaction-%E5%8E%8B%E7%BC%A9) （压缩）
        - 1.5.3 [Retention and Tiering](https://github.com/yoreyuan/My_hadoop/blob/master/doc/Apache-Druid/Data-Ingestion.md#153-retention-and-tiering-%E4%BF%9D%E7%95%99%E4%B8%8E%E5%88%86%E5%B1%82) （保留与分层）
        - 1.5.4 [Deletes](https://github.com/yoreyuan/My_hadoop/blob/master/doc/Apache-Druid/Data-Ingestion.md#154-deletes)

### 3 Querying （查询） 
### 4 Design （设计）
### 5 Operations （操作）
### 6 Configuration （配置）
### 7 Development （开发）
### 8 Misc （杂项）




























