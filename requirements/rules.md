Create the following utility methods on the class SwedishHolidays:
- isHoliday - given a date (all java11 date types) return boolean
    - Add support to send in java.sql.Date
    - java.sql.Timestamp
    - 
- isHolidayToday - no input return boolean
- listHolidays - input language (SE/EN enum or String). Respond with a list of holidays with date, name and description (explaining the rules for that holiday calculation)
- listHolidaysSE - no input. Respond with a list of holidays with date, name and description (explaining the rules for that holiday calculation) (this is a shorthand method for list holidays with a specified lang)
- listHolidaysEN - no input. Respond with a list of holidays with date, name and description (explaining the rules for that holiday calculation) (this is a shorthand method for list holidays with a specified lang)
- If no timezone is specified assume swedish timestamp (stockholm)
- If timezone is specified, recalculate the point in time to what date it is in sweden at this time.
- Gather a description of the logic and print it as a single log line.


Name | Calculation
New Year’s Day [Note 1] | Fixed date, January 1
Epiphany Eve [Note 2] | Fixed date, January 5
Epiphany | Fixed date, January 6 (Epiphany)
Maundy Thursday [Note 2] | Movable date, Thursday before Easter Sunday
Good Friday | Movable date, Friday before Easter Sunday
Easter Eve | Movable date, Saturday before Easter Sunday
Easter Sunday | Movable date, first Sunday after the ecclesiastical full moon following the vernal equinox
Easter Monday | Movable date, the day after Easter Sunday (Monday)
Walpurgis Eve [Note 2] | Fixed date, April 30
May Day | Fixed date, May 1
Ascension Day | Movable date, the sixth Thursday after Easter Sunday
National Day of Sweden | Fixed date, June 6
Whitsun Eve | Movable date, the day before Pentecost (Saturday)
Pentecost | Movable date, seventh Sunday after Easter Sunday
Midsummer Eve [Note 3] | Movable date, Friday between June 19 and June 25 (Friday before Midsummer Day)
Midsummer Day | Movable date, Saturday between June 20 and June 26
All Saints’ Eve [Note 2] | Movable date, Friday between October 30 and November 5
All Saints’ Day | Movable date, Saturday between October 31 and November 6
Christmas Eve [Note 3] | Fixed date, December 24
Christmas Day | Fixed date, December 25
St. Stephen’s Day (Boxing Day) | Fixed date, December 26
New Year’s Eve [Note 3] | Fixed date, December 31

Here is the swedish translation, the first column is just the date for 2025, the real rules are in the last column.

2025	Namn	Beräkning
1 jan	Nyårsdagen[Not 1]	Fast datum, 1 januari
5 jan	Trettondagsafton[Not 2]	Fast datum, 5 januari
6 jan	Trettondedag jul	Fast datum, 6 januari (Trettondedagen)
17 apr	Skärtorsdagen[Not 2]	Rörligt datum, torsdagen före Påskdagen
18 apr	Långfredagen	Rörligt datum, fredagen före Påskdagen
19 apr	Påskafton	Rörligt datum, lördagen före Påskdagen
20 apr	Påskdagen	Rörligt datum, första söndagen efter ecklesiastisk fullmåne, efter vårdagjämningen
21 apr	Annandag påsk	Rörligt datum, dagen efter påskdagen (d.v.s. en måndag)
30 apr	Valborgsmässoafton[Not 2]	Fast datum, 30 april
1 maj	Första maj	Fast datum, 1 maj
29 maj	Kristi himmelsfärdsdag	Rörligt datum, sjätte torsdagen efter påskdagen
6 jun	Sveriges nationaldag	Fast datum, 6 juni
7 jun	Pingstafton	Rörligt datum, dagen före pingstdagen (d.v.s. en lördag)
8 jun	Pingstdagen	Rörligt datum, sjunde söndagen efter påskdagen
20 jun	Midsommarafton[Not 3]	Rörligt datum, fredagen mellan 19 juni och 25 juni (fredagen före midsommardagen)
21 jun	Midsommardagen	Rörligt datum, lördagen mellan 20 juni och 26 juni
31 okt	Allhelgonaafton[Not 2]	Rörligt datum, fredagen mellan 30 oktober och 5 november
1 nov	Alla helgons dag	Rörligt datum, lördagen mellan 31 oktober och 6 november
24 dec	Julafton[Not 3]	Fast datum, 24 december
25 dec	Juldagen	Fast datum, 25 december
26 dec	Annandag jul	Fast datum, 26 december
31 dec	Nyårsafton[Not 3]	Fast datum, 31 december


