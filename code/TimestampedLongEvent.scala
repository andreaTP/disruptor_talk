
case class TimestampedLongEvent(var timestamp: Long, var value: Long) {
  override def toString(): String =
    s"TLE[ timestamp: $timestamp, value: $value ]"
}
