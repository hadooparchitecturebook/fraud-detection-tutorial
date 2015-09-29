package com.cloudera.sa.node360.symulator;

import com.cloudera.sa.node360.constant.TopLevelConst;
import com.cloudera.sa.node360.model.EventPojo;
import com.cloudera.sa.node360.model.NodePojo;
import com.cloudera.sa.node360.model.SamplePojo;
import com.cloudera.sa.node360.nodepuller.listener.EventListener;
import com.cloudera.sa.node360.service.HBaseService;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutionException;

/**
 * Created by ted.malaska on 6/4/15.
 */
public class FastLoadNodeDataSymulator {

  EventListener listener;
  HBaseService hbaseService;
  long sleepBetweenIterations;
  long maxIterations;
  int updateNodeListEveryNIterations;
  int updateEtcFileEveryNRequest;
  long symTime;

  public FastLoadNodeDataSymulator(EventListener listener, HBaseService hbaseService,
                                   long sleepBetweenIterations, long maxIterations,
                                   int updateNodeListEveryNIterations,
                                   int updateEtcFileEveryNRequest) {
    this.listener = listener;
    this.hbaseService = hbaseService;
    this.sleepBetweenIterations = sleepBetweenIterations;
    this.maxIterations = maxIterations;
    this.updateNodeListEveryNIterations = updateNodeListEveryNIterations;
    this.updateEtcFileEveryNRequest = updateEtcFileEveryNRequest;
    this.symTime = System.currentTimeMillis();
  }

  public void run(int numberOfExternalNodes) throws IOException, InterruptedException, ExecutionException {

    List<NodePojo> internalNodeList = null;
    List<String> fullNodeList = new ArrayList<String>();

    for (int i = 0; i < maxIterations; i++) {
      if (i == 0 || i % updateNodeListEveryNIterations == 0) {
        internalNodeList = hbaseService.getFullNodeList();

        for (NodePojo node: internalNodeList) {
          fullNodeList.add(node.getIpAddress());
        }
        for (int j = 0; j < numberOfExternalNodes; j++) {
          fullNodeList.add("127.42.42." + j);
        }
      }
      for (NodePojo node: internalNodeList) {
        produceCpuEvents(node);
        produceMemEvents(node);
        produceSwapEvents(node);
        produceDfEvents(node);
        produceEtcEvents(node);
        produceNewFlowEvents(node, fullNodeList);
      }
      symTime += 5000;
      Thread.sleep(sleepBetweenIterations);
    }
  }
  HashMap<String, NetFlowPrettyRandomGenerator> netFlowGenerator = new HashMap<String, NetFlowPrettyRandomGenerator>();
  public void produceNewFlowEvents(NodePojo node, List<String> fullNodeList) throws ExecutionException, InterruptedException, IOException {
    NetFlowPrettyRandomGenerator gen = netFlowGenerator.get(node.getIpAddress());
    if (gen == null) {
      gen = new NetFlowPrettyRandomGenerator(node.getIpAddress(),
              fullNodeList, (r.nextInt(15) + 5), 10000);
      netFlowGenerator.put(node.getIpAddress(), gen);
    }

    for (int i = 0; i < 5; i++) {
      listener.publishEvent(new EventPojo(node.getIpAddress(), symTime, TopLevelConst.NET_FLOW_LINE_CHART_DATA, gen.nextValue()));
    }
  }


