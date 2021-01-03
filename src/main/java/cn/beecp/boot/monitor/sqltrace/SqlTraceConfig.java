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
package cn.beecp.boot.monitor.sqltrace;

/**
 * trace Config
 *
 * @author Chris.Liao
 */
public class SqlTraceConfig {
    private boolean sqlTrace;
    private boolean sqlShow;
    private int sqlTraceMaxSize;
    private long sqlTraceTimeout;
    private long sqlExecAlertTime;
    private SqlTraceAlert sqlTraceAlert;
    private long sqlTraceTimeoutScanPeriod;

    public boolean isSqlTrace() {
        return sqlTrace;
    }

    public void setSqlTrace(boolean sqlTrace) {
        this.sqlTrace = sqlTrace;
    }

    public boolean isSqlShow() {
        return sqlShow;
    }

    public void setSqlShow(boolean sqlShow) {
        this.sqlShow = sqlShow;
    }

    public int getSqlTraceMaxSize() {
        return sqlTraceMaxSize;
    }

    public void setSqlTraceMaxSize(int sqlTraceMaxSize) {
        this.sqlTraceMaxSize = sqlTraceMaxSize;
    }

    public long getSqlTraceTimeout() {
        return sqlTraceTimeout;
    }

    public void setSqlTraceTimeout(long sqlTraceTimeout) {
        this.sqlTraceTimeout = sqlTraceTimeout;
    }

    public long getSqlExecAlertTime() {
        return sqlExecAlertTime;
    }

    public void setSqlExecAlertTime(long sqlExecAlertTime) {
        this.sqlExecAlertTime = sqlExecAlertTime;
    }

    public SqlTraceAlert getSqlTraceAlert() {
        return sqlTraceAlert;
    }

    public void setSqlTraceAlert(SqlTraceAlert sqlTraceAlert) {
        this.sqlTraceAlert = sqlTraceAlert;
    }

    public long getSqlTraceTimeoutScanPeriod() {
        return sqlTraceTimeoutScanPeriod;
    }

    public void setSqlTraceTimeoutScanPeriod(long sqlTraceTimeoutScanPeriod) {
        this.sqlTraceTimeoutScanPeriod = sqlTraceTimeoutScanPeriod;
    }
}
