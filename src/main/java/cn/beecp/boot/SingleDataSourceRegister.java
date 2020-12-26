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
import cn.beecp.boot.monitor.DataSourceWrapper;
import cn.beecp.boot.monitor.proxy.SQLExecutionPool;
import cn.beecp.boot.setFactory.BeeDataSourceSetFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

import javax.sql.DataSource;

import static cn.beecp.boot.SystemUtil.*;

/*
 *  SpringBoot dataSource config demo
 *  spring.datasource.type=cn.beecp.BeeDataSource
 *  spring.datasource.*=xx
 *
 *   @author Chris.Liao
 */
@ConditionalOnClass(cn.beecp.boot.monitor.DataSourceWrapper.class)
@ConditionalOnProperty(name = "spring.datasource.type", havingValue = "cn.beecp.BeeDataSource")
public class SingleDataSourceRegister {

    @Bean
    public DataSource beeDataSource(Environment environment) throws Exception {
        String sqlExecTraceInd = environment.getProperty(Spring_DS_Prefix + "." + Spring_DS_KEY_ExecutionTrace);
        String sqlExecTraceTimeout = environment.getProperty(Spring_DS_Prefix + "." + Spring_DS_KEY_ExecutionTrace_Timeout);

        boolean traceSqlExecution = true;
        if (!SystemUtil.isBlank(sqlExecTraceInd)) {
            try {
                traceSqlExecution = Boolean.parseBoolean(sqlExecTraceInd.trim());
            } catch (Throwable e) {
            }
        }
        if (!SystemUtil.isBlank(sqlExecTraceTimeout)) {
            try {
                SQLExecutionPool.getInstance().setTracedTimeoutMs(Long.parseLong(sqlExecTraceTimeout.trim()));
            } catch (Throwable e) {
            }
        }

        BeeDataSource ds = new BeeDataSource();
        BeeDataSourceSetFactory dsAttrSetFactory = new BeeDataSourceSetFactory();
        dsAttrSetFactory.setAttributes(ds, Spring_DS_Prefix, environment);//set properties to dataSource
        DataSourceWrapper dsWrapper = new DataSourceWrapper(ds, traceSqlExecution);
        DataSourceCollector.getInstance().addDataSource(dsWrapper);
        return dsWrapper;
    }
}
