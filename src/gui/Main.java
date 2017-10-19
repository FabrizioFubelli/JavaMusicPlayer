package gui;

import gui.utils.Graphic;
import gui.utils.Button;
import gui.utils.ProgressDialog;
import javafx.application.Platform;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import ulg.play.Song;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import ulg.music.MusicDictionary;
import ulg.play.Play;
import ulg.play.media.MediaPlayer;
import ulg.play.utils.ConvertFlac;
import ulg.programs.ExternalPrograms;
import ulg.utils.UtilFunctions;

import java.io.File;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

import static gui.utils.ProgressDialog.TOTAL;
import static gui.utils.ProgressDialog.current;
import static javafx.application.Platform.runLater;
import static ulg.play.Play.BACKGROUND_POOLS;
//import static ulg.play.Play.TASKS;
import static ulg.play.Play.TASKS;
import static ulg.play.media.MediaPlayer.RUNNING_SONGS;
import static ulg.utils.UtilFunctions.SCREEN_WIDTH;
import static ulg.utils.UtilFunctions.selectDirectory;

/**
 * Created by: Fabrizio Fubelli
 * Date: 10/01/2017.
 */
public class Main extends Application {
    public static void main(String[] args) {
        launch(args);
    }
    public static final String NAME ="JavaMusicPlayer";
    public static Stage primaryStage;
    public static Button.ButtonImage PLAY_PAUSE;
    public static  Button.ButtonImage STOP;
    public static  Button.ButtonImage PREVIOUS;
    public static  Button.ButtonImage NEXT;

    private static final AtomicLong lastKeyCall = new AtomicLong(0);
    private static volatile String musicDirectory;
    private static ImageView BACKGROUND;

    private final TableView<Song> table = new TableView<>();
    private final ObservableList<Song> data = FXCollections.observableArrayList();
    private final Graphic graphic = new Graphic();
    private final List<Song> lastListenedSongs = new ArrayList<>();
    private final List<Thread> Threads = new ArrayList<>();

    private volatile ObservableList<Song> selectedSongs;
    private volatile ObservableList<Song> allSongs;

    /**
     * The main entry point for all JavaFX applications.
     * The start method is called after the init method has returned,
     * and after the system is ready for the application to begin running.
     * <p>
     * <p>
     * NOTE: This method is called on the JavaFX Application Thread.
     * </p>
     *
     * @param stage the primary stage for this application, onto which
     *                     the application scene can be set. The primary stage will be embedded in
     *                     the browser if the application was launched as an applet.
     *                     Applications may create other stages, if needed, but they will not be
     *                     primary stages and will not be embedded in the browser.
     */
    @Override
    public void start(Stage stage) throws Exception {
        primaryStage = stage;
        primaryStage.setOnCloseRequest(e -> {
            primaryStage.hide();
            try { Play.off();
            } catch (Exception ignored) {}
            RUNNING_SONGS.shutdownNow();
            BACKGROUND_POOLS.shutdownNow();
            //TASKS.shutdownNow();
            this.Threads.forEach(Thread::interrupt);
            try {
                Thread.sleep(150);
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }
            ConvertFlac.deleteTempFiles();
        });
        primaryStage.setWidth(UtilFunctions.SCREEN_WIDTH/2.3);
        primaryStage.setHeight(UtilFunctions.SCREEN_HEIGHT/2.2);
        primaryStage.setScene(newRoot());
        primaryStage.setTitle(NAME);
        primaryStage.getIcons().add(createApplicationIcon());
        primaryStage.setMaximized(true);
        primaryStage.show();
    }

    private final GridPane FXbuttons = setFXbuttons();

    /**
     * Ottiene il nome dell'attributo
     * @param s l'attributo
     * @return il nome dell'attributo della classe Song
     */
    private static String sosSpace(String s) {
        if (s.contains("(sort)")) {
            return s.substring(0, s.length()-7)+"sort".toLowerCase();
        }
        int i = 0;
        StringBuilder sc = new StringBuilder(s);
        while (i != -1) {
            i = sc.indexOf(" ");
            try {
                sc.replace(i,i+1, "_");
            } catch (Exception ignored) {}
        }
        return sc.toString().toLowerCase();
    }

