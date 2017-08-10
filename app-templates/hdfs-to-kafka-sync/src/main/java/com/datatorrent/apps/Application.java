/*
 * Copyright (c) 2012-2017 DataTorrent, Inc.
 * All Rights Reserved.
 * The use of this source code is governed by the Limited License located at
 * https://www.datatorrent.com/datatorrent-openview-software-license/
 */

package com.datatorrent.apps;

import java.io.File;
import java.util.Map;

import org.apache.apex.malhar.lib.fs.FSRecordReaderModule;
import org.apache.commons.io.FileUtils;
import org.apache.hadoop.conf.Configuration;

import com.google.common.collect.Maps;

import com.datatorrent.api.DAG;
import com.datatorrent.api.StreamingApplication;
import com.datatorrent.api.annotation.ApplicationAnnotation;
import com.datatorrent.contrib.formatter.CsvFormatter;
import com.datatorrent.contrib.kafka.KafkaSinglePortOutputOperator;
import com.datatorrent.contrib.parser.CsvParser;
import com.datatorrent.lib.transform.TransformOperator;

@org.apache.hadoop.classification.InterfaceStability.Evolving
@ApplicationAnnotation(name="HDFS-to-Kafka-Sync")
public class Application implements StreamingApplication
{

  @Override
  public void populateDAG(DAG dag, Configuration conf)
  {
    FSRecordReaderModule lineReader = dag.addModule("recordReader", FSRecordReaderModule.class);

    KafkaSinglePortOutputOperator<String,byte[]> kafkaOutput = 
        dag.addOperator("kafkaOutput", new KafkaSinglePortOutputOperator<String,byte[]>());

    dag.addStream("data", lineReader.records, kafkaOutput.inputPort);
    
    
    /*
     * To add custom logic to your DAG, add your custom operator here with
     * dag.addOperator api call and connect it in the dag using the dag.addStream
     * api call. 
     * 
     * For example: 
     * 
     * To parse incoming csv lines, transform them and outputting them on kafka.
     * recordReader->CSVParser->Transform->CSVFormatter->KafkaOutput can be achieved as follows
     * 
     * Adding operators: 
     * CsvParser csvParser = dag.addOperator("csvParser", CsvParser.class);
     *
     * TransformOperator transform = dag.addOperator("transform", new TransformOperator());
     * Map<String, String> expMap = Maps.newHashMap();
     * expMap.put("name", "{$.name}.toUpperCase()");
     * transform.setExpressionMap(expMap);
     * CsvFormatter formatter = dag.addOperator("formatter", new CsvFormatter());
     * 
     * Use KafkaSinglePortOutputOperator<String,String> instead of 
     *    KafkaSinglePortOutputOperator<String,String> i.e. 
     * 
     * Replace the following line below
     * KafkaSinglePortOutputOperator<String,byte[]> kafkaOutput = 
     *      dag.addOperator("kafkaOutput", new KafkaSinglePortOutputOperator<String,byte[]>());
     * 
     * with these lines
     * KafkaSinglePortOutputOperator<String,String> kafkaOutput = 
     *      dag.addOperator("kafkaOutput", new KafkaSinglePortOutputOperator<String,String>());
     *
     * Connect these operators with approriate streams
     * Replace the following line below
     * dag.addStream("data", lineReader.records, out.inputPort);
     * 
     * with these lines
     * dag.addStream("record", lineReader.records, csvParser.in);
     * dag.addStream("pojo", csvParser.out, transform.input);
     * dag.addStream("transformed", transform.output, formatter.in);
     * dag.addStream("string", formatter.out, kafkaOutput.inputPort);
     *
     * In properties.xml, properties-test.xml->dt.operator.kafkaOutput.prop.producerProperties 
     * Replace
     * serializer.class=kafka.serializer.DefaultEncoder
     * with
     * serializer.class=kafka.serializer.StringEncoder
     * 
     * In ApplicationTests.java->
     * Replace the following line below 
     * FileUtils.readFileToString(new File("src/test/resources/test_events.txt")).split("\\n");
     * with
     * FileUtils.readFileToString(new File("src/test/resources/test_events_transformed.txt")).split("\\n");
     * 
     */    
    
  }
}
