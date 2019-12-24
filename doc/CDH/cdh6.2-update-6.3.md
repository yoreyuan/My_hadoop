CDH 6.2.0 升级到 CDH 6.3.1 
----

# 备份
```bash
# 0 查看系统
lsb_release -a

# 1 创建一个顶级备份目录。(各个节点执行)
export CM_BACKUP_DIR="`date +%F`-CM6.2.0"
echo $CM_BACKUP_DIR
mkdir -p $CM_BACKUP_DIR

# 2 备份 Agent 目录和运行时状态。（Agent 节点执行）
sudo -E tar -zcPf $CM_BACKUP_DIR/cloudera-scm-agent.tar.gz --exclude=*.sock /etc/cloudera-scm-agent /etc/default/cloudera-scm-agent /var/run/cloudera-scm-agent /var/lib/cloudera-scm-agent

# 3 备份现有的存储库目录。
sudo -E tar -zcPf $CM_BACKUP_DIR/repository.tar.gz /etc/yum.repos.d

# 4 备份Cloudera Management Service
## 4.1 在 Service Monitor 角色的机器上备份以下目录： (cdh 页面可以看到：例如 cdh2 节点)
sudo cp -rp /var/lib/cloudera-service-monitor /var/lib/cloudera-service-monitor-`date +%F`-CM6.2.0
## 4.2 在 Host Monitor  角色的机器上备份以下目录： (例如 cdh3 节点)
sudo cp -rp /var/lib/cloudera-host-monitor /var/lib/cloudera-host-monitor-`date +%F`-CM6.2.0
## 4.3 在  Event Server 角色的机器上备份以下目录： (例如 cdh3 节点)
sudo cp -rp /var/lib/cloudera-scm-eventserver /var/lib/cloudera-scm-eventserver-`date +%F`-CM6.2.0

# 5 停止 Cloudera Manager Server 和 Cloudera Management Service
## 5.1 CDH 页面 停止 Cloudera Management Service
## 5.2 在 Cloudera Manager Server 机器上执行如下命令关闭 Cloudera Manager Server
sudo systemctl stop cloudera-scm-server

# 6 备份 Cloudera Manager 数据库
## 6.1 在元数据节点 查看数据库配置信息
cat /etc/cloudera-scm-server/db.properties
## 6.2 备份 MySQL 数据
##  数据库默认的有 scm、rman、nav、navms、amon，同时如果有也可以把剩余的 hue、metastore、sentry、oozie 备份
mysqldump --databases database_name --host=cdh1 --port=3306 -u scm -p > $CM_BACKUP_DIR/database_name-backup-`date +%F`-CM6.2.0.sql

# 7 备份 Cloudera Manager Server
sudo -E tar -zcPf $CM_BACKUP_DIR/cloudera-scm-server.tar.gz /etc/cloudera-scm-server /etc/default/cloudera-scm-server

```

