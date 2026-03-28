package me.mrbast.platform.format;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;


/***
 * Used to set key:value passed to Formatter
 */
public class Format {

    private Map<String, Supplier<String>> values = new HashMap<>();

    private Format(){

    }

    public Map<String, Supplier<String>> getValues() {
        return values;
    }

    public static class FormatArgument{


        private String[] arguments;
        public FormatArgument(String[] parameters) {
            this.arguments = parameters;
        }


        public Format as(){
            return new Format();
        }


        public Format as(String... values){
            Format format = new Format();
            Map<String, Supplier<String>> map = format.getValues();
            for (int i = 0; i < arguments.length; i++) {
                int finalI = i;
                map.put(arguments[i], ()-> values[finalI]);
            }
            return format;
        }

        @SafeVarargs
        public final Format as(Supplier<String>... values){
            Format format = new Format();
            Map<String, Supplier<String>> map = format.getValues();
            for (int i = 0; i < arguments.length; i++) {
                map.put(arguments[i], values[i]);
            }
            return format;

        }
    }


    public static Supplier<String> str(String value){
        return ()->value;
    }

    public static FormatArgument of(String... parameters){
        return new FormatArgument(parameters);
    }


    @Override
    public String toString() {
        return "Format{" +
                "values=" + values +
                '}';
    }
}
