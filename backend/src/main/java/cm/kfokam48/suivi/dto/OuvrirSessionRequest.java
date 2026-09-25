package cm.kfokam48.suivi.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record OuvrirSessionRequest(
        @NotBlank(message = "le titre est obligatoire") String titre,
        @NotNull(message = "la promotion est obligatoire") Long promotionId
) {
}
