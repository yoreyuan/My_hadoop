package yore.etl;

/**
 * 电影信息的实体类
 *
 * Created by yore on 2019/7/24 20:51
 */
public class MovieEntity implements java.io.Serializable{

    /** 电影标识 */
    private String id ;
    /** 电影名 */
    private String movie_name = "-";
     /** 导演 */
    private String director = "-";
     /** 编剧 */
    private String scriptwriter = "-";
     /** 主演 */
    private String protagonist = "-";
     /** 类型 */
    private String kind = "-";
     /** 制片国家/地区 */
    private String country = "-";
     /** 语言 */
    private String language = "-";
     /** 上映日期 */
    private String release_date = "-";
     /** 片长 */
    private String mins = "-";
     /** 又名 */
    private String alternate_name = "-";
     /** IMDb链接 */
    private String imdb = "-";
    /** 评分 */
    private String rating_num = "-";
    /** 评分人数 */
    private String rating_people = "-";
    /** url */
    private String url = "-";

    private final static String separator = ",";

    public MovieEntity() {
    }

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }

    public String getMovie_name() {
        return movie_name;
    }
    public void setMovie_name(String movie_name) {
        this.movie_name = movie_name;
    }

    public String getDirector() {
        return director;
    }
    public void setDirector(String director) {
        this.director = director;
    }

    public String getScriptwriter() {
        return scriptwriter;
    }
    public void setScriptwriter(String scriptwriter) {
        this.scriptwriter = scriptwriter;
    }

    public String getProtagonist() {
        return protagonist;
    }
    public void setProtagonist(String protagonist) {
        this.protagonist = protagonist;
    }

    public String getKind() {
        return kind;
    }
    public void setKind(String kind) {
        this.kind = kind;
    }

    public String getCountry() {
        return country;
    }
    public void setCountry(String country) {
        this.country = country;
    }

    public String getLanguage() {
        return language;
    }
    public void setLanguage(String language) {
        this.language = language;
    }

    public String getRelease_date() {
        return release_date;
    }
    public void setRelease_date(String release_date) {
        this.release_date = release_date;
    }

    public String getMins() {
        return mins;
    }
    public void setMins(String mins) {
        this.mins = mins;
    }

    public String getAlternate_name() {
        return alternate_name;
    }
    public void setAlternate_name(String alternate_name) {
        this.alternate_name = alternate_name;
    }

    public String getImdb() {
        return imdb;
    }
    public void setImdb(String imdb) {
        this.imdb = imdb;
    }

    public String getRating_num() {
        return rating_num;
    }
    public void setRating_num(String rating_num) {
        this.rating_num = rating_num;
    }

    public String getRating_people() {
        return rating_people;
    }
    public void setRating_people(String rating_people) {
        this.rating_people = rating_people;
    }

    public String getUrl() {
        return url;
    }
    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public String toString() {

        return id  + separator +
               movie_name  + separator +
               director  + separator +
               scriptwriter  + separator +
               protagonist  + separator +
               kind   + separator +
               country  + separator +
               language   + separator +
               release_date  + separator +
               mins  + separator +
               alternate_name   + separator +
               imdb   + separator +
               rating_num   + separator +
               rating_people  + separator +
               url
                ;
    }
}
