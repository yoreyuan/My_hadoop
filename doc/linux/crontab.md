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

