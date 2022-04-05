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
package cn.beecp.boot.datasource.statement;

import java.util.concurrent.TimeUnit;

/**
 * statement Config
 *
 * @author Chris.Liao
 */
public class StatementTraceConfig {
    private boolean sqlShow;
    private boolean sqlTrace;
    private int sqlTraceMaxSize = 100;
    private long sqlExecSlowTime = TimeUnit.SECONDS.toMillis(6);
    private long sqlTraceTimeout = TimeUnit.MINUTES.toMillis(3);
    private long sqlTraceTimeoutScanPeriod = TimeUnit.MINUTES.toMillis(3);
    private StatementTraceAlert sqlExecAlertAction;

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
}
