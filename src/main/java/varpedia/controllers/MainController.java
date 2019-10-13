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
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;

/**
 * Controller for the MainScreen, which displays current creations that can be played and deleted, as well as acting as
 * the gateway to making new creations.
 *
 * Authors: Di Kun Ong and Tudor Zagreanu
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

    @SuppressWarnings("unchecked")
	@FXML
    private void initialize() {
    	// populate table view with saved creations

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
                    Instant conf = p.getValue().getLastViewed();

                    if (conf == null) {
                        return "Unwatched";
                    } else {
                        return conf.toString();
                    }
                }
            };
        });
    	
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
        sortChoiceBox.getSelectionModel().select(0);
        
        sortChoiceBox.setOnAction((event) -> {
        	TableColumn<Creation, ?> main = sortChoiceBox.getSelectionModel().getSelectedItem();
        	TableColumn<Creation, ?> second = null;
        	
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
        
    	populateTable();
        deleteAppfiles();

        // disable play and delete buttons until a creation is selected
        playBtn.disableProperty().bind(creationTableView.getSelectionModel().selectedItemProperty().isNull());
        deleteBtn.disableProperty().bind(creationTableView.getSelectionModel().selectedItemProperty().isNull());
    }

    @FXML
    private void pressPlayButton(ActionEvent event) {
        // store a creation's filename and open PlaybackScreen
        sendDataToFile(getSelectedFilename(), "playback-name.txt");
        changeScene(event, "/varpedia/PlaybackScreen.fxml");
    }

    @FXML
    private void pressDeleteButton(ActionEvent event) {
        // ask for confirmation
        _alertHelper.showAlert(Alert.AlertType.WARNING,
                "Are you sure you want to delete the selected creation?", // add selected creation name here later
                ButtonType.YES, ButtonType.CANCEL);

        if (_alertHelper.getResult() == ButtonType.YES) {
            // delete creation file
            File file = new File("creations/" + getSelectedFilename() + ".mp4");
            File file2 = new File("creations/" + getSelectedFilename() + ".dat");
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
    private String getSelectedFilename() {
        return creationTableView.getSelectionModel().getSelectedItem().getCreationName();
    }

    /**
     * Helper method that retrives the observable list for the FXML component.
     * @return observable list of Creations
     */
    public ObservableList<Creation> getCreationList() {
        return creationList;
    }

    /**
     * Helper method that runs a task to populate the creationList with chunks in the creations directory.
     */
    private void populateTable() {
        // This method re-populates the List everytime and cannot obtain confidence and lastViewed data
        // need a new method that can retain this data even between restarts of the app
        // TODO: redo completely to use serialization of creation objects

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
