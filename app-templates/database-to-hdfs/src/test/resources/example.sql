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

/**
 * Cleanup and create new database.
 */
DROP DATABASE IF EXISTS testdb;

CREATE DATABASE testdb;

/**
 * Connect to database and create table along with some values.
 */
\c testdb;

CREATE TABLE IF NOT EXISTS test_event_table (
  ACCOUNT_NO integer PRIMARY KEY NOT NULL,
  NAME varchar(255) DEFAULT NULL,
  AMOUNT integer DEFAULT NULL
);

INSERT INTO test_event_table (ACCOUNT_NO, NAME, AMOUNT) VALUES
(1, 'User11', 1000),
(2, 'User12', 2000),
(3, 'User13', 3000),
(4, 'User14', 4000),
(5, 'User15', 5000),
(6, 'User16', 6000),
(7, 'User17', 7000),
(8, 'User18', 8000),
(9, 'User19', 9000),
(10, 'User110', 1000);
