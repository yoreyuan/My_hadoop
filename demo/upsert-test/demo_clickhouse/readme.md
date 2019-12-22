
# 1 ClickHouse环境
节点 | 服务
---- | :----
cdh1    | ClickHouse Server
cdh2    | ClickHouse Server
cdh3    | ClickHouse Server


# 2 Database
```bash
# 登陆 clickHouse client
clickhouse-client -h 127.0.0.1 --port 19000 -u default --password KavrqeN1 --multiline
```
```sql
-- 创建测试库
CREATE DATABASE IF NOT EXISTS upsert_test ;

-- 查看库
cdh1 :) SHOW DATABASES;
↙ Progress: 0.00 rows, 0.00 B (0.00 rows/s., 0.00 B/s.) 
┌─name────────┐
│ default     │
│ system      │
│ upsert_test │
└─────────────┘
← Progress: 0.00 rows, 0.00 B (0.00 rows/s., 0.00 B/s.) ↖ Progress: 3.00 rows, 372.00 B (1.50 thousand rows/s., 186.12 KB/s.)
3 rows in set. Elapsed: 0.002 sec.

-- 同样的在 ClickHouse 其它节点执行上述建库语句。例如这里在 cdh2、cdh3建库

```

# 3 Max Cloumn
[JdbcClient.supportColumnMax()](src/main/java/yore/JdbcClient.java)

```sql
-- 创建 ClickHouse 表
CREATE TABLE target_m(
 id           UInt64  COMMENT '标签主键',
 name   FixedString(64)  COMMENT '用户名',
 indicator1    Float64  COMMENT '指标1'
)ENGINE = MergeTree ORDER BY id;

-- 添加列的语法格式如下 (drop)
ALTER TABLE upsert_test.target_m ADD COLUMN indicatori Float64 COMMENT '指标i';



```

在实测时，字段数到到`10000`时依然没有报错，共花费 1661718ms 。可以使用DBeaver工具查看。
```sql
cdh3.ygbx.com :) desc target_m;
DESCRIBE TABLE target_m
↙ Progress: 0.00 rows, 0.00 B (0.00 rows/s., 0.00 B/s.) 
┌─name─────────┬─type────────────┬─default_type─┬─default_expression─┬─comment──┬─codec_expression─┬─ttl_expression─┐
│ id           │ UInt64          │              │                    │ 标签主键 │                  │                │
│ name         │ FixedString(64) │              │                    │ 用户名   │                  │                │
│ indicator1   │ Float64         │              │                    │ 指标1    │                  │                │
│ indicator2   │ Float64         │              │                    │ 指标2    │                  │                │
  …… …… 
│ indicator9996  │ Float64         │              │                    │ 指标9996  │                  │                │
│ indicator9997  │ Float64         │              │                    │ 指标9997  │                  │                │
│ indicator9998  │ Float64         │              │                    │ 指标9998  │                  │                │
└────────────────┴─────────────────┴──────────────┴────────────────────┴───────────┴──────────────────┴────────────────┘
↗ Progress: 0.00 rows, 0.00 B (0.00 rows/s., 0.00 B/s.) → Progress: 10.00 thousand rows, 928.06 KB (2.21 thousand rows/s., 204.78 KB/s.)   Showed first 10000.
10003 rows in set. Elapsed: 4.532 sec. Processed 10.00 thousand rows, 928.06 KB (2.21 thousand rows/s., 204.78 KB/s.)
```

## 3.1 1W列插入
导入 `row10000_col10000_column.csv` 数据文件，用时49s
```bash
# awk '{print $0"\n"}' row10000_col10000_column.csv > row10000_col10000_column.csv2
# head -n 10 row10000_col10000_column.csv | awk -F ',' '{print NF}'
# sed -n '1,5000'p row10000_col10000_column.csv >> row10000_col10000_column_0-5000.csv
# sed -n '5001,10000'p row10000_col10000_column.csv >> row10000_col10000_column_5001-1w.csv
echo `date` >> input.log
clickhouse-client -h cdh3 --port 19000 -u default --password KavrqeN1 --max_memory_usage 0  --query "INSERT INTO upsert_test.target_m FORMAT CSV" < /home/row10000_col10000_column.csv
echo `date` >> input.log

```

