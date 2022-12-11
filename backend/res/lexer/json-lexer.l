package org.catalyst.json;

import static org.catalyst.json.JSONParser.Token.*;

%%

%final
%class JSONLexer
%unicode
%line
%column
%type JSONParser.Token

%eofval{

    return JSONParser.Token.EOF_$;

%eofval}

%{

    public int line() {

        return (yyline + 1);
    }

    public int column() {

        return (yycolumn + 1);
    }

%}

%%

"{" { return LEFT_CURLY_BRACKET; }

"}" { return RIGHT_CURLY_BRACKET; }

"[" { return LEFT_BRACKET; }

"]" { return RIGHT_BRACKET; }

":" { return COLON; }

"," { return COMMA; }

"true" { return TRUE; }

"false" { return FALSE; }

"null" { return NULL; }

[0-9]+ ("." [0-9]+)? ([eE] [0-9]+)? { return NUMBER; }

"\"" [^\"]* "\"" { return STRING; }

" " | \R | "\t" { /* WHITESPACE IGNORED */ }

[^] { throw new RuntimeException("unexpected token: " + yytext() + String.format(" [line %d, column %d]", line(), column())); }