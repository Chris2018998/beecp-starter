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

import cn.beecp.BeeDataSource;
import cn.beecp.boot.datasource.BeeDataSourceSetFactory;
import cn.beecp.boot.monitor.BeeDataSourceCollector;
import cn.beecp.boot.monitor.BeeDataSourceWrapper;
import cn.beecp.boot.monitor.sqltrace.SqlTracePool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotationMetadata;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import javax.sql.XADataSource;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Supplier;

import static cn.beecp.boot.DataSourceUtil.*;

/*
 *  SpringBoot dataSource config demo
 *
 *  spring.datasource.sqlExecutionTrace=true
 *  spring.datasource.sqlExecutionTraceTimeout=18000
 *
 *  spring.datasource.nameList=ds1,ds2
 *  spring.datasource.ds1.datasourceType=cn.beecp.BeeDataSoruce
 *  spring.datasource.ds1.propertySetFactory=cn.beecp.boot.BeeDsAttributeSetFactory
 *  spring.datasource.ds1.primary=true
 *  spring.datasource.ds1.attributeX=xxxx
 *
 *  spring.datasource.ds2.primary=false
 *  spring.datasource.ds2.jndiName=PlatformJndi
 *
 *   @author Chris.Liao
 */
public class MultiDataSourceRegister extends SingleDataSourceRegister implements EnvironmentAware, ImportBeanDefinitionRegistrar {

    //Spring  DsAttributeSetFactory map
    private static final Map<Class, DataSourceFieldSetFactory> setFactoryMap = new HashMap<>(1);

    static {
        setFactoryMap.put(BeeDataSource.class, new BeeDataSourceSetFactory());
    }

    //logger
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    //springboot
    private Environment environment;
    //jndi name context
    private InitialContext context = null;

    /**
     * Read dataSource configuration from environment and create DataSource
     *
     * @param environment SpringBoot Environment
     */
    public final void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    /**
     * Read dataSource configuration from environment and create DataSource
     *
     * @param importingClassMetadata register class meta
     * @param registry               springboot bean definition registry factory
     */
    public final void registerBeanDefinitions(AnnotationMetadata importingClassMetadata,
                                              BeanDefinitionRegistry registry) {
        //store dataSource register Info
        String dataSourceNames = getConfigValue(environment, Spring_DS_Prefix, Spring_DS_KEY_NameList);
        if (DataSourceUtil.isBlank(dataSourceNames))
            throw new ConfigException("Missed config item:" + Spring_DS_Prefix + "." + Spring_DS_KEY_NameList);
        String[] dsNames = dataSourceNames.trim().split(",");
        ArrayList<String> dsNameList = new ArrayList(dsNames.length);
        for (String name : dsNames) {
            name = name.trim();
            if (DataSourceUtil.isBlank(name)) continue;

            if (dsNameList.contains(name))
                throw new ConfigException("Duplicated dataSource name:" + name);
            if(this.existsBeanDefinition(name,registry))
                throw new ConfigException("Spring bean definition existed with dataSource name:" + name);

            dsNameList.add(name);
        }
        if (dsNameList.isEmpty())
            throw new ConfigException("Missed config item value:" + Spring_DS_Prefix + "." + Spring_DS_KEY_NameList);

        configSqlTracePool(environment);////set config properties to sql trace pool
        boolean isSqlTrace = SqlTracePool.getInstance().isSqlTrace();
        List<DsRegisterInfo> dsRegisterList = new LinkedList();
        Map<String, BeeDataSourceWrapper> dataSourceMap = new HashMap<>(dsNameList.size());

        try {
            for (String dsName : dsNameList) {
                String dsConfigPrefix = Spring_DS_Prefix + "." + dsName;

                String jndiNameTex = getConfigValue(environment, dsConfigPrefix, Spring_DS_KEY_Jndi);
                String primaryText = getConfigValue(environment, dsConfigPrefix, Spring_DS_KEY_Primary);
                boolean primaryDataSource = DataSourceUtil.isBlank(primaryText) ? false : Boolean.valueOf(primaryText.trim());

                Object ds = null;//may be DataSource or XADataSource
                if (!DataSourceUtil.isBlank(jndiNameTex)) {//jndi dataSource
                    ds = lookupJndiDataSource(jndiNameTex.trim());
                } else {//independent type
                    ds = createDataSource(dsName, dsConfigPrefix, environment);
                }

                if (ds != null) {
                    if (ds instanceof BeeDataSource) {//current dataSource type is BeeDataSource
                        BeeDataSourceWrapper dsWrapper = new BeeDataSourceWrapper((BeeDataSource) ds,dsName,isSqlTrace);
                        dataSourceMap.put(dsName, dsWrapper);
                        ds = dsWrapper;
                    }

                    DsRegisterInfo info = new DsRegisterInfo();
                    info.setDataSource(ds);//maybe XA Type
                    info.setPrimary(primaryDataSource);
                    info.setRegisterName(dsName);
                    dsRegisterList.add(info);
                }
            }

            //register to bean container
            for (DsRegisterInfo regInfo : dsRegisterList) {
                registerDataSourceBean(regInfo, registry);
            }
            BeeDataSourceCollector.getInstance().setDataSourceMap(dataSourceMap);
        } catch (Throwable e) {//failed then close all created dataSource
            for (DsRegisterInfo regInfo : dsRegisterList) {
                closeDataSource(regInfo.getDataSource());
            }
            throw new RuntimeException("multi-DataSource register failed", e);
        }
    }

