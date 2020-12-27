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
import cn.beecp.boot.monitor.proxy.SqlExecutionAlert;
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
        boolean traceSqlExec = setSQLExecutionTrace(environment);
        BeeDataSource ds = new BeeDataSource();
        BeeDataSourceSetFactory dsAttrSetFactory = new BeeDataSourceSetFactory();
        dsAttrSetFactory.setAttributes(ds, Spring_DS_Prefix, environment);//set properties to dataSource
        DataSourceWrapper dsWrapper = new DataSourceWrapper(ds, traceSqlExec);
        DataSourceCollector.getInstance().addDataSource(dsWrapper);
        return dsWrapper;
    }

    //read sql execution trace configuration
    protected boolean setSQLExecutionTrace(Environment environment) {
        String sqlExecTraceInd = environment.getProperty(Spring_DS_Prefix + "." + Spring_DS_KEY_SQL_Exec_Trace);
        String sqlExecTraceTimeout = environment.getProperty(Spring_DS_Prefix + "." + Spring_DS_KEY_SQL_Exec_Trace_Timeout);
        String sqlExecTraceMaxSize = environment.getProperty(Spring_DS_Prefix + "." + Spring_DS_KEY_SQL_Exec_Trace_MaxSize);
        String sqlExecutionAlertTime = environment.getProperty(Spring_DS_Prefix + "." + Spring_DS_KEY_SQL_Exec_Alert_Time);
        String sqlExecutionAlertAction = environment.getProperty(Spring_DS_Prefix + "." + Spring_DS_KEY_SQL_Exec_Alert_Action);

        boolean traceSQLExecution = true;
        SQLExecutionPool tracePool = SQLExecutionPool.getInstance();
        if (!SystemUtil.isBlank(sqlExecTraceInd)) {
            try {
                traceSQLExecution = Boolean.parseBoolean(sqlExecTraceInd.trim());
            } catch (Throwable e) {
            }
        }
        if (!SystemUtil.isBlank(sqlExecTraceTimeout)) {
            try {
                tracePool.setTraceTimeoutMs(Long.parseLong(sqlExecTraceTimeout.trim()));
            } catch (Throwable e) {
            }
        }

        if (!SystemUtil.isBlank(sqlExecTraceMaxSize)) {
            try {
                tracePool.setTraceMaxSize(Integer.parseInt(sqlExecTraceMaxSize.trim()));
            } catch (Throwable e) {
            }
        }

        if (!SystemUtil.isBlank(sqlExecutionAlertTime)) {
            try {
                tracePool.setSqlExecutionAlertTime(Long.parseLong(sqlExecutionAlertTime.trim()));
            } catch (Throwable e) {
            }
        }

        if (!SystemUtil.isBlank(sqlExecutionAlertAction)) {
            try {
                Class actionClass = Class.forName(sqlExecutionAlertAction);
                SqlExecutionAlert alert = (SqlExecutionAlert) actionClass.newInstance();

                tracePool.setSqlExecutionAlert(alert);
            } catch (Throwable e) {
            }
        }

        return traceSQLExecution;
    }
}
