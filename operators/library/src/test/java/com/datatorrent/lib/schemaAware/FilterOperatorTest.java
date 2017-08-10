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
import java.util.Map.Entry;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.datatorrent.api.Operator.InputPort;
import com.datatorrent.api.Operator.OutputPort;
import com.datatorrent.schema.api.Schema;
import com.datatorrent.schema.api.SchemaExtended;
import com.datatorrent.schema.util.TupleSchemaRegistry;

@org.apache.hadoop.classification.InterfaceStability.Evolving
public class FilterOperatorTest
{
  static FilterOperator filter = new FilterOperator();
  static Map<InputPort, Schema> inSchema = new HashMap<InputPort, Schema>();
  static Map<OutputPort, Schema> outSchema = new HashMap<OutputPort, Schema>();
  static SchemaExtended schemaExtended = new SchemaExtended();

  @BeforeClass
  public static void setup() throws IOException
  {
    String name = "ad";
    schemaExtended.setName(name);
    schemaExtended.setFqcn(TupleSchemaRegistry.FQCN_PACKAGE + name);
    schemaExtended.addField("adId", Integer.class);
    schemaExtended.addField("campId", Long.class);
    schemaExtended.addField("adName", String.class);
    schemaExtended.addField("active", Boolean.class);

    inSchema.put(filter.input, schemaExtended);
    outSchema.put(filter.truePort, new Schema());
    outSchema.put(filter.falsePort, new Schema());
    outSchema.put(filter.error, new Schema());
    filter.registerSchema(inSchema, outSchema);
    filter.setup(null);
  }

  @Test
  public void TestSchemaRegistration()
  {
    Assert.assertEquals(4, inSchema.get(filter.input).getFieldList().size());
    Assert.assertEquals(inSchema.get(filter.input).getFieldList().size(),
        outSchema.get(filter.truePort).getFieldList().size());
    Assert.assertEquals(inSchema.get(filter.input).getFieldList().size(),
        outSchema.get(filter.falsePort).getFieldList().size());
    Assert.assertEquals(inSchema.get(filter.input).getFieldList().size(),
        outSchema.get(filter.error).getFieldList().size());

    for (Entry<String, Class> field : inSchema.get(filter.input).getFieldList().entrySet()) {
      Assert.assertTrue(outSchema.get(filter.truePort).getFieldList().containsKey(field.getKey()));
      Assert.assertEquals(field.getValue(), outSchema.get(filter.truePort).getFieldList().get(field.getKey()));
    }
  }

}
