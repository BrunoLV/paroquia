const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080/api/v1';

export type LogLevel = 'INFO' | 'WARN' | 'ERROR' | 'DEBUG';

export interface ClientLogEntry {
  nivel: LogLevel;
  mensagem: string;
  rota?: string;
  componente?: string;
  detalhes?: string;
  timestamp?: string;
}

const BATCH_SIZE = 10;
const FLUSH_INTERVAL_MS = 3000;

class ClientLogger {
  private buffer: ClientLogEntry[] = [];
  private flushTimeout: ReturnType<typeof setTimeout> | null = null;
  private isFlushing = false;

  public info(mensagem: string, contexto?: { componente?: string; detalhes?: string }): void {
    this.log('INFO', mensagem, contexto);
  }

  public warn(mensagem: string, contexto?: { componente?: string; detalhes?: string }): void {
    this.log('WARN', mensagem, contexto);
  }

  public error(
    mensagem: string,
    error?: unknown,
    contexto?: { componente?: string; detalhes?: string }
  ): void {
    let stackTrace = contexto?.detalhes;
    if (error instanceof Error) {
      stackTrace = stackTrace ? `${stackTrace}\n${error.stack}` : error.stack;
    } else if (error && !stackTrace) {
      stackTrace = String(error);
    }

    this.log('ERROR', mensagem, {
      componente: contexto?.componente,
      detalhes: stackTrace,
    });
  }

  public debug(mensagem: string, contexto?: { componente?: string; detalhes?: string }): void {
    this.log('DEBUG', mensagem, contexto);
  }

  private log(
    nivel: LogLevel,
    mensagem: string,
    contexto?: { componente?: string; detalhes?: string }
  ): void {
    const rota = typeof window !== 'undefined' ? window.location.pathname : '/';
    const entry: ClientLogEntry = {
      nivel,
      mensagem,
      rota,
      componente: contexto?.componente ?? 'App',
      detalhes: contexto?.detalhes,
      timestamp: new Date().toISOString(),
    };

    // Imprime no console nativo para visibilidade no DevTools
    this.printToConsole(entry);

    this.buffer.push(entry);

    if (this.buffer.length >= BATCH_SIZE) {
      this.flush();
    } else if (!this.flushTimeout) {
      this.flushTimeout = setTimeout(() => {
        this.flush();
      }, FLUSH_INTERVAL_MS);
    }
  }

  public async flush(): Promise<void> {
    if (this.flushTimeout) {
      clearTimeout(this.flushTimeout);
      this.flushTimeout = null;
    }

    if (this.buffer.length === 0 || this.isFlushing) {
      return;
    }

    this.isFlushing = true;
    const batch = [...this.buffer];
    this.buffer = [];

    // Em ambiente de testes unitários (Vitest/jsdom), evita disparar chamadas fetch de rede não mockadas
    if (import.meta.env.MODE === 'test' && !(globalThis as Record<string, unknown>).__TEST_ENABLE_LOGGER_NETWORK__) {
      this.isFlushing = false;
      return;
    }

    try {
      await fetch(`${API_BASE_URL}/observabilidade/logs`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        credentials: 'include',
        body: JSON.stringify({ logs: batch }),
      });
    } catch {
      // Falha graciosa: evita loop recursivo se a API de observabilidade estiver indisponível
    } finally {
      this.isFlushing = false;
    }
  }

  public getBufferLength(): number {
    return this.buffer.length;
  }

  public clearBuffer(): void {
    this.buffer = [];
    if (this.flushTimeout) {
      clearTimeout(this.flushTimeout);
      this.flushTimeout = null;
    }
  }

  private printToConsole(entry: ClientLogEntry): void {
    const prefix = `[UI_${entry.nivel}] [${entry.componente}]`;
    switch (entry.nivel) {
      case 'ERROR':
        console.error(prefix, entry.mensagem, entry.detalhes ?? '');
        break;
      case 'WARN':
        console.warn(prefix, entry.mensagem, entry.detalhes ?? '');
        break;
      case 'DEBUG':
        console.debug(prefix, entry.mensagem, entry.detalhes ?? '');
        break;
      default:
        console.info(prefix, entry.mensagem, entry.detalhes ?? '');
        break;
    }
  }
}

export const logger = new ClientLogger();
export default logger;
