package com.nexigroup.pagopa.cruscotto.sert.web.rest.errors;

import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serial;
import java.io.Serializable;
import lombok.Getter;

@Schema(description = "Dettagli errore campo")
@Getter
public class FieldErrorVM implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "Nome applicazione")
    private final String applicationName;

    @Schema(description = "Nome dell'oggetto contenente il campo in errore")
    private final String objectName;

    @Schema(description = "Campo in errore")
    private final String field;

    @Schema(description = "Messaggio di errore")
    private final String message;

    @Schema(description = "Tipo di errore")
    private final String type;

    public FieldErrorVM(String applicationName, String dto, String field, String message, String type) {
        this.applicationName = applicationName;
        this.objectName = dto;
        this.field = field;
        this.message = message;
        this.type = type;
    }
}
