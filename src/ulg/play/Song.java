package ulg.play;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

import java.util.Objects;

/**
 * Created by: Fabrizio Fubelli
 * Date: 10/01/2017.
 */
public class Song {
    private final SimpleStringProperty file = new SimpleStringProperty();
    private final SimpleStringProperty albumartist = new SimpleStringProperty();        // 0
    private final SimpleStringProperty albumartistsort = new SimpleStringProperty();    // 1
    private final SimpleIntegerProperty disc_number = new SimpleIntegerProperty();      // 2
    private final SimpleIntegerProperty disc_total = new SimpleIntegerProperty();       // 3
    private final SimpleStringProperty album = new SimpleStringProperty();              // 4
    private final SimpleIntegerProperty track = new SimpleIntegerProperty();            // 5
    private final SimpleIntegerProperty track_total = new SimpleIntegerProperty();       // 6
    private final SimpleStringProperty title = new SimpleStringProperty();              // 7
    private final SimpleStringProperty artist = new SimpleStringProperty();             // 8
    private final SimpleStringProperty artistsort = new SimpleStringProperty();         // 9
    private final SimpleStringProperty genre = new SimpleStringProperty();              // 10
    private final SimpleDoubleProperty duration = new SimpleDoubleProperty();           // 11
    private final SimpleIntegerProperty bitrate = new SimpleIntegerProperty();          // 12
    private final SimpleIntegerProperty filesize = new SimpleIntegerProperty();         // 13
    private final SimpleStringProperty type = new SimpleStringProperty();               // 14

    public Song(String f, String[] attrs) {
        this.file.set(f);
        this.albumartistsort.set(attrs[0]);
        this.albumartist.set(attrs[1]);
        try {
            this.disc_number.set(Integer.valueOf(attrs[2]));
        } catch (Exception ignored) {
        }
        try {
            this.disc_total.set(Integer.valueOf(attrs[3]));
        } catch (Exception ignored) {
        }
        this.album.set(attrs[4]);
        try {
            this.track.set(Integer.valueOf(attrs[5]));
        } catch (Exception ignored) {
        }
        try {
            this.track_total.set(Integer.valueOf(attrs[6]));
        } catch (Exception ignored) {
        }
        this.title.set(attrs[7]);
        this.artist.set(attrs[8]);
        this.artistsort.set(attrs[9]);

        this.genre.set(attrs[10]);
        try {
            this.duration.set(Double.valueOf(attrs[11]));
        } catch (Exception ignored) {
        }
        try {
            this.bitrate.set(Integer.valueOf(attrs[12]));
        } catch (Exception ignored) {
        }
        try {
            this.filesize.set(Integer.valueOf(attrs[13]));
        } catch (Exception ignored) {
        }
        this.type.set(attrs[14]);
    }

    public String getFile() { return this.file.get(); }
    public void setFile(String f) {
        this.file.set(f);
    }
    public String getAlbumartist() { return this.albumartist.get(); }
    public void setAlbumartist(String aa) { this.albumartist.set(aa); }
    public String getAlbumartistsort() { return this.albumartistsort.get(); }
    public void setAlbumartistsort(String aa) { this.albumartistsort.set(aa); }
    public Integer getDisc_number() { return this.disc_number.get() == 0 ? null : this.disc_number.get(); }
    public void setDisc_number(String dn) { this.disc_number.set(Integer.valueOf(dn)); }
    public Integer getDisc_total() { return this.disc_total.get() == 0 ? null : this.disc_total.get(); }
    public void setDisc_total(String dt) { this.disc_total.set(Integer.valueOf(dt)); }
    public String getAlbum() { return this.album.get(); }
    public void setAlbum(String a) {this.album.set(a); }
    public Integer getTrack() { return this.track.get() == 0 ? null : this.track.get(); }
    public void setTrack(String t) { this.track.set(Integer.getInteger(t)); }
    public Integer getTrack_total() { return this.track_total.get() == 0 ? null : this.track_total.get(); }
    public void setTrack_total(String t) { this.track_total.set(Integer.getInteger(t)); }
    public String getTitle() { return this.title.get(); }
    public void setTitle(String t) {this.title.set(t); }
    public String getArtist() { return this.artist.get(); }
    public void setArtist(String a) { this.artist.set(a); }
    public String getArtistsort() { return this.artistsort.get(); }
    public void setArtistsort(String a) { this.artistsort.set(a); }
    public String getGenre() { return this.genre.get(); }
    public void setGenre(String a) { this.genre.set(a); }
    public Double getDuration() { return this.duration.get() == 0 ? null : this.duration.get(); }
    public void setDuration(String d) { this.duration.set(Integer.valueOf(d)); }
    public Integer getBitrate() { return this.bitrate.get() == 0 ? null : this.bitrate.get(); }
    public void setBitrate(String b) { this.bitrate.set(Integer.valueOf(b)); }
    public Integer getFilesize() { return this.filesize.get() == 0 ? null : this.filesize.get(); }
    public void setFilesize(String s) { this.filesize.set(Integer.valueOf(s)); }
    public String getType() { return this.type.get(); }

    @Override
    public String toString() {
        return "Song("+this.file.get()+")";
    }

    @Override
    public boolean equals(Object o) {
        if (!Objects.isNull(o) && o.getClass() == this.getClass()) {
            Song other = (Song) o;
            return Objects.equals(this.file.get(), other.file.get());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.file.get());
    }
}
