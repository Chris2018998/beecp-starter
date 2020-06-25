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
package cn.beecp.boot;

import cn.beecp.BeeDataSource;
import cn.beecp.BeeDataSourceConfig;
import cn.beecp.util.BeecpUtil;
import org.springframework.core.env.Environment;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/*
 *  Bee Data Source Attribute Set Factory
 *
 *  spring.datasource.d1.poolName=BeeCP1
 *  spring.datasource.d1.username=root
 *  spring.datasource.d1.password=root
 *  spring.datasource.d1.jdbcUrl=jdbc:mysql://localhost:3306/test
 *  spring.datasource.d1.driverClassName=com.mysql.cj.jdbc.Driver
 *
 *  @author Chris.Liao
 */

public class BeeDataSourceAttributeSetFactory implements DataSourceAttributeSetFactory{

    /**
     * attribute name list need set
     */
    private static List<Field> attributeList=new LinkedList<Field>();
    static {
        Class configClass=BeeDataSourceConfig.class;
        Field[] fields=configClass.getDeclaredFields();
        for(Field field:fields){
            String fieldName=field.getName();
            if("checked".equals(fieldName)
                    || "connectionFactory".equals(fieldName)
                    || "connectProperties".equals(fieldName))
                continue;
            attributeList.add(field);
        }
    }

    /**
     *  get Properties values from environment and set to dataSource
     *
     * @param ds
     * @param configPrefix
     * @param environment
     */
    public void set(DataSource ds,String configPrefix,Environment environment)throws Exception{
        BeeDataSource bds=(BeeDataSource)ds;
        Iterator<Field> itor=attributeList.iterator();
        while(itor.hasNext()){
            Field field = itor.next();
            String configVal=environment.getProperty(configPrefix+"."+field.getName());
            if(!BeecpUtil.isNullText(configVal)) {
                configVal=configVal.trim();
                Class fieldType=field.getType();
                if(Modifier.isPrivate(field.getModifiers())||Modifier.isProtected(field.getModifiers()))
                 field.setAccessible(true);

                if(fieldType.equals(String.class)){
                    field.set(bds,configVal);
                }else if(fieldType.equals(Boolean.class) || fieldType.equals(Boolean.TYPE)){
                    field.set(bds,Boolean.valueOf(configVal));
                }else if(fieldType.equals(Integer.class) || fieldType.equals(Integer.TYPE)){
                    field.set(bds,Integer.valueOf(configVal));
                }else if(fieldType.equals(Long.class) || fieldType.equals(Long.TYPE)){
                    field.set(bds,Long.valueOf(configVal));
                }
            }
        }
    }
}
