Tuning Apache Hive on Spark in CDH(CDH中调配 Apache Hive 运行在 Spark)
----
[cloudera 对应文档原文](https://www.cloudera.com/documentation/enterprise/5-16-x/topics/admin_hos_tuning.html)

**最低角色要求**：配置程序（也由集群管理员、完全管理员提供

Hive 运行在 Spark 上比 Hive 运行在 MapReduce 提供更好的性能，同时提供相同的功能。 在Spark上运行Hive不需要更改用户查询。 
具体而言，完全支持用户定义的函数（UDF），并且大多数与性能相关的配置使用相同的语义。

本主题介绍如何在Spark上配置和调优Hive以获得最佳性能。 本主题假定您的群集由Cloudera Manager管理，并且您使用YARN作为Spark群集管理器。

以下各节中描述的示例假设有一个40主机的YARN群集，每个主机具有32个内核和120 GB内存。



