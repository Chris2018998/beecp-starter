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
package org.stone.beecp.springboot;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.Order;
import org.stone.beecp.springboot.annotation.DsId;

import static org.stone.tools.CommonUtil.isBlank;


/*
 *  combine DataSource Aspect
 *
 *  @author Chris Liao
 */

@Aspect
@Order(1)
public class CombineDataSourceAspect {
    private final String primaryDsId;
    private final ThreadLocal<SpringBootDataSource> dsThreadLocal;

    CombineDataSourceAspect(String primaryDsId, ThreadLocal<SpringBootDataSource> dsThreadLocal) {
        this.primaryDsId = primaryDsId;
        this.dsThreadLocal = dsThreadLocal;
    }

    //*********************************aspect methods begin **********************************************************//
    @Pointcut("@annotation(org.stone.beecp.springboot.annotation.DsId)")
    public void pointcut() {
        //do nothing
    }

    @Around("pointcut()")
    public Object setDataSourceId(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        DsId annotation = methodSignature.getMethod().getAnnotation(DsId.class);
        String dsId = annotation.value();

        try {
            if (isBlank(dsId)) dsId = primaryDsId;
            dsThreadLocal.set(SpringBootDataSourceManager.getInstance().getSpringBootDataSource(dsId));
            return joinPoint.proceed();
        } finally {
            if (!isBlank(dsId)) dsThreadLocal.remove();
        }
    }
    //***************************************************************************************************************//
}
