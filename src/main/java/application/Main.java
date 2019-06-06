package application;
import java.io.File;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;


public class Main extends Application {

	private boolean platformIsAndroid = true;
	private TextField tfMysqlHost, tfMysqlPort, tfusername, tfDatabase, tfCondorLicense;
	private PasswordField pf;
	private VBox settingsBox, osSelectionBox;
	private HBox continueBox;
	private Button continueFromDatabaseSettingsButton, continueFromOSSelectionButton;
	private BorderPane root;
	private Stage primaryStage;
	private Button backToOSSelection;



	@Override
	public void start(Stage primaryStage) {
		try {
			this.primaryStage = primaryStage;
			root = new BorderPane();
			
			initOSSelectionBox();
			initSettingsBox();
			
			
			continueBox = new HBox();
			continueBox.setPadding(new Insets(10));
			continueBox.setSpacing(5);
			continueBox.alignmentProperty().set(Pos.CENTER);
			continueBox.getChildren().add(continueFromOSSelectionButton);
			
			root.setCenter(osSelectionBox);
			root.setBottom(continueBox);

			Scene scene = new Scene(root,400.0,414.0);
			scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());

			primaryStage.setTitle("COIN 2019 - WhatsApp Chats Extractor v.1");
			primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("uni2.png")));
			primaryStage.setScene(scene);
			primaryStage.show();

		} catch(Exception e) {
			e.printStackTrace();
		}

	}


	public static void main(String[] args) {
		launch(args);
	}
	
	private void initOSSelectionBox() {
		final ToggleGroup group = new ToggleGroup();

		Label labelSelection = new Label("What is your mobile phone's operating system?");
		labelSelection.setFont(new Font(18));
		labelSelection.setTextFill(Color.WHITE);
		
		RadioButton rbAndroid = new RadioButton("Android");
		rbAndroid.setToggleGroup(group);
		rbAndroid.setTextFill(Color.WHITE);
		rbAndroid.setSelected(true);

		RadioButton rbIOS = new RadioButton("IOS (Iphone)");
		rbIOS.setTextFill(Color.WHITE);
		rbIOS.setToggleGroup(group);

		osSelectionBox = new VBox();
		HBox toggleBox = new HBox();

		ImageView ivIOS = new ImageView();
		ivIOS.setImage(new Image(getClass().getResourceAsStream("apple2.png")));
		ivIOS.setOnMouseClicked(e->{
			rbIOS.setSelected(true);
		});

		ImageView ivAndroid = new ImageView();
		ivAndroid.setImage(new Image(getClass().getResourceAsStream("android2.png")));
		ivAndroid.setOnMouseClicked(e->{
			rbAndroid.setSelected(true);
		});

		VBox iosBox = new VBox();
		iosBox.prefWidthProperty().bind(toggleBox.widthProperty().divide(2));
		iosBox.getChildren().add(ivIOS);
		iosBox.getChildren().add(rbIOS);
		iosBox.alignmentProperty().set(Pos.CENTER);
		iosBox.setSpacing(10);

		VBox androidBox = new VBox();
		androidBox.prefWidthProperty().bind(toggleBox.widthProperty().divide(2));
		androidBox.alignmentProperty().set(Pos.CENTER);
		androidBox.getChildren().add(ivAndroid);
		androidBox.getChildren().add(rbAndroid);
		androidBox.setSpacing(10);

		toggleBox.getChildren().add(androidBox);
		toggleBox.getChildren().add(iosBox);
		toggleBox.prefHeightProperty().bind(osSelectionBox.heightProperty().subtract(labelSelection.getHeight()));
		toggleBox.setSpacing(10);

		osSelectionBox.alignmentProperty().set(Pos.TOP_CENTER);
		osSelectionBox.getChildren().add(labelSelection);
		osSelectionBox.getChildren().add(toggleBox);
		osSelectionBox.setSpacing(20);
		osSelectionBox.setPadding(new Insets(10));

		backToOSSelection = new Button ("<- Go back");
		backToOSSelection.setOnAction(e->{
			continueBox.getChildren().removeAll(continueFromDatabaseSettingsButton,backToOSSelection);
			continueBox.getChildren().add(continueFromOSSelectionButton);
			continueFromOSSelectionButton.alignmentProperty().set(Pos.CENTER);
			root.getChildren().remove(settingsBox);
			root.setCenter(osSelectionBox);
			
			primaryStage.sizeToScene();
		});
		backToOSSelection.setPrefWidth(100);
		backToOSSelection.alignmentProperty().set(Pos.CENTER_LEFT);
		continueFromOSSelectionButton = new Button("Continue ->");
		continueFromOSSelectionButton.setPrefWidth(200);
		continueFromOSSelectionButton.setOnAction( e -> {

			//setNextScene
			if(rbIOS.isSelected()) platformIsAndroid = false;
			else platformIsAndroid = true;

			root.getChildren().remove(osSelectionBox);
			root.setCenter(settingsBox);
			continueBox.getChildren().remove(continueFromOSSelectionButton);
			
			continueBox.getChildren().add(backToOSSelection);
			continueBox.getChildren().add(continueFromDatabaseSettingsButton);
			primaryStage.sizeToScene();

		});
	}
	
	private void initSettingsBox() {
		VBox hostBox = new VBox();
		Label labelHost = new Label("MySQL Host:");
		labelHost.setTextFill(Color.WHITE);
		hostBox.getChildren().add(labelHost);
		tfMysqlHost = new TextField("localhost");
		hostBox.getChildren().add(tfMysqlHost);

		VBox portBox = new VBox();
		Label labelPort = new Label("MySQL Port:");
		labelPort.setTextFill(Color.WHITE);
		portBox.getChildren().add(labelPort);
		tfMysqlPort = new TextField("3306");
		portBox.getChildren().add(tfMysqlPort);

		VBox usernameBox = new VBox();
		Label labelUser = new Label("Username:");
		labelUser.setTextFill(Color.WHITE);
		usernameBox.getChildren().add(labelUser);
		tfusername = new TextField("root");
		usernameBox.getChildren().add(tfusername);

		VBox passwordBox = new VBox();
		Label labelPassword = new Label("Password:");
		labelPassword.setTextFill(Color.WHITE);
		pf = new PasswordField();
		passwordBox.getChildren().add(labelPassword);
		passwordBox.getChildren().add(pf);

		VBox databaseBox = new VBox();
		Label labelDatabase = new Label("Database:");
		labelDatabase.setTextFill(Color.WHITE);
		databaseBox.getChildren().add(labelDatabase);
		tfDatabase = new TextField("default");
		databaseBox.getChildren().add(tfDatabase);

		VBox condorLicenceBox = new VBox();
		Label labelLicence = new Label("Condor License Key:");
		labelLicence.setTextFill(Color.WHITE);
		condorLicenceBox.getChildren().add(labelLicence);
		tfCondorLicense = new TextField();
		condorLicenceBox.getChildren().add(tfCondorLicense);
		
		Label labelInstr = new Label("Please fill in all fields below!");
		labelInstr.setFont(new Font(18));
		labelInstr.setTextFill(Color.WHITE);
	
		
		settingsBox = new VBox();
		settingsBox.setPadding(new Insets(10));
		settingsBox.setSpacing(5);
		settingsBox.alignmentProperty().set(Pos.TOP_CENTER);
		settingsBox.getChildren().addAll(labelInstr,condorLicenceBox,hostBox,portBox,usernameBox,passwordBox,databaseBox);
		
		continueFromDatabaseSettingsButton = new Button("Continue ->");
		continueFromDatabaseSettingsButton.setPrefWidth(100);
		continueFromDatabaseSettingsButton.alignmentProperty().set(Pos.CENTER_RIGHT);

		continueFromDatabaseSettingsButton.setOnAction( e -> {
			if(tfMysqlHost.getText().isEmpty() || tfMysqlPort.getText().isEmpty() || tfusername.getText().isEmpty() || tfDatabase.getText().isEmpty() || tfCondorLicense.getText().isEmpty() || pf.getText().isEmpty()) {
				Alert alert = new Alert(AlertType.ERROR, "Please fill in all required fields", ButtonType.OK);
				alert.showAndWait();
			}
		});
		
		//TODO
		
		VBox directoryBox = new VBox();
		TextField dir = new TextField();
		FileChooser fileChooser = new FileChooser();
		Button openButton = new Button("Navigate");
		openButton.setPrefWidth(100);
		openButton.setOnAction( e-> {
			File file = fileChooser.showOpenDialog(primaryStage);
			if (file != null) dir.setText(file.getAbsolutePath());
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
	}

	private void printStageSize(Stage stage){
		System.out.println("Stage Height: "+stage.getHeight());
		System.out.println("Stage Width: "+stage.getWidth());
	}
}