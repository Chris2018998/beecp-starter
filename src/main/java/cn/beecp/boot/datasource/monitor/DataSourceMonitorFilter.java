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

import cn.beecp.pool.PoolStaticCenter;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * request filter
 *
 * @author Chris.Liao
 */
@WebFilter(filterName = "beecpMonitorFilter", urlPatterns = "/beecp/*")
public class DataSourceMonitorFilter implements Filter {
    private final String[] excludeUrls = {"/login", "/json", ".js", ".css", ".ico", ".jpg", ".png"};
    private String monitorUserId;
    private String monitorValidPassedTagName;

    DataSourceMonitorFilter(String monitorUser, String monitorValidPassedTagName) {
        this.monitorUserId = monitorUser;
        this.monitorValidPassedTagName = monitorValidPassedTagName;
    }

    public void destroy() {
        //do nothing
    }

    public void init(FilterConfig var1) {
        //do nothing
    }

    public void doFilter(ServletRequest var1, ServletResponse var2, FilterChain var3) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) var1;
        HttpSession session = req.getSession();
        Object attributeVal = session.getAttribute(monitorValidPassedTagName);
        String servletPath = req.getServletPath();
        if ("Y".equals(attributeVal) || PoolStaticCenter.isBlank(monitorUserId) || isExcludeUrl(servletPath)) {
            var3.doFilter(var1, var2);
        } else {
            req.getRequestDispatcher("/beecp/login.html").forward(var1, var2);
        }
    }

    private boolean isExcludeUrl(String spath) {
        for (String str : excludeUrls) {
            if (spath.indexOf(str) != -1) {
                return true;
            }
        }
        return false;
    }
}
