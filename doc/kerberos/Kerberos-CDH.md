# kerberos 简介
Kerberos 网络认证基于对称密码学，并需要一个值得信赖的第三方。

# kerberos 体系结构
## Kerberos 在 Hadoop 中概念
* Kerberos Principal
	- 主体： 在Kerberos中用户或者服务被称为主体，一般由两部分或者三部分组成：primary 主要标识、instance实例名称（可选）、realm领域。在Kerberos的认证系统中通过主体标识唯一身份，在CDH中主体一般是操作系统用户名或者服务名。
	- `username@YOUR-REALM.COM`		app1@HADOOP.COM
	- `name/fully.qualified.domain.name@YOUR-REALM.COM`		hbase/node1@HADOOP.COM
	- Kerberos通过将票据Tickets分配给Kerberos主体使其可以访问启动Kerberos集群的Hadoop服务。
* Kerberos Keytab
	- Keytab文件包含了Kerberos主体和该主体密钥的加密副本，对于Hadoop守护进程来说每个keytab文件是唯一的，因为实例包含了主机名，如`hbase/node@HADOOP.COM`，该文件用来认证 Kerberos主机上的主体，因此keytab一定要保存。
* Delegation Tokens
	- 用户通过Kerberos认证后，提交作业，如果用户此时登出系统，则后续的认证通过委托令牌的方式完成，委托令牌和 Nn共享密钥，用于模拟用户来获得执行任务，委托令牌有效期为一天，可以通过更新程序更新令牌不超过7天，任务完成以后委托令牌取消。

# kerberos 工作机制
* Kerberos依靠Ticket概念来工作，每个部分概念如下：
	- KDC（Key Distribution Center），密钥分发中心。由 AS 和 TGS 组成，是所有通信的主要枢纽，保存有每个客户端或者服务的密钥副本，辅助完成通信认证。
	- AS（Authentication Server），认证服务器
	- TGS（Ticket Granting Server），票据授权服务器
	- TGT（Ticket Granting Ticket），票据授权票据，票据的票据
	- SS（Service Server）特定服务提供端

* Kerberos 设计的参与者
	- 请求访问某个资源的主体，可以使用户或者服务
	- 被请求的资源，一般是具体某个服务，比如 hive 等
	- KDC

功能概述
要开启一个认证会话，客户端首先将用户名发送到 KDC 的 AS 进行认证（一般通过 kinit 命令完成），KDC 服务器生成响应的票据授权票据（TGT）并打上时间戳，
TGT 是一个用于请求和其他服务通信的票据，并在数据库中查找请求用户的密码，并用查找到的密码对 TGT 进行加密，将加密结果返回给请求用户。

客户端收到返回结果，使用自己的密码解密得到 TGT 票据授权票据，该 TGT 会在一段时间后自动失效，有些程序可以用户登录期间进行自动更新，
比如Hadoop的hdfs用户。当客户端需要请求服务时，客户端将该 TGT 发送到 KDC 的 TGS 服务，当用户的 TGT 通过验证并且有权限访问所申请的服务时，
TGS 生成一个被请求服务对应的 Ticket 和 Session Key，并发给请求客户端。

客户端将该 Ticket 和要请求的服务一同发送给目的服务端，完成验证并获的相应服务。







# kerberos 安装部署
以CDH 为例，如果CDH 已安装了 Sentry，先停止 Sentry，等安装完 Kerberos 之后再把这个服务开启。

## KDC节点的安装
### 下载安装
如果可以连接网络，也可以直接通过 yum 方式安装：`yum install krb5-server krb5-libs krb5-auth-dialog krb5-workstation`

