package com.datatorrent.lib.metrics;

import org.apache.apex.malhar.lib.fs.FSRecordCompactionOperator;

import com.datatorrent.api.Context;

public class RedshiftJdbcTransactionableOutputOperator extends org.apache.apex.malhar.lib.db.redshift.RedshiftJdbcTransactionableOutputOperator
{
  @Override
  protected String generateCopyStatement(FSRecordCompactionOperator.OutputMetaData data)
  {
    return super.generateCopyStatement(data);
  }

  @Override
  public void setup(Context.OperatorContext context)
  {
    super.setup(context);
  }

  @Override
  public void beginWindow(long windowId)
  {
    super.beginWindow(windowId);
  }

  @Override
  public void endWindow()
  {
    super.endWindow();
  }
}
