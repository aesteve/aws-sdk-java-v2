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

import static software.amazon.awssdk.benchmark.utils.BenchmarkUtils.PORT_NUMBER;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolVersion;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.message.BasicStatusLine;
import software.amazon.awssdk.utils.StringInputStream;

/**
 * Class javadoc
 */
public class MockServer {

    /**
     * The server socket which the test service will listen to.
     */
    private ServerSocket serverSocket;
    private Thread listenerThread;
    private final String content;
    private HttpResponse response;

    public MockServer(int statusCode, String statusMessage, String content) {
        this.content = content;
        response = new BasicHttpResponse(
            new BasicStatusLine(new ProtocolVersion("HTTP", 1, 1), statusCode, statusMessage));
        setEntity(response, content);
        response.addHeader("Content-Length", String.valueOf(content.getBytes().length));
        response.addHeader("Connection", "close");
    }

    private static void setEntity(HttpResponse response, String content) {
        BasicHttpEntity entity = new BasicHttpEntity();
        entity.setContent(new StringInputStream(content));
        response.setEntity(entity);
    }

    public void start() {
        try {
            serverSocket = new ServerSocket(PORT_NUMBER); // auto-assign a port at localhost
            System.out.println("Listening on port " + serverSocket.getLocalPort());
        } catch (IOException e) {
            throw new RuntimeException("Unable to start the server socket.", e);
        }

        listenerThread = new MockServerListenerThread(serverSocket);
        listenerThread.setDaemon(true);
        listenerThread.start();
    }

    public void stop() {
        listenerThread.interrupt();
        try {
            listenerThread.join(10 * 1000);
        } catch (InterruptedException e1) {
            System.err.println("The listener thread didn't terminate " + "after waiting for 10 seconds.");
        }

        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                throw new RuntimeException("Unable to stop the server socket.", e);
            }
        }
    }

    private class MockServerListenerThread extends Thread {
        /** The server socket which this thread listens and responds to. */
        private final ServerSocket serverSocket;

        public MockServerListenerThread(ServerSocket serverSocket) {
            this.serverSocket = serverSocket;
            setDaemon(true);
        }

        @Override
        public void run() {
            try {
                while (true) {
                    Socket socket = null;
                    try {
                        socket = serverSocket.accept();
                        try (DataOutputStream out = new DataOutputStream(socket.getOutputStream())) {
                            StringBuilder builder = new StringBuilder();
                            builder.append(response.getStatusLine().toString() + "\r\n");
                            for (Header header : response.getAllHeaders()) {
                                builder.append(header.getName() + ":" + header.getValue() + "\r\n");
                            }
                            builder.append("\r\n");
                            builder.append(content);
                            //System.out.println(builder.toString());
                            out.writeBytes(builder.toString());
                        }
                    } catch (SocketException se) {
                        // Ignored or expected.
                    } finally {
                        if (socket != null) {
                            socket.close();
                        }
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException("Error when waiting for new socket connection.", e);
            }
        }
    }

}
