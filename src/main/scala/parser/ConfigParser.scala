package parser

import parser.VType.VType
import shapeless.{HList, LabelledGeneric}

object ConfigParser {
  def apply[A] = new ConfigParserHelper[A]

  class ConfigParserHelper[A] {
    def apply[C, R <: HList](c: C)(implicit
                                   readC: Reader[C],
                                   gen: LabelledGeneric.Aux[A, R],
                                   parser: ParserImplicits[R]): VType[A] = {
      val struct: Nested = readC.read(c)
      val rv: VType[R] = parser(struct)
      rv.map(gen.from)
    }
  }
}
