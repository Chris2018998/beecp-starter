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

import cn.beecp.boot.DsId;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.Order;

import static cn.beecp.pool.PoolStaticCenter.isBlank;

/*
 *  combine DataSource Aspect
 *
 *  @author Chris.Liao
 */

@Aspect
@Order(1)
public class CombineDataSourceAspect {
    //*************************static methods for combine inner dataSource *******************************************//
    private static String combinePrimaryDsId;
    private static ThreadLocal<SpringBootDataSource> combineDataSourceLocal = new ThreadLocal<>();

    static void setCombinePrimaryDsId(String primaryDsId) {
        combinePrimaryDsId = primaryDsId;
    }

    static SpringBootDataSource getCurrentDs() {
        return combineDataSourceLocal.get();
    }
    //**************************************************************************************************************//

    //*********************************aspect methods begin ********************************************************//
    @Pointcut("@annotation(cn.beecp.boot.DsId)")
    public void pointcut() {
        //do nothing
    }

    @Around("pointcut()")
    public Object setDataSourceId(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        DsId annotation = methodSignature.getMethod().getAnnotation(DsId.class);
        String dsId = annotation.value();

        try {
            if (isBlank(dsId)) dsId = combinePrimaryDsId;
            combineDataSourceLocal.set(SpringBootDataSourceManager.getInstance().getSpringBootDataSource(dsId));
            return joinPoint.proceed();
        } finally {
            if (!isBlank(dsId)) combineDataSourceLocal.remove();
        }
    }
    //***************************************************************************************************************//
}
