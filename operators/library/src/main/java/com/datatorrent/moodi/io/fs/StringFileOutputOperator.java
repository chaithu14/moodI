/*
 * Copyright (c) 2012-2017 DataTorrent, Inc.
 * All Rights Reserved.
 * The use of this source code is governed by the Limited License located at
 * https://www.datatorrent.com/datatorrent-openview-software-license/
 */

package com.datatorrent.moodi.io.fs;

import org.apache.apex.malhar.lib.fs.GenericFileOutputOperator;

import com.datatorrent.api.AutoMetric;
import com.datatorrent.api.Context;

@org.apache.hadoop.classification.InterfaceStability.Evolving
public class StringFileOutputOperator extends GenericFileOutputOperator.StringFileOutputOperator
{
  @AutoMetric
  private long totalBytes;
  @AutoMetric
  private long totalEventsWritten = 0;
  @AutoMetric
  private transient long bytesWrittenPerSecond;
  @AutoMetric
  private transient long eventsWrittenPerSecond;
  private transient long eventsWrittenPerWindow;
  private transient long bytesWrittenPerWindow;
  private transient double windowTimeSec;

  @Override
  public void beginWindow(long windowId)
  {
    super.beginWindow(windowId);
    bytesWrittenPerSecond = 0;
    eventsWrittenPerSecond = 0;
    bytesWrittenPerWindow = 0;
    eventsWrittenPerWindow = 0;
  }

  @Override
  public void endWindow()
  {
    super.endWindow();
    bytesWrittenPerSecond = (long)(bytesWrittenPerWindow/windowTimeSec);
    eventsWrittenPerSecond = (long)(eventsWrittenPerWindow/windowTimeSec);
    totalBytes += bytesWrittenPerWindow;
    totalEventsWritten += eventsWrittenPerWindow;
  }

  @Override
  public void setup(Context.OperatorContext context)
  {
    super.setup(context);
    windowTimeSec = (context.getValue(Context.OperatorContext.APPLICATION_WINDOW_COUNT) *
      context.getValue(Context.DAGContext.STREAMING_WINDOW_SIZE_MILLIS) * 1.0) / 1000.0;
  }

  @Override
  protected byte[] getBytesForTuple(String tuple)
  {
    byte[] result = super.getBytesForTuple(tuple);
    bytesWrittenPerWindow += result.length;
    eventsWrittenPerWindow++;
    return result;
  }
}
