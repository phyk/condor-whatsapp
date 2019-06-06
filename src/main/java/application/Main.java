package application;
	
import java.io.File;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;


public class Main extends Application {
	
    private String mysql_port;
    private String username;
    private String password;
    private String database;
    private String condor_license;
    private boolean platformIsAndroid;
    private String ios_backup_directory;
	
	
	@Override
	public void start(Stage primaryStage) {
		try {
			BorderPane root = new BorderPane();
			
			VBox portBox = new VBox();
			Label labelPort = new Label("MySQL Port:");
			labelPort.setTextFill(Color.WHITE);
			portBox.getChildren().add(labelPort);
			portBox.getChildren().add(new TextField(mysql_port));
			
			VBox usernameBox = new VBox();
			Label labelUser = new Label("Username:");
			labelUser.setTextFill(Color.WHITE);
			usernameBox.getChildren().add(labelUser);
			usernameBox.getChildren().add(new TextField(username));
			
			VBox passwordBox = new VBox();
			Label labelPassword = new Label("Password:");
			labelPassword.setTextFill(Color.WHITE);
			passwordBox.getChildren().add(labelPassword);
			passwordBox.getChildren().add(new PasswordField());
			
			VBox databaseBox = new VBox();
			Label labelDatabase = new Label("Database:");
			labelDatabase.setTextFill(Color.WHITE);
			databaseBox.getChildren().add(labelDatabase);
			databaseBox.getChildren().add(new TextField(database));
			
			VBox condorLicenceBox = new VBox();
			Label labelLicence = new Label("Condor Licence:");
			labelLicence.setTextFill(Color.WHITE);
			condorLicenceBox.getChildren().add(labelLicence);
			condorLicenceBox.getChildren().add(new TextField(condor_license));
			
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

	        RadioButton rbAndroid = new RadioButton("Android");
	        rbAndroid.setToggleGroup(group);
	        rbAndroid.setTextFill(Color.WHITE);
	        rbAndroid.setSelected(true);

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
	        toggleBox.getChildren().add(rbAndroid);
	        toggleBox.getChildren().add(rbIOS);
	        toggleBox.setSpacing(10);
	        
	        VBox osSelectionBox = new VBox();
	        Label labelSelection = new Label("What is your mobile phone's operating system?");
	        labelSelection.setTextFill(Color.WHITE);
	        osSelectionBox.getChildren().add(labelSelection);
	        osSelectionBox.getChildren().add(toggleBox);
	        
	        
			VBox continueBox = new VBox();
			Button continueButton = new Button("Continue");
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
			settingsBox.getChildren().add(osSelectionBox);
			settingsBox.getChildren().add(directoryBox);
			//settingsBox.getChildren().add(continueButton);
			
			
			root.setCenter(settingsBox);
			root.setBottom(continueBox);
			

			
			Scene scene = new Scene(root,400,400);
			scene.getStylesheets().add(getClass().getClassLoader().getResource("css/application.css").toExternalForm());
			primaryStage.setTitle("COIN 2019 - WhatsApp chats extractor v.1");
			
			primaryStage.getIcons().add(new Image(getClass().getClassLoader().getResourceAsStream("uni2.png")));
			
			//stage.getIcons().add(new Image(<yourclassname>.class.getResourceAsStream("icon.png")));
			
			primaryStage.setScene(scene);
		
			primaryStage.show();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		launch(args);
	}
}
