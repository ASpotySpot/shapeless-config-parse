package parser

import org.scalatest.FlatSpec

class ParserTest extends FlatSpec with BaseTest {
  case class A(aa: String)
  case class B(a: Int, b: A, c: Boolean)
  case class C(opt: Option[String], list: List[Char])

  "ConfigParser" should "parse when the types are all already correct" in {
    val struct = Nested(Map(
      "a" -> Value(3),
      "b" -> Nested(Map(
        "aa" -> Value("hello")
      )),
      "c" -> Value(true)
    ))
    val expected = B(3, A("hello"), true)
    val result = ConfigParser[B](struct)
    result shouldEqual Right(expected)
  }

  it should "parse when types are strings" in {
    val struct = Nested(Map(
      "a" -> Value("3"),
      "b" -> Nested(Map(
        "aa" -> Value("hello")
      )),
      "c" -> Value("true")
    ))
    val expected = B(3, A("hello"), true)
    val result = ConfigParser[B](struct)
    result shouldEqual Right(expected)
  }

  it should "handle options and lists" in {
    val struct1 = Nested(Map("opt" -> Value(Some("hi")), "list" -> Value(List('a', 'b'))))
    val expected1 = C(Some("hi"), List('a', 'b'))
    val result1 = ConfigParser[C](struct1)
    result1 shouldEqual Right(expected1)

    val struct2 = Nested(Map("opt" -> Value(None), "list" -> Value(List.empty[Char])))
    val expected2 = C(None, List.empty)
    val result2 = ConfigParser[C](struct2)
    result2 shouldEqual Right(expected2)
  }

  it should "handle options and lists when defined as strings" in {
    val struct1 = Nested(Map("opt" -> Value(Some("hi")), "list" -> Value("a,b")))
    val expected1 = C(Some("hi"), List('a', 'b'))
    val result1 = ConfigParser[C](struct1)
    result1 shouldEqual Right(expected1)

    val struct2 = Nested(Map("opt" -> Value(""), "list" -> Value("")))
    val expected2 = C(None, List.empty)
    val result2 = ConfigParser[C](struct2)
    result2 shouldEqual Right(expected2)
  }

  it should "fail when given an struct" in {
    val struct = Nested(Map(
      "a" -> Value("3"),
      "b" -> Nested(Map(
        "bad-key" -> Value("hello")
      )),
      "c" -> Value("bad-type")
    ))
    val result = ConfigParser[B](struct)
    val expected = Left(List(
      """Could not find key 'aa' in 'bad-key'""",
      """Failed to convert 'bad-type' into a 'Boolean'"""
    ))

    result shouldEqual expected
  }
}
