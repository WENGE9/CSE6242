// Databricks notebook source
// Q2 [25 pts]: Analyzing a Large Graph with Spark/Scala on Databricks

// STARTER CODE - DO NOT EDIT THIS CELL
import org.apache.spark.sql.functions.desc
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types._
import spark.implicits._

// COMMAND ----------

// STARTER CODE - DO NOT EDIT THIS CELL
// Definfing the data schema
val customSchema = StructType(Array(StructField("answerer", IntegerType, true), StructField("questioner", IntegerType, true),
    StructField("timestamp", LongType, true)))

// COMMAND ----------

// STARTER CODE - YOU CAN LOAD ANY FILE WITH A SIMILAR SYNTAX.
// MAKE SURE THAT YOU REPLACE THE examplegraph.csv WITH THE mathoverflow.csv FILE BEFORE SUBMISSION.
val df = spark.read
   .format("com.databricks.spark.csv")
   .option("header", "false") // Use first line of all files as header
   .option("nullValue", "null")
   .schema(customSchema)
   .load("/FileStore/tables/mathoverflow.csv")
   .withColumn("date", from_unixtime($"timestamp"))
   .drop($"timestamp")


// COMMAND ----------

//display(df)
df.show()

// COMMAND ----------

// PART 1: Remove the pairs where the questioner and the answerer are the same person.
// ALL THE SUBSEQUENT OPERATIONS MUST BE PERFORMED ON THIS FILTERED DATA
val q1 = df.select("*").filter(df("answerer") =!= df("questioner")).toDF("answerer","questioner","date")
//deduplicated.collect.foreach(println)
// ENTER THE CODE BELOW



// COMMAND ----------

// PART 2: The top-3 individuals who answered the most number of questions - sorted in descending order - if tie, the one with lower node-id gets listed first : the nodes with the highest out-degrees.
val q2 = q1.groupBy(q1("answerer")).agg(count("*").alias("questions_answered")).orderBy($"questions_answered".desc,$"answerer".asc).show(3)
// ENTER THE CODE BELOW


// COMMAND ----------

// PART 3: The top-3 individuals who asked the most number of questions - sorted in descending order - if tie, the one with lower node-id gets listed first : the nodes with the highest in-degree.
val q3 = q1.groupBy(q1("questioner")).agg(count("*").alias("questions_asked")).orderBy($"questions_asked".desc,$"questioner".asc).show(3)
// ENTER THE CODE BELOW


// COMMAND ----------

// PART 4: The top-5 most common asker-answerer pairs - sorted in descending order - if tie, the one with lower value node-id in the first column (u->v edge, u value) gets listed first.
val q4 = q1.groupBy(q1("answerer"),q1("questioner")).agg(count("*").alias("count")).orderBy($"count".desc,$"answerer".asc,$"questioner".asc).show(5)
// ENTER THE CODE BELOW

// COMMAND ----------

// PART 5: Number of interactions (questions asked/answered) over the months of September-2010 to December-2010 (i.e. from September 1, 2010 to December 31, 2010). List the entries by month from September to December.
val q5_year = q1.withColumn("Date",date_format($"date","yyyy-MM-dd")).withColumn("year",year($"date")).filter($"year" === "2010").drop("year")
val q5_month = q5_year.withColumn("month",month($"Date")).filter($"month" >= 9)
val q5 = q5_month.groupBy($"month").agg(count("*").alias("total_interaction")).orderBy($"month".asc).show()
// Reference: https://www.obstkel.com/blog/spark-sql-date-functions
// Read in the data and extract the month and year from the date column.
// Hint: Check how we extracted the date from the timestamp.
// ENTER THE CODE BELOW


// COMMAND ----------

// PART 6: List the top-3 individuals with the maximum overall activity, i.e. total questions asked and questions answered.
val answerer_id = q1.groupBy(q1("answerer").alias("userID")).agg(count("*").alias("total_activity_answer"))
val questioner_id = q1.groupBy(q1("questioner").alias("userID")).agg(count("*").alias("total_activity_question"))
val joined = answerer_id.join(questioner_id,Seq("userID"),"fullouter").na.fill(0,Seq("total_activity_answer")).na.fill(0,Seq("total_activity_question"))
val q6 = joined.withColumn("total_activity",$"total_activity_answer"+$"total_activity_question").drop("total_activity_answer").drop("total_activity_question").orderBy($"total_activity".desc,$"userID".asc).show(3)

//.where(answerer_id("userID") === questioner_id("userID2")).drop("userID2").withColumn("total_activity",$"total_activity_answer"+$"total_activity_question").drop("total_activity_answer").drop("total_activity_question").orderBy($"total_activity".desc,$"userID".asc).show()


// COMMAND ----------


