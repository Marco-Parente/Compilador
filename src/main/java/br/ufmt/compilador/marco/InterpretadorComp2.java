package br.ufmt.compilador.marco;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.Stack;

public class InterpretadorComp2 {

  private List<String> c = new ArrayList<>();
  private Stack<Double> d = new Stack<>();
  private int i = 0;
  private int s = -1;

  Scanner scanner = new Scanner(System.in);

  public InterpretadorComp2(String arquivoEntrada) {
    try (BufferedReader br = new BufferedReader(new FileReader(arquivoEntrada))) {
      String line;
      while ((line = br.readLine()) != null) {
        c.add(line.split(" - ")[1]);
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void executar() {
    boolean terminou = false;
    while (c.size() > i) {
      List<String> instrucao = Arrays.asList(c.get(i).split(" "));

      switch (instrucao.get(0)) {
        case "CRCT":
          crct(Double.valueOf(instrucao.get(1)));
          break;
        case "CRVL":
          crvl(Integer.valueOf(instrucao.get(1)));
          break;
        case "SOMA":
          soma();
          break;
        case "SUBT":
          subt();
          break;
        case "MULT":
          mult();
          break;
        case "DIVI":
          divi();
          break;
        case "INVE":
          inve();
          break;
        case "CONJ":
          conj();
          break;
        case "DISJ":
          disj();
          break;
        case "NEGA":
          nega();
          break;
        case "CPME":
          cpme();
          break;
        case "CPMA":
          cpma();
          break;
        case "CPIG":
          cpig();
          break;
        case "CDES":
          cdes();
          break;
        case "CPMI":
          cpmi();
          break;
        case "CMAI":
          cmai();
          break;
        case "ARMZ":
          armz(Integer.valueOf(instrucao.get(1)));
          break;
        case "DSVI":
          dsvi(Integer.valueOf(instrucao.get(1)));
          break;
        case "DSVF":
          dsvf(Integer.valueOf(instrucao.get(1)));
          break;
        case "LEIT":
          leit();
          break;
        case "IMPR":
          impr();
          break;
        case "ALME":
          alme(Integer.valueOf(instrucao.get(1)));
          break;
        case "INPP":
          inpp();
          break;
        case "PARA":
          terminou = true;
          break;
      }

      if (terminou) {
        break;
      }

      if (!instrucao.get(0).equals("DSVF") && !instrucao.get(0).equals("DSVI")) {
        i++;
      }
    }
  }

  private void crct(Double k) {
    d.push(k);
    s += 1;
  }

  private void crvl(int n) {
    d.push(d.get(n));
    s += 1;
  }

  private void soma() {
    Double topo = d.pop();
    Double antecessor = d.pop();
    d.push(antecessor + topo);
    s -= 1;
  }

  private void subt() {
    Double topo = d.pop();
    Double antecessor = d.pop();
    d.push(antecessor - topo);
    s -= 1;
  }

  private void mult() {
    Double topo = d.pop();
    Double antecessor = d.pop();
    d.push(antecessor * topo);
    s -= 1;

  }

  private void divi() {
    Double topo = d.pop();
    Double antecessor = d.pop();
    d.push(antecessor / topo);
    s -= 1;
  }

  private void inve() {
    d.push(-d.pop());
  }

  private void conj() {
    Double topo = d.pop();
    Double antecessor = d.pop();
    if (topo == 1 && antecessor == 1) {
      d.push(1.0);
    } else {
      d.push(0.0);
    }
    s -= 1;
  }

  private void disj() {
    Double topo = d.pop();
    Double antecessor = d.pop();
    if (topo == 1 || antecessor == 1) {
      d.push(1.0);
    } else {
      d.push(0.0);
    }
    s -= 1;
  }

  private void nega() {
    d.push(1 - d.pop());
  }

  private void cpme() {
    Double topo = d.pop();
    Double antecessor = d.pop();
    if (antecessor < topo) {
      d.push(1.0);
    } else {
      d.push(0.0);
    }
    s -= 1;
  }

  private void cpma() {
    Double topo = d.pop();
    Double antecessor = d.pop();
    if (antecessor > topo) {
      d.push(1.0);
    } else {
      d.push(0.0);
    }
    s -= 1;
  }

  private void cpig() {
    Double topo = d.pop();
    Double antecessor = d.pop();
    if (antecessor == topo) {
      d.push(1.0);
    } else {
      d.push(0.0);
    }
    s -= 1;
  }

  private void cdes() {
    Double topo = d.pop();
    Double antecessor = d.pop();
    if (antecessor != topo) {
      d.push(1.0);
    } else {
      d.push(0.0);
    }
    s -= 1;
  }

  private void cpmi() {
    Double topo = d.pop();
    Double antecessor = d.pop();
    if (antecessor <= topo) {
      d.push(1.0);
    } else {
      d.push(0.0);
    }
    s -= 1;
  }

  private void cmai() {
    Double topo = d.pop();
    Double antecessor = d.pop();
    if (antecessor >= topo) {
      d.push(1.0);
    } else {
      d.push(0.0);
    }
    s -= 1;
  }

  private void armz(int n) {
    Double topo = d.pop();
    d.set(n, topo);
    s -= 1;
  }

  private void dsvi(int p) {
    i = p;
  }

  private void dsvf(int p) {
    if (d.pop() == 0) {
      i = p;
    } else {
      i++;
    }
    s -= 1;
  }

  private void leit() {
    s += 1;
    d.push(scanner.nextDouble());
  }

  private void impr() {
    s -= 1; // Talvez errado

  }

  private void alme(int m) {
    for (int i = 0; i < m; i++) {
      d.push(0.0);
    }
    s += m;
  }

  private void inpp() {
    s = -1;
  }

}
