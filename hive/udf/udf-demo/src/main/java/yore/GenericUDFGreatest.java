package yore;

import org.apache.hadoop.hive.ql.exec.Description;

/**
 * <pre>
 *     查看官方文档：<a href="https://cwiki.apache.org/confluence/display/Hive/LanguageManual+UDF#LanguageManualUDF-MathematicalFunctions">LanguageManualUDF-MathematicalFunctions </a>
 *      会在<code>greatest(T v1, T v2, ...)</code>的解释中看到
 *      <cite>Returns the greatest value of the list of values (as of Hive 1.1.0). Fixed to return NULL when one or more arguments are NULL, and strict type restriction relaxed, consistent with ">" operator (as of Hive 2.0.0).</<cite>>
 *
 *      这里他解释到在 2.0.0 修复了 null 值问题。这里修复的具体问题是当 执行 greatest(null, 1) 时 null 值会被忽略的问题，为了保持和 MySQL 一致，修复了这个问题。
 *
 * </pre>
 *
 * <a href="https://github.com/apache/hive/blob/f37c5de6c32b9395d1b34fa3c02ed06d1bfbf6eb/ql/src/java/org/apache/hadoop/hive/ql/udf/generic/GenericUDFGreatest.java">GenericUDFGreatest</a>
 *
 * GenericUDF Class for SQL construct "greatest(v1, v2, .. vn)".
 */
@Description(name = "greatest",
        value = "_FUNC_(v1, v2, ...) - Returns the greatest value in a list of values",
        extended = "Example:\n"
                + "  > SELECT _FUNC_(2, 3, 1) FROM src LIMIT 1;\n" + "  3")
public class GenericUDFGreatest extends GenericUDFBaseNwayCompare{

    @Override
    protected String getFuncName() {
        return "greatest";
    }

    @Override
    protected int getOrder() {
        return 1;
    }
}
