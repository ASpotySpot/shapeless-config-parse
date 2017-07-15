# shapeless-config-parse

Simple shapeless example for loading a generic config file into case classes.
Largely just me learning shapeless.
Core Idea from 
https://stackoverflow.com/questions/31640565/converting-mapstring-any-to-a-case-class-using-shapeless


Allows loading from:
* com.typesafe.Config 
* java.util.Properties 
* scala.xml.Node 

And if the config is invalid it returns a full list of errors. 

Example Use:

To parse the following properties 
```
def p = new java.util.Properties()
p.setProperty("greeting", "hello")
p.setProperty("nested.age", "32")
p.setProperty("nested.truth", true)
p.setProperty("names.firstname", "james")
p.setProperty("names.surname", "bob")
```
into the the following classes
```
case class Bar(age: Int, truth: Boolean)
case class Names(firstname: String, surname: Strubg)
case class Foo(greeting: String, nested: Bar, names: Names)
```

We just need
``` 
import parser.Reader._
import parser.ConfigParser

val result: Either[List[String], Foo] =  ConfigParser[Foo](p))
println(result) //Right(Foo("hello", Bar(32, true), Names("james", "bob")))
```

Error handling
```
p.setProperty("nested.age", "not-an-int")
p.remove("greeting")
val result: Either[List[String], Foo] =  ConfigParser[Foo](p))
result.left.foreach(println) 
//Failed to convert 'not-an-int' into a 'Int'
//Could not find key 'greeting' in 'nested,names'
```

Its also possible to define a custom parser.Reader[A] for any A which would allow reading of any A directly into a case class.

