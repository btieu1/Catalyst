package org.catalyst.test;

import org.catalyst.extract.Extractor;
import org.catalyst.json.JSONParser;

import java.io.FileNotFoundException;
import java.io.FileReader;

public final class TestMain {

    public static void main(final String[] args)
            throws FileNotFoundException {
        
        if (args.length != 1) {

            System.err.println("usage: jsonPath");
            
            System.exit(1);
            
        }
        
        final JSONParser jsonParser = new JSONParser(new FileReader(args[0]));

        final Object json = jsonParser.parse();
        
        Extractor.extract(json);
        
    }
    
    private TestMain() { }
    
}