package org.catalyst.test;

import org.catalyst.extract.Extractor;
import org.catalyst.json.JSONParser;

import java.io.StringReader;

public final class TestMain {

    public static void main(final String[] args) {
        
        final String json = """
                {
                                
                  "title": "Software Developer",
                  "experience": "2-3",
                  "related jobs": [
                    
                    "Full stack Developer",
                    "Data analyst"
                    
                  ]
                                
                }
                """;
        
        final JSONParser jsonParser = new JSONParser(new StringReader(json));
        
        jsonParser.parse();
        
        Extractor.extract(jsonParser.getJSON());
        
    }
    
    private TestMain() { }
    
}