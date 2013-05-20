package odyssey

object Station {
  // related to weather stations
  
  // going to read 
  // TODO: replace .TXT reference with 
  // ftp://ftp.ncdc.noaa.gov/pub/data/inventories/WBAN.TXT.Z
  // formats are listed in:
  // ftp://ftp.ncdc.noaa.gov/pub/data/inventories/WBAN-FMT.TXT

  case class WBAN(
      CoopStationID:                  Option[String],  // 01 - 06     Coop Station Id
      ClimateDivision:                Option[String],  // 08 - 09     Climate Division
      WBANStationID:                  Option[String],  // 11 - 15     WBAN Station Id
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
      BeginningDate:                  Option[String],  //162 - 169    Beginning Date of Period of Record ( YYYYMMDD )  ( "00000101" => Unknown Begin Date )
      EndingDate:                     Option[String],  //171 - 178    Ending Date of Period of Record ( YYYYMMDD )  ( "99991231" => Station Currently Open )
      Latitude:                       Option[String],  //180 - 180    Latitude Direction  ( " " => North, "-" => South ) //181 - 182    Latitude Degrees //184 - 185    Latitude Minutes  //187 - 188    Latitude Seconds
      Longitude:                      Option[String],  //191 - 193    Longitude Direction  ( " " => East, "-" => West )  //195 - 196    Longitude Degrees  //198 - 199    Longitude Minutes //201 - 202    Longitude Seconds
      LatitudeLongitudePrecisionCode: Option[String],  //201 - 202     Latitude/Longitude Precision Code ( "54" => Degrees, Whole Minutes ) ( "55" => Degrees, Whole Minutes, Whole Seconds ) ( "63" => Degrees, Decimal Minutes to Hundredths ) ( "64" => Degrees, Decimal Minutes to Thousandths ) ( "66" => Deg, Minutes, Decimal Seconds to Tenths ) ( "67" => Deg, Min, Decimal Seconds to Hundredths )
      Elevation:                      Option[String],  //204 - 209     Elevation - Ground  ( Feet )
                                                       //211 - 216     Elevation  ( Feet )
                                                       //218 - 219     Elevation Type Code ( "0" => Unknown Elevation Type ) ( "2" => Barometer Ivory Point ) ( "6" => Ground ) ( "7" => Airport )
      StationRelocation:              Option[String],  //221 - 231     Station Relocation
      StationTypes:                   Option[String]   //233 - 282     Station Types
      //283 - 284     Blank
  )
  
  lazy val WBANInventoryText = scala.io.Source.fromURL("ftp://ftp.ncdc.noaa.gov/pub/data/inventories/WBAN.TXT")
  
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