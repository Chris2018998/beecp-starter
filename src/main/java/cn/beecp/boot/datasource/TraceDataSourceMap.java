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

import java.sql.Connection;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Collect Bee dataSource
 *
 * @author Chris.Liao
 */
class TraceDataSourceMap {
    private final static TraceDataSourceMap instance = new TraceDataSourceMap();
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private Map<String, TraceDataSource> dsMap = new LinkedHashMap<>();
    private ThreadLocal<String> dsIdeLocal = new ThreadLocal();

    public final static TraceDataSourceMap getInstance() {
        return instance;
    }

    public String getCurDsId() {
        return dsIdeLocal.get();
    }

    public void setCurDsId(String dsId) {
        dsIdeLocal.set(dsId);
    }

    public TraceDataSource getDataSource(String dsIde) {
        return dsMap.get(dsIde);
    }

    public void removeDataSource(String dsIde) {
        dsMap.remove(dsIde);
    }

    public void addDataSource(TraceDataSource ds) {
        dsMap.put(ds.getId(), ds);
        if (ds.isBeeType()) {
            Connection con = null;
            try {
                con = ds.getConnection();
            } catch (SQLException e) {
                log.warn("Failed to get connection from datasource({})", ds.getId());
            } finally {
                if (con != null) {
                    try {
                        con.close();
                    } catch (Throwable e) {
                    }
                }
            }
        }
    }

    public TraceDataSource[] getAllDataSource() {
        return dsMap.values().toArray(new TraceDataSource[dsMap.size()]);
    }
}
