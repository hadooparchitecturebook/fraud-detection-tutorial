package com.cloudera.sa.node360.nodepuller.listener;

import com.cloudera.sa.node360.model.EventPojo;
import com.cloudera.sa.node360.model.SamplePojo;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Created by ted.malaska on 6/3/15.
 */
public interface EventListener {

  public void publishEvent(EventPojo event) throws ExecutionException, InterruptedException, IOException;
}
