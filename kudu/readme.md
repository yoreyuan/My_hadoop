
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

CREATE EXTERNAL TABLE collectl_metric_ids
STORED AS KUDU
TBLPROPERTIES(
  'kudu.table_name' = 'collectl_metric_ids'
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

[cdh3:21000] > desc collectl_metric_ids;
Query: describe collectl_metric_ids
+--------+--------+---------+-------------+----------+---------------+---------------+---------------------+------------+
| name   | type   | comment | primary_key | nullable | default_value | encoding      | compression         | block_size |
+--------+--------+---------+-------------+----------+---------------+---------------+---------------------+------------+
| host   | string |         | true        | false    |               | AUTO_ENCODING | DEFAULT_COMPRESSION | 0          |
| metric | string |         | true        | false    |               | AUTO_ENCODING | DEFAULT_COMPRESSION | 0          |
+--------+--------+---------+-------------+----------+---------------+---------------+---------------------+------------+
Fetched 2 row(s) in 0.01s

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

## insert-loadgen
随机插入负载生成器。这将使用 `AUTO_BACKGROUND_FLUSH`模式尽可能快地插入到预先存在的表中。
所有字段都是随机的。负载生成器将继续插入，直到它停止或遇到错误。此负载生成器是单线程的。

### 现在 Impala-shell 创建kudu测试表
该程序不会创建“loadgen_test”表。需要通过其他方式创建表，例如通过impala-shell。
```sql
--这里通过impala-shell创建的表，在代码中应写全名称：impala::kudu_test.loadgen_test
CREATE TABLE kudu_test.loadgen_test(
    id INT,
    name STRING,
    password STRING,
    PRIMARY KEY(id)
)
PARTITION BY HASH PARTITIONS 3
STORED AS KUDU;

--修改字段名：alter  table loadgen_test change age  password string;

[cdh3:21000] > desc loadgen_test;
Query: describe loadgen_test
+----------+--------+---------+-------------+----------+---------------+---------------+---------------------+------------+
| name     | type   | comment | primary_key | nullable | default_value | encoding      | compression         | block_size |
+----------+--------+---------+-------------+----------+---------------+---------------+---------------------+------------+
| id       | int    |         | true        | false    |               | AUTO_ENCODING | DEFAULT_COMPRESSION | 0          |
| name     | string |         | false       | true     |               | AUTO_ENCODING | DEFAULT_COMPRESSION | 0          |
| password | string |         | false       | true     |               | AUTO_ENCODING | DEFAULT_COMPRESSION | 0          |
+----------+--------+---------+-------------+----------+---------------+---------------+---------------------+------------+
Fetched 3 row(s) in 0.01s

--查询插入的数据
[cdh3:21000] > select * from loadgen_test limit 5;
Query: select * from loadgen_test limit 5
Query submitted at: 2019-04-29 18:03:12 (Coordinator: http://cdh3:25000)
Query progress can be monitored at: http://cdh3:25000/query_plan?query_id=f045af6d56427126:8b184e8f00000000
+--------+--------------------------------------+--------------------------------------+
| id     | name                                 | password                             |
+--------+--------------------------------------+--------------------------------------+
| 118831 | ffc8f3a4-7274-44c4-a3ec-01fda5c9528a | ec502177-8f64-4698-958a-e22ae26f8fd7 |
| 148400 | 45dd6c75-1a7f-4f72-9082-1cce10d13e61 | 511cb05e-5021-4f62-81bc-e068d39983b0 |
| 183015 | 406e8a0e-3c67-4698-9ac7-e98a969a8709 | ce24cf04-f03f-4dd4-b012-264b2c44a12a |
| 233081 | 45e5917d-d4d3-4117-9a9f-55ca5a3c4796 | 9ab7c492-70e8-4fb8-a35c-7125b22981af |
| 276233 | d25a65ee-d2c1-44f9-b399-c8361bfffb88 | a298bc65-6c8e-4e8a-8dfc-40c4d8663143 |
+--------+--------------------------------------+--------------------------------------+
Fetched 5 row(s) in 0.13s

```

### 运行
```
# master_addresses 为 Kudu 的master地址。 table_name是预先存在的表名。
java -jar target xxx.jar master_addresses table_name
```
例如：`java -jar target xxx.jar cdh3:7051 impala::kudu_test.loadgen_test`


## java-example
使用同步 Kudu 客户端的简单实例
 - 创建一个表
 - 插入行
 - 修改一个表
 - 扫描行
 - 删除表格



### 运行
```
$ mvn package
$ java -jar target/kudu-java-example-1.0-SNAPSHOT.jar
```

运行参数说明：  
可以为 kudu 指定一组不同的主服务器，只需将属性 kuduMaster设置为 csv 格式的主机地址，如： host:port  
```
java -DkuduMasters=master-0:7051,master-1:7051,master-2:7051 -jar target/kudu-java-example-1.0-SNAPSHOT.jar
```

### 创建示例表
运行 `Example.createExampleTable(client, tableName)`，

#### 查看表
可以用一下两种方式查看创建的表：
1. kudu  
`kudu table list cdh3:7051`  

2. impala-shell  
可以打开 `Kudu UI`，查看创建的表信息。浏览器打开 `kudu-master-ip:8051` ， 点击 `Tables` ,可以看到刚才创建的表 `java_example-时间戳`，  
点击打开，查看详细信息。这里已经有 impala shell 查看表的语句了，直接打开 `impala-shell` 输入如下语句

```sql
[cdh3:21000] > CREATE EXTERNAL TABLE java_example_1556593492110
             > STORED AS KUDU
             > TBLPROPERTIES(
             >     'kudu.table_name' = 'java_example_1556593492110',
             >     'kudu.master_addresses' = 'cdh3:7051'
             > );
Query: CREATE EXTERNAL TABLE java_example_1556593492110
STORED AS KUDU
TBLPROPERTIES(
    'kudu.table_name' = 'java_example_1556593492110',
    'kudu.master_addresses' = 'cdh3:7051'
)
Fetched 0 row(s) in 1.22s
``` 
![java_example-timestamp-detail](kudu_demo/src/main/resources/java_example-timestamp-detail.png)

### 插入数据
运行 `Example.insertRows(client, "java_example_1556593492110", 33);`

#### 查看数据
```sql
[cdh3:21000] > select * from java_example_1556593492110;
Query: select * from java_example_1556593492110
Query submitted at: 2019-04-30 11:42:20 (Coordinator: http://cdh3:25000)
Query progress can be monitored at: http://cdh3:25000/query_plan?query_id=4e490d235f4b8876:1190259500000000
+-----+----------+
| key | value    |
+-----+----------+
| 12  | NULL     |
| 14  | NULL     |
| 30  | NULL     |
| 8   | NULL     |
| 9   | value 9  |
| 22  | NULL     |
| 25  | value 25 |
| 32  | NULL     |
| 11  | value 11 |
| 5   | value 5  |
| 7   | value 7  |
| 6   | NULL     |
| 1   | value 1  |
| 2   | NULL     |
| 16  | NULL     |
| 17  | value 17 |
| 21  | value 21 |
| 31  | value 31 |
| 15  | value 15 |
| 18  | NULL     |
| 23  | value 23 |
| 24  | NULL     |
| 27  | value 27 |
| 28  | NULL     |
| 13  | value 13 |
| 19  | value 19 |
| 3   | value 3  |
| 10  | NULL     |
| 4   | NULL     |
| 20  | NULL     |
| 26  | NULL     |
| 29  | value 29 |
| 0   | NULL     |
+-----+----------+
Fetched 33 row(s) in 0.13s
```

### 修改表（添加一列）
例如在 `java_example_1556593492110` 表中添加一列 `added` ，并赋予默认值。
```
AlterTableOptions ato = new AlterTableOptions();
ato.addColumn("added", org.apache.kudu.Type.DOUBLE, DEFAULT_DOUBLE);
client.alterTable("java_example_1556593492110", ato);
System.out.println("Altered the table");
```

**如果是在 impala-shell 需要重新映射外部Kudu表**
```sql
[cdh3:21000] > ALTER TABLE java_example_1556593492110
             > SET TBLPROPERTIES('kudu.table_name' = 'java_example_1556593492110');
Query: ALTER TABLE java_example_1556593492110
SET TBLPROPERTIES('kudu.table_name' = 'java_example_1556593492110')
+----------------+
| summary        |
+----------------+
| Updated table. |
+----------------+
Fetched 1 row(s) in 1.80s
```

### 扫描数据
运行 `Example.scanTableAndCheckResults(client,"java_example_1556593492110", 33);`


