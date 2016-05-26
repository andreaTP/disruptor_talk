import com.lmax.disruptor.dsl.{Disruptor, ProducerType}
import com.lmax.disruptor.{RingBuffer, BlockingWaitStrategy, BatchEventProcessor}
import java.nio.ByteBuffer
import java.util.concurrent.{Executor,Executors}

object Sequence extends App {
  val executor = 
    //Executors.newCachedThreadPool()
    Executors.newFixedThreadPool(2)

  val bufferSize = 1024;

  val disruptor = new Disruptor(
    () => TimestampedLongEvent(0L, 0L),
    bufferSize,
    executor,
    ProducerType.SINGLE,
    new BlockingWaitStrategy()
    )

  val ringBuffer = disruptor.getRingBuffer()

  /*
    disruptor.handleEventsWith(
      (event: TimestampedLongEvent, sequence: Long, endOfBatch: Boolean) => {
          println("Event1: " + event)})

    disruptor.handleEventsWith(
      (event: TimestampedLongEvent, sequence: Long, endOfBatch: Boolean) => {
          println("Event2: " + event)})
  */
  
  disruptor.handleEventsWith(
    (event: TimestampedLongEvent, sequence: Long, endOfBatch: Boolean) => {
        println("Event1: " + event)}).then(
    (event: TimestampedLongEvent, sequence: Long, endOfBatch: Boolean) => {
        println("Event2: " + event)}
  )

  /*
  val barrier = ringBuffer.newBarrier()

  val customProcessor = new BatchEventProcessor(ringBuffer, barrier, 
    (event: TimestampedLongEvent, sequence: Long, endOfBatch: Boolean) => {
        println("Event1: " + event)})

  disruptor.after(customProcessor).handleEventsWith(
      (event, sequence, endOfBatch) => {
        println("Event2: " + event)
      })
  */

  disruptor.start()


  val bb = ByteBuffer.allocate(16)
         
  for (i <- 0L until 10L) {
      bb.putLong(0, System.currentTimeMillis)
      bb.putLong(8, i)

      ringBuffer.publishEvent(
        (event: TimestampedLongEvent, sequence: Long, buffer: ByteBuffer) => {
          event.timestamp = buffer.getLong(0)
          event.value = buffer.getLong(8)
        },
        bb)
      Thread.sleep(1000)
  }

  System.exit(0)
}
