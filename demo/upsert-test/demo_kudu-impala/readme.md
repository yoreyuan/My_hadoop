
# 1 环境
节点 | 服务
---- | :----
cdh1    | Impala Daemon、Tablet Server
cdh2    | Impala Daemon、Impala StateStore、Impala Catalog Server、Tablet Server
cdh3    | Impala Daemon、 Master、Tablet Server

# 2 Database
```bash
# 登陆 impala-shell
```

```sql
-- 创建测试库
[cdh3.ygbx.com:21000] default> CREATE DATABASE IF NOT EXISTS upsert_test COMMENT 'UPSERT TEST' ;
Query: CREATE DATABASE IF NOT EXISTS upsert_test COMMENT 'UPSERT TEST'
+----------------------------+
| summary                    |
+----------------------------+
| Database has been created. |
+----------------------------+
Fetched 1 row(s) in 0.15s

-- 查看库
[cdh1:21000] default> SHOW DATABASES;
Query: SHOW DATABASES
+------------------+----------------------------------------------+
| name             | comment                                      |
+------------------+----------------------------------------------+
| _impala_builtins | System database for Impala builtin functions |
| default          | Default Hive database                        |
| ……               | ……                                           |
| upsert_test      | UPSERT TEST                                  |
+------------------+----------------------------------------------+
Fetched 10 row(s) in 0.01s

```

# 3 Max Cloumn
[ImpalaJdbcClient.supportColumnMax()](src/main/java/yore/ImpalaJdbcClient.java)

```sql
-- 创建Kudu表
CREATE TABLE target(
id BIGINT,
name STRING,
indicator1 DOUBLE,
PRIMARY KEY (id)
) PARTITION BY HASH (id) PARTITIONS 3
STORED AS KUDU;

```

运行测试代码可以会报如下错误，因为建表时已经有3个字段，从打印的信息可以看到，当添加第`298`字段时报了如下错误，也就是在添加第`301`个字段时报了异常。结论：最大只能创建 `300`个字段。
大概运行 128009ms 。
```
 添加第 298 字段时报错！！
java.sql.SQLException: [Simba][ImpalaJDBCDriver](500051) ERROR processing query/statement. Error Code: 0, SQL state: TStatus(statusCode:ERROR_STATUS, sqlState:HY000, errorMessage:ImpalaRuntimeException: Error adding columns to Kudu table target
CAUSED BY: NonRecoverableException: number of columns 301 is greater than the permitted maximum 300
), Query: ALTER TABLE upsert_test.target ADD COLUMNS(indicator299 DOUBLE);.
	at com.cloudera.hivecommon.api.HS2Client.executeStatementInternal(Unknown Source)
	at com.cloudera.hivecommon.api.HS2Client.executeStatement(Unknown Source)
	at com.cloudera.hivecommon.dataengine.HiveJDBCNativeQueryExecutor.execute(Unknown Source)
	at com.cloudera.jdbc.common.SPreparedStatement.executeWithParams(Unknown Source)
	at com.cloudera.jdbc.common.SPreparedStatement.executeUpdate(Unknown Source)
	at yore.ImpalaJdbcClient.supportColumnMax(ImpalaJdbcClient.java:74)
Caused by: com.cloudera.support.exceptions.GeneralException: [Simba][ImpalaJDBCDriver](500051) ERROR processing query/statement. Error Code: 0, SQL state: TStatus(statusCode:ERROR_STATUS, sqlState:HY000, errorMessage:ImpalaRuntimeException: Error adding columns to Kudu table target
CAUSED BY: NonRecoverableException: number of columns 301 is greater than the permitted maximum 300
), Query: ALTER TABLE upsert_test.target ADD COLUMNS(indicator299 DOUBLE);.
	... 6 more

```

