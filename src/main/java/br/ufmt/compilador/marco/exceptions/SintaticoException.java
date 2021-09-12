package br.ufmt.compilador.marco.exceptions;

import br.ufmt.compilador.marco.Helper;
import br.ufmt.compilador.marco.Token;
import br.ufmt.compilador.marco.Token.Tag;

public class SintaticoException extends RuntimeException {
  public SintaticoException(String mensagem) {
    super("Erro sintático: " + mensagem);
  }

  public SintaticoException(String mensagem, int linha) {
    super("Erro sintático (linha " + linha + "): " + mensagem);
  }

  public SintaticoException(Token token, Integer linha, Tag tagEsperada) {
    super("Erro sintático (linha " + linha + "): Esperado um token com o tipo " + tagEsperada
        + ", mas foi recebido um token com o tipo " + token.getTipo() + " e com valor: \"" + token.getValor() + "\"");
  }

  public SintaticoException(Token token, Integer linha, Tag... tagEsperada) {
    super("Erro sintático (linha " + linha + "): Esperado um token com algum dos tipos: "
        + Helper.listaTagsString(tagEsperada) + ", mas o token recebido tem o tipo " + token.getTipo() + " e valor: \""
        + token.getValor() + "\"");
  }

  public SintaticoException(Token token, Integer linha, String nomeEsperado) {
    super("Erro sintático (linha " + linha + "): Esperado \"" + nomeEsperado + "\", mas foi recebido \""
        + token.getValor() + "\"");
  }

  public SintaticoException(Token token, Integer linha, String... nomeEsperado) {
    super("Erro sintático (linha " + linha + "): Esperado algum dos nomes \"" + nomeEsperado + "\", mas foi recebido \""
        + token.getValor() + "\"");
  }

}
