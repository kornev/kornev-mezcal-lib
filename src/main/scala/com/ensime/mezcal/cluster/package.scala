package com.ensime.mezcal

package object cluster {
  type ConnectionParameters = (String, Int, String, String)
  type ClusterConfig        = List[ConnectionParameters]
}
