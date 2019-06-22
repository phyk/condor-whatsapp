package shared;

import condor.storage.db.mysql.MySql;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;

public class MySqlConnector
{
    private String database;
    private String port;
    private String host;
    private String password;
    private String username;
    private static Logger log = LogManager.getLogger("MySqlConnector");

    public static MySqlConnector create(String host, String port, String database)
    {
        return new MySqlConnector(host, port, database, null, null);
    }

    public static MySqlConnector create(String host, String port, String database, String username, String password)
    {
        return new MySqlConnector(host, port, database, username, password);
    }

    private MySqlConnector(String host, String port, String database, String username, String password)
    {
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
        this.database = database;
    }

    public boolean checkCredentials(String username, String password)
    {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            Connection con = DriverManager.getConnection("jdbc:mysql://"+host+":"+port+"/"+database, username, password);
            this.username = username;
            this.password = password;
            return true;
        } catch (ClassNotFoundException e) {
            log.error(e.getLocalizedMessage());
        } catch (SQLException e) {
            log.error(e.getLocalizedMessage());
        }
        return false;
    }

    public void setUsernameAndPassword(String username, String password)
    {
        this.username = username;
        this.password = password;
    }

    public ResultSet executeQuery(String sql) throws ClassNotFoundException, SQLException {
        Class.forName("com.mysql.jdbc.Driver");
        Connection con = DriverManager.getConnection("jdbc:mysql://"+host+":"+port+"/"+database, username, password);
        Statement stm = con.createStatement();
        return stm.executeQuery(sql);
    }
}
