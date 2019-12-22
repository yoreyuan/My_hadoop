


# 1 Quickstart
## 1.下载
```bash
# win版下载：
http://dl.mongodb.org/dl/win32/x86_64

# Linux版下载
https://www.mongodb.org/dl/linux/x86_64

```

## 2.解压
然后在解压的目录下新建data、logs目录，并新建一个mongo.conf文件。
同时在logs中新建一个mongo.log日志文件
	
## 3配置mongo.conf
```bash
## 数据库路径
dbpath=D:\soft\mongodb-3.4.10\data
## 日志输出文件路径
logpath=D:\soft\mongodb-3.4.10\logs\mongo.log
## 错误日志采用追加模式
logappend=true
## 启用日志文件，默认是启用
journal=true
## 这个选项可以过滤掉一些无用的日志信息，若需要调试可以设置为false
quiet=true
## 端口号，默认为27017
port=27017
```

## 4 启动MongoDB
```bash
# 进入安装目录下的bin\
C:\Windows\system32>d:\soft\mongodb-3.4.10\bin

# cmd中运行
mongod --config "D:\soft\mongodb-3.4.10\mongo.conf" 
	
# 在浏览器中输入：
127.0.0.1:27017
# 显示如下信息，说明安装成功！
# It looks like you are trying to access MongoDB over HTTP on the native driver port.

```

## 5 创建并启动MongoDB服务
其实上一步就已经安装成功了(cmd窗口不能关闭，已关闭服务就停止了)，
但是每次进系统都要输入以上的命令，有点麻烦。
所有可以将以上的服务添加到win系统的服务中

添加服务。(类似于win命令的sc，但是服务能添加，启动会出问题，所以推荐用一下方法)
```bash
# 在4步中的cmd路径下中输入：
mongod --config "D:\soft\mongodb-3.4.10\mongo.conf" --install --serviceName "MongoDB"

# 启动服务：
net start MongoDB
```

## 6 如果要卸载掉服务
```bash
mongod.exe --remove --serviceName "MongoDB"  
sc delete MongoDB

```


## 7 安装MongoDB的可视化工具
这里选择安装Robo T3 ，下载地址为 https:robomongo.org

点击下一步下一步就可以成功安装，安装后运行

在MongoDB Connections 中点击第一行的Create，在弹出的框中
* Name: 起一个连接的名字 比如"localhost_mongodb"
* Address: 127.0.0.1
* 端口号默认：27017
	


## 8 登陆查看
```sql
$MONGODB_HOME/bin/mongo
-- 显示库
show dbs

-- 使用库
use 库名

-- 查看表
show collections
```


----------------------------------


$gt:大于
$lt:小于
$gte:大于或等于
$lte:小于或等于
不等于 $ne
in 和 not in ($in $nin)
取模运算$mod
$all和$in类似，但是他需要匹配条件内所有的值
$size是匹配数组内的元素数量的
$exists用来判断一个元素是否存在：
$type 基于 bson type来匹配一个元素的类型，像是按照类型ID来匹配，不过我没找到bson类型和id对照表。
$not 元操作符 取反


匹配 /* 数字 */
	^[/][*][\s]([\d]*)[\s][*][/]$
	(^[\s]*[\r][\n])([\s]$)


```bash
# 启动MongoDB服务
#/usr/local/mongo/bin/mongod -dbpath /opt/mongo-db -logpath /var/log/mongo/mongo.log -logappend -fork -port 27017 --bind_ip 192.168.100.170
#/usr/local/mongo/bin/mongod -f /usr/local/mongo/mongo.conf
/usr/local/mongo/bin/mongod --config /usr/local/mongo/mongo.conf

# /usr/local/mongo/mongo.conf 如下
dbpath=/opt/mongo-db
logpath=/var/log/mongo/mongo.log
logappend=true
bind_ip=0.0.0.0
journal=true
port=27017
fork=true
maxConns=30000
 
```
	
	
mo

关闭MongoDB服务
	/usr/local/mongo/bin/mongod -shutdown -dbpath /opt/mongo-db
	
登陆Mongo服务
	./mongo 192.168.100.170:27017

