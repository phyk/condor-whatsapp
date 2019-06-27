package com.company;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
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
import java.sql.SQLException;

public class ProcessHandler extends Task {
    private DynamicConfig dc;
    private DefaultConfig df;
    private static Logger log = LogManager.getLogger("condor-whatsapp-main");
    private AndroidWhatsdumpAdapter awa;
    private SimpleBooleanProperty requestCommand;

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
            wcs.createCSVExportIos(df.getStandard_temp_links(), df.getStandard_temp_actors(), dc.getPhoneNumber());
            wcs.close();
            this.updateMessage("Extraction finished");
            this.updateMessage("Calculating honest signals");
            // After condor import generation, import the data to Condor and calculate the Honest Signals
            // Thereafter export the csv Files to the export folder

            CondorHandler.calculateHonestSignals("localhost", dc.getMysqlPort(),
                    dc.getUsername(), dc.getPassword(), dc.getDatabase(), df.getStandard_temp_links(), df.getStandard_temp_actors(),
                    df.getStandard_export_links(), df.getStandard_export_actors(), this);
            this.updateMessage("Export files generated. You can now close this app");
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    private void handleAndroidWhatsapp(DynamicConfig dc, DefaultConfig df) {
        this.updateMessage("Handling Android Data");
        // Get key file and encrypted database to local data folder;
       // if(checkKeyFileExists("output/" + dc.getPhoneNumber().substring(3) + "/key")){
        if(checkKeyFileExists("data/msgstore.db")){
            this.updateMessage("Decrypted database already existing");

            // If decryption worked, generate condor temporary import
            WhatsappDBToCsv wcs = WhatsappDBToCsv.create("data/msgstore.db");
            try {
                wcs.createCSVExportAndroid(df.getStandard_temp_links(), df.getStandard_temp_actors(), dc.getPhoneNumber());
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                wcs.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }

            this.updateMessage("Extraction of link and actor csv succesfull");

                this.updateMessage("Starting calculation of honest signals");
                // After condor import generation, import the data to Condor and calculate the Honest Signals
                // Thereafter export the csv Files to the export folder
                CondorHandler.calculateHonestSignals("localhost", dc.getMysqlPort(),
                        dc.getUsername(), dc.getPassword(), dc.getDatabase(), df.getStandard_temp_links(), df.getStandard_temp_actors(),
                        df.getStandard_export_links(), df.getStandard_export_actors(), this);
                this.updateMessage("Export files generated. You can now close this app");

        }
        else {
            awa = new AndroidWhatsdumpAdapter(this, dc.getPhoneNumber());
            requestCommand = awa.requestInputProperty();
            requestCommand.addListener((observable, oldValue, newValue) -> {
                if (newValue) {
                    this.updateMessage("If you have not entered your activation code and already received it, please enter it now");
                }
            });
            Thread sub = new Thread(awa);
            sub.start();

            while (!checkKeyFileExists("output/" + dc.getPhoneNumber().substring(3) + "/key") || sub.isAlive()) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    log.error(e.getLocalizedMessage());
                }
            }

            continueWithWhatsappAndroid();
        }
    }
    private void continueWithWhatsappAndroid()
    {
        try {
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
                wcs.createCSVExportAndroid(df.getStandard_temp_links(), df.getStandard_temp_actors(), dc.getPhoneNumber());
                wcs.close();

                this.updateMessage("Extraction of link and actor csv succesfull");

                this.updateMessage("Starting calculation of honest signals");
                // After condor import generation, import the data to Condor and calculate the Honest Signals
                // Thereafter export the csv Files to the export folder
                CondorHandler.calculateHonestSignals("localhost", dc.getMysqlPort(),
                        dc.getUsername(), dc.getPassword(), dc.getDatabase(), df.getStandard_temp_links(), df.getStandard_temp_actors(),
                        df.getStandard_export_links(), df.getStandard_export_actors(), this);
                this.updateMessage("Export files generated. You can now close this app");
            }
        } catch (Exception e) {
            log.error(e.getLocalizedMessage());
        }
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

    @Override
    protected Object call() throws Exception {
        this.start();
        return null;
    }
}
