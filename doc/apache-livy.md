[Apache Livy](http://livy.incubator.apache.org/)
==========

# 一 介绍
Apache Livy(孵化中)是 Apache Spark的REST服务，它能够通过REST的方式将代码片段或是序列化的二进制代码提交到Spark集群中去执行。

> Apache Livy is a service that enables easy interaction with a Spark cluster over a REST interface. It enables easy submission of Spark jobs or snippets of Spark code, synchronous or asynchronous result retrieval, as well as Spark Context management, all via a simple REST interface or an RPC client library. Apache Livy also simplifies the interaction between Spark and application servers, thus enabling the use of Spark for interactive web/mobile applications. Additional features include:

Apache Livy 是一种通过REST接口与Spark集群轻松交互的服务。它可以通过简单的REST接口或RPC客户端库轻松提交Spark Job 或Spark代码片段，同步或异步结果检索以及Spark Context的管理。 
Apache Livy还简化了Spark与应用程序服务器之间的交互，从而使Spark能够用于交互式Web或移动应用程序。其他功能包括：
* 长期运行的Spark Contexts 可以由多个客户端用于多个Spark Job
* 跨多个作业和客户端共享缓存的RDD或Dataframe
* 可以同时管理多个Spark Context，并且Spark Contexts在集群(YARN/Mesos)而不是Livy Server上运行，以实现良好的容错性和并发性
* Job可以作为预编译的jar、代码片段或通过 java/scala客户端API提交
* 通过安全的认证通信确保安全性

[livy-architecture.png](http://livy.incubator.apache.org./assets/images/livy-architecture.png)

# 二 安装
## 2.1 下载： 
```bash
 wget http://mirror.bit.edu.cn/apache/incubator/livy/0.6.0-incubating/apache-livy-0.6.0-incubating-bin.zip
```

## 2.2 解压：
```bash
 unzip apache-livy-0.6.0-incubating-bin.zip
 cd apache-livy-0.6.0-incubating-bin
```

## 2.3 配置环境变量
```bash
vim ~/.bash_profile
# set Livy environment
export LIVY_HOME=/opt/apache-livy-0.6.0-incubating-bin
export PATH=$PATH:$LIVY_HOME/bin
```

## 2.4 拷贝出配置文件
将模板配置文件复制成配置文件
```bash
cp $LIVY_HOME/conf/livy-client.conf.template $LIVY_HOME/conf/livy-client.conf
cp $LIVY_HOME/conf/livy.conf.template $LIVY_HOME/conf/livy.conf
cp $LIVY_HOME/conf/livy-env.sh.template $LIVY_HOME/conf/livy-env.sh
cp $LIVY_HOME/conf/log4j.properties.template $LIVY_HOME/conf/log4j.properties
cp $LIVY_HOME/conf/spark-blacklist.conf.template $LIVY_HOME/conf/spark-blacklist.conf
```

配置文件说明：
* **livy.conf**：包含server的配置。 Livy发行版附带一个默认配置文件模板，列出了可用的配置键及其默认值。
* **spark-blacklist.conf**：列出不允许用户覆盖的Spark配置选项。 这些选项将限制为其默认值或Livy使用的Spark配置中设置的值。
* **log4j.properties**：Livy日志记录的配置。 定义日志级别以及将写入日志消息的位置。 默认配置模板将日志消息打印到stderr。

## 2.4 配置`livy-env.sh`
这里主要配置下Spark和Hadoop的信息
```bash
vim $LIVY_HOME/conf/livy-env.sh
# 添加如下配置，保存并退出
export JAVA_HOME=/usr/local/zulu8
export SPARK_HOME=/opt/spark-2.4.3
export SPARK_CONF_DIR=$SPARK_HOME/conf
export HADOOP_HOME=/opt/hadoop-3.1.2
export HADOOP_CONF_DIR=$HADOOP_HOME/etc/hadoop
```

## 2.5 配置`livy.conf`
修改如下配置
```bash
vim $LIVY_HOME/conf/livy.conf
# 添加如下配置，保存并退出
livy.server.host = cdh6
livy.server.port = 8998
livy.spark.master = yarn
livy.spark.deploy-mode = cluster
livy.repl.enable-hive-context = true
```


## 2.7 启动Livy
运行Live 服务之前，需要安装Spark，官网是强烈建议将Spark配置为以YARN群集模式提交应用程序。
这可确保用户会话在YARN群集中正确考虑其资源，并且运行Livy服务器的主机在运行多个用户会话时不会过载。
                      
```bash
$LIVY_HOME/bin/livy-server start
```

## 2.8 验证
* 日志： 查看 `LIVY_HOME/logs`
* WebServer UI： [http://cdh6:8998/ui](http://cdh6:8998)


# 三 开始使用
* [REST API](http://livy.incubator.apache.org/docs/latest/rest-api.html)
* [程序化API](http://livy.incubator.apache.org/docs/latest/programmatic-api.html)


