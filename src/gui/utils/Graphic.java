package gui.utils;

import gui.utils.Button;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.image.Image;
import javafx.scene.layout.GridPane;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Created by: Fabrizio Fubelli
 * Date: 09/01/2017.
 */
public class Graphic {
    private final List<Button.ButtonImage> buttonImages = new ArrayList<>();

    private GridPane buttonsPane;

    public GridPane createMediaButtonsPane() {
        if (!Objects.isNull(buttonsPane) && buttonImages.size() > 0) return buttonsPane;
        buttonsPane = new GridPane();
        int x = 0;
        for (Button.ButtonType buttonType : Button.ButtonType.values()) {
            if (buttonType == Button.ButtonType.PAUSE) continue;
            Button.ButtonImage buttonImage = new Button.ButtonImage(buttonType);
            Image bImage = new Image(getClass().getResource(buttonType.name() + ".png").toString());
            buttonImage.setFitHeight(bImage.getHeight()/3);
            buttonImage.setFitWidth(bImage.getWidth()/3);
            buttonsPane.add(buttonImage, x, 0);
            GridPane.setHalignment(buttonImage, HPos.LEFT);
            GridPane.setValignment(buttonImage, VPos.CENTER);
            buttonImages.add(buttonImage);
            x++;
        }
        return buttonsPane;
    }

    public Button.ButtonImage getButton(Button.ButtonType type) {
        for (Button.ButtonImage buttonImage : this.buttonImages) {
            if (buttonImage.getButtonType() == type) return buttonImage;
        }
        return null;
    }
}
