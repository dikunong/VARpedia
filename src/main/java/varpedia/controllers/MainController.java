package varpedia.controllers;

import javafx.beans.binding.Bindings;
import javafx.beans.value.ObservableValueBase;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableColumn.SortType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.StringConverter;
import varpedia.helpers.AlertHelper;
import varpedia.models.Creation;
import varpedia.helpers.SafeExitHelper;
import varpedia.helpers.ThemeHelper;
import varpedia.VARpediaApp;
import varpedia.tasks.ClearTask;
import varpedia.tasks.ListPopulateTask;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.URISyntaxException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;

/**
 * Controller for the MainScreen, which displays current creations that can be played and deleted, as well as acting as
 * the gateway to making new creations.
 *
 * @author Di Kun Ong and Tudor Zagreanu
 */
public class MainController extends Controller {

    @FXML
    private Button playBtn;
    @FXML
    private Button deleteBtn;
    @FXML
    private Button createBtn;
    @FXML
    private ChoiceBox<TableColumn<Creation, ?>> sortChoiceBox;
    @FXML
    private Label themeLabel;
    @FXML
    private ToggleButton themeBtn;

    @FXML
    private final ObservableList<Creation> creationList = FXCollections.observableArrayList();
    @FXML
    private TableView<Creation> creationTableView;
    @FXML
    private TableColumn<Creation, Image> creationThumbCol;
    @FXML
    private TableColumn<Creation, String> creationNameCol;
    @FXML
    private TableColumn<Creation, String> creationConfCol;
    @FXML
    private TableColumn<Creation, String> creationViewCol;

    private ExecutorService pool = VARpediaApp.newTimedCachedThreadPool();
    private AlertHelper _alertHelper = AlertHelper.getInstance();
    private ThemeHelper _themeHelper = ThemeHelper.getInstance();
    private SafeExitHelper _safeExitHelper = SafeExitHelper.getInstance();

    @FXML
    private void initialize() {
    	// set up table view columns and sorting
        initializeColumns();
        initializeSort();

        // populate table view with saved creations
    	populateTable();
        deleteAppfiles();

        // set up the theme switcher
        initializeThemes();
        
        // when entering the MainController it is safe to exit (all the unsaved progress is deleted anyway)
        _safeExitHelper.setSafeToExit(true);

        // disable the TableView if there are no creations
        creationTableView.disableProperty().bind(Bindings.size(creationList).isEqualTo(0));

        // disable play and delete buttons until a creation is selected
        playBtn.disableProperty().bind(creationTableView.getSelectionModel().selectedItemProperty().isNull());
        deleteBtn.disableProperty().bind(creationTableView.getSelectionModel().selectedItemProperty().isNull());
    }

    @FXML
    private void pressPlayButton(ActionEvent event) {
        // store a creation's name and rating and open PlaybackScreen
        sendDataToFile(getSelectedName(), "playback-name.txt");
        sendDataToFile(creationTableView.getSelectionModel().getSelectedItem().getConfidence() + "", "playback-rating.txt");
        changeScene(event, "/varpedia/fxml/PlaybackScreen.fxml");
    }

    @FXML
    private void pressDeleteButton(ActionEvent event) {
        // ask for confirmation
        _alertHelper.showAlert(Alert.AlertType.WARNING, "Confirm delete",
                "Are you sure you want to delete the " + getSelectedName() + " creation?",
                ButtonType.YES, ButtonType.CANCEL);

        if (_alertHelper.getResult() == ButtonType.YES) {
            // delete creation files
            File file = new File("creations/" + getSelectedName() + ".mp4");
            File file2 = new File("creations/" + getSelectedName() + ".dat");
            File file3 = new File("creations/" + getSelectedName() + ".jpg");
            if (file.delete() && (!file2.exists() || file2.delete()) && (!file3.exists() || file3.delete())) {
                // update table view
                creationList.remove(creationTableView.getSelectionModel().getSelectedItem());
            } else {
                _alertHelper.showAlert(Alert.AlertType.ERROR, "Error", "Could not delete creation.");
            }
        }
    }

