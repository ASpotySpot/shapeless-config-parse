package parser

import parser.VType.VType

trait Conf[N] {
  def get(n: N, key: String): VType[Either[N, Any]]
}

