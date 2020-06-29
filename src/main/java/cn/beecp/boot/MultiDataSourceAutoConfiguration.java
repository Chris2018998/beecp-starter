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
import cn.beecp.util.BeecpUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.BeanDefinition;

import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.*;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.ApplicationContext;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import javax.xml.crypto.Data;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/*
 *  SpringBoot dataSource config demo
 *
 *  spring.datasource.name=d1,d2
 *  spring.datasource.d1.datasourceType=cn.beecp.BeeDataSoruce
 *  spring.datasource.d1.datasourceAttributeSetFactory=cn.beecp.boot.BeeDataSourceAttributeSetFactory
 *  spring.datasource.d1.primary=true
 *  spring.datasource.d1.attributeX=xxxx
 *
 *  spring.datasource.d2.primary=false
 *  spring.datasource.d2.jndiName=PlatformJndi
 *
 *   @author Chris.Liao
 */
@AutoConfigureBefore(DataSourceAutoConfiguration.class)
public class MultiDataSourceAutoConfiguration implements BeanDefinitionRegistryPostProcessor{
    //Default DataSourceName
    private static final String Default_DataSource_Name="cn.beecp.BeeDataSource";
    //Spring dataSource configuration prefix-key name
    private static final String Spring_DataSource_Prefix="spring.datasource";
    //Spring dataSource configuration key name
    private static final String Spring_DataSource_NameList="spring.datasource.nameList";
    //Spring  DataSourceAttributeSetFactory map
    private static final Map<Class,DataSourceAttributeSetFactory> setFactoryMap=new HashMap<>();
    //logger
    private static final Logger log = LoggerFactory.getLogger(MultiDataSourceAutoConfiguration.class);
    static{
        setFactoryMap.put(BeeDataSource.class,new BeeDataSourceAttributeSetFactory());
    }

    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {}
    public void postProcessBeanFactory(ConfigurableListableBeanFactory factory) throws BeansException {
        Environment environment=factory.getBean(Environment.class);
        registerDataSource(factory,environment);
    }

