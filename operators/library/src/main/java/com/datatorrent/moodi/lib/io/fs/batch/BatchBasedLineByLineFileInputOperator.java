/*
 * Copyright (c) 2012-2017 DataTorrent, Inc.
 * All Rights Reserved.
 * The use of this source code is governed by the Limited License located at
 * https://www.datatorrent.com/datatorrent-openview-software-license/
 */

package com.datatorrent.moodi.lib.io.fs.batch;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.apex.api.ControlAwareDefaultOutputPort;
import org.apache.hadoop.fs.Path;

import com.datatorrent.batch.BatchControlTuple;

@org.apache.hadoop.classification.InterfaceStability.Evolving
public class BatchBasedLineByLineFileInputOperator extends AbstractBatchFileInputOperator<String>
{
  public final transient ControlAwareDefaultOutputPort<String> output = new ControlAwareDefaultOutputPort<String>();

  protected transient BufferedReader br;

  @Override
  protected InputStream openFile(Path path) throws IOException
  {
    InputStream is = super.openFile(path);
    br = new BufferedReader(new InputStreamReader(is));
    return is;
  }

  @Override
  protected void closeFile(InputStream is) throws IOException
  {
    super.closeFile(is);
    br.close();
    br = null;
  }

  @Override
  protected String readEntity() throws IOException
  {
    return br.readLine();
  }

  @Override
  protected void emit(String tuple)
  {
    output.emit(tuple);
  }

  @Override
  public void emitStartBatchControlTuple()
  {
    output.emitControl(new BatchControlTuple.StartBatchControlTupleImpl());
  }

  @Override
  public void emitEndBatchControlTuple()
  {
    output.emitControl(new BatchControlTuple.EndBatchControlTupleImpl());
  }
}
