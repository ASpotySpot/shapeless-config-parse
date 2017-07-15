package parser

import parser.VType.VType

import scala.util.Try

trait FromStr[A] {
  def apply(s: String): VType[A] = {
    VType.fromOption(convert(s), s"Failed to convert '$s' into a '$description'")
  }
  protected def description: String
  protected def convert(s: String): Option[A]
}
object FromStr {
  implicit val intFromStr = new FromStr[Int] {
    override protected def description: String = "Int"
    override protected def convert(s: String): Option[Int] = Try(s.toInt).toOption
  }
  implicit val strFromStr = new FromStr[String] {
    override protected def description: String = "String"
    override protected def convert(s: String): Option[String] = Some(s)
  }
  implicit val boolFromStr = new FromStr[Boolean] {
    override protected def description: String = "Boolean"
    override protected def convert(s: String): Option[Boolean] = Try(s.toBoolean).toOption
  }
  implicit val longFromStr = new FromStr[Long] {
    override protected def description: String = "Long"
    override protected def convert(s: String): Option[Long] = Try(s.toLong).toOption
  }
  implicit val charFromStr = new FromStr[Char] {
    override protected def description: String = "Char"
    override protected def convert(s: String): Option[Char] = if(s.length == 1) Some(s.head) else None
  }

  implicit def optFromStr[A](implicit af: FromStr[A]) = new FromStr[Option[A]] {
    override protected def description: String = s"Option(${af.description})"
    override protected def convert(s: String): Option[Option[A]] = {
      Option(s).filter(_.nonEmpty) match {
        case None => Some(None)
        case Some(ss) => af.convert(ss).map(Some(_))
      }
    }
  }

  implicit def listFromStr[A](implicit af: FromStr[A]) = new FromStr[List[A]] {
    override protected def description: String = s"Option(${af.description})"
    override protected def convert(s: String): Option[List[A]] = {
      Option(s).filter(_.nonEmpty) match {
        case None => Some(List.empty)
        case Some(ss) => ss.split(',').map(af.convert).toList.foldRight(Option(List.empty[A])){
          case (Some(a), Some(ls)) => Some(a :: ls)
          case _ => None
        }
      }
    }
  }
}
