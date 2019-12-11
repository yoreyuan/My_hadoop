HDFS 文件权限问题
------

# 1 关于文件权限
```bash
# 1 Linux 的文件信息
[hive@cdh1 mysql]$ ll | grep test
drwx------ 2 mysql mysql       4096 Dec  8 20:35 flink_test
drwx------ 2 mysql mysql       4096 Dec 11 01:32 test
-rw-r--r-- 1 mysql mysql         29 Dec 11 09:40 test.csv


# 2 HDFS 的文件信息
[hive@cdh1 mysql]$ hadoop fs -ls /home
Found 5 items
drwxrwxrwx   - dr.who supergroup          0 2019-07-29 15:23 /home/flink
-rw-r--r--   3 root   supergroup      14205 2019-10-17 04:03 /home/rankQuote.csv
-rw-r--r--   3 hdfs   supergroup        329 2019-06-19 17:22 /home/sample.txt
drwxr-x---   - mysql  hive                0 2019-12-11 09:38 /home/test
-rw-r-----   3 mysql  hive               29 2019-12-11 09:40 /home/test.csv

```

关于上面信息的说明
* 第一列表示 权限许可： Permission
* 第二列。 Linux表示被引用的次数，文件默认为1 ，文件夹默认为2（Linux世界一切皆文件）。HDFS 表示的为副本数 Replication
* 第三列表示 拥有者：  Owner
* 第四列表示 组：      Group
* 第五列表示 大小：     Size
* 第六列表示 最近修改时间： Last Modified
* 第七列表示 文件或文件夹名。HDFS是绝对路径名称


权限列有由三组权限组成：所有者权限、组权限、公共权限
* 第1个字符 标识文件类型：`-` 文件、`d`文件夹、`l` 软连接
* 第2-4字符 标识所有者的权限
    * 第 2 字符，是否拥有读权限。yes=400，no=0
    * 第 3 字符，是否拥有写权限。200
    * 第 4 字符，是否拥有执行权限。 100
* 第5-7字符 标识组权限
    * 第 5 字符，是否拥有读权限。40
    * 第 6 字符，是否拥有写权限。20
    * 第 7 字符，是否拥有执行权限。 10
* 第8-10字符 标识公共权限
    * 第 8 字符，是否拥有读权限。4
    * 第 9 字符，是否拥有写权限。2
    * 第 10 字符，是否拥有执行权限。 1

# 2 Centos

## 2.1 关于 su 命令
* su 用户1： 表示将用户1的身份赋予给当前用户
* su - 用户1： 将用户1赋予当当前用户外，还会将环境设置为 用户1 登陆时的环境

可以看如下命令，执行`su hdfs` 时环境变量并没有变化，依然是原来用户的环境变量，只是拥有了 hdfs 的身份。
执行`su - hdfs`时环境变量已经改变，且会切换到 hdfs的家目录下。这个也就是有时切换了用户，但执行命令时还会报 `command not found` 的原因。
```bash
[root@cdh1 ~]# whoami
root
[root@cdh1 ~]# pwd
/root
[root@cdh1 ~]# echo $PATH
/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/usr/local/zulu8/bin:/usr/local/zulu8/jre/bin:/usr/local/scala/bin:/opt/cloudera/parcels/CDH-6.2.0-1.cdh6.2.0.p0.967373/lib/kafka/bin:/opt/cloudera/parcels/CDH-6.2.0-1.cdh6.2.0.p0.967373/lib/zookeeper/bin:/opt/flink-1.9.0/bin:/opt/apache-storm-1.2.2/bin:/root/bin
[root@cdh1 ~]# su hdfs
[hdfs@cdh1 root]$ pwd
/root
[hdfs@cdh1 root]$ echo $PATH
/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/usr/local/zulu8/bin:/usr/local/zulu8/jre/bin:/usr/local/scala/bin:/opt/cloudera/parcels/CDH-6.2.0-1.cdh6.2.0.p0.967373/lib/kafka/bin:/opt/cloudera/parcels/CDH-6.2.0-1.cdh6.2.0.p0.967373/lib/zookeeper/bin:/opt/flink-1.9.0/bin:/opt/apache-storm-1.2.2/bin:/root/bin
[hdfs@cdh1 root]$ exit
exit
[root@cdh1 ~]# su - hdfs
Last login: Wed Dec 11 11:29:20 CST 2019 on pts/2
[hdfs@cdh1 ~]$ pwd
/var/lib/hadoop-hdfs
[hdfs@cdh1 ~]$ echo $PATH
/usr/local/bin:/bin:/usr/bin:/usr/local/sbin:/usr/sbin:/usr/local/zulu8/bin:/usr/local/zulu8/jre/bin:/usr/local/scala/bin:/opt/cloudera/parcels/CDH-6.2.0-1.cdh6.2.0.p0.967373/lib/kafka/bin:/opt/cloudera/parcels/CDH-6.2.0-1.cdh6.2.0.p0.967373/lib/zookeeper/bin:/opt/flink-1.9.0/bin:/opt/apache-storm-1.2.2/bin:/var/lib/hadoop-hdfs/.local/bin:/var/lib/hadoop-hdfs/bin
[hdfs@cdh1 ~]$ exit
logout

```

