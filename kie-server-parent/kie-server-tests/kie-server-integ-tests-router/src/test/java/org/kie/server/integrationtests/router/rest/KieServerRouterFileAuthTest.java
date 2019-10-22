/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package org.kie.server.integrationtests.router.rest;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.stream.Stream;

import org.assertj.core.api.Assertions;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kie.server.controller.client.exception.KieServerControllerHTTPClientException;
import org.kie.server.integrationtests.router.client.KieServerRouterClient;
import org.kie.server.router.Configuration;
import org.kie.server.router.KieServerRouter;
import org.kie.server.router.KieServerRouterConstants;

public class KieServerRouterFileAuthTest {

    private static final String KIE_SERVER_ID = "server-id";
    private static final String KIE_SERVER_PASSWORD = "pwd";

    private static KieServerRouter router;
    private static File repository;

    private static String serverUrl;

    @BeforeClass
    public static void startStandaloneRouter() throws Exception{
        // setup repository for config of router
        repository = new File("target/standalone-router-repo");
        repository.mkdirs();
        System.setProperty(KieServerRouterConstants.ROUTER_REPOSITORY_DIR, repository.getAbsolutePath());
        System.setProperty(KieServerRouterConstants.KIE_ROUTER_MANAGEMENT_SECURED, "true");

        // Add Kie server id and password for authentication
        KieServerRouter.main(new String[]{"-addInstance", KIE_SERVER_ID, KIE_SERVER_PASSWORD});

        // setup and start router
        Integer port = allocatePort();
        router = new KieServerRouter();
        router.start("localhost", port);

        serverUrl = "http://localhost:" + port;
    }

    @AfterClass
    public static void stopStandaloneRouter(){
        // stop router and remove its config
        System.clearProperty(KieServerRouterConstants.ROUTER_REPOSITORY_DIR);
        System.clearProperty(KieServerRouterConstants.KIE_ROUTER_MANAGEMENT_SECURED);
        router.stop(true);
        router = null;

        Stream.of(repository.listFiles()).forEach(f-> f.delete());
        repository.delete();
    }

    @Test
    public void testValidAuthMgmtKieServerRouter() {
        Assert.assertNotNull(getRouterConfig(KIE_SERVER_ID, KIE_SERVER_PASSWORD));
    }

    @Test
    public void testInvalidAuthMgmtKieServerRouter() {
        Assertions.assertThatExceptionOfType(KieServerControllerHTTPClientException.class).isThrownBy(() -> getRouterConfig("invalidUser", KIE_SERVER_PASSWORD));
        Assertions.assertThatExceptionOfType(KieServerControllerHTTPClientException.class).isThrownBy(() -> getRouterConfig(KIE_SERVER_ID, "invalidPassword"));
        Assertions.assertThatExceptionOfType(KieServerControllerHTTPClientException.class).isThrownBy(() -> getRouterConfig("invalidUser", "invalidPassword"));
    }

    private Configuration getRouterConfig(String username, String password) {
        try (KieServerRouterClient routerClient = new KieServerRouterClient(serverUrl)) {
            routerClient.setCredentials(username, password);
            return routerClient.getRouterConfig();
        }
    }

    private static int allocatePort() {
        try {
            ServerSocket server = new ServerSocket(0);
            int port = server.getLocalPort();
            server.close();
            return port;
        } catch (IOException e) {
            // failed to dynamically allocate port, try to use hard coded one
            return 9783;
        }
    }
}
