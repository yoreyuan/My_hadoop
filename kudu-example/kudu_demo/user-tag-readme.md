Kudu 测试
--

# 集群信息

角色 | 主机 | 系统 | 内存 | 核数
---- | ---- | ---- | ---- | ----
Master| cdh3 | CentOS Linux release 7.4.1708 | 32 GB | 8
Tablet Server| cdh1 | CentOS Linux release 7.4.1708 | 16 GB | 4
Tablet Server| cdh2 | CentOS Linux release 7.4.1708 | 32 GB | 8
Tablet Server| cdh3 | CentOS Linux release 7.4.1708 | 32 GB | 8

# Kudu Version
```
kudu 1.7.0-cdh5.16.1
revision 4ad36868c5d782efb3eb5339c12a33b6824cce79
build type RELEASE
built by jenkins at 21 Nov 2018 22:13:38 PST on impala-ec2-pkg-centos-7-031c.vpc.cloudera.com
build id 2018-11-21_21-08-25
``` 

# 测试

## 1 当表为 5列时

### 1.1 创建表`tag_5`
```
createTable("tag_5", 5);
```

### 1.2 结果
item | 1w | 10w | 100w
---- | ---- | ---- | ----
kudu api insert | 1069 ms | - | -
kudu api scan | 365 ms | - | -
impala select | 1.59 s | - | -
impala count | 0.14 s | - | -



## 2 当表为 50 列时
### 2.1 创建表`tag_50`
```
createTable("tag_50", 50);
```

### 2.2 结果
item | 1w | 10w | 100w
---- | ---- | ---- | ----
kudu api insert | 1539 ms | - | -
kudu api scan | 657ms | - | -
impala count | 0.13 s | - | -


## 3 当表为 300 列时
**最多支持300列**  
官方文档的说明：[Known Limitations](https://kudu.apache.org/docs/schema_design.html#known-limitations)
![org.apache.kudu.client.NonRecoverableException: number of columns 305 is greater than the permitted maximum 300](src/main/resources/permitted-maximum-300.png)

### 3.1 创建表`tag_300`
```
createTable("tag_300", 300);
```

### 3.2 结果
item | 1w | 10w | 100w
---- | ---- | ---- | ----
kudu api insert | 4112 ms | - | -
kudu api scan | 2353 ms | - | -
impala count | 0.13 s | - | -


