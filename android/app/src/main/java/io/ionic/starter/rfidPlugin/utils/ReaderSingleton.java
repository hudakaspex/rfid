package io.ionic.starter.rfidPlugin.utils;

import com.payne.reader.bean.config.AntennaCount;
import com.payne.reader.process.ReaderImpl;

public class ReaderSingleton {
  private static ReaderSingleton instance;
  private final ReaderImpl reader;

  // Private constructor to prevent instantiation
  private ReaderSingleton() {
    // Initialize the ReaderImpl with SINGLE_CHANNEL
    reader = (ReaderImpl) ReaderImpl.create(AntennaCount.SINGLE_CHANNEL);
  }

  // Public method to provide access to the singleton instance
  public static synchronized ReaderSingleton getInstance() {
    if (instance == null) {
      instance = new ReaderSingleton();
    }
    return instance;
  }

  // Method to get the ReaderImpl instance
  public ReaderImpl getReader() {
    return reader;
  }
}
