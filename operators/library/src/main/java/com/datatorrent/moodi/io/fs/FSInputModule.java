/*
 * Copyright (c) 2012-2017 DataTorrent, Inc.
 * All Rights Reserved.
 * The use of this source code is governed by the Limited License located at
 * https://www.datatorrent.com/datatorrent-openview-software-license/
 */

package com.datatorrent.moodi.io.fs;

@org.apache.hadoop.classification.InterfaceStability.Evolving
public class FSInputModule extends com.datatorrent.lib.io.fs.FSInputModule
{
  @Override
  public FileSplitterInput createFileSplitter()
  {
    return new FileSplitterInput();
  }

  @Override
  public FSSliceReader createBlockReader()
  {
    return new FSSliceReader();
  }
}
