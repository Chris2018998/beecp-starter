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
package org.stone.beecp.springboot.monitor.redis;

import org.stone.beecp.springboot.SpringBootDataSourceUtil;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.LinkedList;
import java.util.List;

import static org.stone.tools.CommonUtil.isNotBlank;

/**
 * Redis read task
 *
 * @author Chris Liao
 */
public class RedisReadTask extends RedisBaseTask {

    RedisReadTask(JedisPool pool) {
        super(pool);
    }

    public void run() {
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            List<RedisPackage> redisPackageList = new LinkedList<>();
            for (String redisKey : jedis.keys(RedisKeyPrefix)) {
                String monitorJson = jedis.get(redisKey);
                if (isNotBlank(monitorJson)) {
                    redisPackageList.add(SpringBootDataSourceUtil.string2Object(monitorJson, RedisPackage.class));
                }
            }

            if (!redisPackageList.isEmpty()) {
                //@todo
            }
        } catch (Throwable e) {
            Log.error("Failed read monitor-package from redis,cause:", e);
        } finally {
            if (jedis != null) jedis.close();
        }
    }
}
