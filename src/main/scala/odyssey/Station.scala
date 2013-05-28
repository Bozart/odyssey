package odyssey

import com.github.nscala_time.time.Imports._
import scala.util.parsing.combinator.RegexParsers

// parser to read weather station records in the WBAN inventory
/** WBANStationParser is a parser for records from the NOAA WBAN inventory
 *  This class uses parsers to build WBAN objects from records in the NOAA
 *  WBAN station inventory.
 *  
 *  The formats are found at [[ftp://ftp.ncdc.noaa.gov/pub/data/inventories/WBAN-FMT.TXT]]
 *  However, I think that the format listed in that file is out of date or otherwise incorrect.
 *  
 *  @todo clean up the namespace, but still allow unit testing to work
 */
class WBANStationParser extends RegexParsers {
  //EXAMPLE:                                                                                           1                                                                                                   2                                                                                      
  //         1         2         3         4         5         6         7         8         9         0         1         2         3         4         5         6         7         8         9         0         1         2         3         4         5         6         7         8         9         0         1         2         3
  //123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890  
  //
  //       04 14732                       UNITED STATES        NY QUEENS                         +5    LAGUARDIA                      LAGUARDIA                      19391001 19391007  40 46 00 -073 52 00 -99999     52  2             AIRWAYS                                           -99999  
  //          94789                       UNITED STATES        NY QUEENS                         +5    NEW YORK FT TOTTEN             NEW YORK FT TOTTEN             19730314 19800902  40 47 00 -073 46 00     26     26  2             UPPERAIR                                              26  
  //305803 04 94789                       UNITED STATES        NY QUEENS                         +5    NEW YORK IDLEWILD AP           NEW YORK IDLEWILD AP           19501231 19510101  40 39 00 -073 47 00     20     20  6             AIRWAYS COOP UPPERAIR                                 20  
  //          04776                       CANADA               QC                                      SENNETERRE                     SENNETERRE                     19600401 19601231  48 20 00 -077 11 00 -99999   1113  0                                                               -99999  
  //          33034       DABB            ALGERIA                                                      ANNABA                         ANNABA                         19450601 19451031  36 50 00  007 48 00 -99999      4  0                                                               -99999  

  
  //because data is encoded with position, we can't skip the whitespace.
  override def skipWhitespace = false

  // helper functions to define regular width text or numbers
  def txt(n: Int) = ("." * n).r ^^ {_.trim}
  def num(n: Int) = txt(n) ^^ { _.toInt }
  def maybeTxt(n: Int) = txt(n) ^^ {
    _ match {
      case "" => None
      case x  => Some(x)
    }
  }
  def maybeNum(n: Int) = txt(n) ^^ {
    _ match {
      case "" => None
      case x  => Some(x.toInt)
    }
  }
  // function where if we have the missing value then we return nothing,
  // otherwise Option(v)
  def numMissing(v: Int, missval: Int) = v match {
    case `missval` => None
    case x         => Some(x)
  }
  def dateMissing(d: LocalDate, missval: LocalDate) = d match {
    case `missval` => None
    case x         => Some(x)
  }
  
  // read a date in yyyymmdd format
  def yyyymmdd = num(4) ~ num(2) ~ num(2) ^^ {case (y~m~d) => new LocalDate(y,m,d)}
  val unknownStartDate = new LocalDate(0,1,1)
  val unknownEndDate = new LocalDate(9999,12,31)
  
  def startDate = yyyymmdd ^^ {dateMissing(_,unknownStartDate)}
  def endDate = yyyymmdd ^^ {dateMissing(_,unknownEndDate)}
  def dateSpan = startDate ~ (" " ~> endDate) ^^ {
    case (start~end) => Station.DateSpan(start, end)
  }
  
  // parser indicating that we have up to n numbers, and that if the 
  // number matches the missing value, return None.
  def optNum(n: Int, missval: Int) = num(n) ^^ {numMissing(_, missval)}
  
  // parsers for latitude and longitude
  def latDeg = num(3)
  def lonDeg = num(4)
  def min = num(2)
  def sec = num(2)
  
  def lat = latDeg ~ (" " ~> min) ~ (" " ~> sec) ^^ {
    case (d~m~s) => Station.DMS(d,m,s)
  }
  def lon = lonDeg ~ (" " ~> min) ~ (" " ~> sec) ^^ {
    case (d~m~s) => Station.DMS(d,m,s)
  }
  
  // elevation
  def ele = optNum(6,-99999)
  def eleType = maybeNum(2)
  
  // location combines the latitude, longitude, and elevation
  def location = lat ~ (" " ~> lon) ~ (" " ~> ele) ~ (" " ~> ele) ~ (" " ~> eleType) ^^ {
    case (lat~lon~eleg~ele~typ) => Station.LatLonElevation(lat,lon,eleg,ele,typ) 
  }
  
