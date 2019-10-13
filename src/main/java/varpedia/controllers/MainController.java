package varpedia.controllers;

import javafx.beans.value.ObservableValueBase;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableColumn.SortType;
import javafx.util.StringConverter;
import varpedia.AlertHelper;
import varpedia.Creation;
import varpedia.VARpediaApp;
import varpedia.tasks.ClearTask;
import varpedia.tasks.ListPopulateTask;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
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
    private final ObservableList<Creation> creationList = FXCollections.observableArrayList();
    @FXML
    private TableView<Creation> creationTableView;
    @FXML
    private TableColumn<Creation, String> creationNameCol;
    @FXML
    private TableColumn<Creation, String> creationConfCol;
    @FXML
    private TableColumn<Creation, String> creationViewCol;

    private ExecutorService pool = VARpediaApp.newTimedCachedThreadPool();
    private AlertHelper _alertHelper = AlertHelper.getInstance();

    @FXML
    private void initialize() {
    	// set up table view columns and sorting
        initializeColumns();
        initializeSort();

        // populate table view with saved creations
    	populateTable();
        deleteAppfiles();

        // disable play and delete buttons until a creation is selected
        playBtn.disableProperty().bind(creationTableView.getSelectionModel().selectedItemProperty().isNull());
        deleteBtn.disableProperty().bind(creationTableView.getSelectionModel().selectedItemProperty().isNull());
    }

    @FXML
    private void pressPlayButton(ActionEvent event) {
        // store a creation's name and open PlaybackScreen
        sendDataToFile(getSelectedName(), "playback-name.txt");
        changeScene(event, "/varpedia/PlaybackScreen.fxml");
    }

    @FXML
    private void pressDeleteButton(ActionEvent event) {
        // ask for confirmation
        _alertHelper.showAlert(Alert.AlertType.WARNING,
                "Are you sure you want to delete the " + getSelectedName() + " creation?",
                ButtonType.YES, ButtonType.CANCEL);

        if (_alertHelper.getResult() == ButtonType.YES) {
            // delete creation file
            File file = new File("creations/" + getSelectedName() + ".mp4");
            File file2 = new File("creations/" + getSelectedName() + ".dat");
            if (file.delete() && (!file2.exists() || file2.delete())) {
                // update table view
                creationList.remove(creationTableView.getSelectionModel().getSelectedItem());
            } else {
                _alertHelper.showAlert(Alert.AlertType.ERROR, "Could not delete creation.");
            }
        }
    }

    @FXML
    private void pressCreateButton(ActionEvent event) {
        // open WikitSearchScreen
        changeScene(event, "/varpedia/WikitSearchScreen.fxml");
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
	                    		creationList.add((Creation)ois.readObject());
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
}