```bash
# 1 卸载旧版本 Kerberos
# 因为可能系统会带有这个包，建议如果有直接卸载，否则可能后期安装会因为版本问题而报错
rpm -qa | grep -E "krb5|libkadm5"
rpm -e --nodeps xxx

## 2 下载二进制制包形式（以 CentOS 7 x86 64 位 为例）
## 访问： https://pkgs.org/download/krb5-libs
## 下载 krb5-libs-*.rpm
wget http://mirror.centos.org/centos/7/updates/x86_64/Packages/krb5-libs-1.15.1-37.el7_7.2.x86_64.rpm
## 下载 krb5-workstation-*.rpm
wget http://mirror.centos.org/centos/7/updates/x86_64/Packages/libkadm5-1.15.1-37.el7_7.2.x86_64.rpm
wget http://mirror.centos.org/centos/7/updates/x86_64/Packages/krb5-workstation-1.15.1-37.el7_7.2.x86_64.rpm
## 下载 krb5-server-*.rpm
wget http://mirror.centos.org/centos/7/updates/x86_64/Packages/krb5-server-1.15.1-37.el7_7.2.x86_64.rpm

## 3 下载完毕后，查看如下：
[root@cdh1 software]# ll
total 2856
-rw-r--r-- 1 root root  824024 Sep 14  2019 krb5-libs-1.15.1-37.el7_7.2.x86_64.rpm
-rw-r--r-- 1 root root 1070820 Sep 14  2019 krb5-server-1.15.1-37.el7_7.2.x86_64.rpm
-rw-r--r-- 1 root root  836076 Sep 14  2019 krb5-workstation-1.15.1-37.el7_7.2.x86_64.rpm
-rw-r--r-- 1 root root  182108 Sep 14  2019 libkadm5-1.15.1-37.el7_7.2.x86_64.rpm

# 安装前面下载的rpm 包 
# （KDC 安装krb5-libs、krb5-server、krb5-workstation，其他节点安装 krb5-libs、krb5-workstation）
# KDC 需要安装 krb5-lib-1.xx.x, krb5-workstation-1.xx.x, krb5-server-1.xx.x
# KDC 从节点上只需要安装 krb5-lib-1.xx.x, krb5-workstation-1.xx.x
# 如果安装的时候有依赖上的检查冲突，可以加上参数 --nodeps --force
# 
# 如果报 libkadm5，可以尝试重新安装或升级 libkadm5
#  否则会有如下的错误
#   error: Failed dependencies:
#           krb5-libs(x86-64) = 1.15.1-37.el7_7.2 is needed by krb5-workstation-1.15.1-37.el7_7.2.x86_64
#           libkadm5(x86-64) = 1.15.1-37.el7_7.2 is needed by krb5-workstation-1.15.1-37.el7_7.2.x86_64
rpm -ivh libkadm5-1.15.1-37.el7_7.2.x86_64.rpm
rpm -ivh krb5-libs-1.15.1-37.el7_7.2.x86_64.rpm
rpm -ivh krb5-workstation-1.15.1-37.el7_7.2.x86_64.rpm
rpm -ivh krb5-server-1.15.1-37.el7_7.2.x86_64.rpm
```

**错误1**：在安装时如果报如下的错误，需要下载安装 words 插件，因为有些应用或数据库会使用这个来检查单词的拼写，或者密码检查器会使用这个来查找有误的密码。
```
error: Failed dependencies:
        /usr/share/dict/words is needed by krb5-server-1.15.1-37.el7_7.2.x86_64
```

**解决**：如果可以使用yum，直接安装即可 yum install words。这里给出离线安装方式：
```bash
# 1 查看是否有 words
rpm -qa | grep words

# 2 下载 words 包 
wget http://mirror.centos.org/centos/7/os/x86_64/Packages/words-3.0-22.el7.noarch.rpm

# 3 安装。安装成功后会在 /usr/share/dict/words 有一个词文件。
rpm -ivh words-3.0-22.el7.noarch.rpm
```

安装的过程如下：
```bash
[root@cdh2 software]# rpm -ivh libkadm5-1.15.1-37.el7_7.2.x86_64.rpm
Preparing...                          ################################# [100%]
Updating / installing...
   1:libkadm5-1.15.1-37.el7_7.2       ################################# [100%]
   
[root@cdh2 software]# rpm -ivh krb5-libs-1.15.1-37.el7_7.2.x86_64.rpm
Preparing...                          ################################# [100%]
Updating / installing...
   1:krb5-libs-1.15.1-37.el7_7.2      ################################# [100%]

[root@cdh2 software]# rpm -ivh krb5-workstation-1.15.1-37.el7_7.2.x86_64.rpm
Preparing...                          ################################# [100%]
Updating / installing...
   1:krb5-workstation-1.15.1-37.el7_7.################################# [100%]

[root@cdh2 software]#  rpm -ivh krb5-server-1.15.1-37.el7_7.2.x86_64.rpm
error: Failed dependencies:
        /usr/share/dict/words is needed by krb5-server-1.15.1-37.el7_7.2.x86_64
[root@cdh2 software]# wget http://mirror.centos.org/centos/7/os/x86_64/Packages/words-3.0-22.el7.noarch.rpm
[root@cdh2 software]# rpm -ivh words-3.0-22.el7.noarch.rpm
Preparing...                          ################################# [100%]
Updating / installing...
   1:words-3.0-22.el7                 ################################# [100%]

[root@cdh2 software]# rpm -ivh krb5-server-1.15.1-37.el7_7.2.x86_64.rpm
Preparing...                          ################################# [100%]
Updating / installing...
   1:krb5-server-1.15.1-37.el7_7.2    ################################# [100%]

```

