package yore.etl;

import okhttp3.*;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

/**
 * 本真善意的目的爬取内容，请大家以学习目的使用
 *
 * Created by yore on 2019/7/24 20:04
 */
public class CrawlMovie {
    private static Logger LOG = LoggerFactory.getLogger(CrawlMovie.class);
    public static final String OUT_PATH = "/Users/yoreyuan/Downloads/doubanMovie.csv";


    public static void main(String[] args) throws  Exception{
        // 获取电影详情信息的url
        getTopUrlList();

        // 获取每个电影详情信息
        getMovieDetile();

    }


    //https://movie.douban.com/top250
    private static final String URL = "https://movie.douban.com/top250";
    //    private static final String URL = "https://movie.douban.com/subject/1292052/";
    private HttpClient httpClient;
    static Random random = new Random();
    // 这里用set集合，对于顺序没有严格要求，后面再对这份数据做进一步的分析
    static Set<String> movieSet = new HashSet<String>(){
        {
//            add("https://movie.douban.com/subject/1292052/");
//            add("https://movie.douban.com/subject/1295124/");
//            add("https://movie.douban.com/subject/1309163/");
        }
    };


    /**
     * 获取电影详情连接
     * <pre>
     *     https://movie.douban.com/top250
     *     默认请求回来25条数据
     *     start参数指定返回数据的起始标识，
     *
     *     例如：
     *      start=0     返回 排行 1-25的电影
     *      start=25    返回 排行 26-50的电影
     *
     * </pre>
     *
     * @auther: yore
     */
    public static void getTopUrlList(){
        //
        for(int i=0; i < 250 ; i++){
            if(i%25==0){
//                System.out.println(URL+"?start=" + i);
                okHttpAsync(URL+"?start=" + i);
            }
        }
        System.out.println(movieSet);
    }

    /**
     * 分析处理电影集合中的电影详情内容
     *
     * @auther: yore
     */
    public static void getMovieDetile() throws Exception {
        OkHttpClient okHttpClient = new OkHttpClient();
        OutputStreamWriter out = Utils.getOutPutStreamWriter(OUT_PATH);
        for (String u : movieSet) {
            Request request = new Request.Builder()
                    .url(u)
                    .get()
                    .build();
            Call call = okHttpClient.newCall(request);
            okHttpSync(call, u, out);
            out.flush();
//            call.cancel();
            Thread.sleep(random.nextInt(3000));
        }
        out.close();
    }


    /**
     * OkHttp同步方式请求，用来获取电影列表
     * @param url 请求的连接
     * @auther: yore
     */
    public static void okHttpAsync(String url){
        OkHttpClient okHttpClient = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();
        try {
            Response response = okHttpClient.newCall(request).execute();
            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
            String html = response.body().string();
            Element element = Jsoup.parse(html).body().getElementById("content");

            for(Element aElement : element.getElementsByClass("grid_view").get(0).getElementsByTag("a")){
                String href = aElement.attr("href");
//                System.out.println(href);
                movieSet.add(href);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * OkHttp异步方式请求，
     * 获取给定连接的详细信息。
     *
     * @param call 请求的回调
     * @auther: yore
     */
    public static void okHttpSync(Call call, String url, OutputStreamWriter out){
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                LOG.error(String.format("OkHttp方式：异步 get 请求%s发生了异常", URL));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if(!response.isSuccessful()) {
                    try {
                        Thread.sleep(3000);
                        // 当发生请求返回异常码时，再重试一次
                        okHttpSync(call, url, out);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }catch (Exception e2){
                        throw new IOException("Unexpected code " + response);
                    }
                }
                String html = response.body().string();
                Element body = Jsoup.parse(html).body();
                Element info = body.getElementById("info");

                MovieEntity movieEntity = new MovieEntity();

                movieEntity.setMovie_name(body.getElementById("content").getElementsByTag("h1").text().replace(",", "，"));
                movieEntity.setUrl(url);
                movieEntity.setId(Utils.getIdByDoubanURL(url));

//                for (Element spanElement : info.select("span[class=pl]")) {
//                    System.out.println(spanElement.text());
//                }
//
//                System.out.println("---------");
//                for (Element spanElement : info.select(">span")) {
//                    System.out.println(spanElement.text());
//                }

                String[] infoHtmlArr = info.html()
                        // 将英文逗号替换为中文逗号，后面文件统一为 csv
                        .replace(",", "，")
                        .replaceAll("\n", "")
                        .split("<br>");
                for(String infoHtml : infoHtmlArr){
                    Element el1 = Jsoup.parse(infoHtml);
                    String[] el1TextArr = el1.text().split(": ");
                    if("导演".equals(el1TextArr[0])) movieEntity.setDirector(el1TextArr[1]);
                    if("编剧".equals(el1TextArr[0])) movieEntity.setScriptwriter(el1TextArr[1]);
                    if("主演".equals(el1TextArr[0])) movieEntity.setProtagonist(el1TextArr[1]);
                    if("类型".equals(el1TextArr[0])) movieEntity.setKind(el1TextArr[1]);
                    if("制片国家/地区".equals(el1TextArr[0])) movieEntity.setCountry(el1TextArr[1]);
                    if("语言".equals(el1TextArr[0])) movieEntity.setLanguage(el1TextArr[1]);
                    if("上映日期".equals(el1TextArr[0])) movieEntity.setRelease_date(el1TextArr[1]);
                    if("片长".equals(el1TextArr[0])) movieEntity.setMins(el1TextArr[1]);
                    if("又名".equals(el1TextArr[0])) movieEntity.setAlternate_name(el1TextArr[1]);
                    if("IMDb链接".equals(el1TextArr[0])) movieEntity.setImdb(el1TextArr[1]);
                }

                movieEntity.setRating_num(body.getElementById("interest_sectl").getElementsByTag("strong").get(0).text());
                movieEntity.setRating_people(body.getElementById("interest_sectl").select("a[class=rating_people]>span").text());

                out.write(movieEntity.toString() + "\n");
                System.out.println(movieEntity.getMovie_name());
            }
        });

    }

    // ----------- HttpClient -----------

    public static void httpClinet(){
        CrawlMovie crawlData = new CrawlMovie();
        crawlData.prepare(crawlData);
        HttpGet get = new HttpGet(URL);

        try {
            HttpResponse response = crawlData.httpClient.execute(get);
            String html = EntityUtils.toString(response.getEntity());

            System.out.println(html);

        } catch (IOException e) {
            e.printStackTrace();
            LOG.error(String.format("HttpClient方式：get 请求%s发生了异常", URL));
        }
    }

    private void prepare(CrawlMovie crawlData){
        crawlData.httpClient = HttpClientBuilder.create()
                .setUserAgent("Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/55.0.2883.87 Safari/537.36")
                .build();
    }

}
