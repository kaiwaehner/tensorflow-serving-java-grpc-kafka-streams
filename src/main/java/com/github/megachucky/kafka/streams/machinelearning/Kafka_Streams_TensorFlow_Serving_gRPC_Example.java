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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.ForeachAction;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KStreamBuilder;

/**
 * @author Thamme Gowda
 */
public class Kafka_Streams_TensorFlow_Serving_gRPC_Example {

	private static final String imageInputTopic = "ImageInputTopic";
	private static final String imageOutputTopic = "ImageOutputTopic";

	// Prediction Value
	private static String imageClassification = "unknown";
	private static String imageProbability = "unknown";

	private static String imagePath = null;
	private static List<Map.Entry<String, Double>> list = null;

	public static void main(String[] args) throws Exception {

		// if (args.length != 2){
		// System.out.println("Invalid args");
		// System.out.println("Usage: <host:port> <image>");
		// System.out.println("\tExample: localhost:9090 ~/Pictures/cat.jpg");
		// System.exit(1);
		// }

		// String[] parts = args[0].split(":");
		// String server = parts[0];
		// int port = Integer.parseInt(parts[1]);
		// String imagePath = "src/main/resources/example.jpg";

		// Configure Kafka Streams Application
		final String bootstrapServers = args.length > 0 ? args[0] : "localhost:9092";
		final Properties streamsConfiguration = new Properties();
		// Give the Streams application a unique name. The name must be unique
		// in the Kafka cluster
		// against which the application is run.
		streamsConfiguration.put(StreamsConfig.APPLICATION_ID_CONFIG,
				"kafka-streams-tensorflow-image-recognition-example");
		// Where to find Kafka broker(s).
		streamsConfiguration.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);

		// Specify default (de)serializers for record keys and for record
		// values.
		streamsConfiguration.put(StreamsConfig.KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
		streamsConfiguration.put(StreamsConfig.VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());

		// In the subsequent lines we define the processing topology of the
		// Streams application.
		final KStreamBuilder builder = new KStreamBuilder();

		// Construct a `KStream` from the input topic "ImageInputTopic", where
		// message values represent lines of text
		final KStream<String, String> imageInputLines = builder.stream(imageInputTopic);

		// Stream Processor (in this case 'foreach' to add custom logic, i.e. apply the
		// analytic model)
		imageInputLines.foreach((key, value) -> {

		
			System.out.println("Image path: " + value);

			imagePath = value;
			
			imageClassification = "unknown";
			imageProbability = "unknown";

			TensorflowObjectRecogniser recogniser = new TensorflowObjectRecogniser("localhost", 9000);

			System.out.println("Image = " + imagePath);
			InputStream jpegStream;
			try {
				jpegStream = new FileInputStream(imagePath);
				list = recogniser.recognise(jpegStream);
				System.out.println(list);
				recogniser.close();
				jpegStream.close();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		});

		// airlineInputLines.print();

		// Transform message: Add prediction information
		KStream<String, Object> transformedMessage = imageInputLines
				.mapValues(value -> "Prediction: What is the content of this picture? => " + list);

		// Send prediction information to Output Topic
		transformedMessage.to(imageOutputTopic);

		// Start Kafka Streams Application to process new incoming images from the Input
		// Topic
		final KafkaStreams streams = new KafkaStreams(builder, streamsConfiguration);

		streams.cleanUp();

		streams.start();

		System.out.println("Image Recognition Microservice is running...");

		System.out.println("Input image to Kafka topic " + imageInputTopic + "; Output prediction to Kafka topic " + imageOutputTopic);

		// Add shutdown hook to respond to SIGTERM and gracefully close Kafka
		// Streams
		Runtime.getRuntime().addShutdownHook(new Thread(streams::close));

	}
}
