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
import com.datatorrent.api.DAG.Locality;
import com.datatorrent.api.StreamingApplication;
import com.datatorrent.api.annotation.ApplicationAnnotation;
import com.datatorrent.moodi.io.fs.HDFSFileCopyModule;
import com.datatorrent.moodi.lib.io.fs.s3.S3InputModule;

/**
 * Simple application illustrating file copy from S3
 */
@org.apache.hadoop.classification.InterfaceStability.Evolving
@ApplicationAnnotation(name="S3-to-HDFS-Sync")
public class Application implements StreamingApplication
{

  @Override
  public void populateDAG(DAG dag, Configuration conf)
  {

    S3InputModule inputModule = dag.addModule("S3InputModule", new S3InputModule());
    HDFSFileCopyModule outputModule = dag.addModule("HDFSFileCopyModule", new HDFSFileCopyModule());

    dag.addStream("FileMetaData", inputModule.filesMetadataOutput, outputModule.filesMetadataInput);
    dag.addStream("BlocksMetaData", inputModule.blocksMetadataOutput, outputModule.blocksMetadataInput)
        .setLocality(Locality.THREAD_LOCAL);
    dag.addStream("BlocksData", inputModule.messages, outputModule.blockData).setLocality(Locality.THREAD_LOCAL);

    dag.setAttribute(Context.DAGContext.METRICS_TRANSPORT, null);
  }

}
