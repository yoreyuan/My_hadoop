Apache DolphinScheduler
---

[官网](https://dolphinscheduler.apache.org/zh-cn/) &nbsp; &nbsp; |  &nbsp; &nbsp; 
[GitHub](https://github.com/apache/incubator-dolphinscheduler) &nbsp; &nbsp; |  &nbsp; &nbsp; 


<br/>

Apache DolphinScheduler是一个分布式易扩展的可视化DAG工作流任务调度系统

*******

# 1 Home
分布式易扩展的可视化DAG工作流任务调度系统。Apache DolphinScheduler是一个分布式去中心化，易扩展的可视化DAG工作流任务调度系统。
致力于解决数据处理流程中错综复杂的依赖关系，使调度系统在数据处理流程中开箱即用。

## 1.1 特性一览
**高可靠性**：去中心化的多Master和多Worker, 自身支持HA功能, 采用任务队列来避免过载，不会造成机器卡死。

**简单易用**：DAG监控界面，所有流程定义都是可视化，通过拖拽任务定制DAG，通过API方式与第三方系统对接, 一键部署。

**丰富的使用场景**：支持暂停恢复操作. 支持多租户，更好的应对大数据的使用场景. 支持更多的任务类型，如 spark, hive, mr, python, sub_process, shell。

**高扩展性**：支持自定义任务类型，调度器使用分布式调度，调度能力随集群线性增长，Master和Worker支持动态上下线。

# 2 博客
关于系统架构设计可以查看官方提供的**xiaochun.liu**一篇博客 [DolphinScheduler系统架构设计](https://dolphinscheduler.apache.org/zh-cn/blog/architecture-design.html)


# 3 文档
## 3.1 部署
### 3.1.1 后端部署
后端有2种部署方式，分别为自动化部署和编译源码部署。下面主要介绍下载编译后的二进制包一键自动化部署的方式完成DolphinScheduler后端部署。

#### 3.1.1.1 基础软件安装
* Mysql (5.5+) : 必装
* JDK (1.8+) : 必装
* ZooKeeper(3.4.6+) ：必装
* Hadoop(2.6+) ：选装， 如果需要使用到资源上传功能，MapReduce任务提交则需要配置Hadoop(上传的资源文件目前保存在Hdfs上)
* Hive(1.2.1) : 选装，hive任务提交需要安装
* Spark(1.x,2.x) : 选装，Spark任务提交需要安装
* PostgreSQL(8.2.15+) : 选装，PostgreSQL PostgreSQL存储过程需要安装

**注意**：EasyScheduler本身不依赖Hadoop、Hive、Spark、PostgreSQL,仅是会调用他们的Client，用于对应任务的运行。

#### 3.1.1.2 创建部署用户
在所有需要部署调度的机器上创建部署用户（本次以node2、node3节点为例），因为worker服务是以 sudo -u {linux-user} 方式来执行作业，所以部署用户需要有 sudo 权限，而且是免密的。

```
# 1 创建用户
useradd escheduler

# 2 设置 escheduler 用户密码
passwd escheduler

# 3 赋予sudo权限。编辑系统 sudoers 文件
# 如果没有编辑权限，以root用户登录，赋予w权限
# chmod 640 /etc/sudoers
vi /etc/sudoers

# 大概在100行，在root下添加如下
escheduler  ALL=(ALL)       NOPASSWD: NOPASSWD: ALL

# 并且需要注释掉 Default requiretty 一行。如果有则注释，没有没有跳过
#Default requiretty

########### end ############


# 4 切换到 escheduler 用户
su escheduler
  
```

#### 3.1.1.3 下载并解压
```bash
# 1 创建安装目录
sudo mkdir /opt/DolphinScheduler

# 2 将DolphinScheduler赋予给escheduler用户
sudo chown -R escheduler:escheduler /opt/DolphinScheduler

# 3 下载后端。简称escheduler-backend
cd /opt/DolphinScheduler
wget https://github.com/apache/incubator-dolphinscheduler/releases/download/1.1.0/escheduler-1.1.0-backend.tar.gz

# 4 解压
mkdir escheduler-backend
tar -zxf escheduler-1.1.0-backend.tar.gz -C escheduler-backend
cd escheduler-backend/

# 5 目录介绍
# [escheduler@node2 escheduler-backend]$ tree -L 1
# .
# ├── bin           # 基础服务启动脚本
# ├── conf          # 项目配置文件
# ├── install.sh    # 一键部署脚本
# ├── lib           # 项目依赖jar包，包括各个模块jar和第三方jar
# ├── script        # 集群启动、停止和服务监控启停脚本
# └── sql           # 项目依赖sql文件
# 5 directories, 1 file

```

#### 3.1.1.4 针对escheduler用户ssh免密配置
```bash
# 1 配置SSH免密
# 1.1 node2 节点执行
#   有提示直接回车
ssh-keygen -t rsa
# 拷贝到node2和node3。提示输入密码时，输入 escheduler 用户的密码
ssh-copy-id -i ~/.ssh/id_rsa.pub escheduler@node2
ssh-copy-id -i ~/.ssh/id_rsa.pub escheduler@node3

# 1.2 node3 节点执行
#   有提示直接回车
ssh-keygen -t rsa
# 拷贝到node2和node3。提示输入密码时，输入 escheduler 用户的密码
ssh-copy-id -i ~/.ssh/id_rsa.pub escheduler@node2
ssh-copy-id -i ~/.ssh/id_rsa.pub escheduler@node3
```

#### 3.1.1.5 数据库初始化

执行以下命令创建数据库和账号
```sql
CREATE DATABASE escheduler DEFAULT CHARACTER SET utf8 DEFAULT COLLATE utf8_general_ci;
-- 设置数据用户escheduler的访问密码为 escheduler，并且不对访问的ip做限制
-- 测试环境将访问设置为所有，如果是生产，可以限制只能子网段的ip才能访问（'198.168.33.%'）
GRANT ALL PRIVILEGES ON escheduler.* TO 'escheduler'@'%' IDENTIFIED BY 'escheduler';
flush privileges;

```

创建表和导入基础数据 修改`vim /opt/DolphinScheduler/escheduler-backend/conf/dao/data_source.properties`中的下列属性
```bash
# 大概在第 4 行修改MySQL数据库的url
spring.datasource.url=jdbc:mysql://node1:3306/escheduler?characterEncoding=UTF-8
# 用户名。
spring.datasource.username=escheduler
# 密码。填入上一步IDENTIFIED BY 后面设置的密码
spring.datasource.password=escheduler
```

执行创建表和导入基础数据脚本
```bash
# 前面已进入/opt/DolphinScheduler/escheduler-backend目录下，然后执行数据初始化脚本
# 最后看到  create escheduler success 表示数据库初始化成功
sh ./script/create_escheduler.sh
```

#### 3.1.1.6 修改部署目录权限及运行参数
```bash
# 1 修改conf/env/目录下的 .escheduler_env.sh 环境变量
vim conf/env/.escheduler_env.sh

# 将对应的修改为自己的组件或框架的路径
export HADOOP_HOME=/opt/hadoop-3.1.2
export HADOOP_CONF_DIR=/opt/hadoop-3.1.2/etc/hadoop
export SPARK_HOME1=/opt/spark-2.3.4-bin-hadoop2.7
#export SPARK_HOME2=/opt/soft/spark2
#export PYTHON_HOME=/opt/soft/python
export JAVA_HOME=/usr/local/zulu8/
export HIVE_HOME=/opt/apache-hive-3.1.1-bin
#export PATH=$HADOOP_HOME/bin:$SPARK_HOME1/bin:$SPARK_HOME2/bin:$PYTHON_HOME:$JAVA_HOME/bin:$HIVE_HOME/bin:$PATH
export PATH=$HADOOP_HOME/bin:$SPARK_HOME1/bin:$JAVA_HOME/bin:$HIVE_HOME/bin:$PATH

# ==========
# CDH 版
# ==========
#export HADOOP_HOME=/opt/cloudera/parcels/CDH-6.2.0-1.cdh6.2.0.p0.967373/lib/hadoop
#export HADOOP_CONF_DIR=/etc/hadoop/conf.cloudera.yarn
#export SPARK_HOME1=/opt/cloudera/parcels/CDH-6.2.0-1.cdh6.2.0.p0.967373/lib/spark
##export SPARK_HOME2=/opt/soft/spark2
##export PYTHON_HOME=/opt/soft/python
#export JAVA_HOME=/usr/local/zulu8/
#export HIVE_HOME=/opt/cloudera/parcels/CDH-6.2.0-1.cdh6.2.0.p0.967373/lib/hive
##export PATH=$HADOOP_HOME/bin:$SPARK_HOME1/bin:$SPARK_HOME2/bin:$PYTHON_HOME:$JAVA_HOME/bin:$HIVE_HOME/bin:$PATH
#export PATH=$HADOOP_HOME/bin:$SPARK_HOME1/bin:$JAVA_HOME/bin:$HIVE_HOME/bin:$PATH

```

修改 `install.sh`中的各参数，替换成自身业务所需的值，这里只列出了重要的修改项，其它默认不用改即可。
```bash
# mysql配置
# mysql 地址,端口
mysqlHost="192.168.33.3:3306"

# mysql 数据库名称
mysqlDb="escheduler"
 
# mysql 用户名
mysqlUserName="escheduler"

# mysql 密码
# 注意：如果有特殊字符，请用 \ 转移符进行转移
mysqlPassword="escheduler"

# conf/config/install_config.conf配置
# 注意：安装路径,不要当前路径(pwd)一样。一键部署脚本分发到其它节点时的安装路径
installPath="/opt/DolphinScheduler/escheduler"

# 部署用户
# 注意：部署用户需要有sudo权限及操作hdfs的权限，如果开启hdfs，根目录需要自行创建
deployUser="escheduler"

# zk集群
zkQuorum="192.168.33.3:2181,192.168.33.6:2181,192.168.33.9:2181"

# 安装hosts
# 注意：安装调度的机器hostname列表，如果是伪分布式，则只需写一个伪分布式hostname即可
ips="192.168.33.6,192.168.33.9"

# conf/config/run_config.conf配置
# 运行Master的机器
# 注意：部署master的机器hostname列表
masters="192.168.33.6"

# 运行Worker的机器
# 注意：部署worker的机器hostname列表
workers="192.168.33.6,192.168.33.9"

# 运行Alert的机器
# 注意：部署alert server的机器hostname列表
alertServer="192.168.33.6"

# 运行Api的机器
# 注意：部署api server的机器hostname列表
apiServers="192.168.33.6"

# 用到邮箱发送邮件时务必配置上邮件服务，否则执行结果发送时会提示失败
# cn.escheduler.server.worker.runner.TaskScheduleThread:[249] - task escheduler # failure : send mail failed!
java.lang.RuntimeException: send mail failed!
# alert配置
# 邮件协议，默认是SMTP邮件协议
mailProtocol="SMTP"
# 邮件服务host。以网易邮箱为例。QQ邮箱的服务为 smtp.qq.com
mailServerHost="smtp.163.com"
# 邮件服务端口。SSL协议端口 465/994，非SSL协议端口 25
mailServerPort="465"
# 发送人。
# 网易邮箱在 客户端授权密码 获取，具体可以看下图
mailSender="yuandd_yore@163.com"
# 发送人密码
mailPassword="yore***"

# 下载Excel路径
xlsFilePath="/home/escheduler/xls"

#是否启动监控自启动脚本
# 开关变量,在1.0.3版本中增加，控制是否启动自启动脚本(监控master,worker状态,如果掉线会自动启动) 
# 默认值为"false"表示不启动自启动脚本,如果需要启动改为"true"
monitorServerState="true"

# 资源中心上传选择存储方式：HDFS,S3,NONE
resUploadStartupType="HDFS"

# 如果resUploadStartupType为HDFS，defaultFS写namenode地址，支持HA,需要将core-site.xml和hdfs-site.xml放到conf目录下
# 如果是S3，则写S3地址，比如说：s3a://escheduler，注意，一定要创建根目录/escheduler
defaultFS="hdfs://192.168.33.3:8020"

# resourcemanager HA配置，如果是单resourcemanager,这里为yarnHaIps=""
yarnHaIps="192.168.33.3"

# 如果是单 resourcemanager,只需要配置一个主机名称,如果是resourcemanager HA,则默认配置就好
singleYarnIp="192.168.33.3"

# common 配置
# 程序路径
programPath="/opt/DolphinScheduler/escheduler"

#下载路径
downloadPath="/tmp/escheduler/download"

# 任务执行路径
execPath="/tmp/escheduler/exec"

# SHELL环境变量路径
shellEnvPath="$installPath/conf/env/.escheduler_env.sh"

# 资源文件的后缀
resSuffixs="txt,log,sh,conf,cfg,py,java,sql,hql,xml"

# api 配置
# api 服务端口
apiServerPort="12345"

```

如果使用hdfs相关功能，需要拷贝hdfs-site.xml和core-site.xml到conf目录下
```bash
cp $HADOOP_HOME/etc/hadoop/hdfs-site.xml conf/
cp $HADOOP_HOME/etc/hadoop/core-site.xml conf/

```

#### 3.1.1.7 执行脚本一键部署
```bash
# 1 一键部署并启动
sh install.sh

# 2 查看日志
[escheduler@node2 escheduler-backend]$ tree /opt/DolphinScheduler/escheduler/logs
#/opt/DolphinScheduler/escheduler/logs
#├── escheduler-alert.log
#├── escheduler-alert-server-node-b.test.com.out
#├── escheduler-alert-server.pid
#├── escheduler-api-server-node-b.test.com.out
#├── escheduler-api-server.log
#├── escheduler-api-server.pid
#├── escheduler-logger-server-node-b.test.com.out
#├── escheduler-logger-server.pid
#├── escheduler-master.log
#├── escheduler-master-server-node-b.test.com.out
#├── escheduler-master-server.pid
#├── escheduler-worker.log
#├── escheduler-worker-server-node-b.test.com.out
#├── escheduler-worker-server.pid
#└── {processDefinitionId}
#    └── {processInstanceId}
#        └── {taskInstanceId}.log


# 3 查看Java进程
# 3.1 node2
[escheduler@node2 escheduler-backend]$ jps
31651 WorkerServer              # worker服务
31784 ApiApplicationServer      # api服务
31609 MasterServer              # master服务
31743 AlertServer               # alert服务
31695 LoggerServer              # logger服务

# 3.2 node3
[escheduler@cdh3 DolphinScheduler]$ jps
26678 WorkerServer
26718 LoggerServer

```

**错误1**：如果查看`/opt/DolphinScheduler/escheduler/logs/escheduler-api-server-*.out`日志报如下错误
```log
nohup: failed to run command ‘/bin/java’: No such file or directory

```

**解决**：将JAVA_HOME/bin下的java软连接到`/bin`下。（每个dolphinscheduler节点都执行）
```bash
ln -s $JAVA_HOME/bin/java /bin/java

```


#### 3.1.1.8 dolphinscheduler后端服务启停
```bash
# 启动
/opt/DolphinScheduler/escheduler/script/start_all.sh

# 停止
/opt/DolphinScheduler/escheduler/script/stop_all.sh

```

### 3.1.2 前端部署
前端有3种部署方式，分别为自动化部署，手动部署和编译源码部署。这里主要使用自动化脚本方式部署DolphinScheduler前端服务。

#### 3.1.2.1 下载并解压
```bash
# 1 下载 UI 前端。简称escheduler-ui
# 在node2节点下的 /opt/DolphinScheduler 
wget https://github.com/apache/incubator-dolphinscheduler/releases/download/1.1.0/escheduler-1.1.0-ui.tar.gz

# 2 解压
mkdir escheduler-ui
tar -zxf escheduler-1.1.0-ui.tar.gz -C escheduler-ui
cd escheduler-ui

```

#### 3.1.2.2 执行自动化部署脚本
执行自动化部署脚本。脚本会提示一些参数，根据提示完成安装。
```
[escheduler@cdh2 escheduler-ui]$ sudo ./install-escheduler-ui.sh
欢迎使用easy scheduler前端部署脚本,目前前端部署脚本仅支持CentOS,Ubuntu
请在 escheduler-ui 目录下执行
linux
请输入nginx代理端口，不输入，则默认8888 :8888
请输入api server代理ip,必须输入，例如：192.168.xx.xx :192.168.33.6
请输入api server代理端口,不输入，则默认12345 :12345
=================================================
        1.CentOS6安装
        2.CentOS7安装
        3.Ubuntu安装
        4.退出
=================================================
请输入安装编号(1|2|3|4)：2

…… 

Complete!
port option is needed for add
FirewallD is not running
setenforce: SELinux is disabled
请浏览器访问：http://192.168.33.6:8888

```

在这个脚本执行过程中会用过yum安装Nginx，通过引导设置后的Nginx配置文件为`/etc/nginx/conf.d/escheduler.conf`。如果本地已经存在Nginx，可以选择手动部署，
只需修改Nginx配置文件`vim /etc/nginx/conf.d/default.conf`，配置如下信息，最后重启Nginx：`systemctl restart nginx`。
```html
server {
    listen       8888;  # 访问端口
    server_name  localhost;
    #charset koi8-r;
    #access_log  /var/log/nginx/host.access.log  main;
    location / {
        root   /xx/dist; # 上面前端解压的dist目录地址(自行修改)
        index  index.html index.html;
    }
    location /escheduler {
        proxy_pass http://192.168.33.6:12345; # 接口地址(自行修改)
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header x_real_ipP $remote_addr;
        proxy_set_header remote_addr $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_http_version 1.1;
        proxy_connect_timeout 4s;
        proxy_read_timeout 30s;
        proxy_send_timeout 12s;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
    }
    #error_page  404              /404.html;
    # redirect server error pages to the static page /50x.html
    #
    error_page   500 502 503 504  /50x.html;
    location = /50x.html {
        root   /usr/share/nginx/html;
    }
}
```

**问题1**：上传文件大小限制
编辑配置文件 `vim /etc/nginx/nginx.conf`
```bash
# 更改上传大小
client_max_body_size 1024m
```

#### 3.1.2.3 dolphinscheduler前端服务启停
```bash
# 1 启动
systemctl start nginx

# 2 状态
systemctl status nginx

# 3 停止
#nginx -s stop
systemctl stop nginx

```

## 3.2 快速上手
可以查看我的blog [工作流任务调度系统：Apache DolphinScheduler](https://blog.csdn.net/github_39577257/article/details/102783298)


## 3.3 同其它调度工具的对比
Class | Item | DolphinScheduler | Azkaban
---- | ---- | :---- | :----
稳定性 | 单点故障   | 去中心化的多 Master 和多 Worker | 是，单个 Web 和调度程序组合节点
&nbsp;| HA额外要求 | 不需要（本身就支持HA） | DB
&nbsp;| 过载处理   | 任务队列机制，单个机器上可调度的任务数量可以灵活配置，当任务过多时会缓存在任务队列里，不会造成机器卡死 | 任务太多会卡死服务器
易用性 | DAG监控界面   | 任务状态、任务类型、重试次数、任务运行机器、可视化变量等关键信息一目了然 | 只能看到任务状态
&nbsp;| 可视化流程定义 | 是，所有流程定义操作都是可视化的，通过拖拽任务来绘制DAG，配置数据源及资源，同时对于第三方系统提供API方式的操作 | 否，通过自定义DSL绘制DAG打包上传
&nbsp;| 快速部署      | 一键部署 | 集群化部署，复杂
功能   | 是否能暂停和恢复 | 支持暂停、恢复操作 | 否，只能先将工作流杀死再重新运行
&nbsp; | 是否支持多租户   | 支持。DolphinScheduler上的用户可以通过租户和Hadoop用户实现多对一或一对一的映射关系，这对于大数据作业上的调度是非常重要的 | 否
&nbsp;| 任务类型        | 支持传统的shell任务，同时支持大数据平台任务调度MR、Spark、SQL(MySQL、PostgreSQL、Hive、SparkSQL、Impala、ClickHouse、Oracle)、Python、Procedure、Sub_Process | shell、gobblin、hadoopJava、Java、Hive、Pig、Spark、hdfsToTeradata、teradataToHdfs
&nbsp;| 契合度          | 支持大数据作业Spark、Hive、MR的调度，同时由于支持多租户，于大数据业务更加契合 | 由于不支持多租户，在大数据平台业务使用上不够灵活
扩展性 | 是否支持自定义任务类型 | 是 | 是
&nbsp;| 是否支持集群扩展      | 是，调度器使用分布式调度，整体的调度能力会随着集群的规模线性增长，Master和Worker支持动态上下线 | 是，但是复杂，Executor水平扩展




