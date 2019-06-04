package com.company;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

public class DynamicConfig {
    private String mysql_port;
    private String username;
    private String password;
    private String database;
    private String condor_license;
    private boolean platformIsAndroid;
    private String ios_backup_directory;

    private DynamicConfig(String path)
    {
        readConfigFromFile(path);
    }

    private void readConfigFromFile(String path)
    {
        try {
            ArrayList<String> configFile = new ArrayList<String>(Files.readAllLines(Paths.get(path)));
            for (String line :
                    configFile) {
                if((!line.startsWith("#")) && (!line.equals("")))
                {
                    String[] items = line.split("=");
                    switch(items[0]){
                        case "mysql_port":
                            mysql_port = items[1];break;
                        case "username":
                            username = items[1];break;
                        case "password":
                            password = items[1];break;
                        case "database":
                            database = items[1];break;
                        case "condor_license":
                            condor_license = items[1];break;
                        case "platform":
                            platformIsAndroid = items[1].equals("android");break;
                        case "ios_backup_directory":
                            ios_backup_directory = items[1];break;
                        default:
                            break;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static DynamicConfig create(String path)
    {
        return new DynamicConfig(path);
    }

    /**
     * Uses the default path 'config/config.txt'
     * @return
     */
    public static DynamicConfig create()
    {
        return create("config/dynamic_config.txt");
    }

    public String getMysql_port() {
        return mysql_port;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getDatabase() {
        return database;
    }

    public String getCondor_license() {
        return condor_license;
    }

    public boolean isPlatformIsAndroid() {
        return platformIsAndroid;
    }

    public String getIos_backup_directory() {
        return ios_backup_directory;
    }
}
