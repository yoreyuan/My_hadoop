package yore;

import yore.etl.RankQuoteEntity;

import java.lang.reflect.Field;

/**
 * 将字符串按照特定分隔符分隔，赋值到给定的实体对象，并返回。<br/>
 *
 * 定义泛型方法时，必须在返回值前边加一个<T>，来声明这是一个泛型方法，持有一个泛型T，然后才可以用泛型T作为方法的返回值。
 *
 * Created by yore on 2019/7/25 11:40
 */
public class BeanUtils {

    /**
     * 根据逗号分隔，将值分别赋值到给定的对象中
     *
     * @auther: yore
     * @param csvStr csv格式的字符串
     * @param entity 要赋值到的实体对象
     * @return 实体对象
     * @date: 2019/7/25 1:17 PM
     */
    public static  <T> T copyToBean(String csvStr, Class<T> entity) throws IllegalAccessException, InstantiationException {
        return copyToBean(csvStr, ",", entity);
    }

    /**
     * 根据逗号分隔，将值分别赋值到给定的对象中
     *
     * @auther: yore
     * @param formatStr 给定分隔符格式的字符串
     * @param separator 指定的分隔符
     * @param entity 要赋值到的实体对象
     * @return 实体对象
     * @date: 2019/7/25 1:17 PM
     */
    public static  <T> T copyToBean(String formatStr, String separator, Class<T> entity) throws IllegalAccessException, InstantiationException {
        T t = entity.newInstance();
        String[] strArr = formatStr.split(separator);
        Field[] fields = t.getClass().getDeclaredFields();
//        System.out.println("strArr=" + strArr.length + "\tfields=" + fields.length);
        for (int i = 0, j=0; i < strArr.length; i++, j++) {
            if("separator".equals(fields[i].getName()) ) j++;
            Field field = fields[j];
            field.setAccessible(true);
//            System.out.println(fields[i].getName());
            field.set(t, strArr[i]);
        }
        return t;
    }


    public static void main(String[] args) throws Exception {
        String quote = "3792799,135,岁月流逝，来日可追。";

        RankQuoteEntity entity = BeanUtils.copyToBean(quote, RankQuoteEntity.class);
//        MovieEntity entity = BeanUtils.copyToBean(quote, MovieEntity.class);
        System.out.println(entity);
    }

}
