/*
 * Copyright Chris2018998
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cn.beecp.boot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;

import javax.sql.DataSource;
import javax.sql.XAConnection;
import javax.sql.XADataSource;
import java.sql.Connection;
import java.sql.SQLException;

/*
 *  Util
 *
 *  @author Chris.Liao
 */
public class DataSourceUtil {

    //Spring dataSource configuration prefix-key name
    public static final String Spring_DS_Prefix = "spring.datasource";

    //Spring dataSource configuration key name
    public static final String Spring_DS_KEY_NameList = "nameList";

    //Spring jndi dataSource configuration key name
    public static final String Spring_DS_KEY_Jndi = "jndiName";

    //indicator:Spring dataSource register as primary datasource
    public static final String Spring_DS_KEY_Primary = "primary";

    //Datasource class name
    public static final String Spring_DS_KEY_DatasourceType = "datasourceType";

    //Datasource attribute set factory
    public static final String Spring_DS_KEY_FieldSetFactory = "fieldSetFactory";

    //Default DataSourceName
    public static final String Default_DS_Class_Name = "cn.beecp.BeeDataSource";

    //Separator MiddleLine
    public static final String Separator_MiddleLine = "-";

    //Separator UnderLine
    public static final String Separator_UnderLine = "_";

    private static final Logger log = LoggerFactory.getLogger(DataSourceUtil.class);

    public static final boolean isBlank(String str) {
        if (str == null) return true;
        int strLen = str.length();
        for (int i = 0; i < strLen; ++i) {
            if (!Character.isWhitespace(str.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    public static final String getConfigValue(Environment environment, String configPrefix, String key) {
        String value = readConfig(environment, configPrefix + "." + key);
        if (DataSourceUtil.isBlank(value))
            value = readConfig(environment, configPrefix + "." + propertyToField(key, Separator_MiddleLine));
        if (DataSourceUtil.isBlank(value))
            value = readConfig(environment, configPrefix + "." + propertyToField(key, Separator_UnderLine));
        return value;
    }

    private static final String readConfig(Environment environment, String key) {
        String value = environment.getProperty(key);
        if (!DataSourceUtil.isBlank(value)) {
            value = value.trim();
            log.info("config:{}={}", key, value);
        }
        return value;
    }

    public static final String propertyToField(String property, String separator) {
        if (property == null)
            return "";

        char[] chars = property.toCharArray();
        StringBuilder sb = new StringBuilder();
        for (char c : chars) {
            if (Character.isUpperCase(c)) {
                sb.append(separator + Character.toLowerCase(c));
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    static final void tryGetConnection(Object ds, String dsName) {
        //try to init DataSource pool
        if (ds instanceof DataSource) {
            Connection con = null;
            DataSource dds = (DataSource) ds;
            try {
                con = dds.getConnection();
            } catch (SQLException e) {//may network error
                log.error("Failed to get Connection from dataSource({}):", dsName, e);
            } finally {
                if (con != null) {
                    try {
                        con.close();
                    } catch (Throwable e) {
                    }
                }
            }
        } else if (ds instanceof XADataSource) {
            XAConnection con = null;
            XADataSource xds = (XADataSource) ds;
            try {
                con = xds.getXAConnection();
            } catch (SQLException e) {//may network error
                log.error("Failed to get Connection from dataSource({}):", dsName, e);
            } finally {
                if (con != null) {
                    try {
                        con.close();
                    } catch (Throwable e) {
                    }
                }
            }
        }
    }
}
