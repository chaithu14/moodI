/*
 * Copyright (c) 2012-2017 DataTorrent, Inc.
 * All Rights Reserved.
 * The use of this source code is governed by the Limited License located at
 * https://www.datatorrent.com/datatorrent-openview-software-license/
 */

package com.datatorrent.batch;

/**
 * When an operator implements this interface, it implies that it will support
 * emitting start batch and end batch control tuples
 *
 */
@org.apache.hadoop.classification.InterfaceStability.Evolving
public interface Batchable
{
  /**
   * Emit a start batch control tuple on the control aware output port
   */
  public void emitStartBatchControlTuple();

  /**
   * Emit a start batch control tuple on the control aware output port
   */
  public void emitEndBatchControlTuple();
}
