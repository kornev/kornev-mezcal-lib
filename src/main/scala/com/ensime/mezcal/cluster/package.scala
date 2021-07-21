package com.ensime.mezcal

package object cluster {
  type ConnectionParameters = (String, Int, String, String)
  type NodesConfig          = List[ConnectionParameters]
}