查看数据库
	db.stats(); 
	复制集状态查询：rs.status()
		self:只会出现在执行rs.status()命令的成员里
		uptime:从本节点 网络可达到当前所经历的时间
		lastHeartbeat：当前服务器最后一次收到其心中的时间
		Optime & optimeDate:命令发出时oplog所记录的操作时间戳
		pingMs: 网络延迟
		syncingTo: 复制源
		stateStr:
		可提供服务的状态：primary, secondary, arbiter
		即将提供服务的状态：startup, startup2, recovering
		不可提供服务状态：down,unknow,removed,rollback,fatal

	查看服务状态详情:   db.serverStatus()
						db.serverStatus().connections 

查看数据库表
	db.m_underwrite_info.stats(); 
	

-----------
比较信息

设置用户名和密码
db.addUser("mongodb","mongo")


修改字段值
	db.getCollection('m_compare_info').update({},{$rename:{"new_flag":"0"}},false,true)
	db.getCollection('m_underwrite_info').update({}, {$set: {"rh_term_a_rtn_flag": NumberInt(1)}}, false, true)

修改值
	db.getCollection('m_compare_info').update({"new_flag":"1"},{"new_flag":"0"})

删除一条数据
	db.getCollection('m_underwrite_info').remove({"borrower_cert_no":"11010219790115234X"})
删除一个字段
	db.collection.update(criteria,objNew,upsert,multi)
		参数说明：
		criteria：查询条件
		objNew：update对象和一些更新操作符
		upsert：如果不存在update的记录，是否插入objNew这个新的文档，true为插入，默认为false，不插入。
		multi：默认是false，只更新找到的第一条记录。如果为true，把按条件查询出来的记录全部更新。
	db.m_underwrite_info.update({'term':'C20180704'},{$unset:{'fh_drfr_pjws_json':''}},false,true)

添加一个字段
	db.url.update({}, {$set: {content:""}}, {multi: 1})。

删除表
	mongo
	db.getCollection('m_borrower_stat_data').drop()
	db.getCollection('m_compare_info').drop()
	db.getCollection('m_underwrite_info').drop()
	show collections

导出表中的数据
	mongoexport -h 127.0.0.1 -d yg_mongo -c m_underwrite_info --type csv -o /root/m_underwrite_info.csv
	./mongoexport -h 192.168.100.170 -d yg_test -c m_cpws_info --type json -o /root/m_cpws_info.json


导入
	./mongoimport --db yg_mongo --host 192.168.100.170 --port 27017 --collection m_underwrite_info_1128 --file /root/m_underwrite_info_20181128.json
	./mongoimport --db yg_mongo --host 192.168.100.170 --port 27017 --collection m_compare_info_1128 --file /root/m_compare_info_20181128.json
	./mongoimport --db yg_mongo --host 192.168.100.170 --port 27017 --collection m_borrower_stat_data_1128 --file /root/m_borrower_stat_data_20181128.json
	D:/soft/mongodb-3.4.10/bin/mongoimport --db yg_mongo --host 127.0.0.1 --port 27017 --collection m_cpws_info --file ./m_cpws_info.json

	./mongoimport --db yg_mongo --host 192.168.100.170 --port 27017 --collection m_underwrite_info_1207 --file /root/m_underwrite_info_20181207.json

备份/还原  
mongodump --host 192.168.100.170 -u yg_mongo -p 123456 -d yg_mongo -o '/root/Backup/mongo_data/mongo_2019-11-30' --authenticationDatabase admin

mongodump --host 192.168.100.170 -u yg_mongo -p 123456 -d yg_mongo -o '/root/Backup/mongo_data/mongo_2019-11-30'
mongodump --host 192.168.100.170 -u yg_test -p 123456 -d yg_test -o '/root/Backup/mongo_data/mongo_2019-11-30'
	备份：
	（全库：）mongodump -h 192.168.100.170:27017   -o db_data_back/ --gzip
	（yg_mongo库） mongodump -h 192.168.100.170:27017 -d yg_mongo -o db_data_back/

	恢复：
mongorestore -h 192.168.100.170:27017 -d yg_mongo --drop --gzip /root/db_data_back/yg_mongo/
		


