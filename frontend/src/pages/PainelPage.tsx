import { useNavigate } from "react-router";
import { tokenStorage } from "../auth/tokenStorage";
import { useEffect, useState } from "react";
import type { SaldoResponse } from "../types/saldo";
import type { ExtratoResponse } from '../types/extrato';
import { contaService } from "../api/contaService";
import { handleApiError } from "../api/handleApiError";
import { Alert, Box, Button, Card, CardContent, CircularProgress, Container, Divider, Snackbar, Stack, Typography } from "@mui/material";
import { formatarMoeda } from "../utils/formatarMoeda";

export function PainelPage() {
    const navigate = useNavigate();
    const contaId = tokenStorage.getContaId();

    const [saldo, setSaldo] = useState<SaldoResponse | null>(null);
    const [loading, setLoading] = useState(true);
    const [extrato, setExtrato] = useState<ExtratoResponse | null>(null);
    const [loadingExtrato, setLoadingExtrato] = useState(true);

    const [mensagemErro, setMensagemErro] = useState('');
    const [paginaAtual, setPaginaAtual] = useState(0);
    const [atualizacao, setAtualizacao] = useState(0);

    useEffect(() => {
        if (contaId === null) {
            return;
        }

        let ativo = true;

        async function carregarSaldo() {
            try {
                const response = await contaService.consultarSaldo(contaId!);

                if (ativo) {
                    setSaldo(response);
                }
            } catch (error: unknown) {
                if (ativo) {
                    setMensagemErro(handleApiError(error).mensagem);
                }
            } finally {
                setLoading(false);
            }
        }

        void carregarSaldo();

        return () => {
            ativo = false;
        }
    }, [contaId, atualizacao])

    useEffect(() => {
      if (contaId === null) {
        return;
      }

      let ativo = true;

      async function carregarExtrato() {
        setLoadingExtrato(true);

        try {
          const response = await contaService.consultaExtrato(
            contaId!,
            paginaAtual,
            20,
          );

          if (ativo) {
            const ultimaPaginaValida = Math.max(0, response.totalPages -1);

            if (paginaAtual > ultimaPaginaValida) {
                setPaginaAtual(ultimaPaginaValida);
                return;
            }
            setExtrato(response);
          }
        } catch (error: unknown) {
          if (ativo) {
            setExtrato(null);
            setMensagemErro(handleApiError(error).mensagem);
          }
        } finally {
          if (ativo) {
            setLoadingExtrato(false);
          }
        }
      }

      void carregarExtrato();

      return () => {
        ativo = false;
      };
    }, [contaId, paginaAtual, atualizacao]);

    function handleLogout() {
        tokenStorage.remove();
        navigate('/login', {replace: true});
    }

    function handleAtualizar() {
        if (loading || loadingExtrato){
            return;
        }

        setLoading(true);
        setLoadingExtrato(true);

        setAtualizacao((valor) => valor + 1);
    }

    const saldoFormatado = saldo ? formatarMoeda(saldo.saldo) : "R$ 0,00";

    if (contaId === null) {
        return (
          <Container maxWidth="md" sx={{ py: 4 }}>
            <Typography color="error">
              Não foi possível identificar a conta autenticada.
            </Typography>
            <Button onClick={handleLogout}>Voltar ao login</Button>
          </Container>
        );
    }

    return (
      <Container maxWidth="md" sx={{ py: { xs: 4, sm: 6 } }}>
        <Stack spacing={3}>
          <Stack
            direction={{ xs: "column", sm: "row" }}
            spacing={2}
            sx={{
              justifyContent: "space-between",
              alignItems: { xs: "flex-start", sm: "center" },
            }}
          >
            <Typography variant="h4" component="h1" color="primary">
              Minha conta
            </Typography>

            <Stack spacing={2} direction="row">
              <Button
                variant="outlined"
                onClick={handleAtualizar}
                disabled={loading || loadingExtrato}
              >
                Atualizar
              </Button>
              <Button variant="outlined" color="error" onClick={handleLogout}>
                Sair
              </Button>
            </Stack>
          </Stack>

          {loading ? (
            <Box sx={{ display: "flex", justifyContent: "center", py: 6 }}>
              <CircularProgress />
            </Box>
          ) : saldo ? (
            <Card>
              <CardContent sx={{ p: { xs: 3, sm: 4 } }}>
                <Stack spacing={2}>
                  <Typography variant="h6">{saldo.titular}</Typography>

                  <Typography
                    color="textSecondary"
                    sx={{ overflowWrap: "anywhere" }}
                  >
                    Conta: {saldo.numero}
                  </Typography>

                  <Box>
                    <Typography variant="overline" color="textSecondary">
                      Saldo disponível
                    </Typography>

                    <Typography
                      variant="h4"
                      sx={{ overflowWrap: "anywhere", fontWeight: 700 }}
                    >
                      {saldoFormatado}
                    </Typography>
                  </Box>
                </Stack>
              </CardContent>
            </Card>
          ) : (
            <Typography color="textSecondary">
              Não foi possível carregar o saldo da conta.
            </Typography>
          )}
          <Card>
            <CardContent sx={{ p: { xs: 3, sm: 4 } }}>
              <Stack spacing={2}>
                <Typography
                  variant="h6"
                  component="h2"
                  color="textPrimary"
                  sx={{ fontWeight: 600 }}
                >
                  Extrato
                </Typography>
                {loadingExtrato ? (
                  <Box
                    sx={{ display: "flex", justifyContent: "center", py: 4 }}
                  >
                    <CircularProgress />
                  </Box>
                ) : extrato && extrato.content.length > 0 ? (
                  <Stack divider={<Divider />} spacing={2}>
                    {extrato.content.map((lacamento, index) => (
                      <Stack
                        key={`${extrato.page}-${index}`}
                        direction={{ xs: "column", sm: "row" }}
                        spacing={1}
                        sx={{
                          justifyContent: "space-between",
                          alignItems: "center",
                        }}
                      >
                        <Box>
                          <Typography sx={{ fontWeight: 600 }}>
                            {lacamento.tipo}
                          </Typography>
                          <Typography variant="body2" color="textSecondary">
                            {new Date(lacamento.dataHora).toLocaleString(
                              "pt-BR",
                            )}
                          </Typography>
                        </Box>
                        <Typography
                          sx={{ fontWeight: 700 }}
                          color={
                            lacamento.tipo === "SAQUE" ? "error" : "success"
                          }
                        >
                          {formatarMoeda(lacamento.valor)}
                        </Typography>
                      </Stack>
                    ))}
                  </Stack>
                ) : (
                  <Typography color="textSecondary">
                    {extrato
                      ? "Nenhuma movimentação encontrada."
                      : "Não foi possível carregar o extrato."}
                  </Typography>
                )}
              </Stack>
            </CardContent>
          </Card>
          {extrato && extrato.totalPages > 0 && (
            <Stack
              direction="row"
              spacing={2}
              sx={{ justifyContent: "center", alignItems: "center", pt: 2 }}
            >
              <Button
                variant="outlined"
                disabled={loadingExtrato || paginaAtual === 0}
                onClick={() => setPaginaAtual((pagina) => pagina - 1)}
              >
                Anterior
              </Button>
              <Typography variant="body2" color="textSecondary">
                Página {extrato.page + 1} de {extrato.totalPages}
              </Typography>
              <Button
                variant="outlined"
                disabled={loading || paginaAtual >= extrato.totalPages - 1}
                onClick={() => setPaginaAtual((pagina) => pagina + 1)}
              >
                Próxima
              </Button>
            </Stack>
          )}
        </Stack>

        <Snackbar
          open={Boolean(mensagemErro)}
          autoHideDuration={4000}
          onClose={() => setMensagemErro("")}
          anchorOrigin={{ vertical: "bottom", horizontal: "center" }}
        >
          <Alert
            severity="error"
            variant="filled"
            onClose={() => setMensagemErro("")}
          >
            {mensagemErro}
          </Alert>
        </Snackbar>
      </Container>
    );
}