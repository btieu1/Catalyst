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
        
        TRUE,
        
        FALSE,
        
        NULL,
        
        NUMBER,
        
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
    
    private boolean didMatch(final Token token) {
        
        if (lookAheadToken == token) {

            lookAheadToken = next();
            
            return true;
            
        }
        
        return false;
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
    
    list : LEFT_BRACKET listValues RIGHT_BRACKET
         ;
    
    listValues : entryValue remainingListValues
               |
               ;
    
    remainingListValues : COMMA entryValue remainingListValues
                        |
                        ;
    
    entryValue : TRUE
               | FALSE
               | NULL
               | NUMBER
               | STRING
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
        
        match(LEFT_BRACKET);

        final List<Object> objects = new ArrayList<>();
        
        listValues(objects);
        
        match(RIGHT_BRACKET);
        
        return objects;
    }
    
    private void listValues(final List<Object> objects) {
        
        if ((lookAheadToken == TRUE)
                || (lookAheadToken == FALSE)
                || (lookAheadToken == NULL)
                || (lookAheadToken == NUMBER)
                || (lookAheadToken == STRING)
                || (lookAheadToken == LEFT_BRACKET)
                || (lookAheadToken == LEFT_CURLY_BRACKET)) {
            
            final Object object = entryValue();
            
            objects.add(object);
            
            remainingListValues(objects);
            
        }
        
    }

    private void remainingListValues(final List<Object> objects) {

        if (didMatch(COMMA)) {
            
            final Object object = entryValue();
            
            objects.add(object);
            
            remainingListValues(objects);
            
        }
        
    }
    
    private Object entryValue() {
        
        if (didMatch(TRUE)) {
            
            return true;
            
        } else if (didMatch(FALSE)) {

            return false;

        } else if (didMatch(NULL)) {
            
            return null;
            
        }
        
        Result result = tryMatch(NUMBER);
        
        if (result.matched()) {
            
            return Double.parseDouble(result.text());
            
        }
        
        result = tryMatch(STRING);
        
        if (result.matched()) {
            
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

        if (didMatch(COMMA)) {

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