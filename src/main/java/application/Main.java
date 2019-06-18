package application;
	
import java.io.File;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.company.ProcessHandler;
import javafx.application.Application;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.concurrent.Worker;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.InputEvent;
import javafx.scene.input.MouseEvent;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import shared.DefaultConfig;
import shared.DynamicConfig;


public class Main extends Application {
	
    private TextField mysql_port = new TextField("3306");
    private TextField username = new TextField("root");
    private TextField password = new PasswordField();
    private TextField database = new TextField("condor_temp");
    private TextField condor_license = new TextField("");
    private TextField phone_number = new TextField("");
    private RadioButton platformIsAndroid = new RadioButton("Android");
    private TextField ios_backup_directory = new TextField("");

    private ProcessHandler dbProcess;
	
	
	@Override
	public void start(Stage primaryStage) {
		try {
			BorderPane root = new BorderPane();
			
			VBox portBox = new VBox();
			Label labelPort = new Label("MySQL Port:");
			labelPort.setTextFill(Color.WHITE);
			portBox.getChildren().add(labelPort);
			portBox.getChildren().add(mysql_port);

			
			VBox usernameBox = new VBox();
			Label labelUser = new Label("Username:");
			labelUser.setTextFill(Color.WHITE);
			usernameBox.getChildren().add(labelUser);
			usernameBox.getChildren().add(username);
			
			VBox passwordBox = new VBox();
			Label labelPassword = new Label("Password:");
			labelPassword.setTextFill(Color.WHITE);
			passwordBox.getChildren().add(labelPassword);
			passwordBox.getChildren().add(password);
			
			VBox databaseBox = new VBox();
			Label labelDatabase = new Label("Database:");
			labelDatabase.setTextFill(Color.WHITE);
			databaseBox.getChildren().add(labelDatabase);
			databaseBox.getChildren().add(database);

            VBox phoneNumberBox = new VBox();
            Label labelPhoneNumber = new Label("Phone Number:");
            labelPhoneNumber.setTextFill(Color.WHITE);
            phoneNumberBox.getChildren().add(labelPhoneNumber);
            phoneNumberBox.getChildren().add(phone_number);

			VBox condorLicenseBox = new VBox();
			Label labelLicence = new Label("Condor License:");
			labelLicence.setTextFill(Color.WHITE);
			condorLicenseBox.getChildren().add(labelLicence);
			condorLicenseBox.getChildren().add(condor_license);
			
			VBox directoryBox = new VBox();
			TextField dir = new TextField();
			
			
			FileChooser fileChooser = new FileChooser();
	        Button openButton = new Button("Navigate");
	        openButton.setPrefWidth(100);
			
	        openButton.setOnAction(
	                new EventHandler<ActionEvent>() {
	                    @Override
	                    public void handle(final ActionEvent e) {
	                        File file = fileChooser.showOpenDialog(primaryStage);
	                        if (file != null) {
	                            // put dir to textfield
	                        	dir.setText(file.getAbsolutePath());
	                        }
	                    }
	                });
	        
	        
	        Label labelDir = new Label("IOS backup directory:");
	        labelDir.setTextFill(Color.WHITE);
	        directoryBox.getChildren().add(labelDir);
	        
	        HBox dirBox = new HBox();
	        dirBox.getChildren().add(openButton);
	        dirBox.setSpacing(5);
	        dir.prefWidthProperty().bind(dirBox.widthProperty().subtract(openButton.getPrefWidth()));
	        dirBox.getChildren().add(dir);
	        
	        directoryBox.getChildren().add(dirBox);
	        
	        directoryBox.setDisable(true);
	        
	        final ToggleGroup group = new ToggleGroup();

	        platformIsAndroid.setToggleGroup(group);
	        platformIsAndroid.setTextFill(Color.WHITE);
	        platformIsAndroid.setSelected(true);

	        RadioButton rbIOS = new RadioButton("IOS (Iphone)");
	        rbIOS.setTextFill(Color.WHITE);
	        rbIOS.setToggleGroup(group);
	         
	        rbIOS.selectedProperty().addListener((arg, oldVal, newVal) -> {
	        	if(newVal == true) {
	        		directoryBox.setDisable(false);
	        	}
	        	if(newVal == false) {
	        		dir.clear();
	        		directoryBox.setDisable(true);
	        	}
	        });
	        
	        HBox toggleBox = new HBox();
	        toggleBox.getChildren().add(platformIsAndroid);
	        toggleBox.getChildren().add(rbIOS);
	        toggleBox.setSpacing(10);
	        
	        VBox osSelectionBox = new VBox();
	        Label labelSelection = new Label("What is your mobile phone's operating system?");
	        labelSelection.setTextFill(Color.WHITE);
	        osSelectionBox.getChildren().add(labelSelection);
	        osSelectionBox.getChildren().add(toggleBox);
	        
	        
			VBox continueBox = new VBox();
			Button continueButton = new Button("Continue");

            EventHandler<InputEvent> handler = new EventHandler<InputEvent>() {
                public void handle(InputEvent event) {
                    if(checkFieldsValid(mysql_port, username, password, database, condor_license, platformIsAndroid,
                            ios_backup_directory, phone_number)) {
                        DynamicConfig dc = DynamicConfig.createEmpty("config/dynConf.txt");
                        dc.setCondor_license(condor_license.getText());
                        dc.setDatabase(database.getText());
                        dc.setIos_backup_directory(ios_backup_directory.getText());
                        dc.setMysql_port(mysql_port.getText());
                        dc.setPassword(password.getText());
                        dc.setUsername(username.getText());
                        dc.setPlatformIsAndroid(platformIsAndroid.isSelected());
                        dc.setPhone_number(phone_number.getText());
                        dc.close();
                        dbProcess = new ProcessHandler(dc, DefaultConfig.create());
                        Thread th = new Thread(dbProcess);
                        th.start();
                    }
                }
            };

			continueButton.addEventHandler(MouseEvent.MOUSE_CLICKED, handler);
			continueBox.getChildren().add(continueButton);
			continueBox.setPadding(new Insets(20));
			//continueBox.setStyle("-fx-background-color: black;");
			continueBox.alignmentProperty().set(Pos.CENTER);
			continueButton.setPrefWidth(200);
			
			VBox settingsBox = new VBox();
			settingsBox.setPadding(new Insets(10));
			settingsBox.setSpacing(5);
			settingsBox.getChildren().add(portBox);
			settingsBox.getChildren().add(usernameBox);
			settingsBox.getChildren().add(passwordBox);
			settingsBox.getChildren().add(databaseBox);
            settingsBox.getChildren().add(condorLicenseBox);
			settingsBox.getChildren().add(phoneNumberBox);
			settingsBox.getChildren().add(osSelectionBox);
			settingsBox.getChildren().add(directoryBox);
			//settingsBox.getChildren().add(continueButton);
			
			
			root.setCenter(settingsBox);
			root.setBottom(continueBox);
			

			
			Scene scene = new Scene(root,400,450);
			scene.getStylesheets().add(getClass().getClassLoader().getResource("css/application.css").toExternalForm());
			primaryStage.setTitle("COIN 2019 - WhatsApp chats extractor v.1");
			
			primaryStage.getIcons().add(new Image(getClass().getClassLoader().getResourceAsStream("img/uni2.png")));
			
			//stage.getIcons().add(new Image(<yourclassname>.class.getResourceAsStream("icon.png")));
			
			primaryStage.setScene(scene);
		
			primaryStage.show();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

    private boolean checkFieldsValid(TextField portBox, TextField usernameBox, TextField passwordBox, TextField databaseBox,
                                  TextField condorLicenceBox, RadioButton toggleBox, TextField directoryBox, TextField phoneNumberBox) {
	    boolean everythingChecked = true;
        Pattern numbers = Pattern.compile(".[0-9]+");
        Matcher m = numbers.matcher(portBox.getText());
	    if(!m.matches())
        {
            everythingChecked = false;
        }
        m = numbers.matcher(phoneNumberBox.getText());
	    if(!m.matches())
        {
            everythingChecked = false;
        }
        if(usernameBox.getText().equals(""))
        {
            everythingChecked = false;
        }
        if(passwordBox.getText().equals(""))
        {
            everythingChecked = false;
        }
        if(databaseBox.getText().equals(""))
        {
            everythingChecked = false;
        }
        if (condorLicenceBox.getText().equals(""))
        {
            everythingChecked = false;
        }
        if (toggleBox.isSelected())
        {
            if(directoryBox.getText().equals(""))
            {
                everythingChecked = false;
            }
            else
            {
                if(!Paths.get(directoryBox.getText()).toFile().exists())
                {
                    everythingChecked = false;
                }
            }
        }
        return everythingChecked;
    }

    public static void main(String[] args) {
		launch(args);
	}
}