## 3.2 查询
```sql
cdh3.ygbx.com :) SELECT COUNT(*) FROM target_m;
SELECT count(*)
FROM target_m
→ Progress: 0.00 rows, 0.00 B (0.00 rows/s., 0.00 B/s.) 
┌─count()─┐
│   10000 │
└─────────┘
↘ Progress: 0.00 rows, 0.00 B (0.00 rows/s., 0.00 B/s.) ↓ Progress: 10.00 thousand rows, 80.00 KB (95.50 thousand rows/s., 764.01 KB/s.)  98%
1 rows in set. Elapsed: 0.105 sec. Processed 10.00 thousand rows, 80.00 KB (95.38 thousand rows/s., 763.03 KB/s.)


cdh3.ygbx.com :) SELECT id,name,indicator1,indicator2,indicator9998 FROM target_m LIMIT 3;
SELECT
    id,
    name,
    indicator1,
    indicator2,
    indicator9998
FROM target_m
LIMIT 3
↑ Progress: 0.00 rows, 0.00 B (0.00 rows/s., 0.00 B/s.) 
┌─────────────────id─┬─name───┬─indicator1─┬─indicator2─┬─indicator9998─┐
│ 120164155826114560 │ 平翔旭 │      46.19 │      11.51 │         85.44 │
│ 120164156740472832 │ 明娴   │      34.78 │      60.96 │          7.25 │
│ 120164157059239936 │ 湛毅   │      24.49 │      31.76 │         97.43 │
└────────────────────┴────────┴────────────┴────────────┴───────────────┘
↗ Progress: 0.00 rows, 0.00 B (0.00 rows/s., 0.00 B/s.) → Progress: 3.00 rows, 296.00 B (59.39 rows/s., 5.86 KB/s.)  0%
3 rows in set. Elapsed: 0.051 sec.

cdh3.ygbx.com :) SELECT SUM(indicator1) AS sum_indi1,SUM(indicator9998) AS sum_indi9998 FROM target_m ;
SELECT
    SUM(indicator1) AS sum_indi1,
    SUM(indicator9998) AS sum_indi9998
FROM target_m
↘ Progress: 0.00 rows, 0.00 B (0.00 rows/s., 0.00 B/s.) ┌─────────sum_indi1─┬───────sum_indi9998─┐
│ 499570.0200000009 │ 502561.72999999986 │
└───────────────────┴────────────────────┘
↓ Progress: 0.00 rows, 0.00 B (0.00 rows/s., 0.00 B/s.) ↙ Progress: 10.00 thousand rows, 160.00 KB (118.56 thousand rows/s., 1.90 MB/s.)  98%
1 rows in set. Elapsed: 0.084 sec. Processed 10.00 thousand rows, 160.00 KB (118.46 thousand rows/s., 1.90 MB/s.)

```

# 4 分布式表及建表
```bash
clickhouse-client -h 127.0.0.1 --port 19000 -u default --password KavrqeN1  --multiline
```

