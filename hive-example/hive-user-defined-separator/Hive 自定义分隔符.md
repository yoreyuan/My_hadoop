Hive中的自定义分隔符(包含Hadoop和Hive详细安装)
--
本文详细部分可以查看我的博客
[Hive中的自定义分隔符(包含Hadoop和Hive详细安装)](https://blog.csdn.net/github_39577257/article/details/89020980)

# 1 环境准备
环境使用Centos 7，同时先安装好JDK。Hive需要依赖于Hadoop，因此需要先安装好Hadoop和Hive。
本次Hadoop选用的版本是**2.7.7**，Hive的版本是**1.2.2**。

## 1.1 Hadoop安装
1. 下载
```
wget http://archive.apache.org/dist/hadoop/common/hadoop-2.7.7/hadoop-2.7.7.tar.gz
```

2. 解压
```
tar -zxvf hadoop-2.7.7.tar.gz -C /opt/
```

3. 修改 hadoop-env.sh 配置文件
进入到刚解压的Hadoop根目录下，`vim etc/hadoop/hadoop-env.sh`，大概在27行，编辑 JAVA_HOME
```
export JAVA_HOME=/usr/local/jdk1.8.0_151
```

4. 修改 core-site.xml 配置文件
进入到刚解压的Hadoop根目录下，`vim etc/hadoop/core-site.xml`
```
<configuration>
    <property>
        <name>fs.defaultFS</name>
        <value>hdfs://node1:8020</value>
    </property>
    <property>
  		<name>hadoop.tmp.dir</name>
  		<value>/opt/data/hadoop/tmp</value>
 	</property>
    <property>
  		<name>fs.checkpoint.period</name>
		<value>3600</value>
 	</property>
 	<!-- 使用beenline需要配置这项 -->
 	<!-- 使用sqoop需要配置这项 -->
 	<property>
  		<name>hadoop.proxyuser.root.groups</name>
  		<value>*</value>
 	</property>
 	<property>
  		<name>hadoop.proxyuser.root.hosts</name>
  		<value>*</value>
 	</property>
</configuration>
```


4. 修改 hdfs-site.xml 配置文件
进入到刚解压的Hadoop根目录下，`vim etc/hadoop/hdfs-site.xml`
```
<configuration>
	<property>
		<name>dfs.namenode.name.dir</name>
		<value>/opt/data/hadoop/hdfs/name</value>
	</property>
	<property>
		<name>dfs.datanode.data.dir</name>
		<value>/opt/data/hadoop/hdfs/data</value>
	</property>
	<property>
		<name>dfs.namenode.checkpoint.dir</name>
		<value>/opt/data/hadoop/hdfs/namesecondary</value>
	</property>
	<property>
		<name>dfs.replication</name>
		<value>1</value>
	</property>
	<property>
		<name>dfs.namenode.http-address</name>
		<value>node1:50070</value>
	</property>
	<property>
		<name>dfs.namenode.secondary.http-address</name>
		<value>node1:50090</value>
	</property>
	<property>
		<name>dfs.webhdfs.enabled</name>
		<value>true</value>
	</property>
	<property>
		<name>dfs.permissions</name>
		<value>false</value>
	</property>
</configuration>
```

5. 修改 mapred-site.xml 配置文件
进入到刚解压的Hadoop根目录下，先复制一份：`cp etc/hadoop/mapred-site.xml.template etc/hadoop/mapred-site.xml`,
然后再编辑 `vim etc/hadoop/mapred-site.xml`
```
<configuration>
	<property>
        <name>mapreduce.framework.name</name>
        <value>yarn</value>
    </property>
	<!-- jobhistory properties -->
	<property>
		<name>mapreduce.jobhistory.address</name>
		<value>node1:10020</value>
	</property>
	<property>
		<name>mapreduce.jobhistory.webapp.address</name>
		<value>node1:19888</value>
	</property>
</configuration>
```

6. 修改 yarn-site.xml 配置文件
进入到刚解压的Hadoop根目录下，`vim etc/hadoop/yarn-site.xml`
```
<configuration>

<!-- Site specific YARN configuration properties -->

	<property>
        <name>yarn.resourcemanager.hostname</name>
        <value>node1</value>
    </property>
	<property>
        <name>yarn.nodemanager.aux-services</name>
        <value>mapreduce_shuffle</value>
    </property>
	<property>
         <name>yarn.nodemanager.aux-services.mapreduce.shuffle.class</name>
         <value>org.apache.hadoop.mapred.ShuffleHandler</value>
    </property>

	<property>
		<name>yarn.nodemanager.local-dirs</name>
		<value>/opt/data/hadoop/yarn/local</value>
	</property>
	<property>
		<name>yarn.nodemanager.remote-app-log-dir</name>
		<value>/opt/data/hadoop/yarn/logs</value>
	</property>

	<property>
		<name>yarn.resourcemanager.address</name>
		<value>${yarn.resourcemanager.hostname}:8032</value>
	</property>
	<property>
		<name>yarn.resourcemanager.scheduler.address</name>
		<value>${yarn.resourcemanager.hostname}:8030</value>
	</property>
	<property>
		<name>yarn.resourcemanager.webapp.address</name>
		<value>${yarn.resourcemanager.hostname}:8088</value>
	</property>
	<property>
		<name>yarn.resourcemanager.webapp.https.address</name>
		<value>${yarn.resourcemanager.hostname}:8090</value>
	</property>
	<property>
		<name>yarn.resourcemanager.resource-tracker.address</name>
		<value>${yarn.resourcemanager.hostname}:8031</value>
	</property>
	<property>
		<name>yarn.resourcemanager.admin.address</name>
		<value>${yarn.resourcemanager.hostname}:8033</value>
	</property>

	<property>
		<name>yarn.log-aggregation-enable</name>
		<value>true</value>
	</property>
	<property>
		<name>yarn.log.server.url</name>
		<value>http://node1:19888/jobhistory/logs/</value>
	</property>
	<property>
		<name>yarn.nodemanager.vmem-check-enabled</name>
		<value>false</value>
	</property>
</configuration>
```

7. 修改 slaves 配置文件
进入到刚解压的Hadoop根目录下，`vim etc/hadoop/slaves`，文件中将从节点加进去
```
node1
```

8. 配置Hadoop环境变量 
编辑用户的profile文件，`vim /etc/profile`
```
#hadoop配置
export HADOOP_HOME=/opt/hadoop-2.7.7
export PATH=$PATH:$JAVA_HOME/bin:$HADOOP_HOME/bin:$HADOOP_HOME/sbin
export HADOOP_USER_CLASSPATH_FIRST=true
```
立即生效 `. /etc/profile`

9. 监测环境变量是否生效
任意路径下输入 `hadoop version`，可以看到Hadoop的安装版本信息标识环境变量已生效。

10. 格式化 NameNode
```
bin/hdfs namenode -format
```

11. 启动 HDFS
```
 sbin/start-all.sh
```
也可以用sbin下的其他脚本分别启动 hdfs 和 yarn

12. 检查
查看启动的线程 `jps`
```
[root@centos7 hadoop-2.7.7]# jps
2514 DataNode
2675 SecondaryNameNode
2935 NodeManager
2393 NameNode
2826 ResourceManager
3210 Jps
```

13. 页面访问UI
在浏览器中输入 `http://node1:50070`可以看到Hadoop的详细信息


14. 运行自带的WordCount测试环境是否可以正常运行和计算

```
hadoop fs -mkdir /input
hadoop fs -put README.txt /input
hadoop jar share/hadoop/mapreduce/hadoop-mapreduce-examples-2.7.7.jar wordcount  /input   /output
```
有如下语句表示可以正常运行
```
19/04/03 11:03:29 INFO mapreduce.Job:  map 0% reduce 0%
19/04/03 11:03:34 INFO mapreduce.Job:  map 100% reduce 0%
19/04/03 11:03:40 INFO mapreduce.Job:  map 100% reduce 100%
19/04/03 11:03:41 INFO mapreduce.Job: Job job_1554310998109_0001 completed successfully
```




## 1.2 Hive安装
1. 下载
```
wget http://archive.apache.org/dist/hive/hive-1.2.2/apache-hive-1.2.2-bin.tar.gz
```

2. 解压
```
tar -zxvf apache-hive-1.2.2-bin.tar.gz -C /opt/
```

3. 在HDFS上创建所需的目录
```
hdfs dfs -mkdir -p /hive/warehouse
hdfs dfs -mkdir -p /hive/tmp
hdfs dfs -mkdir -p /hive/log
hdfs dfs -chmod -R 777 /hive/warehouse
hdfs dfs -chmod -R 777 /hive/tmp
hdfs dfs -chmod -R 777 /hive/log
```

4. Mysql需要设置一个可以远程访问的账号，然后再创建一个hive数据库
```
mysql> use mysql; 
mysql> select host,user from user; 
mysql> grant all privileges on *.* to 'hive'@'%' identified by '远程访问mysql的密码' with grant option; 
mysql> flush privileges;
mysql> create database hive;
mysql> exit;
```

5. 配置hive的环境变量
编辑 profile 文件
```
export HIVE_HOME=/opt/apache-hive-1.2.2-bin
export PATH=$PATH:$HIVE_HOME/bin
```

6. 复制重命名hive的配置文件
```
cd $HIVE_HOME/conf
cp hive-env.sh.template hive-env.sh
cp hive-default.xml.template hive-site.xml
cp hive-log4j.properties.template hive-log4j.properties
cp hive-exec-log4j.properties.template hive-exec-log4j.properties
cp beeline-log4j.properties.template beeline-log4j.properties
```

7. 修改hive-env.sh中的内容：
```
export JAVA_HOME=/usr/local/jdk1.8.0_131
export HADOOP_HOME=/opt/hadoop-2.7.7
export HIVE_HOME=/opt/apache-hive-1.2.2-bin
export HIVE_CONF_DIR=/opt/apache-hive-1.2.2-bin/conf
```

8. 配置hive-log4j.properties
找到log4j.appender.EventCounter=org.apache.hadoop.hive.shims.HiveEventCounter注释掉
改为：
```
 72 #log4j.appender.EventCounter=org.apache.hadoop.hive.shims.HiveEventCounter
 73 log4j.appender.EventCounter=org.apache.hadoop.log.metrics.EventCounter
```

9. 修改hive-site.xml
在hive-site.xml中找到对应的配置项，修改如下配置（Hive元数据保存到Mysql)，其他配置项默认。
```
  <!--Hive工作写，HDFS根目录位置-->
	<property>
		<name>hive.exec.scratchdir</name>
		<value>/hive/tmp</value>
		<description>HDFS root scratch dir for Hive jobs which gets created with write all (733) permission. For each connecting user, an HDFS scratch dir: ${hive.exec.scratchdir}/&lt;username&gt; is created, with ${hive.scratch.dir.permission}.</description>
	</property>
	<!-- 数据存储的HDFS目录，用来存储Hive数据库、表等数据 -->
	<property>
		<name>hive.metastore.warehouse.dir</name>
		<value>/hive/warehouse</value>
		<description>location of default database for the warehouse</description>
	</property>
	<!-- 远程服务HiveServer2绑定的IP -->
	<property>
		<name>hive.server2.thrift.bind.host</name>
		<value>node1</value>
		<description>Bind host on which to run the HiveServer2 Thrift service.</description>
	</property>
	<!-- 配置mysql数据库连接，用来存储数据库元信息 -->
	<property>
		<name>javax.jdo.option.ConnectionURL</name>
		<!--<value>jdbc:derby:;databaseName=metastore_db;create=true</value>-->
		<value>jdbc:mysql://node1:3306/hive?createDatabaseIfNotExist=true&amp;useUnicode=true&amp;characterEncoding=utf8&amp;useSSL=false</value>
    	<description>JDBC connect string for a JDBC metastore</description>
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
	<!-- 配置Hive Web方式时，对应的war包内容包含Hive源码中Web相关内容 -->
	<!--<property>
		<name>hive.hwi.war.file</name>
		<value>lib/hive-hwi-1.2.2.war</value>
	</property>
	-->
```

