package com.company;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import tech.tablesaw.api.Row;
import tech.tablesaw.api.StringColumn;
import tech.tablesaw.api.Table;
import tech.tablesaw.io.csv.CsvWriteOptions;
import tech.tablesaw.io.csv.CsvWriter;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;

public class WhatsappDBToCsv {

    private static Logger log = LogManager.getLogger("condor-whatsapp-main");

    private Connection conn;

    private WhatsappDBToCsv(String databaseLocationPath)
    {
        try {
            this.conn = openDbConnection(databaseLocationPath);
        } catch (SQLException e) {
            log.error(e.getLocalizedMessage());
        }
    }

    public static WhatsappDBToCsv create(String databaseLocationPath)
    {
        return new WhatsappDBToCsv(databaseLocationPath);
    }

    public static void main(String[] args) throws Exception {

        // Add config File with info instead of args

        String message = AndroidDbDecrypter.decrypt(args[1], args[0], "data/msgstore.db");

        if(message.equals("Decryption of crypt12 file was successful.")) {
            WhatsappDBToCsv wcs = WhatsappDBToCsv.create("data/msgstore.db");
            wcs.createCSVExportAndroid("data/links.csv","data/actors.csv", "");
            wcs.closeDbConnection();
            System.exit(0);
        }
    }

