
# 1 准备数据

## 1.1 Hive
```sql
-- 建表
CREATE TABLE `flower_name` (
  `id` int COMMENT '主键',
  `date` varchar(110) COMMENT '日期',
  `en_name` varchar(128) COMMENT '英文名',
  `name` varchar(128) COMMENT '花名',
  `utterance` varchar(255) COMMENT '含义'
)ROW FORMAT DELIMITED FIELDS TERMINATED BY '|' 
STORED AS TEXTFILE;

-- 如下数据文件，以 | 分割的文本数据
1|1月1日|雪莲花|Snow Drop|81B60DA28D5DEF0B8F735D1CBA56A4E5
2|1月2日|黄水仙|Narcisus Jonquilla|BAEE91C1CAFB8D7060F606843298C424
3|1月3日|藏红花|Spring Crocus|1ED48705D0020CE92130DFE3BBE37852
4|1月4日|风信子|Hyacinth|7916CCFA2E24A9AB08C620F5ABBEEFD2
5|1月5日|雪割草|hepatica|EE4DDBC6D4CE93499A37F4A93EBD1DDB
6|1月6日|白色紫罗兰|Violet|DD7F5675C6C2D9A1B5B9F7A7764A303EB43854DB83A3EE5FEA49ADB46E2F7F6B
7|1月7日|白色郁金香|Tulip|A9E978CEAF80344304487C4384609C53
8|1月8日|紫色紫罗兰|Violet|8D4C956F2E4C8A9A5B286221DFD0ECF6
9|1月9日|黄色紫罗兰|Voilet|D7B297C136E2CEA3457C6E7C72993210
10|1月10日|黄杨|Box-Tree|4134826A9117E0169E1F58607D0FB967
11|1月11日|匀桧叶|Arbor-Vitae|78C8A5BD91B0B46944DF8F5B817718D8
12|1月12日|庭荠|Sweet Alyssum|344FF3CBB0C99E82B04BA2BDC782814B
13|1月13日|水仙|Narcissus|F0255F21D8DDF2DD1B0F310297AF4E6B
14|1月14日|报春花|Cyclamen|FBA715DBED589345FAC5D798B1AE0D7E
15|1月15日|剌竹|Thorn|00D5A273C1A47851064DB3600E7F4D43
16|1月16日|黄色风信子|Hyacinth|3DB90590EEEFA2F16AA6A83975F24E77
17|1月17日|酸模|Sorrel|AEF74CA958A3BED52E12394C367B329A
18|1月18日|印度锦葵|Indian Mallow|BAFA1A30FC4F494E32A8134284AAB2F4
19|1月19日|松|Pine|D4591D56BAD2DD460A9D94C859576A4A
20|1月20日|金凤花|Butter Cup|1F63DD3F25E38A6492F955758FA4761B
21|1月21日|常春藤|Ivy|E4E7CEA6348A346201D04BD0A922A287
22|1月22日|苔藓|Moss|F372A370BB2204A1C8B8B56A2AB076C5
23|1月23日|芦荟|Bulrush|259F62C87A8B05B2CE55F37CE2919F46
24|1月24日|番红花|Saffron Crocus|61E2F3C526C33134FFC1DF375789AC8D
25|1月25日|鼠耳草|Cerasrium|444DBD65A8266BBB71AFEB0AF3E44C3F
26|1月26日|含羞草|Humble Plant|30AB4150B2063F4E6C6BF27371191860
27|1月27日|七度灶|Mountain Ash|00DBCE714104EBDF455BD426B2F811B1
28|1月28日|黑色白杨木|Black Piolar|8BA91B432FBAD26736B2372C65E13FB5
29|1月29日|苔藓|Moss|DD64FE4645DFD4A8A5A788E94B67428F
30|1月30日|金盏花|Marsh Marigold|73A1ABB30A8D7E11E2A7BE8600273F2D
31|1月31日|黄色藏红花|Spring Crocus|129E46A29C3F1E9E9F689B9EE29798D5

-- 将数据导入 Hive。从本地到如到表
LOAD DATA LOCAL INPATH '/home/yore/data/flower_name.txt'  OVERWRITE INTO TABLE flower_name;
```

## 1.2 MySQL 

