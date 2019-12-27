
# 1 需求
 一个string 参数 str1，
 处理:str1=null or '' 还回'0'
 否则输出str1
 输出string 值
 

# 2 通过 SQL
使用 Impala Shell
```bash
impala-shell
```
 
```sql
-- 1 添加测试数据
INSERT INTO tmp_test VALUES(0, NULL);
INSERT INTO tmp_test VALUES(1, "");
INSERT INTO tmp_test VALUES(2, "  two ");
INSERT INTO tmp_test VALUES(3, "three");
INSERT INTO tmp_test VALUES(4, " bigdata 大数据 ");

-- 2 查看测试表
[cdh3.ygbx.com:21000] default> select * from tmp_test;
+----+--------+
| id | name   |
+----+--------+
| 3  | three  |
| 0  | NULL   |
| 2  |   two  |
| 1  |        |
+----+--------+

-- 3 查看系统自带的函数
[cdh3.yore.com:21000] _impala_builtins> SHOW FUNCTIONS IN _impala_builtins LIKE 'nvl*';
+--------------+---------------------------------+-------------+---------------+
| return type  | signature                       | binary type | is persistent |
+--------------+---------------------------------+-------------+---------------+
| BIGINT       | nvl(BIGINT, BIGINT)             | BUILTIN     | true          |
| BOOLEAN      | nvl(BOOLEAN, BOOLEAN)           | BUILTIN     | true          |
| DECIMAL(*,*) | nvl(DECIMAL(*,*), DECIMAL(*,*)) | BUILTIN     | true          |
| DOUBLE       | nvl(DOUBLE, DOUBLE)             | BUILTIN     | true          |
| FLOAT        | nvl(FLOAT, FLOAT)               | BUILTIN     | true          |
| INT          | nvl(INT, INT)                   | BUILTIN     | true          |
| SMALLINT     | nvl(SMALLINT, SMALLINT)         | BUILTIN     | true          |
| STRING       | nvl(STRING, STRING)             | BUILTIN     | true          |
| TIMESTAMP    | nvl(TIMESTAMP, TIMESTAMP)       | BUILTIN     | true          |
| TINYINT      | nvl(TINYINT, TINYINT)           | BUILTIN     | true          |
+--------------+---------------------------------+-------------+---------------+
Fetched 10 row(s) in 0.01s

-- 4 case when
--  可以看到 id 为 0 的 null 和 id 为 1 的 "" 都已经转成了 "0"
[cdh3.yore.com:21000] default> SELECT id,CASE WHEN nullvalue(name) THEN "0" WHEN name="" THEN "0" ELSE TRIM(name) END AS name FROM tmp_test;
+----+-------+
| id | name  |
+----+-------+
| 3  | three |
| 0  | 0     |
| 2  | two   |
| 1  | 0     |
+----+-------+
Fetched 4 row(s) in 0.16s

```

# 3 UDF
## 3.1 源码
代码请看 [MyNVLUDF](src/main/java/yore/MyNVLUDF.java)

## 3.2 使用

```bash
# 将 UDF jar 包上传到 HDFS
hadoop fs -put field-equal-udf.jar /app/udf-lib/

```

