[Apache Impala](https://impala.apache.org/)
------

# 目录
* [1 介绍](#1介绍)
* [2 架构](#2架构)
* [3 使用](#3使用)
    - [3.1 表](#31表)
    - [3.2 参数设定](#32参数设定)
    - [3.3 关于主键](#33关于主键
    - [3.4 impala-shell](#34impala-shell)
    - [3.5 Kudu 数据导出](#35Kudu数据导出)
    - [3.6 创建Impala表](#36创建Impala表)
    - [3.7 创建Kudu表](#37创建Kudu表)
    - [3.8 导入数据到Kudu](#3导入数据到Kudu)
* [4 实例](#4实例)
    - [4.1 Impala shell](#41Impalashell)
    - [4.2 改和查](#42改和查)
    - [4.3 Hue](#43Hue)
    - [4.4 Kudu+Impala](#44Kudu+Impala)

<br/></br>
----


# 1 介绍
Apache Impala （黑斑羚） 是 Apache Hadoop 的开源本机分析数据库（native analytic database）。Impala提高了Apache Hadoop上SQL查询性能的标准，
同时保留了熟悉的用户体验。使用Impala，可以实时查询存储在HDFS或Apache HBase中的数据-包括SELECT、JOIN和聚合函数。
此外，Impala使用与Apache Hive相同的元数据，SQL语法（Hive SQL），ODBC驱动程序和用户界面（Hue Beeswax），
为面向批处理或实时查询提供了一个熟悉且统一的平台。（因此，Hive用户可以以很少的设置开销使用Impala。）


* **在Hadoop上执行BI风格的查询**：Impala 为 Hadoop 上的BI/分析查询提供了低延迟和高并发性（不是由批处理框架（如Apache Hive）提供的）。即使在多租户环境中，Impala也会线性缩减。
* **统一您的基础架构**：使用与Hadoop相同的部署文件和数据格式、元数据、安全性和资源管理框架，实现了没有冗余的基础设施或数据的转换/复制。
* **快速实施**：对于Apache Hive用户，Impala使用相同的元数据和ODBC驱动程序。与Hive一样，Impala也支持SQL，因此您不必担心重新造轮子。
* **企业级安全性保障**：Impala与本地Hadoop安全性和Kerberos集成在一起以进行身份​​验证，并且通过Sentry模块，您可以确保为正确的数据授权了正确的用户和应用程序。
* **保持不受锁定的约束**：Impala是开源的（Apache许可证）。
* **扩展Hadoop用户体验**：借助Impala，无论是使用SQL查询还是BI应用程序，更多的用户都可以通过单个存储库和元数据存储与从源头到分析的更多数据进行交互。

# 2 架构
为了避免延迟，Impala绕开了MapReduce通过与商业并行RDBMS中非常相似的专用分布式查询引擎去直接访问数据。其结果是，性能比Hive快了几个数量级，具体取决于查询和配置的类型。

![Architecture](https://impala.apache.org/img/impala.png)

与查询Hadoop数据的替代方法相比，这种方法有很多优点：
* 由于对数据节点进行了本地处理，因此避免了网络瓶颈；
* 可以利用单个、开放和统一的元数据存储；
* 无需进行昂贵的数据格式转换，因此不会产生任何开销；所有数据均可立即查询，对于ETL没有延迟；
* 所有硬件均用于Impala查询以及MapReduce；
* 仅需单个计算机池（machine pool）即可扩展。

想要对Impala有更深入的了解可以阅读[Impala: A Modern, Open-Source SQL Engine for Hadoop](http://cidrdb.org/cidr2015/Papers/CIDR15_Paper28.pdf)论文,
也可以看查我的blog中对篇论文的译文 [Impala：适用于Hadoop的现代开源SQL引擎](https://blog.csdn.net/github_39577257/article/details/93366028)。


# 3 使用

## 3.1 表
Impala中的表分为：
* external tables:  外部表
* internal table:   内部表

使用以下ALTER TABLE语句将表从内部切换到外部，或从外部切换到内部：
```sql
-- Switch a table from internal to external.
ALTER TABLE table_name SET TBLPROPERTIES('EXTERNAL'='TRUE');

-- Switch a table from external to internal.
ALTER TABLE table_name SET TBLPROPERTIES('EXTERNAL'='FALSE');
```

查看某表的类型信息
```sql
DESCRIBE FORMATTED 表名; 
```
* Table Type
    * MANAGED_TABLE 内部表
    * EXTERNAL_TABLE 外部表
    
## 3.2 参数设定
```sql
--设置主键
PRIMARY KEY(f1,f2)

--设置分区数
PARTITION BY HASH(f1) PARTITIONS 16

--设置副本数
TBLPROPERTIES('kudu.num_tablet_replicas' = 'n')

STORMD AS KUDU
```

## 3.3 关于主键
Impala表中没有主键的严格限制，但是在Kudu表中必须指定主键，如果数据文件中没有主键，可以对元数据添加一列行号作为主键
```bash
 awk '{print FNR","$0}'  table_source_data.csv >table_result_data.csv
```


## 3.4 impala-shell
在impala所在的服务器输入`impala-shell`可进入impala

## 3.5 Kudu 数据导出
```bash
 impala-shell -q "select * from kudu_test.b060_prplclaim" -B --output_delimiter="," -o /home/kudu_test.b060_prplclaim.csv
```
* **-q** 查询语句
* **-B** 格式化输出
* **--output_delimiter** 输入分隔符
* **-o** 输出的文件 

## 3.6 创建Impala表
hdfs上home下已经上传了数据，然后在 impala-shell 中创建一个impala表。

```sql
create table type_test(
    eid bigint,
    name VARCHAR(32),
    salary float,
    resp string,
    num double,
    come timestamp
) 
ROW FORMAT DELIMITED FIELDS TERMINATED BY ',' 
location '/home/sample.txt';
```

## 3.7 创建kudu表
```sql
create table kudu_type_test(
    eid bigint not null,
    name string,
    salary float,
    resp string,
    num double,
    come timestamp,
    primary key(eid)
) 
PARTITION BY HASH (eid) PARTITIONS 2
STORED AS KUDU;

```

## 3.8 导入数据到Kudu
```sql
insert into kudu_test.kudu_type_test 
select * from kudu_test.type_test;

--插入单条数据
INSERT INTO 表名 VALUES(99, "sarah");
insert into b060_prplclaim values("3", "2019-06-21 18:00:00", "b", 0.33, "2019-06-30 18:00:00");
```

# 4 实例
## 4.1 Impala shell
```
# 在任意一个Impala daemon 节点上执行如下命令，进入impals 命令行
[root@cdh2 ~]# impala-shell
Starting Impala Shell without Kerberos authentication
Opened TCP connection to cdh2.ygbx.com:21000
Connected to cdh2.ygbx.com:21000
Server version: impalad version 3.2.0-cdh6.2.0 RELEASE (build edc19942b4debdbfd485fbd26098eef435003f5d)
***********************************************************************************
Welcome to the Impala shell.
(Impala Shell v3.2.0-cdh6.2.0 (edc1994) built on Thu Mar 14 00:14:35 PDT 2019)
When you set a query option it lasts for the duration of the Impala shell session.
***********************************************************************************
[cdh2:21000] default>

```

```sql
-- 1 库
--  1.1 建库
[cdh2:21000] default> CREATE DATABASE IF NOT EXISTS impala_demo COMMENT '用于演示impala使用的数据库';
+----------------------------+
| summary                    |
+----------------------------+
| Database has been created. |
+----------------------------+
Fetched 1 row(s) in 0.32s

--  1.2 库列表
-- 如果注释中文乱码，请修改Hive元数据库的DBS表的DESC列的编码格式
--  alter table DBS modify column `DESC` varchar(4000) character set utf8;
[cdh2:21000] default> SHOW DATABASES;
+------------------+----------------------------------------------+
| name             | comment                                      |
+------------------+----------------------------------------------+
| _impala_builtins | System database for Impala builtin functions |
| default          | Default Hive database                        |
| impala_demo      | 用于演示impala使用的数据库                   |
+------------------+----------------------------------------------+
Fetched 3 row(s) in 0.01s

--  1.3 查看impala_demo库信息
[cdh2:21000] default> DESCRIBE DATABASE impala_demo;
Query: describe DATABASE impala_demo
+-------------+--------------------------------------------------------------+----------------------------+
| name        | location                                                     | comment                    |
+-------------+--------------------------------------------------------------+----------------------------+
| impala_demo | hdfs://cdh1:8020/user/hive/warehouse/impala_demo.db | 用于演示impala使用的数据库 |
+-------------+--------------------------------------------------------------+----------------------------+
Fetched 1 row(s) in 0.63s

--  1.4 使用 impala_demo 库
[cdh2:21000] default> USE impala_demo;
Query: USE impala_demo
Fetched 0 row(s) in 0.33s


-- 2 表
--  2.1 创建一个电影表。这里是一个外表，删除表后数据不会被删除，依然在HDFS上
[cdh2:21000] impala_demo> CREATE EXTERNAL TABLE movie(
                        >  id BIGINT COMMENT '电影标识',
                        >  movie_name VARCHAR(255) COMMENT '电影名',
                        >  director VARCHAR(128) COMMENT '导演',
                        >  scriptwriter VARCHAR(128) COMMENT '编剧',
                        >  protagonist STRING COMMENT '主演',
                        >  kind VARCHAR(64) COMMENT '类型',
                        >  country VARCHAR(32) COMMENT '地区',
                        >  language VARCHAR(32) COMMENT '语言',
                        >  release_date VARCHAR(64) COMMENT '上映日期',
                        >  mins VARCHAR(16) COMMENT '片长',
                        >  alternate_name VARCHAR(128) COMMENT '又名',
                        >  imdb VARCHAR(128) COMMENT 'IMDb链接',
                        >  rating_num FLOAT  COMMENT '评分',
                        >  rating_people BIGINT COMMENT '评分人数',
                        >  url VARCHAR(255) COMMENT 'url'
                        > )
                        > COMMENT '电影表'
                        > ROW FORMAT DELIMITED FIELDS TERMINATED BY ','
                        > STORED AS textfile;
+-------------------------+
| summary                 |
+-------------------------+
| Table has been created. |
+-------------------------+
Fetched 1 row(s) in 1.47s

--  2.2 创建一个语录排名表
[cdh2:21000] impala_demo> CREATE external TABLE quote(
                        >  id BIGINT COMMENT '电影标识',
                        >  rank BIGINT COMMENT '排名',
                        >  quote STRING COMMENT '语录'
                        > )COMMENT '语录排名表'
                        > ROW FORMAT DELIMITED FIELDS TERMINATED BY ','
                        > STORED AS textfile ;
+-------------------------+
| summary                 |
+-------------------------+
| Table has been created. |
+-------------------------+
Fetched 1 row(s) in 0.06s

--  2.3 查看当前库下所有表的列表
[cdh2:21000] impala_demo> SHOW TABLES;
+-------+
| name  |
+-------+
| movie |
| quote |
+-------+
Fetched 2 row(s) in 0.01s

--  2.4 查看movie表详细信息
[cdh2:21000] impala_demo> DESC movie;
+----------------+--------------+----------+
| name           | type         | comment  |
+----------------+--------------+----------+
| id             | bigint       | 电影标识 |
| movie_name     | varchar(255) | 电影名   |
| director       | varchar(128) | 导演     |
| scriptwriter   | varchar(128) | 编剧     |
| protagonist    | string       | 主演     |
| kind           | varchar(64)  | 类型     |
| country        | varchar(32)  | 地区     |
| language       | varchar(32)  | 语言     |
| release_date   | varchar(64)  | 上映日期 |
| mins           | varchar(16)  | 片长     |
| alternate_name | varchar(128) | 又名     |
| imdb           | varchar(128) | IMDb链接 |
| rating_num     | float        | 评分     |
| rating_people  | bigint       | 评分人数 |
| url            | varchar(255) | url      |
+----------------+--------------+----------+
Fetched 15 row(s) in 0.01s

--  2.5 查看quote表的建表信息
[cdh2:21000] impala_demo> SHOW CREATE TABLE quote;
+-------------------------------------------------------------------------------+
| result                                                                        |
+-------------------------------------------------------------------------------+
| CREATE EXTERNAL TABLE impala_demo.quote (                                     |
|   id BIGINT COMMENT '电影标识',                                               |
|   rank BIGINT COMMENT '排名',                                                 |
|   quote STRING COMMENT '语录'                                                 |
| )                                                                             |
|  COMMENT '语录排名表'                                                         |
| ROW FORMAT DELIMITED FIELDS TERMINATED BY ','                                 |
| WITH SERDEPROPERTIES ('field.delim'=',', 'serialization.format'=',')          |
| STORED AS TEXTFILE                                                            |
| LOCATION 'hdfs://cdh1:8020/user/hive/warehouse/impala_demo.db/quote' |
| TBLPROPERTIES ('numFiles'='0', 'numFilesErasureCoded'='0', 'totalSize'='0')   |
+-------------------------------------------------------------------------------+
Fetched 1 row(s) in 0.01s


-- 3 加载数据
--  3.1 从HDFS上加载数据到movie表。只能是HDFS，目前不支持从本地加载数据
--   前提先将 doubanMovie.csv 数据上传到 HDFS上的 /home下
[cdh2:21000] impala_demo> LOAD DATA INPATH '/home/doubanMovie.csv' INTO TABLE movie;
+----------------------------------------------------------+
| summary                                                  |
+----------------------------------------------------------+
| Loaded 1 file(s). Total files in destination location: 1 |
+----------------------------------------------------------+
Fetched 1 row(s) in 0.69s

--  3.2 从其它表中将数据加载到 quote 表。
--   前提，在default库下存在quote表，且表中已经有数据。
--   这里可以使用Hive中创建的表，因为Impala和Hive元数据共享，Impala可以查看到Hive的表和数据
[cdh2:21000] impala_demo>  INSERT INTO TABLE quote
                        >  SELECT * FROM default.quote;
Modified 250 row(s) in 1.33s


-- 4 分析
--  4.1 查看执行计划
[cdh2:21000] impala_demo> EXPLAIN SELECT m.id,m.movie_name,m.rating_num,m.rating_people,q.rank,q.quote FROM movie m
                        > LEFT JOIN quote q ON q.id=m.id
                        > ORDER BY m.rating_num desc,m.rating_people DESC LIMIT 10;
+------------------------------------------------------------------------------------+
| Explain String                                                                     |
+------------------------------------------------------------------------------------+
| Max Per-Host Resource Reservation: Memory=34.27MB Threads=5                        |
| Per-Host Resource Estimates: Memory=2.06GB                                         |
| WARNING: The following tables are missing relevant table and/or column statistics. |
| impala_demo.movie, impala_demo.quote                                               |
|                                                                                    |
| PLAN-ROOT SINK                                                                     |
| |                                                                                  |
| 05:MERGING-EXCHANGE [UNPARTITIONED]                                                |
| |  order by: rating_num DESC, rating_people DESC                                   |
| |  limit: 10                                                                       |
| |                                                                                  |
| 03:TOP-N [LIMIT=10]                                                                |
| |  order by: rating_num DESC, rating_people DESC                                   |
| |  row-size=51B cardinality=10                                                     |
| |                                                                                  |
| 02:HASH JOIN [LEFT OUTER JOIN, BROADCAST]                                          |
| |  hash predicates: m.id = q.id                                                    |
| |  row-size=59B cardinality=unavailable                                            |
| |                                                                                  |
| |--04:EXCHANGE [BROADCAST]                                                         |
| |  |                                                                               |
| |  01:SCAN HDFS [impala_demo.quote q]                                              |
| |     partitions=1/1 files=1 size=13.87KB                                          |
| |     row-size=27B cardinality=unavailable                                         |
| |                                                                                  |
| 00:SCAN HDFS [impala_demo.movie m]                                                 |
|    partitions=1/1 files=1 size=162.44KB                                            |
|    row-size=32B cardinality=unavailable                                            |
+------------------------------------------------------------------------------------+
Fetched 28 row(s) in 3.83s

-- 4.2 执行SQL
[cdh2:21000] impala_demo> SELECT m.id,m.movie_name,m.rating_num,m.rating_people,q.rank,q.quote FROM movie m
                        > LEFT JOIN quote q ON q.id=m.id
                        > ORDER BY m.rating_num desc,m.rating_people DESC LIMIT 10;
+---------+----------------------------------------------+-------------------+---------------+------+--------------------------------+
| id      | movie_name                                   | rating_num        | rating_people | rank | quote                          |
+---------+----------------------------------------------+-------------------+---------------+------+--------------------------------+
| 1292052 | 肖申克的救赎 The Shawshank Redemption (1994) | 9.699999809265137 | 1502851       | 1    | 希望让人自由。                 |
| 1291546 | 霸王别姬 (1993)                              | 9.600000381469727 | 1112641       | 2    | 风华绝代。                     |
| 1296141 | 控方证人 Witness for the Prosecution (1957)  | 9.600000381469727 | 195362        | 29   | 比利·怀德满分作品。            |
| 1292063 | 美丽人生 La vita è bella (1997)              | 9.5               | 690618        | 5    | 最美的谎言。                   |
| 1295124 | 辛德勒的名单 Schindler‘s List (1993)         | 9.5               | 613865        | 8    | 拯救一个人，就是拯救整个世界。 |
| 1295644 | 这个杀手不太冷 Léon (1994)                   | 9.399999618530273 | 1363430       | 3    | 怪蜀黍和小萝莉不得不说的故事。 |
| 1292720 | 阿甘正传 Forrest Gump (1994)                 | 9.399999618530273 | 1178003       | 4    | 一部美国近现代史。             |
| 1292722 | 泰坦尼克号 Titanic (1997)                    | 9.399999618530273 | 1119405       | 7    | 失去的才是永恒的。             |
| 1293182 | 十二怒汉 12 Angry Men (1957)                 | 9.399999618530273 | 253408        | 36   | 1957年的理想主义。             |
| 1291561 | 千与千寻 千と千尋の神隠し (2001)             | 9.300000190734863 | 1205228       | 6    | 最好的宫崎骏，最好的久石让。   |
+---------+----------------------------------------------+-------------------+---------------+------+--------------------------------+
Fetched 10 row(s) in 1.18s


-- 5 删除
--  5.1 删除库
[cdh2:21000] default> DROP DATABASE IF EXISTS impala_demo;
+----------------------------+
| summary                    |
+----------------------------+
| Database has been dropped. |
+----------------------------+
Fetched 1 row(s) in 0.98s

--  5.2 删除表
[cdh2:21000] impala_demo> DROP TABLE movie;
+-------------------------+
| summary                 |
+-------------------------+
| Table has been dropped. |
+-------------------------+
Fetched 1 row(s) in 0.41s

```


## 4.2 改查
必须是Kudu表
```sql
-- 1 查看支持的表操作，连续两次Tab键
[cdh2:21000] impala_demo>
alter     connect   delete    describe  exit      help      insert    profile   rerun     set       show      src       tip       update    use       version
compute   create    desc      drop      explain   history   load      quit      select    shell     source    summary   unset     upsert    values    with

-- 2 创建一个临时测试表
[cdh2:21000] impala_demo> CREATE TABLE tmp_test(
                        > id INT,
                        > name STRING,
                        > PRIMARY KEY
                        > )ROW FORMAT DELIMITED FIELDS TERMINATED BY '|'
                        > STORED AS TEXTFILE;

-- 3 插入两条数据
[cdh2:21000] impala_demo> INSERT INTO TABLE tmp_test VALUES(2, "two");
[cdh2:21000] impala_demo> INSERT INTO TABLE tmp_test VALUES(3, "three");
[cdh2:21000] impala_demo> SELECT * FROM tmp_test;
+----+-------+
| id | name  |
+----+-------+
| 3  | three |
| 2  | two   |
+----+-------+
Fetched 2 row(s) in 1.70s

-- 4 更新数据。仅适用于Kudu存储引擎的Impala表
UPSERT INTO tmp_test(id, name) VALUES(2, "TWO");
UPSERT INTO tmp_test SET name="TWO" WHERE id=2

```

## 4.3 Hue
Hue配置页面搜 `hue_safety_valve.ini`
```yaml

[hbase]
hbase_conf_dir={{HBASE_CONF_DIR}}
thrift_transport=buffered


[[[mysql]]]
nice_name="MySQLDB"
name=MySql JDBC
engine=mysql
host=cdh1
port=3306
user=root
password=123456
##interface=jdbc
## Specific options for connecting to the server.
## The JDBC connectors, e.g. mysql.jar, need to be in the CLASSPATH environment variable.
## If 'user' and 'password' are omitted, they will be prompted in the UI.
## options='{"url": "jdbc:mysql://cdh1:3306/hue", "driver": "com.mysql.jdbc.Driver", "user": "root", "password": "123456"}'
## options='{"url": "jdbc:mysql://localhost:3306/hue", "driver": "com.mysql.jdbc.Driver"}'

[librdbms]
[[databases]]
[[[mysqljdbc]]]
   name=MySql JDBC
  interface=jdbc
   options='{"url": "jdbc:mysql://cdh1:3306/hue", "driver": "com.mysql.jdbc.Driver", "user": "root", "password": "123456"}'

```

## 4.4 Kudu + Impala
```sql
-- 1 创建一个新库
[cdh2:21000] impala_demo> CREATE DATABASE IF NOT EXISTS kudu_demo COMMENT '用于演示kudu使用的数据库';
+----------------------------+
| summary                    |
+----------------------------+
| Database has been created. |
+----------------------------+
Fetched 1 row(s) in 1.28s

-- 2 创建电影排名语录的Kudu表。通过前面创建的Impala quote表，并将Impala 中的quote 表数据加载到Kudu表中
--  指定主键为id(必须指定主键)。设置按照id进行Hash分区，分区为2（一般和Tablet Server数一致）
[cdh2:21000] kudu_demo> CREATE TABLE quote
                      > PRIMARY KEY (id)
                      > PARTITION BY HASH (id) PARTITIONS 2
                      > STORED AS KUDU
                      > AS SELECT * FROM impala_demo.quote;
+---------------------+
| summary             |
+---------------------+
| Inserted 250 row(s) |
+---------------------+
Fetched 1 row(s) in 2.25s


-- 3 创建电影的Kudu表。
--  因为在创建Impala表时，我们字符串使用了 VARCHAR 类型，这个类型直接转Kudu表会报错，不支持。
--  3.1 直接创建一个Kudu表
[cdh2:21000] kudu_demo> CREATE TABLE movie(
                      >  id BIGINT COMMENT '电影标识',
                      >  movie_name STRING COMMENT '电影名',
                      >  director STRING COMMENT '导演',
                      >  scriptwriter STRING COMMENT '编剧',
                      >  protagonist STRING COMMENT '主演',
                      >  kind STRING COMMENT '类型',
                      >  country STRING COMMENT '地区',
                      >  language STRING COMMENT '语言',
                      >  release_date STRING COMMENT '上映日期',
                      >  mins STRING COMMENT '片长',
                      >  alternate_name STRING COMMENT '又名',
                      >  imdb STRING COMMENT 'IMDb链接',
                      >  rating_num FLOAT  COMMENT '评分',
                      >  rating_people BIGINT COMMENT '评分人数',
                      >  url STRING COMMENT 'url',
                      >  PRIMARY KEY (id)
                      > )PARTITION BY HASH (id) PARTITIONS 2
                      > STORED AS KUDU;

+-------------------------+
| summary                 |
+-------------------------+
| Table has been created. |
+-------------------------+
Fetched 1 row(s) in 0.45s

-- 3.2 将 Impala 的 movie 数据插入到 Kudu 的 movie 表
[cdh2:21000] kudu_demo> INSERT INTO movie
                      > SELECT id,movie_name,director,scriptwriter,protagonist,kind,country,language,release_date,mins,
                      > alternate_name,imdb,rating_num,rating_people,url FROM impala_demo.movie ;
Modified 250 row(s), 0 row error(s) in 1.03s

-- 4 使用kudu表，执行SQL
[cdh2:21000] kudu_demo> SELECT m.id,m.movie_name,m.rating_num,m.rating_people,q.rank,q.quote FROM kudu_demo.movie m
                      > LEFT JOIN kudu_demo.quote q ON q.id=m.id
                      > ORDER BY m.rating_num desc,m.rating_people DESC LIMIT 10;
+---------+----------------------------------------------+-------------------+---------------+------+--------------------------------+
| id      | movie_name                                   | rating_num        | rating_people | rank | quote                          |
+---------+----------------------------------------------+-------------------+---------------+------+--------------------------------+
| 1292052 | 肖申克的救赎 The Shawshank Redemption (1994) | 9.699999809265137 | 1502851       | 1    | 希望让人自由。                 |
| 1291546 | 霸王别姬 (1993)                              | 9.600000381469727 | 1112641       | 2    | 风华绝代。                     |
| 1296141 | 控方证人 Witness for the Prosecution (1957)  | 9.600000381469727 | 195362        | 29   | 比利·怀德满分作品。            |
| 1292063 | 美丽人生 La vita è bella (1997)              | 9.5               | 690618        | 5    | 最美的谎言。                   |
| 1295124 | 辛德勒的名单 Schindler’s List (1993)         | 9.5               | 613865        | 8    | 拯救一个人，就是拯救整个世界。 |
| 1295644 | 这个杀手不太冷 Léon (1994)                   | 9.399999618530273 | 1363430       | 3    | 怪蜀黍和小萝莉不得不说的故事。 |
| 1292720 | 阿甘正传 Forrest Gump (1994)                 | 9.399999618530273 | 1178003       | 4    | 一部美国近现代史。             |
| 1292722 | 泰坦尼克号 Titanic (1997)                    | 9.399999618530273 | 1119405       | 7    | 失去的才是永恒的。             |
| 1293182 | 十二怒汉 12 Angry Men (1957)                 | 9.399999618530273 | 253408        | 36   | 1957年的理想主义。             |
| 1291561 | 千与千寻 千と千尋の神隠し (2001)             | 9.300000190734863 | 1205228       | 6    | 最好的宫崎骏，最好的久石让。   |
+---------+----------------------------------------------+-------------------+---------------+------+--------------------------------+
Fetched 10 row(s) in 0.43s


```
