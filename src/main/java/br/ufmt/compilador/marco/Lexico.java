package br.ufmt.compilador.marco;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;

import br.ufmt.compilador.marco.exceptions.LexicoException;

public class Lexico {

  private char[] conteudo;
  private int estado;
  private int pos;

  public int linha = 1;
  private HashMap<String, Token> reservadas = new HashMap<String, Token>() {
    {
      put("program", new Token(Token.Tag.PALAVRA_RESERVADA, "program"));
      put("begin", new Token(Token.Tag.PALAVRA_RESERVADA, "begin"));
      put("end", new Token(Token.Tag.PALAVRA_RESERVADA, "end"));
      put("write", new Token(Token.Tag.PALAVRA_RESERVADA, "write"));
      put("read", new Token(Token.Tag.PALAVRA_RESERVADA, "read"));
      put("if", new Token(Token.Tag.PALAVRA_RESERVADA, "if"));
      put("then", new Token(Token.Tag.PALAVRA_RESERVADA, "then"));
      put("else", new Token(Token.Tag.PALAVRA_RESERVADA, "else"));
      put("integer", new Token(Token.Tag.PALAVRA_RESERVADA, "integer"));
      put("real", new Token(Token.Tag.PALAVRA_RESERVADA, "real"));
      put("while", new Token(Token.Tag.PALAVRA_RESERVADA, "while"));
      put("do", new Token(Token.Tag.PALAVRA_RESERVADA, "do"));
    }
  };

  public Lexico(String arq) {
    try {
      byte[] bytes = Files.readAllBytes(Paths.get(arq));
      conteudo = (new String(bytes)).toCharArray();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private boolean isLetra(char c) {
    return ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z'));
  }

  private boolean isDigito(char c) {
    return ((c >= '0' && c <= '9'));
  }

  private boolean isEspaco(char c) {
    return (c == ' ' || c == '\n' || c == '\t' || c == '\r');
  }

  private boolean isSimbolo(char c) {
    return (c == '=' || c == '<' || c == '>' || c == '=' || c == ':' || c == ';' || c == '.' || c == ',' || c == '$'
        || c == '+' || c == '*' || c == '/' || c == '-' || c == '(' || c == ')' || c == '{' || c == '}');
  }

  private boolean isEOF() {
    return pos >= conteudo.length;
  }

  private char nextChar() {
    if (isEOF()) {
      return 0;
    }
    return conteudo[pos++];
  }

  private void back() {
    pos--;
  }

  public Token nextToken() {
    if (isEOF()) {
      return null;
    }

    estado = 0;
    char c;
    String termo = "";

    while (true) {
      if (isEOF()) {
        pos = conteudo.length + 1;
      }

      c = nextChar();

      switch (estado) {
        case 0:
          if (isEspaco(c)) {
            if (c == '\n') {
              linha++;
            }
            estado = 0;
          } else if (isDigito(c)) {
            estado = 1;
            termo += c;
          } else if (isLetra(c)) {
            estado = 4;
            termo += c;
          } else if (isSimbolo(c)) {
            if (c == '<') {
              estado = 5;
              termo += c;
            } else if (c == '>') {
              estado = 7;
              termo += c;
            } else if (c == ':') {
              estado = 7;
              termo += c;
            } else if (c == '/') {
              termo += c;
              c = nextChar();
              if (c == '*') {
                termo += c;
                estado = 10;
              } else {
                back();
                estado = 9;
              }
            } else if (c == '{') {
              estado = 11;
              termo += c;
            } else {
              estado = 9;
              termo += c;
            }
          } else {
            if (c == 0) {
              return null;
            }
            termo += c;
            throw new LexicoException("Token não não reconhecido: " + c);
          }
          break;
        case 1:
          if (isDigito(c)) {
            estado = 1;
            termo += c;
          } else if (c == '.') {
            estado = 2;
            termo += c;
          } else {
            back();
            return new Token(Token.Tag.INTEGER, termo);
          }
          break;
        case 2:
          if (isDigito(c)) {
            estado = 3;
            termo += c;
          } else {
            throw new RuntimeException("Erro léxico. Número esperado, recebido: " + c);
          }
          break;
        case 3:
          if (isDigito(c)) {
            estado = 3;
            termo += c;
          } else {
            back();
            return new Token(Token.Tag.REAL, termo);
          }
          break;
        case 4:
          if (isLetra(c) || isDigito(c)) {
            estado = 4;
            termo += c;
          } else {
            back();
            Token palavraReservada = reservadas.get(termo);
            if (palavraReservada != null) {
              return new Token(palavraReservada.getTipo(), palavraReservada.getValor());
            }
            return new Token(Token.Tag.IDENTIFICADOR, termo);
          }
          break;
        case 5:
          if (c == '=' || c == '>') {
            estado = 6;
            termo += c;
          } else {
            back();
            return new Token(Token.Tag.SIMBOLO, termo);
          }
          break;
        case 6:
          back();
          return new Token(Token.Tag.SIMBOLO, termo);
        case 7:
          if (c == '=') {
            estado = 8;
            termo += c;
          } else {
            back();
            return new Token(Token.Tag.SIMBOLO, termo);
          }
          break;
        case 8:
          back();
          return new Token(Token.Tag.SIMBOLO, termo);
        case 9:
          back();
          return new Token(Token.Tag.SIMBOLO, termo);
        case 10:
          termo += c;
          if (c == '*') {
            c = nextChar();
            termo += c;
            if (c == '/') {
              return new Token(Token.Tag.COMENTARIO, termo);
            }
          }
          break;
        case 11:
          termo += c;
          if (c == '}') {
            return new Token(Token.Tag.COMENTARIO, termo);
          }
          break;
      }
    }
  }

}
