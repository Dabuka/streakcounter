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
