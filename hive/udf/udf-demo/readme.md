UDF
------
Hive的UDF包括UDF、UDAF和UDTF三种函数。

UDF类别  | 描述
:---- | :----
UDF（User Defined Function） | 用户自定义标量值函数。其输入与输出是一对一的关系，即读入一行数据，写出一条输出值 。
UDTF（User Defined Table Valued Function） | 自定义表值函数。用来解决一次函数调用输出多行数据场景。它是唯一能够返回多个字段的自定义函数，而UDF只能一次计算输出一条返回值。
UDAF（User Defined Aggregation Function） | 自定义聚合函数。其输入与输出是多对一的关系， 即将多条输入记录聚合成一条输出值。它可以与SQL中的GROUP BY语句联用。具体语法请参见聚合函数。

# 1 UDF
下面我们开始编写自己的UDF。假设我们有一张表，表中的一个字段存储的是每个用户的生日，通过这个信息，我们期望计算出每个人所属的星座。
下面是一个样本数据` littlebigdata.txt ` 
```csv
edward capriolo,edward@media6degrees.com,2-12-1981,209.191.139.200,M,10
bob,bob@test.net,10-10-2004,10.10.10.1,M,50
sara connor,sara@sky.net,4-5-1974,64.64.5.1,F,2
```

进入Hive终端
```bash
# 使用hive cli。也可以使用beeline，但这这次为了演示，我们先使用hive登陆
[root@node1 ~]# hive

```

创建表，将样本数据导入到Hive中
```sql
-- 创建 littlebigdata 表
hive>  CREATE TABLE IF NOT EXISTS littlebigdata(
name STRING,
email STRING,
bday STRING,
ip STRING,
gender STRING,
anum INT
)ROW FORMAT DELIMITED FIELDS TERMINATED BY ','
STORED AS TEXTFILE;

-- 加载数据到 littlebigdata 表
0: jdbc:hive2://localhost:10000> LOAD DATA LOCAL INPATH '/root/littlebigdata.txt' OVERWRITE INTO TABLE littlebigdata;

```

## 代码
有两种方式：
* 通过继承UDF来实现：[UDFZodiacSign](src/main/java/yore/UDFZodiacSign.java)
* 通过继承GenericUDF来实现：[GenericUDF](src/main/java/yore/GenericUDFZodiacSign.java)

## 使用
将Jar包上传到Hive服务器上，可以是本地，也可以是HDFS（推荐）`hadoop fs -put zodiac.jar /app/lib/`
```sql
-- 加载jar资源包
-- ADD JAR file:///opt/my_lib/zodiac.jar;
hive> ADD JAR hdfs:/app/lib/zodiac.jar;

-- 查看导入的jar
hive>  list jar;

-- 创建一个临时的函数，只在当前的session下有效，
hive>  CREATE TEMPORARY FUNCTION zodiac AS 'yore.UDFZodiacSign';
-- 创建永久函数，需要上传到hdfs。创建的永久函数可以在Hive的元数据库下的 FUNCS 表看到创建的详细信息
hive>  CREATE FUNCTION zodiac2 AS 'yore.GenericUDFZodiacSign' USING jar 'hdfs:/app/lib/zodiac.jar';

-- 查看创建的函数 zodiac 的描述信息
hive>  DESCRIBE FUNCTION zodiac;
hive>  DESCRIBE FUNCTION EXTENDED zodiac;

-- 查看当前环境下可用的函数
hive>  show functions like 'z*';

--使用自定义的函数查询数据
hive>  SELECT name, bday, zodiac(bday) FROM littlebigdata;
hive>  SELECT zodiac2("3-25-1997", "11-09-1991", "04-09-2019");

-- 删除创建的永久函数
hive>  drop function zodiac2;
--如果使用完毕后，可以执行如下删除函数
hive>  DROP TEMPORARY FUNCTION IF EXISTS zodiac;

-- 退出
hive> exit;

```

再次登录`hive`，查看导入的jar和函数，发现已经都没有了。如果想这些确实经常使用，可以将上面的经常用到的命令添加到 ` $HOME/.hiverc`文件中，
`vim  $HOME/.hiverc`添加如下内容（记得赋予响应的权限`chmod +x $HOME/.hiverc`），再次进入beeline查看，发现可以直接使用这个函数了。
```sql
ADD JAR hdfs:/app/lib/zodiac.jar;
CREATE TEMPORARY FUNCTION zodiac AS 'yore.UDFZodiacSign';

```

