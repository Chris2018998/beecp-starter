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

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Date;
import java.util.Objects;
import java.util.UUID;

import static cn.beecp.boot.datasource.SpringBootDataSourceUtil.formatDate;

/*
 *  SQL Execute Trace entry
 *
 *  @author Chris.Liao
 */
public class StatementTrace {
    private final String sql;
    private final String dsId;
    private final String dsUUID;
    private final String sqlUUID;
    private final String statementType;

    private long startTimeMs;
    private String startTime;
    private long endTimeMs;
    private String endTime;
    private long tookTimeMs;

    private boolean slowInd;
    private boolean successInd;
    private boolean alertedInd;

    @JsonIgnore
    private Throwable failCause;
    private String methodName;

    StatementTrace(String dsId, String dsUUID, String sql, String statementType) {
        this.dsId = dsId;
        this.dsUUID = dsUUID;
        this.sql = sql;
        this.statementType = statementType;
        this.sqlUUID = UUID.randomUUID().toString();

        Date startTime = new Date();
        this.startTimeMs = startTime.getTime();
        this.startTime = formatDate(startTime);
    }

    public String getSql() {
        return sql;
    }

    public String getDsId() {
        return dsId;
    }

    public String getDsUUID() {
        return dsUUID;
    }

    public String getSqlUUID() {
        return sqlUUID;
    }

    public String getStatementType() {
        return statementType;
    }

    public long getStartTimeMs() {
        return startTimeMs;
    }

    public void setStartTimeMs(long startTimeMs) {
        this.startTimeMs = startTimeMs;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public long getEndTimeMs() {
        return endTimeMs;
    }

    public void setEndTimeMs(long endTimeMs) {
        this.endTimeMs = endTimeMs;
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

    public boolean isSlowInd() {
        return slowInd;
    }

    public void setSlowInd(boolean slowInd) {
        this.slowInd = slowInd;
    }

    public boolean isSuccessInd() {
        return successInd;
    }

    public void setSuccessInd(boolean successInd) {
        this.successInd = successInd;
    }

    public boolean isAlertedInd() {
        return alertedInd;
    }

    public void setAlertedInd(boolean alertedInd) {
        this.alertedInd = alertedInd;
    }

    public Throwable getFailCause() {
        return failCause;
    }

    public void setFailCause(Throwable failCause) {
        this.failCause = failCause;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public String toString() {
        return sql;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StatementTrace that = (StatementTrace) o;
        return sqlUUID.equals(that.sqlUUID);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sqlUUID);
    }
}