### 配置 /var/kerberos/krb5kdc/kdc.conf
执行完上一步的安装之后，在 `/var/kerberos` 下会有生成配置文件，下面我们需要修改 `/var/kerberos/krb5kdc/kdc.conf`。
只在 KDC 机器上修改，配置如下，这里对其中某些参数说明如下：CDH.COM：设定 realm ，大小写敏感，一般是大写。
```yaml
[kdcdefaults]
 kdc_ports = 88
 kdc_tcp_ports = 88

[realms]
 CDH.COM = {
   # JDK 8 （至少在 jdk 1.8 _152 之前的）可能不支持，如果使用中发现异常：java.security.InvalidKeyException: Illegal key size，
   # 方法1，可以将 aes256-cts 去点，保留 aes128-cts
   # 方法2，或者下载官方提供的 jce_policy-8.zip 包，解压后将 local_policy.jar 和 US_export_policy.jar 覆盖JDK安装目录下的 jre\lib\security 下的两个文件
   #       每个版本的路径可能稍微有差别，只要找到 unlimited 下的
   #       下载地址 http://www.oracle.com/technetwork/java/javase/downloads/jce8-download-2133166.html
   #master_key_type = aes256-cts
   acl_file = /var/kerberos/krb5kdc/kadm5.acl
   dict_file = /usr/share/dict/words
   admin_keytab = /var/kerberos/krb5kdc/kadm5.keytab
   max_renewable_life = 7d 0h 0m 0s
   supported_enctypes = aes256-cts:normal aes128-cts:normal des3-hmac-sha1:normal arcfour-hmac:normal camellia256-cts:normal camellia128-cts:normal des-hmac-sha1:normal des-cbc-md5:normal des-cbc-crc:normal
 }
```

### 配置 /var/kerberos/krb5kdc/kadm5.acl
Realme 改为上面配置的名字CDH.COM。这样名称匹配 `*/admin@CDH.COM` 的都会认为 admin，权限是 `*`，代表全部权限。
```bash
 */admin@CDH.COM *
```

### 配置 /etc/krb5.conf
其中 cdh2.yore.com 为 KDC 服务的主机名。
```yaml

# Configuration snippets may be placed in this directory as well
includedir /etc/krb5.conf.d/

[logging]
 default = FILE:/var/log/krb5libs.log
 kdc = FILE:/var/log/krb5kdc.log
 admin_server = FILE:/var/log/kadmind.log

[libdefaults]
 default_realm = CDH.COM
 dns_lookup_realm = false
 dns_lookup_kdc = false
 ticket_lifetime = 24h
 renew_lifetime = 7d
 forwardable = true
 #rdns = false
 #pkinit_anchors = /etc/pki/tls/certs/ca-bundle.crt
# default_realm = EXAMPLE.COM
 #default_ccache_name = KEYRING:persistent:%{uid}

[realms]
 CDH.COM = {
  kdc = cdh2.yore.com
  admin_server = cdh2.yore.com
 }

[domain_realm]
.cdh2.yore.com = CDH.COM
cdh2.yore.com = CDH.COM
```

### 创建 Kerberos 数据库
```bash
# 1 创建/初始化 Kerberos database
# 当遇到问题，可能需要执行： /usr/sbin/kdb5_util -r CDH.COM -m destory -f。 
#            删除  /var/kerberos/krb5kdc/principal*
# 
# 期间会要求输入密码。kdc123
/usr/sbin/kdb5_util create -s -r CDH.COM

# 2 查看生成的文件
# 前两个是我们前两步设置的，后面的 principal* 就是本次生成的
[root@cdh2 software]# ll /var/kerberos/krb5kdc/
total 24
-rw-r--r-- 1 root root   19 Mar 25 21:41 kadm5.acl
-rw-r--r-- 1 root root  488 Mar 25 21:42 kdc.conf
-rw------- 1 root root 8192 Mar 25 21:40 principal
-rw------- 1 root root 8192 Mar 25 21:40 principal.kadm5
-rw------- 1 root root    0 Mar 25 21:40 principal.kadm5.lock
-rw------- 1 root root    0 Mar 25 21:40 principal.ok

```