去重
	db.getCollection('m_compare_info').distinct('borrower_code',{'new_flag':'1','strategy_class_code':'DSJ','hit_flag':{'$in':['1','2','3','4','6']}})
	db.getCollection('m_borrower_stat_data').distinct('borrower_code',{'risk_bigdata_num':{$gt:0}})
	db.getCollection('m_underwrite_info').distinct("relater_cert_no",{"term":"C20181101"})

条件查询并统计个数
	db.runCommand({ distinct: "m_borrower_stat_data", key: "borrower_code", query: {'risk_monitor_peak_num':{$gt:0}}}).values.length
	db.getCollection('m_borrower_stat_data').count({'risk_monitor_peak_num':{$gt:0}})

复制表数据
	db.m_underwrite_info.copyTo("m_underwrite_info_copy")

	db.cloneCollection("192.168.110.73", "m_underwrite_info_t2", {"term" : "C20180305"})

查询某个字段不为空
	db.getCollection('20180408').find({"resultJson.person.personArray.cpws":
    {$nin:[[]]}})




********
## 修改Mongo最大连接数
mongoDB是使用文档方式存储的，而取决于mongoDB的连接池数量也是这个open files的值，
mongoDB的机制是 open files*80% + 16 = 允许最大连接数。
open files默认值是1024，既默认的最大连接数是800多
```bash
# ①查看系统的open file
	# ulimit -a
	或者 ulimit -n

# ②修改系统的open file
# vim /etc/security/limits.conf
* soft nofile 65535
* hard nofile 65535

# ③查看Mongo是否修改过来
/data/mongodbtest/mongodb-linux-x86_64-3.4.4/bin/mongo 192.168.100.170:27017
# db.serverStatus().connections
```


## 创建含有密码的库
```sql
-- (1)登陆mongo
$MONGO_HOME/mongo 192.168.100.170:27017

-- (2)查看当前的库
--  此时会查看默认的含有两个库，admin和local
repset:PRIMARY> show dbs

-- (3)使用admin库
repset:PRIMARY> use admin
repset:PRIMARY> show collections
system.users
system.version

-- (4)添加一个超级管理员
repset:PRIMARY> db.addUser('root','root')

-- 新版本不支持这个函数操作
repset:PRIMARY> db.createUser({user:"root",pwd:"123456", roles:[{role:"userAdminAnyDatabase",db:"admin"},{role:"readWriteAnyDatabase",db:"admin"}]})
-- 先退出 (ctrl+c)程序，测试重启服务后再次连接MongoDB是否需要按提示输入用户名、密码进行操作。

-- (5)开启安全性验证:
--   在mongodb路径的bin目录下，执行 mongod --dbpath  /data/mongodbtest/replset/data  --auth
mongod --dbpath /u01/mongoDB/mongodb/db/ --auth

-- (6)设定自己的库的用户名和密码：
repset:PRIMARY> use rrx_anti_fraud
repset:PRIMARY> db.createUser({user:"root",pwd:"123456",roles: [ { role: "dbOwner", db: "rrx_anti_fraud" } ]})
repset:PRIMARY> use risk_control_cloud_v1
repset:PRIMARY> db.createUser({user:"riskcontrolcloud",pwd:"rrx360credit",roles: [ { role: "dbOwner", db: "risk_control_cloud_v1" } ]})
repset:PRIMARY> use rrx_unicorn
repset:PRIMARY> db.createUser({user:"rrx_unicorn",pwd:"rrx_unicorn",roles: [ { role: "dbOwner", db: "rrx_unicorn" } ]})
repset:PRIMARY> use rrx_unicorn_regression
repset:PRIMARY> db.createUser({user:"rrx_unicorn_regression",pwd:"rrx_unicorn_regression",roles: [ { role: "dbOwner", db: "rrx_unicorn_regression" } ]})
repset:PRIMARY> use unicorn
repset:PRIMARY> db.createUser({user:"",pwd:"",roles: [ { role: "dbOwner", db: "unicorn" } ]})
	
use monitor
db.createUser({user:"用户名",pwd:"密码",roles: [ { role: "monitorDBRoles", db: "monitor" } ]})


```

