/*
 * Copyright (c) 2012-2017 DataTorrent, Inc.
 * All Rights Reserved.
 * The use of this source code is governed by the Limited License located at
 * https://www.datatorrent.com/datatorrent-openview-software-license/
 */

package com.datatorrent.image;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.datatorrent.lib.io.fs.AbstractFileOutputOperator;

@org.apache.hadoop.classification.InterfaceStability.Evolving
public class BytesFileWriter extends AbstractFileOutputOperator<Data>
{
  private static final transient Logger LOG = LoggerFactory.getLogger(BytesFileWriter.class);

  public String fileName;
  private boolean eof;

  @Override
  protected byte[] getBytesForTuple(Data tuple)
  {
    eof = true;
    return tuple.bytesImage;
  }

  @Override
  protected String getFileName(Data tuple)
  {
    fileName = tuple.fileName;
    return tuple.fileName;
  }

  @Override
  public void endWindow()
  {

    if (!eof) {
      LOG.error("Error end of file not found " + fileName);
      return;
    }
    if (null == fileName) {
      LOG.error("Error file name is null" + fileName);
      return;
    }
    try {
      finalizeFile(fileName);
      Thread.sleep(100);
    } catch (Exception e) {
      LOG.error("Error in finalizing the file in Super class" + e);
    }
    super.endWindow();
    eof = false;
    fileName = null;
  }


}
