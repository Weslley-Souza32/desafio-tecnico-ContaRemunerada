
import { Button, Container, Stack, Typography } from '@mui/material';
import { Link } from 'react-router';

type PlaceholderPageProps = {
  title: string;
};

export function PlaceholderPage({ title }: PlaceholderPageProps) {
  return (
    <Container maxWidth="sm" sx={{ py: 8 }}>
      <Stack spacing={3}>
        <Typography variant="h4" component="h1" color="primary">
          {title}
        </Typography>

        <Typography color="text.secondary">
          Esta página será implementada nas próximas issues.
        </Typography>

        <Stack direction="row" spacing={2} sx={{ flexWrap: 'wrap' }}>
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
      </Stack>
    </Container>
  );
}
