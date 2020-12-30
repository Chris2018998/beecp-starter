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
    private int sqlTraceSize;
    private long sqlTraceTimeout;
    private long sqlTraceTimeoutScanPeriod;
    private long sqlTraceAlertTime;
    private SqlTraceAlert sqlTraceAlert;

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

    public int getSqlTraceSize() {
        return sqlTraceSize;
    }

    public void setSqlTraceSize(int sqlTraceSize) {
        this.sqlTraceSize = sqlTraceSize;
    }

    public long getSqlTraceTimeout() {
        return sqlTraceTimeout;
    }

    public void setSqlTraceTimeout(long sqlTraceTimeout) {
        this.sqlTraceTimeout = sqlTraceTimeout;
    }

    public long getSqlTraceTimeoutScanPeriod() {
        return sqlTraceTimeoutScanPeriod;
    }

    public void setSqlTraceTimeoutScanPeriod(long sqlTraceTimeoutScanPeriod) {
        this.sqlTraceTimeoutScanPeriod = sqlTraceTimeoutScanPeriod;
    }

    public long getSqlTraceAlertTime() {
        return sqlTraceAlertTime;
    }

    public void setSqlTraceAlertTime(long sqlTraceAlertTime) {
        this.sqlTraceAlertTime = sqlTraceAlertTime;
    }

    public SqlTraceAlert getSqlTraceAlert() {
        return sqlTraceAlert;
    }

    public void setSqlTraceAlert(SqlTraceAlert sqlTraceAlert) {
        this.sqlTraceAlert = sqlTraceAlert;
    }
}
