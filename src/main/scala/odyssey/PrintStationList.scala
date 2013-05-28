package odyssey

// futures, promises, etc.
// todo: get rid of the block import.  It really pollutes the namespace.
import scala.concurrent._
import scala.concurrent.duration._
import ExecutionContext.Implicits.global

/** PrintStationList is an example of how to use the WBAN parser
  * This application retrieves the set of stations from the NOAA WBAN
  * inventory, parses them, and then prints out the results to the console.
  *
  * It is intended as a demonstration of futures, the loan pattern, and parser
  * combinators in scala.
  *
  * @todo use the zipped version of the station list instead of the txt
  * @todo save this into a database and only retrieve it if the data is missing
  * @todo check if there are other api's at the NOAA for station inventory
  */
object PrintStationList extends App {

  // this example is pretty sweet.  It is parsing the station list
  // in an async call, and printing the output as it is coming in
  // instead of waiting for the entire file to become available.
  // an obvious change is to use Station.inventoryURLZ instead for
  // the zip file, and also to push it into a database instead of
  // simply printing it out.
  val stationtext = future {
    common.util.io.withURLSource(Station.inventoryURL) {
      src => (src.getLines map Station.parseLine map println).toList
    }
  }

  // define two callbacks which can help keep track of what happens to stationtext
  stationtext onSuccess { case _ => println("completed") }
  stationtext onFailure { case t => println("An error has occured: " + t.getMessage) }

  // if there isn't an await statement at the end, the project will just
  // finish.
  Await.ready(stationtext, 60 seconds)

}