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
package cn.beecp.boot.datasource.monitor.redis;

import cn.beecp.boot.datasource.statement.StatementTrace;
import cn.beecp.pool.ConnectionPoolMonitorVo;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * Redis Package object
 *
 * @author Chris.Liao
 */

public class RedisPackage {
    private String packageUUID;
    private List<ConnectionPoolMonitorVo> dsList;
    private Collection<StatementTrace> sqlList;

    public RedisPackage() {
        this.packageUUID = "BeeMonitor_Package_" + UUID.randomUUID().toString();
    }

    public String getPackageUUID() {
        return packageUUID;
    }

    public void setPackageUUID(String packageUUID) {
        this.packageUUID = packageUUID;
    }

    public List<ConnectionPoolMonitorVo> getDsList() {
        return dsList;
    }

    public void setDsList(List<ConnectionPoolMonitorVo> dsList) {
        this.dsList = dsList;
    }

    public Collection<StatementTrace> getSqlList() {
        return sqlList;
    }

    public void setSqlList(Collection<StatementTrace> sqlList) {
        this.sqlList = sqlList;
    }
}