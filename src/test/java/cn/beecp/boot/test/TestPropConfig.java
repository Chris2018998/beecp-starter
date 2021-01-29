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
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static cn.beecp.boot.test.util.TestUtil.testExecuteSQL;
import static cn.beecp.boot.test.util.TestUtil.testGetConnection;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = MultiDsController.class)
@ActiveProfiles("prop_conf")
@WebAppConfiguration
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TestPropConfig {
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private MockMvc mockMvc;
    @Autowired
    private WebApplicationContext webApplicationContext;

    @Before
    public void setUp() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    public void test1() throws Exception {
        testGetConnection("ds1", mockMvc);
    }

    @Test
    public void test2() throws Exception {
        testGetConnection("ds2", mockMvc);
    }

    @Test
    public void test3() throws Exception {
        testExecuteSQL("ds1", "select * from TEST_USER", "Statement", mockMvc);
    }

    @Test
    public void test4() throws Exception {
        testExecuteSQL("ds1", "select * from TEST_USER2", "PreparedStatement", mockMvc);
    }

    @Test
    public void test5() throws Exception {
        testExecuteSQL("ds1", "{call BEECP_HELLO()}", "CallableStatement", mockMvc);
    }

    @Test
    //ERROR SQL
    public void test6() throws Exception {
        testExecuteSQL("ds1", "select * from TEST_USER3", "PreparedStatement", mockMvc);
    }

}
