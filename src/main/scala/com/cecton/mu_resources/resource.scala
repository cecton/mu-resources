package com.cecton.mu_resources.resource

trait Resource

sealed trait Relationship {
  val _1: Resource
  val _2: Resource

  def reciprocal: Relationship
}

case class One2One(val _1: Resource, val _2: Resource) extends Relationship {
  def reciprocal = One2One(_2, _1)
}

case class Many2One(val _1: Resource, val _2: Resource) extends Relationship {
  def reciprocal = One2Many(_2, _1)
}

case class One2Many(val _1: Resource, val _2: Resource) extends Relationship {
  def reciprocal = Many2One(_2, _1)
}

case class Many2Many(val _1: Resource, val _2: Resource) extends Relationship {
  def reciprocal = Many2Many(_2, _1)
}

sealed case class Relationships(val relationships: Relationship*) {
  def all = relationships ++ relationships.map(_.reciprocal)
  val map: Map[Resource, Seq[Relationship]] = all.groupBy(_._1)
}
