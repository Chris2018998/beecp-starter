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

import cn.beecp.boot.datasource.sqltrace.SqlTracePool;
import cn.beecp.pool.PoolStaticCenter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * Controller
 *
 * @author Chris.Liao
 */

@Controller
public class DataSourceMonitor {
    private final static String chinese_page = "/beecp/chinese.html";
    private final static String english_page = "/beecp/english.html";
    private static final Logger log = LoggerFactory.getLogger(DataSourceMonitor.class);
    private SpringBootDataSourceCenter traceDataSourceMap = SpringBootDataSourceCenter.getInstance();

    @RequestMapping("/beecp")
    public String welcome1() {
        return "redirect:/beecp/";
    }

    @RequestMapping("/beecp/")
    public String welcome2() {
        return chinese_page;
    }

    @RequestMapping("/beecp/cn")
    public String openChinesePage1() {
        return chinese_page;
    }

    @RequestMapping("/beecp/chinese")
    public String openChinesePage2() {
        return chinese_page;
    }

    @RequestMapping("/beecp/en")
    public String openEnglishPage1() {
        return english_page;
    }

    @RequestMapping("/beecp/english")
    public String openEnglishPage2() {
        return english_page;
    }

    //*****************************************************************************//
    //                        Below are Rest methods                               //
    //*****************************************************************************//

    @ResponseBody
    @PostMapping("/beecp/login")
    public String login(@RequestBody Map<String, String> paramMap) {
        DataSourceMonitorAdmin admin = DataSourceMonitorAdmin.singleInstance;
        if (!PoolStaticCenter.isBlank(admin.getUserId())) {
            String userId = paramMap.get("userId");
            String password = paramMap.get("password");
            if (admin.getUserId().equals(userId) && PoolStaticCenter.equalsString(admin.getPassword(), password)) {
                ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
                HttpServletRequest request = servletRequestAttributes.getRequest();
                request.getSession().setAttribute(DataSourceMonitorAdmin.PASSED_ATTR_NAME, "Y");
                return "OK";//passed
            } else {
                return "FAIL";
            }
        } else {
            return "OK";//passed
        }
    }

    @ResponseBody
    @PostMapping("/beecp/getSqlTraceList")
    public Object getSqTraceList() {
        return SqlTracePool.getInstance().getTraceQueue();
    }

    @ResponseBody
    @PostMapping("/beecp/getDataSourceList")
    public List<Map<String, Object>> getDataSourceList() {
        return traceDataSourceMap.getPoolMonitorVoList();
    }

    @ResponseBody
    @PostMapping("/beecp/clearAllConnections")
    public void clearAllConnections(@RequestBody Map<String, String> parameterMap) {
        if (parameterMap != null) {
            String dsId = parameterMap.get("dsId");
            SpringBootDataSource ds = traceDataSourceMap.getDataSource(dsId);
            if (ds != null) ds.clear();
        }
    }
}
