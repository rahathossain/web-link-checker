Web Link Checker using SBT 
==========================

This is another akka project created using sbt from scratch.
    

#### Create Project Directory 
 $ mkdir web-link-checker

 $ cd web-link-checker

#### Create build.sbt File
 $ vi build.sbt

```scala
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

#### Start sbt
Use `sbt` command to start sbt    
$ sbt

 while `sbt` starts, it will create a *project* directory automatically
 
 To exit from sbt use `exit`  

&gt; exit 

#### Create plugin.sbt 

Create `plugin.sbt` inside */web-link-checker/project* directory  
 
$ cd project

$ vi plugins.sbt

```
addSbtPlugin("com.typesafe.sbteclipse" % "sbteclipse-plugin" % "2.5.0")
```

come out from */web-link-checker/project* to */web-link-checker*
 
$ cd ..
 
#### Create Eclipse project
$ sbt

now use 'eclipse' command before loading the project from eclipse   
&gt; eclipse


#### Edit project from Scala Eclipse IDE
Directory structure has already been created. Inside *src/main/scala* create `simple` package

Create following files under `simple` package

* `WebClient.scala`

* `Getter.scala`

* `Controller.scala`

* `Receptionist.scala`

* `Main.scala`


#### Create run configuration to run from eclipse

Navigate through `Run > Run Configurations...`

Select *Java Application* and click right mouse and hit *New*

On **Main** tab, put *project* as `web-link-checker` and *Main class* as `akka.Main`

On **Arguments** tab, put *Program arguments* as `simple.Main` 

On **Arguments** tab, put *VM arguments* as follows 
 
	-Dakka.loglevel=DEBUG -Dakka.actor.debug.receive=on

Alternatively create `src/main/resources/application.conf` file

```scala
akka {
  loglevel = "DEBUG"    
  actor {    
    debug {    
      receive = on      
    }    
  }   
}
```


#### Life Cycle of an Actor



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
```scala
 Terminated(targetActor)
    (existenceConfirmed: Boolean, addressTerminated: Boolean)
	      extends AutoReceiveMessage with PossiblyHarmful
```	      

#### Code Snippet for Supervisor Strategy
	   
```scala
	  var restarts = Map.empty[ActorRef, Int].withDefaultValue(0) 
	 
	 	override val supervisorStrategy = OneForOneStrategy() {
	   case _: Exception =>  restarts(sender) match {
	   	case tooMany if tooMany > 10 =>
	      		restarts -= sender
	      		Stop
	    	case n =>
	      		restarts = restarts.updated(sender, n + 1)
	      		Restart
	     	}
	  	}
```
	   
Above code can be written as follows as well

```
   override val supervisorStrategy = OneForOneStrategy(maxNrOfRetries=10, withinTimeRange=1.minute ){
  		case _: Exception => Restart 
   }	 
```
	 
Note that Getter may get following Exceptions:

```   
	java.net.ConnectException
	java.nio.channels.ClosedChannelException
	java.util.concurrent.ExecutionException	    
```
  

#### Add Clustering facility 

Directory structure has already been created. 
Inside *src/main/scala* create `cluster` package

Create following files under `cluster` package

* `ClusterReceptionist.scala`

* `ClusterMain.scala`

* `ClusterWorker.scala`

#### Running Cluster from Eclipse

Update `src/main/resources/application.conf` file

```scala
akka {
  loggers = ["akka.event.slf4j.Slf4jLogger"]    
  loglevel = DEBUG  
  actor {  
  	provider = akka.cluster.ClusterActorRefProvider  	      
    debug {            
      receive = on      
    }
  }    
  cluster {  
  	min-nr-of-members=2  	
  	auto-down = on
  }   
}
```

Add following extra dependency to build.sbt as below:

```scala
libraryDependencies += "org.slf4j" % "slf4j-api" % "1.7.7"

libraryDependencies += "com.typesafe.akka" % "akka-slf4j_2.10" % "2.3.4"

libraryDependencies += "com.typesafe.akka" % "akka-cluster_2.10" % "2.3.4"

libraryDependencies += "com.typesafe.akka" % "akka-kernel_2.10" % "2.3.4"
``` 

Create two new run configurations for  `cluster.ClusterMain` and `cluster.ClusterWorker`.

Navigate through `Run > Run Configurations...`

For Cluster Main:

	Select *Java Application* and click right mouse and hit *New*

		On **Main** tab, put *project* as `web-link-checker` and *Main class* as `akka.Main`

		On **Arguments** tab, put *Program arguments* as `cluster.ClusterMain` 

Again For Cluster Worker:


		On **Main** tab, put *project* as `web-link-checker` and *Main class* as `akka.Main`

		On **Arguments** tab, put *Program arguments* as `cluster.ClusterWorker` 

		On **Arguments** tab, put *VM arguments* as follows 
 
				-Dakka.remote.netty.tcp.port=0

First start ClusterMain, then start ClusterWorker

	    
#### Running Cluster from SBT

Create following file under `cluster` package

* `LookupApplication.scala`
  
 Open two command prompt and fire up sbt
  
 From one command promt fire up Main
   
 &gt; `runMain cluster.LookupApplication Main`
 
 One main fire up, from another command prompt fire up Worker 
   
 &gt; `runMain cluster.LookupApplication Worker`    
 
 
#### Running cluster from AKKA Microkernel
 
Create following file under `cluster` package

* `BootClusterMain.scala`

* `BootClusterWorker.scala`

Build the jar file from SBT  

&gt; `package`

The jar file will be created `\web-link-checker\target\scala-2.10` as `web-link-checker_2.10-1.0.jar` 

Download AKKA Microkernel and copy the jar inside `\akka-2.2.4\deploy`

Open up command prompt or shell, and from AKKA Microkernel home, first run Main and then run worker as follows:

&gt; bin/akka cluster.BootClusterMain

&gt; bin/akka cluster.BootClusterWorker




 


 
 
 
   