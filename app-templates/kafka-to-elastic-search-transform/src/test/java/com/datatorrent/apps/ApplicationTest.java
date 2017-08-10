/*
 * Copyright (c) 2012-2017 DataTorrent, Inc.
 * All Rights Reserved.
 * The use of this source code is governed by the Limited License located at
 * https://www.datatorrent.com/datatorrent-openview-software-license/
 */

package com.datatorrent.apps;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.CountDownLatch;

import javax.validation.ConstraintViolationException;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthRequest;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthResponse;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestWatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.hadoop.conf.Configuration;

import com.datatorrent.api.LocalMode;

import info.batey.kafka.unit.KafkaUnit;
import info.batey.kafka.unit.KafkaUnitRule;
import kafka.producer.KeyedMessage;

/**
 * Test the DAG declaration in local mode.
 */
@org.apache.hadoop.classification.InterfaceStability.Evolving
public class ApplicationTest
{
  private static final Logger LOG = LoggerFactory.getLogger(ApplicationTest.class);

  private static final int zkPort = 12181;
  private static final int brokerPort = 19092;
  private static final String BROKER = "localhost:" + brokerPort;
  private String outputDir;
  private String outputFilePath;
  private String topic;

  public static final String indexName = "testindex";
  private TransportClient client;
  private CountDownLatch latch;

  public static class TestMeta extends TestWatcher
  {
    private String[] input;
    private int[] docids;
    private int[] expectedOutput;
    private int expectedCount = 0;

    private static String getNextMessage(int num, String namePrefix)
    {
      JSONObject obj = new JSONObject();
      try {
        obj.put("accountNumber", num);
        obj.put("name", namePrefix + num);
        obj.put("amount", 1000 * num);
      } catch (JSONException e) {
        return null;
      }
      return obj.toString();
    }

    @Override
    protected void starting(org.junit.runner.Description description)
    {
      input = new String[10];
      docids = new int[10];
      expectedOutput = new int[10];
      for (int i = 0, j = 0; i < 10; i++) {
        input[i] = getNextMessage(i + 1, "User");
        docids[i] = input[i].hashCode();
        if (((i + 1) * 1000) >= 5000) {
         expectedOutput[j++] = input[i].hashCode();
         expectedCount++;
        }
      }
    }
  }

  @Rule
  public TestMeta testMeta = new TestMeta();

  @Rule
  public KafkaUnitRule kafkaUnitRule = new KafkaUnitRule(zkPort, brokerPort);

  @Before
  public void initClient()
  {
    // prepare default test settings for client.
    Settings settings = Settings.builder()
      .put("client.transport.ignore_cluster_name",true)
      .put("cluster.name", "es")
      .build();
    client = new PreBuiltTransportClient(settings);
    try {
      client.addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("localhost"), 9300));
    } catch (UnknownHostException e) {
      e.printStackTrace();
    }

    // verify the test cluster is working.
    final ClusterHealthResponse res = client.admin().cluster().health(new ClusterHealthRequest()).actionGet();
    LOG.info("Cluster Status: {}", res.getStatus());
  }

  @After
  public void closeClient()
  {
    for (String message: testMeta.input) {
      DeleteResponse response = client.prepareDelete(indexName, message.getClass().getName(), String.valueOf(message.hashCode())).get();
    }
    client.close();
    client = null;
  }

  private Configuration getConfig()
  {
    Configuration conf = new Configuration(false);
    conf.addResource(this.getClass().getResourceAsStream("/META-INF/properties-test.xml"));
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

  @Test
  public void testApplication() throws Exception
  {
    try {

      // write messages to Kafka topic
      Configuration conf = getConfig();
      writeToTopic();

      // initial the latch for this test
      LocalMode.Controller lc = asyncRun(conf);
      Thread.sleep(15000);

      // verify the output in elastic store.
      verifyIndexOnElasticStore();
      lc.shutdown();
    } catch (ConstraintViolationException e) {
      Assert.fail("constraint violations: " + e.getConstraintViolations());
    }
  }

  private void verifyIndexOnElasticStore()
  {
    SearchResponse response = client.prepareSearch(indexName).get(new TimeValue(2000));

    // verify the number of document request are written to elastic search store.
    Assert.assertFalse("Mismatch in number of documents for a given index", (response.getHits().totalHits != testMeta.expectedCount));

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
}

