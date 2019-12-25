
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

-- 3 case when
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

 
 
 