**注意1**：上面的`vim  $HOME/.hiverc`仅作用于`hive`方式登陆的，如果使用`beeline`则不生效。如果使用beeline可以考虑创建永久函数的方式。

**注意2**：类型转化异常
```
0: jdbc:hive2://localhost:10000> SELECT zodiac2("3-25-1997", "11-09-1991");
Error: Error while compiling statement: FAILED: ClassCastException java.lang.String cannot be cast to org.apache.hadoop.io.Text (state=42000,code=40000)
```
当为String时需要输出的类型转换为 org.apache.hadoop.io.Text

<br/>


# 2 UDAF
在写UDAF时一定要注意内存的使用问题，通过配置参数可以调整执行过程的内存使用量，但这种方式并非总是有效：
```xml
<property>
 <name>mapred.child.java.opts</name>
 <value>-Xmx200m</value>
</property>
```

MySQL中有一个很有用的函数`GROUP_CONCAT`，它可以将一组中的所有元素按照用户指定的分隔符封装成一个字符串，例如MySQL中如下使用：
```sql
--创建一个表：people
mysql > CREATE TABLE people (name varchar(32),friendname varchar(32));

-- 插入数据
mysql > insert into people(name, friendname) values("bob", "sara");
mysql > insert into people(name, friendname) values("bob", "john");
mysql > insert into people(name, friendname) values("bob", "ted");
mysql > insert into people(name, friendname) values("john", "sara");
mysql > insert into people(name, friendname) values("ted", "bob");
mysql > insert into people(name, friendname) values("ted", "sara");

-- 查询插入的数据
mysql > SELECT * FROM people;
+------+------------+
| name | friendname |
+------+------------+
| bob  | sara       |
| bob  | john       |
| bob  | ted        |
| john | sara       |
| ted  | bob        |
| ted  | sara       |
+------+------------+
6 rows in set (0.01 sec)
 
-- 统计每个人的所有朋友信息。使用GROUP_CONCAT将同一个人的所有朋友姓名用逗号连接
mysql > SELECT name, GROUP_CONCAT(friendname SEPARATOR ',') FROM people GROUP BY name;
+------+----------------------------------------+
| name | GROUP_CONCAT(friendname SEPARATOR ',') |
+------+----------------------------------------+
| bob  | sara,john,ted                          |
| john | sara                                   |
| ted  | bob,sara                               |
+------+----------------------------------------+
3 rows in set (0.00 sec)
 
```

## 编写UDAF代码，并上传到服务器
代码查看 [GenericUDAFCollect](src/main/java/yore/GenericUDAFCollect.java)。

使用Maven重新打包项目，将打好的项目jar包`zodiac.jar`上传到`hdfs:/app/lib/`下。

## 使用
我们在Hive中实现这个功能。先准备数据`afile.txt`，然后上传到hive服务器上，例如在家目录下`$HOME`
```csv
bob,sara
bob,john
bob,ted
john,sara
ted,bob
ted,sara
```

登陆Hive CLI，这次我们使用beeline连接Hive。
```bash
# 进入Beeline
[root@node1 ~]# beeline
# 连接Hive，后面输入用户名和免密
beeline> !connect jdbc:hive2://localhost:10000
```

