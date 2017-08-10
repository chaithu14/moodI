/*
 * Copyright (c) 2012-2017 DataTorrent, Inc.
 * All Rights Reserved.
 * The use of this source code is governed by the Limited License located at
 * https://www.datatorrent.com/datatorrent-openview-software-license/
 */

package com.datatorrent.lib.schemaAware;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.common.collect.Maps;

import com.datatorrent.api.Operator.InputPort;
import com.datatorrent.api.Operator.OutputPort;
import com.datatorrent.schema.api.Schema;

@org.apache.hadoop.classification.InterfaceStability.Evolving
public class POJOEnricherTest
{
  private static POJOEnricher enrich = new POJOEnricher();
  private static Map<InputPort, Schema> inSchema = new HashMap<InputPort, Schema>();
  private static Map<OutputPort, Schema> outSchema = new HashMap<OutputPort, Schema>();

  @BeforeClass
  public static void setup() throws IOException
  {
    Schema inputSchema = new Schema();
    inputSchema.addField("id", Integer.class);
    inSchema.put(enrich.input, inputSchema);
    outSchema.put(enrich.output, new Schema());
    outSchema.put(enrich.error, new Schema());
    Map<String, String> outputFieldMap = new HashMap<String, String>();
    outputFieldMap.put("uniqueId", "STRING");
    Map<String, String> includeFieldsMap = Maps.newHashMap();
    enrich.setIncludeFieldsMap(includeFieldsMap);
    includeFieldsMap.put("name", "STRING");
    includeFieldsMap.put("age", "INTEGER");
    includeFieldsMap.put("address", "STRING");
    enrich.registerSchema(inSchema, outSchema);
    enrich.setup(null);
  }

  @Test
  public void TestSchemaRegistration()
  {
    Assert.assertEquals(1, inSchema.get(enrich.input).getFieldList().size());
    Assert.assertEquals(4, outSchema.get(enrich.output).getFieldList().size());
    Assert.assertEquals(1, outSchema.get(enrich.error).getFieldList().size());
  }
}
