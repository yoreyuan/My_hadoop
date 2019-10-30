[Apache Hive](http://hive.apache.org/)
========


# 1 deploy
## 1.1 安装之前
* [Mysql](https://blog.csdn.net/github_39577257/article/details/77433996) 
* [Hadoop](apache-hadoop.md)

## 1.2 下载
```bash
 wget http://archive.apache.org/dist/hive/hive-3.1.1/apache-hive-3.1.1-bin.tar.gz
```

## 1.3 解压
```bash
tar -zxf apache-hive-3.1.1-bin.tar.gz -C /opt/
```

## 1.4 配置Hive环境变量
```bash
vim ~/.bash_profile
```
添加如下配置，保存并推出。
```bash
#Hive配置
export HIVE_HOME=/opt/apache-hive-3.1.1-bin
export PATH=$PATH:$HIVE_HOME/bin
```

并使配置生效
```bash
. ~/.bash_profile
```

## 1.5 创建Hive的元数据库
以MySQL为例，在MySQL服务器上执行，如下命令创建一个库，用来放置Hive的元数据信息
```bash
mysql -u <username> -e "create database metastore" -p
```

添加Mysql驱动包到Hive的lib
```bash
wget https://repo1.maven.org/maven2/mysql/mysql-connector-java/5.1.47/mysql-connector-java-5.1.47.jar -P $HIVE_HOME/lib/

```

## 1.6 释放配置文件
将模板配置文件复制为配置文件
```bash
cd $HIVE_HOME/conf
cp hive-env.sh.template hive-env.sh
cp hive-default.xml.template hive-site.xml
cp hive-log4j2.properties.template hive-log4j2.properties
cp beeline-log4j2.properties.template beeline-log4j2.properties
cp llap-cli-log4j2.properties.template llap-cli-log4j2.properties
cp hive-exec-log4j2.properties.template hive-exec-log4j2.properties
cp llap-daemon-log4j2.properties.template llap-daemon-log4j2.properties
```

## 1.7 修改hive-env.sh配置文件
```bash
vim $HIVE_HOME/conf/hive-env.sh
```

如下配置，保存退出。
```bash
export JAVA_HOME=/usr/local/zulu8
export HADOOP_HOME=/opt/hadoop-3.1.2
export HIVE_HOME=/opt/apache-hive-3.1.1-bin
export HIVE_CONF_DIR=${HIVE_HOME}/conf
```

## 1.8 修改 hive-site.xml
```bash
vim $HIVE_HOME/conf/hive-site.xml
```

修改如下配置内容
```xml
<configuration>
  <!-- 远程服务HiveServer2绑定的IP -->
  <property>
    <name>hive.server2.thrift.bind.host</name>
    <value>cdh6</value>
    <description>Bind host on which to run the HiveServer2 Thrift service.</description>
  </property>
    <property>
      <name>hive.metastore.uris</name>
      <!-- <value/> -->
      <value>thrift://cdh6:9083</value>
      <description>Thrift URI for the remote metastore. Used by metastore client to connect to remote metastore.</description>
    </property>
  <!-- 配置数据库连接，用来存储数据库元信息 -->
  <property>
    <name>javax.jdo.option.ConnectionURL</name>
    <!--<value>jdbc:derby:;databaseName=metastore_db;create=true</value>-->
      <value>jdbc:mysql://cdh1:3306/hive_metastore?createDatabaseIfNotExist=true&amp;useUnicode=true&amp;characterEncoding=utf8&amp;useSSL=false</value>
    <description>
      JDBC connect string for a JDBC metastore.
      To use SSL to encrypt/authenticate the connection, provide database-specific SSL flag in the connection URL.
      For example, jdbc:postgresql://myhost/db?ssl=true for postgres database.
    </description>
  </property>
  <!-- 配置mysql数据库驱动名称 -->
  <property>
    <name>javax.jdo.option.ConnectionDriverName</name>
    <!--<value>org.apache.derby.jdbc.EmbeddedDriver</value>-->
    <value>com.mysql.jdbc.Driver</value>
    <description>Driver class name for a JDBC metastore</description>
  </property>
  <!-- Mysql数据库用户名 -->
  <property>
    <name>javax.jdo.option.ConnectionUserName</name>
    <!--<value>APP</value>-->
    <value>root</value>
    <description>Username to use against metastore database</description>
  </property>
  <!-- Mysql数据库登陆密码 -->
  <property>
    <name>javax.jdo.option.ConnectionPassword</name>
     <!--<value>mine</value>-->
     <value>123456</value>
    <description>password to use against metastore database</description>
  </property>
  <!-- 启动时自动建表 -->
  <property>
    <name>datanucleus.schema.autoCreateAll</name>
    <value>true</value>
    <description>Auto creates necessary schema on a startup if one doesn't exist. Set this to false, after creating it once.To enable auto create also set hive.metastore.schema.verification=false. Auto creation is not recommended for production use cases, run schematool command instead.</description>
  </property>
  <property>
    <name>hive.metastore.schema.verification</name>
    <value>false</value>
    <description>
      Enforce metastore schema version consistency.
      True: Verify that version information stored in is compatible with one from Hive jars.  Also disable automatic
            schema migration attempt. Users are required to manually migrate schema after Hive upgrade which ensures
            proper metastore schema migration. (Default)
      False: Warn if the version information stored in metastore doesn't match with one from in Hive jars.
    </description>
  </property>  
  <!--Hive的job临时空间-->
  <property>
    <name>hive.exec.local.scratchdir</name>
    <!-- <value>${system:java.io.tmpdir}/${system:user.name}</value> -->
	<value>/tmp/hive/exec/${user.name}</value>
    <description>Local scratch space for Hive jobs</description>
  </property>
  <property>
    <name>hive.downloaded.resources.dir</name>
    <!-- <value>${system:java.io.tmpdir}/${hive.session.id}_resources</value> -->
	<value>/tmp/hive/${hive.session.id}_resources</value>
    <description>Temporary local directory for added resources in the remote file system.</description>
  </property>
  <property>
    <name>hive.querylog.location</name>
    <!-- <value>${system:java.io.tmpdir}/${system:user.name}</value> -->
	<value>/tmp/hive/log</value>
    <description>Location of Hive run time structured log file</description>
  </property>
  <property>
    <name>hive.server2.logging.operation.log.location</name>
    <!-- <value>${system:java.io.tmpdir}/${system:user.name}/operation_logs</value> -->
	<value>/tmp/hive/server2/${user.name}/operation_logs</value>
    <description>Top level directory where operation logs are stored if logging functionality is enabled</description>
  </property>
  <!--配置执行动态分区的模式。nonstrict：不严格模式；strict：严格模式-->
  <property>
    <name>hive.exec.dynamic.partition.mode</name>
    <value>nonstrict</value>
    <description>
      In strict mode, the user must specify at least one static partition
      in case the user accidentally overwrites all partitions.
      In nonstrict mode all partitions are allowed to be dynamic.
    </description>
  </property>
  
  <property>
    <name>hive.server2.authentication</name>
    <value>NONE</value>
    <description>
      Expects one of [nosasl, none, ldap, kerberos, pam, custom].
      Client authentication types.
        NONE: no authentication check
        LDAP: LDAP/AD based authentication
        KERBEROS: Kerberos/GSSAPI authentication
        CUSTOM: Custom authentication provider
                (Use with property hive.server2.custom.authentication.class)
        PAM: Pluggable authentication module
        NOSASL:  Raw transport
    </description>
  </property>
  <property>
    <name>hive.server2.thrift.client.user</name>
    <!--<value>anonymous</value>-->
    <value>hive</value>
    <description>Username to use against thrift client</description>
  </property>
  <property>
    <name>hive.server2.thrift.client.password</name>
    <!--<value>anonymous</value>-->
    <value>hive</value>
    <description>Password to use against thrift client</description>
  </property>
</configuration>
```

**注意**：这个版本的`hive-site.xml`中的`hive.txn.xlock.iow`配置项的注释有乱码字符，在进行下一步之前必须删除掉那个字符。

## 1.9 初始化 hive
这一步主要初始化Hive的元数据库
```bash
$HIVE_HOME/bin/schematool -dbType mysql -initSchema
```

## 1.10 启动Hive服务
如果需要远程连接，这两个服务必须开启，比如JDBC、数据库工具、beeline等，使用Griffin时必须开启`metastore`服务。
```bash
hive --service metastore >/dev/null 2>&1 &
hive --service hiveserver2 >/dev/null 2>&1 &

```

## 1.11 错误处理
当插入数据时，可能会报如下错误
```sql
hive> insert into person values(102, "小兰", 20);
Query ID = root_20190717170508_b7f5fe15-d8e0-44b9-9999-3127a0597044
Total jobs = 3
Launching Job 1 out of 3
Number of reduce tasks determined at compile time: 1
In order to change the average load for a reducer (in bytes):
  set hive.exec.reducers.bytes.per.reducer=<number>
In order to limit the maximum number of reducers:
  set hive.exec.reducers.max=<number>
In order to set a constant number of reducers:
  set mapreduce.job.reduces=<number>
Starting Job = job_1563341223085_0002, Tracking URL = http://cdh6:8088/proxy/application_1563341223085_0002/
Kill Command = /opt/hadoop-3.1.2/bin/mapred job  -kill job_1563341223085_0002
Hadoop job information for Stage-1: number of mappers: 0; number of reducers: 0
2019-07-07 17:05:15,928 Stage-1 map = 0%,  reduce = 0%
Ended Job = job_1563341223085_0002 with errors
Error during job, obtaining debugging information...
FAILED: Execution Error, return code 2 from org.apache.hadoop.hive.ql.exec.mr.MapRedTask
MapReduce Jobs Launched:
Stage-Stage-1:  HDFS Read: 0 HDFS Write: 0 FAIL
Total MapReduce CPU Time Spent: 0 msec
```

因为资源的限制Map阶段无法完成，可以在执行之前设置下如下两个值
```sql
hive> set mapreduce.map.memory.mb=1025;
hive> set mapreduce.reduce.memory.mb=1025;
```

如果还未解决，可以查看下Hadoop的Yarn，保证RM可以正常执行。

## 1.12 Web Ui
访问 Hive Server 2 的 webui，其端口为`hive-site.xml`的`hive.server2.webui.port`值：  [http://cdh6:10002](http://cdh6:${hive.server2.webui.port})

这里可以看到最近25条执行的SQL，也可以看到Hive的[日志](http://cdh6:10002/logs/hive.log)信息

## 1.13 关闭
这里主要关闭的： metastore 、hiveserver2
```bash
kill -9 `ps -ef | grep metastore |grep -v grep |awk '{print $2}'`
kill -9 `ps -ef | grep hiveserver2 |grep -v grep  |awk '{print $2}'`
```


# 2 使用
## 2.1 HiveSQ中注释中文乱码问题的解决
```sql
create table test (  
id bigint comment '主键ID',  
name string comment '名称',
load_time timestamp,
num double comment '数字'  
)ROW FORMAT DELIMITED FIELDS TERMINATED BY '\t'
STORED AS TEXTFILE;

--查看表，发现有中文乱码问题
hive> desc test;
OK
id                      bigint                  ??ID
name                    string                  ??
num                     double                  ??
Time taken: 0.173 seconds, Fetched: 3 row(s)

```

问题解决：
```sql
-- 登陆MySQL元数据库
mysql> use metastore;

--查看COLUMNS_V2表，发现COMMENT列就是中文乱码的
mysql> select * from COLUMNS_V2;
+-------+---------+-------------+-----------+-------------+
| CD_ID | COMMENT | COLUMN_NAME | TYPE_NAME | INTEGER_IDX |
+-------+---------+-------------+-----------+-------------+
|   287 | ??ID    | id          | bigint    |           0 |
|   287 | ??      | name        | string    |           1 |
|   287 | ??      | num         | double    |           2 |
+-------+---------+-------------+-----------+-------------+
3 rows in set (0.00 sec)

--查看COLUMNS_V2建表语句，可以发现  ENGINE=InnoDB DEFAULT CHARSET=latin1 ，使用的是 latin1 字符，
mysql> show create table COLUMNS_V2;
--因此我们将该表的字符改为utf8
alter table COLUMNS_V2 modify column COMMENT varchar(256) character set utf8; 
-- 同时也把如下的表也改为utf8
alter table TABLE_PARAMS modify column PARAM_VALUE varchar(4000) character set utf8; 
alter table PARTITION_KEYS modify column PKEY_COMMENT varchar(4000) character set utf8; 

```

测试
```sql
--Hive中插入一条数据
insert into table test values(1101, "hive组件", "2019-09-06 17:03:22",2.1);

```


## 2.2 加载本地文件系统数据创建一个表
将数据文件data/teacher.txt上传的hive的本地某个文件夹下，例如/home 下
```sql
-- 创建一个学生表(内表),并插入数据
CREATE TABLE STUDENT(SNO VARCHAR(3),SNAME VARCHAR(4),SSEX VARCHAR(2),SBIRTHDAY DATE,CLASS VARCHAR(5))
ROW FORMAT DELIMITED FIELDS TERMINATED BY '\t';
LOAD DATA LOCAL INPATH '/home/student.txt' OVERWRITE INTO TABLE STUDENT;

```

## 2.3 加载HDFS数据创建一个表
已经将student.csv数据上传到HDFS的/home 下。
```sql
-- 创建一个学生表(内表),并插入数据
CREATE TABLE STUDENT(SNO VARCHAR(3),SNAME VARCHAR(4),SSEX VARCHAR(2),SBIRTHDAY DATE,CLASS VARCHAR(5))
ROW FORMAT DELIMITED FIELDS TERMINATED BY ',';
LOAD DATA INPATH '/home/student.csv' OVERWRITE INTO TABLE STUDENT;

```


```bash
#查找CDH中的包 hive-contrib-*.jar。例如CDH 6.3.0的这个包在 /opt/cloudera/parcels/CDH-6.2.0-1.cdh6.2.0.p0.967373/lib/hive/contrib/hive-contrib-2.1.1-cdh6.2.0.jar
find / -name hive-contrib-*.jar

#可以上传到HDFS上（路径使用 hdfs://${namenode}:8020/），也可以本地(file:///) 
```

## 2.4 关于分隔符多字符的支持
### 2.4.1 通过使用加载Hive自带的工具类
默认情况下，Hive对于分隔符只支持单字符，不过Hive自带一个工具jar包，这个包支持正则和多字符方式定义分隔符。

* 1 查找hive自带的工具jar包位置
```bash
find / -name hive-contrib-*.jar
```


* 2 将上面搜索到的jar包配置到配置hive-site.xml文件中
```xml
<!-- Apache 原生Hive版 -->
<property>
  <name>hive.aux.jars.path</name>
  <value>file:///opt/apache-hive-1.2.2-bin/lib/hive-contrib-1.2.2.jar</value>
  <description>Added by tiger.zeng on 20120202.These JAR file are available to all users for all jobs</description>
</property>

<!-- CDH Hive版 -->
<!--<property>
  <name>hive.aux.jars.path</name>
  <value>file:///opt/cloudera/parcels/CDH-6.2.0-1.cdh6.2.0.p0.967373/lib/hive/contrib/hive-contrib-2.1.1-cdh6.2.0.jar</value>
  <description>Added by tiger.zeng on 20120202.These JAR file are available to all users for all jobs</description>
</property>-->

```



上面配置之后可以不用重启Hive服务，只需要重新进入Hive CLI就可生效，且是永久的。也可以配置为临时的，就是在进入Hive CLI后，临时加载这个jar包，执行如下：
```sql
hive> add jar file:///opt/apache-hive-1.2.2-bin/lib/hive-contrib-1.2.2.jar
```

准备如下数据，分隔符为 |#|，
```text
3324|#|003|#|20190816 09:16:18|#|0.00|#|2017-11-13 12:00:00
3330|#|009|#|20190817 15:21:03|#|1234.56|#|2017-11-14 12:01:00
```

建表时如下声明与定义如下，并加载数据，查询数据：
```sql
-- 1 建表
-- 如果没有上一步设置，需要手动临时导入 
-- add jar file:///opt/cloudera/parcels/CDH-6.2.0-1.cdh6.2.0.p0.967373/lib/hive/contrib/hive-contrib-2.1.1-cdh6.2.0.jar;
-- 例如使用 |#| 作为字段分割符，建表语句如下
CREATE  TABLE  split_test(
id   INT COMMENT '借阅查询ID',
number   STRING COMMENT '流水号',
`date`   STRING COMMENT '查询返回日期',
loanamount   DOUBLE COMMENT '借款金额范围',
createtime   TIMESTAMP COMMENT '创建时间'
)ROW FORMAT SERDE 'org.apache.hadoop.hive.contrib.serde2.MultiDelimitSerDe'
WITH SERDEPROPERTIES ("field.delim"="|#|")
STORED AS TEXTFILE;

-- 2 加载数据
LOAD DATA LOCAL INPATH '/root/split_test.txt'  OVERWRITE INTO TABLE split_test;

-- 3 查询结果如下：
hive> select * from split_test;
OK
3324    003     20190816 09:16:18       0.0     2017-11-13 12:00:00
3330    009     20190817 15:21:03       1234.56 2017-11-14 12:01:00
Time taken: 0.11 seconds, Fetched: 2 row(s)

```

### 2.4.2 通过修改源码自定义
自定义分隔符及多字符分隔符的问题可参阅我的blog [Hive中的自定义分隔符](https://blog.csdn.net/github_39577257/article/details/89020980) 中第三节部分。

<br/>

## 2.5 关于 Hive 中的 UPDATE 和 DELETE

### 2.5.1 通过改造SQL的方式

这里通过Impala SHELL方式（`impala-shell` 命令进入）进行操作，Impala也是可以对Hive的数据进行操作的，因为它们两个是共用的同一个元数据信息的，
如果在Impala发现表或数据没有同步，可以手动在impala-shell执行`invalidate metadata;`通过Hive的元数据到 Impala。
```sql
-- 1 先查看建表语句信息
[cdh3:21000] default> SHOW CREATE TABLE tmp_test;
Query: SHOW CREATE TABLE tmp_test
+----------------------------------------------------------------------+
| result                                                               |
+----------------------------------------------------------------------+
| CREATE TABLE default.tmp_test (                                      |
|   id INT,                                                            |
|   name STRING                                                        |
| )                                                                    |
| ROW FORMAT DELIMITED FIELDS TERMINATED BY ','                        |
| WITH SERDEPROPERTIES ('field.delim'=',', 'serialization.format'=',') |
| STORED AS TEXTFILE                                                   |
| LOCATION 'hdfs://cdh1:8020/user/hive/warehouse/tmp_test'    |
|                                                                      |
+----------------------------------------------------------------------+
Fetched 1 row(s) in 0.07s

-- 2 如不过没有数据可以先插入数据
[cdh3:21000] default> INSERT INTO tmp_test VALUES(0, 'zore');
-- 也可以一次插入多行数据
[cdh3:21000] default> INSERT INTO tmp_test VALUES(1, 'one'), (2, 'two'), (3, 'three');

-- 3 查询数据
[cdh3:21000] default> SELECT * FROM tmp_test;
+----+-------+
| id | name  |
+----+-------+
| 3  | three |
| 1  | one   |
| 2  | two   |
| 0  | zore  |
+----+-------+
Fetched 4 row(s) in 0.18s

-- 4 更新一条数据。将id=2的name值改为TWO
[cdh3.ygbx.com:21000] default> INSERT OVERWRITE TABLE tmp_test
                             > SELECT * FROM
                             > (SELECT id,"TWO" as name FROM tmp_test WHERE id=2
                             > UNION ALL
                             > SELECT * FROM tmp_test WHERE id <> 2) T;
Modified 4 row(s) in 0.32s
-- 查看数据。发现id=2的值已经改为了TWO，更新数据成功。
[cdh3.ygbx.com:21000] default> SELECT * FROM tmp_test;
+----+-------+
| id | name  |
+----+-------+
| 1  | one   |
| 3  | three |
| 0  | zore  |
| 2  | TWO   |
+----+-------+
Fetched 4 row(s) in 0.11s

-- 5 删除数据 
--  5.1 删除一条数据。我们删除一条id=0的数据
[cdh3.ygbx.com:21000] default> INSERT OVERWRITE TABLE tmp_test
                             > SELECT * FROM tmp_test WHERE id <> 0;
Modified 3 row(s) in 0.31s
--  查看数据，发现id=0的数据已经不再表中了（被删除了）
[cdh3.ygbx.com:21000] default>  SELECT * FROM tmp_test;
+----+-------+
| id | name  |
+----+-------+
| 2  | TWO   |
| 1  | one   |
| 3  | three |
+----+-------+
Fetched 3 row(s) in 0.21s

--  5.2 删除多条数据。我们删除 id 为 1 和 2的数据
[cdh3.ygbx.com:21000] default> INSERT OVERWRITE TABLE tmp_test
                             > SELECT * from tmp_test WHERE id not in (1,2);
Modified 1 row(s) in 0.21s
--  查看数据，发现id=1和id=2的两条数据同时被删除，只留下id=3的数据
[cdh3.ygbx.com:21000] default> SELECT * FROM tmp_test;
+----+-------+
| id | name  |
+----+-------+
| 3  | three |
+----+-------+
Fetched 1 row(s) in 0.21s

```

### 2.5.2 通过修改Hive配置文件的方式
修改hive-site.xml文件下面几项配置
```xml
<propertys>
<!-- 在右更新操作时务必设置为false。因为这会生成更高效的执行计划。 
https://cwiki.apache.org/confluence/display/Hive/LanguageManual+DML#LanguageManualDML-Update -->
  <property>
    <name>hive.optimize.sort.dynamic.partition</name>
    <value>false</value>
    <description>
      When enabled dynamic partitioning column will be globally sorted.
      This way we can keep only one record writer open for each partition value
      in the reducer thereby reducing the memory pressure on reducers.
    </description>
  </property>
  <property>
    <name>hive.support.concurrency</name>
    <value>true</value>
    <description>
      Whether Hive supports concurrency control or not. 
      A ZooKeeper instance must be up and running when using zookeeper Hive lock manager 
    </description>
  </property>
  <!--配置执行动态分区的模式。nonstrict：不严格模式；strict：严格模式-->
  <property>
    <name>hive.exec.dynamic.partition.mode</name>
    <value>nonstrict</value>
    <description>
      In strict mode, the user must specify at least one static partition
      in case the user accidentally overwrites all partitions.
      In nonstrict mode all partitions are allowed to be dynamic.
    </description>
  </property>
<property>
    <name>hive.txn.manager</name>
	<!--<value>org.apache.hadoop.hive.ql.lockmgr.DummyTxnManager</value>-->
    <value>org.apache.hadoop.hive.ql.lockmgr.DbTxnManager</value>
    <description>
      Set to org.apache.hadoop.hive.ql.lockmgr.DbTxnManager as part of turning on Hive
      transactions, which also requires appropriate settings for hive.compactor.initiator.on,
      hive.compactor.worker.threads, hive.support.concurrency (true),
      and hive.exec.dynamic.partition.mode (nonstrict).
      The default DummyTxnManager replicates pre-Hive-0.13 behavior and provides
      no transactions.
    </description>
  </property>
  <property>
    <name>hive.compactor.initiator.on</name>
    <value>true</value>
    <description>
      Whether to run the initiator and cleaner threads on this metastore instance or not.
      Set this to true on one instance of the Thrift metastore service as part of turning
      on Hive transactions. For a complete list of parameters required for turning on
      transactions, see hive.txn.manager.
    </description>
  </property>
  <property>
    <name>hive.compactor.worker.threads</name>
    <value>1</value>
    <description>
      How many compactor worker threads to run on this metastore instance. Set this to a
      positive number on one or more instances of the Thrift metastore service as part of
      turning on Hive transactions. For a complete list of parameters required for turning
      on transactions, see hive.txn.manager.
      Worker threads spawn MapReduce jobs to do compactions. They do not do the compactions
      themselves. Increasing the number of worker threads will decrease the time it takes
      tables or partitions to be compacted once they are determined to need compaction.
      It will also increase the background load on the Hadoop cluster as more MapReduce jobs
      will be running in the background.
    </description>
  </property>
  
  <!-- 
    …… 
  -->

</propertys>
```



```sql
-- 如果创建的表都是ORC格式也可以直接在设置全局文件格式
-- SET hive.default.fileformat=ORC

-- 1 建表
--  在没有开启事务时会报如下错误：
--  Error: Error while compiling statement: FAILED: SemanticException [Error 10265]: This command is not allowed on an ACID table default.tmp_test with a non-ACID transaction manager. Failed command: CREATE TABLE tmp_test (
0: jdbc:hive2://cdh5:10000> CREATE TABLE tmp_test (
. . . . . . . . . . . . . > id INT,
. . . . . . . . . . . . . > name STRING
. . . . . . . . . . . . . > )ROW FORMAT DELIMITED FIELDS TERMINATED BY '║'
. . . . . . . . . . . . . > STORED AS ORC
. . . . . . . . . . . . . > TBLPROPERTIES('transactional'='true');


-- 2 如果表中没有数据的插入如下数据
0: jdbc:hive2://cdh5:10000> INSERT INTO tmp_test VALUES(0, 'zore'), (1, 'one'), (2, 'two'), (3, 'three');

```

我们先查看一下HDFS上这个表的数据存储的信息，如下图，可以看到开启事务之后，执行1条插入语句插入多行数据时，会自动在HDFS上改表的路径下生成一个数据目录delta_0000006_0000006_0000，
然后在数据目录下有一个记录文件ACID的VERSION的文件，同时还有一个分桶文件，这个分桶文件的格式是个ORC的文件，我们直接打开它是一个乱码形式的，
如果想看这个文件中的内容，可以Hive提供的工具进行查看。。
```bash
# 1 查看 orcfiledump 的帮助信息
[root@node5 opt]# hive --orcfiledump  --help
#usage ./hive orcfiledump [-h] [-j] [-p] [-t] [-d] [-r <col_ids>] [--recover] [--skip-dump] [--backup-path <new-path>] <path_to_orc_file_or_directory>
#  --json (-j)                 Print metadata in JSON format
#  --pretty (-p)               Pretty print json metadata output
#  --timezone (-t)             Print writer's time zone
#  --data (-d)                 Should the data be printed
#  --rowindex (-r) <col_ids> Comma separated list of column ids for which row index should be printed
#  --recover                   Recover corrupted orc files generated by streaming
#  --skip-dump                 Used along with --recover to directly recover files without dumping
#  --backup-path <new_path>  Specify a backup path to store the corrupted files (default: /tmp)
#  --help (-h)                 Print help message

# 2 以格式化 JSON 的方式打印 orc文件数据。
#   schema      为每一个字段做了编号，从1开始，编号为0的columnId中描述了整个表的字段定义。
#   stripeStatistics    这里是ORC文件中所有stripes的统计信息，其中有每个stripe中每个字段的min/max值，是否有空值等等。
#   fileStatistics 这里是整个文件中每个字段的统计信息，该表只有一个文件，也只有一个stripe。
#   stripes     这里列出了所有stripes的元数据信息，包括index data, row data和stripe footer。
[root@node5 opt]# hive --orcfiledump -j -p /user/hive/warehouse/tmp_test/delta_0000006_0000006_0000/bucket_00000

# 3 查看orc文件数据信息
[root@node5 opt]# hive --orcfiledump -d /user/hive/warehouse/tmp_test/delta_0000006_0000006_0000/bucket_00000 > bucket_00000.orc.txt
{"operation":0,"originalTransaction":6,"bucket":536870912,"rowId":0,"currentTransaction":6,"row":{"id":0,"name":"zore"}}
{"operation":0,"originalTransaction":6,"bucket":536870912,"rowId":1,"currentTransaction":6,"row":{"id":1,"name":"one"}}
{"operation":0,"originalTransaction":6,"bucket":536870912,"rowId":2,"currentTransaction":6,"row":{"id":2,"name":"two"}}
{"operation":0,"originalTransaction":6,"bucket":536870912,"rowId":3,"currentTransaction":6,"row":{"id":3,"name":"three"}}
________________________________________________________________________________________________________________________


```

接着上面的SQL继续执行
```sql
-- 3 查看当前表的数据
0: jdbc:hive2://cdh5:10000> SELECT * FROM tmp_test;
+--------------+----------------+
| tmp_test.id  | tmp_test.name  |
+--------------+----------------+
| 0            | zore           |
| 1            | one            |
| 2            | two            |
| 3            | three          |
+--------------+----------------+

-- 4 更新一条数据。将id=2的name值改为TWO。
-- 如果执行报如下错误，是Hadoop版本和Hive的版本比匹配，可以升级Hadoop版本
--  Error: Error while processing statement: FAILED: Execution Error, return code -101 from org.apache.hadoop.hive.ql.exec.StatsTask. org.apache.hadoop.fs.FileStatus.compareTo(Lorg/apache/hadoop/fs/FileStatus;)I (state=08S01,code=-101)
0: jdbc:hive2://cdh5:10000> UPDATE tmp_test SET name="TWO" WHERE id=2;
-- 查看数据。发现id=2的值已经改为了TWO，更新数据成功。
0: jdbc:hive2://cdh5:10000> SELECT * FROM tmp_test;
+--------------+----------------+
| tmp_test.id  | tmp_test.name  |
+--------------+----------------+
| 0            | zore           |
| 1            | one            |
| 3            | three          |
| 2            | TWO            |
+--------------+----------------+

-- 5 删除数据 
--  5.1 删除一条数据。我们删除一条id=0的数据
0: jdbc:hive2://cdh5:10000> DELETE FROM tmp_test WHERE id=0;
--  查看数据，发现id=0的数据已经不再表中了（被删除了）
0: jdbc:hive2://cdh5:10000> SELECT * FROM tmp_test;
+--------------+----------------+
| tmp_test.id  | tmp_test.name  |
+--------------+----------------+
| 1            | one            |
| 3            | three          |
| 2            | TWO            |
+--------------+----------------+

-- 在 HIVE 3.1.2版本中还不支持删除多条数据的语法：DELETE * FROM tmp_test WHERE id in (1,2);

```

