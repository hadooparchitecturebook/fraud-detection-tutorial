package com.cloudera.sa.node360.kafka;

import com.cloudera.sa.node360.model.EventPojo;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.kafka.clients.producer.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

/**
 * Created by ted.malaska on 6/9/15.
 */
public class EventKafkaProducer {
  String topic;
  private Properties kafkaProps = new Properties();
  private Producer producer;

  public EventKafkaProducer(String topic) {
    this.topic = topic;
  }

  public void setConfigure(Properties properties) {
    kafkaProps = properties;
  }

  public void setConfigure(String brokerList) {


    kafkaProps.put("bootstrap.servers", brokerList);
    kafkaProps.put("metadata.broker.list", brokerList);

    // This is mandatory, even though we don't send keys
    kafkaProps.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
    kafkaProps.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
    kafkaProps.put("acks", "0");

    // how many times to retry when produce request fails?
    kafkaProps.put("retries", "3");
    kafkaProps.put("linger.ms", 2);
    kafkaProps.put("batch.size", 1000);
    kafkaProps.put("queue.time", 2);
  }

  public void start() {
    producer = new KafkaProducer(kafkaProps);
  }

  public void produce(EventPojo event) throws ExecutionException, InterruptedException, IOException {
    produceAsync(event);
  }

  long outputFilterCounter = 0;

  private void produceAsync(EventPojo event) throws IOException {

    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    ObjectOutput out = null;
    try {
      out = new ObjectOutputStream(bos);
      out.writeObject(event);
      byte[] object = bos.toByteArray();

      ProducerRecord record = new ProducerRecord(topic, Bytes.toBytes(event.getNode()),object );
      producer.send(record);

      if (outputFilterCounter++ % 100 == 0) {
        System.out.println("Sent:" + topic + " " + event.getType() + " " + event.getTimestamp() + " " + outputFilterCounter);
      }
    } finally {
      try {
        if (out != null) {
          out.close();
        }
      } catch (IOException ex) {
        // ignore close exception
      }
      try {
        bos.close();
      } catch (IOException ex) {
        // ignore close exception
      }
    }
  }
}