```sql
-- 1 选择 upsert_test 库
cdh3.ygbx.com :) use upsert_test;
USE upsert_test
Ok.
0 rows in set. Elapsed: 0.001 sec.

-- 2 target 表
--  2.1 target 表。在数据集文件所在的一个节点执行
CREATE TABLE target(
 id           UInt64  COMMENT '标签主键',
 name   FixedString(64)  COMMENT '用户名',
 indicator1    Float64  COMMENT '指标1'
)ENGINE = MergeTree ORDER BY id;
--  2.2 在 shell 下执行导入命令。导入 6kw 数据 22 秒
--   echo `date` >> input.log
--   clickhouse-client -h cdh3 --port 19000 -u default --password KavrqeN1 --max_memory_usage 0  --query "INSERT INTO upsert_test.target FORMAT CSV" < /home/target_60000000.csv
--   echo `date` >> input.log
--   date
--  2.3 查看导入的数据总数
cdh3.ygbx.com :) SELECT COUNT(*) FROM target;
SELECT COUNT(*)
FROM target
↘ Progress: 0.00 rows, 0.00 B (0.00 rows/s., 0.00 B/s.) ↓ Progress: 45.38 million rows, 363.07 MB (422.77 million rows/s., 3.38 GB/s.)  74%┌──COUNT()─┐
│ 60000000 │
└──────────┘
↙ Progress: 45.38 million rows, 363.07 MB (339.33 million rows/s., 2.71 GB/s.)  74%← Progress: 60.00 million rows, 480.00 MB (448.45 million rows/s., 3.59 GB/s.)  98%
1 rows in set. Elapsed: 0.134 sec. Processed 60.00 million rows, 480.00 MB (448.23 million rows/s., 3.59 GB/s.)
--  2.4 添加一个日期列
ALTER TABLE target ADD COLUMN load_time Date COMMENT '日期';
--  2.5 target_local。分别在各个 ClickHouse 服务节点执行
--   MergeTree(EventDate, (CounterID, EventDate), 8192)
CREATE TABLE target_local(
 id           UInt64  COMMENT '标签主键',
 name   FixedString(64)  COMMENT '用户名',
 indicator1    Float64  COMMENT '指标1',
 load_time Date
)ENGINE = MergeTree(load_time, id, 8192);
--  2.6 创建分布式表。注意指定库名
CREATE TABLE target_all AS target_local
ENGINE = Distributed(perftest_3shards_1replicas, upsert_test, target_local, rand());
--  2.7 将数据加载到分布式表中
cdh3.ygbx.com :) INSERT INTO target_all SELECT * FROM target;
INSERT INTO target_all SELECT *
FROM target
↖ Progress: 1.16 million rows, 95.39 MB (11.22 million rows/s., 919.77 MB/s.)  1%↑ Progress: 1.18 million rows, 96.73 MB (5.79 million rows/s., 474.45 MB/s.)  1%↗ Progress: 1.20 million rows, 98.08 MB (2.95 million rows/s., 242.27 MB/s.%Ok.
0 rows in set. Elapsed: 19.093 sec. Processed 60.00 million rows, 4.92 GB (3.14 million rows/s., 257.69 MB/s.)
--  2.8 查看分布式表。从下面可以看到 target_all 分布式表的数据为 6kw，其它3个节点的分片表的数据总数之和也是 6kw = 19997471 + 20000491 + 20002038
--   2.8.1 target_all
cdh3.ygbx.com :) SELECT COUNT(*) FROM target_all;
SELECT COUNT(*)
FROM target_all
↖ Progress: 0.00 rows, 0.00 B (0.00 rows/s., 0.00 B/s.) ┌──COUNT()─┐
│ 60000000 │
└──────────┘
↑ Progress: 0.00 rows, 0.00 B (0.00 rows/s., 0.00 B/s.) ↗ Progress: 60.00 million rows, 120.00 MB (2.28 billion rows/s., 4.57 GB/s.)  98%
1 rows in set. Elapsed: 0.026 sec. Processed 60.00 million rows, 120.00 MB (2.27 billion rows/s., 4.54 GB/s.)
--   2.8.2 节点1 的 target_local
cdh1.ygbx.com :) SELECT COUNT(*) FROM target_local;
SELECT COUNT(*)
FROM target_local
↑ Progress: 0.00 rows, 0.00 B (0.00 rows/s., 0.00 B/s.) ┌──COUNT()─┐
│ 19997471 │
└──────────┘
↗ Progress: 0.00 rows, 0.00 B (0.00 rows/s., 0.00 B/s.) → Progress: 20.00 million rows, 39.99 MB (1.86 billion rows/s., 3.72 GB/s.)  98%
1 rows in set. Elapsed: 0.011 sec. Processed 20.00 million rows, 39.99 MB (1.85 billion rows/s., 3.69 GB/s.)
--   2.8.3 节点2 的 target_local
cdh2.ygbx.com :) SELECT COUNT(*) FROM target_local;
SELECT COUNT(*)
FROM target_local
→ Progress: 0.00 rows, 0.00 B (0.00 rows/s., 0.00 B/s.) ┌──COUNT()─┐
│ 20000491 │
└──────────┘
↘ Progress: 0.00 rows, 0.00 B (0.00 rows/s., 0.00 B/s.) ↓ Progress: 20.00 million rows, 40.00 MB (1.79 billion rows/s., 3.57 GB/s.)  98%
1 rows in set. Elapsed: 0.011 sec. Processed 20.00 million rows, 40.00 MB (1.78 billion rows/s., 3.55 GB/s.)
--   2.8.4 节点3 的 target_local
cdh3.ygbx.com :) SELECT COUNT(*) FROM target_local;
SELECT COUNT(*)
FROM target_local
→ Progress: 0.00 rows, 0.00 B (0.00 rows/s., 0.00 B/s.) ┌──COUNT()─┐
│ 20002038 │
└──────────┘
↘ Progress: 0.00 rows, 0.00 B (0.00 rows/s., 0.00 B/s.) ↓ Progress: 20.00 million rows, 40.00 MB (3.10 billion rows/s., 6.21 GB/s.)  98%
1 rows in set. Elapsed: 0.007 sec. Processed 20.00 million rows, 40.00 MB (3.04 billion rows/s., 6.08 GB/s.)


-- 3 target_p1 表
--  3.1 target_p1 表。在数据集文件所在的一个节点执行
CREATE TABLE target_p1(
 id           UInt64  COMMENT '标签主键',
 indicator2    Float64  COMMENT '指标1'
)ENGINE = MergeTree ORDER BY id;
--  3.2 在 shell 下执行导入命令。导入 3kw 数据 6 秒
--   echo `date` >> input.log
--   clickhouse-client -h cdh3 --port 19000 -u default --password KavrqeN1 --max_memory_usage 0  --query "INSERT INTO upsert_test.target_p1 FORMAT CSV" < /home/target_part_30000000.csv
--   echo `date` >> input.log
--   date
--  3.3 查看导入的数据总数
cdh3.ygbx.com :) SELECT COUNT(*) FROM target_p1;
SELECT COUNT(*)
FROM target_p1
↑ Progress: 0.00 rows, 0.00 B (0.00 rows/s., 0.00 B/s.) ┌──COUNT()─┐
│ 30000000 │
└──────────┘
↗ Progress: 0.00 rows, 0.00 B (0.00 rows/s., 0.00 B/s.) → Progress: 30.00 million rows, 240.00 MB (375.42 million rows/s., 3.00 GB/s.)  98%
1 rows in set. Elapsed: 0.080 sec. Processed 30.00 million rows, 240.00 MB (374.61 million rows/s., 3.00 GB/s.)
--  3.4 添加一个日期列
ALTER TABLE target_p1 ADD COLUMN load_time Date COMMENT '日期';
--  3.5 target_p1_local。分别在各个 ClickHouse 服务节点执行
--   MergeTree(EventDate, (CounterID, EventDate), 8192)
CREATE TABLE target_p1_local(
 id           UInt64  COMMENT '标签主键',
 indicator2    Float64  COMMENT '指标1',
 load_time Date
)ENGINE = MergeTree(load_time, id, 8192);
--  3.6 创建分布式表。注意指定库名
CREATE TABLE target_p1_all AS target_p1_local
ENGINE = Distributed(perftest_3shards_1replicas, upsert_test, target_p1_local, rand());
--  2.7 将数据加载到分布式表中
cdh3.ygbx.com :) INSERT INTO target_p1_all SELECT * FROM target_p1;
INSERT INTO target_p1_all SELECT *
FROM target_p1
← Progress: 1.87 million rows, 33.62 MB (18.30 million rows/s., 329.35 MB/s.)  6%↖ Progress: 2.95 million rows, 53.08 MB (14.58 million rows/s., 262.52 MB/s.)  9%↑ Progress: 4.03 million rows, 72.55 MB (10.01 million rows/s., 180.26 MB/%Ok.
0 rows in set. Elapsed: 3.786 sec. Processed 30.00 million rows, 540.00 MB (7.92 million rows/s., 142.63 MB/s.)
--  3.8 查看分布式表。从下面可以看到 target_p1_all 分布式表的数据为 6kw，其它3个节点的分片表的数据总数之和也是 3kw = 9999160 + 10001359 + 9999481
--   3.8.1 target_p1_all
cdh3.ygbx.com :) SELECT COUNT(*) FROM target_p1_all;
SELECT COUNT(*)
FROM target_p1_all
↘ Progress: 0.00 rows, 0.00 B (0.00 rows/s., 0.00 B/s.) ┌──COUNT()─┐
│ 30000000 │
└──────────┘
↓ Progress: 0.00 rows, 0.00 B (0.00 rows/s., 0.00 B/s.) ↙ Progress: 30.00 million rows, 60.00 MB (2.77 billion rows/s., 5.54 GB/s.)  98%
1 rows in set. Elapsed: 0.011 sec. Processed 30.00 million rows, 60.00 MB (2.73 billion rows/s., 5.45 GB/s.)
--   3.8.2 节点1 的 target_p1_local
cdh1.ygbx.com :) SELECT COUNT(*) FROM target_p1_local;
SELECT COUNT(*)
FROM target_p1_local
← Progress: 0.00 rows, 0.00 B (0.00 rows/s., 0.00 B/s.) ┌─COUNT()─┐
│ 9999160 │
└─────────┘
↖ Progress: 0.00 rows, 0.00 B (0.00 rows/s., 0.00 B/s.) ↑ Progress: 10.00 million rows, 20.00 MB (1.82 billion rows/s., 3.65 GB/s.)  98%
1 rows in set. Elapsed: 0.006 sec. Processed 10.00 million rows, 20.00 MB (1.76 billion rows/s., 3.51 GB/s.)
--   3.8.3 节点2 的 target_p1_local
cdh2.ygbx.com :) SELECT COUNT(*) FROM target_p1_local;
SELECT COUNT(*)
FROM target_p1_local
↙ Progress: 0.00 rows, 0.00 B (0.00 rows/s., 0.00 B/s.) ┌──COUNT()─┐
│ 10001359 │
└──────────┘
← Progress: 0.00 rows, 0.00 B (0.00 rows/s., 0.00 B/s.) ↖ Progress: 10.00 million rows, 20.00 MB (2.37 billion rows/s., 4.73 GB/s.)  98%
1 rows in set. Elapsed: 0.004 sec. Processed 10.00 million rows, 20.00 MB (2.26 billion rows/s., 4.52 GB/s.)
--   3.8.4 节点3 的 target_p1_local
cdh3.ygbx.com :) SELECT COUNT(*) FROM target_p1_local;
SELECT COUNT(*)
FROM target_p1_local
← Progress: 0.00 rows, 0.00 B (0.00 rows/s., 0.00 B/s.) ┌─COUNT()─┐
│ 9999481 │
└─────────┘
↖ Progress: 0.00 rows, 0.00 B (0.00 rows/s., 0.00 B/s.) ↑ Progress: 10.00 million rows, 20.00 MB (2.35 billion rows/s., 4.70 GB/s.)  98%
1 rows in set. Elapsed: 0.004 sec. Processed 10.00 million rows, 20.00 MB (2.26 billion rows/s., 4.53 GB/s.)


-- 4 target_p2 表
--  4.1 target_p2 表。在数据集文件所在的一个节点执行
CREATE TABLE target_p2(
 id           UInt64  COMMENT '标签主键',
 indicator3    Float64  COMMENT '指标1'
)ENGINE = MergeTree ORDER BY id;
--  4.2 在 shell 下执行导入命令。导入 600 w 数据 1 秒
--   echo `date` >> input.log
--   clickhouse-client -h cdh3 --port 19000 -u default --password KavrqeN1 --max_memory_usage 0  --query "INSERT INTO upsert_test.target_p2 FORMAT CSV" < /home/target_part_6000000.csv
--   echo `date` >> input.log
--   date
--  4.3 查看导入的数据总数
cdh3.ygbx.com :) SELECT COUNT(*) FROM target_p2;
SELECT COUNT(*)
FROM target_p2
↓ Progress: 0.00 rows, 0.00 B (0.00 rows/s., 0.00 B/s.) ┌─COUNT()─┐
│ 6000000 │
└─────────┘
↙ Progress: 0.00 rows, 0.00 B (0.00 rows/s., 0.00 B/s.) ← Progress: 6.00 million rows, 48.00 MB (357.10 million rows/s., 2.86 GB/s.)  98%
1 rows in set. Elapsed: 0.017 sec. Processed 6.00 million rows, 48.00 MB (355.82 million rows/s., 2.85 GB/s.)
--  4.4 添加一个日期列
ALTER TABLE target_p2 ADD COLUMN load_time Date COMMENT '日期';
--  4.5 target_p2_local。分别在各个 ClickHouse 服务节点执行
--   MergeTree(EventDate, (CounterID, EventDate), 8192)
CREATE TABLE target_p2_local(
 id           UInt64  COMMENT '标签主键',
 indicator3    Float64  COMMENT '指标1',
 load_time Date
)ENGINE = MergeTree(load_time, id, 8192);
--  4.6 创建分布式表。注意指定库名
CREATE TABLE target_p2_all AS target_p2_local
ENGINE = Distributed(perftest_3shards_1replicas, upsert_test, target_p2_local, rand());
--  4.7 将数据加载到分布式表中
cdh3.ygbx.com :) INSERT INTO target_p2_all SELECT * FROM target_p2;
INSERT INTO target_p2_all SELECT *
FROM target_p2
↖ Progress: 1.87 million rows, 33.62 MB (18.12 million rows/s., 326.15 MB/s.)  30%↑ Progress: 2.95 million rows, 53.08 MB (14.51 million rows/s., 261.24 MB/s.)  48%↗ Progress: 4.03 million rows, 72.55 MB (9.99 million rows/s., 179.79 MB%Ok.
0 rows in set. Elapsed: 0.792 sec. Processed 6.00 million rows, 108.00 MB (7.58 million rows/s., 136.36 MB/s.)
--  4.8 查看分布式表。从下面可以看到 target_p2_all 分布式表的数据为 600w，其它3个节点的分片表的数据总数之和也是 600w = 1999113 + 2001065 + 1999822
--   4.8.1 target_p2_all
cdh3.ygbx.com :) SELECT COUNT(*) FROM target_p2_all;
SELECT COUNT(*)
FROM target_p2_all
↙ Progress: 0.00 rows, 0.00 B (0.00 rows/s., 0.00 B/s.) ┌─COUNT()─┐
│ 6000000 │
└─────────┘
← Progress: 0.00 rows, 0.00 B (0.00 rows/s., 0.00 B/s.) ↖ Progress: 6.00 million rows, 12.00 MB (694.42 million rows/s., 1.39 GB/s.)  98%
1 rows in set. Elapsed: 0.009 sec. Processed 6.00 million rows, 12.00 MB (678.89 million rows/s., 1.36 GB/s.)
--   4.8.2 节点1 的 target_p2_local
cdh1.ygbx.com :) SELECT COUNT(*) FROM target_p2_local;
SELECT COUNT(*)
FROM target_p2_local
↗ Progress: 0.00 rows, 0.00 B (0.00 rows/s., 0.00 B/s.) ┌─COUNT()─┐
│ 1999113 │
└─────────┘
→ Progress: 0.00 rows, 0.00 B (0.00 rows/s., 0.00 B/s.) ↘ Progress: 2.00 million rows, 4.00 MB (525.60 million rows/s., 1.05 GB/s.)  98%
1 rows in set. Elapsed: 0.004 sec. Processed 2.00 million rows, 4.00 MB (478.99 million rows/s., 957.97 MB/s.)
--   4.8.3 节点2 的 target_p2_local
cdh2.ygbx.com :) SELECT COUNT(*) FROM target_p2_local;
SELECT COUNT(*)
FROM target_p2_local
↑ Progress: 0.00 rows, 0.00 B (0.00 rows/s., 0.00 B/s.) ┌─COUNT()─┐
│ 2001065 │
└─────────┘
↗ Progress: 0.00 rows, 0.00 B (0.00 rows/s., 0.00 B/s.) → Progress: 2.00 million rows, 4.00 MB (549.51 million rows/s., 1.10 GB/s.)  98%
1 rows in set. Elapsed: 0.004 sec. Processed 2.00 million rows, 4.00 MB (527.96 million rows/s., 1.06 GB/s.)
--   4.8.4 节点3 的 target_p2_local
cdh3.ygbx.com :) SELECT COUNT(*) FROM target_p2_local;
SELECT COUNT(*)
FROM target_p2_local
↑ Progress: 0.00 rows, 0.00 B (0.00 rows/s., 0.00 B/s.) ┌─COUNT()─┐
│ 1999822 │
└─────────┘
↗ Progress: 0.00 rows, 0.00 B (0.00 rows/s., 0.00 B/s.) → Progress: 2.00 million rows, 4.00 MB (600.12 million rows/s., 1.20 GB/s.)  98%
1 rows in set. Elapsed: 0.003 sec. Processed 2.00 million rows, 4.00 MB (574.89 million rows/s., 1.15 GB/s.)

```

