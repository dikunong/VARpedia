package varpedia.helpers;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.Region;

/**
 * Singleton class that manages creation and display of alerts, as well as receiving user input in response.
 *
 * @author Di Kun Ong
 */
public final class AlertHelper {

    private static AlertHelper _alertHelper;
    private ButtonType _result;
    private ThemeHelper _themeHelper = ThemeHelper.getInstance();

    private AlertHelper() {}

    public synchronized static AlertHelper getInstance() {
        if (_alertHelper == null) {
            _alertHelper = new AlertHelper();
        }
        return _alertHelper;
    }

    /**
     * Displays an alert to the user. It will store which button was pressed, which will be accessed later if
     * it represents a split in application logic.
     * Could be an error message, an information dialog, or a confirmation window.
     * @param type AlertType to be displayed
     * @param title The type of the message
     * @param msg Message to be displayed
     * @param buttons 1+ buttons to be given as options - if blank, will display Java defaults
     */
    public void showAlert(Alert.AlertType type, String title, String msg, ButtonType... buttons) {
        Alert alert = new Alert(type, msg, buttons);
        alert.setTitle(title);
        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        String themeCSS;
        if (_themeHelper.getDarkModeStatus()) {
            themeCSS = "/varpedia/styles/theme-dark.css";
        } else {
            themeCSS = "/varpedia/styles/theme-light.css";
        }
        alert.getDialogPane().getStylesheets().add(themeCSS);
        alert.getDialogPane().getStylesheets().add("/varpedia/styles/dialogs.css");
        alert.showAndWait();
        _result = alert.getResult();
    }

    /**
     *
     * @return the ButtonType that the user pressed
     */
    public ButtonType getResult() {
        return _result;
    }
}
