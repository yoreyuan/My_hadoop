[DATAX](https://github.com/alibaba/DataX)
=========
DataX 是阿里巴巴集团内被广泛使用的离线数据同步工具/平台，实现包括 MySQL、Oracle、SqlServer、Postgre、HDFS、Hive、ADS、HBase、
TableStore(OTS)、MaxCompute(ODPS)、DRDS 等各种异构数据源之间高效的数据同步功能。

# 1 详细介绍
请参考：[DataX-Introduction](https://github.com/alibaba/DataX/blob/master/introduction.md)

# 2 支持的数据通道
DataX目前已经有了比较全面的插件体系，主流的RDBMS数据库、NOSQL、大数据计算系统都已经接入，目前支持数据如下图，
详情请点击：[DataX数据源参考指南](https://github.com/alibaba/DataX/wiki/DataX-all-data-channels)

| 类型           | 数据源        | Reader(读) | Writer(写) |文档|
| ------------ | ---------- | :-------: | :-------: |:-------: |
| RDBMS 关系型数据库 | MySQL      |     √     |     √     |[读](https://github.com/alibaba/DataX/blob/master/mysqlreader/doc/mysqlreader.md) 、[写](https://github.com/alibaba/DataX/blob/master/mysqlwriter/doc/mysqlwriter.md)|
|              | Oracle     |     √     |     √     |[读](https://github.com/alibaba/DataX/blob/master/oraclereader/doc/oraclereader.md) 、[写](https://github.com/alibaba/DataX/blob/master/oraclewriter/doc/oraclewriter.md)|
|              | SQLServer  |     √     |     √     |[读](https://github.com/alibaba/DataX/blob/master/sqlserverreader/doc/sqlserverreader.md) 、[写](https://github.com/alibaba/DataX/blob/master/sqlserverwriter/doc/sqlserverwriter.md)|
|              | PostgreSQL |     √     |     √     |[读](https://github.com/alibaba/DataX/blob/master/postgresqlreader/doc/postgresqlreader.md) 、[写](https://github.com/alibaba/DataX/blob/master/postgresqlwriter/doc/postgresqlwriter.md)|
|              | DRDS |     √     |     √     |[读](https://github.com/alibaba/DataX/blob/master/drdsreader/doc/drdsreader.md) 、[写](https://github.com/alibaba/DataX/blob/master/drdswriter/doc/drdswriter.md)|
|              | 通用RDBMS(支持所有关系型数据库)         |     √     |     √     |[读](https://github.com/alibaba/DataX/blob/master/rdbmsreader/doc/rdbmsreader.md) 、[写](https://github.com/alibaba/DataX/blob/master/rdbmswriter/doc/rdbmswriter.md)|
| 阿里云数仓数据存储    | ODPS       |     √     |     √     |[读](https://github.com/alibaba/DataX/blob/master/odpsreader/doc/odpsreader.md) 、[写](https://github.com/alibaba/DataX/blob/master/odpswriter/doc/odpswriter.md)|
|              | ADS        |           |     √     |[写](https://github.com/alibaba/DataX/blob/master/adswriter/doc/adswriter.md)|
|              | OSS        |     √     |     √     |[读](https://github.com/alibaba/DataX/blob/master/ossreader/doc/ossreader.md) 、[写](https://github.com/alibaba/DataX/blob/master/osswriter/doc/osswriter.md)|
|              | OCS        |     √     |     √     |[读](https://github.com/alibaba/DataX/blob/master/ocsreader/doc/ocsreader.md) 、[写](https://github.com/alibaba/DataX/blob/master/ocswriter/doc/ocswriter.md)|
| NoSQL数据存储    | OTS        |     √     |     √     |[读](https://github.com/alibaba/DataX/blob/master/otsreader/doc/otsreader.md) 、[写](https://github.com/alibaba/DataX/blob/master/otswriter/doc/otswriter.md)|
|              | Hbase0.94  |     √     |     √     |[读](https://github.com/alibaba/DataX/blob/master/hbase094xreader/doc/hbase094xreader.md) 、[写](https://github.com/alibaba/DataX/blob/master/hbase094xwriter/doc/hbase094xwriter.md)|
|              | Hbase1.1   |     √     |     √     |[读](https://github.com/alibaba/DataX/blob/master/hbase11xreader/doc/hbase11xreader.md) 、[写](https://github.com/alibaba/DataX/blob/master/hbase11xwriter/doc/hbase11xwriter.md)|
|              | Phoenix4.x   |     √     |     √     |[读](https://github.com/alibaba/DataX/blob/master/hbase11xsqlreader/doc/hbase11xsqlreader.md) 、[写](https://github.com/alibaba/DataX/blob/master/hbase11xsqlwriter/doc/hbase11xsqlwriter.md)|
|              | Phoenix5.x   |     √     |     √     |[读](https://github.com/alibaba/DataX/blob/master/hbase20xsqlreader/doc/hbase20xsqlreader.md) 、[写](https://github.com/alibaba/DataX/blob/master/hbase20xsqlwriter/doc/hbase20xsqlwriter.md)|
|              | MongoDB    |     √     |     √     |[读](https://github.com/alibaba/DataX/blob/master/mongoreader/doc/mongoreader.md) 、[写](https://github.com/alibaba/DataX/blob/master/mongowriter/doc/mongowriter.md)|
|              | Hive       |     √     |     √     |[读](https://github.com/alibaba/DataX/blob/master/hdfsreader/doc/hdfsreader.md) 、[写](https://github.com/alibaba/DataX/blob/master/hdfswriter/doc/hdfswriter.md)|
| 无结构化数据存储     | TxtFile    |     √     |     √     |[读](https://github.com/alibaba/DataX/blob/master/txtfilereader/doc/txtfilereader.md) 、[写](https://github.com/alibaba/DataX/blob/master/txtfilewriter/doc/txtfilewriter.md)|
|              | FTP        |     √     |     √     |[读](https://github.com/alibaba/DataX/blob/master/ftpreader/doc/ftpreader.md) 、[写](https://github.com/alibaba/DataX/blob/master/ftpwriter/doc/ftpwriter.md)|
|              | HDFS       |     √     |     √     |[读](https://github.com/alibaba/DataX/blob/master/hdfsreader/doc/hdfsreader.md) 、[写](https://github.com/alibaba/DataX/blob/master/hdfswriter/doc/hdfswriter.md)|
|              | Elasticsearch       |         |     √     |[写](https://github.com/alibaba/DataX/blob/master/elasticsearchwriter/doc/elasticsearchwriter.md)|
| 时间序列数据库 | OpenTSDB | √ |  |[读](https://github.com/alibaba/DataX/blob/master/opentsdbreader/doc/opentsdbreader.md)|
|  | TSDB | | √ |[写](https://github.com/alibaba/DataX/blob/master/tsdbwriter/doc/tsdbhttpwriter.md)|


# 3 Quick Start
## 3.1 下载
获取下载包有两种方式，一种是直接下载DataX工具包、一种就是直接编译
* 直接下载DataX工具包
需要等待一段时间，大概771MB
```bash
wget http://datax-opensource.oss-cn-hangzhou.aliyuncs.com/datax.tar.gz
tar -zxf datax.tar.gz

```

* 源码编译
```bash
git clone git@github.com:alibaba/DataX.git
cd  {DataX_source_code_home}
mvn -U clean package assembly:assembly -Dmaven.test.skip=true
# 打包成功后的DataX包位于 {DataX_source_code_home}/target/datax/datax/ 

```

## 3.2 配置
获取配置模板：
```bash
#{YOUR_DATAX_HOME}/bin/datax.py -r {YOUR_READER} -w {YOUR_WRITER}
#例如：
{YOUR_DATAX_HOME}/bin/datax.py -r hdfsreader -w hdfswriter

```

如下为一个模板：
```json
{
    "job": {
        "content": [
            {
                "reader": {
                    "name": "hdfsreader",
                    "parameter": {
                        "column": [],
                        "defaultFS": "",
                        "encoding": "UTF-8",
                        "fieldDelimiter": ",",
                        "fileType": "orc",
                        "path": ""
                    }
                },
                "writer": {
                    "name": "hdfswriter",
                    "parameter": {
                        "column": [],
                        "compress": "",
                        "defaultFS": "",
                        "fieldDelimiter": "",
                        "fileName": "",
                        "fileType": "",
                        "path": "",
                        "writeMode": ""
                    }
                }
            }
        ],
        "setting": {
            "speed": {
                "channel": ""
            }
        }
    }
}
```

* **path**：要读取的文件路径，如果要读取多个文件，可以使用正则表达式"*"，注意这里可以支持填写多个路径。
* **defaultFS**：Hadoop hdfs文件系统namenode节点地址
* **fileType**：文件的类型，目前只支持用户配置为"text"、"orc"、"rc"、"seq"、"csv"。**HdfsReader则会只读取用户配置的类型的文件，忽略路径下其他格式的文件**
* **column**：读取字段列表，type指定源数据的类型，index指定当前列来自于文本第几列(以0开始)，value指定当前类型为常量，不从源头文件读取数据，而是根据value值自动生成对应的列。 
* **fieldDelimiter**：读取的字段分隔符。**HdfsReader在读取textfile数据时，需要指定字段分割符，如果不指定默认为','，HdfsReader在读取orcfile时，用户无需指定字段分割符**
* **encoding**：读取文件的编码配置。默认值`utf-8`
* **nullFormat**：文本文件中无法使用标准字符串定义null(空指针)，DataX提供nullFormat定义哪些字符串可以表示为null。
* **haveKerberos**：是否有Kerberos认证，默认false。例如如果用户配置true，则配置项kerberosKeytabFilePath，kerberosPrincipal为必填。
    * kerberosKeytabFilePath： Kerberos认证 keytab文件路径，绝对路径
    * kerberosPrincipal：Kerberos认证Principal名，如xxxx/hadoopclient@xxx.xxx 
* **compress**：当fileType（文件类型）为csv下的文件压缩方式，目前仅支持 gzip、bz2、zip、lzo、lzo_deflate、hadoop-snappy、framing-snappy压缩；
* **hadoopConfig**：hadoopConfig里可以配置与Hadoop相关的一些高级参数，比如HA的配置。
* **csvReaderConfig**：

             




## 3.3 启动
```bash
{YOUR_DATAX_DIR_BIN}/datax.py ./stream2stream.json

```

# 4 数据通道
## 4.1 HdfsReader 插件
HdfsReader需要**Jdk1.7及以上版本**的支持。

目前HdfsReader支持的文件格式如下，且文件内容存放的必须是一张逻辑意义上的二维表。
* textfile（text） 
* orcfile（orc）
* rcfile（rc）
* sequence file（seq）
* 普通逻辑二维表（csv）类型格式的文件

textfile是Hive建表时默认使用的存储格式，数据不做压缩，本质上textfile就是以文本的形式将数据存放在hdfs中，对于DataX而言，HdfsReader实现上类比TxtFileReader，有诸多相似之处。

orcfile，它的全名是Optimized Row Columnar file，是对RCFile做了优化。据官方文档介绍，这种文件格式可以提供一种高效的方法来存储Hive数据。

HdfsReader利用Hive提供的OrcSerde类，读取解析orcfile文件的数据。

## 4.2 HdfsWriter 插件
HdfsWriter提供向HDFS文件系统指定路径中写入TEXTFile文件和ORCFile文件,文件内容可与hive中表关联。

HdfsWriter仅支持textfile和orcfile两种格式的文件，且文件内容存放的必须是一张逻辑意义上的二维表;



datax-HdfsReader&HdfsWriter.md



