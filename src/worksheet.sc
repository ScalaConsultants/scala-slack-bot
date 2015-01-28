import org.joda.time.DateTime

val empty = ""
val notEmpty = "full"

implicit def stringToOption(s: String): Option[String] = if (s.isEmpty) None else Some(s)

val opt1: Option[String] = empty
//opt1: Option[String] = None
val opt2: Option[String] = notEmpty
//opt2: Option[String] = Some(full)

val dt = new DateTime(1421772055000l)