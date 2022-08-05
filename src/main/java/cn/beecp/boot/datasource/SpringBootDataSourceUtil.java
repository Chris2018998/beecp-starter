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

import cn.beecp.BeeDataSource;
import cn.beecp.boot.datasource.factory.BeeDataSourceFactory;
import cn.beecp.boot.datasource.factory.SpringBootDataSourceException;
import cn.beecp.boot.datasource.factory.SpringBootDataSourceFactory;
import cn.beecp.boot.datasource.util.JackSonTool;
import cn.beecp.boot.datasource.util.SpringBootJsonTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.core.env.Environment;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.Supplier;

import static cn.beecp.pool.PoolStaticCenter.*;

/*
 *  Spring Boot DataSource Util
 *
 *  @author Chris.Liao
 */
public class SpringBootDataSourceUtil {
    //Spring dataSource configuration prefix-key name
    static final String Config_DS_Prefix = "spring.datasource";
    //DataSource config id list on springboot
    static final String Config_DS_Id = "dsId";
    //combineId
    static final String Config_DS_CombineId = "combineId";
    //combineDefaultDs
    static final String Config_DS_Combine_PrimaryDs = "combinePrimaryId";

    //indicator:Spring dataSource register as primary datasource
    private static final String Config_DS_Primary = "primary";
    //Datasource class name
    private static final String Config_DS_Type = "type";
    //Spring jndi dataSource configuration key name
    private static final String Config_DS_Jndi = "jndiName";
    //BeeCP DataSource class name
    private static final String BeeCP_DS_Class_Name = BeeDataSource.class.getName();
    private static final ThreadLocal<WeakReference<DateFormat>> DateFormatThreadLocal = new ThreadLocal<WeakReference<DateFormat>>();
    private static final Map<Class, SpringBootDataSourceFactory> DataSourceFactoryMap = new HashMap<>(1);
    private static final Logger log = LoggerFactory.getLogger(SpringBootDataSourceUtil.class);
    private static SpringBootJsonTool jsonTool;
    //***************************************************************************************************************//
    //                                1: spring register or base (3)                                                //
    //***************************************************************************************************************//

    static {
        DataSourceFactoryMap.put(BeeDataSource.class, new BeeDataSourceFactory());
    }

    public static String object2String(Object obj) throws IOException {
        return jsonTool.object2String(obj);
    }

    public static <T> T string2Object(String str, Class<T> clazz) throws IOException {
        return jsonTool.string2Object(str, clazz);
    }

    public static boolean stringEquals(String a, String b) {
        return a != null ? a.equals(b) : b == null;
    }


    //create json tool implementation
    static void createJsonTool(String jsonClassName) {
        if (!isBlank(jsonClassName)) {
            try {
                Class jsonToolClass = Class.forName(jsonClassName);
                SpringBootJsonTool tool = (SpringBootJsonTool) jsonToolClass.newInstance();
                tool.init();
                jsonTool = tool;
            } catch (Throwable e) {
                log.warn("Failed to create json tool by class:{}", jsonClassName);
            }
        }
        if (jsonTool == null) jsonTool = new JackSonTool();
    }


    public static String formatDate(Date date) {
        WeakReference<DateFormat> reference = DateFormatThreadLocal.get();
        DateFormat dateFormat = reference != null ? reference.get() : null;
        if (dateFormat == null) {
            dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss SSS");
            DateFormatThreadLocal.set(new WeakReference<>(dateFormat));
        }
        return dateFormat.format(date);
    }

    public static Supplier createSpringSupplier(Object bean) {
        return new SpringRegSupplier(bean);
    }

    public static boolean existsBeanDefinition(String beanName, BeanDefinitionRegistry registry) {
        try {
            return registry.getBeanDefinition(beanName) != null;
        } catch (NoSuchBeanDefinitionException e) {
            return false;
        }
    }

    //***************************************************************************************************************//
    //                                2: Spring Boot dataSource create(5)                                            //
    //***************************************************************************************************************//
    public synchronized static DataSourceMonitorConfig readMonitorConfig(Environment environment) {
        if (DataSourceMonitorConfig.single == null) {
            //1:create sql statement config instance
            DataSourceMonitorConfig config = new DataSourceMonitorConfig();
            //2:set Properties
            setConfigPropertiesValue(config, Config_DS_Prefix, null, environment);
            DataSourceMonitorConfig.single = config;
            //3:create global json tool()
            createJsonTool(config.getJsonToolClassName());
        }
        return DataSourceMonitorConfig.single;
    }

    static SpringBootDataSource createSpringBootDataSource(String dsPrefix, String dsId, Environment environment) {
        String jndiNameTex = getConfigValue(dsPrefix, Config_DS_Jndi, environment);
        SpringBootDataSource ds;
        if (!isBlank(jndiNameTex)) {//jndi dataSource
            ds = lookupJndiDataSource(dsId, jndiNameTex);
        } else {//independent type
            ds = createDataSourceByDsType(dsPrefix, dsId, environment);
        }

        String primaryText = getConfigValue(dsPrefix, Config_DS_Primary, environment);
        ds.setPrimary(isBlank(primaryText) ? false : Boolean.valueOf(primaryText));
        return ds;
    }