    public void createCSVExportIos(String pathToLinks, String pathToActors, String phoneNumber) throws IOException {
        String sqlIos = "SELECT\n" +
                "ZWAMESSAGE.Z_PK as id," +
                "ZWAMESSAGE.ZFROMJID as sender," +
                "ZWAMESSAGE.ZTOJID as receiver,\n" +
                "ZWAMESSAGE.ZTEXT as content,\n" +
                "ZWAMESSAGE.ZISFROMME as myKey,\n" +
                "datetime(main.ZWAMESSAGE.ZMESSAGEDATE + strftime('%s', '2001-01-01 00:00:00'),+\n" +
                "'unixepoch', 'localtime') as timestamp,\n" +
                "ZWAGROUPMEMBER.ZMEMBERJID as idGroup,\n" +
                "groups.ZMEMBERJID as idRem\n" +
                "FROM ZWAMESSAGE\n" +
                "LEFT JOIN ZWACHATSESSION\n" +
                "ON ZWAMESSAGE.ZCHATSESSION = ZWACHATSESSION.Z_PK\n" +
                "LEFT JOIN ZWAGROUPMEMBER\n" +
                "ON ZWACHATSESSION.Z_PK = ZWAGROUPMEMBER.ZCHATSESSION\n" +
                "LEFT JOIN ZWAGROUPMEMBER AS groups\n" +
                "ON ZWAMESSAGE.ZGROUPMEMBER = groups.Z_PK\n" +
                "WHERE NOT(ZWAMESSAGE.ZTEXT = 0)";

        Table tbl = null;
        try {
            tbl = createTableWithInfo(sqlIos);
        } catch (SQLException e) {
            log.error(e.getLocalizedMessage());
        }

        String enddate = null;
        String startdate = null;
        try {
            enddate = getLatestDateIos();
            startdate = getFirstDateIos();
        } catch (ParseException e) {
            log.error(e.getStackTrace());
        }

        int length = tbl.column(0).size();
        String[] values = new String[length];
        Arrays.fill(values, enddate);
        StringColumn etc = StringColumn.create("Endtime", values);

        StringColumn id = StringColumn.create("uuid", length);
        tbl.column("id").asStringColumn().mapInto(s -> UUID.randomUUID() + "", id);

        StringColumn stc = StringColumn.create("Starttime",length);
        tbl.column("timestamp").asStringColumn().mapInto(s -> {
            try {
                return convertStringToDate(s);
            } catch (ParseException e) {
                log.error(e.getLocalizedMessage());
            }
            return "";
        }, stc);

        StringColumn content = StringColumn.create("content", length);
        tbl.column("content").asStringColumn().mapInto(s -> s.replaceAll("(\\r)",".")
                .replaceAll("\\n"," ").replaceAll(";",",")
                .replaceAll("\r", ".").replaceAll("\n", " ").replaceAll("\"",""), content);


        MessageDigest sha = null;
        try {
            sha = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            log.error(e.getStackTrace());
        }

        HashSet<String> actors = new HashSet<>();
        StringColumn sIdc = StringColumn.create("SourceUuid");
        StringColumn tIdc = StringColumn.create("TargetUuid");
        for (Row row: tbl)
        {
            boolean fromMe = row.getInt("myKey") == 1;
            String sender = row.getString("sender");
            String receiver = row.getString("receiver");
            String idGroup = row.getString("idGroup");
            String idRem = row.getString("idRem");

            String idMyself = phoneNumber.replace("+", "")+"@s.whatsapp.net";

            if(!idGroup.equals(""))
                idGroup = toHexString(sha.digest(idGroup.split("@")[0].getBytes()));

            if(sender.equals(""))
                sender = idMyself;

            if(receiver.equals(""))
                receiver = idMyself;
            receiver = toHexString(sha.digest(receiver.split("@")[0].getBytes()));

            // Group Logic
            if(sender.endsWith("@g.us"))
            {
                if(idRem.equals(""))
                    idRem = idMyself;
                sender = toHexString(sha.digest(sender.split("@")[0].getBytes()));
                idRem = toHexString(sha.digest(idRem.split("@")[0].getBytes()));
                sIdc.append(idRem);
                tIdc.append(idGroup);
                actors.add(idRem);
                actors.add(idGroup);
            }
            // Chat Logic
            else if(sender.endsWith("@s.whatsapp.net") || sender.equals(idMyself))
            {
                sender = toHexString(sha.digest(sender.split("@")[0].getBytes()));
                sIdc.append(sender);
                tIdc.append(receiver);
                actors.add(sender);
                actors.add(receiver);
            }
            else if(sender.endsWith("@broadcast"))
            {
                sender = toHexString(sha.digest(sender.split("@")[0].getBytes()));
                sIdc.append(sender);
                tIdc.append(receiver);
                actors.add(sender);
                actors.add(receiver);
            }
        }

        Table copy = Table.create(id, content, sIdc, tIdc, tbl.column("timestamp"), etc);

        CsvWriteOptions.Builder cwo = CsvWriteOptions.builder(pathToLinks);
        cwo.header(true);
        cwo.separator(';');

        CsvWriter cw = new CsvWriter();
        cw.write(copy, cwo.build());


        StringColumn asc = StringColumn.create("id", new ArrayList<>(actors));
        StringColumn asc2 = StringColumn.create("uuid", new ArrayList<>(actors));
        StringColumn asc3 = StringColumn.create("name", new ArrayList<>(actors));

        length = asc.size();
        values = new String[length];
        Arrays.fill(values, startdate);
        StringColumn stc2 = StringColumn.create("starttime", values);
        values = new String[length];
        Arrays.fill(values, enddate);
        StringColumn etc2 = StringColumn.create("endtime", values);

        Table actorTable = Table.create(asc, asc2, asc3, stc2, etc2);
        cwo = CsvWriteOptions.builder(pathToActors);
        cwo.header(true);
        cwo.separator(';');
        cw.write(actorTable, cwo.build());
    }