将创建的数据`row10000_col300_column.csv`合导入到表，使用代码创建一个Impala表，
```sql
-- 1 创建 Impala 临时表： target_tmp。 

-- 2 上传数据到 target 表 HDFS 路径下
hadoop fs -put row10000_col300_column.csv /user/hive/warehouse/upsert_test.db/target_tmp

-- 3 登入 impala-shell

-- 4 更新元数据
INVALIDATE METADATA;

-- 5 count
[cdh3.ygbx.com:21000] upsert_test> SELECT COUNT(*) FROM target_m_tmp ;
Query: SELECT COUNT(*) FROM target_m_tmp
Query submitted at: 2019-11-29 02:09:58 (Coordinator: http://cdh3.ygbx.com:25000)
Query progress can be monitored at: http://cdh3.ygbx.com:25000/query_plan?query_id=e246e73a4b8488d4:72d0168200000000
+----------+
| count(*) |
+----------+
| 10000    |
+----------+
Fetched 1 row(s) in 5.24s

-- 7 创建Kudu表 并将 target_tmp 表数据导入到建的 Kudu 表中
[cdh3.ygbx.com:21000] upsert_test> CREATE TABLE target
                                 > PRIMARY KEY (id)
                                 > PARTITION BY HASH (id) PARTITIONS 3
                                 > STORED AS KUDU
                                 > AS SELECT * FROM target_tmp;
Query: CREATE TABLE target
PRIMARY KEY (id)
PARTITION BY HASH (id) PARTITIONS 3
STORED AS KUDU
AS SELECT * FROM target_tmp
+-----------------------+
| summary               |
+-----------------------+
| Inserted 10000 row(s) |
+-----------------------+
Fetched 1 row(s) in 4.45s

-- 8 SELECT 。下面显示的时间都是首次查询的
[cdh3.ygbx.com:21000] upsert_test> SELECT COUNT(*) FROM target;
Query: SELECT COUNT(*) FROM target
Query submitted at: 2019-11-29 02:15:24 (Coordinator: http://cdh3.ygbx.com:25000)
Query progress can be monitored at: http://cdh3.ygbx.com:25000/query_plan?query_id=304cec10709e7ad7:502a8d7c00000000
+----------+
| count(*) |
+----------+
| 10000    |
+----------+
Fetched 1 row(s) in 3.53s

[cdh3.ygbx.com:21000] upsert_test>  SELECT id,name,indicator1,indicator2,indicator298 FROM target LIMIT 3;
Query: SELECT id,name,indicator1,indicator2,indicator298 FROM target LIMIT 3
Query submitted at: 2019-11-27 12:17:38 (Coordinator: http://cdh3.ygbx.com:25000)
Query progress can be monitored at: http://cdh3.ygbx.com:25000/query_plan?query_id=e54c77dbd4590cf9:e236e67100000000
+--------------------+--------+-------------------+------------+--------------+
| id                 | name   | indicator1        | indicator2 | indicator298 |
+--------------------+--------+-------------------+------------+--------------+
| 120336229286809600 | 褚坚和 | 90.84999999999999 | 91.63      | 31.85        |
| 120336229597188096 | 昌会思 | 75.70999999999999 | 43.75      | 24.19        |
| 120336229613965312 | 贝玉   | 22.48             | 75.14      | 82.64        |
+--------------------+--------+-------------------+------------+--------------+
Fetched 3 row(s) in 0.12s

[cdh3.ygbx.com:21000] upsert_test> SELECT SUM(indicator1) AS sum_indi1,SUM(indicator298) AS sum_indi298 FROM target ;
Query: SELECT SUM(indicator1) AS sum_indi1,SUM(indicator298) AS sum_indi298 FROM target
Query submitted at: 2019-11-27 12:20:09 (Coordinator: http://cdh3.ygbx.com:25000)
Query progress can be monitored at: http://cdh3.ygbx.com:25000/query_plan?query_id=664138bd88c2d8ba:e99e2cef00000000
+-------------------+-------------------+
| sum_indi1         | sum_indi298       |
+-------------------+-------------------+
| 503471.6699999995 | 496554.7099999997 |
+-------------------+-------------------+
Fetched 1 row(s) in 0.32s

```

