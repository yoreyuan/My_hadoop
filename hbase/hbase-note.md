Apache HBase 
======

# 常用命令
* 进入hbase的shell： 
```bash
 hbase shell
```

* 查看有哪些表： 
```
hbase(main):001:0> list
```

* 创建表
语法：create <table>, {NAME => <family>, VERSIONS => <VERSIONS>}  或 create ‘表名’,’列族1’,’列族2’,…’列族n’ 
```
# 方式一
hbase(main):002:0> create 'test',{NAME => 'f1', VERSIONS => 2},{NAME => 'f2', VERSIONS => 2} 
# 方式二
hbase(main):003:0> create 'test','info1','info2'
```

* 描述表： 
```
hbase(main):004:0> describe 'm_info'
```

* 表是否存在：
```
hbase(main):005:0> exists 'test' 
```

* 向表中添加数据：
```
hbase(main):010:0> put 'test','002','info1:property','0.00'
```

* 扫描整个表数据
```
hbase(main):011:0> scan 'test'
```

* 查询某一行
```
hbase(main):013:0> get 'test','001'
```

* 查询某列簇数据
```
# 如果列簇不存在，会抛出：ERROR: org.apache.hadoop.hbase.regionserver.NoSuchColumnFamilyException: 
hbase(main):016:0> get 'test','001','info1'
```

* 查询摸个版本数据
```
hbase(main):027:0>  get 'test', '001', {COLUMN => 'info1:name',TIMESTAMP => 1563127060156}
```

* 统计记录数
```
hbase(main):012:0> count 'test'
```

* 删除列簇下某一列
```
hbase(main):013:0> delete 'test','001','info1:age'
```

* 删除某列簇
```
hbase(main):014:0> delete 'test','001','info2'
```

* 删除某一行
```
hbase(main):015:0> delete 'test','001'
```

* 清空表
```
hbase(main):016:0> truncate  'test'
```

* 删除表
```
# 停止表
hbase(main):032:0> disable 'test'
# 删除表
hbase(main):033:0> drop 'test'
```

