/*
 * Copyright (c) 2012-2017 DataTorrent, Inc.
 * All Rights Reserved.
 * The use of this source code is governed by the Limited License located at
 * https://www.datatorrent.com/datatorrent-openview-software-license/
 */

package com.datatorrent.apps;

@org.apache.hadoop.classification.InterfaceStability.Evolving
public class PojoEvent
{
  @Override
  public String toString()
  {
    return "PojoEvent [accountNumber=" + accountNumber + ", name=" + name + ", amount=" + amount + "]";
  }

  private int accountNumber;
  private String name;
  private int amount;

  public int getAccountNumber()
  {
    return accountNumber;
  }

  public void setAccountNumber(int accountNumber)
  {
    this.accountNumber = accountNumber;
  }

  public String getName()
  {
    return name;
  }

  public void setName(String name)
  {
    this.name = name;
  }

  public int getAmount()
  {
    return amount;
  }

  public void setAmount(int amount)
  {
    this.amount = amount;
  }
}