### 创建 Kerberos 管理员账号
```bash
# 提示时输入管理员的密码： admin123
[root@cdh2 software]# /usr/sbin/kadmin.local -q "addprinc admin/admin@CDH.COM"
Authenticating as principal root/admin@CDH.COM with password.
WARNING: no policy specified for admin/admin@CDH.COM; defaulting to no policy
Enter password for principal "admin/admin@CDH.COM":
Re-enter password for principal "admin/admin@CDH.COM":
Principal "admin/admin@CDH.COM" created.

```

### 将 Kerberos 添加到自启动服务，并启动krb5kdc和kadmin服务
```bash
systemctl enable krb5kdc
systemctl enable kadmin

systemctl start krb5kdc
systemctl start kadmin

```

### 测试
```bash
# 1 提示输入密码时，输入 admin 的密码（Kerberos 管理员账号的密码）
[root@cdh2 software]# kinit admin/admin@CDH.COM
Password for admin/admin@CDH.COM:

# 2 查看所有的 Principal
/usr/sbin/kadmin.local -q "listprincs"

# klist
[root@cdh2 software]# klist
Ticket cache: FILE:/tmp/krb5cc_0
Default principal: admin/admin@CDH.COM
Valid starting       Expires              Service principal
03/25/2020 22:05:36  03/26/2020 22:05:36  krbtgt/CDH.COM@CDH.COM

```


## 应用服务端节点的安装
```bash
# 1 通 KDC 节点安装类似，这里只需要安装如下两个服务（所有客户端的节点都需要安装）
# 或者直接 yum 安装：yum -y install krb5-libs krb5-workstation
rpm -ivh krb5-libs-1.15.1-37.el7_7.2.x86_64.rpm
rpm -ivh libkadm5-1.15.1-37.el7_7.2.x86_64.rpm
rpm -ivh krb5-workstation-1.15.1-37.el7_7.2.x86_64.rpm

# 2 如果是 CDH ，还需要在 Cloudera Manager Server服务器上安装额外的包
#  OpenLDAP 是 LDAP 的开源套件应用程序开发工具， LDAP是一组用于通过Internet访问目录服务的协议，
#  类似于DNS信息在Internet上传播的方式。openldap-clients软件包包含访问和修改OpenLDAP目录所需的客户端程序。
#
#  也可以直接通过 yum 方式安装：yum -y install openldap-clients
wget http://mirror.centos.org/centos/7/os/x86_64/Packages/openldap-clients-2.4.44-21.el7_6.x86_64.rpm
rpm -ivh openldap-clients-2.4.44-21.el7_6.x86_64.rpm

# 3 将 KDC 服务节点的 /etc/krb5.conf 拷贝到所有Kerberos客户端
scp /etc/krb5.conf root@cdh1:/etc/


```


# 与 CDH 集成
```bash
# 1 在KDC中给Cloudera Manager添加管理员账号。scmAdmin123
[root@cdh2 software]# /usr/sbin/kadmin.local -q "addprinc cloudera-scm/admin@CDH.COM"
Authenticating as principal admin/admin@CDH.COM with password.
WARNING: no policy specified for cloudera-scm/admin@CDH.COM; defaulting to no policy
Enter password for principal "cloudera-scm/admin@CDH.COM":
Re-enter password for principal "cloudera-scm/admin@CDH.COM":
Principal "cloudera-scm/admin@CDH.COM" created.

# 2 进入Cloudera Manager的“管理”->“安全”界面
# ①选择“启用Kerberos”
# ②确保列出的所有检查项都已完成，然后全部点击勾选
# ③点击“继续”，配置相关的KDC信息，包括类型、KDC服务器、KDC Realm、加密类型以及待创建的Service Principal（hdfs，yarn,，hbase，hive等）的更新生命期等
# ④不建议让Cloudera Manager来管理krb5.conf（不勾选）, 点击“继续”
# ⑤输入Cloudera Manager的 Kerbers 管理员账号，一定得和之前创建的账号一致，点击“继续”
# ⑥点击“继续”启用Kerberos
# ⑦Kerberos启用完成，点击“继续”
# ⑧勾选重启集群，点击“继续”
# ⑨集群重启完成，点击“继续”
# ⑩点击“继续”
# ⑪点击“完成”，至此已成功启用Kerberos。
# ⑫回到主页，一切正常，再次查看“管理”->“安全”，界面显示“已成功启用 Kerberos。”

# 3 以 hiveuser01 来操作 Hive
## 3.1 创建 hiveuser01 的 principal。（hiveuser01）
/usr/sbin/kadmin.local -q "addprinc hiveuser01@CDH.COM"
## 3.2 使用 hiveuser01 登陆 Kerberos
kdestroy
kinit hiveuser01
klist
## 3.3 为 hiveuser01 生成一个 keytab 文件
##  hiveuser01@CDH.COM 必须为已存在的 Principal
##  也可以使用 kadmin 来生成。
/usr/sbin/kadmin.local -q "xst -k ./hive_user.keytab hiveuser01@CDH.COM"
## 3.4 通过 keytab 文件认证用户
kinit -kt ./hive_user.keytab hiveuser01@CDH.COM
## 3.5 更新 ticket
##  如果无法更新，是因为 hiveuser01@CDH.COM 的 renewlife 被设置成了0
##  可以通过这个命令查看(kadmin.local下执行): getprinc hiveuser01@CDH.COM
##  modprinc -maxrenewlife "1 week" +allow_renewable hiveuser01@CDH.COM
kinit -R


# 4 Beeline 连接 hive
!connect jdbc:hive2://cdh3:10000/;principal=hive/cdh2@FAYSON.COM


```






