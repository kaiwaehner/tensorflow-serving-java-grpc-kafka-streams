# TensorFlow Serving + Java + Kafka Streams + gRCP
This project contains a demo to do model inference with Apache Kafka, Kafka Streams and a TensorFlow model deployed using [TensorFlow Serving](https://www.tensorflow.org/serving/) (leveraging [Google Cloud ML Engine](https://cloud.google.com/ml-engine/docs/tensorflow/deploying-models) in this example). The concepts are very similar for other ML frameworks and Cloud Providers, e.g. you could also use Apache MXNet and [AWS model server](https://github.com/awslabs/mxnet-model-server).

## Model Serving: Stream Processing vs. Request Response
Machine Learning / Deep Learning models can be used in different way to do predictions. The preferred way is to deploy an analytic model directly into a Kafka Streams application. You could e.g. use the [TensorFlow for Java API](https://www.tensorflow.org/install/install_java). Examples here: [Model Inference within Kafka Streams Microservices](https://github.com/kaiwaehner/kafka-streams-machine-learning-examples). 

However, it is not always a feasible approach. Sometimes it makes sense or is needed to deploy a model in another serving infrastructure like TF-Serving for TensorFlow models. This project shows how access such an infrastructure via Apache Kafka and Kafka Streams.

![Model Serving: Stream Processing vs. Request Response](pictures/Model_Inference_Stream_Processing_vs_Request_Response.png)

*Pros of an external model serving infrastructure like TensorFlow Serving:*
- Simple integration with existing systems and technologies
- Easier to understand if you come from non-streaming world
- Later migration to real streaming is also possible

*Cons:*
- Framework-specific Deployment (e.g. only TensorFlow models)
- Coupling the availability, scalability, and latency/throughput of your Kafka Streams application with the SLAs of the RPC interface
- Side-effects (e.g. in case of failure) not covered by Kafka processing (e.g. Exactly Once)
- Worse latency as communication over internet required
- No local inference (offline, devices, edge processing, etc.)

## TensorFlow Serving (using Google Cloud ML Engine)
The blog post "[How to deploy TensorFlow models to production using TF Serving](https://medium.freecodecamp.org/how-to-deploy-tensorflow-models-to-production-using-tf-serving-4b4b78d41700)" is a great explanation of how to export and deploy trained TensorFlow models to a TensorFlow Serving infrastructure. You can either deploy your own infrastructure anywhere or leverage a cloud service like Google Cloud ML Engine. A [SavedModel](https://www.tensorflow.org/programmers_guide/saved_model#build_and_load_a_savedmodel) is TensorFlow's recommended format for saving models, and it is the required format for deploying trained TensorFlow models using TensorFlow Serving or deploying on Goodle Cloud ML Engine

Things to do:
1. Create Cloud ML Engine
2. Deploy prebuild TensorFlow Model
3. Create Kafka Cluster
4. Implement Kafka Streams application
5. Deploy Kafka Streams application (e.g. to a Kubernetes cluster)
6. Generate streaming data to test the combination of Kafka Streams and TensorFlow Serving


### Step 1: Create a TensorFlow model and export it to 'SavedModel' format.
I simply added an existing pretrained Image Recognition model built with TensorFlow (Inception V1). 

I also created a new model for predictions of census using the "[ML Engine getting started guide](https://cloud.google.com/ml-engine/docs/tensorflow/getting-started-training-prediction)". The data for training is in 'data' folder.

### Step 2: Deploy model to Google ML Engine
[Getting Started with Google ML Engine](https://cloud.google.com/ml-engine/docs/tensorflow/deploying-models)

### Step 3: Create Kafka Cluster using GCP Confluent Cloud
[Confluent Cloud - Apache Kafka as a Service](https://www.confluent.io/confluent-cloud/)

### TODO Implement and deploy Streams app

### Example 4 - Census Prediction with TensorFlow Serving
This example shows how do use TensorFlow Serving to deploy a model. The Kafka Streams app can access it via HTTP or gRPC to do the inference. You could also use e.g. Google Cloud ML Engine to deploy the TensorFlow model in a public cloud the same way.

TODO more details discussed in another github project.

Steps:
- Install and run TensorFlow Serving locally (e.g. in [Docker container](https://www.tensorflow.org/serving/docker))
                docker build --pull -t tensorflow-serving-devel -f Dockerfile.devel .
                docker run -it tensorflow-serving-devel
                
                git clone --recurse-submodules https://github.com/tensorflow/serving
                cd serving/tensorflow
                ./configure
                cd ..
                bazel test tensorflow_serving/...
                
=> Takes long time... Better use a prebuilt container like below
- [Deploy TensorFlow model to TensorFlow serving](https://www.tensorflow.org/programmers_guide/saved_model#load_and_serve_a_savedmodel_in_tensorflow_serving)

- mvn clean package istall

- Start Kafka and create topics
                confluent start kafka
                
                kafka-topics --zookeeper localhost:2181 --create --topic ImageInputTopic --partitions 3 --replication-factor 1
                
                kafka-topics --zookeeper localhost:2181 --create --topic ImageOutputTopic --partitions 3 --replication-factor 1
                
                java -cp target/kafka-streams-machine-learning-examples-1.0-SNAPSHOT-jar-with-dependencies.jar com.github.megachucky.kafka.streams.machinelearning.Kafka_Streams_TensorFlow_Serving_gRPC_Image_Recognition_Example


                java -cp target/kafka-streams-machine-learning-examples-1.0-SNAPSHOT-jar-with-dependencies.jar com.github.megachucky.kafka.streams.machinelearning.Main
                
                
- TODO Start Streams App
- TODO Start Kafka and create topic
- TODO Send test message

- Send messages, e.g. with kafkacat: 
                echo -e "src/main/resources/TensorFlow_Images/dog.jpg" | kafkacat -b localhost:9092 -P -t ImageInputTopic
                
- Consume predictions:
                kafka-console-consumer --bootstrap-server localhost:9092 --topic ImageOutputTopic --from-beginning
- Find more details in the unit test...


https://github.com/gameofdimension/inception-java-client
pull and start the prebuilt container, forward port 9000

# pull and start the prebuilt container, forward port 9000
docker run -it -p 9000:9000 tgowda/inception_serving_tika

# Inside the container, start tensorflow service
root@8311ea4e8074:/# /serving/server.sh
This is hosting the model. The client just uses gRPC and Protobuf. It does not include any TensorFlow APIs.

mvn clean compile exec:java -Dexec.args="localhost:9000 example.jpg"

https://github.com/thammegowda/tensorflow-grpc-java/blob/master/src/main/java/edu/usc/irds/tensorflow/grpc/TensorflowObjectRecogniser.java

java -cp target/kafka-streams-machine-learning-examples-1.0-SNAPSHOT-jar-with-dependencies.jar com.github.megachucky.kafka.streams.machinelearning.Main localhost:9000 src/main/resources/TensorFlow_Images/dog.jpg

java -cp target/kafka-streams-machine-learning-examples-1.0-SNAPSHOT-jar-with-dependencies.jar com.github.megachucky.kafka.streams.machinelearning.Kafka_Streams_TensorFlow_Serving_gRPC_Image_Recognition_Example localhost:9000 src/main/resources/TensorFlow_Images/dog.jpg
