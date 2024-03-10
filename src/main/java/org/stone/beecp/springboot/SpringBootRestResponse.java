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

/**
 * rest response result object
 *
 * @author Chris Liao
 */
public class SpringBootRestResponse {
    public static final int CODE_SUCCESS = 1;
    public static final int CODE_FAILED = 2;
    public static final int CODE_SECURITY = 3;

    private int code;
    private Object result;
    private String message;

    public SpringBootRestResponse() {
    }

    public SpringBootRestResponse(int code) {
        this(code, null, null);
    }

    public SpringBootRestResponse(int code, Object result) {
        this(code, result, null);
    }

    public SpringBootRestResponse(int code, Object result, String message) {
        this.code = code;
        this.result = result;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