# 4 更新
```sql
-- 1 新建表
drop table if exists target;
--  1.1 创建 Impala 临时表。Impala表不用指定主键
CREATE TABLE target_tmp(
id BIGINT,
name STRING,
indicator1 DOUBLE
)ROW FORMAT DELIMITED FIELDS TERMINATED BY ',' 
STORED AS TEXTFILE;

--  1.2 查看建表语句。因为建表时没有指定数据文件的位置，Impala使用默认的位置，所以我们需要将数据文件上传到这个 LOACATION 指定的目录下。
[cdh3.ygbx.com:21000] upsert_test> show create table target_tmp;
Query: show create table target_tmp
+------------------------------------------------------------------------------------+
| result                                                                             |
+------------------------------------------------------------------------------------+
| CREATE TABLE upsert_test.target_tmp (                                              |
|   id BIGINT,                                                                       |
|   name STRING,                                                                     |
|   indicator1 DOUBLE                                                                |
| )                                                                                  |
| ROW FORMAT DELIMITED FIELDS TERMINATED BY ','                                      |
| WITH SERDEPROPERTIES ('field.delim'=',', 'serialization.format'=',')               |
| STORED AS TEXTFILE                                                                 |
| LOCATION 'hdfs://cdh1.ygbx.com:8020/user/hive/warehouse/upsert_test.db/target_tmp' |
|                                                                                    |
+------------------------------------------------------------------------------------+
Fetched 1 row(s) in 0.01s

-- 1.3 创建第1部分的指标表
CREATE TABLE target_p1_tmp(
id BIGINT,
indicator2 DOUBLE
)ROW FORMAT DELIMITED FIELDS TERMINATED BY ',' 
STORED AS TEXTFILE;
-- 1.4 创建第2部分的指标表
CREATE TABLE target_p2_tmp(
id BIGINT,
indicator3 DOUBLE
)ROW FORMAT DELIMITED FIELDS TERMINATED BY ',' 
STORED AS TEXTFILE;

-- ALTER TABLE target_p2_tmp CHANGE indicator2 indicator3 DOUBLE;

```

```bash
# 2 上传数据到 HDFS 上
hadoop fs -put target_60000000.csv /user/hive/warehouse/upsert_test.db/target_tmp
hadoop fs -put target_part_30000000.csv /user/hive/warehouse/upsert_test.db/target_p1_tmp
hadoop fs -put target_part_6000000.csv /user/hive/warehouse/upsert_test.db/target_p2_tmp
```

```sql
-- 3 刷新元数据信息
INVALIDATE METADATA;

-- 4 创建Kudu表，并将数据加载到
--  4.1 target
CREATE TABLE target 
PRIMARY KEY (id)
PARTITION BY HASH (id) PARTITIONS 3
STORED AS KUDU
AS SELECT * FROM target_tmp;
--  4.2 target
CREATE TABLE target_p1 
PRIMARY KEY (id)
PARTITION BY HASH (id) PARTITIONS 3
STORED AS KUDU
AS SELECT * FROM target_p1_tmp;
--  4.3 target
CREATE TABLE target_p2 
PRIMARY KEY (id)
PARTITION BY HASH (id) PARTITIONS 3
STORED AS KUDU
AS SELECT * FROM target_p2_tmp;

-- 5 target 添加两个新的指标列
ALTER TABLE upsert_test.target ADD COLUMNS(indicator2 DOUBLE);
ALTER TABLE upsert_test.target ADD COLUMNS(indicator3 DOUBLE);

```