## 2.2 用户和组
```bash
# 1 用户的创建
#  1.1 创建用户，创建用户家目录，指定组为user1
useradd user1 -m -U -s /bin/bash

#  1.2 创建用户，不创建家目录。-M 不创建家目录、-U 创建一个和用户名一样的组
useradd user2 -M -s /bin/bash --gid user1

#  1.3 查看新建的用户 
cat /etc/passwd | grep user

#  1.4 设置 user1 的密码
passwd user1


#  1.5 切换到 user1 用户
su user2

#  1.6 查看当前登陆的用户
whoami

#  1.7 切换到 user2 用户
su - user1

#  1.8 查看当前路径。su - 用户，会自动切换到用户的家目录
pwd

#  1.9 删除某个用户. -r 表示删除家目录
userdel -r user2


# 2 组
#  2.1 查看已存在的所有组名
cat /etc/group

#  2.2 添加一个组。因为  /usr/sbin/groupadd 权限为只有 root 能对其编辑，需要切换到 root用户
# -rwxr-x--- 1 root root 65536 Mar 14  2019 /usr/sbin/groupadd
groupadd supergroup

#  2.3 将用户(已存在的)添加到这个组中。usermod 同样只有 root 能对其编辑
usermod -G supergroup user1

#  2.4 查看某用户id信息，可以看到用户id、组id
id user1

#  2.5 查看当前用户的归属的所有组
groups user1

#  2.6 将 supergroup 组中的 user1 用户删除
gpasswd -d  user1  supergroup

```


# 3 HDFS

从第一部分可以看到 HDFS 文件的为 supergroup 
```bash
# root 用户上传到 HDFS 一个文件
echo `date` > test.txt
hadoop fs -put test.txt /home

# user1 用户创建一个 HDFS 文件夹
su - user1
hadoop fs -mkdir /home/test

# 查看
[user1@cdh1 ~]$ hadoop fs -ls /home
Found 5 items
drwxrwxrwx   - dr.who supergroup          0 2019-07-29 15:23 /home/flink
-rw-r--r--   3 root   supergroup      14205 2019-10-17 04:03 /home/rankQuote.csv
-rw-r--r--   3 hdfs   supergroup        329 2019-06-19 17:22 /home/sample.txt
drwxr-xr-x   - user1  supergroup          0 2019-12-11 14:41 /home/test
-rw-r--r--   3 root   supergroup         29 2019-12-11 14:38 /home/test.txt

```

## 将root 添加到 HDFS 的默认组中
```bash
# 1 添加一个  supergroup 组
groupadd supergroup

# 2 将root 用户添加到 supergroup 组
usermod -G supergroup root

# 3 查看
groups root

# 4 同步系统权限到 hdfs
su - hdfs -s /bin/bash -c "hdfs dfsadmin -refreshUserToGroupsMappings"

# 5 如果将 root 用户从 supergroup 移除 
gpasswd -d  root  supergroup

```

