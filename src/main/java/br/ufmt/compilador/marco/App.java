package br.ufmt.compilador.marco;

public class App {
  public static void main(String[] args) {
    // Sintatico2 s = new Sintatico2("input2.txt");
    // s.analise();

    Sintatico s = new Sintatico("inputLexico.txt");
    s.analisar();
  }
}
