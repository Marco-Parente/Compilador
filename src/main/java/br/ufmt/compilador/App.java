package br.ufmt.compilador;

import br.ufmt.compilador.marco.InterpretadorComp2;
import br.ufmt.compilador.marco.Lexico;
import br.ufmt.compilador.marco.SintaticoComp2;
import br.ufmt.compilador.marco.Token;

public class App {
  public static void main(String[] args) {
    Lexico lexico = new Lexico("input2.txt");

    Token token = null;

    System.out.println("---------------- Análise léxica ----------------");
    do {
      token = lexico.nextToken();
      System.out.println(token);
    } while (token != null);

    System.out.println("---------------- Análise sintática ----------------");
    SintaticoComp2 sintatico = new SintaticoComp2("input2.txt");
    sintatico.analisar();

    System.out.println("---------------- Interpretador ----------------");
    InterpretadorComp2 interpretador = new InterpretadorComp2("output.txt");
    interpretador.executar();
  }
}