    public void createCSVExportAndroid(String pathToLinks, String pathToActors, String phoneNumber) throws IOException {
        String sqlAndroid = "SELECT main.messages._id AS linkid,\n" +
                "main.messages.data AS content,\n" +
                "main.messages.key_remote_jid as idRef, \n" +
                "main.messages.key_from_me as myKey, \n" +
                "cast(main.messages.timestamp as text) as timestamp,\n" +
                "main.messages.remote_resource as idRem, \n" +
                "main.group_participants.jid as idGroup \n" +
                "FROM main.messages LEFT JOIN main.group_participants\n" +
                "ON main.messages.key_remote_jid = main.group_participants.gjid\n" +
                "WHERE NOT idRef LIKE 'status@broadcast' AND NOT idRef = '-1'";

        Table tbl = null;
        try {
            tbl = createTableWithInfo(sqlAndroid);
        } catch (SQLException e) {
            log.error(e.getStackTrace());
        }


        String enddate = getLatestDateAndroid();
        String startdate = getFirstDateAndroid();
        int length = tbl.column(0).size();
        String[] values = new String[length];
        Arrays.fill(values, enddate);
        StringColumn etc = StringColumn.create("Endtime", values);

        StringColumn stc = StringColumn.create("Starttime", length);
        tbl.column("timestamp").asStringColumn().mapInto(s -> {
            long time = Long.parseLong(s);
            return convertSecondsToDate(time);
        }, stc);

        StringColumn id = StringColumn.create("uuid", length);

        tbl.column("linkid").asStringColumn().mapInto(s -> (UUID.randomUUID() + "").replaceAll("-",""), id);

        StringColumn content = StringColumn.create("content", length);
        tbl.column("content").asStringColumn().mapInto(s -> s.replaceAll("(\\r)",".")
                .replaceAll("\\n"," ").replaceAll(";",",")
                .replaceAll("\r", ".").replaceAll("\n", " ").replaceAll("\"", ""), content);

        MessageDigest sha = null;
            try {
                sha = MessageDigest.getInstance("SHA-256");
            } catch (NoSuchAlgorithmException e) {
                log.error(e.getStackTrace());
        }

        HashSet<String> actors = new HashSet<>();
        StringColumn sIdc = StringColumn.create("SourceUuid");
        StringColumn tIdc = StringColumn.create("TargetUuid");
        for (Row row: tbl)
        {
            boolean fromMe = row.getInt("myKey") == 1;
            String idRef = row.getString("idRef");
            String idGroup = row.getString("idGroup");
            String idRem = row.getString("idRem");
            
            String idMyself = toHexString(sha.digest((phoneNumber.replace("+", "")).getBytes()));


            if(idGroup.equals(""))
                idGroup = idMyself;
            else
                idGroup = toHexString(sha.digest(idGroup.split("@")[0].getBytes()));
            if(!idRem.equals(""))
                idRem = toHexString(sha.digest(idRem.split("@")[0].getBytes()));
            else
                idRem = idMyself;
            // Group Logic
            if(idRef.endsWith("@g.us"))
            {
                if(fromMe)
                {
                    sIdc.append(idMyself);
                    tIdc.append(idGroup);
                    actors.add(idMyself);
                }
                else
                {
                    sIdc.append(idRem);
                    tIdc.append(idGroup);
                    actors.add(idRem);
                }
                actors.add(idGroup);
            }
            // Chat Logic
            if(idRef.endsWith("@s.whatsapp.net"))
            {
                idRef = toHexString(sha.digest(idRef.split("@")[0].getBytes()));
                if(fromMe)
                {
                    sIdc.append(idMyself);
                    tIdc.append(idRef);
                }
                else
                {
                    sIdc.append(idRef);
                    tIdc.append(idMyself);
                }
                actors.add(idRef);
                actors.add(idMyself);
            }
            if(sIdc.isEmpty()){

                System.out.println("Current row "+row.toString());

                System.out.println("boolean from me: "+(row.getInt("myKey") == 1));
                System.out.println("idRef befor hash: "+row.getString("idRef"));
                System.out.println("idGroup befor hash: "+row.getString("idGroup"));
                System.out.println("idRem befor hash: "+row.getString("idRem"));

                System.out.println("sIdc: "+sIdc);
                System.out.println("tIdc: "+tIdc);
                System.out.println("idMyself: "+idMyself);
                System.out.println("idRef: "+idRef);
                System.out.println("idGroup: "+idGroup);
                System.out.println("idRem: "+idRem);
                System.out.println("");
            }
        }

        Table copy = Table.create(id, content, sIdc, tIdc, stc, etc);

        CsvWriteOptions.Builder cwo = CsvWriteOptions.builder(pathToLinks);
        cwo.header(true);
        cwo.separator(';');

        CsvWriter cw = new CsvWriter();
        cw.write(copy, cwo.build());


        StringColumn asc = StringColumn.create("id", new ArrayList<>(actors));
        StringColumn asc2 = StringColumn.create("uuid", new ArrayList<>(actors));
        StringColumn asc3 = StringColumn.create("name", new ArrayList<>(actors));

        length = asc.size();
        values = new String[length];
        Arrays.fill(values, startdate);
        StringColumn stc2 = StringColumn.create("starttime", values);
        values = new String[length];
        Arrays.fill(values, enddate);
        StringColumn etc2 = StringColumn.create("endtime", values);

        Table actorTable = Table.create(asc, asc2, asc3, stc2, etc2);
        cwo = CsvWriteOptions.builder(pathToActors);
        cwo.header(true);
        cwo.separator(';');
        cw.write(actorTable, cwo.build());
    }

