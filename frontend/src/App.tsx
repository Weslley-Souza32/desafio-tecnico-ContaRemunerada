import { useEffect, useState } from 'react';
import './App.css'
import { Alert, Box, Button, Card, CardContent, Container, Stack, Typography } from '@mui/material'
import { getApiHealth } from './api/healthService';

function App() {

  const [apiStatus, setApiStatus] = useState('Verificando API...');

  useEffect(() => {
    getApiHealth()
      .then((response) => {
        setApiStatus(`API: ${response.status}`);
      })
      .catch((error: unknown) => {
        console.error('Erro ao consultar API:', error);
        setApiStatus('Não possível conectar à API');
      });
  }, []);

  return (
    <Box sx={{ minHeight: '100vh', py: 8 }}>
      <Container maxWidth="md">
        <Stack spacing={4}>
          <Box>
            <Typography
              variant="h4"
              component="h1"
              color="primary"
              sx={{ fontWeight: 700 }}
            >
              Conta Remunerada
            </Typography>

            <Typography
              variant="body1"
              color="text.secondary"
              sx={{ mt: 1 }}
            >
              Gerencie sua conta, consulte seu saldo
              e acompanhe seus rendimentos.
            </Typography>
          </Box>
          <Alert severity={apiStatus === 'API: UP' ? 'success' : 'info'}>
            {apiStatus}
          </Alert>
          <Card>
            <CardContent>
              <Stack spacing={3}>
                <Box>
                  <Typography
                    variant="overline"
                    color="text.secondary"
                  >
                    Saldo disponível
                  </Typography>

                  <Typography
                    variant="h3"
                    sx={{ fontWeight: 700 }}
                  >
                    R$ 0,00
                  </Typography>
                </Box>

                <Stack direction="row" spacing={2}>
                  <Button variant="contained">
                    Depositar
                  </Button>

                  <Button variant="outlined">
                    Sacar
                  </Button>
                </Stack>
              </Stack>
            </CardContent>
          </Card>
        </Stack>
      </Container>
    </Box>
  );
}

export default App
