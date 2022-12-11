package org.catalyst.extract;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.regex.Pattern;

public final class Extractor {
    
    private static final Pattern SANITIZER = Pattern.compile("\\\\n");
    
    private static void use(final Object json,
                            final int indentLevel) {
        
        final Consumer<Object> println = object -> System.out.println("    ".repeat(indentLevel) + SANITIZER
                .matcher(object.toString())
                .replaceAll(matchResult -> "")
                .trim());
        
        if (json instanceof Map<?,?> map) {
            
            map.forEach((object1, object2) -> {

                println.accept(object1);
                
                use(object2, indentLevel + 1);
                
            });
            
        } else if (json instanceof List<?> list) {
            
            list.forEach(object -> use(object, indentLevel));
            
        } else {
            
            println.accept(json);
            
        }
        
    }
    
    private static void use(final Object json) {
        
        use(json, 0);
        
    }
    
    public static void extract(final Object json) {
        
        use(json);
        
    }
    
    private Extractor() { }
    
}