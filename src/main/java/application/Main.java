package application;

import java.awt.*;
import java.io.File;
import java.io.IOException;

import javafx.application.Application;
import javafx.event.EventType;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import com.company.ProcessHandler;
import javafx.event.EventHandler;
import javafx.scene.input.InputEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import shared.DefaultConfig;
import shared.DynamicConfig;


public class Main extends Application {

    private boolean platformIsAndroid = true;
    private TextField tfMysqlHost, tfMysqlPort, tfusername, tfDatabase, tfCondorLicense, tfIosBackupDirectory,
            tfCommand, tfPhoneNumber;
    private PasswordField pf;
    private VBox settingsBox, osSelectionBox, directoryBox;
    private HBox continueBox;
    private Button continueFromDatabaseSettingsButton, continueFromOSSelectionButton, sendCommandButton;
    private BorderPane root;
    private Stage primaryStage;
    private Button backToOSSelection;
    private ProcessHandler dbProcess;
    private static Logger log = LogManager.getLogger("condor-whatsapp-main");

    @Override
    public void start(Stage primaryStage) {
        tfMysqlHost = new TextField();
        tfMysqlPort = new TextField();
        tfusername = new TextField();
        tfDatabase = new TextField();
        tfCondorLicense = new TextField();
        tfPhoneNumber = new TextField();
        tfIosBackupDirectory = new TextField();
        tfCommand = new TextField();
        pf = new PasswordField();
        log.trace("Initializing Main");
        try {
            this.primaryStage = primaryStage;
            root = new BorderPane();

            // Prefill via Textfile
            DynamicConfig dc = DynamicConfig.create("config/dynamic_config_ph.txt");
            tfMysqlPort.setText(dc.getMysqlPort());
            tfMysqlHost.setText(dc.getMysqlHost());
            tfusername.setText(dc.getUsername());
            pf.setText(dc.getPassword());
            tfDatabase.setText(dc.getDatabase());
            tfCondorLicense.setText(dc.getCondor_license());
            tfPhoneNumber.setText(dc.getPhoneNumber());
            tfIosBackupDirectory.setText(dc.getIosBackupDirectory());

            log.trace("Initialized Textfields");

            initOSSelectionBox();
            initSettingsBox();

            log.trace("Run through init Methods");

            continueBox = new HBox();
            continueBox.setPadding(new Insets(10));
            continueBox.setSpacing(5);
            continueBox.alignmentProperty().set(Pos.CENTER);
            continueBox.getChildren().add(continueFromOSSelectionButton);

            root.setCenter(osSelectionBox);
            root.setBottom(continueBox);

            VBox continueBox = new VBox();
            continueBox.getChildren().add(continueFromDatabaseSettingsButton);
            continueBox.setPadding(new Insets(20));
            //continueBox.setStyle("-fx-background-color: black;");
            continueBox.alignmentProperty().set(Pos.CENTER);
            continueFromDatabaseSettingsButton.setPrefWidth(200);

            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getClassLoader().getResource("css/application.css").toExternalForm());

            primaryStage.setTitle("COIN 2019 - WhatsApp Chats Extractor v.1");
            primaryStage.getIcons().add(new Image(getClass().getClassLoader().getResourceAsStream("img/uni2.png")));
            primaryStage.setScene(scene);
            primaryStage.show();
            primaryStage.sizeToScene();

        } catch(Exception e) {
            e.printStackTrace();
        }

    }

    private void progressScene(Stage primaryStage)
    {
        BorderPane root = new BorderPane();

        VBox contentBox = new VBox();
        Label title = new Label("Progress");
        title.setTextFill(Color.WHITE);
        contentBox.getChildren().add(title);
        TextArea ta = new TextArea();
        ta.setEditable(false);
        contentBox.getChildren().add(ta);

        DynamicConfig dc = DynamicConfig.createEmpty("config/dynamic_config.txt");
        dc.setCondor_license(tfCondorLicense.getText());
        dc.setDatabase(tfDatabase.getText());
        dc.setIosBackupDirectory(tfIosBackupDirectory.getText());
        dc.setMysqlPort(tfMysqlPort.getText());
        dc.setPassword(pf.getText());
        dc.setUsername(tfusername.getText());
        dc.setPlatformIsAndroid(platformIsAndroid);
        dc.setPhoneNumber(tfPhoneNumber.getText());

        dbProcess = new ProcessHandler(dc, DefaultConfig.create());
        dbProcess.messageProperty().addListener((observable, oldValue, newValue) -> {
            log.trace(newValue);
            StringBuilder sb = new StringBuilder(newValue);
            sb.append("\r\n");
            sb.append(ta.getText());
            ta.setText(sb.toString());
            switch(newValue)
            {
                case "Condor complete":
                    openFiles();
                    dbProcess.cancel();
                    break;
                case "Requesting User Input":
                    tfCommand.setEditable(true);
                    sendCommandButton.setVisible(true);
                    break;
                case "Successfully extracted keyfile and db":
                    tfCommand.setVisible(false);
                    sendCommandButton.setVisible(false);
                    break;
                default:
                    break;
            }
        });

        VBox inputBox = new VBox();
        Label inputLabel = new Label("Enter the command");
        inputLabel.setTextFill(Color.WHITE);
        inputBox.getChildren().add(inputLabel);
        tfCommand.setEditable(false);
        inputBox.getChildren().add(tfCommand);
        sendCommandButton = new Button("Send Command");
        sendCommandButton.setVisible(false);
        sendCommandButton.addEventHandler(MouseEvent.MOUSE_CLICKED, (EventHandler<InputEvent>) event -> {
            dbProcess.passCommand(tfCommand.getText());
        });
        inputBox.getChildren().add(sendCommandButton);

        root.setCenter(contentBox);
        root.setBottom(inputBox);

        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getClassLoader().getResource("css/application.css").toExternalForm());

        primaryStage.setTitle("COIN 2019 - WhatsApp Chats Extractor v.1");
        primaryStage.getIcons().add(new Image(getClass().getClassLoader().getResourceAsStream("img/uni2.png")));
        primaryStage.setScene(scene);
        primaryStage.show();
        primaryStage.sizeToScene();

        Thread th = new Thread(dbProcess);
        th.start();
    }

    private void openFiles() {
        DefaultConfig df = DefaultConfig.create();

        try {
            Desktop.getDesktop().open(new File(df.getStandard_export_actors()));
        } catch (IOException e) {
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
        ivIOS.setImage(new Image(getClass().getClassLoader().getResourceAsStream("img/apple2.png")));
        ivIOS.setOnMouseClicked(e->{
            rbIOS.setSelected(true);
        });

        ImageView ivAndroid = new ImageView();
        ivAndroid.setImage(new Image(getClass().getClassLoader().getResourceAsStream("img/android2.png")));
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

        continueFromOSSelectionButton = new Button("Continue ->");
        continueFromOSSelectionButton.setPrefWidth(200);
        continueFromOSSelectionButton.setOnAction( e -> {

            //setNextScene
            platformIsAndroid = rbAndroid.isSelected();
            log.trace("Platform is Android " +  platformIsAndroid);

            settingsBox.getChildren().remove(directoryBox);
            if(!platformIsAndroid) settingsBox.getChildren().add(directoryBox);

            root.getChildren().remove(osSelectionBox);
            root.setCenter(settingsBox);
            continueBox.getChildren().remove(continueFromOSSelectionButton);

            continueBox.getChildren().add(backToOSSelection);
            continueBox.getChildren().add(continueFromDatabaseSettingsButton);

            primaryStage.sizeToScene();

        });

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
            osSelectionBox.setPrefHeight(150);
            primaryStage.sizeToScene();
        });
        backToOSSelection.setPrefWidth(100);
        backToOSSelection.alignmentProperty().set(Pos.CENTER_LEFT);
    }

    private void initSettingsBox() {
        VBox hostBox = new VBox();
        Label labelHost = new Label("MySQL Host:");
        labelHost.setTextFill(Color.WHITE);
        labelHost.setTooltip(new Tooltip("--- here advise/help/information---"));
        hostBox.getChildren().add(labelHost);
        tfMysqlHost.setTooltip(new Tooltip("--- here advise/help/information---"));
        hostBox.getChildren().add(tfMysqlHost);

        VBox portBox = new VBox();
        Label labelPort = new Label("MySQL Port:");
        labelPort.setTextFill(Color.WHITE);
        labelPort.setTooltip(new Tooltip("--- here advise/help/information---"));
        portBox.getChildren().add(labelPort);
        tfMysqlPort.setTooltip(new Tooltip("--- here advise/help/information---"));
        portBox.getChildren().add(tfMysqlPort);

        VBox usernameBox = new VBox();
        Label labelUser = new Label("Username:");
        labelUser.setTextFill(Color.WHITE);
        labelUser.setTooltip(new Tooltip("--- here advise/help/information---"));
        usernameBox.getChildren().add(labelUser);
        tfusername.setTooltip(new Tooltip("--- here advise/help/information---"));
        usernameBox.getChildren().add(tfusername);

        VBox passwordBox = new VBox();
        Label labelPassword = new Label("Password:");
        labelPassword.setTextFill(Color.WHITE);
        labelPassword.setTooltip(new Tooltip("--- here advise/help/information---"));
        pf.setTooltip(new Tooltip("--- here advise/help/information---"));
        passwordBox.getChildren().add(labelPassword);
        passwordBox.getChildren().add(pf);

        VBox phoneNumberBox = new VBox();
        Label labelPhoneNumber = new Label("Phone Number:");
        labelPhoneNumber.setTooltip(new Tooltip("--- here advise/help/information---"));
        labelPhoneNumber.setTextFill(Color.WHITE);
        tfPhoneNumber.setTooltip(new Tooltip("--- here advise/help/information---"));
        phoneNumberBox.getChildren().add(labelPhoneNumber);
        phoneNumberBox.getChildren().add(tfPhoneNumber);

        VBox databaseBox = new VBox();
        Label labelDatabase = new Label("Choose non existing database name:");
        labelDatabase.setTextFill(Color.WHITE);
        labelDatabase.setTooltip(new Tooltip("--- here advise/help/information---"));
        databaseBox.getChildren().add(labelDatabase);
        tfDatabase.setTooltip(new Tooltip("--- here advise/help/information---"));
        databaseBox.getChildren().add(tfDatabase);

        VBox condorLicenceBox = new VBox();
        Label labelLicence = new Label("Condor License Key:");
        labelLicence.setTooltip(new Tooltip("--- here advise/help/information---"));
        labelLicence.setTextFill(Color.WHITE);
        condorLicenceBox.getChildren().add(labelLicence);
        tfCondorLicense.setTooltip(new Tooltip("--- here advise/help/information---"));
        condorLicenceBox.getChildren().add(tfCondorLicense);

        Label labelInstr = new Label("Please fill in all fields below!");
        labelInstr.setFont(new Font(18));
        labelInstr.setTextFill(Color.WHITE);



        settingsBox = new VBox();
        settingsBox.setPadding(new Insets(10));
        settingsBox.setSpacing(5);
        settingsBox.alignmentProperty().set(Pos.TOP_CENTER);
        settingsBox.getChildren().addAll(labelInstr,condorLicenceBox,hostBox,portBox,usernameBox,passwordBox,databaseBox,phoneNumberBox);

        continueFromDatabaseSettingsButton = new Button("Continue ->");
        continueFromDatabaseSettingsButton.setPrefWidth(100);
        continueFromDatabaseSettingsButton.alignmentProperty().set(Pos.CENTER_RIGHT);

        continueFromDatabaseSettingsButton.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> {
            if(tfMysqlHost.getText().isEmpty() || tfMysqlPort.getText().isEmpty() || tfusername.getText().isEmpty() ||
                    tfDatabase.getText().isEmpty() || pf.getText().isEmpty() ||
                    tfPhoneNumber.getText().isEmpty()) {
                Alert alert = new Alert(AlertType.ERROR, "Please fill in all required fields", ButtonType.OK);
                alert.showAndWait();
            }
            else
                progressScene(primaryStage);
        });


        directoryBox = new VBox();
        DirectoryChooser directoryChooser = new DirectoryChooser();
        Button openButton = new Button("Navigate");
        openButton.setPrefWidth(100);
        openButton.setOnAction( e-> {
            File file = directoryChooser.showDialog(primaryStage);
            if (file != null) tfIosBackupDirectory.setText(file.getAbsolutePath());
        });
        Label labelDir = new Label("Chose a place for your IOS backup:");
        labelDir.setTextFill(Color.WHITE);
        directoryBox.getChildren().add(labelDir);
        HBox dirBox = new HBox();
        dirBox.getChildren().add(openButton);
        dirBox.setSpacing(5);
        tfIosBackupDirectory.prefWidthProperty().bind(dirBox.widthProperty().subtract(openButton.getPrefWidth()));
        dirBox.getChildren().add(tfIosBackupDirectory);
        directoryBox.getChildren().add(dirBox);

    }
}