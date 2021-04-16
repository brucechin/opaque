name := "opaque"

version := "0.1"

organization := "edu.berkeley.cs.amplab"

scalaVersion := "2.11.8"

spName := "amplab/opaque"

sparkVersion := "2.0.2"

sparkComponents ++= Seq("core", "sql", "catalyst")

libraryDependencies += "org.scalatest" %% "scalatest" % "2.2.6" % "test"

parallelExecution := false

// This fixes a class loader problem with scala.Tuple2 class, scala-2.11, Spark 2.x
fork in Test := true
fork in run := true

scalacOptions ++= Seq("-g:vars")
javaOptions ++= Seq("-Xdebug", "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=8000")
// This and the next line fix a problem with forked run: https://github.com/scalatest/scalatest/issues/770
javaOptions in Test ++= Seq("-Xmx62048m", "-XX:ReservedCodeCacheSize=384m", "-XX:MaxPermSize=384m")
javaOptions in run ++= Seq("-Xmx60048m", "-XX:ReservedCodeCacheSize=384m", "-Dspark.master=local[1]")
// Enclave C++ build

// Include Spark dependency for `build/sbt run`, though it is marked as "provided" for use with
// spark-submit. From
// https://github.com/sbt/sbt-assembly/blob/4a211b329bf31d9d5f0fae67ea4252896d8a4a4d/README.md
run in Compile := Defaults.runTask(
  fullClasspath in Compile,
  mainClass in (Compile, run),
  runner in (Compile, run)).evaluated

scalacOptions ++= Seq(
  "-deprecation",
  "-encoding", "UTF-8",
  "-feature",
  "-unchecked",
  "-Xfuture",
  "-Xlint:_",
  "-Ywarn-adapted-args",
  "-Ywarn-dead-code",
  "-Ywarn-inaccessible",
  "-Ywarn-infer-any",
  "-Ywarn-nullary-override",
  "-Ywarn-nullary-unit",
  "-Ywarn-unused-import"
)
scalacOptions in (Compile, console) := Seq.empty


val enclaveBuildTask = TaskKey[Unit]("enclaveBuild", "Builds the C++ enclave code")







initialCommands in console :=
  """
    |import org.apache.spark.SparkContext
    |import org.apache.spark.sql.SQLContext
    |import edu.berkeley.cs.rise.opaque.Utils
    |import edu.berkeley.cs.rise.opaque.benchmark._
    |import org.apache.spark.sql.catalyst.analysis._
    |import org.apache.spark.sql.catalyst.dsl._
    |import org.apache.spark.sql.catalyst.errors._
    |import org.apache.spark.sql.SparkSession
    |import org.apache.spark.sql.catalyst.expressions._
    |import org.apache.spark.sql.catalyst.plans.logical._
    |import org.apache.spark.sql.catalyst.rules._
    |import org.apache.spark.sql.catalyst.util._
    |import org.apache.spark.sql.execution
    |import org.apache.spark.sql.functions._
    |import org.apache.spark.sql.types._
    |import org.apache.log4j.Level
    |import org.apache.log4j.LogManager
    |import edu.berkeley.cs.rise.opaque.benchmark._
    |LogManager.getLogger("org.apache.spark").setLevel(Level.WARN)
    |LogManager.getLogger("org.apache.spark.executor.Executor").setLevel(Level.WARN)
    |
    |        val spark = (org.apache.spark.sql.SparkSession.builder().master("local").appName("Opaque shell").getOrCreate())    
    |
    |    Utils.initSQLContext(spark.sqlContext)
    |    LogManager.getLogger("org.apache.spark").setLevel(Level.ERROR)
    |    LogManager.getLogger("org.apache.spark.executor.Executor").setLevel(Level.ERROR)
    |val sc = spark.sparkContext
    |val sqlContext = spark.sqlContext
    |
    |import spark.implicits._
    |
    |import edu.berkeley.cs.rise.opaque.implicits._
    |edu.berkeley.cs.rise.opaque.Utils.initSQLContext(sqlContext)
  """.stripMargin

cleanupCommands in console := "spark.stop()"

enclaveBuildTask := {
  import sys.process._
  val ret = Seq("src/enclave/build.sh").!
  if (ret != 0) sys.error("C++ build failed.")
  IO.copyFile(
    baseDirectory.value / "src" / "enclave" / "libSGXEnclave.so",
    baseDirectory.value / "libSGXEnclave.so")
  IO.copyFile(
    baseDirectory.value / "src" / "enclave" / "enclave.signed.so",
    baseDirectory.value / "enclave.signed.so")
  // IO.copyFile(
  //   baseDirectory.value / "src" / "enclave" / "libservice_provider.so",
  //   baseDirectory.value / "libservice_provider.so")
}

baseDirectory in enclaveBuildTask := (baseDirectory in ThisBuild).value

compile in Compile := { (compile in Compile).dependsOn(enclaveBuildTask).value }

// Watch the enclave C++ files
watchSources ++=
  ((baseDirectory.value / "src/enclave") ** (("*.cpp" || "*.h" || "*.tcc" || "*.edl")
      -- "Enclave_u.h"
      -- "Enclave_t.h"
      -- "key.cpp")).get

// Synthesize test data
val synthTestDataTask = TaskKey[Unit]("synthTestData", "Synthesizes test data")

synthTestDataTask := {
  val diseaseDataFiles =
    for {
      diseaseDir <- (baseDirectory.value / "data" / "disease").get
      name <- Seq("disease.csv", "gene.csv", "treatment.csv", "patient-125.csv")
    } yield new File(diseaseDir, name)
  if (!diseaseDataFiles.forall(_.exists)) {
    import sys.process._
    val ret = Seq("data/disease/synth-disease-data").!
    if (ret != 0) sys.error("Failed to synthesize test data.")
  }
}

test in Test := { (test in Test).dependsOn(synthTestDataTask).value }
