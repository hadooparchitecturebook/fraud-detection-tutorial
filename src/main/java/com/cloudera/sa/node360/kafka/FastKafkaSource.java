package com.cloudera.sa.node360.kafka;

import kafka.consumer.ConsumerIterator;
import kafka.consumer.ConsumerTimeoutException;
import kafka.consumer.KafkaStream;
import kafka.javaapi.consumer.ConsumerConnector;
import kafka.message.MessageAndMetadata;
import org.apache.flume.*;
import org.apache.flume.conf.Configurable;
import org.apache.flume.conf.ConfigurationException;
import org.apache.flume.event.EventBuilder;
import org.apache.flume.instrumentation.kafka.KafkaSourceCounter;
import org.apache.flume.source.AbstractSource;
import org.apache.flume.source.kafka.KafkaSourceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Created by ted.malaska on 6/14/15.
 */
public class FastKafkaSource extends AbstractSource implements Configurable, PollableSource {
  private static final Logger log = LoggerFactory.getLogger(FastKafkaSource.class);
  private ConsumerConnector consumer;
  private ConsumerIterator<byte[], byte[]> it;
  private String topic;
  private int batchUpperLimit;
  private int timeUpperLimit;
  private int consumerTimeout;
  private boolean kafkaAutoCommitEnabled;
  private Context context;
  private Properties kafkaProps;
  private final List<Event> eventList = new ArrayList();
  private KafkaSourceCounter counter;

  public FastKafkaSource() {
  }

  public Status process() throws EventDeliveryException {
    long batchStartTime = System.currentTimeMillis();
    long batchEndTime = System.currentTimeMillis() + (long)this.timeUpperLimit;

    try {
      boolean e = false;
      long startTime = System.nanoTime();

      while(this.eventList.size() < this.batchUpperLimit && System.currentTimeMillis() < batchEndTime) {
        e = this.hasNext();
        if(e) {
          MessageAndMetadata endTime = this.it.next();
          byte[] kafkaMessage = (byte[])endTime.message();
          byte[] kafkaKey = (byte[])endTime.key();
          HashMap headers = new HashMap();
          headers.put("timestamp", String.valueOf(System.currentTimeMillis()));
          headers.put("topic", this.topic);
          if(kafkaKey != null) {
            headers.put("key", new String(kafkaKey));
          }

          if(log.isDebugEnabled()) {
            log.debug("Message: {}", new String(kafkaMessage));
          }

          Event event = EventBuilder.withBody(kafkaMessage, headers);
          this.eventList.add(event);
        }

        if(log.isDebugEnabled()) {
          log.debug("Waited: {} ", Long.valueOf(System.currentTimeMillis() - batchStartTime));
          log.debug("Event #: {}", Integer.valueOf(this.eventList.size()));
        }
      }

      long endTime1 = System.nanoTime();
      this.counter.addToKafkaEventGetTimer((endTime1 - startTime) / 1000000L);
      this.counter.addToEventReceivedCount(Long.valueOf((long)this.eventList.size()).longValue());
      if(this.eventList.size() > 0) {
        this.getChannelProcessor().processEventBatch(this.eventList);
        this.counter.addToEventAcceptedCount((long)this.eventList.size());
        this.eventList.clear();
        if(log.isDebugEnabled()) {
          log.debug("Wrote {} events to channel", Integer.valueOf(this.eventList.size()));
        }

        if(!this.kafkaAutoCommitEnabled) {
          long commitStartTime = System.nanoTime();
          this.consumer.commitOffsets();
          long commitEndTime = System.nanoTime();
          this.counter.addToKafkaCommitTimer((commitEndTime - commitStartTime) / 1000000L);
        }
      }

      if(!e) {
        if(log.isDebugEnabled()) {
          this.counter.incrementKafkaEmptyCount();
          log.debug("Returning with backoff. No more data to read");
        }

        //Thread.sleep(10);
        return Status.READY;
      } else {
        return Status.READY;
      }
    } catch (Exception var18) {
      log.error("KafkaSource EXCEPTION, {}", var18);
      return Status.BACKOFF;
    }
  }

  public void configure(Context context) {
    this.context = context;
    this.batchUpperLimit = context.getInteger("batchSize", Integer.valueOf(1000)).intValue();
    this.timeUpperLimit = context.getInteger("batchDurationMillis", Integer.valueOf(1000)).intValue();
    this.topic = context.getString("topic");
    if(this.topic == null) {
      throw new ConfigurationException("Kafka topic must be specified.");
    } else {
      this.kafkaProps = KafkaSourceUtil.getKafkaProperties(context);
      this.consumerTimeout = Integer.parseInt(this.kafkaProps.getProperty("consumer.timeout.ms"));
      this.kafkaAutoCommitEnabled = Boolean.parseBoolean(this.kafkaProps.getProperty("auto.commit.enable"));
      if(this.counter == null) {
        this.counter = new KafkaSourceCounter(this.getName());
      }

    }
  }

  public synchronized void start() {
    log.info("Starting {}...", this);

    try {
      this.consumer = KafkaSourceUtil.getConsumer(this.kafkaProps);
    } catch (Exception var6) {
      throw new FlumeException("Unable to create consumer. Check whether the ZooKeeper server is up and that the Flume agent can connect to it.", var6);
    }

    HashMap topicCountMap = new HashMap();
    topicCountMap.put(this.topic, Integer.valueOf(1));

    try {
      Map e = this.consumer.createMessageStreams(topicCountMap);
      List topicList = (List)e.get(this.topic);
      KafkaStream stream = (KafkaStream)topicList.get(0);
      this.it = stream.iterator();
    } catch (Exception var5) {
      throw new FlumeException("Unable to get message iterator from Kafka", var5);
    }

    log.info("Kafka source {} started.", this.getName());
    this.counter.start();
    super.start();
  }

  public synchronized void stop() {
    if(this.consumer != null) {
      this.consumer.shutdown();
    }

    this.counter.stop();
    log.info("Kafka Source {} stopped. Metrics: {}", this.getName(), this.counter);
    super.stop();
  }

  boolean hasNext() {
    try {
      this.it.hasNext();
      return true;
    } catch (ConsumerTimeoutException var2) {
      return false;
    }
  }
}
