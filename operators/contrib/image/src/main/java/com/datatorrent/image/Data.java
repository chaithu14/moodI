/*
 * Copyright (c) 2012-2017 DataTorrent, Inc.
 * All Rights Reserved.
 * The use of this source code is governed by the Limited License located at
 * https://www.datatorrent.com/datatorrent-openview-software-license/
 */
package com.datatorrent.image;
/**
 * This is the standard POJO that flows across all image processing operators making them compatible with
 * each other.
 */
@org.apache.hadoop.classification.InterfaceStability.Evolving
public class Data
{
  byte[] bytesImage;
  String fileName;
  String imageType; // jpg,png etc
}
