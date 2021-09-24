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

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * page Controller
 *
 * @author Chris.Liao
 */

@Controller
public class DataSourceMonitorPage {
    @RequestMapping("/beecp")
    public String welcome1() {return "redirect:/beecp/"; }
    @RequestMapping("/beecp/")
    public String welcome2() { return "/beecp/chinese.html";}
    @RequestMapping("/beecp/cn")
    public String openChinesePage1() { return "/beecp/chinese.html";}
    @RequestMapping("/beecp/chinese")
    public String openChinesePage2() { return "/beecp/chinese.html";}
    @RequestMapping("/beecp/index")
    public String openChinesePage3() {return "/beecp/chinese.html";}

    @RequestMapping("/beecp/en")
    public String openEnglishPage1() {return "/beecp/english.html";}
    @RequestMapping("/beecp/english")
    public String openEnglishPage2() {return "/beecp/english.html";}

}
