import java.nio.ByteBuffer
import java.util.concurrent.{Executor, Executors}
import com.lmax.disruptor.{EventFactory, EventHandler, RingBuffer}
import com.lmax.disruptor.dsl.Disruptor

object Basic extends App {
  val executor = Executors.newCachedThreadPool()

  val factory = new TimestampedLongEventFactory()

  val bufferSize = 1024;

  val disruptor = new Disruptor(factory, bufferSize, executor)

  disruptor.handleEventsWith(new TimestampedLongEventHandler())

  disruptor.start()

  val ringBuffer = disruptor.getRingBuffer()

  val producer = new TimestampedLongEventProducer(ringBuffer)

  val bb = ByteBuffer.allocate(16)
         
  for (i <- 0L until 10L) {
      bb.putLong(0, System.currentTimeMillis)
      bb.putLong(8, i)
      producer.onData(bb)
      Thread.sleep(1000)
  }

  System.exit(0)
}

class TimestampedLongEventFactory extends EventFactory[TimestampedLongEvent] {
    def newInstance(): TimestampedLongEvent =
      TimestampedLongEvent(0L, 0L)
}

class TimestampedLongEventHandler extends EventHandler[TimestampedLongEvent] {
    def onEvent(event: TimestampedLongEvent, sequence: Long, endOfBatch: Boolean) =
      println(s"Event: $event")
}

class TimestampedLongEventProducer(ringBuffer: RingBuffer[TimestampedLongEvent]) {

    def onData(bb: ByteBuffer) = {
      val seq = ringBuffer.next()
      try {
        val event = ringBuffer.get(seq)
        event.timestamp = bb.getLong(0)
        event.value = bb.getLong(8)
      } finally {
        ringBuffer.publish(seq)
      }
    }

}