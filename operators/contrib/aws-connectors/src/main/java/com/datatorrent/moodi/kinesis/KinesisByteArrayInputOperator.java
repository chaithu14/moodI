/*
 * Copyright (c) 2012-2017 DataTorrent, Inc.
 * All Rights Reserved.
 * The use of this source code is governed by the Limited License located at
 * https://www.datatorrent.com/datatorrent-openview-software-license/
 */

package com.datatorrent.moodi.kinesis;

import com.amazonaws.services.kinesis.model.Record;

import com.datatorrent.api.AutoMetric;
import com.datatorrent.api.Context;
import com.datatorrent.common.util.Pair;

/**
 * KinesisByteArrayInputOperator extends from com.datatorrent.contrib.kinesis.KinesisByteArrayInputOperator.
 * It emits the bytesReadPerSecond, bytesRead, eventsRead, eventsReadPerSecond metrics.
 *
 */
@org.apache.hadoop.classification.InterfaceStability.Evolving
public class KinesisByteArrayInputOperator extends com.datatorrent.contrib.kinesis.KinesisByteArrayInputOperator
{
  @AutoMetric
  private long bytesReadPerSecond;
  @AutoMetric
  private long totalBytesRead = 0;
  @AutoMetric
  private long totalEventsRead = 0;
  @AutoMetric
  private long eventsReadPerSecond;
  private long eventsReadPerWindow;
  private long bytesReadPerWindow;
  private double windowTimeSec;

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
    bytesReadPerSecond = 0;
    bytesReadPerWindow = 0;
    eventsReadPerWindow = 0;
    eventsReadPerSecond = 0;
  }

  @Override
  public void emitTuple(Pair<String, Record> data)
  {
    super.emitTuple(data);
    bytesReadPerWindow += data.second.getData().array().length;
    eventsReadPerWindow++;
  }

  @Override
  public void endWindow()
  {
    super.endWindow();
    bytesReadPerSecond = (long)(bytesReadPerWindow/windowTimeSec);
    eventsReadPerSecond = (long)(eventsReadPerWindow/windowTimeSec);
    totalBytesRead += bytesReadPerWindow;
    totalEventsRead += eventsReadPerWindow;
  }
}
