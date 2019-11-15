[ClickHouse](https://clickhouse.yandex/) 安装(含集群方式)和使用
------

[Home](https://clickhouse.yandex/) &nbsp; &nbsp; | &nbsp; &nbsp;[GitHub](https://github.com/ClickHouse/ClickHouse)


<br/>


# 1 安装部署
## 1.1 安装之前
首先需要保证我们的系统使用的是x86_64处理器构架的Linux系统，并且**支持SSE 4.2指令集**，可以执行如下命令检查是否支持SSE 4.2，如果返回SSE 4.2 supported表示支持，则可以继续下面的安装。同时终端必须使用UTF-8编码。
```bash
grep -q sse4_2 /proc/cpuinfo && echo "SSE 4.2 supported" | echo "SSE 4.2 not supported"
```

同时我们最好再调整一下CentOS系统对打开文件数的限制，在`/etc/security/limits.conf`、`/etc/security/limits.d/*-nproc.conf`这2个文件的末尾加入一下内容。
```bash
*               soft    nofile          65536
*               hard    nofile          65536
*               soft    nproc          131072
*               hard    nproc          131072
```

修改完毕之后，SSH工具重新连接，再次登录后，执行如下命令查看，如果输出的值是我们设置的则表示已生效。
```bash
# 查看
ulimit -n
```

## 1.2 单节点方式
如果服务器可以连接网络，则可以直接通过yum方式安装，执行如下命令，如果是普通用户需要有sudo权限。下面的命令会自动下载资源安装稳定版的ClickHouse，如果需要安装最新版，把stable替换为testing。
```bash
# CentOS / RedHat
sudo yum install yum-utils
sudo rpm --import https://repo.yandex.ru/clickhouse/CLICKHOUSE-KEY.GPG
sudo yum-config-manager --add-repo https://repo.yandex.ru/clickhouse/rpm/stable/x86_64
sudo yum install clickhouse-server clickhouse-client
```

Yandex ClickHouse团队建议我们使用官方预编译的rpm软件包，用于CentOS、RedHat和所有其他基于rpm的Linux发行版。这种方式比较适合无法方位外网的生产环境安装，因此下面将主要采用这种方式安装和部署。ClickHouse的rpm包可以访问[Download](https://packagecloud.io/altinity/clickhouse)下载所需版本的安装包，执行如下命令下载资源包并安装。
```bash
# 1 下载
wget --content-disposition https://packagecloud.io/Altinity/clickhouse/packages/el/7/clickhouse-server-19.16.3.6-1.el7.x86_64.rpm/download.rpm
wget --content-disposition https://packagecloud.io/Altinity/clickhouse/packages/el/7/clickhouse-client-19.16.3.6-1.el7.x86_64.rpm/download.rpm
wget --content-disposition https://packagecloud.io/Altinity/clickhouse/packages/el/7/clickhouse-common-static-19.16.3.6-1.el7.x86_64.rpm/download.rpm
wget --content-disposition https://packagecloud.io/Altinity/clickhouse/packages/el/7/clickhouse-server-common-19.16.3.6-1.el7.x86_64.rpm/download.rpm

# 2 安装
rpm -ivh clickhouse-*-19.16.3.6-1.el7.x86_64.rpm
```

ClickHouse Server服务的启停命令如下。我们可以先启动ClickHouse服务。
```bash
# 1 启动。
# 可以在/var/log/clickhouse-server/目录中查看日志。
#sudo /etc/init.d/clickhouse-server start
systemctl start clickhouse-server

# 2 查看状态
systemctl status clickhouse-server

# 3 重启
systemctl restart clickhouse-server

# 4 关闭
systemctl stop clickhouse-server
```

启动ClickHouse Client服务，验证是否安装成功，如果可以正常执行，那么我们就可以快速去开始体验ClickHouse了。
```bash
# 1 未设置密码时
clickhouse-client
```
```sql
-- 2 执行一个简单的SQL。可以正常解析并执行。
cdh1 :) SELECT 1;
SELECT 1
→ Progress: 0.00 rows, 0.00 B (0.00 rows/s., 0.00 B/s.) ┌─1─┐
│ 1 │
└───┘
```

## 1.3 集群方式
例如现在准备在三个节点（cdh1、cdh2、cdh3）的机器上安装部署ClickHouse，CentOS 7系统的防火墙和SELINUX已经关闭或禁止或端口已开放。
集群的方需要依赖ZooKeeper服务，因此先要保证ZooKeeper服务正常启动，剩余的安装方式和单节点差不多，只不过需要添加一个集群形式的配置文件。

先在cdh1、cdh2、cdh3三个节点都通过rpm方式安装ClickHouse服务。先在cdh1节点配置`/etc/metrika.xml`（需要自己创建），
这个文件主要将ClickHouse各个服务的host和port、ZooKeeper集群的各个节点配置到文件中。cdh2和cdh3也同样配置，
只不过需要将`<macros>`标签下的`<replica>`标签中的值改为自己节点的主机名或者ip。
```xml
<yandex>
	<!-- /etc/clickhouse-server/config.xml 中配置的remote_servers的incl属性值，-->
    <clickhouse_remote_servers>
	<!-- 3分片1备份 -->
        <perftest_3shards_1replicas>
		<!-- 数据分片1  -->
            <shard>
                <internal_replication>true</internal_replication>
                <replica>
                    <host>cdh1</host>
                    <port>9000</port>
                </replica>
            </shard>
            <!-- 数据分片2  -->
            <shard>
                <replica>
                    <internal_replication>true</internal_replication>
                    <host>cdh2</host>
                    <port>9000</port>
                </replica>
            </shard>
            <!-- 数据分片3  -->
            <shard>
                <internal_replication>true</internal_replication>
                <replica>
                    <host>cdh3</host>
                    <port>9000</port>
                </replica>
            </shard>
        </perftest_3shards_1replicas>
    </clickhouse_remote_servers>

    <!--zookeeper相关配置-->
    <zookeeper-servers>
        <node index="1">
            <host>cdh1</host>
            <port>2181</port>
        </node>
        <node index="2">
            <host>cdh2</host>
            <port>2181</port>
        </node>
        <node index="3">
            <host>cdh3</host>
            <port>2181</port>
        </node>
    </zookeeper-servers>
    
    <macros>
        <replica>cdh1</replica>
    </macros>
    
    <networks>
        <ip>::/0</ip>
    </networks>
    
    <clickhouse_compression>
        <case>
            <min_part_size>10000000000</min_part_size>
            <min_part_size_ratio>0.01</min_part_size_ratio>
            <method>lz4</method>
        </case>
    </clickhouse_compression>

</yandex>
```

因为集群之间需要互相方位其它节点的服务，需要开放ClickHouse服务的ip和端口，在cdh1、cdh2、cdh3三个机器上配置`/etc/clickhouse-server/config.xml`文件，在`<yandex>`标签下释放 `<listen_host>`标签（大概在69、70行），配置如下。
```xml
<yandex>
    
    <!-- Listen specified host. use :: (wildcard IPv6 address), if you want to accept connections both with IPv4 and IPv6 from everywhere. -->
    <listen_host>::</listen_host>

    <!-- 设置时区为东八区，大概在第144行附近-->
    <timezone>Asia/Shanghai</timezone>

    <!-- 设置扩展配置文件的路径，大概在第229行附近-->
    <include_from>/etc/clickhouse-server/metrika.xml</include_from>

    <!-- 大概在160附近，注释其中配置的用于测试分布式存储的分片配置-->
    <!-- Test only shard config for testing distributed storage 
    <test_shard_localhost>
    ……
    </test_unavailable_shard>
    -->
</yandex>
```

为了不让ClickHouse裸奔，现在我们配置一下用户认证部分。密码配置有两种方式，一种是明文方式，一种是密文方式（sha256sum的Hash值），官方推荐使用密文作为密码配置，
在cdh1、cdh2、cdh3三个机器上配置`/etc/clickhouse-server/users.xml`文件。用户名和密码的配置主要是在<users>标签中，下面的配置文件中配置了两个用户，
一个是默认用户default，就是如果未指明用户时默认使用的用户，其密码配置的为**sha256密文**方式，
第二个用户是ck，为一个只读用户，即只能查看数据，无法建表修改数据等操作，其密码直接采用的明文方式进行配置。
```xml
<?xml version="1.0"?>
<yandex>
    <!-- Profiles of settings. -->
    <profiles>
        <!-- Default settings. -->
        <default>
            <max_memory_usage>10000000000</max_memory_usage>
            <use_uncompressed_cache>0</use_uncompressed_cache>
            <load_balancing>random</load_balancing>
        </default>
        <!-- Profile that allows only read queries. -->
        <readonly>
            <readonly>1</readonly>
        </readonly>
    </profiles>

    <!-- Users and ACL. -->
    <users>
        <default>
            <!-- 
                <password>KavrqeN1</password>
                通过如下命令随机执行随机获取一个： PASSWORD=$(base64 < /dev/urandom | head -c8); echo "$PASSWORD"; echo -n "$PASSWORD" | sha256sum | tr -d '-'  
            -->
	<password_sha256_hex>abb23878df2926d6863ca539f78f4758722966196e8f918cd74d8c11e95dc8ae</password_sha256_hex>
            <networks incl="networks" replace="replace">
                <ip>::/0</ip>
            </networks>

            <!-- Settings profile for user. -->
            <profile>default</profile>

            <!-- Quota for user. -->
            <quota>default</quota>

            <!-- For testing the table filters -->
            <databases>
                <test>
                    <!-- Simple expression filter -->
                    <filtered_table1>
                        <filter>a = 1</filter>
                    </filtered_table1>

                    <!-- Complex expression filter -->
                    <filtered_table2>
                        <filter>a + b &lt; 1 or c - d &gt; 5</filter>
                    </filtered_table2>

                    <!-- Filter with ALIAS column -->
                    <filtered_table3>
                        <filter>c = 1</filter>
                    </filtered_table3>
                </test>
            </databases>
        </default>
        
        <ck>
            <password>123456</password>
            <networks incl="networks" replace="replace">
                <ip>::/0</ip>
            </networks>
            <profile>readonly</profile>
            <quota>default</quota>
        </ck>
    </users>

    <!-- Quotas. -->
    <quotas>
        <!-- Name of quota. -->
        <default>
            <!-- Limits for time interval. You could specify many intervals with different limits. -->
            <interval>
                <!-- Length of interval. -->
                <duration>3600</duration>

                <!-- No limits. Just calculate resource usage for time interval. -->
                <queries>0</queries>
                <errors>0</errors>
                <result_rows>0</result_rows>
                <read_rows>0</read_rows>
                <execution_time>0</execution_time>
            </interval>
        </default>
    </quotas>
</yandex>
```

其中生成sha256sum的Hash值可以执行如下命令（第一行），回车后输出两行信息（第二行和第三行），其中第二行是原始密码，
第三行是加密的密文，配置文件使用第三行的字符串，客户端登陆是使用第二行的密码。
```bash
PASSWORD=$(base64 < /dev/urandom | head -c8); echo "$PASSWORD"; echo -n "$PASSWORD" | sha256sum | tr -d '-'
KavrqeN1
abb23878df2926d6863ca539f78f4758722966196e8f918cd74d8c11e95dc8ae
```

由于配置了密码，因此需要让集群的每个节点也知道每个节点的密码，因此在上面的`/etc/clickhouse-server/metrika.xml`配置文件的分片中增加用户名和密码的配置，
这里为了方便三个节点的密码配置的一样，也可以每个节点密码不一样。
```xml
		<perftest_3shards_1replicas>
            <shard>
                <internal_replication>true</internal_replication>
                <replica>
                    <host>cdh1</host>
                    <port>9000</port>
                    <user>default</user>
                    <password>KavrqeN1</password>
                </replica>
            </shard>
            
            <shard>
                <replica>
                    <internal_replication>true</internal_replication>
                    <host>cdh2</host>
                    <port>9000</port>
                    <user>default</user>
                    <password>KavrqeN1</password>
                </replica>
            </shard>
        
            <shard>
                <internal_replication>true</internal_replication>
                <replica>
                    <host>cdh3</host>
                    <port>9000</port>
                    <user>default</user>
                    <password>KavrqeN1</password>
                </replica>
            </shard>
        </perftest_3shards_1replicas>
```

在三个节点的服务器上分别启动ClickHouse服务，执行如下命令。启动时请保证每个节点的9000端口未被占用(`lsof -i:9000`)，
如果占用请修改`/etc/clickhouse-server/config.xml`文件中的端口（`<tcp_port>9000</tcp_port>`），
同时记得`/etc/clickhouse-server/metrika.xml`中的端口号也要统一。
```bash
# 1 启动服务
# 可以在/var/log/clickhouse-server/目录中查看日志。
#sudo /etc/init.d/clickhouse-server start
systemctl start clickhouse-server

# 查看服务状态。如果Active 显示的为 active，且信息中没有错误，则表示启动成功。
systemctl status clickhouse-server
```

<br/>

# 4 客户端工具
## 4.1 clickhouse-client
```bash
# 1 未设置密码时
clickhouse-client

# 2 指定用户名和密码 
#    -h		指定
#    -u		指定用户，不指定则使用默认用户 default
#    --port  指定服务的端口。默认为9000，如果端口更给了，则需要指定端口号
#    --password    指定登陆用户的密码
#    --multiline	支持多行SQL，否则是单行，回车（换行）自动执行，也可以使用 \ 符多行输入。
#    --help   查看参数的帮助说明
clickhouse-client -h 127.0.0.1 -u default --password KavrqeN1
clickhouse-client -h 127.0.0.1 --port 9000 -u default --password KavrqeN1  --multiline
# 指定sql命令方式
clickhouse-client -h 127.0.0.1 --port 9000 -u default --password KavrqeN1  --multiline -q "SELECT now()" 

```
```sql
-- 查看集群信息
cdh3 :) SELECT * FROM system.clusters;
SELECT *
FROM system.clusters
↙ Progress: 0.00 rows, 0.00 B (0.00 rows/s., 0.00 B/s.) ┌─cluster─────────────────┬─shard_num─┬─shard_weight─┬─replica_num─┬─host_name─┬─host_address────┬──port─┬─is_local─┬─user────┬─default_database─┬─errors_count─┬─estimated_recovery_time─┐
│ perftest_3shards_1replicas │         1 │            1 │           1 │ cdh1      │ 192.168.33.3 │ 9000 │        0 │ default │                  │            0 │                       0 │
│ perftest_3shards_1replicas │         2 │            1 │           1 │ cdh2      │ 192.168.33.6 │ 9000 │        0 │ default │                  │            0 │                       0 │
│ perftest_3shards_1replicas │         3 │            1 │           1 │ cdh3      │ 192.168.33.9 │ 9000 │        1 │ default │                  │            0 │                       0 │
└────────────────────────────┴───────────┴──────────────┴─────────────┴───────────┴──────────────┴───────┴──────────┴─────────┴──────────────────┴──────────────┴─────────────────────────┘
← Progress: 0.00 rows, 0.00 B (0.00 rows/s., 0.00 B/s.) ↖ Progress: 3.00 rows, 360.00 B (1.36 thousand rows/s., 163.76 KB/s.)
3 rows in set. Elapsed: 0.003 sec.

```

## 4.2 DBeaver
* 新建连接
* All（或者Analytical），选择ClickHouse，下一步
* 端口默认是8123，主机选择ClickHouse的Server节点(如果是集群，随意一个ClickHouse 服务节点都行)。填写用户认证处设置用户名和密码。
* 测试连接，会提示下载驱动，确认下载即可。

查看ClickHouse集群信息，在DBeaver中执行如下SQL。可以看到集群的分片、分片表示序号、host名字、端口号、用户名等信息。
![在这里插入图片描述](https://img-blog.csdnimg.cn/20191114145857831.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L2dpdGh1Yl8zOTU3NzI1Nw==,size_16,color_FFFFFF,t_70)

## 4.3 JDBC
项目中引入依赖
```xml
 <dependency>
    <groupId>ru.yandex.clickhouse</groupId>
    <artifactId>clickhouse-jdbc</artifactId>
    <version>0.2</version>
</dependency>
```
Scala代码查看[JdbcClient](../../clickhouse/ch-jdbc-client/src/main/java/yore/JdbcClient.scala)


<br/>

# 5 数据类型
固定长度的整型，包括有符号整型或无符号整型。
* 整型范围
	- **Int8** →  \[-128 : 127]
	- **Int16** →  \[-32768 : 32767]
	- **Int32** →  \[-2147483648 : 2147483647]
	- **Int64** →  \[-9223372036854775808 : 9223372036854775807]
* 无符号整型范围
	- **UInt8** →  \[0 : 255]
	- **UInt16** →  \[0 : 65535]
	- **UInt32** →  \[0 : 4294967295]
	- **UInt64** →  \[0 : 18446744073709551615]

* 浮点型
	- **Float32** → 对应于float
	- **Float64** → 对应于double

* 小数型
	- **Decimal(P, S)** →  P ：精度,有效范围：\[1:38]，决定可以有多少个十进制数字（包括分数）。S :规模,有效范围\[0：P]，决定数字的小数部分中包含的小数位数。
	- **Decimal32(S)** →  等效于p从1到9
	- **Decimal64(S)** →  等效于p从10到18
	- **Decimal128(S)** →  等效于p从19到38

* Boolean。不支持，可以使用UInt8类型，用1或0表示

* 字符串类型
	- **String** → 字符串可以任意长度的。它可以包含任意的字节集，包含空字节。ClickHouse 没有编码的概念。字符串可以是任意的字节集，按它们原本的方式进行存储和输出。
	- **FixedString(N)** → 固定长度 N 的字符串（N 必须是严格的正自然数）。
	- **UUID** → 专门用户保存UUID类型的值，格式如`00000000-0000-0000-0000-000000000000`，Clickhouse自带函数`generateUUIDv4()`可生成

* 日期
	- **Date** → 日期类型，用两个字节存储，表示从 1970-01-01 (无符号) 到当前的日期值。允许存储从 Unix 纪元开始到编译阶段定义的上限阈值常量（目前上限是2106年，但最终完全支持的年份为2105）。
	最小值输出为0000-00-00。日期中没有存储时区信息。
	- **DateTime** → 时间戳类型。用四个字节（无符号的）存储 Unix 时间戳）。允许存储与日期类型相同的范围内的值。Unix中的值范围为`[1970-01-01 00:00:00, 2105-12-31 23:59:59]`。
	默认情况下，客户端连接到服务的时候会使用服务端时区。您可以通过启用客户端命令行选项 `--use_client_time_zone`来设置使用客户端时间。

* 其他
	- **Enum8** → 用 `'String'= Int8` 对描述。例如 `x Enum8('hello' = 1, 'world' = 2)`
	- **Enum16** → 用` 'String'= Int16` 对描述。
	- **Array[T]**
	- **Tuple(T1, T2, ...)**

<br/>

# 6 表引擎
* **MergeTree**：适用于高负载任务的最通用和功能最强大的表引擎。这些引擎的共同特点是可以快速插入数据并进行后续的后台数据处理。
MergeTree系列引擎支持数据复制（使用Replicated* 的引擎版本），分区和一些其他引擎不支持的其他功能。
* **Log**：具有最小功能的轻量级引擎。当您需要快速写入许多小表（最多约100万行）并在以后整体读取它们时，该类型的引擎是最有效的。
* **Intergation engines**
	- **HDFS**：`ENGINE=HDFS('hdfs://hdfs1:9000/other_storage', 'TSV')`
	- **MySQL**：
	- Kafka：
	- JDBC：
	- ODBC：
* 其他
    - **[Distributed](https://clickhouse.yandex/docs/en/operations/table_engines/distributed/)**
    - MaterializedView
    - Dictionary
    - Merge
    - File
    - Null
    - Set
    - Join
    - URL
    - View
    - Memory
    - Buffer


# 7 数据库引擎
默认情况下，ClickHouse使用自己的数据库引擎，但它同时支持MySQL数据库引擎，这种方式将远程的MySQL服务器中的表映射到ClickHouse中，并允许您对表进行**INSERT**和**SELECT**查询，
以方便您在ClickHouse与MySQL之间进行数据交换。MySQL数据库引擎会将对其的查询转换为MySQL语法并发送到MySQL服务器中，因此我们可以执行诸如SHOW TABLES或SHOW CREATE TABLE之类的操作。
但是通过这种方式不能对数据进行：`ATTACH/DETACH`、`DROP`、`RENAME`、`CREATE TABLE`、`ALTER`。

下面我们通过ClickHouse的MySQL数据库引擎来查询MySQL中的一份数据来演示，其中一个表的数据大概有2kw。通过这个演示感受一下ClickHouse的速度。
```sql
-- 1 在ClickHouse中创建MySQL类型的数据库，同时与MySQL服务器交换数据：
cdh3 :) CREATE DATABASE IF NOT EXISTS flink_test
:-] ENGINE = MySQL('cdh1:3306', 'flink_test', 'scm', 'scm');
CREATE DATABASE IF NOT EXISTS flink_test
ENGINE = MySQL('cdh1:3306', 'flink_test', 'scm', 'scm')
Ok.
0 rows in set. Elapsed: 0.004 sec.

-- 2 查看库
cdh3 :) SHOW DATABASES;
SHOW DATABASES
→ Progress: 0.00 rows, 0.00 B (0.00 rows/s., 0.00 B/s.) ┌─name───────┐
│ default    │
│ flink_test │
│ system     │
└────────────┘
↘ Progress: 0.00 rows, 0.00 B (0.00 rows/s., 0.00 B/s.) ↓ Progress: 3.00 rows, 290.00 B (528.24 rows/s., 51.06 KB/s.)
3 rows in set. Elapsed: 0.006 sec.

-- 3 查看 flink_test 库中的表。此时在ClickHouse中便可以看到MySQL中的表。其它未用到的表已省略
cdh3 :) SHOW TABLES FROM flink_test;
SHOW TABLES FROM flink_test
↙ Progress: 0.00 rows, 0.00 B (0.00 rows/s., 0.00 B/s.) ┌─name───────────────────┐
│ vote_recordss_memory   │
│ w3                     │
└────────────────────────┘
← Progress: 0.00 rows, 0.00 B (0.00 rows/s., 0.00 B/s.) ↖ Progress: 17.00 rows, 661.00 B (1.95 thousand rows/s., 75.99 KB/s.)
17 rows in set. Elapsed: 0.009 sec.

-- 4 选择库
cdh3 :) USE flink_test;
USE flink_test
Ok.
0 rows in set. Elapsed: 0.005 sec.

-- 5 插入数据(表名区分大小写)
cdh3 :) INSERT INTO w3 VALUES(3, 'Mercury');
INSERT INTO w3 VALUES
Ok.
1 rows in set. Elapsed: 0.022 sec.

-- 6 查询数据。数据插入后不支持删除和更新。
cdh3 :) SELECT * FROM w3;
SELECT *
FROM w3
← Progress: 0.00 rows, 0.00 B (0.00 rows/s., 0.00 B/s.) ┌─id─┬─f1──────┐
│  3 │ Mercury │
│  5 │ success │
└────┴─────────┘
↖ Progress: 0.00 rows, 0.00 B (0.00 rows/s., 0.00 B/s.) ↑ Progress: 2.00 rows, 42.00 B (202.58 rows/s., 4.25 KB/s.)
2 rows in set. Elapsed: 0.010 sec.

-- 7 查看 MySQL 和 ClickHouse 对数据的聚合能力
--  7.1 MySQL。可以看到在MySQL中统计一张将近2千万数据量的表花费了 29.54 秒
mysql> SELECT COUNT(*) FROM vote_recordss_memory;
+----------+
| COUNT(*) |
+----------+
| 19999998 |
+----------+
1 row in set (29.54 sec)

--  7.2 ClickHouse 中执行一次COUNT，花费了 9.713 秒
cdh3 :) SELECT COUNT(*) FROM vote_recordss_memory;
SELECT COUNT(*)
FROM vote_recordss_memory
↘ Progress: 0.00 rows, 0.00 B (0.00 rows/s., 0.00 B/s.) ↓ Progress: 131.07 thousand rows, 131.07 KB (1.14 million rows/s., 1.14 MB/s.) ↙ Progress: 327.68 thousand rows, 327.68 KB (1.52 million rows/s., 1.52 MB/s.) ← Progress: 524.29 thousand rows, 524.29 KB (1.66 millio ┌──COUNT()─┐
│ 19999998 │
└──────────┘
↓ Progress: 19.79 million rows, 19.79 MB (2.04 million rows/s., 2.04 MB/s.) ↙ Progress: 20.00 million rows, 20.00 MB (2.06 million rows/s., 2.06 MB/s.)
1 rows in set. Elapsed: 9.713 sec. Processed 20.00 million rows, 20.00 MB (2.06 million rows/s., 2.06 MB/s.)

-- 7.3 在查询时指定mysql的连接、库名、表名、登陆信息，等价于上面的SQL。
cdh3 :) SELECT COUNT(*) FROM  mysql('cdh1:3306', 'flink_test', 'vote_recordss_memory', 'root', '123456');

-- 8 使用 ClickHouse 的 MergeTree 表引擎
--  8.1 切换到 ClickHouse 默认库下
cdh1 :) USE default;
USE default
Ok.
0 rows in set. Elapsed: 0.007 sec.

--  8.2 创建表并指定 MergeTree 表引擎，将MySQL数据加载进来，同时指定排序规则主键值为准
cdh1 :) CREATE TABLE vote_recordss
:-] ENGINE = MergeTree--(id, create_time)
:-] ORDER BY id AS
:-] SELECT * FROM mysql('cdh1:3306', 'flink_test', 'vote_recordss_memory', 'root', '123456');
CREATE TABLE vote_recordss
ENGINE = MergeTree
ORDER BY id AS
SELECT *
FROM mysql('cdh1:3306', 'flink_test', 'vote_recordss_memory', 'root', '123456')
↖ Progress: 65.54 thousand rows, 3.01 MB (299.97 thousand rows/s., 13.80 MB/s.) ↑ Progress: 131.07 thousand rows, 6.03 MB (411.12 thousand rows/s., 18.91 MB/s.) ↗ Progress: 196.61 thousand rows, 9.04 MB (468.88 thousand rows/s., 21.57 MB/s.) → Progress: 262.14 thousand  Ok.
0 rows in set. Elapsed: 27.917 sec. Processed 20.00 million rows, 920.00 MB (716.40 thousand rows/s., 32.95 MB/s.)

--  8.3 查询。可以看到是count某个值的速度速度约为MySQL的2950倍
cdh1 :) SELECT COUNT(*) FROM vote_recordss;
SELECT COUNT(*)
FROM vote_recordss
↙ Progress: 0.00 rows, 0.00 B (0.00 rows/s., 0.00 B/s.) ┌──COUNT()─┐
│ 19999998 │
└──────────┘
← Progress: 0.00 rows, 0.00 B (0.00 rows/s., 0.00 B/s.) ↖ Progress: 20.00 million rows, 80.00 MB (2.26 billion rows/s., 9.06 GB/s.)  98%
1 rows in set. Elapsed: 0.009 sec. Processed 20.00 million rows, 80.00 MB (2.20 billion rows/s., 8.79 GB/s.)

--  8.4 去重。可以看到ClickHouse速度约为MySQL的94倍
mysql> SELECT DISTINCT group_id from vote_recordss_memory ;
+----------+
| group_id |
+----------+
|        1 |
|        2 |
|        0 |
+----------+
3 rows in set (12.79 sec)
--  ClickHouse中执行
cdh1 :) SELECT DISTINCT group_id from vote_recordss;
SELECT DISTINCT group_id
FROM vote_recordss
↑ Progress: 0.00 rows, 0.00 B (0.00 rows/s., 0.00 B/s.) ┌─group_id─┐
│        0 │
│        2 │
│        1 │
└──────────┘
↗ Progress: 0.00 rows, 0.00 B (0.00 rows/s., 0.00 B/s.) → Progress: 19.04 million rows, 76.17 MB (145.18 million rows/s., 580.70 MB/s.)  94%↘ Progress: 20.00 million rows, 80.00 MB (147.97 million rows/s., 591.87 MB/s.)  98%
3 rows in set. Elapsed: 0.136 sec. Processed 20.00 million rows, 80.00 MB (147.44 million rows/s., 589.76 MB/s.)

--  8.5 分组统计。可以看到ClickHouse速度约为MySQL的94倍
mysql> SELECT SUM(vote_num),group_id from vote_recordss_memory GROUP BY group_id;
+---------------+----------+
| SUM(vote_num) | group_id |
+---------------+----------+
|   33344339689 |        0 |
|   33315889226 |        1 |
|   33351509121 |        2 |
+---------------+----------+
3 rows in set (16.26 sec)
--  ClickHouse中执行
cdh1 :)  SELECT SUM(vote_num),group_id from vote_recordss GROUP BY group_id;
SELECT
    SUM(vote_num),
    group_id
FROM vote_recordss
GROUP BY group_id
↙ Progress: 0.00 rows, 0.00 B (0.00 rows/s., 0.00 B/s.) ← Progress: 11.43 million rows, 91.42 MB (101.40 million rows/s., 811.20 MB/s.)  56%┌─SUM(vote_num)─┬─group_id─┐
│   33344339689 │        0 │
│   33351509121 │        2 │
│   33315889226 │        1 │
└───────────────┴──────────┘
↖ Progress: 11.43 million rows, 91.42 MB (66.08 million rows/s., 528.64 MB/s.)  56%↑ Progress: 20.00 million rows, 160.00 MB (115.61 million rows/s., 924.84 MB/s.)  98%
3 rows in set. Elapsed: 0.173 sec. Processed 20.00 million rows, 160.00 MB (115.56 million rows/s., 924.45 MB/s.)

--  8.6 排序取TOP 10。可以看到ClickHouse速度约为MySQL的25倍
mysql> SELECT * FROM vote_recordss_memory ORDER BY create_time DESC,vote_num LIMIT 10;
+----------+----------------------+----------+----------+--------+---------------------+
| id       | user_id              | vote_num | group_id | status | create_time         |
+----------+----------------------+----------+----------+--------+---------------------+
| 19999993 | 4u6PJYvsDD4khghreFvm |     2388 |        0 |      1 | 2019-10-15 01:00:20 |
| 19999998 | shTrosZpT5zux3wiKH5a |     4991 |        2 |      1 | 2019-10-15 01:00:20 |
| 19999995 | xRwQuMgQeuBoXvsBusFO |     6737 |        2 |      1 | 2019-10-15 01:00:20 |
| 19999996 | 5QNgMYoQUSsuX7Aqarw8 |     7490 |        2 |      2 | 2019-10-15 01:00:20 |
| 19999997 | eY12Wq9iSm0MH1PUTChk |     7953 |        0 |      2 | 2019-10-15 01:00:20 |
| 19999994 | ZpS0dWRm1TdhzTxTHCSj |     9714 |        0 |      1 | 2019-10-15 01:00:20 |
| 19999946 | kf7FOTUHAICP5Mv2xodI |       32 |        2 |      2 | 2019-10-15 01:00:19 |
| 19999738 | ER90qVc4CJCKH5bxXYTo |       57 |        1 |      2 | 2019-10-15 01:00:19 |
| 19999810 | gJHbBkGf0bJViwy5BB2d |      190 |        1 |      2 | 2019-10-15 01:00:19 |
| 19999977 | Wq7bogXRiHubhFlAHBJH |      208 |        0 |      2 | 2019-10-15 01:00:19 |
+----------+----------------------+----------+----------+--------+---------------------+
10 rows in set (15.31 sec)
--  ClickHouse中执行
cdh1 :)  SELECT * FROM vote_recordss ORDER BY create_time DESC,vote_num LIMIT 10;
SELECT *
FROM vote_recordss
ORDER BY
    create_time DESC,
    vote_num ASC
LIMIT 10
↗ Progress: 0.00 rows, 0.00 B (0.00 rows/s., 0.00 B/s.) → Progress: 2.34 million rows, 107.77 MB (21.21 million rows/s., 975.60 MB/s.)  11%↘ Progress: 5.31 million rows, 244.19 MB (24.97 million rows/s., 1.15 GB/s.)  26%↓ Progress: 8.75 million rows, 402.46 MB (27.97 mi%┌───────id─┬─user_id──────────────┬─vote_num─┬─group_id─┬─status─┬─────────create_time─┐
│ 19999993 │ 4u6PJYvsDD4khghreFvm │     2388 │        0 │      1 │ 2019-10-15 01:00:20 │
│ 19999998 │ shTrosZpT5zux3wiKH5a │     4991 │        2 │      1 │ 2019-10-15 01:00:20 │
│ 19999995 │ xRwQuMgQeuBoXvsBusFO │     6737 │        2 │      1 │ 2019-10-15 01:00:20 │
│ 19999996 │ 5QNgMYoQUSsuX7Aqarw8 │     7490 │        2 │      2 │ 2019-10-15 01:00:20 │
│ 19999997 │ eY12Wq9iSm0MH1PUTChk │     7953 │        0 │      2 │ 2019-10-15 01:00:20 │
│ 19999994 │ ZpS0dWRm1TdhzTxTHCSj │     9714 │        0 │      1 │ 2019-10-15 01:00:20 │
│ 19999946 │ kf7FOTUHAICP5Mv2xodI │       32 │        2 │      2 │ 2019-10-15 01:00:19 │
│ 19999738 │ ER90qVc4CJCKH5bxXYTo │       57 │        1 │      2 │ 2019-10-15 01:00:19 │
│ 19999810 │ gJHbBkGf0bJViwy5BB2d │      190 │        1 │      2 │ 2019-10-15 01:00:19 │
│ 19999977 │ Wq7bogXRiHubhFlAHBJH │      208 │        0 │      2 │ 2019-10-15 01:00:19 │
└──────────┴──────────────────────┴──────────┴──────────┴────────┴─────────────────────┘
↖ Progress: 16.65 million rows, 766.10 MB (27.46 million rows/s., 1.26 GB/s.)  82%↑ Progress: 20.00 million rows, 920.00 MB (32.98 million rows/s., 1.52 GB/s.)  98%
10 rows in set. Elapsed: 0.607 sec. Processed 20.00 million rows, 920.00 MB (32.93 million rows/s., 1.51 GB/s.)
```
在不同的查询查询场景下，ClickHouse都要比MySQL快很多，整个查询ClickHouse均能控制在1秒内，
这个给人的印象实在太深刻了，是不是有种，放开MySQL吧~，专业的事情让专业的数据库来做吧😸。

<br/>

# 8 示例
示例部分我们主要分析官方提供的一份航班数据集，这份数据有109个字段，记录了1987年到2018年之间的比较详细的航班信息，
例如我们可以从这份数据中获取每天的航班次数、每周延误超过10分钟的航班数、各航空公司延误超过10分钟的次数等等。
## 8.1 获取数据
获取航班数据集的方式有两种，第一种方式通过下载每年每月份的CSV文件，然后倒入ClickHouse数据库，这种方式大约需要下载374个zip包，共6.6GB。
第二种方式是直接下载下载预处理好的分区数据，解压到ClickHouse的数据目录下（/var/lib/clickhouse），
重启ClickHouse服务即可查询到新倒入的数据，这种方式需要下载大约16GB的数据。

第一种方式可能会由于网络原因有些文件下载时有损坏，导入时如果报错，需要重新下载那年那月的数据文件，下载时间较长。
采用第二种方式虽然数据文件较大，但是下载过程只有一个文件，下载相对快些。这里为了预先演示csv文件的导入过程，我们采用第一种方式获取数据，并导入到数据库。

执行如下脚本文件，迭代获取1987年到2018年的数据集文件。
```bash
for s in `seq 1987 2018`
do
for m in `seq 1 12`
do
wget https://transtats.bts.gov/PREZIP/On_Time_Reporting_Carrier_On_Time_Performance_1987_present_${s}_${m}.zip
done
done
```
## 8.2 插入数据
```bash
# 1 登录ClickHouse Client，在cdh3节点登录，因为下载的数据文件在cdh3节点
#  为了支持多行SQL，指定参数 --multiline
clickhouse-client -h 127.0.0.1 --port 9000 -u default --password KavrqeN1  --multiline
```

```sql
-- 2 在cdh3节点上创建表，主要用来导入数据时使用
cdh3 :) CREATE TABLE `ontime` (
  `Year` UInt16,
  `Quarter` UInt8,
  `Month` UInt8,
  `DayofMonth` UInt8,
  `DayOfWeek` UInt8,
  `FlightDate` Date,
  `UniqueCarrier` FixedString(7),
  `AirlineID` Int32,
  `Carrier` FixedString(2),
  `TailNum` String,
  `FlightNum` String,
  `OriginAirportID` Int32,
  `OriginAirportSeqID` Int32,
  `OriginCityMarketID` Int32,
  `Origin` FixedString(5),
  `OriginCityName` String,
  `OriginState` FixedString(2),
  `OriginStateFips` String,
  `OriginStateName` String,
  `OriginWac` Int32,
  `DestAirportID` Int32,
  `DestAirportSeqID` Int32,
  `DestCityMarketID` Int32,
  `Dest` FixedString(5),
  `DestCityName` String,
  `DestState` FixedString(2),
  `DestStateFips` String,
  `DestStateName` String,
  `DestWac` Int32,
  `CRSDepTime` Int32,
  `DepTime` Int32,
  `DepDelay` Int32,
  `DepDelayMinutes` Int32,
  `DepDel15` Int32,
  `DepartureDelayGroups` String,
  `DepTimeBlk` String,
  `TaxiOut` Int32,
  `WheelsOff` Int32,
  `WheelsOn` Int32,
  `TaxiIn` Int32,
  `CRSArrTime` Int32,
  `ArrTime` Int32,
  `ArrDelay` Int32,
  `ArrDelayMinutes` Int32,
  `ArrDel15` Int32,
  `ArrivalDelayGroups` Int32,
  `ArrTimeBlk` String,
  `Cancelled` UInt8,
  `CancellationCode` FixedString(1),
  `Diverted` UInt8,
  `CRSElapsedTime` Int32,
  `ActualElapsedTime` Int32,
  `AirTime` Int32,
  `Flights` Int32,
  `Distance` Int32,
  `DistanceGroup` UInt8,
  `CarrierDelay` Int32,
  `WeatherDelay` Int32,
  `NASDelay` Int32,
  `SecurityDelay` Int32,
  `LateAircraftDelay` Int32,
  `FirstDepTime` String,
  `TotalAddGTime` String,
  `LongestAddGTime` String,
  `DivAirportLandings` String,
  `DivReachedDest` String,
  `DivActualElapsedTime` String,
  `DivArrDelay` String,
  `DivDistance` String,
  `Div1Airport` String,
  `Div1AirportID` Int32,
  `Div1AirportSeqID` Int32,
  `Div1WheelsOn` String,
  `Div1TotalGTime` String,
  `Div1LongestGTime` String,
  `Div1WheelsOff` String,
  `Div1TailNum` String,
  `Div2Airport` String,
  `Div2AirportID` Int32,
  `Div2AirportSeqID` Int32,
  `Div2WheelsOn` String,
  `Div2TotalGTime` String,
  `Div2LongestGTime` String,
  `Div2WheelsOff` String,
  `Div2TailNum` String,
  `Div3Airport` String,
  `Div3AirportID` Int32,
  `Div3AirportSeqID` Int32,
  `Div3WheelsOn` String,
  `Div3TotalGTime` String,
  `Div3LongestGTime` String,
  `Div3WheelsOff` String,
  `Div3TailNum` String,
  `Div4Airport` String,
  `Div4AirportID` Int32,
  `Div4AirportSeqID` Int32,
  `Div4WheelsOn` String,
  `Div4TotalGTime` String,
  `Div4LongestGTime` String,
  `Div4WheelsOff` String,
  `Div4TailNum` String,
  `Div5Airport` String,
  `Div5AirportID` Int32,
  `Div5AirportSeqID` Int32,
  `Div5WheelsOn` String,
  `Div5TotalGTime` String,
  `Div5LongestGTime` String,
  `Div5WheelsOff` String,
  `Div5TailNum` String
) ENGINE = MergeTree(FlightDate, (Year, FlightDate), 8192);

-- 3 查看创建的表
cdh3 :) SHOW TABLES;
SHOW TABLES
↗ Progress: 0.00 rows, 0.00 B (0.00 rows/s., 0.00 B/s.) ┌─name───┐
│ ontime │
└────────┘
→ Progress: 0.00 rows, 0.00 B (0.00 rows/s., 0.00 B/s.) ↘ Progress: 1.00 rows, 31.00 B (357.34 rows/s., 11.08 KB/s.)
1 rows in set. Elapsed: 0.003 sec.

-- 4 也可以对创建的表进行修改名字
RENAME TABLE ontime TO ontime_local;
```

下面开始导入数据，这里为了查看整个导入数据执行的时间，我这里写了一个小脚本，执行这个脚本开始导入数据。将下面的脚本保存到cdh3节点的下载的数据文件的目录下，
例如保存为 `load_to_ontime.sh`，赋予脚本文件执行权限`chmod +x load_to_ontime.sh`，并执行 `./load_to_ontime.sh`。
因为中间有加压的过程，导入会需要一段时间，这里导入的过程大概花费了1933045 毫秒，共 184694329 条数据。
```bash
# 获取当前毫秒时间的函数
getMsec(){
	current=`date "+%Y-%m-%d %H:%M:%S"`     #获取当前时间，例：2016-06-06 15:40:41       
	timeStamp=`date -d "$current" +%s`      #将current转换为时间戳，精确到秒
	currentTimeStamp=$((timeStamp*1000+`date "+%N"`/1000000)) #将current转换为时间戳，精确到毫秒
}

getMsec
a=$currentTimeStamp
# ----------------
#java -version 
for i in *.zip; do echo $i; unzip -cq $i '*.csv' | sed 's/\.00//g' | clickhouse-client -h cdh5 -u default --password F4N/ihgS --query="INSERT INTO ontime FORMAT CSVWithNames"; done

# ----------------
getMsec
b=$currentTimeStamp
spendtime=$((b-a))

echo -e '\n ---------------------------- '
echo "共费时：$spendtime 毫秒"

```

导入完毕后，可以在cdh3节点查看插入的数据条数。并且可以看到查询的速度非常快，在[7 数据库引擎](#7) 小节时MySQL中COUNT一下表的数据条数时，在2kw条数据时花费了近半分钟。
```sql
cdh3 :) SELECT COUNT(1) FROM ontime;
SELECT COUNT(1)
FROM ontime
↖ Progress: 0.00 rows, 0.00 B (0.00 rows/s., 0.00 B/s.) ↑ Progress: 19.57 million rows, 19.57 MB (141.60 million rows/s., 141.60 MB/s.)  10%↗ Progress: 43.43 million rows, 43.43 MB (182.14 million rows/s., 182.14 MB/s.)  23%→ Progress: 63.75 million rows, 63.75 MB (188.%┌──COUNT(1)─┐
│ 184694329 │
└───────────┘
↖ Progress: 160.87 million rows, 160.87 MB (200.45 million rows/s., 200.45 MB/s.)  85%↑ Progress: 184.69 million rows, 184.69 MB (230.12 million rows/s., 230.12 MB/s.)  98%
1 rows in set. Elapsed: 0.803 sec. Processed 184.69 million rows, 184.69 MB (230.03 million rows/s., 230.03 MB/s.)
```
## 8.3 创建分布式表并加载数据
分布表（Distributed）本身不存储数据，相当于路由，在创建时需要指定集群名、数据库名、数据表名、分片KEY，这里分片用rand()函数，表示随机分片。
查询分布式表会根据集群配置信息，路由到具体的数据表，再把结果进行合并。
```bash
# 1 创建一个分片的本地表
#  这里分别在 cdh1、cdh2、cdh3节点，执行如下进入 clickhouse client
clickhouse-client -h 127.0.0.1 --port 9000 -u default --password KavrqeN1  --multiline
```

```sql
-- 执行16.9.2节的 2步创建ontime表相似的建表语句,只是表名改为ontime_local 
CREATE TABLE `ontime_local` (
  …… 
) ENGINE = MergeTree(FlightDate, (Year, FlightDate), 8192);

-- 2 创建分布式表。
--  ontime_all与ontime在同一个节点上，方便插入数据。
cdh3 :) CREATE TABLE ontime_all AS ontime_local
ENGINE = Distributed(perftest_3shards_1replicas, default, ontime_local, rand());

-- 3 查看cdh3 节点的表。
cdh3 :) SHOW TABLES;
SHOW TABLES
↗ Progress: 0.00 rows, 0.00 B (0.00 rows/s., 0.00 B/s.) ┌─name─────────┐
│ ontime       │
│ ontime_all   │
│ ontime_local │
└──────────────┘
→ Progress: 0.00 rows, 0.00 B (0.00 rows/s., 0.00 B/s.) ↘ Progress: 3.00 rows, 103.00 B (1.32 thousand rows/s., 45.45 KB/s.)
3 rows in set. Elapsed: 0.002 sec.

-- 4 插入数据到分布式表。
cdh3 :) INSERT INTO ontime_all SELECT * FROM ontime;
INSERT INTO ontime_all SELECT *
FROM ontime
→ Progress: 188.42 thousand rows, 136.97 MB (341.59 thousand rows/s., 248.32 MB/s.) ▏                                                                                                                                                                                       0%%
↗ Progress: 90.92 million rows, 66.03 GB (322.35 thousand rows/s., 234.09 MB/s.) ██████████████████████████████████████████████████████████████████████████████████████████▋                                                                                                48%Ok.
0 rows in set. Elapsed: 571.056 sec. Processed 184.69 million rows, 134.19 GB (323.43 thousand rows/s., 234.98 MB/s.)

-- 5 查看各个节点的分片表中的数据。
--  可以看到三个节点的分片数据之和等于 ontime 表的数据总数，也就是 ontime_all 表的数据总数
--  5.1 cdh1
cdh1 :) SELECT COUNT(*) FROM ontime_local;
SELECT COUNT(*)
FROM ontime_local
↙ Progress: 0.00 rows, 0.00 B (0.00 rows/s., 0.00 B/s.) ← Progress: 3.32 million rows, 3.32 MB (18.15 million rows/s., 18.15 MB/s.)  4%↖ Progress: 4.73 million rows, 4.73 MB (16.70 million rows/s., 16.70 MB/s.)  6%↑ Progress: 6.16 million rows, 6.16 MB (16.06 million ro%┌──COUNT()─┐
│ 61562643 │
└──────────┘
↘ Progress: 59.96 million rows, 59.96 MB (15.60 million rows/s., 15.60 MB/s.) ██████████████████████████████████████████████████████████████████████████████████████████████████████████████▊   87%
1 rows in set. Elapsed: 3.843 sec. Processed 61.56 million rows, 61.56 MB (16.02 million rows/s., 16.02 MB/s.)
--  5.2 cdh2
cdh2 :)  SELECT COUNT(*) FROM ontime_local;
SELECT COUNT(*)
FROM ontime_local
↙ Progress: 0.00 rows, 0.00 B (0.00 rows/s., 0.00 B/s.) ← Progress: 7.69 million rows, 7.69 MB (42.52 million rows/s., 42.52 MB/s.)  11%↖ Progress: 11.15 million rows, 11.15 MB (39.69 million rows/s., 39.69 MB/s.)  16%↑ Progress: 13.48 million rows, 13.48 MB (35.35 mill%┌──COUNT()─┐
│ 61555036 │
└──────────┘
↑ Progress: 60.19 million rows, 60.19 MB (16.99 million rows/s., 16.99 MB/s.) ██████████████████████████████████████████████████████████████████████████████████████████████████████████████▊   88%
1 rows in set. Elapsed: 3.543 sec. Processed 61.56 million rows, 61.56 MB (17.37 million rows/s., 17.37 MB/s.)
--  5.3 cdh3
cdh3 :)  SELECT COUNT(*) FROM ontime_local;
SELECT COUNT(*)
FROM ontime_local
↑ Progress: 0.00 rows, 0.00 B (0.00 rows/s., 0.00 B/s.) ↗ Progress: 3.13 million rows, 3.13 MB (17.45 million rows/s., 17.45 MB/s.)  4%→ Progress: 4.56 million rows, 4.56 MB (16.34 million rows/s., 16.34 MB/s.)  6%↘ Progress: 6.12 million rows, 6.12 MB (16.13 million ro%┌──COUNT()─┐
│ 61576650 │
└──────────┘
← Progress: 59.97 million rows, 59.97 MB (15.55 million rows/s., 15.55 MB/s.) ██████████████████████████████████████████████████████████████████████████████████████████████████████████████▊   88%
1 rows in set. Elapsed: 3.858 sec. Processed 61.58 million rows, 61.58 MB (15.96 million rows/s., 15.96 MB/s.)

-- 6 查看分布式表的数据总数。
cdh3 :)  SELECT COUNT(*) FROM ontime_all;
SELECT COUNT(*)
FROM ontime_all
↑ Progress: 0.00 rows, 0.00 B (0.00 rows/s., 0.00 B/s.) ↗ Progress: 3.37 million rows, 3.37 MB (18.02 million rows/s., 18.02 MB/s.)  4%→ Progress: 4.74 million rows, 4.74 MB (16.54 million rows/s., 16.54 MB/s.)  6%↘ Progress: 13.79 million rows, 13.79 MB (35.62 million %┌───COUNT()─┐
│ 184694329 │
└───────────┘
↑ Progress: 183.85 million rows, 183.85 MB (45.68 million rows/s., 45.68 MB/s.) ██████████████████████████████████████████████████████████████████████████████████████████████████████████████▊   89%
1 rows in set. Elapsed: 4.026 sec. Processed 184.69 million rows, 184.69 MB (45.88 million rows/s., 45.88 MB/s.)
```

## 8.4 执行SQL
```sql
-- Q0 平均每月的航班记录数
cdh3 :) SELECT avg(c1) FROM (
:-]     SELECT Year, Month, count(*) AS c1
:-]     FROM ontime_all
:-]     GROUP BY Year, Month
:-] );
SELECT avg(c1)
FROM
(
    SELECT
        Year,
        Month,
        count(*) AS c1
    FROM ontime_all
    GROUP BY
        Year,
        Month
)
↖ Progress: 0.00 rows, 0.00 B (0.00 rows/s., 0.00 B/s.) ↑ Progress: 28.47 million rows, 85.42 MB (147.53 million rows/s., 442.59 MB/s.)  41%↗ Progress: 83.67 million rows, 251.00 MB (282.10 million rows/s., 846.30 MB/s.)  61%→ Progress: 145.80 million rows, 437.41 MB (3%┌────────────avg(c1)─┐
│ 493835.10427807487 │
└────────────────────┘
↘ Progress: 145.80 million rows, 437.41 MB (308.61 million rows/s., 925.82 MB/s.)  71%↓ Progress: 184.69 million rows, 554.08 MB (390.67 million rows/s., 1.17 GB/s.)  90%
1 rows in set. Elapsed: 0.474 sec. Processed 184.69 million rows, 554.08 MB (389.56 million rows/s., 1.17 GB/s.)

-- Q1. 查询从2000年到2008年每天的航班数
cdh3 :) SELECT DayOfWeek, count(*) AS c
:-] FROM ontime_all
:-] WHERE Year>=2000 AND Year<=2008
:-] GROUP BY DayOfWeek
:-] ORDER BY c DESC;
SELECT
    DayOfWeek,
    count(*) AS c
FROM ontime_all
WHERE (Year >= 2000) AND (Year <= 2008)
GROUP BY DayOfWeek
ORDER BY c DESC
← Progress: 0.00 rows, 0.00 B (0.00 rows/s., 0.00 B/s.) ┌─DayOfWeek─┬───────c─┐
│         5 │ 8732422 │
│         1 │ 8730614 │
│         4 │ 8710843 │
│         3 │ 8685626 │
│         2 │ 8639632 │
│         7 │ 8274367 │
│         6 │ 7514194 │
└───────────┴─────────┘
↖ Progress: 0.00 rows, 0.00 B (0.00 rows/s., 0.00 B/s.) ↑ Progress: 59.29 million rows, 177.86 MB (456.57 million rows/s., 1.37 GB/s.)  92%
7 rows in set. Elapsed: 0.130 sec. Processed 59.29 million rows, 177.86 MB (455.75 million rows/s., 1.37 GB/s.)

-- Q2. 查询从2000年到2008年每周延误超过10分钟的航班数。
cdh3 :) SELECT DayOfWeek, count(*) AS c
:-] FROM ontime_all
:-] WHERE DepDelay>10 AND Year>=2000 AND Year<=2008
:-] GROUP BY DayOfWeek
:-] ORDER BY c DESC;
SELECT
    DayOfWeek,
    count(*) AS c
FROM ontime_all
WHERE (DepDelay > 10) AND (Year >= 2000) AND (Year <= 2008)
GROUP BY DayOfWeek
ORDER BY c DESC
↑ Progress: 0.00 rows, 0.00 B (0.00 rows/s., 0.00 B/s.) ↗ Progress: 12.24 million rows, 56.74 MB (87.55 million rows/s., 405.92 MB/s.)  57%→ Progress: 38.83 million rows, 179.93 MB (161.84 million rows/s., 749.94 MB/s.)  60%┌─DayOfWeek─┬───────c─┐
│         5 │ 2088300 │
│         4 │ 1918325 │
│         1 │ 1795120 │
│         7 │ 1782292 │
│         3 │ 1640798 │
│         2 │ 1538291 │
│         6 │ 1391984 │
└───────────┴─────────┘
↘ Progress: 38.83 million rows, 179.93 MB (121.54 million rows/s., 563.16 MB/s.)  60%↓ Progress: 59.29 million rows, 273.62 MB (185.56 million rows/s., 856.35 MB/s.)  92%
7 rows in set. Elapsed: 0.320 sec. Processed 59.29 million rows, 273.62 MB (185.52 million rows/s., 856.17 MB/s.)

-- Q3. 查询2000年到2008年每个机场延误超过10分钟以上的次数
cdh3 :) SELECT Origin, count(*) AS c
:-] FROM ontime_all
:-] WHERE DepDelay>10 AND Year>=2000 AND Year<=2008
:-] GROUP BY Origin
:-] ORDER BY c DESC
:-] LIMIT 10;
SELECT
    Origin,
    count(*) AS c
FROM ontime_all
WHERE (DepDelay > 10) AND (Year >= 2000) AND (Year <= 2008)
GROUP BY Origin
ORDER BY c DESC
LIMIT 10
↖ Progress: 0.00 rows, 0.00 B (0.00 rows/s., 0.00 B/s.) ↑ Progress: 18.71 million rows, 102.05 MB (125.90 million rows/s., 686.73 MB/s.)  87%↗ Progress: 49.15 million rows, 267.10 MB (197.87 million rows/s., 1.08 GB/s.)  76%┌─Origin─┬──────c─┐
│ ORD    │ 846692 │
│ ATL    │ 822955 │
│ DFW    │ 601318 │
│ LAX    │ 391247 │
│ PHX    │ 391191 │
│ LAS    │ 351713 │
│ DEN    │ 345108 │
│ EWR    │ 292916 │
│ DTW    │ 289233 │
│ IAH    │ 283861 │
└────────┴────────┘
→ Progress: 49.15 million rows, 267.10 MB (167.80 million rows/s., 911.83 MB/s.)  76%↘ Progress: 59.29 million rows, 322.25 MB (202.30 million rows/s., 1.10 GB/s.)  92%
10 rows in set. Elapsed: 0.293 sec. Processed 59.29 million rows, 322.25 MB (202.26 million rows/s., 1.10 GB/s.)

-- Q4. 查询2007年各航空公司延误超过10分钟以上的次数
cdh3 :) SELECT Carrier, count(*)
:-] FROM ontime_all
:-] WHERE DepDelay>10 AND Year=2007
:-] GROUP BY Carrier
:-] ORDER BY count(*) DESC;
SELECT
    Carrier,
    count(*)
FROM ontime_all
WHERE (DepDelay > 10) AND (Year = 2007)
GROUP BY Carrier
ORDER BY count(*) DESC
↖ Progress: 0.00 rows, 0.00 B (0.00 rows/s., 0.00 B/s.) ┌─Carrier─┬─count()─┐
│ WN      │  296293 │
│ AA      │  176203 │
│ MQ      │  145630 │
│ US      │  135987 │
│ UA      │  128174 │
│ OO      │  127426 │
│ EV      │  101796 │
│ XE      │   99915 │
│ DL      │   93675 │
│ NW      │   90429 │
│ CO      │   76662 │
│ YV      │   67905 │
│ FL      │   59460 │
│ OH      │   59034 │
│ B6      │   50740 │
│ 9E      │   46948 │
│ AS      │   42830 │
│ F9      │   23035 │
│ AQ      │    4299 │
│ HA      │    2746 │
└─────────┴─────────┘
↑ Progress: 0.00 rows, 0.00 B (0.00 rows/s., 0.00 B/s.) ↗ Progress: 7.46 million rows, 37.14 MB (113.75 million rows/s., 566.67 MB/s.)  95%
20 rows in set. Elapsed: 0.066 sec. Processed 7.46 million rows, 37.14 MB (113.40 million rows/s., 564.90 MB/s.)

-- Old ANY INNER|RIGHT|FULL join在默认情况下是禁用的，需要开启才能执行下面的SQL
cdh3 :) Set any_join_distinct_right_table_keys=1;
SET any_join_distinct_right_table_keys = 1
Ok.
0 rows in set. Elapsed: 0.001 sec.
-- Q5. 查询2007年各航空公司延误超过10分钟以上的百分比
cdh3 :) SELECT Carrier, c, c2, c*100/c2 as c3 FROM (
:-]     SELECT Carrier, count(*) AS c FROM ontime_all
:-]     WHERE DepDelay>10
:-]         AND Year=2007
:-]     GROUP BY Carrier
:-] )ANY INNER JOIN(
:-]     SELECT Carrier, count(*) AS c2
:-]     FROM ontime_all
:-]     WHERE Year=2007
:-]     GROUP BY Carrier
:-] ) USING Carrier
:-] ORDER BY c3 DESC;
SELECT
    Carrier,
    c,
    c2,
    (c * 100) / c2 AS c3
FROM
(
    SELECT
        Carrier,
        count(*) AS c
    FROM ontime_all
    WHERE (DepDelay > 10) AND (Year = 2007)
    GROUP BY Carrier
)
ANY INNER JOIN
(
    SELECT
        Carrier,
        count(*) AS c2
    FROM ontime_all
    WHERE Year = 2007
    GROUP BY Carrier
) USING (Carrier)
ORDER BY c3 DESC
↙ Progress: 0.00 rows, 0.00 B (0.00 rows/s., 0.00 B/s.) ← Progress: 9.94 million rows, 42.20 MB (83.14 million rows/s., 352.96 MB/s.)  95%┌─Carrier─┬──────c─┬──────c2─┬─────────────────c3─┐
│ EV      │ 101796 │  286234 │ 35.563909249075934 │
│ US      │ 135987 │  485447 │ 28.012738774778708 │
│ AA      │ 176203 │  633857 │ 27.798541311368336 │
│ MQ      │ 145630 │  540494 │ 26.943869867195566 │
│ AS      │  42830 │  160185 │  26.73783437899928 │
│ B6      │  50740 │  191450 │ 26.503003395142336 │
│ UA      │ 128174 │  490002 │  26.15785241692891 │
│ WN      │ 296293 │ 1168871 │ 25.348648396615197 │
│ OH      │  59034 │  236032 │ 25.011015455531453 │
│ CO      │  76662 │  323151 │  23.72327487768876 │
│ F9      │  23035 │   97760 │ 23.562806873977088 │
│ YV      │  67905 │  294362 │ 23.068534661403305 │
│ XE      │  99915 │  434773 │  22.98095787916913 │
│ FL      │  59460 │  263159 │  22.59470510223857 │
│ NW      │  90429 │  414526 │  21.81503693375084 │
│ OO      │ 127426 │  597880 │  21.31297250284338 │
│ DL      │  93675 │  475889 │  19.68421207466447 │
│ 9E      │  46948 │  258851 │  18.13707499681284 │
│ AQ      │   4299 │   46360 │  9.273080241587575 │
│ HA      │   2746 │   56175 │  4.888295505117935 │
└─────────┴────────┴─────────┴────────────────────┘
↖ Progress: 9.94 million rows, 42.20 MB (76.76 million rows/s., 325.89 MB/s.)  95%↑ Progress: 14.91 million rows, 66.96 MB (115.10 million rows/s., 516.88 MB/s.)  95%
20 rows in set. Elapsed: 0.130 sec. Processed 14.91 million rows, 66.96 MB (114.91 million rows/s., 516.05 MB/s.)
-- 另一个查询版本
cdh3 :) SELECT Carrier, avg(DepDelay>10)*100 AS c3
:-] FROM ontime_all
:-] WHERE Year=2007
:-] GROUP BY Carrier
:-] ORDER BY Carrier;
SELECT
    Carrier,
    avg(DepDelay > 10) * 100 AS c3
FROM ontime_all
WHERE Year = 2007
GROUP BY Carrier
ORDER BY Carrier ASC
↓ Progress: 0.00 rows, 0.00 B (0.00 rows/s., 0.00 B/s.) ┌─Carrier─┬─────────────────c3─┐
│ 9E      │  18.13707499681284 │
│ AA      │ 27.798541311368336 │
│ AQ      │  9.273080241587575 │
│ AS      │  26.73783437899928 │
│ B6      │ 26.503003395142333 │
│ CO      │  23.72327487768876 │
│ DL      │  19.68421207466447 │
│ EV      │ 35.563909249075934 │
│ F9      │ 23.562806873977088 │
│ FL      │  22.59470510223857 │
│ HA      │  4.888295505117935 │
│ MQ      │ 26.943869867195563 │
│ NW      │  21.81503693375084 │
│ OH      │ 25.011015455531453 │
│ OO      │  21.31297250284338 │
│ UA      │  26.15785241692891 │
│ US      │  28.01273877477871 │
│ WN      │ 25.348648396615197 │
│ XE      │ 22.980957879169132 │
│ YV      │ 23.068534661403305 │
└─────────┴────────────────────┘
↙ Progress: 0.00 rows, 0.00 B (0.00 rows/s., 0.00 B/s.) ← Progress: 7.46 million rows, 59.65 MB (73.51 million rows/s., 588.09 MB/s.)  95%
20 rows in set. Elapsed: 0.102 sec. Processed 7.46 million rows, 59.65 MB (73.40 million rows/s., 587.24 MB/s.)

--Q6. 同上一个查询一致,只是查询范围扩大到2000年到2008年
cdh3 :) SELECT Carrier, c, c2, c*100/c2 as c3 FROM (
:-]     SELECT Carrier, count(*) AS c
:-]     FROM ontime_all
:-]     WHERE DepDelay>10
:-]         AND Year>=2000 AND Year<=2008
:-]     GROUP BY Carrier
:-] )ANY INNER JOIN(
:-]     SELECT Carrier, count(*) AS c2
:-]     FROM ontime_all
:-]     WHERE Year>=2000 AND Year<=2008
:-]     GROUP BY Carrier
:-] ) USING Carrier
:-] ORDER BY c3 DESC;
SELECT
    Carrier,
    c,
    c2,
    (c * 100) / c2 AS c3
FROM
(
    SELECT
        Carrier,
        count(*) AS c
    FROM ontime_all
    WHERE (DepDelay > 10) AND (Year >= 2000) AND (Year <= 2008)
    GROUP BY Carrier
)
ANY INNER JOIN
(
    SELECT
        Carrier,
        count(*) AS c2
    FROM ontime_all
    WHERE (Year >= 2000) AND (Year <= 2008)
    GROUP BY Carrier
) USING (Carrier)
ORDER BY c3 DESC
← Progress: 0.00 rows, 0.00 B (0.00 rows/s., 0.00 B/s.) ↖ Progress: 2.69 million rows, 10.77 MB (18.70 million rows/s., 74.79 MB/s.)  12%↑ Progress: 10.09 million rows, 40.34 MB (41.29 million rows/s., 165.18 MB/s.)  15%↗ Progress: 15.35 million rows, 61.40 MB (44.10 mi%┌─Carrier─┬───────c─┬──────c2─┬─────────────────c3─┐
│ EV      │  461050 │ 1697172 │ 27.165779308166762 │
│ AS      │  354145 │ 1427189 │ 24.814162665211125 │
│ B6      │  197249 │  811341 │ 24.311479390293353 │
│ FL      │  298916 │ 1265138 │ 23.627145813342104 │
│ WN      │ 2165483 │ 9280539 │  23.33359086147906 │
│ YV      │  198787 │  854056 │ 23.275640004870876 │
│ XE      │  233488 │ 1036015 │ 22.537125427720643 │
│ MQ      │  876799 │ 3954895 │ 22.169969114224273 │
│ UA      │ 1096646 │ 5094635 │ 21.525506734044736 │
│ F9      │   72150 │  336958 │ 21.412164127279958 │
│ DH      │  147041 │  693047 │ 21.216598585665906 │
│ OH      │  301681 │ 1466421 │ 20.572605002246966 │
│ HP      │  245293 │ 1208561 │ 20.296286244550338 │
│ AA      │ 1276555 │ 6318386 │ 20.203814708376473 │
│ US      │  909154 │ 4650400 │ 19.550017202821262 │
│ TW      │   94808 │  511852 │  18.52254167220212 │
│ OO      │  556247 │ 3090849 │  17.99657634520483 │
│ CO      │  522219 │ 2925290 │ 17.851871096540854 │
│ DL      │ 1050448 │ 5912486 │  17.76660443678006 │
│ 9E      │   89391 │  521059 │ 17.155638804818647 │
│ NW      │  725076 │ 4280049 │ 16.940834088581695 │
│ RU      │  216279 │ 1314294 │ 16.455907125802902 │
│ TZ      │   32998 │  208420 │ 15.832453699261107 │
│ AQ      │   17239 │  154381 │ 11.166529559984713 │
│ HA      │   15968 │  274265 │  5.822106356990502 │
└─────────┴─────────┴─────────┴────────────────────┘
↖ Progress: 108.85 million rows, 476.35 MB (65.70 million rows/s., 287.49 MB/s.) ███████████████████████████████████████████████████████████████████████████████████████████████████████████████████████████████████████████████████████████████▌                           84%
25 rows in set. Elapsed: 1.657 sec. Processed 118.58 million rows, 522.95 MB (71.56 million rows/s., 315.58 MB/s.)
-- 另一个查询版本
cdh3 :) SELECT Carrier, avg(DepDelay>10)*100 AS c3
:-] FROM ontime_all
:-] WHERE Year>=2000 AND Year<=2008
:-] GROUP BY Carrier
:-] ORDER BY Carrier;
SELECT
    Carrier,
    avg(DepDelay > 10) * 100 AS c3
FROM ontime_all
WHERE (Year >= 2000) AND (Year <= 2008)
GROUP BY Carrier
ORDER BY Carrier ASC
← Progress: 0.00 rows, 0.00 B (0.00 rows/s., 0.00 B/s.) ↖ Progress: 19.77 million rows, 158.15 MB (144.99 million rows/s., 1.16 GB/s.)  92%┌─Carrier─┬─────────────────c3─┐
│ 9E      │ 17.155638804818647 │
│ AA      │ 20.203814708376473 │
│ AQ      │ 11.166529559984713 │
│ AS      │ 24.814162665211125 │
│ B6      │ 24.311479390293353 │
│ CO      │ 17.851871096540854 │
│ DH      │ 21.216598585665906 │
│ DL      │  17.76660443678006 │
│ EV      │  27.16577930816676 │
│ F9      │ 21.412164127279958 │
│ FL      │   23.6271458133421 │
│ HA      │  5.822106356990502 │
│ HP      │ 20.296286244550338 │
│ MQ      │ 22.169969114224273 │
│ NW      │ 16.940834088581695 │
│ OH      │  20.57260500224697 │
│ OO      │  17.99657634520483 │
│ RU      │ 16.455907125802902 │
│ TW      │  18.52254167220212 │
│ TZ      │ 15.832453699261107 │
│ UA      │ 21.525506734044736 │
│ US      │ 19.550017202821262 │
│ WN      │  23.33359086147906 │
│ XE      │ 22.537125427720643 │
│ YV      │ 23.275640004870876 │
└─────────┴────────────────────┘
↑ Progress: 19.77 million rows, 158.15 MB (89.81 million rows/s., 718.53 MB/s.)  92%↗ Progress: 59.29 million rows, 474.31 MB (269.31 million rows/s., 2.15 GB/s.)  92%
25 rows in set. Elapsed: 0.220 sec. Processed 59.29 million rows, 474.31 MB (269.15 million rows/s., 2.15 GB/s.)

-- Q7. 每年航班延误超过10分钟的百分比
cdh3 :) SELECT Year, c1/c2 FROM (
:-]     select Year, count(*)*100 as c1
:-]     from ontime_all
:-]     WHERE DepDelay>10
:-]     GROUP BY Year
:-] )ANY INNER JOIN(
:-]     select Year, count(*) as c2
:-]     from ontime_all
:-]     GROUP BY Year
:-] ) USING (Year)
:-] ORDER BY Year;
SELECT
    Year,
    c1 / c2
FROM
(
    SELECT
        Year,
        count(*) * 100 AS c1
    FROM ontime_all
    WHERE DepDelay > 10
    GROUP BY Year
)
ANY INNER JOIN
(
    SELECT
        Year,
        count(*) AS c2
    FROM ontime_all
    GROUP BY Year
) USING (Year)
ORDER BY Year ASC
↑ Progress: 0.00 rows, 0.00 B (0.00 rows/s., 0.00 B/s.) ↗ Progress: 15.13 million rows, 30.25 MB (65.64 million rows/s., 131.28 MB/s.)  22%→ Progress: 61.58 million rows, 123.15 MB (186.24 million rows/s., 372.48 MB/s.)  90%↘ Progress: 192.43 million rows, 403.55 MB (43%┌─Year─┬─────divide(c1, c2)─┐
│ 1987 │  19.91704692543066 │
│ 1988 │  16.61709049583091 │
│ 1989 │ 19.950091248115527 │
│ 1990 │ 16.645130151570143 │
│ 1991 │ 14.721627756959183 │
│ 1992 │ 14.675431256341861 │
│ 1993 │ 15.424984631696157 │
│ 1994 │ 16.568031802021913 │
│ 1995 │ 19.221638572082064 │
│ 1996 │ 22.182805887088954 │
│ 1997 │  19.16513468701882 │
│ 1998 │  19.35637890988224 │
│ 1999 │ 20.087415003643347 │
│ 2000 │  23.17167181619297 │
│ 2001 │ 18.905807519714198 │
│ 2002 │ 16.237691267090707 │
│ 2003 │ 15.024550977569685 │
│ 2004 │ 19.248380268947592 │
│ 2005 │ 20.759289560703337 │
│ 2006 │ 23.155993582679844 │
│ 2007 │  24.53487096299114 │
│ 2008 │  21.99228614641999 │
│ 2009 │ 19.752600078911243 │
│ 2010 │  20.35832838381071 │
│ 2011 │ 20.293064527340643 │
│ 2012 │ 19.324733358461426 │
│ 2013 │ 22.717310450049155 │
│ 2014 │  23.99744596516966 │
│ 2015 │  21.30306187628661 │
│ 2016 │ 19.843393812866502 │
│ 2017 │ 20.725472238586505 │
│ 2018 │ 20.937579625604737 │
└──────┴────────────────────┘
↖ Progress: 363.61 million rows, 1.16 GB (477.79 million rows/s., 1.52 GB/s.)  88%↑ Progress: 369.39 million rows, 1.18 GB (485.33 million rows/s., 1.55 GB/s.)  90%
32 rows in set. Elapsed: 0.761 sec. Processed 369.39 million rows, 1.18 GB (485.19 million rows/s., 1.55 GB/s.)
-- 另一个查询版本
cdh3 :) SELECT Year, avg(DepDelay>10)
:-] FROM ontime_all
:-] GROUP BY Year
:-] ORDER BY Year;
SELECT
    Year,
    avg(DepDelay > 10)
FROM ontime_all
GROUP BY Year
ORDER BY Year ASC
↗ Progress: 0.00 rows, 0.00 B (0.00 rows/s., 0.00 B/s.) → Progress: 31.89 million rows, 191.34 MB (162.62 million rows/s., 975.72 MB/s.)  46%↘ Progress: 125.09 million rows, 750.53 MB (428.87 million rows/s., 2.57 GB/s.)  61%↓ Progress: 170.99 million rows, 1.03 GB (436%┌─Year─┬─avg(greater(DepDelay, 10))─┐
│ 1987 │         0.1991704692543066 │
│ 1988 │         0.1661709049583091 │
│ 1989 │        0.19950091248115528 │
│ 1990 │        0.16645130151570142 │
│ 1991 │        0.14721627756959182 │
│ 1992 │        0.14675431256341862 │
│ 1993 │         0.1542498463169616 │
│ 1994 │        0.16568031802021913 │
│ 1995 │        0.19221638572082064 │
│ 1996 │        0.22182805887088955 │
│ 1997 │        0.19165134687018823 │
│ 1998 │        0.19356378909882238 │
│ 1999 │        0.20087415003643347 │
│ 2000 │        0.23171671816192968 │
│ 2001 │          0.189058075197142 │
│ 2002 │        0.16237691267090706 │
│ 2003 │        0.15024550977569684 │
│ 2004 │        0.19248380268947593 │
│ 2005 │        0.20759289560703337 │
│ 2006 │        0.23155993582679846 │
│ 2007 │         0.2453487096299114 │
│ 2008 │         0.2199228614641999 │
│ 2009 │        0.19752600078911242 │
│ 2010 │        0.20358328383810712 │
│ 2011 │        0.20293064527340643 │
│ 2012 │        0.19324733358461427 │
│ 2013 │        0.22717310450049155 │
│ 2014 │         0.2399744596516966 │
│ 2015 │        0.21303061876286608 │
│ 2016 │          0.198433938128665 │
│ 2017 │        0.20725472238586506 │
│ 2018 │        0.20937579625604738 │
└──────┴────────────────────────────┘
↙ Progress: 170.99 million rows, 1.03 GB (401.45 million rows/s., 2.41 GB/s.)  83%← Progress: 184.69 million rows, 1.11 GB (433.57 million rows/s., 2.60 GB/s.)  90%
32 rows in set. Elapsed: 0.426 sec. Processed 184.69 million rows, 1.11 GB (433.36 million rows/s., 2.60 GB/s.)

--Q8. 每年更受人们喜爱的目的地
cdh3 :) SELECT DestCityName, uniqExact(OriginCityName) AS u
:-] FROM ontime_all
:-] WHERE Year >= 2000 and Year <= 2010
:-] GROUP BY DestCityName
:-] ORDER BY u DESC LIMIT 10;
SELECT
    DestCityName,
    uniqExact(OriginCityName) AS u
FROM ontime_all
WHERE (Year >= 2000) AND (Year <= 2010)
GROUP BY DestCityName
ORDER BY u DESC
LIMIT 10
→ Progress: 0.00 rows, 0.00 B (0.00 rows/s., 0.00 B/s.) ↘ Progress: 3.89 million rows, 180.29 MB (27.35 million rows/s., 1.27 GB/s.)  14%↓ Progress: 15.25 million rows, 706.59 MB (62.86 million rows/s., 2.91 GB/s.)  19%↙ Progress: 27.57 million rows, 1.28 GB (79.33 mill%┌─DestCityName──────────┬───u─┐
│ Atlanta, GA           │ 193 │
│ Chicago, IL           │ 167 │
│ Dallas/Fort Worth, TX │ 161 │
│ Cincinnati, OH        │ 138 │
│ Minneapolis, MN       │ 138 │
│ Detroit, MI           │ 130 │
│ Houston, TX           │ 129 │
│ Denver, CO            │ 127 │
│ Salt Lake City, UT    │ 119 │
│ New York, NY          │ 115 │
└───────────────────────┴─────┘
↘ Progress: 70.03 million rows, 3.24 GB (78.09 million rows/s., 3.62 GB/s.)  89%↓ Progress: 72.19 million rows, 3.34 GB (80.46 million rows/s., 3.73 GB/s.)  92%
10 rows in set. Elapsed: 0.898 sec. Processed 72.19 million rows, 3.34 GB (80.37 million rows/s., 3.72 GB/s.)

-- Q9. 每年的航班次数
cdh3 :) SELECT Year, count(*) AS c1
:-] FROM ontime_all
:-] GROUP BY Year;
SELECT
    Year,
    count(*) AS c1
FROM ontime_all
GROUP BY Year
← Progress: 0.00 rows, 0.00 B (0.00 rows/s., 0.00 B/s.) ↖ Progress: 61.58 million rows, 123.15 MB (349.79 million rows/s., 699.58 MB/s.)  90%↑ Progress: 173.75 million rows, 347.49 MB (629.11 million rows/s., 1.26 GB/s.)  85%┌─Year─┬──────c1─┐
│ 1987 │ 1311826 │
│ 1988 │ 5202096 │
│ 1989 │ 5041200 │
│ 1990 │ 5270893 │
│ 1991 │ 5076925 │
│ 1992 │ 5092157 │
│ 1993 │ 5070501 │
│ 1994 │ 5180048 │
│ 1995 │ 4888012 │
│ 1996 │ 5351983 │
│ 1997 │ 5411843 │
│ 1998 │ 5384721 │
│ 1999 │ 5527884 │
│ 2000 │ 5683047 │
│ 2001 │ 5967780 │
│ 2002 │ 5271359 │
│ 2003 │ 6488540 │
│ 2004 │ 7129270 │
│ 2005 │ 7140596 │
│ 2006 │ 7141922 │
│ 2007 │ 7455458 │
│ 2008 │ 7009726 │
│ 2009 │ 6450285 │
│ 2010 │ 6450117 │
│ 2011 │ 6085281 │
│ 2012 │ 6096762 │
│ 2013 │ 6369482 │
│ 2014 │ 5819811 │
│ 2015 │ 5819079 │
│ 2016 │ 5617658 │
│ 2017 │ 5674621 │
│ 2018 │ 7213446 │
└──────┴─────────┘
↗ Progress: 173.75 million rows, 347.49 MB (596.01 million rows/s., 1.19 GB/s.)  85%→ Progress: 184.69 million rows, 369.39 MB (633.47 million rows/s., 1.27 GB/s.)  90%
32 rows in set. Elapsed: 0.292 sec. Processed 184.69 million rows, 369.39 MB (632.92 million rows/s., 1.27 GB/s.)

-- Q10.
cdh3 :) SELECT min(Year), max(Year), Carrier, count(*) AS cnt,
:-]    sum(ArrDelayMinutes>30) AS flights_delayed,
:-]    round(sum(ArrDelayMinutes>30)/count(*),2) AS rate
:-] FROM ontime_all
:-] WHERE
:-]    DayOfWeek NOT IN (6,7) AND OriginState NOT IN ('AK', 'HI', 'PR', 'VI')
:-]    AND DestState NOT IN ('AK', 'HI', 'PR', 'VI')
:-]    AND FlightDate < '2010-01-01'
:-] GROUP by Carrier
:-] HAVING cnt>100000 and max(Year)>1990
:-] ORDER by rate DESC
:-] LIMIT 1000;
SELECT
    min(Year),
    max(Year),
    Carrier,
    count(*) AS cnt,
    sum(ArrDelayMinutes > 30) AS flights_delayed,
    round(sum(ArrDelayMinutes > 30) / count(*), 2) AS rate
FROM ontime_all
WHERE (DayOfWeek NOT IN (6, 7)) AND (OriginState NOT IN ('AK', 'HI', 'PR', 'VI')) AND (DestState NOT IN ('AK', 'HI', 'PR', 'VI')) AND (FlightDate < '2010-01-01')
GROUP BY Carrier
HAVING (cnt > 100000) AND (max(Year) > 1990)
ORDER BY rate DESC
LIMIT 1000
↘ Progress: 0.00 rows, 0.00 B (0.00 rows/s., 0.00 B/s.) ↓ Progress: 1.51 million rows, 17.60 MB (6.85 million rows/s., 79.64 MB/s.)  3%↙ Progress: 2.02 million rows, 23.78 MB (6.30 million rows/s., 74.12 MB/s.)  4%← Progress: 5.62 million rows, 65.97 MB (13.35 million r%┌─min(Year)─┬─max(Year)─┬─Carrier─┬──────cnt─┬─flights_delayed─┬─rate─┐
│      2003 │      2009 │ EV      │  1454777 │          237698 │ 0.16 │
│      2003 │      2009 │ B6      │   683874 │          103677 │ 0.15 │
│      2003 │      2009 │ FL      │  1082489 │          158748 │ 0.15 │
│      2006 │      2009 │ YV      │   740608 │          110389 │ 0.15 │
│      2006 │      2009 │ XE      │  1016010 │          152431 │ 0.15 │
│      2003 │      2005 │ DH      │   501056 │           69833 │ 0.14 │
│      2001 │      2009 │ MQ      │  3238137 │          448037 │ 0.14 │
│      2003 │      2006 │ RU      │  1007248 │          126733 │ 0.13 │
│      2004 │      2009 │ OH      │  1195868 │          160071 │ 0.13 │
│      1987 │      2009 │ UA      │  9655762 │         1203503 │ 0.12 │
│      2003 │      2006 │ TZ      │   136735 │           16496 │ 0.12 │
│      1987 │      2009 │ CO      │  6092575 │          681750 │ 0.11 │
│      1987 │      2009 │ AA      │ 10678564 │         1189672 │ 0.11 │
│      1987 │      2001 │ TW      │  2693587 │          283362 │ 0.11 │
│      1987 │      2009 │ AS      │  1511966 │          147972 │  0.1 │
│      1987 │      2009 │ NW      │  7648247 │          729920 │  0.1 │
│      1987 │      2009 │ DL      │ 11948844 │         1163924 │  0.1 │
│      1988 │      2009 │ US      │ 10229664 │          986338 │  0.1 │
│      2007 │      2009 │ 9E      │   577244 │           59440 │  0.1 │
│      2003 │      2009 │ OO      │  2654259 │          257069 │  0.1 │
│      1987 │      1991 │ PA      │   218938 │           20497 │ 0.09 │
│      2005 │      2009 │ F9      │   307569 │           28679 │ 0.09 │
│      1987 │      2005 │ HP      │  2628455 │          236633 │ 0.09 │
│      1987 │      2009 │ WN      │ 12726332 │         1108072 │ 0.09 │
└───────────┴───────────┴─────────┴──────────┴─────────────────┴──────┘
← Progress: 129.28 million rows, 1.52 GB (21.70 million rows/s., 255.84 MB/s.) ███████████████████████████████████████████████████████████████████████████████████████████████████████████████████████████████████████████████████████████████████████████▊                 90%
24 rows in set. Elapsed: 5.960 sec. Processed 129.55 million rows, 1.53 GB (21.74 million rows/s., 256.33 MB/s.)
   
SELECT min(Year), max(Year), Carrier, count(*) AS cnt,
   sum(ArrDelayMinutes>30) AS flights_delayed,
   round(sum(ArrDelayMinutes>30)/count(*),2) AS rate
FROM ontime_all
WHERE
   DayOfWeek NOT IN (6,7) AND OriginState NOT IN ('AK', 'HI', 'PR', 'VI')
   AND DestState NOT IN ('AK', 'HI', 'PR', 'VI')
   AND FlightDate < '2010-01-01'
GROUP by Carrier
HAVING cnt>100000 and max(Year)>1990
ORDER by rate DESC
LIMIT 1000;

```


## 8.5 和单节点查询对比
```sql
-- Q0
cdh3.ygbx.com :) SELECT avg(c1) FROM (
:-]     SELECT Year, Month, count(*) AS c1
:-]     FROM ontime
:-]     GROUP BY Year, Month
:-] );
SELECT avg(c1)
FROM
(
    SELECT
        Year,
        Month,
        count(*) AS c1
    FROM ontime
    GROUP BY
        Year,
        Month
)
← Progress: 0.00 rows, 0.00 B (0.00 rows/s., 0.00 B/s.) ↖ Progress: 34.48 million rows, 103.43 MB (233.81 million rows/s., 701.44 MB/s.)  18%↑ Progress: 59.65 million rows, 178.94 MB (241.84 million rows/s., 725.51 MB/s.)  31%↗ Progress: 87.55 million rows, 262.66 MB (2%┌────────────avg(c1)─┐
│ 493835.10427807487 │
└────────────────────┘
↙ Progress: 157.20 million rows, 471.61 MB (217.07 million rows/s., 651.22 MB/s.)  83%← Progress: 184.69 million rows, 554.08 MB (254.93 million rows/s., 764.79 MB/s.)  98%
1 rows in set. Elapsed: 0.725 sec. Processed 184.69 million rows, 554.08 MB (254.65 million rows/s., 763.96 MB/s.)

-- Q1. 查询从2000年到2008年每天的航班数
cdh3 :) SELECT DayOfWeek, count(*) AS c
:-] FROM ontime
:-] WHERE Year>=2000 AND Year<=2008
:-] GROUP BY DayOfWeek
:-] ORDER BY c DESC;
SELECT
    DayOfWeek,
    count(*) AS c
FROM ontime
WHERE (Year >= 2000) AND (Year <= 2008)
GROUP BY DayOfWeek
ORDER BY c DESC
↗ Progress: 0.00 rows, 0.00 B (0.00 rows/s., 0.00 B/s.) → Progress: 45.69 million rows, 137.06 MB (401.53 million rows/s., 1.20 GB/s.)  75%┌─DayOfWeek─┬───────c─┐
│         5 │ 8732422 │
│         1 │ 8730614 │
│         4 │ 8710843 │
│         3 │ 8685626 │
│         2 │ 8639632 │
│         7 │ 8274367 │
│         6 │ 7514194 │
└───────────┴─────────┘
↘ Progress: 45.69 million rows, 137.06 MB (293.00 million rows/s., 878.99 MB/s.)  75%↓ Progress: 59.29 million rows, 177.86 MB (380.13 million rows/s., 1.14 GB/s.)  98%
7 rows in set. Elapsed: 0.156 sec. Processed 59.29 million rows, 177.86 MB (379.60 million rows/s., 1.14 GB/s.)

-- Q2. 查询从2000年到2008年每周延误超过10分钟的航班数。
cdh3 :) SELECT DayOfWeek, count(*) AS c
:-] FROM ontime
:-] WHERE DepDelay>10 AND Year>=2000 AND Year<=2008
:-] GROUP BY DayOfWeek
:-] ORDER BY c DESC;
SELECT
    DayOfWeek,
    count(*) AS c
FROM ontime
WHERE (DepDelay > 10) AND (Year >= 2000) AND (Year <= 2008)
GROUP BY DayOfWeek
ORDER BY c DESC
↘ Progress: 0.00 rows, 0.00 B (0.00 rows/s., 0.00 B/s.) ↓ Progress: 24.71 million rows, 114.22 MB (205.39 million rows/s., 949.21 MB/s.)  40%↙ Progress: 52.06 million rows, 240.27 MB (238.90 million rows/s., 1.10 GB/s.)  86%┌─DayOfWeek─┬───────c─┐
│         5 │ 2088300 │
│         4 │ 1918325 │
│         1 │ 1795120 │
│         7 │ 1782292 │
│         3 │ 1640798 │
│         2 │ 1538291 │
│         6 │ 1391984 │
└───────────┴─────────┘
← Progress: 52.06 million rows, 240.27 MB (211.66 million rows/s., 976.96 MB/s.)  86%↖ Progress: 59.29 million rows, 273.62 MB (241.05 million rows/s., 1.11 GB/s.)  98%
7 rows in set. Elapsed: 0.247 sec. Processed 59.29 million rows, 273.62 MB (240.27 million rows/s., 1.11 GB/s.)

-- Q3. 查询2000年到2008年每个机场延误超过10分钟以上的次数
cdh3 :) SELECT Origin, count(*) AS c
:-] FROM ontime
:-] WHERE DepDelay>10 AND Year>=2000 AND Year<=2008
:-] GROUP BY Origin
:-] ORDER BY c DESC
:-] LIMIT 10;
SELECT
    Origin,
    count(*) AS c
FROM ontime
WHERE (DepDelay > 10) AND (Year >= 2000) AND (Year <= 2008)
GROUP BY Origin
ORDER BY c DESC
LIMIT 10
↙ Progress: 0.00 rows, 0.00 B (0.00 rows/s., 0.00 B/s.) ← Progress: 9.24 million rows, 50.25 MB (77.45 million rows/s., 421.14 MB/s.)  15%↖ Progress: 20.66 million rows, 113.08 MB (91.33 million rows/s., 499.83 MB/s.)  34%↑ Progress: 33.70 million rows, 183.47 MB (104.3%┌─Origin─┬──────c─┐
│ ORD    │ 846692 │
│ ATL    │ 822955 │
│ DFW    │ 601318 │
│ LAX    │ 391247 │
│ PHX    │ 391191 │
│ LAS    │ 351713 │
│ DEN    │ 345108 │
│ EWR    │ 292916 │
│ DTW    │ 289233 │
│ IAH    │ 283861 │
└────────┴────────┘
→ Progress: 49.73 million rows, 270.73 MB (100.17 million rows/s., 545.29 MB/s.)  82%↘ Progress: 59.29 million rows, 322.24 MB (119.40 million rows/s., 648.97 MB/s.)  98%
10 rows in set. Elapsed: 0.497 sec. Processed 59.29 million rows, 322.24 MB (119.39 million rows/s., 648.91 MB/s.)

-- Q4. 查询2007年各航空公司延误超过10分钟以上的次数
cdh3 :) SELECT Carrier, count(*)
:-] FROM ontime
:-] WHERE DepDelay>10 AND Year=2007
:-] GROUP BY Carrier
:-] ORDER BY count(*) DESC;
SELECT
    Carrier,
    count(*)
FROM ontime
WHERE (DepDelay > 10) AND (Year = 2007)
GROUP BY Carrier
ORDER BY count(*) DESC
↓ Progress: 0.00 rows, 0.00 B (0.00 rows/s., 0.00 B/s.) ┌─Carrier─┬─count()─┐
│ WN      │  296293 │
│ AA      │  176203 │
│ MQ      │  145630 │
│ US      │  135987 │
│ UA      │  128174 │
│ OO      │  127426 │
│ EV      │  101796 │
│ XE      │   99915 │
│ DL      │   93675 │
│ NW      │   90429 │
│ CO      │   76662 │
│ YV      │   67905 │
│ FL      │   59460 │
│ OH      │   59034 │
│ B6      │   50740 │
│ 9E      │   46948 │
│ AS      │   42830 │
│ F9      │   23035 │
│ AQ      │    4299 │
│ HA      │    2746 │
└─────────┴─────────┘
↙ Progress: 0.00 rows, 0.00 B (0.00 rows/s., 0.00 B/s.) ← Progress: 7.46 million rows, 37.14 MB (165.52 million rows/s., 824.53 MB/s.)  98%
20 rows in set. Elapsed: 0.045 sec. Processed 7.46 million rows, 37.14 MB (165.35 million rows/s., 823.69 MB/s.)

-- Q5. 查询2007年各航空公司延误超过10分钟以上的百分比
cdh3 :) SELECT Carrier, c, c2, c*100/c2 as c3 FROM (
:-]     SELECT Carrier, count(*) AS c FROM ontime
:-]     WHERE DepDelay>10
:-]         AND Year=2007
:-]     GROUP BY Carrier
:-] )ANY INNER JOIN(
:-]     SELECT Carrier, count(*) AS c2
:-]     FROM ontime
:-]     WHERE Year=2007
:-]     GROUP BY Carrier
:-] ) USING Carrier
:-] ORDER BY c3 DESC;
SELECT
    Carrier,
    c,
    c2,
    (c * 100) / c2 AS c3
FROM
(
    SELECT
        Carrier,
        count(*) AS c
    FROM ontime
    WHERE (DepDelay > 10) AND (Year = 2007)
    GROUP BY Carrier
)
ANY INNER JOIN
(
    SELECT
        Carrier,
        count(*) AS c2
    FROM ontime
    WHERE Year = 2007
    GROUP BY Carrier
) USING (Carrier)
ORDER BY c3 DESC
→ Progress: 0.00 rows, 0.00 B (0.00 rows/s., 0.00 B/s.) ┌─Carrier─┬──────c─┬──────c2─┬─────────────────c3─┐
│ EV      │ 101796 │  286234 │ 35.563909249075934 │
│ US      │ 135987 │  485447 │ 28.012738774778708 │
│ AA      │ 176203 │  633857 │ 27.798541311368336 │
│ MQ      │ 145630 │  540494 │ 26.943869867195566 │
│ AS      │  42830 │  160185 │  26.73783437899928 │
│ B6      │  50740 │  191450 │ 26.503003395142336 │
│ UA      │ 128174 │  490002 │  26.15785241692891 │
│ WN      │ 296293 │ 1168871 │ 25.348648396615197 │
│ OH      │  59034 │  236032 │ 25.011015455531453 │
│ CO      │  76662 │  323151 │  23.72327487768876 │
│ F9      │  23035 │   97760 │ 23.562806873977088 │
│ YV      │  67905 │  294362 │ 23.068534661403305 │
│ XE      │  99915 │  434773 │  22.98095787916913 │
│ FL      │  59460 │  263159 │  22.59470510223857 │
│ NW      │  90429 │  414526 │  21.81503693375084 │
│ OO      │ 127426 │  597880 │  21.31297250284338 │
│ DL      │  93675 │  475889 │  19.68421207466447 │
│ 9E      │  46948 │  258851 │  18.13707499681284 │
│ AQ      │   4299 │   46360 │  9.273080241587575 │
│ HA      │   2746 │   56175 │  4.888295505117935 │
└─────────┴────────┴─────────┴────────────────────┘
↘ Progress: 0.00 rows, 0.00 B (0.00 rows/s., 0.00 B/s.) ↓ Progress: 14.91 million rows, 66.96 MB (161.78 million rows/s., 726.53 MB/s.)  98%
20 rows in set. Elapsed: 0.092 sec. Processed 14.91 million rows, 66.96 MB (161.70 million rows/s., 726.15 MB/s.)
-- 另一个查询版本
cdh3.ygbx.com :) SELECT Carrier, avg(DepDelay>10)*100 AS c3
:-] FROM ontime
:-] WHERE Year=2007
:-] GROUP BY Carrier
:-] ORDER BY Carrier;
SELECT
    Carrier,
    avg(DepDelay > 10) * 100 AS c3
FROM ontime
WHERE Year = 2007
GROUP BY Carrier
ORDER BY Carrier ASC
↗ Progress: 0.00 rows, 0.00 B (0.00 rows/s., 0.00 B/s.) ┌─Carrier─┬─────────────────c3─┐
│ 9E      │  18.13707499681284 │
│ AA      │ 27.798541311368336 │
│ AQ      │  9.273080241587575 │
│ AS      │  26.73783437899928 │
│ B6      │ 26.503003395142333 │
│ CO      │  23.72327487768876 │
│ DL      │  19.68421207466447 │
│ EV      │ 35.563909249075934 │
│ F9      │ 23.562806873977088 │
│ FL      │  22.59470510223857 │
│ HA      │  4.888295505117935 │
│ MQ      │ 26.943869867195563 │
│ NW      │  21.81503693375084 │
│ OH      │ 25.011015455531453 │
│ OO      │  21.31297250284338 │
│ UA      │  26.15785241692891 │
│ US      │  28.01273877477871 │
│ WN      │ 25.348648396615197 │
│ XE      │ 22.980957879169132 │
│ YV      │ 23.068534661403305 │
└─────────┴────────────────────┘
→ Progress: 0.00 rows, 0.00 B (0.00 rows/s., 0.00 B/s.) ↘ Progress: 7.46 million rows, 59.64 MB (166.82 million rows/s., 1.33 GB/s.)  98%
20 rows in set. Elapsed: 0.045 sec. Processed 7.46 million rows, 59.64 MB (165.69 million rows/s., 1.33 GB/s.)

--Q6. 同上一个查询一致,只是查询范围扩大到2000年到2008年
cdh3.ygbx.com :) SELECT Carrier, c, c2, c*100/c2 as c3 FROM (
:-]     SELECT Carrier, count(*) AS c
:-]     FROM ontime
:-]     WHERE DepDelay>10
:-]         AND Year>=2000 AND Year<=2008
:-]     GROUP BY Carrier
:-] )ANY INNER JOIN(
:-]     SELECT Carrier, count(*) AS c2
:-]     FROM ontime
:-]     WHERE Year>=2000 AND Year<=2008
:-]     GROUP BY Carrier
:-] ) USING Carrier
:-] ORDER BY c3 DESC;
SELECT
    Carrier,
    c,
    c2,
    (c * 100) / c2 AS c3
FROM
(
    SELECT
        Carrier,
        count(*) AS c
    FROM ontime
    WHERE (DepDelay > 10) AND (Year >= 2000) AND (Year <= 2008)
    GROUP BY Carrier
)
ANY INNER JOIN
(
    SELECT
        Carrier,
        count(*) AS c2
    FROM ontime
    WHERE (Year >= 2000) AND (Year <= 2008)
    GROUP BY Carrier
) USING (Carrier)
ORDER BY c3 DESC
↖ Progress: 0.00 rows, 0.00 B (0.00 rows/s., 0.00 B/s.) ↑ Progress: 24.72 million rows, 98.89 MB (199.86 million rows/s., 799.46 MB/s.)  40%↗ Progress: 52.13 million rows, 208.54 MB (232.89 million rows/s., 931.58 MB/s.)  86%→ Progress: 74.93 million rows, 312.83 MB (23%┌─Carrier─┬───────c─┬──────c2─┬─────────────────c3─┐
│ EV      │  461050 │ 1697172 │ 27.165779308166762 │
│ AS      │  354145 │ 1427189 │ 24.814162665211125 │
│ B6      │  197249 │  811341 │ 24.311479390293353 │
│ FL      │  298916 │ 1265138 │ 23.627145813342104 │
│ WN      │ 2165483 │ 9280539 │  23.33359086147906 │
│ YV      │  198787 │  854056 │ 23.275640004870876 │
│ XE      │  233488 │ 1036015 │ 22.537125427720643 │
│ MQ      │  876799 │ 3954895 │ 22.169969114224273 │
│ UA      │ 1096646 │ 5094635 │ 21.525506734044736 │
│ F9      │   72150 │  336958 │ 21.412164127279958 │
│ DH      │  147041 │  693047 │ 21.216598585665906 │
│ OH      │  301681 │ 1466421 │ 20.572605002246966 │
│ HP      │  245293 │ 1208561 │ 20.296286244550338 │
│ AA      │ 1276555 │ 6318386 │ 20.203814708376473 │
│ US      │  909154 │ 4650400 │ 19.550017202821262 │
│ TW      │   94808 │  511852 │  18.52254167220212 │
│ OO      │  556247 │ 3090849 │  17.99657634520483 │
│ CO      │  522219 │ 2925290 │ 17.851871096540854 │
│ DL      │ 1050448 │ 5912486 │  17.76660443678006 │
│ 9E      │   89391 │  521059 │ 17.155638804818647 │
│ NW      │  725076 │ 4280049 │ 16.940834088581695 │
│ RU      │  216279 │ 1314294 │ 16.455907125802902 │
│ TZ      │   32998 │  208420 │ 15.832453699261107 │
│ AQ      │   17239 │  154381 │ 11.166529559984713 │
│ HA      │   15968 │  274265 │  5.822106356990502 │
└─────────┴─────────┴─────────┴────────────────────┘
↓ Progress: 102.58 million rows, 445.83 MB (209.32 million rows/s., 909.75 MB/s.)  85%↙ Progress: 118.58 million rows, 522.94 MB (241.78 million rows/s., 1.07 GB/s.)  98%
25 rows in set. Elapsed: 0.490 sec. Processed 118.58 million rows, 522.94 MB (241.75 million rows/s., 1.07 GB/s.)
-- 另一个查询版本
cdh3 :) SELECT Carrier, avg(DepDelay>10)*100 AS c3
:-] FROM ontime
:-] WHERE Year>=2000 AND Year<=2008
:-] GROUP BY Carrier
:-] ORDER BY Carrier;
SELECT
    Carrier,
    avg(DepDelay > 10) * 100 AS c3
FROM ontime
WHERE (Year >= 2000) AND (Year <= 2008)
GROUP BY Carrier
ORDER BY Carrier ASC
↗ Progress: 0.00 rows, 0.00 B (0.00 rows/s., 0.00 B/s.) → Progress: 14.71 million rows, 117.71 MB (127.95 million rows/s., 1.02 GB/s.)  24%↘ Progress: 35.97 million rows, 287.79 MB (163.32 million rows/s., 1.31 GB/s.)  59%↓ Progress: 59.29 million rows, 474.31 MB (188.0%┌─Carrier─┬─────────────────c3─┐
│ 9E      │ 17.155638804818647 │
│ AA      │ 20.203814708376473 │
│ AQ      │ 11.166529559984713 │
│ AS      │ 24.814162665211125 │
│ B6      │ 24.311479390293353 │
│ CO      │ 17.851871096540854 │
│ DH      │ 21.216598585665906 │
│ DL      │  17.76660443678006 │
│ EV      │  27.16577930816676 │
│ F9      │ 21.412164127279958 │
│ FL      │   23.6271458133421 │
│ HA      │  5.822106356990502 │
│ HP      │ 20.296286244550338 │
│ MQ      │ 22.169969114224273 │
│ NW      │ 16.940834088581695 │
│ OH      │  20.57260500224697 │
│ OO      │  17.99657634520483 │
│ RU      │ 16.455907125802902 │
│ TW      │  18.52254167220212 │
│ TZ      │ 15.832453699261107 │
│ UA      │ 21.525506734044736 │
│ US      │ 19.550017202821262 │
│ WN      │  23.33359086147906 │
│ XE      │ 22.537125427720643 │
│ YV      │ 23.275640004870876 │
└─────────┴────────────────────┘
↙ Progress: 59.29 million rows, 474.31 MB (187.56 million rows/s., 1.50 GB/s.)  98%
25 rows in set. Elapsed: 0.316 sec. Processed 59.29 million rows, 474.31 MB (187.45 million rows/s., 1.50 GB/s.)

-- Q7. 每年航班延误超过10分钟的百分比
cdh3 :) SELECT Year, c1/c2 FROM (
:-]     select Year, count(*)*100 as c1
:-]     from ontime
:-]     WHERE DepDelay>10
:-]     GROUP BY Year
:-] )ANY INNER JOIN(
:-]     select Year, count(*) as c2
:-]     from ontime
:-]     GROUP BY Year
:-] ) USING (Year)
:-] ORDER BY Year;
SELECT
    Year,
    c1 / c2
FROM
(
    SELECT
        Year,
        count(*) * 100 AS c1
    FROM ontime
    WHERE DepDelay > 10
    GROUP BY Year
)
ANY INNER JOIN
(
    SELECT
        Year,
        count(*) AS c2
    FROM ontime
    GROUP BY Year
) USING (Year)
ORDER BY Year ASC
→ Progress: 0.00 rows, 0.00 B (0.00 rows/s., 0.00 B/s.) ↘ Progress: 83.57 million rows, 167.13 MB (627.23 million rows/s., 1.25 GB/s.)  44%↓ Progress: 132.54 million rows, 265.08 MB (567.96 million rows/s., 1.14 GB/s.)  70%↙ Progress: 173.49 million rows, 346.99 MB (519%┌─Year─┬─────divide(c1, c2)─┐
│ 1987 │  19.91704692543066 │
│ 1988 │  16.61709049583091 │
│ 1989 │ 19.950091248115527 │
│ 1990 │ 16.645130151570143 │
│ 1991 │ 14.721627756959183 │
│ 1992 │ 14.675431256341861 │
│ 1993 │ 15.424984631696157 │
│ 1994 │ 16.568031802021913 │
│ 1995 │ 19.221638572082064 │
│ 1996 │ 22.182805887088954 │
│ 1997 │  19.16513468701882 │
│ 1998 │  19.35637890988224 │
│ 1999 │ 20.087415003643347 │
│ 2000 │  23.17167181619297 │
│ 2001 │ 18.905807519714198 │
│ 2002 │ 16.237691267090707 │
│ 2003 │ 15.024550977569685 │
│ 2004 │ 19.248380268947592 │
│ 2005 │ 20.759289560703337 │
│ 2006 │ 23.155993582679844 │
│ 2007 │  24.53487096299114 │
│ 2008 │  21.99228614641999 │
│ 2009 │ 19.752600078911243 │
│ 2010 │  20.35832838381071 │
│ 2011 │ 20.293064527340643 │
│ 2012 │ 19.324733358461426 │
│ 2013 │ 22.717310450049155 │
│ 2014 │  23.99744596516966 │
│ 2015 │  21.30306187628661 │
│ 2016 │ 19.843393812866502 │
│ 2017 │ 20.725472238586505 │
│ 2018 │ 20.937579625604737 │
└──────┴────────────────────┘
↘ Progress: 347.39 million rows, 1.08 GB (377.66 million rows/s., 1.18 GB/s.)  92%↓ Progress: 369.39 million rows, 1.18 GB (401.56 million rows/s., 1.28 GB/s.)  98%
32 rows in set. Elapsed: 0.921 sec. Processed 369.39 million rows, 1.18 GB (401.16 million rows/s., 1.28 GB/s.)

-- 另一个查询版本
cdh3 :) SELECT Year, avg(DepDelay>10)
:-] FROM ontime
:-] GROUP BY Year
:-] ORDER BY Year;
SELECT
    Year,
    avg(DepDelay > 10)
FROM ontime
GROUP BY Year
ORDER BY Year ASC
↙ Progress: 0.00 rows, 0.00 B (0.00 rows/s., 0.00 B/s.) ← Progress: 42.55 million rows, 255.29 MB (346.27 million rows/s., 2.08 GB/s.)  22%↖ Progress: 92.43 million rows, 554.55 MB (404.85 million rows/s., 2.43 GB/s.)  49%↑ Progress: 147.11 million rows, 882.66 MB (438.%┌─Year─┬─avg(greater(DepDelay, 10))─┐
│ 1987 │         0.1991704692543066 │
│ 1988 │         0.1661709049583091 │
│ 1989 │        0.19950091248115528 │
│ 1990 │        0.16645130151570142 │
│ 1991 │        0.14721627756959182 │
│ 1992 │        0.14675431256341862 │
│ 1993 │         0.1542498463169616 │
│ 1994 │        0.16568031802021913 │
│ 1995 │        0.19221638572082064 │
│ 1996 │        0.22182805887088955 │
│ 1997 │        0.19165134687018823 │
│ 1998 │        0.19356378909882238 │
│ 1999 │        0.20087415003643347 │
│ 2000 │        0.23171671816192968 │
│ 2001 │          0.189058075197142 │
│ 2002 │        0.16237691267090706 │
│ 2003 │        0.15024550977569684 │
│ 2004 │        0.19248380268947593 │
│ 2005 │        0.20759289560703337 │
│ 2006 │        0.23155993582679846 │
│ 2007 │         0.2453487096299114 │
│ 2008 │         0.2199228614641999 │
│ 2009 │        0.19752600078911242 │
│ 2010 │        0.20358328383810712 │
│ 2011 │        0.20293064527340643 │
│ 2012 │        0.19324733358461427 │
│ 2013 │        0.22717310450049155 │
│ 2014 │         0.2399744596516966 │
│ 2015 │        0.21303061876286608 │
│ 2016 │          0.198433938128665 │
│ 2017 │        0.20725472238586506 │
│ 2018 │        0.20937579625604738 │
└──────┴────────────────────────────┘
↗ Progress: 147.11 million rows, 882.66 MB (354.18 million rows/s., 2.13 GB/s.)  78%→ Progress: 184.69 million rows, 1.11 GB (444.64 million rows/s., 2.67 GB/s.)  98%
32 rows in set. Elapsed: 0.415 sec. Processed 184.69 million rows, 1.11 GB (444.56 million rows/s., 2.67 GB/s.)

--Q8. 每年更受人们喜爱的目的地
cdh3 :) SELECT DestCityName, uniqExact(OriginCityName) AS u
:-] FROM ontime
:-] WHERE Year >= 2000 and Year <= 2010
:-] GROUP BY DestCityName
:-] ORDER BY u DESC LIMIT 10;
SELECT
    DestCityName,
    uniqExact(OriginCityName) AS u
FROM ontime
WHERE (Year >= 2000) AND (Year <= 2010)
GROUP BY DestCityName
ORDER BY u DESC
LIMIT 10
↙ Progress: 0.00 rows, 0.00 B (0.00 rows/s., 0.00 B/s.) ← Progress: 3.65 million rows, 169.04 MB (30.51 million rows/s., 1.41 GB/s.)  4%↖ Progress: 7.80 million rows, 361.12 MB (35.37 million rows/s., 1.64 GB/s.)  10%↑ Progress: 11.85 million rows, 548.92 MB (36.58 mill%┌─DestCityName──────────┬───u─┐
│ Atlanta, GA           │ 193 │
│ Chicago, IL           │ 167 │
│ Dallas/Fort Worth, TX │ 161 │
│ Minneapolis, MN       │ 138 │
│ Cincinnati, OH        │ 138 │
│ Detroit, MI           │ 130 │
│ Houston, TX           │ 129 │
│ Denver, CO            │ 127 │
│ Salt Lake City, UT    │ 119 │
│ New York, NY          │ 115 │
└───────────────────────┴─────┘
↓ Progress: 69.28 million rows, 3.21 GB (45.69 million rows/s., 2.12 GB/s.) █████████████████████████████████████████████████████████████████████████████████████████████████████████████████████████████████████████████████████████████████████████████████████▊          94%
10 rows in set. Elapsed: 1.516 sec. Processed 72.19 million rows, 3.34 GB (47.61 million rows/s., 2.20 GB/s.)

-- Q9.
cdh3 :) SELECT Year, count(*) AS c1
:-] FROM ontime
:-] GROUP BY Year;
SELECT
    Year,
    count(*) AS c1
FROM ontime
GROUP BY Year
↘ Progress: 0.00 rows, 0.00 B (0.00 rows/s., 0.00 B/s.) ↓ Progress: 50.84 million rows, 101.69 MB (423.25 million rows/s., 846.49 MB/s.)  27%↙ Progress: 77.95 million rows, 155.91 MB (353.78 million rows/s., 707.56 MB/s.)  41%← Progress: 107.71 million rows, 215.42 MB (%┌─Year─┬──────c1─┐
│ 1987 │ 1311826 │
│ 1988 │ 5202096 │
│ 1989 │ 5041200 │
│ 1990 │ 5270893 │
│ 1991 │ 5076925 │
│ 1992 │ 5092157 │
│ 1993 │ 5070501 │
│ 1994 │ 5180048 │
│ 1995 │ 4888012 │
│ 1996 │ 5351983 │
│ 1997 │ 5411843 │
│ 1998 │ 5384721 │
│ 1999 │ 5527884 │
│ 2000 │ 5683047 │
│ 2001 │ 5967780 │
│ 2002 │ 5271359 │
│ 2003 │ 6488540 │
│ 2004 │ 7129270 │
│ 2005 │ 7140596 │
│ 2006 │ 7141922 │
│ 2007 │ 7455458 │
│ 2008 │ 7009726 │
│ 2009 │ 6450285 │
│ 2010 │ 6450117 │
│ 2011 │ 6085281 │
│ 2012 │ 6096762 │
│ 2013 │ 6369482 │
│ 2014 │ 5819811 │
│ 2015 │ 5819079 │
│ 2016 │ 5617658 │
│ 2017 │ 5674621 │
│ 2018 │ 7213446 │
└──────┴─────────┘
↗ Progress: 173.43 million rows, 346.85 MB (305.18 million rows/s., 610.35 MB/s.)  92%→ Progress: 184.69 million rows, 369.39 MB (324.99 million rows/s., 649.98 MB/s.)  98%
32 rows in set. Elapsed: 0.569 sec. Processed 184.69 million rows, 369.39 MB (324.37 million rows/s., 648.74 MB/s.)

-- Q10.
cdh3.ygbx.com :) SELECT min(Year), max(Year), Carrier, count(*) AS cnt,
:-]    sum(ArrDelayMinutes>30) AS flights_delayed,
:-]    round(sum(ArrDelayMinutes>30)/count(*),2) AS rate
:-] FROM ontime
:-] WHERE
:-]    DayOfWeek NOT IN (6,7) AND OriginState NOT IN ('AK', 'HI', 'PR', 'VI')
:-]    AND DestState NOT IN ('AK', 'HI', 'PR', 'VI')
:-]    AND FlightDate < '2010-01-01'
:-] GROUP by Carrier
:-] HAVING cnt>100000 and max(Year)>1990
:-] ORDER by rate DESC
:-] LIMIT 1000;
SELECT
    min(Year),
    max(Year),
    Carrier,
    count(*) AS cnt,
    sum(ArrDelayMinutes > 30) AS flights_delayed,
    round(sum(ArrDelayMinutes > 30) / count(*), 2) AS rate
FROM ontime
WHERE (DayOfWeek NOT IN (6, 7)) AND (OriginState NOT IN ('AK', 'HI', 'PR', 'VI')) AND (DestState NOT IN ('AK', 'HI', 'PR', 'VI')) AND (FlightDate < '2010-01-01')
GROUP BY Carrier
HAVING (cnt > 100000) AND (max(Year) > 1990)
ORDER BY rate DESC
LIMIT 1000
↘ Progress: 0.00 rows, 0.00 B (0.00 rows/s., 0.00 B/s.) ↓ Progress: 7.98 million rows, 93.16 MB (56.94 million rows/s., 665.05 MB/s.)  6%↙ Progress: 17.02 million rows, 200.29 MB (70.85 million rows/s., 833.62 MB/s.)  12%← Progress: 26.92 million rows, 316.72 MB (77.50 %┌─min(Year)─┬─max(Year)─┬─Carrier─┬──────cnt─┬─flights_delayed─┬─rate─┐
│      2003 │      2009 │ EV      │  1454777 │          237698 │ 0.16 │
│      2003 │      2009 │ B6      │   683874 │          103677 │ 0.15 │
│      2003 │      2009 │ FL      │  1082489 │          158748 │ 0.15 │
│      2006 │      2009 │ YV      │   740608 │          110389 │ 0.15 │
│      2006 │      2009 │ XE      │  1016010 │          152431 │ 0.15 │
│      2003 │      2005 │ DH      │   501056 │           69833 │ 0.14 │
│      2001 │      2009 │ MQ      │  3238137 │          448037 │ 0.14 │
│      2003 │      2006 │ RU      │  1007248 │          126733 │ 0.13 │
│      2004 │      2009 │ OH      │  1195868 │          160071 │ 0.13 │
│      1987 │      2009 │ UA      │  9655762 │         1203503 │ 0.12 │
│      2003 │      2006 │ TZ      │   136735 │           16496 │ 0.12 │
│      1987 │      2009 │ CO      │  6092575 │          681750 │ 0.11 │
│      1987 │      2009 │ AA      │ 10678564 │         1189672 │ 0.11 │
│      1987 │      2001 │ TW      │  2693587 │          283362 │ 0.11 │
│      1987 │      2009 │ AS      │  1511966 │          147972 │  0.1 │
│      1987 │      2009 │ NW      │  7648247 │          729920 │  0.1 │
│      1987 │      2009 │ DL      │ 11948844 │         1163924 │  0.1 │
│      1988 │      2009 │ US      │ 10229664 │          986338 │  0.1 │
│      2007 │      2009 │ 9E      │   577244 │           59440 │  0.1 │
│      2003 │      2009 │ OO      │  2654259 │          257069 │  0.1 │
│      1987 │      1991 │ PA      │   218938 │           20497 │ 0.09 │
│      2005 │      2009 │ F9      │   307569 │           28679 │ 0.09 │
│      1987 │      2005 │ HP      │  2628455 │          236633 │ 0.09 │
│      1987 │      2009 │ WN      │ 12726332 │         1108072 │ 0.09 │
└───────────┴───────────┴─────────┴──────────┴─────────────────┴──────┘
↘ Progress: 127.86 million rows, 1.51 GB (80.42 million rows/s., 948.45 MB/s.) ████████████████████████████████████████████████████████████████████████████████████████████████████████████████████████████████████████████████████████████████████████████████████████     96%
24 rows in set. Elapsed: 1.591 sec. Processed 129.55 million rows, 1.53 GB (81.42 million rows/s., 960.07 MB/s.)


cdh3 :) select Carrier, count() as c, round(quantileTDigest(0.99)(DepDelay), 2) as q
:-] from ontime
:-] group by Carrier order by q desc limit 5;
SELECT
    Carrier,
    count() AS c,
    round(quantileTDigest(0.99)(DepDelay), 2) AS q
FROM ontime
GROUP BY Carrier
ORDER BY q DESC
LIMIT 5
← Progress: 0.00 rows, 0.00 B (0.00 rows/s., 0.00 B/s.) ↖ Progress: 9.53 million rows, 57.20 MB (74.43 million rows/s., 446.62 MB/s.)  5%↑ Progress: 18.49 million rows, 110.93 MB (80.13 million rows/s., 480.81 MB/s.)  9%↗ Progress: 28.17 million rows, 169.00 MB (84.29 m%┌─Carrier─┬───────c─┬──────q─┐
│ G4      │   96221 │ 260.16 │
│ B6      │ 3296792 │ 194.53 │
│ NK      │  588574 │ 193.74 │
│ EV      │ 6424908 │ 188.44 │
│ YV      │ 1919314 │ 180.48 │
└─────────┴─────────┴────────┘
→ Progress: 180.26 million rows, 1.08 GB (87.48 million rows/s., 524.89 MB/s.) ██████████████████████████████████████████████████████████████████████████████████████████████████████████████████████████████████████████████████████████████████████████████████████       95%
5 rows in set. Elapsed: 2.061 sec. Processed 184.69 million rows, 1.11 GB (89.62 million rows/s., 537.73 MB/s.)

```

# 9 数据的导入导出
数据库的数据导入导出时比较重要的功能，当我们迁移数据库数据或者备份数据库数据等时，需要将数据导出到外部系统或者导入到数据库中。
ClickHouse支持多种数据导入导出的格式，支持的详细格式列表可以查看官方文档[Formats for Input and Output Data](https://clickhouse.yandex/docs/en/interfaces/formats/)，
如果确定了导入的数据库支持二进制且对性能有要求，可以考虑使用二进制压缩格式。这里介绍接种常用的且导入和导出都支持的格式：csv和json。数据集我们使用一份基准测试数据。

## 9.1 获取基准测试数据
```bash
# 1 获取基准数据生成的项目，并编译生成基准数据生成的代码
git clone https://github.com/vadimtk/ssb-dbgen.git
cd ssb-dbgen
make

# 2 生成数据。
#  由于这里我们只针对数据的导入和导出，一次我们只生成 supplier 表的数据 
#  2000 行，大概有  188K。也可以调整-s 后面的参数，更改生成数据规模大小，值越大生成的数据集也越大
./dbgen -s 1 -T s


# 当然这个基准数据生成的项目也可以生成其它表的数据。
# 例如可以生成customer表数据。当s后的值调整为1000时数据集有30000000 行，大概有  3.2G
./dbgen -s 1 -T c
# 例如可以生成lineorder表数据，当s后的参数调整为1000时，数据集大于 50GB
./dbgen -s 1 -T l
# 例如可以生成part表数据，下面命令会生成 200000 行，大概有  20M 的数据
./dbgen -s 1 -T p
# 例如可以生成date表数据，下面命令会生成 2556 行，大概有  272K 的数据
./dbgen -s 1 -T p

# 更详细的可以查看 https://github.com/vadimtk/ssb-dbgen.git 中的README文件
```

## 9.2 建表并加载数据
```sql
--  1 创建 supplier 表
CREATE TABLE supplier(
 S_SUPPKEY       UInt32,
 S_NAME          String,
 S_ADDRESS       String,
 S_CITY          LowCardinality(String),
 S_NATION        LowCardinality(String),
 S_REGION        LowCardinality(String),
 S_PHONE         String
)ENGINE = MergeTree ORDER BY S_SUPPKEY;

--  2 将数据加载到 supplier 表
clickhouse-client -h 127.0.0.1 --port 9000 -u default --password KavrqeN1 --query "INSERT INTO supplier FORMAT CSV" < supplier.tbl
```

## 9.3 数据的导出
```
# 1 数据导出
#  说明，--query="SQL"，sql的语法格式为 SELECT * FROM 表名 FORMAT 输出格式

#  1.1 以CSV格式，指定需要导出的某些字段信息
clickhouse-client -h 127.0.0.1 --port 9000 -u default --password KavrqeN1 --query "SELECT S_SUPPKEY, S_NAME, S_ADDRESS,S_CITY,S_NATION,S_REGION,S_PHONE FROM supplier  FORMAT CSV" > /opt/supplier.tb0.csv

#  1.2 以CSV格式，导出表中所有字段的数据
clickhouse-client -h 127.0.0.1 --port 9000 -u default --password KavrqeN1 --query "SELECT * FROM supplier  FORMAT CSV" > /opt/supplier.tb1.csv
#  查看导出的数据文件 
head -n 5 supplier.tb1.csv
1,"Supplier#000000001","sdrGnXCDRcfriBvY0KL,i","PERU     0","PERU","AMERICA","27-989-741-2988"
2,"Supplier#000000002","TRMhVHz3XiFu","ETHIOPIA 1","ETHIOPIA","AFRICA","15-768-687-3665"
3,"Supplier#000000003","BZ0kXcHUcHjx62L7CjZS","ARGENTINA7","ARGENTINA","AMERICA","11-719-748-3364"
4,"Supplier#000000004","qGTQJXogS83a7MB","MOROCCO  4","MOROCCO","AFRICA","25-128-190-5944"
5,"Supplier#000000005","lONEYAh9sF","IRAQ     5","IRAQ","MIDDLE EAST","21-750-942-6364"

# 1.3 以CSV格式带表头信息形式，导出表中所有字段的数据
clickhouse-client -h 127.0.0.1 --port 9000 -u default --password KavrqeN1 --query "SELECT * FROM supplier  FORMAT CSVWithNames" > /opt/supplier.tb2.csv
#  查看导出的数据文件 
head -n 5 supplier.tb2.csv
"S_SUPPKEY","S_NAME","S_ADDRESS","S_CITY","S_NATION","S_REGION","S_PHONE"
1,"Supplier#000000001","sdrGnXCDRcfriBvY0KL,i","PERU     0","PERU","AMERICA","27-989-741-2988"
2,"Supplier#000000002","TRMhVHz3XiFu","ETHIOPIA 1","ETHIOPIA","AFRICA","15-768-687-3665"
3,"Supplier#000000003","BZ0kXcHUcHjx62L7CjZS","ARGENTINA7","ARGENTINA","AMERICA","11-719-748-3364"
4,"Supplier#000000004","qGTQJXogS83a7MB","MOROCCO  4","MOROCCO","AFRICA","25-128-190-5944"

#  1.4 以制表分隔符形式导出数据
clickhouse-client -h 127.0.0.1 --port 9000 -u default --password KavrqeN1 --query "SELECT * FROM supplier  FORMAT TabSeparated``" > /opt/supplier.tb3.txt
#  查看导出的数据文件 
head -n 5  supplier.tb3.txt
1       Supplier#000000001      sdrGnXCDRcfriBvY0KL,i   PERU     0      PERU    AMERICA 27-989-741-2988
2       Supplier#000000002      TRMhVHz3XiFu    ETHIOPIA 1      ETHIOPIA        AFRICA  15-768-687-3665
3       Supplier#000000003      BZ0kXcHUcHjx62L7CjZS    ARGENTINA7      ARGENTINA       AMERICA 11-719-748-3364
4       Supplier#000000004      qGTQJXogS83a7MB MOROCCO  4      MOROCCO AFRICA  25-128-190-5944
5       Supplier#000000005      lONEYAh9sF      IRAQ     5      IRAQ    MIDDLE EAST     21-750-942-6364

# 1.5 带表头信息的方式，以制表符方式导出数据文件。TabSeparatedWithNames 等价于 TSVWithNames
# 在解析这种文件时第一行会被完全忽略
clickhouse-client -h 127.0.0.1 --port 9000 -u default --password KavrqeN1 --query "SELECT * FROM supplier  FORMAT TSVWithNames" > /opt/supplier.tb4.txt
#  查看导出的数据文件 
head -n 5  supplier.tb4.txt
S_SUPPKEY       S_NAME  S_ADDRESS       S_CITY  S_NATION        S_REGION        S_PHONE
1       Supplier#000000001      sdrGnXCDRcfriBvY0KL,i   PERU     0      PERU    AMERICA 27-989-741-2988
2       Supplier#000000002      TRMhVHz3XiFu    ETHIOPIA 1      ETHIOPIA        AFRICA  15-768-687-3665
3       Supplier#000000003      BZ0kXcHUcHjx62L7CjZS    ARGENTINA7      ARGENTINA       AMERICA 11-719-748-3364
4       Supplier#000000004      qGTQJXogS83a7MB MOROCCO  4      MOROCCO AFRICA  25-128-190-5944

# 1.6 带表头信息的方式，以制表符方式导出数据文件。TabSeparatedWithNamesAndTypes 等价于 TSVWithNamesAndTypes
# 在解析这种文件时前两行会被完全忽略
clickhouse-client -h 127.0.0.1 --port 9000 -u default --password KavrqeN1 --query "SELECT * FROM supplier  FORMAT TabSeparatedWithNamesAndTypes" > /opt/supplier.tb5.txt
#  查看导出的数据文件 
head -n 5  supplier.tb5.txt
S_SUPPKEY       S_NAME  S_ADDRESS       S_CITY  S_NATION        S_REGION        S_PHONE
UInt32  String  String  LowCardinality(String)  LowCardinality(String)  LowCardinality(String)  String
1       Supplier#000000001      sdrGnXCDRcfriBvY0KL,i   PERU     0      PERU    AMERICA 27-989-741-2988
2       Supplier#000000002      TRMhVHz3XiFu    ETHIOPIA 1      ETHIOPIA        AFRICA  15-768-687-3665
3       Supplier#000000003      BZ0kXcHUcHjx62L7CjZS    ARGENTINA7      ARGENTINA       AMERICA 11-719-748-3364

# 1.7 以 KV 形式输出每一行，和前面的 TabSeparated 类似，不过是 name=value 的格式
clickhouse-client -h 127.0.0.1 --port 9000 -u default --password KavrqeN1 --query "SELECT * FROM supplier  FORMAT TSKV" > /opt/supplier.tb6.txt
#  查看导出的数据文件 
head -n 5  supplier.tb6.txt
S_SUPPKEY=1     S_NAME=Supplier#000000001       S_ADDRESS=sdrGnXCDRcfriBvY0KL,i S_CITY=PERU     0       S_NATION=PERU   S_REGION=AMERICA        S_PHONE=27-989-741-2988
S_SUPPKEY=2     S_NAME=Supplier#000000002       S_ADDRESS=TRMhVHz3XiFu  S_CITY=ETHIOPIA 1       S_NATION=ETHIOPIA       S_REGION=AFRICA S_PHONE=15-768-687-3665
S_SUPPKEY=3     S_NAME=Supplier#000000003       S_ADDRESS=BZ0kXcHUcHjx62L7CjZS  S_CITY=ARGENTINA7       S_NATION=ARGENTINA      S_REGION=AMERICA        S_PHONE=11-719-748-3364
S_SUPPKEY=4     S_NAME=Supplier#000000004       S_ADDRESS=qGTQJXogS83a7MB       S_CITY=MOROCCO  4       S_NATION=MOROCCO        S_REGION=AFRICA S_PHONE=25-128-190-5944
S_SUPPKEY=5     S_NAME=Supplier#000000005       S_ADDRESS=lONEYAh9sF    S_CITY=IRAQ     5       S_NATION=IRAQ   S_REGION=MIDDLE EAST    S_PHONE=21-750-942-6364

# 1.8 以元组形式打印每一行，每个括号用英文逗号分割
clickhouse-client -h 127.0.0.1 --port 9000 -u default --password KavrqeN1 --query "SELECT * FROM supplier LIMIT 3  FORMAT Values" > /opt/supplier.tb7.txt
#  查看导出的数据文件 
head  supplier.tb7.txt
(1,'Supplier#000000001','sdrGnXCDRcfriBvY0KL,i','PERU     0','PERU','AMERICA','27-989-741-2988'),(2,'Supplier#000000002','TRMhVHz3XiFu','ETHIOPIA 1','ETHIOPIA','AFRICA','15-768-687-3665'),(3,'Supplier#000000003','BZ0kXcHUcHjx62L7CjZS','ARGENTINA7','ARGENTINA','AMERICA','11-719-748-3364')

# 1.9 以 JSON 形式打印每一行
clickhouse-client -h 127.0.0.1 --port 9000 -u default --password KavrqeN1 --query "SELECT * FROM supplier  FORMAT JSONEachRow" > /opt/supplier.tb8.json
#  查看导出的数据文件 
head -n 5 supplier.tb8.json
{"S_SUPPKEY":1,"S_NAME":"Supplier#000000001","S_ADDRESS":"sdrGnXCDRcfriBvY0KL,i","S_CITY":"PERU     0","S_NATION":"PERU","S_REGION":"AMERICA","S_PHONE":"27-989-741-2988"}
{"S_SUPPKEY":2,"S_NAME":"Supplier#000000002","S_ADDRESS":"TRMhVHz3XiFu","S_CITY":"ETHIOPIA 1","S_NATION":"ETHIOPIA","S_REGION":"AFRICA","S_PHONE":"15-768-687-3665"}
{"S_SUPPKEY":3,"S_NAME":"Supplier#000000003","S_ADDRESS":"BZ0kXcHUcHjx62L7CjZS","S_CITY":"ARGENTINA7","S_NATION":"ARGENTINA","S_REGION":"AMERICA","S_PHONE":"11-719-748-3364"}
{"S_SUPPKEY":4,"S_NAME":"Supplier#000000004","S_ADDRESS":"qGTQJXogS83a7MB","S_CITY":"MOROCCO  4","S_NATION":"MOROCCO","S_REGION":"AFRICA","S_PHONE":"25-128-190-5944"}
{"S_SUPPKEY":5,"S_NAME":"Supplier#000000005","S_ADDRESS":"lONEYAh9sF","S_CITY":"IRAQ     5","S_NATION":"IRAQ","S_REGION":"MIDDLE EAST","S_PHONE":"21-750-942-6364"}

# 1.10 以二进制格式逐行格式化和解析数据
clickhouse-client -h 127.0.0.1 --port 9000 -u default --password KavrqeN1 --query "SELECT * FROM supplier  FORMAT RowBinary" > /opt/supplier.tb9.dat
```

## 9.4 数据的导入

数据库操作的可选项
```sql
-- 1 修改原有表名
rename table supplier to supplier_bak;

-- 2 删除表
drop table supplier;

-- 3 创建表
CREATE TABLE supplier(
 S_SUPPKEY       UInt32,
 S_NAME          String,
 S_ADDRESS       String,
 S_CITY          LowCardinality(String),
 S_NATION        LowCardinality(String),
 S_REGION        LowCardinality(String),
 S_PHONE         String
)ENGINE = MergeTree ORDER BY S_SUPPKEY;

-- 4 清空表
ALTER TABLE supplier DELETE WHERE 1=1;

```

开始导入，sql的语法格式为 `INSERT INTO 表名 FORMAT 输出格式`，输入格式同输出格式，这里以CSV和JSON两种方式为例，其它类似。
```bash
# 1 以CSV
clickhouse-client -h 127.0.0.1 --port 9000 -u default --password KavrqeN1 --query "INSERT INTO supplier FORMAT CSV" < /opt/supplier.tb1.csv

# 2 以JSON形式（导入之前可以先清除表数据）
clickhouse-client -h 127.0.0.1 --port 9000 -u default --password KavrqeN1 --query "INSERT INTO supplier FORMAT JSONEachRow" < /opt/supplier.tb8.json

```

# 10 Movie 数据分析
依旧使用我们前面获取的豆瓣电影的数据，
[doubanMovie.csv](https://github.com/yoreyuan/My_hadoop/blob/master/hadoop/film-ranking/src/main/resources/doubanMovie.csv)、
[rankQuote.csv](https://github.com/yoreyuan/My_hadoop/blob/master/hadoop/film-ranking/src/main/resources/rankQuote.csv)

```sql
-- 1 创建一张电影表
CREATE TABLE movie(
 id           UInt64  COMMENT '电影标识',
 movie_name   FixedString(256)  COMMENT '电影名',
 director     FixedString(128)  COMMENT '导演',
 scriptwriter FixedString(256)  COMMENT '编剧',
 protagonist  String  COMMENT '主演',
 kind         FixedString(64) COMMENT '类型',
 country      FixedString(64) COMMENT '地区',
 language     String COMMENT '语言', 
 release_date FixedString(128) COMMENT '上映日期',
 mins         String COMMENT '片长',
 alternate_name FixedString(256) COMMENT '又名',
 imdb         FixedString(128) COMMENT 'IMDb链接',
 rating_num   Float32  COMMENT '评分',
 rating_people UInt32  COMMENT '评分人数',
 url          FixedString(256) COMMENT 'url'
)ENGINE = MergeTree ORDER BY id;

-- 2 创建豆瓣电影排名与评语表
CREATE TABLE quote(
 id     UInt64 COMMENT '电影标识',
 rank   UInt32 COMMENT '排名',
 quote  String COMMENT '语录'
)ENGINE = MergeTree ORDER BY id;

```
```bash
# 3 导入数据
#  3.1 导入 Movie 数据
clickhouse-client -h 127.0.0.1 --port 9000 -u default --password KavrqeN1 --query "INSERT INTO movie FORMAT CSV" < /home/doubanMovie.csv 

#  3.2 导入 quote 数据
clickhouse-client -h 127.0.0.1 --port 9000 -u default --password KavrqeN1 --query "INSERT INTO quote FORMAT CSV" < /home/rankQuote.csv
```


```sql
-- 4 执行SQL分析查看数据。获取评分最高且评论人数最多的电影评语
-- ClickHouse支持的Join语法可以查看：https://clickhouse.yandex/docs/en/query_language/select/#select-join
cdh3 :) SELECT m.id,m.movie_name,m.rating_num,m.rating_people,q.rank,q.quote FROM movie m
:-] LEFT JOIN quote q
:-] ON q.id=m.id
:-] ORDER BY m.rating_num DESC,m.rating_people DESC LIMIT 10;
SELECT
    m.id,
    m.movie_name,
    m.rating_num,
    m.rating_people,
    q.rank,
    q.quote
FROM movie AS m
LEFT JOIN quote AS q ON q.id = m.id
ORDER BY
    m.rating_num DESC,
    m.rating_people DESC
LIMIT 10
↑ Progress: 0.00 rows, 0.00 B (0.00 rows/s., 0.00 B/s.) 
┌──────id─┬─movie_name───────────────────────────────────┬─rating_num─┬─rating_people─┬─rank─┬─quote──────────────────────────┐
│ 1292052 │ 肖申克的救赎 The Shawshank Redemption (1994) │        9.7 │       1502851 │    1 │ 希望让人自由。                 │
│ 1291546 │ 霸王别姬 (1993)                              │        9.6 │       1112641 │    2 │ 风华绝代。                     │
│ 1296141 │ 控方证人 Witness for the Prosecution (1957)  │        9.6 │        195362 │   29 │ 比利·怀德满分作品。            │
│ 1292063 │ 美丽人生 La vita è bella (1997)              │        9.5 │        690618 │    5 │ 最美的谎言。                   │
│ 1295124 │ 辛德勒的名单 Schindler's List (1993)         │        9.5 │        613865 │    8 │ 拯救一个人，就是拯救整个世界。 │
│ 1295644 │ 这个杀手不太冷 Léon (1994)                   │        9.4 │       1363430 │    3 │ 怪蜀黍和小萝莉不得不说的故事。 │
│ 1292720 │ 阿甘正传 Forrest Gump (1994)                 │        9.4 │       1178003 │    4 │ 一部美国近现代史。             │
│ 1292722 │ 泰坦尼克号 Titanic (1997)                    │        9.4 │       1119405 │    7 │ 失去的才是永恒的。             │
│ 1293182 │ 十二怒汉 12 Angry Men (1957)                 │        9.4 │        253408 │   36 │ 1957年的理想主义。             │
│ 1291561 │ 千与千寻 千と千尋の神隠し (2001)             │        9.3 │       1205228 │    6 │ 最好的宫崎骏，最好的久石让。   │
└─────────┴──────────────────────────────────────────────┴────────────┴───────────────┴──────┴────────────────────────────────┘
↗ Progress: 0.00 rows, 0.00 B (0.00 rows/s., 0.00 B/s.) → Progress: 500.00 rows, 84.28 KB (86.34 thousand rows/s., 14.55 MB/s.)  3%
10 rows in set. Elapsed: 0.006 sec.
'

-- 5 修改数据
--  在 quote 表中 id 为 5908478 数据的评语没有句号，我们修改这条数据给它加上句号
cdh3 :) SELECT * FROM quote WHERE id=5908478;
┌──────id─┬─rank─┬─quote────────────────────────────────────────┐
│ 5908478 │  244 │ 你要相信，这世上真的有爱存在，不管在什么年纪 │
└─────────┴──────┴──────────────────────────────────────────────┘
-- 修改
cdh3 :) ALTER TABLE quote UPDATE quote = "你要相信，这世上真的有爱存在，不管在什么年纪。" where id=5908478;
ALTER TABLE quote
    UPDATE quote = "你要相信，这世上真的有爱存在，不管在什么年纪。" WHERE id = 5908478
Ok.
0 rows in set. Elapsed: 0.016 sec.
-- 再次查看数据，
cdh3 :) SELECT * FROM quote WHERE id=5908478;
┌──────id─┬─rank─┬─quote──────────────────────────────────────────┐
│ 5908478 │  244 │ 你要相信，这世上真的有爱存在，不管在什么年纪。 │
└─────────┴──────┴────────────────────────────────────────────────┘

```

# 卸载
```bash
# 1 查看rpm列表
rpm -qa | grep clickhouse

# 卸载
# rpm -e --nodeps 要卸载的软件包
rpm -e clickhouse-client-19.16.2.2-2.noarch clickhouse-common-static-19.16.2.2-2.x86_64 clickhouse-server-19.16.2.2-2.noarch

# 删除配置文件
rm -rf /etc/clickhouse-server
rm -rf /etc/clickhouse-client
rm -rf /var/lib/clickhouse

```








