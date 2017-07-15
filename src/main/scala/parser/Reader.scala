package parser

import java.util.Properties

import com.typesafe.config.Config

import scala.collection.JavaConverters.asScalaSet

trait Reader[A] {
  def read(a: A): Nested
}

object Reader {
  implicit val structReader = new Reader[Nested] {
    override def read(a: Nested): Nested = a
  }

  implicit val hoconReader: Reader[Config] = new Reader[Config] {
    override def read(a: Config): Nested = delimReader[Config, AnyRef](
      (c, k) => c.getAnyRef(k),
      asScalaSet(a.entrySet()).map(_.getKey).toSet
    )(a)
  }

  implicit val propertiesReader: Reader[Properties] = new Reader[Properties] {
    override def read(p: Properties): Nested = delimReader[Properties, String](
      (c, k) => c.getProperty(k),
      asScalaSet(p.stringPropertyNames()).toSet
    )(p)
  }

  private def delimReader[C, V](readConf: (C, String) => V, keySet: Set[String]): (C => Nested) = {

    def innerParse(keySet: Set[String], depth: Int, c: C): Nested = {
      val (nestedKeys, valueKeys) = partByKeys(keySet, depth)
      val values = parseValues(valueKeys, depth, c)
      val nested = groupByParent(nestedKeys, depth, c)
      Nested(values ++ nested)
    }

    def partByKeys(keySet: Set[String], depth: Int): (Set[String], Set[String]) = {
      keySet.partition(_.split('.').lift(depth + 1).isDefined)
    }

    def parseValues(keySet: Set[String], depth: Int, c: C): Map[String, Value] = {
      keySet.map{k => (k.split('.')(depth), Value(readConf(c, k)))}.toMap
    }

    def groupByParent(keySet: Set[String], depth: Int, c: C): Map[String, Nested] = {
      keySet.groupBy(_.split('.')(depth)).mapValues{keyList =>
        innerParse(keyList, depth + 1, c)
      }
    }
    c => innerParse(keySet, 0, c)
  }
}
