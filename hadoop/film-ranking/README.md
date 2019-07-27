电影数据分析
==========

# 1. 关于数据
也许我们天生对电影比较喜爱，Spark对数据集的分析也多采用电影数据，可以使用`movielens`的电影数据，
下载链接为 [/datasets/movielens](http://files.grouplens.org/datasets/movielens/)

为了更加的有趣，我们手动自己爬取一份数据，对感兴趣的点进行分析。豆瓣是比较有名的一个社区网站，发布的电影排行还是比较可靠的，
因此主要爬取分析[豆瓣电影 Top 250](https://movie.douban.com/top250)数据。仅用于学习使用，切勿恶意爬取豆瓣。

# 2. 数据格式

执行[CrawlMovie](src/main/java/yore/etl/CrawlMovie.java)，生成的豆瓣电影数据(movie)格式如下：
```
电影标识, 电影名, 导演, 编剧, 主演, 类型, 地区, 语言, 上映日期, 片长, 又名, IMDb链接, 评分, 评分人数, url
1292401,真爱至上 Love Actually (2003),理查德·柯蒂斯,理查德·柯蒂斯,休·格兰特 / 科林·费尔斯 / 艾玛·汤普森 / 凯拉·奈特莉 / 连姆·尼森 / 托马斯·布罗迪-桑斯特 / 比尔·奈伊 / 马丁·弗瑞曼 / 劳拉·琳妮 / 艾伦·瑞克曼 / 克里斯·马歇尔 / 罗德里戈·桑托罗 / 罗温·艾金森 / 比利·鲍伯·松顿 / 玛汀·麦古基安 / 安德鲁·林肯 / 露西娅·莫尼斯 / 海克·玛卡琪,剧情 / 喜剧 / 爱情,英国 / 美国 / 法国,英语 / 葡萄牙语 / 法语,2003-09-07(多伦多电影节) / 2003-11-06(美国) / 2003-11-21(英国),135 分钟,爱是您，爱是我(台) / 真的恋爱了(港) / 真情角落 / 真爱 / Love Actually Is All Around,tt0314331,8.6,427111,https://movie.douban.com/subject/1292401/
3792799,岁月神偷 歲月神偷 (2010),罗启锐,罗启锐 / 张婉婷,吴君如 / 任达华 / 钟绍图 / 李治廷 / 蔡颖恩 / 秦沛 / 夏萍 / 谷德昭 / 许鞍华 / 张同祖 / 庄域飞 / 威廉希路 / 李健兴 / 林耀祺 / 廖爱玲 / 张翼东 / 钱耀荣 / 谭瓒强 / 陈庆航 / 黎祥荣,剧情 / 家庭,香港 / 中国大陆,粤语 / 汉语普通话 / 英语 / 上海话 / 法语,2010-04-16(中国大陆) / 2010-02-14(柏林电影节) / 2010-03-11(香港),117分钟 / 120分钟(柏林电影节),1969太空漫游 / Echoes Of The Rainbow,tt1602572,8.7,391421,https://movie.douban.com/subject/3792799/

```

执行[CrawRankQuote](src/main/java/yore/etl/CrawRankQuote.java)，生成的豆瓣排名语录数据quote格式如下：
```
电影标识,排名,语录
1292401,144,爱，是个动词。
3792799,135,岁月流逝，来日可追。
```

# 3. Hadoop
将获取到的两份数据上传到HDFS的 /home/douban 目录下，

## 3.1 需求
获取评分最高且评论人数最多的电影评语。

## 3.2 分析
如果对SQL比较熟悉的，大概已经知道，这个需求分为两大部分，
* 第一步分是Join，根据需要的信息，从两份数据中提取信息根据id关联为一份数据
* 第二部分是排序，先根据评分降序，再根据评论人数降序

对应到Hadoop，需要有两个Job，第一个Job实现两份文件的Join；地二个Job对关联后的数据进行排序。

中间用的Hadoop的知识点有：MapReduc、多文件输入、二次排序、多Job关联。

## 3.3 代码实现
[MovieDriver](src/main/java/yore/mr/MovieDriver.java)

运行后的结果如下：
```
1292052,肖申克的救赎 The Shawshank Redemption (1994),9.7,1502851,1,希望让人自由。,
1291546,霸王别姬 (1993),9.6,1112641,2,风华绝代。,
1296141,控方证人 Witness for the Prosecution (1957),9.6,195362,29,比利·怀德满分作品。,
1292063,美丽人生 La vita è bella (1997),9.5,690618,5,最美的谎言。,
1295124,辛德勒的名单 Schindler's List (1993),9.5,613865,8,拯救一个人，就是拯救整个世界。,
1295644,这个杀手不太冷 Léon (1994),9.4,1363430,3,怪蜀黍和小萝莉不得不说的故事。,
1292720,阿甘正传 Forrest Gump (1994),9.4,1178003,4,一部美国近现代史。,
1292722,泰坦尼克号 Titanic (1997),9.4,1119405,7,失去的才是永恒的。,
1293182,十二怒汉 12 Angry Men (1957),9.4,253408,36,1957年的理想主义。,
1291561,千与千寻 千と千尋の神隠し (2001),9.3,1205228,6,最好的宫崎骏，最好的久石让。,
```

# 4 Hive
## 4.1 创建表并将数据导入
创建两张表，
```sql
--电影表
CREATE EXTERNAL TABLE movie(
 id BIGINT COMMENT '电影标识',
 movie_name VARCHAR(255) COMMENT '电影名',
 director VARCHAR(128) COMMENT '导演',
 scriptwriter VARCHAR(128) COMMENT '编剧',
 protagonist STRING COMMENT '主演',
 kind VARCHAR(64) COMMENT '类型',
 country VARCHAR(32) COMMENT '地区',
 language VARCHAR(32) COMMENT '语言', 
 release_date VARCHAR(64) COMMENT '上映日期',
 mins VARCHAR(16) COMMENT '片长',
 alternate_name VARCHAR(128) COMMENT '又名',
 imdb VARCHAR(128) COMMENT 'IMDb链接',
 rating_num FLOAT  COMMENT '评分',
 rating_people BIGINT COMMENT '评分人数',
 url VARCHAR(255)COMMENT 'url'
)ROW FORMAT DELIMITED FIELDS TERMINATED BY ','
STORED AS TEXTFILE 
--location '/user/hive/warehouse/test.db';
;
--textfile:行式存储，默认的存储格式,将所有类型的数据都存储为String类型。不便于数据的解析，也不具备随机读写的能力，但它却比较通用；stored as textfile;
--sequencefile:行式存储。每条record包含record legth、key length、key、value等四部分。多了冗余信息，所以存储相同的数据，sequencefile比textfile略大。但正是因为这些冗余信息，使其具备随机读写的能力。该存储格式压缩时压缩只有value。 stored as sequencefile;
--rcfile:facebook开源，比标准行式存储节约10%的空间。先水平划分按行存储的row group,row group中再按列存储数据。尽管支持支持随机读写，但依然由于存储性能问题没啥大用处。stored as rcfile
--orc:优化过后的RCFile。可作用于表或者表的分区。在水平上划分为多个按行存储的Stripe(250MB)。Row Data中再按列存储数据。Index Data记录的是整型数据最大值最小值、字符串数据前后缀信息，每个列的位置等等。
--    这就使得查询十分得高效，默认每一万行数据建立一个Index Data。ORC存储大小为TEXTFILE的40%左右，使用压缩则可以进一步将这个数字降到10%~20%。orc默认使用zlip的压缩格式，可以支持zlip和snappy压缩格式。
--    不配压缩测试：stored as orc  tblproperties("orc.compress"="none") 
--    配压缩测试：tored as orcfile，
--    查看Hadoop支持的压缩格式： hadoop checknative
--    设置hive的压缩形式： hive>set orc.compression=zlip; 或者建表时：properties("orc.compress"="zlip")
--parquet：存储大小为TEXTFILE的60%~70%，压缩后在20%~30%之间。

--加载数据到movie表
LOAD DATA local INPATH '/home/douban/doubanMovie.csv' OVERWRITE INTO TABLE movie;

-- 排名与评语表
CREATE external TABLE quote(
 id BIGINT COMMENT '电影标识',
 rank BIGINT COMMENT '排名',
 quote STRING COMMENT '语录'
)ROW FORMAT DELIMITED FIELDS TERMINATED BY ',';

--加载数据到quote表
LOAD DATA local INPATH '/home/douban/rankQuote.csv' OVERWRITE INTO TABLE quote;

```

## 4.2 分析
```
--获取评分最高且评论人数最多的电影评语。order by 和 sort by
0: jdbc:hive2://cdh6:10000> SELECT m.id,m.movie_name,m.rating_num,m.rating_people,q.rank,q.quote FROM movie m LEFT JOIN quote q ON q.id=m.id ORDER BY m.rating_num desc,m.rating_people desc limit 10;

+----------+------------------------------------------+---------------+------------------+---------+------------------+
|   m.id   |               m.movie_name               | m.rating_num  | m.rating_people  | q.rank  |     q.quote      |
+----------+------------------------------------------+---------------+------------------+---------+------------------+
| 1292052  | 肖申克的救赎 The Shawshank Redemption (1994)   | 9.7           | 1502851          | 1       | 希望让人自由。          |
| 1291546  | 霸王别姬 (1993)                              | 9.6           | 1112641          | 2       | 风华绝代。            |
| 1296141  | 控方证人 Witness for the Prosecution (1957)  | 9.6           | 195362           | 29      | 比利·怀德满分作品。       |
| 1292063  | 美丽人生 La vita è bella (1997)              | 9.5           | 690618           | 5       | 最美的谎言。           |
| 1295124  | 辛德勒的名单 Schindler's List (1993)           | 9.5           | 613865           | 8       | 拯救一个人，就是拯救整个世界。  |
| 1295644  | 这个杀手不太冷 Léon (1994)                      | 9.4           | 1363430          | 3       | 怪蜀黍和小萝莉不得不说的故事。  |
| 1292720  | 阿甘正传 Forrest Gump (1994)                 | 9.4           | 1178003          | 4       | 一部美国近现代史。        |
| 1292722  | 泰坦尼克号 Titanic (1997)                     | 9.4           | 1119405          | 7       | 失去的才是永恒的。        |
| 1293182  | 十二怒汉 12 Angry Men (1957)                 | 9.4           | 253408           | 36      | 1957年的理想主义。      |
| 1291561  | 千与千寻 千と千尋の神隠し (2001)                     | 9.3           | 1205228          | 6       | 最好的宫崎骏，最好的久石让。   |
+----------+------------------------------------------+---------------+------------------+---------+------------------+

```


