This small console app can help you watch your streaks (habbit streaks or some other).

It can provide info like: 'I don't smoke for 75 days', 'I use this pair of contact lenses for 14 days', 'I've had sex 3 times in 2017' (oh, man) and so on.

You need [Java 8](https://www.java.com) to run Streak Counter and JDK 8 + Maven to build from sources.

How to use:
1. Download release jar file or build from sources;
1. Execute 'java -jar streakcounter.jar' (better create .bat or .sh script to stop writing 'java -jar blabla');
1. Use some commands like 'java -jar streakcounter.jar add test';
1. Add streaks, save breaks, get stats.

Streak Counter stores its database in a file. File location is set in streakCounter.properties and defaults to current directory. Database can be put in Dropbox or other cloud service directory to sync between desktops.

Available commands:
* add <name> - add streak
* break <list of names or numbers separated by comma> [days in the past] - break the streak
* reset <name or number> - delete all breaks from streak with given name or number and reset creation date
* delete <name or number> - delete streak (info still kept in database)
* since [dd.mm.yyyy] - count breaks since date (no date - since year start)
* stats - year by year stats
* help - this text

Example output with comments:
```
$ sc                                          #script running java -jar streakCounter.jar
=== 31.12.2017 15:26:05 ===                   #last db change time
01. Lenses -> 4                               #4 days without lenses
02. SEX -> 3                                  #3 days without sex
---------------------------                   #separator of good/neutral streaks from bad streaks
03. DIET -> 2                                 #2 days on a diet
04. SMOKING -> 2                              #2 days without smoking
05. ALCOHOL -> 5                              #5 days without alcohol


$ sc since                                    #this year stats
01. Lenses -> 0/2 (0%) created 11.12.2017     #used lenses 0/2 days this year
02. SEX -> 0/2 (0%) created 18.06.2016
03. DIET -> 0/2 (0%) created 18.06.2016
04. SMOKING -> 0/2 (0%) created 18.06.2016
05. ALCOHOL -> 0/2 (0%) created 18.06.2016


$ sc stats
          Lenses    SEX       DIET      SMOKING   ALCOHOL  
2016                120(32%)  153(41%)  118(32%)  67(18%)  
2017      14(3%)    203(55%)  244(66%)  175(47%)  55(15%)  
```
