关于CentOS7的密码
----

# 1 用户的创建与删除
```bash
# 1 创建用户
adduser 用户名
passwd 用户名

# 建工作组
groupadd 组名   
# 新建用户同时增加工作组
useradd -g 组名 用户名   


# 2 删除用户
userdel 用户名
groupdel 用户名
# 强制删除该用户的主目录和主目录下的所有文件和子目录）
# usermod –G 组名 用户名

## 2.1 完全删除用户
userdel -r 用户名


```

# 2 sudo 
```
# 3 赋予sudo权限。编辑系统 sudoers 文件
# 如果没有编辑权限，以root用户登录，赋予w权限
# chmod 640 /etc/sudoers
vi /etc/sudoers

# 大概在100行，在root下添加如下
escheduler  ALL=(ALL)       NOPASSWD: NOPASSWD: ALL

# 并且需要注释掉 Default requiretty 一行。如果有则注释，没有没有跳过
#Default requiretty

```


# 1 修改普通用户密码
```bash
# 1 root用户登录

# 2 查看当前所有用户，自己创建的用户，在最后
cat /etc/passwd

# 3 确定修改某个用户
passwd 某个用户
# 新密码
# 再次确认新密码

```

# 2 修改root用户密码
```bash
passwd 
# 输入root旧密码
# 设置新密码

```




# 3 找回 root 密码
重启CentOS 7 系统，在开机是，按`e`键（英文输入法下），进入启动编辑模式。

然后按向下键，找到以“Linux16”开头的行，在该行的最后面输入`init=/bin/sh`
```
fi
linux16 /vmlinuz-x.xx.x-xxx.x.x.el7.x86_64  root=xxx …… rhgb quiet LANG=zh_CN.UTF-8 init=/bin/sh
initrd16 /initramfs-x.xx.x-xxx.el7.x86_64.img

```

接下来按“ctrl+X”组合键进入单用户模式

然后输入“ls”查询当前位置（也可以省略），回车

接下来再输入`mount -o remount,rw /`，挂载文件系统为可写入。

然后再输入`passwd`，回车

接下来再输入`touch /.autorelabel`，回车

输入`exec /sbin/init`，回车

回车后出现下面的界面，这里稍微等几分钟，系统会自动重启

