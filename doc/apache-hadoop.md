[Apache Hadoop](http://hadoop.apache.org/)
========



# 1 deploy
## 1.1 安装之前
* 绑定Ip
* 配置Host
* 配置SSH
* 暴露端口，或者关闭防火墙
* Disable selinux
* [配置NTP](https://blog.csdn.net/github_39577257/article/details/92471365#4.8)
* JDK
* 这里使用root用户。如果使用 Hadoop，先创建用户，给用到的目录赋予响应的权限即可


## 1.2 下载
```bash
 wget http://archive.apache.org/dist/hadoop/common/hadoop-3.1.2/hadoop-3.1.2.tar.gz
```

## 1.3 解压
```bash
tar -zxf hadoop-3.1.2.tar.gz -C /opt/
```

## 1.4 配置Hadoop环境变量
```bash
vim ~/.bash_profile
```
添加如下配置，保存并推出。
```bash
#hadoop配置
export HADOOP_HOME=/opt/hadoop-3.1.2
export PATH=$PATH:$JAVA_HOME/bin:$HADOOP_HOME/bin:$HADOOP_HOME/sbin
```

并使配置生效，验证。
```bash
. ~/.bash_profile
# 查看Hadoop版本
hadoop version
```

## 1.5 创建需要需要的文件夹
```bash
mkdir -p /opt/hadoop/dfs/dn
mkdir -p /opt/hadoop/dfs/nn
mkdir -p /opt/hadoop/dfs/snn
mkdir -p /opt/hadoop/yarn/container-logs

```

## 1.6 修改 hadoop-env.sh 配置文件
```bash
vim $HADOOP_HOME/etc/hadoop/hadoop-env.sh
```

大概在`54行`，配置上自己的 JAVA_HOME，保存退出。
```bash
export JAVA_HOME=/usr/local/zulu8
export HADOOP_HOME=/opt/hadoop-3.1.2
export HADOOP_CONF_DIR=${HADOOP_HOME}/etc/hadoop
```

## 1.7 配置 [core-site.xml](http://hadoop.apache.org/docs/stable/hadoop-project-dist/hadoop-common/core-default.xml)
```bash
vim $HADOOP_HOME/etc/hadoop/core-site.xml
```

添加如下配置内容
```xml
<configuration>
    <property>
        <name>fs.defaultFS</name>
        <value>hdfs://cdh6:8020</value>
    </property>
    <!-- 设置垃圾回收的时间，0为禁止，单位分钟数 -->
    <property>
      <name>fs.trash.interval</name>
      <value>60</value>
   </property>
   <property>
        <name>fs.trash.checkpoint.interval</name>
        <value>0</value>
   </property>
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

## 1.8 配置 [hdfs-site.xml](http://hadoop.apache.org/docs/stable/hadoop-project-dist/hadoop-hdfs/hdfs-default.xml)
```bash
vim $HADOOP_HOME/etc/hadoop/hdfs-site.xml
```

添加如下配置内容
```xml
<configuration>
   <property>
      <name>dfs.namenode.name.dir</name>
      <value>file:///opt/hadoop/dfs/nn</value>
   </property>
   <property>
      <name>dfs.datanode.data.dir</name>
      <value>file:///opt/hadoop/dfs/dn</value>
   </property>
   <property>
      <name>dfs.namenode.checkpoint.dir</name>
      <value>file:///opt/hadoop/dfs/snn</value>
   </property>
   <!--block的副本数，默认为3-->
   <property>
      <name>dfs.replication</name>
      <value>1</value>
   </property>
   <property>
      <name>dfs.namenode.http-address</name>
      <value>cdh6:50070</value>
   </property>
   <property>
      <name>dfs.namenode.secondary.http-address</name>
      <value>cdh6:50090</value>
   </property>
   <property>
      <name>dfs.permissions</name>
      <value>false</value>
   </property>
</configuration>
```

## 1.9 配置 [mapred-site.xml](http://hadoop.apache.org/docs/stable/hadoop-mapreduce-client/hadoop-mapreduce-client-core/mapred-default.xml)
```bash
vim $HADOOP_HOME/etc/hadoop/mapred-site.xml
```

添加如下配置内容
```xml
<configuration>
    <property>
        <name>mapreduce.framework.name</name>
        <value>yarn</value>
    </property>
    <property>
        <name>mapreduce.jobhistory.address</name>
        <value>cdh6:10020</value>
    </property>
    <property>
        <name>mapreduce.jobhistory.webapp.address</name>
        <value>cdh6:19888</value>
    </property>
    <property>
        <name>mapreduce.jobhistory.webapp.https.address</name>
        <value>cdh6:19890</value>
    </property>
</configuration>
```

## 1.10 配置 [yarn-site.xml](http://hadoop.apache.org/docs/stable/hadoop-yarn/hadoop-yarn-common/yarn-default.xml)
```bash
vim $HADOOP_HOME/etc/hadoop/yarn-site.xml
```

添加如下配置内容
```xml
<configuration>
 
<!-- Site specific YARN configuration properties -->
 
   <property>
        <name>yarn.resourcemanager.hostname</name>
        <value>cdh6</value>
    </property>
   <property>
        <name>yarn.nodemanager.aux-services</name>
        <value>mapreduce_shuffle</value>
    </property>
   <property>
         <name>yarn.nodemanager.aux-services.mapreduce.shuffle.class</name>
         <value>org.apache.hadoop.mapred.ShuffleHandler</value>
    </property>
 
   <!--NodeManager本地目录-->
   <property>
      <name>yarn.nodemanager.local-dirs</name>
      <value>file:///opt/hadoop/yarn</value>
   </property>
   <!--NodeManager容器日志目录-->
   <property>
      <name>yarn.nodemanager.log-dirs</name>
      <value>file:///opt/hadoop/yarn/container-logs</value>
   </property>
   <property>
      <name>yarn.log-aggregation-enable</name>
      <value>true</value>
   </property>
   <property>
      <name>yarn.log.server.url</name>
      <value>http://cdh6:19888/jobhistory/logs/</value>
   </property>
   <property>
      <name>yarn.nodemanager.vmem-check-enabled</name>
      <value>false</value>
   </property>
   <property>
      <name>yarn.application.classpath</name>
         <value>
            $HADOOP_HOME/etc/hadoop,
            $HADOOP_HOME/share/hadoop/common/*,
            $HADOOP_HOME/share/hadoop/common/lib/*,
            $HADOOP_HOME/share/hadoop/hdfs/*,
            $HADOOP_HOME/share/hadoop/hdfs/lib/*,
            $HADOOP_HOME/share/hadoop/mapreduce/*,
            $HADOOP_HOME/share/hadoop/mapreduce/lib/*,
            $HADOOP_HOME/share/hadoop/yarn/*,
            $HADOOP_HOME/share/hadoop/yarn/lib/*
         </value>
   </property>
</configuration>
```

## 1.11 配置 workers
```bash
vim $HADOOP_HOME/etc/hadoop/workers
```

将集群的所有Worker添加到文件中
```
cdh6
cdh1
```

## 1.12 将配置好的文件分发到各个 worker 节点
```bash
scp -r $HADOOP_HOME/ root@cdh1:/opt/
# 其它节点
```

## 1.13 格式化 NameNode
```bash
$HADOOP_HOME/bin/hdfs namenode -format
```

## 1.14 启动Hadoop
```bash
$HADOOP_HOME/sbin/start-dfs.sh
$HADOOP_HOME/sbin/start-yarn.sh
```

### 1.14.1 问题解决
启动时如果出现这个问题，此文件中添加对应配置
```
[root@cdh6 ~]# $HADOOP_HOME/sbin/start-dfs.sh
Starting namenodes on [cdh6]
ERROR: Attempting to operate on hdfs namenode as root
ERROR: but there is no HDFS_NAMENODE_USER defined. Aborting operation.
Starting datanodes
ERROR: Attempting to operate on hdfs datanode as root
ERROR: but there is no HDFS_DATANODE_USER defined. Aborting operation.
Starting secondary namenodes [cdh6]
ERROR: Attempting to operate on hdfs secondarynamenode as root
ERROR: but there is no HDFS_SECONDARYNAMENODE_USER defined. Aborting operation.
```

`start-dfs.sh`文件头部添加
```bash
#!/usr/bin/env bash
HDFS_DATANODE_USER=root
HADOOP_SECURE_DN_USER=hdfs
HDFS_NAMENODE_USER=root
HDFS_SECONDARYNAMENODE_USER=root
```

`start-yarn.sh`文件头部添加
```bash
#!/usr/bin/env bash
YARN_RESOURCEMANAGER_USER=root
HADOOP_SECURE_DN_USER=yarn
YARN_NODEMANAGER_USER=root
```

## 1.15 验证和查看是否启动成功
* jps:  Master节点的进程有：`NameNode`、`SecondaryNameNode`、`ResourceManager`，在worker节点的进程有：`DataNode`、`NodeManager`
* HDFS: [http://cdh6:50070](http://cdh6:50070)
* YARN: [http://cdh6:8088](http://cdh6:8088) 、 `http://cdh6:8088/stacks`
* 查看hdfs汇总信息： ` hdfs dfsadmin -report `
* HDFS上创建一个文件夹：`  hadoop dfs -mkdir /tmp/input `
* 往HDFS上传一份数据： `  hadoop fs -put $HADOOP_HOME/README.txt /tmp/input ` 
* 提交一个MR测试一下： `  hadoop jar $HADOOP_HOME/share/hadoop/mapreduce/hadoop-mapreduce-examples-3.1.2.jar wordcount  /tmp/input   /tmp/output `
* 查看结果： `  hadoop fs -head /tmp/output/part-r-00000 `

## 1.16 对集群进行基准测试
执行如下命令测试
```bash
# 不指定参数时会列出测试项，选择对应测试项测试即可。
$HADOOP_HOME/bin/hadoop jar  $HADOOP_HOME/share/hadoop/mapreduce/hadoop-mapreduce-client-jobclient-*-tests.jar TestDFSIO
``` 
指定的参数说明如下：

参数 | 说明
:---- | :----
DFSCIOTest                  | libhdfs 分布式 I/O 基准测试
DistributedFSCheck          | 文件系统一致性的分布式检查
JHLogAnalyzer               | Job History Log analyzer
MRReliabilityTest           | 通过注入faults/failures来测试MR框架可靠性的程序
NNdataGenerator             | 生成 NNloadGenerator 要使用的数据
NNloadGenerator             | 在 Namenode 上运行生成 NN loadgenerator的 WITHOUT MR 负载
NNloadGeneratorMR           | 在NameNode使用 NN loadgenerator作为MR job运行生成负载
NNstructureGenerator        | 生成NNdataGenerator使用的结构
SliveTest                   | HDFS 压力测试和实施数据验证
TestDFSIO                   | 分布式 I/O 基准测试
fail                        | 总是失败的一个job
filebench                   | Benchmark SequenceFile(Input or Output) Format(block,record compressed and uncompressed), Text(Input or Output) Format(compressed and uncompressed)
gsleep                      | 一个睡眠job，其映射器为每条记录创建1MB缓冲区
largesorter                 | 大型排序测试
loadgen                     | 生成 map/reduce 负载生成器
mapredtest                  | map/reduce 测试检查
minicluster                 | 单进程 HDFS 和 MR 集群.
mrbench                     | 可以创建许多小Job的 map/reduce 基准
nnbench                     | 侧重于带有MR的NameN基准
nnbenchWithoutMR            | 侧重不带有MR的基准
sleep                       | 一个Job的每个Map和Reduce休眠
testbigmapoutput            | 一个map/reduce程序，使用于非常大的不可拆分文件，并进行特征 map/reduce
testfilesystem              | FileSystem 的 read/write 测试
testmapredsort              | 一个map/reduce程序，用于验证map-reduce框架的排序。
testsequencefile            | 对二进制键值对的flat 文件测试
testsequencefileinputformat | 序列文件输入格式的测试
testtextinputformat         | 文本输入格式的测试
threadedmapbench            | 一个map/reduce基准测试，用于比较map与多次spills的性能，并通过1次溢出map
timelineperformance         | 启动映射器以测试时间轴服务性能的job



## 1.17 关闭Hadoop
```bash
$HADOOP_HOME/sbin/stop-all.sh
```


