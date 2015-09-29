package com.cloudera.sa.node360.flume;

import org.apache.flume.ChannelException;
import org.apache.flume.Event;
import org.apache.flume.Transaction;
import org.apache.flume.channel.AbstractChannel;
import org.apache.flume.channel.BasicChannelSemantics;
import org.apache.flume.channel.BasicTransactionSemantics;

/**
 * Created by ted.malaska on 6/13/15.
 */
public class NullChannel extends AbstractChannel {

  NullTransactionSemantics nts = new NullTransactionSemantics();

  @Override
  public void put(Event event) throws ChannelException {

  }

  @Override
  public Event take() throws ChannelException {
    return null;
  }

  @Override
  public Transaction getTransaction() {
    return nts;
  }

  private class NullTransactionSemantics implements Transaction {


    @Override
    public void begin() {

    }

    @Override
    public void commit() {

    }

    @Override
    public void rollback() {

    }

    @Override
    public void close() {

    }
  }
}
