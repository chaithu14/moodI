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
import com.datatorrent.moodi.kinesis.KinesisByteArrayInputOperator;
import com.datatorrent.moodi.lib.db.RedshiftOutputModule;

@org.apache.hadoop.classification.InterfaceStability.Evolving
@ApplicationAnnotation(name="Kinesis-to-Redshift")
public class Application implements StreamingApplication
{

  @Override
  public void populateDAG(DAG dag, Configuration conf)
  {
    // Operator used to get the data from AWS kinesis streams in form of byte array.
    KinesisByteArrayInputOperator inputOperator = dag.addOperator("KinesisInput", new KinesisByteArrayInputOperator());

    // Operator used to insert the values from AWS Kinesis streams to AWS Redshift database.
    RedshiftOutputModule jdbcOutputOperator = dag.addModule("JdbcOutput", new RedshiftOutputModule());

    // connect the above operators.
    dag.addStream("KinesisToRedshift", inputOperator.outputPort, jdbcOutputOperator.input);

    dag.setAttribute(Context.DAGContext.METRICS_TRANSPORT, null);
    /*
     * To add custom logic to your DAG, add your custom operator here with
     * dag.addOperator api call and connect it in the dag using the dag.addStream
     * api call.
     *
     * For example:
     *
     * To add the transformation operator in the DAG, use the following block of code:
     * CsvParser csvParser = dag.addOperator("csvParser", CsvParser.class);
     * TransformOperator transform = dag.addOperator("transform", new TransformOperator());
     * Map<String, String> expMap = Maps.newHashMap();
     * expMap.put("name", "{$.name}.toUpperCase()");
     * transform.setExpressionMap(expMap);
     * CsvFormatter formatter = dag.addOperator("formatter", new CsvFormatter());
     * StringToByteArrayConverterOperator converterOp = dag.addOperator("converter", new StringToByteArrayConverterOperator());
     *
     * And to connect it in the DAG as follows:
     * kinesisInput --> csvParser --> Transform --> csvFormatter --> byteArrayConverter -> redshiftJdbcOutput
     *
     * Replace the following line:
     * dag.addStream("KinesisToRedshift", inputOperator.outputPort, jdbcOutputOperator.input);
     *
     * with the following two lines:
     * dag.addStream("data", inputOperator.outputPort, csvParser.in);
     * dag.addStream("pojo", csvParser.out, transform.input);
     * dag.addStream("transformed", transform.output, formatter.in);
     * dag.addStream("string", formatter.out, converterOp.input).setLocality(DAG.Locality.THREAD_LOCAL);
     * dag.addStream("writeToJDBC", converterOp.output, jdbcOutputOperator.input);
     */
  }
}