  // there is some stuff at the end that we'll ignore for now.
  def blankEnd = ".*".r
  def WBAN = 
      maybeTxt(6) ~              // 01 - 06     Coop Station Id
      (" " ~> maybeTxt(2)) ~     // 08 - 09     Climate Division
      (" " ~> txt(5)) ~          // 11 - 15     WBAN Station Id
      (" " ~> maybeTxt(5)) ~     // 17 - 21     WMO Station Id
      (" " ~> maybeTxt(4)) ~     // 23 - 26     FAA LOC ID
      (" " ~> maybeTxt(5)) ~     // 28 - 32     NWS Location Identifier
      (" " ~> maybeTxt(4)) ~     // 34 - 37     ICAO Station Id
      (" " ~> txt(20)) ~         // 39 - 58     Country
      (" " ~> maybeTxt(2)) ~     // 60 - 61     State/Province Abbreviation  ( United States/Canada Only )
      (" " ~> maybeTxt(30)) ~    // 63 - 92     County Name ( United States Only ) 
      (" " ~> maybeTxt(5)) ~     // 94 - 98     Time Zone ( Number of Hours Added to Local Time to Get Greenwich Time )
      (" " ~> txt(30)) ~         //100 - 129    Historical Coop Station Name
      (" " ~> txt(30)) ~         //131 - 160    Historical WBAN Station Name
      (" " ~> dateSpan) ~        //162 - 169    Beginning Date of Period of Record ( YYYYMMDD )  ( "00000101" => Unknown Begin Date )
                                 //171 - 178    Ending Date of Period of Record ( YYYYMMDD )  ( "99991231" => Station Currently Open )
      (" " ~> location) ~        //180 - 180    Latitude Direction  ( " " => North, "-" => South ) //181 - 182    Latitude Degrees //184 - 185    Latitude Minutes  //187 - 188    Latitude Seconds
                                 //191 - 193    Longitude Direction  ( " " => East, "-" => West )  //195 - 196    Longitude Degrees  //198 - 199    Longitude Minutes //201 - 202    Longitude Seconds
                                 // I think this is not actually in the data!!!
                                 //201 - 202     Latitude/Longitude Precision Code ( "54" => Degrees, Whole Minutes ) ( "55" => Degrees, Whole Minutes, Whole Seconds ) ( "63" => Degrees, Decimal Minutes to Hundredths ) ( "64" => Degrees, Decimal Minutes to Thousandths ) ( "66" => Deg, Minutes, Decimal Seconds to Tenths ) ( "67" => Deg, Min, Decimal Seconds to Hundredths )
                                 // that means you should subtract 3 from the following ones.
                                 //204 - 209     Elevation - Ground  ( Feet )
                                 //211 - 216     Elevation  ( Feet )
                                 //218 - 219     Elevation Type Code ( "0" => Unknown Elevation Type ) ( "2" => Barometer Ivory Point ) ( "6" => Ground ) ( "7" => Airport )
      (" " ~> maybeTxt(11)) ~    //221 - 231     Station Relocation
      (" " ~> maybeTxt(50)) ~    //233 - 282     Station Types
      (blankEnd) ^^              //283 - 284     Blank
      {case         (coopid~cd~wbanid~wmoid~faaid~nwsid~icaoid~country~state~county~tz~histcoop~histwban~datespan~loc~reloc~typ~b) =>
        Station.WBAN(coopid,cd,wbanid,wmoid,faaid,nwsid,icaoid,country,state,county,tz,histcoop,histwban,datespan,loc,reloc,typ)
      }
}

object Station {
  // classes which define the weather station containers

  //the inventory of WBAN stations from NOAA is located at
  val inventoryURL = "ftp://ftp.ncdc.noaa.gov/pub/data/inventories/WBAN.TXT"

  // TODO: replace .TXT reference with 
  val inventoryURLZ = "ftp://ftp.ncdc.noaa.gov/pub/data/inventories/WBAN.TXT.Z"
  // to do that, I think the best approach is a loan pattern where we
  // download the .Z file to a temporary location, unzip it, and then
  // iterate over the values.  The finally clauses would close and then
  // delete the temporary files.

  // class representing a latitude or longitude's information
  case class DMS(
    Degrees: Int,
    Minutes: Int,
    Seconds: Int)

  // class combining a latitude and longitude with elevation data
  case class LatLonElevation(
    Latitude: DMS,
    Longitude: DMS,
    ElevationGround: Option[Int],
    Elevation: Option[Int],
    ElevationTypeCode: Option[Int])

  // DateSpan is the starting and ending date when the weather station is active for
  // if the beginning date is None then it means the date is unknown,
  // if the ending date is None then the station is still active.
  case class DateSpan(
    BeginningDate: Option[LocalDate],
    EndingDate: Option[LocalDate])

  // complete station data
  // note: WBANStationID and DateSpan constitute a candidate key
  case class WBAN(
    CoopStationID: Option[String],
    ClimateDivision: Option[String],
    WBANStationID: String,
    WMOStationID: Option[String],
    FAALOCID: Option[String],
    NWSLocationIdentifier: Option[String],
    ICAOStationID: Option[String],
    Country: String,
    StateProvinceAbbreviation: Option[String],
    CountyName: Option[String],
    TimeZone: Option[String],
    HistoricalCoopStationName: String,
    HistoricalWBANStationName: String,
    DateSpan: DateSpan,
    Location: LatLonElevation,
    Relocation: Option[String],
    StationTypes: Option[String])

  // function which turns a single line from the wban inventory into a WBAN
  val WP = new WBANStationParser()

  def parseLine(s: String) =
    WP.parseAll(WP.WBAN, s) match {
      case WP.Success(x, _)   => x
      case WP.Failure(msg, _) => None // add some error checking here I guess?
      case WP.Error(msg, _)   => None
    }
}