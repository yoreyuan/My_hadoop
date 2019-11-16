expect 使用
===
# 1 简介

## 1.1 百度百科介绍
[摘自百度](https://baike.baidu.com/item/expect/4598715)

Expect是一个免费的编程工具语言，用来实现自动和交互式任务进行通信，而无需人的干预。Expect的作者Don Libes在1990年开始编写Expect时对Expect做有如下定义：
Expect是一个用来实现自动交互功能的软件套件(Expect [is a] software suite for automating interactive tools)。使用它，
系统管理员可以创建脚本来对命令或程序进行输入，而这些命令和程序是期望从终端（terminal）得到输入，一般来说这些输入都需要手工输入进行的。
Expect则可以根据程序的提示模拟标准输入提供给程序需要的输入来实现交互程序执行。甚至可以实现简单的BBS聊天机器人。 :)

Expect是不断发展的，随着时间的流逝，其功能越来越强大，已经成为系统管理员的的一个强大助手。
Expect需要**Tcl编程语言**的支持，要在系统上运行Expect**必须首先安装Tcl**。

## 1.2 官网
[Expect](https://core.tcl-lang.org/expect/index)
Expect是用于自动化交互式应用程序（例如telnet，f​​tp，passwd，fsck，rlogin，tip等）的工具。Expect确实使这些事情变得微不足道。
Expect对于测试这些相同的应用程序也很有用。通过添加Tk，您还可以在X11 GUI中包装交互式应用程序。

Expect可以使其他任务难以完成的各种任务变得容易。您会发现Expect是绝对有价值的工具-使用它，您将能够自动化您从未想过的任务-而且您将能够快速，轻松地实现此自动化。

### 1.2.1 历史
Expect是在1987年9月构想的。版本2的大部分是在1990年1月至1990年4月之间设计和编写的。此后进行了较小的演变，直到Tcl 6.0发布。那时（1991年10月），
版本3重写了Expect的大约一半。有关 更多信息，请参见[HISTORY文件](https://core.tcl-lang.org/expect/HISTORY)。HISTORY文件包含在Expect分发中。

1993年1月左右，推出了Expect 4的Alpha版本。这包括Tk支持以及大量增强功能。对用户界面本身进行了一些更改，这就是更改主要版本号的原因。1993年8月发布了Expect 4的生产版本。

1993年10月，发布了Expect 5的Alpha版本以匹配Tcl 7.0。进行了大量增强，包括对用户界面本身进行了一些更改，这就是（再次）更改主版本号的原因。94年3月发布了Expect 5的生产版本。

在1999年夏天，为了支持Tcl 8.2，对Expect进行了大量重写。（Expect从未移植到8.1，因为它包含基本缺陷。）这包括创建exp通道驱动程序和对象支持，
以便利用新的regexp引擎和UTF / Unicode。用户界面高度兼容，但并不完全向后兼容。有关更多详细信息，请参见发行版中的NEWS文件。



## 1.3 其它
Expect是一种自动交互语言，能实现在shell脚本中为scp和ssh等自动输入密码自动登陆，我们通过shell可以实现简单的控制流功能，如:循环、判断等。
但是对于需要交互的场合必须通过人工干预，expect就时是用来实现这种功能的工具。 从最简单的层次来说，expect的工作方式像是一个通用化的chat脚本工具，
chat脚本最早用于uucp网路i内，以用来实现实现计算机之间需要建立连接时进行特定的登陆会话自动化。 
Chat脚本由一系列的expect-send对组成，expect等待输出中输出的特定字符，通常是一个提示符，然后发送特定的响应。

# 2 安装
## 2.1 通过yum安装
```bash
# yum安装
yum  install -y expect

# 检查
expect -v

```

## 2.2 下载源码包安装
就如简介中介绍的 `expect` 需要[tcl](https://core.tcl-lang.org/index.html)的支持。因此安装之前需要先安装tcl。

* TCL的下载页面可访问：[download.html](https://www.tcl.tk/software/tcltk/download.html)
* TCL的编译指南：[compile.html](https://www.tcl.tk/doc/howto/compile.html)

安装tcl
```bash
# 1 下载 
wget https://prdownloads.sourceforge.net/tcl/tcl8.6.9-src.tar.gz

# 2 解压
tar -zxf tcl8.6.9-src.tar.gz
cd tcl8.6.9

# 3 目录结构如下。可以看到这是一个包含多系统的源码包
# [root@cdh5 tcl8.6.9]# tree -L 1 ./
# ./
# ├── ChangeLog
# ├── ChangeLog.1999
# ├── ChangeLog.2000
# ├── ChangeLog.2001
# ├── ChangeLog.2002
# ├── ChangeLog.2003
# ├── ChangeLog.2004
# ├── ChangeLog.2005
# ├── ChangeLog.2007
# ├── ChangeLog.2008
# ├── changes
# ├── compat
# ├── doc
# ├── generic
# ├── library
# ├── libtommath
# ├── license.terms
# ├── macosx
# ├── pkgs
# ├── README
# ├── tests
# ├── tools
# ├── unix
# └── win

# 4 进入unix。本次以Linux为例
cd unix

# 5 编译
# 可以指定参数，
#  --prefix=directory  默认是在 /usr/local 下
./configure
make
make test
make install

```

安装 expect

* Expect的下载页面可访问：[download.html](https://jaist.dl.sourceforge.net/project/expect/Expect/)
* Expect下载页面可访问： [sourceforge page](https://sourceforge.net/projects/expect/)

```bash
# 1 下载
wget https://jaist.dl.sourceforge.net/project/expect/Expect/5.45.4/expect5.45.4.tar.gz

# 2 解压
tar -zxf expect5.45.4.tar.gz

# 3 进入到expect
 cd expect5.45.4
 
# 4 编译安装
./configure && make && make install

# 5 检查
expect -v

```

# 3 使用
## 3.1 shell脚本形式
将如下的脚本保存在`job.sh`，然后执行` sh job.sh`
```bash
#!/bin/bash

# 匹配提示符
CMD_PROMPT="\](\$|#)"

# 要执行的脚本
script="/root/expect_test.sh"
username="root"
password="***123654***"
host="cdh3"
port=22
expect -c "
    send_user connecting\ to\ $hostssh.md$port $username@$host
    expect {
        *yes/no { send -- yes\r;exp_continue;}
        *password* { send -- $password\r;}
    }
    expect -re $CMD_PROMPT
    send -- $script\r
    expect -re $CMD_PROMPT
    exit
"

echo -e "\n************************************************************************************"
echo " 		 Successful execution at node ${host}	"
echo -e "************************************************************************************\n"

# send_user		空格要转义.  是回显，相当于echo。
# send			发指令到对端，向进程发送输入内容。
# exp_continue	重新匹配所在的expect，相当于while的continue，表示expect的匹配从头开始继续匹配。
# spawn			开启新的进程
# expect		匹配上一条指令的输出
# -re			表示匹配正则表达式

# exit,close,wait：exit表示退出脚本，close表示立即关闭过程，而wait则是等待过程返回eof时关闭。
# ineract       留在环境中不退出 
# interact：    运行表示将控制权交给用户，与spawn生成的进程进行交互。由用户与spawn生成的进程进行交互，比如登录ftp服务器并下载的过程中，
# #             登录ftp服务器的过程可以由用户输入自己的用户名和密码，然后用户再输入q字符将控制权交给脚本，由脚本完成后面的交互动作。

```


## 3.2 expect脚本形式
将如下的脚本保存到`job.exp`文件中，然后执行` expect job.exp`
```
#!/usr/local/bin/expect

# 要执行的脚本  
set script "/root/expect_test.sh"

set username "root"
set password "***123654***"
set host "cdh3"
set port "22"

spawn ssh -p $port $username@$host
expect {
"*yes/no" {send "yes\r"; exp_continue} 
"*password:" {send "$password\r"}
"*Password:" {send "$password\r"}
}
puts "\n************************************************************************************"
puts "\n		 LOGIN ${host} SUCCESS  START TO EXCE COMMAND	"
puts "\n************************************************************************************"

#expect "jzcj@*"  {send "cd ${RUN_PATH}\r"}
expect "$username@*"  {send "sh $script\r"}
expect "$username@*"  {send "exit\r"}

#exit
expect eof

```

