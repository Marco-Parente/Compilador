package br.ufmt.compilador.marco.exceptions;

public class SemanticoException extends RuntimeException {
  public SemanticoException(String mensagem) {
    super("Erro semântico: " + mensagem);
  }

  public SemanticoException(String mensagem, int linha) {
    super("Erro semântico (linha " + linha + "): " + mensagem);
  }
}
