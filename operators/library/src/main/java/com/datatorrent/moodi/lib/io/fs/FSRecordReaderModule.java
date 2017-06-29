package com.datatorrent.moodi.lib.io.fs;

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
