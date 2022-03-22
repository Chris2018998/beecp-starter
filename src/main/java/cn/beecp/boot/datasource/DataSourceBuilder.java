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
import org.springframework.core.env.Environment;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

import static cn.beecp.boot.datasource.SpringBootDataSourceUtil.*;

/**
 * DataSource builder from springboot Environment
 *
 * @author Chris.Liao
 */
class DataSourceBuilder {

    //Spring  DsAttributeSetFactory map
    private static final Map<Class, SpringBootDataSourceFactory> factoryMap = new HashMap<>(1);

    static {
        factoryMap.put(BeeDataSource.class, new BeeDataSourceFactory());
    }

    /**
     * create a dataSource
     *
     * @param dsId        dataSource config Name
     * @param dsPrefix    dataSource config prefix
     * @param environment dataSource config environment
     * @return a dataSource
     */
    public DataSourceHolder createDataSource(String dsId, String dsPrefix, Environment environment, boolean isSqlTrace) {
        String jndiNameTex = getConfigValue(environment, dsPrefix, SP_DS_Jndi);
        if (!SpringBootDataSourceUtil.isBlank(jndiNameTex)) {//jndi dataSource
            return lookupJndiDataSource(dsId, jndiNameTex, isSqlTrace);
        } else {//independent type
            return createDataSourceByDsType(dsId, dsPrefix, environment, isSqlTrace);
        }
    }

    /**
     * lookup a dataSource from middle-container
     *
     * @param dsId     dataSource config Name to register to Spring container
     * @param jndiName dataSource jndi name on middle-container
     * @return a jndi DataSource
     */
    private DataSourceHolder lookupJndiDataSource(String dsId, String jndiName, boolean traceSQL) {
        try {
            InitialContext context = new InitialContext();
            Object namingObj = context.lookup(jndiName);
            if (namingObj instanceof DataSource) {
                return new DataSourceHolder(dsId, new SpringRegDataSource(dsId, (DataSource) namingObj, traceSQL, true), true);
            } else {
                throw new SpringBootDataSourceException("DataSource(" + dsId + ")-Jndi Name(" + jndiName + ") is not a data source object");
            }
        } catch (NamingException e) {
            throw new SpringBootDataSourceException("DataSource(" + dsId + ")-Failed to lookup data source by jndi-name:" + jndiName);
        }
    }

    //create rawConn dataSource instance by config class name
    private DataSourceHolder createDataSourceByDsType(String dsId, String dsConfigPrefix, Environment environment, boolean traceSQL) {
        //1:load dataSource class
        String dataSourceClassName = getConfigValue(environment, dsConfigPrefix, SP_DS_Type);
        if (SpringBootDataSourceUtil.isBlank(dataSourceClassName))
            dataSourceClassName = SP_DS_Default_Type;//BeeDataSource is default
        else
            dataSourceClassName = dataSourceClassName.trim();

        //2:create dataSource
        DataSource ds;
        Class dataSourceClass = loadClass(dsId, dataSourceClassName);
        SpringBootDataSourceFactory dsFactory = factoryMap.get(dataSourceClass);
        if (dsFactory == null && SpringBootDataSourceFactory.class.isAssignableFrom(dataSourceClass))
            dsFactory = (SpringBootDataSourceFactory) createInstanceByClassName(dsId, dataSourceClass);
        if (dsFactory != null) {//create by factory
            try {
                ds = dsFactory.createDataSource(environment, dsId, dsConfigPrefix);
            } catch (SpringBootDataSourceException e) {
                throw e;
            } catch (Exception e) {
                throw new SpringBootDataSourceException("DataSource(" + dsId + ")-Failed to get instance from dataSource factory", e);
            }
        } else if (DataSource.class.isAssignableFrom(dataSourceClass)) {
            ds = createInstanceByClassName(dsId, dataSourceClass);
            configDataSource(ds, environment, dsId, dsConfigPrefix);
        } else {
            throw new SpringBootDataSourceException("DataSource(" + dsId + ")-target type is not a valid data source type");
        }

        return new DataSourceHolder(dsId, new SpringRegDataSource(dsId, ds, traceSQL, false), false);
    }

    private Class loadClass(String dsId, String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new SpringBootDataSourceException("DataSource(" + dsId + ")-Not found class:" + className);
        }
    }

    private DataSource createInstanceByClassName(String dsId, Class objClass) {
        try {
            return (DataSource) objClass.newInstance();
        } catch (Exception e) {
            throw new SpringBootDataSourceException("DataSource(" + dsId + ")-Failed to instantiated the class:" + objClass.getName(), e);
        }
    }
}

