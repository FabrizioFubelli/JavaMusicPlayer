package gui.utils;

import javafx.concurrent.Task;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.Background;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import ulg.utils.UtilFunctions;

import java.util.concurrent.atomic.AtomicInteger;

public class ProgressDialog {

    private static int SLEEP = 500;
    public static final AtomicInteger TOTAL = new AtomicInteger();      // Total of elements
    public static final AtomicInteger current = new AtomicInteger();    // Elaborated elements

    private final AtomicInteger lastProportion = new AtomicInteger(0);

    private void setSleepTime() {
        int total = TOTAL.get();
        if (total <= 100) SLEEP = 20;
        else if (total <= 500) SLEEP = 50;
        else if (total <= 1000) SLEEP = 70;
        else if (total <= 2000) SLEEP = 200;
        else if (total <= 5000) SLEEP = 600;
        else if (total <= 10000) SLEEP = 1000;
        else if (total <= 30000) SLEEP = 3000;
        else if (total <= 60000) SLEEP = 5000;
        else if (total <= 100000) SLEEP = 8000;
        else SLEEP = 12000;
    }

    public void start() {
        setSleepTime();
        ProgressForm pForm = new ProgressForm();
        Task<Void> task = new Task<Void>() {
            @Override
            public Void call() throws InterruptedException {
                while (current.get() < TOTAL.get()) {
                    int currentProportion = (current.get() * 100) / TOTAL.get();
                    if (currentProportion > lastProportion.get()) updateProgress(currentProportion, 100);
                    lastProportion.set(currentProportion);
                    Thread.sleep(SLEEP);
                }
                return null;
            }
        };
        pForm.activateProgressBar(task);
        task.setOnSucceeded(event -> pForm.getDialogStage().close());
        Stage dialog = pForm.getDialogStage();
        dialog.centerOnScreen();
        dialog.show();
        new Thread(task).start();
    }

    private static class CircleProgress {
        private final ProgressIndicator pin = new ProgressIndicator();
        CircleProgress() {
            pin.setBackground(Background.EMPTY);
            pin.setCenterShape(true);
        }
        public ProgressIndicator get() {
            return this.pin;
        }
    }

    private static class BarProgress {
        private final ProgressBar pb = new ProgressBar();
        BarProgress() {
            pb.setBackground(Background.EMPTY);
            pb.setCenterShape(true);
        }
        public ProgressBar get() {
            return this.pb;
        }
    }

    private static class ProgressForm {
        private final Stage dialogStage;
        private final ProgressIndicator circleProgress;
        private final ProgressBar barProgress;
        ProgressForm() {
            dialogStage = new Stage();
            dialogStage.setResizable(false);
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            this.circleProgress = new CircleProgress().get();
            this.barProgress = new BarProgress().get();
            final VBox vbox = new VBox(this.circleProgress, this.barProgress);
            vbox.setBackground(Background.EMPTY);
            vbox.setSpacing(20);
            vbox.setAlignment(Pos.CENTER);
            Scene scene = new Scene(vbox);
            scene.setFill(Color.TRANSPARENT);
            dialogStage.setScene(scene);
            double size = UtilFunctions.SCREEN_HEIGHT/7;
            dialogStage.setHeight(size);
            dialogStage.setWidth(size);
            dialogStage.initStyle(StageStyle.TRANSPARENT);
            vbox.setMinHeight(size);
            vbox.setMinWidth(size);
            dialogStage.centerOnScreen();
        }

        void activateProgressBar(final Task<?> task)  {
            this.barProgress.progressProperty().bind(task.progressProperty());
            dialogStage.centerOnScreen();
            dialogStage.show();
        }

        Stage getDialogStage() {
            return dialogStage;
        }
    }
}