    private static int getColumnLength(String ret) {
        if (ret.contains("disc") || ret.contains("total")) {
            return 80;
        } else if (ret.contains("track") || ret.contains("bitrate") || ret.contains("type") ||
                ret.contains("filesize")) {
            return 60;
        } else if (ret.contains("Albumartistsort")) return 130;
        return 100;
    }

    private static volatile long lastPlayCall = 0;
    private void waitCall() {
        long sleep = System.currentTimeMillis() - lastPlayCall;
        lastPlayCall = System.currentTimeMillis();
        if (sleep < 900) try {
            Thread.sleep(sleep);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private Scene newRoot() {
        GridPane root = new GridPane();
        int numRows = 3;
        for (int i = 0; i < numRows; i++) {
            RowConstraints rc = new RowConstraints();
            if (i == 1) rc.setPrefHeight(UtilFunctions.SCREEN_HEIGHT/8.9);
            rc.setValignment(VPos.BOTTOM);
            root.getRowConstraints().add(rc);
        }
        ColumnConstraints cc = new ColumnConstraints();
        cc.setHalignment(HPos.CENTER);
        cc.setPercentWidth(100);
        root.getColumnConstraints().add(cc);

        root.add(createMenuBar(), 0, 0);


        double playerHeight = UtilFunctions.SCREEN_HEIGHT/8.9;
        this.FXbuttons.setAlignment(Pos.CENTER);
        this.FXbuttons.setOpacity(0.5);
        this.FXbuttons.setDisable(true);
        this.FXbuttons.setMinHeight(playerHeight);
        BACKGROUND = new ImageView(new Image(getClass().getResource("background_music2.jpg").toString(), SCREEN_WIDTH, playerHeight,false,false));
        BACKGROUND.setOpacity(0.2);

        root.add(BACKGROUND,0,1);
        root.add(this.FXbuttons, 0, 1);

        this.table.setPrefSize(UtilFunctions.SCREEN_WIDTH, UtilFunctions.SCREEN_HEIGHT);
        root.add(this.table, 0, 2);

        root.setBackground(new Background(new BackgroundFill(Color.BLACK, null, null)));

        this.table.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        this.selectedSongs = this.table.getSelectionModel().getSelectedItems();
        this.table.setOnKeyPressed(e -> {
            long currentTime = System.currentTimeMillis();
            if (lastKeyCall.get() >= currentTime-300) return;
            lastKeyCall.set(currentTime);
            if (e.getCode() == KeyCode.SPACE) {
                waitCall();
                this.playpause();
            } else if (e.getCode() == KeyCode.ENTER) {
                if (Play.isPlaying()) {
                    waitCall();
                    Play.stop();
                } else {
                    this.playpause();
                }
            }
        });
        this.table.setRowFactory( tv -> {
            TableRow<Song> row = new TableRow<>();
            row.setOnMouseClicked(e -> {
                if (e.getClickCount() > 1) {
                    long currentTime = System.currentTimeMillis();
                    //System.out.println("\n\nMain -> doubleClickedOnRow()");
                    try {
                        if (lastKeyCall.get() >= currentTime-300) {
                            return;
                        }
                        lastKeyCall.set(currentTime);
                        if (this.selectedSongs.size() == 0) {
                            return;
                        }
                        this.firstSongIndex = 0;
                        waitCall();
                        Play.readyForNewPlaylist(this.getSongsFromSelectedTrack(), this.firstSongIndex);
                        this.lastListenedSongs.clear();
                        this.lastListenedSongs.addAll(this.selectedSongs);
                        Play.play();
                    } catch (IndexOutOfBoundsException ex ) {
                        ex.printStackTrace();
                    }
                }
            });
            return row;
        });
        //this.table.setEditable(true);
        final String[] attrs = {"File", "Albumartist", "Albumartist (sort)", "Disc number", "Disc total", "Album", "Track",
                "Track total", "Title", "Artist", "Artist (sort)", "Genre", "Duration", "Bitrate", "Filesize", "Type"};
        int total_len = 0;
        for (String s : attrs) {
            String attr = sosSpace(s);
            TableColumn<Song, String> col = new TableColumn<>(s);
            int len = getColumnLength(attr);
            col.setPrefWidth(len);
            total_len += len;
            col.setCellValueFactory(
                    new PropertyValueFactory<>(attr));
            this.table.getColumns().add(col);
        }
        if (total_len < SCREEN_WIDTH) {
            this.table.getColumns().get(0).setPrefWidth(86+(SCREEN_WIDTH-total_len));
        }
        //return scene;
        return new Scene(root, UtilFunctions.SCREEN_WIDTH/2.3, UtilFunctions.SCREEN_WIDTH/2.2);
    }

    private int firstSongIndex;
    private List<Song> getSongsFromSelectedTrack() {
        if (this.selectedSongs.size() == 0) return this.allSongs;
        else if (this.selectedSongs.size() > 1) return this.selectedSongs;
        final Song song = this.selectedSongs.get(0);
        for (Song t : this.allSongs) {
            if (Objects.equals(t, song)) return this.allSongs;
            firstSongIndex++;
        }
        return Collections.emptyList();
    }

    private MenuBar createMenuBar() {
        MenuItem open = new MenuItem("Apri cartella");
        MenuItem exit = new MenuItem("Esci");
        open.setOnAction(e -> this.selectMusicDirectory());
        exit.setOnAction(e -> {
            primaryStage.close();
            Platform.exit();
        });
        Menu file = new Menu("File", null, open, exit);
        MenuItem startMusicEditor = this.defMenu("Avvia Editor Musicale", 0);
        Menu option = new Menu("Opzioni", null, startMusicEditor);
        return new MenuBar(file, option);
    }

    /**
     * Crea un nuovo sottomenu
     * @param name il nome visualizzato del sottomenu
     * @param i l'identificatore del sottomenu
     * @return un sottomenu
     */
    private MenuItem defMenu(String name, int i) {
        MenuItem menuItem = new MenuItem(name);
        if (i == 0) { // Avvia Editor Musicale
            menuItem.setOnAction(e -> {
                Thread t = new Thread(() -> {
                    ExternalPrograms.openExternalProgram(ExternalPrograms.Program.PY_SORTING);
                    menuItem.setDisable(false);
                    this.Threads.remove(Thread.currentThread());
                });
                this.Threads.add(t);
                menuItem.setDisable(true);
                t.start();
            });
        }
        return menuItem;
    }

    private Image createApplicationIcon() {
        return new Image(getClass().getResource("/icon/icon.png").toString());
    }

    private boolean changedSongs() {
        int size = this.selectedSongs.size();
        if (this.lastListenedSongs.size() != size) return true;
        for (int i = 0; i < size; i++) {
            Song t1 = this.selectedSongs.get(i);
            Song t2 = this.lastListenedSongs.get(i);
            if (!t1.equals(t2)) return false;
        }
        return true;
    }

    private void playpause() {
        //System.out.println("Main -> playpause()");
        if (Play.isPlaying()) {
            waitCall();
            Play.pause();
        }
        else if (Play.isPaused()) {
            waitCall();
            Play.play();
        }
        else if (Play.isStopped()) {
            Play.readyForNewPlaylist(this.getSongsFromSelectedTrack(), firstSongIndex);
            /*
            if (changedSongs()) {
                this.lastListenedSongs.clear();
                this.lastListenedSongs.addAll(this.selectedSongs);
                this.firstSongIndex = 0;
                Play.readyForNewPlaylist(this.getSongsFromSelectedTrack(), firstSongIndex);
            }*/
            waitCall();
            Play.play();
        }
        else {
            this.lastListenedSongs.clear();
            this.lastListenedSongs.addAll(this.selectedSongs);
            this.firstSongIndex = 0;
            waitCall();
            Play.readyForNewPlaylist(this.getSongsFromSelectedTrack(), firstSongIndex);
            Play.play();
        }
    }

    private GridPane setFXbuttons() {
        GridPane FXbuttons = graphic.createMediaButtonsPane();
        PLAY_PAUSE = graphic.getButton(Button.ButtonType.PLAY);
        STOP = graphic.getButton(Button.ButtonType.STOP);
        PREVIOUS = graphic.getButton(Button.ButtonType.PREVIOUS);
        NEXT = graphic.getButton(Button.ButtonType.NEXT);
        PLAY_PAUSE.setOnMouseClicked(e -> {
            PLAY_PAUSE.mouseClick();
            this.playpause();
        });
        STOP.setOnMouseClicked(e -> {
            STOP.mouseClick();
            waitCall();
            Play.stop();
        });
        PREVIOUS.setOnMouseClicked(e -> {
            PREVIOUS.mouseClick();
            waitCall();
            Play.previous();
        });
        NEXT.setOnMouseClicked(e -> {
            NEXT.mouseClick();
            waitCall();
            Play.next();
        });
        return FXbuttons;
    }

    /**
     * Imposta la directory contenente i files musicali
     */
    private void selectMusicDirectory() {
        final File selectedDir = selectDirectory("Seleziona una cartella contenente files musicali ", primaryStage);
        if (Objects.isNull(selectedDir)) return;
        this.disableAllButtons(true);
        if (Play.isPlaying()) Play.stop();
        if (!Objects.isNull(allSongs) && allSongs.size() > 0) Play.readyForNewPlaylist(Collections.emptyList(), 0);
        current.set(0);
        TOTAL.set(100);
        this.data.clear();
        final ProgressDialog progressDialog = new ProgressDialog();
        progressDialog.start();
        new Thread(() -> {
            try {
                musicDirectory = selectedDir.getAbsolutePath();
                final MusicDictionary MusicDict = new MusicDictionary(musicDirectory);
                /*this.number_of_files.set(0);
                this.getNumberOfFiles(selectedDir);
                //System.out.println("numero di files nella cartella selezionata: "+this.number_of_files.get());
                TOTAL.set(this.number_of_files.get());*/
                final Map<String, String[]> map = MusicDict.getMap();
                map.forEach((k, v) -> this.data.add(new Song(k, v)));
                runLater(() -> {
                    this.table.setItems(data);
                    this.allSongs = this.table.getItems();
                });
                // runLater...
                this.disableAllButtons(false);
                PREVIOUS.setButtonDisabled(true);
                NEXT.setButtonDisabled(true);
                STOP.setButtonDisabled(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    /*
    private final AtomicInteger number_of_files = new AtomicInteger(0);
    private void getNumberOfFiles(File file) {
        if (file.isFile()) this.number_of_files.getAndIncrement();
        else {
            final List<ForkJoinTask> tasks = new ArrayList<>();
            final File[] subfiles = file.listFiles();
            if (Objects.isNull(subfiles)) return;
            for (final File subfile : subfiles) tasks.add(ForkJoinTask.adapt(() -> getNumberOfFiles(subfile)));
            for (final ForkJoinTask t : ForkJoinTask.invokeAll(tasks)) {
                t.join();
                t.cancel(true);
            }
        }
    }*/

    private void disableAllButtons(boolean disable) {
        this.FXbuttons.setDisable(disable);
        double d = disable ? 0.5 : 1;
        BACKGROUND.setOpacity(d-0.3);
        this.FXbuttons.setOpacity(d);
        PLAY_PAUSE.setOpacity(d);
        STOP.setOpacity(d);
        PREVIOUS.setOpacity(d);
        NEXT.setOpacity(d);
    }
}