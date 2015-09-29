package com.cloudera.sa.node360.symulator;

import java.util.Random;

/**
 * Created by ted.malaska on 6/4/15.
 */
public class ChartPrettyRandomGenerator {
  static Random r = new Random();
  int currentPathIndex = 0;
  int currentPathLength = r.nextInt(10);
  boolean isPossitive = r.nextBoolean();
  boolean isLong;
  long maxLong;
  int maxChangeInt;
  long currentLong;

  public ChartPrettyRandomGenerator(boolean isLong, long maxLong, int maxChangeInt) {
    this.isLong = isLong;
    this.maxLong = maxLong;
    this.maxChangeInt = maxChangeInt;
    if (maxLong < Integer.MAX_VALUE) {
      currentLong = r.nextInt((int)maxLong);
    } else {
      currentLong = r.nextInt(Integer.MAX_VALUE);
    }
  }

  public String getNextValue() {
    if (currentPathIndex++ >= currentPathLength) {
      currentPathIndex = 0;
      currentPathLength = r.nextInt(10);
      isPossitive = r.nextBoolean();
    }
    int change = r.nextInt(maxChangeInt);
    if (!isPossitive) {
      change = change * -1;
    }
    currentLong += change;
    if (currentLong < 0) {
      currentLong = Math.abs(change);
    } else if (currentLong > maxLong) {
      currentLong = maxLong;
    }

    if (isLong) {
      return Long.toString(currentLong);
    } else {
      return Double.toString((double)currentLong / 100);
    }
  }
}
