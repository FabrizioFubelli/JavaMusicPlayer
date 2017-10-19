package ulg.programs;


import ulg.programs.python_sorting.PythonSorting;
import ulg.programs.utils.OsCheck;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by: Fabrizio Fubelli
 * Date: 11/01/2017.
 */


public class ExternalPrograms {

    public static final OsCheck.OSType OS = OsCheck.getOperatingSystemType();


    public enum Program {
        PY_SORTING
    }

    /**
     * Runs Fabri MusicSort program in the Windows system. Please note that this assumes
     * you are running this program on Windows.
     */
    public static void openExternalProgram(Program program) {
        if (program == Program.PY_SORTING) {
            PythonSorting pythonSorting = new PythonSorting();
            List<String> commands = new ArrayList<>();
            if (OS == OsCheck.OSType.Windows) {
                commands.addAll(Arrays.asList("cmd", "START", "CMD.EXE", "/C", "MusicSort.bat"));
            } else {
                commands.addAll(Arrays.asList("/bin/bash", "MusicSort.bat"));
            }
            Process pr;
            try {
                pr = Runtime.getRuntime().exec(commands.toArray(new String[commands.size()]), null, pythonSorting.getMusicSortURL());
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
            try {
                pr.waitFor();
            } catch (InterruptedException ignored) {
            }
            pr.destroy();
        }
    }

}