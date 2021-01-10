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
package cn.beecp.boot.monitor;

import cn.beecp.BeeDataSource;
import cn.beecp.boot.monitor.sqltrace.ProxyFactory;
import cn.beecp.pool.ConnectionPoolMonitorVo;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;

/*
 *  Bee Data Source Wrapper
 *
 *  @author Chris.Liao
 */
public class BeeDataSourceWrapper implements DataSource {
    private String dsName;
    private boolean traceSQL;
    private BeeDataSource delegate;

    public BeeDataSourceWrapper(BeeDataSource delegate, String dsName, boolean traceSQL) {
        this.delegate = delegate;
        this.dsName = dsName;
        this.traceSQL = traceSQL;
    }

    public String getDsName() {
        return dsName;
    }

    public boolean isTraceSQL() {
        return traceSQL;
    }

    public ConnectionPoolMonitorVo getPoolMonitorVo() throws Exception {
        return delegate.getPoolMonitorVo();
    }

    public void resetPool() throws SQLException {
        delegate.resetPool(false);
    }

    public Connection getConnection() throws SQLException {
        Connection con = delegate.getConnection();
        return traceSQL ? ProxyFactory.createConnection(con, dsName) : con;
    }

    public Connection getConnection(String username, String password) throws SQLException {
        Connection con = delegate.getConnection(username, password);
        return traceSQL ? ProxyFactory.createConnection(con, dsName) : con;
    }

    public java.io.PrintWriter getLogWriter() throws SQLException {
        return delegate.getLogWriter();
    }

    public void setLogWriter(java.io.PrintWriter out) throws SQLException {
        delegate.setLogWriter(out);
    }

    public void close() {
        delegate.close();
    }

    public int getLoginTimeout() throws SQLException {
        return delegate.getLoginTimeout();
    }

    public void setLoginTimeout(int seconds) throws SQLException {
        delegate.setLoginTimeout(seconds);
    }

    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return delegate.getParentLogger();
    }

    public <T> T unwrap(java.lang.Class<T> iface) throws java.sql.SQLException {
        return delegate.unwrap(iface);
    }

    public boolean isWrapperFor(java.lang.Class<?> iface) throws java.sql.SQLException {
        return delegate.isWrapperFor(iface);
    }
}
