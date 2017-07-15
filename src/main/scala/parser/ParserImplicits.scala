package parser

import parser.VType._
import shapeless.labelled.{FieldType, field}
import shapeless.{::, HList, HNil, LabelledGeneric, Lazy, Typeable, Witness}

import scala.reflect.ClassTag

trait ParserImplicits[L <: HList] {
  def apply(c: Nested): VType[L]
}


//Last fallback of 2. There is a Typeable[V] but no FromStr[V]
trait ParseValue {
  implicit def hconsParser2a[K <: Symbol, V, T <: HList](
      implicit
      witness: Witness.Aux[K],
      typeable: Typeable[V],
      parserT: Lazy[ParserImplicits[T]])
    : ParserImplicits[FieldType[K, V] :: T] =

    new ParserImplicits[FieldType[K, V] :: T] {
      def apply(stuct: Nested): VType[FieldType[K, V] :: T] = {

        val vv: VType[FieldType[K, V]] = for {
          either <- stuct.get(witness.value.name)
          any <- VType.checkRight(either, s"Expected a value at '${witness.value.name}' but found a config '${either.left.get}'")
          value <- VType.cast[V](any.value)
        } yield field[K](value)

        val tv: VType[T] = parserT.value(stuct)
        VType.merge(vv, tv) { case (v, t) => v :: t }
      }
    }
}

//Last fallback of 2. There is a FromStr[V] but no Typeable[V]
trait ParseValueAsStr {
  private def parse[V](any: Any)(implicit fromStr: FromStr[V], ct: ClassTag[V]): VType[V] = {
    any match {
      case s: String => fromStr(s)
      case _ => VType.fail(s"No typeable isntance found for '$ct' and '$any' is not a string")
    }
  }

  implicit def hconsParser2b[K <: Symbol, V, T <: HList](
                                                         implicit
                                                         witness: Witness.Aux[K],
                                                         fromStr: FromStr[V],
                                                         ct: ClassTag[V],
                                                         parserT: Lazy[ParserImplicits[T]])
  : ParserImplicits[FieldType[K, V] :: T] =

    new ParserImplicits[FieldType[K, V] :: T] {
      def apply(stuct: Nested): VType[FieldType[K, V] :: T] = {

        val vv: VType[FieldType[K, V]] = for {
          either <- stuct.get(witness.value.name)
          any <- VType.checkRight(either, s"Expected a value at '${witness.value.name}' but found a config '${either.left.get}'")
          value <- parse(any.value)
        } yield field[K](value)

        val tv: VType[T] = parserT.value(stuct)
        VType.merge(vv, tv) { case (v, t) => v :: t }
      }
    }
}

//Fall back for values. Gets here if a FromStr[V] exists but no labelled generic
//Tries to use Typeable[V] and if it fails uses FromStr[V] if possible
trait ParseValueWithFromStr extends ParseValue with ParseValueAsStr {
  private def cast[V](any: Any)(implicit typeable: Typeable[V], fromStr: FromStr[V]): VType[V] = {
    val typeTry = VType.cast[V](any)
    any match {
      case s: String => typeTry.recover(fromStr(s), true)
      case _ => typeTry
    }
  }

  implicit def hconsParser1[K <: Symbol, V, T <: HList](
                                                         implicit
                                                         witness: Witness.Aux[K],
                                                         typeable: Typeable[V],
                                                         fromStr: FromStr[V],
                                                         parserT: Lazy[ParserImplicits[T]])
  : ParserImplicits[FieldType[K, V] :: T] =

    new ParserImplicits[FieldType[K, V] :: T] {
      def apply(stuct: Nested): VType[FieldType[K, V] :: T] = {

        val vv: VType[FieldType[K, V]] = for {
          either <- stuct.get(witness.value.name)
          any <- VType.checkRight(either, s"Expected a value at '${witness.value.name}' but found a config '${either.left.get}'")
          value <- cast[V](any.value)
        } yield field[K](value)

        val tv: VType[T] = parserT.value(stuct)
        VType.merge(vv, tv) { case (v, t) => v :: t }
      }
    }
}


//Main instance. For parsing case classes. Gets in here if a LabelledGeneric can be found. ie.) Its a case class
object ParserImplicits extends ParseValueWithFromStr {

  implicit def hnilFromMap: ParserImplicits[HNil] =
    new ParserImplicits[HNil] {
      def apply(c: Nested): VType[HNil] =
        VType.success(HNil)
    }

  implicit def hconsParser0[K <: Symbol, V, R <: HList, T <: HList](
      implicit
      witness: Witness.Aux[K],
      gen: LabelledGeneric.Aux[V, R],
      parserH: ParserImplicits[R],
      parserT: ParserImplicits[T]): ParserImplicits[FieldType[K, V] :: T] =
    new ParserImplicits[FieldType[K, V] :: T] {
      def apply(struct: Nested): VType[FieldType[K, V] :: T] = {

        val vv: VType[FieldType[K, V]] = for {
          either <- struct.get(witness.value.name)
          config <- VType.checkLeft(either, s"Expected a config at '${witness.value.name}' but found a value '${either.right.get}'")
          value <- parserH(config)
        } yield field[K](gen.from(value))

        val tv: VType[T] = parserT(struct)
        VType.merge(vv, tv) { case (v, t) => v :: t }
      }
    }
}
