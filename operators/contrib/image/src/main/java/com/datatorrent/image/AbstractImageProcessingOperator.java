/*
 * Copyright (c) 2012-2017 DataTorrent, Inc.
 * All Rights Reserved.
 * The use of this source code is governed by the Limited License located at
 * https://www.datatorrent.com/datatorrent-openview-software-license/
 */

package com.datatorrent.image;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import javax.imageio.ImageIO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.datatorrent.api.DefaultInputPort;
import com.datatorrent.api.DefaultOutputPort;
import com.datatorrent.common.util.BaseOperator;

/**
 * This class handles the input and output of all the image processing operators under image.
 * It provides additional read/write methods and sets file path, file name, file type to make sure all operators are
 * compatible with each other.
 * All image processing operators should extend this class and override the processTuple(Data data) method.
 *
 */
@org.apache.hadoop.classification.InterfaceStability.Evolving
public abstract class AbstractImageProcessingOperator extends BaseOperator
{
  public  String fileType;
  public final transient DefaultOutputPort<Data> output = new DefaultOutputPort<Data>();
  public String filePath;
  public transient BufferedImage bufferedImage = null;
  public int bufferedImageType;
  public final transient DefaultInputPort<Data> input = new DefaultInputPort<Data>()
  {

    @Override
    public void process(Data tuple)
    {
      filePath = tuple.fileName;
      fileType = tuple.imageType;
      processTuple(tuple);
    }
  };

  public BufferedImage byteArrayToBufferedImage(byte[] imageInBytes)
  {
    byte[] byteImage = imageInBytes;
    InputStream in = new ByteArrayInputStream(byteImage);
    try {
      bufferedImage = ImageIO.read(in);
      in.reset();
      in.close();
    } catch (IOException e) {
      throw new RuntimeException("Error in reading file " + e);
    }
    return bufferedImage;
  }

  public byte[] bufferedImageToByteArray(BufferedImage bufferedImage, String fileType)
  {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    try {
      ImageIO.write(bufferedImage,fileType, baos);
    } catch (IOException e) {
      throw new RuntimeException("Error in writing image to byte array " + e);
    }
    return baos.toByteArray();
  }

  abstract void processTuple(Data data);
}
