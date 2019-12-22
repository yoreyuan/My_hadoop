
# 1 环境
系统： CentOS Linux release 7.6.1810 (Core)
内存： 系统内存是 32/32/16，但在测试时还有其它业务运行，可用内存大概都仅有 50% 空间
磁盘： 空间充足
CPU：  Intel Xeon E3

在测试时最好申请一个资源充足的干净的环境进行测试，否则测试（因为有些很极端的测试）过程会报一些异常，如：
```
Code: 241. DB::Exception: Received from cdh3:19000. DB::Exception: Memory limit (for query) exceeded: would use 9.31 GiB (attempt to allocate chunk of 4217780 bytes), maximum: 9.31 GiB.
```

因此本次将ClickHouse的`min_part_size`配置为 5GB，
```
    <clickhouse_compression>
        <case>
            <min_part_size>5368709120</min_part_size>
            <min_part_size_ratio>0.01</min_part_size_ratio>
            <method>lz4</method>
        </case>
    </clickhouse_compression>
```

# 2 支持的最大列数
* Kudu + Impala ：不能超过 300 列
* ClickHouse： 10000列依然支持

# 3 超大宽表数据插入与查询
## 3.1 Kudu + Impala
Kudu只支持最大300列的表，我们按照最大列生成1W条的数据，通过先创建Impala表，将数据上传到HDFS，通过Location的位置，我们将数据快速导入到Impala表，
然后再通过 `CREATE TABLE 表名 PRIMARY KEY (主键字段) PARTITION BY HASH (要HASH的字段) PARTITIONS 分区数 STORED AS KUDU AS SELECT  ……` 方式创建Kudu表并将数据载入。
通过查询发现确实比Hive快很多，但是如果数据量很大时，第一次查询需要的时间更长些，其它因为Kudu是列式存储，增加太多字段对查询效率的影响并不是很大。 

## 3.2 ClickHouse
我们创建1w列时程序依然没有报错，还能继续添加字段。我们以1w列的打宽表为例，生成1w行1w列数据文件，导入ClickHouse，在导入时因为字段较大，如果在一次查询中指定太多字段时有内存不够的风险，
因此当空间较小时，指定`--max_memory_usage 0`取消内存限制，同时是一个列式存储数据库，查询效率也是很快速。
执行相同的查询，在列数比Kudu+Impala多的情况下，查询时间依然比Kudu+Impala方案要快。
在都以JDBC方式添加列时，ClickHouse 也是要比 Impala + Kudu 方式快很多。

# 4 更新数据
在使用SQL方式情况下，使用UPDATE批量更新时因为集群问题出现了超时异常，我们暂时略过，建议在比较稳定的环境下再测这部分。Impala + Kudu 暂时不计入本次测试。

为了和 Kudu + Impala 同样的三节点服务的方式，在 ClickHouse 中创建的测试表都是以分布式方式的。ClickHouse 使用 INSERT 方式根据主键更新插入，从最终的结果可以看到，
更新插入 3000w 条数据用时 8.153 sec ，更新插入 600w 条数据用时 1.682 sec，速度相比于 Impala + Kudu 的超时现象来说，已经非常快速。更快速的是其聚合统计查询。
通过两表的指标字段的 SUM 值来看，其结果基本相等。


# 5 总结
Impala + Kudu 和 ClickHouse 都可以支持数据的导入和导出，也支持对数据的 UPDATE 和 DELETE，但其各自的语法有所差异。
Impala + Kudu 对集群的资源和性能以及稳定性有要求，当数据量大时会出现超时现象。
ClickHouse也可以以集群方式部署，但是在创建分布式表时有点繁琐。

在集群环境基本相同的条件下（Impala + Kudu 三个节点，ClickHouse分布式表三个分片表）总体上看Impala更适合于海量数据的查询，当对数据频繁修改时请保证集群有充足的资源和性能。ClickHouse 可以以集群方式部署，但是集群的稳定性和数据的高可用性需要手动维护，
如果有数据的导入和超出操作，对文件的类型支持有限，但是其导入和导出的性能还算令人满意，ClickHouse数据的更新方式有两种：根据条件单表字段的跟新和根据主键更新插入，
以分布式表方式在不进行表关联时其聚合统计性能非常高。在对 ClickHouse 有更好的管理能力的情况下，ClickHouse是一个不错的选择。




