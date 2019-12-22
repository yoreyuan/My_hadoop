Nginx
----

# 安装
```bash
# 1 下载 Nginx 离线安装包
# 例如下载 Cento7 CPU指令为 x86版本的 
wget http://nginx.org/packages/mainline/centos/7/x86_64/RPMS/nginx-1.17.6-1.el7.ngx.x86_64.rpm

# 2 安装
rpm -ivh nginx-1.17.6-1.el7.ngx.x86_64.rpm

# 3 启动
systemctl start nginx

# 4 状态
systemctl status nginx

# 5 停止
#nginx -s stop
systemctl stop nginx

```

# 索引
## 自有的
```
    location ~ ^(/.*) {
        root /home/web/dist;
        autoindex on;                   # 打开目录浏览
        autoindex_localtime on;         # 改为on后，显示的文件时间为文件的服务器时间。
        autoindex_exact_size off;       # 默认为on，显示出文件的确切大小，单位是bytes。改为off后，显示出文件的大概大小，单位是kB或者MB或者GB。
        charset utf-8,gbk;              # 解决中文乱码问题。
        
        #fancyindex on;
        #fancyindex_exact_size off;
        #fancyindex_localtime on;
        #fancyindex_footer "@yoreyuan"
     }

```

## nginx+fancy
[Fancy Index](https://www.nginx.com/resources/wiki/modules/fancy_index/)
```bash
yum install gcc-c++
#yum install gcc gcc-c++ autoconf automake
#yun -y install zlib zlib-devel openssl openssl-devel pcre pcre-devel （安装依赖zlib、openssl和pcre）

# 安装pcre
wget https://jaist.dl.sourceforge.net/project/pcre/pcre/8.43/pcre-8.43.tar.gz
tar -zxf pcre-8.43.tar.gz
cd pcre-8.43
./configure
make
make install

# 开始安装 fancy
wget https://github.com/aperezdc/ngx-fancyindex/archive/master.zip
unzip master.zip
cd ngx-fancyindex-master

wget https://tengine.taobao.org/download/tengine-2.1.2.tar.gz
tar -zxf tengine-2.1.2.tar.gz
cd tengine-2.1.2/
# 如果报： ./configure: error: SSL modules require the OpenSSL library.
# yum -y install openssl openssl-devel
./configure --add-module=../
make
make install

git clone https://github.com/aperezdc/ngx-fancyindex.git ngx-fancyindex
cd ngx-fancyindex/
./configure --add-module=/etc/nginx/ngx-fancyindex
make
sudo make install

```


