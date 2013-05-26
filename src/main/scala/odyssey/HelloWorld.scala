package odyssey
import Station._
object HelloWorld extends App {
  val stationtext = scala.io.Source.fromURL(Station.inventoryURL).getLines()
  val stationdata = (stationtext map {parseLine}).toStream
  

}