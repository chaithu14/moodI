/*
 * Copyright (c) 2012-2017 DataTorrent, Inc.
 * All Rights Reserved.
 * The use of this source code is governed by the Limited License located at
 * https://www.datatorrent.com/datatorrent-openview-software-license/
 */

package com.datatorrent.apps;

import com.datatorrent.api.Context;
import com.datatorrent.api.DAG;
import com.datatorrent.api.StreamingApplication;
import com.datatorrent.api.annotation.ApplicationAnnotation;
import com.datatorrent.moodi.kinesis.KinesisByteArrayInputOperator;
import com.datatorrent.moodi.lib.io.fs.s3.S3MetricsTupleOutputModule;

import org.apache.hadoop.conf.Configuration;

@org.apache.hadoop.classification.InterfaceStability.Evolving
@ApplicationAnnotation(name="kinesis-to-S3")
public class Application implements StreamingApplication
{
  @Override
  public void populateDAG(DAG dag, Configuration conf)
  {
    // Add Kinesis as input and S3 as output operators respectively to dag.
    KinesisByteArrayInputOperator inputModule = dag.addOperator("KinesisInput", new KinesisByteArrayInputOperator());
    S3MetricsTupleOutputModule.S3BytesOutputModule outputModule = dag.addModule("S3OutputModule", new S3MetricsTupleOutputModule.S3BytesOutputModule());
    outputModule.setCompactionParallelPartition(true);
    // Create a stream for messages from Kinesis to S3
    dag.addStream("KinesisToS3", inputModule.outputPort, outputModule.input);

    dag.setAttribute(Context.DAGContext.METRICS_TRANSPORT, null);

    /*
     * To add custom logic to your DAG, add your custom operator here with
     * dag.addOperator api call and connect it in the dag using the dag.addStream
     * api call.
     *
     * For example:
     *
     * To parse incoming csv lines, transform them and outputting them on kafka.
     * KinesisInput->CSVParser->Transform->CSVFormatter->S3OutputModule can be achieved as follows
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
     * Use S3TupleOutputModule.S3StringOutputModule instead of S3TupleOutputModule.S3BytesOutputModule i.e.
     * Replace the following line below:
     * S3MetricsTupleOutputModule.S3BytesOutputModule outputModule = dag.addModule("S3OutputModule", new S3MetricsTupleOutputModule.S3BytesOutputModule());
     * with this lines:
     * S3MetricsTupleOutputModule.S3StringOutputModule outputModule = dag.addModule("S3OutputModule", new S3MetricsTupleOutputModule.S3StringOutputModule());
     *
     * Connect these operators with appropriate streams
     * Replace the following line below:
     * dag.addStream("KinesisToS3", inputModule.outputPort, outputModule.input);
     *
     * with these lines:
     * dag.addStream("data", inputModule.outputPort, csvParser.in);
     * dag.addStream("pojo", csvParser.out, transform.input);
     * dag.addStream("transformed", transform.output, formatter.in);
     * dag.addStream("string", formatter.out, outputModule.input);
     *
     */
  }
}
