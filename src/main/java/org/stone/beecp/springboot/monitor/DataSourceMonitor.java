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
package org.stone.beecp.springboot.monitor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.stone.beecp.springboot.SpringBootDataSourceManager;
import org.stone.beecp.springboot.SpringBootDataSourceUtil;
import org.stone.beecp.springboot.SpringBootRestResponse;

import java.util.Map;

import static org.stone.tools.CommonUtil.isBlank;

/**
 * Controller
 *
 * @author Chris Liao
 */
@Controller
public class DataSourceMonitor {
    private final static String CN_PAGE = "/beecp/chinese.html";
    private final static String EN_PAGE = "/beecp/english.html";
    private final String userId;
    private final String password;
    private final String loggedInTagName;
    private final SpringBootDataSourceManager dsManager = SpringBootDataSourceManager.getInstance();
    private HttpSession session;

    DataSourceMonitor(String userId, String password, String loggedInTagName) {
        this.userId = userId;
        this.password = password;
        this.loggedInTagName = loggedInTagName;
    }

    @ModelAttribute
    public void setReqAndRes(HttpServletRequest req, HttpServletResponse res) {
        this.session = req.getSession();
    }

    @RequestMapping("/beecp")
    public String welcome1() {
        return "redirect:/beecp/";
    }

    @RequestMapping("/beecp/")
    public String welcome2() {
        return CN_PAGE;
    }

    @RequestMapping("/beecp/cn")
    public String openChinesePage1() {
        return CN_PAGE;
    }

    @RequestMapping("/beecp/chinese")
    public String openChinesePage2() {
        return CN_PAGE;
    }

    @RequestMapping("/beecp/en")
    public String openEnglishPage1() {
        return EN_PAGE;
    }

    @RequestMapping("/beecp/english")
    public String openEnglishPage2() {
        return EN_PAGE;
    }

    //****************************************************************************************************************//
    //                                         Below are Rest methods                                                 //
    //****************************************************************************************************************//
    @ResponseBody
    @PostMapping("/beecp/login")
    public SpringBootRestResponse login(@RequestBody Map<String, String> paramMap) {
        if ("Y".equals(session.getAttribute(loggedInTagName)))//has logined
            return new SpringBootRestResponse(SpringBootRestResponse.CODE_SUCCESS, null, "Login Success");
        if (isBlank(userId))
            return new SpringBootRestResponse(SpringBootRestResponse.CODE_SUCCESS, null, "Login Success");

        try {
            String userId = paramMap.get("userId");
            String password = paramMap.get("password");
            if (this.userId.equals(userId) && SpringBootDataSourceUtil.stringEquals(this.password, password)) {//checked pass
                session.setAttribute(loggedInTagName, "Y");
                return new SpringBootRestResponse(SpringBootRestResponse.CODE_SUCCESS, null, "Login Success");
            } else
                return new SpringBootRestResponse(SpringBootRestResponse.CODE_FAILED, null, "Login Failed");
        } catch (Throwable e) {
            return new SpringBootRestResponse(SpringBootRestResponse.CODE_FAILED, e, "Login Failed");
        }
    }

    @ResponseBody
    @PostMapping("/beecp/getDataSourceList")
    public SpringBootRestResponse getDataSourceList() {
        try {
            return new SpringBootRestResponse(SpringBootRestResponse.CODE_SUCCESS, dsManager.getPoolMonitorVoList(), "OK");
        } catch (Throwable e) {
            return new SpringBootRestResponse(SpringBootRestResponse.CODE_FAILED, e, "Failed to 'getDataSourceList'");
        }
    }

    @ResponseBody
    @PostMapping("/beecp/getSqlTraceList")
    public SpringBootRestResponse getSqTraceList() {
        try {
            return new SpringBootRestResponse(SpringBootRestResponse.CODE_SUCCESS, dsManager.getSqlExecutionList(), "OK");
        } catch (Throwable e) {
            return new SpringBootRestResponse(SpringBootRestResponse.CODE_FAILED, e, "Failed to 'getSqlTraceList'");
        }
    }

    @ResponseBody
    @PostMapping("/beecp/clearPool")
    public SpringBootRestResponse clearPool(@RequestBody Map<String, String> parameterMap) {
        try {
            if (parameterMap != null && parameterMap.containsKey("dsId")) {
                String dsId = parameterMap.get("dsId");
                dsManager.clearPool(dsId);
            }
            return new SpringBootRestResponse(SpringBootRestResponse.CODE_SUCCESS, null, "OK");
        } catch (Throwable e) {
            return new SpringBootRestResponse(SpringBootRestResponse.CODE_FAILED, e, "Failed to clear datasource pool'");
        }
    }


    @ResponseBody
    @PostMapping("/beecp/interruptPool")
    public SpringBootRestResponse interruptPool(@RequestBody Map<String, String> parameterMap) {
        try {
            if (parameterMap != null && parameterMap.containsKey("dsId")) {
                String dsId = parameterMap.get("dsId");
                dsManager.interruptPool(dsId);
            }
            return new SpringBootRestResponse(SpringBootRestResponse.CODE_SUCCESS, null, "OK");
        } catch (Throwable e) {
            return new SpringBootRestResponse(SpringBootRestResponse.CODE_FAILED, e, "Failed to interrupt datasource pool'");
        }
    }
}
