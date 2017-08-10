/*
 * Copyright (c) 2012-2017 DataTorrent, Inc.
 * All Rights Reserved.
 * The use of this source code is governed by the Limited License located at
 * https://www.datatorrent.com/datatorrent-openview-software-license/
 */

package com.datatorrent.lib.schemaAware;

import java.util.Map;

import com.datatorrent.contrib.cassandra.CassandraPOJOOutputOperator;
import com.datatorrent.schema.api.Schema;
import com.datatorrent.schema.api.SchemaAware;

@org.apache.hadoop.classification.InterfaceStability.Evolving
public class CassandraOutputOperator extends CassandraPOJOOutputOperator implements SchemaAware
{

  @Override
  public void registerSchema(Map<InputPort, Schema> arg0, Map<OutputPort, Schema> arg1)
  {
    // TODO Auto-generated method stub
    
  }

}
