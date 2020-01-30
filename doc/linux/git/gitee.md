Gitee - 码云
-------

[官网](https://gitee.com/)


# GiteeTree
Gitee 目录树插件 [开源中国 / GiteeTree](https://gitee.com/oschina/GitCodeTree)

```bash
# 1 下载。可以下载源码编译，这里直接下载官方提供的预先构建的包
#  浏览器访问，https://gitee.com/oschina/GitCodeTree/tree/master/dist
wget https://gitee.com/oschina/GitCodeTree/raw/master/dist/chrome.zip

# 2 解压
mkdir GitCodeTree
unzip chrome.zip -d ./GitCodeTree

# 3 添加到 Chrome 浏览器的扩展插件中
#  打开 chrome://extensions/
#  或者 设置 -> 更多工具（L）-> 扩展程序（E） 
#  点击 【加载以解压的扩展程序】

# 4 删除
#  如果插件原始文件不再使用，可以选择删除
rm chrome.zip

```

# 简易的命令行入门
```bash
# Git 全局设置:
git config --global user.name "yoreyuan"
git config --global user.email "suyuexi3@gmail.com"


# 创建 git 仓库:
mkdir BJMR
cd BJMR
git init
touch README.md
git add README.md
git commit -m "first commit"
git remote add origin https://gitee.com/quhe_work/BJMR.git
git push -u origin master


# 已有仓库?
cd existing_git_repo
git remote add origin https://gitee.com/quhe_work/BJMR.git
git push -u origin master

```


# 命令

提交代码
```bash
# -f 是 --force 缩写，但通常不这样使用
git push -u origin2 master -f


# 查看某个用户的代码提交量
# 结果如:   added lines: 14568, removed lines: 2041, total lines: 12527
git log --author="yoreyuan" --pretty=tformat: --numstat | awk '{ add += $1; subs += $2; loc += $1 - $2 } END { printf "added lines: %s, removed lines: %s, total lines: %s\n", add, subs, loc }' -


```

