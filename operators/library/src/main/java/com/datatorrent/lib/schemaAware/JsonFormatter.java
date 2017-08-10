/*
 * Copyright (c) 2012-2017 DataTorrent, Inc.
 * All Rights Reserved.
 * The use of this source code is governed by the Limited License located at
 * https://www.datatorrent.com/datatorrent-openview-software-license/
 */

package com.datatorrent.lib.schemaAware;

import java.util.Map;

import org.apache.hadoop.classification.InterfaceStability;

import com.datatorrent.schema.api.Schema;
import com.datatorrent.schema.api.SchemaAware;

@InterfaceStability.Evolving
public class JsonFormatter extends com.datatorrent.lib.formatter.JsonFormatter implements SchemaAware
{
  public void registerSchema(Map<InputPort, Schema> inSchema, Map<OutputPort, Schema> outSchema)
  {
    //No need to override this as there is not schema property of JsonFormatter that needs to be set
  }
}