# mongoDB创建用户名密码登录和其他一些设置
```bash
# 启动MongoDB服务
/usr/local/mongo/bin/mongod --config /usr/local/mongo/mongo.conf

# /usr/local/mongo/mongo.conf 如下
dbpath=/opt/mongo-db
logpath=/var/log/mongo/mongo.log
logappend=true
bind_ip=0.0.0.0
journal=true
port=27017
fork=true
# 开启认证
auth=true
maxConns=30000
 
```

## 创建用户管理员
```sql
> use admin
switched to db admin

> db.createUser({user:"root",pwd:"ygmongo",roles:["userAdminAnyDatabase"]})
Successfully added user: { "user" : "root", "roles" : [ "userAdminAnyDatabase" ] }

-- 返回1表示登录成功
> db.auth("root","ygmongo")
1

```

## 创建数据库用户
```sql
> use yg_mongo
switched to db yg_mongo

> db.createUser({user:"yg_mongo",pwd:"123456",roles:["readWrite"]})
Successfully added user: { "user" : "yg_mongo", "roles" : [ "readWrite" ] }

> use yg_test
switched to db yg_test
> db.createUser({user:"yg_test",pwd:"123456",roles:["readWrite"]})
Successfully added user: { "user" : "yg_mongo", "roles" : [ "readWrite" ] , db: "yg_test"}

```

Built-In Roles（内置角色）：
* 1. 数据库用户角色：read、readWrite;
* 2. 数据库管理角色：dbAdmin、dbOwner、userAdmin；
* 3. 集群管理角色：clusterAdmin、clusterManager、clusterMonitor、hostManager；
* 4. 备份恢复角色：backup、restore；
* 5. 所有数据库角色：readAnyDatabase、readWriteAnyDatabase、userAdminAnyDatabase、dbAdminAnyDatabase
* 6. 超级用户角色：root  
* // 这里还有几个角色间接或直接提供了系统超级用户的访问（dbOwner 、userAdmin、userAdminAnyDatabase）
* 7. 内部角色：__system


具体角色的功能： 
* `Read`：允许用户读取指定数据库
* `readWrite`：允许用户读写指定数据库
* `dbAdmin`：允许用户在指定数据库中执行管理函数，如索引创建、删除，查看统计或访问system.profile
* `userAdmin`：允许用户向system.users集合写入，可以找指定数据库里创建、删除和管理用户
* `clusterAdmin`：只在admin数据库中可用，赋予用户所有分片和复制集相关函数的管理权限。
* `readAnyDatabase`：只在admin数据库中可用，赋予用户所有数据库的读权限
* `readWriteAnyDatabase`：只在admin数据库中可用，赋予用户所有数据库的读写权限
* `userAdminAnyDatabase`：只在admin数据库中可用，赋予用户所有数据库的userAdmin权限
* `dbAdminAnyDatabase`：只在admin数据库中可用，赋予用户所有数据库的dbAdmin权限。
* `root`：只在admin数据库中可用。超级账号，超级权限


## 加入开机自启
```bash
vim /etc/rc.local
# 在下面添加
/usr/local/mongo/bin/mongod --config /usr/local/mongo/mongo.conf
```

**或者**，通过服务服务方式
```bash
# 1 新建一个mongodb的服务文件
vim /lib/systemd/system/mongodb.service

```

2 添加如下配置
```bash
[Unit]
Description=mongodb
After=network.target remote-fs.target nss-lookup.target

[Service]
Type=forking
ExecStart=/usr/local/mongo/bin/mongod --config /usr/local/mongo/mongo.conf
ExecReload=/bin/kill -s HUP $MAINPID
ExecStop=/usr/local/mongo/bin/mongod  --shutdown --config /usr/local/mongo/mongo.conf
PrivateTmp=true

[Install]
WantedBy=multi-user.target
```

```bash
# 3 设置权限
chmod 754  /lib/systemd/system/mongodb.service

# 4 开机启动    
systemctl enable mongodb.service 

# 5 启动服务
systemctl start mongodb.service

# 6 查看状态
systemctl status mongodb.service

# 7 关闭服务
systemctl stop mongodb.service  
 

```