    private static SpringBootDataSource lookupJndiDataSource(String dsId, String jndiName) {
        try {
            Object namingObj = new InitialContext().lookup(jndiName);
            if (namingObj instanceof DataSource) {
                return new SpringBootDataSource(dsId, (DataSource) namingObj, true);
            } else {
                throw new SpringBootDataSourceException("DataSource(" + dsId + ")-Jndi Name(" + jndiName + ") is not a data source object");
            }
        } catch (NamingException e) {
            throw new SpringBootDataSourceException("DataSource(" + dsId + ")-Failed to lookup data source by jndi-name:" + jndiName);
        }
    }

    private static SpringBootDataSource createDataSourceByDsType(String dsPrefix, String dsId, Environment environment) {
        //1:load dataSource class
        String dsClassName = getConfigValue(dsPrefix, Config_DS_Type, environment);
        dsClassName = isBlank(dsClassName) ? BeeCP_DS_Class_Name : dsClassName.trim();

        //2:create dataSource class
        Class dsClass;
        try {
            dsClass = Class.forName(dsClassName);
        } catch (ClassNotFoundException e) {
            throw new SpringBootDataSourceException("DataSource(" + dsId + ")-Not found class:" + dsClassName);
        }

        //3:create dataSource
        DataSource ds;
        SpringBootDataSourceFactory dsFactory = DataSourceFactoryMap.get(dsClass);
        if (dsFactory == null && SpringBootDataSourceFactory.class.isAssignableFrom(dsClass))
            dsFactory = (SpringBootDataSourceFactory) createInstanceByClassName(dsId, dsClass);
        if (dsFactory != null) {//create by factory
            try {
                ds = dsFactory.createDataSource(dsPrefix, dsId, environment);
            } catch (SpringBootDataSourceException e) {
                throw e;
            } catch (Exception e) {
                throw new SpringBootDataSourceException("DataSource(" + dsId + ")-Failed to get instance from dataSource factory", e);
            }
        } else if (DataSource.class.isAssignableFrom(dsClass)) {
            ds = (DataSource) createInstanceByClassName(dsId, dsClass);
            setConfigPropertiesValue(ds, dsPrefix, dsId, environment);
        } else {
            throw new SpringBootDataSourceException("DataSource(" + dsId + ")-target type is not a valid data source type");
        }

        return new SpringBootDataSource(dsId, ds, false);
    }

    private static Object createInstanceByClassName(String dsId, Class objClass) {
        try {
            return objClass.newInstance();
        } catch (Exception e) {
            throw new SpringBootDataSourceException("DataSource(" + dsId + ")-Failed to instantiated the class:" + objClass.getName(), e);
        }
    }

    //***************************************************************************************************************//
    //                                3: Spring Boot configuration set(3)                                            //
    //***************************************************************************************************************//
    public static void setConfigPropertiesValue(Object bean, String dsPrefix, String dsId, Environment environment) throws SpringBootDataSourceException {
        try {
            //1:get all set methods
            Map<String, Method> setMethodMap = getClassSetMethodMap(bean.getClass());
            //2:create map to collect config value
            Map<String, Object> setValueMap = new HashMap<>(setMethodMap.size());
            //3:loop to find out properties config value by set methods
            for (String propertyName : setMethodMap.keySet()) {
                String configVal = getConfigValue(dsPrefix, propertyName, environment);
                if (isBlank(configVal)) continue;
                setValueMap.put(propertyName, configVal.trim());
            }

            //4:inject found config value to ds config object
            setPropertiesValue(bean, setMethodMap, setValueMap);
        } catch (Throwable e) {
            throw new SpringBootDataSourceException("DataSource(" + dsId + ")-Failed to set properties", e);
        }
    }

    public static String getConfigValue(String dsPrefix, final String propertyName, Environment environment) {
        String value = readConfig(environment, dsPrefix + "." + propertyName);
        if (value != null) return value;

        String newPropertyName = propertyName.substring(0, 1).toLowerCase(Locale.US) + propertyName.substring(1);
        value = readConfig(environment, dsPrefix + "." + newPropertyName);
        if (value != null) return value;

        value = readConfig(environment, dsPrefix + "." + propertyNameToFieldId(newPropertyName, Separator_MiddleLine));
        if (value != null) return value;

        return readConfig(environment, dsPrefix + "." + propertyNameToFieldId(newPropertyName, Separator_UnderLine));
    }

    private static String readConfig(Environment environment, String key) {
        String value = environment.getProperty(key);
        if (!isBlank(value)) {
            value = value.trim();
            log.info("{}={}", key, value);
        }
        return value;
    }

    //***************************************************************************************************************//
    //                               4: other(2)                                                                     //
    //***************************************************************************************************************//
    static void tryToCloseDataSource(DataSource ds) {
        Class dsClass = ds.getClass();
        Class[] paramTypes = new Class[0];
        Object[] paramValues = new Object[0];
        String[] methodNames = new String[]{"close", "shutdown", "terminate"};
        for (String name : methodNames) {
            try {
                dsClass.getMethod(name, paramTypes).invoke(ds, paramValues);
                break;
            } catch (Throwable e) {
                //do nothing
            }
        }
    }

    private static final class SpringRegSupplier implements Supplier {
        private final Object ds;

        SpringRegSupplier(Object ds) {
            this.ds = ds;
        }

        public Object get() {
            return ds;
        }
    }
}
