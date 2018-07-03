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

import com.google.protobuf.ByteString;
import io.grpc.ManagedChannel;
import io.grpc.netty.NettyChannelBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.github.megachucky.kafka.streams.machinelearning.InceptionInference.InceptionRequest;
import static com.github.megachucky.kafka.streams.machinelearning.InceptionInference.InceptionResponse;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * This class offers image recognition implementation.
 *
 */
public class TensorflowObjectRecogniser implements Closeable {

  private static final Logger LOG = LoggerFactory.getLogger(TensorflowObjectRecogniser.class);

  private ManagedChannel channel;
  private InceptionBlockingStub stub;

  public TensorflowObjectRecogniser(String host, int port) {
    LOG.debug("Creating channel host:{}, port={}", host, port);
    try {
      channel = NettyChannelBuilder
          .forAddress(host, port)
          .usePlaintext(true)
          .build();
      stub = new InceptionBlockingStub(channel);
      //TODO: test channel here with a sample image
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public List<Map.Entry<String, Double>> recognise(InputStream stream) throws Exception {

    List<Map.Entry<String, Double>> objects = new ArrayList<>();
    ByteString jpegData = ByteString.readFrom(stream);
    InceptionRequest request = InceptionRequest.newBuilder()
        .setJpegEncoded(jpegData)
        .build();
    long st = System.currentTimeMillis();
    InceptionResponse response = stub.classify(request);
    long timeTaken = System.currentTimeMillis() - st;
    LOG.debug("Time taken : {}ms", timeTaken);
    Iterator<String> classes = response.getClassesList().iterator();
    Iterator<Float> scores = response.getScoresList().iterator();
    while (classes.hasNext() && scores.hasNext()){
      String className = classes.next();
      Float score = scores.next();
      Map.Entry<String, Double>object = new AbstractMap.SimpleEntry<>(className, score.doubleValue());
      objects.add(object);
    }
    return objects;
  }

  @Override
  public void close() throws IOException {
    if (channel != null){
      LOG.debug("Closing the channel ");
      channel.shutdownNow();
    }
  }
}
