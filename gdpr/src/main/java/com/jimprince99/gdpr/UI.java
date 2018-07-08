package com.jimprince99.gdpr;

import java.io.File;
import java.io.IOException;
import java.util.function.Predicate;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class UI extends Application {
	private static FileHandler fileTxt;
	private static SimpleFormatter formatterTxt;

	private String logLevel = "none";
	private Boolean partialConversion = true;
	private String sourceFilename = "";
	private static File file = null;
	TextField sourceField = null;
	private String destFilename = "";
	TextField destField = null;
	FileChooser fileChooser = null;
	Stage primaryStage = null;
	private static Logger logger = null;
	Label errorLabel1 = null;
	TextField errorLabel2 = null;

	public static void main(String[] args) {

		Application.launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		primaryStage.setTitle("Hello World!");
		Integer width = 800;
		Integer hight = 200;
		getParameters().getUnnamed();
		getParameters().getRaw();

		GridPane gridPane = new GridPane();
		gridPane.setHgap(20);
		gridPane.setVgap(20);

		Label sourceLabel = new Label("Source file");
		sourceField = new TextField("source filename");
		sourceField.autosize();
		fileChooser = new FileChooser();
		final Button browseSourceButton = new Button("Browse files");
		browseSourceButton.setOnAction(browseSourceButtonAction);

		Label destLabel = new Label("Destination file");
		destField = new TextField("destination filename");
		destField.setEditable(false);

		ComboBox<String> loggingCombo = new ComboBox<String>();
		loggingCombo.getItems().addAll("None", "Normal", "Full");
		loggingCombo.setValue("None");
		loggingCombo.valueProperty().addListener(loggingComboListener);

		Label loggingLabel = new Label("Logging level");

		CheckBox partialCheckBox = new CheckBox();
		partialCheckBox.setSelected(true);
		partialCheckBox.setText("Partial Anonimisation");
		partialCheckBox.addEventFilter(javafx.scene.input.MouseEvent.MOUSE_CLICKED, partialCheckBoxMousePress);

		Button convertButton = new Button("Convert");
		convertButton.addEventFilter(javafx.scene.input.MouseEvent.MOUSE_CLICKED, convertButtonMousePress);
				
		Label errorLabel1 = new Label("Result:");
		errorLabel2 = new TextField("  ");
		errorLabel2.setMinWidth(20);
		errorLabel2.autosize();



		GridPane.setConstraints(sourceLabel, 1, 1);
		GridPane.setConstraints(sourceField, 2, 1, 3, 1);
		GridPane.setConstraints(browseSourceButton, 5, 1);
		GridPane.setConstraints(destLabel, 1, 2);
		GridPane.setConstraints(destField, 2, 2, 3, 1);
		GridPane.setConstraints(loggingLabel, 1, 3);
		GridPane.setConstraints(loggingCombo, 2, 3);
		GridPane.setConstraints(partialCheckBox, 3, 3);
		GridPane.setConstraints(convertButton, 5, 2);
		GridPane.setConstraints(errorLabel1, 1, 4);
		GridPane.setConstraints(errorLabel2, 2, 4, 3, 1);
		gridPane.setGridLinesVisible(false);

		gridPane.getChildren().addAll(sourceLabel, sourceField, browseSourceButton, destLabel, destField, loggingCombo,
				loggingLabel, partialCheckBox, convertButton, errorLabel1, errorLabel2);
		Scene scene = new Scene(gridPane, width, hight);
		// scene.getStylesheets().add("myStyle.css");

		primaryStage.setScene(scene);
		primaryStage.setTitle("GDPR");
		primaryStage.setResizable(true);

		primaryStage.show();
	}

	// Handling the mouse clicked event(on box)
	EventHandler<javafx.scene.input.MouseEvent> partialCheckBoxMousePress = new EventHandler<javafx.scene.input.MouseEvent>() {

		public void handle(javafx.scene.input.MouseEvent e) {
			partialConversion = !partialConversion;
		}
	};

	// Handling the mouse clicked event(on box)
	EventHandler<javafx.scene.input.MouseEvent> convertButtonMousePress = new EventHandler<javafx.scene.input.MouseEvent>() {

		public void handle(javafx.scene.input.MouseEvent e) {
			// call conversion method
	        getLogger();

			logger.info("calling gdpr with sourceFilename=" + sourceFilename
					+ "partialConversion=" + partialConversion);
			GdprWorker gdpr = new GdprWorker(sourceFilename, logger, partialConversion);

			Thread gdrpThread = new Thread(gdpr, "gdpr1");
			gdrpThread.start();
			
			try {
				gdrpThread.join();
			} catch (InterruptedException e2) {
			}
			
			String result = gdpr.getResultString();

			if (isEmpty.test(result)) {
				errorLabel2.setText("Success");
			} else {
				errorLabel2.setText(result);
				errorLabel2.prefColumnCountProperty().bind(errorLabel2.textProperty().length());
			}
			
		}
	};

	ChangeListener<String> loggingComboListener = new ChangeListener<String>() {
		public void changed(ObservableValue ov, String t, String t1) {
			logLevel = t1;
			
			switch (logLevel) {
			case "Normal":
		        logger.setLevel(Level.WARNING);
				break;
				
			case "Full":
		        logger.setLevel(Level.INFO);
				break;
				
				default: logger.setLevel(Level.OFF);
			}
		}
	};

	EventHandler<ActionEvent> browseSourceButtonAction = new EventHandler<ActionEvent>() {
		public void handle(final ActionEvent e) {
			file = fileChooser.showOpenDialog(primaryStage);

			sourceFilename = file.getAbsolutePath();
			destFilename = getDestFilenamesourceFilename(file);
			sourceField.setText(sourceFilename);
			sourceField.prefColumnCountProperty().bind(sourceField.textProperty().length());

			destField.setText(destFilename);
			destField.prefColumnCountProperty().bind(destField.textProperty().length());
		}
	};

	protected String getDestFilenamesourceFilename(File file) {
		String path = file.getParentFile().getPath();
		String filename = file.getName();

		String[] parts = filename.split("\\.");
		if (parts.length == 2) {
			String newFilename = parts[0] + "_gdpr." + parts[1];
			return path + File.separator + newFilename;
		} else {
			return path + File.separator + filename + "_gdpr";
		}
	}
	
	private static void getLogger() {
		logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
	    logger.setLevel(Level.OFF);

		String path = null;
		try {
			path = file.getParentFile().getPath();
		} catch (NullPointerException e) 
		{
			path = System.getProperty("user.dir");;
		}
        try {
			fileTxt = new FileHandler(path + File.separator + "gdpr.log");
		} catch (SecurityException e) {
			System.err.println("SecurityException: Unable to open logger");
		} catch (IOException e) {
			System.err.println("IOException: Unable to open logger");
		}
        Handler[] handlers = logger.getHandlers();
        for(Handler handler : handlers) {
            logger.removeHandler(handler);
        }

        formatterTxt = new SimpleFormatter();
        fileTxt.setFormatter(formatterTxt);
        logger.addHandler(fileTxt);
	}

	Predicate<String> isEmpty = p -> {
		return ((p == null) || (p.equals("")) && (p.length() < 3));
	};
}
