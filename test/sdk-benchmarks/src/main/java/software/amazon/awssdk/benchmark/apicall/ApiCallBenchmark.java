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

package software.amazon.awssdk.benchmark.apicall;

import static software.amazon.awssdk.benchmark.utils.BenchmarkUtils.ERROR_JSON_BODY;
import static software.amazon.awssdk.benchmark.utils.BenchmarkUtils.JSON_BODY;
import static software.amazon.awssdk.benchmark.utils.BenchmarkUtils.PORT_NUMBER;

import java.util.concurrent.Callable;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.infra.Blackhole;
import software.amazon.awssdk.benchmark.utils.AnotherMockServer;

public abstract class ApiCallBenchmark {
    private Callable callable;
    private AnotherMockServer mockServer;

    @Setup(Level.Trial)
    public void setup() throws Exception {
        mockServer = new AnotherMockServer(PORT_NUMBER, JSON_BODY, ERROR_JSON_BODY);
        mockServer.start();
    }

    @TearDown(Level.Trial)
    public void tearDown() throws Exception {
        mockServer.stop();
    }

    @Benchmark
    public void run(Blackhole blackhole) throws Exception {
        blackhole.consume(callable().call());
    }

    abstract Callable callable();
}