执行如下命令使用
```sql
-- 创建表people
0: jdbc:hive2://localhost:10000> CREATE TABLE people (
. . . . . . . . . . . . . . . .> name varchar(32),
. . . . . . . . . . . . . . . .> friendname varchar(32)
. . . . . . . . . . . . . . . .> )ROW FORMAT DELIMITED FIELDS TERMINATED BY ','
. . . . . . . . . . . . . . . .> STORED AS TEXTFILE;

-- 加载数据到 people 表
0: jdbc:hive2://localhost:10000> LOAD DATA LOCAL INPATH '${env:HOME}/afile.txt' INTO TABLE people;

-- 查看数据
0: jdbc:hive2://localhost:10000> SELECT * FROM people LIMIT 10;
+--------------+--------------------+
| people.name  | people.friendname  |
+--------------+--------------------+
| bob          | sara               |
| bob          | john               |
| bob          | ted                |
| john         | sara               |
| ted          | bob                |
| ted          | sara               |
+--------------+--------------------+
6 rows selected (2.129 seconds)

-- 使用Hive自带的方法 collect_list 输出name的集合
0: jdbc:hive2://localhost:10000> SELECT collect_list(name) FROM people;
+-----------------------------------------+
|                   _c0                   |
+-----------------------------------------+
| ["bob","bob","bob","john","ted","ted"]  |
+-----------------------------------------+
1 row selected (13.076 seconds)

-- 使用Hive自带的方法 collect_set 输出去重name后的集合
0: jdbc:hive2://localhost:10000> SELECT collect_set(name) FROM people;
+-----------------------+
|          _c0          |
+-----------------------+
| ["bob","john","ted"]  |
+-----------------------+
1 row selected (11.241 seconds)

-- 使用hive自带的方法实现。直接使用 collect_list 输出结果差不多，但它是一个集合的样式
-- 并不像在 MySQL 那样用逗号连接，因此需要用下面的 concat_ws 函数用逗号合并集合中的元素
0: jdbc:hive2://localhost:10000> SELECT name,collect_list(friendname) FROM people GROUP BY name;
+-------+------------------------+
| name  |          _c1           |
+-------+------------------------+
| bob   | ["sara","john","ted"]  |
| john  | ["sara"]               |
| ted   | ["bob","sara"]         |
+-------+------------------------+
3 rows selected (10.987 seconds)

-- 如果字段不是String，可以转成String： cast(friendname AS STRING)
0: jdbc:hive2://localhost:10000> SELECT name,concat_ws(',', collect_list(friendname)) FROM people GROUP BY name;
+-------+----------------+
| name  |      _c1       |
+-------+----------------+
| bob   | sara,john,ted  |
| john  | sara           |
| ted   | bob,sara       |
+-------+----------------+
3 rows selected (11.264 seconds)


-- 通过上面的方式，但可以看到使用自带的函数也能实现这个功能
-- 但是比较复杂，中间使用到了两个函数的组合才最终实现了MySQL中的 GROUP_CONCAT 同样的方法
-- 下面使用我们自定的 UDAF 来实现
0: jdbc:hive2://localhost:10000> ADD JAR hdfs:/app/lib/zodiac.jar;
0: jdbc:hive2://localhost:10000> CREATE TEMPORARY FUNCTION collect  AS 'yore.GenericUDAFCollect';
0: jdbc:hive2://localhost:10000> DESCRIBE FUNCTION EXTENDED collect ;
-- 使用自定义的UDAF函数collect查询数据
0: jdbc:hive2://localhost:10000> SELECT name,concat_ws(',', collect (friendname)) FROM people GROUP BY name;
+-------+----------------+
| name  |      _c1       |
+-------+----------------+
| bob   | sara,john,ted  |
| john  | sara           |
| ted   | bob,sara       |
+-------+----------------+
3 rows selected (11.683 seconds)

```

# 3 UDTF
## 单行返回多个字段的UDTF
```bash
# 进入Beeline
[root@node1 ~]# beeline
# 连接Hive，后面输入用户名和免密
# 连接Hive，后面提示输入用户名和密码，输入用户名和密码即可连接到Hive
# 其实也可以通过beeline连接MySQL、Oracle、Impala等，
# 例如 Impala：beeline -d "com.cloudera.impala.jdbc41.Driver" -u "jdbc:impala://cdh2:21050/default;auth=noSasl" --isolation=default
# 例如 MySQL：!connect jdbc:mysql://cdh1:3306  root 123456 com.mysql.jdbc.Driver
beeline> !connect jdbc:hive2://localhost:10000
```

