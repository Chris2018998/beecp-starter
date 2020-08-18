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
package cn.beecp.boot.monitor;

import cn.beecp.BeeDataSource;
import cn.beecp.pool.FastConnectionPool;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@RestController
public class DataSourceMonitor{
   private  List<Map<String,Object>> poolInfoList=new LinkedList<Map<String,Object>>();

    @RequestMapping( "/dataSourceMonitor/getDataSourceInfo" )
    public List<Map<String,Object>> getDataSourceInfo(){
        poolInfoList.clear();
        BeeDataSource[] dsArray=DataSourceCollector.getInstance().getAllDataSource();
        for(BeeDataSource ds:dsArray){
            try {
                if (ds.isClosed()) {
                    DataSourceCollector.getInstance().removeDataSource(ds);
                }else{
                    retrievePoolInfo(ds,poolInfoList);
                }
            }catch(SQLException e){ }
        }

        return poolInfoList;
    }

    private static Field field=null;
    private static void retrievePoolInfo(final BeeDataSource ds,List<Map<String,Object>> poolInfoList) {
        try {
            if(field==null){
                field = ds.getClass().getDeclaredField("pool");
                field.setAccessible(true);
            }
             Object pool= field.get(ds);
            if(pool instanceof FastConnectionPool){
                poolInfoList.add(((FastConnectionPool)pool).getPoolInfo());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
