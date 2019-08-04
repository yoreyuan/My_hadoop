Elasticsearch
====
[ES 官网](https://www.elastic.co/cn/) | [ES GitHub](https://github.com/elastic/elasticsearch)

# 目录
* 1 ES 安装
    + 1.1 下载解压
    + 1.2 设置用户和权限
    + 1.3 配置
        - 配置 elasticsearch.yml
    + 1.4 启动
    + 1.5 Head 插件安装 
        - 1.5.1 npm安装
        - 1.5.2 如果没有安装 Git 先安装 Git
        - 1.5.3 下载 `elasticsearch-head`
        - 1.5.4 安装插件
        - 1.5.5 配置 Gruntfile.js 
        - 1.5.6 配置 _site/app.js
        - 1.5.7 启动访问
        - 1.5.8 定时监控Head 插件是否运行
    + 1.6 重启服务
        - 1.6.1 关闭服务
        - 1.6.2 修改配置
        - 1.6.3 启动服务
* 2 中文分词器
    + 2.1 安装 IK 分词器
    + 2.2 配置 IK 分词器(没特殊要求可以先不用配置)
    + 2.3 使用

<br/><br/>

# 1 安装
ES、Kibana、head安装请查看 [es+head+kibana.md](./es+head+kibana.md)。包含es密码设置、head密码设置

或者查看我们 blog [Elasticsearch 6.x安及其Kibana和head插件安装](https://blog.csdn.net/github_39577257/article/details/98426553)



## 1 中文分词使用
不指定分析类型 `analyzer` 时使用的是默认的分词器，

* 1 POST    http://cdh2:9200/yg/_analyze/
```json
{
 "text":"中华人民共和国"
}
```
result:
```json
{
    "tokens": [
        {
            "token": "中",
            "start_offset": 0,
            "end_offset": 1,
            "type": "<IDEOGRAPHIC>",
            "position": 0
        },
        {
            "token": "华",
            "start_offset": 1,
            "end_offset": 2,
            "type": "<IDEOGRAPHIC>",
            "position": 1
        },
        {
            "token": "人",
            "start_offset": 2,
            "end_offset": 3,
            "type": "<IDEOGRAPHIC>",
            "position": 2
        },
        {
            "token": "民",
            "start_offset": 3,
            "end_offset": 4,
            "type": "<IDEOGRAPHIC>",
            "position": 3
        },
        {
            "token": "共",
            "start_offset": 4,
            "end_offset": 5,
            "type": "<IDEOGRAPHIC>",
            "position": 4
        },
        {
            "token": "和",
            "start_offset": 5,
            "end_offset": 6,
            "type": "<IDEOGRAPHIC>",
            "position": 5
        },
        {
            "token": "国",
            "start_offset": 6,
            "end_offset": 7,
            "type": "<IDEOGRAPHIC>",
            "position": 6
        }
    ]
}
```

* 2 POST    http://cdh2:9200/yg/_analyze/
```json
{
 "text":"中华人民共和国",
 "analyzer":"ik_smart"
}
```
result:
```json
{
   "tokens": [
       {
           "token": "中华人民共和国",
           "start_offset": 0,
           "end_offset": 7,
           "type": "CN_WORD",
           "position": 0
       }
   ]
}
```

* 3 POST    http://cdh2:9200/yg/_analyze/
```json
{
  "text":"中华人民共和国",
  "analyzer":"ik_max_word"
}
```
result:
```json
{
  "tokens": [
    {
      "token": "中华人民共和国",
      "start_offset": 0,
      "end_offset": 7,
      "type": "CN_WORD",
      "position": 0
    },
    {
      "token": "中华人民",
      "start_offset": 0,
      "end_offset": 4,
      "type": "CN_WORD",
      "position": 1
    },
    {
      "token": "中华",
      "start_offset": 0,
      "end_offset": 2,
      "type": "CN_WORD",
      "position": 2
    },
    {
      "token": "华人",
      "start_offset": 1,
      "end_offset": 3,
      "type": "CN_WORD",
      "position": 3
    },
    {
      "token": "人民共和国",
      "start_offset": 2,
      "end_offset": 7,
      "type": "CN_WORD",
      "position": 4
    },
    {
      "token": "人民",
      "start_offset": 2,
      "end_offset": 4,
      "type": "CN_WORD",
      "position": 5
    },
    {
      "token": "共和国",
      "start_offset": 4,
      "end_offset": 7,
      "type": "CN_WORD",
      "position": 6
    },
    {
      "token": "共和",
      "start_offset": 4,
      "end_offset": 6,
      "type": "CN_WORD",
      "position": 7
    },
    {
      "token": "国",
      "start_offset": 6,
      "end_offset": 7,
      "type": "CN_CHAR",
      "position": 8
    },
    {
      "token": "国徽",
      "start_offset": 7,
      "end_offset": 9,
      "type": "CN_WORD",
      "position": 9
    }
  ]
}
```













