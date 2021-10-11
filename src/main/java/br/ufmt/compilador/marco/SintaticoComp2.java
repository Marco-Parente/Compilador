package br.ufmt.compilador.marco;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import br.ufmt.compilador.marco.Token.Tag;
import br.ufmt.compilador.marco.exceptions.SemanticoException;
import br.ufmt.compilador.marco.exceptions.SintaticoException;

public class SintaticoComp2 {

  private Lexico scan;
  private Token token;
  private Map<String, Entry<Token, Integer>> tabelaSimbolo = new HashMap<>();
  private Tag tipo;

  private int linhaCodigo;

  private List<String> c = new ArrayList<>();
  private int s = -1;

  public SintaticoComp2(String arq) {
    scan = new Lexico(arq);
  }

  private void obterSimbolo() {
    token = scan.nextToken();
    if (token != null && token.getTipo().equals(Token.Tag.COMENTARIO)) {
      obterSimbolo();
    }
  }

  private boolean verificarTipo(Tag tipo) {
    return token != null && token.getTipo() == tipo;
  }

  private boolean verificarValor(String valorEsperado) {
    return token != null && token.getValor().equals(valorEsperado);
  }

  private void verificarValorThrow(String valorEsperado) {
    if (token == null || !token.getValor().equals(valorEsperado)) {
      throw new SintaticoException(token, scan.linha, valorEsperado);
    }
  }

  private void code2(String codigo) {
    c.add(linhaCodigo++ + " - " + codigo);
  }

  private void programa() {
    System.out.println("programa");
    verificarValorThrow("program");
    code2("INPP");
    obterSimbolo();
    if (verificarTipo(Tag.IDENTIFICADOR)) {
      obterSimbolo();
      corpo();
      verificarValorThrow(".");
      obterSimbolo();
    } else {
      throw new SintaticoException(token, scan.linha, Tag.IDENTIFICADOR);
    }
  }

  private void corpo() {
    System.out.println("corpo");
    dc();
    verificarValorThrow("begin");
    comandos();
    obterSimbolo();
    verificarValorThrow("end");
    code2("PARA");
    obterSimbolo();
  }

  private void dc() {
    System.out.println("dc");
    if (verificarValor("real") || verificarValor("integer")) {
      dc_v();
      verificarValorThrow(";");
      obterSimbolo();
      dc();
    }
  }

  private void dc_v() {
    System.out.println("dc_v");
    tipo_var();
    verificarValorThrow(":");
    obterSimbolo();
    variaveis();
  }

  private void tipo_var() {
    System.out.println("tipo_var");
    if (!verificarValor("real") && !verificarValor("integer")) {
      throw new SintaticoException(token, scan.linha, Tag.REAL, Tag.INTEGER);
    }

    if (verificarValor("integer")) {
      tipo = Tag.INTEGER;
    } else {
      tipo = Tag.REAL;
    }
    obterSimbolo();
  }

  private void variaveis() {
    System.out.println("variaveis");
    if (!verificarTipo(Tag.IDENTIFICADOR)) {
      throw new SintaticoException(token, scan.linha, Tag.IDENTIFICADOR);
    }

    if (tabelaSimbolo.containsKey(token.getValor())) {
      throw new SemanticoException("A variável \"" + token.getValor() + "\" já foi definida anteriormente.",
          scan.linha);
    } else {
      code2("ALME 1");
      tabelaSimbolo.put(token.getValor(), Map.entry(new Token(tipo, token.getValor()), ++s));
    }

    obterSimbolo();
    mais_var();
  }

  private void mais_var() {
    System.out.println("mais_var");
    if (verificarValor(",")) {
      obterSimbolo();
      variaveis();
    }
  }

  private void comandos() {
    System.out.println("comandos");
    comando();
    mais_comandos();
  }

  private void mais_comandos() {
    System.out.println("mais_comandos");
    if (verificarValor(";")) {
      comandos();
    }
  }

  private void comando() {
    obterSimbolo();
    System.out.println("comando");
    if (verificarValor("read") || verificarValor("write")) {
      String operador = token.getValor();
      obterSimbolo();
      verificarValorThrow("(");
      obterSimbolo();
      if (verificarTipo(Tag.IDENTIFICADOR)) {
        if (operador == "read") {
          code2("LEIT");
          code2("ARMZ " + tabelaSimbolo.get(token.getValor()).getValue());
        } else {
          code2("CRVL " + tabelaSimbolo.get(token.getValor()).getValue());
          code2("IMPR");
        }
        obterSimbolo();
        verificarValorThrow(")");
        obterSimbolo();
      } else {
        throw new SintaticoException(token, scan.linha, Tag.IDENTIFICADOR);
      }

    } else if (verificarTipo(Tag.IDENTIFICADOR)) {
      Entry<Token, Integer> tokenAtual = tabelaSimbolo.get(token.getValor());
      if (tokenAtual != null) {
        obterSimbolo();
        verificarValorThrow(":=");
        obterSimbolo();
        expressao();
        code2("ARMZ " + tokenAtual.getValue());
      } else {
        throw new SemanticoException("Variável '" + token.getValor() + "' não declarada!", scan.linha);
      }
    } else if (verificarValor("if")) {
      obterSimbolo();
      condicao();
      verificarValorThrow("then");
      code2("DSVF #");
      comandos();
      c = c.stream().map(s -> s.replace("#", String.valueOf(c.size() + 1))).collect(Collectors.toList());
      pfalsa();
      verificarValorThrow("$");
      obterSimbolo();
    } else if (verificarValor("while")) {
      obterSimbolo();
      int linhaWhile = c.size();
      System.out.println("linhaWhile agr" + linhaWhile);
      condicao();
      code2("DSVF !");
      verificarValorThrow("do");
      comandos();
      code2("DSVI " + linhaWhile);
      System.out.println("linhaWhile dps" + linhaWhile);
      c = c.stream().map(s -> s.replace("!", String.valueOf(c.size()))).collect(Collectors.toList());
      verificarValorThrow("$");
    } else {
      throw new SintaticoException(token, scan.linha, "IDENTIFICADOR", "read", "write", "while");
    }
  }

