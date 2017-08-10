/*
 * Copyright (c) 2012-2017 DataTorrent, Inc.
 * All Rights Reserved.
 * The use of this source code is governed by the Limited License located at
 * https://www.datatorrent.com/datatorrent-openview-software-license/
 */

package com.datatorrent.image;

import java.awt.image.BufferedImage;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.coobird.thumbnailator.Thumbnails;

/**
 * This operator can be used to resize an image to the desired resolution be specifying
 * the width and height or scale  properties
 * in the properties.xml.
 * It uses net.coobird.thumbnailator.*  libraries for resizing the images.
 * Usage:
 * Set the width and height property before launch to resize the image to the desired resolution.
 * OR
 * Set the scale property to resize the image while maintaining the original aspect ratio.
 *
 */

@org.apache.hadoop.classification.InterfaceStability.Evolving
public class ImageResizeOperator extends AbstractImageProcessingOperator
{
  private int width = 0;
  private int height = 0;
  public double scale = 1;

  public double getScale()
  {
    return scale;
  }

  /**
   * Set the ratio at which the image will be scaled. Maintains the original aspect ratio of the image.
   * @param scale
   */
  public void setScale(double scale)
  {
    this.scale = scale;
  }

  public int getWidth()
  {
    return width;
  }

  /**
   * Width of resized image in pixels.
   * @param width
   */
  public void setWidth(int width)
  {
    this.width = width;
  }

  public int getHeight()
  {
    return height;
  }

  /**
   * Height of resized image in pixels.
   * @param height
   */
  public void setHeight(int height)
  {
    this.height = height;
  }

  protected void resize(Data data)
  {
    try {
      bufferedImage = byteArrayToBufferedImage(data.bytesImage);
      BufferedImage resizedImage;
      if (height == width && width == 0) {
        resizedImage = Thumbnails.of(bufferedImage).scale(scale).asBufferedImage();
      } else {
        resizedImage = Thumbnails.of(bufferedImage).size(width, height).asBufferedImage();
      }
      data.bytesImage = bufferedImageToByteArray(resizedImage,data.imageType);
      output.emit(data);
    } catch (IOException e) {
      throw new RuntimeException("Error image not available to Thumbnails.of() " + e);
    }
  }

  @Override
  void processTuple(Data data)
  {
    resize(data);
  }

}
