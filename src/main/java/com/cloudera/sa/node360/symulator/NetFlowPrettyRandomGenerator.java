package com.cloudera.sa.node360.symulator;

import com.cloudera.sa.node360.model.NetFlowPojo;
import com.cloudera.sa.node360.model.SamplePojo;

import java.util.*;

/**
 * Created by ted.malaska on 6/8/15.
 */
public class NetFlowPrettyRandomGenerator {
  String sourceNode;
  List<String> nodeDestinationList = new ArrayList<String>();
  int numberOfIterationsBeforeNodeSelectChange;
  int currentIteration = 0;
  int maxValuePerIteration;

  Random r = new Random();

  static List<String> protocolList = new ArrayList<String>();
  static List<Integer> portList = new ArrayList<Integer>();
  static {
    protocolList.add("IGP");
    protocolList.add("TCP");
    protocolList.add("RSVP");
    protocolList.add("ICMP");
    protocolList.add("WSN");

    portList.add(8080);
    portList.add(80);
    portList.add(4648);
    portList.add(5854);
    portList.add(4242);
  }

  public NetFlowPrettyRandomGenerator(String sourceNode, List<String> fullNodeList,
                                      int maxNodesToTalkTo,
                                      int maxValuePerIteration) {
    this.sourceNode = sourceNode;

    for (int i = 0; i < maxNodesToTalkTo; i++) {
      nodeDestinationList.add(fullNodeList.get(r.nextInt(fullNodeList.size())));
    }

    this.maxValuePerIteration = maxValuePerIteration;
  }

  public NetFlowPojo nextValue() {
    String node = nodeDestinationList.get(r.nextInt(nodeDestinationList.size()));

    return new NetFlowPojo(protocolList.get(r.nextInt(protocolList.size())),
            sourceNode, portList.get(r.nextInt(portList.size())),
            node, portList.get(r.nextInt(portList.size())),
            r.nextInt(maxValuePerIteration), r.nextInt(maxValuePerIteration));
  }
}
