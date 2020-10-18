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
import cn.beecp.boot.monitor.DataSourceCollector;
import cn.beecp.boot.setFactory.BeeDataSourceSetFactory;
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
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/*
 *  SpringBoot dataSource config demo
 *
 *  spring.datasource.name=ds1,ds2
 *  spring.datasource.ds1.datasourceType=cn.beecp.BeeDataSoruce
 *  spring.datasource.ds1.datasourceAttributeSetFactory=cn.beecp.boot.BeeDataSourceAttributeSetFactory
 *  spring.datasource.ds1.primary=true
 *  spring.datasource.ds1.attributeX=xxxx
 *
 *  spring.datasource.ds2.primary=false
 *  spring.datasource.ds2.jndiName=PlatformJndi
 *
 *   @author Chris.Liao
 */
public class MultiDataSourceRegister implements EnvironmentAware, ImportBeanDefinitionRegistrar {
    //Default DataSourceName
    private static final String Default_DataSource_Class_Name = "cn.beecp.BeeDataSource";
    //Spring dataSource configuration prefix-key name
    private static final String Spring_DataSource_Prefix = "spring.datasource";
    //Spring dataSource configuration key name
    private static final String Spring_DataSource_NameList = "spring.datasource.nameList";
    //Spring  DataSourceAttributeSetFactory map
    private static final Map<Class, DataSourceAttributeSetFactory> setFactoryMap = new HashMap<>();

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
        List<DataSourceRegisterInfo> dataSourceRegisterList = new LinkedList();
        String dataSourceNames = environment.getProperty(Spring_DataSource_NameList);
        if (!SystemUtil.isBlank(dataSourceNames)) {
            String[] dsNames = dataSourceNames.trim().split(",");
            for (String dsName : dsNames) {
                if (SystemUtil.isBlank(dsName)) continue;

                dsName = dsName.trim();
                String dsConfigPrefix = Spring_DataSource_Prefix + "." + dsName;
                String jndiNameKeyName = dsConfigPrefix + ".jndiName";
                String primaryKeyName = dsConfigPrefix + ".primary";

                String jndiNameText = environment.getProperty(jndiNameKeyName);
                String primaryText = environment.getProperty(primaryKeyName);
                boolean primaryDataSource = SystemUtil.isBlank(primaryText) ? false : Boolean.valueOf(primaryText.trim());

                Object ds = null;//may be DataSource or XADataSource
                if (!SystemUtil.isBlank(jndiNameText)) {//jndi dataSource
                    ds = lookupJndiDataSource(jndiNameText.trim());
                } else {//independent type
                    ds = createDataSource(dsName, dsConfigPrefix, environment);
                }

                if (ds != null) {
                    DataSourceRegisterInfo info = new DataSourceRegisterInfo();
                    info.setDataSource(ds);//maybe XA Type
                    info.setPrimary(primaryDataSource);
                    info.setRegisterName(dsName);
                    dataSourceRegisterList.add(info);
                }
            }
        }

