package parser.core

object VType {
  type VType[A] = Either[List[String], A]

  def success[V](v: V): VType[V] = Right(v)
  def fail[V](s: String): VType[V] = Left(s :: Nil)

  def fromOption[A](opt: Option[A], err: => String): VType[A] = opt match {
    case Some(a) => success(a)
    case None => fail(err)
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
}
