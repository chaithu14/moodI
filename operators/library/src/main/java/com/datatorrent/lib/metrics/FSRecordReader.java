package com.datatorrent.lib.metrics;

import java.io.IOException;

import com.datatorrent.api.AutoMetric;
import com.datatorrent.api.Context;
import com.datatorrent.lib.io.block.BlockMetadata;
import com.datatorrent.lib.io.block.ReaderContext;

public class FSRecordReader extends org.apache.apex.malhar.lib.fs.FSRecordReader
{
  @AutoMetric
  private long bytesRead = 0;
  @AutoMetric
  private long bytesReadPerSec;
  @AutoMetric
  private long eventsRead = 0;
  @AutoMetric
  private long eventsReadPerSec;
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
    bytesReadPerSec = 0;
    eventsReadPerSec = 0;
    bytesReadPerWindow = 0;
    eventsReadPerWindow = 0;
  }

  @Override
  protected void readBlock(BlockMetadata blockMetadata) throws IOException
  {
    readerContext.initialize(stream, blockMetadata, consecutiveBlock);
    ReaderContext.Entity entity;
    while ((entity = readerContext.next()) != null) {

      counters.getCounter(ReaderCounterKeys.BYTES).add(entity.getUsedBytes());

      byte[] record = entity.getRecord();

      if (record != null) {
        counters.getCounter(ReaderCounterKeys.RECORDS).increment();
        records.emit(record);
        bytesReadPerWindow += record.length;
        eventsReadPerWindow++;
      }
    }
  }

  @Override
  public void endWindow()
  {
    super.endWindow();
    bytesReadPerSec = (long)(bytesReadPerWindow/windowTimeSec);
    eventsReadPerWindow = (long)(eventsReadPerWindow/windowTimeSec);
    bytesRead += bytesReadPerWindow;
    eventsRead += eventsReadPerWindow;
  }
}