    private static String toHexString(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();

        for (byte aByte : bytes) {
            String hex = Integer.toHexString(0xFF & aByte);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }

        return hexString.toString();
    }

    private String getLatestDateAndroid()
    {
        String selectLatestTimestamp =
                "SELECT main.messages.timestamp FROM main.messages " +
                "ORDER BY main.messages.timestamp DESC LIMIT 1";
        String columnLabel = "timestamp";

        String enddate = executeSqlAndGetResult(selectLatestTimestamp, columnLabel);
        if(enddate == null)
        {
            enddate = convertSecondsToDate(System.currentTimeMillis());
        }

        return enddate;
    }

    private String getLatestDateIos() throws ParseException {
        String selectLatestTimestamp =
                "SELECT datetime(main.ZWAMESSAGE.ZMESSAGEDATE + strftime('%s', '2001-01-01 00:00:00'),\n" +
                        "       'unixepoch', 'localtime') as timestamp FROM main.ZWAMESSAGE " +
                        "ORDER BY main.ZWAMESSAGE.ZMESSAGEDATE DESC LIMIT 1";
        String columnLabel = "timestamp";

        String enddate = executeSqlAndGetResultIos(selectLatestTimestamp, columnLabel);
        if(enddate == null)
        {
            enddate = convertSecondsToDate(System.currentTimeMillis());
        }

        return convertStringToDate(enddate);
    }

    private String executeSqlAndGetResultIos(String sql, String columnLabel)
    {
        String enddate = null;
        try(Statement stm = conn.createStatement()) {
            ResultSet rs = stm.executeQuery(sql);

            enddate = rs.getString(columnLabel);
            rs.close();
            stm.close();
        } catch (SQLException e) {
            log.error(e.getStackTrace());
        }
        return enddate;
    }

    private String executeSqlAndGetResult(String sql, String columnLabel)
    {
        String enddate;
        try(Statement stm = conn.createStatement()) {
            ResultSet rs = stm.executeQuery(sql);

            long endtime = rs.getLong(columnLabel);
            rs.close();
            stm.close();
            enddate = convertSecondsToDate(endtime);
            return enddate;
        } catch (SQLException e) {
            log.error(e.getStackTrace());
        }
        return null;
    }

    private String getFirstDateAndroid() {
        String selectLatestTimestamp =
                "SELECT main.messages.timestamp FROM main.messages " +
                        "ORDER BY main.messages.timestamp ASC LIMIT 1";
        String columnLabel = "timestamp";

        String enddate = executeSqlAndGetResult(selectLatestTimestamp, columnLabel);
        if(enddate == null)
        {
            enddate = convertSecondsToDate(0);
        }

        return enddate;
    }

    private String getFirstDateIos() throws ParseException {
        String selectLatestTimestamp =
                "SELECT datetime(main.ZWAMESSAGE.ZMESSAGEDATE + strftime('%s', '2001-01-01 00:00:00'),\n" +
                        "       'unixepoch', 'localtime') as timestamp FROM main.ZWAMESSAGE " +
                        "ORDER BY main.ZWAMESSAGE.ZMESSAGEDATE ASC LIMIT 1";
        String columnLabel = "timestamp";

        String enddate = executeSqlAndGetResultIos(selectLatestTimestamp, columnLabel);
        if(enddate == null)
        {
            enddate = convertSecondsToDate(0);
        }

        return convertStringToDate(enddate);
    }

    private String convertSecondsToDate(long time)
    {
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
        return format.format(new Date(time));
    }

    private String convertStringToDate(String time) throws ParseException {
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
        DateFormat input = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return format.format(input.parse(time));
    }

    private Connection openDbConnection(String pathToDb) throws SQLException {
        String url = "jdbc:sqlite:"+pathToDb;
        return DriverManager.getConnection(url);
    }

    private void closeDbConnection() throws SQLException {
        this.conn.close();
    }

    private Table createTableWithInfo(String sql) throws SQLException {
        Statement stmt = conn.createStatement();
        ResultSet results = stmt.executeQuery(sql);
        return Table.read().db(results);
    }

    public void close() throws SQLException {
        this.closeDbConnection();
    }
}
