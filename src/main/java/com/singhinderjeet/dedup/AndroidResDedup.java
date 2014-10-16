package com.singhinderjeet.dedup;

import java.io.File;
import java.io.IOException;

/**
 * Main class to detect and delete duplicates.
 *
 * @author Inderjeet Singh
 */
public final class AndroidResDedup {
  enum Action {
    UPDATE_INTERSECTION() {
      @Override public void run(File first, File second) {
        try {
          AndroidResources firstRes = AndroidResources.parse(first);
          AndroidResources secondRes = AndroidResources.parse(second);
          AndroidResources revisedSecondRes = secondRes.intersect(firstRes);
          if (!secondRes.isSame(revisedSecondRes)) {
            revisedSecondRes.writeToFile(second);
            System.out.println("Updated: " + second.getAbsolutePath());
            linesUpdatedCount += secondRes.getLineCount() - revisedSecondRes.getLineCount();
            ++filesUpdatedCount;
          }
        } catch (IOException ioe) {
          System.err.println("Ignoring " + first.getAbsolutePath() + " Exception: " + ioe.getMessage());
        }
      }
    };
    public abstract void run(File first, File second);
  }

  private static int filesProcessedCount = 0;
  private static int filesUpdatedCount = 0;
  private static int linesUpdatedCount = 0;
  public static void main(String[] args) throws Exception {
    if (args.length != 2) {
      printUsageAndExit();
    }
    Action action = Action.UPDATE_INTERSECTION;
    boolean hasOption = args[0].startsWith("-");
    File originalsDir = new File(args[hasOption ? 1 : 0]);
    File duplicatesDir = new File (args[hasOption ? 2 : 1]);
    dedup(originalsDir, duplicatesDir, action);
    System.out.println("Processed: " + filesProcessedCount + ", Updated: " + filesUpdatedCount);
    System.out.println("#Lines updated: " + linesUpdatedCount);
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
    System.err.println("Usage: dedupAndroid <dir-with-originals> <dir-with-duplicate-resources>");
    System.err.println("Updates the files in dir-with-duplicate-resources by deleting the duplicated resources lines");
    System.exit(-1);
  }
}
