package com.company;

import tech.tablesaw.api.Row;
import tech.tablesaw.api.StringColumn;
import tech.tablesaw.api.Table;
import tech.tablesaw.io.csv.CsvWriteOptions;
import tech.tablesaw.io.csv.CsvWriter;
import java.io.IOException;
import java.sql.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.function.Consumer;
import java.util.function.Function;

public class WhatsappDBToCsv {

    private Connection conn;

    protected WhatsappDBToCsv(String databaseLocationPath)
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

    public static void main(String[] args) throws IOException, SQLException {
        WhatsappDBToCsv wcs = WhatsappDBToCsv.create(args[0]);
        wcs.createCSVExport();
        wcs.closeDbConnection();
        return;
    }

    public void createCSVExport() throws IOException {
            String sql = "SELECT main.messages.data AS content, " +
                    "main.messages.key_remote_jid as idRef, " +
                    "main.messages.key_from_me as myKey, " +
                    "cast(main.messages.timestamp as text) as timestamp, \n" +
                    "main.messages.remote_resource as idRem, " +
                    "main.group_participants.jid as idGroup \n" +
                    "FROM main.messages LEFT JOIN main.group_participants\n" +
                    "ON main.messages.key_remote_jid = main.group_participants.gjid\n" +
                    "WHERE (NOT (idRem = idGroup AND myKey = 0)) AND " +
                    "(NOT (mykey = 1 AND idRef LIKE '@g.us' AND idGroup = '')) AND NOT idRef LIKE 'status@broadcast'";
        Table tbl = null;
        try {
            tbl = createTableWithInfo(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        String enddate = getLatestDate();
        int length = tbl.column(0).size();
        String[] values = new String[length];
        Arrays.fill(values, enddate);
        StringColumn etc = StringColumn.create("Endtime", values);

        StringColumn stc = StringColumn.create("Starttime", length);
        tbl.column("timestamp").asStringColumn().mapInto(new Function<String, String>() {
            @Override
            public String apply(String s) {
                long time = Long.parseLong(s);
                return convertSecondsToDate(time);
            }
        }, stc);

        StringColumn content = StringColumn.create("content", length);
        tbl.column("content").asStringColumn().mapInto(new Function<String, String>() {
            @Override
            public String apply(String s) {
                return s.replace("\r\n","").replace(";","").replace("\r","");
            }
        }, content);

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
                idGroup = idGroup.substring(0,13);
            if(!idRem.equals(""))
                idRem = idRem.substring(0,13);
            // Group Logic
            if(idRef.endsWith("@g.us"))
            {
                if(fromMe)
                {
                    sIdc.append(idMyself);
                    tIdc.append(idGroup);
                }
                else
                {
                    sIdc.append(idRem);
                    tIdc.append(idGroup);
                }
            }
            // Chat Logic
            if(idRef.endsWith("@s.whatsapp.net"))
            {
                idRef = idRef.substring(0,13);
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
            }
        }

        Table copy = tbl.create(content, sIdc, tIdc, stc, etc);

        CsvWriteOptions.Builder cwo = CsvWriteOptions.builder("condor_import.csv");
        cwo.header(true);
        cwo.separator(';');

        CsvWriter cw = new CsvWriter();
        cw.write(copy, cwo.build());
        return;
    }


    public String getLatestDate()
    {
        String selectLatestTimestamp =
                "SELECT main.messages.timestamp FROM main.messages " +
                "ORDER BY main.messages.timestamp DESC LIMIT 1";
        String enddate = null;
        try(Statement stm = conn.createStatement()) {
            ResultSet rs = stm.executeQuery(selectLatestTimestamp);

            long endtime = rs.getLong("timestamp");
            rs.close();
            stm.close();
            enddate = convertSecondsToDate(endtime);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return enddate;
    }

    public String convertSecondsToDate(long time)
    {
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss");
        return format.format(new Date(time));
    }

    public Connection openDbConnection(String pathToDb) throws SQLException {
        String url = "jdbc:sqlite:"+pathToDb;
        Connection conn = DriverManager.getConnection(url);
        return conn;
    }

    public void closeDbConnection() throws SQLException {
        this.conn.close();
    }

    public Table createTableWithInfo(String sql) throws SQLException {
        Table table = null;
        Statement stmt = conn.createStatement();
        ResultSet results = stmt.executeQuery(sql);
        table = Table.read().db(results);

        return table;
    }
}
