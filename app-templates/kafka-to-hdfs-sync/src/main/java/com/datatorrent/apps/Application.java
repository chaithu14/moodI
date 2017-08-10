/*
 * Copyright (c) 2012-2017 DataTorrent, Inc.
 * All Rights Reserved.
 * The use of this source code is governed by the Limited License located at
 * https://www.datatorrent.com/datatorrent-openview-software-license/
 */

package com.datatorrent.apps;

import java.util.Map;

import org.apache.apex.malhar.kafka.KafkaSinglePortInputOperator;
import org.apache.apex.malhar.lib.fs.GenericFileOutputOperator.BytesFileOutputOperator;
import org.apache.apex.malhar.lib.fs.GenericFileOutputOperator.StringFileOutputOperator;
import org.apache.hadoop.conf.Configuration;

import com.google.common.collect.Maps;

import com.datatorrent.api.DAG;
import com.datatorrent.api.StreamingApplication;
import com.datatorrent.api.annotation.ApplicationAnnotation;
import com.datatorrent.contrib.formatter.CsvFormatter;
import com.datatorrent.contrib.parser.CsvParser;
import com.datatorrent.lib.transform.TransformOperator;

@org.apache.hadoop.classification.InterfaceStability.Evolving
@ApplicationAnnotation(name = "Kafka-to-HDFS-Sync")
public class Application implements StreamingApplication
{

  public void populateDAG(DAG dag, Configuration conf)
  {
    KafkaSinglePortInputOperator kafkaInputOperator = dag.addOperator("kafkaInput", KafkaSinglePortInputOperator.class);
    BytesFileOutputOperator fileOutput = dag.addOperator("fileOutput", BytesFileOutputOperator.class);

    dag.addStream("data", kafkaInputOperator.outputPort, fileOutput.input);

    /*
     * To add custom logic to your DAG, add your custom operator here with
     * dag.addOperator api call and connect it in the dag using the dag.addStream
     * api call. 
     * 
     * For example: 
     * 
     * To parse incoming csv lines, transform them and outputting them on kafka.
     * kafkaInput->CSVParser->Transform->CSVFormatter->fileOutput can be achieved as follows
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
     * Use StringFileOutputOperator instead of BytesFileOutputOperator i.e. 
     * Replace the following line below:
     * BytesFileOutputOperator fileOutput = dag.addOperator("fileOutput", BytesFileOutputOperator.class);
     * with this lines:
     * StringFileOutputOperator fileOutput = dag.addOperator("fileOutput", new StringFileOutputOperator());
     *
     * Connect these operators with approriate streams
     * Replace the following line below:
     * dag.addStream("data", kafkaInputOperator.outputPort, fileOutput.input);
     * 
     * with these lines:
     * dag.addStream("data", kafkaInputOperator.outputPort, csvParser.in);
     * dag.addStream("pojo", csvParser.out, transform.input);
     * dag.addStream("transformed", transform.output, formatter.in);
     * dag.addStream("string", formatter.out, fileOutput.input);
     *
     * In ApplicationTests.java->
     * Replace the following line from compare()
     * Assert.assertArrayEquals(lines, output.split("\\n"));
     * with
     * Assert.assertArrayEquals(lines_transformed, output.split("\\n"));
     *
     * Note: "lines" is the output ref to the application which is under uncommented DAG and "lines_transformed"
     *        output belongs to the custom logic DAG.
     */
  }

}
