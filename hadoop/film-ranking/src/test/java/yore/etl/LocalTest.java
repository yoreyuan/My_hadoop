package yore.etl;

import java.io.BufferedReader;
import java.io.OutputStreamWriter;

/**
 * Created by yore on 2019/7/25 04:14
 */
public class LocalTest {

//    @Test
    public void localAnalyse() throws Exception{

        BufferedReader in = Utils.getFileReader("/Users/yoreyuan/Downloads/doubanMovie.csv");
        OutputStreamWriter out = Utils.getOutPutStreamWriter("/Users/yoreyuan/Downloads/doubanMovieWithId.csv");

        String line = "";
        while ( (line=in.readLine())!=null ) {
            String[] lineArr = line.split(",");
            //TODO 本地分析

            out.write(Utils.getIdByDoubanURL(lineArr[lineArr.length-1]) + "," + line + "\n");
        }
        out.close();
        in.close();
    }


}
