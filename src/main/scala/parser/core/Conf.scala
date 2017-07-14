package parser.core

import VType.VType

trait Conf[N] {
  def get(n: N, key: String): VType[Either[N, Any]]
}

