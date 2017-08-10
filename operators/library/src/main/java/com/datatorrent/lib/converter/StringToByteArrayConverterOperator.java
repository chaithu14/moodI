/*
 * Copyright (c) 2012-2017 DataTorrent, Inc.
 * All Rights Reserved.
 * The use of this source code is governed by the Limited License located at
 * https://www.datatorrent.com/datatorrent-openview-software-license/
 */

package com.datatorrent.lib.converter;

import com.datatorrent.api.DefaultInputPort;
import com.datatorrent.api.DefaultOutputPort;
import com.datatorrent.common.util.BaseOperator;

/**
 * This operator converts String to Byte array
 */
@org.apache.hadoop.classification.InterfaceStability.Evolving
public class StringToByteArrayConverterOperator extends BaseOperator implements Converter<String, byte[]>
{
  @Override
  public byte[] convert(String s)
  {
    return s.getBytes();
  }

  //Input port which accepts String
  public final transient DefaultInputPort<String> input = new DefaultInputPort<String>()
  {
    @Override
    public void process(String tuple)
    {
      output.emit(convert(tuple));
    }
  };

  public final transient DefaultOutputPort<byte[]> output = new DefaultOutputPort<>();
}