创建函数并使用
```sql
-- 1 创建自定函数
--  1.1 不指定 为 null 或者 "" 时需要替换的值时，默认返回 "0"
CREATE FUNCTION IF NOT EXISTS yore_nvl(STRING) RETURNS STRING LOCATION 'hdfs:/app/udf-lib/field-equal-udf.jar' symbol='yore.MyNVLUDF';

--  1.2 指定 为 null 或者 "" 需要替换的值时为 参数 2 
CREATE FUNCTION IF NOT EXISTS yore_nvl(STRING, STRING) RETURNS STRING LOCATION 'hdfs:/app/udf-lib/field-equal-udf.jar' symbol='yore.MyNVLUDF';


-- 2 查看已创建的自定义函数
[cdh3.yore.com:21000] default>  SHOW FUNCTIONS;
+-------------+--------------------------+-------------+---------------+
| return type | signature                | binary type | is persistent |
+-------------+--------------------------+-------------+---------------+
| STRING      | yore_nvl(STRING)         | JAVA        | false         |
| STRING      | yore_nvl(STRING, STRING) | JAVA        | false         |
+-------------+--------------------------+-------------+---------------+
Fetched 2 row(s) in 0.01s


-- 3 使用
--  3.1 不指定替换的字符串
[cdh3.yore.com:21000] default> SELECT id,yore_nvl(name) AS name FROM tmp_test;
+----+-------+
| id | name  |
+----+-------+
| 3  | three |
| 0  | 0     |
| 2  | two   |
| 1  | 0     |
+----+-------+
Fetched 4 row(s) in 0.11s

--  3.2 指定替换的字符串 
[cdh3.yore.com:21000] default> SELECT id,yore_nvl(name, "-") AS name FROM tmp_test;
+----+-------+
| id | name  |
+----+-------+
| 3  | three |
| 2  | two   |
| 1  | -     |
| 0  | -     |
+----+-------+
Fetched 4 row(s) in 0.11s


-- 4 删除自定义的函数
DROP FUNCTION  yore_nvl(STRING);
DROP FUNCTION  yore_nvl(STRING, STRING);

```


## 3.3 其它类型的支持
```sql
-- ARRAY, BIGINT, BINARY, BOOLEAN, CHAR, DATE, DATETIME, DECIMAL, REAL, FLOAT, INTEGER, MAP, SMALLINT, STRING, STRUCT, TIMESTAMP, TINYINT, VARCHAR

-- 建表
CREATE TABLE t(
a1 BIGINT,
a2 INTEGER,
b BOOLEAN, 
c1 CHAR(5),
c2 VARCHAR(30),
c3 STRING,
--d1 DATE,
--d2 DATETIME,
d3 TIMESTAMP,
e1 FLOAT,
e2 DOUBLE
);

-- 插入测试数据
insert into t values(111, 1, true, CAST('a' AS CHAR(5)), CAST("Impala" AS VARCHAR(30)), "apache impala", "2019-12-26 11:02:03", 3.0, 3.1415926);
insert into t(a1,b) values(222, false);
--  insert into t(a1, a2, b, c2, c3) values(333, 3, false, CAST("  bigdata 大数据 " AS VARCHAR(30)), "  bigdata2 大数据2 " );


-- 查看表结构信息
[cdh3.yore.com:21000] default> DESC t;
+------+-------------+---------+
| name | type        | comment |
+------+-------------+---------+
| a1   | bigint      |         |
| a2   | int         |         |
| b    | boolean     |         |
| c1   | char(5)     |         |
| c2   | varchar(30) |         |
| c3   | string      |         |
| d3   | timestamp   |         |
| e1   | float       |         |
| e2   | double      |         |
+------+-------------+---------+
Fetched 9 row(s) in 0.01s

-- 查看数据
[cdh3.yore.com:21000] default> select * from t;
+-----+------+-------+-------+--------+---------------+---------------------+------+-----------+
| a1  | a2   | b     | c1    | c2     | c3            | d3                  | e1   | e2        |
+-----+------+-------+-------+--------+---------------+---------------------+------+-----------+
| 111 | 1    | true  | a     | Impala | apache impala | 2019-12-26 11:02:03 | 3    | 3.1415926 |
| 222 | NULL | false | NULL  | NULL   | NULL          | NULL                | NULL | NULL      |
+-----+------+-------+-------+--------+---------------+---------------------+------+-----------+
Fetched 1 row(s) in 0.11s


-- 测试
SELECT yore_nvl(CAST(a1 AS STRING)) AS name FROM t;
SELECT yore_nvl(CAST(a2 AS STRING)) AS name FROM t;
SELECT yore_nvl(CAST(b AS STRING)) AS name FROM t;
SELECT yore_nvl(CAST(c1 AS STRING)) AS name FROM t;
SELECT yore_nvl(CAST(c2 AS STRING)) AS name FROM t;
SELECT yore_nvl(CAST(c3 AS STRING)) AS name FROM t;
SELECT yore_nvl(CAST(d3 AS STRING)) AS name FROM t;
SELECT yore_nvl(CAST(e1 AS STRING)) AS name FROM t;
SELECT yore_nvl(CAST(e2 AS STRING)) AS name FROM t;

```
 