10. 替换文件中的一些路径
在本地创建如下文件夹  
```
/opt/data/hive/tmp
/opt/data/hive/log
```
然后将 hive-site.xml中的 `${system:java.io.tmpdir}` 改成 `/opt/data/hive/tmp`

11. 添加MySQL驱动包
下载驱动包 `wget http://central.maven.org/maven2/mysql/mysql-connector-java/5.1.47/mysql-connector-java-5.1.47.jar`
然后放在$HIVE_HOME目录下的lib下

12. 更新Hadoop的jar包
将$HADOOP_HOME/share/hadoop/yarn/lib/下的jline*.jar文件，替换为$HIVE_HOME/lib/jline-2.12.jar

13. 初始化hive
到bin目录下执行` schematool -dbType mysql -initSchema`。这一步会在Mysql的hive库下初始化的表。

14. 动hive
启动metastore(这是jps查看进程会有一个RunJar) ` hive --service metastore & `。这一步会启动一个 RunJar 进程。

15 使用和退出
```
hive
hive> show databases;
OK
default
Time taken: 0.786 seconds, Fetched: 1 row(s)
hive> quit;
```


# 2 环境准备
Hive默认采用的TextInputFormat类。Hive将HDFS上的文件导入Hive会进行如下处理：① 调用InputFormat，将文件切成不同的文档。每篇文档即一行(Row)；②调用SerDe的Deserializer，将一行(Row)，切分为各个字段；

