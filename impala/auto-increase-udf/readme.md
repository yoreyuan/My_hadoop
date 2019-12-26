
# 使用
## 打包
```bash
# 这一步会生成 SnowFlake-udf.jar 包
mvn clean install
```

## 上传到HDFS
```bash
# 查看 jar 包
jar -tvf SnowFlake-udf.jar

# 创建存放 jar 包的HDFS路径
hadoop fs -mkdir -p /app/udf-lib

hadoop fs -put SnowFlake-udf.jar /app/udf-lib/

```

## 创建函数
```sql
-- 创建函数
-- CREATE FUNCTION IF NOT EXISTS row_sequence() RETURNS BIGINT LOCATION 'hdfs:///app/udf-lib/hive-contrib-2.1.1-cdh6.2.0.jar' symbol='org.apache.hadoop.hive.contrib.udf.UDFRowSequence';
-- CREATE FUNCTION IF NOT EXISTS getSerial() RETURNS BIGINT LOCATION 'hdfs:///app/udf-lib/SnowFlake-udf.jar' symbol='yore.SerialUDF';
CREATE FUNCTION IF NOT EXISTS getSerial(bigint) RETURNS BIGINT LOCATION 'hdfs:///app/udf-lib/SnowFlake-udf.jar' symbol='yore.SerialUDF';
CREATE FUNCTION IF NOT EXISTS getSerial(string) RETURNS BIGINT LOCATION 'hdfs:///app/udf-lib/SnowFlake-udf.jar' symbol='yore.SerialUDF';



SELECT row_sequence(),id,rank,quote FROM quote LIMIT 10;


-- 查看创建的函数
[cdh3.com:21000] default> SHOW FUNCTIONS;
Query: SHOW FUNCTIONS
+-------------+-------------------+-------------+---------------+
| return type | signature         | binary type | is persistent |
+-------------+-------------------+-------------+---------------+
| BIGINT      | getserial(BIGINT) | JAVA        | false         |
| BIGINT      | getserial(STRING) | JAVA        | false         |
+-------------+-------------------+-------------+---------------+
Fetched 3 row(s) in 0.01s

-- 删除函数
DROP FUNCTION  getserial(BIGINT);
DROP FUNCTION  getserial(STRING);

```

## 使用
```sql

-- SELECT row_sequence() AS _id,id,rank,quote FROM quote LIMIT 10;
-- SELECT getSerial(),project_name,statistics_date FROM t_index_value_offline_month;
[cdh3.yore.com:21000] default> SELECT getSerial(id) _id,id,rank,quote FROM quote LIMIT 10;
+--------------------+---------+------+--------------------------------+
| _id                | id      | rank | quote                          |
+--------------------+---------+------+--------------------------------+
| 113847305039523840 | 1292052 | 1    | 希望让人自由。                 |
| 113847305039523841 | 1291546 | 2    | 风华绝代。                     |
| 113847305039523842 | 1295644 | 3    | 怪蜀黍和小萝莉不得不说的故事。 |
| 113847305039523843 | 1292720 | 4    | 一部美国近现代史。             |
| 113847305039523844 | 1292063 | 5    | 最美的谎言。                   |
| 113847305043718144 | 1291561 | 6    | 最好的宫崎骏，最好的久石让。   |
| 113847305043718145 | 1292722 | 7    | 失去的才是永恒的。             |
| 113847305043718146 | 1295124 | 8    | 拯救一个人，就是拯救整个世界。 |
| 113847305043718147 | 3541415 | 9    | 诺兰给了我们一场无法盗取的梦。 |
| 113847305043718148 | 3011091 | 10   | 永远都不能忘记你所爱的人。     |
+--------------------+---------+------+--------------------------------+
Fetched 10 row(s) in 0.12s


SELECT getSerial(quote) _id,id,rank,quote FROM quote LIMIT 10;


```

**说明**： 理论上次 UDF 可以不用传入参数，只需每次调用返回唯一不重复值的。但是 在 [Limitations and Restrictions for Impala UDFs](http://impala.apache.org/docs/build/html/topics/impala_udf.html#ariaid-title23)

> All Impala UDFs must be deterministic, that is, produce the same output each time when passed the same argument values. For example, an Impala UDF must not call functions such as rand() to produce different values for each invocation. It must not retrieve data from external sources, such as from disk or over the network.

从官方文档可以看到，如果每次传入相同的参数时，它都会返回相同的输出。且 CHAR 和 VARCHAR 不能用户 UDF 的输出参数或返回参数。


