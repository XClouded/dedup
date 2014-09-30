package com.singhinderjeet.dedup;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileWriter;

import org.junit.Test;

/**
 * Unit test for {@link Main}.
 *
 * @author Inderjeet Singh
 */
public class MainTest {

  @Test
  public void testSameContents() throws Exception {
    File first = File.createTempFile("dedup-first", "txt");
    first.deleteOnExit();
    File second = File.createTempFile("dedup-second", "txt");
    second.deleteOnExit();
    writeContents(first, "abcd");
    writeContents(second, "abcd");
    assertTrue(Main.sameContents(first, second));
    writeContents(second, "defg");
    assertFalse(Main.sameContents(first, second));
  }

  private void writeContents(File file, String contents) throws Exception {
    FileWriter writer = new FileWriter(file);
    writer.append(contents);
    writer.close();
  }
}