```sql
-- 1 UDF形式。使用parse_url函数

-- 1.1 查看函数文档信息
0: jdbc:hive2://localhost:10000> DESCRIBE FUNCTION EXTENDED parse_url ;
+----------------------------------------------------+
|                      tab_name                      |
+----------------------------------------------------+
| parse_url(url, partToExtract[, key]) - extracts a part from a URL |
| Parts: HOST, PATH, QUERY, REF, PROTOCOL, AUTHORITY, FILE, USERINFO |
| key specifies which query to extract               |
| Example:                                           |
|   > SELECT parse_url('http://facebook.com/path/p1.php?query=1', 'HOST') FROM src LIMIT 1; |
|   'facebook.com'                                   |
|   > SELECT parse_url('http://facebook.com/path/p1.php?query=1', 'QUERY') FROM src LIMIT 1; |
|   'query=1'                                        |
|   > SELECT parse_url('http://facebook.com/path/p1.php?query=1', 'QUERY', 'query') FROM src LIMIT 1; |
|   '1'                                              |
| Function class:org.apache.hadoop.hive.ql.udf.UDFParseUrl |
| Function type:BUILTIN                              |
+----------------------------------------------------+
12 rows selected (0.069 seconds)

-- 1.2 使用 parse_url 函数解析url，多次调用解析需要的信息。
--  从SQL上我们可以看到要解析8个字段信息输出为表字段时，就需要重复调用8次
0: jdbc:hive2://localhost:10000> SELECT parse_url(weblogs.url, 'HOST') AS  host,
. . . . . . . . . . . . . . . .> parse_url(weblogs.url, 'PATH') AS path ,
. . . . . . . . . . . . . . . .> parse_url(weblogs.url, 'PROTOCOL') AS protocol ,
. . . . . . . . . . . . . . . .> parse_url(weblogs.url, 'AUTHORITY') AS authority ,
. . . . . . . . . . . . . . . .> parse_url(weblogs.url, 'USERINFO') AS userinfo ,
. . . . . . . . . . . . . . . .> parse_url(weblogs.url, 'QUERY', 'sourceid') AS sourceid,
. . . . . . . . . . . . . . . .> parse_url(weblogs.url, 'QUERY', 'q') AS keyword,
. . . . . . . . . . . . . . . .> parse_url(weblogs.url, 'QUERY', 'ie') AS ie
. . . . . . . . . . . . . . . .> FROM (
. . . . . . . . . . . . . . . .> select "https://www.google.com/search?q=mercury&oq=mercury&aqs=chrome..69i57j0l4j69i60.2126j0j7&sourceid=chrome&ie=UTF-8" AS url
. . . . . . . . . . . . . . . .> ) weblogs;
+-----------------+----------+-----------+-----------------+-----------+-----------+----------+--------+
|      host       |   path   | protocol  |    authority    | userinfo  | sourceid  | keyword  |   ie   |
+-----------------+----------+-----------+-----------------+-----------+-----------+----------+--------+
| www.google.com  | /search  | https     | www.google.com  | NULL      | chrome    | mercury  | UTF-8  |
+-----------------+----------+-----------+-----------------+-----------+-----------+----------+--------+
1 row selected (0.59 seconds)



-- 2 UDTF形式。使用 parse_url_tuple 函数

-- 2.1 查看函数文档信息
-- parse_url_tuple(url, partname1, partname2, ..., partnameN)。从URL中抽取N个部分信息
-- 提取的信息可以为（名字区分大小写，且不应包含不必要的空格）： HOST, PATH, QUERY, REF, PROTOCOL, AUTHORITY, FILE, USERINFO, QUERY:<KEY_NAME> 
0: jdbc:hive2://localhost:10000> DESCRIBE FUNCTION EXTENDED parse_url_tuple ;
+----------------------------------------------------+
|                      tab_name                      |
+----------------------------------------------------+
| parse_url_tuple(url, partname1, partname2, ..., partnameN) - extracts N (N>=1) parts from a URL. |
| It takes a URL and one or multiple partnames, and returns a tuple. All the input parameters and output column types are string. |
| Partname: HOST, PATH, QUERY, REF, PROTOCOL, AUTHORITY, FILE, USERINFO, QUERY:<KEY_NAME> |
| Note: Partnames are case-sensitive, and should not contain unnecessary white spaces. |
| Example:                                           |
|   > SELECT b.* FROM src LATERAL VIEW parse_url_tuple(fullurl, 'HOST', 'PATH', 'QUERY', 'QUERY:id') b as host, path, query, query_id LIMIT 1; |
|   > SELECT parse_url_tuple(a.fullurl, 'HOST', 'PATH', 'QUERY', 'REF', 'PROTOCOL', 'FILE',  'AUTHORITY', 'USERINFO', 'QUERY:k1') as (ho, pa, qu, re, pr, fi, au, us, qk1) from src a; |
| Function class:org.apache.hadoop.hive.ql.udf.generic.GenericUDTFParseUrlTuple |
| Function type:BUILTIN                              |
+----------------------------------------------------+
9 rows selected (0.26 seconds)

-- 2.2 使用 parse_url_tuple 函数解析url，然后生成多列的结果数据。
--   url可以是从表中获取，例如 select parse_url_tuple(weblogs.url, ……) from weblogs AS (field1, field2, ……);
--   这里为了演示直接指定了一个 url 的字符串。
0: jdbc:hive2://localhost:10000>  SELECT parse_url_tuple(
. . . . . . . . . . . . . . . .>  "https://www.google.com/search?q=mercury&oq=mercury&aqs=chrome..69i57j0l4j69i60.2126j0j7&sourceid=chrome&ie=UTF-8",
. . . . . . . . . . . . . . . .>  'HOST', 'PATH', 'PROTOCOL',  'AUTHORITY', 'USERINFO', 'QUERY:sourceid', 'QUERY:q', 'QUERY:ie')
. . . . . . . . . . . . . . . .>  AS (host, path, protocol, authority, userinfo, sourceid, keyword, ie);
+-----------------+----------+-----------+-----------------+-----------+-----------+----------+--------+
|      host       |   path   | protocol  |    authority    | userinfo  | sourceid  | keyword  |   ie   |
+-----------------+----------+-----------+-----------------+-----------+-----------+----------+--------+
| www.google.com  | /search  | https     | www.google.com  | NULL      | chrome    | mercury  | UTF-8  |
+-----------------+----------+-----------+-----------------+-----------+-----------+----------+--------+
1 row selected (0.168 seconds)

```

