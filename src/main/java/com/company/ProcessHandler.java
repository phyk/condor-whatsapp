package com.company;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.concurrent.Task;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import shared.DefaultConfig;
import shared.DynamicConfig;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ProcessHandler extends Task {
    private DynamicConfig dc;
    private DefaultConfig df;
    private static Logger log = LogManager.getLogger("condor-whatsapp-main");
    private AndroidWhatsdumpAdapter awa;
    private SimpleBooleanProperty requestCommand;
    private long max;

    public ProcessHandler(DynamicConfig dc, DefaultConfig df)
    {
        this.dc = dc;
        this.df = df;
    }
    public void start()
    {
        this.updateMessage("Process started");
        if(dc.isPlatformIsAndroid())
        {
            handleAndroidWhatsapp(dc, df);
        }
        else
        {
            handleIOsWhatsapp(dc, df);
        }
    }

    private void handleIOsWhatsapp(DynamicConfig dc, DefaultConfig df)
    {
        this.updateMessage("Handling IOs Data");
        try {
            // Copy Whatsapp Database from unencrypted iPhone Backup to local data folder
            DbExtractor.extractDbToDirectory(dc.getIosBackupDirectory(), "data");

            this.updateMessage("Extracted db from directory");
            this.updateMessage("Starting extraction of message data");
            // Use local sqlite Database to generate condor-readable import
            WhatsappDBToCsv wcs = WhatsappDBToCsv.create(df.getStandard_db_location());
            wcs.createCSVExportIos(df.getStandard_temp_links(), df.getStandard_temp_actors());
            wcs.close();
            this.updateMessage("Extraction finished");
            this.updateMessage("Calculating honest signals");
            // After condor import generation, import the data to Condor and calculate the Honest Signals
            // Thereafter export the csv Files to the export folder
            getLineCount(df.getStandard_temp_links());

            CondorHandler.calculateHonestSignals("localhost", dc.getMysqlPort(),
                    dc.getUsername(), dc.getPassword(), dc.getDatabase(), df.getStandard_temp_links(), df.getStandard_temp_actors(),
                    df.getStandard_export_links(), df.getStandard_export_actors(), this);
            this.updateMessage("Calculated honest signals");
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    private void handleAndroidWhatsapp(DynamicConfig dc, DefaultConfig df)
    {
        this.updateMessage("Handling Android Data");
        // Get key file and encrypted database to local data folder
        awa = new AndroidWhatsdumpAdapter(this, dc.getPhoneNumber());
        requestCommand = awa.requestInputProperty();
        requestCommand.addListener((observable, oldValue, newValue) -> {
            if(newValue)
            {
                this.updateMessage("Requesting User Input");
            }
        });
        Thread sub = new Thread(awa);
        sub.start();

        try {
            while(!checkKeyFileExists("output"+dc.getPhoneNumber().substring(3)+"/key"))
            {
                Thread.sleep(500);
            }
            sub.interrupt();

            this.updateMessage("Moving the key file and the database to the intermediate directory");
            // Copy the file to the data directory
            DbExtractor.extractEncryptedDbAndKeyFile(
                    "output/"+dc.getPhoneNumber().substring(3)+"/msgstore.db.crypt12",
                    "output/"+dc.getPhoneNumber().substring(3)+"/key",
                    df.getStandard_encdb_location(), df.getStandard_key_location());
            this.updateMessage("Moved the key file and the database");
            this.updateMessage("Decrypting the database");
            // Decrypt the database
            String message = AndroidDbDecrypter.decrypt(df.getStandard_key_location(), df.getStandard_encdb_location(),
                    df.getStandard_db_location());
            this.updateMessage(message);

            // If decryption worked, generate condor temporary import
            if(message.equals("Decryption of crypt12 file was successful.")) {
                WhatsappDBToCsv wcs = WhatsappDBToCsv.create(df.getStandard_db_location());
                wcs.createCSVExportAndroid(df.getStandard_temp_links(), df.getStandard_temp_actors());
                wcs.close();

                this.updateMessage("Extraction of link and actor csv succesfull");

                getLineCount(df.getStandard_temp_links());

                this.updateMessage("Starting calculation of honest signals");
                // After condor import generation, import the data to Condor and calculate the Honest Signals
                // Thereafter export the csv Files to the export folder
                CondorHandler.calculateHonestSignals("localhost", dc.getMysqlPort(),
                        dc.getUsername(), dc.getPassword(), dc.getDatabase(), df.getStandard_temp_links(), df.getStandard_temp_actors(),
                        df.getStandard_export_links(), df.getStandard_export_actors(), this);
                this.updateMessage("Honest Signals calculated");
            }
        } catch (Exception e) {
            log.error(e.getStackTrace());
        }
    }

    private void getLineCount(String standard_temp_links) {
        long result = 0;
        try (
                FileReader input = new FileReader(standard_temp_links);
                LineNumberReader count = new LineNumberReader(input);
                )
        {
            while (count.skip(Long.MAX_VALUE) > 0)
            {
                // Loop just in case the file is > Long.MAX_VALUE or skip() decides to not read the entire file
            }

            result = count.getLineNumber() + 1;                                    // +1 because line index starts at 0
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        max = result;
    }

    private boolean checkKeyFileExists(String location) {
        Path check = Paths.get(location);
        return check.toFile().exists();
    }

    public void passMessage(String message)
    {
        this.updateMessage(message);
    }

    public void passCommand(String command)
    {
        awa.runCommand(command);
    }

    public void updateProgress(long progress)
    {
        this.updateProgress(progress, max);
    }

    @Override
    protected Object call() throws Exception {
        this.start();
        return null;
    }
}
