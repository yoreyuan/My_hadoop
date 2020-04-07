package yore;

import javax.security.sasl.AuthenticationException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hive.conf.HiveConf;
import org.slf4j.Logger;


/**
 * hive-service 包
 *
 * beeline --color=true -d "org.apache.hive.jdbc.HiveDriver" -u "jdbc:hive2://cdh3:10000/default" -n reportmart -p report_123 --hiveconf hive.query.redaction.rules=NULL
 * beeline --color=true -d "org.apache.hive.jdbc.HiveDriver" -u "jdbc:hive2://cdh3:10000/default;user=hive;password=hive123" -n hive -p hive123 --hiveconf hive.query.redaction.rules=null
 *
 * cat /var/run/cloudera-scm-agent/process/1383-hive-HIVESERVER2/redaction-rules.json
 *
 * systemctl status cloudera-scm-agent
 *
 * grep -rni "hive.query.redaction.rules"
 *
 *
 * Created by yore on 2020/3/26 18:33
 */
public class CustomPasswdAuthenticator implements org.apache.hive.service.auth.PasswdAuthenticationProvider {
    private Logger LOG = org.slf4j.LoggerFactory.getLogger(CustomPasswdAuthenticator.class);
    private static final String HIVE_JDBC_PASSWD_AUTH_PREFIX="hive.jdbc_passwd.auth.%s";
    private Configuration conf=null;

    @Override
    public void Authenticate(String userName, String passwd) throws AuthenticationException {
        LOG.info("user: "+userName+" try login.");
        String passwdConf = getConf().get(String.format(HIVE_JDBC_PASSWD_AUTH_PREFIX, userName));
        if(passwdConf==null){
            String message = "user's ACL configration is not found. user:"+userName;
            LOG.info(message);
            throw new AuthenticationException(message);
        }
        if(!passwd.equals(passwdConf)){
            String message = "user name and password is mismatch. user:"+userName;
            throw new AuthenticationException(message);
        }
    }

    public Configuration getConf() {
        if(conf==null){
            this.conf=new Configuration(new HiveConf());
        }
        return conf;
    }
    public void setConf(Configuration conf) {
        this.conf=conf;
    }



    public static void main(String[] args) throws ClassNotFoundException, SQLException {
        // Hive 连接信息
        final String dirverName = "org.apache.hive.jdbc.HiveDriver";
        String url = "jdbc:hive2://cdh3:10000/hive_test";
        String user = "reportmart";
        String password = "report_123";

        // 带 LDAP 认证的，需要填写 Hive配置的 hive.server2.authentication.ldap.baseDN 的 LDAP 中 域的 用户名与密码
        user = "impala";
        password = "cdhImpala_123";

        Class.forName(dirverName);
        Connection conn;

        boolean hashPassword = true;
        if(hashPassword){ // 带密码的
            conn = DriverManager.getConnection(url, user, password);
        }else{ // 不带密码的
            conn = DriverManager.getConnection(url);
        }

        PreparedStatement ps = conn.prepareStatement("SHOW TABLES");
        ResultSet result = ps.executeQuery();

        List<String> tablesName = new ArrayList<>();
        while (result.next()){
            tablesName.add(result.getString(1));
        }
        ps.close();
        System.out.println(tablesName);

    }
}
