package com.main

import org.apache.spark.{SparkConf, SparkContext}

/**
  * Created by carofr01 on 2/15/2017.
  */
object main extends App{

  val conf = new SparkConf().setAppName("CountWork").setMaster("local[*]")
  val sc = new SparkContext(conf)
  sc.textFile("path-to-input")
    .map(line => (line,1))
    .reduceByKey((a,b) => a+b)
    .foreach(println)

}
