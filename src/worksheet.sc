val name = Some("upase")
val error = None

//oO.getOrElse("")
//pO.getOrElse("alsd")

val m = Map("name" -> name, "error" -> error).collect {case (key, Some(value)) => key -> value}
