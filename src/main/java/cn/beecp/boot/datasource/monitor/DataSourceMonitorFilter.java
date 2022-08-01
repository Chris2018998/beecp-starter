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
package cn.beecp.boot.datasource.monitor;

import cn.beecp.boot.datasource.SpringBootDataSourceUtil;
import cn.beecp.boot.datasource.SpringBootRestResponse;
import cn.beecp.pool.PoolStaticCenter;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import static cn.beecp.boot.datasource.SpringBootRestResponse.CODE_SECURITY;

/**
 * request filter
 *
 * @author Chris.Liao
 */
public class DataSourceMonitorFilter implements Filter {
    private final String userId;
    private final String validPassedTagName;

    private final String[] excludeUrls = {"/login", "/json", ".js", ".css", ".ico", ".jpg", ".png"};
    private final String[] restUrls = {"/beecp/login", "/beecp/getSqlTraceList", "/beecp/getDataSourceList", "/beecp/clearDataSource"};

    DataSourceMonitorFilter(String userId, String validPassedTagName) {
        this.userId = userId;
        this.validPassedTagName = validPassedTagName;
    }

    public void destroy() {
        //do nothing
    }

    public void init(FilterConfig var1) {
        //do nothing
    }

    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        if (PoolStaticCenter.isBlank(userId)) chain.doFilter(req, res);
        HttpServletRequest httpReq = (HttpServletRequest) req;
        String requestPath = httpReq.getServletPath();
        if ("Y".equals(httpReq.getSession().getAttribute(validPassedTagName)) || isExcludeUrl(requestPath)) {
            chain.doFilter(req, res);
        } else if (isRestUrl(requestPath)) {
            res.setContentType("application/json");
            OutputStream ps = res.getOutputStream();
            SpringBootRestResponse restResponse = new SpringBootRestResponse(CODE_SECURITY, null, "unauthorized");
            ps.write(SpringBootDataSourceUtil.object2String(restResponse).getBytes(StandardCharsets.UTF_8));
        } else {
            req.getRequestDispatcher("/beecp/login.html").forward(req, res);
            return;
        }
    }

    private boolean isExcludeUrl(String requestPath) {
        for (String str : excludeUrls)
            if (requestPath.contains(str)) return true;
        return false;
    }

    private boolean isRestUrl(String requestPath) {
        for (String str : restUrls)
            if (requestPath.endsWith(str)) return true;
        return false;
    }
}
