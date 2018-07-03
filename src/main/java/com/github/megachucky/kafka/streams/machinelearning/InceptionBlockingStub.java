/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.megachucky.kafka.streams.machinelearning;

import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ExperimentalApi;
import io.grpc.MethodDescriptor;
import io.grpc.MethodDescriptor.MethodType;
import io.grpc.protobuf.ProtoUtils;
import io.grpc.stub.AbstractStub;

import static com.github.megachucky.kafka.streams.machinelearning.InceptionInference.InceptionRequest;
import static com.github.megachucky.kafka.streams.machinelearning.InceptionInference.InceptionResponse;
import static io.grpc.MethodDescriptor.generateFullMethodName;
import static io.grpc.stub.ClientCalls.blockingUnaryCall;

/**
 * Stub implementation for {@link #SERVICE_NAME} (Tensorflow' Inception service )
 */
public class InceptionBlockingStub extends AbstractStub<InceptionBlockingStub> {

  public static final String SERVICE_NAME = "tensorflow.serving.InceptionService";
  public static final String METHOD_NAME = "Classify";

  @ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static final MethodDescriptor<InceptionRequest, InceptionResponse> METHOD_DESCRIPTOR = MethodDescriptor
      .create(MethodType.UNARY,
          generateFullMethodName(SERVICE_NAME, METHOD_NAME),
          ProtoUtils.marshaller(InceptionRequest.getDefaultInstance()),
          ProtoUtils.marshaller(InceptionResponse.getDefaultInstance()));

  public InceptionBlockingStub(Channel channel) {
    super(channel);
  }

  private InceptionBlockingStub(Channel channel, CallOptions callOptions) {
    super(channel, callOptions);
  }

  @Override
  protected InceptionBlockingStub build(Channel channel,
      CallOptions callOptions) {
    return new InceptionBlockingStub(channel, callOptions);
  }

  public InceptionResponse classify(InceptionRequest request) {
    return blockingUnaryCall(getChannel(), METHOD_DESCRIPTOR, getCallOptions(),
        request);
  }
}