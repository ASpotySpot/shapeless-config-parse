package parser.core

import shapeless.labelled.{FieldType, field}
import shapeless.{::, HList, HNil, LabelledGeneric, Lazy, Typeable, Witness}
import parser.core.VType.VType

trait ParserImplicits[L <: HList, C] {
  def apply(c: C)(implicit conf: Conf[C]): VType[L]
}

trait ConfigLowPrio {
  implicit def hconsParser1[C, K <: Symbol, V, T <: HList](
      implicit
      witness: Witness.Aux[K],
      typeable: Typeable[V],
      parserT: Lazy[ParserImplicits[T, C]])
    : ParserImplicits[FieldType[K, V] :: T, C] =
    new ParserImplicits[FieldType[K, V] :: T, C] {
      def apply(c: C)(implicit conf: Conf[C]): VType[FieldType[K, V] :: T] = {

        val vv: VType[FieldType[K, V]] = for {
          either <- conf.get(c, witness.value.name)
          any <- VType.checkRight(either, s"Expected a value at '${witness.value.name}' but found a config '${either.left.get}'")
          value <- VType.fromOption(typeable.cast(any), s"'$any' could not be cast as '${typeable.describe}")
        } yield field[K](value)

        val tv: VType[T] = parserT.value(c)
        VType.merge(vv, tv) { case (v, t) => v :: t }
      }
    }
}

object ParserImplicits extends ConfigLowPrio {

  implicit def hnilFromMap[C]: ParserImplicits[HNil, C] =
    new ParserImplicits[HNil, C] {
      def apply(c: C)(implicit conf: Conf[C]): VType[HNil] =
        VType.success(HNil)
    }

  implicit def hconsParser0[C, K <: Symbol, V, R <: HList, T <: HList](
      implicit
      witness: Witness.Aux[K],
      gen: LabelledGeneric.Aux[V, R],
      parserH: ParserImplicits[R, C],
      parserT: ParserImplicits[T, C],
      nestedTypeable: Typeable[C]): ParserImplicits[FieldType[K, V] :: T, C] =
    new ParserImplicits[FieldType[K, V] :: T, C] {
      def apply(c: C)(implicit conf: Conf[C]): VType[FieldType[K, V] :: T] = {

        val vv: VType[FieldType[K, V]] = for {
          either <- conf.get(c, witness.value.name)
          any <- VType.checkLeft(either, s"Expected a config at '${witness.value.name}' but found a value '${either.right.get}'")
          config <- VType.fromOption(nestedTypeable.cast(any), s"'$any' could not be cast as '${nestedTypeable.describe}'")
          value <- parserH(config)
        } yield field[K](gen.from(value))

        val tv: VType[T] = parserT(c)
        VType.merge(vv, tv) { case (v, t) => v :: t }
      }
    }
}
