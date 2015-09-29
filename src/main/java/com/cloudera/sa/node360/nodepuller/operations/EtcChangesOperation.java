package com.cloudera.sa.node360.nodepuller.operations;

import com.cloudera.sa.node360.model.EventPojo;
import com.cloudera.sa.node360.nodepuller.listener.EventListener;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * Created by ted.malaska on 6/3/15.
 */
public class EtcChangesOperation extends AbstractSshExecOperation{

  Map<String, String> latestHostsMeta = new HashMap<String, String>();

  @Override
  public String getCommand() {
    return "ls -lt /etc/hosts; cat /etc/hosts";
  }

  @Override
  public void processResults(String host, int port, String result, EventListener listener) throws ExecutionException, InterruptedException, IOException {
    //TODO yes this can be faster
    int metaIndex = result.indexOf('\n');
    String meta = result.substring(0, metaIndex);

    String latestMeta = latestHostsMeta.get(host);

    if (latestMeta == null || !latestMeta.equals(meta)) {
      latestMeta = meta;
      latestHostsMeta.put(host, latestMeta);
      listener.publishEvent(new EventPojo(host, System.currentTimeMillis(), "etc/hosts", meta, result.substring(metaIndex + 1)));
    }
  }
}
