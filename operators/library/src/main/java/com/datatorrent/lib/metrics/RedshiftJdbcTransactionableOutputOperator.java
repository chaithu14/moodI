package com.datatorrent.lib.metrics;

import org.apache.apex.malhar.lib.fs.FSRecordCompactionOperator;

import com.datatorrent.api.AutoMetric;
import com.datatorrent.api.Context;

public class RedshiftJdbcTransactionableOutputOperator extends org.apache.apex.malhar.lib.db.redshift.RedshiftJdbcTransactionableOutputOperator
{
  @AutoMetric
  private long bytesWrittenPerSec;
  @AutoMetric
  private long eventsWrittenPerSec;
  @AutoMetric
  private long eventsWritten = 0;
  @AutoMetric
  private long bytesWritten = 0;
  private long bytesWrittenPerWindow;
  private long eventsWrittenPerWindow;
  private double windowTimeSec;

  @Override
  protected String generateCopyStatement(FSRecordCompactionOperator.OutputMetaData data)
  {
    bytesWrittenPerWindow += data.getSize();
    if (data instanceof com.datatorrent.lib.metrics.FSRecordCompactionOperator.OutputMetaData) {
      eventsWrittenPerWindow += ((com.datatorrent.lib.metrics.FSRecordCompactionOperator.OutputMetaData)data).getNoOfTuplesWritten();
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
    bytesWritten += bytesWrittenPerWindow;
    eventsWritten += eventsWrittenPerWindow;
  }
}
