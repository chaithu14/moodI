/*
 * Copyright (c) 2012-2017 DataTorrent, Inc.
 * All Rights Reserved.
 * The use of this source code is governed by the Limited License located at
 * https://www.datatorrent.com/datatorrent-openview-software-license/
 */

package com.datatorrent.apps;

import java.util.Map;

import com.datatorrent.api.Context;
import com.datatorrent.api.DAG;
import com.datatorrent.api.StreamingApplication;
import com.datatorrent.api.annotation.ApplicationAnnotation;
import com.datatorrent.contrib.formatter.CsvFormatter;
import com.datatorrent.contrib.parser.CsvParser;
import com.datatorrent.moodi.lib.io.fs.s3.S3RecordReaderWrapperModule;
import com.datatorrent.lib.converter.StringToByteArrayConverterOperator;
import com.datatorrent.moodi.lib.db.RedshiftOutputModule;
import com.datatorrent.lib.transform.TransformOperator;
import org.apache.hadoop.conf.Configuration;

import com.google.common.collect.Maps;

@org.apache.hadoop.classification.InterfaceStability.Evolving
@ApplicationAnnotation(name="S3-to-redshift")
public class Application implements StreamingApplication
{
  @Override
  public void populateDAG(DAG dag, Configuration conf)
  {
    //Add S3 as input and redshift as output operators to DAG
    S3RecordReaderWrapperModule inputModule = dag.addModule("S3Input", new S3RecordReaderWrapperModule());

    CsvParser csvParser = dag.addOperator("csvParser", CsvParser.class);
    TransformOperator transform = dag.addOperator("transform", new TransformOperator());
    Map<String, String> expMap = Maps.newHashMap();
    expMap.put("name", "{$.name}.toUpperCase()");
    transform.setExpressionMap(expMap);
    CsvFormatter formatter = dag.addOperator("formatter", new CsvFormatter());
    StringToByteArrayConverterOperator converterOp = dag.addOperator("converter", new StringToByteArrayConverterOperator());
    RedshiftOutputModule redshiftOutput = dag.addModule("RedshiftOutput", new RedshiftOutputModule());

    // Partition Parallel
    dag.setInputPortAttribute(csvParser.in, Context.PortContext.PARTITION_PARALLEL, true);
    dag.setInputPortAttribute(transform.input, Context.PortContext.PARTITION_PARALLEL, true);
    dag.setInputPortAttribute(formatter.in, Context.PortContext.PARTITION_PARALLEL, true);
    dag.setInputPortAttribute(converterOp.input, Context.PortContext.PARTITION_PARALLEL, true);
    //Create streams
    dag.addStream("data", inputModule.records, csvParser.in);
    dag.addStream("pojo", csvParser.out, transform.input);
    dag.addStream("transformed", transform.output, formatter.in);
    dag.addStream("string", formatter.out, converterOp.input).setLocality(DAG.Locality.THREAD_LOCAL);
    dag.addStream("writeToJDBC", converterOp.output, redshiftOutput.input);

    dag.setAttribute(Context.DAGContext.METRICS_TRANSPORT, null);
  }
}