## HDFS 组合权限
```bash
# 1 将 /home/test.txt 组改为 hive 
hadoop fs -chgrp -R hive /home/test.txt

# 2 将 /home/test.txt 拥有者改为 user1
hadoop fs  -chown user1 /home/test.txt

# 3 将 /home/test.txt 的文件权限改为拥有者有所有权限、组用户仅有读权限
hadoop fs -chmod 740 /home/test.txt

# 4 使用 user2 用户读取文件。发现报权限不够的错误
bash-4.2$ hadoop fs -cat /home/test.txt
cat: Permission denied: user=user2, access=READ, inode="/home/test.txt":user1:hive:-rwxr-----

# 5 将 user2 添加到 hive组。需要root用户执行
usermod -G hive user2

# 6 查看 hive 组的用户
cat /etc/group  | grep hive

# 7 再次查看数据。此时可以正常查看到数据
bash-4.2$ hadoop fs -cat /home/test.txt
Wed Dec 11 14:38:18 CST 2019

```


# Hive
```bash
# 先把 user2 从 hive 权限组中删除
gpasswd -d  user2  hive
su - hdfs -s /bin/bash -c "hdfs dfsadmin -refreshUserToGroupsMappings"

# 进入 hive cli
hive

```

```sql
-- 1 建一个测试表 yore
create table yore(id int, name string);

-- 2 插入一条数据
INSERT INTO yore VALUES(1, "one");

-- 3 查询表中的数据
hive> SELECT * FROM yore;
OK
1       one
Time taken: 0.245 seconds, Fetched: 1 row(s)

-- 查看建表语句。可以看到保存在 HDFS 上的位置为 
--  hdfs://cdh1:8020/user/hive/warehouse/yore
SHOW CREATE TABLE yore;
 

```

```bash
# 1 查看 yore 表文件的权限，
[root@cdh1 ~]# hadoop fs -ls /user/hive/warehouse/yore
Found 1 items
-rwxrwx---   3 user1 hive          6 2019-12-11 15:54 /user/hive/warehouse/yore/000000_0

# 2 为了演示，最好设置为 公共用户不能访问， 770
hadoop fs -chmod -R 770 /user/hive/warehouse/yore

# 3 公共用户 user2 可以看到是没有权限读取的
bash-4.2$ whoami
user2
bash-4.2$ hadoop fs -cat /user/hive/warehouse/yore/000000_0
cat: Permission denied: user=user2, access=EXECUTE, inode="/user/hive/warehouse/yore":user1:hive:drwxrwx---

```

下看先 Hive 的权限
操作 |	解释
---- | ----
ALL	    | 所有权限
ALTER	| 允许修改元数据（modify metadata data of object）---表信息数据
UPDATE	| 允许修改物理数据（modify physical data of object）---实际数据
CREATE	| 允许进行Create操作
DROP	| 允许进行DROP操作
INDEX	| 允许建索引（目前还没有实现）
LOCK	| 当出现并发的使用允许用户进行LOCK和UNLOCK操作
SELECT	      | 允许用户进行SELECT操作
SHOW_DATABASE |	允许用户查看可用的数据库


