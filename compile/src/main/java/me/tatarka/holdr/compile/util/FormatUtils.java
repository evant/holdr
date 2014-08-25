package me.tatarka.holdr.compile.util;

public class FormatUtils {
   private static String underscoreToCamel(String input, boolean firstLetterUppercase) {
       if (input == null) return null;
       StringBuilder builder = new StringBuilder();
       boolean firstFound = false;
       boolean capitalizeNext = firstLetterUppercase;
       for (char c: input.toCharArray()) {
           if (c == '_') {
               if (firstFound) {
                   capitalizeNext = true;
               }
           } else {
               firstFound = true;
               char appendChar = capitalizeNext ? Character.toUpperCase(c) : c;
               builder.append(appendChar);
               capitalizeNext = false;
           }
       }
       return builder.toString();
   }

    public static String underscoreToUpperCamel(String input) {
        return underscoreToCamel(input, true);
    }

    public static String underscoreToLowerCamel(String input) {
        return underscoreToCamel(input, false);
    }
}
