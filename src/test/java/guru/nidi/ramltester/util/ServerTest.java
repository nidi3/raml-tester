/*
 * Copyright (C) 2014 Stefan Niederhauser (nidin@gmx.ch)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package guru.nidi.ramltester.util;

import org.apache.catalina.*;
import org.apache.catalina.startup.Tomcat;
import org.junit.AfterClass;
import org.junit.Before;
import org.slf4j.bridge.SLF4JBridgeHandler;

import javax.servlet.ServletException;

/**
 *
 */
public class ServerTest {
    private static Tomcat tomcat;
    private static Server server;
    private static Context ctx;
    private static boolean inited;

    @Before
    public void initImpl() throws LifecycleException, ServletException {
        if (!inited) {
            inited = true;
            SLF4JBridgeHandler.removeHandlersForRootLogger();
            SLF4JBridgeHandler.install();

            tomcat = new Tomcat();
            tomcat.setPort(8080);
            tomcat.setBaseDir(".");
            ctx = tomcat.addWebapp("/", "src/test");
            ((Host) ctx.getParent()).setAppBase("");

            init(ctx);

            tomcat.start();
            server = tomcat.getServer();
            server.start();
        }
    }

    protected void init(Context ctx) {
    }

    @AfterClass
    public static void stopTomcat() throws LifecycleException {
        if (tomcat.getServer() != null && tomcat.getServer().getState() != LifecycleState.DESTROYED) {
            if (tomcat.getServer().getState() != LifecycleState.STOPPED) {
                tomcat.stop();
            }
            tomcat.destroy();
        }
    }
}
