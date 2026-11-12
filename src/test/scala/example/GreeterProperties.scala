package example

import org.scalacheck.Prop.forAll

class GreeterProperties extends munit.ScalaCheckSuite:
  property("trimming the name does not change the result") {
    forAll { (name: String) =>
      Greeter.greet(name) == Greeter.greet(name.trim)
    }
  }

  property("a trimmed nonempty name is included unchanged") {
    forAll { (name: String) =>
      val trimmed = name.trim
      if trimmed.isEmpty then Greeter.greet(name).isLeft
      else Greeter.greet(name) == Right(s"Hello, $trimmed!")
    }
  }
