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


## 2.1 加载本地文件系统数据创建一个表
将数据文件data/teacher.txt上传的hive的本地某个文件夹下，例如/home 下
```sql
-- 创建一个学生表(内表),并插入数据
CREATE TABLE STUDENT(SNO VARCHAR(3),SNAME VARCHAR(4),SSEX VARCHAR(2),SBIRTHDAY DATE,CLASS VARCHAR(5))
ROW FORMAT DELIMITED FIELDS TERMINATED BY '\t';
LOAD DATA LOCAL INPATH '/home/student.txt' OVERWRITE INTO TABLE STUDENT;

```

## 2.2 加载HDFS数据创建一个表
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

# hive-site.xml配置
```xml
<property>
  <name>hive.aux.jars.path</name>
  <value>file:///opt/cloudera/parcels/CDH-6.2.0-1.cdh6.2.0.p0.967373/lib/hive/contrib/hive-contrib-2.1.1-cdh6.2.0.jar</value>
  <description>Added by tiger.zeng on 20120202.These JAR file are available to all users for all jobs</description>
</property>
```

```sql
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

```
