package com.singhinderjeet.dedup.util;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.Reader;

public class IoUtils {
    public static String readAsString(Reader reader) throws IOException {
        try {
            StringBuilder sb = new StringBuilder();
            BufferedReader bufReader = new BufferedReader(reader);
            String line = null;
            while ((line = bufReader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            return sb.toString();
        } finally {
            reader.close();
        }
    }

    public static void closeIgnoringExceptions(Closeable closeable) {
      try {
        if (closeable != null) closeable.close();
      } catch (IOException ignored) {}
    }
}
