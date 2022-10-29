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

import cn.beecp.boot.datasource.monitor.DataSourceMonitor;
import cn.beecp.boot.datasource.statement.StatementTraceAlert;

import java.util.concurrent.TimeUnit;

import static cn.beecp.pool.PoolStaticCenter.isBlank;

/*
 *  monitor config
 *
 * spring.datasource.monitor-userId=admin
 * spring.datasource.monitor-password=admin
 *
 * spring.datasource.sql-trace=true
 * spring.datasource.sql-show=true
 * spring.datasource.sql-trace-max-size=100
 * spring.datasource.sql-exec-slow-time=5000
 * spring.datasource.sql-trace-timeout=60000
 * spring.datasource.sql-exec-alert-action=xxxxx
 * spring.datasource.sql-trace-timeout-scan-period=18000
 *
 * spring.datasource.redis-host=192.168.1.1
 * spring.datasource.redis-port=6379
 * spring.datasource.redis-password=redis
 * spring.datasource.redis-send-period=18000
 * spring.datasource.redis-read-period=18000
 *
 * spring.datasource.jsonToolClassName=cn.beecp.boot.datasource.util.JackSonTool
 *
 * @author Chris.Liao
 */
public class DataSourceMonitorConfig {
    static DataSourceMonitorConfig single;

    //*********************************sql trace config begin *********************************************************//
    private boolean sqlShow;
    private boolean sqlTrace;
    private int sqlTraceMaxSize = 100;
    private long sqlExecSlowTime = TimeUnit.SECONDS.toMillis(6);
    private long sqlTraceTimeout = TimeUnit.MINUTES.toMillis(3);
    private long sqlTraceTimeoutScanPeriod = TimeUnit.MINUTES.toMillis(3);
    private StatementTraceAlert sqlExecAlertAction;

    //*********************************sql trace config end***********************************************************//

    //*********************************monitor config begin **********************************************************//
    private String monitorUserId;
    private String monitorPassword;
    private String monitorLoggedInTagName = DataSourceMonitor.class.getName();
    //*********************************monitor config end************************************************************//

    //*********************************redis config begin ************************************************************//
    private String redisHost;
    private int redisPort = 6379;
    private int redisTimeoutMs = 2000;
    private String redisUserId;
    private String redisPassword;
    private long redisSendPeriod = TimeUnit.MINUTES.toMillis(3);//node send
    private long redisReadPeriod = TimeUnit.MINUTES.toMillis(3);//center read
    //*********************************redis config end***************************************************************//

    //*********************************other config begin ************************************************************//
    private String jsonToolClassName;
    //*********************************other config end**********************************************************

    public boolean isSqlShow() {
        return sqlShow;
    }

    public void setSqlShow(boolean sqlShow) {
        this.sqlShow = sqlShow;
    }

    public boolean isSqlTrace() {
        return sqlTrace;
    }

    public void setSqlTrace(boolean sqlTrace) {
        this.sqlTrace = sqlTrace;
    }

    public int getSqlTraceMaxSize() {
        return sqlTraceMaxSize;
    }

    public void setSqlTraceMaxSize(int sqlTraceMaxSize) {
        if (sqlTraceMaxSize > 0) this.sqlTraceMaxSize = sqlTraceMaxSize;
    }

    public long getSqlTraceTimeout() {
        return sqlTraceTimeout;
    }

    public void setSqlTraceTimeout(long sqlTraceTimeout) {
        if (sqlTraceTimeout > 0)
            this.sqlTraceTimeout = sqlTraceTimeout;
    }

    public long getSqlExecSlowTime() {
        return sqlExecSlowTime;
    }

    public void setSqlExecSlowTime(long sqlExecSlowTime) {
        if (sqlExecSlowTime > 0)
            this.sqlExecSlowTime = sqlExecSlowTime;
    }

    public long getSqlTraceTimeoutScanPeriod() {
        return sqlTraceTimeoutScanPeriod;
    }

    public void setSqlTraceTimeoutScanPeriod(long sqlTraceTimeoutScanPeriod) {
        if (sqlTraceTimeoutScanPeriod > 0)
            this.sqlTraceTimeoutScanPeriod = sqlTraceTimeoutScanPeriod;
    }

    public StatementTraceAlert getSqlExecAlertAction() {
        return sqlExecAlertAction;
    }

    public void setSqlExecAlertAction(StatementTraceAlert sqlExecAlertAction) {
        if (sqlExecAlertAction != null) this.sqlExecAlertAction = sqlExecAlertAction;
    }

    public String getMonitorUserId() {
        return monitorUserId;
    }

    public void setMonitorUserId(String monitorUserId) {
        this.monitorUserId = monitorUserId;
    }

    public String getMonitorPassword() {
        return monitorPassword;
    }

    public void setMonitorPassword(String monitorPassword) {
        this.monitorPassword = monitorPassword;
    }

    public String getMonitorLoggedInTagName() {
        return monitorLoggedInTagName;
    }

    public void setMonitorLoggedInTagName(String monitorLoggedInTagName) {
        if (!isBlank(monitorLoggedInTagName))
            this.monitorLoggedInTagName = monitorLoggedInTagName;
    }

    public String getRedisHost() {
        return redisHost;
    }

    public void setRedisHost(String redisHost) {
        this.redisHost = redisHost;
    }

    public int getRedisPort() {
        return redisPort;
    }

    public void setRedisPort(int redisPort) {
        if (redisPort > 0) this.redisPort = redisPort;
    }

    public int getRedisTimeoutMs() {
        return redisTimeoutMs;
    }

    public void setRedisTimeoutMs(int redisTimeoutMs) {
        if (redisTimeoutMs > 0) this.redisTimeoutMs = redisTimeoutMs;
    }

    public String getRedisUserId() {
        return redisUserId;
    }

    public void setRedisUserId(String redisUserId) {
        this.redisUserId = redisUserId;
    }

    public String getRedisPassword() {
        return redisPassword;
    }

    public void setRedisPassword(String redisPassword) {
        this.redisPassword = redisPassword;
    }

    public long getRedisSendPeriod() {
        return redisSendPeriod;
    }

    public void setRedisSendPeriod(long redisSendPeriod) {
        if (redisSendPeriod > 0)
            this.redisSendPeriod = redisSendPeriod;
    }

    public long getRedisReadPeriod() {
        return redisReadPeriod;
    }

    public void setRedisReadPeriod(long redisReadPeriod) {
        if (redisReadPeriod > 0)
            this.redisReadPeriod = redisReadPeriod;
    }

    public String getJsonToolClassName() {
        return jsonToolClassName;
    }

    public void setJsonToolClassName(String jsonToolClassName) {
        this.jsonToolClassName = jsonToolClassName;
    }
}
