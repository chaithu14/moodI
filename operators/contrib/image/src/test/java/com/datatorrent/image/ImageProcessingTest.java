/*
 * Copyright (c) 2012-2017 DataTorrent, Inc.
 * All Rights Reserved.
 * The use of this source code is governed by the Limited License located at
 * https://www.datatorrent.com/datatorrent-openview-software-license/
 */

package com.datatorrent.image;
/**
 * Tests for basic image processing operators.
 */

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.commons.io.FileUtils;


@org.apache.hadoop.classification.InterfaceStability.Evolving
public class ImageProcessingTest extends ImageResizeOperator
{
  private Data data = new Data();
  File imageFile;
  private static final Logger LOG = LoggerFactory.getLogger(ImageProcessingTest.class);

  @Before
  public void setup()
  {
    String filePath;
    String[] compatibleFileTypes = {"jpg", "png", "jpeg", "fits", "gif", "tif"};
    BufferedImage bufferedImage = null;
    imageFile = new File("src/test/resources/TestImages/TestImage.jpg");
    filePath = imageFile.getAbsolutePath();
    try {
      bufferedImageType = ImageIO.read(imageFile).getType();
      bufferedImage = ImageIO.read(imageFile);
    } catch (IOException e) {
      LOG.debug("Error in writing image to byte array " + e);
    }
    for (int i = 0; i < compatibleFileTypes.length; i++) {
      if ( filePath.toString().toLowerCase().endsWith(compatibleFileTypes[i])) {
        data.imageType = compatibleFileTypes[i];
        if (data.imageType.equalsIgnoreCase("jpeg")) {
          data.imageType = "jpg";
        }
      }
    }
    data.fileName = imageFile.getName();
    data.bytesImage = bufferedImageToByteArray(bufferedImage, data.imageType);
  }

  @Test
  public void resizeTest()
  {
    ImageResizeOperator imageResizer = new ImageResizeOperator();
    imageResizer.scale = 0.5;
    BufferedImage original = byteArrayToBufferedImage(data.bytesImage);
    imageResizer.resize(data);
    BufferedImage result = byteArrayToBufferedImage(data.bytesImage);
    Assert.assertEquals((original.getWidth() * imageResizer.scale), (double)result.getWidth(), 0.0);
  }

  @Test
  public void compressTest()
  {
    File compressedFile = new File("src/test/resources/TestImages/CompressedTestImage.jpg");
    ImageCompressionOperator compress = new ImageCompressionOperator();
    compress.compressionRatio = 0.9f;
    compress.compress(data);
    try {
      FileUtils.writeByteArrayToFile(compressedFile, data.bytesImage);
    } catch (IOException e) {
      LOG.debug("Error in writing image to byte array" + e);
    }
    Assert.assertEquals(true, (imageFile.length() > compressedFile.length()));
    Assert.assertEquals(true, (compress.compressionRatio < 1f));

  }

  @Test
  public void fileFormatConversionTest()
  {
    ImageFormatConversionOperator fileFormatConverter = new ImageFormatConversionOperator();
    fileFormatConverter.toFileType = "png";
    fileFormatConverter.converter(data);
    Assert.assertEquals(true, (data.imageType.equalsIgnoreCase(fileFormatConverter.toFileType)));

  }
}
