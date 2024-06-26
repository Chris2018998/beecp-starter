package org.stone.beecp.springboot.test.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.stone.beecp.springboot.SpringBootRestResponse;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static org.stone.tools.CommonUtil.isBlank;

public class ClientSideUtil {
    private static final Logger log = LoggerFactory.getLogger(ClientSideUtil.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static final boolean testGetConnection(String dsId, MockMvc mockMvc, String url) throws Exception {
        //1:Try to get connection
        Map<String, String> paramMap = new HashMap<String, String>(1);
        paramMap.put("dsId", dsId);
        Object restResult = getRest(mockMvc, url, paramMap, "get");
        if (!"OK".equals(restResult)) return false;

        //2:Get pool list to check ds pool whether exist in list
        String poolInfoListURL = "/beecp/getDataSourceList";
        Object response = getRest(mockMvc, poolInfoListURL, null, "post");
        List<Map<String, Object>> poolList = (List<Map<String, Object>>) response;

        for (Map map : poolList) {
            String pDsId = map.get("dsId").toString();
            String idleSize = map.get("idleSize").toString();
            log.info("{}-idleSize:{}", pDsId, idleSize);
            if (pDsId.equals(dsId)) return true;
        }
        return false;
    }


    public static boolean testExecuteSQL(String dsId, String sql, String sqlType, MockMvc mockMvc, int testType, String url) throws Exception {
        Map<String, String> paramMap = new HashMap<String, String>(3);
        paramMap.put("dsId", dsId);
        paramMap.put("sql", sql);
        paramMap.put("type", sqlType);
        paramMap.put("slowInd", (testType == 2) ? "true" : "false");
        getRest(mockMvc, url, paramMap, "get");

        String getSqlListUrl = "/beecp/getSqlTraceList";
        Object response = getRest(mockMvc, getSqlListUrl, null, "post");
        List<Map<String, Object>> sqlList = (List<Map<String, Object>>) response;

        if (testType == 0) {//normal
            for (Map map : sqlList) {
                String pDsId = map.get("dsId").toString();
                String exeSql = map.get("sql").toString();
                boolean execInd = map.get("endTime") != null;
                if (dsId.equals(pDsId) && execInd && sql.equals(exeSql)) {
                    String tookTimeMs = map.get("tookTimeMs").toString();
                    log.info("ds:{},Time:{}ms,SQL:{}", pDsId, tookTimeMs, exeSql);
                    return true;
                }
            }
            return false;
        } else if (testType == 1) {//error test
            for (Map map : sqlList) {
                String pDsId = map.get("dsId").toString();
                String exeSql = map.get("sql").toString();
                boolean execInd = map.get("endTime") != null;
                boolean execSuccessInd = (boolean) map.get("successInd");
                if (dsId.equals(pDsId) && sql.equals(exeSql) && execInd && !execSuccessInd) {
                    String tookTimeMs = map.get("tookTimeMs").toString();
                    log.info("ds:{},Time:{}ms,SQL:{}", pDsId, tookTimeMs, exeSql);
                    return true;
                }
            }
            return false;
        } else if (testType == 2) {//slow test
            for (Map map : sqlList) {
                System.out.println("test map:" + map);
                String pDsId = map.get("dsId").toString();
                String exeSql = map.get("sql").toString();
                boolean execInd = map.get("endTime") != null;
                boolean execSlowInd = false;
                if (execInd) execSlowInd = (Boolean) map.get("slowInd");

                if (dsId.equals(pDsId) && sql.equals(exeSql) && execInd && execSlowInd) {
                    String tookTimeMs = map.get("tookTimeMs").toString();
                    log.info("ds:{},Time:{}ms,SQL:{}", pDsId, tookTimeMs, exeSql);
                    return true;
                }
            }
            return false;
        }
        return false;
    }

    private static Object getRest(MockMvc mockMvc, String url, Map<String, String> paramMap, String callType) throws Exception {
        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.get(url);
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

        String responseMessage = mvcResult.getResponse().getContentAsString();
        System.out.println("Json Response:" + responseMessage);

        SpringBootRestResponse restResponse = string2Obj(responseMessage, SpringBootRestResponse.class);
        if (restResponse.getCode() == SpringBootRestResponse.CODE_FAILED)
            throw new Exception(restResponse.getMessage());
        return restResponse.getResult();
    }

    private static <T> T string2Obj(String str, Class<T> clazz) {
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
}