# 升级 Cloudera Manager
## 升级 Server
如果集群环境可以访问 [https://archive.cloudera.com](https://archive.cloudera.com)，则可以直接使用官网提供的公共存储库。
package manager 会使用这个下载并安装最新版本的 Cloudera Manager 软件包，

如果无法访问 Internet，需要配置自己内网的资源镜像服务，将 [package repository](https://docs.cloudera.com/documentation/enterprise/upgrade/topics/cm_ig_create_local_package_repo.html#internal_package_repo)
根据文档说明下载到本地，例如 [http://MyWebServer:port/cloudera-repos](#)。

如果集群节点比较多时，**建议使用第二种离线方式**。

将各个节点的存储库文件`/etc/yum.repos.d/cloudera-manager.repo`改为如下
```bash
[cloudera-manager]
# Packages for Cloudera Manager
name=Cloudera Manager
baseurl=https://archive.cloudera.com/cm6/6.3.1/redhat7/yum/
gpgkey=https://archive.cloudera.com/cm6/6.3.1/redhat7/yum/RPM-GPG-KEY-cloudera
gpgcheck=1

```

```bash
# 查看确定哪些软件包可以安装或升级
yum deplist cloudera-manager-agent

```

升级 Cloudera Manager Server
* 停止 `Cloudera Management Service` 。登陆到**Cloudera Manager Admin**，关闭此服务。
* 停止 `Cloudera Manager Agent`。因为前面已经停止了`Cloudera Manager Server`，这里直接停止 Agent 服务。
```bash
sudo systemctl stop cloudera-scm-agent
```
* 升级包
```bash
# 1 clean yum
sudo yum clean all

# upgrade server。在 server 节点执行
# 如果中间有提示 db.properties ，回答 N
# 如果有提示 GPG key ，回答 Y
# 如果中间获取资源时失败可以重新执行
sudo yum upgrade cloudera-manager-server cloudera-manager-daemons cloudera-manager-agent 

# upgrade agent。在其余 agent 执行
sudo yum upgrade cloudera-manager-daemons cloudera-manager-agent 

# 验证是否升级安装成功
rpm -qa 'cloudera-manager-*'

# 启动 Cloudera Manager Agent.（可选，升级后服务已自动启动）
sudo systemctl start cloudera-scm-agent

# 启动 Cloudera Manager Server
sudo systemctl start cloudera-scm-server

# 使用 Web 浏览器打开 Cloudera Manager Admin 控制页面。需要一段时间（几分钟）才能启动。
# 根据提示完成 Agent 服务的升级。
# 中间遇到问题可以查看如下日志
#  server 日志： tail -f /var/log/cloudera-scm-server/cloudera-scm-server.log
#  Agetn 日志：  tail -f /var/log/cloudera-scm-agent/cloudera-scm-agent.log
#  or : tail -f /var/log/messages
#  打开页面后 Host Inspector 时有警告可以参考 
#   [CDH 6.2.0 或 6.3.0 安装实战及官方文档资料链接](https://blog.csdn.net/github_39577257/article/details/92471365)
http://cdh1:7180/cmf/upgrade

```

点击 **显示检查器结果** 查看检查结果，如果提示：

警告提示 `The user 'kudu' is not part of group 'hive' on the following hosts:`
```bash
# 将 kudu 用户添加的已存在的 hive 组中
sudo usermod -G hive kudu

# 查看 
cat /etc/group | grep hive
groups kudu

```

## 升级 Agents
当 server 节点升级完毕，Agent 也升级完毕之后，访问 [http://cdh1:7180/cmf/upgrade](http://cdh1:7180/cmf/upgrade)。首先查看 升级的 Agent的主机列表信息，如果有某些节点有问题，可以通过前面提到的日志信息进行排查。然后必须要对 Host 进行检查，点击检查之后，如果没有问题会是一个绿色的对勾，如果有问题，点击 【显示检查器结果】查看警告问题的原因进行排查。

例如当点击 **显示检查器结果** 查看检查结果，警告提示 `The user 'kudu' is not part of group 'hive' on the following hosts:`，可以将 kudu用户添加到 hive 的用户组里。其它的原因可以查看我在 CDH 安装的 blog [CDH 6.2.0 或 6.3.0 安装实战及官方文档资料链接](https://blog.csdn.net/github_39577257/article/details/92471365)

```bash
# 将 kudu 用户添加的已存在的 hive 组中。
sudo usermod -G hive kudu

# 查看 
cat /etc/group | grep hive
groups kudu
```

问题排除后，如下图
![在这里插入图片描述](https://img-blog.csdnimg.cn/20191218145223259.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L2dpdGh1Yl8zOTU3NzI1Nw==,size_16,color_FFFFFF,t_70)

审核更改 ↓
![审核更改](https://img-blog.csdnimg.cn/20191218150055445.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L2dpdGh1Yl8zOTU3NzI1Nw==,size_16,color_FFFFFF,t_70)

重启 Cloudera Management ↓
![在这里插入图片描述](https://img-blog.csdnimg.cn/20191218150148930.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L2dpdGh1Yl8zOTU3NzI1Nw==,size_16,color_FFFFFF,t_70)

命令详细信息 ↓
![命令详细信息](https://img-blog.csdnimg.cn/20191218150236561.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L2dpdGh1Yl8zOTU3NzI1Nw==,size_16,color_FFFFFF,t_70)

Upgrade Cloudera Manager ↓。如果没有问题会显示如下页面，最后提示我们访问 Home Page，进行后续的操作。
![Upgrade Cloudera Manager](https://img-blog.csdnimg.cn/20191218150312615.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L2dpdGh1Yl8zOTU3NzI1Nw==,size_16,color_FFFFFF,t_70)


## After
进入 Cloudera Manager Admin 页面，调整页面提示的配置。
完整之后，重启响应的 组件 服务



# 升级 CDH

完整可以查看我的 blog [CDH之JDK 版本升级(Open JDK1.8)和cdh升级](https://blog.csdn.net/github_39577257/article/details/91562909#2)


