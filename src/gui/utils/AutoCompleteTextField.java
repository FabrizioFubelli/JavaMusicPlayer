package gui.utils;

import gui.Main;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;

import java.util.*;
import java.util.stream.Collectors;

import static gui.Main.ATTRS;
import static gui.Main.stringCompare;

/**
 * This class is a TextField which implements an "autocomplete" functionality, based on a supplied list of entries.
 * @author Caleb Brinkman
 */
class AutoCompleteTextField extends TextField
{
    private final ArrayList<MusicElement> entries;
    private final ContextMenu entriesPopup;
    private final Image typeAlbumArtist = new Image(getClass().getResource("albumartist.png").toString(), 35, 35,false,false);
    private final Image typeAlbum = new Image(getClass().getResource("album.jpg").toString(), 35, 35,false,false);
    private final Image typeTitle = new Image(getClass().getResource("title.png").toString(), 35, 35,false,false);
    private final Image typeArtist = new Image(getClass().getResource("artist.png").toString(), 35, 35,false,false);

    AutoCompleteTextField() {
        super();

        this.setOnAction(event -> {
            if (this.getText().isEmpty()) Main.viewAll();
        });

        this.entries = new ArrayList<>();
        this.entriesPopup = new ContextMenu();

        textProperty().addListener((observableValue, s, s2) -> {
            final String text = this.getText();
            if (text.length() == 0) {
                this.entriesPopup.hide();
            } else {
                final LinkedList<MusicElement> searchResult = new LinkedList<>();

                int i;
                final ArrayList<Integer> indexes = new ArrayList<>();

                for (i = 0; i < entries.size(); i++) {
                    final MusicElement element = entries.get(i);
                    if (stringCompare.stringStartWith(text, element.comparableValue, element.isArtist())) {
                        indexes.add(i);
                    }
                }

                if (indexes.size() < 10) {
                    for (i = 0; i < entries.size(); i++) {
                        if (!indexes.contains(i)) {
                            final MusicElement element = entries.get(i);
                            if (stringCompare.stringContains(text, element.comparableValue, element.isArtist())) {
                                indexes.add(i);
                            }
                        }
                    }
                }

                if (entries.size() > 0 && indexes.size() > 0) {
                    indexes.forEach(index -> searchResult.add(entries.get(index)));
                    populatePopup(searchResult);
                    if (!this.entriesPopup.isShowing()) {
                        this.entriesPopup.show(AutoCompleteTextField.this, Side.BOTTOM, 0, 0);
                    }
                } else {
                    this.entriesPopup.hide();
                }
            }
        });

        focusedProperty().addListener((observableValue, aBoolean, aBoolean2) -> this.entriesPopup.hide());

    }

    void setEntries(HashMap<String, HashSet<String>> map) {
        this.entries.clear();
        this.entries.addAll(map.get(ATTRS[2]).stream().map(e -> new MusicElement(e, MusicElementType.ALBUMARTIST)).collect(Collectors.toSet()));
        this.entries.addAll(map.get(ATTRS[5]).stream().map(e -> new MusicElement(e, MusicElementType.ALBUM)).collect(Collectors.toSet()));
        this.entries.addAll(map.get(ATTRS[8]).stream().map(e -> new MusicElement(e, MusicElementType.TITLE)).collect(Collectors.toSet()));
        this.entries.addAll(map.get(ATTRS[10]).stream().map(e -> new MusicElement(e, MusicElementType.ARTIST)).collect(Collectors.toSet()));
    }

    /**
     * Populate the entry set with the given search results.  Display is limited to 10 entries, for performance.
     * @param searchResult The set of matching strings.
     */
    private void populatePopup(List<MusicElement> searchResult) {

        final boolean[] aa_al_t_ar = {false, false, false, false};
        final int maxEntries = 50;
        final int count = Math.min(searchResult.size(), maxEntries);
        this.entriesPopup.getItems().clear();

        MusicElement last_result = null;

        for (int i = 0; i < count; i++) {
            final MusicElement result = searchResult.get(i);
            if (!Objects.isNull(last_result) && !Objects.equals(last_result, result)) {
                for (int j = 0; j < aa_al_t_ar.length; j++) aa_al_t_ar[j] = false;
            }

            last_result = result;

            GridPane entryPane = new GridPane();

            final ImageView type = new ImageView();
            final Label space = new Label("  ");
            final Label res = new Label(result.realValue);

            entryPane.add(type, 0, i);
            entryPane.add(space, 1, i);
            entryPane.add(res, 2, i);
            entryPane.setAlignment(Pos.CENTER_LEFT);

            CustomMenuItem item = new CustomMenuItem(entryPane, true);

            if (!aa_al_t_ar[0] && result.type == MusicElementType.ALBUMARTIST) {
                aa_al_t_ar[0] = true;
                type.setImage(this.typeAlbumArtist);
                item.setOnAction(actionEvent -> {
                    setText(result.realValue);
                    entriesPopup.hide();
                    Main.viewSearchedAlbumArtist(result.comparableValue);
                });
            } else if (!aa_al_t_ar[1] && result.type == MusicElementType.ALBUM) {
                aa_al_t_ar[1] = true;
                type.setImage(this.typeAlbum);
                item.setOnAction(actionEvent -> {
                    setText(result.realValue);
                    entriesPopup.hide();
                    Main.viewSearchedAlbum(result.comparableValue);
                });
            } else if (!aa_al_t_ar[2] && result.type == MusicElementType.TITLE) {
                aa_al_t_ar[2] = true;
                type.setImage(this.typeTitle);
                item.setOnAction(actionEvent -> {
                    setText(result.realValue);
                    entriesPopup.hide();
                    Main.viewSearchedTitle(result.comparableValue);
                });
            } else if (!aa_al_t_ar[3] && result.type == MusicElementType.ARTIST) {
                aa_al_t_ar[3] = true;
                type.setImage(this.typeArtist);
                item.setOnAction(actionEvent -> {
                    setText(result.realValue);
                    entriesPopup.hide();
                    Main.viewSearchedArtist(result.comparableValue);
                });
            }

            this.entriesPopup.getItems().add(item);
        }

        //CustomMenuItem item = new CustomMenuItem(entryPane, true);
        //menuItems.add(item);

    }


    enum MusicElementType {
        ALBUMARTIST,
        ARTIST,
        TITLE,
        ALBUM
    }


    public class MusicElement {
        private final String realValue;
        private final String comparableValue;
        private final MusicElementType type;
        private MusicElement(String realValue, MusicElementType type) {
            this.realValue = realValue;
            this.type = type;
            this.comparableValue = stringCompare.removeSpecialChars(this.realValue, this.isArtist());
        }

        private boolean isArtist() {
            return this.type == MusicElementType.ARTIST || this.type == MusicElementType.ALBUMARTIST;
        }

        @Override
        public boolean equals(Object o) {
            if (!Objects.isNull(o) && o.getClass() == this.getClass()) {
                MusicElement other = (MusicElement) o;
                return Objects.equals(this.realValue, other.realValue) && this.type == other.type;
            }
            return false;
        }
    }
}