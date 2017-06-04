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

import java.util.Map;

import com.datatorrent.api.DAG;
import com.datatorrent.api.StreamingApplication;
import com.datatorrent.api.annotation.ApplicationAnnotation;
import com.datatorrent.contrib.formatter.CsvFormatter;
import com.datatorrent.contrib.parser.CsvParser;
import com.datatorrent.lib.converter.StringToByteArrayConverterOperator;
import com.datatorrent.lib.transform.TransformOperator;
import org.apache.apex.malhar.lib.db.redshift.RedshiftOutputModule;
import org.apache.apex.malhar.lib.fs.s3.S3RecordReaderModule;
import org.apache.hadoop.conf.Configuration;

import com.google.common.collect.Maps;

@ApplicationAnnotation(name="S3-to-redshift")
public class Application implements StreamingApplication
{
  private String s3Schema = "s3n";

  public void setS3FilesToInput(S3RecordReaderModule inputOperator, Configuration conf)
  {
    String accessKey = conf.get("apex.app-param.accessKeyForS3Input");
    String secretKey = conf.get("apex.app-param.secretKeyForS3Input");
    String bucketName = conf.get("apex.app-param.bucketNameForS3Input");
    String files = conf.get("apex.app-param.filesForScanning");
    String inputURI = s3Schema + "://" + accessKey + ":" + secretKey + "@" + bucketName + "/";
    String uriFiles = "";
    String[] inputFiles = files.split(",");
    for (int i = 0; i < inputFiles.length; i++) {
      uriFiles += inputURI + inputFiles[i];
      if (i != inputFiles.length - 1) {
        uriFiles += ",";
      }
    }
    inputOperator.setFiles(uriFiles);
  }

  @Override
  public void populateDAG(DAG dag, Configuration conf)
  {
    //Add S3 as input and redshift as output operators to DAG
    S3RecordReaderModule inputModule = dag.addModule("S3Input", new S3RecordReaderModule());
    setS3FilesToInput(inputModule, conf);

    CsvParser csvParser = dag.addOperator("csvParser", CsvParser.class);
    TransformOperator transform = dag.addOperator("transform", new TransformOperator());
    Map<String, String> expMap = Maps.newHashMap();
    expMap.put("name", "{$.name}.toUpperCase()");
    transform.setExpressionMap(expMap);
    CsvFormatter formatter = dag.addOperator("formatter", new CsvFormatter());
    StringToByteArrayConverterOperator converterOp = dag.addOperator("converter", new StringToByteArrayConverterOperator());
    RedshiftOutputModule redshiftOutput = dag.addModule("RedshiftOutput", new RedshiftOutputModule());

    //Create streams
    dag.addStream("data", inputModule.records, csvParser.in);
    dag.addStream("pojo", csvParser.out, transform.input);
    dag.addStream("transformed", transform.output, formatter.in);
    dag.addStream("string", formatter.out, converterOp.input).setLocality(DAG.Locality.THREAD_LOCAL);
    dag.addStream("writeToJDBC", converterOp.output, redshiftOutput.input);
  }

  /**
   * Return the schema for s3 file system
   * @return schema
   */
  public String getS3Schema()
  {
    return s3Schema;
  }

  /**
   * Set the schema for s3 file system. By default the value is "s3n".
   * @param s3Schema s3 schema
   */
  public void setS3Schema(String s3Schema)
  {
    this.s3Schema = s3Schema;
  }
}
