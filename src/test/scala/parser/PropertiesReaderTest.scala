package parser

import java.util.Properties

import org.scalatest.FlatSpec


class PropertiesReaderTest extends FlatSpec with BaseTest {
  private def makeProps(m: Map[String, String]): Properties = {
    val p = new Properties()
    m.foreach{case (k, v) => p.setProperty(k, v)}
    p
  }

  "PropertiesReader" should "read a properties object into structures" in {
    val m = Map(
      "key" -> "value",
      "key2" -> "value2",
      "inner.key" -> "inner-value-1",
      "inner.key2" -> "inner-value-2",
      "inner.inner.key" -> "inner-inner-value",
      "inner2.key" -> "inner2-value"
    )
    val result = Reader.propertiesReader.read(makeProps(m))
    val expected = Nested(Map(
      "key" -> Value("value"),
      "key2" -> Value("value2"),
      "inner" -> Nested(Map(
        "key" -> Value("inner-value-1"),
        "key2" -> Value("inner-value-2"),
        "inner" -> Nested(Map(
          "key" -> Value("inner-inner-value")
        ))
      )),
      "inner2" -> Nested(Map(
        "key" -> Value("inner2-value")
      ))
    ))
    result shouldEqual expected
  }

  it should "create the correct structure to parse a case class" in {
    case class A(a: Int)
    case class B(b: Boolean)
    case class C(c: String, ca: A, cb: B)
    val p = makeProps(Map("c" -> "hi", "cb.b" -> "true", "ca.a" -> "32"))
    val x = ConfigParser[C](p) shouldEqual Right(C("hi", A(32), B(true)))
  }
}
