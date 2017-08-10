/*
 * Copyright (c) 2012-2017 DataTorrent, Inc.
 * All Rights Reserved.
 * The use of this source code is governed by the Limited License located at
 * https://www.datatorrent.com/datatorrent-openview-software-license/
 */

package com.datatorrent.moodi.lib.io.fs;

@org.apache.hadoop.classification.InterfaceStability.Evolving
public class FSRecordReaderModule extends org.apache.apex.malhar.lib.fs.FSRecordReaderModule
{
  @Override
  public org.apache.apex.malhar.lib.fs.FSRecordReader createRecordReader()
  {
    FSRecordReader recordReader = new FSRecordReader();
    recordReader.setMode(getMode());
    recordReader.setRecordLength(getRecordLength());
    return recordReader;
  }
}
