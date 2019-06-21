Linux 定时任务 Crontab 命令
---

linux 系统则是由 cron (crond) 这个系统服务来控制的。
Linux 系统上面原本就有非常多的计划性工作，因此这个系统服务是默认启动的。

Linux下的任务调度分为两类:
* 系统任务调度
* 用户任务调度

```bash
[root@cdh etc]# cat /etc/crontab
SHELL=/bin/bash
PATH=/sbin:/bin:/usr/sbin:/usr/bin
MAILTO=root
# For details see man 4 crontabs
# Example of job definition:
# .---------------- minute (0 - 59)
# |  .------------- hour (0 - 23)
# |  |  .---------- day of month (1 - 31)
# |  |  |  .------- month (1 - 12) OR jan,feb,mar,apr ...
# |  |  |  |  .---- day of week (0 - 6) (Sunday=0 or 7) OR sun,mon,tue,wed,thu,fri,sat
# |  |  |  |  |
# *  *  *  *  * user-name  command to be executed
```
第三行 `MAILTO` 变量指定了 crond 的任务执行信息将通过电子邮件发送给root用户，如果 MAILTO 变量的值为空，则表示不发送任务 执行信息给用户.

常用命令
```bash
# 累出 crontab 文件
 crontab -l
```


# 使用
比如用 `crontab` 来定时执行脚本，监控某个程序的运行状态，如果程序没有运行，则启动程序

脚本如下，例如创建一个`monitoring.sh`脚本，内容如下
```bash
#!/bin/bash

# 监控某个进程的脚本
#######

ps -fe|grep grunt |grep -v grep
if [ $? -ne 0 ]
then
	now_date=`date +"%Y-%m-%d %H:%M:%S"`
	echo -e "$now_date \t elasticsearch-head 挂起，尝试重启"
	# 必须带这个目录下执行，否则启动失败。
	cd /usr/local/elasticsearch-head
	npm run start >/dev/null 2>&1 &
#else
#echo "runing....."
fi
##### 
# grunt 表示进程特征字符串，能够查询到唯一进程的特征字符串
# 0表示存在的
# $? -ne 0 不存在，$? -eq 0 存在
```

每隔3分钟监测一次，执行`crontab -e`，输入`i` 插入如下内容
```bash
*/3 * * * * /bin/bash /home/es/monitoring.sh >> /var/log/es/monitoring.log
```
`esc`退出编辑模式，`:wq`保存。



