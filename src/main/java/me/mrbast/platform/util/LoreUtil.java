package me.mrbast.platform.util;

import java.util.Arrays;
import java.util.List;

public class LoreUtil {




    public static List<String> toLore(String formatted){
        return Arrays.asList(formatted.split("\r?\n"));
    }
}
