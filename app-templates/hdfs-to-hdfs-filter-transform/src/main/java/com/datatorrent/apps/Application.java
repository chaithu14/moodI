/*
 * Copyright (c) 2012-2017 DataTorrent, Inc.
 * All Rights Reserved.
 * The use of this source code is governed by the Limited License located at
 * https://www.datatorrent.com/datatorrent-openview-software-license/
 */


package com.datatorrent.apps;

import org.apache.apex.malhar.lib.fs.FSRecordReaderModule;
import org.apache.apex.malhar.lib.fs.GenericFileOutputOperator.StringFileOutputOperator;
import org.apache.hadoop.conf.Configuration;

import com.datatorrent.api.DAG;
import com.datatorrent.api.StreamingApplication;
import com.datatorrent.api.annotation.ApplicationAnnotation;
import com.datatorrent.lib.schemaAware.CsvFormatter;
import com.datatorrent.lib.schemaAware.CsvParser;
import com.datatorrent.lib.schemaAware.FilterOperator;
import com.datatorrent.lib.schemaAware.TransformOperator;

@org.apache.hadoop.classification.InterfaceStability.Evolving
@ApplicationAnnotation(name="HDFS-to-HDFS-Filter-Transform")
public class Application implements StreamingApplication
{

  @Override
  public void populateDAG(DAG dag, Configuration conf)
  {
    FSRecordReaderModule recordReader = dag.addModule("recordReader", FSRecordReaderModule.class);
    CsvParser csvParser = dag.addOperator("csvParser", CsvParser.class);
    FilterOperator filterOperator = dag.addOperator("filter", new FilterOperator());
    TransformOperator transform = dag.addOperator("transform", new TransformOperator());
    CsvFormatter formatter = dag.addOperator("formatter", new CsvFormatter());
    StringFileOutputOperator fileOutput = dag.addOperator("fileOutput", new StringFileOutputOperator());

    dag.addStream("record", recordReader.records, csvParser.in);
    dag.addStream("pojo", csvParser.out, filterOperator.input);
    dag.addStream("filteredPojo", filterOperator.truePort, transform.input);
    dag.addStream("transformedPojo", transform.output, formatter.in);
    dag.addStream("string", formatter.out, fileOutput.input);

  }
}
