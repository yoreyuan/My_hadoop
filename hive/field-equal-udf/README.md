UDF
----

# 1 判断 Hive 表中传入的两个字段的值是否相等

## 1 1代码
[FieldEqualUDF](src/main/java/yore/FieldEqualUDF.java)

## 1.2 打包
通过Maven打包，打包之前可以将依赖范围调整为 **provided**，打包后的包为`field-equal-udf.jar`

## 1.3 使用
将Jar包上传到Hive服务器上，可以是本地，也可以是HDFS（推荐）`hadoop fs -put field-equal-udf.jar /app/udf-lib`
```sql
-- 1 加载jar资源包
-- ADD JAR file:///opt/my_lib/zodiac.jar;
hive> ADD JAR hdfs:/app/udf-lib/field-equal-udf.jar;

-- 2 查看导入的jar
hive>  list jar;

-- 3 创建一个临时的函数，只在当前的session下有效，
hive>  CREATE TEMPORARY FUNCTION field_equal AS 'yore.FieldEqualUDF';
-- 创建永久函数，需要上传到hdfs。创建的永久函数可以在Hive的元数据库下的 FUNCS 表看到创建的详细信息
hive>  CREATE FUNCTION field_equal AS 'yore.FieldEqualUDF' USING jar 'hdfs:/app/udf-lib/field-equal-udf.jar';

-- 4 查看自定义的函数
hive>  SHOW FUNCTIONS LIKE 'field_*';

-- 5 查看创建的函数 zodiac 的描述信息
hive>  DESCRIBE FUNCTION field_equal;
hive>  DESCRIBE FUNCTION EXTENDED field_equal;

-- 6 使用自定义的函数查询数据
--  6.1 不传值时
hive>  SELECT field_equal();
--  6.2 只传入一个字段时
hive>  SELECT field_equal("hello");
--  6.3 传入两个数字型
hive>  SELECT field_equal(127, 127);
--  6.4 传入两个数字型
hive>  SELECT field_equal(127, 128);
--  6.5 传入两个小数
hive>  SELECT field_equal(1.1, 1.1);
--  6.6 传入两个小数
hive>  SELECT field_equal(3.1, 3.14);
--  6.7 传入两个字符串
hive>  SELECT field_equal("hive", "hive");
--  6.8 传入两个字符串
hive>  SELECT field_equal("hadoop", "hive");
--  6.9 传入两个日期型
hive>  SELECT field_equal(date_sub('2019-03-25',1), date_sub('2019-03-25',1));
--  6.10 传入两个日期型
hive>  SELECT field_equal(date_sub('2019-03-25',1), date_sub('2019-03-25',2));


-- 7 删除创建的永久函数
hive>  drop function field_equal;
--7.1 如果使用完毕后，可以执行如下删除函数
hive>  DROP TEMPORARY FUNCTION IF EXISTS field_equal;

-- 8 退出
hive> exit;

```

# Impala
```sql
-- 创建自定义函数
[cdh3:21000] upsert_test> CREATE FUNCTION IF NOT EXISTS field_equal(STRING, STRING) RETURNS BOOLEAN LOCATION 'hdfs:/app/udf-lib/field-equal-udf.jar' symbol='yore.FieldEqualUDF';
+----------------------------+
| summary                    |
+----------------------------+
| Function has been created. |
+----------------------------+
Fetched 1 row(s) in 0.05s

[cdh3:21000] upsert_test> SELECT field_equal("hive", "hive");
Query: SELECT field_equal("hive", "hive")
Query submitted at: 2019-11-27 10:06:18 (Coordinator: http://cdh3.ygbx.com:25000)
Query progress can be monitored at: http://cdh3.ygbx.com:25000/query_plan?query_id=a8444f849fc7c6d4:f8cfb52100000000
+-----------------------------------------+
| upsert_test.field_equal('hive', 'hive') |
+-----------------------------------------+
| true                                    |
+-----------------------------------------+
Fetched 1 row(s) in 0.11s
SELECT field_equal("hive", "hive");

-- ARRAY, BIGINT, BINARY, BOOLEAN, CHAR, DATE, DATETIME, DECIMAL, REAL, FLOAT, INTEGER, MAP, SMALLINT, STRING, STRUCT, TIMESTAMP, TINYINT, VARCHAR
-- int，int
CREATE FUNCTION IF NOT EXISTS field_equal(BIGINT, BIGINT) RETURNS BOOLEAN LOCATION 'hdfs:/app/udf-lib/field-equal-udf.jar' symbol='yore.FieldEqualUDF';
SELECT field_equal(127, 127);
-- float,float
CREATE FUNCTION IF NOT EXISTS field_equal(float, float) RETURNS BOOLEAN LOCATION 'hdfs:/app/udf-lib/field-equal-udf.jar' symbol='yore.FieldEqualUDF';
SELECT field_equal(1.1, 1.1);
-- boolean, boolean
CREATE FUNCTION IF NOT EXISTS field_equal(BOOLEAN, BOOLEAN) RETURNS BOOLEAN LOCATION 'hdfs:/app/udf-lib/field-equal-udf.jar' symbol='yore.FieldEqualUDF';
SELECT field_equal(true, true);
SELECT field_equal(false, false);
SELECT field_equal(true, false);


SHOW FUNCTIONS;
DROP FUNCTION  field_equal(BIGINT, BIGINT);
DROP FUNCTION  field_equal(BOOLEAN, BOOLEAN);
DROP FUNCTION  field_equal(FLOAT, FLOAT);
DROP FUNCTION  field_equal(STRING, STRING);

```