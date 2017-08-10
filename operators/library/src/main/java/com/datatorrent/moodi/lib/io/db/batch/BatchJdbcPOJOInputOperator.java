/*
 * Copyright (c) 2012-2017 DataTorrent, Inc.
 * All Rights Reserved.
 * The use of this source code is governed by the Limited License located at
 * https://www.datatorrent.com/datatorrent-openview-software-license/
 */

package com.datatorrent.moodi.lib.io.db.batch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.apex.api.ControlAwareDefaultOutputPort;
import org.apache.hadoop.classification.InterfaceStability.Evolving;

import com.datatorrent.api.Context;
import com.datatorrent.api.DefaultOutputPort;
import com.datatorrent.api.Operator;
import com.datatorrent.batch.BatchControlTuple;
import com.datatorrent.batch.Batchable;

/**
 * A concrete input operator that reads from a database through the JDBC
 * API.<br/>
 * This operator by default uses the fieldInfos and the table name to construct
 * the sql query.<br/>
 * The operator emits start batch before reading any tuples from the database.
 * Once all records are read and there are no more records available, the
 * operator will emit end batch control tuple. The application is shutdown after
 * the shutdown window Id is committed by the DAG.
 */
@Evolving
public class BatchJdbcPOJOInputOperator extends JdbcPOJOInputOperator
    implements Operator.CheckpointNotificationListener, Batchable
{

  protected boolean startBatchEmitted = false;
  protected boolean endBatchEmitted = false;
  protected transient long recordsEmittedTillPreviousWindow;
  private long shutdownWindowId = -1;
  private transient long currentWindowId;

  public final transient ControlAwareDefaultOutputPort<Object> outputPort = new ControlAwareDefaultOutputPort<Object>()
  {
    @Override
    public void setup(Context.PortContext context)
    {
      pojoClass = context.getValue(Context.PortContext.TUPLE_CLASS);
    }
  };

  @Override
  public void beginWindow(long windowId)
  {
    super.beginWindow(windowId);
    currentWindowId = windowId;
    recordsEmittedTillPreviousWindow = tuplesRead;
  }

  @Override
  public void emitTuples()
  {
    if (shouldEmitStartBatch()) {
      emitStartBatchControlTuple();
      startBatchEmitted = true;
    }
    super.emitTuples();
  }

  @Override
  public void endWindow()
  {
    super.endWindow();
    if (shouldEmitEndBatch()) {
      emitEndBatchControlTuple();
      endBatchEmitted = true;
      shutdownWindowId = this.currentWindowId;
    }
  }

  @Override
  public void emitStartBatchControlTuple()
  {
    if (getOutputPort() instanceof ControlAwareDefaultOutputPort) {
      BatchControlTuple startBatchControlTuple = new BatchControlTuple.StartBatchControlTupleImpl();
      ControlAwareDefaultOutputPort<Object> output = (ControlAwareDefaultOutputPort<Object>)getOutputPort();
      output.emitControl(startBatchControlTuple);
    } else {
      LOG.error("Output port is not control aware, skipped emitting start batch control tuple");
    }
  }

  @Override
  public void emitEndBatchControlTuple()
  {
    if (getOutputPort() instanceof ControlAwareDefaultOutputPort) {
      BatchControlTuple endBatchControlTuple = new BatchControlTuple.EndBatchControlTupleImpl();
      ControlAwareDefaultOutputPort<Object> output = (ControlAwareDefaultOutputPort<Object>)getOutputPort();
      output.emitControl(endBatchControlTuple);
    } else {
      LOG.error("Output port is not control aware, skipped emitting end batch control tuple");
    }
  }

  /**
   * decides and returns whether start batch control tuple should be emitted or
   * not
   *
   * @return
   */
  protected boolean shouldEmitStartBatch()
  {
    return !startBatchEmitted;
  }

  /**
   * decides and returns whether end batch control tuple should be emitted or
   * not
   *
   * @return
   */
  protected boolean shouldEmitEndBatch()
  {
    //If no tuples were emitted in the emitTuples call
    return (!endBatchEmitted && recordsEmittedTillPreviousWindow == tuplesRead);
  }

  protected DefaultOutputPort<Object> getOutputPort()
  {
    return this.outputPort;
  }

  @Override
  public void checkpointed(long windowId)
  {
  }

  @Override
  public void beforeCheckpoint(long windowId)
  {
  }

  @Override
  public void committed(long windowId)
  {
    //Shutdown the application after all records are read.
    if (shutdownWindowId != -1 && windowId > shutdownWindowId) {
      throw new ShutdownException();
    }
  }

  public static final Logger LOG = LoggerFactory.getLogger(BatchJdbcPOJOInputOperator.class);
}
