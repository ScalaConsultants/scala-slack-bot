java.util.UUID.randomUUID.toString

val md = java.security.MessageDigest.getInstance("SHA-1")
val ha = new sun.misc.BASE64Encoder().encode(md.digest("893dcfa8-04c7-476d-a1e2-f1960d15f7f7".getBytes))


import java.io.File

import spray.json._

case class Colors(name: String)

object MyJsonProtocol extends DefaultJsonProtocol {
  implicit val colorFormat = jsonFormat1(Colors)
}

import MyJsonProtocol._

val colors1 = Colors("blue")

colors1.toJson.prettyPrint

val l = List("20150129151259_insert_new_model.sql",
  "20150129151332_update_company_model.sql",
  "20150129151325_update_user_model.sql",
  "20150129151340_add_index_to_user_table.sql").sorted

l.foreach(a => println(a.take(14)))


File.pathSeparatorChar