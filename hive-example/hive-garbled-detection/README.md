Hive乱码检测分析
==

# 一、自定义函数方法对乱码进行分析

## 1.1 编写代码

## 1.2 打包自定义函数代码

## 1.3 添加到 hive 中
以下操作进入Hive CLI操作  

1. 添加自定义jar包： `add jar path/yore_udf.jar`
2. 查看添加的jar包： `list jar`
3. 创建自定义的函数: 
```sql
create temporary function is_garbled as 'yore.IsGarbled';
create temporary function ratio_garbled as 'yore.RatioGarbled';
```

## 1.4 使用
```sql
--返回乱码的记录，并限制返回的条数
select * from t1 where
  select is_garbled(*) from t1
  limit 3;

--统计乱码行记录占总数据的比值
select ratio_garbled(*) from t1;

```

## 1.5 删除自定义函数
```sql
DROP TEMPORARY FUNCTION is_garbled;
DROP TEMPORARY FUNCTION ratio_garbled;
```


