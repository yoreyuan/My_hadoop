package yore;

import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.Random;

/**
 * Created by yore on 2019/09/22 10:02
 */
public class CreateData {

    // 数据的输出路径，保存到项目的 resources 下
    final static String OUT_PATH = "kylin/kylin-demo/src/main/resources/fact_data.txt";
    // 随机生成的数据条数
    final static Integer rowNum = 100;

    public static void main(String[] args) throws Exception{
        OutputStreamWriter out = new OutputStreamWriter(
                new FileOutputStream(OUT_PATH, true), "UTF-8");


        for(int i=0; i<rowNum; i++){
            System.out.println(i + " data. ...");
            out.write(new FactDataEntity() + "\n");
            out.flush();
        }

        out.close();

    }


    /**
     * 生成模拟实时表数据的实体类，
     * 生成的数据格式如下：
     *      2017-07-25|HGMBH1U0Y7O4Y7WM6T|G03|G0301|8040|Mac OS|3
     */
    static class FactDataEntity implements java.io.Serializable{
        private static final long serialVersionUID = 6763645175767982929L;

        final String[] cookidId = {
                "0", "1", "2", "3", "4", "5", "6", "7", "8", "9",
                "A", "B", "C", "D", "E", "F", "G", "H", "I", "J",
                "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T",
                "U", "V", "W", "X", "Y", "Z"
        };
        final String[] regionId = {"G01", "G02", "G03", "G04", "G05"};
        final String[] osId = {"Android 5.0", "Mac OS", "Window 7"};
        final Random random = new Random();

        private String tempDate = null;
        private String cookieIdTemp = "";
        private String regionTemp = null;
        private String cityTemp = null;
        private String sidTemp = null;
        private String osTemp = null;
        private String pvTemp = null;

        public FactDataEntity() {
            // 随机生成日期
            tempDate = "2016-03-" + String.format("%02d", 1+random.nextInt(31));

            // 随机生成18位长的 cookieIdTemp
            for(int j=0;j<18; j++) {
                cookieIdTemp += cookidId[random.nextInt(cookidId.length)];
            }

            // 区域标识
            regionTemp = regionId[random.nextInt(regionId.length)];

            // 每个区域随机生成2个城市标识。从01开始
            cityTemp = regionTemp + "0" + (1 + random.nextInt(2));

            //
            sidTemp = "" + random.nextInt(10000);

            // OS
            osTemp = osId[random.nextInt(osId.length)];

            pvTemp = "" + (1+random.nextInt(9));
        }

        @Override
        public String toString() {
            return tempDate + '|' +
                    cookieIdTemp + '|' +
                    regionTemp + '|' +
                    cityTemp + '|' +
                    sidTemp + '|' +
                    osTemp + '|' +
                    pvTemp;
        }
    }

}
