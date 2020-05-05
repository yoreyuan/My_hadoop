package yore;

import org.apache.hadoop.hive.ql.exec.Description;
import org.apache.hadoop.hive.ql.exec.UDFArgumentTypeException;
import org.apache.hadoop.hive.ql.metadata.HiveException;
import org.apache.hadoop.hive.ql.parse.SemanticException;
import org.apache.hadoop.hive.ql.udf.generic.AbstractGenericUDAFResolver;
import org.apache.hadoop.hive.ql.udf.generic.GenericUDAFEvaluator;
import org.apache.hadoop.hive.serde2.objectinspector.*;
import org.apache.hadoop.hive.serde2.typeinfo.TypeInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * 实现一个UDAF，将给定字段的多行值合并为一个集合
 *
 *  Created by yore on 2019/5/19 20:46
 */
@Description(name = "collect", value = "_FUNC_(x) - Returns a list of objects. "+
        "CAUTION will easily OOM on large data sets" )
public class GenericUDAFCollect extends AbstractGenericUDAFResolver {

    public GenericUDAFCollect() {
    }

    /**
     * 类型检查
     */
    @Override
    public GenericUDAFEvaluator getEvaluator(TypeInfo[] parameters)
            throws SemanticException {
        if (parameters.length != 1) {
            throw new UDFArgumentTypeException(parameters.length - 1,
                    "Exactly one argument is expected.");
        }
        if (parameters[0].getCategory() != ObjectInspector.Category.PRIMITIVE) {
            throw new UDFArgumentTypeException(0,
                    "Only primitive type arguments are accepted but "
                            + parameters[0].getTypeName() + " was passed as parameter 1.");
        }
        return new GenericUDAFMkListEvaluator();
    }



    static class GenericUDAFMkListEvaluator extends GenericUDAFEvaluator {
        private PrimitiveObjectInspector inputOI;
        private StandardListObjectInspector loi;
        private StandardListObjectInspector internalMergeOI;

        /**
         * Hive会调用此方法来初始实例化一个UDAF evaluator类。
         * 它会根据model值的不同来决定输入的类型和输出的类型，注意：init()的调用不是单次的，是多次的。
         *
         * parameters为源数据存储格式inspector返回值，
         * 其中的Mode，决定了在Map阶段和Reduce阶段在审计到UDF函数计算的时候，会调用UDF类中的那些方法。
         *
         */
        @Override
        public ObjectInspector init(Mode m, ObjectInspector[] parameters) throws HiveException {
            super.init(m, parameters);
            /**
             *
             *
             * PARTIAL1：  这个是 mapreduce 的 map 阶段。从原始数据到部分聚集数据：iterate()和 TerminatePartial()将调用。
             *
             * PARTIAL2：  这个是mapreduce的map端的Combiner阶段。负责在map端合并map的数据，从部分数据聚合到部分数据聚合：merge() 和 terminatePartial() 将被调用。
             *
             * FINAL：      mapreduce的reduce阶段。从部分数据的聚合到完全聚合：merge()和 terminate() 将被调用。
             *
             * COMPLETE：  如果出现了这个阶段，表示mapreduce只有map，没有reduce，所以map端就直接出结果了。从原始数据直接到完全聚合：将会调用 iterate()和terminate()。
             *
             */
            if (m == Mode.PARTIAL1) {
                inputOI = (PrimitiveObjectInspector) parameters[0];
                return ObjectInspectorFactory
                        .getStandardListObjectInspector(
                                (PrimitiveObjectInspector) ObjectInspectorUtils
                                        .getStandardObjectInspector(inputOI));
            } else {
                if (!(parameters[0] instanceof StandardListObjectInspector)) {
                    inputOI = (PrimitiveObjectInspector) ObjectInspectorUtils
                            .getStandardObjectInspector(parameters[0]);
                    return (StandardListObjectInspector) ObjectInspectorFactory
                            .getStandardListObjectInspector(inputOI);
                } else {
                    internalMergeOI = (StandardListObjectInspector) parameters[0];
                    inputOI = (PrimitiveObjectInspector) internalMergeOI.getListElementObjectInspector();
                    loi = (StandardListObjectInspector) ObjectInspectorUtils
                            .getStandardObjectInspector(internalMergeOI);
                    return loi;
                }
            }
        }


        static class MkArrayAggregationBuffer implements AggregationBuffer {
            List<Object> container;
        }

        @Override
        public void reset(AggregationBuffer agg) throws HiveException {
            ((MkArrayAggregationBuffer) agg).container =
                    new ArrayList<Object>();
        }

        /**
         * 返回一个用于存储中间聚合结果的对象
         */
        @Override
        public AggregationBuffer getNewAggregationBuffer()
                throws HiveException {
            MkArrayAggregationBuffer ret = new MkArrayAggregationBuffer();
            reset(ret);
            return ret;
        }

        /**
         * 将一行新的数据载入到聚合buffer中。
         * Map阶段，迭代处理输入sql传入过来的列数据
         */
        // Mapside
        @Override
        public void iterate(AggregationBuffer agg, Object[] parameters) throws HiveException {
            // 断言，如果为true，则程序继续，否则抛出AssertionError，并终止执行。
            assert (parameters.length == 1);
            Object p = parameters[0];
            if (p != null) {
                MkArrayAggregationBuffer myagg = (MkArrayAggregationBuffer) agg;
                putIntoList(p, myagg);
            }
        }

        /**
         * 以一种可持久化的方法返回当前聚合的内容。
         * 这里所说的持久化是指返回值只可以使用Java基本数据类型和array，以及基本封装类型（例如Double）,
         * Hadoop中的 Writables类、List和Maps类型。不能使用用户自定义的类，即使实现了java.io.Serializable
         */
        // Mapside
        @Override
        public Object terminatePartial(AggregationBuffer agg) throws HiveException {
            MkArrayAggregationBuffer myagg = (MkArrayAggregationBuffer) agg;
            ArrayList<Object> ret = new ArrayList<>(myagg.container.size());
            ret.addAll(myagg.container);
            return ret;
        }

        /**
         * 将 terminatePartial 返回的中间部分聚合结果，合并到当前集合中
         * @throws HiveException
         */
        @Override
        public void merge(AggregationBuffer agg, Object partial) throws HiveException {
            MkArrayAggregationBuffer myagg = (MkArrayAggregationBuffer) agg;
            ArrayList<Object> partialResult = (ArrayList<Object>) internalMergeOI.getList(partial);
            for(Object i : partialResult) {
                putIntoList(i, myagg);
            }
        }

        /**
         * 返回最终聚合结果给Hive
         */
        @Override
        public Object terminate(AggregationBuffer agg) throws HiveException {
            MkArrayAggregationBuffer myagg = (MkArrayAggregationBuffer) agg;
            ArrayList<Object> ret = new ArrayList<>(myagg.container.size());
            ret.addAll(myagg.container);
            return ret;
        }

        /**
         * 添加数据到缓存
         * @param p sql输入参数的的每行值
         * @param myagg ArrayList缓存对象
         */
        private void putIntoList(Object p, MkArrayAggregationBuffer myagg) {
            Object pCopy = ObjectInspectorUtils.copyToStandardObject(p,this.inputOI);
            myagg.container.add(pCopy);
        }

    }
}
