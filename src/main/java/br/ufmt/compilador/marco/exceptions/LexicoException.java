package br.ufmt.compilador.marco.exceptions;

public class LexicoException extends RuntimeException {
	public LexicoException(String mensagem) {
		super("Erro léxico: " + mensagem);
	}

	public LexicoException(String mensagem, int linha) {
		super("Erro léxico (linha " + linha + "): " + mensagem);
	}
}
