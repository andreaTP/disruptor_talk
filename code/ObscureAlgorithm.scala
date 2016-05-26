
import math.sqrt
import math.pow

import java.nio.ByteBuffer
import java.util.concurrent.CountDownLatch

import com.lmax.disruptor.dsl.{Disruptor, ProducerType}
import com.lmax.disruptor.{RingBuffer, YieldingWaitStrategy, SleepingWaitStrategy}
import java.util.concurrent.{Executor,Executors}

import scala.io.Source
import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken

object Algo extends App {
  val n = 10
  val iters = 15

  val pointSize = 100000

  case class Point(var x: Double, var y: Double) {

    var id = 0

    def /(k: Double): Point = new Point(x / k, y / k)

    def +(p: Point) = new Point(x + p.x, y + p.y)
    def -(p: Point) = new Point(x - p.x, y - p.y)

    def modulus = sqrt(sq(x) + sq(y))
  }

  def closest(x: Point, choices: List[Point]): Int =
    (choices.zipWithIndex.minBy { y => dist(x, y._1) })._2

  def sq(x: Double) = x * x

  def dist(x: Point, y: Point) = (x - y).modulus

  var count = new CountDownLatch(pointSize)

  var centroids: List[Point] = List()

  val mapDisruptors = 
    for (_ <- 0 until 4) yield
      new Disruptor(
        () => new Point(0L, 0L),
        pow(2, 20).toInt,
        Executors.newCachedThreadPool,
        ProducerType.SINGLE,
        new YieldingWaitStrategy()
      )

  val mapRB = mapDisruptors.map(_.getRingBuffer())

  val reduceDisruptors =
    for (_ <- 0 until n) yield
      new Disruptor(
        () => new Point(0L, 0L),
        pow(2, 20).toInt,
        Executors.newCachedThreadPool,
        ProducerType.MULTI,
        //new SleepingWaitStrategy()
        new YieldingWaitStrategy()
      )

  val reduceRB = reduceDisruptors.map(_.getRingBuffer())

  val reducesSums =
    (for (_ <- 0 until n) yield (Point(0.0,0.0), 0)).toArray

  for (i <- 0 until n)
    reduceDisruptors(i).handleEventsWith(
      (event, sequence, endOfBatch) => {
        reducesSums(i) = (reducesSums(i)._1 + event, reducesSums(i)._2 + 1)
        count.countDown
      }
    )

  for (i <- 0 until 4) {
    val bb = ByteBuffer.allocate(16)
    mapDisruptors(i).handleEventsWith(
      (event, sequence, endOfBatch) => {
        if (event.id == i) {
          bb.putDouble(0, event.x)
          bb.putDouble(8, event.y)

          reduceRB(closest(event, centroids)).publishEvent(
            (event: Point, sequence: Long, buffer: ByteBuffer) => {
              event.x = buffer.getDouble(0)
              event.y = buffer.getDouble(8)
            },
            bb)
        }
      }
    )
  }

/*
  for (x <- mapDisruptors)
    x.start()
*/
  for (x <- reduceDisruptors)
    x.start()

  val bb = ByteBuffer.allocate(24)

  def clusters(xs: List[Point]) = {
    count = new CountDownLatch(pointSize)

    var i = 0
    var c = xs.size - 1
    while (c >= 0) {
      val p = xs(c)
      bb.putDouble(0, p.x)
      bb.putDouble(8, p.y)
      //bb.putInt(16, i)
      
      reduceRB(closest(xs(c)/*event*/, centroids)).publishEvent(
        (event: Point, sequence: Long, buffer: ByteBuffer) => {
          event.x = buffer.getDouble(0)
          event.y = buffer.getDouble(8)
        },
        bb)
/*
      mapRB(i).publishEvent(
        (event: Point, sequence: Long, buffer: ByteBuffer) => {
          event.x = buffer.getDouble(0)
          event.y = buffer.getDouble(8)
          event.id = buffer.getInt(16)
        },
        bb)
*/
      if (i + 1 > 3) i = 0
      else i += 1

      c -= 1
    }

    count.await

    centroids =
      (for (x <- reducesSums) yield (x._1 / x._2.toDouble)).toList

    println("New Centroids calculated!")

    for (p <- centroids) {
      println(s"Point( ${p.x}, ${p.y} )")
    }
  }

  def run(xs: List[Point]) = {
    centroids = xs take n

    for (i <- 1 to iters) {
      clusters(xs)
    }

    clusters(xs)
  }

  val factory = new JsonFactory()
  val jp = factory.createJsonParser(new java.io.File("/home/andrea/workspace/kmeans/points.json"))

  var points = List[Point]()
  var i = 0
  var exit = false
  while (true && !exit) {
    var actual = jp.nextValue()
    while (actual == JsonToken.START_ARRAY) {
      actual = jp.nextValue()
    }
      try {
        val x = jp.getDoubleValue()
        jp.nextToken();
        val y = jp.getDoubleValue()
        points = points :+ new Point(x,y)
        i += 1
      } catch {
        case _ : Throwable => 
          exit = true
      }
    if (!exit) {
      actual = jp.nextToken()
      while (jp.nextToken() == JsonToken.END_ARRAY) {
        actual = jp.nextToken()
      }
    }
  }
  jp.close()

  val iterations = 1

  println("points -> "+points.size)
  val start = System.currentTimeMillis
  for (i <- 1 to iterations) {
    run(points)
  }
  val time = (System.currentTimeMillis - start) / iterations

  println(s"Made $iterations iterations with an average of $time milliseconds")

  System.exit(0)
}
