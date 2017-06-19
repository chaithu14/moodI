package com.datatorrent.metrics;

import com.amazonaws.services.kinesis.model.Record;

import com.datatorrent.api.AutoMetric;
import com.datatorrent.api.Context;
import com.datatorrent.common.util.Pair;

public class KinesisByteArrayInputOperator extends com.datatorrent.contrib.kinesis.KinesisByteArrayInputOperator
{
  @AutoMetric
  private long bytesReadPerSecond;
  @AutoMetric
  private long bytesRead;
  @AutoMetric
  private long eventsRead;
  @AutoMetric
  private long eventsReadPerSecond;
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
    bytesRead = 0;
    eventsRead = 0;
    eventsReadPerSecond = 0;
  }

  @Override
  public void emitTuple(Pair<String, Record> data)
  {
    super.emitTuple(data);
    bytesRead += data.second.getData().array().length;
    eventsRead++;
  }

  @Override
  public void endWindow()
  {
    super.endWindow();
    bytesReadPerSecond = (long)(bytesRead/windowTimeSec);
    eventsReadPerSecond = (long)(eventsRead/windowTimeSec);
  }
}