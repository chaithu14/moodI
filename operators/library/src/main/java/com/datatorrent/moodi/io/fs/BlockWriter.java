/*
 * Copyright (c) 2012-2017 DataTorrent, Inc.
 * All Rights Reserved.
 * The use of this source code is governed by the Limited License located at
 * https://www.datatorrent.com/datatorrent-openview-software-license/
 */

package com.datatorrent.moodi.io.fs;

import com.datatorrent.api.AutoMetric;
import com.datatorrent.api.Context;
import com.datatorrent.lib.io.block.AbstractBlockReader;
import com.datatorrent.netlet.util.Slice;

@org.apache.hadoop.classification.InterfaceStability.Evolving
public class BlockWriter extends com.datatorrent.lib.io.block.BlockWriter
{
  @AutoMetric
  protected  transient long totalBytesWritten = 0;
  @AutoMetric
  protected transient long bytesWrittenPerSecond = 0;

  protected transient long bytesWrittenPerWindow = 0;
  private transient double windowTimeSec;

  @Override
  public void setup(Context.OperatorContext context)
  {
    super.setup(context);
    windowTimeSec = (context.getValue(Context.OperatorContext.APPLICATION_WINDOW_COUNT) *
      context.getValue(Context.DAGContext.STREAMING_WINDOW_SIZE_MILLIS) * 1.0) / 1000.0;
  }

  @Override
  public void beginWindow(long windowId)
  {
    super.beginWindow(windowId);
    bytesWrittenPerWindow = 0;
  }

  @Override
  protected byte[] getBytesForTuple(AbstractBlockReader.ReaderRecord<Slice> tuple)
  {
    byte[] result = super.getBytesForTuple(tuple);
    bytesWrittenPerWindow += result.length;
    return result;
  }

  @Override
  public void endWindow()
  {
    super.endWindow();
    bytesWrittenPerSecond = (long)(bytesWrittenPerWindow/windowTimeSec);
    totalBytesWritten = fileCounters.getCounter(Counters.TOTAL_BYTES_WRITTEN).longValue();
  }
}
