import {Component, type ErrorInfo, type ReactNode} from 'react';
import {Box, Typography} from '@mui/material';
import RefreshIcon from '@mui/icons-material/Refresh';
import ActionButton from '@/shared/presentation/components/ActionButton';
import logger from '@/shared/infrastructure/logging/logger';

interface Props {
  children: ReactNode;
}

interface State {
  hasError: boolean;
}

class AppErrorBoundary extends Component<Props, State> {
  public state: State = {hasError: false};

  public static getDerivedStateFromError(): State {
    return {hasError: true};
  }

  public componentDidCatch(error: Error, errorInfo: ErrorInfo): void {
    logger.error('Erro de renderização capturado por AppErrorBoundary', error, {
      componente: 'AppErrorBoundary',
      detalhes: errorInfo.componentStack?.slice(0, 500),
    });
  }

  private readonly handleReload = (): void => {
    window.location.reload();
  };

  public render(): ReactNode {
    if (!this.state.hasError) {
      return this.props.children;
    }

    return (
      <Box sx={{minHeight: 320, display: 'grid', placeItems: 'center', px: 3}}>
        <Box sx={{maxWidth: 480, textAlign: 'center'}}>
          <Typography component="h1" variant="h5" color="primary.main" gutterBottom>
            Não foi possível carregar esta tela
          </Typography>
          <Typography color="text.secondary" sx={{mb: 3}}>
            Atualize a página para tentar novamente.
          </Typography>
          <ActionButton color="primary" title="Atualizar página" icon={<RefreshIcon />} onClick={this.handleReload} />
        </Box>
      </Box>
    );
  }
}

export default AppErrorBoundary;
