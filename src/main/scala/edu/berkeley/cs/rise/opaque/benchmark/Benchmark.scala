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

/**
 * Convenient runner for benchmarks.
 *
 * To run locally, use
 * `$OPAQUE_HOME/build/sbt 'run edu.berkeley.cs.rise.opaque.benchmark.Benchmark'`.
 *
 * To run on a cluster, use `$SPARK_HOME/bin/spark-submit` with appropriate arguments.
 */
object Benchmark {
  def dataDir: String = {
    if (System.getenv("SPARKSGX_DATA_DIR") == null) {
      throw new Exception("Set SPARKSGX_DATA_DIR")
    }
    System.getenv("SPARKSGX_DATA_DIR")
  }

  def main(args: Array[String]): Unit = {
    val spark = SparkSession.builder()
      .appName("QEDBenchmark")
      .getOrCreate()
    Utils.initSQLContext(spark.sqlContext)

    // val numPartitions =
    //   if (spark.sparkContext.isLocal) 1 else spark.sparkContext.defaultParallelism

    val nums = Seq(1,2,3)
    for(n <- nums){
      BigDataBenchmark.q1(spark, Insecure, "10000", 1)
      BigDataBenchmark.q1(spark, Insecure, "100000", 1)
      BigDataBenchmark.q1(spark, Insecure, "1000000", 3)
      BigDataBenchmark.q1(spark, Insecure, "10000000", 10)
    }

    for(n <- nums){
      BigDataBenchmark.q2(spark, Insecure, "10000", 1)
      BigDataBenchmark.q2(spark, Insecure, "100000", 1)
      BigDataBenchmark.q2(spark, Insecure, "1000000", 3)
      BigDataBenchmark.q2(spark, Insecure, "10000000", 10)
    }
    for(n <- nums){
      BigDataBenchmark.q3(spark, Insecure, "10000", 1)
      BigDataBenchmark.q3(spark, Insecure, "100000", 1)
      BigDataBenchmark.q3(spark, Insecure, "1000000", 3)
      BigDataBenchmark.q3(spark, Insecure, "10000000", 10)
    }

    for(n <- nums){
      BigDataBenchmark.q1(spark, Encrypted, "10000", 1)
      BigDataBenchmark.q1(spark, Encrypted, "100000", 1)
      BigDataBenchmark.q1(spark, Encrypted, "1000000", 3)
      BigDataBenchmark.q1(spark, Encrypted, "10000000", 10)
    }

    for(n <- nums){
      BigDataBenchmark.q2(spark, Encrypted, "10000", 1)
      BigDataBenchmark.q2(spark, Encrypted, "100000", 1)
      BigDataBenchmark.q2(spark, Encrypted, "1000000", 3)
      BigDataBenchmark.q2(spark, Encrypted, "10000000", 10)
    }
    for(n <- nums){
      BigDataBenchmark.q3(spark, Encrypted, "10000", 1)
      BigDataBenchmark.q3(spark, Encrypted, "100000", 1)
      BigDataBenchmark.q3(spark, Encrypted, "1000000", 3)
      BigDataBenchmark.q3(spark, Encrypted, "10000000", 10)
    }



      
    spark.stop()
  }
}