```sql
-- 建表
CREATE TABLE `flower_name` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
  `date` varchar(110) COLLATE utf8_bin DEFAULT NULL COMMENT '日期',
  `en_name` varchar(128) COLLATE utf8_bin NOT NULL COMMENT '英文名',
  `name` varchar(128) COLLATE utf8_bin NOT NULL COMMENT '花名',
  `utterance` varchar(255) COLLATE utf8_bin DEFAULT NULL COMMENT '含义',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=367 DEFAULT CHARSET=utf8 COLLATE=utf8_bin;


-- 插入测试数据
INSERT INTO flower_name(id, date, name, en_name, utterance) VALUES(1, '1月1日', '雪莲花', 'Snow Drop', '81B60DA28D5DEF0B8F735D1CBA56A4E5');
INSERT INTO flower_name(id, date, name, en_name, utterance) VALUES(2, '1月2日', '黄水仙', 'Narcisus Jonquilla', 'BAEE91C1CAFB8D7060F606843298C424');
INSERT INTO flower_name(id, date, name, en_name, utterance) VALUES(3, '1月3日', '藏红花', 'Spring Crocus', '1ED48705D0020CE92130DFE3BBE37852');
INSERT INTO flower_name(id, date, name, en_name, utterance) VALUES(4, '1月4日', '风信子', 'Hyacinth', '7916CCFA2E24A9AB08C620F5ABBEEFD2');
INSERT INTO flower_name(id, date, name, en_name, utterance) VALUES(5, '1月5日', '雪割草', 'hepatica', 'EE4DDBC6D4CE93499A37F4A93EBD1DDB');
INSERT INTO flower_name(id, date, name, en_name, utterance) VALUES(6, '1月6日', '白色紫罗兰', 'Violet', 'DD7F5675C6C2D9A1B5B9F7A7764A303EB43854DB83A3EE5FEA49ADB46E2F7F6B');
INSERT INTO flower_name(id, date, name, en_name, utterance) VALUES(7, '1月7日', '白色郁金香', 'Tulip', 'A9E978CEAF80344304487C4384609C53');
INSERT INTO flower_name(id, date, name, en_name, utterance) VALUES(8, '1月8日', '紫色紫罗兰', 'Violet', '8D4C956F2E4C8A9A5B286221DFD0ECF6');
INSERT INTO flower_name(id, date, name, en_name, utterance) VALUES(9, '1月9日', '黄色紫罗兰', 'Voilet', 'D7B297C136E2CEA3457C6E7C72993210');
INSERT INTO flower_name(id, date, name, en_name, utterance) VALUES(10, '1月10日', '黄杨', 'Box-Tree', '4134826A9117E0169E1F58607D0FB967');
INSERT INTO flower_name(id, date, name, en_name, utterance) VALUES(11, '1月11日', '匀桧叶', 'Arbor-Vitae', '78C8A5BD91B0B46944DF8F5B817718D8');
INSERT INTO flower_name(id, date, name, en_name, utterance) VALUES(12, '1月12日', '庭荠', 'Sweet Alyssum', '344FF3CBB0C99E82B04BA2BDC782814B');
INSERT INTO flower_name(id, date, name, en_name, utterance) VALUES(13, '1月13日', '水仙', 'Narcissus', 'F0255F21D8DDF2DD1B0F310297AF4E6B');
INSERT INTO flower_name(id, date, name, en_name, utterance) VALUES(14, '1月14日', '报春花', 'Cyclamen', 'FBA715DBED589345FAC5D798B1AE0D7E');
INSERT INTO flower_name(id, date, name, en_name, utterance) VALUES(15, '1月15日', '剌竹', 'Thorn', '00D5A273C1A47851064DB3600E7F4D43');
INSERT INTO flower_name(id, date, name, en_name, utterance) VALUES(16, '1月16日', '黄色风信子', 'Hyacinth', '3DB90590EEEFA2F16AA6A83975F24E77');
INSERT INTO flower_name(id, date, name, en_name, utterance) VALUES(17, '1月17日', '酸模', 'Sorrel', 'AEF74CA958A3BED52E12394C367B329A');
INSERT INTO flower_name(id, date, name, en_name, utterance) VALUES(18, '1月18日', '印度锦葵', 'Indian Mallow', 'BAFA1A30FC4F494E32A8134284AAB2F4');
INSERT INTO flower_name(id, date, name, en_name, utterance) VALUES(19, '1月19日', '松', 'Pine', 'D4591D56BAD2DD460A9D94C859576A4A');
INSERT INTO flower_name(id, date, name, en_name, utterance) VALUES(20, '1月20日', '金凤花', 'Butter Cup', '1F63DD3F25E38A6492F955758FA4761B');
INSERT INTO flower_name(id, date, name, en_name, utterance) VALUES(21, '1月21日', '常春藤', 'Ivy', 'E4E7CEA6348A346201D04BD0A922A287');
INSERT INTO flower_name(id, date, name, en_name, utterance) VALUES(22, '1月22日', '苔藓', 'Moss', 'F372A370BB2204A1C8B8B56A2AB076C5');
INSERT INTO flower_name(id, date, name, en_name, utterance) VALUES(23, '1月23日', '芦荟', 'Bulrush', '259F62C87A8B05B2CE55F37CE2919F46');
INSERT INTO flower_name(id, date, name, en_name, utterance) VALUES(24, '1月24日', '番红花', 'Saffron Crocus', '61E2F3C526C33134FFC1DF375789AC8D');
INSERT INTO flower_name(id, date, name, en_name, utterance) VALUES(25, '1月25日', '鼠耳草', 'Cerasrium', '444DBD65A8266BBB71AFEB0AF3E44C3F');
INSERT INTO flower_name(id, date, name, en_name, utterance) VALUES(26, '1月26日', '含羞草', 'Humble Plant', '30AB4150B2063F4E6C6BF27371191860');
INSERT INTO flower_name(id, date, name, en_name, utterance) VALUES(27, '1月27日', '七度灶', 'Mountain Ash', '00DBCE714104EBDF455BD426B2F811B1');
INSERT INTO flower_name(id, date, name, en_name, utterance) VALUES(28, '1月28日', '黑色白杨木', 'Black Piolar', '8BA91B432FBAD26736B2372C65E13FB5');
INSERT INTO flower_name(id, date, name, en_name, utterance) VALUES(29, '1月29日', '苔藓', 'Moss', 'DD64FE4645DFD4A8A5A788E94B67428F');
INSERT INTO flower_name(id, date, name, en_name, utterance) VALUES(30, '1月30日', '金盏花', 'Marsh Marigold', '73A1ABB30A8D7E11E2A7BE8600273F2D');
INSERT INTO flower_name(id, date, name, en_name, utterance) VALUES(31, '1月31日', '黄色藏红花', 'Spring Crocus', '129E46A29C3F1E9E9F689B9EE29798D5');

```


# 编译打包
```bash
# mvn clean package -Dmaven.test.skip=true -Dmaven.javadoc.skip=true
mvn clean package -Dmaven.test.skip=true

```



