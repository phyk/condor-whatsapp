package com.company;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.concurrent.Task;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import shared.DefaultConfig;
import shared.DynamicConfig;

public class ProcessHandler extends Task {
    private DynamicConfig dc;
    private DefaultConfig df;
    private static Logger log = LogManager.getLogger("condor-whatsapp-main");
    private AndroidWhatsdumpAdapter awa;
    private SimpleBooleanProperty requestCommand;
    private SimpleBooleanProperty isDone;

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
            CondorHandler.calculateHonestSignals(dc.getCondor_license(), "localhost", dc.getMysqlPort(),
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
        isDone = awa.isDoneProperty();
//        requestCommand.addListener((observable, oldValue, newValue) -> {
//            if(newValue)
//            {
//                this.updateMessage("Requesting User Input");
//            }
//        });
//        isDone.addListener((observable, oldValue, newValue) -> {
//            if(newValue)
//            {
//                this.updateMessage("Successfully extracted keyfile and db");
//            }
//        });
//        Thread sub = new Thread(awa);
//        sub.start();

        try {
//            while(!awa.isDoneProperty().getValue())
//            {
//                Thread.sleep(500);
//            }

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
                this.updateMessage("Starting calculation of honest signals");
                // After condor import generation, import the data to Condor and calculate the Honest Signals
                // Thereafter export the csv Files to the export folder
                CondorHandler.calculateHonestSignals(dc.getCondor_license(), "localhost", dc.getMysqlPort(),
                        dc.getUsername(), dc.getPassword(), dc.getDatabase(), df.getStandard_temp_links(), df.getStandard_temp_actors(),
                        df.getStandard_export_links(), df.getStandard_export_actors(), this);
                this.updateMessage("Honest Signals calculated");
            }
        } catch (Exception e) {
            log.error(e.getStackTrace());
        }
    }

    public void passMessage(String message)
    {
        this.updateMessage(message);
        if(message.equals("[INFO] Private key extracted in C:\\Users\\Philipp\\Documents\\StudDocs\\Master\\SS2019\\COINSeminar\\condor-whatsapp\\output\\15753363836\\key"))
        {
            awa.isDoneProperty().set(true);
        }
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
