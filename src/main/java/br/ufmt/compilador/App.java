package br.ufmt.compilador;

import br.ufmt.compilador.marco.Lexico;
import br.ufmt.compilador.marco.Sintatico;
import br.ufmt.compilador.marco.Token;

public class App {
  public static void main(String[] args) {
    Lexico lexico = new Lexico("inputLexico.txt");

    Token token = null;

    System.out.println("---------------- Análise léxica ----------------");
    do {
      token = lexico.nextToken();
      System.out.println(token);
    } while (token != null);

    System.out.println("---------------- Análise sintática ----------------");
    Sintatico sintatico = new Sintatico("inputLexico.txt");
    sintatico.analisar();

    // s.analise();
  }
}
