关于CentOS7的密码
----

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

