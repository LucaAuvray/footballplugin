package fr.codinbox.footballplugin.utils;

public class StringUtils {

    public static String getOrShort(String str, int chars) {
        if(str.length() <= chars)
            return str;
        return str.substring(0, chars);
    }

}
