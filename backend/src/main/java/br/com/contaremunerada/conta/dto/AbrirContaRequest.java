package br.com.contaremunerada.conta.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AbrirContaRequest(
    @NotBlank(message = "O titular é obrigatório.")
    String titular,

    @NotBlank(message = "A senha é obrigatória.")
    @Size(min = 6, message = "A senha deve possuir no mínimo 6 caracteres.")
    String senha
){

}
