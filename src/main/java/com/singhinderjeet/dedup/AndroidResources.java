package com.singhinderjeet.dedup;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.singhinderjeet.dedup.util.IoUtils;

public class AndroidResources {

  public static AndroidResources parse(File first) throws IOException {
    BufferedReader bufReader = null;
    try {
      bufReader = new BufferedReader(new FileReader(first));
      List<String> lines = new ArrayList<String>();
      String line = null;
      while ((line = bufReader.readLine()) != null) {
        lines.add(line);
      }
      return new AndroidResources(lines);
    } finally {
      IoUtils.closeIgnoringExceptions(bufReader);
    }
  }

  private final List<String> lines;

  public AndroidResources(List<String> lines) {
    this.lines = lines;
  }

  /**
   * Returns the intersection of the current resources with other. The intersection is
   * defined as the set of resources in the current minus the exact duplicates in other.
   */
  public AndroidResources intersect(AndroidResources other) {
    List<String> intersection = new ArrayList<String>();
    for (String line : lines) {
      boolean skip = false;
      if (isResourceLine(line)) {
        skip = other.lines.contains(line);
      }
      if (!skip) intersection.add(line);
    }
    return new AndroidResources(intersection);
  }

  public boolean isSame(AndroidResources other) {
    if (lines.size() != other.lines.size()) return false;
    for (int i = 0; i < lines.size(); ++i) {
      if (!lines.get(i).equals(other.lines.get(i))) return false;
    }
    return true;
  }

  private static boolean isResourceLine(String line) {
    line = line.trim();
    if (line.isEmpty()
        || line.startsWith("<?") // xml header
        || line.startsWith("<!") // comment
        || line.startsWith("/>") // ending tag
        || line.startsWith("<string-array")
        || line.startsWith("<resources")) {
      return false;
    }
    if ((line.startsWith("<string") && line.endsWith("/string>")) 
        || (line.startsWith("<drawable") && line.endsWith("/drawable>"))
        || (line.startsWith("<dimen") && line.endsWith("/dimen>"))
        || (line.startsWith("<integer") && line.endsWith("/integer>"))
        || (line.startsWith("<color") && line.endsWith("/color>"))) {
      return true;
    }
    return false;
  }

  public void writeToFile(File file) {
    BufferedWriter writer = null;
    try {
      writer = new BufferedWriter(new FileWriter(file));
      for (String line : lines) {
        writer.append(line);
        writer.newLine();
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    } finally {
      IoUtils.closeIgnoringExceptions(writer);
    }
  }

  public int getLineCount() {
    return lines.size();
  }
}
