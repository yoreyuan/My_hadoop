Docker 快速构建
------------


```bash
# 拉取一个镜像
docker pull registry.cn-beijing.aliyuncs.com/jokerk8s/selfimage:mycentos-v0.1

# 从镜像中运行
docker run -itd --privileged  -p 3722:22 -p 8888:80 -p 33060:3306  registry.cn-beijing.aliyuncs.com/jokerk8s/selfimage:mycentos-v0.1


docker ps 
docker ps -a


# 启动 docker
docker start 容器ID

# 暂停 docker 服务
docker stop 容器ID

# 打包已经镜像
docker commit 容器ID name:tag
# 查看镜像
docker images
# 删除镜像
docker rmi -f 镜像ID

docker exec -it 容器ID /bin/bash


yum install mariadb-server -y
ss -lantu


# docker 重新启动之后需要重新导入环境变量
source /etc/profile


```



```bash

python / python

```

