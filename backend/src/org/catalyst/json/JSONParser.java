package org.catalyst.json;

import java.io.IOException;
import java.io.Reader;
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

    public void parse() {

        lookAheadToken = next();

        start();

        if (lookAheadToken != EOF_$) {

            throw syntaxException("EOF");

        }
        
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
    
    start : object
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
    
    entryValue : STRING
               | object
               | LEFT_BRACKET list RIGHT_BRACKET
               ;
    
    list : entryValue restOfList
         |
         ;
    
    restOfList : COMMA entryValue restOfList
               |
               ;
    
    */
    
    private void start() {
        
        object();
        
    }
    
    private void object() {
        
        match(LEFT_CURLY_BRACKET);
        
        entries();
        
        match(RIGHT_CURLY_BRACKET);
        
    }
    
    private void entries() {
        
        if (lookAheadToken == STRING) {
            
            entry();
            
            moreEntries();
            
        }
        
    }
    
    private void moreEntries() {
        
        if (tryMatch(COMMA).matched) {
            
            entry();
            
            moreEntries();
            
        }
        
    }
    
    private void entry() {
        
        match(STRING);
        
        match(COLON);
        
        entryValue();
        
    }
    
    private void entryValue() {
        
        final Result result = tryMatch(STRING);
        
        if (result.matched) {
            
            //
            
            return;
            
        }
        
        if (lookAheadToken == LEFT_CURLY_BRACKET) {
            
            object();
            
            return;
            
        }
        
        match(LEFT_BRACKET);
        
        list();
        
        match(RIGHT_BRACKET);
        
    }
    
    private void list() {
        
        if ((lookAheadToken == STRING)
                || (lookAheadToken == LEFT_CURLY_BRACKET)
                || (lookAheadToken == LEFT_BRACKET)) {
            
            entryValue();
            
            restOfList();
            
        }
        
    }
    
    private void restOfList() {
        
        if (tryMatch(COMMA).matched) {
            
            entryValue();
            
            restOfList();
            
        }
        
    }
    
    public Map<Object, Object> getJSON() {
        
        // TODO
        
        return null;
    }
    
}