  HashMap<String, ChartPrettyRandomGenerator> cpuUserGenerator = new HashMap<String, ChartPrettyRandomGenerator>();
  HashMap<String, ChartPrettyRandomGenerator> cpuSysGenerator = new HashMap<String, ChartPrettyRandomGenerator>();
  public void produceCpuEvents(NodePojo node) throws ExecutionException, InterruptedException, IOException {
    ChartPrettyRandomGenerator genUser = cpuUserGenerator.get(node.getIpAddress());
    if (genUser == null) {
      genUser = new ChartPrettyRandomGenerator(true, 80, 20);
      cpuUserGenerator.put(node.getIpAddress(), genUser);
    }
    ChartPrettyRandomGenerator genSys = cpuSysGenerator.get(node.getIpAddress());
    if (genSys == null) {
      genSys = new ChartPrettyRandomGenerator(true, 80, 20);
      cpuSysGenerator.put(node.getIpAddress(), genSys);
    }
    List<SamplePojo> samples = new ArrayList<SamplePojo>();

    long userCpu = Long.parseLong(genUser.getNextValue());
    long systemCpu = Long.parseLong(genSys.getNextValue());
    long idleCpu;

    if (userCpu + systemCpu > 100) {
      systemCpu = 100 - userCpu;
      idleCpu = 0;
    } else {
      idleCpu = 100 - (userCpu + systemCpu);
    }

    samples.add(new SamplePojo("User", Long.toString(userCpu)));
    samples.add(new SamplePojo("Sys", Long.toString(systemCpu)));
    samples.add(new SamplePojo("Idle", Long.toString(idleCpu)));

    listener.publishEvent(new EventPojo(node.getIpAddress(), symTime, TopLevelConst.CPU_LINE_CHART_DATA, samples));
  }

  HashMap<String, ChartPrettyRandomGenerator> memGenerator = new HashMap<String, ChartPrettyRandomGenerator>();
  public void produceMemEvents(NodePojo node) throws ExecutionException, InterruptedException, IOException {
    ChartPrettyRandomGenerator gen = memGenerator.get(node.getIpAddress());
    if (gen == null) {
      gen = new ChartPrettyRandomGenerator(true, 128000000, 1000000);
      memGenerator.put(node.getIpAddress(), gen);
    }
    List<SamplePojo> samples = new ArrayList<SamplePojo>();

    samples.add(new SamplePojo("Used", gen.getNextValue(), "128000000"));

    listener.publishEvent(new EventPojo(node.getIpAddress(), symTime, TopLevelConst.MEMORY_LINE_CHART_DATA, samples));
  }

  HashMap<String, ChartPrettyRandomGenerator> swapGenerator = new HashMap<String, ChartPrettyRandomGenerator>();
  public void produceSwapEvents(NodePojo node) throws ExecutionException, InterruptedException, IOException {
    ChartPrettyRandomGenerator gen = swapGenerator.get(node.getIpAddress());
    if (gen == null) {
      gen = new ChartPrettyRandomGenerator(true, 12800000, 1000);
      swapGenerator.put(node.getIpAddress(), gen);
    }
    List<SamplePojo> samples = new ArrayList<SamplePojo>();

    samples.add(new SamplePojo("Swap", gen.getNextValue(), "12800000"));

    listener.publishEvent(new EventPojo(node.getIpAddress(), symTime, "swap", samples));
  }

