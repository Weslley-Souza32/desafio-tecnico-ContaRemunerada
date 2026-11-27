import { Alert, Box, Button, Card, CardContent, CircularProgress, Container, Snackbar, Stack, TextField, Typography } from "@mui/material";
import React, { useState } from "react";
import { Link, useNavigate, useSearchParams } from "react-router";
import { authService } from "../api/authService";
import { handleApiError } from "../api/handleApiError";
import { tokenStorage } from "../auth/tokenStorage";

export function LoginPage() {
    const [numero, setNumero] = useState('');
    const [senha, setSenha] = useState('');
    const [erros, setErros] = useState({
        numero: '',
        senha: ''
    });
    const [loading, setLoading] = useState(false);
    const [notificacao, setNotificacao] = useState({
        open: false,
        mensagem: '',
        severity: 'success' as 'success' | 'error',
    });
    const navigate = useNavigate();
    const [searchParams, setSearchParams] = useSearchParams();

    const sessaoExpirada = searchParams.get('session') === 'expired';

    function handleCloseSnackbar() {
      setNotificacao((atual) => ({...atual, open: false}));

      if (sessaoExpirada) {
        setSearchParams(
          (params) => {
            const novosParamentros = new URLSearchParams(params);
            novosParamentros.delete("session");
            return novosParamentros;
          },
          { replace: true },
        );
      }
    }

    async function handleSubmit(event: React.SubmitEvent<HTMLFormElement>) {
        event?.preventDefault();

        if (loading) {
            return;
        }

        const novosErros = {
            numero: numero.trim() ? '' : 'Informe o número da conta',
            senha: senha ? '' : 'Informe a senha',
        };

        setErros(novosErros);

        if (novosErros.numero || novosErros.senha){
            return;
        }

        setLoading(true);

        try {
            const response = await authService.login({numero: numero.trim(), senha});

            tokenStorage.setSession(response.token, response.contaId);
            navigate('/painel', {replace: true})
            
        } catch (error: unknown) {
            const apiError = handleApiError(error);

            setNotificacao({
                open: true,
                mensagem: apiError.status === 401
                ? 'Número da conta ou senha inválidos.'
                : apiError.mensagem,
                severity: 'error',
            });
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
                  Entrar na conta
                </Typography>
                <Typography color="text.secondary" sx={{ mt: 1 }}>
                  Informe o número da sua conta e senha.
                </Typography>
              </Box>
              <Box component="form" onSubmit={handleSubmit} noValidate>
                <Stack spacing={3}>
                  <TextField
                    label="Número da conta"
                    value={numero}
                    onChange={(e) => setNumero(e.target.value)}
                    error={Boolean(erros.numero)}
                    autoComplete="username"
                    fullWidth
                  />
                  <TextField
                    label="senha"
                    value={senha}
                    onChange={(e) => setSenha(e.target.value)}
                    error={Boolean(erros.senha)}
                    autoComplete="current-password"
                    fullWidth
                    type="password"
                  />
                  <Button
                    type="submit"
                    variant="contained"
                    size="large"
                    fullWidth
                    disabled={loading}
                    startIcon={
                      loading ? (
                        <CircularProgress size={18} color="inherit" />
                      ) : undefined
                    }
                  >
                    {loading ? "Entrando" : "Entrar"}
                  </Button>
                  <Button component={Link} to="/cadastro">
                    Ainda não tenho conta
                  </Button>
                </Stack>
              </Box>
            </Stack>
          </CardContent>
        </Card>
        <Snackbar
          open={sessaoExpirada || notificacao.open}
          autoHideDuration={4000}
          onClose={handleCloseSnackbar}
          anchorOrigin={{ vertical: "bottom", horizontal: "center" }}
        >
          <Alert
            severity={sessaoExpirada ? "error" : notificacao.severity}
            variant="filled"
            onClose={handleCloseSnackbar}
          >
            {sessaoExpirada
              ? "Sua sessão expirou. Faça login novamente."
              : notificacao.mensagem}
          </Alert>
        </Snackbar>
      </Container>
    );
}