## 返回多行数据的UDTF

UDTF代码可以查看：[GenericUDTFFor](src/main/java/yore/GenericUDTFFor.java)，
通过Maven方式打包后提交到Hive服务器上，和前面的一样，执行如下来使用这个UDTF。

```bash
# 将重新打好的jar包上传到HDFS
hadoop fs -put zodiac.jar /app/lib

# 使用beeline 登陆Hive
#    参数说明（这里列出几个重要的，其它参数可以通过 --help查看）：
#        -u         (URL)要连接的 JDBC URL
#        -n         (username)连接数据库的用户名 
#        -p         (password)连接数据库的用户的密码
#        -d         (driver)指定连接数据库的驱动
#        -i         (initialization)指定初始化的脚本文件
#        -e         (executed)执行一个SQL语句
#        -f         (file)指定执行的SQL脚本文件
#        --help                         获取帮助信息
#        --hiveconf property=value      给定一个属性和值配置信息
#        --hivevar name=value           配置一个单元变量名和值（Hive特定的），会在会话级别设置并引用
#        --isolation=LEVEL              指定事物个隔离级别。例如：--isolation=default
#
beeline -u "jdbc:hive2://localhost:10000" -n hive

```

准备数据`student_score.txt`（例如放到Hive服务本地的家目录下），如下
```
bob|Chinese:80,Math:60,English:90
john|Chinese:90,Math:80,English:70
ted|Chinese:88,Math:90,English:96
sara|Chinese:99,Math:65,English:60
```

