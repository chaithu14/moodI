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
import com.datatorrent.lib.io.fs.FSInputModule;

import org.apache.apex.malhar.lib.io.block.PartFileWriter;
import org.apache.hadoop.conf.Configuration;

@org.apache.hadoop.classification.InterfaceStability.Evolving
@ApplicationAnnotation(name="Hdfs-part-file-copy-App")
public class Application implements StreamingApplication
{
  @Override
  public void populateDAG(DAG dag, Configuration conf)
  {
    // Add FSInputModule as input and PartFileWriter as output operators to dag.
    FSInputModule input = dag.addModule("HDFSInputModule", new FSInputModule());
    PartFileWriter output = dag.addOperator("PartFileCopy", new PartFileWriter());

    dag.setInputPortAttribute(output.input, Context.PortContext.PARTITION_PARALLEL, true);
    dag.setInputPortAttribute(output.blockMetadataInput, Context.PortContext.PARTITION_PARALLEL, true);

    // Create a stream for blockData, fileMetadata, blockMetadata from Input to PartFileWriter
    dag.addStream("BlocksMetaData", input.blocksMetadataOutput, output.blockMetadataInput).setLocality(DAG.Locality.CONTAINER_LOCAL);
    dag.addStream("BlocksData", input.messages, output.input).setLocality(DAG.Locality.CONTAINER_LOCAL);
    dag.addStream("FileMetaData", input.filesMetadataOutput, output.fileMetadataInput);
  }
}
