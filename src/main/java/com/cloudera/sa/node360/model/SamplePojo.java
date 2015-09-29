package com.cloudera.sa.node360.model;

import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ted.malaska on 6/3/15.
 */
public class SamplePojo  implements java.io.Serializable{
  String key;
  String value;
  String max;
  String status;

  public SamplePojo(String key, String value, String max, String status) {
    this.key = key;
    this.value = value;
    this.max = max;
    this.status = status;
  }

  public SamplePojo(String key, String value, String max) {
    this.key = key;
    this.value = value;
    this.max = max;
  }

  public SamplePojo(String key, String value) {
    this.key = key;
    this.value = value;
  }

  public SamplePojo(String pojo) {
    System.out.println("pojo:" + pojo);
    int colonIndex = pojo.indexOf(':');
    key = pojo.substring(0, colonIndex);
    String[] parts = StringUtils.split(pojo.substring(colonIndex + 1), ';');
    value = parts[0];
    if (parts.length > 1) { max = parts[1]; }
    if (parts.length > 2) { status = parts[2]; }
  }

  public String toString() {
    if (status != null) {
      return key + ":" + value + ";" + max + ";" + status;
    } else if (max != null) {
      return key + ":" + value + ";" + max;
    } else {
      return key + ":" + value;
    }
  }

  static public String toString(List<SamplePojo> pojos) {
    StringBuilder strBuilder = new StringBuilder();
    boolean isFirst = true;
    for (SamplePojo pojo: pojos) {
      if (isFirst) {
        isFirst = false;
        strBuilder.append(pojo);
      } else {
        strBuilder.append("," + pojo);
      }
    }
    return strBuilder.toString();
  }

  static public List<SamplePojo> readListOfSamplePojos(String listString) {
    final String[] splits = StringUtils.split(listString, ',');
    List<SamplePojo> pojos = new ArrayList<SamplePojo>();
    for (String split: splits) {
      pojos.add(new SamplePojo(split));
    }
    return pojos;
  }

  public String getKey() {
    return key;
  }

  public void setKey(String key) {
    this.key = key;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  public String getMax() {
    return max;
  }

  public void setMax(String max) {
    this.max = max;
  }
}
