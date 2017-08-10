/*
 * Copyright (c) 2012-2017 DataTorrent, Inc.
 * All Rights Reserved.
 * The use of this source code is governed by the Limited License located at
 * https://www.datatorrent.com/datatorrent-openview-software-license/
 */

package com.datatorrent.moodi.io.fs;

import com.datatorrent.api.AutoMetric;

public class FileSplitterInput extends com.datatorrent.lib.io.fs.FileSplitterInput
{
  @AutoMetric
  protected int totalFilesRead = 0;

  @Override
  public void endWindow()
  {
    super.endWindow();
    totalFilesRead += filesProcessed;
  }
}