## 将 600W 的指标数据更新到 指标表中
根据主键进行更新插入
```sql
-- 1 查看指标表（6kw）
[cdh3.ygbx.com:21000] upsert_test> SELECT * FROM target ORDER BY id DESC LIMIT 10;
Query: SELECT * FROM target ORDER BY id DESC LIMIT 10
Query submitted at: 2019-11-28 09:25:36 (Coordinator: http://cdh3.ygbx.com:25000)
Query progress can be monitored at: http://cdh3.ygbx.com:25000/query_plan?query_id=674fb528ec575c87:d2bd30600000000
+--------------------+--------+-------------------+------------+------------+
| id                 | name   | indicator1        | indicator2 | indicator3 |
+--------------------+--------+-------------------+------------+------------+
| 119620533737230409 | 袁友裕 | 94.94             | NULL       | NULL       |
| 119620533737230408 | 常士以 | 85.81999999999999 | NULL       | NULL       |
| 119620533737230407 | 许启   | 55.85             | NULL       | NULL       |
| 119620533737230406 | 岑德   | 61.67             | NULL       | NULL       |
| 119620533737230405 | 孟茂进 | 8.91              | NULL       | NULL       |
| 119620533737230404 | 祁彬   | 55.63             | NULL       | NULL       |
| 119620533737230403 | 彭邦   | 55.94             | NULL       | NULL       |
| 119620533737230402 | 皮嘉   | 27.14             | NULL       | NULL       |
| 119620533737230401 | 杜妹   | 88.14             | NULL       | NULL       |
| 119620533737230400 | 常美娜 | 81.98999999999999 | NULL       | NULL       |
+--------------------+--------+-------------------+------------+------------+
Fetched 10 row(s) in 13.58s

-- 2 使用 UPDATE 根据主键更新单条数据数据。(6kw)
[cdh3.ygbx.com:21000] upsert_test> UPDATE target SET indicator1=3.25 WHERE id=119620533737230408;
Query: UPDATE target SET indicator1=3.25 WHERE id=119620533737230408
Query submitted at: 2019-11-28 09:30:55 (Coordinator: http://cdh3.ygbx.com:25000)
Query progress can be monitored at: http://cdh3.ygbx.com:25000/query_plan?query_id=f647f3b7bd278c0e:84e42ce00000000
Modified 1 row(s), 0 row error(s) in 0.12s
-- 2.1 查看更新的结果
[cdh3.ygbx.com:21000] upsert_test> SELECT * FROM target WHERE id=119620533737230408;
Query: SELECT * FROM target WHERE id=119620533737230408
Query submitted at: 2019-11-28 09:30:58 (Coordinator: http://cdh3.ygbx.com:25000)
Query progress can be monitored at: http://cdh3.ygbx.com:25000/query_plan?query_id=12417fa85a94eab6:64692f9700000000
+--------------------+--------+------------+------------+------------+
| id                 | name   | indicator1 | indicator2 | indicator3 |
+--------------------+--------+------------+------------+------------+
| 119620533737230408 | 常士以 | 3.25       | NULL       | NULL       |
+--------------------+--------+------------+------------+------------+
Fetched 1 row(s) in 0.11s
-- 2.2 使用 UPDATE 更新数据 (6kw join 600w)
[cdh3.ygbx.com:21000] upsert_test> UPDATE t0 SET t0.indicator3=t2.indicator3
                                 > FROM target t0 LEFT JOIN target_p2 t2
                                 > ON t0.id=t2.id;
Query: UPDATE t0 SET t0.indicator3=t2.indicator3
FROM target t0 LEFT JOIN target_p2 t2
ON t0.id=t2.id
Query submitted at: 2019-11-26 19:14:55 (Coordinator: http://cdh3.ygbx.com:25000)
Query progress can be monitored at: http://cdh3.ygbx.com:25000/query_plan?query_id=c74701e6305f3dd4:a95c634400000000
ERROR: Kudu error(s) reported, first error: Timed out: Failed to write batch of 551882 ops to tablet be27ee716f044106b23b9da1508d5e68 after 1 attempt(s): Failed to write to server: 3628f47403ba4d8ba00df8d70499db01 (cdh2.ygbx.com:7050): Write RPC to 192.168.100.166:7050 timed out after 179.996s (SENT)
Error in Kudu table 'impala::upsert_test.target': Timed out: Failed to write batch of 551882 ops to tablet 9561854fb6ee419aabd712d392039324 after 1 attempt(s): Failed to write to server: 697b3569654b4e86ae94397f1604bf1d (cdh3.ygbx.com:7050): Write RPC to 192.168.100.165:7050 timed out after 179.996s (SENT) (1 of 1043702 similar)
--  2.3 恢复数据为null
UPDATE target SET indicator3=NULL;


-- 3 使用 UPSERT 更新数据 (3kw)
--  3.1 查看 target 表指标合计
[cdh3.ygbx.com:21000] upsert_test> SELECT SUM(indicator2) FROM target;
Query: SELECT SUM(indicator2) FROM target
Query submitted at: 2019-11-29 10:07:46 (Coordinator: http://cdh3.ygbx.com:25000)
Query progress can be monitored at: http://cdh3.ygbx.com:25000/query_plan?query_id=1e459b75b71d8130:35af938600000000
+-----------------+
| sum(indicator2) |
+-----------------+
| NULL            |
+-----------------+
Fetched 1 row(s) in 1.13s
--  3.2 查看 target_p1 表指标合计
[cdh3.ygbx.com:21000] upsert_test> SELECT SUM(indicator2) FROM target_p1;
Query: SELECT SUM(indicator2) FROM target_p1
Query submitted at: 2019-11-29 10:08:23 (Coordinator: http://cdh3.ygbx.com:25000)
Query progress can be monitored at: http://cdh3.ygbx.com:25000/query_plan?query_id=5240e394398b5e04:9655b04e00000000
+-------------------+
| sum(indicator2)   |
+-------------------+
| 1499969540.469904 |
+-------------------+
Fetched 1 row(s) in 1.93s
--  3.3 UPSERT 方式将 target_p1 指标更新到 target 表
[cdh3.ygbx.com:21000] upsert_test> UPSERT INTO target(id,indicator2) SELECT id,indicator2 FROM target_p1;
Query: UPSERT INTO target(id,indicator2) SELECT id,indicator2 FROM target_p1
Query submitted at: 2019-11-28 15:11:24 (Coordinator: http://cdh3.ygbx.com:25000)
Query progress can be monitored at: http://cdh3.ygbx.com:25000/query_plan?query_id=384ab4c50d20ba1f:cec2c79d00000000
ERROR: Kudu error(s) reported, first error: Timed out: Failed to write batch of 551882 ops to tablet be27ee716f044106b23b9da1508d5e68 after 1 attempt(s): Failed to write to server: 3628f47403ba4d8ba00df8d70499db01 (cdh2.ygbx.com:7050): Write RPC to 192.168.100.166:7050 timed out after 179.996s (SENT)
Error in Kudu table 'impala::upsert_test.target': Timed out: Failed to write batch of 551882 ops to tablet 0b15dde7ceb545da81904006bdad914d after 1 attempt(s): Failed to write to server: 3628f47403ba4d8ba00df8d70499db01 (cdh2.ygbx.com:7050): Write RPC to 192.168.100.166:7050 timed out after 179.996s (SENT) (1 of 1135678 similar)
--  由于环境问题，本次限制下数据数
UPSERT INTO target(id,indicator2) SELECT id,indicator2 FROM target_p1 LIMIT 1;


SELECT SUM(indicator2) FROM target;




UPDATE t1 SET indicator2=indicator3 
FROM target_p1 t1 LEFT JOIN target_p2 t2 
ON t1.id=t2.id 
WHERE t1.id=119615892920209409 AND t2.id=119615892920209409;



UPSERT INTO target_p1


UPDATE target SET indicator3=NULL  ;

SELECT sum(t0.indicator1),sum(t1.indicator2) FROM target t0
LEFT JOIN target_p1 t1
ON t0.id=t1.id;

SELECT COUNT(t1.indicator2),SUM(t2.indicator3) FROM (SELECT id,indicator2 FROM target_p1 LIMIT 1000000) t1
LEFT JOIN (SELECT id,indicator3 FROM target_p2 LIMIT 1000000) t2
ON t1.id=t2.id;



```





