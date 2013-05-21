package odyssey
import com.github.nscala_time.time.Imports._
object Station {
  // stuff related to weather stations
  
  // first goal is to be able to read the inventory of WBAN stations from NOAA at
  // ftp://ftp.ncdc.noaa.gov/pub/data/inventories/WBAN.TXT
  // formats are listed in:
  // ftp://ftp.ncdc.noaa.gov/pub/data/inventories/WBAN-FMT.TXT
  // although I don't think that file is completely right

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
      LatLonPrecision:   String,
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
  case class WBAN(
      CoopStationID:                  Option[String],  // 01 - 06     Coop Station Id
      ClimateDivision:                Option[String],  // 08 - 09     Climate Division
      WBANStationID:                  String,          // 11 - 15     WBAN Station Id <- this is the primary key?
      WMOStationID:                   Option[String],  // 17 - 21     WMO Station Id
      FAALOCID:                       Option[String],  // 23 - 26     FAA LOC ID
      NWSLocationIdentifier:          Option[String],  // 28 - 32     NWS Location Identifier
      ICAOStationID:                  Option[String],  // 34 - 37     ICAO Station Id
      Country:                        Option[String],  // 39 - 58     Country
      StateProvinceAbbreviation:      Option[String],  // 60 - 61     State/Province Abbreviation  ( United States/Canada Only )
      CountyName:                     Option[String],  // 63 - 92     County Name ( United States Only ) 
      TimeZone:                       Option[String],  // 94 - 98     Time Zone ( Number of Hours Added to Local Time to Get Greenwich Time )
      HistoricalCoopStationName:      Option[String],  //100 - 129    Historical Coop Station Name
      HistoricalWBANStationName:      Option[String],  //131 - 160    Historical WBAN Station Name
      DateSpan:                       DateSpan,        //162 - 169    Beginning Date of Period of Record ( YYYYMMDD )  ( "00000101" => Unknown Begin Date )
                                                       //171 - 178    Ending Date of Period of Record ( YYYYMMDD )  ( "99991231" => Station Currently Open )
      Location:                       LatLonElevation  //180 - 180    Latitude Direction  ( " " => North, "-" => South ) //181 - 182    Latitude Degrees //184 - 185    Latitude Minutes  //187 - 188    Latitude Seconds
                                                       //191 - 193    Longitude Direction  ( " " => East, "-" => West )  //195 - 196    Longitude Degrees  //198 - 199    Longitude Minutes //201 - 202    Longitude Seconds
                                                       // I think this is not actually in the data!!!
                                                       //201 - 202     Latitude/Longitude Precision Code ( "54" => Degrees, Whole Minutes ) ( "55" => Degrees, Whole Minutes, Whole Seconds ) ( "63" => Degrees, Decimal Minutes to Hundredths ) ( "64" => Degrees, Decimal Minutes to Thousandths ) ( "66" => Deg, Minutes, Decimal Seconds to Tenths ) ( "67" => Deg, Min, Decimal Seconds to Hundredths )
                                                       // that means you should subtract 3 from the following ones.
                                                       //204 - 209     Elevation - Ground  ( Feet )
                                                       //211 - 216     Elevation  ( Feet )
                                                       //218 - 219     Elevation Type Code ( "0" => Unknown Elevation Type ) ( "2" => Barometer Ivory Point ) ( "6" => Ground ) ( "7" => Airport )
                                                       //221 - 231     Station Relocation
                                                       //233 - 282     Station Types
                                                       //283 - 284     Blank
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
  
  def parseLine(L: String): List[String] = {
    val LineMatch = ("^(.{6})." +  // 01 - 06    Coop Station Id
                     "(.{2})." +  // 08 - 09     Climate Division
                     "(.{5})." +  // 11 - 15     WBAN Station Id <- this is the primary key?
                     "(.{5})." +  // 17 - 21     WMO Station Id
                     "(.{4})." +  // 23 - 26     FAA LOC ID
                     "(.{5})." +  // 28 - 32     NWS Location Identifier
                     "(.{4})." +  // 34 - 37     ICAO Station Id
                     "(.{20})." +  // 39 - 58     Country
                     "(.{2})." +  // 60 - 61     State/Province Abbreviation  ( United States/Canada Only )
                     "(.{30})." +  // 63 - 92     County Name ( United States Only ) 
                     "(.{5})." +  // 94 - 98     Time Zone ( Number of Hours Added to Local Time to Get Greenwich Time )
                     "(.{30})." +  //100 - 129    Historical Coop Station Name
                     "(.{30})." +  //131 - 160    Historical WBAN Station Name
                     "(.{17})." +  //162 - 169    Beginning Date of Period of Record ( YYYYMMDD )  ( "00000101" => Unknown Begin Date )
                                   //171 - 178    Ending Date of Period of Record ( YYYYMMDD )  ( "99991231" => Station Currently Open )
                     "(.{37})." +  //180 - 180    Latitude Direction  ( " " => North, "-" => South ) 
                                   //181 - 182    Latitude Degrees 
                                   //184 - 185    Latitude Minutes  
                                   //187 - 188    Latitude Seconds
                                   //190 - 190    Longitude Direction  ( " " => East, "-" => West )  
                                   //191 - 193    Longitude Degrees  <-- this is a mistake in the NOAA doc
                                   //195 - 196    Longitude Minutes 
                                   //198 - 199    Longitude Seconds
                                   //201 - 206     Elevation - Ground  ( Feet )
                                   //208 - 213     Elevation  ( Feet )
                                   //216 - 216     Elevation Type Code ( "0" => Unknown Elevation Type ) ( "2" => Barometer Ivory Point ) ( "6" => Ground ) ( "7" => Airport )
                     "(.{11})." + //218 - 228     Station Relocation
                     "(.{49})." +    //230 - 278     Station Types
                     "(.{6}).*").r //280 - 285     Don't know???  then two blank
     val LineMatch(coop,cdiv,wban,wmo,faa,nws,icao,
         country,state,county,tz,histcoop,histwban,
         daterange,loc,reloc,types,dontknow) = L
     
     coop :: cdiv :: wban :: wmo :: faa :: nws :: icao ::
       country :: state :: county :: tz :: histcoop ::
       histwban :: daterange :: loc :: reloc :: types ::
       dontknow :: Nil
  }

}