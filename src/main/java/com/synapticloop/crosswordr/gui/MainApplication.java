package com.synapticloop.crosswordr.gui;

import com.synapticloop.crosswordr.util.Settings;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.util.Objects;

public class MainApplication extends Application {
	private double xOffset = 0;
	private double yOffset = 0;

	@Override
	public void start(Stage stage) throws IOException {
		Settings.loadSettings();

		FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("/gui/main.fxml"));
		Scene scene = new Scene(fxmlLoader.load());
		MainController mainController = fxmlLoader.getController();
		mainController.setStage(stage);
		AnchorPane anchorPane = (AnchorPane) scene.lookup("#root");
		anchorPane.setOnMousePressed(event -> {
			xOffset = event.getSceneX();
			yOffset = event.getSceneY();
		});
		anchorPane.setOnMouseDragged(event -> {
			stage.setX(event.getScreenX() - xOffset);
			stage.setY(event.getScreenY() - yOffset);
		});

		boolean darkmode = Settings.getBooleanSetting(MainApplication.class, "darkmode", false);
		mainController.setDarkmode(darkmode);

		stage.setTitle("Crosswordr");
		stage.initStyle(StageStyle.TRANSPARENT);
		stage.getIcons().add(new Image(Objects.requireNonNull(MainApplication.class.getResourceAsStream("/gui/images/layout-grid.png"))));
		stage.setScene(scene);
		stage.show();

		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			Settings.saveSettings();
			Platform.exit();
		}));

		stage.setOnCloseRequest(event -> {
			Settings.saveSettings();
			Platform.exit();
			event.consume();
			System.exit(0);
		});
	}

	public static void main(String[] args) {
		launch();
	}
}