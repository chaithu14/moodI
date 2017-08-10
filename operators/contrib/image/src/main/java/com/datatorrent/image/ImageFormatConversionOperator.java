/*
 * Copyright (c) 2012-2017 DataTorrent, Inc.
 * All Rights Reserved.
 * The use of this source code is governed by the Limited License located at
 * https://www.datatorrent.com/datatorrent-openview-software-license/
 */

package com.datatorrent.image;

import java.awt.HeadlessException;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ij.IJ;
import ij.ImagePlus;
import ij.io.Opener;
import ij.process.ImageProcessor;

/**
 * This operator uses ImageJ libraries for converting images to the desired file format.
 * The following image formats are supported jpeg,png,tiff,gif,fits,raw,avi,bmp.
 *
 * Usage:
 * Convert an image to and from any of the supported file formats.
 * by specifying the toFileType property in properties.xml
 *
 */
@org.apache.hadoop.classification.InterfaceStability.Evolving
public class ImageFormatConversionOperator extends AbstractImageProcessingOperator
{
  @NotNull
  public String toFileType;

  public String getToFileType()
  {
    return toFileType;
  }

  /**
   * Sets the resultant image format.(eg. png)
   * @param toFileType
   */
  public void setToFileType(String toFileType)
  {
    this.toFileType = toFileType;
  }

  protected void converter(Data data)
  {
    String fromFileType = data.imageType;
    if (!fromFileType.contains("fit")) {
      bufferedImage = byteArrayToBufferedImage(data.bytesImage);
      ImagePlus imgPlus = new ImagePlus("source", bufferedImage);
      try {
        IJ.saveAs(imgPlus, toFileType, "");
      } catch (HeadlessException h) {
        throw new RuntimeException("imageJ is not running in headless mode" + h + "/n");
      }
      ImageProcessor imageProcessor = imgPlus.getProcessor();
      BufferedImage bufferedImage1;
      ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
      try {
        bufferedImage1 = (BufferedImage)imageProcessor.createImage();
        ImageIO.write(bufferedImage1, toFileType, byteArrayOutputStream);
      } catch (IOException e) {
        throw new RuntimeException("Error in writing image to byteArray" + e);
      }
      data.bytesImage = byteArrayOutputStream.toByteArray();
      data.fileName = data.fileName.replace(fromFileType, toFileType);
      data.imageType = toFileType;
      try {
        byteArrayOutputStream.flush();
        byteArrayOutputStream.reset();
        byteArrayOutputStream.close();
        imageProcessor.reset();
      } catch (IOException e) {
        throw new RuntimeException("Error in closing byteArrayOutputStream" + e);
      }
      output.emit(data);
    } else {
      ImagePlus imgPlus = new Opener().deserialize(data.bytesImage);
      try {
        IJ.saveAs(imgPlus, toFileType, "");
      } catch (Exception e) {
        throw new RuntimeException("Error in saving image with imageJ  " + e);
      }
      ImageProcessor imageProcessor = imgPlus.getProcessor();
      BufferedImage bufferedImage1;
      ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
      try {
        bufferedImage1 = (BufferedImage)imageProcessor.createImage();
        ImageIO.write(bufferedImage1, toFileType, byteArrayOutputStream);
      } catch (IOException e) {
        throw new RuntimeException("Error in writing image to byteArray" + e);
      }
      data.bytesImage = byteArrayOutputStream.toByteArray();
      data.fileName = data.fileName.replace(fromFileType, toFileType);
      data.imageType = toFileType;
      try {
        byteArrayOutputStream.flush();
        byteArrayOutputStream.reset();
        byteArrayOutputStream.close();
      } catch (IOException e) {
        throw new RuntimeException("Error in writing image to byteArray", e );
      }
      output.emit(data);
    }
  }

  @Override
  void processTuple(Data data)
  {
    converter(data);
  }
}