**需求**：有一张hive表，分别是学生姓名name(string)，学生成绩score(map<string,string>),成绩列中key是学科名称，value是对应学科分数，
请用一个hql求一下每个学生成绩最好的学科及分数、最差的学科及分数、平均分数。
```sql
-- 1 建表 student_score 。
-- 学科分数信息为一个Map类型，字段间分隔符为管道线|，集合值的分隔符为英文逗号，key和value分隔符为冒号
0: jdbc:hive2://localhost:10000> CREATE TABLE student_score(
. . . . . . . . . . . . . . . .> name string,
. . . . . . . . . . . . . . . .> score map<String,string>
. . . . . . . . . . . . . . . .> )ROW FORMAT delimited
. . . . . . . . . . . . . . . .> FIELDS TERMINATED BY '|'
. . . . . . . . . . . . . . . .> COLLECTION ITEMS TERMINATED BY ','
. . . . . . . . . . . . . . . .> MAP KEYS TERMINATED by ':'
. . . . . . . . . . . . . . . .> STORED AS TEXTFILE;

-- 2 导入数据
0: jdbc:hive2://localhost:10000> LOAD DATA LOCAL INPATH '${env:HOME}/student_score.txt' INTO TABLE student_score;

-- 3 查看👀数据
0: jdbc:hive2://localhost:10000> SELECT name,score FROM student_score;
+-------+----------------------------------------------+
| name  |                    score                     |
+-------+----------------------------------------------+
| bob   | {"Chinese":"80","Math":"60","English":"90"}  |
| john  | {"Chinese":"90","Math":"80","English":"70"}  |
| ted   | {"Chinese":"88","Math":"90","English":"96"}  |
| sara  | {"Chinese":"99","Math":"65","English":"60"}  |
+-------+----------------------------------------------+
4 rows selected (0.242 seconds)

-- 4 explode使用。Hive中没有直接操作Map的方法，因此需要用这个函数拆分为表
0: jdbc:hive2://localhost:10000> SELECT explode(score) AS (key,value) FROM student_score;
+----------+--------+
|   key    | value  |
+----------+--------+
| Chinese  | 80     |
| Math     | 60     |
| English  | 90     |
| Chinese  | 90     |
| Math     | 80     |
| English  | 70     |
| Chinese  | 88     |
| Math     | 90     |
| English  | 96     |
| Chinese  | 99     |
| Math     | 65     |
| English  | 60     |
+----------+--------+
12 rows selected (0.18 seconds)
-- 紧接着我们可能会想到关联上name就可以处理了嘛，但当执行如下hql是发现报了错，
-- 那我们该怎么把需要的信息连接到 HDTF 输出的表上呢？这是就需要用到侧视图了 
0: jdbc:hive2://localhost:10000> SELECT name,explode(score) AS (key,value) FROM student_score;
Error: Error while compiling statement: FAILED: SemanticException 1:35 AS clause has an invalid number of aliases. Error encountered near token 'value' (state=42000,code=40000)

-- 5 lateral view 。问题完美得到解决，剩下就可以对学生和分数实现复杂的分析了
0: jdbc:hive2://localhost:10000> SELECT name,key,value FROM student_score
. . . . . . . . . . . . . . . .> lateral VIEW explode(score) scntable AS key,value;
+-------+----------+--------+
| name  |   key    | value  |
+-------+----------+--------+
| bob   | Chinese  | 80     |
| bob   | Math     | 60     |
| bob   | English  | 90     |
| john  | Chinese  | 90     |
| john  | Math     | 80     |
| john  | English  | 70     |
| ted   | Chinese  | 88     |
| ted   | Math     | 90     |
| ted   | English  | 96     |
| sara  | Chinese  | 99     |
| sara  | Math     | 65     |
| sara  | English  | 60     |
+-------+----------+--------+
12 rows selected (0.15 seconds)

-- 6 创建一个视图
--   只要为了后面使用 5 步的结果表时更加的方便
0: jdbc:hive2://localhost:10000> CREATE VIEW IF NOT EXISTS ss_view AS
. . . . . . . . . . . . . . . .> SELECT name,key,value FROM student_score
. . . . . . . . . . . . . . . .> lateral VIEW explode(score) scntable AS key,value;
--  6.1查看视图表： 
0: jdbc:hive2://localhost:10000> SHOW VIEWS;
+-----------+
| tab_name  |
+-----------+
| ss_view   |
+-----------+
1 row selected (0.05 seconds)
-- 6.2 查看视图数据(数据和第5步是一模一样的)： 
0: jdbc:hive2://localhost:10000> SELECT name,key,value FROM ss_view;
+-------+----------+--------+
| name  |   key    | value  |
+-------+----------+--------+
| bob   | Chinese  | 80     |
| bob   | Math     | 60     |
| bob   | English  | 90     |
| john  | Chinese  | 90     |
| john  | Math     | 80     |
| john  | English  | 70     |
| ted   | Chinese  | 88     |
| ted   | Math     | 90     |
| ted   | English  | 96     |
| sara  | Chinese  | 99     |
| sara  | Math     | 65     |
| sara  | English  | 60     |
+-------+----------+--------+
12 rows selected (0.148 seconds)
-- 删除视图：DROP VIEW IF EXISTS ss_view

-- 7 用用hql求出每个学生成绩最好的学科(subject_max)及分数(score_max)、最差的学科(subject_min)及分数(score_min)、平均分数(score_avg)。
0: jdbc:hive2://localhost:10000> SELECT t1.name name,t1.subject_max subject_max,t1.score_max score_max,t2.subject_min subject_min, t2.score_min score_min,t2.score_avg score_avg
. . . . . . . . . . . . . . . .> FROM (SELECT ma.name name,ss_view.key subject_max,ma.score_max score_max FROM
. . . . . . . . . . . . . . . .>  (SELECT name,MAX(value) AS score_max FROM ss_view GROUP BY name) ma LEFT JOIN ss_view
. . . . . . . . . . . . . . . .>  ON ss_view.name=ma.name AND ss_view.value=ma.score_max
. . . . . . . . . . . . . . . .> ) t1 LEFT JOIN (
. . . . . . . . . . . . . . . .> SELECT mi.name name,ss_view.key subject_min,mi.score_min score_min,mi.score_avg score_avg FROM
. . . . . . . . . . . . . . . .> (SELECT name,MIN(value) AS score_min,AVG(value) score_avg FROM ss_view GROUP BY name) mi LEFT JOIN ss_view
. . . . . . . . . . . . . . . .> ON ss_view.name=mi.name AND ss_view.value=mi.score_min
. . . . . . . . . . . . . . . .> ) t2 ON t1.name=t2.name;
+-------+--------------+------------+--------------+------------+--------------------+
| name  | subject_max  | score_max  | subject_min  | score_min  |     score_avg      |
+-------+--------------+------------+--------------+------------+--------------------+
| bob   | English      | 90         | Math         | 60         | 76.66666666666667  |
| john  | Chinese      | 90         | English      | 70         | 80.0               |
| sara  | Chinese      | 99         | English      | 60         | 74.66666666666667  |
| ted   | English      | 96         | Chinese      | 88         | 91.33333333333333  |
+-------+--------------+------------+--------------+------------+--------------------+
4 rows selected (110.515 seconds)

```


