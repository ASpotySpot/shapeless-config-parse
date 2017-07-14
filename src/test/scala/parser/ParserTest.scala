package parser

import java.io.File
import com.typesafe.config.ConfigFactory
import org.scalatest.{FlatSpec, Matchers}
import parser.core.ConfigParser
import parser.instances._

class ParserTest extends FlatSpec with Matchers {
  case class A(a: Int, b: B, c: Boolean)
  case class B(aa: String)

  "ConfigParser" should "parse a hocon file into a case class" in {
    val conf = ConfigFactory.parseFile(new File("src/test/resources/my-conf.config"))
    val result = ConfigParser[A](conf)
    val expected = Right(A(32, B("hello"), true))
    result shouldEqual expected
  }

  it should "fail when given an invalid hocon file" in {
    val conf = ConfigFactory.parseFile(new File("src/test/resources/bad-conf.config"))
    val result = ConfigParser[A](conf)
    val expected = Left(List(
      """Could not find key 'aa' in 'Config(SimpleConfigObject({"bad-key":"hello"}))'""",
      """'bad-type' could not be cast as 'Boolean'"""
    ))
    result shouldEqual expected
  }
}
