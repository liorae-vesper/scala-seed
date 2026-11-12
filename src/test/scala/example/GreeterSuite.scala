package example

class GreeterSuite extends munit.FunSuite:
  test("greet a named person") {
    assertEquals(Greeter.greet("Ada"), Right("Hello, Ada!"))
  }

  test("trim surrounding whitespace") {
    assertEquals(Greeter.greet("  Ada \t"), Right("Hello, Ada!"))
  }

  test("reject an empty name") {
    assertEquals(Greeter.greet(""), Left("Name must not be blank"))
  }

  test("reject a whitespace-only name") {
    assertEquals(Greeter.greet(" \t\n"), Left("Name must not be blank"))
  }

  test("preserve Unicode and internal whitespace") {
    assertEquals(Greeter.greet("João da Silva"), Right("Hello, João da Silva!"))
  }
