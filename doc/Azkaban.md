Azkaban - Open-source Workflow Manager
===
[Azkaban 官网](https://azkaban.github.io/)

# 目录
* [安装 - 从源码开始构建](#安装---从源码开始构建)
    + [clone 源码](#clone-源码)
    + [Build](#Build)
    + [切换到需要的版本](#切换到需要的版本)
    + [开始](开始)
        - [设置数据库](#设置数据库)
        - [服务端](#服务端)
            * [azkaban.properties](#azkabanproperties)
            * [azkaban-users.xml](#azkabanusers-xml)
        - [执行端](#执行端)
            * [azkaban.properties](#azkabanproperties-1)
    + [启动](#启动)
    + [使用](#使用)
        - [Project部分](#Project部分)
        - [Job的创建](#Job的创建)
        - [查看Job](#查看Job)
        - [多Job工作流](#多Job工作流)

*****

<br/>

Azkaban是在LinkedIn上创建的批处理工作流作业调度程序，用于运行Hadoop作业。 
Azkaban通过作业依赖性解决订单，并提供易于使用的Web用户界面来维护和跟踪您的工作流程。

Features
* 兼容任何版本的Hadoop
* 易于使用的Web UI
* 简单的Web和http工作流上传
* 项目工作区
* 调度工作流程
* 模块化和可插入
* 身份验证和授权
* 跟踪用户操作
* 有关失败和成功的电子邮件提醒
* SLA警报和自动查杀
* 重试失败的工作

Azkaban由三部分构成：
1. Relational Database(Mysql)
azkaban将大多数状态信息都存于MySQL中,Azkaban Web Server 和 Azkaban Executor Server也需要访问DB。
2. Azkaban Web Server
提供了Web UI，是azkaban的主要管理者，包括 project 的管理，认证，调度，对工作流执行过程的监控等。
3. Azkaban Executor Server
调度工作流和任务，纪录工作流活任务的日志，之所以将AzkabanWebServer和AzkabanExecutorServer分开，主要是因为在某个任务流失败后，可以更方便的将重新执行。而且也更有利于Azkaban系统的升级

- - - -

# 1 安装 - 从源码开始构建
官方没有提供编译好的安装包，因此项目的使用需要先对源码进行编译，然后再进行部署安装。

* Java 8或更高版本
* Azkaban构建使用Gradle，因此需要安装Gradle 
```bash
# 下载
http://services.gradle.org/distributions/gradle-5.4.1-bin.zip
mkdir /usr/local/gradle
unzip -d /usr/local/gradle gradle-5.4.1-bin.zip
ls /usr/local/gradle/gradle-5.4.1

vim /.bash_profile
# 输入如下，保存，退出
GRADLE_HOME=/usr/local/gradle/gradle-5.4.1
export PATH=$PATH:$GRADLE_HOME/bin

source ~/.bash_profile
gradle -v
```

## 1.1 clone 源码
```bash
#也可以直接下载对应版本的源码
 git clone https://github.com/azkaban/azkaban.git
```

## 1.2 Build
进入到 clone 代码的根目录，执行如下命令：
```bash
./gradlew build
```

如果重新构建运行        `./gradlew clean`
如果构建并且安装发布    `./gradlew installDist`
如果运行测试           `./gradlew test`
如果构建时不运行测试    `./gradlew build -x test`

## 1.3 切换到需要的版本
```bash
git tag
git checkout tags/3.73.1
git branch

./gradlew clean build -x test

```

## 1.4 编译中问题解决
**问题1**：如果提示如下错误`llect2: fatal error: cannot find 'ld'`：
```
Starting a Gradle Daemon (subsequent builds will be faster)
Parallel execution with configuration on demand is an incubating feature.
> Task :az-exec-util:linkMainExecutable FAILED
collect2: fatal error: cannot find 'ld'
compilation terminated.
FAILURE: Build failed with an exception.
* What went wrong:
Execution failed for task ':az-exec-util:linkMainExecutable'.
> A build operation failed.
      Linker failed while linking main.
  See the complete log at: file:///opt/azkaban/az-exec-util/build/tmp/linkMainExecutable/output.txt
* Try:
Run with --stacktrace option to get the stack trace. Run with --info or --debug option to get more log output. Run with --scan to get full insights.
* Get more help at https://help.gradle.org
BUILD FAILED in 16s
41 actionable tasks: 15 executed, 7 from cache, 19 up-to-date
```

查看ld，发现缺少ld命令
```bash
 [root@cdh6 azkaban]# whereis ld
 ld: /usr/bin/ld.bfd /usr/bin/ld.gold /usr/share/man/man1/ld.1.gz
 
 [root@cdh6 azkaban]#  ll /usr/bin/ld*
 -rwxr-xr-x 1 root root 1006216 Oct 30  2018 /usr/bin/ld.bfd
 -rwxr-xr-x 1 root root    5302 Jul  3 21:25 /usr/bin/ldd
 -rwxr-xr-x 1 root root 5354296 Oct 30  2018 /usr/bin/ld.gold
```

因此我们修复这个命令，先删除`binutils`
```bash
#查看ld RPM信息
[root@cdh6 azkaban]# rpm -qf /usr/bin/ld
binutils-2.27-34.base.el7.x86_64
#删除这个，然后重新安装
[root@cdh6 azkaban]# rpm -e binutils --nodeps
admindir /var/lib/alternatives invalid
admindir /var/lib/alternatives invalid

```

在重新安装`binutils`
```bash
#下载。http://rpmfind.net/linux/rpm2html/search.php?query=binutils，例如CentOS7下载如下包
 wget http://rpmfind.net/linux/centos/7.6.1810/os/x86_64/Packages/binutils-2.27-34.base.el7.x86_64.rpm
# 安装
[root@cdh6 ~]# rpm -ivh binutils-2.27-34.base.el7.x86_64.rpm
Preparing...                          ################################# [100%]
Updating / installing...
   1:binutils-2.27-34.base.el7        ################################# [100%]
admindir /var/lib/alternatives invalid
admindir /var/lib/alternatives invalid
admindir /var/lib/alternatives invalid

```

再次查看`ld`，如下
```bash
[root@cdh6 ~]#  ll /usr/bin/ld*
-rwxr-xr-x 1 root root 1006216 Oct 30  2018 /usr/bin/ld.bfd
-rwxr-xr-x 1 root root    5302 Jul  3 21:25 /usr/bin/ldd
-rwxr-xr-x 1 root root 5354296 Oct 30  2018 /usr/bin/ld.gold
```
但还是没有`ld`，但是不要慌，其依赖是已经修复，例如错误
```bash
 error while loading shared libraries: libbfd-2.25.1-31.base.el7.so: cannot open shared object file: No such file or directory
```

此时只需要从其他CentOS系统中拷贝ld，并做个软连就可以了
```bash
#从其他节点（cdh4）拷贝 alternatives
[root@cdh4 ~]# scp -r /etc/alternatives/ root@cdh6:/etc/alternatives

# 软连修复
[root@cdh6 ~]#  cd /usr/bin
[root@cdh6 bin]# ln -s /etc/alternatives/ld  ld
# 再次查看。
[root@cdh6 bin]# ls -l ld*
lrwxrwxrwx 1 root root      20 Aug 19 19:00 ld -> /etc/alternatives/ld
-rwxr-xr-x 1 root root 1006216 Oct 30  2018 ld.bfd
-rwxr-xr-x 1 root root    5302 Jul  3 21:25 ldd
-rwxr-xr-x 1 root root 5354296 Oct 30  2018 ld.gold
```

**问题2**：编译时过程中获取`com.gradle.build-scan`插件发生错误。
```log
FAILURE: Build failed with an exception.
* Where:
Build file '/root/azkaban/build.gradle' line: 31
* What went wrong:
Plugin [id: 'com.gradle.build-scan', version: '1.9'] was not found in any of the following sources:
- Gradle Core Plugins (plugin is not in 'org.gradle' namespace)
- Plugin Repositories (could not resolve plugin artifact 'com.gradle.build-scan:com.gradle.build-scan.gradle.plugin:1.9')
  Searched in the following repositories:
    Gradle Central Plugin Repository
```
解决：
```bash
# 1 下载 build-scan-plugin 1.9 的资源包
wget https://plugins.gradle.org/m2/com/gradle/build-scan-plugin/1.9/build-scan-plugin-1.9.jar

# 2 上传到azkaban 编译服务器上

# 3 手动导入Maven本地仓库
mvn install:install-file -DgroupId=com.gradle -DartifactId=build-scan-plugin -Dversion=1.9 -Dpackaging=jar -Dfile=build-scan-plugin-1.9.jar
```

**问题3**：报`com.moowork.gradle:gradle-node-plugin:1.2.0`缺失。
```log
FAILURE: Build failed with an exception.
* Where:
Build file '/root/azkaban/azkaban-solo-server/build.gradle' line: 43
* What went wrong:
A problem occurred evaluating project ':azkaban-solo-server'.
> A problem occurred configuring project ':azkaban-web-server'.
   > Could not resolve all artifacts for configuration ':azkaban-web-server:classpath'.
      > Could not resolve com.moowork.gradle:gradle-node-plugin:1.2.0.
        Required by:
            project :azkaban-web-server > com.moowork.node:com.moowork.node.gradle.plugin:1.2.0
         > Could not resolve com.moowork.gradle:gradle-node-plugin:1.2.0.
            > Could not get resource 'https://plugins.gradle.org/m2/com/moowork/gradle/gradle-node-plugin/1.2.0/gradle-node-plugin-1.2.0.pom'.
               > Could not GET 'https://plugins.gradle.org/m2/com/moowork/gradle/gradle-node-plugin/1.2.0/gradle-node-plugin-1.2.0.pom'.
                  > plugins-artifacts.gradle.org: Name or service not known
         > Could not resolve com.moowork.gradle:gradle-node-plugin:1.2.0.
            > Could not get resource 'https://plugins.gradle.org/m2/com/moowork/gradle/gradle-node-plugin/1.2.0/gradle-node-plugin-1.2.0.pom'.
               > Could not GET 'https://plugins.gradle.org/m2/com/moowork/gradle/gradle-node-plugin/1.2.0/gradle-node-plugin-1.2.0.pom'.
                  > plugins-artifacts.gradle.org
* Try:
Run with --stacktrace option to get the stack trace. Run with --info or --debug option to get more log output. Run with --scan to get full insights.
* Get more help at https://help.gradle.org
BUILD FAILED in 1m 25s
```

**解决**：
```bash
# 1 下载
wget https://github.com/srs/gradle-node-plugin/archive/v1.2.0.zip

# 2 解压
unzip v1.2.0.zip

# 3 编译
cd gradle-node-plugin-1.2.0/
# 这里跳过测试，如果需要测试可以执行：gradle test
./gradlew clean build  -x test

# 4 手动导入Maven仓库
mvn install:install-file -DgroupId=com.moowork.gradle -DartifactId=gradle-node-plugin -Dversion=1.2.0 -Dpackaging=jar -Dfile=build/libs/gradle-node-plugin-1.2.0.jar

# 5 记得添加完毕后返回Azkaban的源码目录，然后再次执行编译

```

<br/>

这次OK，可以重新编译
```bash
[root@cdh6 azkaban]# ./gradlew clean build -x test
Parallel execution with configuration on demand is an incubating feature.
> Task :azkaban-common:compileJava
Note: Some input files use or override a deprecated API.
Note: Recompile with -Xlint:deprecation for details.
Note: Some input files use unchecked or unsafe operations.
Note: Recompile with -Xlint:unchecked for details.
> Task :azkaban-exec-server:compileJava
Note: Some input files use unchecked or unsafe operations.
Note: Recompile with -Xlint:unchecked for details.
> Task :azkaban-hadoop-security-plugin:compileJava
Note: Some input files use or override a deprecated API.
Note: Recompile with -Xlint:deprecation for details.
Note: /opt/azkaban/azkaban-hadoop-security-plugin/src/main/java/azkaban/security/HadoopSecurityManager_H_2_0.java uses unchecked or unsafe operations.
Note: Recompile with -Xlint:unchecked for details.
> Task :azkaban-web-server:npmSetup
/opt/azkaban/azkaban-web-server/.gradle/npm/npm-v5.6.0/bin/npm -> /opt/azkaban/azkaban-web-server/.gradle/npm/npm-v5.6.0/lib/node_modules
/opt/azkaban/azkaban-web-server/.gradle/npm/npm-v5.6.0/bin/npx -> /opt/azkaban/azkaban-web-server/.gradle/npm/npm-v5.6.0/lib/node_modules
+ npm@5.6.0
added 476 packages in 10.486s
> Task :azkaban-web-server:compileJava
Note: Some input files use or override a deprecated API.
Note: Recompile with -Xlint:deprecation for details.
Note: Some input files use unchecked or unsafe operations.
Note: Recompile with -Xlint:unchecked for details.
> Task :az-hadoop-jobtype-plugin:compileJava
Note: Some input files use or override a deprecated API.
Note: Recompile with -Xlint:deprecation for details.
Note: /opt/azkaban/az-hadoop-jobtype-plugin/src/main/java/azkaban/jobtype/HadoopSecureSparkWrapper.java uses unchecked or unsafe operations.
Note: Recompile with -Xlint:unchecked for details.
> Task :azkaban-web-server:npm_install
added 39 packages in 1.293s
> Task :azkaban-solo-server:compileJava
Note: /opt/azkaban/azkaban-solo-server/src/main/java/azkaban/soloserver/AzkabanSingleServer.java uses or overrides a deprecated API.
Note: Recompile with -Xlint:deprecation for details.
> Task :az-hdfs-viewer:compileJava
Note: Some input files use or override a deprecated API.
Note: Recompile with -Xlint:deprecation for details.
> Task :az-reportal:compileJava
Note: /opt/azkaban/az-reportal/src/main/java/azkaban/reportal/util/StreamProviderHDFS.java uses or overrides a deprecated API.
Note: Recompile with -Xlint:deprecation for details.
BUILD SUCCESSFUL in 35s
93 actionable tasks: 79 executed, 8 from cache, 6 up-to-date

```

上一步完成之后，可以看到如下几个包：
```bash
azkaban/azkaban-web-server/build/distributions/azkaban-exec-server-3.73.1.tar.gz
azkaban/azkaban-exec-server/build/distributions/azkaban-web-server-3.73.1.tar.gz
#azkaban/azkaban-db/build/distributions/azkaban-db-3.73.1.tar.gz
azkaban/azkaban-db/build/sql/create-all-sql-3.73.1.sql
```

### 1.5 初始化MySQL中的数据库和表
保证已经安装了 Mysql，而且官网也建议用户选择 Mysql 作为 Azkaban 数据库。
确定MySQL支持较大的数据包，设置可以`vim /etc/my.cnf`，设置如下：
```bash
[mysqld]
...
max_allowed_packet=1024M

```

然后重启MySQL服务：
```bash
# service mysql restart
$ sudo /sbin/service mysqld restart
```

执行`create-all-sql-3.73.1.sql`SQL脚本：
```sql
--为 Azkaban 创建数据库
mysql> CREATE DATABASE azkaban;

--为 Azkaban 创建一个 Mysql 用户，并赋予响应的权限。
mysql> CREATE USER 'username'@'%' IDENTIFIED BY 'password';
mysql> GRANT SELECT,INSERT,UPDATE,DELETE ON azkaban.* to '<username>'@'%' WITH GRANT OPTION;

mysql> use azkaban;

mysql>  source /home/yore/azkaban/azkaban-db/build/sql/create-all-sql-3.73.1.sql;

mysql> show tables;
+--------------------------+
| Tables_in_airflow        |
+--------------------------+
| QRTZ_BLOB_TRIGGERS       |
| QRTZ_CALENDARS           |
| QRTZ_CRON_TRIGGERS       |
| QRTZ_FIRED_TRIGGERS      |
| QRTZ_JOB_DETAILS         |
| QRTZ_LOCKS               |
| QRTZ_PAUSED_TRIGGER_GRPS |
| QRTZ_SCHEDULER_STATE     |
| QRTZ_SIMPLE_TRIGGERS     |
| QRTZ_SIMPROP_TRIGGERS    |
| QRTZ_TRIGGERS            |
| active_executing_flows   |
| active_sla               |
| execution_dependencies   |
| execution_flows          |
| execution_jobs           |
| execution_logs           |
| executor_events          |
| executors                |
| project_events           |
| project_files            |
| project_flow_files       |
| project_flows            |
| project_permissions      |
| project_properties       |
| project_versions         |
| projects                 |
| properties               |
| triggers                 |
+--------------------------+
29 rows in set (0.00 sec)
--提前手动插入executor的ip和端口。Azkaban默认也执行节点通信使用的是12321端口，
--如果12321端口占用，可以使用其他端口，后面配置文件修改，例如这里使用12331端口
--这一步其实可以不用插入，executor启动会自动插入，active标记为0，
--当web启动正常访问executor时会改为1，当关闭是会删除这个ip。
--这里建议手动插入真实ip，并标记为1。如果是多个，就执行多次插入语句
mysql> insert into executors(host,port,active) values('192.168.33.11',12331,1);

```

## 1.6 开始
安装方式有两种
* [with the Solo Server](https://azkaban.readthedocs.io/en/latest/getStarted.html#getting-started-with-the-solo-server)
* [with the Multi Executor Server](https://azkaban.readthedocs.io/en/latest/getStarted.html#getting-started-with-the-multi-executor-server)

本次以第二种方式安装

### 1.6.1 Executor端Server安装
执行端服务我们规划安装到node3（192.168.33.11）节点，我们首先把`azkaban-exec-server-3.73.1.tar.gz`包上传到node3。
#### 1 解压
解压前面得到的 `azkaban-exec-server-3.73.1.tar.gz`
```bash
tar -zxf azkaban-exec-server-3.73.1.tar.gz
```

解压之后，目录说明如下：   

Folder |	Description
:---- | :----
bin  | 	启动/停止Azkaban solo 服务的脚本
conf |	Azkaban solo 服务的配置文件
lib  |	Azkaban的jar依赖库
extlib  |	添加到extlib 的其它jar将添加到Azkaban的 classpath中
plugins |	可以安装插件的目录

#### 2 配置[azkaban.properties](https://azkaban.readthedocs.io/en/latest/configuration.html#azkaban-executor-server-configuration)
进入到 ` azkaban-exec-server-3.73.1/conf/` 文件夹下，修改：
```yaml
# Azkaban Personalization Settings
azkaban.name=My-Azkaban
azkaban.label=My Azkaban
# 十六进制值，默认为 #FF3601（红色）。允许您为Azkaban UI设置样式颜色
azkaban.color=#fc5b13
# Azkaban将显示的时区
default.timezone.id=Asia/Shanghai

azkaban.default.servlet.path=/index
# 设置ui的css和javascript文件的目录
web.resource.dir=web/
# Azkaban plugin settings
azkaban.jobtype.plugin.dir=plugins/jobtypes
# Directory where viewer plugins are installed.
viewer.plugin.dir=plugins/viewer
# Azkaban UserManager class
user.manager.class=azkaban.user.XmlUserManager
user.manager.xml.file=conf/azkaban-users.xml
# Loader for projects
executor.global.properties=conf/global.properties
azkaban.project.dir=projects

# Velocity dev mode
velocity.dev.mode=false
# Azkaban Jetty server properties.
jetty.use.ssl=false
jetty.maxThreads=25
jetty.port=8081
# ssl端口
#jetty.ssl.port=8443
# 密钥库文件。使用绝对路径。keytool -keystore keystore -alias jetty -genkey -keyalg RSA
#jetty.keystore=/app/disk1/keystore
# jetty密码（ssl文件密码）
#jetty.password=
# 密钥密码（jetty主密码与keystore文件相同）
#jetty.keypassword=
# The trust store（SSL文件名）
#jetty.truststore=
# The trust password（SSL文件密码）
#jetty.trustpassword=
jetty.connector.stats=true

# Azkaban Executor settings
# executo 端口，默认为12321。
executor.port=12331
executor.connector.stats=true
executor.maxThreads=50
executor.flow.threads=30
# 日志保留时间，默认为12周，这里配置为4周
execution.logs.retention.ms=2419200000
# Where the Azkaban web server is located
azkaban.webserver.url=http://192.168.33.9:8081
# mail settings
# azkaban用于发送电子邮件的地址
#mail.sender=xxx@xxx.com
# 电子邮件服务主机
#mail.host=smtp.xxx.com
# 电子邮件服务用户名
#mail.user=
# 电子邮件用户名密码
#mail.password=
job.failure.email=
job.success.email=
# User facing web server configurations used to construct the user facing server URLs. They are useful when there is a reverse proxy between Azkaban web servers and users.
# enduser -> myazkabanhost:443 -> proxy -> localhost:8081
# when this parameters set then these parameters are used to generate email links.
# if these parameters are not set then jetty.hostname, and jetty.port(if ssl configured jetty.ssl.port) are used.
# azkaban.webserver.external_hostname=myazkabanhost.com
# azkaban.webserver.external_ssl_port=443
# azkaban.webserver.external_port=8081

# JMX stats
# 每个作业可以请求的最大初始内存量。此验证在项目上载时执行
#job.max.Xms=1GB
# 每个作业可以请求的最大内存量。此验证在项目上载时执行
#job.max.Xmx=2GB
# 防止除具有Admin角色的人以外的任何人创建新项目
lockdown.create.projects=false
# 缓存目录
cache.directory=cache

# Azkaban mysql settings by default. Users should configure their own username and password.
database.type=mysql
mysql.port=3306
mysql.host=node1
mysql.database=airflow
mysql.user=root
mysql.password=123456
# Azkaban Web客户端可以打开到数据库的连接数
mysql.numconnections=100

```

#### 3 发送到其他节点
```bash
# 可以node2和node3都作为执行端服务，例如这里只启一个执行服务，
#scp -r azkaban-exec-server-3.73.1 root@node2:/opt/
scp -r azkaban-exec-server-3.73.1 root@node3:/opt/
```


#### 4 启动
**注意**：启动顺序一定是先启动`azkaban-exec-server`，再启动`azkaban-web-server`
```bash
cd /opt/azkaban-exec-server-3.73.1
bin/start-exec.sh
```

查看启动信息
```bash
# 查看日志。start-exec.sh启动时会生成一个 executorServerLog_* 开头的日志文件，可以查看启动信息
tail -f executorServerLog_*.out

# jps可以查看到 AzkabanExecutorServer进程
jps

# 查看执行端口状态，最好执行下面的请求。如果成功会提示：{"status":"success"}
curl -G "localhost:12331/executor?action=activate" && echo
```

#### 5 关闭
如果需要重启集群或者关闭集群时，可以执行如下命令：
```bash
bin/shutdown-exec.sh
```

<br/>


### 1.6.2 Web端Server安装
web端服务我们规划在node1（192.168.33.9）节点，我们首先把`azkaban-web-server-3.73.1.tar.gz`包上传到node1。

#### 1 解压
在Azkaban的web端节点，解压`azkaban-web-server-3.73.1.tar.gz`包:
```bash
tar -zxf azkaban-web-server-3.73.1.tar.gz
```

#### 2 配置[azkaban.properties](https://azkaban.readthedocs.io/en/latest/configuration.html#azkaban-web-server-configurations)
进入到 ` azkaban-web-server-3.73.1/conf ` 文件夹下，修改：
```yaml
# Azkaban Personalization Settings
azkaban.name=YGXB-Azkaban
azkaban.label=YGXB Azkaban
# 十六进制值，默认为 #FF3601（红色）。允许您为Azkaban UI设置样式颜色
azkaban.color=#fc5b13
# Azkaban将显示的时区
default.timezone.id=Asia/Shanghai

azkaban.default.servlet.path=/index
# 设置ui的css和javascript文件的目录
web.resource.dir=web/
# Directory where viewer plugins are installed
viewer.plugin.dir=plugins/viewer
# Azkaban UserManager class
user.manager.class=azkaban.user.XmlUserManager
user.manager.xml.file=conf/azkaban-users.xml
# Loader for projects
executor.global.properties=conf/global.properties
azkaban.project.dir=projects

# Velocity dev mode
velocity.dev.mode=false
# Azkaban Jetty server properties.
jetty.use.ssl=false
jetty.maxThreads=25
jetty.port=8081
# ssl端口
#jetty.ssl.port=8443
# 密钥库文件。使用绝对路径。keytool -keystore keystore -alias jetty -genkey -keyalg RSA
#jetty.keystore=/app/disk1/keystore
# jetty密码（ssl文件密码）
#jetty.password=
# 密钥密码（jetty主密码与keystore文件相同）
#jetty.keypassword=
# The trust store（SSL文件名）
#jetty.truststore=
# The trust password（SSL文件密码）
#jetty.trustpassword=
jetty.connector.stats=true

# Azkaban Executor settings
executor.port=12331
executor.host=ip/hostname
executor.connector.stats=true
# mail settings
# azkaban用于发送电子邮件的地址
#mail.sender=xxx@xxx.com
# 电子邮件服务主机
#mail.host=smtp.xxx.com
# 电子邮件服务用户名
#mail.user=
# 电子邮件用户名密码
#mail.password=
job.failure.email=
job.success.email=
# User facing web server configurations used to construct the user facing server URLs. They are useful when there is a reverse proxy between Azkaban web servers and users.
# enduser -> myazkabanhost:443 -> proxy -> localhost:8081
# when this parameters set then these parameters are used to generate email links.
# if these parameters are not set then jetty.hostname, and jetty.port(if ssl configured jetty.ssl.port) are used.
# azkaban.webserver.external_hostname=myazkabanhost.com
# azkaban.webserver.external_ssl_port=443
# azkaban.webserver.external_port=8081

# JMX stats
# 每个作业可以请求的最大初始内存量。此验证在项目上载时执行
#job.max.Xms=1GB
# 每个作业可以请求的最大内存量。此验证在项目上载时执行
#job.max.Xmx=2GB
# 缓存目录
cache.directory=cache
#Project Manager Settings
# 上载项目时使用的临时目录
project.temp.dir=temp
# 清理前保留的未使用项目版本数
project.version.retention=3
# 自动将项目的创建者作为代理用户添加到项目中
creator.default.proxy=true
# 防止除具有Admin角色的人以外的任何人创建新项目
lockdown.create.projects=false
# 防止除管理员用户和具有上传项目权限的用户以外的任何人
lockdown.upload.projects=false
# 会话时间以毫秒为单位
session.time.to.live=86400000
# 被驱逐前的最大会话数
max.num.sessions=10000

# Azkaban mysql settings by default. Users should configure their own username and password.
database.type=mysql
mysql.port=3306
mysql.host=node1
mysql.database=airflow
mysql.user=root
mysql.password=123456
# Azkaban Web客户端可以打开到数据库的连接数
mysql.numconnections=100

#Multiple Executor
# azkaban在多执行器模式下运行设置为true
azkaban.use.multiple.executors=true
# 调度时使用的常见硬分隔符列表。要从StaticRemaining，FlowSize，MinimumFreeMemory和CpuStatus中选择。过滤顺序无关紧要
azkaban.executorselector.filters=StaticRemainingFlowSize,MinimumFreeMemory,CpuStatus
# 用于对给定流的可用执行程序进行排名的整数权重。目前，{ComparatorName}可以是NumberOfAssignedFlowComparator，Memory，LastDispatched和CpuUsage作为ComparatorName。
# 例如： - azkaban.executorselec tor.comparator.Memory = 2
azkaban.executorselector.comparator.Memory=1
azkaban.executorselector.comparator.NumberOfAssignedFlowComparator=1
azkaban.executorselector.comparator.LastDispatched=1
azkaban.executorselector.comparator.CpuUsage=1
# 应该从Web服务器初始化启用队列处理器
azkaban.queueprocessing.enabled=true
# 可以在Web服务器上排队的最大流量
azkaban.webserver.queue.size=100000
# 无执行程序统计信息刷新时可以处理的最长时间（以毫秒为单位）	
azkaban.activeexecutor.refresh.milisecinterval=50000
# 在没有执行程序统计信息刷新的情况下可以处理的最大排队流数
azkaban.activeexecutor.refresh.flowinterval=5
# 刷新执行程序统计信息的最大线程数
azkaban.executorinfo.refresh.maxThreads=5

```

#### 3 配置 [azkaban-users.xml](https://azkaban.readthedocs.io/en/latest/userManager.html#xmlusermanager)
permissions | 说明
:---- | :----
ADMIN	        | 授予Azkaban所有物品的所有权限。
READ	        | 为用户提供对每个项目及其日志的只读访问权限
WRITE	        | 允许用户上传文件，更改作业属性或删除任何项目
EXECUTE	        | 允许用户触发任何流的执行
SCHEDULE	    | 用户可以为任何流添加或删除计划
CREATEPROJECTS  | 如果项目创建被锁定，则允许用户创建新项目


修改自己的用户信息
```xml
<azkaban-users>
  <user username="azkaban" password="azkaban" groups="admin"  />
  <user username="yore" password="yore" groups="admin" />
  <user username="user1" password="user1***" groups="mydepart" />
  <user username="user2" password="user2***" groups="mydepart" />
  <user username="metrics" password="metrics" roles="metrics" />

  <group name="admin" roles="admin" />
  <group name="mydepart" roles="mydepart" />

  <!-- role：ADMIN、READ、WRITE、EXECUTE、SCHEDULE、CREATEPROJECTS -->
  <role name="admin" permissions="ADMIN"/>
  <role name="mydepart" permissions="READ,WRITE,EXECUTE"/>
  <role name="metrics" permissions="METRICS"/>
  
</azkaban-users>
```

#### 4 启动
**注意**：启动顺序一定是先启动`azkaban-exec-server`，再启动`azkaban-web-server`
```bash
cd azkaban-web-server-3.73.1
bin/start-web.sh
```

查看启动信息
```bash
# 查看日志。start-exec.sh启动时会生成一个 webServerLog_* 开头的日志文件，可以查看启动信息
# 可能会看到这个错误信息： ERROR [PluginCheckerAndActionsLoader] [Azkaban] plugin path plugins/triggers doesn't exist! 
# 这个信息可以不用处理，服务是可以正常使用的
tail -f webServerLog_*.out

# jps可以查看到 AzkabanWebServer 进程
jps

```

#### 5 关闭
如果需要重启集群或者关闭集群时，可以执行如下命令：
```bash
bin/shutdown-web.sh 
```

#### 6 访问
**浏览器访问https://node1:8081/**
* ADMIN权限的用户： **username**=azkaban  **password**=azkaban  
或者 **username**=yore  **password**=yore
* METRICS权限用户： **username**=odps  **password**=odps


<br/>


# 2 使用
登陆后界面如下
![Azkaban index](image/azkaban-index.png)
首页可看到有六个菜单：
* Projects                  可以创建工程，所有的flow将在工程中运行
* Scheduling                显示定时任务
* Executing                 显示当前运行的任务
* History                   显示历史运行任务
* Flow Trigger Schedule     流触发时间表
* Documentation             文档

### Project部分
创建工程：
一个工程可以包含一个或多个flows，一个flow包含多个job。
Job是我们想在Azkaban中运行的一个进程，**可以是简单的Linux命令，可以是Java程序，也可以是复杂的shell脚本**，
如果我们安装了插件，也可以运行插件。
一个Job可以依赖于另一个Job，这种多个Job和他们的依赖组成的图表叫做Flow。

* 点击绿色的按钮`+ Create Project`，输入Project的`名字`和`说明`，点击 蓝色的 `Create Project`

* 会进入到创建的Project详细页面

![create-project](image/azkaban-create-project.png)
    + Flows         工作流程，由多个Job组成
    + Permissions   权限管理
    + Project Logs  工作日志
    
#### Job的创建
创建一个以`.job`结尾的文本文件，例如创建一个Job，用来打印 Hello World，名字叫做 hello-world.job。

输入如下内容，`type=command`表示使用的是Unix原生命令执行，
```bash
#hello-world.job
type=command
command=echo 'Hello World!'
```

将上面的文件打包为`zip`，然后通过`Azkaban Web Client`页面上传压缩包。
<kbd>Upload</kbd> -> <kbd>选择文件</kbd> -> <kbd>Upload</kbd>

`Execute Flow`
![execute-flow-hello-world](image/azkaban-execute-flow-hello-world.png)

`Schedule`: 执行的方式，默认为每分钟执行一次
`Execute`: 开始执行

#### 查看Job
<kbd>Project</kbd> -> <kbd>test</kbd> -> <kbd>Flows / hello-world</kbd>
 -> <kbd>选择文件</kbd> -> <kbd>Executions</kbd>  -> <kbd>Execution Id 例如33</kbd>
 -> <kbd>Job List</kbd> -> <kbd>Details</kbd>
 
可以在日志中查看到打印的内容
```log
28-05-2019 10:21:19 CST hello-world INFO - Starting job hello-world at 1559010079954
28-05-2019 10:21:19 CST hello-world INFO - job JVM args: '-Dazkaban.flowid=hello-world' '-Dazkaban.execid=33' '-Dazkaban.jobid=hello-world'
28-05-2019 10:21:19 CST hello-world INFO - user.to.proxy property was not set, defaulting to submit user azkaban
28-05-2019 10:21:19 CST hello-world INFO - Building command job executor. 
28-05-2019 10:21:19 CST hello-world INFO - Memory granted for job hello-world
28-05-2019 10:21:19 CST hello-world INFO - 1 commands to execute.
28-05-2019 10:21:19 CST hello-world INFO - cwd=/root/.su/azkaban-3.70.2/azkaban-exec-server-3.70.2/executions/33
28-05-2019 10:21:19 CST hello-world INFO - effective user is: azkaban
28-05-2019 10:21:19 CST hello-world INFO - Command: echo 'Hello World!'
28-05-2019 10:21:19 CST hello-world INFO - Environment variables: {JOB_OUTPUT_PROP_FILE=/root/.su/azkaban-3.70.2/azkaban-exec-server-3.70.2/executions/33/hello-world_output_4545773749185721534_tmp, JOB_PROP_FILE=/root/.su/azkaban-3.70.2/azkaban-exec-server-3.70.2/executions/33/hello-world_props_7818103133171060093_tmp, KRB5CCNAME=/tmp/krb5cc__test__hello-world__hello-world__33__azkaban, JOB_NAME=hello-world}
28-05-2019 10:21:19 CST hello-world INFO - Working directory: /root/.su/azkaban-3.70.2/azkaban-exec-server-3.70.2/executions/33
28-05-2019 10:21:19 CST hello-world INFO - Hello World!
28-05-2019 10:21:19 CST hello-world INFO - Process completed successfully in 0 seconds.
28-05-2019 10:21:19 CST hello-world INFO - output properties file=/root/.su/azkaban-3.70.2/azkaban-exec-server-3.70.2/executions/33/hello-world_output_4545773749185721534_tmp
28-05-2019 10:21:19 CST hello-world INFO - Finishing job hello-world at 1559010079981 with status SUCCEEDED
```
### 多Job工作流
多个Job组成一个Flow，在定义 Job时只需要指定dependencies参数
例如
a_canal     Mysql数据增量导入Kafka
b_init      Mysql数据批量导入Kudu
c_flink     Kafka数据实时同步到Kudu
d_flink     Kafka数据实时指标计算

0 15 0 * * ?

#### a_canal.job
```bash
type=command
command=echo 'Mysql数据增量导入Kafka # sh bin/startup.sh'
```

#### b_init.job
```bash
type=command
command=echo 'Mysql数据批量导入Kudu # flink run -c com.yore x_a.jar '
dependencies=a_canal
```

#### c_flink .job
```bash
type=command
command=echo 'Kafka数据实时同步到Kudu # flink run -c com.yore x_c.jar '
dependencies=b_init
```

#### d_flink.job
```bash
type=command
command=echo 'Kafka数据实时指标计算 # flink run -c com.yore x_d.jar'
dependencies=b_init
```


