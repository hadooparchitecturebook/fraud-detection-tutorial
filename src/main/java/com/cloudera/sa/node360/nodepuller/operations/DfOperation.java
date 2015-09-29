package com.cloudera.sa.node360.nodepuller.operations;

import com.cloudera.sa.node360.model.EventPojo;
import com.cloudera.sa.node360.nodepuller.listener.EventListener;
import com.cloudera.sa.node360.model.SamplePojo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

/**
 * Created by ted.malaska on 6/3/15.
 */
public class DfOperation extends AbstractSshExecOperation{

  @Override
  public String getCommand() {
    return "df";
  }

  @Override
  public void processResults(String host, int port, String result, EventListener listener) throws ExecutionException, InterruptedException, IOException {
    //TODO yes this can be faster
    String lines[] = result.split("\\r?\\n");
    int counter = 0;
    ArrayList list = new ArrayList<SamplePojo>();
    for (String line: lines) {
      if (counter++ > 0) {
        String cells[] = line.split("\\s+");
        if (cells.length > 4)
          list.add(new SamplePojo(cells[0], cells[2], cells[3]));
      }
    }

    listener.publishEvent(new EventPojo(host, System.currentTimeMillis(), "drives", list));
  }
}
