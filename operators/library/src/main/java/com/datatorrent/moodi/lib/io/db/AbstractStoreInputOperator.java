/*
 * Copyright (c) 2012-2017 DataTorrent, Inc.
 * All Rights Reserved.
 * The use of this source code is governed by the Limited License located at
 * https://www.datatorrent.com/datatorrent-openview-software-license/
 */
package com.datatorrent.moodi.lib.io.db;

import java.io.IOException;

import com.datatorrent.api.Context.OperatorContext;
import com.datatorrent.api.DefaultOutputPort;
import com.datatorrent.api.InputOperator;
import com.datatorrent.lib.db.Connectable;

/**
 * This is the base implementation of an input adapter which reads from a store.&nbsp;
 * A concrete operator should be created from this skeleton implementation.
 * <p></p>
 * @displayName Abstract Store Input
 * @category Input
 *
 * @param <T> The tuple type
 * @param <S> The store type
 * @since 0.9.3
 */
@org.apache.hadoop.classification.InterfaceStability.Evolving
public abstract class AbstractStoreInputOperator<T, S extends Connectable> implements InputOperator
{
  /**
   * The output port on which tuples read form a store are emitted.
   */
  public final transient DefaultOutputPort<T> outputPort = new DefaultOutputPort<T>();
  protected S store;
  /**
   * Gets the store.
   *
   * @return the store
   */
  public S getStore()
  {
    return store;
  }

  /**
   * Sets the store.
   *
   * @param store
   */
  public void setStore(S store)
  {
    this.store = store;
  }


  @Override
  public void beginWindow(long l)
  {
  }

  @Override
  public void endWindow()
  {
  }

  @Override
  public void setup(OperatorContext t1)
  {
    try {
      store.connect();
    } catch (IOException ex) {
      throw new RuntimeException(ex);
    }
  }

  @Override
  public void teardown()
  {
    try {
      store.disconnect();
    } catch (IOException ex) {
      // ignore
    }
  }
}
