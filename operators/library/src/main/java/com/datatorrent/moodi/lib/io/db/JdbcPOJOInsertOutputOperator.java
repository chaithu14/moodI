/*
 * Copyright (c) 2012-2017 DataTorrent, Inc.
 * All Rights Reserved.
 * The use of this source code is governed by the Limited License located at
 * https://www.datatorrent.com/datatorrent-openview-software-license/
 */

package com.datatorrent.moodi.lib.io.db;

import com.datatorrent.api.AutoMetric;
import com.datatorrent.api.Context;

/**
 * JdbcPOJOInsertOutputOperator extends from com.datatorrent.lib.db.jdbc.JdbcPOJOInsertOutputOperator.
 * It emits the bytesWrittenPerSecond, bytesWritten, totalEventsWritten, eventsWrittenPerSecond metrics.
 *
 */
@org.apache.hadoop.classification.InterfaceStability.Evolving
public class JdbcPOJOInsertOutputOperator extends com.datatorrent.lib.db.jdbc.JdbcPOJOInsertOutputOperator
{
  @AutoMetric
  private long totalEventsWritten = 0;

  @AutoMetric
  private long totalBadEvents = 0;

  @AutoMetric
  private transient long eventsWrittenPerSecond;

  private transient long eventsWrittenPerWindow;
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
    eventsWrittenPerWindow = 0;
    eventsWrittenPerSecond = 0;
  }

  @Override
  public void processTuple(Object tuple)
  {
    eventsWrittenPerWindow += 1;
    super.processTuple(tuple);
    /*
     * due to issue in malhar operator AbstractTransactionalOutputOperator, below calculation is required
     * Once the issue is fixed in operator below code would be changed.
     */
    if (getTuplesWrittenSuccessfully() > 0) {
      totalBadEvents += eventsWrittenPerWindow - getTuplesWrittenSuccessfully();
      eventsWrittenPerWindow = getTuplesWrittenSuccessfully();
    }
  }

  @Override
  public void endWindow()
  {
    super.endWindow();
    eventsWrittenPerSecond = (long)(eventsWrittenPerWindow/windowTimeSec);
    totalEventsWritten += eventsWrittenPerWindow;
  }
}