    private void registerDataSource(ConfigurableListableBeanFactory registry,Environment environment){
        String dataSourceNames=environment.getProperty(Spring_DataSource_NameList);
        if(!BeecpUtil.isNullText(dataSourceNames)){
            String[]dsNames=dataSourceNames.trim().split(",");
            for(String dsName:dsNames){
                if(BeecpUtil.isNullText(dsName))continue;

                dsName=dsName.trim();
                String dsConfigPrefix=Spring_DataSource_Prefix+"."+dsName;
                String jndiNameKeyName=dsConfigPrefix+".jndiName";
                String primaryKeyName=dsConfigPrefix+".primary";

                String jndiNameText=environment.getProperty(jndiNameKeyName);
                String primaryText=environment.getProperty(primaryKeyName);
                boolean primaryDataSource=BeecpUtil.isNullText(primaryText)?false:Boolean.valueOf(primaryText.trim());

                DataSource ds;
                if(!BeecpUtil.isNullText(jndiNameText)){//jndi type
                    try {
                        ds = lookupDataSource(jndiNameText.trim());
                        registerDataSourceBean(ds,dsName,primaryDataSource,registry);
                    }catch(NamingException e) {
                        log.error("Jndi DataSource not found：" + dsName);
                    }
                }else{//independent type
                    String dataSourceType=dsConfigPrefix+".datasourceType";
                    String dataSourceAttributeSetFactory=dsConfigPrefix+".datasourceAttributeSetFactory";
                    String dataSourceClassName=environment.getProperty(dataSourceType);
                    String dataSourceAttributeSetFactoryClassName=environment.getProperty(dataSourceAttributeSetFactory);

                    if(BeecpUtil.isNullText(dataSourceClassName))
                        dataSourceClassName=Default_DataSource_Name;//BeeDataSource is default

                    Class dataSourceClass=loadClass(dataSourceClassName,DataSource.class,"DataSource");
                    if(dataSourceClass==null){
                        log.error("DataSource class load failed,dataSource name:{},class name:{}",dsName,dataSourceClassName);
                        continue;
                    }
                    ds=(DataSource)createInstanceByClassName(dataSourceClass,DataSource.class,"DataSource");
                    if(ds==null){
                        log.error("DataSource instance create failed,dataSource name:{},class name:{}",dsName,dataSourceClassName);
                        continue;
                    }

                    DataSourceAttributeSetFactory dsAttrSetFactory=null;
                    if(!BeecpUtil.isNullText(dataSourceAttributeSetFactoryClassName)){
                        Class dataSourceAttributeSetFactoryClass=loadClass(dataSourceAttributeSetFactoryClassName,DataSourceAttributeSetFactory.class,"DataSourceAttributeSetFactory");
                        dsAttrSetFactory=(DataSourceAttributeSetFactory)createInstanceByClassName(dataSourceAttributeSetFactoryClass,DataSourceAttributeSetFactory.class,"DataSourceAttributeSetFactory");
                    }
                    if(dsAttrSetFactory==null)dsAttrSetFactory=setFactoryMap.get(dataSourceClass);
                    if(dsAttrSetFactory==null) {
                        log.error("DataSource instance create failed,dataSource name:{},class name:{}", dsName, dataSourceClassName);
                    }else{
                        try {
                            dsAttrSetFactory.set(ds,dsConfigPrefix,environment);//set properties to dataSource
                            registerDataSourceBean(ds,dsName,primaryDataSource,registry);//register DataSource as Ioc Bean
                        }catch(Exception e) {
                            e.printStackTrace();
                            log.error("Failed to set attribute on dataSource:" + dsName,e);
                        }
                    }
                }
            }
        }
    }
    private void registerDataSourceBean(final DataSource dataSource,String beanName,boolean primary,ConfigurableListableBeanFactory factory) {
        if (!existsBeanDefinition(beanName,factory)) {
            GenericBeanDefinition define = new GenericBeanDefinition();
            define.setBeanClass(dataSource.getClass());
            define.setPrimary(primary);
            define.setInstanceSupplier(new Supplier(){
                public DataSource get() {
                    return dataSource;
                }
            });

            ((DefaultListableBeanFactory) factory).registerBeanDefinition(beanName,define);
             String[] names=factory.getBeanNamesForType(DataSource.class);
             for(String name:names){
                 BeanDefinition datasourceInfo= factory.getBeanDefinition(name);
                 System.out.println(name+ "  registerDataSourceBean: " + datasourceInfo.getBeanClassName());
             }
           log.error("register dataSource({}) with name:{}",dataSource.getClass(),beanName);
        } else {
            log.error("BeanDefinition with name:{} already exists in spring context" + beanName);
        }
    }
    private boolean existsBeanDefinition(String beanName,ConfigurableListableBeanFactory registry){
        try {
            return (registry.getBeanDefinition(beanName) != null) ? true : false;
        }catch(NoSuchBeanDefinitionException e){
            return false;
        }
    }
    private DataSource lookupDataSource(String name)throws NamingException {
        InitialContext context=new InitialContext();
        return new JndiDataSourceWrapper((DataSource) context.lookup(name));
    }
    private Class loadClass(String className,Class type,String typeName){
        try {
            Class objClass = Class.forName(className);
            if(!type.isAssignableFrom(objClass)){
                log.warn("Target class({}) is not sub class of {}",className,typeName);
                return null;
            }else if(Modifier.isAbstract(objClass.getModifiers())){
                log.warn("Target class({}) is abstract",className,typeName);
                return null;
            }
            return objClass;
        }catch(Exception e){
            log.error("Failed to load class:{}",className,e);
            return null;
        }
    }
    private  Object createInstanceByClassName(Class objClass,Class type,String typeName){
        try {
            return objClass.newInstance();
        }catch(Exception e){
            log.error("Failed to create instance by class name:{}",objClass.getName(),e);
            return null;
        }
    }
}
