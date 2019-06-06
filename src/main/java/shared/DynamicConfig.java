package shared;

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
                    if(items.length < 2)
                        continue;
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

    public void setMysql_port(String mysql_port) {
        this.mysql_port = mysql_port;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    public void setCondor_license(String condor_license) {
        this.condor_license = condor_license;
    }

    public void setPlatformIsAndroid(boolean platformIsAndroid) {
        this.platformIsAndroid = platformIsAndroid;
    }

    public void setIos_backup_directory(String ios_backup_directory) {
        this.ios_backup_directory = ios_backup_directory;
    }
}
