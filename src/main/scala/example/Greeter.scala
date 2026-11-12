package example

object Greeter:
  def greet(name: String): Either[String, String] =
    val trimmed = name.trim
    if trimmed.isEmpty then Left("Name must not be blank")
    else Right(s"Hello, $trimmed!")