  private void condicao() {
    System.out.println("condicao");
    expressao();
    String relacaoSyn = relacao();
    expressao();

    switch (relacaoSyn) {
      case "=":
        code2("CPIG");
        break;
      case "<>":
        code2("CDES");
        break;
      case ">=":
        code2("CMAI");
        break;
      case "<=":
        code2("CPMI");
        break;
      case ">":
        code2("CPMA");
        break;
      case "<":
        code2("CPME");
        break;
    }
  }

  private String relacao() {
    System.out.println("relacao");
    if (verificarValor("=") || verificarValor("<>") || verificarValor(">=") || verificarValor("<=")
        || verificarValor(">") || verificarValor("<")) {
      String relacaoSyn = token.getValor();
      obterSimbolo();
      return relacaoSyn;
    } else {
      throw new SintaticoException(token, scan.linha, "=", "<>", ">=", "<=", ">", "<");
    }
  }

  private void expressao() {
    System.out.println("expressao");
    termo();
    outros_termos();
  }

  private void termo() {
    System.out.println("termo");
    String opUnSyn = op_un();
    fator();
    if (!opUnSyn.isBlank()) {
      code2("INVE");
    }
    mais_fatores();
  }

  private String op_un() {
    System.out.println("op_un");
    if (verificarValor("-")) {
      obterSimbolo();
      return "-";
    }
    return "";
  }

  private void fator() {
    System.out.println("fator");
    if (verificarTipo(Tag.IDENTIFICADOR)) {
      Entry<Token, Integer> tokenAtual = tabelaSimbolo.get(token.getValor());
      if (tokenAtual == null) {
        throw new SemanticoException("O token " + token.getValor() + " não foi declarado.");
      }
      code2("CRVL " + tabelaSimbolo.get(token.getValor()).getValue());
      obterSimbolo();
    } else if (verificarTipo(Tag.INTEGER) || verificarTipo(Tag.REAL)) {
      code2("CRCT " + token.getValor());
      obterSimbolo();
    } else if (verificarValor("(")) {
      obterSimbolo();
      expressao();
      verificarValorThrow(")");
      obterSimbolo();
    } else {
      throw new SintaticoException(
          "Esperado algum token do tipo " + Helper.listaTagsString(Tag.IDENTIFICADOR, Tag.INTEGER, Tag.REAL)
              + " ou um token com valor \"(\", mas foi recebido: " + token,
          scan.linha);
    }
  }

  private void outros_termos() {
    System.out.println("outros_termos");
    if (verificarValor("+") || verificarValor("-")) {
      String opAdSyn = op_ad();
      termo();
      if (opAdSyn.equals("+")) {
        code2("SOMA");
      } else {
        code2("SUBT");
      }
      outros_termos();
    }
  }

  private String op_ad() {
    System.out.println("op_ad");
    if (verificarValor("-") || verificarValor("+")) {
      String opAdSyn = token.getValor();
      obterSimbolo();
      return opAdSyn;
    } else {
      throw new SintaticoException(token, scan.linha, "-", "+");
    }
  }

  private void mais_fatores() {
    System.out.println("mais_fatores");
    if (verificarValor("*") || verificarValor("/")) {
      String opMulSyn = op_mul();
      fator();
      if (opMulSyn.equals("*")) {
        code2("MULT");
      } else {
        code2("DIVI");
      }
      mais_fatores();
    }
  }

  private String op_mul() {
    System.out.println("op_mul");
    if (verificarValor("/") || verificarValor("*")) {
      String syn = token.getValor();
      obterSimbolo();
      return syn;
    } else {
      throw new SintaticoException(token, scan.linha, "/", "*");
    }
  }

  private void pfalsa() {
    System.out.println("pfalsa");
    if (verificarValor("else")) {
      code2("DSVI @");
      comandos();
      c = c.stream().map(s -> s.replace("@", String.valueOf(c.size()))).collect(Collectors.toList());

    }
  }

  public void analisar() {
    obterSimbolo();
    programa();
    if (token == null) {
      System.out.println("Tudo certo!");
      System.out.println("---------------- Código intermediário ----------------");
      try {
        FileWriter fileWriter = new FileWriter("output.txt");
        PrintWriter printWriter = new PrintWriter(fileWriter);
        c.forEach(s -> {
          System.out.println(s);
          printWriter.printf(s + '\n');
        });
        printWriter.close();
      } catch (IOException e) {
        System.out.println("Erro ao gerar arquivo");
      }

    } else {
      throw new SintaticoException("Esperado fim de cadeia, recebido: " + token);
    }
  }

}
