package parser

import parser.VType._

sealed trait Structure {
  def asEither: Either[Nested, Value] = this match {
    case v: Value => Right(v)
    case n: Nested => Left(n)
  }
}
case class Value(value: Any) extends Structure
case class Nested(value: Map[String, Structure]) extends Structure {
  def get(s: String): VType[Either[Nested, Value]] =
    value.getVal(s).map(_.asEither)

  def filterKeys(f: String => Boolean): Nested = {
    Nested(value.filterKeys(f).mapValues{
      case v: Value => v
      case n: Nested => n.filterKeys(f)
    })
  }
}

