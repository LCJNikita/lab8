package controllers;

import javafx.collections.FXCollections;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import movies.*;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static controllers.ClientGUI.bundle;

public class EditorController {

    private static Movie newbie = null;
    private static final Stage window;
    private static VBox root;
    private static Button doneBtn = new Button(bundle.getString("editor.doneBtn"));
    private static TextField titleInput = new TextField();
    private static TextField oscarsInput = new TextField();
    private static TextField boxOfInput = new TextField();
    private static TextField xCordInput = new TextField();
    private static TextField yCordInput = new TextField();
    private static CheckBox personCheckBox = new CheckBox();
    private static ComboBox<MovieGenre> genreInput = new ComboBox<>(FXCollections.observableList(Arrays.asList(MovieGenre.values())));
    private static ComboBox<MpaaRating> ratingInput = new ComboBox<>(FXCollections.observableList(Arrays.asList(MpaaRating.values())));
    private static TextField nameInput = new TextField();
    private static TextField heightInput = new TextField();
    private static TextField weightInput = new TextField();
    private static Label errorLbl = new Label();
    private static List<TextField> fields = Arrays.asList(titleInput, oscarsInput, boxOfInput, xCordInput,
            yCordInput, nameInput, heightInput, weightInput);


    static {
        window = new Stage();
        window.initModality(Modality.APPLICATION_MODAL);
        window.initStyle(StageStyle.UTILITY);
        root = new VBox();
        window.setScene(new Scene(root, 600, 450));
        root.getChildren().addAll(fields);
        root.getChildren().addAll(personCheckBox);
        root.getChildren().addAll(genreInput, ratingInput, errorLbl, doneBtn);
        root.setAlignment(Pos.CENTER);
        errorLbl.setTextFill(Color.RED);
        personCheckBox.setOnAction(e -> {
            nameInput.setVisible(personCheckBox.isSelected());
            heightInput.setVisible(personCheckBox.isSelected());
            weightInput.setVisible(personCheckBox.isSelected());
        });


    }

    private static void init(){
        titleInput.setText(null);
        titleInput.setPromptText(bundle.getString("label.movie.title"));

        oscarsInput.setText(null);
        oscarsInput.setPromptText(bundle.getString("label.movie.oscars"));

        boxOfInput.setText(null);
        boxOfInput.setPromptText(bundle.getString("label.movie.box_office"));

        xCordInput.setText(null);
        xCordInput.setPromptText(bundle.getString("label.coordinates.x"));

        yCordInput.setText(null);
        yCordInput.setPromptText(bundle.getString("label.coordinates.y"));

        genreInput.setValue(null);
        ratingInput.setValue(null);

        personCheckBox.setText(bundle.getString("label.movie.screenwriter"));

        nameInput.setText(null);
        nameInput.setPromptText(bundle.getString("label.person.name"));
        nameInput.setVisible(false);

        heightInput.setText(null);
        heightInput.setPromptText(bundle.getString("label.person.height"));
        heightInput.setVisible(false);

        weightInput.setText(null);
        weightInput.setPromptText(bundle.getString("label.person.weight"));
        weightInput.setVisible(false);
    }

    private static void checkPerson() throws IllegalStateException {
        AtomicReference<Boolean> isCorrect = new AtomicReference<>(true);
        if (personCheckBox.isSelected()) {
            Arrays.asList(nameInput, heightInput, weightInput).forEach(f -> {
                if (f.getText() == null || f.getText().isEmpty()) {
                    f.setBorder(Borders.RED_BORDER);
                    isCorrect.set(false);
                } else f.setBorder(null);
            });
            if (!isCorrect.get()) {
                throw new IllegalStateException(bundle.getString("error.validate.not_empty"));
            }

            Arrays.asList(heightInput).forEach(f -> {
                try {
                    Long.parseLong(f.getText());
                    f.setBorder(null);
                } catch (NumberFormatException e) {
                    f.setBorder(Borders.RED_BORDER);
                    isCorrect.set(false);
                }
            });
            if (!isCorrect.get()) {
                throw new IllegalStateException(bundle.getString("error.validate.integer"));
            }

            Arrays.asList(weightInput).forEach(f -> {
                try {
                    Float.parseFloat(f.getText());
                    f.setBorder(null);
                } catch (NumberFormatException e) {
                    f.setBorder(Borders.RED_BORDER);
                    isCorrect.set(false);
                }
            });
            if (!isCorrect.get()) {
                throw new IllegalStateException(bundle.getString("error.validate.decimal"));
            }

            Arrays.asList(weightInput, heightInput).forEach(f -> {
                if (Float.parseFloat(f.getText()) <= 0) {
                    f.setBorder(Borders.RED_BORDER);
                    isCorrect.set(false);
                } else f.setBorder(null);
            });
            if (!isCorrect.get()) {
                throw new IllegalStateException(bundle.getString("error.validate.positive"));
            }
        }
    }