    @FXML
    private void pressCreateButton(ActionEvent event) {
        // it is unsafe to exit when creating a creation
    	_safeExitHelper.setSafeToExit(false);
        // open WikitSearchScreen
        changeScene(event, "/varpedia/fxml/WikitSearchScreen.fxml");
    }

    /**
     * Helper method that retrieves a filename of a selected creation.
     * @return Creation filename
     */
    private String getSelectedName() {
        return creationTableView.getSelectionModel().getSelectedItem().getCreationName();
    }

    /**
     * Helper method that retrieves the observable list for the FXML component.
     * @return observable list of Creations
     */
    public ObservableList<Creation> getCreationList() {
        return creationList;
    }

    /**
     * Helper method that initialises the values of each column in the TableView, and how they should parse
     * the serialized data in a way that will become user-friendly output.
     */
    private void initializeColumns() {
        creationThumbCol.setCellValueFactory((CellDataFeatures<Creation, Image> p) -> {
            return new ObservableValueBase<Image>(){
                public Image getValue() {
                    return p.getValue().getImage();
                }
            };
        });
        
        creationThumbCol.setCellFactory((TableColumn<Creation, Image> col) -> new TableCell<Creation, Image>() {
            private ImageView imageView = new ImageView();

            @Override
            public void updateItem(Image item, boolean empty) {
                super.updateItem(item, empty);

                // get the thumbnail image
                if (empty) {
                	// don't set any image
                    setText(null);
                    setGraphic(null);
                    return;
                } else if (item == null) {
                    // if there is no thumbnail, load a default icon
                    // get the correct icon based on the current theme
                    String defaultIcon = "/varpedia/images/light-theme-icons/movie_black.png";
                    if (_themeHelper.getDarkModeStatus()) {
                        defaultIcon = "/varpedia/images/dark-theme-icons/movie_white.png";
                    }

                    try {
                        defaultIcon = getClass().getResource(defaultIcon).toURI().toString();
                        imageView.setImage(new Image(defaultIcon));
                    } catch (URISyntaxException e) {        // this should never happen
                        setText(null);
                        setGraphic(null);
                        return;
                    }
                } else {
                	imageView.setImage(item);
                }

                imageView.setPreserveRatio(true);
                imageView.setFitHeight(70);
                imageView.fitWidthProperty().bind(creationThumbCol.widthProperty().subtract(16));
                setText(null);
                setGraphic(imageView);
            }	
        });
    	
    	creationNameCol.setCellValueFactory((CellDataFeatures<Creation, String> p) -> {
            return new ObservableValueBase<String>(){
                public String getValue() {
                    return p.getValue().getCreationName();
                }
            };
        });

        creationConfCol.setCellValueFactory((CellDataFeatures<Creation, String> p) -> {
            return new ObservableValueBase<String>(){
                public String getValue() {
                    int conf = p.getValue().getConfidence();

                    if (conf == -1) {
                        return "Unrated";
                    } else {
                        return conf + "/5";
                    }
                }
            };
        });

        creationViewCol.setCellValueFactory((CellDataFeatures<Creation, String> p) -> {
            return new ObservableValueBase<String>(){
                public String getValue() {
                    // formatter to parse the Instant into a user-readable format
                    // ISO date format to ensure sorting works correctly
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm a")
                            .withLocale(Locale.UK)
                            .withZone(ZoneId.systemDefault());
                    Instant conf = p.getValue().getLastViewed();

                    if (conf == null) {
                        return "Unwatched";
                    } else {
                        return formatter.format(conf);
                    }
                }
            };
        });
    }

