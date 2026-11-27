import { useRef, useState } from "react";
import type { TipoMovimentacao } from "../types/movimentacao";
import { Alert, Button, CircularProgress, Dialog, DialogActions, DialogContent, DialogTitle, Stack, TextField, Typography } from "@mui/material";
import { normalizarValorMonetario } from "../utils/normalizarValorMonetario";
import { movimentacaoService } from "../api/movimentacaoService";
import { handleApiError } from "../api/handleApiError";
import axios from "axios";

interface MovimentacaoDialogProps {
    open: boolean;
    tipo: TipoMovimentacao;
    contaId: number;
    onClose: () => void;
    onSuccess: (tipo: TipoMovimentacao) => void;
}

export default function MovimentacaoDialog({
    open,
    tipo,
    contaId,
    onClose,
    onSuccess,
}: MovimentacaoDialogProps) {

    const[valor, setValor] = useState('');
    const [tentouConfirmar, setTentouConfirmar] = useState(false);
    const [enviando, setEnviando] = useState(false);
    const [erroOperacao, setErroOperacao] =useState('');

    const envioEmAndamento = useRef(false);

    const valorNormalizado = normalizarValorMonetario(valor);
    const valorInvalido = tentouConfirmar && valorNormalizado === null;

    const deposito = tipo === 'DEPOSITO';

    const titulo = deposito ? 'Realizar depósito' : 'Realizar saque';

    function handleCancelar() {
        if (envioEmAndamento.current){
            return;
        }
        setValor('');
        setTentouConfirmar(false);
        onClose();
    }

    
async function handleConfirmar() {
  if (envioEmAndamento.current) {
    return;
  }

  setTentouConfirmar(true);
  setErroOperacao('');

  if (valorNormalizado === null) {
    return;
  }

  envioEmAndamento.current = true;
  setEnviando(true);

  try {
    const dados = {
      valor: valorNormalizado,
    };

    if (tipo === 'DEPOSITO') {
      await movimentacaoService.depositar(contaId, dados);
    } else {
      await movimentacaoService.sacar(contaId, dados);
    }

    setValor('');
    setTentouConfirmar(false);

    onSuccess(tipo);
    onClose();
  } catch (error: unknown) {
    if (axios.isAxiosError(error) && !error.response) {
        setErroOperacao(
            'Não foi possível confirmar o resultado da operação. ' + 
            'Consulte seu saldo e extrato antes de tentar novamente, ' +
            'pois a movimentação pode ter sido concluída.' 
        )
    } else {
        setErroOperacao(handleApiError(error).mensagem);
    }
  } finally {
    envioEmAndamento.current = false;
    setEnviando(false);
  }
}


    return (
      <Dialog
        open={open}
        onClose={(_event, reason) => {
            if (envioEmAndamento.current){
                return;
            }
            if (reason === 'escapeKeyDown' || reason === 'backdropClick') {
                handleCancelar();
            }
        }}
        fullWidth
        maxWidth="xs"
      >
        <DialogTitle sx={{ fontWeight: 600 }} color="textSecondary">
          {titulo}
        </DialogTitle>
        <DialogContent>
          <Stack spacing={2} sx={{ pt: 1 }}>
            <Typography variant="body2" color="textSecondary">
              Informe o valor da movimentação
            </Typography>
            <TextField
              label="Valor (R$)"
              placeholder="Ex.: 150,00"
              value={valor}
              onChange={(e) => setValor(e.target.value)}
              error={valorInvalido}
              helperText={
                valorInvalido
                  ? "Informe um valor válido maior que zero, com até duas casas decimais."
                  : "Utilize vírgula para os centavos Ex.: 150,00."
              }
              fullWidth
              autoFocus
              slotProps={{
                htmlInput: {
                  inputMode: "decimal",
                },
              }}
            />
            {erroOperacao && (
              <Alert severity="error">
                {erroOperacao}
              </Alert>
            )}
          </Stack>
        </DialogContent>
        <DialogActions sx={{ p: 2 }}>
          <Button onClick={handleCancelar} disabled={enviando}>
            Cancelar
          </Button>
          <Button
            variant="contained"
            onClick={handleConfirmar}
            disabled={enviando}
            startIcon={
              enviando ? (
                <CircularProgress size={16} color="inherit" />
              ) : undefined
            }
          >
            {enviando ? "Processando..." : "Confirmar"}
          </Button>
        </DialogActions>
      </Dialog>
    );
}