## 2.1 Hive操作
```
hive> create table test1(id int);
OK
Time taken: 0.442 seconds
hive> show tables;
OK
test1
Time taken: 0.031 seconds, Fetched: 1 row(s)
hive> describe extended test1;
OK
id                      int                 
         
Detailed Table Information      Table(tableName:test1, dbName:default, owner:root, createTime:1554317591, lastAccessTime:0, retention:0, sd:StorageDescriptor(cols:[FieldSchema(name:id, type:int, comment:null)], location:hdfs://node1:8020/hive/warehouse/test1, inputFormat:org.apache.hadoop.mapred.TextInputFormat, outputFormat:org.apache.hadoop.hive.ql.io.HiveIgnoreKeyTextOutputFormat, compressed:false, numBuckets:-1, serdeInfo:SerDeInfo(name:null, serializationLib:org.apache.hadoop.hive.serde2.lazy.LazySimpleSerDe, parameters:{serialization.format=1}), bucketCols:[], sortCols:[], parameters:{}, skewedInfo:SkewedInfo(skewedColNames:[], skewedColValues:[], skewedColValueLocationMaps:{}), storedAsSubDirectories:false), partitionKeys:[], parameters:{transient_lastDdlTime=1554317591}, viewOriginalText:null, viewExpandedText:null, tableType:MANAGED_TABLE)
Time taken: 0.126 seconds, Fetched: 3 row(s)
hive>
```
从上面打印的信息可以看到，hive的输入和输出调用的类有：  
* inputFormat:org.apache.hadoop.mapred.TextInputFormat,  
* outputFormat:org.apache.hadoop.hive.ql.io.HiveIgnoreKeyTextOutputFormat,  


# 代码

## Maven项目的 pom.xml文件中添加如下依赖

## 自定义重写的 TextInputFormat 类

## 自定义的 LineRecordReader 类


# Useing

```sql
//"US-ASCII""ISO-8859-1""UTF-8""UTF-16BE""UTF-16LE""UTF-16"  
hive> set textinputformat.record.encoding=UTF-8;
// 字段间的切分字符
hive> set textinputformat.record.fieldsep=,;
// 行切分字符
hive> set textinputformat.record.linesep=|+|;


hive> create table test  (  
    >   id string,  
    >   name string  
    > )  stored as  
    > INPUTFORMAT 'yore.hive.SQPTextInputFormat'  
    > OUTPUTFORMAT 'org.apache.hadoop.hive.ql.io.HiveIgnoreKeyTextOutputFormat' ; 


hive> load data local inpath '/root/hive_separator.txt' 
    > overwrite into table test;


hive> select * from test;

```
