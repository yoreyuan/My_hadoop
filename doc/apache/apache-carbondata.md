Apache CarbonData
======
[官网](http://carbondata.apache.org/)   
[CarbonData Github](https://github.com/apache/carbondata)   
[Release](https://cwiki.apache.org/confluence/display/CARBONDATA/Releases)  

CarbonData是一种高性能数据解决方案，支持各种数据分析方案，包括BI分析，临时SQL查询，详细记录快速过滤查找，流分析等。
CarbonData已经部署在许多企业生产环境中，在最大的场景之一中，它支持在具有3PB数据（超过5万亿条记录）的[单个表上进行查询，响应时间少于3秒](https://cwiki.apache.org/confluence/display/CARBONDATA/Apache+CarbonData+1.5.4+Release)！

- - - - 

# 首页

## 什么是 CarbonData
Apache CarbonData是一种索引的柱状数据格式，用于在大数据平台上进行快速分析，例如Apache Hadoop，Apache Spark等

* Multi-dimensional OLAP Query  多维OLAP查询  
* Full Scan Query 完整扫描查询  
* Small Scan Query 微量扫描查询  
* CarbonData: Unified file Format 统一文件格式  
* A single copy of data balance to fit all data access  单个数据平衡拷贝以适应所有数据访问
![CDfullyIndexing.png](http://carbondata.apache.org/images/CDfullyIndexing.png)


## 为何选择CarbonData
1. [独特的数据组织](https://cwiki.apache.org/confluence/display/CARBONDATA/Unique+Data+Organization)  
以`Clounmnar`格式存储数据，个数据块彼此独立地排序，以实现更快的过滤和更好的压缩。
2. [多级索引](https://cwiki.apache.org/confluence/display/CARBONDATA/Multi+Level+Indexing)  
在各个级别使用多个索引，以加快搜索速度并加快查询处理速度。
3. [与大数据生态系统的无缝集成](https://cwiki.apache.org/confluence/display/CARBONDATA/Seamless+Integration+with+Big+Data+Eco-System)  
深度与Spark的DataFrame和SQL兼容和集成。
4. [高级下推优化](https://cwiki.apache.org/confluence/display/CARBONDATA/Advanced+PushDown+Optimizations)  
将大部分查询处理推向数据附近，以最大限地减少读取、处理、转换、传输和混洗的数据量。
5. [字典编码](https://cwiki.apache.org/confluence/display/CARBONDATA/Dictionary+Encoding)  
编码数据可减少存储空间并加快处理速度。
6. [更新和删除支持](https://cwiki.apache.org/confluence/display/CARBONDATA/Update+and+Delete+Support)  
支持update和delete BigData。


## 特性
CarbonData文件格式是HDFS中的一个列式存储，它具有许多现代列式格式的功能，如可拆分、压缩模式、复杂数据类型等，并且CarbonData具有以下独特功能：
* **将数据与索引一起存储**：它可以显着提高查询性能并减少 I/O 扫描和CPU资源（查询中有过滤器）。CarbonData索引由多级索引组成，
处理框架可以利用此索引来减少调度和处理所需的任务，并且还可以在任务端扫描中以更精细的粒度单位（称为blocklet）跳过扫描而不是扫描整个文件。
* **可操作的编码数据**：通过支持高效压缩和全局编码方案，可以查询压缩/编码数据，可以在将结果返回给用户之前转换数据，这是“后期实现的”。
* **支持单一数据格式的各种用例**：如交互式OLAP样式查询，顺序访问（大扫描），随机访问（窄扫描）。


--------

# Documentation

## 在线文档
* What is CarbonData （什么是CarbonData）
* Quick Start （快速开始）
* Use Cases （用例）
* Language Reference （语言参考）
* CarbonData Data Definition Language （CarbonData数据定义语言）
* CarbonData Data Manipulation Language （CarbonData数据操作语言）
* CarbonData Streaming Ingestion （CarbonData流式摄入）
* Configuring CarbonData （配置CarbonData）
* DataMap Developer Guide （DataMap开发人员指南）
* Data Types （数据类型）
* CarbonData DataMap Management （CarbonData DataMap 管理）
* CarbonData BloomFilter DataMap 
* CarbonData Lucene DataMap 
* CarbonData Pre-aggregate DataMap （CarbonData预聚合数DataMap）
* CarbonData Timeseries DataMap
* SDK Guide （SDK指南）
* C++ SDK Guide （C++ SDK指南）
* Performance Tuning （性能调优）
* S3 Storage （S3存储）
* Carbon as Spark's Datasource （Carbon作为Spark的数据源）
* FAQs （常见问题解答）

***********************

# 1 [什么是CarbonData](https://github.com/apache/carbondata/blob/master/docs/introduction.md)
CarbonData是一个完全索引的柱状和Hadoop本机数据存储，用于处理繁重的分析工作负载和使用Spark SQL对大数据进行详细查询。CarbonData允许通过PetaBytes数据进行更快的交互式查询。

## 1.1 意味着什么
CarbonData具有经过特殊工程设计的优化，例如多级索引、压缩和编码技术，旨在提高分析查询的性能，其中包括过滤器、聚合和非重复计数，
用户希望在几个商业级的集群节点上的TB级数据进行查询时能够获得亚秒级的响应时间。

CarbonData有
* 独特的数据组织可加快检索速度并最大程度地减少检索到的数据量
* 用于与Spark进行深度集成的高级下推优化，以便即席使用Spark DataSource API和其他实验功能，从而确保在数据附近执行计算，以最大程度地减少读取、处理、转换和传输（混洗）的数据量
* 多级索引可有效修剪要扫描的文件和数据，从而减少 I/O 扫描和CPU处理

## 1.2 CarbonData 特征和功能
CarbonData具有丰富的功能集，可支持大数据分析中的各种用例。下表列出了CarbonData支持的主要功能。
### 1.2.1 表管理
* DDL（Create, Alter,Drop,CTAS）
CarbonData提供了自己的DDL以创建和管理Carbondata表。这些DDL符合Hive，Spark SQL格式，并支持其他属性和配置，以利用CarbonData功能。

* DML（Load,Insert）
CarbonData提供了自己的DML来管理carbondata表中的数据。它通过配置添加了许多自定义项，以根据用户需求方案完全自定义行为。

* Update 和 Delete
CarbonData支持大数据的更新和删除。CarbonData提供类似于Hive的语法，以支持CarbonData表上的IUD操作。

* Segment管理
CarbonData具有Segment的独特概念，可以有效地管理CarbonData表的增量负载。Segment管理有助于轻松控制表，轻松实现保留，还用于为正在执行的操作提供事务处理功能。

* 分区（Partition）
CarbonData支持2种分区。①类似于hive分区的分区；②CarbonData分区支持哈希、list，范围分区。

* 压缩（Compaction）
CarbonData将增量负载作为segments进行管理。压缩有助于压缩不断增长的segments数，还可以改善查询过滤器的修剪。

* 外部表（External Tables）
CarbonData可以读取任何carbondata文件并自动从文件中推断模式，并提供关系表视图以使用Spark或任何其他应用程序执行sql查询。

### 1.2.2 DataMaps（数据映射）
* 预聚合（Pre-Aggregate）
CarbonData具有数据映射的概念，可在查询时帮助修剪数据，从而提高性能。预聚合表（Pre Aggregate tables ）是一种数据映射，可以按数量级提高查询性能。
CarbonData将自动预聚合增量数据并重新写入查询以自动从最合适的预聚合表中提取，以更快地为查询提供服务。

* 时间序列(Time Series)
CarbonData建立在对时间顺序（年，月，日，小时，分钟，秒）的理解中。时间序列是一个预聚合表，可以在增量加载期间自动将数据汇总到所需级别，并从最合适的预聚合表提供查询。

* 布隆过滤器(Bloom filter)
CarbonData支持将bloom筛选器作为数据映射，以便快速有效地修剪数据以进行扫描并实现更快的查询性能。

* Lucene
Lucene在索引较长的文本数据方面很受欢迎。CarbonData提供了Lucene数据映射，因此可以使用lucene对文本列进行索引，并将索引结果用于在查询过程中有效修剪要检索的数据。

* MV（物化视图）
MV是一种预先聚合的表，可以支持有效的查询重写和处理。CarbonData提供的MV可以重写查询以从任何表（包括非Carbondata表）获取数据。典型的用例是将非carbondata事实表的聚合数据存储到carbondata中，并使用mv重写查询以从carbondata中获取。

### 1.2.3 Streaming
* Spark Streaming
CarbonData支持准实时地将数据流转换为carbondata并使其立即可用于查询。CarbonData提供了DSL，可轻松创建源表和接收器表，而无需用户编写应用程序。

### 1.2.4 SDK
* CarbonData 写入
CarbonData支持使用SDK从非 Spark 应用程序写入数据。用户可以使用SDK从自定义应用程序生成Carbondata文件。 典型的用例是编写插入到kafka的流应用程序，并使用carbondata作为用于存储的接收器（目标）表。

* CarbonData 读取
CarbonData支持使用SDK从非 Spark 应用程序读取数据。 用户可以使用SDK从其应用程序中读取碳数据文件并进行自定义处理。

### 1.2.5 存储
* S3
CarbonData可以写入S3、OBS或任何确认S3协议的云存储。 CarbonData使用 HDFS api写入云对象存储。

* HDFS
CarbonData使用HDFS API从HDFS写入和读取数据。 CarbonData可以利用位置信息来有效地建议Spark在数据附近运行任务。

* Alluxio
CarbonData还支持使用[Alluxio](https://github.com/apache/carbondata/blob/master/docs/quick-start-guide.md#alluxio) 进行读写。

## 1.3 与大数据生态系统整合
有关将CarbonData与这些执行引擎集成的详细信息，请参阅 [Presto](https://github.com/apache/carbondata/blob/master/docs/quick-start-guide.md#spark) 
与 [Spark](https://github.com/apache/carbondata/blob/master/docs/quick-start-guide.md#presto) 集成。

## 1.4 CarbonData适用的方案
CarbonData可用于各种分析工作负载。此处记录了一些使用CarbonData的最[典型用例](https://github.com/apache/carbondata/blob/master/docs/usecases.md)。

## 1.5 性能结果
![carbondata-performance.png](https://github.com/apache/carbondata/blob/master/docs/images/carbondata-performance.png?raw=true)


----------
# 2 Quick Start （快速开始）
本教程提供了使用CarbonData的快速介绍。要遵循本指南，请先从[CarbonData网站](https://dist.apache.org/repos/dist/release/carbondata/)下载打包版本的CarbonData，
或者可以按照[构建CarbonData](https://github.com/apache/carbondata/tree/master/build)的步骤创建它。

## 2.1 要求
* CarbonData支持Spark版本最高为2.2.1。请从[Spark网站](https://spark.apache.org/downloads.html)下载Spark软件包。
* 使用以下命令创建`sample.csv`文件。将数据加载到CarbonData时需要CSV文件
```bash
cd carbondata
cat > sample.csv << EOF
id,name,city,age
1,david,shenzhen,31
2,eason,shenzhen,27
3,jarry,wuhan,35
EOF
```

## 2.2 集成
### 2.2.1 与执行引擎集成 
CarbonData可以与Spark、Presto和Hive执行引擎集成。以下文档指南有关使用这些执行引擎进行安装和配置。

#### 2.2.1.1 Spark

#### 2.2.1.2 Presto

#### 2.2.1.3 Hive


### 2.2.2 与存储引擎集成 
#### 2.2.2.1 HDFS

#### 2.2.2.2 S3

#### 2.2.2.3 Alluxio


## 2.3 安装和配置CarbonData以在Spark Shell本地运行


----------
# 3 Use Cases （用例）
CarbonData可用于各种分析工作负载。此处记录了一些使用CarbonData的最典型用例。 

CarbonData用于但不限于

*银行
    - 欺诈检测分析
    - 风险状况分析
    - 作为zip表来更新客户的每日余额
* 电讯
    - 为VIP客户检测信号异常，以提供更好的客户体验
    - 分析GSM数据的MR、CHR记录，以确定特定时间段的高负载并重新平衡配置
    - 分析访问站点、视频、屏幕尺寸、流带宽、质量以确定网络质量，路由配置
* 网络/互联网
    - 分析正在访问的页面或视频、服务器负载、流质量、屏幕尺寸
* 智慧城市
    - 车辆追踪分析
    - 异常行为分析

这些用例可以大致分为以下几类：
* 全面扫描/明细/交互式查询
* 聚合/ OLAP BI查询
* 实时提取（流式传输）和查询

## 3.1 电信场景中的明细查询
### 场景
用户想要分析移动用户的所有CHR（呼叫历史记录）和MR（测量记录），以识别10秒内的服务故障。用户还希望对数据运行机器学习模型，以公平地估计可能发生故障的原因和时间，
并提前采取行动以满足VIP客户的SLA（服务水平协议）。

### 挑战
* 数据输入速率可能会根据用户在特定时间段内的专注程度而有所不同，因此需要更高的数据加载速度
* 需要充分利用群集，并在各种应用程序之间共享群集，以更好地消耗资源并节省资源
* 查询需要是交互式的，即查询获取少量数据，并且需要在几秒钟内返回
* 每隔几分钟将数据加载到系统中。

### 解决
设置由YARN管理的 Hadoop + Spark + CarbonData 集群。

对CarbonData提出以下配置建议（这些调整是在CarbonData引入`SORT_COLUMNS`参数之前提出的，使用该参数排序顺序和架构顺序可能会有所不同。）

**将常用列添加到表定义的左侧**。按基数升序添加。建议将msisdn、imsi列保留在模式的开头。使用最新的CarbonData时，需要在开始时对msisdn、imsi配置`SORT_COLUMNS`。

随着时间戳的自然增加，在其右侧添加时间戳列。

为查询和数据加载创建两个单独的YARN队列。

除此之外，建议在集群中配置以下CarbonData配置。

配置 | 参数 | 值 | 描述
---- | ---- | ---- | ----
数据载入    | `carbon.graph.rowset.size`                | 100000 | 根据每行的大小，这确定了数据加载期间所需的内存。较高的值会导致内存占用量增加
数据载入    | `carbon.number.of.cores.while.loading`    | 12     | 更多内核可以提高数据加载速度
数据载入    | `carbon.sort.size`                        | 100000 | 一次排序的记录数。配置的记录数更多将导致内存占用增加
数据载入    | `table_blocksize`                         | 256    | 在查询过程中有效安排多个任务
数据载入    | `carbon.sort.intermediate.files.limit`    | 100    | 核心数量增加时增加到100个。可以在backgorund中执行合并。如果要合并的文件数量较少，则排序线程将处于空闲状态
数据载入    | `carbon.use.local.dir`                    | TRUE   | yarn应用程序目录通常位于单个磁盘上。YARN将配置有多个磁盘以用作临时文件或随机分配给应用程序。使用yarn temp目录将允许Carbon使用多个磁盘并提高IO性能
压缩       | `carbon.compaction.level.threshold`       | 6,6    | 由于频繁的小负载，压缩更多的segments将提供更好的查询结果
压缩       | `carbon.enable.auto.load.merge`           | true   | 由于数据加载较小，因此自动压缩可减少segments数，并且压缩可以及时完成
压缩       | `carbon.number.of.cores.while.compacting` | 4      | 核心数更多可以提高压实速度
压缩       | `carbon.major.compaction.size`            | 921600 | 多个负载的总和，可合并为一个segment

### 取得的成果
参数  |	结果
---- | ----
查询  |	<3秒
数据载入速度            |	每个节点40 MB/s
并发查询性能（20个查询） |	<10秒

## 3.2 智慧城市场景中的详细查询
### 场景
用户想要分析特定时间段内的人/车辆的运动和行为。此输出数据需要与外部表结合在一起以提取人员详细信息。该查询将以不同的时间段作为过滤器运行，以识别潜在的行为不匹配。

### 挑战
* 每天生成的数据非常庞大。每天需要加载多次数据以适应传入数据的大小。
* 每6小时完成一次数据加载。

### 解决
设置由YARN管理的 Hadoop + Spark + CarbonData 集群。

由于需要在一段时间内查询数据，因此建议将时间列保留在schema的开头。

使用表的block大小为512MB。

使用本地排序模式。

除此之外，建议在集群中配置以下CarbonData配置。

由于基数很高，因此使用所有列都是无字典的。

配置 | 参数 | 值 | 描述
---- | ---- | ---- | ----
数据载入    | `carbon.graph.rowset.size`                | 100000 | 根据每行的大小，这确定了数据加载期间所需的内存。较高的值会导致内存占用量增加
数据载入    | `enable.unsafe.sort`                      | TRUE   | 排序期间生成的临时数据数量巨大，这会导致GC瓶颈。使用unsafe可以减少GC的压力
数据载入    | `enable.offheap.sort`                     | TRUE   | 排序期间生成的临时数据数量巨大，这会导致GC瓶颈。使用offheap可以减轻对GC的压力。offheap可以通过java `unsafe.hence enable.unsafe.sort`需要为true
数据载入    | `offheap.sort.chunk.size.in.mb`           | 128    | 分配用于排序的内存大小。可以根据可用内存增加此值大小
数据载入    | `carbon.number.of.cores.while.loading`    | 12     | 更多内核可以提高数据加载速度
数据载入    | `carbon.sort.size`                        | 100000 | 一次排序的记录数。配置的记录数更多将导致内存占用增加
数据载入    | `table_blocksize`                         | 512    | 在查询过程中有效安排多个任务
数据载入    | `carbon.sort.intermediate.files.limit`    | 100    | 核心数量增加时增加到100个。可以在backgorund中执行合并。如果要合并的文件数量较少，则排序线程将处于空闲状态
数据载入    | `carbon.use.local.dir`                    | TRUE   | yarn应用程序目录通常位于单个磁盘上。YARN将配置有多个磁盘以用作临时文件或随机分配给应用程序。使用yarn temp目录将允许Carbon使用多个磁盘并提高IO性能
数据载入    | `sort.inmemory.size.in.mb`                | 92160  | 分配用于执行内存排序的内存。当节点中有更多内存可用时，配置此选项将在内存中保留更多排序块，从而由于没有或者非常少的IO而使合并排序更快
压缩       | `carbon.major.compaction.size`            | 921600  | 多个负载的总和，可合并为一个segment
压缩       | `carbon.number.of.cores.while.compacting` | 12      | 核心数更多可以提高压实速度
压缩       | `carbon.enable.auto.load.merge`           | FALSE   | 由于数据量巨大，因此执行自动次要压缩是一项成本很高的过程。当集群负载较少时执行手动压缩
查询       | `carbon.enable.vector.reader`             | true    | 为了更快地获取结果，支持Spark向量处理将加快查询速度
查询       | `enable.unsafe.in.query.processing`       | true    | 需要大量扫描的数据，这反过来又会生成寿命较短的Java对象。这会导致GC压力。使用unsafe和offheap将减少GC开销
查询       | `use.offheap.in.query.processing`         | true    | 需要大量扫描的数据，这反过来又会生成寿命较短的Java对象。这会导致GC压力。使用unsafe和offheap将减少GC开销.offheap可以通过` unsafe.hence`访问，因此`enable.unsafe.in.query.processing`需要为true
查询       | `enable.unsafe.columnpage`                | TRUE    | 将列页面保留在堆内存中，以减少由Java对象引起的内存开销，并减少GC压力。
查询       | `carbon.unsafe.working.memory.in.mb`      | 10240   | 用于堆操作的内存量，您可以根据数据大小增加此内存


### 取得的成果
参数  |	结果
---- | ----
查询（跨1个segment的时间段）  |	<3秒
资料载入速度            |	每个节点45 MB/s


## 3.3 Web/Internet 方案中的 OLAP/BI 查询
### 场景
一家互联网公司希望分析平均下载速度，在特定区域或地区中使用的手机的种类，正在使用的Apps的种类，在特定区域中流行的视频类型，以使他们能够确定合适的视频分辨率大小，加快转移，并执行更多分析以更好地为客户服务。

### 挑战
* 由于数据是通过BI工具查询的，因此所有查询都包含group by，这意味着CarbonData需要返回更多记录，因为不能将限制下推到carbondata层。
* 结果必须更快地返回，因为BI工具在获取数据之前不会响应，从而导致不良的用户体验。
* 数据加载的频率可能较低（一天一次或两次），但是原始数据量很大，这导致按查询分组的运行速度较慢。
* 由于BI仪表板，并发查询可能更多

### 目标
1. 聚合查询更快
2. 并发性高（支持的并发查询数）

### 解决
* 使用表 block 大小为128MB，以便更有效地进行修剪
* 使用全局排序模式，以便将要提取的数据分组在一起
* 为非基于时间戳的分组创建基于查询的预聚合表
* 对于包含按日期分组的查询，请创建基于时间序列的Datamap（预聚合）表，以便在创建过程中汇总数据，并且提取速度更快
* 减少Spark shuffle分区。（在我们的14节点群集上，将其配置从默认值200减少到35）。
* 为基数较少的列启用全局字典。可以对编码数据进行聚合，从而提高性能
* 对于基数较高的列，请启用本地字典，以使存储量较小并可以利用字典来进行扫描

## 3.4 处理近实时数据摄取场景
### 场景
需要支持存储连续到达的数据并使其立即可用于查询。

### 挑战
当数据摄取接近实时并且需要立即提供数据以供查询时，通常的情况是以微批方式加载数据，但这会导致生成许多小文件的问题。这带来了两个问题：
* HDFS中的小文件处理效率低下
* CarbonData会降低查询性能，因为当非时间列上的过滤器时，所有小文件都必须被查询
* CarbonData将降低查询性能，因为当非时间列上的过滤器必须查询所有小文件时。
* 由于数据不断到达，因此分配资源进行压缩可能不可行。

### 目标
1. 数据到达时几乎可以实时查询
2. CarbonData不会遇到小文件问题

### 解决
* 使用CarbonData的流表支持
* 如果不担心查询性能稍慢，则将`carbon.streaming.segment.max.size`属性配置为更高的值（默认为1GB）
* 将`carbon.streaming.auto.handoff.enabled`配置为true，以便在达到`carbon.streaming.segment.max.size`之后，该 segment 将转换为针对查询优化的格式
* 禁用自动压缩，当集群不忙时以默认值`4,3`手动触发次要压缩
* 根据 segments 的大小和创建 segments 的频率手动触发重要的压缩
* 启用本地字典

---------



# [快速开始](https://github.com/apache/carbondata/blob/master/docs/quick-start-guide.md)

访问[/release/carbondata](https://dist.apache.org/repos/dist/release/carbondata/)，选择版本下载，
可以下下载源码[构建CarbonData](https://github.com/apache/carbondata/tree/master/build)，
也可以下载编译好的版本的jar包的[carbondata](https://dist.apache.org/repos/dist/release/carbondata/)。

## 下载
例如这里下载
[apache-carbondata-1.6.0-bin-spark2.3.2-hadoop2.7.2.jar](https://dist.apache.org/repos/dist/release/carbondata/1.6.0/apache-carbondata-1.6.0-bin-spark2.3.2-hadoop2.7.2.jar)

## 先决条件
[Spark 2.3.2](http://archive.apache.org/dist/spark/spark-2.3.2/)







