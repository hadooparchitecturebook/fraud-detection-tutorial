# fraud-detection-tutorial

## Disclaimer
This code and repo comes with no guarantees or support from the Hadoop Architectures team. We don't have the bandwidth to support users of this repo in any way. This was done as a mere Proof-Of-Concept to help demonstrate core concepts around the use case.

## Demonstrations

This code has been used in the following presentations:

* ["Hadoop Application Architectures: Fraud Detection" @ Strata / Hadoop World New York 2015](http://tiny.cloudera.com/app-arch-new-york)

## Introduction
The demo uses 5 nodes. The first 2 nodes (NODE1 and NODE2) run Kafka, NODE3 runs flume and zookeeper and NODE3, NODE4 and NODE5 form a 3 node hadoop cluster with HDFS and YARN. Cloudera Manager server runs on NODE3 as well, and can be reached through a web browser on port 7180 ($NODE3:7180). NODE4 also runs the Jetty server. It's left as an exercise to the reader to set up a cluster in this topology.

The name of our cluster nodes, and other properties required in this file are in cluster.properties file in the root directory of this repository. We recommend you source out this file on all nodes of the cluster:
<pre>
. cluster.properties
</pre>

In order to build the demo, simply run build.sh script in the root directory of this repository.
<pre>
./build.sh
</pre>

You have to do install Flume with Cloudera Manager on NODE3. In particular, you have a create this directory
<pre>
mkdir -p /usr/lib/flume-ng/plugins.d/sink1/lib
</pre>

In order to load the Node360 to NODE3, run:
<pre>
scp target/Node360.jar $SSH_USER@$NODE3:/usr/lib/flume-ng/plugins.d/sink1/lib
</pre>

You can upload the node360 code to nodes 3 and 4 for the web server and kafka using:
<pre>
scp target/Node360.jar $SSH_USER@$NODE1:./
scp target/Node360.jar $SSH_USER@$NODE4:./
</pre>

In order to create kafka topics, you can run the following commands from any node of the cluster - preferably, one of node 1 or 2 since they would have kafka installed:
<pre>
kafka-topics --zookeeper $NODE3:2181 --create --topic node_status --partitions 2 --replication-factor 2
kafka-topics --zookeeper $NODE3:2181 --create --topic net_flow --partitions 2 --replication-factor 2
</pre>

DON'T RUN THESE! These are the commands you'd use for deleting the topic, they are only provided for your convenience and clean up:
<pre>
kafka-topics --zookeeper $NODE3:2181 --delete --topic node_status
kafka-topics --zookeeper $NODE3:2181 --delete --topic net_flow
</pre>

This is the command to list the topics to make sure they got created:
<pre>
kafka-topics --zookeeper $NODE3:2181 --list
</pre>

The following commands test the topic:
<pre>
kafka-console-producer --broker-list $NODE1:9092,$NODE2:9092 --topic test
kafka-console-consumer --zookeeper $NODE3:2181 --topic test --from-beginning
</pre>

You also need update Flume agent's configuration with the following. <b>Please be sure to replace references to NODE* to your nodes</b>.
<pre>
tier1.sources = kafka-source-1 kafka-source-2
tier1.channels = channel1 null-channel1
tier1.sinks = sink1

tier1.channels.channel1.type = org.apache.flume.channel.kafka.KafkaChannel
tier1.channels.channel1.capacity = 10000
tier1.channels.channel1.transactionCapacity = 1000
tier1.channels.channel1.brokerList = NODE1:9092,NODE2:9092
tier1.channels.channel1.topic = net_flow
tier1.channels.channel1.groupID = foo_2
tier1.channels.channel1.zookeeperConnect = NODE3:2181
tier1.channels.channel1.parseAsFlumeEvent = false

tier1.channels.null-channel1.type = com.cloudera.sa.node360.flume.NullChannel

tier1.sinks.sink1.type = hdfs
tier1.sinks.sink1.hdfs.path = /user/hive/warehouse/netflow
tier1.sinks.sink1.hdfs.writeFormat = com.cloudera.sa.node360.flume.NetFlowEventSerializer$Builder
tier1.sinks.sink1.hdfs.inUsePrefix = .tmp.
tier1.sinks.sink1.hdfs.batchsize = 10
tier1.sinks.sink1.hdfs.rollInterval = 0
tier1.sinks.sink1.hdfs.rollSize = 100000000
tier1.sinks.sink1.hdfs.rollCount = 0
tier1.sinks.sink1.hdfs.fileType = SequenceFile
tier1.sinks.sink1.hdfs.serializer = com.cloudera.sa.node360.flume.NetFlowEventSerializer
tier1.sinks.sink1.hdfs.codeC = snappy
tier1.sinks.sink1.channel = channel1

tier1.sources.kafka-source-1.type = com.cloudera.sa.node360.kafka.FastKafkaSource
tier1.sources.kafka-source-1.zookeeperConnect = NODE3:2181
tier1.sources.kafka-source-1.topic = net_flow
tier1.sources.kafka-source-1.interceptors = i1
tier1.sources.kafka-source-1.interceptors.i1.type = com.cloudera.sa.node360.flume.EventInterceptor$Builder
tier1.sources.kafka-source-1.batchSize = 1000
tier1.sources.kafka-source-1.batchDurationMillis = 3
tier1.sources.kafka-source-1.groupId = foo_source_1
tier1.sources.kafka-source-1.channels = null-channel1

tier1.sources.kafka-source-2.type = com.cloudera.sa.node360.kafka.FastKafkaSource
tier1.sources.kafka-source-2.zookeeperConnect = NODE3:2181
tier1.sources.kafka-source-2.topic = node_status
tier1.sources.kafka-source-2.interceptors = i1
tier1.sources.kafka-source-2.interceptors.i1.type = com.cloudera.sa.node360.flume.EventInterceptor$Builder
tier1.sources.kafka-source-2.batchSize = 1000
tier1.sources.kafka-source-2.batchDurationMillis = 3
tier1.sources.kafka-source-2.groupId = foo_source_2
tier1.sources.kafka-source-2.channels = null-channel1
</pre>

If not done already, make sure JAVA_HOME is set and java is on the PATH on NODE1 and NODE4 (and preferably, all nodes) by doing the following:
<pre>
echo 'export PATH=/usr/java/jdk1.7.0_75-cloudera/bin/:$PATH' >> .bash_profile
echo 'export JAVA_HOME=/usr/java/jdk1.7.0_75-cloudera/' >> .bash_profile
</pre>

You can create the HBase table using:
<pre>
java -cp Node360.jar com.cloudera.sa.node360.playarea.HBaseAdminMain create 100 $NODE3
</pre>

DON'T RUN THIS. This command deletes the HBase table and is only provided for your convenience and cleanup:
<pre>
java -cp Node360.jar com.cloudera.sa.node360.playarea.HBaseAdminMain drop
</pre>

You can populate the Kafka topic with events like this. THe parameter list here follows the order - netFlowTopic, nodeStatusTopic, brokerList, sleepBetweenIterations, maxIterations, updateNodeListEveryNIterations, updateEtcFileEveryNRequest zookkeeperQuorum.

The command is:
<pre>
java -cp Node360.jar com.cloudera.sa.node360.playarea.KafkaProducerMain \
net_flow node_status \
$NODE1:9092,$NODE2:9092 \
5000 2000 100 7 $NODE3

This command copies the website contents to Node 4:
<pre>
scp webapp.zip $SSH_USER@$NODE4:./
ssh root@$NODE4 'unzip webapp.zip'
</pre>

This command starts the Jetty server:
<pre>
java -cp Node360.jar com.cloudera.sa.node360.playarea.JettyMain 4251 $NODE3
</pre>

To get to the homepage, you can point your browser to http://$NODE4:4251/fe/home.jsp

These are some steps you can run to analyze data in Hive (optional):
<pre>
CREATE TABLE netflow (
  time_of_netflow BIGINT,
  source_address STRING , 
  source_port INT , 
  protocal STRING , 
  number_of_bytes INT , 
  dest_address STRING , 
  dest_port INT ) 
ROW FORMAT DELIMITED
   FIELDS TERMINATED BY ","
STORED AS SEQUENCEFILE;

LOAD DATA INPATH "/tmp/node_status/channel1" OVERWRITE INTO TABLE netflow;

SELECT
 source_address,
 dest_address,
 SUM(number_of_bytes),
 AVG(number_of_bytes),
 MAX(number_of_bytes),
 MIN(number_of_bytes),
 COUNT(number_of_bytes)
FROM netflow
GROUP BY source_address, dest_address

SELECT
 source_address,
 dest_address,
 SUM(number_of_bytes) byte_sum,
 AVG(number_of_bytes),
 MAX(number_of_bytes),
 MIN(number_of_bytes),
 COUNT(number_of_bytes)
FROM netflow
GROUP BY source_address, dest_address 
ORDER BY byte_sum DESC

--- Sym
java -cp Node360.jar com.cloudera.sa.node360.playarea.KafkaProducerMain net_flow node_status 172.28.198.81:9092,172.28.198.82:9092 5000 200 100 7

