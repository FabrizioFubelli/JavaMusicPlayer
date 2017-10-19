package gui;

import gui.utils.Graphic;
import gui.utils.Button;
import gui.utils.ProgressDialog;
import gui.utils.StringCompare;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.StageStyle;
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
import ulg.play.utils.ConvertFlac;
import ulg.utils.UtilFunctions;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

import static gui.utils.ProgressDialog.TOTAL;
import static gui.utils.ProgressDialog.current;
import static javafx.application.Platform.runLater;
import static ulg.play.Play.BACKGROUND_POOLS;
import static ulg.play.media.MediaPlayer.RUNNING_SONGS;
import static ulg.utils.UtilFunctions.*;

/**
 * Created by: Fabrizio Fubelli
 * Date: 10/01/2017.
 */
public class Main extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    public static Stage primaryStage;
    public static Button.ButtonImage PLAY_PAUSE;
    public static  Button.ButtonImage STOP;
    public static  Button.ButtonImage PREVIOUS;
    public static  Button.ButtonImage NEXT;

    public static final String NAME ="JavaMusicPlayer";
    public static final StringCompare stringCompare = new StringCompare();
    public static final String[] ATTRS = {
            "File",                 // 0
            "Albumartist",          // 1
            "Albumartist (sort)",   // 2
            "Disc number",          // 3
            "Disc total",           // 4
            "Album",                // 5
            "Track",                // 6
            "Track total",          // 7
            "Title",                // 8
            "Artist",               // 9
            "Artist (sort)",        // 10
            "Genre",                // 11
            "Duration",             // 12
            "Bitrate",              // 13
            "Filesize (MB)",        // 14
            "Type"                  // 15
    };

    private static final ObservableList<Song> data = FXCollections.observableArrayList();
    private static final AtomicLong lastKeyCall = new AtomicLong(0);
    private static final TableView<Song> table = new TableView<>();
    private static volatile String musicDirectory;
    private static volatile long lastPlayCall = 0;
    private static volatile ObservableList<Song> allSongs;

    private static ImageView BACKGROUND;

    private final Graphic graphic = new Graphic();
    private final GridPane FXbuttons = setFXbuttons();
    private final GridPane TOOLS = setTools();
    private final List<Song> lastListenedSongs = new ArrayList<>();

    private volatile ObservableList<Song> selectedSongs;

    private int firstSongIndex;






    /* *************************************************** GRAFICA ************************************************** */

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
            //this.Threads.forEach(Thread::interrupt);
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

    private Image createApplicationIcon() {
        return new Image(getClass().getResource("/icon/icon.png").toString());
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

        this.TOOLS.setAlignment(Pos.CENTER);
        this.TOOLS.setDisable(true);
        this.TOOLS.setMinHeight(50);

        BACKGROUND = new ImageView(new Image(getClass().getResource("background_music2.jpg").toString(), SCREEN_WIDTH, playerHeight,false,false));
        BACKGROUND.setOpacity(0.2);

        root.add(BACKGROUND,0,1);
        root.add(this.FXbuttons, 0, 1);
        root.add(this.TOOLS, 0, 2);

        table.setPrefSize(UtilFunctions.SCREEN_WIDTH, UtilFunctions.SCREEN_HEIGHT);
        root.add(table, 0, 3);

        root.setBackground(new Background(new BackgroundFill(Color.BLACK, null, null)));

        table.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        this.selectedSongs = table.getSelectionModel().getSelectedItems();
        table.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.SPACE) {
                long currentTime = System.currentTimeMillis();
                if (lastKeyCall.get() >= currentTime-300) return;
                lastKeyCall.set(currentTime);
                waitCall();
                this.playpause();
            } else if (e.getCode() == KeyCode.ENTER) {
                rowClick();
            }
        });
        table.setContextMenu(newTrackContestMenu());
        table.setRowFactory( tv -> {
            TableRow<Song> row = new TableRow<>();
            row.setOnMouseClicked(e -> {
                if (e.getClickCount() > 1) {
                    rowClick();
                }
            });
            return row;
        });
        table.setEditable(true);
        int total_len = 0;
        for (String s : ATTRS) {
            String attr = sosSpace(s);
            TableColumn<Song, String> col = new TableColumn<>(s);
            int len = getColumnLength(attr);
            col.setPrefWidth(len);
            total_len += len;
            col.setCellValueFactory(new PropertyValueFactory<>(attr));
            table.getColumns().add(col);
        }
        if (total_len < SCREEN_WIDTH) {
            table.getColumns().get(0).setPrefWidth(86+(SCREEN_WIDTH-total_len));
        }
        //return scene;
        return new Scene(root, UtilFunctions.SCREEN_WIDTH/2.3, UtilFunctions.SCREEN_WIDTH/2.2);
    }

    /**
     * Ottiene il nome dell'attributo
     * @param s l'attributo
     * @return il nome dell'attributo della classe Song
     */
    private static String sosSpace(String s) {
        if (s.contains("(sort)")) {
            return s.substring(0, s.length()-7)+"sort".toLowerCase();
        }
        if (s.contains("Filesize")) {
            return "filesize";
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

    /**
     * Calcola la larghezza che deve avere la colonna
     * @param ret il nome della colonna
     * @return un intero
     */
    private static int getColumnLength(String ret) {
        if (ret.contains("disc") || ret.contains("total")) {
            return 80;
        } else if (ret.contains("track") || ret.contains("bitrate") || ret.contains("type") || ret.contains("genre")) {
            return 60;
        } else if (ret.contains("Albumartistsort")) return 130;
        return 100;
    }

    /**
     * Crea un nuovo ContextMenu per il click destro su una row della tabella
     * @return un context menu
     */
    private ContextMenu newTrackContestMenu() {
        final MenuItem modifica = new MenuItem("Modifica");
        modifica.setOnAction(e -> {
            final List<Song> songs = table.getSelectionModel().getSelectedItems();
            final Dialog<ButtonType> dialog = new Dialog<>();
            final GridPane editPane = new GridPane();
            final double width = SCREEN_WIDTH/2.7;
            final double height = SCREEN_HEIGHT/1.5;

            Text file = null;
            TextField title = null, album = null;
            String firstSong = null;
            ImageView cover = null;
            boolean no_cover = false;
            int lastRound = Integer.MAX_VALUE;
            int lastSize = Integer.MIN_VALUE;

            for (Song song : songs) {
                if (Objects.isNull(song)) continue;
                final File parent = new File(song.getFile()).getParentFile();
                if (!no_cover) {
                    if (Objects.isNull(firstSong)) {
                        firstSong = parent.getAbsolutePath();
                    } else if (!Objects.equals(parent.getAbsolutePath(), firstSong)) {
                        cover = null;
                        no_cover = true;
                    }
                }
                if (!Objects.isNull(file) && !Objects.equals(song.getFile(), file.getText())) {
                    file.setText("<multiple>");
                } else {
                    file = new Text(song.getFile());
                    if (!Objects.isNull(cover) || no_cover) continue;
                    final File[] dirs = parent.listFiles();
                    if (!Objects.isNull(dirs)) {
                        for (File f : dirs) {
                            if (f.isFile()) {
                                final String fName = f.getName();
                                final int extIndex = fName.length() - 4;
                                if (fName.lastIndexOf(".jpg") == extIndex ||
                                        fName.lastIndexOf(".png") == extIndex  ||
                                        fName.lastIndexOf(".gif") == extIndex) {
                                    final Image image = new Image(f.toURI().toString());
                                    final int size = (int) image.getWidth();
                                    final int round = (int) Math.abs(image.getHeight()-image.getWidth());
                                    if ( round < lastRound || (round == lastRound && lastSize < size)) {
                                        lastRound = round;
                                        lastSize = size;
                                        cover = new ImageView(image);
                                    }
                                }
                            }
                        }
                    }
                }
                if (!Objects.isNull(title) && !Objects.equals(song.getTitle(), title.getText())) {
                    title.setText("");
                    title.setPromptText("<multiple>");
                } else {
                    title = new TextField(song.getTitle());
                }
                if (!Objects.isNull(album) && !Objects.equals(song.getAlbum(), album.getText())) {
                    album.setText("");
                    album.setPromptText("<multiple>");
                } else {
                    album = new TextField(song.getAlbum());
                }
            }

            editPane.setHgap(10);
            editPane.setVgap(10);

            if (!Objects.isNull(cover)) {
                cover.setFitWidth(height/2.5);
                cover.setPreserveRatio(true);
                cover.setSmooth(true);
                cover.setCache(true);
                editPane.add(cover, 0, 0, 4, 4);
            } else {
                final Rectangle square = new Rectangle();
                square.setFill(Color.LIGHTGRAY);
                square.setWidth(height/2.5);
                square.setHeight(height/2.5);
                editPane.add(square, 0, 0, 4, 4);
            }

            editPane.add(new Label("Title   ") ,5, 0);
            editPane.add(title, 6, 0);

            editPane.add(new Label("Album   ") ,5, 1);
            assert album != null;
            album.setPrefWidth(width/2.5);
            editPane.add(album, 6, 1);

            dialog.initStyle(StageStyle.UTILITY);
            dialog.setTitle("Modifica: " + (!Objects.equals(file.getText(), "<multiple>") ? file.getText() : "elementi multipli"));
            dialog.setHeaderText("Modifica:     " + (!Objects.equals(title.getPromptText(), "<multiple>") ? title.getText() : "elementi multipli"));

            dialog.getDialogPane().setContent(editPane);
            dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
            dialog.getDialogPane().getButtonTypes().add(ButtonType.APPLY);
            dialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);

            dialog.getDialogPane().lookupButton(ButtonType.APPLY).addEventFilter(
                    ActionEvent.ACTION,
                    event -> {
                        event.consume();
                        // Apply...
                        System.out.println("apply");
                    }
            );
            dialog.getDialogPane().lookupButton(ButtonType.OK).addEventFilter(
                    ActionEvent.ACTION,
                    event -> {
                        // Apply and Close...
                    }
            );

            dialog.setX((SCREEN_WIDTH-width)/2);
            dialog.setY((SCREEN_HEIGHT-height)/2);
            dialog.show();
            dialog.setWidth(width);
            dialog.setHeight(height);
        });
        final MenuItem rimuovi = new MenuItem("Rimuovi");
        return new ContextMenu(modifica, rimuovi);
    }

    /**
     * Esegue la canzone scelta dall'utente
     */
    private void rowClick() {
        long currentTime = System.currentTimeMillis();
        try {
            while ((currentTime-300) <= lastKeyCall.get()) {
                Thread.sleep(Math.min(lastKeyCall.get() - (currentTime-300), 100));
                currentTime = System.currentTimeMillis();
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
        } catch (IndexOutOfBoundsException | InterruptedException e ) {
            e.printStackTrace();
        }
    }

    /**
     * Crea una nuova barra del menu
     * @return una nuova barra del menu
     */
    private MenuBar createMenuBar() {
        MenuItem open = new MenuItem("Apri cartella");
        MenuItem exit = new MenuItem("Esci");
        open.setOnAction(e -> this.selectMusicDirectory());
        exit.setOnAction(e -> {
            primaryStage.close();
            Platform.exit();
        });
        Menu file = new Menu("File", null, open, exit);
        //MenuItem startEditTracks = this.defMenu("Avvia Editor Musicale", 0);
        //Menu option = new Menu("Opzioni", null, startMusicEditor);


        //MenuItem startEditTracks = this.defMenu("MEGA", 0);
        //Menu mega = new Menu("Mega", null, startEditTracks);

        //return new MenuBar(file, mega);
        return new MenuBar(file);
        //return new MenuBar(file);
    }

    /**
     * Crea un nuovo sottomenu
     * @ param name il nome visualizzato del sottomenu
     * @ param i l'identificatore del sottomenu
     * @ return un sottomenu
     */
    /*
    private MenuItem defMenu(String name, int i) {
        MenuItem menuItem = new MenuItem(name);
        if (i == 0) { // Avvia MEGA
            menuItem.setOnAction(e -> {
                try {
                    final String appKey = "tewHmZDZ";
                    //MegaApiJava megaApi = new MegaApiJava(appKey, "JVM", getMusicPlayerPath(), new MegaGfxProcessor());
                    MegaHandler mh = new MegaHandler("music.all@protonmail.com", "FA00!ciao7@2c");
                    mh.get_files();
                } catch (Exception e1) {
                    e1.printStackTrace();
                }

                /*
                //new Thread(() -> {
                    try {

                        //MegaApiJava megaApiJava = new MegaApiJava(appKey);
                        System.out.println("\nLogin:");
                        //MegaHandler megaHandler = new MegaHandler("music_A@mail.com", "fabrymusicA");
                        MegaHandler megaHandler = new MegaHandler("music.all@protonmail.com", "FA00!ciao7@2c");
                        megaHandler.login();

                        System.out.println("\nMEGA user:");
                        System.out.println(megaHandler.get_user());
                        /*System.out.println("\nMEGA files:");
                        for (MegaFile f : megaHandler.get_files()) {
                            System.out.println(f.getName());
                        }*/
                    /*} catch (Exception e1) {
                        e1.printStackTrace();
                    }*/

                    /*
                    final String[] args = {};
                    System.out.println("Calling MainPanel...");
                    MainPanel.main(args);
                    String hostName = "localhost";
                    int portNumber = 1337;
                    try {
                        Socket echoSocket = new Socket(hostName, portNumber);
                        PrintWriter out = new PrintWriter(echoSocket.getOutputStream(), true);
                        BufferedReader in = new BufferedReader(new InputStreamReader(echoSocket.getInputStream()));
                        BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                    /*try {
                        //MegaTest.main(null);
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }*/
                //}).start();

            /*});
                /*Thread t = new Thread(() -> {
                    //ExternalPrograms.openExternalProgram(ExternalPrograms.Program.PY_SORTING);
                    menuItem.setDisable(false);
                    //this.Threads.remove(Thread.currentThread());
                });
                //this.Threads.add(t);
                menuItem.setDisable(true);
                t.start();*/
    /*}
        return menuItem;
    }*/

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

    private GridPane setTools() {
        return graphic.createToolsPane();
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

    private void disableAllButtons(boolean disable) {
        this.FXbuttons.setDisable(disable);
        this.TOOLS.setDisable(disable);
        double d = disable ? 0.5 : 1;
        BACKGROUND.setOpacity(d-0.3);
        this.FXbuttons.setOpacity(d);
        PLAY_PAUSE.setOpacity(d);
        STOP.setOpacity(d);
        PREVIOUS.setOpacity(d);
        NEXT.setOpacity(d);
    }

    /* ************************************************************************************************************** */










     /* *************************************** CHIAMATI USANDO LA RICERCA ****************************************** */

    public static void viewAll() {
        runLater(() -> {
            table.setItems(data);
            table.getRowFactory().call(table);
            allSongs = table.getItems();
        });
    }

    public static void viewSearchedAlbumArtist(final String albumArtist) {
        final ObservableList<Song> dataAlbumArtist = FXCollections.observableArrayList();
        data.forEach(d -> {
            if (stringCompare.stringEquals(d.getAlbumartistsort(), albumArtist, true) ||
                    stringCompare.stringEquals(d.getAlbumartist(), albumArtist, true)) {
                dataAlbumArtist.add(d);
            }
        });
        runLater(() -> {
            table.setItems(dataAlbumArtist);
            table.getRowFactory().call(table);
            allSongs = table.getItems();
        });
    }

    public static void viewSearchedAlbum(final String album) {
        final ObservableList<Song> dataAlbum = FXCollections.observableArrayList();
        data.forEach(d -> {
            if (stringCompare.stringEquals(d.getAlbum(), album, false)) {
                dataAlbum.add(d);
            }
        });
        runLater(() -> {
            table.setItems(dataAlbum);
            table.getRowFactory().call(table);
            allSongs = table.getItems();
        });
    }

    public static void viewSearchedTitle(final String title) {
        final ObservableList<Song> dataTitle = FXCollections.observableArrayList();
        data.forEach(d -> {
            if (stringCompare.stringEquals(d.getTitle(), title, false)) {
                dataTitle.add(d);
            }
        });
        runLater(() -> {
            table.setItems(dataTitle);
            table.getRowFactory().call(table);
            allSongs = table.getItems();
        });
    }

    public static void viewSearchedArtist(final String artist) {
        final ObservableList<Song> dataArtist = FXCollections.observableArrayList();
        data.forEach(d -> {
            if (stringCompare.stringEquals(d.getArtistsort(), artist, true) ||
                    stringCompare.stringEquals(d.getArtist(), artist, true)) {
                dataArtist.add(d);
            }
        });
        runLater(() -> {
            table.setItems(dataArtist);
            allSongs = table.getItems();
        });
    }

    /* ************************************************************************************************************** */










    /* **************************************** GESTIONE CANZONI DA ESEGUIRE **************************************** */

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
        data.clear();
        final ProgressDialog progressDialog = new ProgressDialog();
        progressDialog.start();
        new Thread(() -> {
            try {
                musicDirectory = selectedDir.getAbsolutePath();
                final MusicDictionary MusicDict = new MusicDictionary(musicDirectory);
                /*this.number_of_files.set(0);
                //System.out.println("numero di files nella cartella selezionata: "+this.number_of_files.get());
                TOTAL.set(this.number_of_files.get());*/
                final Map<String, String[]> map = MusicDict.getMap();
                final HashMap<String, HashSet<String>> searchItems = new HashMap<>();

                searchItems.put(ATTRS[2], new HashSet<>());
                searchItems.put(ATTRS[5], new HashSet<>());
                searchItems.put(ATTRS[8], new HashSet<>());
                searchItems.put(ATTRS[10], new HashSet<>());

                map.forEach((k, v) -> {
                    data.add(new Song(k, v));
                    searchItems.get(ATTRS[2]).add(v[1].length() == 0 ? v[0] : v[1]);    // 2   Albumartist (sort) OR Albumartist
                    searchItems.get(ATTRS[5]).add(v[4]);    // 5   Album
                    searchItems.get(ATTRS[8]).add(v[7]);    // 8   Title
                    searchItems.get(ATTRS[10]).add(v[9].length() == 0 ? v[8] : v[9]);   // 10  Artist (sort) OR Artist
                });

                viewAll();
                // runLater...

                this.disableAllButtons(false);
                PREVIOUS.setButtonDisabled(true);
                NEXT.setButtonDisabled(true);
                STOP.setButtonDisabled(true);
                this.graphic.setSearchItems(searchItems);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    /**
     * Calcola la lista delle prossime canzoni da eseguire (a partire da quella attualmente selezionata)
     * @return una lista di canzoni
     */
    private List<Song> getSongsFromSelectedTrack() {
        if (this.selectedSongs.size() == 0) return allSongs;
        else if (this.selectedSongs.size() > 1) return this.selectedSongs;
        final Song song = this.selectedSongs.get(0);
        for (Song t : allSongs) {
            if (Objects.equals(t, song)) return allSongs;
            firstSongIndex++;
        }
        return Collections.emptyList();
    }

    /**
     * Si occupa di gestire la canzone attuale, in base alle scelte dell'utente
     */
    private void playpause() {
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
            if (changedSongs()) {
                this.lastListenedSongs.clear();
                this.lastListenedSongs.addAll(this.selectedSongs);
                this.firstSongIndex = 0;
                System.out.println(this.lastListenedSongs);
                Play.readyForNewPlaylist(this.getSongsFromSelectedTrack(), firstSongIndex);
            }
            waitCall();
            Play.play();
        }
        else {
            //this.lastListenedSongs.clear();
            //this.lastListenedSongs.addAll(this.selectedSongs);
            this.firstSongIndex = 0;
            waitCall();
            Play.readyForNewPlaylist(this.getSongsFromSelectedTrack(), firstSongIndex);
            Play.play();
        }
    }

    /**
     * Forza l'attesa di almeno 900 millisecondi ad un processo
     */
    private void waitCall() {
        long sleep = System.currentTimeMillis() - lastPlayCall;
        lastPlayCall = System.currentTimeMillis();
        if (sleep < 900) try {
            Thread.sleep(sleep);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /* ************************************************************************************************************** */

}