/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.datatorrent.apps;

import java.io.File;
import java.io.IOException;
import java.util.Random;

import javax.validation.ConstraintViolationException;

import org.apache.commons.io.FileUtils;
import org.apache.hadoop.conf.Configuration;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.datatorrent.api.LocalMode;

import info.batey.kafka.unit.KafkaUnit;
import info.batey.kafka.unit.KafkaUnitRule;
import kafka.producer.KeyedMessage;

/**
 * Test the DAG declaration in local mode.
 */
public class ApplicationTest
{
  private static final Logger LOG = LoggerFactory.getLogger(ApplicationTest.class);

  private static final int zkPort = 12181;
  private static final int brokerPort = 19092;
  private static final String BROKER = "localhost:" + brokerPort;
  private String outputDir;
  private String outputFilePath;
  private String topic;

  public static class TestMeta extends TestWatcher
  {
    public String baseDirectory;
    private String[] input;
    private String[] expectedOutput;
    public Random rand = new Random();

    private static String getNextMessage(int num, String namePrefix)
    {
      JSONObject obj = new JSONObject();
      try {
        obj.put("accountNumber",num);
        obj.put("name", namePrefix + num);
        obj.put("amount", 1000*num);
      } catch (JSONException e) {
        return null;
      }
      return obj.toString();
    }
    
    @Override
    protected void starting(org.junit.runner.Description description)
    {
      this.baseDirectory = "target/" + description.getClassName() + "/" + description.getMethodName();
      input = new String[10];
      for (int i = 0; i< 10; i++) {
        input[i] = getNextMessage(i+1, "User");
      }
      expectedOutput = new String[6];
      for (int i = 5, j=0; i<= 10; i++, j++) {
        expectedOutput[j] = getNextMessage(i, "USER");
      }
    }

    @Override
    protected void finished(Description description)
    {
      super.finished(description);
      try {
        FileUtils.forceDelete(new File(baseDirectory));
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

  }

  @Rule
  public TestMeta testMeta = new TestMeta();

  @Before
  public void setup() throws Exception
  {
    outputDir = testMeta.baseDirectory + File.separator + "output";
    outputFilePath = outputDir + "/output.txt_6.0";
  }

  // broker port must match properties.xml
  @Rule
  public KafkaUnitRule kafkaUnitRule = new KafkaUnitRule(zkPort, brokerPort);

  
  @Test
  public void testApplication() throws Exception
  {
    try {
      
      // write messages to Kafka topic
      Configuration conf = getConfig();

      writeToTopic();

      // run app asynchronously; terminate after results are checked
      LocalMode.Controller lc = asyncRun(conf);

      // check for presence of output file
      waitForOutputTuples();

      // compare output lines to input
      compare();

      lc.shutdown();
    } catch (ConstraintViolationException e) {
      Assert.fail("constraint violations: " + e.getConstraintViolations());
    }
  }

  private void writeToTopic()
  {
    KafkaUnit ku = kafkaUnitRule.getKafkaUnit();
    ku.createTopic(topic);
    for (String message : testMeta.input) {
      KeyedMessage<String, String> kMsg = new KeyedMessage<>(topic, message);
      ku.sendMessages(kMsg);
    }
    LOG.debug("Sent messages to topic {}", topic);
  }

  private Configuration getConfig()
  {
    Configuration conf = new Configuration(false);
    conf.addResource(this.getClass().getResourceAsStream("/META-INF/properties-test.xml"));
    conf.set("dt.operator.fileOutput.prop.filePath", outputDir);
    topic = conf.get("dt.operator.kafkaInput.prop.topics");
    return conf;
  }

  private LocalMode.Controller asyncRun(Configuration conf) throws Exception
  {
    LocalMode lma = LocalMode.newInstance();
    lma.prepareDAG(new Application(), conf);
    LocalMode.Controller lc = lma.getController();
    lc.runAsync();
    return lc;
  }

  private void waitForOutputTuples() throws Exception
  {
    File file = new File(outputFilePath);
    final int MAX = 60;
    for (int i = 0; i < MAX && (!file.exists()); ++i) {
      LOG.debug("Sleeping, i = {}", i);
      Thread.sleep(1000);
    }
    if (!file.exists()) {
      String msg = String.format("Error: %s not found after %d seconds%n", outputFilePath, MAX);
      throw new RuntimeException(msg);
    }
  }

  private void compare() throws Exception
  {
    // read output file
    File file = new File(outputFilePath);
    String output = FileUtils.readFileToString(file);
    Assert.assertArrayEquals(testMeta.expectedOutput, output.split("\\n"));
  }
}
