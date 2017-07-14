package parser.core

import shapeless.{HList, LabelledGeneric}
import VType.VType

object ConfigParser {
  def apply[A] = new Helper[A]

  class Helper[A] {
    def apply[C, R <: HList](c: C)(implicit
                                   conf: Conf[C],
                                   gen: LabelledGeneric.Aux[A, R],
                                   parser: ParserImplicits[R, C]): VType[A] = {
      parser(c).map(gen.from)
    }
  }
}
