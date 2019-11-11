Ansible
------
[官网](https://www.ansible.com/) &nbsp; &nbsp; | &nbsp; &nbsp;
[Documentation](https://docs.ansible.com/)


# 1 概述
##  1.什么是Ansible
Ansible是python 中的一套模块，系统中的一套自动化工具，只需要使用ssh协议连接及可用来系统管理、自动化执行命令等任务。

## 2 Ansible优势 
* ansible不需要单独安装客户端，也不需要启动任何服务
* ansible是python中的一套完整的自动化执行任务模块
* ansible playbook，采用yaml配置，对于自动化任务执行一目了然
* ansible 模块较多，对于自动化的场景支持较丰富

## 3 Ansible架构
* 连接插件connectior plugins用于连接主机 用来连接被管理端
* 核心模块 core modules 连接主机实现操作， 它依赖于具体的模块来做具体的事情
* 自定义模块 custom modules，根据自己的需求编写具体的模块
* 插件 plugins，完成模块功能的补充
* 剧本 playbooks，ansible的配置文件,将多个任务定义在剧本中，由ansible自动执行
* 主机清单 inventor，定义ansible需要操作主机的范围

最重要的一点是 ansible是模块化的 它所有的操作都依赖于模块

![Ansible架构图](https://ask.qcloudimg.com/http-save/1033579/p1ugrfqtmo.jpeg?imageView2/2/w/1620)


# 2 Ansible安装
## 2.1 yum方式安装 
```bash
# 安装
yum install -y ansible

# 查看版本
ansible --version

```

## 2.2 离线安装
其中用到的包，可以到[PyPi网站](https://pypi.org/)搜索下载。
### 2.2.1 安装python-devel
先安装 python-devel ，否则安装下面时会报`fatal error: Python.h: No such file or directory`错误。
```bash
# yum方式安装
#yum -y install python-devel openssl-devel

# 下载rpm包
wget http://mirror.centos.org/centos/7/os/x86_64/Packages/python-devel-2.7.5-86.el7.x86_64.rpm
wget http://mirror.centos.org/centos/7/os/x86_64/Packages/openssl-devel-1.0.2k-19.el7.x86_64.rpm

# 安装
# 如果是普通用户需要有sudo权限
#yum -y install python-devel-2.7.5-86.el7.x86_64.rpm
#yum -y install openssl-devel-1.0.2k-19.el7.x86_64.rpm
rpm -Uvh python-devel-2.7.5-86.el7.x86_64.rpm --nodeps --force
rpm -Uvh openssl-devel-1.0.2k-19.el7.x86_64.rpm --nodeps --force


```

### 2.2.2 安装 setuptools
```bash
# 下载
wget https://files.pythonhosted.org/packages/11/0a/7f13ef5cd932a107cd4c0f3ebc9d831d9b78e1a0e8c98a098ca17b1d7d97/setuptools-41.6.0.zip

# 安装 setuptools
unzip setuptools-41.6.0.zip
cd setuptools-41.6.0
python setup.py install
cd ../

# 查看版本
easy_install --version

```

### 2.2.3 安装pycrypto
```bash
wget https://files.pythonhosted.org/packages/60/db/645aa9af249f059cc3a368b118de33889219e0362141e75d4eaf6f80f163/pycrypto-2.6.1.tar.gz
tar -xzf pycrypto-2.6.1.tar.gz 
cd pycrypto-2.6.1
python setup.py install
cd ../
```

### 2.2.4  安装 PyYAML
```bash
wget https://files.pythonhosted.org/packages/e3/e8/b3212641ee2718d556df0f23f78de8303f068fe29cdaa7a91018849582fe/PyYAML-5.1.2.tar.gz
tar -xzf PyYAML-5.1.2.tar.gz
cd PyYAML-5.1.2
python setup.py install
cd ../
```

### 2.2.5  安装MarkupSafe
````bash
wget https://files.pythonhosted.org/packages/b9/2e/64db92e53b86efccfaea71321f597fa2e1b2bd3853d8ce658568f7a13094/MarkupSafe-1.1.1.tar.gz
tar -xzf MarkupSafe-1.1.1.tar.gz 
cd MarkupSafe-1.1.1
python setup.py  install
cd ../
````

### 2.2.6 安装Jinja2
```bash
wget https://files.pythonhosted.org/packages/7b/db/1d037ccd626d05a7a47a1b81ea73775614af83c2b3e53d86a0bb41d8d799/Jinja2-2.10.3.tar.gz
tar -xzf Jinja2-2.10.3.tar.gz
cd Jinja2-2.10.3
python setup.py  install
cd ../
```

### 2.2.7 安装ecdsa
```bash
wget https://files.pythonhosted.org/packages/b0/9e/dffa648ea8f2bc9e58e96a9fcb8702c4b4f520047071b257acfb41d6924f/ecdsa-0.14.1.tar.gz
tar -xzf ecdsa-0.14.1.tar.gz
cd ecdsa-0.14.1
python setup.py install
cd ../
```

### 2.2.8 安装simplejson
```bash
wget https://files.pythonhosted.org/packages/e3/24/c35fb1c1c315fc0fffe61ea00d3f88e85469004713dab488dee4f35b0aff/simplejson-3.16.0.tar.gz
tar -xzf simplejson-3.16.0.tar.gz 
cd simplejson-3.16.0
python setup.py install
cd ../
```

### 2.2.9 安装libffi-devel
```bash
wget http://mirror.centos.org/centos/7/os/x86_64/Packages/libffi-devel-3.0.13-18.el7.x86_64.rpm
yum -y install libffi-devel-3.0.13-18.el7.x86_64.rpm
```

### 2.2.10 安装pycparser
```bash
wget https://files.pythonhosted.org/packages/68/9e/49196946aee219aead1290e00d1e7fdeab8567783e83e1b9ab5585e6206a/pycparser-2.19.tar.gz
tar -xzf pycparser-2.19.tar.gz
cd pycparser-2.19
python setup.py install
cd ../
```

### 2.2.11 安装cffi
```bash
wget https://files.pythonhosted.org/packages/2d/bf/960e5a422db3ac1a5e612cb35ca436c3fc985ed4b7ed13a1b4879006f450/cffi-1.13.2.tar.gz
tar -xzf cffi-1.13.2.tar.gz
cd cffi-1.13.2
python setup.py install
cd ../
```


### 2.2.12 安装ipaddress
```bash
wget https://files.pythonhosted.org/packages/b9/9a/3e9da40ea28b8210dd6504d3fe9fe7e013b62bf45902b458d1cdc3c34ed9/ipaddress-1.0.23.tar.gz
tar -xzf ipaddress-1.0.23.tar.gz 
cd ipaddress-1.0.23
python setup.py install
cd ../
```


### 2.2.13 安装six
```bash
wget https://files.pythonhosted.org/packages/94/3e/edcf6fef41d89187df7e38e868b2dd2182677922b600e880baad7749c865/six-1.13.0.tar.gz
tar -xzf six-1.13.0.tar.gz 
cd six-1.13.0
python setup.py install
cd ../
```


### 2.2.14  安装asn1crypto
```bash
wget https://files.pythonhosted.org/packages/c1/a9/86bfedaf41ca590747b4c9075bc470d0b2ec44fb5db5d378bc61447b3b6b/asn1crypto-1.2.0.tar.gz
tar -xzf asn1crypto-1.2.0.tar.gz
cd asn1crypto-1.2.0
python setup.py install
cd ../
```


### 2.2.15 安装idna
```bash
wget https://files.pythonhosted.org/packages/ad/13/eb56951b6f7950cadb579ca166e448ba77f9d24efc03edd7e55fa57d04b7/idna-2.8.tar.gz
tar -xzf idna-2.8.tar.gz 
cd idna-2.8
python setup.py install
cd ../

```

### 2.2.16 安装pyasn1
```bash
wget https://files.pythonhosted.org/packages/ca/f8/2a60a2c88a97558bdd289b6dc9eb75b00bd90ff34155d681ba6dbbcb46b2/pyasn1-0.4.7.tar.gz
tar -xzf pyasn1-0.4.7.tar.gz
cd pyasn1-0.4.7
python setup.py install
cd ../
```

### 2.2.17 安装PyNaCl
```bash
wget https://files.pythonhosted.org/packages/61/ab/2ac6dea8489fa713e2b4c6c5b549cc962dd4a842b5998d9e80cf8440b7cd/PyNaCl-1.3.0.tar.gz
tar -xzf PyNaCl-1.3.0.tar.gz 
cd PyNaCl-1.3.0
python setup.py install
cd ..
```


### 2.2.18 安装cryptography
```bash
wget https://files.pythonhosted.org/packages/be/60/da377e1bed002716fb2d5d1d1cab720f298cb33ecff7bf7adea72788e4e4/cryptography-2.8.tar.gz
tar -zxf cryptography-2.8.tar.gz
cd cryptography-2.8
python setup.py install
cd ..
```

### 2.2.19 安装bcrypt
```bash
wget https://files.pythonhosted.org/packages/fa/aa/025a3ab62469b5167bc397837c9ffc486c42a97ef12ceaa6699d8f5a5416/bcrypt-3.1.7.tar.gz
tar -xzf bcrypt-3.1.7.tar.gz
cd bcrypt-3.1.7
python setup.py install
cd ..
```

### 2.2.20 安装paramiko
```bash
wget https://files.pythonhosted.org/packages/1f/e9/9c0e26be89b002464116dd181db36edd6b0a37631f69b5e0a1d0a4e28ccf/paramiko-2.5.1.tar.gz
tar -xzf paramiko-2.5.1.tar.gz
cd paramiko-2.5.1
python setup.py install
cd ..
```

### 2.2.21 安装 ansible
```bash
# 下载
wget https://releases.ansible.com/ansible/ansible-2.9.0.tar.gz

# 解压
tar -xvf ansible-2.9.0.tar.gz
cd ansible-2.9.0/

# 安装
python setup.py install

# 查看版本
ansible --version

```

## 2.3 安装 sshpass
Ansible使用sshpass程序通过SSH登录到服务器时使用的密码，否则会报如下错误
```log
to use the 'ssh' connection type with passwords, you must install the  program
```

```bash
wget https://jaist.dl.sourceforge.net/project/sshpass/sshpass/1.06/sshpass-1.06.tar.gz
tar xvzf sshpass-1.06.tar.gz
cd sshpass-1.06
./configure
make
make install
sshpass -V

```


# 3 使用
## 3.1 配置自己的数据资产清单
```bash
# 主机资产清单
cat /etc/ansible/hosts
```

配置格式如下：
```bash
#方式一、主机+端口+密码
[webservers]
10.0.0.31 ansible_ssh_port=22 ansible_ssh_user=root ansible_ssh_pass='123456'
10.0.0.41 ansible_ssh_port=22 ansible_ssh_user=root ansible_ssh_pass='123456'

#方式二、主机+端口+密码
[webservers]
web[1:2].oldboy.com ansible_ssh_pass='123456'

#方式三、主机+端口+密码
[webservers]
web[1:2].oldboy.com
[webservers:vars]
ansible_ssh_pass='123456'

```

## 3.2 配置样例
```bash
# 1 如果文件不存在先创建路径
mkdir /etc/ansible

# 2 编辑ansible主机资产清单文件
vim /etc/ansible/hosts

```

如下配置，配置了三个组，每个组可以配置自己的主机名、SSH的用户和访问密码。
```bash
[cdh]
192.168.33.3 ansible_ssh_port=22 ansible_ssh_user=root ansible_ssh_pass='7ygQPN96E'
192.168.33.6 ansible_ssh_port=22 ansible_ssh_user=root ansible_ssh_pass='eCyFegMcF'

[impala]
192.168.33.9 ansible_ssh_port=22 ansible_ssh_user=root ansible_ssh_pass='xKc31eq8P'

[mongo]
192.168.33.6 ansible_ssh_port=22 ansible_ssh_user=root ansible_ssh_pass='eCyFegMcF'

```

## 3.3 command命令
```bash
# 1 执行命令形式，组名可以是上面配置文件中的 cdh、impala等名字
ansible 组名 -m command -a "hostname"

# 2 如果需要使用管道操作，则使用shell
# 通过ifconfig查看网络信息，并过滤出inet的信息。如果无法执行，请安装：yum install -y net-tools
ansible 组名 -m shell -a "ifconfig | grep inet"

# 3 本地脚本在远程主机上执行，sql脚本必须是shell形式，这个脚本在本地放置即可，不用上传到远程服务器上
ansible impala -m script -a "impala-shell -f /home/yore/my_sql.sh"

```

`my_sql.sh`脚本文件如下
```bash
#impala-shell -q "SELECT COUNT(*) FROM kudu_test.t_index_value_offline_month;"

#impala-shell -q
impala-shell -q "
SHOW DATABASES;
USE kudu_test;
SELECT COUNT(*) FROM t_index_value_offline_month;
"

```

执行完毕时的日志如下：
```sql
192.168.33.9 | CHANGED => {
    "changed": true,
    "rc": 0,
    "stderr": "Shared connection to 192.168.33.9 closed.\r\n",
    "stderr_lines": [
        "Shared connection to 192.168.33.9 closed."
    ],
    "stdout": "Starting Impala Shell without Kerberos authentication\r\nOpened TCP connection to cdh3.com:21000\r\nConnected to cdh3.com:21000\r\nServer version: impalad version 3.2.0-cdh6.2.0 RELEASE (build edc19942b4debdbfd485fbd26098eef435003f5d)\r\nQuery: SHOW DATABASES\r\n+------------------+----------------------------------------------+\r\n| name             | comment                                      |\r\n+------------------+----------------------------------------------+\r\n| _impala_builtins | System database for Impala builtin functions |\r\n| db_test          |                                              |\r\n| default          | Default Hive database                        |\r\n| hive_test        |                                              |\r\n| impala_demo      | 用于演 示impala使用的数据库                   |\r\n| impala_test      |                                              |\r\n| kudu_demo        | 用于演示kudu使用的数据库                     |\r\n| kudu_test        |                                              |\r\n+------------------+----------------------------------------------+\r\nFetched 8 row(s) in 0.01s\r\nQuery: USE kudu_test\r\nFetched 0 row(s) in 0.00s\r\nQuery: SELECT COUNT(*) FROM t_index_value_offline_month\r\nQuery submitted at: 2019-11-08 14:31:48 (Coordinator: http://cdh3.com:25000)\r\nQuery progress can be monitored at: http://cdh3.com:25000/query_plan?query_id=90445844e948f2fc:5df456c100000000\r\n+----------+\r\n| count(*) |\r\n+----------+\r\n| 96       |\r\n+----------+\r\nFetched 1 row(s) in 0.12s\r\n",
    "stdout_lines": [
        "Starting Impala Shell without Kerberos authentication",
        "Opened TCP connection to cdh3.com:21000",
        "Connected to cdh3.com:21000",
        "Server version: impalad version 3.2.0-cdh6.2.0 RELEASE (build edc19942b4debdbfd485fbd26098eef435003f5d)",
        "Query: SHOW DATABASES",
        "+------------------+----------------------------------------------+",
        "| name             | comment                                      |",
        "+------------------+----------------------------------------------+",
        "| _impala_builtins | System database for Impala builtin functions |",
        "| db_test          |                                              |",
        "| default          | Default Hive database                        |",
        "| impala_demo      | 用于演示impala使用的数据库                   |",
        "| kudu_demo        | 用于演示kudu使用的数据库                     |",
        "| kudu_test        |                                              |",
        "+------------------+----------------------------------------------+",
        "Fetched 6 row(s) in 0.01s",
        "Query: USE kudu_test",
        "Fetched 0 row(s) in 0.00s",
        "Query: SELECT COUNT(*) FROM t_index_value_offline_month",
        "Query submitted at: 2019-11-08 14:31:48 (Coordinator: http://cdh3.com:25000)",
        "Query progress can be monitored at: http://cdh3.com:25000/query_plan?query_id=90445844e948f2fc:5df456c100000000",
        "+----------+",
        "| count(*) |",
        "+----------+",
        "| 96       |",
        "+----------+",
        "Fetched 1 row(s) in 0.12s"
    ]
}
```

