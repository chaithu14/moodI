/*
 * Copyright (c) 2012-2017 DataTorrent, Inc.
 * All Rights Reserved.
 * The use of this source code is governed by the Limited License located at
 * https://www.datatorrent.com/datatorrent-openview-software-license/
 */

package com.datatorrent.io.fs.utils;

import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.hadoop.classification.InterfaceStability.Evolving;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

import com.datatorrent.lib.io.fs.AbstractFileInputOperator.DirectoryScanner;

/**
 * The class that is used to scan for new files in the directory specified. It
 * provides an abstract method isScanComplete() which can be used to indicate
 * the end of input to the operator using this scanner implementation.
 */
@SuppressWarnings("serial")
@Evolving
public abstract class Scanner extends DirectoryScanner
{
  public abstract boolean isScanComplete();

  /**
   * This scanner will perform a single scan for the specified directory and end
   * the scan after one call
   */
  public static class SingleScanDirectoryScanner extends Scanner
  {
    private boolean scanDone;

    @Override
    public LinkedHashSet<Path> scan(FileSystem fs, Path filePath, Set<String> consumedFiles)
    {
      LinkedHashSet<Path> files = super.scan(fs, filePath, consumedFiles);
      scanDone = true;
      return files;
    }

    @Override
    public boolean isScanComplete()
    {
      return scanDone;
    }
  }

  public static class NoOpScanner extends Scanner
  {
    @Override
    public boolean isScanComplete()
    {
      return true;
    }

    @Override
    public LinkedHashSet<Path> scan(FileSystem fs, Path filePath, Set<String> consumedFiles)
    {
      return null;
    }
  }
}
