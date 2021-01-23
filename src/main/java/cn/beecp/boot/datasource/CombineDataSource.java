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

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;

/**
 * Combine DataSource for Multi-DataSource
 *
 * @author Chris.Liao
 */
public class CombineDataSource implements DataSource {
    private String defaultId;
    private boolean isClosed = false;
    private TraceDataSourceMap dataSourceMap = TraceDataSourceMap.getInstance();

    public CombineDataSource(String defaultId) {
        this.defaultId = defaultId;
    }

    public boolean isClosed() {
        return isClosed;
    }

    public void close() {
        this.isClosed = true;
    }

    public Connection getConnection() throws SQLException {
        return getTraceDataSource().getConnection();
    }

    public Connection getConnection(String username, String password) throws SQLException {
        return getTraceDataSource().getConnection(username, password);
    }

    private TraceDataSource getTraceDataSource() throws SQLException {
        if (isClosed) throw new SQLException("DataSource has closed");
        String dsId = dataSourceMap.getCurDsId();
        dsId = !DataSourceUtil.isBlank(dsId) ? dsId : defaultId;
        TraceDataSource ds = dataSourceMap.getDataSource(dsId);
        if (ds == null) throw new SQLException("Datasource(" + dsId + ") not exists");
        return ds;
    }

    public java.io.PrintWriter getLogWriter() throws SQLException {
        throw new SQLFeatureNotSupportedException("Not supported");
    }

    public void setLogWriter(java.io.PrintWriter out) throws SQLException {
        throw new SQLFeatureNotSupportedException("Not supported");
    }

    public int getLoginTimeout() throws SQLException {
        throw new SQLFeatureNotSupportedException("Not supported");
    }

    public void setLoginTimeout(int seconds) throws SQLException {
        throw new SQLFeatureNotSupportedException("Not supported");
    }

    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        throw new SQLFeatureNotSupportedException("Not supported");
    }

    public <T> T unwrap(Class<T> iface) throws SQLException {
        throw new SQLFeatureNotSupportedException("Not supported");
    }

    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        throw new SQLFeatureNotSupportedException("Not supported");
    }
}
