package odyssey


// futures, promises, etc.
import scala.concurrent._
import scala.concurrent.duration._
import ExecutionContext.Implicits.global



object HelloWorld extends App {
  
  // this example is pretty sweet.  It is parsing the station list
  // in an async call, and printing the output as it is coming in
  // instead of waiting for the entire file to become available.
  val stationtext = future { common.util.doLoanURLCall(Station.inventoryURL)(
      (t: scala.io.BufferedSource) =>
        (t.getLines map Station.parseLine map println).toList) }

  stationtext onSuccess {case _ => println("completed")}
  stationtext onFailure {case t => println("An error has occured: " + t.getMessage)}
  
  // if there isn't an await statement at the end, the project will just
  // finish.
  Await.ready(stationtext, 60 seconds)
  
}