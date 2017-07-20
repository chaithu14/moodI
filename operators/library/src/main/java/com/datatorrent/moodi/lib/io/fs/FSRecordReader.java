package com.datatorrent.moodi.lib.io.fs;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.datatorrent.api.AutoMetric;
import com.datatorrent.api.Context;
import com.datatorrent.api.Stats;
import com.datatorrent.lib.io.block.BlockMetadata;
import com.datatorrent.lib.io.block.ReaderContext;

public class FSRecordReader extends org.apache.apex.malhar.lib.fs.FSRecordReader
{
  @AutoMetric
  private long totalBytesRead = 0;
  @AutoMetric
  private transient long bytesReadPerSec;
  @AutoMetric
  private long totalEventsRead = 0;
  @AutoMetric
  private transient long eventsReadPerSec;
  private boolean collectStats;
  private transient long nextMillis;
  private transient long eventsReadPerWindow;
  private transient long bytesReadPerWindow;
  private transient double windowTimeSec;

  public FSRecordReader()
  {
    super();
    collectStats = true;
  }

  @Override
  public void setup(Context.OperatorContext context)
  {
    super.setup(context);
    windowTimeSec = (context.getValue(Context.OperatorContext.APPLICATION_WINDOW_COUNT) *
        context.getValue(Context.DAGContext.STREAMING_WINDOW_SIZE_MILLIS) * 1.0) / 1000.0;
  }

  @Override
  public Response processStats(BatchedOperatorStats stats)
  {
    response.repartitionRequired = false;
    if (!collectStats) {
      return response;
    }

    List<Stats.OperatorStats> lastWindowedStats = stats.getLastWindowedStats();
    if (lastWindowedStats != null && lastWindowedStats.size() > 0) {
      Stats.OperatorStats lastStats = lastWindowedStats.get(lastWindowedStats.size() - 1);
      if (lastStats.inputPorts.size() > 0) {
        int queueSize = lastStats.inputPorts.get(0).queueSize;
        // Don't count the control tuples
        if (queueSize > 0) {
          queueSize = queueSize - 1;
        }
        backlogPerOperator.put(stats.getOperatorId(), queueSize);
      }
    }

    if (System.currentTimeMillis() < nextMillis) {
      return response;
    }

    long totalBacklog = 0;
    for (Map.Entry<Integer, Integer> backlog : backlogPerOperator.entrySet()) {
      totalBacklog += backlog.getValue();
    }

    if (nextMillis == 0) {
      // Initial Partition
      if (partitionCount != minReaders && totalBacklog == partitionCount) {
        partitionCount = minReaders;
        response.repartitionRequired = true;
        nextMillis = System.currentTimeMillis() + intervalMillis;
        return response;
      }
    }

    nextMillis = System.currentTimeMillis() + intervalMillis;

    backlogPerOperator.clear();

    if (totalBacklog == partitionCount) {
      return response; //do not repartition
    }

    int newPartitionCount;
    if (totalBacklog > maxReaders) {
      newPartitionCount = maxReaders;
    } else if (totalBacklog < minReaders) {
      newPartitionCount = minReaders;
    } else {
      newPartitionCount = getAdjustedCount(totalBacklog);
    }

    if (newPartitionCount == partitionCount) {
      return response; //do not repartition
    }

    partitionCount = newPartitionCount;
    response.repartitionRequired = true;

    return response;
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
    totalBytesRead += bytesReadPerWindow;
    totalEventsRead += eventsReadPerWindow;
  }

  @Override
  public void setCollectStats(boolean collectStats)
  {
    this.collectStats = collectStats;
  }
}
