import java.security.MessageDigest

val start = System.currentTimeMillis()
private val url =
  "https://obs.line-scdn.net/0hIdjfo_jTFktXHwDsSH1pHGhPHTpkcgFIN2cEfSdDJn5-JlUea3teLXcYTnsqLlkUY35faHNKHyl4J1IZ"

final val hexCode = "0123456789abcdef".toCharArray;

private def printHexBinary(data: Array[Byte]): String = {
  val r: StringBuilder = new StringBuilder(data.length * 2);
  for (b <- data) {
    r.append(hexCode((b >> 4) & 0xf));
    r.append(hexCode(b & 0xf));
  }

  r.toString()
}
val md = MessageDigest.getInstance("SHA-256")
md.update(
  url.toCharArray.map(_.toByte)
)
println(s"Custom contenthash: ${printHexBinary(md.digest())}")

val end = System.currentTimeMillis()
println(s"Time: ${end - start}ms")
