/*
 * Copyright (c) 2012-2017 DataTorrent, Inc.
 * All Rights Reserved.
 * The use of this source code is governed by the Limited License located at
 * https://www.datatorrent.com/datatorrent-openview-software-license/
 */

package com.datatorrent.lib.schemaAware;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.datatorrent.api.Operator.InputPort;
import com.datatorrent.api.Operator.OutputPort;
import com.datatorrent.lib.appdata.schemas.SchemaUtils;
import com.datatorrent.lib.testbench.CollectorTestSink;
import com.datatorrent.lib.util.KeyValPair;
import com.datatorrent.schema.api.Schema;
import com.datatorrent.schema.api.SchemaExtended;

@org.apache.hadoop.classification.InterfaceStability.Evolving
public class CsvParserTest
{
  static CsvParser parser = new CsvParser();
  static Map<OutputPort, Schema> outSchema = new HashMap<OutputPort, Schema>();
  static SchemaExtended schemaExtended = new SchemaExtended();
  static CollectorTestSink<Object> error = new CollectorTestSink<Object>();
  static CollectorTestSink<Object> pojoPort = new CollectorTestSink<Object>();
  static CollectorTestSink<Object> objectPort = new CollectorTestSink<Object>();

  @BeforeClass
  public static void setup()
  {
    String name = "ad";
    schemaExtended.setName(name);
    schemaExtended.setFqcn(name);
    outSchema.put(parser.out, schemaExtended);
    parser.setSchema(SchemaUtils.jarResourceFileToString("csvSchema.json"));
    parser.registerSchema(new HashMap<InputPort, Schema>(), outSchema);
    parser.out.setSink(pojoPort);
    parser.parsedOutput.setSink(objectPort);
    parser.err.setSink(error);

    parser.setup(null);
  }

  @Test
  public void TestSchemaRegistration()
  {
    Assert.assertEquals(12, outSchema.get(parser.out).getFieldList().size());
    Assert.assertEquals(outSchema.get(parser.out).getField("adId"), Integer.class);
    Assert.assertEquals(outSchema.get(parser.out).getField("campaignId"), Integer.class);
    Assert.assertEquals(outSchema.get(parser.out).getField("adName"), String.class);
    Assert.assertEquals(outSchema.get(parser.out).getField("bidPrice"), Double.class);
    Assert.assertEquals(outSchema.get(parser.out).getField("startDate"), Date.class);
    Assert.assertEquals(outSchema.get(parser.out).getField("endDate"), Date.class);
    Assert.assertEquals(outSchema.get(parser.out).getField("securityCode"), Long.class);
    Assert.assertEquals(outSchema.get(parser.out).getField("active"), Boolean.class);
    Assert.assertEquals(outSchema.get(parser.out).getField("parentCampaign"), String.class);
    Assert.assertEquals(outSchema.get(parser.out).getField("optimized"), Boolean.class);
    Assert.assertEquals(outSchema.get(parser.out).getField("weatherTargeted"), Character.class);
    Assert.assertEquals(outSchema.get(parser.out).getField("valid"), String.class);
  }

  @Test
  public void TestPropogation() throws IllegalAccessException, IllegalArgumentException, InvocationTargetException,
      NoSuchMethodException, SecurityException, IOException
  {

    schemaExtended.generateBean();
    Class clazz = schemaExtended.getBeanClass();
    parser.setClazz(clazz);

    String input = "1234,98233,adxyz,0.2,2015-03-08 03:37:12,11/12/2012,12,y,OPTIMIZE,CAMP_AD,Y,yes";
    parser.beginWindow(0);
    parser.in.process(input.getBytes());
    parser.endWindow();
    Object obj = pojoPort.collectedTuples.get(0);

    Assert.assertEquals(1, pojoPort.collectedTuples.size());
    Assert.assertEquals(1, objectPort.collectedTuples.size());
    Assert.assertEquals(0, error.collectedTuples.size());

    Assert.assertEquals(1234, clazz.getDeclaredMethod("getAdId").invoke(obj));
    Assert.assertEquals(98233, clazz.getDeclaredMethod("getCampaignId").invoke(obj));
    Assert.assertEquals("adxyz", clazz.getDeclaredMethod("getAdName").invoke(obj));
    Assert.assertEquals(0.2, clazz.getDeclaredMethod("getBidPrice").invoke(obj));
    Assert.assertEquals(Date.class, clazz.getDeclaredMethod("getStartDate").invoke(obj).getClass());
    Assert.assertEquals(Date.class, clazz.getDeclaredMethod("getEndDate").invoke(obj).getClass());
    Assert.assertEquals(12L, clazz.getDeclaredMethod("getSecurityCode").invoke(obj));
    Assert.assertTrue((Boolean)clazz.getDeclaredMethod("getActive").invoke(obj));
    Assert.assertEquals("CAMP_AD", clazz.getDeclaredMethod("getParentCampaign").invoke(obj));
    Assert.assertTrue((Boolean)clazz.getDeclaredMethod("getOptimized").invoke(obj));
    Assert.assertEquals('Y', clazz.getDeclaredMethod("getWeatherTargeted").invoke(obj));
    Assert.assertEquals("yes", clazz.getDeclaredMethod("getValid").invoke(obj));

  }

  @Test
  public void TestParserInvalidInput() throws IOException
  {
    schemaExtended.generateBean();
    Class clazz = schemaExtended.getBeanClass();
    parser.setClazz(clazz);

    String input = ",98233,adxyz,0.2,2015-03-08 03:37:12,11/12/2012,12,y,,CAMP_AD,Y,yes";
    parser.beginWindow(0);
    parser.in.process(input.getBytes());
    parser.endWindow();

    Assert.assertEquals(0, pojoPort.collectedTuples.size());
    Assert.assertEquals(0, objectPort.collectedTuples.size());
    Assert.assertEquals(1, error.collectedTuples.size());
    KeyValPair<String, String> errorTuple = (KeyValPair<String, String>)error.collectedTuples.get(0);
    Assert.assertEquals(input, errorTuple.getKey());

  }

  @After
  public void clearSinks()
  {
    error.clear();
    objectPort.clear();
    pojoPort.clear();
  }

  @AfterClass
  public static void cleanUp()
  {
    parser.teardown();
  }

}
