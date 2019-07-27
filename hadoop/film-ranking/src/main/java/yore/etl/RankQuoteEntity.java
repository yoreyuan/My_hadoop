package yore.etl;

/**
 * Created by yore on 2019/7/24 23:46
 */
public class RankQuoteEntity implements java.io.Serializable{
    private final static String separator = ",";

    /** 电影标识 */
    private String id ;
    /** 排名 */
    private String rank ;
    /** 语录 */
    private String quote = "无";


    public String getId() {
        return id;
    }

    public String getRank() {
        return rank;
    }

    public String getQuote() {
        return quote;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setRank(String rank) {
        this.rank = rank;
    }

    public void setQuote(String quote) {
        this.quote = quote;
    }

    @Override
    public String toString() {
        return id  + separator +
                rank  + separator +
                quote
                ;
    }
}
