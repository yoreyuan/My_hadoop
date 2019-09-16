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
以`Clounmnar`格式存储数据，每个数据块（行组）独立于另一个进行排序，意识下更快的过滤和更好的压缩。
2. [多级索引](https://cwiki.apache.org/confluence/display/CARBONDATA/Multi+Level+Indexing)  
利用各种级别的多个索引来实现更快的搜索并加快查询处理速度。
3. [与大数据生态系统的无缝集成](https://cwiki.apache.org/confluence/display/CARBONDATA/Seamless+Integration+with+Big+Data+Eco-System)  
深度与Spark的DataFrame和SQL的合规性集成。
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

# [什么是CarbonData](https://github.com/apache/carbondata/blob/master/docs/introduction.md)
CarbonData是一个完全索引的柱状和Hadoop本机数据存储，用于处理繁重的分析工作负载和使用Spark SQL对大数据进行详细查询。CarbonData允许通过PetaBytes数据进行更快的交互式查询。

CarbonData具有专门设计的优化功能，如多级索引，压缩和编码技术，旨在提高分析查询的性能，其中包括过滤器，聚合和不同的计数，
用户希望在只有几个商业硬件集群的节点上的TB级别的数据提供一个亚秒级的查询响应。


# [快速开始](https://github.com/apache/carbondata/blob/master/docs/quick-start-guide.md)

访问[/release/carbondata](https://dist.apache.org/repos/dist/release/carbondata/)，选择版本下载，
可以下下载源码[构建CarbonData](https://github.com/apache/carbondata/tree/master/build)，
也可以下载编译好的版本的jar包的[carbondata](https://dist.apache.org/repos/dist/release/carbondata/)。

## 下载
例如这里下载
[apache-carbondata-1.6.0-bin-spark2.3.2-hadoop2.7.2.jar](https://dist.apache.org/repos/dist/release/carbondata/1.6.0/apache-carbondata-1.6.0-bin-spark2.3.2-hadoop2.7.2.jar)

## 先决条件
[Spark 2.3.2](http://archive.apache.org/dist/spark/spark-2.3.2/)







