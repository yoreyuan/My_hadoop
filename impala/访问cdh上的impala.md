200 服务器访问 CDH 权限和方法
--------

# 资料
[Cloudera Enterprise 6.0 Documentation](https://docs.cloudera.com/documentation/enterprise/6/6.0.html)
[New Features in CDH 6.0.0](https://docs.cloudera.com/documentation/enterprise/6/release-notes/topics/rg_cdh_600_new_features.html#impala_new_600)
[CDH - Impala]()


# 1 数据迁移
数据同步的工具有很多，比如Hadoop和结构化数据存储之间高效批量数据传输的工具Apache Sqoop，借助于 Hadoop集群可以并行的高效传输数据，
但是这种方式往往需要依赖于一个Hadoop环境，在生产环境有时我们并没有直接的权限操作这个环境，而是只提供一个HDFS的端口，这时用Sqoop就不是很方便。
Oracle数据和MySQL数据的同步工具会想到alibaba / yugong，基于MySQL binlog增量订阅和消费中间件使用起来非常方便，
同时也执行消息队里模式（Apache Kafka、Apache RockMQ），但这个主要针对于特定场景和业务下。


[DataX](https://github.com/alibaba/DataX#support-data-channels)是阿里巴巴集团内被广泛使用的离线数据同步工具or平台，
它支持MySQL、Oracle、SQLServer、PostgreSQL、HDFS、Hive、HBase、MaxCompute(ODPS)、MongoDB、Elasticsearch等各种异构数据源之间的高效的数据同步。
Reader和Writer支持的列表可以访问文档 [Support Data Channels](https://github.com/alibaba/DataX#support-data-channels)。

![Datax 异构架构图](https://imgconvert.csdnimg.cn/aHR0cHM6Ly9jbG91ZC5naXRodWJ1c2VyY29udGVudC5jb20vYXNzZXRzLzEwNjcxNzUvMTc4Nzk4NDEvOTNiN2ZjMWMtNjkyNy0xMWU2LThjZGEtN2NmODQyMGZjNjVmLnBuZw?x-oss-process=image/format,png)

因为团队中有使用 DataX 的经验，因此数据迁移我们沿用原先的方案，继续使用 DataX。

## 1.1 数据路径规范

## 1.2 需要申请的权限
在 CDH 环境默认 HDFS 的 NameNode 的端口 8020。数据迁移部分实质就是将不同数据源的数据写入到 HDFS 上，
例如 ` hdfs://${fs.defaultFS}:8020/${yourPath} `，因此需要申请能够访问 8020 端口的权限，主要用来写入数据。


# 2 业务执行
## 2.1 Hive
在 CDH 环境默认连接 hive 的端口为 10000 （这个端口为 Hive 配置文件 `hive.server2.thrift.port` 配置项的值）。
因此**需要申请访问 CDH Hive 服务的 10000 端口的权限**。

连接 Hive 的方式有：
* hive CLI 
* beeline
* JDBC

本次主要使用 beeline 为主，介绍下 SQL 的执行和 SQL 脚本的执行

### 2.1.1 测试数据
假设有如下表和数据
```sql
-- 1 查看表
hive> desc tmp_test;
OK
id                      int                 
name                    string 

-- 2 插入 3 条数据
insert into tmp_test values(1, "a"),(2, "b"), (3, "c");

```

### 2.1.2 方式一：执行 sql 语句 
```bash
# 参数说明如下
#  -n	username，连接 Hive 的用户名（如果未开启权限认证可以不写）
#  -p   password, 连接 Hive 的用户的密码（如果未开启权限认证可以不写）
#  -d   driver class, 连接 Hive 的 驱动类（无特殊情况下，可以选填）
#  -u   database url, 必填，连接 Hive 的 URL
#  --hiveconf  格式为 property=value ， 设置 Hive 属性值
#  --hivevar  格式为 name=value，配置会话级别的变量名和值，例如 --hivevar hive.security.authorization.enabled=false
#  -e   query, 执行的 查询语句
#  --help  查看帮助
beeline -n hive -p hive -d "org.apache.hive.jdbc.HiveDriver" -u "jdbc:hive2://cdh3:10000/default" --color=true \
--hiveconf mapreduce.job.queuename=datacenter  -e "select count(*) from hive_test.tmp_test"


#beeline -u "jdbc:hive2://cdh3:10000/default" \
#-e "select count(*) from hive_test.tmp_test"

``` 

### 2.1.3 方式二：sql 脚本
在 sql 脚本的目录下有 my-hive.sql 文件，文件中有如下 SQL
```sql
SELECT m.id,m.movie_name,m.rating_num,m.rating_people,q.rank,q.quote FROM movie m LEFT JOIN quote q ON q.id=m.id ORDER BY m.rating_num DESC,m.rating_people DESC LIMIT 10;
use hive_test;
select count(*) from tmp_test;

```

使用 beeline 执行 上面的 SQL 脚本
```bash
# 参数说明：
#  -f   执行的脚本文件
beeline -n hive -p hive -d "org.apache.hive.jdbc.HiveDriver" -u "jdbc:hive2://cdh3:10000/default" \
--hiveconf mapreduce.job.queuename=datacenter \
-f ./my-hive.sql

```




## 2.2 Impala
在 CDH 环境默认连接 hive 的端口为 21050 （配置文件的 Impala Daemon HiveServer2 的配置内容， 即配置项 hs2_port 的值）。
因此**需要申请访问 CDH Impala 服务的 21050 端口的权限**，
如果使用 impala-shell 方式远程执行，则需要开启 **21000** 端口（端口为 mpala Daemon Beeswax 的端口）

连接 Impala 的方式
* mpala-shell 
* beeline
* JDBC



### 2.1.1 mpala-shell 方式执行脚本

```bash
# 1 执行 sql 语句 
# 参数说明如下：
#   -u      认证的用户，默认为 root
#   -i      指定 Impala 的 IMPALAD 服务地址，默认为 localhost:21000，
#   -f      执行的查询 脚本文件
impala-shell -u impala -i cdh3:21000 \
-q "use impala_demo; SELECT id,movie_name,rating_num,rating_people,release_date FROM movie ORDER BY release_date DESC LIMIT 5;" 


# 2 sql 脚本

# 2.1 my-impala.sql 中有如下 sql
use impala_demo; 
SELECT id,movie_name,rating_num,rating_people,release_date FROM movie ORDER BY release_date DESC LIMIT 5;

# 2.2 执行
impala-shell -u impala -i cdh3:21000 \
-f ./my-impala.sql

```

### 2.1.2 beeline 执行 脚本 
使用这种方法，需要在 beeline服务环境下配置上 Impala的驱动包，例如在 CDH 环境可以配置如下，
```bash
# 1 下载驱动包
wget https://downloads.cloudera.com/connectors/impala_jdbc_2.5.41.1061.zip

# 2 解压到某一个临时的文件夹下
mkdir impala_drive
unzip impala_jdbc_2.5.41.1061.zip -d ./impala_drive/
# 查看  ./impala_drive/2.5.41.1061\ GA/ 可以看到有两个版本的 JDBC 驱动压缩包
unzip ./impala_drive/2.5.41.1061\ GA/Cloudera_ImpalaJDBC41_2.5.41.zip -d ./impala_drive/

# 3 拷贝第二步解压的两个 jar 包到指定目录（CDH 环境下）
cp ./impala_drive/ImpalaJDBC41.jar  /opt/cloudera/parcels/CDH/lib/hive/auxlib/
cp ./impala_drive/TCLIServiceClient.jar  /opt/cloudera/parcels/CDH/lib/hive/auxlib/

# 4 如果下载的驱动包不再使用，可以删除
rm -rf impala_drive/

```


```bash
# 1 执行 sql 语句 
#  如果有错误提示 Error: [Simba][JDBC](11975) Unsupported transaction isolation level: 4. (state=HY000,code=11975)
#  可以加上参数：--isolation=default 
beeline -n impala -d "com.cloudera.impala.jdbc41.Driver" -u "jdbc:impala://cdh3:21050/impala_demo" \
--color=true --isolation=TRANSACTION_SERIALIZABLE --incremental=false  \
-e "SELECT id,movie_name,rating_num,rating_people,release_date FROM movie ORDER BY release_date DESC LIMIT 5;"


# 2 sql 脚本
beeline -n impala -d "com.cloudera.impala.jdbc41.Driver" -u "jdbc:impala://cdh3:21050/impala_demo" \
--isolation=default \
-f ./my-impala.sql

``` 

<br/>

也可以访问我的 blog [Beeline 的进阶使用](https://blog.csdn.net/github_39577257/article/details/104645603)关于连接 Impala 部分的内容