    //lookup a dataSource from middle-container
    private Object lookupJndiDataSource(String jndiName) {
        try {
            if (context == null) context = new InitialContext();
            Object namingObj = context.lookup(jndiName);
            if (namingObj instanceof XADataSource) {
                return new JndiXADataSourceWrapper((XADataSource) namingObj);
            } else if (namingObj instanceof DataSource) {
                return new JndiDataSourceWrapper((DataSource) namingObj);
            } else {
                throw new ConfigException("Jndi Name(" + jndiName + ") is not a dataSource object");
            }
        } catch (NamingException e) {
            throw new ConfigException("Failed to lookup dataSource by name:" + jndiName);
        }
    }

    //create dataSource instance by config class name
    private Object createDataSource(String dsName, String dsConfigPrefix, Environment environment) {
        //1:load dataSource class and instantiate it
        String dataSourceClassName = getConfigValue(environment, dsConfigPrefix, Spring_DS_KEY_DatasourceType);
        if (DataSourceUtil.isBlank(dataSourceClassName))
            dataSourceClassName = Default_DS_Class_Name;//BeeDataSource is default
        else
            dataSourceClassName = dataSourceClassName.trim();
        Class dataSourceClass = loadClass(dataSourceClassName.trim(), DataSource.class, "DataSource");

        Object ds = null;//may be DataSource or XADataSource
        if (XADataSource.class.isAssignableFrom(dataSourceClass)) {
            ds = createInstanceByClassName(dataSourceClass, XADataSource.class);
        } else if (DataSource.class.isAssignableFrom(dataSourceClass)) {
            ds = createInstanceByClassName(dataSourceClass, DataSource.class);
        } else {
            throw new ConfigException("Config value was not a valid datasource with key:" + dsConfigPrefix + "." + Spring_DS_KEY_DatasourceType);
        }

        //2:load dataSource class and instantiate it
        String dataSourceFieldSetFactoryClassName = getConfigValue(environment, dsConfigPrefix, Spring_DS_KEY_FieldSetFactory);
        if (!(ds instanceof BeeDataSource) && DataSourceUtil.isBlank(dataSourceFieldSetFactoryClassName))
            throw new ConfigException("Missed dataSource field set factory with key:" + dsConfigPrefix + "." + Spring_DS_KEY_FieldSetFactory);
        DataSourceFieldSetFactory dsFieldSetFactory =null;
        if (!DataSourceUtil.isBlank(dataSourceFieldSetFactoryClassName)) {
            dataSourceFieldSetFactoryClassName = dataSourceFieldSetFactoryClassName.trim();
            Class dataSourceAttributeSetFactoryClass = loadClass(dataSourceFieldSetFactoryClassName, DataSourceFieldSetFactory.class, "DataSource properties factory");
            dsFieldSetFactory = (DataSourceFieldSetFactory) createInstanceByClassName(dataSourceAttributeSetFactoryClass, DataSourceFieldSetFactory.class);
        }
        if (dsFieldSetFactory == null) dsFieldSetFactory = setFactoryMap.get(dataSourceClass);
        if (dsFieldSetFactory == null)
            throw new ConfigException("Not found dataSource properties inject factory,please check config key:" + dsConfigPrefix + "." + Spring_DS_KEY_FieldSetFactory);

        //3:inject properties to dataSource
        try {
            dsFieldSetFactory.setFields(ds, dsName, dsConfigPrefix, environment);//set properties to dataSource
        } catch (Exception e) {
            throw new ConfigException("Failed to inject attribute to dataSource(" + dsName + ")", e);
        }
        return ds;
    }

    //register dataSource to Spring bean container
    private void registerDataSourceBean(DsRegisterInfo regInfo, BeanDefinitionRegistry registry) {
        GenericBeanDefinition define = new GenericBeanDefinition();
        define.setBeanClass(regInfo.getDataSource().getClass());
        define.setPrimary(regInfo.isPrimary());
        define.setInstanceSupplier(new Supplier() {
            public Object get() {
                return regInfo.getDataSource();
            }
        });
        registry.registerBeanDefinition(regInfo.getRegisterName(), define);
        log.info("Registered dataSource({}) with bean name:{}", define.getBeanClassName(), regInfo.getRegisterName());
    }

    private boolean existsBeanDefinition(String beanName, BeanDefinitionRegistry registry) {
        try {
            return registry.getBeanDefinition(beanName) != null;
        } catch (NoSuchBeanDefinitionException e) {
            return false;
        }
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

    private void closeDataSource(Object dataSource) {
        Method method = null;
        Class dsClass = dataSource.getClass();
        try {
            method = dsClass.getMethod("close", new Class[0]);
        } catch (Exception e) {
        }

        if (method == null) {
            try {
                method = dsClass.getMethod("destroy", new Class[0]);
            } catch (Exception e) {
            }
        }

        if (method == null) {
            try {
                method = dsClass.getMethod("terminate", new Class[0]);
            } catch (Exception e) {
            }
        }

        if (method != null) {
            try {
                method.invoke(dataSource, new Object[0]);
            } catch (Exception e) {
            }
        }
    }

    class DsRegisterInfo {

        private boolean primary;

        private String registerName;

        private Object dataSource;

        public boolean isPrimary() {
            return primary;
        }

        public void setPrimary(boolean primary) {
            this.primary = primary;
        }

        public String getRegisterName() {
            return registerName;
        }

        public void setRegisterName(String registerName) {
            this.registerName = registerName;
        }

        public Object getDataSource() {
            return dataSource;
        }

        public void setDataSource(Object dataSource) {
            this.dataSource = dataSource;
        }
    }
}

