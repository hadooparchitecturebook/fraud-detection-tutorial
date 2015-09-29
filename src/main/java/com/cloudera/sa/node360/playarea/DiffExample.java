package com.cloudera.sa.node360.playarea;

import difflib.Delta;
import difflib.DiffUtils;
import difflib.Patch;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by ted.malaska on 6/4/15.
 */
public class DiffExample {
  public static void main(String args[]) {
    List<String> original = new ArrayList<String>();
    List<String> revised  = new ArrayList<String>();

    original.add("foobar1");
    original.add("foobar2");
    original.add("foobar3");
    original.add("foobar4");
    original.add("foobar5");

    revised.add("foobar1");
    revised.add("foobar2");
    revised.add("foobar3");
    revised.add("foobar3.1");
    revised.add("foobar4");
    revised.add("foobar5.1");


    // Compute diff. Get the Patch object. Patch is the container for computed deltas.
    Patch<String> patch = DiffUtils.diff(original, revised);

    final List<Delta<String>> deltas = patch.getDeltas();

    for (Delta<String> delta: deltas) {
      System.out.println(delta);
    }
  }
}
