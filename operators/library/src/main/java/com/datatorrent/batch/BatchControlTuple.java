/*
 * Copyright (c) 2012-2017 DataTorrent, Inc.
 * All Rights Reserved.
 * The use of this source code is governed by the Limited License located at
 * https://www.datatorrent.com/datatorrent-openview-software-license/
 */

package com.datatorrent.batch;

import org.apache.apex.api.operator.ControlTuple;
import org.apache.hadoop.classification.InterfaceStability.Evolving;

@Evolving
public interface BatchControlTuple extends ControlTuple
{
  @Evolving
  interface StartBatchControlTuple extends BatchControlTuple
  {
  }

  @Evolving
  interface EndBatchControlTuple extends BatchControlTuple
  {
  }

  public static class StartBatchControlTupleImpl implements StartBatchControlTuple
  {
    @Override
    public DeliveryType getDeliveryType()
    {
      return DeliveryType.IMMEDIATE;
    }
  }

  public static class EndBatchControlTupleImpl implements EndBatchControlTuple
  {
    @Override
    public DeliveryType getDeliveryType()
    {
      return DeliveryType.END_WINDOW;
    }
  }
}
