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
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotationMetadata;

import static cn.beecp.boot.datasource.SpringBootDataSourceUtil.*;

/**
 * Register Monitor to springboot
 *
 * @author Chris.Liao
 */
public class DataSourceMonitorRegister implements EnvironmentAware, ImportBeanDefinitionRegistrar {
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    //springboot environment
    private Environment environment;

    /**
     * Read dataSource configuration from environment and create DataSource
     *
     * @param environment SpringBoot Environment
     */
    public final void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    //Register self bean to ioc
    public final void registerBeanDefinitions(AnnotationMetadata importingClassMetadata,
                                              BeanDefinitionRegistry registry) {

        //1: register monitor controller
        String resetControllerRegName = DataSourceMonitor.class.getName();
        if (!existsBeanDefinition(resetControllerRegName, registry)) {
            GenericBeanDefinition define = new GenericBeanDefinition();
            define.setBeanClass(DataSourceMonitor.class);
            define.setPrimary(true);
            define.setInstanceSupplier(createSupplier(new DataSourceMonitor()));
            registry.registerBeanDefinition(resetControllerRegName, define);
            log.info("Register DataSource-restController({}) with id:{}", define.getBeanClassName(), resetControllerRegName);
        } else {
            log.error("BeanDefinition id {} already exists in spring context", resetControllerRegName);
        }

        //2: register monitor controller filter
        String resetControllerFilterRegName = DataSourceMonitorFilter.class.getName();
        if (!existsBeanDefinition(resetControllerFilterRegName, registry)) {
            GenericBeanDefinition define = new GenericBeanDefinition();
            define.setBeanClass(DataSourceMonitorFilter.class);
            define.setPrimary(true);
            define.setInstanceSupplier(createSupplier(new DataSourceMonitorFilter()));
            registry.registerBeanDefinition(resetControllerFilterRegName, define);
            log.info("Register DataSource-restController-Filter({}) with id:{}", define.getBeanClassName(), resetControllerRegName);
        } else {
            log.error("BeanDefinition id {} already exists in spring context", resetControllerFilterRegName);
        }

        //3:read admin account and password
        setAdminInfo(environment);
    }

    /**
     * read admin info
     *
     * @param environment Springboot environment
     */
    private void setAdminInfo(Environment environment) {
        String adminName = getConfigValue(Config_DS_Prefix, Config_DS_Monitor_UserId, environment);
        String adminPassword = getConfigValue(Config_DS_Prefix, Config_DS_Monitor_Password, environment);
        DataSourceMonitorAdmin admin = DataSourceMonitorAdmin.singleInstance;
        admin.setUserId(adminName);
        admin.setPassword(adminPassword);
    }
}
