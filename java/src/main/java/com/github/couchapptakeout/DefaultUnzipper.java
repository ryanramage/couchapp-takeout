/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.github.couchapptakeout;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.bushe.swing.event.EventBus;

/**
 *
 * @author ryan.ramage
 */
public class DefaultUnzipper implements Unzipper {

    @Override
    public void doUnzip(File zipfile, File directory)throws IOException {
       ZipFile zfile = new ZipFile(zipfile);
        Enumeration<? extends ZipEntry> entries = zfile.entries();
        int total = zfile.size();
        int count = 0;

        while (entries.hasMoreElements()) {
          EventBus.publish(new LoadingMessage(-1, -1, null, count++, total, "Unpacking..." ));
          ZipEntry entry = entries.nextElement();
          File file = new File(directory, entry.getName());
          if (entry.isDirectory()) {
            file.mkdirs();
          } else {
            file.getParentFile().mkdirs();
            InputStream in = zfile.getInputStream(entry);
            try {
              copy(in, file);
              file.setReadable(true);
              file.setWritable(true);
              file.setExecutable(true);
            } finally {
              in.close();
            }
          }
        }

    }

  private static void copy(InputStream in, OutputStream out) throws IOException {
    byte[] buffer = new byte[1024];
    while (true) {
      int readCount = in.read(buffer);
      if (readCount < 0) {
        break;
      }
      out.write(buffer, 0, readCount);
    }
  }

  private static void copy(File file, OutputStream out) throws IOException {
    InputStream in = new FileInputStream(file);
    try {
      copy(in, out);
    } finally {
      in.close();
    }
  }

  private static void copy(InputStream in, File file) throws IOException {
    OutputStream out = new FileOutputStream(file);
    try {
      copy(in, out);
    } finally {
      out.close();
    }
  }
}
