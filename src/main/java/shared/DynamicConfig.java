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
    private StringProperty mysqlPort;
    private StringProperty username;
    private StringProperty password;
    private StringProperty database;
    private StringProperty condorLicense;
    private BooleanProperty platformIsAndroid;
    private StringProperty iosBackupDirectory;
    private StringProperty phoneNumber;
    private StringProperty mySqlHost;
    private BooleanProperty isMacOs;

    private DynamicConfig(String path, boolean empty)
    {
        this.path = new SimpleStringProperty("");
        this.mysqlPort = new SimpleStringProperty("");
        this.username = new SimpleStringProperty("");
        this.password = new SimpleStringProperty("");
        this.database = new SimpleStringProperty("");
        this.condorLicense = new SimpleStringProperty("");
        this.iosBackupDirectory = new SimpleStringProperty("");
        this.phoneNumber = new SimpleStringProperty("");
        this.platformIsAndroid = new SimpleBooleanProperty();
        this.mySqlHost = new SimpleStringProperty("");
        this.isMacOs = new SimpleBooleanProperty();

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
                        case "mysqlPort":
                            mysqlPort.setValue(items[1]);break;
                        case "username":
                            username.setValue(items[1]);break;
                        case "password":
                            password.setValue(items[1]);break;
                        case "database":
                            database.setValue(items[1]);break;
                        case "condorLicense":
                            condorLicense.setValue(items[1]);break;
                        case "platform":
                            platformIsAndroid.setValue(items[1].equals("android"));break;
                        case "iosBackupDirectory":
                            iosBackupDirectory.setValue(items[1]);break;
                        case "phoneNumber":
                            phoneNumber.setValue(items[1]);break;
                        case "mysqlHost":
                            mySqlHost.setValue(items[1]);break;
                        case "operatingSystem":
                            isMacOs.setValue(items[1].equals("Mac"));break;
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
            fw.write("mysqlPort="+this.mysqlPort.getValue()+"\r\n");
            fw.write("mysqlHost="+this.mySqlHost.getValue()+"\r\n");
            fw.write("username="+this.username.getValue()+"\r\n");
            fw.write("password="+this.password.getValue()+"\r\n");
            fw.write("database="+this.database.getValue()+"\r\n");
            fw.write("condorLicense="+this.condorLicense.getValue()+"\r\n");
            fw.write("# either 'android' or 'ios'\r\n");
            fw.write("platform="+(this.platformIsAndroid.getValue()?"android":"ios")+"\r\n");
            fw.write("iosBackupDirectory="+this.iosBackupDirectory.getValue()+"\r\n");
            fw.write("# format +49<number without leading 0>\r\n");
            fw.write("phoneNumber="+this.phoneNumber.getValue()+"\r\n");
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

    public StringProperty mysqlPortProperty() {
        return mysqlPort;
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
        return condorLicense;
    }

    public BooleanProperty platformIsAndroidProperty() {
        return platformIsAndroid;
    }

    public StringProperty iosBackupDirectoryProperty() {
        return iosBackupDirectory;
    }

    public StringProperty phoneNumberProperty() {
        return phoneNumber;
    }

    public String getMysqlHost() { return mySqlHost.getValue(); }

    public void setMysqlHost(String host) {
        mySqlHost.setValue(host);
    }

    public String getMysqlPort() {
        return mysqlPort.getValue();
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
        return condorLicense.getValue();
    }

    public boolean isPlatformIsAndroid() {
        return platformIsAndroid.getValue();
    }

    public String getIosBackupDirectory() {
        return iosBackupDirectory.getValue();
    }

    public String getPhoneNumber() {
        return phoneNumber.getValue();
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber.setValue(phoneNumber);
    }

    public void setMysqlPort(String mysqlPort) {
        this.mysqlPort.setValue(mysqlPort);
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
        this.condorLicense.setValue(condor_license);
    }

    public void setPlatformIsAndroid(boolean platformIsAndroid) {
        this.platformIsAndroid.setValue(platformIsAndroid);
    }

    public void setIosBackupDirectory(String iosBackupDirectory) {
        this.iosBackupDirectory.setValue(iosBackupDirectory);
    }

    public boolean isMacOs()
    {
        return isMacOs.getValue();
    }

    public void close() {
        this.writeConfigToFile();
    }
}
