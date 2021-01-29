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

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static cn.beecp.boot.datasource.DataSourceUtil.isBlank;

/*
 *  Util
 *
 *  @author Chris.Liao
 */
public class TestUtil {

    private static final Logger log = LoggerFactory.getLogger(TestUtil.class);
    /**
     * string转object
     */
    private static ObjectMapper objectMapper = new ObjectMapper();

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
           // e.printStackTrace();
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

    //
    public static final String getRest(MockMvc mockMvc, String url, Map<String, String> paramMap, String callType) throws Exception {
        MockHttpServletRequestBuilder requestBuilder = null;
        if ("get".equals(callType))
            requestBuilder = MockMvcRequestBuilders.get(url);
        else
            requestBuilder = MockMvcRequestBuilders.post(url);

        requestBuilder.contentType(MediaType.APPLICATION_FORM_URLENCODED);

        if (paramMap != null) {
            Iterator<Map.Entry<String, String>> itor = paramMap.entrySet().iterator();
            while (itor.hasNext()) {
                Map.Entry<String, String> entry = itor.next();
                requestBuilder.param(entry.getKey(), entry.getValue());
            }
        }

        MvcResult mvcResult = mockMvc.perform(requestBuilder)
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        mvcResult.getResponse().setCharacterEncoding("utf8");
        return mvcResult.getResponse().getContentAsString();
    }

    public static final <T> T string2Obj(String str, Class<T> clazz) {
        try {
            if (isBlank(str) || clazz == null) {
                return null;
            } else if (clazz.equals(String.class)) {
                return (T) str;
            } else {
                return objectMapper.readValue(str, clazz);
            }
        } catch (IOException e) {
            System.out.println("Parse String to Object error");
            e.printStackTrace();
            return null;
        }
    }

    public static final <T> T string2Obj(String str, Class<?> collectionClass, Class<?>... elementClasses) {
        try {
            JavaType javaType = objectMapper.getTypeFactory().constructParametricType(collectionClass, elementClasses);
            return objectMapper.readValue(str, javaType);
        } catch (IOException e) {
            System.out.println("Parse String to Object error");
            e.printStackTrace();
            return null;
        }
    }

    public static final void testGetConnection(String dsId, MockMvc mockMvc) throws Exception {
        String testGetConUrl = "/testGetConnection";
        Map<String, String> paramMap = new HashMap<String, String>(1);
        paramMap.put("dsId", dsId);
        String getConResult = getRest(mockMvc, testGetConUrl, paramMap, "get");
        log.info("test GetConn result:" + getConResult);
        if (!"OK".equals(getConResult)) throw new Exception("Failed to get connection from " + dsId);

        String getPoolListUrl = "/dsMonitor/getPoolList";
        String response = getRest(mockMvc, getPoolListUrl, null, "post");
        log.info("getPoolList result:" + response);
        List<Map<String, Object>> poolList = string2Obj(response, List.class, Map.class);
        for (Map map : poolList)
            if (dsId.equals(map.get("dsId"))) return;

        throw new Exception(dsId + " not in server pool list");
    }

    public static final void testExecuteSQL(String dsId, String sql, String type, MockMvc mockMvc) throws Exception {
        String testGetConUrl = "/testSQL";
        Map<String, String> paramMap = new HashMap<String, String>(1);
        paramMap.put("dsId", dsId);
        paramMap.put("sql", sql);
        paramMap.put("type", type);
        String getConResult = getRest(mockMvc, testGetConUrl, paramMap, "get");
        log.info("test SQL result:" + getConResult);
       // if (!"OK".equals(getConResult)) throw new Exception("Failed to get connection from " + dsId);

        String getSqlListUrl = "/dsMonitor/getSqlTraceList";
        String response = getRest(mockMvc, getSqlListUrl, null, "post");
        log.info("getSqlTraceList result:" + response);
        List<Map<String, Object>> sqlList = string2Obj(response, List.class, Map.class);
        for (Map map : sqlList) {
            if (dsId.equals(map.get("dsId")) && sql.equals(map.get("sql"))) return;
        }
        throw new Exception("target sql not in trace list");
    }
}