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

import javax.sql.DataSource;
import java.sql.*;

/*
 *  Util
 *
 *  @author Chris.Liao
 */
public class TestUtil {

    public static String testGetConnection(DataSource ds) throws Exception {
        Connection con = null;
        try {
            con = ds.getConnection();
            return "OK";
        } catch (SQLException e) {
            e.printStackTrace();
            return "Failed";
        } finally {
            if (con != null) {
                try {
                    con.close();
                } catch (SQLException e) {
                }
            }
        }
    }

    public static String testSQL(DataSource ds, String sql, String type) throws Exception {
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
                pst.execute();
            } else if ("CallableStatement".equalsIgnoreCase(type)) {
                cst = con.prepareCall(sql);
                cst.execute();
            }
            return "OK";
        } catch (SQLException e) {
            e.printStackTrace();
            return "Failed";
        } finally {
            if (st != null)
                try {
                    st.close();
                } catch (SQLException e) {
                }
            if (pst != null)
                try {
                    pst.close();
                } catch (SQLException e) {
                }
            if (cst != null)
                try {
                    cst.close();
                } catch (SQLException e) {
                }
            if (con != null)
                try {
                    con.close();
                } catch (SQLException e) {
                }
        }
    }
}