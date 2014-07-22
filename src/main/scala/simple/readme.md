
Getter.scala is an Actor, which born with an url and depth size as tag value, 
once it borns, it reads all the content from the url and finds all the links in it 
and sends them to it's parent actor one by one along with the tag value i.e. 
depth and when sending is done it kill itself.

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


