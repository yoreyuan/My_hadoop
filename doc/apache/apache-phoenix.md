Apache Phoenix
=========
Phoenix详细的信息可以查看： [官网](http://phoenix.apache.org/index.html) &nbsp; &nbsp; |  &nbsp; &nbsp; 
[apache/phoenix](https://github.com/apache/phoenix) &nbsp; &nbsp; |  &nbsp; &nbsp; 
[Download](http://archive.apache.org/dist/phoenix/) &nbsp; &nbsp; |  &nbsp; &nbsp; 
[Performance](http://phoenix.apache.org/performance.html) &nbsp; &nbsp; |  &nbsp; &nbsp; 
[支持的SQL语法](http://phoenix.apache.org/language/index.html)

<br/>

CDH6中集成的HBase版本为`2.1.0+cdh6.2.0` ,我们从官方的镜像资源下载列表中看到最新的CDH支持到`cdh5.14.2`的版本，这种版本的直接有一个`parcels`包，可以通过Cloudera Manager的parcel页面添加资源，来安装组件。

通过查看源码`git branch -a`可以看到 remotes/origin/**5.x-cdh6** 分支，但在源码编译安装时这个分支还不完整，编译出来时会缺少一些包，因此我们直接尝试安装`v5.0.0-HBase-2.0`版本的Phoenix，安装方式有两种，第一种直接下载官方编译好的 [apache-phoenix-5.0.0-HBase-2.0](http://archive.apache.org/dist/phoenix/apache-phoenix-5.0.0-HBase-2.0/)包`apache-phoenix-5.0.0-HBase-2.0-bin.tar.gz`；第二种是通过编译源码的方式来安装。这里我们直接使用源码编译来进行安装。

<br/>

# 目录

* 1 [Installation](#1 )
    - 1.1 [源码编译](#1.1 )
    - 1.2 [安装](#1.2)
    - 1.3 [Cloudera Manager 的配置](#1.3)
* 2 [Using](#2 )
    + 2.1 [NoClassDefFoundError解决](#2.1 )
    + 2.2 [原有HBase表的映射同步](#2.2 )
    + 2.3 [查看表信息和插入数据](#2.3 )
    + 2.4 [数据类型](#2.4 )
    + 2.5 [修改表信息](#2.5)
    + 2.6 [删除库](#2.6 )
    + 2.7 [JDBC](#2.7 )
        - 2.7.1 [新建一个Maven工程](#2.7.1 )
        - 2.7.2 [pom.xml](#2.7.2)
        - 2.7.3 [JDBC Client代码](#2.7.3 )
* 3 [工具](#3 )
    + 3.1 [DBeaver](#3.1)
    + 3.2 [SQuirrel SQL](#3.2 )
        - 3.2.1 [下载](#3.2.1)
        - 3.2.2 [解压和配置](#3.2.2)
        - 3.2.3 [启动](#3.2.3 )
        - 3.2.4 [配置 Phoenix 驱动](#3.2.4)
        - 3.2.5 [连接Phoenix](#3.2.5 )
        - 3.2.6 [查询](#3.2.6 )
* 4 [资料](#4 )

*******************

# 1 Installation
CDH6中集成的HBase版本为`2.1.0+cdh6.2.0` ,我们从官方的镜像资源下载列表中看到最新的CDH支持到`cdh5.14.2`的版本，这种版本的直接有一个`parcels`包，可以通过Cloudera Manager的parcel页面添加资源，来安装组件。

通过查看源码`git branch -a`可以看到 remotes/origin/**5.x-cdh6** 分支，但在源码编译安装时这个分支还不完整，编译出来时会缺少一些包，因此我们直接尝试安装`v5.0.0-HBase-2.0`版本的Phoenix，
安装方式有两种，第一种直接下载官方编译好的 [apache-phoenix-5.0.0-HBase-2.0](http://archive.apache.org/dist/phoenix/apache-phoenix-5.0.0-HBase-2.0/)包`apache-phoenix-5.0.0-HBase-2.0-bin.tar.gz`，
第二种是通过编译源码的方式来安装。这里我们直接使用源码编译来进行安装。

## 1.1 源码编译
```bash
# clone源码
git clone https://github.com/apache/phoenix.git
# 进入源码根目录
cd phoenix/
# 查看tag ,或者查看分支 git branch -a
git tag
# 选择一个版本，进入其分支，或者切换到某个分支：git checkout -b remotes/origin/5.x-cdh6
git checkout tags/v5.0.0-HBase-2.0
# 编译
mvn -T2C install -DskipTests
```

编译时报如果下错误`Phoenix Client`编译失败
```
[INFO] ------------------------------------------------------------------------
[INFO] Reactor Summary for Apache Phoenix 5.1.0-HBase-2.0-SNAPSHOT:
[INFO]
[INFO] Apache Phoenix ..................................... SUCCESS [ 43.165 s]
[INFO] Phoenix Core ....................................... SUCCESS [27:21 min]
[INFO] Phoenix - Pherf .................................... SUCCESS [04:06 min]
[INFO] Phoenix Client ..................................... FAILURE [14:53 min]
[INFO] Phoenix Server ..................................... SUCCESS [01:48 min]
[INFO] Phoenix Assembly ................................... SUCCESS [02:26 min]
[INFO] Phoenix - Tracing Web Application .................. SUCCESS [02:25 min]
[INFO] ------------------------------------------------------------------------
[INFO] BUILD FAILURE
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  43:20 min (Wall Clock)
[INFO] Finished at: 2019-07-07T01:52:02+08:00
[INFO] ------------------------------------------------------------------------
[ERROR] Failed to execute goal org.apache.maven.plugins:maven-shade-plugin:3.1.1:shade (default-shaded) on project phoenix-client: Error creating shaded jar: duplicate entry: LICENSE.txt -> [Help 1]
[ERROR]
[ERROR] To see the full stack trace of the errors, re-run Maven with the -e switch.
[ERROR] Re-run Maven using the -X switch to enable full debug logging.
[ERROR]
[ERROR] For more information about the errors and possible solutions, please read the following articles:
[ERROR] [Help 1] http://cwiki.apache.org/confluence/display/MAVEN/MojoExecutionException
[ERROR]
[ERROR] After correcting the problems, you can resume the build with the command
[ERROR]   mvn <goals> -rf :phoenix-client
```

重复的`LICENSE.txt`条目，那我们找的`Phoenix Client`的pom文件，搜`maven-shade-plugin`将其中的下面的注释掉
```
<!--
<transformer
    implementation="org.apache.maven.plugins.shade.resource.IncludeResourceTransformer">
  <resource>LICENSE.txt</resource>
  <file>${project.basedir}/../LICENSE</file>
</transformer>
-->
```

重新编译这个模块` mvn install -pl phoenix-client -am`，再次运行发现会报如下的错误
```
[INFO] ------------------------------------------------------------------------
[INFO] Reactor Summary for Apache Phoenix 5.1.0-HBase-2.0-SNAPSHOT:
[INFO]
[INFO] Apache Phoenix ..................................... SUCCESS [  5.270 s]
[INFO] Phoenix Core ....................................... SUCCESS [01:08 min]
[INFO] Phoenix - Pherf .................................... SKIPPED
[INFO] Phoenix Client ..................................... SKIPPED
[INFO] Phoenix Server ..................................... SKIPPED
[INFO] Phoenix Assembly ................................... FAILURE [  9.714 s]
[INFO] Phoenix - Tracing Web Application .................. SKIPPED
[INFO] ------------------------------------------------------------------------
[INFO] BUILD FAILURE
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  01:26 min (Wall Clock)
[INFO] Finished at: 2019-07-07T09:16:02+08:00
[INFO] ------------------------------------------------------------------------
[ERROR] Failed to execute goal org.apache.maven.plugins:maven-assembly-plugin:2.5.2:single (package-to-tar) on project phoenix-assembly: Failed to create assembly: Error creating assembly archive all: This archives contains unclosed entries. -> [Help 1]
[ERROR]
[ERROR] To see the full stack trace of the errors, re-run Maven with the -e switch.
[ERROR] Re-run Maven using the -X switch to enable full debug logging.
[ERROR]
[ERROR] For more information about the errors and possible solutions, please read the following articles:
[ERROR] [Help 1] http://cwiki.apache.org/confluence/display/MAVEN/MojoExecutionException
[ERROR]
[ERROR] After correcting the problems, you can resume the build with the command
[ERROR]   mvn <goals> -rf :phoenix-assembly
```
但我们可以看到`Apache Phoenix`是编译成功的，Phoenix Assembly编译失败，这个失败的部分主要是组织资源打包为一个tar.gz的包，因此这个错误可以暂时忽略，下面直接使用编译的包进行安装

如果编译时报如下错误：
```
[INFO] ------------------------------------------------------------------------
[INFO] BUILD FAILURE
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  01:14 min (Wall Clock)
[INFO] Finished at: 2019-07-15T13:57:32+08:00
[INFO] ------------------------------------------------------------------------
[ERROR] Failed to execute goal net.alchim31.maven:scala-maven-plugin:3.2.0:compile (scala-compile-first) on project phoenix-spark: wrap: org.apache.commons.exec.ExecuteException: Process exited with an error: 1 (Exit value: 1) -> [Help 1]
[ERROR]
[ERROR] To see the full stack trace of the errors, re-run Maven with the -e switch.
[ERROR] Re-run Maven using the -X switch to enable full debug logging.
[ERROR]
[ERROR] For more information about the errors and possible solutions, please read the following articles:
[ERROR] [Help 1] http://cwiki.apache.org/confluence/display/MAVEN/MojoExecutionException
[ERROR]
[ERROR] After correcting the problems, you can resume the build with the command
[ERROR]   mvn <goals> -rf :phoenix-spark
```
可以看到是`phoenix-spark`模块的`net.alchim31.maven:scala-maven-plugin:3.2.0`插件编译时发生了错误，我们可以将其原有的`scala-maven-plugin`注释掉，修改为如下，然后重新编译：
```xml
<plugin>
    <groupId>net.alchim31.maven</groupId>
    <artifactId>scala-maven-plugin</artifactId>
    <version>3.2.2</version>
    <executions>
        <execution>
            <goals>
                <goal>compile</goal>
                <goal>testCompile</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```


编译成功后如下：
```
[INFO] ------------------------------------------------------------------------
[INFO] Reactor Summary for Apache Phoenix 5.0.0-HBase-2.0:
[INFO]
[INFO] Apache Phoenix ..................................... SUCCESS [  9.616 s]
[INFO] Phoenix Core ....................................... SUCCESS [03:34 min]
[INFO] Phoenix - Flume .................................... SUCCESS [01:56 min]
[INFO] Phoenix - Kafka .................................... SUCCESS [01:33 min]
[INFO] Phoenix - Pig ...................................... SUCCESS [04:59 min]
[INFO] Phoenix Query Server Client ........................ SUCCESS [ 48.315 s]
[INFO] Phoenix Query Server ............................... SUCCESS [07:59 min]
[INFO] Phoenix - Pherf .................................... SUCCESS [  8.362 s]
[INFO] Phoenix - Spark .................................... SUCCESS [09:20 min]
[INFO] Phoenix - Hive ..................................... SUCCESS [57:28 min]
[INFO] Phoenix Client ..................................... SUCCESS [01:20 min]
[INFO] Phoenix Server ..................................... SUCCESS [ 37.906 s]
[INFO] Phoenix Assembly ................................... SUCCESS [ 19.902 s]
[INFO] Phoenix - Tracing Web Application .................. SUCCESS [  9.255 s]
[INFO] Phoenix Load Balancer .............................. SUCCESS [ 19.176 s]
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  01:01 h (Wall Clock)
[INFO] Finished at: 2019-07-07T11:09:10+08:00
[INFO] ------------------------------------------------------------------------
```

## 1.2 安装
将编译成功的`phoenix-assembly/target/phoenix-5.0.0-HBase-2.0.tar.gz`包拷贝到Phoenix的安装节点，然后解压
```bash
cp phoenix-assembly/target/phoenix-5.0.0-HBase-2.0.tar.gz /opt/
cd /opt
tar -zxf phoenix-5.0.0-HBase-2.0.tar.gz
cd phoenix-5.0.0-HBase-2.0
```

此时为了方便可以配置下环境变量
```bash
vim ~/.bash_profile
# 添加如下配置，并保存退出

export PHOENIX_HOME=/opt/phoenix-5.0.0-HBase-2.0
export PATH=$PATH:$PHOENIX_HOME/bin

```

接下来将Phoenix根目录下的`phoenix-5.0.0-HBase-2.0-*.jar` 包分发到每个RegionServer的lib下
```bash
scp phoenix-5.0.0-HBase-2.0-*.jar root@cdh3:/opt/cloudera/parcels/CDH-6.2.0-1.cdh6.2.0.p0.967373/lib/hbase/lib/
scp phoenix-5.0.0-HBase-2.0-*.jar root@cdh2:/opt/cloudera/parcels/CDH-6.2.0-1.cdh6.2.0.p0.967373/lib/hbase/lib/
scp phoenix-5.0.0-HBase-2.0-*.jar root@cdh1:/opt/cloudera/parcels/CDH-6.2.0-1.cdh6.2.0.p0.967373/lib/hbase/lib/
```

这里最好也将`phoenix-5.0.0-HBase-2.0-client.jar`拷贝到Phoenix安装的根目录下
```bash
cp phoenix-client/target/phoenix-5.0.0-HBase-2.0-client.jar $PHOENIX_HOME/
```

## 1.3 Cloudera Manager 的配置
登陆Cloudera Manager页面，然后修改HBase配置，进入配置页面后搜索`hbase-site.xml 的 HBase 服务高级配置代码段`，然后添加如下配置，然后重启 HBase 使配置生效。
```xml
<property>
<name>hbase.table.sanity.checks</name>
<value>false</value>
</property>
```



# 2 Using
进入Phoenix的命令行执行
```bash
# 后面跟的参数为HBase配置的ZK的地址
sqlline.py cdh2:2181
```

2.1 NoClassDefFoundError解决

如果进入Phoenix终端时报如下错误：
```
Error: org.apache.phoenix.exception.PhoenixIOException: org.apache.hadoop.hbase.DoNotRetryIOException: java.lang.NoClassDefFoundError: org/apache/htrace/Trace
        at org.apache.hadoop.hbase.ipc.RpcServer.call(RpcServer.java:469)
        at org.apache.hadoop.hbase.ipc.CallRunner.run(CallRunner.java:130)
        at org.apache.hadoop.hbase.ipc.RpcExecutor$Handler.run(RpcExecutor.java:324)
        at org.apache.hadoop.hbase.ipc.RpcExecutor$Handler.run(RpcExecutor.java:304)
Caused by: java.lang.NoClassDefFoundError: org/apache/htrace/Trace
        at org.apache.phoenix.coprocessor.BaseScannerRegionObserver$RegionScannerHolder.overrideDelegate(BaseScannerRegionObserver.java:222)
        at org.apache.phoenix.coprocessor.BaseScannerRegionObserver$RegionScannerHolder.nextRaw(BaseScannerRegionObserver.java:273)
        at org.apache.hadoop.hbase.regionserver.RSRpcServices.scan(RSRpcServices.java:3134)
        at org.apache.hadoop.hbase.regionserver.RSRpcServices.scan(RSRpcServices.java:3383)
        at org.apache.hadoop.hbase.shaded.protobuf.generated.ClientProtos$ClientService$2.callBlockingMethod(ClientProtos.java:42002)
        at org.apache.hadoop.hbase.ipc.RpcServer.call(RpcServer.java:413)
        ... 3 more
Caused by: java.lang.ClassNotFoundException: org.apache.htrace.Trace
        at java.net.URLClassLoader.findClass(URLClassLoader.java:382)
        at java.lang.ClassLoader.loadClass(ClassLoader.java:424)
        at sun.misc.Launcher$AppClassLoader.loadClass(Launcher.java:349)
        at java.lang.ClassLoader.loadClass(ClassLoader.java:357)
        ... 9 more (state=08000,code=101)
```

可以看到提示缺少`org.apache.htrace.Trace`类，因此我们需要将`htrace-core-3.2.0-incubating.jar`拷贝到HBase的lib下：
```bash
# CDH环境下已经有这个包了，这里直接将其拷贝到HBase的lib下（每个RegionServer）
# 如果没有可以直接下载这包到HBase的lib下： wget https://repo1.maven.org/maven2/org/apache/htrace/htrace-core/3.2.0-incubating/htrace-core-3.2.0-incubating.jar
cp /opt/cloudera/cm/lib/cdh5/htrace-core-3.2.0-incubating.jar  /opt/cloudera/parcels/CDH-6.2.0-1.cdh6.2.0.p0.967373/lib/hbase/lib/
```

**注意**： 如果依旧提示缺少这个包，则需要重启HBase服务

再次进入
```sql
[root@cdh3 phoenix-5.0.0-HBase-2.0]# bin/sqlline.py cdh2
Setting property: [incremental, false]
Setting property: [isolation, TRANSACTION_READ_COMMITTED]
issuing: !connect jdbc:phoenix:cdh2 none none org.apache.phoenix.jdbc.PhoenixDriver
Connecting to jdbc:phoenix:cdh2
SLF4J: Class path contains multiple SLF4J bindings.
SLF4J: Found binding in [jar:file:/opt/phoenix-5.0.0-HBase-2.0/phoenix-5.0.0-HBase-2.0-client.jar!/org/slf4j/impl/StaticLoggerBinder.class]
SLF4J: Found binding in [jar:file:/opt/cloudera/parcels/CDH-6.2.0-1.cdh6.2.0.p0.967373/jars/slf4j-log4j12-1.7.25.jar!/org/slf4j/impl/StaticLoggerBinder.class]
SLF4J: See http://www.slf4j.org/codes.html#multiple_bindings for an explanation.
Connected to: Phoenix (version 5.0)
Driver: PhoenixEmbeddedDriver (version 5.0)
Autocommit status: true
Transaction isolation: TRANSACTION_READ_COMMITTED
Building list of tables and columns for tab-completion (set fastconnect to true to skip)...
133/133 (100%) Done
Done
sqlline version 1.2.0
0: jdbc:phoenix:cdh2> !tables
+------------+--------------+-------------+---------------+----------+------------+----------------------------+-----------------+--------------+-----------------+---------------+---------------+-----------------+------------+-------------+----------------+-------------+
| TABLE_CAT  | TABLE_SCHEM  | TABLE_NAME  |  TABLE_TYPE   | REMARKS  | TYPE_NAME  | SELF_REFERENCING_COL_NAME  | REF_GENERATION  | INDEX_STATE  | IMMUTABLE_ROWS  | SALT_BUCKETS  | MULTI_TENANT  | VIEW_STATEMENT  | VIEW_TYPE  | INDEX_TYPE  | TRANSACTIONAL  | IS_NAMESPAC |
+------------+--------------+-------------+---------------+----------+------------+----------------------------+-----------------+--------------+-----------------+---------------+---------------+-----------------+------------+-------------+----------------+-------------+
|            | SYSTEM       | CATALOG     | SYSTEM TABLE  |          |            |                            |                 |              | false           | null          | false         |                 |            |             | false          | false       |
|            | SYSTEM       | FUNCTION    | SYSTEM TABLE  |          |            |                            |                 |              | false           | null          | false         |                 |            |             | false          | false       |
|            | SYSTEM       | LOG         | SYSTEM TABLE  |          |            |                            |                 |              | true            | 32            | false         |                 |            |             | false          | false       |
|            | SYSTEM       | SEQUENCE    | SYSTEM TABLE  |          |            |                            |                 |              | false           | null          | false         |                 |            |             | false          | false       |
|            | SYSTEM       | STATS       | SYSTEM TABLE  |          |            |                            |                 |              | false           | null          | false         |                 |            |             | false          | false       |
+------------+--------------+-------------+---------------+----------+------------+----------------------------+-----------------+--------------+-----------------+---------------+---------------+-----------------+------------+-------------+----------------+-------------+
```


## 2.2 原有HBase表的映射同步
Phoenix对表和列名都是区分大小写的，**如果不加双引号默认为大写**。通过上面可以看到HBase原有的表并没有列出来，这是因为 Phoenix 无法自动识别 HBase 中原有的表，所以需要将 HBase 中已有的做映射，才能够被 Phoenix 识别并操作。

```sql
--其实也就是使用CREATE语句重新创建表来映射
0: jdbc:phoenix:cdh2:2181> CREATE TABLE "m_info" (
. . . . . . . . . . . . .> "ROW" varchar primary key,
. . . . . . . . . . . . .> "cf1"."gc" double ,
. . . . . . . . . . . . .> "cf1"."id" bigint ,
. . . . . . . . . . . . .> "cf1"."jd" double ,
. . . . . . . . . . . . .> "cf1"."name" varchar ,
. . . . . . . . . . . . .> "cf1"."wd" double
. . . . . . . . . . . . .> )column_encoded_bytes=0;
1 row affected (7.282 seconds)

-- 查询数据
0: jdbc:phoenix:cdh2> select * from "m_info" ;
+------+-----------------------+-----------------------+-----------------------+-------+-------------------------+
| ROW  |          gc           |          id           |          jd           | name  |           wd            |
+------+-----------------------+-----------------------+-----------------------+-------+-------------------------+
| 2    | 1.1                   | 2                     | 3.33                  | 名字1  | 5.1234                  |
+------+-----------------------+-----------------------+-----------------------+-------+-------------------------+

```

## 2.3 表信息和插入数据
```sql
-- 查看表结构
0: jdbc:phoenix:cdh2:2181> !describe "m_info";
+------------+--------------+-------------+--------------+------------+------------+--------------+----------------+-----------------+-----------------+-----------+----------+-------------+----------------+-------------------+--------------------+-------------------+---+
| TABLE_CAT  | TABLE_SCHEM  | TABLE_NAME  | COLUMN_NAME  | DATA_TYPE  | TYPE_NAME  | COLUMN_SIZE  | BUFFER_LENGTH  | DECIMAL_DIGITS  | NUM_PREC_RADIX  | NULLABLE  | REMARKS  | COLUMN_DEF  | SQL_DATA_TYPE  | SQL_DATETIME_SUB  | CHAR_OCTET_LENGTH  | ORDINAL_POSITION  | I |
+------------+--------------+-------------+--------------+------------+------------+--------------+----------------+-----------------+-----------------+-----------+----------+-------------+----------------+-------------------+--------------------+-------------------+---+
|            |              | m_info      | ROW          | 12         | VARCHAR    | null         | null           | null            | null            | 0         |          |             | null           | null              | null               | 1                 | f |
|            |              | m_info      | gc           | 8          | DOUBLE     | null         | null           | null            | null            | 1         |          |             | null           | null              | null               | 2                 | t |
|            |              | m_info      | id           | -5         | BIGINT     | null         | null           | null            | null            | 1         |          |             | null           | null              | null               | 3                 | t |
|            |              | m_info      | jd           | 8          | DOUBLE     | null         | null           | null            | null            | 1         |          |             | null           | null              | null               | 4                 | t |
|            |              | m_info      | name         | 12         | VARCHAR    | null         | null           | null            | null            | 1         |          |             | null           | null              | null               | 5                 | t |
|            |              | m_info      | wd           | 8          | DOUBLE     | null         | null           | null            | null            | 1         |          |             | null           | null              | null               | 6                 | t |
+------------+--------------+-------------+--------------+------------+------------+--------------+----------------+-----------------+-----------------+-----------+----------+-------------+----------------+-------------------+--------------------+-------------------+---+

-- 插入数据。这里注意的是value中的值如果是字符串，必须用单引号
0: jdbc:phoenix:cdh2:2181> UPSERT INTO "m_info"("ROW", "gc", "id", "jd", "name", "wd") VALUES('2', 1.1, 2, 3.33, '名字1', 5.1234);
1 row affected (1.074 seconds)
```

## 2.4 数据类型
[Data Types](http://phoenix.apache.org/language/datatypes.html)

字段类型 | 映射的类 | 值的范围 | 占用字节 
:---- | :---- | ---- | ---- 
INTEGER | `java.lang.Integer` | -2147483648 到 2147483647  | 4字节
UNSIGNED_INT | `java.lang.Integer` | 0 到 2147483647 | 4字节
BIGINT | `java.lang.Long` | -9223372036854775808 到 9223372036854775807 | 8字节
UNSIGNED_LONG | `java.lang.Long` | 0 到 9223372036854775807 | 8字节
TINYINT | `java.lang.Byte` | -128 到 127 | 1字节
UNSIGNED_TINYINT | `java.lang.Byte` | 0 到 127 | 1字节
SMALLINT | `java.lang.Short` | -32768 到 32767 | 2字节
UNSIGNED_SMALLINT | `java.lang.Short` | 0 到 32767 | 2字节
FLOAT | `java.lang.Float` | -3.402823466 E + 38 到 3.402823466 E + 38 | 4字节
UNSIGNED_FLOAT | `java.lang.Float` | 0到3.402823466 E + 38 | 4字节
DOUBLE | `java.lang.Double` | -1.7976931348623158 E + 308 到 1.7976931348623158 E + 308 | 8字节
UNSIGNED_DOUBLE | `java.lang.Double` | 0 到 1.7976931348623158 E + 308 | 8字节
DECIMAL 或 DECIMAL(precisionInt , scaleInt) | `java.math.BigDecimal` | 最大精度为38位。可变长度 | 
BOOLEAN | `java.lang.Boolean` | 0表示false，1表示true | 1字节
TIME | `java.sql.Time` | 自纪元以来的毫秒数（基于时间GMT） | 8字节
UNSIGNED_TIME | `java.sql.Time` | 格式为 yyyy-MM-dd hh:mm:ss | 8字节
DATE | `java.sql.Date` | 自纪元以来的毫秒数（基于时间GMT） | 8字节
UNSIGNED_DATE | `java.sql.Date` | 格式为  yyyy-MM-dd hh:mm:ss | 8字节
TIMESTAMP | `java.sql.Timestamp` | 自纪元以来的毫秒数（基于时间GMT） | 12字节（纪元时间长8字节加纳秒的4字节整数）
UNSIGNED_TIMESTAMP | `java.sql.Timestamp` | 格式为 yyyy-MM-dd hh:mm:ss\[.nnnnnnnnn] | 12字节
VARCHAR 或 VARCHAR(precisionInt) | `java.lang.String` | 可选的最大字节长度 | 
CHAR(precisionInt) | `java.lang.String` | 固定长度字符串 | 
BINARY(precisionInt) | `byte[]` | 原始固定长度字节数组 | 
VARBINARY | `byte[]` | 原始可变长度字节数组 | 
ARRAY 或 ARRAY\[dimensionInt] | `java.sql.Array` |  | 


## 2.5 修改表信息
首先Phoenix对字段的映射的元数据信息如下，下面修改字段类型时可以由此进行修改

DATA_TYPE | TYPE_NAME
:---- | :----:
INTEGER | 4
UNSIGNED_INT | 4
BIGINT | -5
UNSIGNED_LONG | -5
TINYINT | -6
UNSIGNED_TINYINT | -6
SMALLINT | 5
UNSIGNED_SMALLINT | 5
FLOAT | 6
UNSIGNED_FLOAT | 6
DOUBLE | 8
UNSIGNED_DOUBLE | 8
DECIMAL | 3
BOOLEAN | 16
TIME | 92
UNSIGNED_TIME | 92
DATE | 91
UNSIGNED_DATE | 91
TIMESTAMP | 93
UNSIGNED_TIMESTAMP | 93
VARCHAR | 12
CHAR | 1
BINARY | -2
VARBINARY | -3

```sql
0: jdbc:phoenix:cdh2> CREATE TABLE "test" (
. . . . . . . . . . > "ROW" varchar primary key,
. . . . . . . . . . > "info1"."age" UNSIGNED_TINYINT ,
. . . . . . . . . . > "info1"."name" varchar ,
. . . . . . . . . . > "info1"."property" double
. . . . . . . . . . > )column_encoded_bytes=0;
3 rows affected (8.962 seconds)
-- 查看表中的字段信息
0: jdbc:phoenix:cdh2> select TENANT_ID,TABLE_SCHEM,TABLE_NAME,COLUMN_NAME,COLUMN_FAMILY,DATA_TYPE,COLUMN_SIZE,DECIMAL_DIGITS from SYSTEM.CATALOG where TABLE_NAME='test';
+------------+--------------+-------------+--------------+----------------+------------+--------------+-----------------+
| TENANT_ID  | TABLE_SCHEM  | TABLE_NAME  | COLUMN_NAME  | COLUMN_FAMILY  | DATA_TYPE  | COLUMN_SIZE  | DECIMAL_DIGITS  |
+------------+--------------+-------------+--------------+----------------+------------+--------------+-----------------+
|            |              | test        |              |                | null       | null         | null            |
|            |              | test        | ROW          |                | 12         | null         | null            |
|            |              | test        | age          | info1          | -6         | null         | null            |
|            |              | test        | name         | info1          | 12         | null         | null            |
|            |              | test        | property     | info1          | 8          | null         | null            |
+------------+--------------+-------------+--------------+----------------+------------+--------------+-----------------+
5 rows selected (0.064 seconds)
-- 将name的varchar类型的长度调整为 32，前4个字段为主键
5 rows selected (0.064 seconds)
0: jdbc:phoenix:cdh2> upsert into SYSTEM.CATALOG (TENANT_ID,TABLE_SCHEM,TABLE_NAME,COLUMN_NAME,COLUMN_FAMILY,COLUMN_SIZE) values('','','test','name','info1',32);
1 row affected (0.493 seconds)
0: jdbc:phoenix:cdh2> upsert into SYSTEM.CATALOG (TENANT_ID,TABLE_SCHEM,TABLE_NAME,COLUMN_NAME,COLUMN_FAMILY,DATA_TYPE) values('','','test','age','info1',-5);
1 row affected (0.019 seconds)
0: jdbc:phoenix:cdh2> select TENANT_ID,TABLE_SCHEM,TABLE_NAME,COLUMN_NAME,COLUMN_FAMILY,DATA_TYPE,COLUMN_SIZE,DECIMAL_DIGITS from SYSTEM.CATALOG where TABLE_NAME='test';
+------------+--------------+-------------+--------------+----------------+------------+--------------+-----------------+
| TENANT_ID  | TABLE_SCHEM  | TABLE_NAME  | COLUMN_NAME  | COLUMN_FAMILY  | DATA_TYPE  | COLUMN_SIZE  | DECIMAL_DIGITS  |
+------------+--------------+-------------+--------------+----------------+------------+--------------+-----------------+
|            |              | test        |              |                | null       | null         | null            |
|            |              | test        | ROW          |                | 12         | null         | null            |
|            |              | test        | age          | info1          | -5         | null         | null            |
|            |              | test        | name         | info1          | 12         | 32           | null            |
|            |              | test        | property     | info1          | 8          | null         | null            |
+------------+--------------+-------------+--------------+----------------+------------+--------------+-----------------+
5 rows selected (0.039 seconds)

```

## 2.6 删除库
```sql
-- 如果HBase的数据和Phoenix的数据是同步的，则可以直接删除SCHEM
0: jdbc:phoenix:cdh3> DROP SCHEMA IF EXISTS my_schema;

-- 如果不能删除，可以先删除HBase的表信息
hbase(main):003:0> disable "test.Person"
Took 0.5061 seconds
hbase(main):004:0> drop "test.Person"
Took 0.2915 seconds
-- 再清除Phoenix的数据
-- 查看CATALOG信息
0: jdbc:phoenix:cdh3> select TENANT_ID,TABLE_SCHEM,TABLE_NAME,COLUMN_NAME,COLUMN_FAMILY,TABLE_SEQ_NUM,TABLE_TYPE,PK_NAME from SYSTEM.CATALOG;
-- 删除里面的表信息
| TENANT_ID  | TABLE_SCHEM  | TABLE_NAME  |        COLUMN_NAME     | COLUMN_FAMILY  | TABLE_SEQ_NUM  | TABLE_TYPE  | PK_NAME  |
+------------+--------------+-------------+-------------------------------------+----------------+----------------+-------------+----------+
|            | ……           | ……          | ……                     |                | null           |             |          |
|            | test         | Person      |                        |                | 0              | u           |          |
|            | test         | Person      |                        | 0              | null           |             |          |
|            | test         | Person      | age                    | 0              | null           |             |          |
|            | test         | Person      | idcard_num             |                | null           |             |          |
|            | test         | Person      | name                   | 0              | null           |             |          |
+------------+--------------+-------------+------------------------+----------------+----------------+-------------+----------+
-- 删除SCHEM信息
0: jdbc:phoenix:cdh3> DELETE FROM SYSTEM.CATALOG WHERE TABLE_SCHEM='test';
5 rows affected (0.352 seconds)
-- 再次使用 !tables 查看表，发现库以及表均已经成功删除

```

## 2.7 JDBC
使用JDBC方式连接Phoenix，对数据进行处理

### 2.7.1 新建一个Maven工程

### 2.7.2 pom.xml
```xml
<dependencies>
    <!-- https://mvnrepository.com/artifact/org.apache.phoenix/phoenix-core -->
    <dependency>
        <groupId>org.apache.phoenix</groupId>
        <artifactId>phoenix-core</artifactId>
        <version>5.0.0-HBase-2.0</version>
    </dependency>
    <!-- https://mvnrepository.com/artifact/org.apache.hadoop/hadoop-common -->
    <dependency>
        <groupId>org.apache.hadoop</groupId>
        <artifactId>hadoop-common</artifactId>
        <version>3.0.0</version>
    </dependency>

    <dependency>
        <groupId>junit</groupId>
        <artifactId>junit</artifactId>
        <version>4.12</version>
    </dependency>
</dependencies>
```

### 2.7.3 JDBC Client代码
[PhoenixJdbcClient](../../hbase/phoenix-jdbc)



# 3 工具
以下两款常用的工具都支持Win和Mac OS

## 3.1 [DBeaver](https://dbeaver.io/)
在创建Phoenix连接，编辑驱动设那里点击 <kbd> 添加文件(F) </kbd> ，选中前面我们编译出来的`phoenix-5.0.0-HBase-2.0-client.jar`包即可。

![DBeaver](https://img-blog.csdnimg.cn/20190716144359160.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L2dpdGh1Yl8zOTU3NzI1Nw==,size_16,color_FFFFFF,t_70)


## 3.2 [SQuirrel SQL](http://www.squirrelsql.org/)
### 3.2.1 下载
进入这个页面下载 [http://squirrel-sql.sourceforge.net/](http://squirrel-sql.sourceforge.net/)，这里可以下载压缩包版的 [Plain zips the latest release for Windows/Linux/MacOS X/others](http://sourceforge.net/projects/squirrel-sql/files/1-stable/3.9.1-plainzip/)。
有三个版本，选在一个版本下载即可，例如这里下载[squirrelsql-3.9.1-optional.zip](https://sourceforge.net/projects/squirrel-sql/files/1-stable/3.9.1-plainzip/squirrelsql-3.9.1-optional.zip/download)

### 3.2.2 解压和配置
解压到安装目录，然后将`phoenix-5.0.0-HBase-2.0-client.jar`和[`phoenix-core-5.0.0-HBase-2.0.jar`](https://repo1.maven.org/maven2/org/apache/phoenix/phoenix-core/5.0.0-HBase-2.0/phoenix-core-5.0.0-HBase-2.0.jar)下载或者软连接到解压的包中的`lib`目录中。
```bash
Su-yuexi:squirrelsql-3.9.1-optional yoreyuan$ cd lib
Su-yuexi:lib yoreyuan$ ln -s ~/.dbeaver-drivers/phoenix-5.0.0-HBase-2.0-client.jar phoenix-5.0.0-HBase-2.0-client.jar
Su-yuexi:lib yoreyuan$ ln -s ~/soft/maven/repository/org/apache/phoenix/phoenix-core/5.0.0-HBase-2.0/phoenix-core-5.0.0-HBase-2.0.jar phoenix-core-5.0.0-HBase-2.0.jar
Su-yuexi:lib yoreyuan$ cd ../
Su-yuexi:squirrelsql-3.9.1-optional yoreyuan$ chmod +x squirrel-sql-mac.sh
```

### 3.2.3 启动
执行启动脚本，此时会弹出`SQuirrel SQL Client Help`和`SQuirrel SQL Client`窗口。如果安装或其它操作也可以查看帮助文档。
```bash
./squirrel-sql-mac.sh
```

### 3.2.4 配置 Phoenix 驱动
* 添加Driver。点击`Driver`
* Driver Name: 填写一个名字
* Example URL: `jdbc:phoenix:cdh2,cdh3:2181`
* 在`Java Class Path`选在我们前面添加的`lib/phoenix-core-5.0.0-HBase-2.0.jar`
* 点击`List Drivers`，等一会会自动在`Class Name：`补上`org.apache.phoenix.jdbc.PhoenixDriver`
* OK
* 配置成功后会在左侧的`Drivers`区域添加上去我们刚配置的Phoenix连接。

### 3.2.5 连接Phoenix
* 点击`Aliases`中的`➕`
* Name: 连接的别名
* Driver: 选择上一步配置好的驱动。URL会自动填入。
* Test。User Name 和 Password 可以不用填写。
* OK

![SQuirrel SQL Client - Aliases](https://img-blog.csdnimg.cn/20190716141841894.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L2dpdGh1Yl8zOTU3NzI1Nw==,size_16,color_FFFFFF,t_70)

### 3.2.6 查询
![squirrel sql 查询](https://img-blog.csdnimg.cn/20190716144128841.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L2dpdGh1Yl8zOTU3NzI1Nw==,size_16,color_FFFFFF,t_70)


# 4 资料
这里推荐一份 云栖社区的一份资料，可以参考

[Phoenix资料](https://yq.aliyun.com/articles/574090)


