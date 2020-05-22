package com.alibaba.datax.core.transport.transformer;

import com.alibaba.datax.common.element.Column;
import com.alibaba.datax.common.element.Record;
import com.alibaba.datax.common.element.StringColumn;
import com.alibaba.datax.common.exception.DataXException;
import com.alibaba.datax.transformer.Transformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yore.utils.AesUtil;

import java.util.Arrays;

/**
 * 自定义一个 DataX Transformer ，用于特殊情况下的解密
 *
 * <pre>
 *     编译项目（clone 完整项目时务必这样编译）
 *     mvn clean compile -Dmaven.test.skip=true
 * </pre>
 *
 * Created by yore on 2020/5/7 10:02
 */
public class YoreDecryptTransformer extends Transformer{
    private static final Logger LOG = LoggerFactory.getLogger(YoreDecryptTransformer.class);

    public YoreDecryptTransformer() {
        setTransformerName("dx_yore_decrypt");
    }


    /**
     * 实现对指定字段的解密处理
     *
     * @author Yore
     * @param record 记录
     * @param paras transformer 方法传入的参数
     * @return Record 记录
     */
    @Override
    public Record evaluate(Record record, Object... paras) {
        int columnIndex;
        String aesKey = null;

        try {
            if(paras.length == 1){
                columnIndex = (Integer) paras[0];
            }else if(paras.length ==2){
                columnIndex = (Integer) paras[0];
                aesKey = (String) paras[1];
            }else{
                throw new RuntimeException(getTransformerName() + " paras at moust 2");
            }
        }catch (Exception e){
            throw DataXException.asDataXException(TransformerErrorCode.TRANSFORMER_ILLEGAL_PARAMETER, "paras:" + Arrays.asList(paras).toString() + " => " + e.getMessage());
        }


        Column column = record.getColumn(columnIndex);

        try {
            String oriValue = column.asString();
            //如果字段为空，跳过，不进行解密操作
            if(oriValue == null){
                return record;
            }

            String newValue;
            if(aesKey == null){
                newValue = AesUtil.decrypt(oriValue);
            }else if(aesKey.trim().length()<1){
                newValue = AesUtil.decrypt(oriValue);
                LOG.warn("指定的解密密钥 key={} 无效，将采用默认密钥解密", aesKey);
            }else {
                AesUtil.init(aesKey);
                newValue = AesUtil.decrypt(oriValue);
            }

            record.setColumn(columnIndex, new StringColumn(newValue));

        }catch (Exception e){
            throw DataXException.asDataXException(TransformerErrorCode.TRANSFORMER_RUN_EXCEPTION, e.getMessage(),e);
        }

        return record;
    }
}
