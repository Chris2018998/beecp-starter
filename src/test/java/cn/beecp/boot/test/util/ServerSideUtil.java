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
package cn.beecp.boot.test.util;

import cn.beecp.pool.ConnectionPoolStatics;

import javax.sql.DataSource;
import java.sql.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

public class ServerSideUtil {
    public static String testGetConnection(DataSource ds) throws Exception {
        Connection con = null;
        try {
            con = ds.getConnection();
            return "OK";
        } catch (SQLException e) {
            e.printStackTrace();
            return "Failed";
        } finally {
            ConnectionPoolStatics.oclose(con);
        }
    }

    public static String testSQL(DataSource ds, String sql, String type, String slowInd) throws Exception {
        Statement st = null;
        PreparedStatement pst = null;
        CallableStatement cst = null;
        Connection con = null;

        try {
            con = ds.getConnection();
            if ("Statement".equalsIgnoreCase(type)) {
                st = con.createStatement();
                st.execute(sql);
            } else if ("PreparedStatement".equalsIgnoreCase(type)) {
                pst = con.prepareStatement(sql);
                if ("true".equals(slowInd)) LockSupport.parkNanos(TimeUnit.SECONDS.toNanos(2));
                pst.execute();
            } else if ("CallableStatement".equalsIgnoreCase(type)) {
                cst = con.prepareCall(sql);
                if ("true".equals(slowInd)) LockSupport.parkNanos(TimeUnit.SECONDS.toNanos(2));
                cst.execute();
            }
            return "OK";
        } catch (SQLException e) {
            // e.printStackTrace();
            return "Failed";
        } finally {
            ConnectionPoolStatics.oclose(st);
            ConnectionPoolStatics.oclose(pst);
            ConnectionPoolStatics.oclose(cst);
            ConnectionPoolStatics.oclose(con);
        }
    }
}
