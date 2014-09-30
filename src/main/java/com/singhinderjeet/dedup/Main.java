package com.singhinderjeet.dedup;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * Main class to detect and delete duplicates.
 *
 * @author Inderjeet Singh
 */
public final class Main {
  enum Action {
    LIST() {
      @Override public void run(File first, File second) {
        String firstStr = getDisplayString(first);
        String secondStr = getDisplayString(second);
        System.out.println(firstStr + ", " + secondStr);
      }
      private String getDisplayString(File file) {
        if (!file.exists()) return "-";
        StringBuilder sb = new StringBuilder(file.getAbsolutePath());
        if (file.isDirectory()) sb.append(" [Dir]");
        return sb.toString();
      }
    },
    DELETE_IF_SAME() {
      @Override public void run(File first, File second) {
        if (sameContents(first, second)) {
          boolean success = second.delete();
          if (success) ++filesDeletedCount;
          System.out.println((success ? "deleted " : "couldn't delete ") + second.getAbsolutePath());
        } else {
          System.out.println("Ignored different files: " + first.getAbsolutePath() + ", " + second.getAbsolutePath());
        }
      }
    },
    DELETE_UNCONDITIONALLY() {
      @Override public void run(File first, File second) {
        if (first.isDirectory() || second.isDirectory()) return;
        boolean sameContents = sameContents(first, second);
        boolean success = second.delete();
        if (success) ++filesDeletedCount;
        System.out.println((success ? "deleted " : "couldn't delete [") + (sameContents ? "same " : "different ") + "contents] " + second.getAbsolutePath());
      }
    };
    public abstract void run(File first, File second);
  }

  private static int filesProcessedCount = 0;
  private static int filesDeletedCount = 0;
  public static void main(String[] args) throws Exception {
    if (args.length < 2 || args.length > 3) {
      printUsageAndExit();
    }
    Action action = Action.LIST;
    if (args[0].equals("-d")) {
      action = Action.DELETE_IF_SAME;
    } else if (args[0].equals("-D")) {
      action = Action.DELETE_UNCONDITIONALLY;
    }
    if (action == Action.LIST && args.length > 2) {
      printUsageAndExit();
    }
    boolean hasOption = args[0].startsWith("-");
    File originalsDir = new File(args[hasOption ? 1 : 0]);
    File duplicatesDir = new File (args[hasOption ? 2 : 1]);
    dedup(originalsDir, duplicatesDir, action);
    System.out.println("Processed: " + filesProcessedCount + ", Deleted: " + filesDeletedCount);
  }

  private static void dedup(File first, File second, Action action) {
    if (first.isDirectory() && second.isDirectory()) {
      for (String firstEntryName : first.list()) {
        File firstEntry = new File(first, firstEntryName);
        File secondEntry = new File(second, firstEntryName);
        dedup(firstEntry, secondEntry, action);
      }
    } else {
      action.run(first, second);
      ++filesProcessedCount;
    }
  }

  private static void printUsageAndExit() {
    System.err.println("Usage: dedup [-dD] <dir-with-originals> <dir-with-duplicates>");
    System.err.println("Only lists the duplicates if -d or -D are not provided.");
    System.err.println("-d to delete if the two files with same name are exactly the same in contents.");
    System.err.println("-D to delete even if the two files with same name are different.");
    System.exit(-1);
  }

  static boolean sameContents(File first, File second) {
    BufferedReader reader1 = null;
    BufferedReader reader2 = null;
    try {
      reader1 = new BufferedReader(new FileReader(first));
      reader2 = new BufferedReader(new FileReader(second));

      String line1 = null;
      String line2 = null;
      int returnValue = 1;
      while (returnValue == 1
          && (line1 = reader1.readLine()) != null
          && (line2 = reader2.readLine()) != null) {
        if (!line1.equalsIgnoreCase(line2)) return false;
      }
      return true;
    } catch (IOException e) {
      return false; // Not same
    } finally {
      closeIgnoringExceptions(reader1);
      closeIgnoringExceptions(reader2);
    }
  }

  private static void closeIgnoringExceptions(Closeable closeable) {
    try {
      closeable.close();
    } catch (Exception ignored) {}
  }
}
