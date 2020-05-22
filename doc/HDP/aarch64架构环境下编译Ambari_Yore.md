* 整个安装以这个官方文档为准：[鲲鹏大数据解决方案](https://support.huaweicloud.com/prtg-hdp-kunpengbds/kunpengambarihdp_02_0001.html)

* 通过华为的移植分析工具分析，对于 Ambari 2.7.3 的移植分析结果可以查看 [执行移植分析](https://support.huaweicloud.com/prtg-hdp-kunpengbds/kunpengambarihdp_02_0009.html)


# 目录
* 1 [前言](#1)
* 2 [环境](#2)
    + 2.1 [JDK](#2.1)
    + 2.2 [Maven](#2.2)
    + 2.3 [安装 Ant](#2.3)
    + 2.4 [sbt 安装](#2.4)
    + 2.5 [node.js](#2.5)
    + 2.6 [安装Phantomjs](#2.6)
    + 2.7 [搭建golang环境](#2.7)
    + 2.8 [搭建文件服务器](#2.8)
    + 2.9 [升级 cmake](#2.9)
    + 2.10 [编译leveldb](#2.10)
        - 2.10.1 [编译snappy-1.1.5](#2.10.1)
        - 2.10.2 [编译leveldb-1.20](#2.10.2)
        - 2.10.3 [编译leveldbjni](#2.10.3)
    + 2.11 [安装 Scala](#2.11)
* 3 [编译依赖](#3)
    + 3.1 [编译安装 frontend-maven-plugin](#3.1)
    + 3.2 [编译jline-2.11](#3.2)
    + 3.3 [编译jline-2.12](#3.3)
    + 3.4 [编译jline-2.12.1](#3.4)
    + 3.5 [编译jline-2.13](#3.5)
    + 3.6 [编译jline-2.14.3](#3.6)
    + 3.7 [编译leveldbjni-all-1.8.jar](#3.7)
    + 3.8 [编译netty-all-4.0.23.Final.jar](#3.8)
    + 3.9 [编译netty-all-4.0.29.Final](#3.9)
    + 3.10 [编译 lz4-java-1.2.0.jar](#3.10)
    + 3.11 [编译snappy-java-1.0.4.1](#3.11)
    + 3.12 [编译snappy-java-1.0.5](#3.12)
    + 3.13 [编译snappy-java-1.1.0.1](#3.13)
    + 3.14 [编译snappy-java-1.1.1.3](#3.14)
    + 3.15 [编译snappy-java-1.1.1.6](#3.15)
    + 3.16 [编译snappy-java-1.1.1.7](#3.16)
    + 3.17 [编译snappy-java-1.1.2.6](#3.17)
    + 3.18 [编译 snappy-1.1.7](#3.18)
    + 3.19 [编译安装netty-4.1.17](#3.19)
    + 3.20 [编译Hbase-shaded-netty-2.1.0](#3.20)
    + 3.21 [编译commons-crypto-1.0.0](#3.21)
    + 3.22 [编译scala-compiler-2.11.8](#3.22)
    + 3.23 [编译scala-compiler-2.11.12](#3.23)
    + 3.24 [编译scala-compiler-2.12.7](#3.24)
    + 3.25 [编译安装netty-4.0.52源码](#3.25)
    + 3.26 [编译安装netty-tcnative-parent-2.0.6.Final](#3.26)
    + 3.27 [编译安装netty-tcnative-parent-2.0.7.Final](#3.27)
    + 3.28 [编译Jansi-1.0.jar](#3.28)
    + 3.29 [编译 Jansi-1.4.jar](#3.29)
    + 3.30 [安装Protoc](#3.30)
* 4 [编译 Ambari](#4)
    + 4.1 [下载 Ambari 源码](#4.1) 
    + 4.2 [编译_posixsubprocess32.so](#4.2)
    + 4.3 [编译_speedups.so](#4.3)
    + 4.4 [编译 Phoenix-5.0.0](#4.4)
    + 4.5 [编译 hadoop-3.1.1](#4.5)
    + 4.6 [编译 hbase-2.1.0](#4.6)
    + 4.7 [编译安装 grafana-2.6.0](#4.7)
    + 4.8 [接着4.1继续完后后续Ambari 源码的编译](#4.8)
* 5 [汇总](#5)
    + 5.1 [资源路径](#5.1)
    + 5.2 [编译汇总目录表](#5.2)
* 6 [🎉镜像 🎉及连接](#6)

<br/>

**********

# <a id="1"></a>1 前言
大数据生态圈其实对 x86架构 支持是最友好的，但偶尔我们查看服务器信息时，如下图所示，可以看到服务器CPU 架构为 aarch64（属于 ARMv8架构的一种执行状态），例如[鲲鹏920](https://www.huaweicloud.com/kunpeng/product/kunpeng920.html)处理器，这样我们再到官网下载二进制部署包时可能发现没有对应的版本（并不是全部会受影响）。这也是本文的由来，在 aarch64架构系统上编译 Ambari 二进制部署包。
![在这里插入图片描述](https://img-blog.csdnimg.cn/20200520165054429.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L2dpdGh1Yl8zOTU3NzI1Nw==,size_16,color_FFFFFF,t_70#pic_center)

# <a id="2"></a>2 环境
```bash
yum install -y rpm-build
yum install -y gcc-c++
yum install -y python-devel
yum install -y git

```

环境变量配置如下，后面会依次安装一下软件，这里可以先提前配置上
```bash
### set maven environment
MAVEN_HOME=/opt/installed/apache-maven-3.5.4
PATH=$MAVEN_HOME/bin:$PATH

### set java environment
JAVA_HOME=/opt/installed/jdk8u222-b10
JRE_HOME=$JAVA_HOME/jre
CLASS_PATH=.:$JAVA_HOME/lib/dt.jar:$JAVA_HOME/lib/tools.jar:$JRE_HOME/lib
PATH=$PATH:$JAVA_HOME/bin:$JRE_HOME/bin

### set Ant environment
export ANT_HOME=/opt/installed/apache-ant-1.9.15
export PATH=$PATH:$ANT_HOME/bin

### set sbt environment
SBT_HOME=/opt/installed/sbt
export PATH=$PATH:$SBT_HOME/bin

### set phantomjs environment
PHANTOMJS_HOME=/opt/installed/phantomjs
PATH=$PHANTOMJS_HOME/bin:$PATH

### set node environment
NODE_HOME=/opt/installed/node-v8.6.0-linux-arm64
PATH=$NODE_HOME/bin:$PATH

### set golang environment
GOROOT=/opt/installed/go/
GOPATH=/opt/installed/go/path
PATH=$GOROOT/bin:$PATH

export JAVA_HOME JRE_HOME CLASS_PATH MAVEN_HOME GOROOT GOPATH  PHANTOMJS_HOME  PATH
```

配置完环境变量后执行（统一配置到系统环境变量）：`source /etc/profile`

部分的version信息如下图：
![part of version info](https://img-blog.csdnimg.cn/20200520175407890.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L2dpdGh1Yl8zOTU3NzI1Nw==,size_16,color_FFFFFF,t_70#pic_center)


## <a id="2.1"></a>2.1 JDK
```bash
wget https://github.com/AdoptOpenJDK/openjdk8-binaries/releases/download/jdk8u222-b10/OpenJDK8U-jdk_aarch64_linux_hotspot_8u222b10.tar.gz
tar -zxf OpenJDK8U-jdk_aarch64_linux_hotspot_8u222b10.tar.gz -C /opt/installed/

# 验证
java -version
```


## <a id="2.2"></a>2.2 Maven
[参考文档](https://support.huaweicloud.com/prtg-hdp-kunpengbds/kunpengambarihdp_02_0005.html)

* 配置 maven 镜像 `vim /opt/installed/apache-maven-3.5.4/conf/settings.xml`
* 务必配置上Maven 中央仓库地址（因为从2020-01-15号之后访问maven中央仅支持 https 方式访问）

```

<localRepository>/opt/maven_repo</localRepository>

<mirrors>
   <!-- 华为云镜像 -->
    <mirror>
        <id>huaweimaven</id>
        <name>huawei maven</name>
        <url>https://mirrors.huaweicloud.com/repository/maven/</url>
        <mirrorOf>central</mirrorOf>
    </mirror>
    <!-- 阿里云镜像 -->
    <mirror>
        <id>nexus-aliyun</id>
        <mirrorOf>central</mirrorOf>
        <name>Nexus aliyun</name>
        <url>http://maven.aliyun.com/nexus/content/groups/public</url>
    </mirror>
	
	<mirror>
      <id>central-repos1</id>
      <name>Central Repository 2</name>
      <url>https://repo1.maven.org/maven2/</url>
      <!-- 表示只为central仓库做镜像，如果想为所有的仓库做镜像那么可以改为 -->
      <mirrorOf>*</mirrorOf>
    </mirror>
    
</mirrors>
```
* 【注意1】连接 http://s3-ap-southeast-1.amazonaws.com/dynamodb-local-singapore/release 无法访问，暂时注释
* 【注意2】连接 http://mirrors.ibiblio.org/maven2 无法访问，暂时注释

```bash
wget https://archive.apache.org/dist/maven/maven-3/3.5.4/binaries/apache-maven-3.5.4-bin.tar.gz
tar -zxf apache-maven-3.5.4-bin.tar.gz -C /opt/installed/

# 验证
mvn -v
```

## <a id="2.3"></a>2.3 安装 Ant
```bash
wget https://mirrors.tuna.tsinghua.edu.cn/apache//ant/binaries/apache-ant-1.9.15-bin.tar.gz
tar -zxf apache-ant-1.9.15-bin.tar.gz -C /opt/installed/
/opt/installed/apache-ant-1.9.15

# 配置环境变量，前面已配置，可以略去

# 查看版本
ant -version

wget https://repo1.maven.org/maven2/org/apache/maven/maven-ant-tasks/2.1.3/maven-ant-tasks-2.1.3.jar -P $ANT_HOME/lib/

```

## <a id="2.4"></a>2.4 sbt 安装
```bash
# 访问 https://www.scala-sbt.org/download.html 页面下载合适的版本
wget https://piccolo.link/sbt-1.3.10.zip
unzip sbt-1.3.10.zip -d /opt/installed/

# 设置参数（在开头添加如下）
vim sbt

SBT_OPTS="-Xms512M -Xmx1536M -Xss1M -XX:+CMSClassUnloadingEnabled -XX:MaxPermSize=256M"
# java $SBT_OPTS -jar /opt/installed/sbt/bin/sbt-launch.jar

# 查看版本信息
# /opt/installed/sbt/bin/sbt --version
sbt -V

```


## <a id="2.5"></a>2.5 node.js
设置如下环境变量
```bash
wget http://nodejs.org/dist/v8.6.0/node-v8.6.0-linux-arm64.tar.gz
tar -zxf node-v8.6.0-linux-arm64.tar.gz -C /opt/installed/

# 验证
node -v
npm -v

```

```bash
# 前端包管理工具
npm install -g bower
# 基于node.js 的一个前端自动化构建工具
npm install -g gulp
# HTML5 构建工具
npm install -g brunch
```
![node.js 安装](https://img-blog.csdnimg.cn/20200520173842825.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L2dpdGh1Yl8zOTU3NzI1Nw==,size_16,color_FFFFFF,t_70#pic_center)

## <a id="2.6"></a>2.6 安装Phantomjs
为了编译加速最好将其替换为国内镜像（见下方），要不这个下载编译实在是慢。
```bash
# 也可以下载对应版本的源码包，然后 git init
#git clone https://github.com/ariya/phantomjs.git
git clone https://gitee.com/naivefool/phantomjs.git
cd phantomjs
git tag
git checkout 2.1.1
git submodule init
# 获取代码更新前更换为国内 gitee 上的连接（见下面配置）
git submodule update

# 先执行编译，之后会在 src/qt/qtbase/src/gui 目录中下生成 Makefile
python build.py

# 第一次编译可能报错：Error:Invalid operands (*UND* and *UND* sections) for '*'
# 修改 Makefile 。将-O3修改为-O2（大概在 16、17 行），重新启动编译即可
vim src/qt/qtbase/src/gui/Makefile

CFLAGS        = -pipe -O2 -fPIC -fvisibility=hidden -fno-exceptions -Wall -W -Wno-unused-parameter -Wno-main -D_REENTRANT $(DEFINES)
CXXFLAGS      = -pipe -O2 -fPIC -fvisibility=hidden -fvisibility-inlines-hidden -std=c++0x -fno-exceptions -Wall -W -D_REENTRANT $(DEFINES)

```
![修改 phantomjs 源码](https://img-blog.csdnimg.cn/20200520174132425.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L2dpdGh1Yl8zOTU3NzI1Nw==,size_16,color_FFFFFF,t_70#pic_center)
```bash
python build.py
cp -r ../phantomjs /opt/installed/
# 验证
phantomjs -v

```

当执行完 `git submodule init` 后，编辑当前项目 git 配置文件 `vim .git/config`
```bash
[core]
        repositoryformatversion = 0
        filemode = true
        bare = false
        logallrefupdates = true
[remote "origin"]
        fetch = +refs/heads/*:refs/remotes/origin/*
[branch "master"]
        remote = origin
        merge = refs/heads/master
[submodule "3rdparty-win"]
        url = https://gitee.com/naivefool/phantomjs-3rdparty-win.git
[submodule "qtbase"]
        url = https://gitee.com/naivefool/qtbase.git
[submodule "qtwebkit"]
        url = https://gitee.com/naivefool/qtwebkit.git
```

![phantomjs安装](https://img-blog.csdnimg.cn/2020052017401368.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L2dpdGh1Yl8zOTU3NzI1Nw==,size_16,color_FFFFFF,t_70#pic_center)

## <a id="2.7"></a>2.7 搭建golang环境
```bash
wget https://dl.google.com/go/go1.9.linux-arm64.tar.gz

tar -zxf go1.9.linux-arm64.tar.gz
mv go /opt/installed


# 手动在“${GOPATH}”下创建三个目录，这个目录后面安装 grafana 会用到
mkdir ${GOPATH}
cd ${GOPATH}
mkdir src bin pkg

# 验证
go version

```

## <a id="2.8"></a>2.8 搭建文件服务器
```bash
yum install -y httpd

systemctl start httpd.service
systemctl enable httpd.service
systemctl status httpd.service

```

## <a id="2.9"></a>2.9 升级 cmake
保证系统的 CMake 版本 3.9 或者更高版本
![cmake版本过低](https://img-blog.csdnimg.cn/20200520174313609.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L2dpdGh1Yl8zOTU3NzI1Nw==,size_16,color_FFFFFF,t_70#pic_center)
```bash
# 查看版本。
cmake --version
# 移除旧版本
sudo yum remove cmake cmake-data

wget https://cmake.org/files/v3.9/cmake-3.9.2.tar.gz
tar -zxf cmake-3.9.2.tar.gz -C /opt/installed/
cd /opt/installed/cmake-3.9.2
./configure
make && make install

cmake    
cd /usr/bin/
ln -s /opt/installed/cmake-3.9.2/bin/cmake cmake
ln -s /opt/installed/cmake-3.9.2/bin/cpack cpack
ln -s /opt/installed/cmake-3.9.2/bin/ctest ctest

cmake --version

```

## <a id="2.10"></a>2.10 编译leveldb
LevelDB 是一个在Google 编写的快速键值存储库，它提供从字符串键到字符串值的有序映射。
关于Leveldb的更多信息请访问[官方链接](https://github.com/google/leveldb)。

编译 leveldbjni-all-1.8.jar 之前，需要先将 snappy 和 leveldb 编译成静态库，
再将两个静态库编译进 leveldbjni.so 里，最后生成leveldbjni-all-1.8.jar压缩包。

编译leveldbjni-all-1.8.jar之前，需要先将snappy和leveldb编译成静态库，再将两个静态库编译进leveldbjni.so里，最后生成leveldbjni-all-1.8.jar压缩包。

参考：https://bbs.huaweicloud.com/forum/thread-22684-1-1.html 、https://support.huaweicloud.com/prtg-tpdl-kunpengbds/kunpengleveldbjniall_02_0003.html

### <a id="2.10.1"></a>2.10.1 编译snappy-1.1.5
```bash
wget https://github.com/google/snappy/archive/1.1.5.tar.gz
tar -zxvf 1.1.5.tar.gz
cd snappy-1.1.5

./autogen.sh
./configure --with-pic
make -j20

# 编译完成后，将在“snappy-1.1.5/.libs”目录下生成libsnappy.so和libsnappy.a文件，将libsnappy.a文件拷贝到上一级目录
# 将静态库拷贝到对应目录，是为了后续将静态库编译进leveldbjni.so做准备。
cp .libs/libsnappy.a ./
export SNAPPY_HOME=`pwd`

```


### <a id="2.10.2"></a>2.10.2 编译leveldb-1.20
* 【注意】重点是根据文档截图，将已提交的 patch 合并到此版本代码中
```bash
wget https://github.com/google/leveldb/archive/v1.20.tar.gz
tar -zxvf v1.20.tar.gz 
cd leveldb-1.20

# 修改文件build_detect_platform，按照下图修改250至252行，其中第250-251行是修改原行内容，而第252行是新增行。
vim build_detect_platform
#> 
echo "PLATFORM_CCFLAGS=$PLATFORM_CCFLAGS $PLATFORM_SHARED_CFLAGS" >> $OUTPUT
echo "PLATFORM_CXXFLAGS=$PLATFORM_CXXFLAGS $PLATFORM_SHARED_CFLAGS" >> $OUTPUT
echo "PLATFORM_SHARED_CFLAGS=" >> $OUTPUT


# 根据 https://support.huaweicloud.com/prtg-tpdl-kunpengbds/kunpengleveldbjniall_02_0006.html ，将相应的patch合入代码，再执行编译
#请严格按照 git 上的两个 patch 合并
#https://github.com/Xingwd/leveldb/commit/abb05abdbd9a92440e41a33704fba783818929d4
#https://github.com/Xingwd/leveldb/commit/e521f3c4a0f92ba01a3b2a077517507c021a8cea
make -j20

export LEVELDB_HOME=`pwd`
cp out-static/libleveldb.a ./

```

### <a id="2.10.3"></a>2.10.3 编译leveldbjni
详情可以查看 https://support.huaweicloud.com/prtg-tpdl-kunpengbds/kunpengleveldbjniall_02_0003.html

```bash
git clone https://github.com/fusesource/leveldbjni.git
cd leveldbjni
export LEVELDBJNI_HOME=`pwd`

# 请按照文档进行修改

sed -i 's/99-master-SNAPSHOT/1.8/g' `find . -name pom.xml`

# 编译之前先按照官方文档的故障排除，修复可能存在的问题
# https://support.huaweicloud.com/prtg-tpdl-kunpengbds/kunpengleveldbjniall_02_0005.html
mvn clean package -P download -P linux64-aarch64 -DskipTests
# 如果没有其他问题，可以 install 到本地仓库

# 查看编译的结果
find ./ -name *.jar
#./leveldbjni-all/target/leveldbjni-all-1.8.jar
#./leveldbjni-linux64-aarch64/target/leveldbjni-linux64-aarch64-1.8.jar
#./leveldbjni-linux64/target/leveldbjni-linux64-1.8.jar
#./leveldbjni/target/leveldbjni-1.8.jar
#./leveldbjni/target/leveldbjni-1.8-tests.jar
#./leveldbjni/target/leveldbjni-1.8-sources.jar


# 验证 leveldbjni-all-1.8.jar
# 解压查看该jar包对应的目录so的CPU架构是否正确
jar -xvf leveldbjni-all-1.8.jar
# 验证libleveldbjni.so里面是否包含了libsnappy.a和libleveldb.a的静态库，而不是链接其动态库。
# 使用ldd命令查看，从结果来看，不包含snappy和leveldb模块的动态库链接
ldd ./META-INF/native/aarch64/libleveldbjni.so
ldd ./META-INF/native/linux64/libleveldbjni.so
# 选择leveldb.so中的某个函数，查看leveldbjni.so的符号表，检查该so中是否存在对应函数
nm ./META-INF/native/aarch64/libleveldbjni.so | grep ResumeCompactions
nm ./META-INF/native/linux64/libleveldbjni.so | grep ResumeCompactions

```
![文件信息](https://img-blog.csdnimg.cn/20200520175004405.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L2dpdGh1Yl8zOTU3NzI1Nw==,size_16,color_FFFFFF,t_70#pic_center)


## <a id="2.11"></a>2.11 安装 Scala

```bash
# 下载页面 https://www.scala-lang.org/download/all.html
wget https://downloads.lightbend.com/scala/2.11.8/scala-2.11.8.tgz

tar -zxvf scala-2.11.8.tgz -C /opt/installed/

# 在环境变量中添加如下，并使其生效
### set sbt environment
SCALA_HOME=/opt/installed/scala-2.11.8
export PATH=$PATH:$SCALA_HOME/bin


# 安装Protoc 之后，执行下面查看
scala -version

```

<br/><br/>

# <a id="3"></a>3 编译依赖
Checkso工具为移植性分析工具，该工具主要用于扫描安装包中是否包含了X86的jar包/so/可执行程序等。
* 【Checkso】为华为内部使用工具，暂无法下载。
* 移植分析的一个示例 [分析Hadoop](https://support.huaweicloud.com/prtg-cdh-kunpengbds/kunpengbds_02_0011.html)

* [可参考资料](https://support.huaweicloud.com/prtg-hdp-kunpengbds/kunpengambarihdp_02_0020.html)

* 【注意】如果是复制的官方文档，务必将`<repositorys>`标签改为` <repositories>`。
```xml
    <repositories>
        <repository>
            <id>kunpengmaven</id>
            <name>kunpeng maven</name>
            <url>https://mirrors.huaweicloud.com/kunpeng/maven</url>
        </repository>
    </repositories>
````

## <a id="3.1"></a>3.1 编译安装 frontend-maven-plugin
```bash
git clone https://github.com/eirslett/frontend-maven-plugin.git
cd frontend-maven-plugin/
git tag
#git checkout -b 0.0.16 frontend-plugins-0.0.16
# git checkout frontend-plugins-1.9.1
# wget https://repo1.maven.org/maven2/org/apache/maven/plugins/maven-clean-plugin/2.5/maven-clean-plugin-2.5.jar
# mvn install:install-file -Dfile=maven-clean-plugin-2.5.jar -DgroupId=org.apache.maven.plugins -DartifactId=maven-clean-plugin -Dversion=2.5 -Dpackaging=jar
git checkout frontend-plugins-0.0.16
mvn clean -DskipTests install
```

## <a id="3.2"></a>3.2 编译jline-2.11
```bash
wget https://github.com/jline/jline2/archive/jline-2.11.tar.gz
tar -zxf jline-2.11.tar.gz
cd jline2-jline-2.11
#修改pom.xml，添加鲲鹏maven仓库
# mvn package
mvn install

```

## <a id="3.3"></a>3.3 编译jline-2.12
```bash
wget https://github.com/jline/jline2/archive/jline-2.12.tar.gz
tar -zxf jline-2.12.tar.gz
cd jline2-jline-2.12
#修改pom.xml，添加鲲鹏maven仓库
# mvn package
mvn install

```

## <a id="3.4"></a>3.4 编译jline-2.12.1
```bash
wget https://github.com/jline/jline2/archive/jline-2.12.1.tar.gz
tar -zxf jline-2.12.1.tar.gz
cd jline2-jline-2.12.1
#修改pom.xml，添加鲲鹏maven仓库
# mvn package
mvn install

```

## <a id="3.5"></a>3.5 编译jline-2.13
```bash
wget https://github.com/jline/jline2/archive/jline-2.13.tar.gz
tar -zxf jline-2.13.tar.gz
cd jline2-jline-2.13
#修改pom.xml，添加鲲鹏maven仓库
# mvn package -DskipTests -Dmaven.javadoc.skip=true
mvn install

```

## <a id="3.6"></a>3.6 编译jline-2.14.3
```bash
wget https://github.com/jline/jline2/archive/jline-2.14.3.tar.gz
tar -zxf jline-2.14.3.tar.gz
cd jline2-jline-2.14.3
#修改pom.xml，添加鲲鹏maven仓库
# mvn package -DskipTests -Dmaven.javadoc.skip=true
mvn install -Dmaven.javadoc.skip=true -DskipTests
# cp ./target/jline-2.14.3.jar xxx/jython-2.7.1/extlibs/

```

## <a id="3.7"></a>3.7 编译leveldbjni-all-1.8.jar
跳到 [2.10 编译leveldb](#2.10)

## <a id="3.8"></a>3.8 编译netty-all-4.0.23.Final.jar
```bash
# 安装apr-1.5.2源码
wget https://archive.apache.org/dist/apr/apr-1.5.2.tar.gz
tar -zxf apr-1.5.2.tar.gz
cd apr-1.5.2
./configure --prefix=/opt/installed/apr
make
make install


# 安装netty-tcnative-1.1.30.Fork2源码
wget https://codeload.github.com/netty/netty-tcnative/tar.gz/netty-tcnative-1.1.30.Fork2
mv netty-tcnative-1.1.30.Fork2 netty-tcnative-1.1.30.Fork2.tar.gz
tar -zxf netty-tcnative-1.1.30.Fork2.tar.gz
cd netty-tcnative-netty-tcnative-1.1.30.Fork2
mvn install

# 安装netty-4.0.23源码
wget https://github.com/netty/netty/archive/netty-4.0.23.Final.tar.gz
tar -zxf netty-4.0.23.Final.tar.gz
cd netty-netty-4.0.23.Final
mvn install -DskipTests

```

## <a id="3.9"></a>3.9 编译netty-all-4.0.29.Final
```bash
# 安装apr-1.5.2源码（已安装，略去）

# 安装netty-tcnative-1.1.33.Fork3源码
wget https://codeload.github.com/netty/netty-tcnative/tar.gz/netty-tcnative-1.1.33.Fork3
mv netty-tcnative-1.1.33.Fork3 netty-tcnative-1.1.33.Fork3.tar.gz
tar -zxf netty-tcnative-1.1.33.Fork3.tar.gz
cd netty-tcnative-netty-tcnative-1.1.33.Fork3
mvn install

# 安装netty-4.0.29源码
wget https://github.com/netty/netty/archive/netty-4.0.29.Final.tar.gz
tar -zxf netty-4.0.29.Final.tar.gz
cd netty-netty-4.0.29.Final
mvn install -DskipTests

```

## <a id="3.10"></a>3.10 编译 lz4-java-1.2.0.jar
```bash
wget https://github.com/lz4/lz4-java/archive/1.2.0.zip
unzip 1.2.0.zip
cd lz4-java-1.2.0
ant ivy-bootstrap
wget https://repo1.maven.org/maven2/org/apache/ivy/ivy/2.2.0/ivy-2.2.0.jar -P /root/.ant/lib/
# 如果报 SERVER ERROR: HTTPS Required url=http://repo1.maven.org/maven2 ，修改连接为 https。
# 因为从2020年1月15日开始，中央存储库不再支持通过纯HTTP进行的不安全通信，并且要求对存储库的所有请求都通过HTTPS进行加密
# 例如如下错误，可以通过自建的 http 服务来解决。
# [ivy:resolve] :::: ERRORS
# [ivy:resolve] 	SERVER ERROR: HTTPS Required url=http://repo1.maven.org/maven2/com/carrotsearch/randomizedtesting/junit4-ant/2.0.9/junit4-ant-2.0.9.pom
# [ivy:resolve] 	SERVER ERROR: HTTPS Required url=http://repo1.maven.org/maven2/com/carrotsearch/randomizedtesting/junit4-ant/2.0.9/junit4-ant-2.0.9.jar
# [ivy:resolve] 
# [ivy:resolve] :: USE VERBOSE OR DEBUG MESSAGE LEVEL FOR MORE DETAILS
ant

# 如果报错时
# ant ivy-bootstrap
# ls /usr/share/ant/lib /root/.ant/lib
# ant --execdebug

# 编译成功之后，在 dist 有如下 jar
# lz4-1.2-SNAPSHOT.jar  lz4-1.2-SNAPSHOT-javadoc.jar  lz4-1.2-SNAPSHOT.pom  lz4-1.2-SNAPSHOT-sources.jar
mvn install:install-file -Dfile=dist/lz4-1.2-SNAPSHOT.jar -DgroupId=net.jpountz.lz4 -DartifactId=lz4 -Dversion=1.2-SNAPSHOT -Dpackaging=jar
mvn install:install-file -Dfile=dist/lz4-1.2-SNAPSHOT.jar -DgroupId=net.jpountz.lz4 -DartifactId=lz4 -Dversion=1.2.0 -Dpackaging=jar

```

【缺少证书，不选用此方法】如果上面报 `SERVER ERROR: HTTPS Required url` 错误，可以通过 Nginx 代理转为 https
```bash
# wget http://nginx.org/download/nginx-1.18.0.tar.gz
# tar -zxf nginx-1.18.0.tar.gz
# cd nginx-1.18.0
# # 告诉等会安装的文件要放在哪里
# ./configure --prefix=/opt/installed/nginx-1.18.0 --with-http_ssl_module
# make
# # 安装到配置的路径
# make install
# 
# ############
# systemctl stop httpd.service
# /opt/installed/nginx-1.18.0/sbin/nginx
# /opt/installed/nginx-1.18.0/sbin/nginx -s reload
```

将缺失的jar 下载到本地 http 文件索引路径下，然后本地 /etc/hosts 中临时配置 repo1.maven.org 指向本地，然后开启 http 服务。
 编译完毕后可以临时关闭，用的时候再开启。

```bash
cd /var/www/html/maven2
ls org/apache/ant/ant/1.8.2/
wget https://repo1.maven.org/maven2/org/apache/ant/ant/1.8.2/ant-1.8.2.pom -P org/apache/ant/ant/1.8.2/
wget https://repo1.maven.org/maven2/org/apache/ant/ant/1.8.2/ant-1.8.2.jar -P org/apache/ant/ant/1.8.2/
ls org/apache/ant/ant-junit/1.8.2/
wget https://repo1.maven.org/maven2/org/apache/ant/ant-junit/1.8.2/ant-junit-1.8.2.pom -P org/apache/ant/ant-junit/1.8.2/
wget https://repo1.maven.org/maven2/org/apache/ant/ant-junit/1.8.2/ant-junit-1.8.2.jar -P org/apache/ant/ant-junit/1.8.2/
ls junit/junit/4.10/
wget https://repo1.maven.org/maven2/junit/junit/4.10/junit-4.10.pom -P junit/junit/4.10/
wget https://repo1.maven.org/maven2/junit/junit/4.10/junit-4.10.jar -P junit/junit/4.10/
ls asm/asm/3.3.1/
wget https://repo1.maven.org/maven2/asm/asm/3.3.1/asm-3.3.1.pom -P asm/asm/3.3.1/
wget https://repo1.maven.org/maven2/asm/asm/3.3.1/asm-3.3.1.jar -P asm/asm/3.3.1/
ls com/google/guava/guava/10.0.1/
wget https://repo1.maven.org/maven2/com/google/guava/guava/10.0.1/guava-10.0.1.pom -P com/google/guava/guava/10.0.1/
wget https://repo1.maven.org/maven2/com/google/guava/guava/10.0.1/guava-10.0.1.jar -P com/google/guava/guava/10.0.1/
ls commons-io/commons-io/2.3/
wget https://repo1.maven.org/maven2/commons-io/commons-io/2.3/commons-io-2.3.pom -P commons-io/commons-io/2.3/
wget https://repo1.maven.org/maven2/commons-io/commons-io/2.3/commons-io-2.3.jar -P commons-io/commons-io/2.3/
ls com/google/code/gson/gson/2.0/
wget https://repo1.maven.org/maven2/com/google/code/gson/gson/2.0/gson-2.0.pom -P com/google/code/gson/gson/2.0/
wget https://repo1.maven.org/maven2/com/google/code/gson/gson/2.0/gson-2.0.jar -P com/google/code/gson/gson/2.0/
ls org/simpleframework/simple-xml/2.6.2/
wget https://repo1.maven.org/maven2/org/simpleframework/simple-xml/2.6.2/simple-xml-2.6.2.pom -P org/simpleframework/simple-xml/2.6.2/
wget https://repo1.maven.org/maven2/org/simpleframework/simple-xml/2.6.2/simple-xml-2.6.2.jar -P org/simpleframework/simple-xml/2.6.2/
ls com/carrotsearch/randomizedtesting/randomizedtesting-runner/2.0.9/
wget https://repo1.maven.org/maven2/com/carrotsearch/randomizedtesting/randomizedtesting-runner/2.0.9/randomizedtesting-runner-2.0.9.pom -P com/carrotsearch/randomizedtesting/randomizedtesting-runner/2.0.9/
wget https://repo1.maven.org/maven2/com/carrotsearch/randomizedtesting/randomizedtesting-runner/2.0.9/randomizedtesting-runner-2.0.9.jar -P com/carrotsearch/randomizedtesting/randomizedtesting-runner/2.0.9/
ls xpp3/xpp3/1.1.3.3/
wget https://repo1.maven.org/maven2/xpp3/xpp3/1.1.3.3/xpp3-1.1.3.3.pom -P xpp3/xpp3/1.1.3.3/
wget https://repo1.maven.org/maven2/xpp3/xpp3/1.1.3.3/xpp3-1.1.3.3.jar -P xpp3/xpp3/1.1.3.3/
ls org/apache/ant/ant-parent/1.8.2/
wget https://repo1.maven.org/maven2/org/apache/ant/ant-parent/1.8.2/ant-parent-1.8.2.pom -P org/apache/ant/ant-parent/1.8.2/
wget https://repo1.maven.org/maven2/org/apache/ant/ant-parent/1.8.2/ant-parent-1.8.2.jar -P org/apache/ant/ant-parent/1.8.2/
ls com/google/guava/guava-parent/10.0.1/
wget https://repo1.maven.org/maven2/com/google/guava/guava-parent/10.0.1/guava-parent-10.0.1.pom -P com/google/guava/guava-parent/10.0.1/
wget https://repo1.maven.org/maven2/com/google/guava/guava-parent/10.0.1/guava-parent-10.0.1.jar -P com/google/guava/guava-parent/10.0.1/
ls asm/asm-parent/3.3.1/
wget https://repo1.maven.org/maven2/asm/asm-parent/3.3.1/asm-parent-3.3.1.pom -P asm/asm-parent/3.3.1/
wget https://repo1.maven.org/maven2/asm/asm-parent/3.3.1/asm-parent-3.3.1.jar -P asm/asm-parent/3.3.1/
ls org/sonatype/oss/oss-parent/5/
wget https://repo1.maven.org/maven2/org/sonatype/oss/oss-parent/5/oss-parent-5.pom -P org/sonatype/oss/oss-parent/5/
wget https://repo1.maven.org/maven2/org/sonatype/oss/oss-parent/5/oss-parent-5.jar -P org/sonatype/oss/oss-parent/5/
ls stax/stax-api/1.0.1/
wget https://repo1.maven.org/maven2/stax/stax-api/1.0.1/stax-api-1.0.1.pom -P stax/stax-api/1.0.1/
wget https://repo1.maven.org/maven2/stax/stax-api/1.0.1/stax-api-1.0.1.jar -P stax/stax-api/1.0.1/
ls stax/stax/1.2.0/
wget https://repo1.maven.org/maven2/stax/stax/1.2.0/stax-1.2.0.pom -P stax/stax/1.2.0/
wget https://repo1.maven.org/maven2/stax/stax/1.2.0/stax-1.2.0.jar -P stax/stax/1.2.0/
ls org/apache/ant/ant-launcher/1.8.2/
wget https://repo1.maven.org/maven2/org/apache/ant/ant-launcher/1.8.2/ant-launcher-1.8.2.pom -P org/apache/ant/ant-launcher/1.8.2/
wget https://repo1.maven.org/maven2/org/apache/ant/ant-launcher/1.8.2/ant-launcher-1.8.2.jar -P org/apache/ant/ant-launcher/1.8.2/
ls org/hamcrest/hamcrest-core/1.1/
wget https://repo1.maven.org/maven2/org/hamcrest/hamcrest-core/1.1/hamcrest-core-1.1.pom -P org/hamcrest/hamcrest-core/1.1/
wget https://repo1.maven.org/maven2/org/hamcrest/hamcrest-core/1.1/hamcrest-core-1.1.jar -P org/hamcrest/hamcrest-core/1.1/
ls com/google/guava/guava-bootstrap/10.0.1/
wget https://repo1.maven.org/maven2/com/google/guava/guava-bootstrap/10.0.1/guava-bootstrap-10.0.1.pom -P com/google/guava/guava-bootstrap/10.0.1/
wget https://repo1.maven.org/maven2/com/google/guava/guava-bootstrap/10.0.1/guava-bootstrap-10.0.1.jar -P com/google/guava/guava-bootstrap/10.0.1/
ls org/hamcrest/hamcrest-parent/1.1/
wget https://repo1.maven.org/maven2/org/hamcrest/hamcrest-parent/1.1/hamcrest-parent-1.1.pom -P org/hamcrest/hamcrest-parent/1.1/
wget https://repo1.maven.org/maven2/org/hamcrest/hamcrest-parent/1.1/hamcrest-parent-1.1.jar -P org/hamcrest/hamcrest-parent/1.1/
ls org/apache/commons/commons-parent/24/
wget https://repo1.maven.org/maven2/org/apache/commons/commons-parent/24/commons-parent-24.pom -P org/apache/commons/commons-parent/24/
wget https://repo1.maven.org/maven2/org/apache/commons/commons-parent/24/commons-parent-24.jar -P org/apache/commons/commons-parent/24/
ls org/apache/apache/9/
wget https://repo1.maven.org/maven2/org/apache/apache/9/apache-9.pom -P org/apache/apache/9/
wget https://repo1.maven.org/maven2/org/apache/apache/9/apache-9.jar -P org/apache/apache/9/
ls org/mvel/mvel2/2.1.5.Final/
wget https://repo1.maven.org/maven2/org/mvel/mvel2/2.1.5.Final/mvel2-2.1.5.Final.pom -P org/mvel/mvel2/2.1.5.Final/
wget https://repo1.maven.org/maven2/org/mvel/mvel2/2.1.5.Final/mvel2-2.1.5.Final.jar -P org/mvel/mvel2/2.1.5.Final/
ls ant-contrib/cpptasks/1.0b5/
wget https://repo1.maven.org/maven2/ant-contrib/cpptasks/1.0b5/cpptasks-1.0b5.pom -P ant-contrib/cpptasks/1.0b5/
wget https://repo1.maven.org/maven2/ant-contrib/cpptasks/1.0b5/cpptasks-1.0b5.jar -P ant-contrib/cpptasks/1.0b5/
ls ant/ant/1.6.5/
wget https://repo1.maven.org/maven2/ant/ant/1.6.5/ant-1.6.5.pom -P ant/ant/1.6.5/
wget https://repo1.maven.org/maven2/ant/ant/1.6.5/ant-1.6.5.jar -P ant/ant/1.6.5/
ls xerces/xercesImpl/2.8.1/
wget https://repo1.maven.org/maven2/xerces/xercesImpl/2.8.1/xercesImpl-2.8.1.pom -P xerces/xercesImpl/2.8.1/
wget https://repo1.maven.org/maven2/xerces/xercesImpl/2.8.1/xercesImpl-2.8.1.jar -P xerces/xercesImpl/2.8.1/
ls org/apache/apache/4/
wget https://repo1.maven.org/maven2/org/apache/apache/4/apache-4.pom -P org/apache/apache/4/
wget https://repo1.maven.org/maven2/org/apache/apache/4/apache-4.jar -P org/apache/apache/4/
ls org/apache/apache/3/
wget https://repo1.maven.org/maven2/org/apache/apache/3/apache-3.pom -P org/apache/apache/3/
wget https://repo1.maven.org/maven2/org/apache/apache/3/apache-3.jar -P org/apache/apache/3/
ls xml-apis/xml-apis/1.3.03/
wget https://repo1.maven.org/maven2/xml-apis/xml-apis/1.3.03/xml-apis-1.3.03.pom -P xml-apis/xml-apis/1.3.03/
wget https://repo1.maven.org/maven2/xml-apis/xml-apis/1.3.03/xml-apis-1.3.03.jar -P xml-apis/xml-apis/1.3.03/
ls org/apache/apache/1/
wget https://repo1.maven.org/maven2/org/apache/apache/1/apache-1.pom -P org/apache/apache/1/
wget https://repo1.maven.org/maven2/org/apache/apache/1/apache-1.jar -P org/apache/apache/1/
```

## <a id="3.11"></a>3.11 编译snappy-java-1.0.4.1
```bash
wget https://gitee.com/mirrors/snappy-java/repository/archive/snappy-java-1.0.4.1
mv snappy-java-1.0.4.1 snappy-java-1.0.4.1.zip
unzip snappy-java-1.0.4.1.zip
mv snappy-java snappy-java-1.0.4.1
cd snappy-java-1.0.4.1
# 1.修改Makefile文件中，snappy压缩包下载地址（注释原下载地址，替换新的）。
# 2.修改Makefile文件中解压命令（注释原解压命令，替换新的）。
vim Makefile
make

# 如果报如下错误
#gzip: stdin: not in gzip format
#tar: Child returned status 1
#tar: Error is not recoverable: exiting now
rm -rf target/snappy-1.0.4.tar.gz
# 注释Makefile文件中，snappy压缩包下载地址，手动下载对应的snappy-1.0.4.tar.gz
wget https://src.fedoraproject.org/lookaside/pkgs/snappy/snappy-1.0.4.tar.gz/b69151652e82168bc5c643bcd6f07162/snappy-1.0.4.tar.gz -P target/

# 编译出的jar包位于：target/snappy-java-1.0.4.1.jar
cd ..
mvn install:install-file -Dpackaging=jar -Dfile=snappy-java-1.0.4.1/target/snappy-java-1.0.4.1.jar -DgroupId=org.xerial.snappy -DartifactId=snappy-java -Dversion=1.0.4.1

```
## <a id="3.12"></a>3.12 编译snappy-java-1.0.5
已编译，这里略过，可参考[ snappy-java-1.0.4.1,1.0.5 移植指南（CentOS 7.6）](https://support.huaweicloud.com/prtg-tpdl-kunpengbds/kunpengbds_02_0051.html)> [编译](https://support.huaweicloud.com/prtg-tpdl-kunpengbds/kunpengsnappyjava105_02_0004.html)


## <a id="3.13"></a>3.13 编译snappy-java-1.1.0.1
```bash
wget https://gitee.com/mirrors/snappy-java/repository/archive/1.1.0.1
mv 1.1.0.1 snappy-java-1.1.0.1.zip
unzip snappy-java-1.1.0.1.zip
mv snappy-java snappy-java-1.1.0.1
cd snappy-java-1.1.0.1
# 1.修改Makefile文件中，注释掉 #curl -o$@ http://snappy.googlecode.com/files/snappy-$(VERSION).tar.gz
# 2.修改Makefile文件中解压命令（注释原解压命令，替换新的）。 $(TAR) xvf $< -C $(TARGET)
vim Makefile
wget https://src.fedoraproject.org/repo/pkgs/snappy/snappy-1.1.0.tar.gz/c8f3ef29b5281e78f4946b2d739cea4f/snappy-1.1.0.tar.gz -P target/
# 将版本修改为 1.1.0.1
vim pom.xml
make
# 编译出的jar包位于：target/snappy-java-1.1.0.1.jar
cd ..
mvn install:install-file -Dpackaging=jar -Dfile=snappy-java-1.1.0.1/target/snappy-java-1.1.0.1.jar -DgroupId=org.xerial.snappy -DartifactId=snappy-java -Dversion=1.1.0.1

```

## <a id="3.14"></a>3.14 编译snappy-java-1.1.1.3
```bash
wget https://gitee.com/mirrors/snappy-java/repository/archive/1.1.1.3
mv 1.1.1.3 snappy-java-1.1.1.3.zip
unzip snappy-java-1.1.1.3.zip
mv snappy-java snappy-java-1.1.1.3
cd snappy-java-1.1.1.3
# 1.修改Makefile文件中，注释掉	38  #curl -o$@ http://snappy.googlecode.com/files/snappy-$(VERSION).tar.gz
# 2.修改Makefile文件中解压命令（注释原解压命令，替换新的）。$(TAR) xvf $< -C $(TARGET)
vim Makefile
wget https://src.fedoraproject.org/repo/pkgs/snappy/snappy-1.1.1.tar.gz/8887e3b7253b22a31f5486bca3cbc1c2/snappy-1.1.1.tar.gz -P target/
make

# 如果报 Download failed. Obtain the jar manually and place it at /root/.sbt/launchers/0.13.5/sbt-launch.jar，手动下载
wget http://dl.bintray.com/typesafe/ivy-releases/org.scala-sbt/sbt-launch/0.13.5/sbt-launch.jar -P /root/.sbt/launchers/0.13.5/
# 然后 注释掉sbt文件的该行(340)：-sbt-launch-repo) require_arg path "$1" "$2" && sbt_launch_repo="$2" && shift 2 ;;。
vim sbt

# 编译出的jar包位于：target/snappy-java-1.1.1.3.jar
mvn install:install-file -Dpackaging=jar -Dfile=target/snappy-java-1.1.1.3.jar -DgroupId=org.xerial.snappy -DartifactId=snappy-java -Dversion=1.1.1.3

```

将缺失的jar 下载到本地
```bash
cd /var/www/html/maven2
ls jline/jline/2.11/
mkdir -p jline/jline/2.11/
cp /opt/maven_repo/jline/jline/2.11/jline-2.11.pom jline/jline/2.11/
cp /opt/maven_repo/jline/jline/2.11/jline-2.11.jar jline/jline/2.11/
ls org/scala-lang/scala-library/2.10.4/
wget https://repo1.maven.org/maven2/org/scala-lang/scala-library/2.10.4/scala-library-2.10.4.pom -P org/scala-lang/scala-library/2.10.4/
wget https://repo1.maven.org/maven2/org/scala-lang/scala-library/2.10.4/scala-library-2.10.4.jar -P org/scala-lang/scala-library/2.10.4/
ls org/scala-lang/scala-compiler/2.10.4/
wget https://repo1.maven.org/maven2/org/scala-lang/scala-compiler/2.10.4/scala-compiler-2.10.4.pom -P org/scala-lang/scala-compiler/2.10.4/
wget https://repo1.maven.org/maven2/org/scala-lang/scala-compiler/2.10.4/scala-compiler-2.10.4.jar -P org/scala-lang/scala-compiler/2.10.4/
ls org/apache/ivy/ivy/2.3.0/
wget https://repo1.maven.org/maven2/org/apache/ivy/ivy/2.3.0/ivy-2.3.0.pom -P org/apache/ivy/ivy/2.3.0/
wget https://repo1.maven.org/maven2/org/apache/ivy/ivy/2.3.0/ivy-2.3.0.jar -P org/apache/ivy/ivy/2.3.0/
ls com/jcraft/jsch/0.1.46/
wget https://repo1.maven.org/maven2/com/jcraft/jsch/0.1.46/jsch-0.1.46.pom -P com/jcraft/jsch/0.1.46/
wget https://repo1.maven.org/maven2/com/jcraft/jsch/0.1.46/jsch-0.1.46.jar -P com/jcraft/jsch/0.1.46/
ls org/scala-sbt/test-interface/1.0/
wget https://repo1.maven.org/maven2/org/scala-sbt/test-interface/1.0/test-interface-1.0.pom -P org/scala-sbt/test-interface/1.0/
wget https://repo1.maven.org/maven2/org/scala-sbt/test-interface/1.0/test-interface-1.0.jar -P org/scala-sbt/test-interface/1.0/
ls org/scala-lang/scala-reflect/2.10.4/
wget https://repo1.maven.org/maven2/org/scala-lang/scala-reflect/2.10.4/scala-reflect-2.10.4.pom -P org/scala-lang/scala-reflect/2.10.4/
wget https://repo1.maven.org/maven2/org/scala-lang/scala-reflect/2.10.4/scala-reflect-2.10.4.jar -P org/scala-lang/scala-reflect/2.10.4/
ls org/apache/apache/7/
wget https://repo1.maven.org/maven2/org/apache/apache/7/apache-7.pom -P org/apache/apache/7/
wget https://repo1.maven.org/maven2/org/apache/apache/7/apache-7.jar -P org/apache/apache/7/
ls org/sonatype/oss/oss-parent/6/
wget https://repo1.maven.org/maven2/org/sonatype/oss/oss-parent/6/oss-parent-6.pom -P org/sonatype/oss/oss-parent/6/
wget https://repo1.maven.org/maven2/org/sonatype/oss/oss-parent/6/oss-parent-6.jar -P org/sonatype/oss/oss-parent/6/
ls org/scala-lang/jline/2.10.4/
wget https://repo1.maven.org/maven2/org/scala-lang/jline/2.10.4/jline-2.10.4.pom -P org/scala-lang/jline/2.10.4/
wget https://repo1.maven.org/maven2/org/scala-lang/jline/2.10.4/jline-2.10.4.jar -P org/scala-lang/jline/2.10.4/
ls org/fusesource/jansi/jansi/1.4/
wget https://repo1.maven.org/maven2/org/fusesource/jansi/jansi/1.4/jansi-1.4.pom -P org/fusesource/jansi/jansi/1.4/
wget https://repo1.maven.org/maven2/org/fusesource/jansi/jansi/1.4/jansi-1.4.jar -P org/fusesource/jansi/jansi/1.4/
ls org/codehaus/codehaus-parent/4/
wget https://repo1.maven.org/maven2/org/codehaus/codehaus-parent/4/codehaus-parent-4.pom -P org/codehaus/codehaus-parent/4/
wget https://repo1.maven.org/maven2/org/codehaus/codehaus-parent/4/codehaus-parent-4.jar -P org/codehaus/codehaus-parent/4/
ls org/scala-lang/scala-library/2.10.3/
wget https://repo1.maven.org/maven2/org/scala-lang/scala-library/2.10.3/scala-library-2.10.3.pom -P org/scala-lang/scala-library/2.10.3/
wget https://repo1.maven.org/maven2/org/scala-lang/scala-library/2.10.3/scala-library-2.10.3.jar -P org/scala-lang/scala-library/2.10.3/
```

## <a id="3.15"></a>3.15 编译snappy-java-1.1.1.6
```bash
wget https://gitee.com/mirrors/snappy-java/repository/archive/1.1.1.6
mv 1.1.1.6 snappy-java-1.1.1.6.zip
unzip snappy-java-1.1.1.6.zip
mv snappy-java snappy-java-1.1.1.6
cd snappy-java-1.1.1.6
# 1.修改Makefile文件中，注释掉	38  #curl -o$@ http://snappy.googlecode.com/files/snappy-$(VERSION).tar.gz
# 2.修改Makefile文件中解压命令（注释原解压命令，替换新的）。$(TAR) xvf $< -C $(TARGET)
vim Makefile
mkdir target
cp ../snappy-java-1.1.1.3/target/snappy-1.1.1.tar.gz target/
make

# 如果报 Download failed. Obtain the jar manually and place it at /root/.sbt/launchers/0.13.6/sbt-launch.jar，手动下载
wget http://dl.bintray.com/typesafe/ivy-releases/org.scala-sbt/sbt-launch/0.13.6/sbt-launch.jar -P /root/.sbt/launchers/0.13.6/
# 然后 注释掉sbt文件的该行(340)：-sbt-launch-repo) require_arg path "$1" "$2" && sbt_launch_repo="$2" && shift 2 ;;。
vim sbt

# 编译出的jar包位于：target/snappy-java-1.1.1.6.jar
mvn install:install-file -Dpackaging=jar -Dfile=target/snappy-java-1.1.1.6.jar -DgroupId=org.xerial.snappy -DartifactId=snappy-java -Dversion=1.1.1.6

```

## <a id="3.16"></a>3.16 编译snappy-java-1.1.1.7
```bash
wget https://github.com/xerial/snappy-java/archive/1.1.1.7.zip
mv 1.1.1.7.zip snappy-java-1.1.1.7.zip
unzip snappy-java-1.1.1.7.zip
cd snappy-java-1.1.1.7
# 1.修改Makefile文件中，注释掉	38  #curl -o$@ http://snappy.googlecode.com/files/snappy-$(VERSION).tar.gz
# 2.修改Makefile文件中解压命令（注释原解压命令，替换新的）。$(TAR) xvf $< -C $(TARGET)
vim Makefile
mkdir target
cp ../snappy-java-1.1.1.3/target/snappy-1.1.1.tar.gz target/
make

# 编译出的jar包位于：target/snappy-java-1.1.1.7.jar
mvn install:install-file -Dpackaging=jar -Dfile=target/snappy-java-1.1.1.7.jar -DgroupId=org.xerial.snappy -DartifactId=snappy-java -Dversion=1.1.1.7

```

## <a id="3.17"></a>3.17 编译snappy-java-1.1.2.6
```bash
wget https://gitee.com/mirrors/snappy-java/repository/archive/1.1.2.6
mv 1.1.2.6 snappy-java-1.1.2.6.zip
unzip snappy-java-1.1.2.6.zip
mv snappy-java snappy-java-1.1.2.6
cd snappy-java-1.1.2.6
# 1.修改Makefile文件中，注释掉	38  #curl -o$@ http://snappy.googlecode.com/files/snappy-$(VERSION).tar.gz
# 2.修改Makefile文件中解压命令（注释原解压命令，替换新的）。$(TAR) xvf $< -C $(TARGET)
vim Makefile
wget http://repository.timesys.com/buildsources/s/snappy/snappy-1.1.2/snappy-1.1.2.tar.gz -P target/

# 如果报 Download failed. Obtain the jar manually and place it at /root/.sbt/launchers/0.13.9/sbt-launch.jar，手动下载
wget http://dl.bintray.com/typesafe/ivy-releases/org.scala-sbt/sbt-launch/0.13.9/sbt-launch.jar -P /root/.sbt/launchers/0.13.9/
# 然后 注释掉sbt文件的该行(340)：-sbt-launch-repo) require_arg path "$1" "$2" && sbt_launch_repo="$2" && shift 2 ;;。
vim sbt

make
# 编译出的jar包位于：target/snappy-java-1.1.2.6.jar
mvn install:install-file -Dpackaging=jar -Dfile=target/snappy-java-1.1.2.6.jar -DgroupId=org.xerial.snappy -DartifactId=snappy-java -Dversion=1.1.2.6

```

## <a id="3.18"></a>3.18 编译 snappy-1.1.7
```
# 下载 https://github.com/google/snappy/releases/tag/1.1.7
wget https://github.com/google/snappy/archive/1.1.7.tar.gz
mv 1.1.7.tar.gz snappy-1.1.7.tar.gz
tar -zxvf snappy-1.1.7.tar.gz
cd snappy-1.1.7

mkdir build
cd build
cmake ../
make install

# 修改CMakeLists.txt文件开启动态链接库编译，将该选项从“OFF”改为“ON”。
cd ..
vim CMakeLists.txt
# 大概在 第（6 行）
option(BUILD_SHARED_LIBS "Build shared libraries(DLLs)." ON)

```

## <a id="3.19"></a>3.19 编译安装netty-4.1.17
```bash
wget https://github.com/netty/netty/archive/netty-4.1.17.Final.tar.gz
tar -zxf netty-4.1.17.Final.tar.gz
cd netty-netty-4.1.17.Final

# 修改“pom.xml”，注释以下代码（大概在 594-600 行）和 187 行
vim pom.xml

# 去除一下测试代码中的错误
mv transport/src/test/java/io/netty/channel/PendingWriteQueueTest.java transport/src/test/java/io/netty/channel/PendingWriteQueueTestjava
mv codec/src/test/java/io/netty/handler/codec/ByteToMessageCodecTest.java codec/src/test/java/io/netty/handler/codec/ByteToMessageCodecTestjava
mv handler/src/test/java/io/netty/handler/stream/ChunkedWriteHandlerTest.java handler/src/test/java/io/netty/handler/stream/ChunkedWriteHandlerTestjava
mv handler/src/test/java/io/netty/handler/flush/FlushConsolidationHandlerTest.java handler/src/test/java/io/netty/handler/flush/FlushConsolidationHandlerTestjava

mvn install -DskipTests

```

## <a id="3.20"></a>3.20 编译Hbase-shaded-netty-2.1.0
```bash
wget https://github.com/apache/hbase-thirdparty/archive/rel/2.1.0.tar.gz
mv 2.1.0.tar.gz hbase-thirdparty-rel-2.1.0.tar.gz
tar -zxf hbase-thirdparty-rel-2.1.0.tar.gz
cd hbase-thirdparty-rel-2.1.0

# 修改“hbase-shaded-netty/pom.xml”文件，将“x86_64”修改为“aarch_64”。（大概在 128、129 行）
vim hbase-shaded-netty/pom.xml

mvn clean install

```

## <a id="3.21"></a>3.21 编译commons-crypto-1.0.0
```bash
wget https://github.com/apache/commons-crypto/archive/CRYPTO-1.0.0.tar.gz
tar -zxvf CRYPTO-1.0.0.tar.gz
cd commons-crypto-CRYPTO-1.0.0
mvn clean install -DskipTests

```

## <a id="3.22"></a>3.22 编译scala-compiler-2.11.8
编译此依赖需要先编译 jline-2.12.1
```bash
# 下载scala-2.11.8源码
git clone https://github.com/scala/scala.git
cp -r scala scala-2.11.8
cd scala-2.11.8
git checkout v2.11.8
sed -i "48,48s%)%),\n\ \ \ \ Keys.\`package\`\ := bundle.value%g" project/Osgi.scala
sbt package

# 编译完成之后的 scala-compiler.jar 在如下路径下
build-sbt/pack/lib/scala-compiler.jar

mvn install:install-file -Dpackaging=jar -Dfile=build-sbt/pack/lib/scala-compiler.jar \
-DgroupId=org.scala-lang -DartifactId=scala-compiler -Dversion=2.11.8

```

## <a id="3.23"></a>3.23 编译scala-compiler-2.11.12
```bash
cd ..
cp -r scala scala-2.11.12
cd scala-2.11.12
git checkout v2.11.12
sed -i "50,50s%)%),\n\ \ \ \ Keys.\`package\`\ := bundle.value%g" project/Osgi.scala
sbt package

# 编译完成之后的 scala-compiler.jar 在如下路径下
ll build/pack/lib/

mvn install:install-file -Dpackaging=jar -Dfile=build/pack/lib/scala-compiler.jar \
-DgroupId=org.scala-lang -DartifactId=scala-compiler -Dversion=2.11.12

```

## <a id="3.24"></a>3.24 编译scala-compiler-2.12.7
```bash
cd ..
wget https://codeload.github.com/scala/scala/tar.gz/v2.12.7
tar -zxf scala-2.12.7.tar.gz.gz
cd scala-2.12.7
sbt package

# 编译完成之后的 scala-compiler.jar 在如下路径下
build-sbt/pack/lib/scala-compiler.jar

mvn install:install-file -Dpackaging=jar -Dfile=build-sbt/pack/lib/scala-compiler.jar \
-DgroupId=org.scala-lang -DartifactId=scala-compiler -Dversion=2.12.7

```

## <a id="3.25"></a>3.25 编译安装netty-4.0.52源码
```bash
wget https://github.com/netty/netty/archive/netty-4.0.52.Final.tar.gz
tar -zxvf netty-4.0.52.Final.tar.gz
cd netty-netty-4.0.52.Final
# 修改“netty-netty-4.0.52.Final/pom.xml”，注释以下代码（592-598行）
vim pom.xml +592

mvn install -DskipTests

```
![在这里插入图片描述](https://img-blog.csdnimg.cn/2020052017563044.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L2dpdGh1Yl8zOTU3NzI1Nw==,size_16,color_FFFFFF,t_70#pic_center)


## <a id="3.26"></a>3.26 编译安装netty-tcnative-parent-2.0.6.Final
详细参看 https://support.huaweicloud.com/prtg-tpdl-kunpengbds/kunpengnettyall4052_02_0004.html
```bash
wget https://codeload.github.com/netty/netty-tcnative/tar.gz/netty-tcnative-parent-2.0.6.Final
mv netty-tcnative-netty-tcnative-parent-2.0.6.Final.tar.gz.gz netty-tcnative-parent-2.0.6.Final.tar.gz
tar -zxvf netty-tcnative-parent-2.0.6.Final.tar.gz
cd netty-tcnative-netty-tcnative-parent-2.0.6.Final

# 修改“pom.xml”，屏蔽boringssl的编译。注释 472 行引入的模块
# 同时还需 注释掉该文件中两处下载apr-1.6.3的部分。（322行、373行）
vim pom.xml

# 下载apr-1.6.3，放置于以下位置
wget https://archive.apache.org/dist/apr/apr-1.6.2.tar.gz -P openssl-static/target

# 改为 apr 自己安装的目录：/opt/installed/apr
vim openssl-dynamic/pom.xml +171

# 修改“openssl-static/pom.xml”，注释掉该文件中3处下载openssl-1.0.2l的部分（136-140行、202-206行、252-256行）
vim openssl-static/pom.xml +136
# 下载openssl-1.0.2l，放置于以下位置
wget https://ftp.openssl.org/source/old/1.0.2/openssl-1.0.2l.tar.gz
mv openssl-1.0.2l.tar.gz openssl-static/target

# 将下载的apr-1.6.2，放置于以下位置
cp openssl-static/target/apr-1.6.2.tar.gz libressl-static/target
# 修改“libressl-static/pom.xml”，注释掉该文件中1处下载libressl-2.5.5的部分（141行、173行）
vim libressl-static/pom.xml +141
wget http://ftp.openbsd.org/pub/OpenBSD/LibreSSL/libressl-2.5.5.tar.gz
mv libressl-2.5.5.tar.gz libressl-static/target

mvn install -DskipTests

```
![netty-tcnative-parent-2.0.6.Final编译成功](https://img-blog.csdnimg.cn/20200520175524246.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L2dpdGh1Yl8zOTU3NzI1Nw==,size_16,color_FFFFFF,t_70#pic_center)

## <a id="3.27"></a>3.27 编译安装netty-tcnative-parent-2.0.7.Final
详细参看 https://support.huaweicloud.com/prtg-tpdl-kunpengbds/kunpengnettyall4117_02_0004.html
```bash
wget https://codeload.github.com/netty/netty-tcnative/tar.gz/netty-tcnative-parent-2.0.7.Final
mv netty-tcnative-parent-2.0.7.Final netty-tcnative-parent-2.0.7.Final.tar.gz
tar -zxvf netty-tcnative-parent-2.0.7.Final.tar.gz
cd netty-tcnative-netty-tcnative-parent-2.0.7.Final

# 修改pom.xml，屏蔽boringssl的编译。注释 272 行引入的模块
# 同时还需 注释掉该文件中两处下载apr-1.6.3的部分。（322行、373行）

# 下载apr-1.6.3，放置于以下位置
wget https://archive.apache.org/dist/apr/apr-1.6.3.tar.gz
mv apr-1.6.3.tar.gz openssl-static/target

# 修改“openssl-static/pom.xml”，注释掉该文件中3处下载openssl-1.0.2l的部分（135-140行、203-207行、254-258行）
vim openssl-static/pom.xml
# 下载openssl-1.0.2l，放置于以下位置
wget https://ftp.openssl.org/source/old/1.0.2/openssl-1.0.2l.tar.gz
mv openssl-1.0.2l.tar.gz openssl-static/target

mvn install -DskipTests

cp openssl-static/target/apr-1.6.3.tar.gz libressl-static/target
mvn install -DskipTests

```

## <a id="3.28"></a>3.28 编译Jansi-1.0.jar
```bash
wget https://github.com/fusesource/jansi-native/archive/jansi-native-1.0.zip
unzip jansi-native-1.0.zip
cd jansi-native-jansi-native-1.0/
# 注释掉 pom.xml 的 157-172 行。 gpg.skip=true
mvn clean -Dplatform=linux64 package

```

## <a id="3.29"></a>3.29 编译 Jansi-1.4.jar
```bash
wget https://github.com/fusesource/jansi/archive/jansi-1.4.tar.gz
tar -zxf jansi-1.4.tar.gz
cd jansi-jansi-1.4

#修改pom.xml，注释掉下面125-148行的这部分内容

mvn clean install

```

## <a id="3.30"></a>3.30 安装Protoc
这部分直接参考华为文档 [ 鲲鹏大数据解决方案 > 移植指南（Apache）> Hadoop 3.1.1 移植指南（CentOS 7.6）> 配置编译环境> 安装Protoc](https://support.huaweicloud.com/prtg-apache-kunpengbds/kunpenghadoop_02_0008.html)
```bash
# 安装Protoc 之后，执行下面查看
protoc --version

wget https://github.com/protocolbuffers/protobuf/releases/download/v2.5.0/protobuf-2.5.0.tar.gz
tar -zxf protobuf-2.5.0.tar.gz -C /opt/installed/
cd /opt/installed/protobuf-2.5.0

# 安装依赖库
# wget http://124.193.70.227:18080/ambari/repodata/repomd.xml -P /root/HDP/repodata/
yum -y install patch automake libtool

# 上传protoc.patch到服务器，打补丁，其中protoc.patch的路径视实际情况而定。
cp /root/protoc.patch ./src/google/protobuf/stubs/
cd ./src/google/protobuf/stubs/
patch -p1 < protoc.patch
cd -

# 编译并安装到系统默认目录。
./autogen.sh && ./configure CFLAGS='-fsigned-char' && make -j8 && make install


# 这一步非常重要，否则后面编译 HBase 就会出现问题
mvn install:install-file -DgroupId=com.google.protobuf -DartifactId=protoc -Dversion=2.5.0 -Dclassifier=linux-aarch_64 -Dpackaging=exe -Dfile=/usr/local/bin/protoc

```


<br/><br/>


# <a id="4"></a>4 编译 Ambari

## <a id="4.1"></a>4.1 下载 Ambari 源码 
```bash
wget https://github.com/hortonworks/ambari-release/archive/AMBARI-2.7.3.0-139-tag.tar.gz
tar -zxf AMBARI-2.7.3.0-139-tag.tar.gz -C /opt/installed
cd /opt/installed/ambari-release-AMBARI-2.7.3.0-139-tag/

export PHANTOMJS_ARCH="arm64"
git config --global http.sslverify false

```


## <a id="4.2"></a>4.2 编译_posixsubprocess32.so
```bash
# 通过命令安装python2-subprocess32
yum install -y python2-subprocess32.aarch64
# 在 /usr/lib64/python2.7/site-packages/_posixsubprocess.so 处获得 _posixsubprocess.so
mv /usr/lib64/python2.7/site-packages/_posixsubprocess.so /usr/lib64/python2.7/site-packages/_posixsubprocess32.so

# 在ambari的源码中"ambari-common/src/main/python/ambari_commons/libs"目录下执行命令
cd ambari-common/src/main/python/ambari_commons/libs
cp -r x86_64 aarch64

cp /usr/lib64/python2.7/site-packages/_posixsubprocess32.so aarch64/
rm -rf aarch64/_posixsubprocess.so

```

## <a id="4.3"></a>4.3 编译_speedups.so
```bash
# 从ambari源码中“ambari-common/src/main/python/ambari_simplejson/__init__.py"文件中100行可知simplejson的版本为2.0.9
vim /opt/installed/ambari-release-AMBARI-2.7.3.0-139-tag/ambari-common/src/main/python/ambari_simplejson/__init__.py

# pip 安装
## 如果失败表示 pip 未安装
pip --version
whereis python
# 访问https://pypi.org/project/pip/官网进行下载
curl https://bootstrap.pypa.io/get-pip.py -o get-pip.py
sudo python get-pip.py
```
![在这里插入图片描述](https://img-blog.csdnimg.cn/20200520174831998.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L2dpdGh1Yl8zOTU3NzI1Nw==,size_16,color_FFFFFF,t_70#pic_center)
```bash

# 下载simplejson源码并解压
pip download simplejson==2.0.9
tar -zxf simplejson-2.0.9.tar.gz
cd simplejson-2.0.9

# 编译simplejson
python setup.py build
# 编译好的so文件在build/lib.linux-aarch64-2.7/simplejson/_speedups.so
ls build/lib.linux-aarch64-2.7/simplejson/

# 使用编译完成的_speedups.so替换掉 ambari-common/src/main/python/ambari_simplejson/目录下的_speedups.so
cp build/lib.linux-aarch64-2.7/simplejson/_speedups.so \
/opt/installed/ambari-release-AMBARI-2.7.3.0-139-tag/ambari-common/src/main/python/ambari_simplejson/_speedups.so

```

## <a id="4.4"></a>4.4 编译 Phoenix-5.0.0
```bash
# 获取 Phoenix 源码
# wget https://github.com/hortonworks/phoenix-release/archive/HDP-3.0.0.0-1634-tag.tar.gz
# yum -y install gcc.aarch64 gcc-c++.aarch64 gcc-gfortran.aarch64 libgcc.aarch64
wget https://github.com/apache/phoenix/archive/v5.0.0-HBase-2.0.tar.gz
tar -zxvf v5.0.0-HBase-2.0.tar.gz
cd phoenix-5.0.0-HBase-2.0

# 修改Pom.xml，添加华为鲲鹏的maven仓库。
vim pom.xml +42
#   <repository>
#     <id>kunpengmaven</id>
#     <name>kunpeng maven</name>
#     <url>https://mirrors.huaweicloud.com/kunpeng/maven</url>
#   </repository>

# 编译安装
# 编译成功后将在源码目录下./phoenix-assembly/target/目录生成phoenix-5.0.0-HBase-2.0.tar.gz包。
mvn clean install -DskipTests -Dmaven.javadoc.skip=true
ll ./phoenix-assembly/target/

# 将 打好的 jar 方法 http 服务器索引目录下
mkdir -p /var/www/html/packages/tar/
cp phoenix-assembly/target/phoenix-5.0.0-HBase-2.0.tar.gz /var/www/html/packages/tar/

```

如果报如下错误
```
[ERROR] Failed to execute goal on project phoenix-core: Could not resolve dependencies for project org.apache.phoenix:phoenix-core:jar:5.0.0-HBase-2.0: Failed to collect dependencies at org.apache.hbase:hbase-testing-util:jar:2.0.0 -> org.apache.hbase:hbase-server:jar:2.0.0 -> org.glassfish.web:javax.servlet.jsp:jar:2.3.2 -> org.glassfish:javax.el:jar:3.0.1-b06-SNAPSHOT: Failed to read artifact descriptor for org.glassfish:javax.el:jar:3.0.1-b06-SNAPSHOT: Could not transfer artifact org.glassfish:javax.el:pom:3.0.1-b06-SNAPSHOT from/to central-repos1 (http://repo1.maven.org/maven2/): Failed to transfer file: http://repo1.maven.org/maven2/org/glassfish/javax.el/3.0.1-b06-SNAPSHOT/javax.el-3.0.1-b06-SNAPSHOT.pom. Return code is: 501 , ReasonPhrase:HTTPS Required. -> [Help 1]
[ERROR] 
[ERROR] To see the full stack trace of the errors, re-run Maven with the -e switch.
[ERROR] Re-run Maven using the -X switch to enable full debug logging.
[ERROR] 
[ERROR] For more information about the errors and possible solutions, please read the following articles:
[ERROR] [Help 1] http://cwiki.apache.org/confluence/display/MAVEN/DependencyResolutionException
[ERROR] 
[ERROR] After correcting the problems, you can resume the build with the command
[ERROR]   mvn <goals> -rf :phoenix-core
```
**解决**：$MAVEN_HOME/conf/setting.xml 中配置阿里的镜像

如果报 `ERROR] java.lang.NoClassDefFoundError: scala/reflect/internal/Trees` 导致 **Phoenix - Spark** 失败。可以这样解决，将 ` phoenix-spark/pom.xml` 插件 `net.alchim31.maven:scala-maven-plugin` 编译的 scala 版本改为 2.11.0

详细的可以查看我在华为云的 [鲲鹏论坛 上回复的该问题的解决方法](https://bbs.huaweicloud.com/forum/thread-49783-1-1.html) &nbsp; &nbsp;([YoreYuan](https://bbs.huaweicloud.com/forum/home.php?mod=space&do=thread&view=me&type=reply&uid=245839&from=space))
![pom.xml 修改scala版本](https://imgconvert.csdnimg.cn/aHR0cHM6Ly9iYnMtaW1nLWNiYy1jbi5vYnMuY24tbm9ydGgtMS5teWh1YXdlaWNsb3VkLmNvbS9kYXRhL2F0dGFjaG1lbnQvZm9ydW0vMjAyMDA1LzE4LzE0MjI1M2luajRxaHQyYXkzZjZhcXoucG5n?x-oss-process=image/format,png)


## <a id="4.5"></a>4.5 编译 hadoop-3.1.1
[执行移植分析](https://support.huaweicloud.com/prtg-apache-kunpengbds/kunpenghadoop_02_0009.html)
```bash
# 安装Protoc
protoc --version
# yum install openssl-devel zlib-devel automake libtool cmake
# 下载链接：https://github.com/hortonworks/hadoop-release/archive/HDP-3.0.0.0-1634-tag.tar.gz。
wget https://archive.apache.org/dist/hadoop/common/hadoop-3.1.1/hadoop-3.1.1-src.tar.gz
tar -zxvf hadoop-3.1.1-src.tar.gz
cd hadoop-3.1.1-src

# 执行基础编译命令。
mvn install -DskipTests -Pdist,native -Dtar -Dmaven.javadoc.skip=true


# 添加snappy库编译命令。
mvn install -DskipTests -Pdist,native -Dtar -Dsnappy.lib=/usr/local/lib64 -Dbundle.snappy -Dmaven.javadoc.skip=true

# 编译成功后，将在源码下的“hadoop-dist/target/”目录生成tar.gz包。
ls hadoop-dist/target/

# 将编译成功后的 hadoop-3.1.1.tar.gz 放到 http 文件服务索引路径
ls /var/www/html/packages/tar/
cp hadoop-dist/target/hadoop-3.1.1.tar.gz /var/www/html/packages/tar/

```

如果报如下错误，收到下载到本地仓库
```
[ERROR] Failed to execute goal on project hadoop-aws: Could not resolve dependencies for project org.apache.hadoop:hadoop-aws:jar:3.1.1: Could not find artifact com.amazonaws:DynamoDBLocal:jar:1.11.86 in nexus-aliyun (http://maven.aliyun.com/nexus/content/groups/public) -> [Help 1]
```

通过上面可以看到在 Hadoop 下的  hadoop-aws 中引入的 `com.amazonaws:DynamoDBLocal:jar:1.11.86` 无法获取，解决
```bash
# 查找 DynamoDBLocal 所在位置
grep -rn "Apache Hadoop Amazon Web Services support" ./
vim hadoop-tools/hadoop-aws/pom.xml +420

# 可以发现其scope 为 test，因此我们可以直接将其注释掉
#420    <!-- <dependency>
#421       <groupId>com.amazonaws</groupId>
#422       <artifactId>DynamoDBLocal</artifactId>
#423       <version>${dynamodb.local.version}</version>
#424       <scope>test</scope>
#425       <exclusions>
#426         <exclusion>
#427           <groupId>org.hamcrest</groupId>
#428           <artifactId>hamcrest-core</artifactId>
#429         </exclusion>
#430         <exclusion>
#431           <groupId>org.eclipse.jetty</groupId>
#432           <artifactId>jetty-http</artifactId>
#433         </exclusion>
#434         <exclusion>
#435           <groupId>org.apache.commons</groupId>
#436           <artifactId>commons-lang3</artifactId>
#437         </exclusion>
#438       </exclusions>
#439     </dependency>-->


# 将此测试类注释掉
mv hadoop-tools/hadoop-aws/src/test hadoop-tools/hadoop-aws/src/test_back

```
![hadoop 编译成功](https://img-blog.csdnimg.cn/20200520175723626.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L2dpdGh1Yl8zOTU3NzI1Nw==,size_16,color_FFFFFF,t_70#pic_center)

## <a id="4.6"></a>4.6 编译 hbase-2.1.0
务必 aarch64 下编译 [安装Protoc](https://support.huaweicloud.com/prtg-apache-kunpengbds/kunpenghivehdp_02_0007.html)，
因为 google 仓库在 2.5.0 不提供 aarch64位的支持。

```bash
protoc --version
# 下载链接：https://github.com/hortonworks/hbase-release/archive/HDP-3.0.0.0-1634-tag.tar.gz。
wget http://archive.apache.org/dist/hbase/2.1.0/hbase-2.1.0-src.tar.gz
tar -zxf hbase-2.1.0-src.tar.gz
cd hbase-2.1.0

# 执行编译。
# validate -Denforcer.skip=true
# mvn package -Pdist,native, -DskipTests -Dtar -Dmaven.javadoc-skip=true assembly:single validate -Denforcer.skip=true
mvn clean package  -DskipTests assembly:single
# 编译成功后，将在源码下的“hbase-assembly/target/”目录生成tar.gz包。
ls hbase-assembly/target/
# 拷贝到 http 索引资源包路径下
cp hbase-assembly/target/hbase-2.1.0-bin.tar.gz /var/www/html/packages/tar/

```

针对 [HBASE-19146](https://issues.apache.org/jira/browse/HBASE-19146) 问题，是因为HBase用到了**protobuf**，用于RPC消息的序列化和反序列化。编译时通过 Maven 插件`protobuf-maven-plugin`将源代码中的`*.proto`文件 转换成Java源文件，然后再通过 Java 编译器编译成 jar 包。这个 Maven 插件依赖于 protoc，根据声明 `${os.detected.classifier}`，Maven会自动对应不同的体系架构，从Maven仓库中下载相应的二进 制执行文件。这是非常方便的Maven内建机制，用于自动支持不同平台的Java依赖，但是HBase依赖于 一个很老版本的protoc 2.5.0， 但是Google还未开始从这个版本支持aarch64。

对比 HBase 的另一个模块hbase-protocol-shaded，应用了[protoc 3.5.1-1](https://repo1.maven.org/maven2/com/google/protobuf/protoc/3.5.1-1/)， 
（`/hbase-protocol-shaded/pom.xml`中第 39 行、 79-96行）这个版本已经有aarch64的支持，编译是成功的。


## <a id="4.7"></a>4.7 编译安装 grafana-2.6.0
参考 [编译安装Grafana](https://support.huaweicloud.com/prtg-hdp-kunpengbds/kunpengambarihdp_02_0027.html)
```bash
wget https://github.com/grafana/grafana/archive/v2.6.0.tar.gz
mv v2.6.0.tar.gz grafana-v2.6.0.tar.gz
tar -zxf grafana-v2.6.0.tar.gz

# 搭建好golang环境，并将源码移动到“GOPATH”目录下的“src”目录
mv grafana-2.6.0 ${GOPATH}/src/
# 拷贝grafana-2.6.0项目编译过程中本身的依赖到src对应目录
cd ${GOPATH}/src/grafana-2.6.0
mkdir -p ../github.com/grafana/grafana/pkg
cp pkg/* ../github.com/grafana/grafana/pkg/ -rf

# 执行后端编译
go run build.go setup
go run build.go build

# 跳过phantomjs报错（因为系统中已安装），执行前端编译
npm config set registry "https://registry.npmjs.org/"
npm config set strict-ssl false
# 可以只安装与grunt-cli相关的
npm install --force
# npm install -g grunt-cli
grunt --force

# 拷贝phantomjs可执行文件到相应目录，打包时将自动将phantomjs打包到grafana的rpm包中。
cp /opt/installed/phantomjs/bin/phantomjs vendor/phantomjs/

# 打包
#go run build.go package
./node_modules/grunt-cli/bin/grunt release --force

```

如果报如下错误
```
Loading "grunt-karma.js" tasks...ERROR
>> TypeError: Cannot read property 'prototype' of undefined
Warning: Task "karma:test" not found. Used --force, continuing.
```
解决：
```bash
# 方便查看错误信息
npm install grunt-contrib-imagemin --save-dev
#npm install -g grunt
#mkdir /opt/installed/go/path/src/grafana-2.6.0/node_modules/gifsicle
npm install grunt-util-args
npm install karma
# grunt-karma@0.8.3
npm install grunt-karma

#npm install --save-dev karma@~0.12.0
# npm uninstall -g grunt
#npm install grunt --save-dev
#npm install grunt-autoprefixer --save
```


## <a id="4.8"></a>4.8 接着[4.1](#4.1)继续完后后续Ambari 源码的编译
```bash
cd /opt/installed/ambari-release-AMBARI-2.7.3.0-139-tag

# 修改CPU架构
# 参考 [问题27](https://support.huaweicloud.com/prtg-hdp-kunpengbds/kunpengambarihdp_02_0015.html#kunpengambarihdp_02_0015__section133321452103219)
#  将所有pom.xml文件中的“needarch”值，从“x86_64”和“noarch”改为“aarch64”
#
# 修改ambari-agent/pom.xml中304行为“aarch64”
vim ambari-agent/pom.xml
du -sh `find . -name *.rpm`

# 将所有pom.xml文件中的“needarch”值，从“x86_64”和“noarch”改为“ aarch64 ”
grep -rn "needarch"  ./
#./ambari-agent/pom.xml:304:          <needarch>aarch64</needarch>
#./ambari-infra/ambari-infra-assembly/pom.xml:74:              <needarch>noarch</needarch>
#./ambari-infra/ambari-infra-assembly/pom.xml:114:                  <needarch>noarch</needarch>
#./ambari-logsearch/ambari-logsearch-assembly/pom.xml:66:              <needarch>noarch</needarch>
#./ambari-metrics/ambari-metrics-assembly/pom.xml:203:              <needarch>x86_64</needarch>
#./ambari-metrics/ambari-metrics-assembly/pom.xml:465:                  <needarch>x86_64</needarch>
#./ambari-metrics/ambari-metrics-assembly/pom.xml:545:                  <needarch>x86_64</needarch>
#./ambari-server/pom.xml:563:          <needarch>x86_64</needarch>
vim ambari-agent/pom.xml +304
vim ambari-infra/ambari-infra-assembly/pom.xml +74
vim ambari-infra/ambari-infra-assembly/pom.xml +114
vim ambari-logsearch/ambari-logsearch-assembly/pom.xml +66
vim ambari-metrics/ambari-metrics-assembly/pom.xml +203
vim ambari-metrics/ambari-metrics-assembly/pom.xml +465
vim ambari-metrics/ambari-metrics-assembly/pom.xml +545
vim ambari-server/pom.xml +563


# 将安装nodejs中下载的node-v8.6.0-linux-arm64.tar.gz和v4.5版nodejs移动到maven本地仓
mkdir -p /opt/maven_repo/com/github/eirslett/node/8.6.0/
mkdir -p /opt/maven_repo/com/github/eirslett/node/4.5.0/
cp /root/yore_ambari/node-v8.6.0-linux-arm64.tar.gz /opt/maven_repo/com/github/eirslett/node/8.6.0/
wget http://nodejs.org/dist/v4.5.0/node-v4.5.0-linux-arm64.tar.gz --no-check-certificate
cp node-v4.5.0-linux-arm64.tar.gz /opt/maven_repo/com/github/eirslett/node/4.5.0/


# 下载yarn到maven本地仓。
wget https://github.com/yarnpkg/yarn/releases/download/v1.1.0/yarn-v1.1.0.tar.gz --no-check-certificate
wget https://github.com/yarnpkg/yarn/releases/download/v0.23.2/yarn-v0.23.2.tar.gz --no-check-certificate
mkdir -p /opt/maven_repo/com/github/eirslett/yarn/1.1.0/
mkdir -p /opt/maven_repo/com/github/eirslett/yarn/0.23.2/yarn-0.23.2./
cp yarn-v1.1.0.tar.gz  /opt/maven_repo/com/github/eirslett/yarn/1.1.0/yarn-1.1.0.tar.gz
cp yarn-v0.23.2.tar.gz  /opt/maven_repo/com/github/eirslett/yarn/0.23.2/yarn-0.23.2./yarn-v0.23.2.tar.gz

# 设置yarn取消ssl验证
tar -zxf yarn-v0.23.2.tar.gz
mv dist  yarn-v0.23.2
cd yarn-v0.23.2
bin/yarn config set strict-ssl false

# 配置npm代理
npm config set strict-ssl false
npm config set registry http://registry.npmjs.org/


# 修改ambari-web/package.json，将phantomjs版本修改为 2.1.1
grep -rn "phantomjs"  ./


vim /opt/installed/ambari-release-AMBARI-2.7.3.0-139-tag/ambari-web/package.json +41
vim /opt/installed/ambari-release-AMBARI-2.7.3.0-139-tag/ambari-web/node_modules/mocha-phantomjs/package.json 
# 第26 行删除"karma-phantomjs-launcher": "0.1"。27 行改为安装的 phantomjs 版本（2.1.1）
vim ambari-admin/src/main/resources/ui/admin-web/package.json
# storm 版本改为 1.1.0 
# vim ambari-metrics/ambari-metrics-storm-sink/pom.xml +34

# 修改ambari-metrics/pom.xml 中仓库地址
vim /opt/installed/ambari-release-AMBARI-2.7.3.0-139-tag/ambari-metrics/pom.xml
## 将hbase，hadoop，grafana，Phoenix地址和文件名修改成提供的地址和相应版本
# <hbase.tar>http://192.168.1.26/packages/tar/hbase-2.1.0-bin.tar.gz</hbase.tar>
# <hbase.folder>hbase-2.1.0-bin.tar.gz</hbase.folder>
# <hadoop.tar>http://192.168.1.26/packages/tar/hadoop-3.1.1.tar.gz</hadoop.tar>
# <hadoop.folder>hadoop-3.1.1.tar.gz</hadoop.folder>
# <grafana.folder>grafana-2.6.0</grafana.folder>
# #<grafana.tar>http://192.168.1.26/packages/tar/grafana-2.6.0.tar.gz</grafana.tar>
# <phoenix.tar>http://192.168.1.26/packages/tar/phoenix-5.0.0-HBase-2.0.tar.gz</phoenix.tar>


# 修改ambari-metrics/pom.xml中hdp的url为http://repo.hortonworks.com/content/repositories/releases
# 将以下标签中 ur l改为 http://repo.hortonworks.com/content/repositories/releases
vim ambari-metrics/pom.xml +84


# 修改ambari-admin/src/main/resources/ui/admin-web/package.json 中 karma-phantomjs-launcher为1.0。
# "karma-phantomjs-launcher": "1.0",
vim ambari-admin/src/main/resources/ui/admin-web/package.json +26


# 执行grep -rn "oss\.sonatype" 将查找出的所有pom中的该maven仓库注释掉（对应的整个 repository 标签）。
# ambari-infra/pom.xml:63:      <id>oss.sonatype.org</id>
# ambari-infra/pom.xml:65:      <url>https://oss.sonatype.org/content/groups/staging</url>
# ambari-logsearch/pom.xml:81:      <id>oss.sonatype.org</id>
# ambari-logsearch/pom.xml:83:      <url>https://oss.sonatype.org/content/groups/staging</url>
# ambari-server/pom.xml:1851:      <id>oss.sonatype.org</id>
# ambari-server/pom.xml:1853:      <url>https://oss.sonatype.org/content/groups/staging</url>
# ambari-serviceadvisor/pom.xml:62:      <id>oss.sonatype.org</id>
# ambari-serviceadvisor/pom.xml:64:      <url>https://oss.sonatype.org/content/groups/staging</url>
# pom.xml:107:      <id>oss.sonatype.org</id>
# pom.xml:109:      <url>https://oss.sonatype.org/content/groups/staging</url>
vim ambari-infra/pom.xml +63
vim ambari-logsearch/pom.xml +81
vim ambari-server/pom.xml +1851
vim ambari-serviceadvisor/pom.xml +62
vim pom.xml +107


# 将所有https://repository.apache.org/content/repositories/snapshots/ 改为http://repository.apache.org/content/repositories/snapshots/
grep -rn "repository.apache.org"
# ambari-infra/pom.xml:77:      <url>https://repository.apache.org/content/groups/staging/</url>
# ambari-infra/pom.xml:81:      <url>https://repository.apache.org/content/repositories/snapshots/</url>
# ambari-logsearch/pom.xml:95:      <url>https://repository.apache.org/content/groups/staging/</url>
# ambari-logsearch/pom.xml:99:      <url>https://repository.apache.org/content/repositories/snapshots/</url>
# ambari-metrics/pom.xml:62:    <distMgmtSnapshotsUrl>https://repository.apache.org/content/repositories/snapshots</distMgmtSnapshotsUrl>
# ambari-metrics/pom.xml:65:    <distMgmtStagingUrl>https://repository.apache.org/service/local/staging/deploy/maven2</distMgmtStagingUrl>
# ambari-metrics/pom.xml:90:      <url>https://repository.apache.org/content/repositories/snapshots</url>
# pom.xml:59:    <distMgmtSnapshotsUrl>https://repository.apache.org/content/repositories/snapshots</distMgmtSnapshotsUrl>
# pom.xml:62:    <distMgmtStagingUrl>https://repository.apache.org/service/local/staging/deploy/maven2</distMgmtStagingUrl>
# pom.xml:121:      <url>https://repository.apache.org/content/groups/staging/</url>
# pom.xml:125:      <url>https://repository.apache.org/content/repositories/snapshots/</url>

```

编译执行
```bash
# 整体编译
mvn -B install package rpm:rpm -DnewVersion=2.7.3.0.0 -DskipTests -Dmaven.test.skip=true -Drat.numUnapprovedLicenses=1000 -Dpython.ver="python>=2.6" -Drat.skip -Dcheckstyle.skip

# 编译各个模块
mvn clean package -Dbuild-rpm -Dmaven.test.skip=true -DskipTests -Drat.numUnapprovedLicenses=1000 -Dpython.ver="python>=2.6"

```


# <a id="5"></a>5 汇总

## <a id="5.1"></a>5.1 资源路径
Item  |  Path  
:---- | :----  
下载的资源包             | `/root/yore_ambari`
jar 包相关资源和编译     | `/root/yore_ambari/_jar`
Maven 本地仓库路径       | `/opt/maven_repo`
安装路径                 | `/opt/installed`
http文件服务路径         | `/var/www/html/`


## <a id="5.2"></a>5.2 编译汇总目录表
打 `√` 的表示已编译完成，未打 `√` 的表示还存在问题。
* [x] _posixsubprocess32.so
* [x] _speedups.so
* [ ] AMBARI-2.7.3.0-139
* [x] Apache/2.4.6
* [x] apache-ant-1.9.15
* [x] apache-maven-3.5.4
* [x] apr-1.5.2
* [x] cmake-3.9.2
* [x] commons-crypto-1.0.0
* [x] go1.9.linux-arm64
* [x] gcc-4.8.5
* [ ] grafana-2.6.0
* [x] hadoop-3.1.1
* [x] hbase-2.1.0
* [x] Hbase-shaded-netty-2.1.0
* [ ] Jansi-1.4
* [ ] Jansi-native1.0
* [x] jdk_aarch64_linux_hotspot_8u222b10
* [x] jline-2.11
* [x] jline-2.12
* [x] jline-2.12.1
* [x] jline-2.13
* [x] jline-2.14.3
* [x] leveldb-1.20
* [x] leveldbjni-all-1.8
* [x] lz4-java-1.2.0
* [x] netty-tcnative-1.1.30.Fork2
* [x] netty-tcnative-1.1.33.Fork3
* [x] netty-tcnative-parent-2.0.6.Final
* [x] netty-tcnative-parent-2.0.7.Final
* [x] netty-all-4.0.23
* [x] netty-all-4.0.29
* [x] netty-all-4.0.52
* [x] netty-all-4.1.17
* [x] node-v8.6.0-linux-arm64
* [x] Phantomjs 2.1.1
* [x] phoenix-5.0.0-HBase-2.0
* [x] pip-20.1
* [x] protobuf-2.5.0
* [x] python2-subprocess32.aarch64
* [x] sbt-1.3.10
* [x] scala-compiler-2.11.8
* [x] scala-compiler-2.11.12
* [x] scala-compiler-2.12.7
* [x] simplejson-2.0.9
* [x] snappy-1.1.5
* [x] snappy-1.1.7
* [x] snappy-java-1.0.4.1
* [x] snappy-java-1.0.5
* [x] snappy-java-1.1.0.1
* [x] snappy-java-1.1.1.3
* [x] snappy-java-1.1.1.6
* [x] snappy-java-1.1.1.7
* [x] snappy-java-1.1.2.6


# <a id="6"></a>6 🎉镜像 🎉
编译到这里的时候脑海中是不是会浮现出 `小朋友 你是否有好多问号`([《听妈妈的话-周杰伦》](https://y.qq.com/n/yqq/song/002hXDfk0LX9KO.html))的画面，我只是想在 aarch64的系统上部署 HDP，现在仅编译 Ambari 就费了这么多时间，华为也给了官方文档，能否提供一份编译成功的镜像包呢？通过翻阅文档终于找到了：

ARM 版软件包下载地址
* [HDP-UTILS](https://mirrors.huaweicloud.com/kunpeng/yum/el/7/bigdata/HDP-UTILS-1.1.0.22/repos/)
* [HDP-GPL 3.1.0.0 ](https://mirrors.huaweicloud.com/kunpeng/yum/el/7/bigdata/HDP-GPL/3.x/updates/3.1.0.0/)
* [ambari 2.7.3.0](https://mirrors.huaweicloud.com/kunpeng/yum/el/7/bigdata/ambari/2.x/updates/2.7.3.0/)
* [HDP 3.1.0.0](https://mirrors.huaweicloud.com/kunpeng/yum/el/7/bigdata/HDP/3.x/updates/3.1.0.0/)

<br/>

[Ambari与HDP之间的版本支持信息](https://supportmatrix.hortonworks.com/)

<br/>

更新的状态可以访问我的 blog [aarch64架构（ARMv8）系统环境下编译 Ambari](https://blog.csdn.net/github_39577257/article/details/106211612)


