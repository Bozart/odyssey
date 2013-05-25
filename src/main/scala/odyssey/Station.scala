package odyssey

import com.github.nscala_time.time.Imports._
import scala.util.parsing.combinator.RegexParsers

// parser to read weather station records in the WBAN inventory
class WBANStationParser extends RegexParsers {
  // formats are listed in:
  // ftp://ftp.ncdc.noaa.gov/pub/data/inventories/WBAN-FMT.TXT
  // although I don't think that file is completely right

  //because data is encoded with position, we can't skip the whitespace.
  override def skipWhitespace = false

  // helper functions to define regular width text or numbers
  def txt(n: Int) = ("." * n).r ^^ {_.trim}
  def num(n: Int) = txt(n) ^^ { _.toInt }
  def maybetxt(n: Int) = txt(n) ^^ {
    _ match {
      case "" => None
      case x  => Some(x)
    }
  }
  def maybenum(n: Int) = txt(n) ^^ {
    _ match {
      case "" => None
      case x  => Some(x.toInt)
    }
  }
  // function where if we have the missing value then we return nothing,
  // otherwise Option(v)
  def NumMissing(v: Int, missval: Int) = v match {
    case `missval` => None
    case x         => Some(x)
  }
  def DateMissing(d: LocalDate, missval: LocalDate) = d match {
    case `missval` => None
    case x         => Some(x)
  }
  
  // read a date in yyyymmdd format
  def yyyymmdd = num(4) ~ num(2) ~ num(2) ^^ {case (y~m~d) => new LocalDate(y,m,d)}
  val unknownStartDate = new LocalDate(0,1,1)
  val unknownEndDate = new LocalDate(9999,12,31)
  
  def startdate = yyyymmdd ^^ {DateMissing(_,unknownStartDate)}
  def enddate = yyyymmdd ^^ {DateMissing(_,unknownEndDate)}
  def DateSpan = startdate ~ (" " ~> enddate) ^^ {
    case (start~end) => Station.DateSpan(start, end)
  }
  
  // parser indicating that we have up to n numbers, and that if the 
  // number matches the missing value, return None.
  def optnum(n: Int, missval: Int) = num(n) ^^ {NumMissing(_, missval)}
  
  def LatDeg = num(3)
  def LonDeg = num(4)
  def Min = num(2)
  def Sec = num(2)
  
  def Lat = LatDeg ~ (" " ~> Min) ~ (" " ~> Sec) ^^ {
    case (d~m~s) => Station.DMS(d,m,s)
  }
  def Lon = LonDeg ~ (" " ~> Min) ~ (" " ~> Sec) ^^ {
    case (d~m~s) => Station.DMS(d,m,s)
  }
  
  def Ele = optnum(6,-99999)
  def EleType = maybenum(2)
  
  
  def Location = Lat ~ (" " ~> Lon) ~ (" " ~> Ele) ~ (" " ~> Ele) ~ (" " ~> EleType) ^^ {
    case (lat~lon~eleg~ele~typ) => Station.LatLonElevation(lat,lon,eleg,ele,typ) 
  }
  def BlankEnd = ".*".r
  def WBAN = 
      maybetxt(6) ~              // 01 - 06     Coop Station Id
      (" " ~> maybetxt(2)) ~     // 08 - 09     Climate Division
      (" " ~> txt(5)) ~          // 11 - 15     WBAN Station Id <- this is the primary key?
      (" " ~> maybetxt(5)) ~     // 17 - 21     WMO Station Id
      (" " ~> maybetxt(4)) ~     // 23 - 26     FAA LOC ID
      (" " ~> maybetxt(5)) ~     // 28 - 32     NWS Location Identifier
      (" " ~> maybetxt(4)) ~     // 34 - 37     ICAO Station Id
      (" " ~> maybetxt(20)) ~    // 39 - 58     Country
      (" " ~> maybetxt(2)) ~     // 60 - 61     State/Province Abbreviation  ( United States/Canada Only )
      (" " ~> maybetxt(30)) ~    // 63 - 92     County Name ( United States Only ) 
      (" " ~> maybetxt(5)) ~     // 94 - 98     Time Zone ( Number of Hours Added to Local Time to Get Greenwich Time )
      (" " ~> maybetxt(30)) ~    //100 - 129    Historical Coop Station Name
      (" " ~> maybetxt(30)) ~    //131 - 160    Historical WBAN Station Name
      (" " ~> DateSpan) ~        //162 - 169    Beginning Date of Period of Record ( YYYYMMDD )  ( "00000101" => Unknown Begin Date )
                                 //171 - 178    Ending Date of Period of Record ( YYYYMMDD )  ( "99991231" => Station Currently Open )
      (" " ~> Location) ~        //180 - 180    Latitude Direction  ( " " => North, "-" => South ) //181 - 182    Latitude Degrees //184 - 185    Latitude Minutes  //187 - 188    Latitude Seconds
                                 //191 - 193    Longitude Direction  ( " " => East, "-" => West )  //195 - 196    Longitude Degrees  //198 - 199    Longitude Minutes //201 - 202    Longitude Seconds
                                 // I think this is not actually in the data!!!
                                 //201 - 202     Latitude/Longitude Precision Code ( "54" => Degrees, Whole Minutes ) ( "55" => Degrees, Whole Minutes, Whole Seconds ) ( "63" => Degrees, Decimal Minutes to Hundredths ) ( "64" => Degrees, Decimal Minutes to Thousandths ) ( "66" => Deg, Minutes, Decimal Seconds to Tenths ) ( "67" => Deg, Min, Decimal Seconds to Hundredths )
                                 // that means you should subtract 3 from the following ones.
                                 //204 - 209     Elevation - Ground  ( Feet )
                                 //211 - 216     Elevation  ( Feet )
                                 //218 - 219     Elevation Type Code ( "0" => Unknown Elevation Type ) ( "2" => Barometer Ivory Point ) ( "6" => Ground ) ( "7" => Airport )
      (" " ~> maybetxt(11)) ~    //221 - 231     Station Relocation
      (" " ~> maybetxt(50)) ~    //233 - 282     Station Types
      (BlankEnd) ^^              //283 - 284     Blank
      {case         (coopid~cd~wbanid~wmoid~faaid~nwsid~icaoid~country~state~county~tz~histcoop~histwban~datespan~loc~reloc~typ~b) =>
        Station.WBAN(coopid,cd,wbanid,wmoid,faaid,nwsid,icaoid,country,state,county,tz,histcoop,histwban,datespan,loc,reloc,typ)
      }
}


