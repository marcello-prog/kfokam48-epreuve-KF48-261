package cm.kfokam48.suivi.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record MarquerPresenceRequest(
        @NotBlank(message = "le code est obligatoire") String code,
        @NotNull(message = "l'étudiant est obligatoire") Long etudiantId
) {
}
