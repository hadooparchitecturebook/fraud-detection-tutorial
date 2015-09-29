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
public class TopOperation extends AbstractSshExecOperation {

  @Override
  public String getCommand() {
    return "top -n 1 -b";
  }

  @Override
  public void processResults(String host, int port, String result, EventListener listener) throws ExecutionException, InterruptedException, IOException {
    processCpuData(host, result, listener);
    processMemoryData(host, result, listener);
    processSwapData(host, result, listener);


  }

  private void processCpuData(String host, String result, EventListener listener) throws ExecutionException, InterruptedException, IOException {

    ArrayList list = new ArrayList<SamplePojo>();

    int cpuIndex = result.indexOf("Cpu(s):");
    int cpuPercentIndex = result.indexOf('%',  cpuIndex);
    String cpuUser = result.substring(cpuIndex + 7, cpuPercentIndex).trim();

    list.add(new SamplePojo("User", cpuUser));

    int cpuSystemIndex = result.indexOf('%', cpuPercentIndex + 4);
    String cpuSystem = result.substring(cpuPercentIndex + 4, cpuSystemIndex).trim();

    list.add(new SamplePojo("Sys", cpuSystem));

    int cpuNiIndex = result.indexOf("%ni,");
    int cpuIdletIndex = result.indexOf('%', cpuNiIndex + 4);
    String cpuIdle = result.substring(cpuIndex + 4, cpuPercentIndex).trim();

    list.add(new SamplePojo("Idle", cpuIdle));

    listener.publishEvent(new EventPojo(host, System.currentTimeMillis(), "cpu", list));
  }

  private void processMemoryData(String host, String result, EventListener listener) throws ExecutionException, InterruptedException, IOException {

    ArrayList list = new ArrayList<SamplePojo>();

    int memIndex = result.indexOf("Mem:");
    int k1Index = result.indexOf('k', memIndex + 4);
    String totalMemory = result.substring(memIndex + 4, k1Index).trim();

    list.add(new SamplePojo("Total", totalMemory));

    int k2Index = result.indexOf('k', k1Index + 8);
    String usedMemory = result.substring(k1Index + 8, k2Index).trim();

    list.add(new SamplePojo("Used", usedMemory));

    listener.publishEvent(new EventPojo(host, System.currentTimeMillis(), "memory", list));
  }

  private void processSwapData(String host, String result, EventListener listener) throws ExecutionException, InterruptedException, IOException {

    ArrayList list = new ArrayList<SamplePojo>();

    int swapIndex = result.indexOf("Swap:");
    int k1Index = result.indexOf('k', swapIndex + 5);
    String swaped = result.substring(swapIndex + 5, k1Index).trim();

    list.add(new SamplePojo("Swap", swaped));


    listener.publishEvent(new EventPojo(host, System.currentTimeMillis(), "swap", list));
  }
}