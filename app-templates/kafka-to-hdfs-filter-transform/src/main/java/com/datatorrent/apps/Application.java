/*
 * Copyright (c) 2012-2017 DataTorrent, Inc.
 * All Rights Reserved.
 * The use of this source code is governed by the Limited License located at
 * https://www.datatorrent.com/datatorrent-openview-software-license/
 */

package com.datatorrent.apps;

import org.apache.hadoop.conf.Configuration;

import com.datatorrent.api.Context;
import com.datatorrent.api.DAG;
import com.datatorrent.api.StreamingApplication;
import com.datatorrent.api.annotation.ApplicationAnnotation;
import com.datatorrent.lib.schemaAware.FilterOperator;
import com.datatorrent.lib.schemaAware.JsonFormatter;
import com.datatorrent.lib.schemaAware.JsonParser;
import com.datatorrent.lib.schemaAware.TransformOperator;
import com.datatorrent.moodi.io.fs.StringFileOutputOperator;
import com.datatorrent.moodi.kafka.KafkaSinglePortInputOperator;

@org.apache.hadoop.classification.InterfaceStability.Evolving
@ApplicationAnnotation(name = "Kafka-to-HDFS-Filter-Transform")
public class Application implements StreamingApplication
{

  public void populateDAG(DAG dag, Configuration conf)
  {
    KafkaSinglePortInputOperator kafkaInputOperator = dag.addOperator("kafkaInput", KafkaSinglePortInputOperator.class);
    JsonParser parser = dag.addOperator("parser", new JsonParser());
    TransformOperator transform = dag.addOperator("transform", new TransformOperator());
    FilterOperator filterOperator = dag.addOperator("filter", new FilterOperator());
    JsonFormatter formatter = dag.addOperator("formatter", new JsonFormatter());
    StringFileOutputOperator fileOutput = dag.addOperator("fileOutput", new StringFileOutputOperator());

    dag.addStream("data", kafkaInputOperator.outputPort, parser.in);
    dag.addStream("pojo", parser.out, filterOperator.input);
    dag.addStream("filtered", filterOperator.truePort, transform.input);
    dag.addStream("transformed", transform.output, formatter.in);
    dag.addStream("string", formatter.out, fileOutput.input);

    dag.setAttribute(Context.DAGContext.METRICS_TRANSPORT, null);
  }

}
