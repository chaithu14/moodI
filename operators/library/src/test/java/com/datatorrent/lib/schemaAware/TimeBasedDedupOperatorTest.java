/*
 * Copyright (c) 2012-2017 DataTorrent, Inc.
 * All Rights Reserved.
 * The use of this source code is governed by the Limited License located at
 * https://www.datatorrent.com/datatorrent-openview-software-license/
 */

package com.datatorrent.lib.schemaAware;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.datatorrent.api.Attribute.AttributeMap.DefaultAttributeMap;
import com.datatorrent.api.Context.OperatorContext;
import com.datatorrent.api.Operator.InputPort;
import com.datatorrent.api.Operator.OutputPort;
import com.datatorrent.lib.helper.OperatorContextTestHelper;
import com.datatorrent.schema.api.Schema;
import com.datatorrent.schema.api.SchemaExtended;
import com.datatorrent.schema.util.TupleSchemaRegistry;
import com.datatorrent.stram.engine.PortContext;

@org.apache.hadoop.classification.InterfaceStability.Evolving
public class TimeBasedDedupOperatorTest
{

  private static TimeBasedDedupOperator dedup = new TimeBasedDedupOperator();
  private static Map<InputPort, Schema> inSchema = new HashMap<InputPort, Schema>();
  private static Map<OutputPort, Schema> outSchema = new HashMap<OutputPort, Schema>();
  private static SchemaExtended schemaExtended = new SchemaExtended();

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
    schemaExtended.addField("key", Long.class);
    schemaExtended.addField("date", Date.class);

    inSchema.put(dedup.input, schemaExtended);
    outSchema.put(dedup.unique, new Schema());
    outSchema.put(dedup.duplicate, new Schema());
    outSchema.put(dedup.expired, new Schema());

    dedup.registerSchema(inSchema, outSchema);

    dedup.setKeyExpression("key");
    dedup.setTimeExpression("date.getTime()");
    dedup.setBucketSpan(10);
    dedup.setExpireBefore(60);
    DefaultAttributeMap attributes = new DefaultAttributeMap();
    OperatorContext context = OperatorContextTestHelper.mockOperatorContext(0, attributes);
    dedup.setup(context);
    dedup.input.setup(new PortContext(attributes, context));
  }

  @Test
  public void TestSchemaRegistration()
  {
    Assert.assertEquals(6, inSchema.get(dedup.input).getFieldList().size());
    Assert.assertEquals(inSchema.get(dedup.input).getFieldList().size(),
        outSchema.get(dedup.unique).getFieldList().size());
    Assert.assertEquals(inSchema.get(dedup.input).getFieldList().size(),
        outSchema.get(dedup.duplicate).getFieldList().size());
    Assert.assertEquals(inSchema.get(dedup.input).getFieldList().size(),
        outSchema.get(dedup.expired).getFieldList().size());

    for (Entry<String, Class> field : inSchema.get(dedup.input).getFieldList().entrySet()) {
      Assert.assertTrue(outSchema.get(dedup.unique).getFieldList().containsKey(field.getKey()));
      Assert.assertEquals(field.getValue(), outSchema.get(dedup.unique).getFieldList().get(field.getKey()));
    }
  }

}
