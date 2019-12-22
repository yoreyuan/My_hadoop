
# 找回 add 后丢失的 文件

```bash
# 查看最近 60 次 add 的文件（悬挂的文件）。
#  根据时间确定某次的历史点 范围，例如查看 12月 14号 文件变动的历史点，结果以改动时间倒序输出，
Su-yuexi:My_hadoop yoreyuan$ find .git/objects -type f | xargs ls -lt | sed 60q | grep 'Dec 14'
-r--r--r--  1 yoreyuan  staff       15 Dec 14 14:09 .git/objects/8b/7c0aa0deb0422b334a623715d06a2ab0156448
-r--r--r--  1 yoreyuan  staff      163 Dec 14 09:59 .git/objects/b5/f514c04c785e19cac3c7155a43c55050e1cb41
-r--r--r--  1 yoreyuan  staff      408 Dec 14 09:59 .git/objects/25/6f7e903c19b3637ae0608a04b56930f7daadce
-r--r--r--  1 yoreyuan  staff      672 Dec 14 09:59 .git/objects/20/dcddc1fd9896c53a449c600f165de096042bba



# 查看某个变动节点的数据
#  将上面最后一列中的 .git/objects 删除，第三个字符的 / 删除，剩余的 40 位字符
#  cat 之后如果有多文件，会有文件唯一 字符串 和 文件名，再根据这个字符串值找回文件
#  尽管原始文件名暂时看不到，但是看到失而复得的文件内容，还是好开心，赶快保存起来
Su-yuexi:My_hadoop yoreyuan$ git cat-file -p 8b7c0aa0deb0422b334a623715d06a2ab0156448

```

# 回退的 某次提交
```bash
# 查看已提交的历史信息
git log --pretty=oneline

# 在 IDEA 的 Version Control 里查看提交的 log ，根据 提交的说明、提交者、日期， 确定回退的版本点
# 右键 Copy Revision Number，复制 版本id
# 回退一个版本,清空暂存区,将已提交的内容的版本恢复到本地,本地的文件也将被恢复的版本替换
#git reset --hard 14c63deeb947e7e3964365a632959c5ca89144af
# 回退一个版本,且会将暂存区的内容和本地已提交的内容全部恢复到未暂存的状态,不影响原来本地文件(未提交的也不受影响)
git reset 14c63deeb947e7e3964365a632959c5ca89144af

git reset --hard b5f514c04c785e19cac3c7155a43c55050e1cb41

 

```



Soft 
	Files won't change,differences will be staged for commit
Mixed
	Files wont't change,differences won't be staged
Hard
	Files will be reverted to thes state of the selected commit. Warning:any local changes will be lost.
Keep
	Files will be reverted to the state of the seleted commit, but local changes will be kept intact



