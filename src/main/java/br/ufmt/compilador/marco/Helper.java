package br.ufmt.compilador.marco;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import br.ufmt.compilador.marco.Token.Tag;

public class Helper {
  static public String listaTagsString(Tag... tags) {
    List<Tag> tagsList = Arrays.asList(tags);
    return "[" + tagsList.stream().map(t -> t.name()).collect(Collectors.joining(",")) + "]";
  }
}
