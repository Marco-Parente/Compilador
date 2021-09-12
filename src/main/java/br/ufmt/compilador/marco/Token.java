package br.ufmt.compilador.marco;

public class Token {

  public enum Tag {
    INTEGER, REAL, IDENTIFICADOR, SIMBOLO, PALAVRA_RESERVADA,
  }

  private String valor;
  private Tag tipo;

  public Token(Tag tipo, String valor) {
    this.valor = valor;
    this.tipo = tipo;
  }

  public String getValor() {
    return this.valor;
  }

  public void setValor(String valor) {
    this.valor = valor;
  }

  public Tag getTipo() {
    return this.tipo;
  }

  public void setTipo(Tag tipo) {
    this.tipo = tipo;
  }

  @Override
  public String toString() {
    return "Token [" + tipo.name() + ", " + valor + "]";
  }

}
