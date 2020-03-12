Beeline 的使用
-------

# 1 Beeline 在 Hive 官方文档中的介绍

## 1.1 [HiveServer2 Clients](https://cwiki.apache.org/confluence/display/Hive/HiveServer2+Clients)
* Beeline – 命令行 Shell
    + Beeline 样例  
    + Beeline 命令
    + Beeline Hive 命令
    + Beeline 命令项
    + 输出格式
        - table
        - vertical
        - xmlattr
        - xmlelements
        - 分数值（Separated-Value）输出格式
            * csv2, tsv2, dsv
                - Quoting in csv2, tsv2 and dsv Formats
            * csv, tsv
    + HiveServer2 记录
    + 取消查询
    + Terminal端脚本的后台查询

## 1.2 Beeline – 命令行 Shell
HiveServer2 支持与 HiveServer2 一起使用的命令行 Shell Beeline。它是一个基于 [SQLLine CLI](http://sqlline.sourceforge.net/)的 JDBC 客户端。
[SQLLine 的详细文档](http://sqlline.sourceforge.net/#manual)也适用于 Beeline。

使用 Beeline 替代 Hive CLI 的详细实现可以[查看🔗](https://cwiki.apache.org/confluence/display/Hive/Replacing+the+Implementation+of+Hive+CLI+Using+Beeline)

Beeline Shell 在嵌入式模式（embedded mode）和**远程模式**（remote mode）下均可工作。 在嵌入式模式下，它运行嵌入式Hive（类似于[Hive CLI](https://cwiki.apache.org/confluence/display/Hive/LanguageManual+Cli)），
而远程模式用于通过Thrift连接到单独的 HiveServer2 进程。 从 [Hive 0.14](https://issues.apache.org/jira/browse/HIVE-7615) 开始，
当 Beeline 与 HiveServer2 一起使用时它还会打印 HiveServer2 的日志消息，以查询执行到 STDERR（标准错误） 的查询。 **建议将远程 HiveServer2 模式用于生产环境**，
因为它更安全并且不需要为用户授予直接 HDFS/metastore 访问权限。

**注意**：在远程模式下 HiveServer2 仅接受有效的 Thrift 调用，即使在HTTP模式下，消息正文也包含 Thrift 有效负载。


### 1.2.1 Beeline 样例
```sql
% bin/beeline 
Hive version 0.11.0-SNAPSHOT by Apache
beeline> !connect jdbc:hive2://localhost:10000 scott tiger
!connect jdbc:hive2://localhost:10000 scott tiger 
Connecting to jdbc:hive2://localhost:10000
Connected to: Hive (version 0.10.0)
Driver: Hive (version 0.10.0-SNAPSHOT)
Transaction isolation: TRANSACTION_REPEATABLE_READ
0: jdbc:hive2://localhost:10000> show tables;
show tables;
+-------------------+
|     tab_name      |
+-------------------+
| primitives        |
| src               |
| src1              |
| src_json          |
| src_sequencefile  |
| src_thrift        |
| srcbucket         |
| srcbucket2        |
| srcpart           |
+-------------------+
9 rows selected (1.079 seconds)

```

您还可以在命令行上指定连接参数，这意味着您可以从 UNIX Shell 历史记录中找到带有连接字符串的命令。
```bash
% beeline -u jdbc:hive2://localhost:10000/default -n scott -w password_file
Hive version 0.11.0-SNAPSHOT by Apache

Connecting to jdbc:hive2://localhost:10000/default

```

**注意**： 带 NoSASL（没有简单身份认证和安全层的）连接的 Beeline
如果要通过 NOSASL 模式进行连接，则必须明确指定身份验证模式：
```sql
% bin/beeline
beeline> !connect jdbc:hive2://<host>:<port>/<db>;auth=noSasl hiveuser pass 
```

### 1.2.2 Beeline 命令
命令  | 描述
:---- | :----
`!<SQLLine command>`  | SQLLine 命令列表可从 [http://sqlline.sourceforge.net/](http://sqlline.sourceforge.net/) 获得。<br/> **示例**：`!quit` 退出 Beeline 客户端。
`!delimiter`          | 设置用 Beeline 编写的查询的分隔符。允许使用多字符分隔符，但不允许使用引号、斜杠，并且`--`符为默认的；<br/> **用法**：`!delimiter $$` <br/> **版本**：[3.0.0 (HIVE-10865)](https://issues.apache.org/jira/browse/HIVE-10865)

### 1.2.3 Beeline Hive 命令
使用 Hive JDBC 驱动程序时，可以从 Beeline 运行 Hive 特定命令（与 Hive CLI 命令相同）。

采用 `;` （英文分号）作为终止命令。脚本中的注释可以使用 `--` 前缀指定。

**提示**：连接 Hive 之后（!connect jdbc:hive2://localhost:10000/default）

命令  | 描述
:---- | :----
`reset`  | 将配置重置为默认值。
`reset <key>` | 将指定配置变量(key)的值重置为默认值。**注意**：如果是拼写错误的变量名，Beeline 将不会提示错误。
`set <key>=<value>` | 设置特定配置变量(key)的值。**注意**：如果是拼写错误的变量名，Beeline 将不会提示错误。
`set` | 打印由用户或 Hive 覆盖的变量列表
`set -v` | 打印所有 Hadoop 和 Hive 配置变量
①`add FILE[S] <filepath> <filepath>*` <br/>②`add JAR[S] <filepath> <filepath>*` <br/>③`add ARCHIVE[S] <filepath> <filepath>*` |  将一个或多个文件、jar 或存档（archives）添加到分布式缓存中的资源列表。有关更多信息请参见[Hive资源](https://cwiki.apache.org/confluence/display/Hive/LanguageManual+Cli#LanguageManualCli-HiveResources)。
①`add FILE[S] <ivyurl> <ivyurl>*`<br/>②`add JAR[S] <ivyurl> <ivyurl>* `<br/>③`add ARCHIVE[S] <ivyurl> <ivyurl>*` | 从[Hive 1.2.0](https://issues.apache.org/jira/browse/HIVE-9664)开始，使用格式为 `ivy://group:module:version?query_string` 的[ivy](http://ant.apache.org/ivy/) URL将一个或多个文件、jar或存档添加到分布式缓存中的资源列表中。有关更多信息请参见[Hive资源](https://cwiki.apache.org/confluence/display/Hive/LanguageManual+Cli#LanguageManualCli-HiveResources)。
①`list FILE[S]`<br/>`list JAR[S]`<br/>②`list ARCHIVE[S]` | 列出已经添加到分布式缓存的资源。有关更多信息请参见[Hive资源](https://cwiki.apache.org/confluence/display/Hive/LanguageManual+Cli#LanguageManualCli-HiveResources)。（自Hive 0.14.0起：[HIVE-7592](https://issues.apache.org/jira/browse/HIVE-7592)）。
①`list FILE[S] <filepath>*`<br/>②`list JAR[S] <filepath>*`<br/>`③list ARCHIVE[S] <filepath>*` | 检查给定资源是否已经添加到分布式缓存中。有关更多信息请参见[Hive资源](https://cwiki.apache.org/confluence/display/Hive/LanguageManual+Cli#LanguageManualCli-HiveResources)。
①`delete FILE[S] <filepath>*`<br/>②`delete JAR[S] <filepath>*`<br/>③`delete ARCHIVE[S] <filepath>*` | 从分布式缓存中删除资源。
①`delete FILE[S] <ivyurl> <ivyurl>*`<br/> ②`delete JAR[S] <ivyurl> <ivyurl>*`<br/> ③`delete ARCHIVE[S] <ivyurl> <ivyurl>*` | 从 [Hive 1.2.0](https://issues.apache.org/jira/browse/HIVE-9664) 开始，从分布式缓存中删除使用`<ivyurl>`添加的资源。有关更多信息，有关更多信息请参见[Hive资源](https://cwiki.apache.org/confluence/display/Hive/LanguageManual+Cli#LanguageManualCli-HiveResources)。
`reload` | 从[Hive 0.14.0](https://issues.apache.org/jira/browse/HIVE-7553)开始，使 HiveServer2 获知配置参数[hive.reloadable.aux.jars.path](https://cwiki.apache.org/confluence/display/Hive/Configuration+Properties#ConfigurationProperties-hive.reloadable.aux.jars.path) 指定的路径中的任何jar变化（无需重新启动 HiveServer2）。所做的更改可以是添加、删除或更新jar文件。
`dfs <dfs command>` | 执行dfs命令。
`<query string>` | 执行 Hive 查询并将结果打印到标准输出。

### 1.2.4 Beeline 命令项
Beeline CLI支持以下命令行选项：

命令  | 描述
:---- | :----
`-u <database URL>` | 要连接的 JDBC URL，如果需要参数值中的特殊字符应使用 URL 编码进行编码。 <br/> 用法：`beeline -u db_URL`
`r` | [重新连接](https://cwiki.apache.org/confluence/display/Hive/HiveServer2+Clients#HiveServer2Clients-Reconnecting)到上次使用的URL（如果用户以前使用 `!connect` 到 URL，并且使用 `!save` 到 beeline.properties 文件）。 <br/> 用法：`beeline -r` <br/> 版本：: 2.1.0 ([HIVE-13670](https://issues.apache.org/jira/browse/HIVE-13670))
`-n <username>` | 连接的用户名。 <br/> 用法：`beeline -n valid_user`
`-p <password>` | 连接的密码。 <br/> 用法：`beeline -p valid_password` <br/> 可选的 password 模式。从Hive 2.2.0（[HIVE-13589](https://issues.apache.org/jira/browse/HIVE-13589)）开始，`-p`选项的参数是可选的。<br/>  用法：`beeline -p [valid_password]` <br/> 如果未提供密码，则`-p` Beeline 将在启动连接时提示您输入密码，当提供密码时，Beeline使用它来启动连接并且不提示。
`-d <driver class>` | 要使用的驱动程序类。<br/> 用法：` beeline -d driver_class`
`-e <query>` | 应该执行的查询，双引号或单引号引起来的查询字符串，可以多次指定此选项。<br/> 用法：` beeline -e "query_string" ` <br/> 支持在单个 query_string 中运行多个用分号分隔的 SQL 语句：1.2.0（[HIVE-9877](https://issues.apache.org/jira/browse/HIVE-9877)）<br/> 错误修复（空指针异常）：0.13.0（[HIVE-5765](https://issues.apache.org/jira/browse/HIVE-5765)） <br/> 错误修复（不支持`--headerInterval`）：0.14.0（[HIVE-7647](https://issues.apache.org/jira/browse/HIVE-7647)）<br/> 错误修复（在后台运行 `-e`）：1.3.0 和 2.0.0（[HIVE-6758](https://issues.apache.org/jira/browse/HIVE-6758)）;早期版本可用的[解决方法](https://issues.apache.org/jira/browse/HIVE-6758?focusedCommentId=13954968&page=com.atlassian.jira.plugin.system.issuetabpanels:comment-tabpanel#comment-13954968)
`-f <file>` | 应执行的脚本文件。 <br/> 用法：`beeline -f filepath` <br/> 版本：0.12.0 ([HIVE-4268](https://issues.apache.org/jira/browse/HIVE-4268))<br/> **注意**：如果脚本包含 tab 符，则查询编译在版本0.12.0中会失败，此错误已在版本0.13.0（[HIVE-6359](https://issues.apache.org/jira/browse/HIVE-6359)）中修复。错误修复（在后台运行`-f`）：1.3.0和2.0.0（[HIVE-6758](https://issues.apache.org/jira/browse/HIVE-6758)）；早期版本可用的[解决方法](https://issues.apache.org/jira/browse/HIVE-6758?focusedCommentId=13954968&page=com.atlassian.jira.plugin.system.issuetabpanels:comment-tabpanel#comment-13954968)
`-i (or) --init <file or files>` | 用于初始化的 init 文件 <br/> 用法：`beeline -i /tmp/initfile` <br/> 单文件。版本: 0.14.0 ([HIVE-6561](https://issues.apache.org/jira/browse/HIVE-6561)) <br/> 多文件文件。版本: 2.1.0 ([HIVE-11336](https://issues.apache.org/jira/browse/HIVE-11336) 
`-w (or) --password-file <password file>` | 从保存密码的文件中读取密码 <br/> 版本：1.2.0 ([HIVE-7175](https://issues.apache.org/jira/browse/HIVE-7175))
`-a (or) --authType <auth type>` | 身份验证类型作为 auth 属性传递给 jdbc <br/> 版本：0.13.0 ([HIVE-5155](https://issues.apache.org/jira/browse/HIVE-5155))
`--property-file <file>` | 从中读取配置属性的文件 <br/> 用法：`beeline --property-file /tmp/a` <br/>版本：2.2.0 ([HIVE-13964](https://issues.apache.org/jira/browse/HIVE-13964))
`--hiveconf property=value` | 给定配置属性的使用值。`hive.conf.restricted.list` 中列出的属性无法使用 `hiveconf` 重置（请参阅[限制列表和白名单](https://cwiki.apache.org/confluence/display/Hive/Configuration+Properties#ConfigurationProperties-RestrictedListandWhitelist)）。 <br/> 用法：`beeline --hiveconf prop1=value1` <br/>版本：0.13.0 ([HIVE-6173](https://issues.apache.org/jira/browse/HIVE-6173))
`--hivevar name=value` | 配置单元变量名称和值，这是特定于 Hive 的设置，其中可以在会话级别设置变量，并在Hive命令或查询中引用。<br/> 用法：`beeline --hivevar var1=value1` 
`--color=[true/false]` | 控制是否使用颜色进行显示。默认为false。<br/> 用法：`beeline --color=true` <br/> （不支持分数值输出格式。请参阅 [HIVE-9770](https://issues.apache.org/jira/browse/HIVE-9770)）
`--showHeader=[true/false]` | 在查询结果中显示列名（true）或者（false），默认为 true。<br/> 用法：`beeline --showHeader=false` 
`--headerInterval=ROWS` | 当 outputformat 为表时，重新显示列标题的间隔（以行数为单位），默认值为100。<br/> 用法：`beeline --headerInterval=50` <br/> （不支持分数值输出格式。请参阅 [HIVE-9770](https://issues.apache.org/jira/browse/HIVE-9770)）
`--fastConnect=[true/false]` | 连接时跳过为 HiveQL 语句的制表符完成而建立所有表和列的列表（true）或建立list（false），默认为true。<br/> 用法：`beeline --fastConnect=false` 
`--autoCommit=[true/false]` | 启用/禁用 自动事务提交。默认为false。 <br/> 用法：`beeline --autoCommit=true` 
`--verbose=[true/false]` | 显示详细的错误消息和调试信息（true）或不显示（false），默认为false。<br/> 用法：`beeline --verbose=true` 
`--showWarnings=[true/false]` | 显示发出任何 HiveQL 命令后在连接上报告的警告，默认为false。<br/> 用法：`beeline --showWarnings=true` 
`--showDbInPrompt=[true/false]` | 在提示中显示当前数据库名称（例如` (库名)>`），默认为false。<br/> 用法：`beeline --showDbInPrompt=true` <br/> 版本: 2.2.0 ([HIVE-14123](https://issues.apache.org/jira/browse/HIVE-14123))
`--showNestedErrs=[true/false]` | 显示嵌套错误，默认为false。<br/> 用法：`beeline --showNestedErrs=true` 
`--numberFormat=[pattern]` | 使用 [DecimalFormat](http://docs.oracle.com/javase/7/docs/api/java/text/DecimalFormat.html) 模式格式化数字。 <br/> 用法：`beeline --numberFormat="#,###,##0.00" `
`--force=[true/false]` | 脚本出现错误是否继续运行，默认为false。 <br/> 用法：`beeline--force=true `
`--maxWidth=MAXWIDTH` | 当 outputformat 是 table 时，在截取数据之前显示的最大宽度（以字符为单位），默认是查询终端当前宽度，然后回落到 80。<br/> 用法：`beeline --maxWidth=150 `
`--maxColumnWidth=MAXCOLWIDTH` | 当 outputformat 为 table 时，最大列宽（以字符为单位）。在Hive 2.2.0+版本（请参阅 [HIVE-14135](https://issues.apache.org/jira/browse/HIVE-14135)）中，默认值为50；在较早版本中，默认值为15。<br/> 用法：`beeline --maxColumnWidth=25`
`--silent=[true/false]` | 是否减少显示的信息消息的数量。它还停止显示来自 HiveServer2（[Hive 0.14](https://issues.apache.org/jira/browse/HIVE-7615)和更高版本）和 HiveQL 命令（[Hive 1.2.0](https://issues.apache.org/jira/browse/HIVE-10202)和更高版本）的查询日志消息。默认为false。<br/> 用法：`beeline --silent=true`
`--autosave=[true/false]` | 自动保存首选项（true）或不自动保存（false），默认为false。 <br/> 用法：`beeline --autosave=true`
`--outputformat=[table/vertical/csv/tsv/dsv/csv2/tsv2]` | 结果显示的格式化模式，默认为表格。有关建议的sv选项的说明，请参见下面的[分隔值输出格式](#1.2.5)。<br/> 用法：`beeline --outputformat=tsv` <br/> 版本：dsv/csv2/tsv2 added in 0.14.0 ([HIVE-8615](https://issues.apache.org/jira/browse/HIVE-8615))
`--truncateTable=[true/false]` | 如果为true，则超过控制台长度时会在控制台中截断表格列。<br/> 版本：0.14.0 ([HIVE-6928](https://issues.apache.org/jira/browse/HIVE-6928))
`--delimiterForDSV= DELIMITER` | 定界符分隔值的分隔符输出格式。默认值为 '&#124;' 字符。<br/> 版本：0.14.0 ([HIVE-7390](https://issues.apache.org/jira/browse/HIVE-7390))
`--isolation=LEVEL` | 将事务隔离级别设置为 TRANSACTION_READ_COMMITTED 或者 TRANSACTION_SERIALIZABLE。请参阅[Java Connection文档](http://docs.oracle.com/javase/7/docs/api/java/sql/Connection.html)中的 “字段详细信息” 部分。<br/> 用法：`beeline --isolation=TRANSACTION_SERIALIZABLE`
`--nullemptystring=[true/false]` | 使用将 null 打印为空字符串（true）的历史行为，或使用将 null 打印为 NULL（false）的当前行为，默认为false。<br/> 用法：`beeline --nullemptystring=false` <br/> 版本： 0.13.0 ([HIVE-4485](https://issues.apache.org/jira/browse/HIVE-4485)) 
`--incremental=[true/false]` | 从 Hive 2.3 起默认为true，之前默认为false。 如果设置为false，则在显示结果之前先提取并缓冲整个结果集，从而获得最佳的显示列大小。设置为true时，结果行将在提取时立即显示，从而以较低的显示列填充为代价，降低了等待时间和内存使用量。 如果在客户端遇到 OutOfMemory，则建议设置 `--incremental=true`（由于获取的结果集的大小太大）。
`--incrementalBufferRows=NUMROWS` | 在 stdout 上打印行时要缓冲的行数，默认为1000；默认值为1000，仅在 `--incremental=true ` 和 `--outputformat=table` 时适用 <br/> 用法：`beeline --incrementalBufferRows=1000` <br/> 版本： 2.3.0 ([HIVE-14170](https://issues.apache.org/jira/browse/HIVE-14170))
`--maxHistoryRows=NUMROWS` | Beeline 存储历史记录的最大行数。<br/>版本：2.3.0 ([HIVE-15166](https://issues.apache.org/jira/browse/HIVE-15166))
`--delimiter=;` | 设置用 Beeline 编写的查询的分割符，允许使用多字符定界符，但不允许使用引号、斜杠，并且 `--`是默认的。<br/> 用法：`beeline --delimiter=$$` <br/> 版本：3.0.0 ([HIVE-10865](https://issues.apache.org/jira/browse/HIVE-10865))
`--convertBinaryArrayToString=[true/false]` | 将二进制列数据显示为字符串或字节数组。<br/> 用法：`beeline --convertBinaryArrayToString=true` <br/> 版本：3.0.0 ([HIVE-14786](https://issues.apache.org/jira/browse/HIVE-14786))
`--help` | 显示用法信息。 <br/> 用法：`beeline --help`

### <a id="1.2.5"></a>1.2.5 输出格式
在Beeline中，结果可以以不同的格式显示。可以使用 outputformat 选项设置格式模式。支持以下输出格式：
* table
* vertical
* xmlattr
* xmlelements
* separated-value formats (csv, tsv, csv2, tsv2, dsv)

#### table
结果显示在表格中，结果的一行对应于表中的一行，一行中的值显示在表中的单独列中。这是默认的格式模式。

例：
```sql
-- 查询结果
> select id, value, comment from test_table;
+-----+---------+-----------------+
| id  |  value  |     comment     |
+-----+---------+-----------------+
| 1   | Value1  | Test comment 1  |
| 2   | Value2  | Test comment 2  |
| 3   | Value3  | Test comment 3  |
+-----+---------+-----------------+

```

#### vertical
结果的每一行都以键值格式的块显示，其中键是列的名称。

例：
```sql
-- 查询结果
> select id, value, comment from test_table;
id       1
value    Value1
comment  Test comment 1

id       2
value    Value2
comment  Test comment 2

id       3
value    Value3
comment  Test comment 3

```

#### xmlattr
结果以 XML 格式显示，其中每一行都是XML中的“结果”元素。 在“结果”元素上，将行的值显示为属性，属性的名称是列的名称。

例：
```sql
-- 查询结果
> select id, value, comment from test_table;
<resultset>
  <result id="1" value="Value1" comment="Test comment 1"/>
  <result id="2" value="Value2" comment="Test comment 2"/>
  <result id="3" value="Value3" comment="Test comment 3"/>
</resultset>

```

#### xmlelements
结果以 XML 格式显示，其中每一行都是XML中的“结果”元素。行的值显示为结果元素的子元素。

例：
```sql
-- 查询结果
> select id, value, comment from test_table;
<resultset>
  <result>
    <id>1</id>
    <value>Value1</value>
    <comment>Test comment 1</comment>
  </result>
  <result>
    <id>2</id>
    <value>Value2</value>
    <comment>Test comment 2</comment>
  </result>
  <result>
    <id>3</id>
    <value>Value3</value>
    <comment>Test comment 3</comment>
  </result>
</resultset>

```

#### 分数值（Separated-Value）输出格式
行的值由不同的分隔符分隔。有五种分隔值输出格式：csv、tsv、csv2、tsv2 和 dsv。

##### csv2、tsv2、dsv
从 [Hive 0.14](https://issues.apache.org/jira/browse/HIVE-8615) 开始，提供了改进的 SV 输出格式，即dsv、csv2 和 tsv2。
这三种格式的区别仅在于单元格之间的分隔符，对于 **csv2 是逗号**，对于 **tsv2 是制表符**，而对于 **dsv 是可配置的**。

对于 dsv 格式，可以使用 delimiterForDSV 选项设置分隔符。 默认分隔符为 '|'。**请注意，仅支持单个字符定界符**。

例：
```sql
-- 查询结果
> select id, value, comment from test_table;

-- csv2
id,value,comment
1,Value1,Test comment 1
2,Value2,Test comment 2
3,Value3,Test comment 3

-- tsv2
id	value	comment
1	Value1	Test comment 1
2	Value2	Test comment 2
3	Value3	Test comment 3

-- dsv (分隔符是 |)
id|value|comment
1|Value1|Test comment 1
2|Value2|Test comment 2
3|Value3|Test comment 3

```

######  csv2、tsv2 和 dsv 格式中的引号
如果未禁用引号，则如果值包含特殊字符（例如分隔符或双引号字符）或跨多行，则在值周围添加双引号。 嵌入的双引号与前面的双引号一起转义。

可以通过将 `disable.quoting.for.sv` 系统变量设置为true来禁用引号。
如果禁用了引号，则不会在值周围添加双引号（即使它们包含特殊字符），并且不会对嵌入的双引号进行转义。 默认情况下，引用被禁用。

例：
```
-- 查询结果
> select id, value, comment from test_table;

-- csv2 中 引号可用时
id,value,comment
1,"Value,1",Value contains comma
2,"Value""2",Value contains double quote
3,Value'3,Value contains single quote

-- csv2中 引号禁用时
id,value,comment
1,Value,1,Value contains comma
2,Value"2,Value contains double quote
3,Value'3,Value contains single quote

```
##### csv, tsv
* 这两种格式的区别仅在于值之间的分隔符，对于csv是逗号，对于tsv是制表符。
* 即使使用`disable.quoting.for.sv`系统变量禁用了引号，这些**值也始终用单引号字符引起来**。
* 这些输出格式不会转义嵌入的单引号。
* 请注意，不建议使用这些输出格式，仅保留它们是为了向后兼容。

例：
```sql
-- 查询结果
> select id, value, comment from test_table;

-- csv
'id','value','comment'
'1','Value1','Test comment 1'
'2','Value2','Test comment 2'
'3','Value3','Test comment 3'

--tsv
'id'	'value'	'comment'
'1'	'Value1'	'Test comment 1'
'2'	'Value2'	'Test comment 2'
'3'	'Value3'	'Test comment 3'

```

### 1.2.6 HiveServer2 记录540/5000
从 Hive 0.14.0 开始，HiveServer2 操作日志可用于 Beeline 客户端。 这些参数配置日志记录：

* [hive.server2.logging.operation.enabled](https://cwiki.apache.org/confluence/display/Hive/Configuration+Properties#ConfigurationProperties-hive.server2.logging.operation.enabled)
* [hive.server2.logging.operation.log.location](https://cwiki.apache.org/confluence/display/Hive/Configuration+Properties#ConfigurationProperties-hive.server2.logging.operation.log.location)
* [hive.server2.logging.operation.verbose（Hive 0.14 to 1.1）](https://cwiki.apache.org/confluence/display/Hive/Configuration+Properties#ConfigurationProperties-hive.server2.logging.operation.verbose)
* [hive.server2.logging.operation.level（从Hive 1.2开始）](https://cwiki.apache.org/confluence/display/Hive/Configuration+Properties#ConfigurationProperties-hive.server2.logging.operation.level)

[HIVE-11488](https://issues.apache.org/jira/browse/HIVE-11488)（Hive 2.0.0）在 HiveServer2 日志文件中添加了对记录 queryId 和 sessionId 的支持。 
要启用该功能，请将 `%X{queryId}` 和 `%X{sessionId}` 编辑或添加到日志记录配置文件的模式格式字符串中。

### 1.2.7 取消查询
当用户在 Beeline Shell上 输入 `CTRL+C` 时，如果同时存在一个查询，则 Beeline 尝试在关闭与 HiveServer2 的套接字连接时取消该查询。
仅当[hive.server2.close.session.on.disconnect](https://cwiki.apache.org/confluence/display/Hive/Configuration+Properties#ConfigurationProperties-hive.server2.close.session.on.disconnect)设置为 true 时才启用此行为。 
从 Hive 2.2.0 开始（[HIVE-15626](https://issues.apache.org/jira/browse/HIVE-15626)）当用户输入 `CTRL+C` 取消正在运行的查询时，Beeline不会退出命令行 shell。 
如果用户希望退出 shell 程序，则可以在取消查询时第二次输入 `CTRL+C`。 但是如果当前没有查询在运行，则第一个 `CTRL+C` 将退出 Beeline Shell。 此行为类似于 Hive CLI 处理 `CTRL+C` 的方式。

建议使用 `!quit` 退出 Beeline shell。

### 1.2.8 Terminal端脚本的后台查询
可以使用 nohup 和 disown 等命令从终端上断开 Beeline 的运行以进行批处理和自动化脚本。

某些版本的 Beeline 客户端可能需要一种解决方法，以允许 nohup 命令将 Beeline 进程正确地置于后台而不停止它。参见
[HIVE-11717](https://issues.apache.org/jira/browse/HIVE-11717)，[HIVE-6758](https://issues.apache.org/jira/browse/HIVE-6758)。

可以更新以下环境变量：
```bash
export HADOOP_CLIENT_OPTS="$HADOOP_CLIENT_OPTS -Djline.terminal=jline.UnsupportedTerminal"
```

使用 nohangup(nohup) 和 连字符(&) 进行运行会将进程置于后台，并允许终端断开连接，同时保持 Beeline 进程运行。
```bash
nohup beeline --silent=true --showHeader=true --outputformat=dsv -f query.hql </dev/null > /tmp/output.log 2> /tmp/error.log &

```

<br/><br/>

**********



```bash

beeline -n scm -p 3UsaTx#bHR -d "com.mysql.jdbc.Driver" \
-u "jdbc:mysql://cdh1:3306/flink_test?useUnicode=true&characterEncoding=utf8&useSSL=false" \
--color=true --isolation=TRANSACTION_SERIALIZABLE --incremental=false 


nohup beeline -n hive -p hive -d "org.apache.hive.jdbc.HiveDriver" -u "jdbc:hive2://cdh3:10000/default" \
--hiveconf mapreduce.job.queuename=datacenter --silent=true --showHeader=true --outputformat=csv2 \
-e "desc hive_test.tmp_test" \
</dev/null >> /tmp/output.log 2>> /tmp/error.log &

nohup beeline -n hive -p hive -d "org.apache.hive.jdbc.HiveDriver" -u "jdbc:hive2://cdh3:10000/default" \
--hiveconf mapreduce.job.queuename=datacenter --silent=true --showHeader=true --outputformat=dsv \
-f ./my-hive.sql \
</dev/null >> /tmp/output.log 2>> /tmp/error.log &

```


<br/>
<br/>

更详细的可以访问我的 blog [Beeline 的进阶使用](https://blog.csdn.net/github_39577257/article/details/104645603)