# 客户端节点上的使用和配置
如果外部某个节点需要访问 Kerberos 应用服务端节点上的某个服务（例如Hive），这里就需要对客户端节点进行一些配置。下面以 Beeline 连接 Hive 为例（Beeline的详细介绍请查看第六部分的内容）。

首先需要客户端获取认证的票据，这就需要我们按照应用服务端安装的步骤安装并配置好 Kerberos 的环境，
环境配置完毕后，需要通过 kinit 获取 KDC 处认证授权（TGS）的票据，这个票据就缓存在客户端节点，带有一个生命周期，在生命周期内可以访问正常请求Kerberos的应用服务节点。
```bash
# kinit 获取一个7天有效期的认证：
kinit -l 7d -kt /etc/hive/conf/hive.keytab  hive/twobigdata07@CDH.COM

```

因为我们使用的工具是 Beeline，Beeline 为 Hive 的新 CLI，因此这里需要对 Hive 的配置文件进行配置

修改 `/etc/hive/conf.cloudera.hive/core-site.xml` 文件中添加或修改配置如下（说明可以查看官方文档[Hadoop in Secure Mode](http://hadoop.apache.org/docs/stable2/hadoop-project-dist/hadoop-common/SecureMode.html)），
`/etc/hive/conf/core-site.xml`的这个值为 simple 可以不用修改。
```xml
<property>
  <name>hadoop.security.authentication</name>
  <!--<value>simple</value>-->
  <value>kerberos</value>
</property>

<!--
<property>
        <name>dfs.client.use.datanode.hostname</name>
        <value>true</value>
        <description>only cofig in clients</description>
</property>
-->


```

添加或修改`/etc/hive/conf.cloudera.hive/hive-site.xml`
```yaml
<property>
  <name>hive.metastore.kerberos.keytab.file</name>
  <value>/etc/hive/conf/hive.keytab</value>
</property>
<property>
  <name>hive.server2.authentication.kerberos.keytab</name>
  <value>/etc/hive/conf/hive.keytab</value>
</property>

<property>
  <name>hive.metastore.kerberos.principal</name>
  <value>hive/twobigdata07@CDH.COM</value>
</property>
<property>
  <name>hive.server2.authentication.kerberos.principal</name>
  <value>hive/twobigdata07@CDH.COM</value>
</property>

<property>
  <name>hive.metastore.sasl.enabled</name>
  <value>true</value>
</property>
<property>
  <name>hive.server2.authentication</name>
  <value>kerberos</value>
</property>

```

正如第四部分介绍的，如果开启了Kerberos，DataX的数据离线同步的 json中需要配置如下的配置项。
```bash
"haveKerberos": true,
"kerberosKeytabFilePath": "/xxx/xxxx/xxx.keytab",
"kerberosPrincipal": "xxx/xxxx@XXX.COM",

```

在Beeline连接Hive时，需要在url中添加认证的主体信息。
```bash
beeline -u "jdbc:hive2://${hive-server2-ip}:10000/default;principal=hive/twobigdata07@CDH.COM"

```


# kerberos 开发使用


1.2.0 版本  
1.2.1 版本


##






