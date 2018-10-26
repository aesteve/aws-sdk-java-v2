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

import com.fasterxml.jackson.annotation.JsonCreator;

/**
 * Class javadoc
 */
public enum  Protocol {
    XML("xml"),
    EC2("ec2"),
    JSON("json"),
    QUERY("query");

    private String value;

    Protocol(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }

    @JsonCreator
    public static Protocol fromValue(String strProtocol) {
        if (strProtocol == null) {
            return null;
        }

        for (Protocol protocol : Protocol.values()) {
            if (protocol.value.equals(strProtocol)) {
                return protocol;
            }
        }

        throw new IllegalArgumentException("Unknown enum value for Protocol : " + strProtocol);
    }


}
