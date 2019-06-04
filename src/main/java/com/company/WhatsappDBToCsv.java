package com.company;

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
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;

public class WhatsappDBToCsv {

    private Connection conn;

    private WhatsappDBToCsv(String databaseLocationPath)
    {
        try {
            this.conn = openDbConnection(databaseLocationPath);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static WhatsappDBToCsv create(String databaseLocationPath)
    {
        return new WhatsappDBToCsv(databaseLocationPath);
    }

    public static void main(String[] args) throws Exception {

        // Add config File with info instead of args

        String message = decrypt12.decrypt(args[1], args[0], "data/msgstore.db");

        if(message.equals("Decryption of crypt12 file was successful.")) {
            WhatsappDBToCsv wcs = WhatsappDBToCsv.create("data/msgstore.db");
            wcs.createCSVExport();
            wcs.closeDbConnection();
            System.exit(0);
        }
    }

    public void createCSVExport() throws IOException {
        String sql = "SELECT main.messages._id AS linkid," +
                "main.messages.data AS content, " +
                "main.messages.key_remote_jid as idRef, " +
                "main.messages.key_from_me as myKey, " +
                "cast(main.messages.timestamp as text) as timestamp, \n" +
                "main.messages.remote_resource as idRem, " +
                "main.group_participants.jid as idGroup \n" +
                "FROM main.messages LEFT JOIN main.group_participants\n" +
                "ON main.messages.key_remote_jid = main.group_participants.gjid\n" +
                "WHERE (NOT (idRem = idGroup AND myKey = 0)) AND " +
                "(NOT (mykey = 1 AND idRef LIKE '@g.us' AND idGroup = '')) AND NOT idRef LIKE 'status@broadcast'";

        /*
         * SQL Fuer iOS
         * KeyFromMe = ! ZMessageFromMe
         * content =
         * group_participants = ZMemberJid
         * timestamp = ZMessageDate
         *
         */
        Table tbl = null;
        try {
            tbl = createTableWithInfo(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        String enddate = getLatestDate();
        String startdate = getFirstDate();
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
        Random r = new Random();
        tbl.column("linkid").asStringColumn().mapInto(s -> r.nextInt(Integer.MAX_VALUE) + "", id);

        StringColumn content = StringColumn.create("content", length);
        tbl.column("content").asStringColumn().mapInto(s -> s.replace("\r\n",". ").replace(";","").replace("\r",""), content);

        MessageDigest sha = null;
            try {
                sha = MessageDigest.getInstance("SHA-256");
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
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
            String idMyself = "0";

            if(idGroup.equals(""))
                idGroup = idMyself;
            else
                idGroup = toHexString(sha.digest(idGroup.substring(0,13).getBytes()));
            if(!idRem.equals(""))
                idRem = toHexString(sha.digest(idRem.substring(0,13).getBytes()));
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
                idRef = toHexString(sha.digest(idRef.substring(0,13).getBytes()));
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
        }

        Table copy = Table.create(id, content, sIdc, tIdc, stc, etc);

        CsvWriteOptions.Builder cwo = CsvWriteOptions.builder("data/links_import.csv");
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
        cwo = CsvWriteOptions.builder("data/actors_import.csv");
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

    private String getLatestDate()
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
            e.printStackTrace();
        }
        return null;
    }

    private String getFirstDate() {
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

    private String convertSecondsToDate(long time)
    {
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
        return format.format(new Date(time));
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
}
