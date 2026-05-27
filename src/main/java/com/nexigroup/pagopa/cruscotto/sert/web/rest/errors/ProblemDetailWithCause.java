package com.nexigroup.pagopa.cruscotto.sert.web.rest.errors;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIncludeProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.ProblemDetail;

/**
 * Classe che estende ProblemDetail di Spring con funzionalità aggiuntive per la gestione delle cause di errore.
 */
@Schema(description = "Rappresentazione dettagliata di un problema con causa")
@Setter
@Getter
@JsonIncludeProperties({"title", "status", "detail"})
public class ProblemDetailWithCause extends ProblemDetail {

    @JsonIgnore
    @Schema(hidden = true)
    private ProblemDetailWithCause cause;

    ProblemDetailWithCause(int rawStatus) {
        super(rawStatus);
    }

    ProblemDetailWithCause(int rawStatus, ProblemDetailWithCause cause) {
        super(rawStatus);
        this.cause = cause;
    }

    @Schema(name = "ProblemDetailWithCauseBuilder", description = "Builder per la creazione di istanze ProblemDetailWithCause")
    public static class ProblemDetailWithCauseBuilder {

        private static final URI BLANK_TYPE = URI.create("about:blank");

        private URI type = BLANK_TYPE;

        private String title;

        private int status;

        private String detail;

        private URI instance;

        @JsonProperty("properties")
        @Schema(
            name = "properties",
            description = "Proprietà aggiuntive del problema",
            example = """
            {
                "message": "error.validation",
                "path": "/api/instances",
                "fieldErrors": [
                    {
                        "applicationName": "pagopaCruscottoSertBackendApp",
                        "field": "predictedDateAnalysis",
                        "message": "non deve essere spazio",
                        "objectName": "instanceRequestBean",
                        "type": "NotBlank"
                    }
                ]
            }
            """
        )
        private Map<String, Object> properties = new HashMap<>();

        @Schema(description = "Causa del problema")
        private ProblemDetailWithCause cause;

        public static ProblemDetailWithCauseBuilder instance() {
            return new ProblemDetailWithCauseBuilder();
        }

        public ProblemDetailWithCauseBuilder withType(URI type) {
            this.type = type;
            return this;
        }

        public ProblemDetailWithCauseBuilder withTitle(String title) {
            this.title = title;
            return this;
        }

        public ProblemDetailWithCauseBuilder withStatus(int status) {
            this.status = status;
            return this;
        }

        public ProblemDetailWithCauseBuilder withDetail(String detail) {
            this.detail = detail;
            return this;
        }

        public ProblemDetailWithCauseBuilder withInstance(URI instance) {
            this.instance = instance;
            return this;
        }

        public ProblemDetailWithCauseBuilder withCause(ProblemDetailWithCause cause) {
            this.cause = cause;
            return this;
        }

        public ProblemDetailWithCauseBuilder withProperties(Map<String, Object> properties) {
            this.properties = properties;
            return this;
        }

        public ProblemDetailWithCauseBuilder withProperty(String key, Object value) {
            this.properties.put(key, value);
            return this;
        }

        public ProblemDetailWithCause build() {
            ProblemDetailWithCause cause = new ProblemDetailWithCause(this.status);
            cause.setType(this.type);
            cause.setTitle(this.title);
            cause.setDetail(this.detail);
            cause.setInstance(this.instance);
            this.properties.forEach(cause::setProperty);
            cause.setCause(this.cause);
            return cause;
        }
    }

    @Override
    @JsonIgnore
    @Schema(hidden = true)
    public URI getInstance() {
        return super.getInstance();
    }

    @Override
    @JsonIgnore
    @Schema(hidden = true)
    public @NotNull URI getType() {
        return super.getType();
    }

    @Override
    @Schema(description = "Titolo del problema")
    public String getTitle() {
        return super.getTitle();
    }

    @Override
    @Schema(description = "Codice di stato HTTP")
    public int getStatus() {
        return super.getStatus();
    }

    @Override
    @Schema(description = "Dettaglio del problema")
    public String getDetail() {
        return super.getDetail();
    }

    @Override
    @JsonIgnore
    @Schema(hidden = true)
    public Map<String, Object> getProperties() {
        return super.getProperties();
    }
}
