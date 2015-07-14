package io.scalac.slack.bots.recruitment

import com.typesafe.config.{Config, ConfigFactory}

trait Measurable {
  val level: Double
  val focus: Double
}
object Measurable {
  val Junior = 0.0D
  val Medior = 1.0D
  val Senior = 2.0D

  val Mobile = 0.0D
  val Backend = 1.0D
  val Frontend = 2.0D

  def levelToDouble(level: String) = level.toLowerCase match {
    case "junior" => Some(Junior)
    case "medior" => Some(Medior)
    case "senior" => Some(Senior)
    case _ => None
  }

  def focusToDouble(focus: String) = focus.toLowerCase match {
    case "mobile" => Some(Mobile)
    case "backend" => Some(Backend)
    case "frontend" => Some(Frontend)
    case _ => None
  }
}

case class TaskData(
  url: String,
  override val level: Double,
  override val focus: Double) extends Measurable

case class Scalac(
  name: String,
  override val level: Double,
  override val focus: Double) extends Measurable

object Scalac {
  def fromConfig(configName: String) = {
    val conf = ConfigFactory.load()
    conf.getConfigList(configName).toArray.map{
      case c: Config =>
        val person = c.root().toConfig
        Scalac(person.getString("name"), person.getDouble("level"), person.getDouble("focus"))
    }.toList
  }
}