object Station {
  // stuff related to weather stations
  
  // first goal is to be able to read the inventory of WBAN stations from NOAA at
  // ftp://ftp.ncdc.noaa.gov/pub/data/inventories/WBAN.TXT

  // TODO: replace .TXT reference with 
  // ftp://ftp.ncdc.noaa.gov/pub/data/inventories/WBAN.TXT.Z

  // class representing a latitude or longitude's information
  case class DMS(
      Degrees:   Int,
      Minutes:   Int,
      Seconds:   Int
      )
  
  // class combining a lat lon with elevation data
  case class LatLonElevation(
      Latitude:          DMS,
      Longitude:         DMS,
      ElevationGround:   Option[Int],
      Elevation:         Option[Int],
      ElevationTypeCode: Option[Int]
      )
  
  // DateSpan is the starting and ending date when the weather station is active for
  // if the beginning date is None then it means the date is unknown,
  // if the ending date is None then the station is still active.
  case class DateSpan(
      BeginningDate: Option[LocalDate],
      EndingDate:    Option[LocalDate]
      )
  
  // complete station data
  case class WBAN(  // note: WBANStationID and DateSpan constitute a candidate key
      CoopStationID:                  Option[String],  
      ClimateDivision:                Option[String],  
      WBANStationID:                  String,          
      WMOStationID:                   Option[String],  
      FAALOCID:                       Option[String],  
      NWSLocationIdentifier:          Option[String],  
      ICAOStationID:                  Option[String],  
      Country:                        Option[String],  
      StateProvinceAbbreviation:      Option[String],  
      CountyName:                     Option[String],   
      TimeZone:                       Option[String],  
      HistoricalCoopStationName:      Option[String],  
      HistoricalWBANStationName:      Option[String],  
      DateSpan:                       DateSpan,        
      Location:                       LatLonElevation, 
      Relocation:                     Option[String],
      StationTypes:                   Option[String] 
  )
  
  lazy val WBANInventoryText = io.Source.fromURL("ftp://ftp.ncdc.noaa.gov/pub/data/inventories/WBAN.TXT")
  
//EXAMPLE:                                                                                           1                                                                                                   2                                                                                      
//         1         2         3         4         5         6         7         8         9         0         1         2         3         4         5         6         7         8         9         0         1         2         3         4         5         6         7         8         9         0         1         2         3
//123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890  
//
//       04 14732                       UNITED STATES        NY QUEENS                         +5    LAGUARDIA                      LAGUARDIA                      19391001 19391007  40 46 00 -073 52 00 -99999     52  2             AIRWAYS                                           -99999  
//          94789                       UNITED STATES        NY QUEENS                         +5    NEW YORK FT TOTTEN             NEW YORK FT TOTTEN             19730314 19800902  40 47 00 -073 46 00     26     26  2             UPPERAIR                                              26  
//305803 04 94789                       UNITED STATES        NY QUEENS                         +5    NEW YORK IDLEWILD AP           NEW YORK IDLEWILD AP           19501231 19510101  40 39 00 -073 47 00     20     20  6             AIRWAYS COOP UPPERAIR                                 20  
//          04776                       CANADA               QC                                      SENNETERRE                     SENNETERRE                     19600401 19601231  48 20 00 -077 11 00 -99999   1113  0                                                               -99999  
//          33034       DABB            ALGERIA                                                      ANNABA                         ANNABA                         19450601 19451031  36 50 00  007 48 00 -99999      4  0                                                               -99999  

}