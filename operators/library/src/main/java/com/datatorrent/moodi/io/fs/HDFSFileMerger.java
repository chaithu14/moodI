/*
 * Copyright (c) 2012-2017 DataTorrent, Inc.
 * All Rights Reserved.
 * The use of this source code is governed by the Limited License located at
 * https://www.datatorrent.com/datatorrent-openview-software-license/
 */

package com.datatorrent.moodi.io.fs;

import java.io.IOException;

import org.apache.hadoop.fs.Path;

import com.datatorrent.api.AutoMetric;

@org.apache.hadoop.classification.InterfaceStability.Evolving
public class HDFSFileMerger extends com.datatorrent.lib.io.fs.HDFSFileMerger
{

  @AutoMetric
  protected int totalCompletedFiles = 0;

  @Override
  protected void moveToFinalFile(Path tempOutFilePath, Path destination) throws IOException
  {
    super.moveToFinalFile(tempOutFilePath, destination);
    totalCompletedFiles++;
  }
}
