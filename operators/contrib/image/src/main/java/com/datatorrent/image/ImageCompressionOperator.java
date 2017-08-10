/*
 * Copyright (c) 2012-2017 DataTorrent, Inc.
 * All Rights Reserved.
 * The use of this source code is governed by the Limited License located at
 * https://www.datatorrent.com/datatorrent-openview-software-license/
 */

package com.datatorrent.image;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Iterator;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.MemoryCacheImageOutputStream;
import javax.validation.constraints.NotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This Operator can be used to compress lossy images (example: jpeg).
 * Compression quality can be set by assigning desired float value to the compressionRatio property in properties.xml
 * 1.0f is the maximum quality image i.e lowest compression.
 * 0.0f is the minimum quality image i.e maximum compression.
 */
@org.apache.hadoop.classification.InterfaceStability.Evolving
public class ImageCompressionOperator extends AbstractImageProcessingOperator
{
  @NotNull
  public float compressionRatio;

  public float getCompressionRatio()
  {
    return compressionRatio;
  }

  /**
   * Sets compressionRatio value set in properties.xml
   * @param compressionRatio
   */
  public void setCompressionRatio(float compressionRatio)
  {
    this.compressionRatio = compressionRatio;
  }

  public void compress(Data data)
  {
    if (data != null) {
      bufferedImage = byteArrayToBufferedImage(data.bytesImage);
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName(data.imageType);
      ImageWriter writer = writers.next();
      writer.setOutput(new MemoryCacheImageOutputStream(baos));
      ImageWriteParam param = writer.getDefaultWriteParam();
      if (param.canWriteCompressed()) {
        param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        param.setCompressionQuality(compressionRatio);
      }
      try {
        writer.write(null, new IIOImage(bufferedImage, null, null), param);
        writer.dispose();
        data.bytesImage = baos.toByteArray();
        baos.flush();
        baos.reset();
        baos.close();
        output.emit(data);
      } catch (IOException e) {
      throw new RuntimeException("Error in writing image to byte array" + e.getMessage());
      }
    }
  }

  @Override
  void processTuple(Data data)
  {
    compress(data);
  }
}