    private static void checkColumns() throws IllegalStateException {
        AtomicReference<Boolean> isCorrect = new AtomicReference<>(true);
        Arrays.asList(titleInput, oscarsInput, boxOfInput, xCordInput, yCordInput).forEach(f -> {
            if (f.getText() == null || f.getText().isEmpty()) {
                f.setBorder(Borders.RED_BORDER);
                isCorrect.set(false);
            } else f.setBorder(null);
        });
        if (!isCorrect.get()) {
            throw new IllegalStateException(bundle.getString("error.validate.not_empty"));
        }

        Arrays.asList(oscarsInput, boxOfInput, xCordInput).forEach(f -> {
            try {
                Long.parseLong(f.getText());
                f.setBorder(null);
            } catch (NumberFormatException e) {
                f.setBorder(Borders.RED_BORDER);
                isCorrect.set(false);
            }
        });
        if (!isCorrect.get()) {
            throw new IllegalStateException(bundle.getString("error.validate.integer"));
        }

        Arrays.asList(yCordInput).forEach(f -> {
            try {
                Float.parseFloat(f.getText());
                f.setBorder(null);
            } catch (NumberFormatException e) {
                f.setBorder(Borders.RED_BORDER);
                isCorrect.set(false);
            }
        });
        if (!isCorrect.get()) {
            throw new IllegalStateException(bundle.getString("error.validate.decimal"));
        }

        Arrays.asList(oscarsInput, boxOfInput, xCordInput, yCordInput).forEach(f -> {
            if (Float.parseFloat(f.getText()) <= 0) {
                f.setBorder(Borders.RED_BORDER);
                isCorrect.set(false);
            } else f.setBorder(null);
        });
        if (!isCorrect.get()) {
            throw new IllegalStateException(bundle.getString("error.validate.positive"));
        }

        if (Float.parseFloat(yCordInput.getText()) > 623) {
            yCordInput.setEffect(Lights.RED_LIGHT);
            throw new IllegalStateException(bundle.getString("error.validate.max_623"));
        } else yCordInput.setEffect(null);

        checkPerson();
    }

    public static void edit(Movie m) {
        init();
        errorLbl.setText(null);
        window.setTitle(bundle.getString("editItem.btn"));
        titleInput.setText(m.getName());
        oscarsInput.setText(String.valueOf(m.getOscarsCount()));
        boxOfInput.setText(String.valueOf(m.getTotalBoxOffice()));
        xCordInput.setText(String.valueOf(m.getCoordinates().getX()));
        yCordInput.setText(String.valueOf(m.getCoordinates().getY()));
        genreInput.setValue(m.getGenre());
        ratingInput.setValue(m.getMpaaRating());
        if (m.getScreenwriter() == null) {
            nameInput.setVisible(false);
            heightInput.setVisible(false);
            weightInput.setVisible(false);
        } else {
            nameInput.setVisible(true);
            heightInput.setVisible(true);
            weightInput.setVisible(true);
            personCheckBox.setSelected(true);
            nameInput.setText(m.getScreenwriter().getName());
            heightInput.setText((String.valueOf(m.getScreenwriter().getHeight())));
            weightInput.setText(String.valueOf(m.getScreenwriter().getWeight()));
        }
        try {
            doneBtn.setOnMouseClicked(event -> {
                checkColumns();
                Person person = null;
                if (personCheckBox.isSelected()) {
                    person = new Person(
                            nameInput.getText(),
                            Long.parseLong(heightInput.getText()),
                            Float.parseFloat(weightInput.getText()));
                }

                m.setName(titleInput.getText());
                m.setOscarsCount(Long.parseLong(oscarsInput.getText()));
                m.setTotalBoxOffice(Long.parseLong(boxOfInput.getText()));
                m.setCoordinates(new Coordinates(Integer.parseInt(xCordInput.getText()), Float.parseFloat(yCordInput.getText())));
                m.setGenre(genreInput.getValue());
                m.setMpaaRating(ratingInput.getValue());
                m.setScreenwriter(person);
                window.close();
            });
            window.showAndWait();
        } catch (Exception e) {
            errorLbl.setText(e.getMessage());
        }
    }

    public static Movie create() {
        init();
        errorLbl.setText(null);

        window.setTitle(bundle.getString("addItem.btn"));


        doneBtn.setOnMouseClicked(event -> {
            try {
                Person person = null;
                if (personCheckBox.isSelected()) {
                    Arrays.asList(nameInput, heightInput, weightInput).forEach(f -> {
                        if (f.getText() == null || f.getText().isEmpty())
                            throw new IllegalStateException(bundle.getString("editor.screenwriter.error"));
                    });
                    person = new Person(
                            nameInput.getText(),
                            Long.parseLong(heightInput.getText()),
                            Long.parseLong(weightInput.getText()));
                }
                checkColumns();
                newbie = new Movie(
                        titleInput.getText(),
                        new Coordinates(Integer.parseInt(xCordInput.getText()), Float.parseFloat(yCordInput.getText())),
                        Long.parseLong(oscarsInput.getText()),
                        Long.parseLong(boxOfInput.getText()),
                        genreInput.getValue(),
                        ratingInput.getValue(),
                        person);

                window.close();
            } catch (Exception e) {
                errorLbl.setText(e.getMessage());
            }
        });
        window.showAndWait();
        return newbie;
    }

}