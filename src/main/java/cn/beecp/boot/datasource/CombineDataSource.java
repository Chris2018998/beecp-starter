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
import java.io.PrintWriter;
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
    private boolean isClosed = false;

    public boolean isClosed() {
        return isClosed;
    }

    public void close() {
        this.isClosed = true;
    }

    public Connection getConnection() throws SQLException {
        return getCurrentDataSource().getConnection();
    }

    public Connection getConnection(String username, String password) throws SQLException {
        return getCurrentDataSource().getConnection(username, password);
    }

    private SpringBootDataSource getCurrentDataSource() throws SQLException {
        if (isClosed) throw new SQLException("DataSource has closed");
        SpringBootDataSource ds = CombineDataSourceAspect.getCurrentDs();
        if (ds == null) throw new SQLException("DataSource not exists");
        return ds;
    }

    public PrintWriter getLogWriter() throws SQLException {
        return getCurrentDataSource().getLogWriter();
    }

    public void setLogWriter(PrintWriter out) throws SQLException {
        getCurrentDataSource().setLogWriter(out);
    }

    public int getLoginTimeout() throws SQLException {
        return getCurrentDataSource().getLoginTimeout();
    }

    public void setLoginTimeout(int seconds) throws SQLException {
        getCurrentDataSource().setLoginTimeout(seconds);
    }

    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        try {
            return getCurrentDataSource().getParentLogger();
        } catch (SQLException e) {
            throw new SQLFeatureNotSupportedException(e);
        }
    }

    public <T> T unwrap(Class<T> face) throws SQLException {
        return getCurrentDataSource().unwrap(face);
    }

    public boolean isWrapperFor(Class<?> face) throws SQLException {
        return getCurrentDataSource().isWrapperFor(face);
    }
}
