TPC
----
[Download](http://www.tpc.org/tpc_documents_current_versions/current_specifications.asp)
[TPC BENCHMARK ™ DS Standard Specification Version 2.11.0 .PDF](http://www.tpc.org/tpc_documents_current_versions/pdf/tpc-ds_v2.11.0.pdf)

# 1 概述

事务处理性能委员会（ Transaction Processing Performance Council ），是由数10家会员公司创建的非盈利组织，总部设在美国。
TPC的成员主要是计算机软硬件厂家，而非计算机用户，它的功能是制定商务应用基准程序（Benchmark）的标准规范、性能和价格度量，并管理测试结果的发布。
TPC不给出基准程序的代码，而只给出基准程序的标准规范（Standard Specification）。

针对数据库不同的使用场景TPC组织发布了多项测试标准，其中被业界广泛接受和使用的有[TPC-C](http://www.tpc.org/tpcc/default.asp) 、[TPC-H](http://www.tpc.org/tpch/default.asp)和
[TPC-DS](http://www.tpc.org/tpcds/default.asp)。
TPC-C于1992年7月获得批准，是针对OLTP的基准测试。
TPC-H是决策支持的基准测试，它由一套面向业务的即席查询和并发数据修改组成，它是一款面向商品零售业的决策支持系统测试基准。
TPC-DS则是针对OLAP的测试，下面重点介绍下TPC-DS。

> The TPC Benchmark DS (TPC-DS) is a decision support benchmark that models several generally applicable aspects of a decision support system, including queries and data maintenance. The benchmark provides a representative evaluation of performance as a general purpose decision support system. A benchmark result measures query response time in single user mode, query throughput in multi user mode and data maintenance performance for a given hardware, operating system, and data processing system configuration under a controlled, complex, multi-user decision support workload. The purpose of TPC benchmarks is to provide relevant, objective performance data to industry users. TPC-DS Version 2 enables emerging technologies, such as Big Data systems, to execute the benchmark.

TPC-DS是一个决策支持的基本测试，它为决策支持系统的几个普遍适用的方面建模，包括数据查询和数据维护。TPC-DS基准测试作为通用决策支持系统提供了对性能的代表性评估。 
基准测试结果可在受控、复杂的多用户决策支持工作负载下测量单用户模式的查询响应时间，多用户模式下的查询吞吐量以及给定硬件、操作系统和数据处理系统配置的数据维护性能。 
TPC-DS基准测试的目的是向行业用户提供相关的客观性能数据。 TPC-DS版本2使新兴技术（例如大数据系统）能够执行基准测试。

# 2 TPC-DS表设计
这个基准测试会生成24张表，其中会有7张事实表和17张维度表，例如事实表Store_sales表为例，其表字段定义如下表：

Column | Datatype   | NULLs | Primary Key   | Foreign Key
---- | ---- | ---- | ----| ----
ss_sold_date_sk | identifier    | N | Y | d_date_sk
ss_sold_time_sk | identifier    |   |   | t_time_sk
ss_item_sk (1)  | identifier    |   |   | i_item_sk
ss_customer_sk  | identifier    |   |   | c_customer_sk
ss_cdemo_sk     | identifier    |   |   | cd_demo_sk
ss_hdemo_sk     | identifier    |   |   | hd_demo_sk
ss_addr_sk      | identifier    |   |   | ca_address_sk
ss_store_sk     | identifier    |   |   | s_store_sk
ss_promo_sk     | identifier    |   |   | p_promo_sk
ss_ticket_number (2)  |identifier | N | Y | 
ss_quantity           | integer      |  |   |  
ss_wholesale_cost     | decimal(7,2) |  |   |  
ss_list_price         | decimal(7,2) |  |   |  
ss_sales_price        | decimal(7,2) |  |   |  
ss_ext_discount_amt   | decimal(7,2) |  |   |  
ss_ext_sales_price    | decimal(7,2) |  |   |  
ss_ext_wholesale_cost | decimal(7,2) |  |   |  
ss_ext_list_price     | decimal(7,2) |  |   |  
ss_ext_tax            | decimal(7,2) |  |   |  
ss_coupon_amt         | decimal(7,2) |  |   |  
ss_net_paid           | decimal(7,2) |  |   |  
ss_net_paid_inc_tax   | decimal(7,2) |  |   |  
ss_net_profit         | decimal(7,2) |  |   |  

这部分可以看文档 [tpc-ds_v2.11.0.pdf](tpc-ds_v2.11.0.pdf) 第 22页



# 3 获取工具
```bash
#访问：
# 浏览器访问下面链接，填写个人信息（名、姓、公司、职位、国家、邮箱 等），务必要同意并完成机器验证，提交后下载链接会发送到邮箱
# http://www.tpc.org/TPC_Documents_Current_Versions/download_programs/tools-download-request.asp?bm_type=TPC-DS&bm_vers=2.11.0&mode=CURRENT-ONLY
wget http://www.tpc.org/tpc_documents_current_versions/temporary_download_files/42d6f585-7c65-469c-b8de-9bfe47b63d81-tpc-ds-tool.zip

mv 42d6f585-7c65-469c-b8de-9bfe47b63d81-tpc-ds-tool.zip TPC-2.11.0.zip

# 解压
unzip TPC-2.11.0.zip
cd v2.11.0rc2/

[root@cdh3 software]# cd v2.11.0rc2/
[root@cdh3 v2.11.0rc2]# ls
answer_sets  EULA.txt  query_templates  query_variants  specification  tests  tools

```

# 4 编译生成数据

```bash
# 1 进入到 tools
cd tools/

# 2 编译
# 如果没有C++编译环境，请先执行如下命令
# yum -y install gcc gcc-c++ libstdc++-devel bison byacc flex
make

# 3 编译完成之后，我们在tools目录下会看到：dsdgen、dsqgen
ls | grep ds

# 4 帮助信息
./dsdgen -help

# 5 使用如下命令生成 1G 数据
#  -DELIMITER   指定字段分隔符，默认是 |
#  - scale      指定生成的数据规模（单位是GB）
#  -parallel    将数据为 n 个块，值必须 >=2
#  -dir         数据输出的路径
#  -TERMINATE  末尾是否有分隔符，参数 Y或者N
./dsdgen -DELIMITER '|' -scale 1 -parallel 2 -TERMINATE N -dir /opt/tmp/data 

# 6 查看生成的数据大小，生成了923M的数据
[root@cdh3 tools]#  du -hd1 /opt/tmp/data
1.1G    /opt/tmp/data

# 7 准备好建表语句
#  建表相关的SQL文件在  v2.11.0rc2/tools 下
#  tpcds_ri.sql         创建表与表之间关系的sql语句
#  tpcds_source.sql     其中又 21 张表（本次不用这个表）
#     s_catalog_page
#     s_zip_to_gmt
#     s_purchase_lineitem
#     s_customer
#     s_customer_address
#     s_purchase
#     s_catalog_order
#     s_web_order
#     s_item
#     s_catalog_order_lineitem
#     s_web_order_lineitem
#     s_store
#     s_call_center
#     s_web_site
#     s_warehouse
#     s_web_page
#     s_promotion
#     s_store_returns
#     s_catalog_returns
#     s_web_returns
#     s_inventory
#     
#  tpcds.sql            创建25张表的sql语句
#     25 张表如下
#     dbgen_version
#     customer_address
#     customer_demographics
#     date_dim
#     warehouse
#     ship_mode
#     time_dim
#     reason
#     income_band
#     item
#     store
#     call_center
#     customer
#     web_site
#     store_returns
#     household_demographics
#     web_page
#     promotion
#     catalog_page
#     inventory
#     catalog_returns
#     web_returns
#     web_sales
#     catalog_sales
#     store_sales
#  
#  注意：每个数据库的建表语句可能会不同，因此需要根据这个建表脚本文件，修改为自己数据库的
#    例如如果是Hive，GigHub上也有根据这个生成的适合Hive的脚本，例如下面的两个项目
#    https://github.com/hortonworks/hive-testbench
#    https://github.com/krutivan/hive-benchmark
[root@cdh3 tools]# ls | grep .sql
tpcds_ri.sql
tpcds_source.sql
tpcds.sql

# 8 查询数据
#  查询数据的sql模板文件在 v2.11.0rc2/query_templates 下
#  有 query1.tpl 到 query99.tpl ，共 99 个SQL

# 9 测试用例的结果
#  在 v2.11.0rc2/answer_sets 下有上面每个query的测试用例的结果，可以在测试时作为参考

```


                        