package com.bytestorm.waygo.domain.valueobject;

import com.bytestorm.waygo.domain.enums.TipoConexao;

public record ArestaRede(Long origem, Long destino, TipoConexao tipo, int tempoSegundos, int distanciaMetros) {
}
