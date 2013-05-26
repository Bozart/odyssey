package odyssey
import scala.concurrent._
import ExecutionContext.Implicits.global

object HelloWorld extends App {
  val stationtext = future { common.doLoanURLCall(Station.inventoryURL)(
      (txt: scala.io.BufferedSource) =>
        (txt.getLines map Station.parseLine).toVector map println) }
  println("hi")
  stationtext onSuccess {case _ => println("completed")}
  stationtext onFailure {case t => println("An error has occured: " + t.getMessage)}
  blocking(stationtext)
  
}