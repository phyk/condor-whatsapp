package shared;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

public class DynamicConfig {
    private StringProperty path;
    private StringProperty mysql_port;
    private StringProperty username;
    private StringProperty password;
    private StringProperty database;
    private StringProperty condor_license;
    private BooleanProperty platformIsAndroid;
    private StringProperty ios_backup_directory;
    private StringProperty phone_number;

    private DynamicConfig(String path, boolean empty)
    {
        this.path = new SimpleStringProperty();
        this.mysql_port = new SimpleStringProperty();
        this.username = new SimpleStringProperty();
        this.password = new SimpleStringProperty();
        this.database = new SimpleStringProperty();
        this.condor_license = new SimpleStringProperty();
        this.ios_backup_directory = new SimpleStringProperty();
        this.phone_number = new SimpleStringProperty();
        this.platformIsAndroid = new SimpleBooleanProperty();

        this.path.setValue(path);
        if(!empty)
            readConfigFromFile();
    }

    private void readConfigFromFile()
    {
        try {
            ArrayList<String> configFile = new ArrayList<String>(Files.readAllLines(Paths.get(path.getValue())));
            for (String line :
                    configFile) {
                if((!line.startsWith("#")) && (!line.equals("")))
                {
                    String[] items = line.split("=");
                    if(items.length < 2)
                        continue;
                    switch(items[0]){
                        case "mysql_port":
                            mysql_port.setValue(items[1]);break;
                        case "username":
                            username.setValue(items[1]);break;
                        case "password":
                            password.setValue(items[1]);break;
                        case "database":
                            database.setValue(items[1]);break;
                        case "condor_license":
                            condor_license.setValue(items[1]);break;
                        case "platform":
                            platformIsAndroid.setValue(items[1].equals("android"));break;
                        case "ios_backup_directory":
                            ios_backup_directory.setValue(items[1]);break;
                        case "phone_number":
                            phone_number.setValue(items[1]);break;
                        default:
                            break;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeConfigToFile()
    {
        try(FileWriter fw = new FileWriter(path.getValue(), false))
        {
            fw.write("# Dynamic config information to be filled out via a GUI\r\n" +
                    "#\r\n");
            fw.write("mysql_port="+this.mysql_port.getValue()+"\r\n");
            fw.write("username="+this.username.getValue()+"\r\n");
            fw.write("password="+this.password.getValue()+"\r\n");
            fw.write("database="+this.database.getValue()+"\r\n");
            fw.write("condor_license="+this.condor_license.getValue()+"\r\n");
            fw.write("# either 'android' or 'ios'\r\n");
            fw.write("platform="+(this.platformIsAndroid.getValue()?"android":"ios")+"\r\n");
            fw.write("ios_backup_directory="+this.ios_backup_directory.getValue()+"\r\n");
            fw.write("# format +49<number without leading 0>\r\n");
            fw.write("phone_number="+this.phone_number.getValue()+"\r\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static DynamicConfig create(String path)
    {
        return new DynamicConfig(path, false);
    }

    public static DynamicConfig createEmpty(String path)
    {
        return new DynamicConfig(path, true);
    }
    /**
     * Uses the default path 'config/config.txt'
     * @return
     */
    public static DynamicConfig create()
    {
        return create("config/dynamic_config.txt");
    }

    public String getPath() {
        return path.get();
    }

    public StringProperty pathProperty() {
        return path;
    }

    public StringProperty mysql_portProperty() {
        return mysql_port;
    }

    public StringProperty usernameProperty() {
        return username;
    }

    public StringProperty passwordProperty() {
        return password;
    }

    public StringProperty databaseProperty() {
        return database;
    }

    public StringProperty condor_licenseProperty() {
        return condor_license;
    }

    public BooleanProperty platformIsAndroidProperty() {
        return platformIsAndroid;
    }

    public StringProperty ios_backup_directoryProperty() {
        return ios_backup_directory;
    }

    public StringProperty phone_numberProperty() {
        return phone_number;
    }

    public String getMysql_port() {
        return mysql_port.getValue();
    }

    public String getUsername() {
        return username.getValue();
    }

    public String getPassword() {
        return password.getValue();
    }

    public String getDatabase() {
        return database.getValue();
    }

    public String getCondor_license() {
        return condor_license.getValue();
    }

    public boolean isPlatformIsAndroid() {
        return platformIsAndroid.getValue();
    }

    public String getIos_backup_directory() {
        return ios_backup_directory.getValue();
    }

    public String getPhone_number() {
        return phone_number.getValue();
    }

    public void setPhone_number(String phone_number) {
        this.phone_number.setValue(phone_number);
    }

    public void setMysql_port(String mysql_port) {
        this.mysql_port.setValue(mysql_port);
    }

    public void setUsername(String username) {
        this.username.setValue(username);
    }

    public void setPassword(String password) {
        this.password.setValue(password);
    }

    public void setDatabase(String database) {
        this.database.setValue(database);
    }

    public void setCondor_license(String condor_license) {
        this.condor_license.setValue(condor_license);
    }

    public void setPlatformIsAndroid(boolean platformIsAndroid) {
        this.platformIsAndroid.setValue(platformIsAndroid);
    }

    public void setIos_backup_directory(String ios_backup_directory) {
        this.ios_backup_directory.setValue(ios_backup_directory);
    }

    public void close() {
        this.writeConfigToFile();
    }
}
