
import { useState } from 'react';
import {
    Alert,
  Box,
  Button,
  Card,
  CardContent,
  CircularProgress,
  Container,
  Snackbar,
  Stack,
  TextField,
  Typography,
} from '@mui/material';
import { Link, useNavigate } from 'react-router';
import type { CriarContaResponse } from '../types/auth';
import { authService } from '../api/authService';
import { handleApiError } from '../api/handleApiError';
import { tokenStorage } from '../auth/tokenStorage';

export function CadastroPage() {
  const [titular, setTitular] = useState('');
  const [senha, setSenha] = useState('');

  const [erros, setErros] = useState({
    titular: '',
    senha: '',
  });
  const [loading, setLoading] = useState(false);
  const [contaCriada, setContaCriada] = useState<CriarContaResponse | null>(null);
  const [notificacao, setNotificacao] = useState({
    open: false,
    mensagem: '',
    severity: 'error' as 'success' | 'error',
  });
  const [autenticado, setAutenticado] = useState(false);
  const navigate = useNavigate();

  function mostrarErro(mensagem: string) {
    setNotificacao({
        open: true,
        mensagem,
        severity: 'error'
    })
  }

  async function handleCopiarNumero() {
    if (!contaCriada) {
        return;
    }

    try {
        await navigator.clipboard.writeText(contaCriada.numero);

        setNotificacao({
            open: true,
            mensagem: 'Número da conta copiado!',
            severity: 'success',
        });
    } catch {
        setNotificacao({
            open: true,
            mensagem: 'Não foi possível copiar o número da conta.',
            severity: 'error',
        });
    }
  }

  async function handleSubmit(event: React.SubmitEvent<HTMLFormElement>) {
    event.preventDefault();

    if (loading || contaCriada) {
      return;
    }

    const novosErros = {
      titular: titular.trim() ? "" : "Informe o nome do titular.",
      senha:
        senha.length >= 6 ? "" : "A senha deve ter no mínimo 6 caracteres.",
    };

    setErros(novosErros);

    if (novosErros.titular || novosErros.senha) {
      return;
    }

    setLoading(true);
    setNotificacao((atual) => ({ ...atual, open: false }));

    try {
      const conta = await authService.criarConta({
        titular: titular.trim(),
        senha,
      });

      // Preserva o número mesmo se o login automático falhar.
      setContaCriada(conta);

      try {
        const loginResponse = await authService.login({
          numero: conta.numero,
          senha,
        });

        tokenStorage.setSession(loginResponse.token, loginResponse.contaId);

        setAutenticado(true);
      } catch (error: unknown) {
        const apiError = handleApiError(error);

        mostrarErro(
          `Conta criada, mas não foi possível realizar o login automático. ${apiError.mensagem}`,
        );
      }
    } catch (error: unknown) {
      const apiError = handleApiError(error);

      mostrarErro(apiError.mensagem);
    } finally {
      setLoading(false);
    }
  }

  return (
    <Container maxWidth="sm" sx={{ py: { xs: 4, sm: 8 } }}>
      <Card>
        <CardContent sx={{ p: { xs: 3, sm: 4 } }}>
          <Stack spacing={3}>
            <Box>
              <Typography
                variant="h4"
                component="h1"
                color="primary"
                sx={{ fontWeight: 700 }}
              >
                Abrir conta
              </Typography>

              <Typography color="text.secondary" sx={{ mt: 1 }}>
                Informe seus dados para criar sua conta remunerada.
              </Typography>
            </Box>

            <Box component="form" onSubmit={handleSubmit} noValidate>
              <Stack spacing={3}>
                <TextField
                  label="Nome do titular"
                  value={titular}
                  onChange={(event) => setTitular(event.target.value)}
                  error={Boolean(erros.titular)}
                  helperText={erros.titular}
                  autoComplete="name"
                  fullWidth
                />

                <TextField
                  label="Senha"
                  type="password"
                  value={senha}
                  onChange={(event) => setSenha(event.target.value)}
                  error={Boolean(erros.senha)}
                  helperText={erros.senha}
                  autoComplete="new-password"
                  fullWidth
                />

                <Button
                  type="submit"
                  variant="contained"
                  size="large"
                  fullWidth
                  disabled={loading || contaCriada !== null}
                  startIcon={
                    loading ? (
                      <CircularProgress size={8} color="inherit" />
                    ) : undefined
                  }
                >
                  {loading ? "Criando conta..." : "Criar conta"}
                </Button>

                <Button component={Link} to="/login">
                  Já tenho uma conta
                </Button>
              </Stack>
            </Box>
            {contaCriada && (
              <Box>
                <Typography variant="h6" color="success.main">
                  Conta criada com sucesso!
                </Typography>

                <Typography sx={{ mt: 1 }}>Seu número de conta é:</Typography>
                <Typography
                  component="p"
                  sx={{
                    mt: 1,
                    fontWeight: 700,
                    fontSize: { xs: "1.25rem", sm: "1.75rem" },
                    overflowWrap: "anywhere",
                    wordBreak: "break-word",
                  }}
                >
                  {contaCriada.numero}
                </Typography>
                <Typography
                  variant="body2"
                  color="text.secondary"
                  sx={{ mt: 1 }}
                >
                  Guarde este número. Você precisará dele para acessar sua
                  conta.
                </Typography>
                <Stack spacing={2} sx={{ mt: 3 }}>
                  <Button
                    variant="outlined"
                    onClick={handleCopiarNumero}
                    fullWidth
                  >
                    Copiar número da conta
                  </Button>
                  {autenticado ? (
                    <Button
                      variant="contained"
                      onClick={() => navigate("/painel", { replace: true })}
                      fullWidth
                    >
                      Acessar minha conta
                    </Button>
                  ) : (
                    <Button
                      component={Link}
                      to="/login"
                      variant="contained"
                      fullWidth
                    >
                      Ir para o login
                    </Button>
                  )}
                </Stack>
                {autenticado && (
                  <Typography color="success.main" sx={{ mt: 2 }}>
                    Autenticação realizada com sucesso!
                  </Typography>
                )}
              </Box>
            )}
          </Stack>
        </CardContent>
      </Card>

      <Snackbar
        open={notificacao.open}
        autoHideDuration={4000}
        onClose={() => setNotificacao((atual) => ({ ...atual, open: false }))}
        anchorOrigin={{ vertical: "bottom", horizontal: "center" }}
      >
        <Alert
          severity={notificacao.severity}
          variant="filled"
          onClose={() => setNotificacao((atual) => ({ ...atual, open: false }))}
        >
          {notificacao.mensagem}
        </Alert>
      </Snackbar>
    </Container>
  );
}