```sql
-- 1 explode
-- select explode(arraycol) as newcol from tablename;
-- select explode(mapcol) as (keyname,valuename) from tablename;

-- 例如前面曾通过 collect_set 获取就是name的数组集合
0: jdbc:hive2://localhost:10000> SELECT collect_set(name) as list FROM people;
+-----------------------+
|         list          |
+-----------------------+
| ["bob","john","ted"]  |
+-----------------------+
1 row selected (10.179 seconds)
-- 通过 explode 将上面的列数据转换为行数据
0: jdbc:hive2://localhost:10000> SELECT explode(collect_set(name)) as list_row FROM people;
+-----------+
| list_row  |
+-----------+
| bob       |
| john      |
| ted       |
+-----------+
3 rows selected (11.446 seconds)

-- 2 lateral view
--  有时使用UDTF不能带其它字段信息，如果需要则可以使用这个函数
--  lateral view是Hive中提供给UDTF的结合，它可以解决UDTF不能添加额外的select列的问题。
--  其实就是为了和UDTF关联使用的。
--  lateral view udtf(expression) tableAlias as columnAlias (,columnAlias)*



-- 在次添加我们自定义函数的jar包资源
0: jdbc:hive2://localhost:10000> ADD JAR hdfs:/app/lib/zodiac.jar;

-- 创建一个临时的UDTF函数 forx
0: jdbc:hive2://localhost:10000> CREATE TEMPORARY FUNCTION forx  AS 'yore.GenericUDTFFor';

-- 查看自定义函数 forx 说明文档信息
0: jdbc:hive2://localhost:10000> DESCRIBE FUNCTION EXTENDED forx ;

-- 使用自定义的UDTF函数 forx 查询数据，使用UTF使用的那个表 people
0: jdbc:hive2://localhost:10000> SELECT forx(1, 5) AS i from people where name='ted';
+----+
| i  |
+----+
| 1  |
| 2  |
| 3  |
| 4  |
| 1  |
| 2  |
| 3  |
| 4  |
+----+
8 rows selected (0.18 seconds)

```


# 4 Hive中序列
```bash
hadoop fs -put /opt/cloudera/parcels/CDH-6.2.0-1.cdh6.2.0.p0.967373/lib/hive/contrib/hive-contrib-2.1.1-cdh6.2.0.jar /app/udf-lib/
```
```sql
add jar hdfs:///app/udf-lib/hive-contrib-2.1.1-cdh6.2.0.jar;
create temporary function row_sequence as 'org.apache.hadoop.hive.contrib.udf.UDFRowSequence'; 

SELECT row_number() over(order by id),id,rank,quote FROM quote LIMIT 10;
SELECT row_sequence(),id,rank,quote FROM quote LIMIT 10;

```


