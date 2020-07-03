package controllers;

import exceptions.LogOutException;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.effect.BoxBlur;
import javafx.scene.effect.Effect;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Locale;
import java.util.ResourceBundle;

public class ClientGUI extends Application {

    public static ResourceBundle bundle = ResourceBundle.getBundle("resources/locales.Locale", Locale.getDefault());
    private final static Effect blur = new BoxBlur(40, 40, 10);
    private final static ImageView background = new ImageView();
    private final static Pane layout = new Pane();
    static Stage stage;

    public static void main(String[] args) {
        Application.launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/interfaces/login.fxml"), bundle);
        Parent root = loader.load();
        stage = primaryStage;
        layout.getChildren().setAll(background, root);
        layout.setStyle("-fx-background-color: null");
        Platform.setImplicitExit(false);
        makeSmoke(primaryStage);
        primaryStage.setTitle("MovieClient");
        primaryStage.initStyle(StageStyle.TRANSPARENT);
        primaryStage.setScene(new Scene(layout, Color.TRANSPARENT));
        stage.getIcons().add(new Image("/resources/icons/window.png"));
        primaryStage.setResizable(false);
        primaryStage.show();
        background.setImage(redrawBackground(stage));
        background.setEffect(blur);
        makeDraggable(primaryStage, layout);
    }

    private void makeDraggable(Stage stage, Node byNode) {
        final SimpleDoubleProperty x = new SimpleDoubleProperty(0);
        final SimpleDoubleProperty y = new SimpleDoubleProperty(0);
        byNode.setOnMousePressed(event -> {
            x.set(stage.getX() - event.getScreenX());
            y.set(stage.getY() - event.getSceneY());
        });
        final SimpleBooleanProperty inDrag = new SimpleBooleanProperty(false);
        byNode.setOnMouseReleased(event -> {
            if (inDrag.get()) {
                stage.hide();
                Timeline pause = new Timeline(new KeyFrame(Duration.millis(5), new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        background.setImage(redrawBackground(stage));
                        layout.getChildren().set(0, background);
                        stage.show();
                    }
                }));
                pause.play();
            }
            inDrag.set(false);
        });
        byNode.setOnMouseDragged(event -> {
            stage.setX(event.getScreenX() + x.get());
            stage.setY(event.getSceneY() + y.get());
            layout.getChildren().set(0, makeSmoke(stage));
            inDrag.set(true);
        });
    }

    private WritableImage redrawBackground(Stage stage) {
        try {
            BufferedImage image = new Robot().createScreenCapture(
                    new java.awt.Rectangle((int) stage.getX(), (int) stage.getY(),
                            (int) stage.getWidth(), (int) stage.getHeight()));
            return SwingFXUtils.toFXImage(image, null);
        } catch (AWTException e) {
            return null; /* никогда не будет выброшено */
        }
    }

    private Rectangle makeSmoke(Stage stage) {
        return new Rectangle(stage.getWidth(), stage.getHeight(),
                Color.WHITESMOKE.deriveColor(0, 1, 1, 0.08));
    }

    public static void setBundle(ResourceBundle b){
        bundle = b;
    }
}
