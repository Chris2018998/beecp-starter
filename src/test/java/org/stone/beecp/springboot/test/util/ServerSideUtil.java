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
package org.stone.beecp.springboot.test.util;

import org.stone.beecp.pool.ConnectionPoolStatics;

import javax.sql.DataSource;
import java.sql.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

public class ServerSideUtil {
    public static String testGetConnection(DataSource ds) throws Exception {
        try (Connection con = ds.getConnection()) {
            return "OK";
        } catch (SQLException e) {
            e.printStackTrace();
            return "Failed";
        }
    }

    public static String testSQL(String dsId,DataSource ds, String sql, String type, String slowInd) throws Exception {
        Statement st = null;
        PreparedStatement pst = null;
        CallableStatement cst = null;
        System.out.println("("+dsId+")........Execute sql............:"+sql);
        try (Connection con = ds.getConnection()) {
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
            if (st != null) ConnectionPoolStatics.oclose(st);
            if (pst != null) ConnectionPoolStatics.oclose(pst);
            if (cst != null) ConnectionPoolStatics.oclose(cst);
        }
    }
}
