package ulg.play.utils.test;

import javafx.application.Application;
import javafx.stage.Stage;
import ulg.play.utils.Flac;
import ulg.utils.UtilFunctions;

import java.io.File;

/**
 * Created by: Fabrizio Fubelli
 * Date: 13/01/2017.
 */
public class Test extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    private static void test() {
        String in2 = "C:\\Users\\Fabrizio\\MySoftwares\\02 I Know What I Like (In Your Wardrobe).flac";
        String in = "C:\\Users\\Fabrizio\\MySoftwares\\01 Macdougall's Men.flac";
        String out = "C:\\Users\\Fabrizio\\MySoftwares\\01 Macdougall's Men.wma";

        Flac flac = new Flac(new File(in));
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        final double screenHeight = UtilFunctions.SCREEN_HEIGHT;
        test();
    }
}
