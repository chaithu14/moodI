/*
 * Copyright (c) 2012-2017 DataTorrent, Inc.
 * All Rights Reserved.
 * The use of this source code is governed by the Limited License located at
 * https://www.datatorrent.com/datatorrent-openview-software-license/
 */

package com.datatorrent.moodi.lib.io.fs.batch;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.apex.api.ControlAwareDefaultInputPort;
import org.apache.apex.api.operator.ControlTuple;

import com.datatorrent.api.StreamCodec;
import com.datatorrent.batch.BatchControlTuple;
import com.datatorrent.lib.io.fs.AbstractFileOutputOperator;

/**
 * This base implementation for a fault tolerant Batch based HDFS output operator,
 * which can handle outputting to multiple files when the output file depends on the tuple.
 * The file a tuple is output to is determined by the getFilePath method. The operator can
 * also output files to rolling mode. In rolling mode by default file names have '.#' appended to the
 * end, where # is an integer. A maximum length for files is specified and whenever the current output
 * file size exceeds the maximum length, output is rolled over to a new file whose name ends in '.#+1'.
 * After receiving the end batch control tuple, the operator will finalize all the open files.
 */
@org.apache.hadoop.classification.InterfaceStability.Evolving
public abstract class AbstractBatchFileOutputOperator<INPUT> extends AbstractFileOutputOperator<INPUT>
{
  private static final Logger LOG = LoggerFactory.getLogger(AbstractBatchFileOutputOperator.class);

  public final transient ControlAwareDefaultInputPort<INPUT> input = new ControlAwareDefaultInputPort<INPUT>()
  {
    @Override
    public void process(INPUT tuple)
    {
      processTuple(tuple);
    }

    @Override
    public StreamCodec<INPUT> getStreamCodec()
    {
      if (AbstractBatchFileOutputOperator.this.streamCodec == null) {
        return super.getStreamCodec();
      } else {
        return streamCodec;
      }
    }

    @Override
    public boolean processControl(ControlTuple controlTuple)
    {
      processControlTuple(controlTuple);
      //This is an output operator. Does not matter what value we return.
      return false;
    }
  };

  public void processControlTuple(ControlTuple controlTuple)
  {
    if (controlTuple instanceof BatchControlTuple.EndBatchControlTuple) {
      LOG.debug("Received end batch");
      List<String> fileNameList = new ArrayList<>(this.getFileNameToTmpName().keySet());
      for (String fileName : fileNameList) {
        try {
          LOG.debug("Finalizing file : {}", fileName);
          finalizeFile(fileName);
        } catch (IOException e) {
          LOG.debug("Failed to finalize file {}", fileName);
          throw new RuntimeException(e);
        }
      }
    }
  }
}
