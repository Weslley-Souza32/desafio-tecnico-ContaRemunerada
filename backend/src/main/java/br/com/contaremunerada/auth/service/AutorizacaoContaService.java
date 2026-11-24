package br.com.contaremunerada.auth.service;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

@Service
public class AutorizacaoContaService {

  public void validarAcesso(Long contaIdSolicitada, Jwt jwt){

    String contaIdAutenticada = jwt.getSubject();

    if (!contaIdSolicitada.toString().equals(contaIdAutenticada)){
      throw new AccessDeniedException("Você não possui permissão para acessar esta conta.");
    }
  }
}
