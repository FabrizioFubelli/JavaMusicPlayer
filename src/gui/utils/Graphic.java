package gui.utils;

import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.image.Image;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;

import java.util.*;

/**
 * Created by: Fabrizio Fubelli
 * Date: 09/01/2017.
 */
public class Graphic {

    private final List<Button.ButtonImage> buttonImages = new ArrayList<>();

    private GridPane buttonsPane;
    /* Tools */
    private GridPane toolsPane;
    private AutoCompleteTextField search;

    public GridPane createMediaButtonsPane() {
        if (Objects.isNull(this.buttonsPane) || this.buttonImages.size() == 0) {
            this.buttonsPane = new GridPane();
            int x = 0;
            for (Button.ButtonType buttonType : Button.ButtonType.values()) {
                if (buttonType == Button.ButtonType.PAUSE) continue;
                Button.ButtonImage buttonImage = new Button.ButtonImage(buttonType);
                Image bImage = new Image(getClass().getResource(buttonType.name() + ".png").toString());
                buttonImage.setFitHeight(bImage.getHeight() / 3);
                buttonImage.setFitWidth(bImage.getWidth() / 3);
                this.buttonsPane.add(buttonImage, x, 0);
                GridPane.setHalignment(buttonImage, HPos.LEFT);
                GridPane.setValignment(buttonImage, VPos.CENTER);
                this.buttonImages.add(buttonImage);
                x++;
            }
        }
        return buttonsPane;
    }

    public GridPane createToolsPane() {
        if (Objects.isNull(this.toolsPane) || Objects.isNull(this.search)) {
            this.toolsPane = new GridPane();
            this.search = new AutoCompleteTextField();
            this.toolsPane.add(search, 0, 0);
            this.toolsPane.setBackground(new Background(new BackgroundFill(new Color(0.9, 0.9, 0.9, 1), CornerRadii.EMPTY, Insets.EMPTY)));
        }
        return this.toolsPane;
    }

    /*
    public void setSearchItems(HashMap<String, HashSet<String>> values) {
        if (Objects.isNull(this.search)) this.createToolsPane();
        this.search.setEntries(values);
    }
    */

    public void setSearchItems(HashMap<String, HashSet<String>> values) {
        if (Objects.isNull(this.search)) this.createToolsPane();
        this.search.setEntries(values);
    }

    public Button.ButtonImage getButton(Button.ButtonType type) {
        for (Button.ButtonImage buttonImage : this.buttonImages) {
            if (buttonImage.getButtonType() == type) return buttonImage;
        }
        return null;
    }
}
