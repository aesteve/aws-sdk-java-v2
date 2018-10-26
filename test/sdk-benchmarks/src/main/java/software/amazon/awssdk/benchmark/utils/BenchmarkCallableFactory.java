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

import static software.amazon.awssdk.benchmark.utils.BenchmarkUtils.getUri;

import java.net.URI;
import java.util.concurrent.Callable;
import software.amazon.awssdk.core.SdkClient;
import software.amazon.awssdk.http.SdkHttpClient;
import software.amazon.awssdk.http.apache.ApacheHttpClient;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.services.protocolec2.ProtocolEc2Client;
import software.amazon.awssdk.services.protocolquery.ProtocolQueryClient;
import software.amazon.awssdk.services.protocolrestjson.ProtocolRestJsonClient;
import software.amazon.awssdk.services.protocolrestxml.ProtocolRestXmlClient;

public class BenchmarkCallableFactory {

    public static Callable callableOfHttpClient(String clientType) {
        URI uri = getUri();

        SdkHttpClient sdkHttpClient;
        ProtocolRestJsonClient client;

        if (clientType.equalsIgnoreCase("UrlConnectionHttpClient")) {
            sdkHttpClient = UrlConnectionHttpClient.builder().build();
        } else {
            sdkHttpClient = ApacheHttpClient.builder().build();
        }

        client = ProtocolRestJsonClient.builder()
                                       .endpointOverride(uri)
                                       .httpClient(sdkHttpClient)
                                       .build();

        return  () -> client.allTypes(BenchmarkUtils.jsonAllTypeRequest());
    }

    public static Callable callableOfProtocol(String value) {
        Protocol protocol = Protocol.fromValue(value);
        URI uri = getUri();
        SdkClient client;
        Callable callable;

        switch (protocol) {
            case XML:
                client = ProtocolRestXmlClient.builder()
                                              .endpointOverride(uri)
                                              .build();
                callable = () -> ((ProtocolRestXmlClient) client).allTypes(BenchmarkUtils.xmlAllTypeRequest());
                break;
            case EC2:
                client = ProtocolEc2Client.builder().endpointOverride(uri).build();
                callable = () -> ((ProtocolEc2Client) client).allTypes(BenchmarkUtils.ec2AllTypeRequest());
                break;
            case JSON:
                client = ProtocolRestJsonClient.builder()
                                               .endpointOverride(uri)
                                               .build();
                callable = () -> ((ProtocolRestJsonClient) client).allTypes(BenchmarkUtils.jsonAllTypeRequest());
                break;
            case QUERY:
                client = ProtocolQueryClient.builder()
                                            .endpointOverride(uri)
                                            .build();
                callable = () -> ((ProtocolQueryClient) client).allTypes(BenchmarkUtils.queryAllTypeRequest());
                break;
            default:
                throw new IllegalArgumentException("invalid protocol");
        }

        return callable;
    }
}
