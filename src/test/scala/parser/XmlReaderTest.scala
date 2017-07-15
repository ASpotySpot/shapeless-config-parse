package parser

import org.scalatest.FlatSpec

import scala.xml.Node

class XmlReaderTest extends FlatSpec with BaseTest {

  "PropertiesReader" should "read an xml object into structures" in {
    val xml =
      <root>
        <a>32</a>
        <b>true</b>
        <c>
          <d>hi</d>
        </c>
      </root>
    val result = Reader.xmlReader.read(xml)
    val expected = Nested(Map(
      "a" -> Value("32"),
      "b" -> Value("true"),
      "c" -> Nested(Map(
        "d" -> Value("hi")
      ))
    ))
   result shouldEqual expected
  }

  it should "create the correct structure to parse a case class" in {
    case class A(a: Int)
    case class B(b: Boolean)
    case class C(c: String, ca: A, cb: B)
    val xml: Node =
      <root>
        <c>hi</c>
        <ca>
          <a>32</a>
        </ca>
        <cb>
          <b>true</b>
        </cb>
      </root>
    ConfigParser[C](xml) shouldEqual Right(C("hi", A(32), B(true)))
  }
}
