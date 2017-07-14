# shapeless-config-parse

Simple shapeless example for loading a generic config file into case classes.

Largely just me learning shapeless.

Core Idea from 
https://stackoverflow.com/questions/31640565/converting-mapstring-any-to-a-case-class-using-shapeless


Example Use:

First some imports
```
//For loading the config file
import java.io.File 
import com.typesafe.config.ConfigFactory
//For using the parser
import parser.core.ConfigParser
import parser.instances._
```
The classes we'll be parsing into
```
case class A(a: Int, b: B, c: Boolean)
case class B(aa: String)
```
The sucessful case
```
//conf file contains {a: 3, b:{aa: "hello"}, c: true}
val conf: Config = ConfigFactory.parseFile(new File("src/test/resources/my-conf.config"))
val result = ConfigParser[A](conf)
println(result.right.get) //A(3, B("hello"), true)
```
The failure case
```
//conf file contains {a: 3, b:{badkey: "hello"}, c: "bad-type"} 
val conf: Config = ConfigFactory.parseFile(new File("src/test/resources/my-conf.config"))
val result = ConfigParser[A](conf)
result.left.get.foreach(println)
//Could not find key 'aa' in 'Config(SimpleConfigObject({"bad-key":"hello"}))'
//'bad-type' could not be cast as 'Boolean'
```