package yore;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.sun.xml.internal.ws.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.math.BigDecimal;
import java.util.*;

/**
 *
 * java -cp gen-dataset.jar yore.Target  totalNum partNum scale path
 *
 * {\"totalNum\":1,\"partNum\":1,\"scale\":0.5,\"path\":\"/home\"}
 * {'totalNum':60, 'partNum': 2,'scale':[0.5, 0.1],'path':'/home'}
 *
 *
 *
 * Created by yore on 2019/11/23 16:34
 */
public class Target {
    private static Logger LOG = LoggerFactory.getLogger(Target.class);
    static StringBuffer sb = new StringBuffer();
    static {
        sb.append("------------------- 请输入正确的参数：\n");
        sb.append(" {'totalNum':1,'partNum':1,'scale':0.5,'path':'/home'} \n");
        sb.append(" \t\t totalNum : 生成的主表的数据量，默认为1 \n");
        sb.append(" \t\t partNum : 生成辅表的数量，默认为1 \n ");
        sb.append(" \t\t scale : 生成的辅表的数据量相比于 totalNum 的值 \n");
        sb.append(" \t\t path : 生成数据的路径 \n");
    }

    public static void main(String[] args) throws IOException {
        System.out.println("输入的参数为：" + args[0]);
        System.out.println("---- rowNum  colNum  path ----  或者");
        System.out.println("---- {'totalNum':60, 'partNum': 2,'scale':[0.5, 0.1],'path':'/home'} ----");
        LOG.info(sb.toString());

        String path = "/home"; // 默认值
//        path = "/Users/yoreyuan/Downloads";


        if(args.length>1){
            System.out.println("\t …… 生成" + args[0] + "*" + args[1] + "的CSV数据文件");

            path = args[2]==null? path: args[2];
            OutputStreamWriter out = FileUtils.getOutPutStreamWriter(path + "/row" + args[0] + "_col" + args[1] + "_column.csv");
            for(int i=0;i<Integer.parseInt(args[0]);i++){
                TagetP1Entity t1 = new TagetP1Entity();

                out.write(t1.toString());
                for(int j=3; j<Integer.parseInt(args[1]); j++){
                    TagetP2Entity t2 = new TagetP2Entity();
                    out.write("," + t2.getIndicator2());
                }
                out.write("\n");
                out.flush();
            }

        }else{
            JSONObject argJsonObj= new JSONObject();
            List<OutputStreamWriter> outList = new ArrayList<OutputStreamWriter>();
            List<Integer> scaleList = new ArrayList<>();

            int totalNum = 1, partNum = 1;

            try {
                argJsonObj = JSON.parseObject(args[0]);
                JSONArray scale = new JSONArray();
                System.out.println(argJsonObj);

                totalNum = argJsonObj.getInteger("totalNum");
                partNum = argJsonObj.getInteger("partNum");
                scale = argJsonObj.getJSONArray("scale");
                path = argJsonObj.getString("path");

                outList.add(FileUtils.getOutPutStreamWriter(path + "/target_"+totalNum+".csv"));
                for(Object s : scale){
                    BigDecimal s2 = (BigDecimal)s;
                    int partTotalNum = (int)Math.floor(totalNum * s2.doubleValue());
                    scaleList.add(partTotalNum);
                    outList.add(FileUtils.getOutPutStreamWriter(path + "/target_part_" + partTotalNum+".csv"));
                }
            }catch (Exception e){
                e.printStackTrace();
                LOG.error(sb.toString());
            }

            List<Integer> partIndex = new ArrayList<Integer>(){{
                addAll(scaleList);
            }};

            /**
             * 生成主表的数据
             */
            for(int i=0; i<totalNum; i++){
                TagetP1Entity t1 = new TagetP1Entity();


                outList.get(0).write(t1.toString() + "\n");
                outList.get(0).flush();

                for(int j=0; j<partIndex.size(); j++){
                    TagetP2Entity t2 = new TagetP2Entity(t1.getId());
                    if(i<partIndex.get(j)) outList.get(j+1).write(t2.toString() + "\n");
                    outList.get(j+1).flush();
                }
            }

            // 关闭资源
            outList.forEach(out -> {
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

        }
    }

}
