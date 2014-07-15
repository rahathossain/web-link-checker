
### Getter.scala

Getter fetch the content from the url and 
on successful fetch it sends the send the body to self
on error send the error to self      

```scala
val future = WebClient.get(url)

future onComplete {
   case Success(body) => self ! body
   case Failure(err) => self ! Status.Failure(err)
}
```

Above code can be written as follows as well 

```scala
import akka.patern.pipe

val future = WebClient.get(url)

future.pipeTo(self)
```

Above codes can be even written more concise way as below:

```scala
import akka.patern.pipe

WebClient get url pipeTo self
```


