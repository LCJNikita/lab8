package controllers;

import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import javafx.util.Duration;
import javafx.util.Pair;
import managers.ConnectionManager;
import movies.Movie;
import movies.MovieGenre;
import movies.MpaaRating;
import server.Response;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class ViewerController implements Initializable {

    @FXML
    private Label userInfoLbl;
    @FXML
    private Label colInfoLbl;
    @FXML
    private Label errorMsg;
    @FXML
    private Button addBtn;
    @FXML
    private Button editBtn;
    @FXML
    private Button delBtn;
    @FXML
    private Button updBtn;
    @FXML
    private TableView<Movie> moviesTbl;
    @FXML
    private TableColumn<Movie, Long> idCol;
    @FXML
    private TableColumn<Movie, String> nameCol;
    @FXML
    private TableColumn<Movie, String> cordsCol;
    @FXML
    private TableColumn<Movie, LocalDate> dateCol;
    @FXML
    private TableColumn<Movie, Long> oscarsCol;
    @FXML
    private TableColumn<Movie, Long> totalBoxOfficeCol;
    @FXML
    private TableColumn<Movie, String> genreCol;
    @FXML
    private TableColumn<Movie, String> mpaaRatingCol;
    @FXML
    private TableColumn<Movie, String> screenwriterCol;
    @FXML
    private ComboBox<MpaaRating> filterInput;
    @FXML
    private Pane canvasField;
    private ArrayList<Pair<Long, Circle>> circles = new ArrayList<>();
    private ConnectionManager conMan = ConnectionManager.getInstance();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        userInfoLbl.setText(ConnectionManager.getUser().getName());
        filterInput.setItems(FXCollections.observableList(Arrays.asList(MpaaRating.values())));
        try {
            colInfoLbl.setText(conMan.execute("info").getText());
        } catch (Exception e) {
            showNotification(Lights.RED_LIGHT, e.getLocalizedMessage());
        }
        Arrays.asList(addBtn, editBtn, delBtn, updBtn).forEach(el ->
                el.focusedProperty().addListener((obs, oldVal, newVal) ->
                        el.setEffect(newVal ? Lights.BLUE_LIGHT : null))
        );
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        cordsCol.setCellValueFactory(new PropertyValueFactory<>("coordinates"));
        dateCol.setCellValueFactory(new PropertyValueFactory<>("creationDate"));
        oscarsCol.setCellValueFactory(new PropertyValueFactory<>("oscarsCount"));
        totalBoxOfficeCol.setCellValueFactory(new PropertyValueFactory<>("totalBoxOffice"));
        genreCol.setCellValueFactory(new PropertyValueFactory<>("genre"));
        mpaaRatingCol.setCellValueFactory(new PropertyValueFactory<>("mpaaRating"));
        screenwriterCol.setCellValueFactory(new PropertyValueFactory<>("screenwriter"));
        update();
    }

    private Color getColorByGenre(MovieGenre genre) {
        if (genre == null) {
            return Color.HOTPINK;
        }
        switch (genre) {
            case ACTION: {
                return Color.ORANGE;
            }
            case HORROR: {
                return Color.DARKRED;
            }
            case COMEDY: {
                return Color.YELLOW;
            }
            case ADVENTURE: {
                return Color.DARKGREEN;
            }
            case SCIENCE_FICTION: {
                return Color.NAVY;
            }
            default: {
                return Color.WHITESMOKE;
            }
        }
    }

    private Circle createCircle(Movie m) {
        int r = (int) m.getOscarsCount() * 10;
        Circle circle = new Circle();
        circle.setStroke(null);
        circle.setFill(getColorByGenre(m.getGenre()));

        circle.setRadius(r);
        canvasField.getChildren().addAll(circle);

        TranslateTransition translate = new TranslateTransition(Duration.millis(1000), circle);
        translate.setToX(m.getCoordinates().getX());
        translate.setToY(m.getCoordinates().getY());
        translate.play();
        circle.setOnMouseClicked(e -> editMovie(m));
        return circle;
    }

    void editMovie(Movie m) {
        try {
            EditorController.edit(m);
            showNotification(Lights.GREEN_LIGHT, conMan.execute("update " + m.toJSONObject().toString()).getText());
            update();
        } catch (Exception e) {
            showNotification(Lights.RED_LIGHT, e.getLocalizedMessage());
        }
    }

    private void updateCircleByMovies(List<Movie> movies) {
        try {
            System.out.println();
            circles.forEach(c -> {
                if (movies.stream().noneMatch(m -> m.getId() == c.getKey())) {
                    ScaleTransition scale = new ScaleTransition(Duration.millis(500), c.getValue());
                    scale.setToX(0);
                    scale.setToY(0);
                    scale.play();
                }
            });
            circles.removeIf(c -> movies.stream().noneMatch(m -> m.getId() == c.getKey()));
            movies.forEach(m -> {
                if (circles.stream().noneMatch(p -> p.getKey() == m.getId())) {
                    circles.add(new Pair<Long, Circle>(m.getId(), createCircle(m)));
                } else {
                    Optional<Pair<Long, Circle>> p = circles.stream().filter(cir -> cir.getKey() == m.getId()).findFirst();
                    if (p.isPresent()) {
                        Circle c = p.get().getValue();

                        c.setFill(getColorByGenre(m.getGenre()));

                        TranslateTransition translate = new TranslateTransition(Duration.millis(1000), c);
                        translate.setToX(m.getCoordinates().getX());
                        translate.setToY(m.getCoordinates().getY());
                        translate.play();

                        double sc = (m.getOscarsCount() * 10) / c.getRadius();

                        ScaleTransition scale = new ScaleTransition(Duration.millis(1000), c);
                        scale.setToX(sc);
                        scale.setToY(sc);
                        scale.play();
                    }
                }
            });
        } catch (Exception e) {
            System.err.println(e);
        }
    }


    @FXML
    private void update() {
        try {
            colInfoLbl.setText(conMan.execute("info").getText());
            Response r = conMan.execute("show");
            List<Movie> movies = r.getMovies();
            moviesTbl.setItems(FXCollections.observableList(movies));
            updateCircleByMovies(movies);
//            showNotification(Lights.GREEN_LIGHT, r.getText());
        } catch (Exception e) {
            showNotification(Lights.RED_LIGHT, e.getLocalizedMessage());
        }
    }

    @FXML
    private void showMaxByOscarsCount() {
        try {
            List<Movie> movies = conMan.execute("show").getMovies();
            if (movies.isEmpty()) {
                showNotification(Lights.GREEN_LIGHT, "Empty");
            } else {
                Movie maxMovie = movies.stream().max(Movie::compareOscarsCountTo).get();
                moviesTbl.getItems().clear();
                moviesTbl.getItems().add(maxMovie);
                updateCircleByMovies(movies);
            }
        } catch (Exception e) {
            showNotification(Lights.RED_LIGHT, e.getLocalizedMessage());
        }
    }

    @FXML
    void clear() {
        try {
            showNotification(Lights.GREEN_LIGHT, conMan.execute("clear").getText());
            update();
        } catch (Exception e) {
            showNotification(Lights.RED_LIGHT, e.getLocalizedMessage());
        }
    }

    private Movie getSelectedMovieFromTable() throws Exception {
        Movie m = moviesTbl.getSelectionModel().getSelectedItem();
        if (m != null) return m;
        else throw new Exception("Movie not selected");
    }

    @FXML
    void edit() {
        try {
            Movie m = getSelectedMovieFromTable();
            EditorController.edit(m);
            showNotification(Lights.GREEN_LIGHT, conMan.execute("update " + m.toJSONObject().toString()).getText());
            update();
        } catch (Exception e) {
            showNotification(Lights.RED_LIGHT, e.getLocalizedMessage());
        }
    }



    @FXML
    void add() {
        try {
            Movie m = EditorController.create();
            showNotification(Lights.GREEN_LIGHT, conMan.execute("add " + m.toJSONObject().toString()).getText());
            update();
        } catch (Exception e) {
            showNotification(Lights.RED_LIGHT, e.getLocalizedMessage());
        }
    }

    @FXML
    void remove() {
        try {
            showNotification(Lights.GREEN_LIGHT, conMan.execute("remove_by_id " + getSelectedMovieFromTable().getId()).getText());
            update();
        } catch (Exception e) {
            showNotification(Lights.RED_LIGHT, e.getLocalizedMessage());
        }
    }

    @FXML
    private void executeScript() {
        try {
            List<String> lines = Files.readAllLines(
                    Paths.get(new FileChooser().showOpenDialog(errorMsg.getScene().getWindow()).getAbsolutePath()));
            for (String l : lines) conMan.execute(l);
        } catch (Exception e) {
            showNotification(Lights.RED_LIGHT, e.getLocalizedMessage());
        }
    }

    @FXML
    private void showNotification(DropShadow lightning, String msg) {
        errorMsg.setText(msg);
        errorMsg.setEffect(lightning);
    }

    @FXML
    private void history() {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("History");
        alert.setHeaderText(null);
        alert.setContentText(ConnectionManager.getParser().getCommandsHistory().toString());
        alert.showAndWait();
    }

    @FXML
    private void filter() {
        try {
            List<Movie> movies = conMan.execute("show").getMovies().
                    stream().
                    filter(m -> m.getMpaaRating() == null || m.getMpaaRating().compareTo(filterInput.getValue()) < 0).
                    collect(Collectors.toList());
            moviesTbl.getItems().clear();
            moviesTbl.getItems().addAll(movies);
            updateCircleByMovies(movies);
        } catch (Exception e) {
            showNotification(Lights.RED_LIGHT, e.getLocalizedMessage());
        }
    }
}