package odyssey
import Station._
object HelloWorld extends App {
  val stationtext = util.doLoanURLCall(Station.inventoryURL)(
      (txt: scala.io.BufferedSource) =>
        (txt.getLines map Station.parseLine).toList)
  println(stationtext)
}