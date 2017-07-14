package parser.instances

import com.typesafe.config.Config
import parser.core.{Conf, VType}
import parser.core.VType.VType

trait HoconConf extends Conf[Config]{
  override def get(n: Config, key: String): VType[Either[Config, Any]] = if(n.hasPath(key)) {
    VType.success(n.getAnyRef(key)).map{
      case _: java.util.HashMap[_, _] => Left(n.getConfig(key))
      case value => Right(value)
    }
  } else {
    VType.fail(s"Could not find key '$key' in '$n'")
  }
}
