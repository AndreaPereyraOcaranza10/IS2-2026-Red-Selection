package redSelection.dulceMayor.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record Auth0CredentialDTO(@NotBlank @Size(max = 8192) String idToken) { }
