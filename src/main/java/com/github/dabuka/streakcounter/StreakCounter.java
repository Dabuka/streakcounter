package com.github.dabuka.streakcounter;

import com.github.dabuka.streakcounter.jaxb.StreakData;
import com.github.dabuka.streakcounter.jaxb.StreakType;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Streak Counter. Make steaks and get to your goals! Fucker.
 *
 * @author Dabuka
 */
public final class StreakCounter {
    // All the super important static fields. This is not some fucking multithreaded app, baby :E
    /** Data from XML-file database */
    private static StreakData streakData;
    /** XML-file with all the important data about your streaks */
    private static File file;
    /** XML marshaller to do some marshalling shit.  */
    private static Marshaller marshaller;

    /** Private constructor. You better don't fuck with this class. This shit's serious. */
    private StreakCounter() {}

    /**
     * It does all the magic.
     *
     * @param args i'm not going to bother myself writing something usefull here. Look at the code.
     * @throws Exception gangstas do no exception handling.
     */
    public static void main(String... args) throws Exception {
        //database is kept in xml format in a file and is built upon xsd schema
        SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        //noinspection ConstantConditions
        Schema schema = sf.newSchema(StreakCounter.class.getClassLoader().getResource("xsd/streakCounter.xsd"));
        JAXBContext jaxbContext = JAXBContext.newInstance(StreakData.class);
        marshaller = jaxbContext.createMarshaller();
        marshaller.setSchema(schema);
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE); //SWAG
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        unmarshaller.setSchema(schema);

        //get database file or create it, database file location is kept in configuration file
        file = getDBFile();

        String xml = readDBFile();

        try (StringReader reader = new StringReader(xml)) {
            //string -> object model
            streakData = (StreakData) unmarshaller.unmarshal(reader);
        }

        String arg0 = args.length == 0 ? "" : args[0];

