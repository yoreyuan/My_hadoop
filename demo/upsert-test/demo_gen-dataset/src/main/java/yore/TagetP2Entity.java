package yore;

import java.text.DecimalFormat;
import java.util.Random;

/**
 * Created by yore on 2019/11/3 16:42
 */
public class TagetP2Entity implements java.io.Serializable{
    private static final long serialVersionUID = 5501692453921048776L;
    private static SnowFlakeGenerator.Factory factory =  new SnowFlakeGenerator.Factory();
    private static SnowFlakeGenerator snowFlakeGenerator = factory.create(0, 1);
    private static   Random random = new Random();
    private static String ROW_FORMAT_DELIMITED_FIELDS_TERMINATED = ",";


    private Long id;
    private Double indicator2;

    public TagetP2Entity() {
        DecimalFormat df = new DecimalFormat("0.00");
        String str = df.format(random.nextDouble()*100);
        this.indicator2 = Double.parseDouble(str);
    }
    public TagetP2Entity(Long id) {
        // 自行自己无参的构造方法
        this();
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    /**
     * 当未传入 id 值时则自动生成
     *
     * @auther: yore
     * @param
     * @return
     * @date: 2019/11/25 5:19 PM
     */
    public void setId() {
        this.id = snowFlakeGenerator.nextId();
    }

    public Double getIndicator2() {
        return indicator2;
    }

    public void setIndicator1(Double indicator2) {
        this.indicator2 = indicator2;
    }

    @Override
    public String toString() {
        return id + ROW_FORMAT_DELIMITED_FIELDS_TERMINATED +
                indicator2;
    }


}