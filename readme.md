Web Link Checker using SBT 
==========================

This is another akka project created using sbt from scratch.
    

### Create Project Directory 
 $ mkdir web-link-checker

 $ cd web-link-checker

### Create build.sbt File
 $ vi build.sbt

```
name := "web-link-checker"

version := "1.0"

scalaVersion := "2.10.4"

libraryDependencies +=
  "com.typesafe.akka" %% "akka-actor" % "2.3-SNAPSHOT"
  
libraryDependencies += "com.ning" % "async-http-client" % "1.7.5"

libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.0.7"

libraryDependencies += "com.typesafe.akka" % "akka-testkit_2.10" % "2.3.3"

libraryDependencies += "org.scalatest" % "scalatest_2.10" % "2.0" % "test"

resolvers += "Akka Snapshot Repository" at "http://repo.akka.io/snapshots/"

resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"  
```

### Start sbt
Use `sbt` command to start sbt    
$ sbt

 while `sbt` starts, it will create a *project* directory automatically
 
 To exit from sbt use `exit`  

&gt; exit 

### Create plugin.sbt 

Create `plugin.sbt` inside */web-link-checker/project* directory  
 
$ cd project

$ vi plugins.sbt

```
addSbtPlugin("com.typesafe.sbteclipse" % "sbteclipse-plugin" % "2.5.0")
```

come out from */web-link-checker/project* to */web-link-checker*
 
$ cd ..
 
### Create Eclipse project
$ sbt

now use 'eclipse' command before loading the project from eclipse   
&gt; eclipse


### Edit project from Scala Eclipse IDE
Directory structure has already been created. Inside *src/main/scala* create `simple` package

Create following files under `simple` package

* `WebClient.scala`

* `Getter.scala`

* `Controller.scala`

* `Receptionist.scala`

* `Main.scala`


### Create run configuration to run from eclipse

Navigate through `Run > Run Configurations...`

Select *Java Application* and click right mouse and hit *New*

On **Main** tab, put *project* as `web-link-checker` and *Main class* as `akka.Main`

On **Arguments** tab, put *Program arguments* as `simple.Main` 

On **Arguments** tab, put *VM arguments* as follows 
 
	-Dakka.loglevel=DEBUG -Dakka.actor.debug.receive=on

Alternatively create `src/main/resources/application.conf` file

```
akka {
  loglevel = "DEBUG"    
  actor {    
    debug {    
      receive = on      
    }    
  }   
}
```


### Life Cycle of an Actor



| Life Cycle    | description   | HOOK        |
| ------------- |:-------------:| -----------:|
| Start         | new Actor     | preStart    |
|(Restart)*     | fail/restart  | preRestart  |
|               |               | postRestart |
| Stop          | stop          | postStop    |
	  											

#### Supervisor can monitor child actor

1) OneForOneStrategy - deal with each child actor in isolation

2) AllForOneStrategy - if decision applies to all children

3) stoppingStrategy - For any failure of any child, issue a stop command

#### External Actor can monitor actor

1) By registering Death Watch

To register death watch use `context.watch(targetActor)` and to unregister use `context.unwatch(targetActor)`

Watchers receives
```
 Terminated(targetActor)
    (existenceConfirmed: Boolean, addressTerminated: Boolean)
	      extends AutoReceiveMessage with PossiblyHarmful
```	      
	    

#### Code Snippet
	   
	 * **************************************************************
	 * var restarts = Map.empty[ActorRef, Int].withDefaultValue(0) 
	 *
	 *	override val supervisorStrategy = OneForOneStrategy() {
	 *  case _: Exception =>  restarts(sender) match {
	 *  	case tooMany if tooMany > 10 =>
	 *     		restarts -= sender
	 *     		Stop
	 *   	case n =>
	 *     		restarts = restarts.updated(sender, n + 1)
	 *     		Restart
	 *    	}
	 * 	}
	 *  
	 * Above code is same as
	 *---------------------------------------------------------------    
	 *	override val supervisorStrategy = OneForOneStrategy(maxNrOfRetries=10, withinTimeRange=1.minute ){
	 * 		case _: Exception => Restart 
	 *  }	 
	 
	 *  Getter may get following Exceptions:
	 *  
	 *  	java.net.ConnectException
	 *   	java.nio.channels.ClosedChannelException
	 *      java.util.concurrent.ExecutionException
	 *   

  