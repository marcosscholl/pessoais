package io.sicredi.aberturadecontasalarioefetivador.factories;

import io.sicredi.aberturadecontasalarioefetivador.dto.GestentDTO;

import java.util.List;

public class GestentFactory {

    public static GestentDTO consultarEntidadeResponse(){
        return criaResultadoObterEntidadeSicredi();
    }

    private static GestentDTO criaResultadoObterEntidadeSicredi() {
        GestentDTO.ContentRecord contentRecord = new GestentDTO.ContentRecord(
                8088,
                "AGENCIA",
                "0167",
                "17",
                "UA São Sebastião do Caí",
                "90608712001586",
                "ATIVA",
                "BGQ",
                "BGA",
                "000671",
                "1214",
                "2006-01-23",
                "2006-01-23",
                false,
                false
        );

        GestentDTO.SortRecord sortRecord = new GestentDTO.SortRecord(false, true, false);
        GestentDTO.PageableRecord pageableRecord = new GestentDTO.PageableRecord(0, 1, sortRecord, 0, true, false);

        return new GestentDTO(
                List.of(contentRecord),
                pageableRecord,
                true,
                1,
                1,
                sortRecord,
                1,
                0,
                1,
                true,
                false
        );
    }
}
