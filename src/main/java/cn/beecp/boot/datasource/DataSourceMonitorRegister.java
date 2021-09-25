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
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;

import static cn.beecp.boot.datasource.SpringBootDataSourceUtil.createSupplier;

/**
 * Register Monitor to springboot
 *
 * @author Chris.Liao
 */
public class DataSourceMonitorRegister implements ImportBeanDefinitionRegistrar {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    //Register self bean to ioc
    public final void registerBeanDefinitions(AnnotationMetadata importingClassMetadata,
                                              BeanDefinitionRegistry registry) {

        // monitor controller
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
    }

    private boolean existsBeanDefinition(String beanName, BeanDefinitionRegistry registry) {
        try {
            return registry.getBeanDefinition(beanName) != null;
        } catch (NoSuchBeanDefinitionException e) {
            return false;
        }
    }
}
