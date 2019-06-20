package com.company;

import javafx.concurrent.Task;
import org.apache.commons.logging.impl.Log4JLogger;
import shared.DefaultConfig;
import shared.DynamicConfig;

public class ProcessHandler extends Task {
    private DynamicConfig dc;
    private DefaultConfig df;

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
            DbExtractor.extractDbToDirectory(dc.getIos_backup_directory(), "data");

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
            CondorHandler.calculateHonestSignals(dc.getCondor_license(), "localhost", dc.getMysql_port(),
                    dc.getUsername(), dc.getPassword(), dc.getDatabase(), df.getStandard_temp_links(), df.getStandard_temp_actors(),
                    df.getStandard_export_links(), df.getStandard_export_actors(), this);
            this.updateMessage("Calculated honest signals");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleAndroidWhatsapp(DynamicConfig dc, DefaultConfig df)
    {
        this.updateMessage("Handling Android Data");
        // Get key file and encrypted database to local data folder
        try {
            this.updateMessage("Moving the key file and the database to the intermediate directory");
            // Copy the file to the data directory
            DbExtractor.extractEncryptedDbAndKeyFile(
                    "output/"+dc.getPhone_number().substring(3)+"/msgstore.db.crypt12",
                    "output/"+dc.getPhone_number().substring(3)+"/key",
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
                CondorHandler.calculateHonestSignals(dc.getCondor_license(), "localhost", dc.getMysql_port(),
                        dc.getUsername(), dc.getPassword(), dc.getDatabase(), df.getStandard_temp_links(), df.getStandard_temp_actors(),
                        df.getStandard_export_links(), df.getStandard_export_actors(), this);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void passMessage(String message)
    {
        this.updateMessage(message);
    }

    @Override
    protected Object call() throws Exception {
        this.start();
        return null;
    }
}
