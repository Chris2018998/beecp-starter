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
import cn.beecp.boot.datasource.config.BeeDataSourceSetFactory;
import cn.beecp.boot.datasource.config.ConfigException;
import cn.beecp.boot.datasource.config.DataSourceFieldSetFactory;
import org.springframework.core.env.Environment;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import javax.sql.XADataSource;
import java.util.HashMap;
import java.util.Map;

import static cn.beecp.boot.datasource.DataSourceUtil.*;

/**
 * DataSource builder by springboot Environment
 *
 * @author Chris.Liao
 */
class DataSourceBuilder {

    //Spring  DsAttributeSetFactory map
    private static final Map<Class, DataSourceFieldSetFactory> setFactoryMap = new HashMap<>(1);

    static {
        setFactoryMap.put(BeeDataSource.class, new BeeDataSourceSetFactory());
    }

    /**
     * create a dataSource
     *
     * @param dsId        dataSource config Name
     * @param dsPrefix    dataSource config prefix
     * @param environment dataSource config environment
     * @return a dataSource
     */
    public DataSourceHolder createDataSource(String dsId, String dsPrefix, Environment environment) {
        String jndiNameTex = getConfigValue(environment, dsPrefix, SP_Multi_DS_Jndi);
        if (!DataSourceUtil.isBlank(jndiNameTex)) {//jndi dataSource
            return lookupJndiDataSource(dsId, jndiNameTex);
        } else {//independent type
            return createDataSourceByDsType(dsId, dsPrefix, environment);
        }
    }

    /**
     * lookup a dataSource from middle-container
     *
     * @param dsId     dataSource config Name to register to Spring container
     * @param jndiName dataSource jndi name on middle-container
     * @return jndi DataSource
     */
    private DataSourceHolder lookupJndiDataSource(String dsId, String jndiName) {
        try {
            InitialContext context = new InitialContext();
            Object namingObj = context.lookup(jndiName);
            if (namingObj instanceof DataSource || namingObj instanceof XADataSource) {
                return new DataSourceHolder(dsId, namingObj, true);
            } else {
                throw new ConfigException("Jndi Name(" + jndiName + ") is not a dataSource object");
            }
        } catch (NamingException e) {
            throw new ConfigException("Failed to lookup dataSource by name:" + jndiName);
        }
    }

    //create dataSource instance by config class name
    private DataSourceHolder createDataSourceByDsType(String dsId, String dsConfigPrefix, Environment environment) {
        //1:load dataSource class and instantiate it
        String dataSourceClassName = getConfigValue(environment, dsConfigPrefix, SP_Multi_DS_Type);
        if (DataSourceUtil.isBlank(dataSourceClassName))
            dataSourceClassName = SP_Multi_DS_Default_Type;//BeeDataSource is default
        else
            dataSourceClassName = dataSourceClassName.trim();
        Class dataSourceClass = loadClass(dataSourceClassName.trim(), DataSource.class, "DataSource");

        Object ds = null;//may be DataSource or XADataSource
        if (XADataSource.class.isAssignableFrom(dataSourceClass)) {
            ds = createInstanceByClassName(dataSourceClass, XADataSource.class);
        } else if (DataSource.class.isAssignableFrom(dataSourceClass)) {
            ds = createInstanceByClassName(dataSourceClass, DataSource.class);
        } else {
            throw new ConfigException("Config value was not a valid datasource with key:" + dsConfigPrefix + "." + SP_Multi_DS_Type);
        }

        //2:load dataSource class and instantiate it
        String dataSourceFieldSetFactoryClassName = getConfigValue(environment, dsConfigPrefix, SP_Multi_DS_FieldSetFactory);
        if (!(ds instanceof BeeDataSource) && DataSourceUtil.isBlank(dataSourceFieldSetFactoryClassName))
            throw new ConfigException("Missed dataSource field set factory with key:" + dsConfigPrefix + "." + SP_Multi_DS_FieldSetFactory);
        DataSourceFieldSetFactory dsFieldSetFactory = null;
        if (!DataSourceUtil.isBlank(dataSourceFieldSetFactoryClassName)) {
            dataSourceFieldSetFactoryClassName = dataSourceFieldSetFactoryClassName.trim();
            Class dataSourceAttributeSetFactoryClass = loadClass(dataSourceFieldSetFactoryClassName, DataSourceFieldSetFactory.class, "DataSource properties factory");
            dsFieldSetFactory = (DataSourceFieldSetFactory) createInstanceByClassName(dataSourceAttributeSetFactoryClass, DataSourceFieldSetFactory.class);
        }

        if (dsFieldSetFactory == null) dsFieldSetFactory = setFactoryMap.get(dataSourceClass);
        if (dsFieldSetFactory == null)
            throw new ConfigException("Not found dataSource properties inject factory,please check config key:" + dsConfigPrefix + "." + SP_Multi_DS_FieldSetFactory);

        try {
            dsFieldSetFactory.setFields(ds, dsId, dsConfigPrefix, environment);//set properties to dataSource
        } catch (Exception e) {
            throw new ConfigException("Failed to inject config value to dataSource(" + dsId + ")", e);
        }
        return new DataSourceHolder(dsId, ds);
    }

    private Class loadClass(String className, Class type, String typeName) {
        try {
            return Class.forName(className);
        } catch (Exception e) {
            throw new ConfigException("Failed to load " + typeName + " class:" + className.trim());
        }
    }

    private Object createInstanceByClassName(Class objClass, Class type) {
        try {
            return objClass.newInstance();
        } catch (Exception e) {
            throw new ConfigException("Failed to create instance by class:" + objClass.getName(), e);
        }
    }
}

