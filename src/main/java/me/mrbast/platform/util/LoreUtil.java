package me.mrbast.platform.util;

import java.util.List;

public class LoreUtil {




    public static List<String> toLore(String formatted){
        return List.of(formatted.split("\r?\n"));
    }
}
