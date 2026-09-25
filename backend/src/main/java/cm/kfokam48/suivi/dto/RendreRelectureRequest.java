package cm.kfokam48.suivi.dto;

import jakarta.validation.constraints.NotNull;

public record RendreRelectureRequest(
        @NotNull(message = "la note est obligatoire") Integer note,
        String commentaire
) {
}
