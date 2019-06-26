package application;

import java.awt.*;
import java.io.File;
import java.io.IOException;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.web.WebView;
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
    private VBox osSelectionBox, directoryBox,connectBox,developerModeBox,IOSBackupBox;
    private HBox continueBox;
    private Button  continueFromOSSelectionButton, sendCommandButton, continueDeveloperModeButton,continueFromConnectingButton,continueAndroidButton,continueIOSdButton;
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
            DynamicConfig dc = DynamicConfig.create("config/dynamic_config.txt");
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
            initDeveloperModePane();
            initConnectPane();
            initIOSBackupBox();

            log.trace("Run through init Methods");

            continueBox = new HBox();
            continueBox.setPadding(new Insets(10));
            continueBox.setSpacing(5);
            continueBox.alignmentProperty().set(Pos.CENTER);
            continueBox.getChildren().add(continueFromConnectingButton);

            root.setCenter(connectBox);
            root.setBottom(continueBox);

            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getClassLoader().getResource("css/application.css").toExternalForm());

            primaryStage.setTitle("COIN 2019 - WhatsApp Chats Extractor v.1");
            primaryStage.getIcons().add(new Image(getClass().getClassLoader().getResourceAsStream("img/logo.png")));
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
        contentBox.setPadding(new Insets(10));
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
                    sendCommandButton.setDisable(false);
                    break;
                case "Successfully extracted keyfile and db":
                    tfCommand.setVisible(false);
                    sendCommandButton.setDisable(true);
                    break;
                default:
                    break;
            }
        });

        VBox inputBox = new VBox(5);
        inputBox.setPadding(new Insets(10));
        Label inputLabel = new Label("Please wait! You will receive an SMS with an activation code - this might take a while!\nPlease enter the activation code and send it.\nRequest a new activation code if you don't get one within 5 minutes.");
        inputLabel.setTextFill(Color.WHITE);
        inputLabel.setFont(new Font(14));
        inputBox.getChildren().add(inputLabel);
        tfCommand.setEditable(false);
        inputBox.getChildren().add(tfCommand);
        sendCommandButton = new Button("Send activation code");
        sendCommandButton.setPrefWidth(300);
        sendCommandButton.setDisable(true);
        sendCommandButton.setOnAction( event -> {
            dbProcess.passCommand(tfCommand.getText());
            dbProcess.passMessage("exit()");
        });

        Button requestButton = new Button("Request new activation code");
        requestButton.setOnAction( event -> {
            dbProcess.passCommand("");
        });
        requestButton.setPrefWidth(300);

        HBox buttonBox = new HBox(5);
        buttonBox.getChildren().addAll(sendCommandButton,requestButton);
        inputBox.getChildren().addAll(buttonBox);

        root.setCenter(contentBox);
        if(platformIsAndroid)
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

        VBox phoneNumberBox = new VBox();
        Label labelPhoneNumber = new Label("Please enter your mobile phone number e.g. +491651234567:");
        labelPhoneNumber.setTooltip(new Tooltip("Please enter your phone number with country code (+49) and without 0: e.g. +491751243567"));
        labelPhoneNumber.setTextFill(Color.WHITE);
        tfPhoneNumber.setTooltip(new Tooltip("Please enter your phone number with country code (+49) and without 0: e.g. +491751243567"));
        phoneNumberBox.getChildren().add(labelPhoneNumber);
        phoneNumberBox.getChildren().add(tfPhoneNumber);

        continueFromOSSelectionButton = new Button("Continue ->");
        continueFromOSSelectionButton.setPrefWidth(200);
        continueFromOSSelectionButton.setOnAction( e -> {

            if(tfPhoneNumber.getText().isEmpty()||!tfPhoneNumber.getText().contains("+")||tfPhoneNumber.getText().contains("+490")||tfPhoneNumber.getText().length()<8){
                Alert alert = new Alert(AlertType.ERROR, "Please enter your phone number with country code (+49) and without 0: e.g. +491751243567", ButtonType.OK);
                alert.showAndWait();
            }
            else {
                //setNextScene
                platformIsAndroid = rbAndroid.isSelected();
                log.trace("Platform is Android " + platformIsAndroid);
                root.getChildren().remove(osSelectionBox);
                continueBox.getChildren().remove(continueFromOSSelectionButton);

                if(platformIsAndroid){
                    root.setCenter(developerModeBox);
                    continueAndroidButton = new Button();
                    continueAndroidButton.setOnAction(c->{
                        progressScene(primaryStage);
                    });
                    continueBox.getChildren().addAll(backToOSSelection,continueDeveloperModeButton);

                }
                else{
                    root.setCenter(IOSBackupBox);
                    continueIOSdButton = new Button("Continue ->");
                    continueIOSdButton.setPrefWidth(100);
                    continueIOSdButton.setOnAction(c->{
                        if(tfIosBackupDirectory.getText().isEmpty()){
                            Alert alert = new Alert(AlertType.ERROR, "Navigate to the place where your IOS Backup is saved by using the navigate button. For instructions see the link above.", ButtonType.OK);
                            alert.showAndWait();
                        }
                        else progressScene(primaryStage);
                    });
                    continueBox.getChildren().addAll(backToOSSelection,continueIOSdButton);
                }

                primaryStage.sizeToScene();
            }
        });

        osSelectionBox.alignmentProperty().set(Pos.TOP_CENTER);
        osSelectionBox.getChildren().add(labelSelection);
        osSelectionBox.getChildren().add(toggleBox);
        osSelectionBox.getChildren().add(phoneNumberBox);
        osSelectionBox.setSpacing(20);
        osSelectionBox.setPadding(new Insets(10));

        backToOSSelection = new Button ("<- Go back");
        backToOSSelection.setOnAction(e->{
            continueBox.getChildren().removeAll(backToOSSelection,continueAndroidButton,continueIOSdButton,continueDeveloperModeButton);
            continueBox.getChildren().add(continueFromOSSelectionButton);
            continueFromOSSelectionButton.alignmentProperty().set(Pos.CENTER);

            root.getChildren().removeAll(developerModeBox);
            root.setCenter(osSelectionBox);
            osSelectionBox.setPrefHeight(150);
            primaryStage.sizeToScene();
        });
        backToOSSelection.setPrefWidth(100);
        backToOSSelection.alignmentProperty().set(Pos.CENTER_LEFT);
    }

    private void initConnectPane() {

        ImageView connectIV = new ImageView();
        connectIV.setImage(new Image(getClass().getClassLoader().getResourceAsStream("img/connecting.png")));

        continueFromConnectingButton = new Button("Continue ->");
        continueFromConnectingButton.setPrefWidth(200);

        continueFromConnectingButton.setOnAction( e->{
            root.getChildren().remove(connectBox);
            root.setCenter(osSelectionBox);
            continueBox.getChildren().remove(continueFromConnectingButton);
            continueBox.getChildren().add(continueFromOSSelectionButton);
            primaryStage.sizeToScene();
        });

        connectBox = new VBox();
        connectBox.getChildren().addAll(connectIV);
    }

    private void initDeveloperModePane(){

        WebView webview = new WebView();
        webview.getEngine().loadContent(
                "<iframe width=\"660\" height=\"415\" src=\"http://www.youtube.com/embed/Jf4RydXv7X8\" frameborder=\"0\" allowfullscreen></iframe>"
        );
        webview.setPrefSize(680, 435);

        VBox androidInstructionBox = new VBox();
        Label instruction0 = new Label("Please enable Developer Options on your android phone & USB-debugging:");
        Label instruction1 = new Label("1. Go to Settings menu and scroll down to About device. Tap it.");
        Label instruction2 = new Label("2. Tap on Software info.");
        Label instruction3 = new Label("3. Tap on Build number 7 times.");
        Label instruction4 = new Label("4. Go back to Settings menu. Scroll to the bottom & tap on Developer Mode.");
        Label instruction5 = new Label("5. Tap on activate USB-debugging");

        Label lAS = new Label("Finally -> please install Android Studio:");
        lAS.setTextFill(Color.WHITE);
        lAS.setFont(new Font(18));
        Hyperlink hlAS = new Hyperlink("Download here");
        hlAS.setFont(new Font(18));
        hlAS.setUnderline(true);
        hlAS.setOnAction(e->{
            getHostServices().showDocument("https://developer.android.com/studio");
        });

        GridPane androidStudioBox = new GridPane();
        androidStudioBox.setHgap(5);
        androidStudioBox.setPadding(new Insets(10,0,10,0));
        androidStudioBox.add(lAS,0,0);
        androidStudioBox.add(hlAS,1,0);

        instruction0.setTextFill(Color.WHITE);
        instruction0.setFont(new Font(18));
        instruction1.setTextFill(Color.WHITE);
        instruction1.setFont(new Font(18));
        instruction2.setTextFill(Color.WHITE);
        instruction2.setFont(new Font(18));
        instruction3.setTextFill(Color.WHITE);
        instruction3.setFont(new Font(18));
        instruction4.setTextFill(Color.WHITE);
        instruction4.setFont(new Font(18));
        instruction5.setTextFill(Color.WHITE);
        instruction5.setFont(new Font(18));
        androidInstructionBox.getChildren().addAll(instruction0,instruction1,instruction2,instruction3,instruction4,instruction5,androidStudioBox);
        androidInstructionBox.setPadding(new Insets(10));
        VBox webviewBox = new VBox();
        webviewBox.getChildren().add(webview);
        webviewBox.setAlignment(Pos.CENTER);
        webviewBox.setStyle("-fx-background-color: black;");
        developerModeBox = new VBox();
        developerModeBox.getChildren().addAll(webviewBox,androidInstructionBox);

        continueDeveloperModeButton = new Button("Continue ->");
        continueDeveloperModeButton.setPrefWidth(100);
        continueDeveloperModeButton.alignmentProperty().set(Pos.CENTER_RIGHT);

        continueDeveloperModeButton.setOnAction(e->{
                progressScene(primaryStage);
        });
    }

    private void initIOSBackupBox(){

        IOSBackupBox = new VBox();
        directoryBox = new VBox();
        DirectoryChooser directoryChooser = new DirectoryChooser();
        Button openButton = new Button("Navigate");
        openButton.setPrefWidth(100);
        openButton.setOnAction( e-> {
            File file = directoryChooser.showDialog(primaryStage);
            if (file != null) tfIosBackupDirectory.setText(file.getAbsolutePath());
        });
        Label labelDir = new Label("Navigate to the place of your IOS backup:");
        labelDir.setTextFill(Color.WHITE);

        HBox dirBox = new HBox();
        dirBox.getChildren().add(openButton);
        dirBox.setSpacing(5);
        directoryBox.setPadding(new Insets(10));
        tfIosBackupDirectory.prefWidthProperty().bind(dirBox.widthProperty().subtract(openButton.getPrefWidth()));
        dirBox.getChildren().add(tfIosBackupDirectory);
        directoryBox.getChildren().addAll(labelDir,dirBox);


        Hyperlink hliTunes = new Hyperlink("Click here for instructions");
        hliTunes.setFont(new Font(14));
        hliTunes.setUnderline(true);
        hliTunes.setOnAction(c1->{
            getHostServices().showDocument("https://www.apple.com/de/itunes/download");
        });

        Hyperlink hlBackup = new Hyperlink("Click here for instructions");
        hlBackup.setFont(new Font(14));
        hlBackup.setUnderline(true);
        hlBackup.setOnAction(c1->{
            getHostServices().showDocument("https://www.wazzapmigrator.com/de/faq/wie-sie-ein-unverschl%C3%BCsseltes-itunes-backup-erhalten");
        });


        Hyperlink hlFindDirectory = new Hyperlink("Click here for instructions");
        hlFindDirectory.setFont(new Font(14));
        hlFindDirectory.setUnderline(true);
        hlFindDirectory.setOnAction(c1->{
            getHostServices().showDocument("https://support.apple.com/en-us/HT204215");
        });

        Label liTunes = new Label("Please install iTunes, if not already installed:");
        liTunes.setFont(new Font(14));
        liTunes.setTextFill(Color.WHITE);

        Label lBackup = new Label("Please make an unencrypted! IOS backup of your Iphone:");
        lBackup.setFont(new Font(14));
        lBackup.setTextFill(Color.WHITE);

        Label lFindDirectory = new Label("Locate iOS backups stored on your Mac or PC:");
        lFindDirectory.setFont(new Font(14));
        lFindDirectory.setTextFill(Color.WHITE);

        GridPane instructionLinksBox = new GridPane();
        instructionLinksBox.add(liTunes,0,0);
        instructionLinksBox.add(lBackup,0,1);
        instructionLinksBox.add(lFindDirectory,0,2);
        instructionLinksBox.add(hliTunes,1,0);
        instructionLinksBox.add(hlBackup,1,1);
        instructionLinksBox.add(hlFindDirectory,1,2);
        instructionLinksBox.setHgap(5);
        instructionLinksBox.setVgap(5);
        instructionLinksBox.setPadding(new Insets(10));

        IOSBackupBox.getChildren().addAll(instructionLinksBox,directoryBox);
    }
}