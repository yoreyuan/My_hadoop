Impala
------

# 目录
* [1 介绍](#1介绍)
    - [1.1 表](#11表)
    - [1.2 参数设定](#12参数设定)
    - [1.3 关于主键](#13关于主键)
# [2 impala-shell](#2impala-shell)

<br/></br>
----


# 1 介绍
## 1.1 表
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
    
## 1.2 参数设定
```sql
--设置主键
PRIMARY KEY(f1,f2)

--设置分区数
PARTITION BY HASH(f1) PARTITIONS 16

--设置副本数
TBLPROPERTIES('kudu.num_tablet_replicas' = 'n')

STORMD AS KUDU
```

## 1.3 关于主键
Impala表中没有主键的严格限制，但是在Kudu表中必须指定主键，如果数据文件中没有主键，可以对元数据添加一列行号作为主键
```bash
 awk '{print FNR","$0}'  table_source_data.csv >table_result_data.csv
```


# 2 impala-shell
在impala所在的服务器输入`impala-shell`可进入impala

## 2.1 Kudu 数据导出
```bash
 impala-shell -q "select * from kudu_test.b060_prplclaim" -B --output_delimiter="," -o /home/kudu_test.b060_prplclaim.csv
```
* **-q** 查询语句
* **-B** 格式化输出
* **--output_delimiter** 输入分隔符
* **-o** 输出的文件 

## 2.2 创建Impala表
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

## 2.3 创建kudu表
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

## 2.4 导入数据到Kudu
```sql
insert into kudu_test.kudu_type_test 
select * from kudu_test.type_test;

--插入单条数据
INSERT INTO 表名 VALUES(99, "sarah");
insert into b060_prplclaim values("3", "2019-06-21 18:00:00", "b", 0.33, "2019-06-30 18:00:00");
```








