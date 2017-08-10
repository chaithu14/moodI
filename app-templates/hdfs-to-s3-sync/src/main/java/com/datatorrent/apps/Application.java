/*
 * Copyright (c) 2012-2017 DataTorrent, Inc.
 * All Rights Reserved.
 * The use of this source code is governed by the Limited License located at
 * https://www.datatorrent.com/datatorrent-openview-software-license/
 */

package com.datatorrent.apps;

import com.datatorrent.api.DAG;
import com.datatorrent.api.StreamingApplication;
import com.datatorrent.api.annotation.ApplicationAnnotation;
import com.datatorrent.lib.io.fs.FSInputModule;
import org.apache.apex.malhar.lib.fs.s3.S3OutputModule;
import org.apache.hadoop.conf.Configuration;

@org.apache.hadoop.classification.InterfaceStability.Evolving
@ApplicationAnnotation(name="HDFS-to-S3-Sync")
public class Application implements StreamingApplication
{

  @Override
  public void populateDAG(DAG dag, Configuration conf)
  {

    /*
     * Define HDFS and S3 as input and output module operators respectively.
     */
    FSInputModule inputModule = dag.addModule("HDFSInputModule", new FSInputModule());
    S3OutputModule outputModule = dag.addModule("S3OutputModule", new S3OutputModule());

    /*
     * Create a stream for Metadata blocks from HDFS to S3 output modules.
     * Note: DAG locality is set to CONTAINER_LOCAL for performance benefits by
     * avoiding any serialization/deserialization of objects.
     */
    dag.addStream("FileMetaData", inputModule.filesMetadataOutput, outputModule.filesMetadataInput);
    dag.addStream("BlocksMetaData", inputModule.blocksMetadataOutput, outputModule.blocksMetadataInput)
            .setLocality(DAG.Locality.CONTAINER_LOCAL);

    /*
     * Create a stream for Data blocks from HDFS to S3 output modules.
     */
    dag.addStream("BlocksData", inputModule.messages, outputModule.blockData).setLocality(DAG.Locality.CONTAINER_LOCAL);
  }
}