  HashMap<String, List<ChartPrettyRandomGenerator>> dfGenerator = new HashMap<String, List<ChartPrettyRandomGenerator>>();
  public void produceDfEvents(NodePojo node) throws ExecutionException, InterruptedException, IOException {
    List<ChartPrettyRandomGenerator> listOfGen = dfGenerator.get(node.getIpAddress());
    if (listOfGen == null) {
      listOfGen = new ArrayList<ChartPrettyRandomGenerator>();
      listOfGen.add(new ChartPrettyRandomGenerator(true, 128000000, 1000000));
      listOfGen.add(new ChartPrettyRandomGenerator(true, 128000000, 100000));
      listOfGen.add(new ChartPrettyRandomGenerator(true, 128000000, 1000000));
      listOfGen.add(new ChartPrettyRandomGenerator(true, 128000000, 100000));
      listOfGen.add(new ChartPrettyRandomGenerator(true, 128000000, 1000000));
      listOfGen.add(new ChartPrettyRandomGenerator(true, 128000000, 100000));
      listOfGen.add(new ChartPrettyRandomGenerator(true, 128000000, 1000000));
      listOfGen.add(new ChartPrettyRandomGenerator(true, 128000000, 100000));
      listOfGen.add(new ChartPrettyRandomGenerator(true, 128000000, 1000000));
      listOfGen.add(new ChartPrettyRandomGenerator(true, 128000000, 100000));
      listOfGen.add(new ChartPrettyRandomGenerator(true, 128000000, 1000000));
      listOfGen.add(new ChartPrettyRandomGenerator(true, 128000000, 100000));

      dfGenerator.put(node.getIpAddress(), listOfGen);
    }
    List<SamplePojo> samples = new ArrayList<SamplePojo>();
    int counter = 0;
    samples.add(new SamplePojo("/dev/sda1", listOfGen.get(counter++).getNextValue(), "128000000"));
    samples.add(new SamplePojo("/dev/sda2", listOfGen.get(counter++).getNextValue(), "128000000"));
    samples.add(new SamplePojo("/dev/sda3", listOfGen.get(counter++).getNextValue(), "128000000"));
    samples.add(new SamplePojo("/dev/sda4", listOfGen.get(counter++).getNextValue(), "128000000"));
    samples.add(new SamplePojo("/dev/sda5", listOfGen.get(counter++).getNextValue(), "128000000"));
    samples.add(new SamplePojo("/dev/sda6", listOfGen.get(counter++).getNextValue(), "128000000"));
    samples.add(new SamplePojo("/dev/sda7", listOfGen.get(counter++).getNextValue(), "128000000"));
    samples.add(new SamplePojo("/dev/sda8", listOfGen.get(counter++).getNextValue(), "128000000"));
    samples.add(new SamplePojo("/dev/sda9", listOfGen.get(counter++).getNextValue(), "128000000"));
    samples.add(new SamplePojo("/dev/sda10", listOfGen.get(counter++).getNextValue(), "128000000"));
    samples.add(new SamplePojo("/dev/sda11", listOfGen.get(counter++).getNextValue(), "128000000"));
    samples.add(new SamplePojo("/dev/sda12", listOfGen.get(counter++).getNextValue(), "128000000"));

    listener.publishEvent(new EventPojo(node.getIpAddress(), symTime, "drives", samples));
  }

  SimpleDateFormat lsDateFormat = new SimpleDateFormat("MMM d HH:mm");
  int etcCounter = 0;
  Random r = new Random();
  public void produceEtcEvents(NodePojo node) throws ExecutionException, InterruptedException, IOException {

    if (etcCounter++ > updateEtcFileEveryNRequest) {
      etcCounter = 0;
      List<SamplePojo> samples = new ArrayList<SamplePojo>();

      String addedLine1 = "";
      if (r.nextBoolean()) {
        addedLine1 = "172.17.70.42\tbda1node02-adm.sjc.cloudera.com bda1node04-adm\n";
      }
      String addedLine2 = "";
      if (r.nextBoolean()) {
        addedLine2 = "172.17.70.42\tbda1node02-adm.sjc.cloudera.com bda1node05-adm\n";
      }

      String file = "#### DO NOT REMOVE THESE LINES ####\n" +
              "#### %INITIALIZED FOR BDA% ####\n" +
              "# Do not remove the following line, or various programs\n" +
              "# that require network functionality will fail.\n" +
              "::1             localhost6.localdomain6 localhost6\n" +
              "\n" +
              "127." + r.nextInt(100) + "." + r.nextInt(100) + "." + r.nextInt(100) + "\tbda1node01-adm.sjc.cloudera.com bda1node01-adm\n" +
              addedLine1 +
              "127.17.70.12\tbda1node02-adm.sjc.cloudera.com bda1node02-adm\n" +
              addedLine2 +
              "127.17.70.13\tbda1node03-adm.sjc.cloudera.com bda1node03-adm\n";

      System.out.println("Pushed File for " +  node.getIpAddress());

      String meta = "-rw-r--r--  1 root root " + file.length() + " " + lsDateFormat.format(new Date(symTime)) + " hosts";

      listener.publishEvent(new EventPojo(node.getIpAddress(), symTime, "/etc/hosts", meta, file));
    }
  }
}
