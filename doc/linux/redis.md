Redis 
------

# 安装
```bash
# 1 下载
#  访问 http://download.redis.io/releases/，下载需要的版本
wget http://download.redis.io/releases/redis-5.0.7.tar.gz

# 2 解压redis
sudo tar -zxf redis-5.0.7.tar.gz -C /usr/local/

# 3 编译redis
cd /usr/local/redis-5.0.7
sudo make

# 4 配置环境变量
vim /etc/profile
# 添加如下配置，并立即生效

export REDIS_HOME=/usr/local/redis-5.0.7
export PATH=$PATH:$REDIS_HOME/src

```

配置 Redis。 `vim $REDIS_HOME/redis.conf`
```bash
# 1 拷贝一份配置文件
sudo cp $REDIS_HOME/redis.conf $REDIS_HOME/my_redis.conf

# 2 配置如下
sudo vim $REDIS_HOME/my_redis.conf

```

主要是开启密码认证和远程访问
```yaml
# 大概 79 行，注释掉，或者 改为 0.0.0.0 ，或者将允许访问的ip，以空格的方式添加到后面
#bind 127.0.0.1

port 6379

# 默认是no,改为yes，在后台启动
# By default Redis does not run as a daemon. Use 'yes' if you need it.
# Note that Redis will write a pid file in /var/run/redis.pid when daemonized.
daemonize yes

databases 16

# 大概在 235 行 。开启 redis 快照，允许持久化到磁盘
stop-writes-on-bgsave-error no

# 设置密码。大概在 507 行
requirepass dq******@!

```

启动 Redis 服务
```bash
# 因为已经配置了 daemonize yes，会直接以后台启动
$REDIS_HOME/src/redis-server  $REDIS_HOME/my_redis.conf

# 查看 Redis 服务
ps -ef|grep redis

```

# 常用命令
```bash
$REDIS_HOME/src/redis-cli  -h 127.0.0.1 -p 6379 

```

```sql
-- 如果有密码，先完成密码认证
127.0.0.1:6379> auth dq******@!
OK

-- 选择库
127.0.0.1:6379> select 3
OK

-- 添加一个表（key）。这里添加一个 hash (k-v)
--  Redis支持五种数据类型：
--    string（字符串），
--    hash（哈希）， (key-value)
--    list（列表），
--    set（集合）
--    zset(sorted set：有序集合)。
127.0.0.1:6379[3]> hset test key01 "hello world"
(integer) 1

-- 查看 表
127.0.0.1:6379[3]> keys *
1) "test"

-- 读取添加的值
127.0.0.1:6379[3]> hget test key01
"hello world"

-- 修改数据
-- 127.0.0.1:6379[3]> config set stop-writes-on-bgsave-error no
-- OK
127.0.0.1:6379[3]> hmset test key01 'hello world!'
OK
-- 查看修改结果
127.0.0.1:6379[3]> hget test key01
"hello world!"

-- 删除 表
127.0.0.1:6379[3]>  del 'test'
(integer) 1

-- 退出
127.0.0.1:6379[3]> quit

```
