package ulg.utils;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.DirectoryChooser;
import javafx.stage.Window;

import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by: Fabrizio Fubelli
 * Date: 11/01/2017.
 */
public class UtilFunctions<T> {
    @SuppressWarnings("unchecked")
    public List<T> convertList(List<?> in) {
        List<T> out = new ArrayList<>();
        in.forEach(o -> out.add((T) o));
        return out;
    }

    public static UtilFunctions<String> string = new UtilFunctions<>();

    public static File selectDirectory(String windowTitle, Window owner) {
        DirectoryChooser fc = new DirectoryChooser();
        fc.setTitle(windowTitle);
        return fc.showDialog(owner);
    }

    public static boolean newDictionaryRequest(File dictionaryFile, File selectedRoot) {
        final AtomicBoolean signal = new AtomicBoolean(false);
        final AtomicBoolean ret = new AtomicBoolean();
        javafx.application.Platform.runLater(() -> {
            Alert request = new Alert(Alert.AlertType.CONFIRMATION, "",
                    ButtonType.YES, ButtonType.NO);
            request.setHeaderText("Il dizionario interno non risulta essere aggiornato! Eventuali nuovi file aggiunti " +
                    "nella cartella selezionata, non saranno visualizzati nel Media Player.\nAggiornare il dizionario ?");
            Optional<ButtonType> result = request.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.YES) {
                ret.set(true);
            } else {
                ret.set(false);
                Alert second_request = new Alert(Alert.AlertType.CONFIRMATION, "",
                        ButtonType.YES, ButtonType.NO);
                second_request.setHeaderText("Si desidera visualizzare ancora questo " +
                        "messaggio (fino alla prossima modifica della cartella selezionata) ?");
                Optional<ButtonType> second_result = second_request.showAndWait();
                if (second_result.isPresent() && second_result.get() == ButtonType.YES) {
                    if (!dictionaryFile.setLastModified(selectedRoot.lastModified())) {
                        throw new IllegalStateException("Impossibile aggiornare il file dizionario");
                    }
                }
            }
            signal.set(true);
            synchronized (signal) {
                signal.notify();
            }
        });
        while (!signal.get()) {
            synchronized (signal) {
                try {
                    signal.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        return ret.get();
    }

    public static final double SCREEN_WIDTH = Toolkit.getDefaultToolkit().getScreenSize().getWidth(); // LARGHEZZA SCHERMO
    public static final double SCREEN_HEIGHT = Toolkit.getDefaultToolkit().getScreenSize().getHeight(); // ALTEZZA SCHERMO
}
