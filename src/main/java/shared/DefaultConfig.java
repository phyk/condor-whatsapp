package shared;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

public class DefaultConfig
{
    private String standard_db_location;
    private String standard_key_location;
    private String standard_encdb_location;
    private String standard_export_actors;
    private String standard_export_links;
    private String standard_temp_links = "data/links.csv";
    private String standard_temp_actors = "data/actors.csv";

    private DefaultConfig(String path)
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
                        case "standard_db_location":
                            standard_db_location = items[1];break;
                        case "standard_key_location":
                            standard_key_location = items[1];break;
                        case "standard_encdb_location":
                            standard_encdb_location = items[1];break;
                        case "standard_export_actors":
                            standard_export_actors = items[1];break;
                        case "standard_export_links":
                            standard_export_links = items[1];break;
                        default:
                            break;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static DefaultConfig create(String path)
    {
        return new DefaultConfig(path);
    }

    /**
     * Uses the default path 'config/config.txt'
     * @return
     */
    public static DefaultConfig create()
    {
        return create("config/config.txt");
    }

    public String getStandard_db_location() {
        return standard_db_location;
    }

    public String getStandard_key_location() {
        return standard_key_location;
    }

    public String getStandard_encdb_location() {
        return standard_encdb_location;
    }

    public String getStandard_export_actors() {
        return standard_export_actors;
    }

    public String getStandard_export_links() {
        return standard_export_links;
    }

    public String getStandard_temp_links() {
        return standard_temp_links;
    }

    public String getStandard_temp_actors() {
        return standard_temp_actors;
    }
}
