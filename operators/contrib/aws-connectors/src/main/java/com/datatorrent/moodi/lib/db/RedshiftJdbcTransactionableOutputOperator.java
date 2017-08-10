/*
 * Copyright (c) 2012-2017 DataTorrent, Inc.
 * All Rights Reserved.
 * The use of this source code is governed by the Limited License located at
 * https://www.datatorrent.com/datatorrent-openview-software-license/
 */

package com.datatorrent.moodi.lib.db;

import org.apache.apex.malhar.lib.fs.FSRecordCompactionOperator;

import com.datatorrent.api.AutoMetric;
import com.datatorrent.api.Context;

@org.apache.hadoop.classification.InterfaceStability.Evolving
public class RedshiftJdbcTransactionableOutputOperator extends org.apache.apex.malhar.lib.db.redshift.RedshiftJdbcTransactionableOutputOperator
{
  @AutoMetric
  private transient long bytesWrittenPerSec;
  @AutoMetric
  private transient long eventsWrittenPerSec;
  @AutoMetric
  private long totalEventsWritten = 0;
  @AutoMetric
  private long totalBytesWritten = 0;
  private transient long bytesWrittenPerWindow;
  private transient long eventsWrittenPerWindow;
  private transient double windowTimeSec;

  @Override
  protected String generateCopyStatement(FSRecordCompactionOperator.OutputMetaData data)
  {
    bytesWrittenPerWindow += data.getSize();
    if (data instanceof com.datatorrent.moodi.lib.io.fs.FSRecordCompactionOperator.OutputMetaData) {
      eventsWrittenPerWindow += ((com.datatorrent.moodi.lib.io.fs.FSRecordCompactionOperator.OutputMetaData)data).getNoOfTuplesWritten();
    }
    return super.generateCopyStatement(data);
  }

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
    bytesWrittenPerSec = 0;
    bytesWrittenPerWindow = 0;
    eventsWrittenPerSec = 0;
    eventsWrittenPerWindow = 0;
  }

  @Override
  public void endWindow()
  {
    super.endWindow();
    bytesWrittenPerSec = (long)(bytesWrittenPerWindow/windowTimeSec);
    eventsWrittenPerSec = (long)(eventsWrittenPerWindow/windowTimeSec);
    totalBytesWritten += bytesWrittenPerWindow;
    totalEventsWritten += eventsWrittenPerWindow;
  }
}
