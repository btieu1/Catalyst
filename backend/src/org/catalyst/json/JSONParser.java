package org.catalyst.json;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.catalyst.json.JSONParser.Token.*;

public final class JSONParser {
    
    public enum Token {

        LEFT_CURLY_BRACKET("{"),
        
        RIGHT_CURLY_BRACKET("}"),
        
        LEFT_BRACKET("["),
        
        RIGHT_BRACKET("]"),
        
        COLON(":"),
        
        COMMA(","),
        
        STRING,
        
        EOF_$("end of file");

        private final String string;

        Token() {

            this.string = name().toLowerCase();

        }

        Token(final String string) {

            this.string = string;

        }

        @Override
        public String toString() {

            return string;
        }

    }

    private final JSONLexer jsonLexer;

    public JSONParser(final Reader reader) {

        jsonLexer = new JSONLexer(reader);

    }

    private Token next() {

        try {

            return jsonLexer.yylex();

        } catch (final IOException e) {

            throw new RuntimeException(e);

        }

    }

    private Token lookAheadToken;

    public Object parse() {

        lookAheadToken = next();

        final Object json = start();

        if (lookAheadToken != EOF_$) {

            throw syntaxException("EOF");

        }
        
        return json;
    }

    private RuntimeException syntaxException(final Token token) {

        return syntaxException(token.toString());
    }

    private RuntimeException syntaxException(final String expected) {

        return semanticException(String.format("expected %s but found %s", expected, jsonLexer.yytext()));
    }

    private RuntimeException semanticException(final String message) {

        return new RuntimeException(String.format("%s [line %d, column %d]",
                message, jsonLexer.line(), jsonLexer.column()));
    }

    private void match(final Token token) {

        if (lookAheadToken != token) {

            throw syntaxException(token);

        }

        lookAheadToken = next();

    }

    private String getMatch(final Token token) {

        if (lookAheadToken != token) {

            throw syntaxException(token);

        }

        final String text = jsonLexer.yytext();

        lookAheadToken = next();

        return text;
    }

    private record Result(String text, boolean matched) { }

    private static final Result NO = new Result(null, false);

    private Result tryMatch(final Token token) {

        if (lookAheadToken != token) {

            return NO;

        }

        final String text = jsonLexer.yytext();

        lookAheadToken = next();
        
        return new Result(text, true);
    }
    
    /*
    
    Grammar:
    
    start : list
          | object
          ;
    
    list : LEFT_BRACKET entryValue restOfList
         ;
    
    restOfList : COMMA entryValue restOfList
               | RIGHT_BRACKET
               ;
    
    entryValue : STRING
               | object
               | list
               ;
    
    object : LEFT_CURLY_BRACKET entries RIGHT_CURLY_BRACKET
           ;
    
    entries : entry moreEntries
            |
            ;
    
    moreEntries : COMMA entry moreEntries
                |
                ;
    
    entry : STRING COLON entryValue
          ;
    
    */
    
    private Object start() {
        
        if (lookAheadToken == LEFT_BRACKET) {

            return list();
            
        } else {
            
            return object();
            
        }
        
    }
    
    private List<Object> list() {
        
        final List<Object> objects = new ArrayList<>();
        
        match(LEFT_BRACKET);
        
        final Object object = entryValue();
        
        objects.add(object);
        
        restOfList(objects);
        
        return objects;
    }
    
    private void restOfList(final List<Object> objects) {
        
        if (tryMatch(COMMA).matched) {
            
            final Object object = entryValue();

            objects.add(object);
            
            restOfList(objects);
            
            return;
            
        }
        
        match(RIGHT_BRACKET);
        
    }

    private Object entryValue() {

        final Result result = tryMatch(STRING);

        if (result.matched) {
            
            final String text = result.text();
            
            return text.substring(1, (text.length() - 1));

        }

        if (lookAheadToken == LEFT_CURLY_BRACKET) {

            return object();

        }
        
        return list();
        
    }

    private Map<Object, Object> object() {

        match(LEFT_CURLY_BRACKET);
        
        final Map<Object, Object> objectMap = new HashMap<>();
        
        entries(objectMap);

        match(RIGHT_CURLY_BRACKET);
        
        return objectMap;
    }

    private void entries(final Map<Object, Object> objectMap) {

        if (lookAheadToken == STRING) {

            entry(objectMap);

            moreEntries(objectMap);

        }

    }

    private void moreEntries(final Map<Object, Object> objectMap) {

        if (tryMatch(COMMA).matched) {

            entry(objectMap);

            moreEntries(objectMap);

        }

    }

    private void entry(final Map<Object, Object> objectMap) {

        final String entry = getMatch(STRING);
        
        final String entryString = entry.substring(1, (entry.length() - 1));
        
        match(COLON);

        final Object value = entryValue();
        
        objectMap.put(entryString, value);
        
    }
    
}