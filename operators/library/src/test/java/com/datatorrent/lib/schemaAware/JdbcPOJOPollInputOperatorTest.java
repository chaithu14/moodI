/*
 * Copyright (c) 2012-2017 DataTorrent, Inc.
 * All Rights Reserved.
 * The use of this source code is governed by the Limited License located at
 * https://www.datatorrent.com/datatorrent-openview-software-license/
 */

package com.datatorrent.lib.schemaAware;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.common.collect.Lists;

import com.datatorrent.api.Operator.InputPort;
import com.datatorrent.api.Operator.OutputPort;
import com.datatorrent.lib.util.FieldInfo;
import com.datatorrent.schema.api.Schema;

@org.apache.hadoop.classification.InterfaceStability.Evolving
public class JdbcPOJOPollInputOperatorTest
{
  private static JdbcPOJOPollInputOperator jdbcInputOperator = new JdbcPOJOPollInputOperator();
  private static Map<OutputPort, Schema> outSchema = new HashMap<OutputPort, Schema>();

  @BeforeClass
  public static void setup()
  {
    outSchema.put(jdbcInputOperator.outputPort, new Schema());
    List<FieldInfo> fieldInfos = Lists.newArrayList();
    fieldInfos.add(new FieldInfo("account_no", "accountNumber", FieldInfo.SupportType.INTEGER));
    fieldInfos.add(new FieldInfo("name", "name", FieldInfo.SupportType.STRING));
    fieldInfos.add(new FieldInfo("amount", "amount", FieldInfo.SupportType.INTEGER));
    jdbcInputOperator.setFieldInfos(fieldInfos);
    jdbcInputOperator.registerSchema(new HashMap<InputPort, Schema>(), outSchema);
  }

  @Test
  public void TestSchemaRegistration()
  {
    Assert.assertEquals(3, outSchema.get(jdbcInputOperator.outputPort).getFieldList().size());
    Assert.assertEquals(outSchema.get(jdbcInputOperator.outputPort).getField("accountNumber"), Integer.class);
    Assert.assertEquals(outSchema.get(jdbcInputOperator.outputPort).getField("amount"), Integer.class);
    Assert.assertEquals(outSchema.get(jdbcInputOperator.outputPort).getField("name"), String.class);
  }

}
