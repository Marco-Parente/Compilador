package br.ufmt.compilador.marco;

import java.util.HashMap;
import java.util.Map;

import br.ufmt.compilador.marco.Token.Tag;
import br.ufmt.compilador.marco.exceptions.SemanticoException;
import br.ufmt.compilador.marco.exceptions.SintaticoException;

public class Sintatico {

	private Lexico scan;
	private Token token;
	private Map<String, Token> tabelaSimbolo = new HashMap<>();
	private Tag tipo;

	private int temp;
	private int linhaCodigo;
	private StringBuilder codigo = new StringBuilder("operador;arg1;arg2;result\n");

	public Sintatico(String arq) {
		scan = new Lexico(arq);
	}

	private void obterSimbolo() {
		token = scan.nextToken();
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

	private String geraTemp() {
		return "t" + temp++;
	}

	private void code(String op, String arg1, String arg2, String result) {
		codigo.append(linhaCodigo++ + " - " + op + ";" + arg1 + ";" + arg2 + ";" + result + "\n");
	}

	private void programa() {
		System.out.println("programa");
		verificarValorThrow("program");
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
		obterSimbolo();
		comandos();
		verificarValorThrow("end");
		code("PARA", "", "", "");
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
			tabelaSimbolo.put(token.getValor(), new Token(tipo, token.getValor()));
			code("ALME", tipo == Tag.INTEGER ? "0" : "0.0", "", token.getValor());
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
			obterSimbolo();
			comandos();
		}
	}

	private void comando() {
		System.out.println("comando");
		if (verificarValor("read") || verificarValor("write")) {
			String operador = token.getValor();
			obterSimbolo();
			verificarValorThrow("(");
			obterSimbolo();
			if (verificarTipo(Tag.IDENTIFICADOR)) {
				code(operador, token.getValor(), "", "");
				obterSimbolo();
				verificarValorThrow(")");
				obterSimbolo();
			} else {
				throw new SintaticoException(token, scan.linha, Tag.IDENTIFICADOR);
			}

		} else if (verificarTipo(Tag.IDENTIFICADOR)) {
			Token tokenAtual = tabelaSimbolo.get(token.getValor());
			if (tokenAtual != null) {
				obterSimbolo();
				verificarValorThrow(":=");
				obterSimbolo();
				String expressaoSyn = expressao();
				code(":=", expressaoSyn, "", tokenAtual.getValor());
			} else {
				throw new SemanticoException("Variável '" + token.getValor() + "' não declarada!", scan.linha);
			}
		} else if (verificarValor("if")) {
			obterSimbolo();
			String condicaoSyn = condicao();
			verificarValorThrow("then");
			code("JF", condicaoSyn, "{", "");
			obterSimbolo();
			comandos();
			int index = codigo.lastIndexOf("{");
			codigo.replace(index, index + 1, String.valueOf(linhaCodigo + 1));
			pfalsa();
			verificarValorThrow("$");
			obterSimbolo();

		}
	}

	private String condicao() {
		System.out.println("condicao");
		String expressaoSyn = expressao();
		String relacaoSyn = relacao();
		String expressaoLinhaSyn = expressao();
		String condicaoSyn = geraTemp();
		code(relacaoSyn, expressaoSyn, expressaoLinhaSyn, condicaoSyn);
		return condicaoSyn;
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

	private String expressao() {
		System.out.println("expressao");
		String termoSyn = termo();
		String outrosTermosSyn = outros_termos(termoSyn);
		return outrosTermosSyn;
	}

	private String termo() {
		System.out.println("termo");
		String opUnSyn = op_un();
		String fatorSyn = fator();
		String maisFatoresSyn = mais_fatores(fatorSyn);
		if (!opUnSyn.isBlank()) {
			String termoSyn = geraTemp();
			code(opUnSyn, null, maisFatoresSyn, termoSyn);
			return termoSyn;
		} else {
			return maisFatoresSyn;
		}
	}

	private String op_un() {
		System.out.println("op_un");
		if (verificarValor("-")) {
			obterSimbolo();
			return "-";
		}
		return "";
	}

	private String fator() {
		System.out.println("fator");
		if (verificarTipo(Tag.IDENTIFICADOR) || verificarTipo(Tag.INTEGER) || verificarTipo(Tag.REAL)) {
			String fatorSyn = token.getValor();
			obterSimbolo();
			return fatorSyn;
		} else if (verificarValor("(")) {
			obterSimbolo();
			String fatorSyn = expressao();
			verificarValorThrow(")");
			obterSimbolo();
			return fatorSyn;
		} else {
			throw new SintaticoException(
					"Esperado algum token do tipo " + Helper.listaTagsString(Tag.IDENTIFICADOR, Tag.INTEGER, Tag.REAL)
							+ " ou um token com valor \"(\", mas foi recebido: " + token,
					scan.linha);
		}
	}

	private String outros_termos(String outrosTermosInh) {
		System.out.println("outros_termos");
		if (verificarValor("+") || verificarValor("-")) {
			String opAdSyn = op_ad();
			String termoSyn = termo();
			String outrosTermosLinhaSyn = outros_termos(termoSyn);
			String outrosTermosSyn = geraTemp();
			code(opAdSyn, outrosTermosInh, outrosTermosLinhaSyn, outrosTermosSyn);
			return outrosTermosSyn;
		}
		return outrosTermosInh;
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

	private String mais_fatores(String maisFatoresInh) {
		System.out.println("mais_fatores");
		if (verificarValor("*") || verificarValor("/")) {
			String opMulSyn = op_mul();
			String fatorSyn = fator();
			String maisFatoresLinhaSyn = mais_fatores(fatorSyn);
			String maisFatoresSyn = geraTemp();
			code(opMulSyn, maisFatoresInh, maisFatoresLinhaSyn, maisFatoresSyn);
			return maisFatoresSyn;
		}
		return maisFatoresInh;
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
			code("goto", "&", "", "");
			obterSimbolo();
			comandos();
			int index = codigo.lastIndexOf("&");
			codigo.replace(index, index + 1, String.valueOf(linhaCodigo));
		}
	}

	public void analisar() {
		obterSimbolo();
		programa();
		if (token == null) {
			System.out.println("Tudo certo!");
			System.out.println("---------------- Código intermediário ----------------");
			System.out.println(codigo.toString());
		} else {
			throw new SintaticoException("Esperado fim de cadeia, recebido: " + token);
		}
	}

}
