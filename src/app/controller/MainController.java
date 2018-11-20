package app.controller;

import java.net.URL;
import java.util.ResourceBundle;

import app.thread.SnakePlayerThread;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import player.snake.SnakePlayer;
import javafx.scene.control.Alert.AlertType;

public class MainController implements Initializable{
	
	@FXML
	private TextField screenXField = new TextField();
	
	@FXML
	private TextField screenYField = new TextField();
	
	@FXML
	private ComboBox<String> scaleComboBox = new ComboBox<>();

	@FXML
	private ComboBox<String> fieldSizeField = new ComboBox<>();
	
	@FXML
	private Button startButton = new Button();
	
	
	public void startOnClick() {
		if("START".equals(startButton.getText())) {
			if(parseAndSetGameParameters()) {
				runGame();
			}
		}else {
			SnakePlayerThread.setActive(false);
			startButton.setText("START");
		}
	}
	
	private boolean parseAndSetGameParameters() {
		try {
			int xOffset = Integer.parseInt(screenXField.getText());
			int yOffset = Integer.parseInt(screenYField.getText());
			int scaleFactor = Integer.parseInt(scaleComboBox.getSelectionModel().getSelectedItem());
			if(xOffset<=0 || yOffset<=0) return false;
			SnakePlayer.setScreenCaptureFactors(xOffset, yOffset, scaleFactor);
			return true;
		}catch (Exception e) {
			// TODO: handle exception
			return false;
		}
	}
	
	private void runGame() {
		try {
			int fieldSize = Integer.parseInt(fieldSizeField.getValue());
			new SnakePlayerThread(fieldSize).start();
			startButton.setText("STOP");
		}catch (Exception e) {
			new Alert(AlertType.ERROR, "Your input must be a number!").show();
		}
	}


	@Override
	public void initialize(URL location, ResourceBundle resources) {
		fieldSizeField.getItems().addAll(new String[] {"7", "9", "11", "13", "15", "17", "19"});
		scaleComboBox.getItems().addAll(new String[] {"100", "125", "150"});
		fieldSizeField.getSelectionModel().select("13");
		scaleComboBox.getSelectionModel().select("150");
	}
}
