package yore.etl;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.commons.lang.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.io.OutputStreamWriter;

/**
 * 获取电影的经典语录。
 *
 * 所使用的jsoup语法可以参考
 * <a href="https://www.open-open.com/jsoup/selector-syntax.htm">使用选择器语法来查找元素</a> &nbsp;
 * <a href="https://blog.csdn.net/championhengyi/article/details/68491306">使用Jsoup的select语法进行元素查找</a>
 *
 * Created by yore on 2019/7/24 23:46
 */
public class CrawlRankQuote {

    private static final String URL = "https://movie.douban.com/top250";
    public static final String OUT_PATH = "/Users/yoreyuan/Downloads/rankQuote.csv";


    public static void main(String[] args) throws IOException  {

        OkHttpClient okHttpClient = new OkHttpClient();
        OutputStreamWriter out = Utils.getOutPutStreamWriter(OUT_PATH);

        for(int i=0; i < 250 ; i++){
            if(i%25==0){
                Request request = new Request.Builder()
                        .url(URL+"?start=" + i)
                        .get()
                        .build();
                Response response = okHttpClient.newCall(request).execute();
                if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

                String html = response.body().string().replace(",", "，");

                for(Element element : Jsoup.parse(html).body().getElementById("content").select(".grid-16-8.clearfix .grid_view > li")){
                    RankQuoteEntity rankQuoteEntity = new RankQuoteEntity();

                    // select 语法可以参考 https://jsoup.org/cookbook/extracting-data/selector-syntax
                    String rank = element.select(".pic>em").text();
                    String href = element.select(".pic>a").attr("href");
                    String quote = element.select(".bd > .quote").text();

                    rankQuoteEntity.setId(Utils.getIdByDoubanURL(href));
                    rankQuoteEntity.setRank(rank);
                    if(StringUtils.isNotBlank(quote))rankQuoteEntity.setQuote(quote);

                    System.out.println(rankQuoteEntity);
                    out.write(rankQuoteEntity.toString() + "\n");
                    out.flush();

                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }
            }
        }

        out.close();

    }

}