        for (DataSourceRegisterInfo regInfo : dataSourceRegisterList) {
            registerDataSourceBean(regInfo, registry);
        }
    }

    //maybe XADataSource,if failed,then log error info,and return null
    private Object lookupJndiDataSource(String jndiName) {
        try {
            if (context == null) context = new InitialContext();
            Object namingObj = context.lookup(jndiName);
            if (namingObj instanceof XADataSource) {
                return new JndiXADataSourceWrapper((XADataSource) namingObj);
            } else if (namingObj instanceof DataSource) {
                return new JndiDataSourceWrapper((DataSource) namingObj);
            } else {
                log.error("Jndi name(" + jndiName + ")is a valid dataSource");
                return null;
            }
        } catch (NamingException e) {
            log.error("Jndi DataSource not found：" + jndiName, e);
            return null;
        }
    }

    //maybe XADataSource,if failed,then log error info,and return null
    private Object createDataSource(String dsName, String dsConfigPrefix, Environment environment) {
        String dataSourceType = dsConfigPrefix + ".datasourceType";
        String dataSourceAttributeSetFactory = dsConfigPrefix + ".datasourceAttributeSetFactory";
        String dataSourceClassName = environment.getProperty(dataSourceType);
        String dataSourceAttributeSetFactoryClassName = environment.getProperty(dataSourceAttributeSetFactory);

        if (SystemUtil.isBlank(dataSourceClassName))
            dataSourceClassName = Default_DataSource_Class_Name;//BeeDataSource is default
        else
            dataSourceClassName = dataSourceClassName.trim();

        Class dataSourceClass = loadClass(dataSourceClassName, DataSource.class, "DataSource");
        if (dataSourceClass == null) {
            log.error("DataSource class load failed,dataSource name:{},class name:{}", dsName, dataSourceClassName);
            return null;
        }

        Object ds = null;//may be DataSource or XADataSource
        if (XADataSource.class.isAssignableFrom(dataSourceClass)) {
            ds = createInstanceByClassName(dataSourceClass, DataSource.class, "XADataSource");
        } else if (DataSource.class.isAssignableFrom(dataSourceClass)) {
            ds = createInstanceByClassName(dataSourceClass, DataSource.class, "DataSource");
            if (ds instanceof BeeDataSource) DataSourceCollector.getInstance().addDataSource((BeeDataSource) ds);
        } else {
            log.error("DataSource class must be extended from DataSource or XADataSource,dataSource name:{},class name:{}", dsName, dataSourceClassName);
            return null;
        }

        DataSourceAttributeSetFactory dsAttrSetFactory = null;
        if (!SystemUtil.isBlank(dataSourceAttributeSetFactoryClassName)) {
            dataSourceAttributeSetFactoryClassName = dataSourceAttributeSetFactoryClassName.trim();
            Class dataSourceAttributeSetFactoryClass = loadClass(dataSourceAttributeSetFactoryClassName, DataSourceAttributeSetFactory.class, "DataSourceAttributeSetFactory");
            dsAttrSetFactory = (DataSourceAttributeSetFactory) createInstanceByClassName(dataSourceAttributeSetFactoryClass, DataSourceAttributeSetFactory.class, "DataSourceAttributeSetFactory");
        }

        if (dsAttrSetFactory == null) dsAttrSetFactory = setFactoryMap.get(dataSourceClass);
        if (dsAttrSetFactory == null) {
            log.error("DataSource instance create failed,dataSource name:{},class name:{}", dsName, dataSourceClassName);
        } else {
            try {
                dsAttrSetFactory.setAttributes(ds, dsConfigPrefix, environment);//set properties to dataSource
            } catch (Exception e) {
                log.error("Failed to set attribute on dataSource:" + dsName, e);
            }
        }
        return ds;
    }

    private void registerDataSourceBean(DataSourceRegisterInfo regInfo, BeanDefinitionRegistry registry) {
        if (!existsBeanDefinition(regInfo.getRegisterName(), registry)) {
            GenericBeanDefinition define = new GenericBeanDefinition();
            define.setBeanClass(regInfo.getDataSource().getClass());
            define.setPrimary(regInfo.isPrimary());
            define.setInstanceSupplier(new Supplier() {
                public Object get() {
                    return regInfo.getDataSource();
                }
            });
            registry.registerBeanDefinition(regInfo.getRegisterName(), define);
            log.info("Register dataSource({}) with bean name:{}", define.getBeanClassName(), regInfo.getRegisterName());
        } else {
            log.error("BeanDefinition with name:{} already exists in spring context", regInfo.getRegisterName());
        }
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
            Class objClass = Class.forName(className);
            if (!type.isAssignableFrom(objClass)) {
                log.warn("Target class({}) is not sub class of {}", className, typeName);
                return null;
            } else if (Modifier.isAbstract(objClass.getModifiers())) {
                log.warn("Target class({}) is abstract", className, typeName);
                return null;
            }
            return objClass;
        } catch (Exception e) {
            log.error("Failed to load class:{}", className, e);
            return null;
        }
    }

    private Object createInstanceByClassName(Class objClass, Class type, String typeName) {
        try {
            return objClass.newInstance();
        } catch (Exception e) {
            log.error("Failed to create instance by class name:{}", objClass.getName(), e);
            return null;
        }
    }
}
