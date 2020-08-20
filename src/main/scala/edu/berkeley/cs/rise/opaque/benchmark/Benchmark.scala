/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package edu.berkeley.cs.rise.opaque.benchmark

import edu.berkeley.cs.rise.opaque.Utils
import org.apache.spark.sql.SparkSession
import edu.berkeley.cs.rise.opaque.Utils
import org.apache.spark.sql.SparkSession
import org.apache.spark.SparkContext
import org.apache.spark.sql.SQLContext
import org.apache.spark.sql.catalyst.analysis._
import org.apache.spark.sql.catalyst.dsl._
import org.apache.spark.sql.catalyst.errors._
import org.apache.spark.sql.catalyst.expressions._
import org.apache.spark.sql.catalyst.plans.logical._
import org.apache.spark.sql.catalyst.rules._
import org.apache.spark.sql.catalyst.util._
import org.apache.spark.sql.execution
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types._
import org.apache.log4j.Level
import org.apache.log4j.LogManager

object Benchmark {
  def dataDir: String = {
    if (System.getenv("SPARKSGX_DATA_DIR") == null) {
      throw new Exception("Set SPARKSGX_DATA_DIR")
    }
    System.getenv("SPARKSGX_DATA_DIR")
  }

  def main(args: Array[String]) {
    val spark = SparkSession.builder().appName("QEDBenchmark").getOrCreate()
    Utils.initSQLContext(spark.sqlContext)
    LogManager.getLogger("org.apache.spark").setLevel(Level.WARN)
    LogManager.getLogger("org.apache.spark.executor.Executor").setLevel(Level.WARN)
    // val numPartitions =
    //   if (spark.sparkContext.isLocal) 1 else spark.sparkContext.defaultParallelism

    val numPartitions = 5

    // Warmup
    BigDataBenchmark.q2(spark, Encrypted, "tiny", numPartitions)
    BigDataBenchmark.q2(spark, Encrypted, "tiny", numPartitions)

    // Run
    BigDataBenchmark.q1(spark, Insecure, "1million", numPartitions)
    BigDataBenchmark.q1(spark, Encrypted, "1million", numPartitions)
    BigDataBenchmark.q1(spark, Oblivious, "1million", numPartitions)

    BigDataBenchmark.q2(spark, Insecure, "1million", numPartitions)
    BigDataBenchmark.q2(spark, Encrypted, "1million", numPartitions)
    BigDataBenchmark.q2(spark, Oblivious, "1million", numPartitions)

    BigDataBenchmark.q3(spark, Insecure, "1million", numPartitions)
    BigDataBenchmark.q3(spark, Encrypted, "1million", numPartitions)
    BigDataBenchmark.q3(spark, Oblivious, "1million", numPartitions)


    Thread.sleep(10000000)

    // if (spark.sparkContext.isLocal) {
    //   for (i <- 8 to 20) {
    //     PageRank.run(spark, Oblivious, math.pow(2, i).toInt.toString, numPartitions)
    //   }

    //   for (i <- 0 to 13) {
    //     JoinReordering.treatmentQuery(spark, (math.pow(2, i) * 125).toInt.toString, numPartitions)
    //     JoinReordering.geneQuery(spark, (math.pow(2, i) * 125).toInt.toString, numPartitions)
    //   }

    //   for (i <- 0 to 13) {
    //     JoinCost.run(spark, Oblivious, (math.pow(2, i) * 125).toInt.toString, numPartitions)
    //   }
    // }

    spark.stop()
  }
}
