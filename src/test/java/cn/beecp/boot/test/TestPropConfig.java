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
package cn.beecp.boot.test;

import cn.beecp.boot.test.controller.MultiDsController;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = MultiDsController.class)
@ActiveProfiles("prop_conf")
@WebAppConfiguration
public class TestPropConfig {
    //logger
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    @Autowired
    private WebApplicationContext webApplicationContext;
    private MockMvc mockMvc;

    @Before
    public void setUp() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    public void testDs1GetConnection() throws Exception {
        testGetConnection("GetConnectionFrom", "ds1");
    }

    @Test
    public void testDs2GetConnection() throws Exception {
        testGetConnection("GetConnectionFrom", "ds2");
    }

    protected void testGetConnection(String testName, String dsId) throws Exception {
        RequestBuilder request = MockMvcRequestBuilders.get("/testGetConnection")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("dsId", dsId);

        MvcResult mvcResult = mockMvc.perform(request)
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        mvcResult.getResponse().setCharacterEncoding("utf8");
        String reponse = mvcResult.getResponse().getContentAsString();
        if (!"OK".equals(reponse)) {
            log.info("testGetConnection");
            throw new Exception("Failed to get Connection from (" + dsId + ")");
        } else {
            log.info("testGetConnection");
        }
    }
}
