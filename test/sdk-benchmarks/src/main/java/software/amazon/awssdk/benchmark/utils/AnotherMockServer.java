/*
 * Copyright 2010-2018 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *  http://aws.amazon.com/apache2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package software.amazon.awssdk.benchmark.utils;

import static software.amazon.awssdk.benchmark.utils.BenchmarkUtils.ERROR_JSON_BODY;
import static software.amazon.awssdk.benchmark.utils.BenchmarkUtils.JSON_BODY;
import static software.amazon.awssdk.benchmark.utils.BenchmarkUtils.PORT_NUMBER;
import static software.amazon.awssdk.benchmark.utils.BenchmarkUtils.XML_BODY;

import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletHandler;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class AnotherMockServer {
    private static final String SUCCESS_AFTER_ATTEMPT_HEADER = "succeed-after-attempt";
    private static final String SDK_RETRY_HEADER = "amz-sdk-retry";
    private final Server server;
    private final String successContent;
    private final String errorContent;
    private String protocol;

    public AnotherMockServer(String successContent, String errorContent) {
        this(0, successContent, errorContent);
    }

    public static void main(String... args) throws Exception {
        AnotherMockServer mockServer = new AnotherMockServer(PORT_NUMBER, JSON_BODY, ERROR_JSON_BODY);
        mockServer.start();
    }

    public AnotherMockServer(int port, String successContent, String errorContent) {
        server = new Server(port);
        this.successContent = successContent;
        this.errorContent = errorContent;

        ServletHandler handler = new ServletHandler();
        //handler.addServletWithMapping(SuccessAlwaysServlet.class, "/2016-03-11/alltypes");
        handler.addServletWithMapping(AlwaysSuccessServlet.class, "/2016-03-11/allTypes");
        //handler.addServletWithMapping(SuccessAfterAttemptServlet.class, "/success-after-attempt");
        server.setHandler(handler);
    }

    public void start() throws Exception {
        server.start();
    }

    public void stop() throws Exception {
        server.stop();
    }

    public int getPort() {
        return ((ServerConnector) server.getConnectors()[0]).getLocalPort();
    }

    /**
     * Always succeeds with with a 200 response.
     */
    public static class AlwaysSuccessServlet extends HttpServlet {

        @Override
        public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
            response.setStatus(HttpStatus.OK_200);
            if (request.getContentType().equals("application/xml")) {
                response.setContentType("application/xml");
                response.getWriter().write(XML_BODY);
            } else {
                response.setContentType("application/json");
                response.getWriter().write(JSON_BODY);
            }
        }
    }

    private class SuccessAfterAttemptServlet extends HttpServlet {
        @Override
        public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
                response.setStatus(HttpStatus.OK_200);
                response.getWriter().write("{}");
        }

        private int getCurrentAttemptNumber(HttpServletRequest request) {
            String retryHeader = request.getHeader(SDK_RETRY_HEADER);
            return Integer.parseInt(retryHeader.split("/")[0]);
        }

        private int getSuccessAterAttempt(HttpServletRequest request) {
            return Integer.parseInt(request.getHeader(SUCCESS_AFTER_ATTEMPT_HEADER));
        }
    }
}