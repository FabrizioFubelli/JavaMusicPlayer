package gui.utils;

import java.util.HashMap;
import java.util.Objects;

/**
 * Created by Fabrizio
 * Date: 07/08/2017.
 */
public class StringCompare {
    private final HashMap<String, String> converted_cache = new HashMap<>();
    private final String[] ARTICOLI = {"I ", "IL ", "LO ", "LA ", "LE ", "L  ", "THE ","GLI "};
    private final char[] ACC_INDEX = {'A', 'E', 'I', 'O', 'U', 'C', 'H', 'M', 'N', 'P', 'S', 'T', 'W'};
    private final String[] ACC = {
            "ÀÁÂÃÄÅÆĀĂĄǞǠǢǺǼȀȂȺΑΔΙӐӒӔᴀᴁᴧᴭḀẠẢẤẦẨẪẬẮẰẲẴẶἈἉἊἋἌἍἎἏᾸᾹᾺΆⱯ".toUpperCase(), //sorted
            "ÈÉÊËĒĔĖĘĚƎȄȆɆΈΕЀЁӖᴇᴱᴲḔḖḘḚḜẸẺẼẾỀỂỄỆἘἙἚἛἜἝῈΈΣ&".toUpperCase(),            //sorted
            "ÌÍÎÏĨĪĬĮİƗǏȈȊ̀́̈͂ΊΙΪЇḬḮỈỊἸἹἺἻἼἽἾἿῘῙῚΊ!".toUpperCase(),                           //sorted
            "ÒÓÔÕÖØŌŎŐŒƟƠǑǪǬǾȌȎȪȬȮȰΌΟОᴏᴓṌṎṐṒỌỎỐỒỔỖỘỚỜỞỠỢὈὉὊὋὌὍῸΌ".toUpperCase(),   //sorted
            "ÙÚÛÜŨŪŬŮŰŲǓǕǗǙǛȔȖɄЦᴜṲṴṶṸṺỤỦỨỪỬỮỰ".toUpperCase(),                           //sorted
            "СÇ".toUpperCase(),
            "ΉΗ".toUpperCase(),
            "Μ".toUpperCase(),
            "ЙЛÑ".toUpperCase(),
            "Ρ".toUpperCase(),
            "$".toUpperCase(),                                                           //sorted
            "ΤТ".toUpperCase(),                                                          //sorted
            "Ẁ".toUpperCase(),                                                           //sorted
    };

    public boolean stringEquals(String s1, String stringWithoutSpecChars, boolean isArtist) {
        return !Objects.isNull(s1) && !Objects.isNull(stringWithoutSpecChars) &&
                (Objects.equals(removeSpecialChars(s1, isArtist), stringWithoutSpecChars));

    }

    boolean stringStartWith(String stringToFind, String stringWithoutSpecChars, boolean isArtist) {
        return !Objects.isNull(stringToFind) && !Objects.isNull(stringWithoutSpecChars) &&
                stringToFind.length() > 0 && stringWithoutSpecChars.length() > 0 &&
                stringWithoutSpecChars.indexOf(removeSpecialChars(stringToFind, isArtist)) == 0;

    }

    boolean stringContains(String stringToFind, String stringWithoutSpecChars, boolean isArtist) {
        return !Objects.isNull(stringToFind) && !Objects.isNull(stringWithoutSpecChars) &&
                stringToFind.length() > 0 && stringWithoutSpecChars.length() > 0 &&
                stringWithoutSpecChars.contains(removeSpecialChars(stringToFind, isArtist));

    }

    String removeSpecialChars(String s, boolean isArtist) {
        if (this.converted_cache.containsKey(s)) { return this.converted_cache.get(s); }
        int i;
        String new_s = s.toUpperCase();

        for (i = 0 ; i < ACC_INDEX.length ; i++) {
            for (char c : ACC[i].toCharArray()) new_s = new_s.replace(c, ACC_INDEX[i]);
        }

        for (char c : "\".,:;-_\\|/'".toCharArray()) new_s = new_s.replace(c, ' ');

        if (isArtist) {
            for (i = 0 ; i < ARTICOLI.length ; i++) {
                if (new_s.indexOf(ARTICOLI[i]) == 0) {
                    new_s = new_s.substring(ARTICOLI[i].length(), new_s.length());
                }
            }
        }

        while (new_s.contains(" ")) { new_s = new_s.replace(" ", ""); }

        this.converted_cache.put(s, new_s);

        return new_s;
    }
}
