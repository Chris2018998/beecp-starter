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

import cn.beecp.boot.datasource.factory.SpringBootDataSourceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;

import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static cn.beecp.pool.PoolStaticCenter.*;

/*
 *  Spring Boot DataSource Util
 *
 *  @author Chris.Liao
 */
public class SpringBootDataSourceUtil {

    //Spring dataSource configuration prefix-key name
    public static final String SP_DS_Prefix = "spring.datasource";

    //Multi-DataSource config id list on springboot
    public static final String SP_Multi_DS_Ids = "dsIds";

    //Spring jndi dataSource configuration key name
    public static final String SP_Multi_DS_Jndi = "jndiName";

    //indicator:Spring dataSource register as primary datasource
    public static final String SP_Multi_DS_Primary = "primary";

    //Datasource class name
    public static final String SP_Multi_DS_Type = "type";

    //Default DataSourceName
    public static final String SP_Multi_DS_Default_Type = "cn.beecp.BeeDataSource";

    //combineId
    public static final String SP_Multi_DS_CombineId = "combineId";

    //combineDefaultDs
    public static final String SP_Multi_DS_Combine_PrimaryDs = "combinePrimaryId";

    private static final Logger log = LoggerFactory.getLogger(SpringBootDataSourceUtil.class);

    private static final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss SSS");

    public static final String formatDate(Date date) {
        return formatter.format(date);
    }

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

    public static final void configDataSource(Object bean, Environment environment, String dsId, String dsConfigPrefix) throws SpringBootDataSourceException {
        try {
            //1:get all set methods
            Map<String, Method> setMethodMap = getSetMethodMap(bean.getClass());

            //2:create map to collect config value
            Map<String, Object> setValueMap = new HashMap<String, Object>(setMethodMap.size());

            //3:loop to find out properties config value by set methods
            Iterator<String> iterator = setMethodMap.keySet().iterator();
            while (iterator.hasNext()) {
                String propertyName = iterator.next();

                String configVal = getConfigValue(environment, dsConfigPrefix, propertyName);
                if (isBlank(configVal)) continue;
                setValueMap.put(propertyName, configVal.trim());
            }

            //4:inject found config value to ds config object
            setPropertiesValue(bean, setMethodMap, setValueMap);
        } catch (Throwable e) {
            throw new SpringBootDataSourceException("DataSource(" + dsId + ")-Failed to set properties", e);
        }
    }

    public static final String getConfigValue(Environment environment, String dsConfigPrefix, String key) {
        String value = readConfig(environment, dsConfigPrefix + "." + key);
        if (isBlank(value))
            value = readConfig(environment, dsConfigPrefix + "." + propertyNameToFieldId(key, DS_Config_Prop_Separator_MiddleLine));
        if (isBlank(value))
            value = readConfig(environment, dsConfigPrefix + "." + propertyNameToFieldId(key, DS_Config_Prop_Separator_UnderLine));
        return value;
    }

    private static final String readConfig(Environment environment, String key) {
        String value = environment.getProperty(key);
        if (!SpringBootDataSourceUtil.isBlank(value)) {
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
