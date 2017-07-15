package parser

import shapeless.Typeable

object VType {
  type VType[A] = Either[List[String], A]

  def success[V](v: V): VType[V] = Right(v)
  def fail[V](s: String): VType[V] = Left(s :: Nil)

  def fromOption[A](opt: Option[A], err: => String): VType[A] = opt match {
    case Some(a) => success(a)
    case None => fail(err)
  }

  def cast[A](any: Any)(implicit typeable: Typeable[A]): VType[A] = {
    VType.fromOption(typeable.cast(any), s"'$any' could not be cast as '${typeable.describe}'")
  }


  def merge[A, B, Z](va: VType[A], vb: VType[B])(f: (A, B) => Z): VType[Z] = (va, vb) match {
    case (Right(a), Right(b)) => Right(f(a, b))
    case (Left(s1), Left(s2)) => Left(s1 ++ s2)
    case (Left(s1), _) => Left(s1)
    case (_, Left(s2)) => Left(s2)
  }

  def checkLeft[A](either: Either[A, _], err: => String): VType[A] = either match {
    case Left(a) => success(a)
    case Right(_) => fail(err)
  }

  def checkRight[B](either: Either[_, B], err: => String): VType[B] = either match {
    case Right(b) => success(b)
    case Left(_) => fail(err)
  }

  implicit class RichEither[A](va: VType[A]) {
    def recover(f: => VType[A], replace: Boolean): VType[A] = va match {
      case ok @ Right(_) => ok
      case Left(ers) => f match {
        case ok2 @ Right(_) => ok2
        case Left(ers2) if replace => Left(ers2)
        case Left(ers2) => Left(ers ++ ers2)
      }
    }
  }

  implicit class RichMap[K, V](m: Map[K, V]) {
    def getVal(k: K): VType[V] = {
      VType.fromOption(m.get(k), s"Could not find key '$k' in '${m.keys.mkString(",")}'")
    }
  }
}
