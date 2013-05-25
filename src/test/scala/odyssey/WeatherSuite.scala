package odyssey

import org.scalatest._
import Station._
class StationParseSpec extends FlatSpec with matchers.ShouldMatchers {
  "A positive latitude" should "be broken into a DMS case class" in {
    val posLatText = " 34 43 00"
    val p = new WBANStationParser()
    val Result = p.parseAll(p.Lat, posLatText) match {
      case p.Success(x, _)     => x
      case p.Failure(msg, _)   => fail(msg)
      case p.Error(msg,_)      => fail(msg)
    }

    Result should equal (DMS(34,43,0))

  }
  "A negative latitude" should "be broken into a DMS case class" in {
    val negLatText = "-03 48 01"
    val p = new WBANStationParser()
    val Result = p.parseAll(p.Lat, negLatText) match {
      case p.Success(x, _)     => x
      case p.Failure(msg, _)   => fail(msg)
      case p.Error(msg,_)      => fail(msg)
    }

    Result should equal (DMS(-3,48,1))

  }
  
  "A positive longitude" should "be broken into a DMS case class" in {
    val posLonText = " 034 43 00"
    val p = new WBANStationParser()
    val Result = p.parseAll(p.Lon, posLonText) match {
      case p.Success(x, _)     => x
      case p.Failure(msg, _)   => fail(msg)
      case p.Error(msg,_)      => fail(msg)
    }

    Result should equal (DMS(34,43,0))

  }
  "A negative longitude" should "be broken into a DMS case class" in {
    val negLonText = "-003 48 01"
    val p = new WBANStationParser()
    val Result = p.parseAll(p.Lon, negLonText) match {
      case p.Success(x, _)     => x
      case p.Failure(msg, _)   => fail(msg)
      case p.Error(msg,_)      => fail(msg)
    }

    Result should equal (DMS(-3,48,1))

  }
  
    

}
