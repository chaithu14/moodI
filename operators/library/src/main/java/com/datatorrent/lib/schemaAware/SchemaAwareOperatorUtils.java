/*
 * Copyright (c) 2012-2017 DataTorrent, Inc.
 * All Rights Reserved.
 * The use of this source code is governed by the Limited License located at
 * https://www.datatorrent.com/datatorrent-openview-software-license/
 */

package com.datatorrent.lib.schemaAware;

import java.util.Date;

import com.datatorrent.contrib.parser.Schema.FieldType;


@org.apache.hadoop.classification.InterfaceStability.Evolving
public class SchemaAwareOperatorUtils
{
  public static Class getClass(FieldType fieldType)
  {
    switch (fieldType) {
      case BOOLEAN:
        return Boolean.class;
      case DOUBLE:
        return Double.class;
      case INTEGER:
        return Integer.class;
      case FLOAT:
        return Float.class;
      case LONG:
        return Long.class;
      case SHORT:
        return Short.class;
      case CHARACTER:
        return Character.class;
      case STRING:
        return String.class;
      case DATE:
        return Date.class;
      default:
        return null;
    }
  }

}
