MySQL
-------

# blog

[Centos7环境下离线安装mysql 5.7 / mysql 8.0](https://blog.csdn.net/github_39577257/article/details/77433996)

[CDH 6.2.0 或 6.3.0 安装实战及官方文档资料链接 # 1.5 MySQL](https://blog.csdn.net/github_39577257/article/details/92471365#1.5)

[Windows/Mac系统Docker方式安装Mysql](https://blog.csdn.net/github_39577257/article/details/82955623)



# 命令
```sql
-- 查看 host 和 user 信息
select Host,User,authentication_string,password_expired,plugin from mysql.user;

-- 查看 某个用户权限
show grants for 'root'@'%';
-- 添加或修改 root 远程访问用户的密码
GRANT ALL PRIVILEGES ON *.* TO 'root'@'%' IDENTIFIED BY '123456';

-- 添加一个远程用户
create user 'scm'@'%' identified by '远程访问的密码'

-- 授权用户 select 查看权限
GRANT select ON test.emp TO 'scm'@'%';


```


