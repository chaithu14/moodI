/*
 * Copyright (c) 2012-2017 DataTorrent, Inc.
 * All Rights Reserved.
 * The use of this source code is governed by the Limited License located at
 * https://www.datatorrent.com/datatorrent-openview-software-license/
 */

package com.datatorrent.lib.schemaAware;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import org.apache.commons.beanutils.BeanUtils;

import com.datatorrent.api.Operator.InputPort;
import com.datatorrent.api.Operator.OutputPort;
import com.datatorrent.contrib.parser.DelimitedSchema;
import com.datatorrent.contrib.parser.DelimitedSchema.Field;
import com.datatorrent.lib.formatter.JsonFormatterTest.Ad;
import com.datatorrent.lib.testbench.CollectorTestSink;
import com.datatorrent.schema.api.Schema;
import com.datatorrent.schema.api.SchemaExtended;

@org.apache.hadoop.classification.InterfaceStability.Evolving
public class CsvFormatterTest
{

  static CsvFormatter formatter = new CsvFormatter();
  static SchemaExtended schema = new SchemaExtended();
  static Map<InputPort, Schema> inSchema = new HashMap<InputPort, Schema>();
  static CollectorTestSink<Object> error = new CollectorTestSink<Object>();
  static CollectorTestSink<Object> out = new CollectorTestSink<Object>();

  @BeforeClass
  public static void setup() throws IOException
  {
    String name = "ad";
    schema.setName(name);
    schema.setFqcn(name);
    schema.addField("adId", int.class);
    schema.addField("campId", Long.class);
    schema.addField("adName", String.class);
    schema.addField("active", Boolean.class);
    schema.generateBean();
    Class clazz = schema.getBeanClass();
    inSchema.put(formatter.in, schema);
    formatter.err.setSink(error);
    formatter.out.setSink(out);

    formatter.setFieldOrder("adId,campId,active,adName");
    formatter.setSeparator(',');
    formatter.registerSchema(inSchema, new HashMap<OutputPort, Schema>());
    formatter.setClazz(clazz);
    formatter.setup(null);

  }

  @Test
  public void TestSchemaRegistration()
  {
    DelimitedSchema delimitedSchema = new DelimitedSchema(formatter.getSchema());
    Assert.assertEquals(',', delimitedSchema.getDelimiterChar());
    Assert.assertEquals(4, delimitedSchema.getFieldNames().size());
    List<Field> fields = delimitedSchema.getFields();
    Assert.assertEquals("adId", fields.get(0).getName());
    Assert.assertEquals("campId", fields.get(1).getName());
    Assert.assertEquals("active", fields.get(2).getName());
    Assert.assertEquals("adName", fields.get(3).getName());
  }

  @Test
  public void TestPropogation()
      throws InstantiationException, IOException, IllegalAccessException, InvocationTargetException
  {

    Object object = formatter.getClazz().newInstance();
    BeanUtils.setProperty(object, "adId", 1234);
    BeanUtils.setProperty(object, "campId", 98233);
    BeanUtils.setProperty(object, "active", true);
    BeanUtils.setProperty(object, "adName", "adxyz");

    formatter.beginWindow(0);
    formatter.in.process(object);
    formatter.endWindow();

    Assert.assertEquals(1, out.collectedTuples.size());
    Assert.assertEquals(0, error.collectedTuples.size());
    Assert.assertEquals("1234,98233,true,adxyz", out.collectedTuples.get(0).toString().trim());
  }

  @Test
  public void TestInvalidInput()
      throws InstantiationException, IOException, IllegalAccessException, InvocationTargetException
  {

    Ad ad = new Ad();
    ad.adId = 1234;
    formatter.beginWindow(0);
    formatter.in.process(ad);
    formatter.endWindow();
    Assert.assertEquals(1, error.collectedTuples.size());
    Assert.assertEquals(0, out.collectedTuples.size());
  }

  @After
  public void clearSinks()
  {

    error.clear();
    out.clear();
  }

  @AfterClass
  public static void cleanUp()
  {
    formatter.teardown();
  }

}
