import com.lmax.disruptor.dsl.{Disruptor, ProducerType}
import com.lmax.disruptor.{RingBuffer, BlockingWaitStrategy, BusySpinWaitStrategy, YieldingWaitStrategy, SleepingWaitStrategy}
import java.nio.ByteBuffer
import java.util.concurrent.{Executor,Executors}

object Java8 extends App {
  val executor = 
    //Executors.newCachedThreadPool()
    Executors.newFixedThreadPool(2)

  val bufferSize = 1024;

  val disruptor = new Disruptor(
    () => TimestampedLongEvent(0L, 0L),
    bufferSize,
    executor,
    ProducerType.SINGLE, // ProducerType.MULTI
    //new SleepingWaitStrategy()
    //new BlockingWaitStrategy()
    new BusySpinWaitStrategy()
    //new YieldingWaitStrategy()
    )

  disruptor.handleEventsWith(
      (event, sequence, endOfBatch) => println("Event: " + event))

  disruptor.start()

  val ringBuffer = disruptor.getRingBuffer()

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