# 5 更新
```sql
-- 1 查看表
cdh3.ygbx.com :) SHOW TABLES;
SHOW TABLES
↘ Progress: 0.00 rows, 0.00 B (0.00 rows/s., 0.00 B/s.) ┌─name────────────┐
│ target          │
│ target_all      │
│ target_local    │
│ target_m        │
│ target_p1       │
│ target_p1_all   │
│ target_p1_local │
│ target_p2       │
│ target_p2_all   │
│ target_p2_local │
└─────────────────┘
↓ Progress: 0.00 rows, 0.00 B (0.00 rows/s., 0.00 B/s.) ↙ Progress: 10.00 rows, 400.00 B (4.66 thousand rows/s., 186.27 KB/s.)
10 rows in set. Elapsed: 0.002 sec.

-- 2 添加两个指标列
ALTER TABLE target_all ADD COLUMN indicator_2 Float64 COMMENT '新指标列2';
ALTER TABLE target_all ADD COLUMN indicator_3 Float64 COMMENT '新指标列3';
--  （重要）下面需要在各个节点执行
ALTER TABLE target_local ADD COLUMN indicator_2 Float64 COMMENT '新指标列2';
ALTER TABLE target_local ADD COLUMN indicator_3 Float64 COMMENT '新指标列3';



-- 3 查看表信息
cdh3.ygbx.com :) desc target_all;
DESCRIBE TABLE target_all
↑ Progress: 0.00 rows, 0.00 B (0.00 rows/s., 0.00 B/s.) 
┌─name────────┬─type────────────┬─default_type─┬─default_expression─┬─comment───┬─codec_expression─┬─ttl_expression─┐
│ id          │ UInt64          │              │                    │ 标签主键  │                  │                │
│ name        │ FixedString(64) │              │                    │ 用户名    │                  │                │
│ indicator1  │ Float64         │              │                    │ 指标1     │                  │                │
│ load_time   │ Date            │              │                    │           │                  │                │
│ indicator_2 │ Float64         │              │                    │ 新指标列2 │                  │                │
│ indicator_3 │ Float64         │              │                    │ 新指标列3 │                  │                │
└─────────────┴─────────────────┴──────────────┴────────────────────┴───────────┴──────────────────┴────────────────┘
↗ Progress: 0.00 rows, 0.00 B (0.00 rows/s., 0.00 B/s.) → Progress: 6.00 rows, 525.00 B (3.70 thousand rows/s., 323.74 KB/s.)
6 rows in set. Elapsed: 0.002 sec.

-- 4 值统计
--  4.1 合计 target_all 的 indicator_2
cdh3.ygbx.com :) SELECT SUM(indicator_2) FROM target_all;
SELECT SUM(indicator_2)
FROM target_all
↑ Progress: 0.00 rows, 0.00 B (0.00 rows/s., 0.00 B/s.) ┌─SUM(indicator_2)─┐
│                0 │
└──────────────────┘
↗ Progress: 0.00 rows, 0.00 B (0.00 rows/s., 0.00 B/s.) → Progress: 60.00 million rows, 600.00 MB (1.33 billion rows/s., 13.33 GB/s.)  98%
1 rows in set. Elapsed: 0.045 sec. Processed 60.00 million rows, 600.00 MB (1.33 billion rows/s., 13.27 GB/s.)
--  4.2 合计 target_p1_all 的 indicator2
cdh3.ygbx.com :) SELECT SUM(indicator2) FROM target_p1_all;
SELECT SUM(indicator2)
FROM target_p1_all
↘ Progress: 0.00 rows, 0.00 B (0.00 rows/s., 0.00 B/s.) ┌────SUM(indicator2)─┐
│ 1499969540.4700198 │
└────────────────────┘
↓ Progress: 0.00 rows, 0.00 B (0.00 rows/s., 0.00 B/s.) ↙ Progress: 30.00 million rows, 240.00 MB (423.49 million rows/s., 3.39 GB/s.)  98%
1 rows in set. Elapsed: 0.071 sec. Processed 30.00 million rows, 240.00 MB (422.05 million rows/s., 3.38 GB/s.)

-- 5 将表  target_p1_all 更新插入到 target_all 表
cdh3.ygbx.com :) INSERT INTO target_all(id,indicator_2) SELECT id, indicator2 AS indicator_2 FROM target_p1_all;
INSERT INTO target_all (id, indicator_2) SELECT
    id,
    indicator2 AS indicator_2
FROM target_p1_all
↗ Progress: 1.47 million rows, 23.46 MB (14.15 million rows/s., 226.33 MB/s.)  14%→ Progress: 1.52 million rows, 24.38 MB (7.48 million rows/s., 119.61 MB/s.)  15%↘ Progress: 4.03 million rows, 64.49 MB (9.97 million rows/s., 159.58 MB/%Ok.
0 rows in set. Elapsed: 8.153 sec. Processed 30.00 million rows, 480.00 MB (3.68 million rows/s., 58.87 MB/s.)

-- 6 验证。统计 target_all 表的 indicator_2 指标，其sum值基本等于 target_p1_all 表的 indicator2 字段值的sum
cdh3.ygbx.com :) SELECT SUM(indicator_2) FROM target_all;
SELECT SUM(indicator_2)
FROM target_all
↙ Progress: 0.00 rows, 0.00 B (0.00 rows/s., 0.00 B/s.) ┌──SUM(indicator_2)─┐
│ 1499969540.470011 │
└───────────────────┘
← Progress: 0.00 rows, 0.00 B (0.00 rows/s., 0.00 B/s.) ↖ Progress: 90.00 million rows, 837.44 MB (1.89 billion rows/s., 17.54 GB/s.)  98%
1 rows in set. Elapsed: 0.048 sec. Processed 90.00 million rows, 837.44 MB (1.88 billion rows/s., 17.45 GB/s.)

-- 7 测试 600w 的数据集
--  7.1 合计 target_all 的 indicator_3
cdh3.ygbx.com :) SELECT SUM(indicator_3) FROM target_all;
SELECT SUM(indicator_3)
FROM target_all
↑ Progress: 0.00 rows, 0.00 B (0.00 rows/s., 0.00 B/s.) ┌─SUM(indicator_3)─┐
│                0 │
└──────────────────┘
↗ Progress: 0.00 rows, 0.00 B (0.00 rows/s., 0.00 B/s.) → Progress: 90.00 million rows, 837.44 MB (2.39 billion rows/s., 22.28 GB/s.)  98%
1 rows in set. Elapsed: 0.038 sec. Processed 90.00 million rows, 837.44 MB (2.38 billion rows/s., 22.16 GB/s.)
--  7.2 合计 target_p2_all 的 indicator3
cdh3.ygbx.com :) SELECT SUM(indicator3) FROM target_p2_all;
SELECT SUM(indicator3)
FROM target_p2_all
↘ Progress: 0.00 rows, 0.00 B (0.00 rows/s., 0.00 B/s.) ┌────SUM(indicator3)─┐
│ 299915568.89000034 │
└────────────────────┘
↓ Progress: 0.00 rows, 0.00 B (0.00 rows/s., 0.00 B/s.) ↙ Progress: 6.00 million rows, 48.00 MB (220.20 million rows/s., 1.76 GB/s.)  98%
1 rows in set. Elapsed: 0.027 sec. Processed 6.00 million rows, 48.00 MB (218.44 million rows/s., 1.75 GB/s.)
-- 7.3 将表  target_p2_all 的指标更新插入到 target_all 表
cdh3.ygbx.com :) INSERT INTO target_all(id,indicator_3) SELECT id, indicator3 AS indicator_3 FROM target_p2_all;
INSERT INTO target_all (id, indicator_3) SELECT
    id,
    indicator3 AS indicator_3
FROM target_p2_all
← Progress: 1.43 million rows, 22.81 MB (13.76 million rows/s., 220.14 MB/s.)  70%↖ Progress: 1.60 million rows, 25.56 MB (7.87 million rows/s., 125.93 MB/s.)  78%↑ Progress: 3.84 million rows, 61.38 MB (9.50 million rows/s., 152.04 MB/%Ok.
0 rows in set. Elapsed: 1.682 sec. Processed 6.00 million rows, 96.00 MB (3.57 million rows/s., 57.08 MB/s.)
-- 7.4 验证。统计 target_all 表的 indicator_3 指标，其sum值基本等于 target_p2_all 表的 indicator3 字段值的sum
cdh3.ygbx.com :) SELECT SUM(indicator_3) FROM target_all;
SELECT SUM(indicator_3)
FROM target_all
↖ Progress: 0.00 rows, 0.00 B (0.00 rows/s., 0.00 B/s.) ┌─SUM(indicator_3)─┐
│ 299915568.889998 │
└──────────────────┘
↑ Progress: 0.00 rows, 0.00 B (0.00 rows/s., 0.00 B/s.) ↗ Progress: 96.00 million rows, 885.44 MB (2.39 billion rows/s., 22.06 GB/s.)  98%
1 rows in set. Elapsed: 0.040 sec. Processed 96.00 million rows, 885.44 MB (2.38 billion rows/s., 21.98 GB/s.)

```


在上面的操作中，有三张表，主指标表有6kw条数据，另外两个指标表分别为 3kw、600w，并且为了保持和Impala+Kudu一样的效果，我们在表的创建时都是采用分布式表的形式。
有上面的操作可以看到 ClickHouse 更新数据时主要使用的是INSERT 方式，根据主键覆盖，更新插入 3000w 条数据用时 8.153 sec ，更新插入 600w 条数据用时 1.682 sec，
相比于Impala+Kudu速度是非常快的，更快的是其聚合查询。


