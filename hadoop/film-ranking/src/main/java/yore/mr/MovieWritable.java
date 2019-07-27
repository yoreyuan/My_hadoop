package yore.mr;

import org.apache.hadoop.io.WritableComparable;
import org.jetbrains.annotations.NotNull;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * 自定义排序--key
 * id,m.movie_name,m.rating_num,m.rating_people,q.rank,q.quote
 *
 * Created by yore on 2019/7/25 20:49
 */
public class MovieWritable implements WritableComparable<MovieWritable> {

    /** 电影标识 */
    private String id ;
    /** 电影名 */
    private String movie_name;
    /** 评分 */
    private Float rating_num ;
    /** 评分人数 */
    private Long rating_people;
    /** 排名 */
    private String rank ;
    /** 语录 */
    private String quote = "无";

    public MovieWritable() {
    }

    @Override
    public int compareTo(@NotNull MovieWritable o) {
        /*if(rating_num != o.rating_num ){
            return rating_num > o.rating_num? -1:1;
        }else if(rating_people != o.rating_people){
            return rating_people > o.rating_people? -1:1;
        }else {
            return 0;
        }*/
        /*if(rating_num == o.rating_num){
            return rating_people > o.rating_people? -1:1;
        }else{
            return rating_num > o.rating_num? -1:1;
        }*/
        // 评分降序
        int comp = o.rating_num.compareTo(this.rating_num);
        if(comp != 0){
            return comp;
        }else{
            // 评论人数降序排
            return o.rating_people.compareTo(this.rating_people);
        }

    }

    @Override
    public void write(DataOutput out) throws IOException {
        out.writeUTF(id);
        out.writeUTF(movie_name);
        out.writeFloat(rating_num);
        out.writeLong(rating_people);
        out.writeUTF(rank);
        out.writeUTF(quote);

    }

    @Override
    public void readFields(DataInput in) throws IOException {
        id = in.readUTF();
        movie_name = in.readUTF();
        rating_num = in.readFloat();
        rating_people = in.readLong();
        rank = in.readUTF();
        quote = in.readUTF();

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

    public Float getRating_num() {
        return rating_num;
    }
    public void setRating_num(Float rating_num) {
        this.rating_num = rating_num;
    }

    public Long getRating_people() {
        return rating_people;
    }
    public void setRating_people(Long rating_people) {
        this.rating_people = rating_people;
    }

    public String getRank() {
        return rank;
    }
    public void setRank(String rank) {
        this.rank = rank;
    }

    public String getQuote() {
        return quote;
    }
    public void setQuote(String quote) {
        this.quote = quote;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == null)return false;
        if(this == obj)return true;
        if(obj instanceof MovieWritable){
            MovieWritable m = (MovieWritable)obj;
            return m.rating_num == rating_num && m.rating_people==rating_people;
        }else{
            return false;
        }
    }

    private final static String separator = ",";

    @Override
    public String toString() {

        return id  + separator +
                movie_name  + separator +
                rating_num   + separator +
                rating_people  + separator +
                rank  + separator +
                quote  + separator
                ;
    }

}
