package yore;

import org.apache.hadoop.hive.ql.exec.FunctionRegistry;
import org.apache.hadoop.hive.ql.exec.UDFArgumentException;
import org.apache.hadoop.hive.ql.exec.UDFArgumentLengthException;
import org.apache.hadoop.hive.ql.metadata.HiveException;
import org.apache.hadoop.hive.ql.udf.generic.GenericUDF;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspectorConverters;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspectorConverters.Converter;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspectorUtils;
import org.apache.hadoop.hive.serde2.typeinfo.PrimitiveTypeInfo;
import org.apache.hadoop.hive.serde2.typeinfo.TypeInfo;
import org.apache.hadoop.hive.serde2.typeinfo.TypeInfoFactory;
import org.apache.hadoop.hive.serde2.typeinfo.TypeInfoUtils;

/**
 * <a href="https://github.com/apache/hive/blob/f37c5de6c3/ql/src/java/org/apache/hadoop/hive/ql/udf/generic/GenericUDFBaseNwayCompare.java">GenericUDFBaseNwayCompare.java</a>
 *
 * Base class for comparison UDF's (Greatest and Least).
 */
public abstract class GenericUDFBaseNwayCompare extends GenericUDF {
    protected transient ObjectInspector[] argumentOIs;
    protected transient Converter[] converters;
    protected transient ObjectInspector resultOI;

    /**
     * @return desired comparison (positive for greatest, negative for least)
     */
    abstract int getOrder();

    @Override
    public ObjectInspector initialize(ObjectInspector[] arguments) throws UDFArgumentException {
        if (arguments.length < 2) {
            throw new UDFArgumentLengthException(getFuncName() + " requires at least 2 arguments, got "
                    + arguments.length);
        }
        if (arguments[0].getCategory() != ObjectInspector.Category.PRIMITIVE) {
            throw new UDFArgumentException(getFuncName() + " only takes primitive types, got "
                    + arguments[0].getTypeName());
        }

        argumentOIs = arguments;
        converters = new Converter[arguments.length];

        TypeInfo commonInfo = TypeInfoUtils.getTypeInfoFromObjectInspector(arguments[0]);

        for (int i = 1; i < arguments.length; i++) {
            PrimitiveTypeInfo currInfo = (PrimitiveTypeInfo) TypeInfoUtils.getTypeInfoFromObjectInspector(arguments[i]);

            commonInfo = FunctionRegistry.getCommonClassForComparison(
                    commonInfo, currInfo);
        }

        resultOI = TypeInfoUtils.getStandardWritableObjectInspectorFromTypeInfo(
                (commonInfo == null) ?
                        TypeInfoFactory.doubleTypeInfo : commonInfo);

        for (int i = 0; i < arguments.length; i++) {
            converters[i] = ObjectInspectorConverters.getConverter(arguments[i], resultOI);
        }

        return resultOI;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public Object evaluate(GenericUDF.DeferredObject[] arguments) throws HiveException {
        Object maxV = null;
        //for case of conversion, convert both values to common type and then compare.
        for (int i = 0; i < arguments.length; i++) {
            Object ai = arguments[i].get();
            if (ai == null) { //NULL if any of the args are nulls
                return null;
            }

            if (maxV == null) { //First non-null item.
                maxV = converters[i].convert(ai);
                continue;
            }
            Object converted = converters[i].convert(ai);
            if (converted == null) {
                return null;
            }
            int result = ObjectInspectorUtils.compare(
                    converted, resultOI,
                    maxV, resultOI);
            if (getOrder() * result > 0) {
                maxV = converted;
            }
        }
        return maxV;
    }

    @Override
    public String getDisplayString(String[] children) {
        return getStandardDisplayString(getFuncName(), children, ",");
    }
}
