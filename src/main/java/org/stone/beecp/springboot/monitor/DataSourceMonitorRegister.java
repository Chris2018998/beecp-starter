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
package org.stone.beecp.springboot.monitor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotationMetadata;
import org.stone.beecp.springboot.DataSourceMonitorConfig;
import org.stone.beecp.springboot.SpringBootDataSourceUtil;

import javax.servlet.Filter;

/**
 * Register Monitor to springboot
 *
 * @author Chris Liao
 */
public class DataSourceMonitorRegister implements EnvironmentAware, ImportBeanDefinitionRegistrar {
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    //springboot environment
    private Environment environment;

    public final void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    //Register monitor bean to ioc
    public final void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        //1:read datasource monitor
        DataSourceMonitorConfig config = SpringBootDataSourceUtil.readMonitorConfig(environment);

        //2:assembly monitor controller
        String resetControllerRegName = DataSourceMonitor.class.getName();
        if (!SpringBootDataSourceUtil.existsBeanDefinition(resetControllerRegName, registry)) {
            GenericBeanDefinition define = new GenericBeanDefinition();
            define.setBeanClass(DataSourceMonitor.class);
            define.setPrimary(true);
            define.setInstanceSupplier(SpringBootDataSourceUtil.createSpringSupplier(new DataSourceMonitor(
                    config.getConsoleUserId(),
                    config.getConsolePassword(),
                    config.getMonitorLoggedInTagName())));
            registry.registerBeanDefinition(resetControllerRegName, define);
            log.info("Register DataSource-restController({}) with id:{}", define.getBeanClassName(), resetControllerRegName);
        } else {
            log.error("BeanDefinition id {} already exists in spring context", resetControllerRegName);
        }

        //3: assembly monitor controller filter
        String resetControllerFilterRegName = DataSourceMonitorFilter.class.getName();
        if (!SpringBootDataSourceUtil.existsBeanDefinition(resetControllerFilterRegName, registry)) {

            DataSourceMonitorFilter dsFilter = new DataSourceMonitorFilter(config.getConsoleUserId(), config.getMonitorLoggedInTagName());
            FilterRegistrationBean<Filter> registration = new FilterRegistrationBean<>(dsFilter);
            registration.setName("beecpMonitorFilter");
            registration.addUrlPatterns("/beecp/*");

            GenericBeanDefinition define = new GenericBeanDefinition();
            define.setBeanClass(FilterRegistrationBean.class);
            define.setPrimary(true);
            define.setInstanceSupplier(SpringBootDataSourceUtil.createSpringSupplier(registration));
            registry.registerBeanDefinition(resetControllerFilterRegName, define);
            log.info("Register DataSource-restController-Filter({}) with id:{}", define.getBeanClassName(), resetControllerRegName);
        } else {
            log.error("BeanDefinition id {} already exists in spring context", resetControllerFilterRegName);
        }
    }
}