    /**
     * Helper method that initializes the sorting controls for the active learning component,
     * as well as how each sorting mode should behave.
     */
    @SuppressWarnings("unchecked")
    private void initializeSort() {
        sortChoiceBox.setConverter(new StringConverter<TableColumn<Creation,?>>(){
            @Override
            public TableColumn<Creation, ?> fromString(String arg0) {
                if (arg0.equals("Name")) {
                    return creationNameCol;
                } else if (arg0.equals("Confidence")) {
                    return creationConfCol;
                } else {
                    return creationViewCol;
                }
            }

            @Override
            public String toString(TableColumn<Creation, ?> arg0) {
                if (arg0 == creationNameCol) {
                    return "Name";
                } else if (arg0 == creationConfCol) {
                    return "Confidence";
                } else {
                    return "Last viewed";
                }
            }
        });

        creationThumbCol.setSortable(false);

        sortChoiceBox.getItems().add(creationNameCol);
        sortChoiceBox.getItems().add(creationConfCol);
        sortChoiceBox.getItems().add(creationViewCol);

        sortChoiceBox.setOnAction((event) -> {
            TableColumn<Creation, ?> main = sortChoiceBox.getSelectionModel().getSelectedItem();
            TableColumn<Creation, ?> second = null;

            // if sorting by confidence or last viewed first, set the other attribute as second sort
            // for if there's need of a tiebreaker
            if (main == creationConfCol) {
                second = creationViewCol;
            } else if (main == creationViewCol) {
                second = creationConfCol;
            }

            main.setSortType(SortType.ASCENDING);

            if (second != null) {
                second.setSortType(SortType.ASCENDING);
                creationTableView.getSortOrder().setAll(main, second);
            } else {
                creationTableView.getSortOrder().setAll(main);
            }

            creationTableView.sort();
        });

        // sort by Name by default
        sortChoiceBox.getSelectionModel().select(0);
    }

    /**
     * Helper method that runs a task to populate the creationList with chunks in the creations directory.
     */
    private void populateTable() {
        Task<List<String>> task = new ListPopulateTask(new File("creations"), ".mp4");
        task.setOnSucceeded(event -> {
            try {
                List<String> newCreations = task.get();
                if (newCreations != null) {
                    for (String s : newCreations) {
                    	File creationFile = new File("creations/" + s + ".dat");
                    	
                    	if (creationFile.exists()) {
	                    	try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(creationFile))) {
	                    		creationList.add((Creation) ois.readObject());
	                        } catch (IOException | ClassNotFoundException e) {
								e.printStackTrace();
							}
                    	} else {
                    	    creationList.add(new Creation(s, -1, null));
                        }
                    }
                }
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        });
        pool.submit(task);
    }

    //TODO: This is running in the GUI thread to avoid race conditions. Could move it out.
    private void deleteAppfiles() {
    	Task<Void> task = new ClearTask(new File("appfiles"));
    	task.run();
    }

    /**
     * Helper method that initializes the theme switching functionality.
     */
    private void initializeThemes() {
        // enable toggling between light and dark themes
        themeBtn.setOnAction(e -> {
            if (themeBtn.isSelected()) {
                themeLabel.setText("Theme: Dark mode");
                Scene currentScene = ((Node) e.getSource()).getScene();
                currentScene.getStylesheets().add(getClass().getResource("/varpedia/styles/theme-dark.css").toString());
                currentScene.getStylesheets().remove(getClass().getResource("/varpedia/styles/theme-light.css").toString());
                _themeHelper.setDarkModeStatus(true);
            } else {
                themeLabel.setText("Theme: Light mode");
                Scene currentScene = ((Node) e.getSource()).getScene();
                currentScene.getStylesheets().add(getClass().getResource("/varpedia/styles/theme-light.css").toString());
                currentScene.getStylesheets().remove(getClass().getResource("/varpedia/styles/theme-dark.css").toString());
                _themeHelper.setDarkModeStatus(false);
            }
            // update tableview default thumbnails
            creationTableView.refresh();
        });

        // set the dark mode status
        if (_themeHelper.getDarkModeStatus()) {
            themeLabel.setText("Theme: Dark mode");
            themeBtn.setSelected(true);
        }

        // set up hover messages to help user use theme switcher
        themeBtn.hoverProperty().addListener((ov, oldValue, newValue) -> {
            String hoverMsg = "";
            if (newValue) {
                // hovered
                if (_themeHelper.getDarkModeStatus()) {
                    hoverMsg = "Click to switch to light mode";
                } else {
                    hoverMsg = "Click to switch to dark mode";
                }
            } else {
                // not hovered
                if (_themeHelper.getDarkModeStatus()) {
                    hoverMsg = "Theme: Dark mode";
                } else {
                    hoverMsg = "Theme: Light mode";
                }
            }
            themeLabel.setText(hoverMsg);
        });
    }
}
