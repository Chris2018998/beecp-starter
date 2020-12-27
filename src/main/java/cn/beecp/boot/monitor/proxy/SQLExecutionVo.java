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
package cn.beecp.boot.monitor.proxy;

/*
 *  SQL Execute Trace Vo
 *
 *  @author Chris.Liao
 */
public class SQLExecutionVo {
    private String poolName;
    private String statementType;
    private String executeSQL;
    private String startTime;
    private long startTimeMs;
    private String endTime;
    private long tookTimeMs;
    private boolean success;
    private boolean timeAlert;
    private String methodName;
    private Throwable failCause;

    public SQLExecutionVo(String sql, String poolName, String statementType) {
        this.executeSQL = sql;
        this.poolName = poolName;
        this.statementType = statementType;
    }

    public String getPoolName() {
        return poolName;
    }

    public void setPoolName(String poolName) {
        this.poolName = poolName;
    }

    public String getStatementType() {
        return statementType;
    }

    public void setStatementType(String statementType) {
        this.statementType = statementType;
    }

    public String getExecuteSQL() {
        return executeSQL;
    }

    public void setExecuteSQL(String executeSQL) {
        this.executeSQL = executeSQL;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public long getStartTimeMs() {
        return startTimeMs;
    }

    public void setStartTimeMs(long startTimeMs) {
        this.startTimeMs = startTimeMs;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public long getTookTimeMs() {
        return tookTimeMs;
    }

    public void setTookTimeMs(long tookTimeMs) {
        this.tookTimeMs = tookTimeMs;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public boolean isTimeAlert() {
        return timeAlert;
    }

    public void setTimeAlert(boolean timeAlert) {
        this.timeAlert = timeAlert;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public Throwable getFailCause() {
        return failCause;
    }

    public void setFailCause(Throwable failCause) {
        this.failCause = failCause;
    }
}
