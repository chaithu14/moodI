/*
 * Copyright (c) 2012-2017 DataTorrent, Inc.
 * All Rights Reserved.
 * The use of this source code is governed by the Limited License located at
 * https://www.datatorrent.com/datatorrent-openview-software-license/
 */

package com.datatorrent.apps;

import org.apache.hadoop.conf.Configuration;

import com.datatorrent.api.DAG;
import com.datatorrent.api.DAG.Locality;
import com.datatorrent.api.StreamingApplication;
import com.datatorrent.api.annotation.ApplicationAnnotation;
import com.datatorrent.lib.io.fs.FSInputModule;
import com.datatorrent.lib.io.fs.HDFSFileCopyModule;

/**
 * Application for HDFS to HDFS file copy
 *
 * @since 3.4.0
 */
@org.apache.hadoop.classification.InterfaceStability.Evolving
@ApplicationAnnotation(name = "HDFS-Sync-App")
public class Application implements StreamingApplication
{
  @Override
  public void populateDAG(DAG dag, Configuration conf)
  {

    FSInputModule inputModule = dag.addModule("HDFSInputModule", new FSInputModule());
    HDFSFileCopyModule outputModule = dag.addModule("HDFSFileCopyModule", new HDFSFileCopyModule());

    dag.addStream("FileMetaData", inputModule.filesMetadataOutput, outputModule.filesMetadataInput);
    dag.addStream("BlocksMetaData", inputModule.blocksMetadataOutput, outputModule.blocksMetadataInput)
        .setLocality(Locality.THREAD_LOCAL);
    dag.addStream("BlocksData", inputModule.messages, outputModule.blockData).setLocality(Locality.THREAD_LOCAL);
  }

}
