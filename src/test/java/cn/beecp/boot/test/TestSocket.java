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

import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
public class TestSocket {
    public static void main(String[]args){
        testSocket("localhost",3306);
    }
    private static void testSocket(String host,int port){
        Socket socket = new Socket();
        long time1=System.currentTimeMillis();
        try {
            socket.connect(new InetSocketAddress(host, port));
            socket.close();
        }catch(Exception e) {
        }finally {
            long time=System.currentTimeMillis()-time1;
            if(socket.isConnected()){
                System.out.println("Connect success,Time："+time+"ms");
            }else{
                System.out.println("Connect failed,Time："+time+"ms");
            }
        }
    }
}
