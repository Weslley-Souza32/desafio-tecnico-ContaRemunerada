import './App.css'
import { Box, Button, Card, CardContent, Container, Stack, Typography } from '@mui/material'

function App() {

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
