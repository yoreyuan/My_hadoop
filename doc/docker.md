Docker
====

目录 |
:---- |
1. 介绍 |
2. 常用命令 |
3 spring中的使用


**********


# 1. 介绍
1、什么是Docker
  An open source project to pack,ship 
  run any application as a lightweight(轻量级的) container
  
Docker的三大核心概念：镜像、容器、仓库
* 镜像：类似虚拟机的镜像、用俗话说就是安装文件。
* 容器：类似一个轻量级的沙箱，容器是从镜像创建应用运行实例，可以将其启动、开始、停止、删除、而这些容器都是相互隔离、互不可见的。
* 仓库：类似代码仓库，是Docker集中存放镜像文件的场所。


# 2. 常用命令
1、Linux下安装
  $ sudo wget -qO- https://get.docker.com | sh
  -q标识输出要简单，O-标识标准输出，而不是输出到文件
  
  非授权用户，
  $ sudo usermod -aG docker xxx 
  把xxx用户添加到docker用户组中
  
  查看Docker信息
  $ docker info 
  
  问题1：Delta RPMs disabled because /usr/bin/applydeltarpm not installed.
  解决：yum install deltarpm 和 yum provides '*/applydeltarpm'
  
  问题2：Cannot connect to the Docker daemon at unix:///var/run/docker.sock. Is the docker daemon running?
  解决： Docker服务没有启动，启动Docker服务 service docker start

***** Centos 6.6下安装Docker
yum -y install docker-io
service docker start
chkconfig docker on
1，获取Centos镜像
   docker pull centos:latest
  
```
1、检查内核版本
# uname -r

2.添加yum仓库
# tee /etc/yum.repos.d/docker.repo <<-'EOF'

[dockerrepo]
name=Docker Repository
baseurl=https://yum.dockerproject.org/repo/main/centos/$releasever/
enabled=1
gpgcheck=1
gpgkey=https://yum.dockerproject.org/gpg
EOF

3、安装
# yum install -y docker-engine

4、验证
# docker version

5、启动Docker
# systemctl start docker.service

6、验证服务是否启动成功
# service docker start
# docker version
如果看到Client和Server都有信息，标识安装成功

7、设置开机启动
# sudo systemctl enable docker

```

* 2 Docker命令
运行一个docker容器
docker run xx echo hello docker

docker pull
    获取image
docker images
    查看docker本地image
docker build
    创建一个image

docker run
    列出运行的container
docker run -p 8080:80 -d 镜像
    -p将docker的端口80映射到真实机的端口8080
    -d程序以守护进程的方式运行

docker ps
    查看docker中程序运行的进程信息
docker ps -a
    列出容器所有的进程信息（包含历史的）

docker cp 本地文件.jar 容器id://opt/yore/jar
    将本地的文件拷贝到容器id下的目录

上面的指令只是临时保存进容器了，如果长久保存，
docker commit -m "消息内容" 容器id 容器名字

docker stop 容器id
    停止docker的容器id
docker rm 进程号
    删除进程信息

删除某个images
    docker rmi 镜像id
    
* 3、 Dockerfile
 第一个Dockerfile
 MAINTAINER xbf
 CMD echo 'hello docker'
```
# vim Dockerfile
然后输入： 
FROM alpine:latest
MAINTAINER xbf
CMD echo 'hello docker'

保存退出
```
使用docker构建
docker build -t hello_docker .
  -t 标签名
  
查看是否生成了一个镜像
docker images hello_docker
```
[root@node2 docker]# docker build -t hello_docker .
Sending build context to Docker daemon  2.048kB
Step 1/3 : FROM alpine:latest
 ---> 11cd0b38bc3c
Step 2/3 : MAINTAINER xbf
 ---> Using cache
 ---> 08ae72e4f605
Step 3/3 : CMD echo 'hello docker'
 ---> Using cache
 ---> f350a7ad7d64
Successfully built f350a7ad7d64
Successfully tagged hello_docker:latest

[root@node2 docker]# docker images hello_docker
REPOSITORY          TAG                 IMAGE ID            CREATED              SIZE
hello_docker        latest              f350a7ad7d64        About a minute ago   4.41MB

[root@node2 docker]# docker run hello_docker
hello docker
```

 第二个Dockerfile
  
```
# vim Dockerfile
FROM ubuntu
MAINTAINER yore
RUN sed -i 's/archive.ubuntu.com/mirrors.ustc.edu.cn/g' /etc/apt/sources.list
RUN apt-get update
RUN apt-get install -y nginx
COPY index.html /var/www/html
ENTRYPOINT ["/usr/sbin/nginx","-g","daemon off;"]
EXPOSE 80

# docker build -t yore/hello-nginx .

# docker run -d -p 80:80 yore/hello-nginx

# curl http:localhost:80

``` 

 Dockerfile语法小结
FROM        base image
RUN         执行命令
ADD         添加文件
COPY        拷贝文件
CMD         执行命令
EXPOSE      暴露端口
WORKDIR     指定路径
MAINTAINER  维护者
ENV         设定环境变量
ENTRYPOINT  容器入口
USER        指定用户
VOLUME      mount point

 镜像分层
Dockerfile中的每一行都会产生一个新层（都会产生一个id）


 4、存储
Volume
  提供独立于容器之外的持久化存储
  
volume操作
```
# docker run -d --name nginx -v /usr/share/nginx/html nginx

# docker inspect nginx

# ls /var/lib/docker/volumes/17c2a22eaca7c4e9fe5096dccc35d86417ddbe79a72c41f6d43f53372bd28717/_data
mack需要进入
screen ~/Library/Containers/com.docker.docker/Data/com.docker.driver.amd64-linux/tty

# cd /var/lib/docker/volumes/17c2a22eaca7c4e9fe5096dccc35d86417ddbe79a72c41f6d43f53372bd28717/_data
# echo "2018-08-22" > index.html

[root@node2 _data]# docker exec -it nginx /bin/bash
root@1829451c1692:/# cd /usr/share/nginx/html/
root@1829451c1692:/usr/share/nginx/html# ls
50x.html  index.html
root@1829451c1692:/usr/share/nginx/html# cat index.html
2018-08-22

```

```
# docker run -v $PWD/code:/var/www/html nginx


# docker run -p 80:80 -d -v $PWD/html:/usr/share/nginx/html nginx
把当前的html挂载到:/usr/share/nginx/html

```
 


# 3 spring中的使用



mvn clean package docker:build

将项目打好包，然后上传到服务器上，

编辑 `Dockerfile`
```
from registry.cn-hangzhou.aliyuncs.com/joint-union/java8
VOLUME /opt
ADD eureka-server-comm-1.0-SNAPSHOT.jar app.jar
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar"]
EXPOSE 8761
```

构建镜像
```bash
docker build -t eureka-server:latest .
```

查看构建的镜像：`docker images`

提交运行： `docker run --name eureka-server -d -p 8761:8761 -t eureka-server`



<br/><br/>
更多可查看我的博客 [Spring Cloud 项目中 Docker 的使用](https://blog.csdn.net/github_39577257/article/details/95250521)


