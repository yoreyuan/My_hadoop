[Apache Zookeeper](http://zookeeper.apache.org/)
==========
[Doc](http://zookeeper.apache.org/doc/current/index.html)

# 一 Overview
官网首页是这样描述zk的

> ZooKeeper is a centralized service for maintaining configuration information, naming, providing distributed synchronization, and providing group services. All of these kinds of services are used in some form or another by distributed applications. Each time they are implemented there is a lot of work that goes into fixing the bugs and race conditions that are inevitable. Because of the difficulty of implementing these kinds of services, applications initially usually skimp on them, which make them brittle in the presence of change and difficult to manage. Even when done correctly, different implementations of these services lead to management complexity when the applications are deployed.

## 1.1 Zookeeper设计目的
* **最终一致性**：client不论连接到那个Server，展示给它的都是同一个视图。
* **可靠性**：具有简单、健壮、良好的性能、如果消息m被到一台服务器接收，那么消息m将被所有服务器接收。
* **实时性**：Zookeeper保证客户端将在一个时间间隔范围内获得服务器的更新信息，或者服务器失效的信息。但由于网络延时等原因，Zookeeper不能保证两个客户端能同时得到刚更新的数据，如果需要最新数据，应该在读数据之前调用sync()接口。
* **等待无关（wait-free）**：慢的或者失效的client不得干预快速的client的请求，使得每个client都能有效的等待。
* **原子性**：更新只能成功或者失败，没有中间状态。
* **顺序性**：包括全局有序和偏序两种：全局有序是指如果在一台服务器上消息a在消息b前发布，则在所有Server上消息a都将在消息b前被发布；偏序是指如果一个消息b在消息a后被同一个发送者发布，a必将排在b前面。

## 1.2 Zookeeper工作原理
在zookeeper的集群中，各个节点共有下面3种角色和4种状态：
* 角色：leader,follower,observer
* 状态：leading,following,observing,looking

Zookeeper的核心是原子广播，这个机制保证了各个Server之间的同步。实现这个机制的协议叫做Zab协议（ZooKeeper Atomic Broadcast protocol）。
Zab协议有两种模式，它们分别是恢复模式（Recovery选主）和广播模式（Broadcast同步）。当服务启动或者在领导者崩溃后，Zab就进入了恢复模式，当领导者被选举出来，
且大多数Server完成了和leader的状态同步以后，恢复模式就结束了。状态同步保证了leader和Server具有相同的系统状态。

* **LOOKING**：当前Server不知道leader是谁，正在搜寻。
* **LEADING**：当前Server即为选举出来的leader。
* **FOLLOWING**：leader已经选举出来，当前Server与之同步。
* **OBSERVING**：observer的行为在大多数情况下与follower完全一致，但是他们不参加选举和投票，而仅仅接受(observing)选举和投票的结果。


## 1.3 Zookeeper集群节点
Zookeeper节点部署越多，服务的可靠性越高，建议部署奇数个节点，因为zookeeper集群是以宕机个数过半才会让整个集群宕机的。

Zookeeper集群是超过半数，整个集群才可用，低于半数才宕机。zookeeper默认采用quorums来支持Leader的选举。
其实quorums机制有两个作用：
1. 可以保证集群中选举出leader，且是唯一的一个，不会出现脑裂(split-brain)。
2. 当客户端更新数据时，当大多数节点更新成功，客户端就会被通知更新成功了，其他节点可以稍后再更新，以致达到数据的最终一致性

zk节点数 | 最少可用的节点数 | 可容忍失效的节点数
---- | ---- | ----
1     | 1   | 0
2     | 2   | 0
3     | 2   | 1
4     | 3   | 1
5     | 3   | 2
6     | 4   | 2
……    | ……  | ……
2n -1 | n   | n-1
2n    | n+1 | n-1


# 二 安装
## 1.1 下载
Zookeeper的下载页面为 [Download](https://www.apache.org/dyn/closer.cgi/zookeeper/)
```bash
wget http://archive.apache.org/dist/zookeeper/zookeeper-3.4.14/zookeeper-3.4.14.tar.gz -P /opt/
```

## 1.2 解压
```bash
tar -zxf zookeeper-3.4.14.tar.gz
```

## 1.3 设置环境变量
zk的每个节点都需要配置
```bash
vim ~/.bash_profile
# 添加如下配置
export ZOOKEEPER_HOME=/opt/zookeeper-3.4.14
export PATH=$PATH:$ZOOKEEPER_HOME/bin
```

使配置在当前环境下生效
```bash
source ~/.bash_profile
```

## 1.4 修改配置
选择一个节点配置，后面scp到其他节点即可。比如选择node1节点
```bash
# 复制配置模板
cp $ZOOKEEPER_HOME/conf/zoo_sample.cfg $ZOOKEEPER_HOME/conf/zoo.cfg 
# 修改配置
vim $ZOOKEEPER_HOME/conf/zoo.cfg 
```

主要配置如下
```yaml
tickTime=2000
initLimit=10
syncLimit=5
# 保存zk快照的目录，不要保存到 /tmp 下
dataDir=/var/lib/zookeeper/data
dataLogDir=/var/lib/zookeeper/logs
# zk服务进程监听的TCP端口，默认情况下，服务端会监听2181端口
clientPort=2181
# 最大客户端连接数。如果您需要处理更多客户端，请增加此值
maxClientCnxns=60
# dataDir下保留的快照数
autopurge.snapRetainCount=5
# 清除任务的时间间隔，以小时为单位。设置为0标识禁用自动清除功能
autopurge.purgeInterval=24

# server.A=B:C:D 
# 中的A是一个数字,表示这个是第几号服务器，数字可随意，能唯一标识即可
# B是这个服务器的IP地址，
# C第一个端口用来集群成员的信息交换,表示这个服务器与集群中的leader服务器交换信息的端口，
# D是在leader挂掉时专门用来进行选举leader所用的端口。
server.1= node1:2888:3888
server.2= node2:2888:3888
server.3= node3:2888:3888

```

## 1.5 将配置好的zk发送到其他各节点
```bash
scp -r $ZOOKEEPER_HOME root@node2:/opt/
scp -r $ZOOKEEPER_HOME root@node3:/opt/

```


## 1.6 创建对应的目录
```bash
mkdir -p /var/lib/zookeeper/{logs,data}
```

## 1.7 创建ServerID标识
在每个zk节点，分别写入对应节点host配置的`A`的标识

```bash
# node1节点写入 1
echo "1" > /var/lib/zookeeper/data/myid
# node2节点写入 2
echo "2" > /var/lib/zookeeper/data/myid
# node3节点写入 3
echo "3" > /var/lib/zookeeper/data/myid
```

## 1.8 启动服务
```bash
# 每个zk节点执行
$ZOOKEEPER_HOME/bin/zkServer.sh start
```

启动的时候，可以查看zk的状态。这里可以留意一下 leader 出现的节点。想一下为什么是这样的
```bash
$ZOOKEEPER_HOME/bin/zkServer.sh status
```

如果是单节点时，查询的状态为`standalone`，启动成功后，查看java进程信息，会发现有一个`QuorumPeerMain`进程。


## 1.9 zkCli
```bash
$ZOOKEEPER_HOME/bin/zkCli.sh 
```

进入后执行` h `，获取帮助，可以联系这几个命令
```
[zk: localhost:2181(CONNECTED) 7] h
ZooKeeper -server host:port cmd args
        stat path [watch]
        set path data [version]
        ls path [watch]
        delquota [-n|-b] path
        ls2 path [watch]
        setAcl path acl
        setquota -n|-b val path
        history
        redo cmdno
        printwatches on|off
        delete path [version]
        sync path
        listquota path
        rmr path
        get path [watch]
        create [-s] [-e] path data acl
        addauth scheme auth
        quit
        getAcl path
        close
        connect host:port
```

* `ls path`： 列出指定节点下的节点信息
* `stat path`：获取指定节点的状态信息。
    - **czxid** 创建该节点的事物ID
    - **ctime** 创建该节点的时间
    - **mZxid** 更新该节点的事物ID
    - **mtime** 更新该节点的时间
    - **pZxid** 操作当前节点的子节点列表的事物ID(这种操作包含增加子节点，删除子节点)
    - **cversion** 当前节点的子节点版本号
    - **dataVersion** 当前节点的数据版本号
    - **aclVersion** 当前节点的acl权限版本号
    - **ephemeralowner** 当前节点的如果是临时节点，该属性是临时节点的事物ID
    - **dataLength** 当前节点的d的数据长度
    - **numchildren** 当前节点的子节点个数
* `ls2 path`： ls和stat两个的结合
* `create  [-s] [-e] path data acl`： 创建一个节点，例如：` create -e /test 'hello'  world:anyone:a `
    + **-s** 表示是顺序节点
    + **-e** 标识是临时节点(客户端与服务端会话结束后会自动消失)
    + **path** 节点路径
    + **data** 节点数据
    + **acl** 节点权限。ZK的节点有5种操作权限，CREATE、READ、WRITE、DELETE、ADMIN 也就是 增、删、改、查、管理权限。
        - world：默认方式，相当于全世界都能访问
        - auth：代表已经认证通过的用户(cli中可以通过addauth digest user:pwd 来添加当前上下文中的授权用户)
        - digest：即用户名:密码这种方式认证，这也是业务系统中最常用的
        - ip：使用Ip地址认证
* `getAcl path`： 获取某个节点的acl
* `get path`： 获取指定节点的数据内容
* `set path data [dataVersion]`： 修改当前节点的数据内容，如果指定版本，需要和当前节点的数据版本一致。` set /test 'hello world' 0`
* `delete path  [version]`： 删除一个节点
* `rmr path`： 删除节点下的所有节点
* `listquota /test`： 查看路径节点的配额信息
* `setquota -n|-b val path`：设置节点配额（比如限制节点数据长度，限制节点中子节点个数）
    * -n 是限制子节点个数 
    * -b 是限制节点数据长度
* `delquota [-n|-b] path`： 删除节点路径的配额信息
* `history `：查看客户端这次会话所执行的所有历史命令
* `redo cmdno`：重新执行指定的历史命令，例如重新执行历史的第7个命令：`redo 7 `
* `quit`： 退出 Cli


## 1.10 关闭
```bash
$ZOOKEEPER_HOME/bin/zkServer.sh stop
```