        switch (arg0) {
            case "help":
                usage();
                break;
            case "":
                list();
                break;
            case "add":
                if (args.length < 2) {
                    usage();
                } else {
                    add(args[1].trim());
                }
                break;
            case "break":
                if (args.length < 2) {
                    usage();
                } else {
                    int daysInPast = 0;
                    if (args.length > 2) {
                        daysInPast = Integer.parseInt(args[2].trim());
                    }
                    breakStreak(args[1].trim(), daysInPast);
                }
                break;
            case "reset":
                if (args.length < 2) {
                    usage();
                } else {
                    reset(args[1].trim());
                }
                break;
            case "delete":
                if (args.length < 2) {
                    usage();
                } else {
                    delete(args[1].trim());
                }
                break;
            case "since":
                if (args.length < 2) {
                    since(null);
                } else {
                    since(args[1].trim());
                }
                break;
            case "stats":
                stats();
                break;
            default:
                usage();
        }
    }

    /**
     * Gets database file or creates it if there is no database file found.
     *
     * @return database xml file.
     */
    private static File getDBFile() throws Exception {
        File db = new File(getDBFilePath());
        if (db.createNewFile()) {
            marshaller.marshal(new StreakData(), db);
        }
        return db;
    }

    /**
     * Gets the path of database file from config file. If there is no config file found - it's created and initialized
     * with default path (current dir).
     *
     * @return The path of database file.
     */
    private static String getDBFilePath() throws Exception {
        String paramName = "db.location";
        String configFilePath = "streakCounter.properties";
        File file = new File(configFilePath);
        if (file.createNewFile()) {
            try (PrintWriter out = new PrintWriter(file)) {
                out.print(paramName + "=streakCounter.xml");
            }
        }

        Properties props = new Properties();
        props.load(new FileReader(configFilePath));
        return props.getProperty(paramName);
    }

    /**
     * Reads database file to String.
     *
     * @return String representing the database.
     */
    private static String readDBFile() throws Exception
    {
        byte[] encoded = Files.readAllBytes(Paths.get(getDBFilePath()));
        return new String(encoded, Charset.defaultCharset());
    }

    /** Prints help to console. */
    private static void usage() {
        System.out.println("Streak Counter\n");
        System.out.println("This small console app can help you watch your streaks (habbit streaks or some other).");
        System.out.println("It can provide info like: 'I don't smoke for 75 days', " +
                "'I use this pair of contact lenses for 14 days', " +
                "'I've had sex 3 times in 2017' (oh, man) and so on\n");

        System.out.println("How to use: add streak, save breaks in app, get stats.\n");

        System.out.println("Streak Counter stores its database in a file. File location is set in " +
                "streakCounter.properties and defaults to current directory. Database can be put in Dropbox or other " +
                "cloud service directory to sync between desktops.\n");
        System.out.println("Visit https://github.com/Dabuka/streakcounter for more details\n");
        System.out.println("Available commands: ");
        System.out.println("add <name> - add streak");
        System.out.println("break <list of names or numbers separated by comma> [days in the past] - break the streak");
        System.out.println("reset <name or number> - delete all breaks from streak with given name or number and reset creation date");
        System.out.println("delete <name or number> - delete streak (info still kept in database)");
        System.out.println("since [dd.mm.yyyy] - count breaks since date (no date - since year start)");
        System.out.println("stats - year by year stats");
        System.out.println("help - this text");
    }

    private static void list() {
        if (streakData.getStreak().isEmpty()) {
            System.out.println("You have no streaks!\n");
            usage();
            return;
        }
        DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
        if (streakData.getLastUpdate() != null) {
            System.out.println(
                    "=== " + dateFormat.format(streakData.getLastUpdate().toGregorianCalendar().getTime()) + " ===");
        }

        NumberFormat format = new DecimalFormat("00");
        List<StreakType> streakList = getActualStreakList();
        for (int i = 0; i < streakList.size(); i++) {
            StreakType streak = streakList.get(i);

            XMLGregorianCalendar lastBreak = streak.getCreated();

            if (streak.getStreakBreaks() != null) {
                List<XMLGregorianCalendar> breaks = streak.getStreakBreaks().getStreakBreakDate();
                if (!breaks.isEmpty()) {
                    lastBreak = breaks.get(breaks.size() - 1);
                }
            }

            long interval = ChronoUnit.DAYS.between(
                    lastBreak.toGregorianCalendar().getTime().toInstant(), new Date().toInstant());

            System.out.println(format.format(i + 1) + ". " + streak.getName() + " -> " + interval);

            if (Boolean.TRUE.equals(streak.isBreakAfter())) {
                System.out.println("---------------------------");
            }
        }
    }

    private static void add(String name) throws Exception {
        List<StreakType> streakList = streakData.getStreak();
        for (StreakType streak : streakList) {
            if (name.equals(streak.getName())) {
                System.out.println("Streak with this name already exists.");
                return;
            }
        }

        StreakType newStreak = new StreakType();
        newStreak.setName(name);
        newStreak.setCreated(getCurrentDate());
        streakData.getStreak().add(newStreak);
        marshaller.marshal(streakData, file);
        list();
    }

    private static void breakStreak(String nameOrNumberList, int daysInPast) throws Exception {
        StringTokenizer tokenizer = new StringTokenizer(nameOrNumberList, ",");
        while (tokenizer.hasMoreTokens()) {
            String nameOrNumber = tokenizer.nextToken();
            StreakType streak = findStreakByNameOrNumber(nameOrNumber);
            if (streak == null) {
                return;
            }

            StreakType.StreakBreaks streakBreaks = streak.getStreakBreaks();
            if (streakBreaks == null) {
                streakBreaks = new StreakType.StreakBreaks();
                streak.setStreakBreaks(streakBreaks);
            }

            List<XMLGregorianCalendar> breakList = streakBreaks.getStreakBreakDate();
            XMLGregorianCalendar date = getCurrentDate();
            if (daysInPast != 0) {
                GregorianCalendar gregorianCalendar = date.toGregorianCalendar();
                gregorianCalendar.add(Calendar.DAY_OF_MONTH, -daysInPast);
                DatatypeFactory datatypeFactory = DatatypeFactory.newInstance();
                date = datatypeFactory.newXMLGregorianCalendar(gregorianCalendar);
            }
            breakList.add(date);
        }

        streakData.setLastUpdate(getCurrentDate());

        marshaller.marshal(streakData, file);
        list();
    }

    private static void reset(String nameOrNumber) throws Exception {
        StreakType streak = findStreakByNameOrNumber(nameOrNumber);
        if (streak == null) {
            return;
        }
        streak.setCreated(getCurrentDate());
        streak.setStreakBreaks(null);
        marshaller.marshal(streakData, file);
        list();
    }

    private static void delete(String nameOrNumber) throws Exception {
        StreakType streak = findStreakByNameOrNumber(nameOrNumber);
        if (streak == null) {
            return;
        }
        streak.setDeleted(getCurrentDate());
        marshaller.marshal(streakData, file);
        list();
    }

    /**
     * Get stats since specified date (or since the year start if no date provided).
     *
     * @param dateStr string representation of the date.
     */
    private static void since(String dateStr) throws Exception {
        if (dateStr == null) {
            dateStr = "01.01." + Calendar.getInstance().get(Calendar.YEAR);
        }

        GregorianCalendar gCal = new GregorianCalendar();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
        gCal.setTime(dateFormat.parse(dateStr));
        XMLGregorianCalendar sinceDate = DatatypeFactory.newInstance().newXMLGregorianCalendar(gCal);

        long diffInDays = ChronoUnit.DAYS.between(gCal.getTime().toInstant(), new Date().toInstant()) + 1;

        NumberFormat format = new DecimalFormat("00");

        List<StreakType> streakList = streakData.getStreak();
        for (int i = 0; i < streakList.size(); i++) {
            StreakType streak = streakList.get(i);
            int breaksCount = 0;

            StreakType.StreakBreaks streakBreaks = streak.getStreakBreaks();
            if (streakBreaks != null) {
                List<XMLGregorianCalendar> breakList = streakBreaks.getStreakBreakDate();
                for (XMLGregorianCalendar streakBreak : breakList) {
                    if (streakBreak.compare(sinceDate) >= 0) {
                        breaksCount++;
                    }
                }
            }

            System.out.println(format.format(i + 1) + ". " + streak.getName() + " -> " + breaksCount + "/"
                    + diffInDays + " (" + (breaksCount*100)/diffInDays + "%) created "
                    + dateFormat.format(streak.getCreated().toGregorianCalendar().getTime()));
        }
    }

    /** Gets year by year stats. */
    private static void stats() {
        List<StreakType> streaks = streakData.getStreak();
        int columnsCount = streaks.size() + 1; //+1 for first column with years

        StringBuilder formatStr = new StringBuilder();
        for (int i = 0; i < columnsCount; i++) {
            formatStr.append("%-9.9s");
            if (i == columnsCount - 1) {
                formatStr.append("%n");
            } else {
                formatStr.append(" ");
            }
        }
        String format = formatStr.toString();

        List<String[]> rows = new ArrayList<>();

        //get column names
        String[] columnNames = new String[columnsCount];
        columnNames[0] = "";
        for (int i = 0; i < streaks.size(); i++) {
            StreakType streak = streaks.get(i);
            columnNames[i + 1] = streak.getName();
        }
        rows.add(columnNames);

        //get data by year
        Map<String, Map<String,Integer>> dataMap = new TreeMap<>();
        for (StreakType streak : streaks) {
            String name = streak.getName();
            StreakType.StreakBreaks streakBreaks = streak.getStreakBreaks();
            List<XMLGregorianCalendar> breakDates
                    = streakBreaks == null ? new ArrayList<>() : streakBreaks.getStreakBreakDate();
            for (XMLGregorianCalendar breakDate : breakDates) {
                String year = String.valueOf(breakDate.getYear());
                Map<String, Integer> yearData = dataMap.computeIfAbsent(year, k -> new HashMap<>());
                Integer breaksCount = yearData.computeIfAbsent(name, k -> 0);
                breaksCount++;
                yearData.put(name, breaksCount);
            }
        }


        for (Map.Entry<String, Map<String, Integer>> dataMapEntry : dataMap.entrySet()) {
            String[] newRow = new String[columnsCount];
            String year = dataMapEntry.getKey();

            long daysInYear = getDaysInYear(year);

            Map<String, Integer> yearData = dataMapEntry.getValue();
            newRow[0] = year;
            for (int i = 1; i < columnsCount; i++) {
                Integer count = yearData.get(columnNames[i]);
                String countValue = count == null ? "" : String.valueOf(count) + "(" + count * 100 / daysInYear + "%)";
                newRow[i] = countValue;
            }
            rows.add(newRow);
        }

        for (String[] row : rows) {
            System.out.printf(format, (Object[]) row);
        }
    }

    /**
     * Get current date in form of {@link XMLGregorianCalendar}
     *
     * @return current date.
     */
    private static XMLGregorianCalendar getCurrentDate() throws DatatypeConfigurationException
    {
        GregorianCalendar gregorianCalendar = new GregorianCalendar();
        DatatypeFactory datatypeFactory = DatatypeFactory.newInstance();
        return datatypeFactory.newXMLGregorianCalendar(gregorianCalendar);
    }

    /**
     * Finds active streak by name or number.
     *
     * @param nameOrNumber name or number.
     * @return Streak.
     */
    private static StreakType findStreakByNameOrNumber(String nameOrNumber) {
        List<StreakType> streakActualList = getActualStreakList();

        try {
            int index = Integer.parseInt(nameOrNumber);
            return streakActualList.get(index - 1);
        } catch (NumberFormatException ignore) {}

        for (StreakType streak : streakActualList) {
            if (nameOrNumber.equalsIgnoreCase(streak.getName())) {
                return streak;
            }
        }

        System.out.println("Record not found by the key " + nameOrNumber);
        return null;
    }

    /**
     * Gets all actual (not deleted) streaks.
     *
     * @return Streak list.
     */
    private static List<StreakType> getActualStreakList() {
        List<StreakType> streakList = streakData.getStreak();
        return streakList.stream().filter(streak -> streak.getDeleted() == null).collect(Collectors.toList());
    }

    /**
     * Gets the number of days in a year.
     *
     * @param year omg, string year.
     * @return no way, it's the number of days!
     */
    @SuppressWarnings("UseOfObsoleteDateTimeApi")
    private static long getDaysInYear(String year) {
        Calendar cal = Calendar.getInstance();
        String currentYear = String.valueOf(cal.get(Calendar.YEAR));

        if (currentYear.equals(year)) {
            Calendar yearStart = Calendar.getInstance();
            yearStart.set(yearStart.get(Calendar.YEAR), Calendar.JANUARY, 1);
            return ChronoUnit.DAYS.between(yearStart.toInstant(), new Date().toInstant()) + 1;
        } else {
            cal.set(Calendar.YEAR, Integer.valueOf(year));
            return cal.getActualMaximum(Calendar.DAY_OF_YEAR);
        }

    }
}