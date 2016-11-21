/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

DROP DATABASE IF EXISTS testdb;

CREATE DATABASE testdb;

\c testdb;

CREATE TABLE IF NOT EXISTS test_event_output_table (
  ACCOUNT_NO integer PRIMARY KEY NOT NULL,
  NAME varchar(255) DEFAULT NULL,
  AMOUNT integer DEFAULT NULL
);


CREATE TABLE dt_meta (
  dt_app_id varchar(100) NOT NULL,
  dt_operator_id int NOT NULL,
  dt_window bigint NOT NULL,
  CONSTRAINT dt_app_id UNIQUE (dt_app_id,dt_operator_id,dt_window)
);
