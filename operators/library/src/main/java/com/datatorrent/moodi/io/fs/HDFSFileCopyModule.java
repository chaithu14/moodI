/*
 * Copyright (c) 2012-2017 DataTorrent, Inc.
 * All Rights Reserved.
 * The use of this source code is governed by the Limited License located at
 * https://www.datatorrent.com/datatorrent-openview-software-license/
 */

package com.datatorrent.moodi.io.fs;

import javax.validation.constraints.NotNull;

import org.apache.hadoop.conf.Configuration;

import com.datatorrent.api.Context.PortContext;
import com.datatorrent.api.DAG;
import com.datatorrent.api.Module;
import com.datatorrent.lib.io.block.AbstractBlockReader.ReaderRecord;
import com.datatorrent.lib.io.block.BlockMetadata;
import com.datatorrent.lib.io.fs.AbstractFileSplitter.FileMetadata;
import com.datatorrent.lib.io.fs.Synchronizer;
import com.datatorrent.netlet.util.Slice;

/**
 * HDFS file copy module can be used in conjunction with file input modules to
 * copy files from any file system to HDFS. This module supports parallel write
 * to multiple blocks of the same file and then stitching those blocks in
 * original sequence.
 *
 * Essential operators are wrapped into single component using Module API.
 *
 *
 * @since 3.4.0
 */
@org.apache.hadoop.classification.InterfaceStability.Evolving
public class HDFSFileCopyModule implements Module
{

  /**
   * Path of the output directory. Relative path of the files copied will be
   * maintained w.r.t. source directory and output directory
   */
  @NotNull
  protected String outputDirectoryPath;

  /**
   * Flag to control if existing file with same name should be overwritten
   */
  private boolean overwriteOnConflict = true;

  /**
   * Relative path of blocks directory w.r.t. app directory. This will be used
   * for temporary storage for blocks.
   */
  private String blocksDirectory = BlockWriter.DEFAULT_BLOCKS_DIR;

  /**
   * Input port for files metadata.
   */
  public final transient ProxyInputPort<FileMetadata> filesMetadataInput = new ProxyInputPort<FileMetadata>();

  /**
   * Input port for blocks metadata
   */
  public final transient ProxyInputPort<BlockMetadata.FileBlockMetadata> blocksMetadataInput = new ProxyInputPort<BlockMetadata.FileBlockMetadata>();

  /**
   * Input port for blocks data
   */
  public final transient ProxyInputPort<ReaderRecord<Slice>> blockData = new ProxyInputPort<ReaderRecord<Slice>>();

  @Override
  public void populateDAG(DAG dag, Configuration conf)
  {

    BlockWriter blockWriter = dag.addOperator("BlockWriter", new BlockWriter());
    Synchronizer synchronizer = dag.addOperator("BlockSynchronizer", new Synchronizer());

    dag.setInputPortAttribute(blockWriter.input, PortContext.PARTITION_PARALLEL, true);
    dag.setInputPortAttribute(blockWriter.blockMetadataInput, PortContext.PARTITION_PARALLEL, true);
    dag.addStream("CompletedBlockmetadata", blockWriter.blockMetadataOutput, synchronizer.blocksMetadataInput);

    HDFSFileMerger merger = new HDFSFileMerger();
    merger = dag.addOperator("FileMerger", merger);
    dag.addStream("MergeTrigger", synchronizer.trigger, merger.input);

    merger.setFilePath(outputDirectoryPath);
    merger.setOverwriteOnConflict(overwriteOnConflict);
    blockWriter.setBlocksDirectory(blocksDirectory);
    merger.setBlocksDirectory(blocksDirectory);

    filesMetadataInput.set(synchronizer.filesMetadataInput);
    blocksMetadataInput.set(blockWriter.blockMetadataInput);
    blockData.set(blockWriter.input);
  }

  /**
   * Path of the output directory. Relative path of the files copied will be
   * maintained w.r.t. source directory and output directory
   *
   * @return output directory path
   */
  public String getOutputDirectoryPath()
  {
    return outputDirectoryPath;
  }

  /**
   * Path of the output directory. Relative path of the files copied will be
   * maintained w.r.t. source directory and output directory
   *
   * @param outputDirectoryPath
   *          output directory path
   */
  public void setOutputDirectoryPath(String outputDirectoryPath)
  {
    this.outputDirectoryPath = outputDirectoryPath;
  }

  /**
   * Flag to control if existing file with same name should be overwritten
   *
   * @return Flag to control if existing file with same name should be
   *         overwritten
   */
  public boolean isOverwriteOnConflict()
  {
    return overwriteOnConflict;
  }

  /**
   * Flag to control if existing file with same name should be overwritten
   *
   * @param overwriteOnConflict
   *          Flag to control if existing file with same name should be
   *          overwritten
   */
  public void setOverwriteOnConflict(boolean overwriteOnConflict)
  {
    this.overwriteOnConflict = overwriteOnConflict;
  }

  /**
   * Relative path of blocks directory w.r.t. app directory. This will be used
   * for temporary storage for blocks.
   * @return Relative path of blocks directory
   */
  public String getBlocksDirectory()
  {
    return blocksDirectory;
  }

  /**
   * Relative path of blocks directory w.r.t. app directory. This will be used
   * for temporary storage for blocks.
   * @param blocksDirectory Relative path of blocks directory
   */
  public void setBlocksDirectory(String blocksDirectory)
  {
    this.blocksDirectory = blocksDirectory;
  }

}