## Sentry 服务
![在这里插入图片描述](https://img-blog.csdnimg.cn/20191211181619866.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L2dpdGh1Yl8zOTU3NzI1Nw==,size_16,color_FFFFFF,t_70)

* 配置Hive使用Sentry服务。Hive -> 配置 -> 搜：`Sentry 服务` -> 选中**Sentry**
* 关闭Hive的用户模拟功能。 Hive -> 配置 -> 搜：`HiveServer2 启用模拟` -> 取消勾选
* 开启 sentry.hive.testing.mode。 Hive -> 配置 -> 搜：`sentry-site.xml` -> 添加配置项
    - 名称： sentry.hive.testing.mode
    - 值： true
* 权限管理配置项。 Hive -> 配置 -> 搜：`hive-site.xml` -> 添加配置项
    - 名称： `hive.security.authorization.enabled`
    - 值： `true`
    - 名称： `hive.security.authorization.createtable.owner.grants`
    - 值： `ALL`
    ```
	<!-- 其实就是添加如下配置项 -->
	<property>
	   <name>hive.security.authorization.enabled</name>
	   <value>true</value>
	    <description>enableordisable the hive clientauthorization</description>
	</property>
	<property>
	   <name>hive.security.authorization.createtable.owner.grants</name>
	   <value>ALL</value>
	   <description>theprivileges automatically granted to theownerwhenever a table gets created. Anexample like "select,drop"willgrant select and drop privilege to theowner of thetable</description>
	</property>
	```
* Impala。Impala -> 配置 -> 搜：`Sentry 服务` -> 选中**Sentry**
* Hue。Hue -> 配置 -> 搜：`Sentry 服务` -> 选中**Sentry**
* HDFS开启 ACL 。HDFS -> 配置 -> 搜：`ACL` -> 勾上查询到的两个 **HDFS（服务范围）**


使用 hive cli 查询数据时
```sql
-- 查询时报 user2 用户没有权限读取数据。
--  看来 hive 的赋权并不能脱离 HDFS 的权限。hive.sentry.provider 映射的是 org.apache.sentry.provider.file.HadoopGroupResourceAuthorizationProvider
hive> SELECT * FROM yore;
FAILED: SemanticException Unable to determine if hdfs://cdh1.ygbx.com:8020/user/hive/warehouse/yore is encrypted: org.apache.hadoop.security.AccessControlException: Permission denied: user=user2, access=READ, inode="/user/hive/warehouse/yore":hive:hive:drwxrwx--x
        at org.apache.hadoop.hdfs.server.namenode.FSPermissionChecker.check(FSPermissionChecker.java:400)
        at org.apache.hadoop.hdfs.server.namenode.FSPermissionChecker.checkPermission(FSPermissionChecker.java:262)
        at org.apache.sentry.hdfs.SentryINodeAttributesProvider$SentryPermissionEnforcer.checkPermission(SentryINodeAttributesProvider.java:86)
        at org.apache.hadoop.hdfs.server.namenode.FSPermissionChecker.checkPermission(FSPermissionChecker.java:194)
        at org.apache.hadoop.hdfs.server.namenode.FSDirectory.checkPermission(FSDirectory.java:1855)
        at org.apache.hadoop.hdfs.server.namenode.FSDirectory.checkPermission(FSDirectory.java:1839)
        at org.apache.hadoop.hdfs.server.namenode.FSDirectory.checkPathAccess(FSDirectory.java:1789)
        at org.apache.hadoop.hdfs.server.namenode.FSDirEncryptionZoneOp.getEZForPath(FSDirEncryptionZoneOp.java:197)
        at org.apache.hadoop.hdfs.server.namenode.FSNamesystem.getEZForPath(FSNamesystem.java:7190)
        at org.apache.hadoop.hdfs.server.namenode.NameNodeRpcServer.getEZForPath(NameNodeRpcServer.java:2066)
        at org.apache.hadoop.hdfs.protocolPB.ClientNamenodeProtocolServerSideTranslatorPB.getEZForPath(ClientNamenodeProtocolServerSideTranslatorPB.java:1464)
        at org.apache.hadoop.hdfs.protocol.proto.ClientNamenodeProtocolProtos$ClientNamenodeProtocol$2.callBlockingMethod(ClientNamenodeProtocolProtos.java)
        at org.apache.hadoop.ipc.ProtobufRpcEngine$Server$ProtoBufRpcInvoker.call(ProtobufRpcEngine.java:523)
        at org.apache.hadoop.ipc.RPC$Server.call(RPC.java:991)
        at org.apache.hadoop.ipc.Server$RpcCall.run(Server.java:869)
        at org.apache.hadoop.ipc.Server$RpcCall.run(Server.java:815)
        at java.security.AccessController.doPrivileged(Native Method)
        at javax.security.auth.Subject.doAs(Subject.java:422)
        at org.apache.hadoop.security.UserGroupInformation.doAs(UserGroupInformation.java:1875)
        at org.apache.hadoop.ipc.Server$Handler.run(Server.java:2675)

```

## 登陆 Hive服务 服务Beeline
Sentry不支持Hive CLI列权限管理，因此我们使用beeline 连接 hive。
```
beeline

beeline> !connect jdbc:hive2://cdh3:10000
Connecting to jdbc:hive2://cdh3:10000
Enter username for jdbc:hive2://cdh3:10000: hive
Enter password for jdbc:hive2://cdh3:10000: ****
Connected to: Apache Hive (version 2.1.1-cdh6.2.0)
Driver: Hive JDBC (version 2.1.1-cdh6.2.0)
Transaction isolation: TRANSACTION_REPEATABLE_READ

```

**注意**：这里使用的用户 user1、user2 必须在集群每个节点创建
```sql
-- 1 创建 admin 角色
create role admin;
create role role_user2;

-- 2 查看角色
show roles;

-- 3 将 yore 表的 ALL 权限赋予给  admin 角色
grant all on table default.yore to role admin;

-- 3 将 yore 表的 select 权限赋予给  role_user2 角色
grant select on table default.yore to role role_user2;

-- 4.1  将角色 admin 的权限赋予 user1 用户
-- GRANT ROLE admin TO USER user1;

-- 4.2 将角色 role_user2 的权限赋予 user2 用户
-- GRANT ROLE role_user2 TO USER `user2`;


-- 5.1 将 admin 角色添加到 user1 
grant role admin to group user1;
-- 5.2 将 role_user2 角色添加到 user2
grant role role_user2 to group user2;


-- 6 查看 user1 用户组的角色信息
SHOW ROLE GRANT GROUP user1;
-- chakan  role_user2 下的权限信息
SHOW GRANT ROLE role_user2;


-- 7 回收角色
--  7.1 回收  某个角色对 库的 权限
REVOKE SELECT ON TABLE yore FROM ROLE role_user2;
--  7.2 回收摸个组下的 某个角色的权限
REVOKE ROLE role_user2 [, role_name] FROM GROUP user2;

```


使用 beeline 查看数据
* user1 用户连接 Hive
```sql
beeline
beeline> !connect jdbc:hive2://cdh3:10000
Connecting to jdbc:hive2://cdh3:10000
Enter username for jdbc:hive2://cdh3:10000: user1
Enter password for jdbc:hive2://cdh3:10000:
Connected to: Apache Hive (version 2.1.1-cdh6.2.0)
Driver: Hive JDBC (version 2.1.1-cdh6.2.0)
Transaction isolation: TRANSACTION_REPEATABLE_READ

-- 1 查看表
0: jdbc:hive2://cdh3:10000> show tables;
+-----------+
| tab_name  |
+-----------+
| yore      |
+-----------+
1 row selected (0.16 seconds)

-- 2 查看表数据
0: jdbc:hive2://cdh3:10000> SELECT * FROM yore;
+----------+------------+
| yore.id  | yore.name  |
+----------+------------+
| 1        | one        |
+----------+------------+
1 row selected (0.293 seconds)

-- 3 插入一条数据
0: jdbc:hive2://cdh3:10000> INSERT INTO yore VALUES(2, "two");

-- 再次查看表数据
0: jdbc:hive2://cdh3:10000> SELECT * FROM yore;
+----------+------------+
| yore.id  | yore.name  |
+----------+------------+
| 1        | one        |
| 2        | two        |
+----------+------------+
2 rows selected (0.374 seconds)

```


* user2 用户连接 Hive
```sql
beeline
beeline> !connect jdbc:hive2://cdh3:10000
Connecting to jdbc:hive2://cdh3:10000
Enter username for jdbc:hive2://cdh3:10000: user2
Enter password for jdbc:hive2://cdh3:10000:
Connected to: Apache Hive (version 2.1.1-cdh6.2.0)
Driver: Hive JDBC (version 2.1.1-cdh6.2.0)
Transaction isolation: TRANSACTION_REPEATABLE_READ

-- 1 查看表
0: jdbc:hive2://cdh3:10000> show tables;
+-----------+
| tab_name  |
+-----------+
| yore      |
+-----------+
1 row selected (0.272 seconds)

-- 2 查看表数据
0: jdbc:hive2://cdh3:10000> SELECT * FROM yore;
+----------+------------+
| yore.id  | yore.name  |
+----------+------------+
| 1        | one        |
+----------+------------+
1 row selected (0.176 seconds)

-- 3 插入一条数据。发现报异常了，提示 user2 没有 QUER 权限
0: jdbc:hive2://cdh3:10000> INSERT INTO yore VALUES(3, "three");
Error: Error while compiling statement: FAILED: SemanticException No valid privileges
 User user2 does not have privileges for QUERY
 The required privileges: Server=server1->Db=default->Table=yore->action=insert->grantOption=false; (state=42000,code=40000)


```


查看 HDFS 上表文件的权限，虽然用户 user2 并不是 表文件的拥有者和 组内，通过权限设置依然我们访问到了表数据。
```bash
[root@cdh2 ~]#  hadoop fs -ls /user/hive/warehouse/yore
Found 2 items
-rwxrwx--x+  3 hive hive          6 2019-12-12 22:40 /user/hive/warehouse/yore/000000_0
-rwxrwx--x+  3 hive hive          6 2019-12-12 22:35 /user/hive/warehouse/yore/000000_0_copy_1
```













