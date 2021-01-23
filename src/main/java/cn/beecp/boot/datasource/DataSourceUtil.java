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
package cn.beecp.boot.datasource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;

import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;

import static cn.beecp.pool.PoolStaticCenter.*;

/*
 *  Util
 *
 *  @author Chris.Liao
 */
public class DataSourceUtil {

    //Spring dataSource configuration prefix-key name
    public static final String SP_DS_Prefix = "spring.datasource";

    //Multi-DataSource config id list on springboot
    public static final String SP_Multi_DS_idList = "dsList";

    //Spring jndi dataSource configuration key name
    public static final String SP_Multi_DS_Jndi = "jndiName";

    //indicator:Spring dataSource register as primary datasource
    public static final String SP_Multi_DS_Primary = "primary";

    //Datasource class name
    public static final String SP_Multi_DS_Type = "type";

    //Datasource attribute set factory
    public static final String SP_Multi_DS_FieldSetFactory = "fieldSetFactory";

    //Default DataSourceName
    public static final String SP_Multi_DS_Default_Type = "cn.beecp.BeeDataSource";

    //combineId
    public static final String SP_Multi_DS_CombineId = "combineId";

    //combineDefaultDs
    public static final String SP_Multi_DS_PrimaryDs = "combineDefaultDs";

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
            value = readConfig(environment, configPrefix + "." + propertyNameToFieldId(key, DS_Config_Prop_Separator_MiddleLine));
        if (DataSourceUtil.isBlank(value))
            value = readConfig(environment, configPrefix + "." + propertyNameToFieldId(key, DS_Config_Prop_Separator_UnderLine));
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

    public static final void setMethodAccessible(Method method, boolean accessible) {
        AccessController.doPrivileged(new PrivilegedAction<String>() {
            public String run() {
                method.setAccessible(accessible);
                return method.getName();
            }
        });
    }

    public static final void tryToCloseDataSource(Object ds) {
        Class[] paramTypes = new Class[0];
        Object[] paramValues = new Object[0];
        Class dsClass = ds.getClass();
        String[] methodNames = new String[]{"close", "shutdown", "terminate"};
        for (String name : methodNames) {
            try {
                Method method = dsClass.getMethod(name, paramTypes);
                method.invoke(ds, paramValues);
                break;
            } catch (Throwable e) {
            }
        }
    }
}
