# Conta Remunerada

Projeto desenvolvido para o desafio técnico da Novo Tempo. A aplicação simula uma conta remunerada: o cliente abre uma conta, faz login, consulta o saldo e o extrato paginado e realiza depósitos e saques.

O backend, em Java 25 e Spring Boot 4, concentra as regras de negócio. No fechamento diário, a aplicação calcula e credita os juros com base na taxa configurada, garantindo que cada conta receba juros no máximo uma vez por data de fechamento.

**Status:** Em desenvolvimento.