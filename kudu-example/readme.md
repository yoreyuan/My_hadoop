
# 一、Kudu概述
## GitHub
Kudu主要是由 C++ 实现的。  
[apache/kudu](https://github.com/apache/kudu)  

[Kudu 官方文档](https://kudu.apache.org/docs/)


## 低延迟随机访问
与其他大数据分析存储不同，Kudu不仅仅是一种文件格式。它是一个实时存储系统，支持对各行进行低延迟毫秒级访问。
对于“NoSQL”式访问，您可以选择Java，C ++或Python API。当然，这些随机访问API可以与机器学习或分析的批量访问结合使用。
[Kudu Java API](https://kudu.apache.org/apidocs/)

## 超快的列式存储  
Kudu可以在几秒钟内对数十亿行和数TB的数据执行向下钻取和大海捞针查询。

## 可以适应于 Apache Hadoop 生态系统
Kudu旨在使用 Hadoop 生态系统，且与其他数据处理框架的集成很简单。
你可以从实时数据源的流式数据中使用 Java 客户端，然后使用Spark、Impala 或者 MapReduce 在到达时立即处理它。
甚至你可以透明的在 其他 Hadoop存储例如 HDFS 或者 HBase透明的添加Kudu表最为数据存储。 
    
## 分布式和容错
为了确保数据安全可用，Kudu使用 Raft 一致性算法复制给定表的所有操作。
Raft确保在响应客户端请求之前，每个写入至少由两个节点保留，确保不会因机器故障而丢失任何数据。
当计算机出现故障时，副本会在几秒钟内重新配置，以保持极高的系统可用性。

- - - - 
# 二、Kudu 和 Impala 集成

1. create/alter/drop table  
Imapala 支持使用 Kudu 作为持久层创建，更改和删除表。这些表遵循与Impala中其他表相同的内部/外部方法，允许灵活的数据提取和查询。
    
2. insert  
可以使用与任何其他Impala表相同的语法将数据插入Impala中的Kudu表，例如使用HDFS或HBase进行持久化的表。

3. update/delete
Impala 支持 update 和 delete SQL 命令逐行或批量修改 Kudu 表中的现有数据。选择 SQL 命令的语法与现有标准尽可能兼容。
除了simple delete 或 update 命令之外，还可以使用 from 子查询中的子句指定复杂连接。

4. 灵活的分区
与Hive中的表分区类似，Kudu 允许您通过散列或范围动态地将表预分割为预定义数量的块(tablets)，以便在整个群集中均匀分配写入和查询。
您可以按任意数量的主键列，任意数量的哈希值和可选的拆分行列表进行分区。

5. 并行扫描
为了在现代硬件上实现最高性能，Impala 使用的 Kudu 客户端在多个 tablets 上并行扫描。

6. 高效查询
在可能的情况下，Impala 将预评估推送到 Kudu，以便预评估尽可能接近数据。在许多工作负载中，查询性能与 Parquet 相当。

# 三、架构
## 服务
* Master  
跟踪所有的 tablets, tablet servers, 目录 Table及与集群相关的其他元数据服务。
在给定的时间上，只能有一个作为master(leader)，如果当前 leader 丢失，新的 master 由 Raft 一致性算法选出。

主服务还协调客户端的元数据操作。例如，在创建表时，客户端在内部请求发送到 master。master将新表的元数据信息写入目录表，
并协调在 tablet服务上创建 tablets的过程。

所有 master 的数据都存储在 tablet，可以复制到所有候选 master。Tablet 服务可以设定间隔（默认为每秒一次）对主设备进行心跳

* Tablet Server  
一个 tablet 服务为客户端存储和 服务tablet。对于给定的 tablet，一个 tablet 服务充当 leader，而其他 tablet 充当 follower 副本。
当每一个 leaders 或者 follower service读请求时，leader 服务写入请求。使用 `Raft 一致性算法` 选出 leader。
一个 tablet 服务可以提供多个 tablet，一个 tablet 能够由多个 tablet servers 服务。  

* Catalog Table
目录表是 Kudu 元数据的中心位置。它存储有关 tables 和 tablets 的信息。目录表可能无法直接读取和写入。
相反，它只能通过客户端 API中公开的元数据操作访问。
    + Tables： 表模式，位置，状态
    + Tablets：现有的 tablets 列表，tablet 服务有每一个 tablet的副本，tablets的当前状态，和起始和结束的key。 
    
    
![Kudu network architecture](https://kudu.apache.org/docs/images/kudu-architecture-2.png)


**由于多种原因，Kudu非常适合时间序列工作负载**


- - - - 
# 安装 Apache Kudu
[Installing Apache Kudu](https://kudu.apache.org/docs/installation.html)

## 先决条件和要求
* 一个或多个主机运行 `master`。建议使用 一个master(没有容错的)，或者三个master(可容忍一个故障)，**master数量一定为奇数**
* 一个或多个主机运行 `tablet servers`, 当使用副本时，需要最少三个 tablet servers。

## RHEL 或 CentOS 上搭建

## Kudu 与 Spark 集成

spark2-shell --packages org.apache.kudu:kudu-spark2_2.11:1.7.0


# 常用的 Kudu UI 链接
[](http://cdh:8050)
[](http://cdh:8051)
[进程内跟踪 UI ](http://cdh:8051/tracing.html)


- - - - 
# 使用Apache Kudu开发应用程序
Kudu提供C ++，Java和Python客户端API，以及用于说明其用途的参考示例。

[kudu/examples/](https://github.com/apache/kudu/tree/master/examples)

主要以 `Java` 为例  
在项目引入依赖
```xml
<dependency>
  <groupId>org.apache.kudu</groupId>
  <artifactId>kudu-client</artifactId>
  <version>1.7.0</version>
</dependency>
```

java目录下有三个项目，
```
collectl
一个小型Java应用程序，它在TCP套接字上侦听与Collectl有线协议对应的时间序列数据。通用的collectl工具可用于将示例数据发送到服务器。

insert-loadgen
生成随机插入负载的Java应用程序。

java-example
连接到Kudu实例的简单Java应用程序，创建表，向其写入数据，然后删除表。

```

## collectl

`java -jar target/kudu-collectl-example-1.0-SNAPSHOT.jar`

`$ collectl --export=graphite,127.0.0.1,p=/`

impala-shell
```sql
--从kudu信息中创建一个impala表
CREATE EXTERNAL TABLE collectl_metrics
STORED AS KUDU
TBLPROPERTIES(
  'kudu.table_name' = 'collectl_metrics'
);

--查看创建的表
[cdh3:21000] > desc collectl_metrics;
Query: describe collectl_metrics
+-----------+--------+---------+-------------+----------+---------------+---------------+---------------------+------------+
| name      | type   | comment | primary_key | nullable | default_value | encoding      | compression         | block_size |
+-----------+--------+---------+-------------+----------+---------------+---------------+---------------------+------------+
| host      | string |         | true        | false    |               | DICT_ENCODING | DEFAULT_COMPRESSION | 0          |
| metric    | string |         | true        | false    |               | DICT_ENCODING | DEFAULT_COMPRESSION | 0          |
| timestamp | int    |         | true        | false    |               | BIT_SHUFFLE   | DEFAULT_COMPRESSION | 0          |
| value     | double |         | false       | false    |               | BIT_SHUFFLE   | DEFAULT_COMPRESSION | 0          |
+-----------+--------+---------+-------------+----------+---------------+---------------+---------------------+------------+
Fetched 4 row(s) in 5.29s

--查看表中统计信息
[cdh3:21000] > select count(distinct metric) from collectl_metrics;
Query: select count(distinct metric) from collectl_metrics
Query submitted at: 2019-04-29 10:01:12 (Coordinator: http://cdh3:25000)
Query progress can be monitored at: http://cdh3:25000/query_plan?query_id=96433bc31a66ab89:e7a3996600000000
+------------------------+
| count(distinct metric) |
+------------------------+
| 23                     |
+------------------------+
Fetched 1 row(s) in 2.08s

```
















