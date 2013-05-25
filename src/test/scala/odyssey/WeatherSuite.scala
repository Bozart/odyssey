package odyssey

import org.scalatest._
import Station._
import com.github.nscala_time.time.Imports._

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
  "a lat, lon, and elevation" should "be put into LatLonElevation case class" in {
    val LocData = " 40 46 00 -073 52 00 -99999     52  2"
    val p = new WBANStationParser()
    val Result = p.parseAll(p.Location, LocData) match {
      case p.Success(x, _)     => x
      case p.Failure(msg, _)   => fail(msg)
      case p.Error(msg,_)      => fail(msg)
    }

    Result should equal (LatLonElevation(DMS(40,46,0),DMS(-73,52,0),None,Some(52),Some(2)))

  }
  "a complete date range" should "be read into a DateSpan case class" in {
    val DateData = "19391001 19391007"
    val p = new WBANStationParser()
    val Result = p.parseAll(p.DateSpan, DateData) match {
      case p.Success(x, _)     => x
      case p.Failure(msg, _)   => fail(msg)
      case p.Error(msg,_)      => fail(msg)
    }

    Result should equal (DateSpan(Some(new LocalDate(1939,10,1)),Some(new LocalDate(1939,10,7))))

  }
  "a date range with unknown start" should "be read into a DateSpan case class" in {
    val DateData = "00000101 19391007"
    val p = new WBANStationParser()
    val Result = p.parseAll(p.DateSpan, DateData) match {
      case p.Success(x, _)     => x
      case p.Failure(msg, _)   => fail(msg)
      case p.Error(msg,_)      => fail(msg)
    }

    Result should equal (DateSpan(None,Some(new LocalDate(1939,10,7))))

  }
  "a date range with unknown end" should "be read into a DateSpan case class" in {
    val DateData = "19391001 99991231"
    val p = new WBANStationParser()
    val Result = p.parseAll(p.DateSpan, DateData) match {
      case p.Success(x, _)     => x
      case p.Failure(msg, _)   => fail(msg)
      case p.Error(msg,_)      => fail(msg)
    }

    Result should equal (DateSpan(Some(new LocalDate(1939,10,1)),None))

  }
  "the full laguardia record" should "parse into a WBAN record" in {
    val FullRec = "       04 14732                       UNITED STATES        NY QUEENS                         +5    LAGUARDIA                      LAGUARDIA                      19391001 19391007  40 46 00 -073 52 00 -99999     52  2             AIRWAYS                                           -99999  "
    val p = new WBANStationParser()
    val Result = p.parseAll(p.WBAN, FullRec) match {
      case p.Success(x, _)     => x
      case p.Failure(msg, _)   => fail(msg)
      case p.Error(msg,_)      => fail(msg)
    }
    
    Result should equal (WBAN(
        None,Some("04"),"14732",None,None,None,None,Some("UNITED STATES"),
        Some("NY"),Some("QUEENS"),Some("+5"),Some("LAGUARDIA"),Some("LAGUARDIA"),
        DateSpan(
            Some(new LocalDate(1939,10,1)),
            Some(new LocalDate(1939,10,7))),
            LatLonElevation(DMS(40,46,0),DMS(-73,52,0),None,Some(52),Some(2)),
            None,Some("AIRWAYS")))
  
  }
}
