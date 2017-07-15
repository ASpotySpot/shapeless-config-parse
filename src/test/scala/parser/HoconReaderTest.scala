package parser

import java.io.File

import com.typesafe.config.{Config, ConfigFactory}
import org.scalatest.FlatSpec


class HoconReaderTest extends FlatSpec with BaseTest {
  private def loadConf(fileName: String): Config = {
    ConfigFactory.parseFile(new File(s"src/test/resources/$fileName"))
  }

  "HoconReader" should "read a Config object into structures" in {
    val c = loadConf("my-conf.config")
    val result = Reader.hoconReader.read(c)
    val expected = Nested(Map(
      "a" -> Value(32),
      "c" -> Value(true),
      "b" -> Nested(Map(
        "aa" -> Value("hello")
      ))
    ))
    result shouldEqual expected
  }

  it should "create the correct structure to parse a case class" in {
    case class A(aa: String)
    case class B(a: Int, b: A, c: Boolean)
    val c = loadConf("my-conf.config")
    ConfigParser[B](c) shouldEqual Right(B(32, A("hello"), true))
  }
}
