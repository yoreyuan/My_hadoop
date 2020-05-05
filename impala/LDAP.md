Impala配置LDAP身份认证
------
[LDAP 中文网](http://www.ldap.org.cn/)
[OpenLDAP Software 2.4 Administrator's Guide](https://www.openldap.org/doc/admin24/)

# 1 简介
LDAP是一款轻量级目录访问协议（Lightweight Directory Access Protocol，简称LDAP），属于开源集中账号管理架构的实现，且支持众多系统版本，被广大互联网公司所采用。

LDAP在TCP/IP之上定义了一个相对简单的升级和搜索目录的协议。LDAP具有两个标准，分别是 X.500 和 LDAP。OpenLDAP 是基于 X.500 标准的，
而且去除了 X.500 复杂的功能并且可以根据自我需求定制额外扩展功能，但与 X.500 也有不同之处，例如OpenLDAP支持TCP/IP协议等，目前TCP/IP是Internet上访问互联网的协议。

## OpenLDAP
OpenLDAP是直接运行在更简单和更通用的 TPC/IP 或其他可靠的传输协议层上，避免了在 OSI 会话层（Open System Interconnection）和表示层的开销，
使连接的建立和包的处理更简单、更快，对于互联网和企业网应用更理想。LDAP 提供并实现目录服务的信息服务。目录服务是一种特殊的数据库系统，
对于数据的读取、浏览、搜索有很好的效果。目录服务一般用来包含基于属性的描述信息并支持精细复杂的过滤功能，
但 OpenLDAP 目录服务不支持通用数据库的大量更新操作所需要的复杂事物管理或回滚策略等。


OpenLDAP 默认以 Berkeley DB 作为后端数据库，Berkeley DB 数据库主要以散列的数据类型进行存储，如以键值对的方式进行存储。Berkeley DB是一类特殊的数据库，
主要用于搜索、浏览、更新查询操作，一般对于一次写入数据多次查询或搜索有很好的效果。Berkeley DB 数据库时面向查询进行优化、面向读取进行优化的数据库。
Berkeley DB 不支持事务性数据库（MySQL、MariaDB、Oracle等）所支持的高并发的高吞吐量以及复杂的事物操作。

OpenLDAP 目录中的信息时按照树型结构进行组织的，具体信息存储在条目（entry）中，条目可以看成关系数据库中的表记录，条目是具有专有名（Distinguished Name，DN）的属性，
DN 是用来引用条目，DN 相当于关系数据库中的主键，是唯一的。属性有类型（type）和一个或者多个值（value）组成，相当于关系数据库中字段的概念。

## OpenLDAP 的优点
* OpenLDAP 是一个跨平台的标准互联网协议，它基于 X.500 标准协议
* OpenLDAP 提供静态数据查询搜索，不需要像在关系数据库中那样通过 SQL 语句维护数据库信息。
* OpenLDAP 基于推和拉的机制实现节点间的数据同步，简称复制（replication）并提供基于 TLS、SASL 的安全认证机制，实现数据加密传输以及 Kerberos 密码验证功能。
* OpenLDAP 可以基于第三方开源软件实现负载（LVS、HAProxy）以及高可用性姐姐方案，24小时提供验证服务，比如 Headbeat、Corosync、Keepalived等。
* OpenLDAP 数据元素使用简单的文本字符串（简称 LDIF 文件）而非一些特殊字符，便于维护管理目录树条目。
* OpenLDAP 可以实现用户的几种认证管理，所有关于账号的变更，只需要在 OpenLDAP 服务端直接操作，无需到每台客户端进行操作，影响范围为全局。
* OpenLDAP 默认使用协议简单，比如支持 TCP/IP 协议传输条目数据，通过使用查找操作实现对目录信息的读写操作，童昂可以通过加密的方式进行获取目录条目信息。
* OpenLDAP 产品应用于各大应用平台（Nginx、HTTP、vsftpd、Samba、SVN、Postfix、OpenStack、Hadoop等）、服务器（HP、IBM、Dell等）以及存储（EMC、NetApp等）控制台，负责管理账号验证功能，实现账号统一管理。
* OpenLDAP 实现具有费用低、配置简单、功能强大、管理容易以及开源的特点
* OpenLDAP 通过 ACL（Access Control List）灵活控制用户访问数据的权限，从而保证数据的安全性。


## LDAP 中的名词解释
名词或缩写 | 全称 | 描述
---- | :---- | :----
Entry | Entry           | 条目，也叫记录项，是LDAP中最基本的颗粒，就像字典中的词条，或者是数据库中的记录。通常对LDAP的添加、删除、更改、检索都是以条目为基本对象的。
Attribute | Attribute   | 每个条目都可以有很多属性（Attribute），比如常见的人都有姓名、地址、电话等属性。每个属性都有名称及对应的值，属性值可以有单个、多个，比如你有多个邮箱。属性不是随便定义的，需要符合一定的规则，而这个规则可以通过schema制定。
objectClass |  &nbsp;   | 内置属性。对象类是属性的集合，LDAP预想了很多人员组织机构中常见的对象，并将其封装成对象类。比如人员（person）含有姓（sn）、名（cn）、电话(telephoneNumber)、密码(userPassword)等属性，单位职工(organizationalPerson)是人员(person)的继承类，除了上述属性之外还含有职务（title）、邮政编码（postalCode）、通信地址(postalAddress)等属性。
Schema  | &nbsp;        | 对象类（ObjectClass）、属性类型（AttributeType）、语法（Syntax）分别约定了条目、属性、值，他们之间的关系如下图所示。所以这些构成了模式(Schema)——对象类的集合。条目数据在导入时通常需要接受模式检查，它确保了目录中所有的条目数据结构都是一致的。
backend & database  | &nbsp; | ldap 的后台进程`slapd`接收、响应请求，但实际存储数据、获取数据的操作是由Backends做的，而数据是存放在database中，所以你可以看到往往你可以看到backend和database指令是一样的值如 bdb 。一个 backend 可以有多个 database instance，但每个 database 的 suffix 和 rootdn 不一样。openldap 2.4版本的模块是动态加载的，所以在使用backend时需要moduleload back_bdb指令。
telephoneNumber | &nbsp;| 电话号码
TLS  |  Transport Layer Security | 分布式LDAP 是以明文的格式通过网络来发送信息的，包括client访问ldap的密码（当然一般密码已然是二进制的），SSL/TLS 的加密协议就是来保证数据传送的保密性和完整性。
SASL | Simple Authenticaion and Security Layer | SASL 简单身份验证安全框架，它能够实现openldap客户端到服务端的用户验证，也是ldapsearch、ldapmodify这些标准客户端工具默认尝试与LDAP服务端认证用户的方式（前提是已经安装好 Cyrus SASL）。SASL有几大工业实现标准：Kerveros V5、DIGEST-MD5、EXTERNAL、PLAIN、LOGIN。
LDIF | LDAP Data Interchange Format | LDIF（数据交换格式）是LDAP数据库信息的一种文本格式，用于数据的导入导出，每行都是“属性: 值”对。
dn  | Distinguished Name| 专有名，类似于Linux文件系统中的绝对路径，类似于关系型数据库的主键
Base DN | Base DN       | LDAP目录树的最顶部就是根，也就是所谓的“Base DN”，如”dc=mydomain,dc=org”。
rdn | Relative dn       | 相对专有名，类似于文件系统中的相对路径，它是与目录树结构无关的部分，一般指dn逗号最左边的部分。
dc  | Domain Componet   | 域名组件。将完整域名划分成几个部分，比如域名为 example.org，拆分为 dc=example,dc=org
uid | User Id           | 用户 ID
cn  | Common Name       | 公共名称，如 Manager
sn  | Surname           | 姓
ou  | Organization Unit | 组织单位，类似于Linux文件系统中的子目录，它是一个容器对象，组织单位可以包含其他各种对象（包括其他组织单元）
o   | Organization      | 组织名
c   | Country           | 国家



## 卸载
```bash
# 1 查看安装的 openldap
rpm -qa | grep openldap

# 2 关闭服务
systemctl stop slapd
systemctl disable slapd

# 3 卸载服务
yum -y remove openldap-servers openldap-clients

# 4 删除相关目录的数据。/etc/openldap 如果删除 certs 也会删除，安装的时候需要注意。
rm -rf /var/lib/ldap
rm -rf /etc/openldap

# 5 删除ldap用户。
# 可以通过 cat /etc/passwd | grep ldap ，命令查看到系统中存在 ldap 用户
userdel ldap

# 6 查看服务状态
systemctl status slapd

```

# 安装 OpenLDAP

## 安装规划
主机 | 系统版本 | ip 地址 | 主机名
LDAP 服务端 | CentOS 7 |  | cdh1.ygbx.com cdh1
LDAP 客户端 | CentOS 7 |  | cdh2.ygbx.com cdh2
LDAP 客户端 | CentOS 7 |  | cdh3.ygbx.com cdh3


软件包说明：

软件包名称 | 软件包功能描述
---- | ----
openldap         | OpenLDAP 服务端和客户端必须使用的库文件
openldap-clients | 在 LDAP 服务端上使用，用于查看和修改目录的命令行的包
openldap-servers | 用于启动服务和设置，包含单独的 LDAP 后台守护进程
openldap-servers-sql | 支持 sql 模块
compat-openldap  | OpenLDAP 兼容性库


## 方式一：yum 方式
如果通过这种方式安装，当可以联网，直接通过 yum 安装即可；如果不能联网，需要配置自己内网的yum源；配置 yum 源 `vim /etc/yum.repos.d/difine.repo`，主要配置如下内容
```bash
# [] 中是yum 仓库的名称，用于区别不同 yum 仓库及功能
[repo_name]
# 仓库的表述信息
name=
# 跟仓库的路径
baseurl=
# 后面跟数字，表示是否启用该仓库，0表示禁用，1表示启用
enabled=1
# 后面跟数字，表示是否检查软件包的 md5sum，用于校验软件包的安全性。0表示不检查，1表示检查
gpgcheck=1
# 后面跟 path，是一个软件包所使用的签名，一般启用 gpgcheck 时才配置。
gpgkey=

```

yum 基本命令说明
```bash
# 1 安装软件包
install package1 package2 ...

# 更新软件包
update package1 package2 ...

# 检查最新的软件包
check-update package1 package2 ...

# 卸载安装的软件包
remove package1 package2 ...

# 查看安装的软件包列表
list 

# 查看软件包的相关信息
info 

# 重建缓存
makecache

# 本地安装软件包
localinstall rpmfile1 rpmfile1 ...

# 本地更新软件包
localupdate rpmfile1 rpmfile1 ...

# 清除缓存，并重建缓存
yum clean all && yum makecache

```

开始安装
```bash
# 1 安装
yum -y install openldap openldap-servers openldap-clients openldap-devel compat-openldap
# 如果需要数据迁移，可以安装 migrationtools
yum install -y migrationtools

# 初始化 OpenLDAP 配置
cp /usr/share/openldap-servers/DB_CONFIG.example /var/lib/ldap/DB_CONFIG
cp /usr/share/openldap-servers/slapd.ldif /etc/openldap/slapd.ldif
chown ldap:ldap /var/lib/ldap/DB_CONFIG
chown -R ldap:ldap /etc/openldap/
chown -R ldap:ldap /var/lib/ldap



```



## 方式二：rpm 方式安装
```bash
# 1 下载
# 在 Cloudera Manager Server服务器上安装额外的包
# 如果安装中报： 
# error: Failed dependencies:
#         libltdl.so.7()(64bit) is needed by openldap-servers-2.4.44-21.el7_6.x86_64
# 需要下载安装 libtool-ltdl
# wget http://mirror.centos.org/centos/7/os/x86_64/Packages/libtool-ltdl-2.4.2-22.el7_3.x86_64.rpm
# rpm -ivh libtool-ltdl-2.4.2-22.el7_3.x86_64.rpm
# rpm -Uvh openldap-2.4.44-21.el7_6.x86_64.rpm
wget http://mirror.centos.org/centos/7/os/x86_64/Packages/openldap-servers-2.4.44-21.el7_6.x86_64.rpm
wget http://mirror.centos.org/centos/7/os/x86_64/Packages/openldap-clients-2.4.44-21.el7_6.x86_64.rpm
rpm -ivh openldap-servers-2.4.44-21.el7_6.x86_64.rpm openldap-clients-2.4.44-21.el7_6.x86_64.rpm


# 2 查看安装的 openldap
# rpm -e --nodeps 
rpm -qa | grep openldap
# 为了防止环境的影响，对yum 执行如下命令，清除缓存并更新
yum clean all 
yum -y upgrade

# 3 拷贝数据库配置文件，并修改文件的属组为 ldap
cp /usr/share/openldap-servers/DB_CONFIG.example /var/lib/ldap/DB_CONFIG
cp /usr/share/openldap-servers/slapd.ldif /etc/openldap/slapd.ldif
chown ldap:ldap /var/lib/ldap/DB_CONFIG
chown -R ldap:ldap /etc/openldap/
chown -R ldap:ldap /var/lib/ldap

# 4 启动和查看状态
#  有错误时可以查看  journalctl -xe
systemctl start slapd
systemctl enable slapd

# 5 查看日志和状态
systemctl status slapd
journalctl -xe
# 查看 389 端口
netstat -ntplu | grep -i :389
lsof -i:389

```

OpenLDAP 中涉及的路径和文件的说明：
* `/etc/openldap/slapd.ldif`，OpenLDAP的主配置文件，记录根域名称、管理员名称、密码、日志、权限等香瓜信息
* `/var/lib/ldap/*` ，OpenLDAP数据文件存储位置，可以根据需求进行调整。但为了保证数据的安全，一般会推荐放到存储设备上或者独立的分区上。
* `/etc/openldap/slapd.d/*`
* `/usr/share/openldap-servers/slapd.ldif` ，模板配置文件
* `/usr/share/openldap-servers/DB_CONFIG.example`，模板数据库配置文件 schema 录几个
* `/etc/openldap/schema` OpenLDAP Schema 规范存放位置

OpenLDAP 监听的端口有以下两个
* 默认监听端口：389（明文数据传输）
* 加密监听端口：636（密文数据传输）


## 安装中出现的问题及解决
如果是卸载了`openldap-servers`、`openldap-clients`的环境重新安装，可能再次安装后，在没有 `/etc/openldap/certs`，这回造成在启动 `systemctl start slapd`时报如下错误
```
…… slapd[20259]: main: TLS init def ctx failed: -1
…… slapd[20259]: slapd stopped.
…… slapd[20259]: connections_destroy: nothing to destroy.
…… systemd[1]: slapd.service: control process exited, code=exited status=1
…… systemd[1]: Failed to start OpenLDAP Server Daemon.
…… systemd[1]: Unit slapd.service entered failed state.
…… systemd[1]: slapd.service failed.

```

如果我们从其它地方拷贝进`/etc/openldap/certs/*`或者卸载时旧的残留文件，又会报如下的错误：
```
…… slapd[28152]: tlsmc_get_pin: INFO: Please note the extracted key file will not be protected with a PIN any more, however it will be still protected at least by file permissions.

```

关于上面的错误，是新版本（例如 2.4.44 ）安装时会出现的一个问题，关于这个问题可以查看这个 [Issues 5493](https://github.com/NethServer/dev/issues/5493)来解决。
这样会重新生成新的 `/etc/openldap/certs`。
```bash
rm -rf /etc/openldap/
yum reinstall openldap-servers openldap --enablerepo=base,updates
yum reinstall http://packages.nethserver.org/nethserver/7.7.1908/updates/x86_64/Packages/nethserver-directory-3.3.3-1.ns7.noarch.rpm --enablerepo=base,updates

```

但是新问题又出现了，重启 slapd 服务，又报了如下的错误：
```
## systemctl status slapd.service
…… slapd[27348]: daemon: bind(8) failed errno=98 (Address already in use)
…… slapd[27348]: daemon: bind(8) failed errno=98 (Address already in use)
…… systemd[1]: slapd.service: control process exited, code=exited status=1
…… systemd[1]: Failed to start OpenLDAP Server Daemon.
…… systemd[1]: Unit slapd.service entered failed state.
…… systemd[1]: slapd.service failed.

##  journalctl -xe 查看的如下的错误
* slapd[27348]: daemon: bind(8) failed errno=98 (Address already in use)
* slapd[27348]: daemon: bind(8) failed errno=98 (Address already in use)
* slapd[27348]: slapd stopped.
* slapd[27348]: connections_destroy: nothing to destroy.
* systemd[1]: slapd.service: control process exited, code=exited status=1
* systemd[1]: Failed to start OpenLDAP Server Daemon.
-- Subject: Unit slapd.service has failed
-- Defined-By: systemd
-- Support: http://lists.freedesktop.org/mailman/listinfo/systemd-devel
--
-- Unit slapd.service has failed.
--
-- The result is failed.
* systemd[1]: Unit slapd.service entered failed state.
* systemd[1]: slapd.service failed.
```

此时我们先查看ldap服务端口是否启来`lsof -i:389`，如果有先杀掉进程，然后删除掉`rm -rf /etc/openldap/certs/password`，这样文件发生改动，再次重启 slapd，
会重新生成`/var/lib/ldap/*`，但是会报如下错误，这个问题好解决，直接修改`/var/lib/ldap/`下的属组` chown ldap:ldap /var/lib/ldap/*`
```
…… runuser[31462]: pam_unix(runuser:session): session closed for user ldap
…… runuser[31464]: pam_unix(runuser:session): session opened for user ldap by (uid=0)
…… runuser[31464]: pam_unix(runuser:session): session closed for user ldap
…… check-config.sh[31439]: Read/write permissions for DB file '/var/lib/ldap/__db.002' are required.
…… runuser[31466]: pam_unix(runuser:session): session opened for user ldap by (uid=0)
…… runuser[31466]: pam_unix(runuser:session): session closed for user ldap
…… systemd[1]: slapd.service: control process exited, code=exited status=1
…… systemd[1]: Failed to start OpenLDAP Server Daemon.
…… systemd[1]: Unit slapd.service entered failed state.
…… systemd[1]: slapd.service failed.
```

重新启动服务，发现错误发生了改变，显示如下错误信息，根据提示我们查看`/var/run/openldap/slapd.args`的信息，发现属组为 root，
因此我们修改这个文件的属组`chown ldap:ldap /var/run/openldap/slapd.args`，再次重启 slapd 服务，
```
## journalctl -xe 查看的如下的错误
…… slapd[966]: unable to open args file "/var/run/openldap/slapd.args": 13 (Permission denied)
…… slapd[966]: slapd stopped.
…… slapd[966]: connections_destroy: nothing to destroy.
…… systemd[1]: slapd.service: control process exited, code=exited status=1
…… systemd[1]: Failed to start OpenLDAP Server Daemon.
-- Subject: Unit slapd.service has failed
-- Defined-By: systemd
-- Support: http://lists.freedesktop.org/mailman/listinfo/systemd-devel
--
-- Unit slapd.service has failed.
--
-- The result is failed.
…… systemd[1]: Unit slapd.service entered failed state.
…… systemd[1]: slapd.service failed.
```

再次重启 slapd 服务，这次服务终于可以正常启动：
```
[root@cdh1 openldap]#  systemctl start slapd
[root@cdh1 openldap]#  systemctl status slapd
● slapd.service - OpenLDAP Server Daemon
   Loaded: loaded (/usr/lib/systemd/system/slapd.service; enabled; vendor preset: disabled)
   Active: active (running) since Sun 2020-03-28 10:23:56 CST; 7s ago
     Docs: man:slapd
           man:slapd-config
           man:slapd-hdb
           man:slapd-mdb
           file:///usr/share/doc/openldap-servers/guide.html
  Process: 1295 ExecStart=/usr/sbin/slapd -u ldap -h ${SLAPD_URLS} $SLAPD_OPTIONS (code=exited, status=0/SUCCESS)
  Process: 1266 ExecStartPre=/usr/libexec/openldap/check-config.sh (code=exited, status=0/SUCCESS)
 Main PID: 1305 (slapd)
   CGroup: /system.slice/slapd.service
           └─1305 /usr/sbin/slapd -u ldap -h ldapi:/// ldap:///
Mar 28 10:23:55 cdh1.yore.com runuser[1288]: pam_unix(runuser:session): session opened for user ldap by (uid=0)
Mar 28 10:23:55 cdh1.yore.com runuser[1288]: pam_unix(runuser:session): session closed for user ldap
Mar 28 10:23:55 cdh1.yore.com runuser[1290]: pam_unix(runuser:session): session opened for user ldap by (uid=0)
Mar 28 10:23:55 cdh1.yore.com runuser[1290]: pam_unix(runuser:session): session closed for user ldap
Mar 28 10:23:55 cdh1.yore.com runuser[1292]: pam_unix(runuser:session): session opened for user ldap by (uid=0)
Mar 28 10:23:55 cdh1.yore.com runuser[1292]: pam_unix(runuser:session): session closed for user ldap
Mar 28 10:23:55 cdh1.yore.com slapd[1295]: @(#) $OpenLDAP: slapd 2.4.44 (Jan 29 2019 17:42:45) $
                                                   mockbuild@x86-01.bsys.centos.org:/builddir/build/BUILD/openldap-2.4.44/openldap-2.4.44/servers/slapd
Mar 28 10:23:55 cdh1.yore.com slapd[1305]: hdb_db_open: database "dc=my-domain,dc=com": unclean shutdown detected; attempting recovery.
Mar 28 10:23:56 cdh1.yore.com slapd[1305]: slapd starting
Mar 28 10:23:56 cdh1.yore.com systemd[1]: Started OpenLDAP Server Daemon.
```


## OpenLDAP 配置（通过配置的方式）
在 OpenLDAP 2.4 版本后，配置 OpenLDAP 有两种方式，第一种是通过修改配置，另一种通过修改数据库的形式完成配置。

通过配置数据库完成的各种配置，属于动态配置且不需要重启 slapd 进程服务。此配置数据库（cn=config）包含一个基于文本的集合 LDIF 文件（位于`/etc/openldap/slapd.d`目录下）。
但是也可以使用传统的配置文件（slapd.ldif）方式进行配置，通过配置文件来实现 slapd 的配置方式。slapd.ldif 可以通过编辑器进行配置，但`cn=config`不建议直接编辑修改，
而是采用 ldap 命令进行修改。


`/etc/openldap/slapd.ldif`配置文件部分配置项说明
```bash
# OpenLDAP 通过加密数据传输加载的配置文件时默认 OpenLDAP 服务采用明文传输数据。
# 在网路数据上传输机器不安全，所以需要通过如下配置将数据加密传输，前提是需要第三方合法的证书机构颁发的数字证书
# TLS settings
#
olcTLSCACertificatePath: /etc/openldap/certs
olcTLSCertificateFile: "OpenLDAP Server"
olcTLSCertificateKeyFile: /etc/openldap/certs/password


# 加载模块
## 对 系统不同位数的支持
olcModulepath:	/usr/lib/openldap
olcModulepath:	/usr/lib64/openldap
## 实现同步
olcModuleload: syncprov.la


# OpenLDAP 服务包含的 schema 文件。
include: file:///etc/openldap/schema/core.ldif


# 指定 OpenLDAP 管理员信息。OpenLDAP 服务管理员对目录树进行管理，如插入、更新、修改及删除等管理操作，要求系统管理员具有 root 身份权限，此管理员用户可以自我修改。
rootdn  "cn=root,dc=ygbx,dc=com"

# 指定 OpenLDAP 服务管理员密码
# 明文方式，不建议
#root   ygbx@123
rootpw  {SSHA}s2hLnvbtj2MmZtMSqp3PgEQwZMr/082K


# rootdn can always read and write EVERYTHING!
#
# 指定 OpenLDAP 管理员信息。OpenLDAP 服务管理员对目录树进行管理，如插入、更新、修改及删除等管理操作，要求系统管理员具有 root 身份权限，此管理员用户可以自我修改。
rootdn  "cn=root,dc=ygbx,dc=com"

# 指定 OpenLDAP 服务管理员密码
# 明文方式，不建议
#root   ygbx@123
rootpw  {SSHA}s2hLnvbtj2MmZtMSqp3PgEQwZMr/082K


#
# Server status monitoring
#

dn: olcDatabase=monitor,cn=config
objectClass: olcDatabaseConfig
olcDatabase: monitor
olcAccess: to * by dn.base="gidNumber=0+uidNumber=0,cn=peercred,cn=external,c
 n=auth" read by dn.base="cn=root,dc=ygbx,dc=com" read by * none


#
# Backend database definitions
#

dn: olcDatabase=hdb,cn=config
objectClass: olcDatabaseConfig
objectClass: olcHdbConfig
olcDatabase: hdb
olcSuffix: dc=ygbx,dc=com
olcRootDN: cn=root,dc=ygbx,dc=com
olcDbDirectory:	/var/lib/ldap
olcDbIndex: objectClass eq,pres
olcDbIndex: ou,cn,mail,surname,givenname eq,pres,sub

```


```bash
# 导入配置数据库
slapadd -n 0 -F /etc/openldap/slapd.d -l /etc/openldap/slapd.ldif

# 修改 管理员用户


# 重启服务
systemctl  restart slapd

# 从上面的配置我们已经看到，默认已经将 core.schema 导入。
# 我们还可以继续将其他 基础Schema导入（可选）
ldapadd -Y EXTERNAL -H ldapi:/// -f /etc/openldap/schema/cosine.ldif
ldapadd -Y EXTERNAL -H ldapi:/// -f /etc/openldap/schema/nis.ldif
ldapadd -Y EXTERNAL -H ldapi:/// -f /etc/openldap/schema/inetorgperson.ldif

# 检查搜索域。主要查看是否修改成自己配置的域信息
#  -x 信息排序
#  -b 指定搜索范围起点
ldapsearch -x -b "dc=ygbx,dc=com"
ldapsearch -x -b '' -s base '(objectclass=*)' namingContexts

```

设置 管理员用户
```bash
# 查看管理员用户信息
ldapsearch -H ldapi:// -LLL -Q -Y EXTERNAL -b "cn=config" "(olcRootDN=*)" dn olcRootDN olcRootPW
```

init_root.ldif 如下内容
```bash
dn: olcDatabase={0}config,cn=config
changetype: modify
add: olcRootPW
olcRootPW: {SSHA}s2hLnvbtj2MmZtMSqp3PgEQwZMr/082K

dn: olcDatabase={2}hdb,cn=config
changetype: modify
add: olcRootPW
olcRootPW: {SSHA}s2hLnvbtj2MmZtMSqp3PgEQwZMr/082K

#修改Suffix
dn: olcDatabase={2}hdb,cn=config
changetype: modify
replace: olcSuffix
olcSuffix: dc=ygbx,dc=com

#修改RootDomain
dn: olcDatabase={2}hdb,cn=config
changetype: modify
replace: olcRootDN
olcRootDN: cn=root,dc=ygbx,dc=com

#修改访问权限
dn: olcDatabase={1}monitor,cn=config
changetype: modify
replace: olcAccess
olcAccess: {0}to * by dn.base="gidNumber=0+uidNumber=0,cn=peercred,cn=external,cn=auth" read by dn.base="cn=root,dc=ygbx,dc=com" read by * none

```


```bash
# 修改
ldapmodify -H ldapi:// -Y EXTERNAL -f init_root.ldif
```



## 创建用户

![Figure 5.1: Sample configuration tree.](https://www.openldap.org/doc/admin24/config_dit.png)

在 `/etc/openldap/`下创建一个基础用户的配置文件`baseDomain.ldif`，然后执行命令：` ldapadd -x -w "ygbx@123" -D "cn=root,dc=ygbx,dc=com" -f /etc/openldap/baseDomain.ldif`
```bash
dn: dc=ygbx,dc=com
o: sinosig com
dc: ygbx
objectClass: top
objectClass: dcObject
objectclass: organization

#dn: cn=root,dc=ygbx,dc=com
#cn: root
#objectClass: organizationalRole
#objectClass: Directory Manager

dn: ou=People,dc=ygbx,dc=com
ou: People
objectClass: top
objectClass: organizationalUnit

dn: ou=Group,dc=ygbx,dc=com
ou: Group
objectClass: top
objectClass: organizationalUnit

```




在创建一个 `user.ldif`，执行命令：` ldapadd -x -w "ygbx@123" -D "cn=root,dc=ygbx,dc=com" -f /etc/openldap/user.ldif`，
objectClass 可以查看`/etc/openldap/schema`
```bash
dn: uid=impala,ou=People,dc=ygbx,dc=com
objectClass: inetOrgPerson
objectClass: posixAccount
objectClass: shadowAccount
cn: impala
sn: cdh
userPassword: ygbxCDHImpala_iy52yu
loginShell: /bin/bash
# id impala
uidNumber: 975
gidNumber: 971
homeDirectory: /var/lib/impala/ldap
#mail: test@test.com

```

测试是否成功
```bash
ldapsearch -LLL -w "ygbx@123" -x -D "cn=root,dc=ygbx,dc=com" -H ldap://cdh1 -b "dc=ygbx,dc=com"

```

```bash
####################
## 可以通过如下方式实现管理员的修改及密码的修改，执行下面的就会将`cn=Manager,dc=ygbx,dc=com` 修改为 `cn=Admin,dc=ygbx,dc=com`，且密码也修改为 `{SSHA}w9pclo6ufhXQKDokgFhHCrql/tMT3r3a` 对应的密码

## 4 创建管理员密码（ygbx@123）
## 输入连词密码，会生成一个 SSHA 的字符串，先保存记下 {SSHA}s2hLnvbtj2MmZtMSqp3PgEQwZMr/082K
#slappasswd
#
#[root@cdh1 ~]# cat << EOF | ldapadd -Y EXTERNAL -H ldapi:///
#dn: olcDatabase={0}config,cn=config
#changetype: modify
#delete: olcRootDN
#
#dn: olcDatabase={0}config,cn=config
#changetype: modify
#all: olcRootDN
#olcRootDN: cn=Admin,cn=config
#
#dn: olcDatabase={0}config,cn=config
#changetype: modify
#all: olcRootPW
#olcRootPW: {SSHA}w9pclo6ufhXQKDokgFhHCrql/tMT3r3a
#EOF
#
```


## 对用户的操作
这部分涉及到对用户的运维，我们可以借助第三方开源软件实现的图形化运维管理工具进行，例如：[PHPladpAdmin 管理工具](http://phpldapadmin.sourceforge.net/wiki/index.php/Main_Page)、
[开源的LDAP浏览器](http://jxplorer.org/)、LDAPAdmin、LAM 等。但是对于Linux系统运维人员，日常的操作几乎都是通过 Linux 命令完成，如果对命令比较熟悉，
直接使用 OpenLDAP 自带的命令来维护效率会比较高，但是熟悉这些命令时存在难度的，所以这里主要介绍针对用户的常用命令。

### OpenLDAP 管理命令汇总
* ldapsearch：   搜索 OpenLDAP 目录树条目
* ldapadd：      通过 LDIF 格式，添加目录树条目
* ldapdelete：   删除 OpenLDAP 目录树条目
* ldapmodify：   修改 OpenLDAP 目录树条目
* ldapwhoami：   检验 OpenLDAP 用户的身份
* ldapmodrdn：   修改 OpenLDAP 目录树 DN 条目
* ldapcompare：  判断 DN 值和指定参数值是否属于同一个条目
* ldappasswd：   修改 OpenLDAP 目录树用户条目实现密码重置
* slaptest：     验证 slapd.conf 文件或 cn=配置目录（slapd.d）
* slapindex：    创建 OpenLDAP 目录树索引，提高查询效率
* slapcat：      将数据条目转换为 OpenLDAP 的 LDIF 文件


### ldapsearch，搜索目录树条目
```bash
# -b 指定查找的节点
# -D 指定查找的 DN，DN 是整个 OpenLDAP 树的主键
# -x 使用简单认证，不使用任何加密算法，例如：TLS、SASL 等相关加密算法
# -w 指定管理员的密码
# -W 在查询时提示输入管理员密码，如果不想输入密码，可以使用 -w user_passwd
# -H 使用 LDAP 服务器的 URI 地址进行操作
# -p 指定 OpenLDAP 监听的端口，默认为 389，如果是加密则端口为 636
# -LLL 禁止输出与过滤条件不匹配的信息
ldapsearch -x -D "cn=root,dc=ygbx,dc=com" -H ldap://cdh1 -b "dc=ygbx,dc=com"  -w "ygbx@123" 

```

### ldapadd，使用 LDIF 格式添加目录树条目
ldapadd 功能上等同于 `ldapmodify -a`。
```bash
# -x 使用简单认证 
# -D 指定查找的 DN
# -w 直接指定密码
# -h 指定 ldaphost，可以使用 FQDN、IP
# -f LDIF 文件的路径
ldapadd -x -D "cn=root,dc=ygbx,dc=com" -w "ygbx@123" -h cdh1.ygbx.com -f hue_user00.ldif


# new_user00.ldif 如下
##########
dn: uid=hue,ou=People,dc=ygbx,dc=com
objectClass: inetOrgPerson
objectClass: posixAccount
objectClass: shadowAccount
cn: hue
sn: cdh
# slappasswd 生成，hue_123_abc
userPassword: {SSHA}MAZYEX6owgYZKUAzf2EYY3PXMz6tIstR
loginShell: /bin/bash
# id hue
uidNumber: 987
gidNumber: 983
homeDirectory: /usr/lib/hue/ldap

```

### ldapdelete，删除 目录树条目
```bash
# -c 持续操作模式，例如，在操作过程中出现错误，也会继续进行之后的相关操作
# -D 指定查找的 DN
# -n 显示正在进行的相关操作
# -x 使用简单认证，
# -f 使用目标文件名作为命令的输入
# -y 指定含有密码的文件进行验证
# -r 递归删除

#ldapdelete -D "cn=root,dc=ygbx,dc=com"  -w "ygbx@123" -h cdh1.ygbx.com -x

```

### ldapmodify，修改目录树条目
```bash
# -a 新增条目
# -n 显示正在进行的相关操作
# -v 显示详细输出结果
# -f 使用目标文件名（.ldif）作为命令的输入

ldapmodify -x -D "cn=root,dc=ygbx,dc=com" -w "ygbx@123" -H ldap://cdh1.ygbx.com -f modify.ldif

##########
# modify.ldif 文件内容如下
dn: uid=test,ou=people,dc=ygbx,dc=com
changetype: modify
replace: pwdReset
pwdReset: TRUE

```

### ldapwhoami，验证服务器身份
```bash
ldapwhoami -x -D "cn=root,dc=ygbx,dc=com" -w "ygbx@123" -H ldap://cdh1.ygbx.com 

```

### ldapmodrdn，修改目录树 DN 条目
```bash
# -f 使用目标文件名作为命令的输入
# -r 删除 OpenLDAP 目录树中 rdn条目的唯一标识名称
# -P 这里指定的是 OpenLDAP 的版本号，比如 V2，V3
# -O 指定 SASL 安全属性
ldapmodrdn -x -D "cn=root,dc=ygbx,dc=com" -w "ygbx@123" -H ldap://cdh1.ygbx.com "uid=test,ou=people,dc=ygbx,dc.com"

```

### ldapcompare，判断 DN 值和指定参数值是否属于同一个条目
```bash
# -P 这里指定的是 OpenLDAP 的版本号，比如 V2，V3
ldapcompare -x -D "cn=root,dc=ygbx,dc=com" -w "ygbx@123" -H ldap://cdh1.ygbx.com "uid=test,ou=people,dc=ygbx,dc.com" "uid:ada"
# 不匹配时显示 FALSE；匹配时显示 TRUE；给出的 DN 条目无法再目录树中检索到显示 UNDEFINED

```

### ldappasswd，修改密码
```bash
# -S 提示用户输入新密码
# -s 指定密码
# -a 通过旧密码，自动生成新密码
# -A 提示输入就密码，自动生成新密码
ldappasswd -x -D "cn=root,dc=ygbx,dc=com" -w "ygbx@123" "uid=hue,ou=people,dc=ygbx,dc.com" -S
# 前面两次为设置新密码，最后提示输入管理员密码

```

### slaptest， 验证 slapd.conf 文件
```bash
# -d 指定 debug 级别
# -f 检测的文件
# -F 检测的文件夹
slaptest -f /etc/openldap/slapd.conf

```

### slapindex， 创建 OpenLDAP 目录树索引
可用来提高查询效率，减轻服务器响应压力，前提是 slapd 进程停止，否则会报错
```bash
# -d 指定 debug 级别
# -f 指定 OpenLDAP 配置文件，并创建索引
# -F 指定 OpenLDAP 数据库目录，并创建索引

```

### slapcat， 数据条目转为 LDIF 文件
可用于备份，结合 slapdadd 指令进行恢复条目数据。
```bash
# -a 添加过滤选项
# -b 指定 suffix 路径，如 dc=ygbx,dc=com
# -d 指定 debug 级别
# -f 指定 OpenLDAP 配置文件
# -F 指定 OpenLDAP 数据库目录
# -c 出错时是否继续
# -v 输出详细信息
slapcat -v -l openldap.ldif

```



## （未完，待续）客户端节点安装
```bash
# 1 查看安装的 openldap
rpm -qa | grep openldap

# 2 安装
rpm -ivh openldap-clients-2.4.44-21.el7_6.x86_64.rpm

# 3 如果有 sssd ，关闭或者卸载
rpm -qa | grep sssd

# 4 安装 nss-pam-ldapd 
wget http://mirror.centos.org/centos/7/os/x86_64/Packages/nss-pam-ldapd-0.8.13-16.el7_6.1.x86_64.rpm 
yum install -y nss-pam-ldapd-0.8.13-16.el7_6.1.x86_64.rpm 
# 查看安装的服务
rpm -qa | grep nss-pam-ldapd

# 5 如果没有 pam-ldapd，请安装
rpm -qa | grep pam-ldapd
yum install pam_ldap -y

```

修改 nslcd.conf 配置文件。在` /etc/nslcd.conf`中添加如下内容
```bash
# LDAP 服务地址
uri ldap://cdh1/
base dc=ygbx,dc=com
ssl no
tls_cacertdir /etc/openldap/certs

```

修改 `/etc/pam.d/system-auth`，添加
```bash
auth        sufficient      pam_ldap.so use_first_pass

account     [default=bad succcess=ok user_unknow=ignore]      pam_ldap.so

password    sufficient     pam_ldap.so user_authtok

session     optional      pam_ldap.so

```

修改 `vim /etc/nsswitch.conf`，
```bash
 33 passwd:     files ldap
 34 shadow:     files ldap
 35 group:      files ldap
 
 37 #hosts:     db files nisplus nis dns
 38 hosts:      files dns

```

修改`vim /etc/sysconfig/authconfig`
```bash
# 启用密码验证
USESHADOW=yes

# 启用 OpenLDAP 验证(默认no)
USELDAPAUTH=yes

# 启用本地验证
USELOCAUTHORIZE=yes

# 启用 LDAP 认证协议
USELDAP=yes

```

加载 nslcd 进程
```bash
systemctl restart nslcd

systemctl status nslcd

chkconfig nslcd on
chkconfig --list nslcd


lsb_release -a

```



修改客户端处的 ` /etc/openldap/ldap.conf ` 配置文件
```bash
BASE    dc=ygbx,dc=com
URI     ldap://cdh1
```

```bash
ldapsearch -b 'dc=ygbx,dc=com'

```





```bash


# 5 初始化
# 以下文本保存至/etc/openldap/init.ldif
# olcRootPW 表示 OpenLDAP 管理员的密码
```


```bash
#olcRootPW 为上面生成的 SSHA 形式的密码
dn: olcDatabase={0}config,cn=config
changetype: modify
add: olcRootPW
olcRootPW: {SSHA}w9pclo6ufhXQKDokgFhHCrql/tMT3r3a

dn: olcDatabase={2}hdb,cn=config
changetype: modify
add: olcRootPW
olcRootPW: {SSHA}w9pclo6ufhXQKDokgFhHCrql/tMT3r3a

#修改Suffix
dn: olcDatabase={2}hdb,cn=config
changetype: modify
replace: olcSuffix
olcSuffix: dc=ygbx,dc=com

#修改RootDomain
dn: olcDatabase={2}hdb,cn=config
changetype: modify
replace: olcRootDN
olcRootDN: cn=root,dc=ygbx,dc=com

#修改访问权限
dn: olcDatabase={1}monitor,cn=config
changetype: modify
replace: olcAccess
olcAccess: {0}to * by dn.base="gidNumber=0+uidNumber=0,cn=peercred,cn=external,cn=auth" read by dn.base="cn=Manager,dc=ygbx,dc=com" read by * none

```

```bash
# 执行我们的配置文件，会修改我们配置的 5 个条目
ldapmodify -Y EXTERNAL -H ldapi:/// -f /etc/openldap/init.ldif




# 对域名进行一些基础配置
# 将以下文本保存至/etc/openldap/baseDomain.ldif
# 执行时会提示输入 LDAP 的密码
ldapadd -x -D cn=Manager,dc=ygbx,dc=com -W -f /etc/openldap/baseDomain.ldif

```

```bash
dn: dc=ygbx,dc=com
o: ygbx com
dc: ygbx
objectClass: top
objectClass: dcObject
objectclass: organization

dn: cn=Manager,dc=ygbx,dc=com
cn: Manager
objectClass: organizationalRole
description: Directory Manager

dn: ou=People,dc=ygbx,dc=com
ou: People
objectClass: top
objectClass: organizationalUnit

dn: ou=Group,dc=ygbx,dc=com
ou: Group
objectClass: top
objectClass: organizationalUnit

```


# CDH 中 Hive 的设置
在 CDH 的 Hive 配置处搜索`hive-site.xml 的 Hive 服务高级配置代码段`，在其中添加

```xml
<configuration>
    <property>
        <name>hive.security.authorization.enabled</name>
        <value>true</value>
        <description>enableor disable the hive clientauthorization</description>
    </property>

    <property>
        <name>hive.security.authorization.createtable.owner.grants</name>
        <value>ALL</value>
        <description>theprivileges automatically granted to the ownerwhenever a table gets created. Anexample like "select,drop" willgrant select and drop privilege to theowner of the table</description>
    </property>

    <property>
        <name>hive.server2.authentication</name>
        <value>LDAP</value>
        <description>
          Expects one of [nosasl, none, ldap, kerberos, pam, custom].
          Client authentication types.
            NONE: no authentication check
            LDAP: LDAP/AD based authentication
            KERBEROS: Kerberos/GSSAPI authentication
            CUSTOM: Custom authentication provider
                    (Use with property hive.server2.custom.authentication.class)
            PAM: Pluggable authentication module
            NOSASL:  Raw transport
        </description>
    </property>
    
    <property>
        <name>hive.server2.authentication.ldap.url</name>
        <value>ldap://cdh1</value>
    </property>

    <property>
        <name>hive.server2.authentication.ldap.baseDN</name>
        <value>ou=People,dc=ygbx,dc=com</value>
    </property>

</configuration>

```

使用 Beeline 连接
```bash
# 用户和密码为 ou=People,dc=ygbx,dc=com 中设置的 用户和免密
beeline --color=true -d "org.apache.hive.jdbc.HiveDriver" -u "jdbc:hive2://cdh3:10000/default" -n impala -p ygbxCDHImpala_iy52y

```





# CDH 中Impala的设置

在 Impala 的配置中设置：
```bash
# 勾选此项
enable_ldap_auth



ldap_uri=ldap://cdh1:389


ldap_bind_pattern=uid=#UID,ou=People,dc=ygbx,dc=com

```

配置proxy user
```bash
# 在 CDH 配置中 搜 " Impala 命令行参数高级配置代码段（安全阀） "
--ldap_passwords_in_clear_ok=true

# core-site.xml 的 Impala Daemon 高级配置代码段（安全阀）,配置
名称: hadoop.proxyuser.hue.hosts
值: *


名称: hadoop.proxyuser.hue.groups
值: *

```

## 错误解决
```
LDAP authentication specified, but without TLS. Passwords would go over the network in the clear. Enable TLS with --ldap_tls or use an ldaps:// URI. To override this is non-production environments, specify --ldap_passwords_in_clear_ok
. Impalad exiting.
Wrote minidump to /var/log/impala-minidumps/impalad/b92a42e0-4358-444e-c4ebd9b1-a8093ae6.dmp
```

```bash
# 对于非SSL的LDAP，需要配置如下项 
# 在 CDH 配置中 搜 " Impala Daemon 命令行参数高级配置代码段（安全阀 "
--authorized_proxy_user_config=hue=*

```


# 界面化管理工具
这部分涉及到对用户的运维，我们可以借助第三方开源软件实现的图形化运维管理工具进行，例如：[PHPladpAdmin 管理工具](http://phpldapadmin.sourceforge.net/wiki/index.php/Main_Page)、
[开源的LDAP浏览器](http://jxplorer.org/)、LDAPAdmin、LAM 等。但是对于Linux系统运维人员，日常的操作几乎都是通过 Linux 命令完成，如果对命令比较熟悉，
直接使用 OpenLDAP 自带的命令来维护效率会比较高，但是熟悉这些命令时存在难度的，所以这里主要介绍针对用户的常用命令。

## jxplorer
```bash
# 1 以 MacOS 系统为例
wget https://jaist.dl.sourceforge.net/project/jxplorer/jxplorer/version%203.3.1.2/jxplorer-3.3.1.2-osx.zip

# 2 解压后直接脱入到 "应用程序"

# 3 点击图标打开如果启动失败，
chmod +x /Applications/jxplorer-3.3.1.2.app/Contents/MacOS/jxplorer


```

可以匿名访问，配置如下项，点击 确定 可登录
* 主机： cdh1
* 基底DN: dc=ygbx,dc=com
* 层级：匿名


用户+密码方式，例如以管理员 root 登陆，其它用户登录无法修改，只能查看
* 主机： cdh1
* 基底DN: dc=yore,dc=com
* 层级：用户+密码
* 使用者DN：cn=root,dc=yore,dc=com
* 密码：yore@123













