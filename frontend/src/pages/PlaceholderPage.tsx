
import { Button, Container, Stack, Typography } from '@mui/material';
import { Link, useNavigate } from 'react-router';
import { tokenStorage } from '../auth/tokenStorage';

type PlaceholderPageProps = {
  title: string;
};

export function PlaceholderPage({ title }: PlaceholderPageProps) {
  const navigate = useNavigate();

function handleLogout() {
  tokenStorage.remove();
  navigate('/login', { replace: true });
}

  return (
    <Container maxWidth="sm" sx={{ py: 8 }}>
      <Stack spacing={3}>
        <Typography variant="h4" component="h1" color="primary">
          {title}
        </Typography>

        <Typography color="text.secondary">
          Esta página será implementada nas próximas issues.
        </Typography>

        <Stack direction="row" spacing={2} sx={{ flexWrap: "wrap" }}>
          <Button component={Link} to="/login">
            Login
          </Button>

          <Button component={Link} to="/cadastro">
            Abrir conta
          </Button>

          <Button component={Link} to="/painel">
            Painel
          </Button>
        </Stack>
        {title === "Painel da conta" && (
          <Button variant="outlined" color="error" onClick={handleLogout}>
            Sair da conta
          </Button>
        )}
      </Stack>
    </Container>
  );
}
