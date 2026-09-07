import {
  Add as AddIcon,
  Block as BlockIcon,
  CheckCircleOutlined as ActivateIcon,
  Search as SearchIcon,
} from "@mui/icons-material";
import {
  Alert,
  Box,
  Button,
  Checkbox,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  FormControlLabel,
  MenuItem,
  Paper,
  Stack,
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableRow,
  TextField,
  Typography,
} from "@mui/material";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useEffect, useState } from "react";
import {
  alterarStatusFiel,
  createFiel,
  getFiel,
  getFieis,
  updateFiel,
} from "@/api/fieis";
import {
  criarRelacionamentoFamiliar,
  listarFamiliares,
  removerRelacionamentoFamiliar,
  type TipoRelacionamentoFamiliar,
} from "@/api/familiares";
import { listarAjustesPendentesDoFiel } from "@/api/ajustesCadastrais";
import { getUnidadesTerritoriais } from "@/api/unidadesTerritoriais";
import type {
  CreateFielPayload,
  Endereco,
  Fiel,
  TipoDocumentoEstrangeiro,
  UpdateFielPayload,
} from "@/api/types";
import PageHeader from "@/components/PageHeader";
import { useAuth } from "@/auth/AuthContext";
import { canManageCadastroFieis } from "@/auth/permissions";
import { useNotification } from "@/context/NotificationContext";

const vazio = (): CreateFielPayload => ({
  nome: "",
  sexo: "MASCULINO",
  dataNascimento: "",
  paisOrigem: "BRA",
  cpf: "",
  rg: "",
  tipoDocumentoEstrangeiro: undefined,
  numeroDocumentoEstrangeiro: "",
  email: "",
  celular: "",
  isWhatsapp: true,
  endereco: {},
  unidadeTerritorialId: "",
  isDizimista: false,
  permitirDivulgacaoAniversario: false,
});

const PAISES_ORIGEM = [
  { codigo: "BRA", nome: "Brasil" },
  { codigo: "ARG", nome: "Argentina" },
  { codigo: "BOL", nome: "Bolívia" },
  { codigo: "COL", nome: "Colômbia" },
  { codigo: "PRT", nome: "Portugal" },
  { codigo: "VEN", nome: "Venezuela" },
] as const;

const cadastroVazio = (): UpdateFielPayload => ({
  nome: "",
  sexo: "MASCULINO",
  dataNascimento: "",
  paisOrigem: "BRA",
  cpf: "",
  rg: "",
  tipoDocumentoEstrangeiro: undefined,
  numeroDocumentoEstrangeiro: "",
  email: "",
  celular: "",
  isWhatsapp: true,
  telefoneFixo: "",
  endereco: {},
  unidadeTerritorialId: "",
  isDizimista: false,
  permitirDivulgacaoAniversario: false,
});

const normalizarDigitos = (valor: string | null | undefined): string =>
  (valor ?? "").replace(/\D/g, "");

const paraCadastro = (fiel: Fiel): UpdateFielPayload => ({
  ...cadastroVazio(),
  nome: fiel.nome ?? "",
  sexo: fiel.sexo ?? "MASCULINO",
  dataNascimento: fiel.dataNascimento ?? "",
  paisOrigem: fiel.dadosIdentificacao?.paisOrigem ?? "BRA",
  cpf: normalizarDigitos(fiel.dadosIdentificacao?.cpfFormatado ?? fiel.cpf),
  rg: fiel.dadosIdentificacao?.rg ?? fiel.rg ?? "",
  tipoDocumentoEstrangeiro:
    fiel.dadosIdentificacao?.tipoDocumentoEstrangeiro ?? undefined,
  numeroDocumentoEstrangeiro:
    fiel.dadosIdentificacao?.documentoEstrangeiroFormatado ?? "",
  email: fiel.contato?.email ?? fiel.email ?? "",
  celular: normalizarDigitos(fiel.contato?.celularFormatado ?? fiel.telefone),
  isWhatsapp: fiel.contato?.isWhatsapp ?? true,
  telefoneFixo: normalizarDigitos(
    fiel.contato?.telefoneFixoFormatado ?? fiel.telefoneFixo,
  ),
  endereco:
    typeof fiel.endereco === "object" && fiel.endereco ? fiel.endereco : {},
  unidadeTerritorialId: fiel.igrejaFrequenta?.id ?? "",
  isDizimista: fiel.isDizimista ?? false,
  permitirDivulgacaoAniversario: fiel.permitirDivulgacaoAniversario ?? false,
});

const Fieis = () => {
  const [busca, setBusca] = useState("");
  const [novo, setNovo] = useState(false);
  const [form, setForm] = useState(vazio());
  const [selecionado, setSelecionado] = useState<Fiel | null>(null);
  const [cadastro, setCadastro] = useState<UpdateFielPayload>(cadastroVazio());
  const [destino, setDestino] = useState("");
  const [tipo, setTipo] = useState<TipoRelacionamentoFamiliar>("CONJUGE");
  const cliente = useQueryClient();
  const { showSuccess, showError } = useNotification();
  const id = selecionado?.fielId ?? selecionado?.id;
  const { user } = useAuth();
  const podeGerirCadastro = canManageCadastroFieis(user);
  const fieis = useQuery({ queryKey: ["fieis"], queryFn: getFieis });
  const unidades = useQuery({
    queryKey: ["unidades"],
    queryFn: getUnidadesTerritoriais,
  });
  const detalhe = useQuery({
    queryKey: ["fiel", id],
    queryFn: () => getFiel(id!),
    enabled: !!id,
  });
  const familiares = useQuery({
    queryKey: ["familiares", id],
    queryFn: () => listarFamiliares(id!),
    enabled: !!id,
  });
  const ajustesPendentes = useQuery({
    queryKey: ["fiel", id, "ajustes-cadastrais"],
    queryFn: () => listarAjustesPendentesDoFiel(id!),
    enabled: !!id && podeGerirCadastro,
  });
  const criar = useMutation({
    mutationFn: createFiel,
    onSuccess: () => {
      cliente.invalidateQueries({ queryKey: ["fieis"] });
      setNovo(false);
      setForm(vazio());
      showSuccess("Fiel cadastrado.");
    },
    onError: () => showError("Não foi possível cadastrar o fiel."),
  });
  const status = useMutation({
    mutationFn: ({ id, ativo }: { id: string; ativo: boolean }) =>
      alterarStatusFiel(id, ativo),
    onSuccess: (_, v) => {
      cliente.invalidateQueries({ queryKey: ["fieis"] });
      showSuccess(v.ativo ? "Cadastro reativado." : "Cadastro desativado.");
    },
    onError: () => showError("Não foi possível alterar o status."),
  });
  const salvarCadastro = useMutation({
    mutationFn: () => updateFiel(id!, cadastro),
    onSuccess: () => {
      cliente.invalidateQueries({ queryKey: ["fieis"] });
      cliente.invalidateQueries({ queryKey: ["fiel", id] });
      showSuccess("Cadastro atualizado.");
    },
    onError: () => showError("Não foi possível atualizar o cadastro."),
  });
  const criarFamiliar = useMutation({
    mutationFn: () => criarRelacionamentoFamiliar(id!, destino, tipo),
    onSuccess: () => {
      familiares.refetch();
      setDestino("");
      showSuccess("Vínculo familiar criado.");
    },
    onError: () => showError("Não foi possível criar o vínculo."),
  });
  const removerFamiliar = useMutation({
    mutationFn: (relacaoId: string) =>
      removerRelacionamentoFamiliar(id!, relacaoId),
    onSuccess: () => {
      familiares.refetch();
      showSuccess("Vínculo familiar inativado.");
    },
    onError: () => showError("Não foi possível inativar o vínculo."),
  });
  const filtrados = (fieis.data ?? []).filter((f) =>
    f.nome?.toLowerCase().includes(busca.toLowerCase()),
  );
  const alterar = <K extends keyof CreateFielPayload>(
    campo: K,
    valor: CreateFielPayload[K],
  ) => setForm((f) => ({ ...f, [campo]: valor }));
  const alterarCadastro = <K extends keyof UpdateFielPayload>(
    campo: K,
    valor: UpdateFielPayload[K],
  ) => setCadastro((atual) => ({ ...atual, [campo]: valor }));
  const alterarEndereco = (campo: keyof Endereco, valor: string) =>
    setCadastro((atual) => ({
      ...atual,
      endereco: {
        ...(typeof atual.endereco === "object" && atual.endereco
          ? atual.endereco
          : {}),
        [campo]: valor,
      },
    }));
  const alterarEnderecoNovo = (campo: keyof Endereco, valor: string) =>
    setForm((atual) => ({
      ...atual,
      endereco: {
        ...(typeof atual.endereco === "object" && atual.endereco
          ? atual.endereco
          : {}),
        [campo]: valor,
      },
    }));
  const abrir = (f: Fiel) => {
    setSelecionado(f);
    setCadastro(paraCadastro(f));
  };
  useEffect(() => {
    if (detalhe.data) setCadastro(paraCadastro(detalhe.data));
  }, [detalhe.data]);
  const endereco =
    typeof cadastro.endereco === "object" && cadastro.endereco
      ? cadastro.endereco
      : {};
  const enderecoNovo =
    typeof form.endereco === "object" && form.endereco ? form.endereco : {};
  const estrangeiro = form.paisOrigem !== "BRA";
  const documentoEstrangeiroPreenchido = Boolean(
    form.tipoDocumentoEstrangeiro && form.numeroDocumentoEstrangeiro,
  );
  const enderecoObrigatorioPreenchido = Boolean(
    enderecoNovo.logradouro &&
      enderecoNovo.numero &&
      enderecoNovo.bairro &&
      enderecoNovo.cidade &&
      enderecoNovo.estado &&
      enderecoNovo.cep,
  );
  const podeSalvarNovo = Boolean(
    form.nome &&
      form.dataNascimento &&
      form.email &&
      form.celular &&
      form.unidadeTerritorialId &&
      enderecoObrigatorioPreenchido &&
      (estrangeiro ? documentoEstrangeiroPreenchido : form.cpf) &&
      !criar.isPending,
  );
  const acaoNovoFiel = podeGerirCadastro ? (
    <Button
      color="primary"
      variant="outlined"
      startIcon={<AddIcon />}
      onClick={() => setNovo(true)}
    >
      Novo fiel
    </Button>
  ) : undefined;
  return (
    <Box sx={{ p: 3 }}>
      <PageHeader
        icon={<SearchIcon />}
        title="Gestão de Fiéis"
        subtitle="Cadastro civil, contato e vínculo comunitário."
        actions={acaoNovoFiel}
      />
      <Paper sx={{ p: 2, mb: 2 }}>
        <TextField
          fullWidth
          value={busca}
          onChange={(e) => setBusca(e.target.value)}
          placeholder="Buscar fiel por nome"
        />
      </Paper>
      <Paper>
        <Table>
          <TableHead>
            <TableRow>
              <TableCell>Nome</TableCell>
              <TableCell>Contato</TableCell>
              <TableCell>Comunidade</TableCell>
              <TableCell>Status</TableCell>
              {podeGerirCadastro ? (
                <TableCell align="right">Ações</TableCell>
              ) : null}
            </TableRow>
          </TableHead>
          <TableBody>
            {filtrados.map((f) => {
              const fielId = f.fielId ?? f.id;
              const ativo = f.isAtivo ?? f.ativo;
              const acaoStatus = podeGerirCadastro ? (
                <TableCell align="right">
                  <Button
                    size="small"
                    color={ativo ? "warning" : "success"}
                    startIcon={ativo ? <BlockIcon /> : <ActivateIcon />}
                    onClick={(e) => {
                      e.stopPropagation();
                      status.mutate({ id: fielId, ativo: !ativo });
                    }}
                  >
                    {ativo ? "Desativar" : "Reativar"}
                  </Button>
                </TableCell>
              ) : null;
              return (
                <TableRow
                  hover
                  key={fielId}
                  onClick={() => abrir(f)}
                  sx={{ cursor: "pointer" }}
                >
                  <TableCell>{f.nome}</TableCell>
                  <TableCell>
                    {f.contato?.celularFormatado ?? f.telefone}
                  </TableCell>
                  <TableCell>{f.igrejaFrequenta?.nome ?? "-"}</TableCell>
                  <TableCell>{ativo ? "Ativo" : "Inativo"}</TableCell>
                  {acaoStatus}
                </TableRow>
              );
            })}
          </TableBody>
        </Table>
      </Paper>
      <Dialog
        open={novo}
        onClose={() => setNovo(false)}
        maxWidth="sm"
        fullWidth
      >
        <DialogTitle>Novo fiel</DialogTitle>
        <DialogContent>
          <Stack spacing={2} sx={{ mt: 1 }}>
            <Alert severity="info">
              Dados são armazenados sem máscaras e exibidos formatados.
            </Alert>
            <TextField
              required
              label="Nome completo"
              value={form.nome}
              onChange={(e) => alterar("nome", e.target.value)}
            />
            <TextField
              select
              label="Sexo"
              value={form.sexo}
              onChange={(e) =>
                alterar("sexo", e.target.value as CreateFielPayload["sexo"])
              }
            >
              <MenuItem value="MASCULINO">Masculino</MenuItem>
              <MenuItem value="FEMININO">Feminino</MenuItem>
            </TextField>
            <TextField
              required
              type="date"
              label="Nascimento"
              slotProps={{ inputLabel: { shrink: true } }}
              value={form.dataNascimento}
              onChange={(e) => alterar("dataNascimento", e.target.value)}
            />
            <TextField
              select
              required
              label="País de origem"
              value={form.paisOrigem}
              onChange={(e) => {
                const paisOrigem = e.target.value;
                setForm((atual) => ({
                  ...atual,
                  paisOrigem,
                  cpf: paisOrigem === "BRA" ? atual.cpf : "",
                  rg: paisOrigem === "BRA" ? atual.rg : "",
                  tipoDocumentoEstrangeiro:
                    paisOrigem === "BRA"
                      ? undefined
                      : atual.tipoDocumentoEstrangeiro,
                  numeroDocumentoEstrangeiro:
                    paisOrigem === "BRA"
                      ? ""
                      : atual.numeroDocumentoEstrangeiro,
                }));
              }}
            >
              {PAISES_ORIGEM.map((pais) => (
                <MenuItem key={pais.codigo} value={pais.codigo}>
                  {pais.nome}
                </MenuItem>
              ))}
            </TextField>
            {estrangeiro ? (
              <>
                <TextField
                  select
                  required
                  label="Documento estrangeiro"
                  value={form.tipoDocumentoEstrangeiro ?? ""}
                  onChange={(e) =>
                    alterar(
                      "tipoDocumentoEstrangeiro",
                      (e.target.value || undefined) as
                        | TipoDocumentoEstrangeiro
                        | undefined,
                    )
                  }
                >
                  <MenuItem value="RNE_RNM">RNE/RNM</MenuItem>
                  <MenuItem value="PASSAPORTE">Passaporte</MenuItem>
                </TextField>
                <TextField
                  required
                  label="Número do documento"
                  value={form.numeroDocumentoEstrangeiro ?? ""}
                  onChange={(e) =>
                    alterar("numeroDocumentoEstrangeiro", e.target.value)
                  }
                />
              </>
            ) : (
              <>
                <TextField
                  required
                  label="CPF"
                  value={form.cpf}
                  onChange={(e) => alterar("cpf", e.target.value)}
                  helperText="Informe os 11 dígitos, sem necessidade de máscara."
                />
                <TextField
                  label="RG"
                  value={form.rg ?? ""}
                  onChange={(e) => alterar("rg", e.target.value)}
                />
              </>
            )}
            <TextField
              required
              label="E-mail"
              value={form.email}
              onChange={(e) => alterar("email", e.target.value)}
            />
            <TextField
              required
              label="Celular"
              value={form.celular}
              onChange={(e) => alterar("celular", e.target.value)}
            />
            <TextField
              label="Telefone fixo"
              value={form.telefoneFixo ?? ""}
              onChange={(e) => alterar("telefoneFixo", e.target.value)}
            />
            <Typography variant="subtitle2">Endereço</Typography>
            <Stack direction={{ xs: "column", sm: "row" }} spacing={2}>
              <TextField required fullWidth label="Logradouro" value={enderecoNovo.logradouro ?? ""} onChange={(e) => alterarEnderecoNovo("logradouro", e.target.value)} />
              <TextField required fullWidth label="Número" value={enderecoNovo.numero ?? ""} onChange={(e) => alterarEnderecoNovo("numero", e.target.value)} />
            </Stack>
            <TextField fullWidth label="Complemento" value={enderecoNovo.complemento ?? ""} onChange={(e) => alterarEnderecoNovo("complemento", e.target.value)} />
            <Stack direction={{ xs: "column", sm: "row" }} spacing={2}>
              <TextField required fullWidth label="Bairro" value={enderecoNovo.bairro ?? ""} onChange={(e) => alterarEnderecoNovo("bairro", e.target.value)} />
              <TextField required fullWidth label="Cidade" value={enderecoNovo.cidade ?? ""} onChange={(e) => alterarEnderecoNovo("cidade", e.target.value)} />
            </Stack>
            <Stack direction={{ xs: "column", sm: "row" }} spacing={2}>
              <TextField required fullWidth label="UF" slotProps={{ htmlInput: { maxLength: 2 } }} value={enderecoNovo.estado ?? ""} onChange={(e) => alterarEnderecoNovo("estado", e.target.value.toUpperCase())} />
              <TextField required fullWidth label="CEP" value={enderecoNovo.cep ?? ""} onChange={(e) => alterarEnderecoNovo("cep", e.target.value)} />
            </Stack>
            <TextField
              select
              required
              label="Igreja que frequenta"
              value={form.unidadeTerritorialId}
              onChange={(e) => alterar("unidadeTerritorialId", e.target.value)}
            >
              {(unidades.data ?? [])
                .filter((u) => u.tipo === "MATRIZ" || u.tipo === "COMUNIDADE")
                .map((u) => (
                  <MenuItem key={u.id} value={u.id}>
                    {u.nome}
                  </MenuItem>
                ))}
            </TextField>
            <FormControlLabel
              control={
                <Checkbox
                  checked={form.isWhatsapp}
                  onChange={(e) => alterar("isWhatsapp", e.target.checked)}
                />
              }
              label="Este celular possui WhatsApp"
            />
            <FormControlLabel
              control={
                <Checkbox
                  checked={form.isDizimista}
                  onChange={(e) => alterar("isDizimista", e.target.checked)}
                />
              }
              label="Dizimista"
            />
            <FormControlLabel
              control={
                <Checkbox
                  checked={form.permitirDivulgacaoAniversario}
                  onChange={(e) =>
                    alterar("permitirDivulgacaoAniversario", e.target.checked)
                  }
                />
              }
              label="Autoriza divulgar somente dia e mês do aniversário"
            />
          </Stack>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setNovo(false)}>Cancelar</Button>
          <Button
            variant="contained"
            disabled={!podeSalvarNovo}
            onClick={() => criar.mutate(form)}
          >
            Salvar
          </Button>
        </DialogActions>
      </Dialog>
      <Dialog
        open={!!selecionado}
        onClose={() => setSelecionado(null)}
        maxWidth="md"
        fullWidth
      >
        <DialogTitle>Ficha do fiel</DialogTitle>
        <DialogContent>
          <Stack spacing={3} sx={{ mt: 1 }}>
            <Typography variant="h6">
              {detalhe.data?.nome ?? selecionado?.nome}
            </Typography>
            <Typography variant="body2">
              {detalhe.data?.dadosIdentificacao?.cpfFormatado ??
                "Documento não informado"}{" "}
              ·{" "}
              {detalhe.data?.igrejaFrequenta?.nome ??
                "Comunidade não informada"}
            </Typography>
            {podeGerirCadastro && (ajustesPendentes.data ?? []).map((ajuste) => (
              <Alert key={ajuste.id} severity="warning">
                Ajuste cadastral pendente: {[
                  ajuste.email,
                  ajuste.celular,
                  ajuste.endereco.logradouro,
                  ajuste.endereco.cidade,
                ].filter(Boolean).join(" · ") || "dados propostos"}.
              </Alert>
            ))}
            {podeGerirCadastro ? (
              <>
                <Typography variant="subtitle1">Dados civis</Typography>
                <Stack direction={{ xs: "column", sm: "row" }} spacing={2}>
                  <TextField required fullWidth size="small" label="Nome completo" value={cadastro.nome ?? ""} onChange={(e) => alterarCadastro("nome", e.target.value)} />
                  <TextField select required size="small" label="Sexo" value={cadastro.sexo ?? "MASCULINO"} onChange={(e) => alterarCadastro("sexo", e.target.value as UpdateFielPayload["sexo"])}><MenuItem value="MASCULINO">Masculino</MenuItem><MenuItem value="FEMININO">Feminino</MenuItem></TextField>
                  <TextField required size="small" type="date" label="Nascimento" slotProps={{ inputLabel: { shrink: true } }} value={cadastro.dataNascimento ?? ""} onChange={(e) => alterarCadastro("dataNascimento", e.target.value)} />
                </Stack>
                <Stack direction={{ xs: "column", sm: "row" }} spacing={2}>
                  <TextField select required fullWidth size="small" label="País de origem" value={cadastro.paisOrigem ?? "BRA"} onChange={(e) => alterarCadastro("paisOrigem", e.target.value)}><MenuItem value="BRA">Brasil</MenuItem><MenuItem value="PRT">Portugal</MenuItem><MenuItem value="ARG">Argentina</MenuItem><MenuItem value="BOL">Bolívia</MenuItem><MenuItem value="COL">Colômbia</MenuItem><MenuItem value="VEN">Venezuela</MenuItem></TextField>
                  <TextField fullWidth size="small" label="CPF" value={cadastro.cpf ?? ""} onChange={(e) => alterarCadastro("cpf", e.target.value)} />
                  <TextField fullWidth size="small" label="RG" value={cadastro.rg ?? ""} onChange={(e) => alterarCadastro("rg", e.target.value)} />
                </Stack>
                {cadastro.paisOrigem !== "BRA" ? <Stack direction={{ xs: "column", sm: "row" }} spacing={2}><TextField select required fullWidth size="small" label="Documento estrangeiro" value={cadastro.tipoDocumentoEstrangeiro ?? ""} onChange={(e) => alterarCadastro("tipoDocumentoEstrangeiro", (e.target.value || undefined) as TipoDocumentoEstrangeiro | undefined)}><MenuItem value="RNE_RNM">RNE/RNM</MenuItem><MenuItem value="PASSAPORTE">Passaporte</MenuItem></TextField><TextField required fullWidth size="small" label="Número do documento" value={cadastro.numeroDocumentoEstrangeiro ?? ""} onChange={(e) => alterarCadastro("numeroDocumentoEstrangeiro", e.target.value)} /></Stack> : null}
                <Typography variant="subtitle1">Contato e endereço</Typography>
                <Stack direction={{ xs: "column", sm: "row" }} spacing={2}>
                  <TextField required fullWidth size="small" label="E-mail" value={cadastro.email ?? ""} onChange={(e) => alterarCadastro("email", e.target.value)} />
                  <TextField required fullWidth size="small" label="Celular" value={cadastro.celular ?? ""} onChange={(e) => alterarCadastro("celular", e.target.value)} />
                  <TextField fullWidth size="small" label="Telefone fixo" value={cadastro.telefoneFixo ?? ""} onChange={(e) => alterarCadastro("telefoneFixo", e.target.value)} />
                </Stack>
                <FormControlLabel control={<Checkbox checked={cadastro.isWhatsapp ?? false} onChange={(e) => alterarCadastro("isWhatsapp", e.target.checked)} />} label="Este celular possui WhatsApp" />
                <Stack direction={{ xs: "column", sm: "row" }} spacing={2}>
                  <TextField fullWidth size="small" label="Logradouro" value={endereco.logradouro ?? ""} onChange={(e) => alterarEndereco("logradouro", e.target.value)} />
                  <TextField fullWidth size="small" label="Número" value={endereco.numero ?? ""} onChange={(e) => alterarEndereco("numero", e.target.value)} />
                  <TextField fullWidth size="small" label="Complemento" value={endereco.complemento ?? ""} onChange={(e) => alterarEndereco("complemento", e.target.value)} />
                </Stack>
                <Stack direction={{ xs: "column", sm: "row" }} spacing={2}>
                  <TextField fullWidth size="small" label="Bairro" value={endereco.bairro ?? ""} onChange={(e) => alterarEndereco("bairro", e.target.value)} />
                  <TextField fullWidth size="small" label="Cidade" value={endereco.cidade ?? ""} onChange={(e) => alterarEndereco("cidade", e.target.value)} />
                  <TextField fullWidth size="small" label="UF" value={endereco.estado ?? ""} onChange={(e) => alterarEndereco("estado", e.target.value)} />
                  <TextField fullWidth size="small" label="CEP" value={endereco.cep ?? ""} onChange={(e) => alterarEndereco("cep", e.target.value)} />
                </Stack>
                <TextField select required fullWidth size="small" label="Igreja que frequenta" value={cadastro.unidadeTerritorialId ?? ""} onChange={(e) => alterarCadastro("unidadeTerritorialId", e.target.value)}>{(unidades.data ?? []).filter((u) => u.tipo === "MATRIZ" || u.tipo === "COMUNIDADE").map((u) => <MenuItem key={u.id} value={u.id}>{u.nome}</MenuItem>)}</TextField>
                <Stack direction={{ xs: "column", sm: "row" }}><FormControlLabel control={<Checkbox checked={cadastro.isDizimista ?? false} onChange={(e) => alterarCadastro("isDizimista", e.target.checked)} />} label="Dizimista" /><FormControlLabel control={<Checkbox checked={cadastro.permitirDivulgacaoAniversario ?? false} onChange={(e) => alterarCadastro("permitirDivulgacaoAniversario", e.target.checked)} />} label="Autoriza divulgar dia e mês do aniversário" /></Stack>
                <Button color="primary" variant="outlined" startIcon={<ActivateIcon />} disabled={salvarCadastro.isPending} onClick={() => salvarCadastro.mutate()}>Salvar alterações</Button>
              </>
            ) : null}
            <Typography variant="h6">Família</Typography>
            {(familiares.data ?? []).map((r) => {
              const acaoInativar = podeGerirCadastro ? (
                <Button
                  size="small"
                  color="error"
                  startIcon={<BlockIcon />}
                  onClick={() => removerFamiliar.mutate(r.id)}
                >
                  Inativar
                </Button>
              ) : null;
              return (
                <Stack
                  key={r.id}
                  direction="row"
                  sx={{ justifyContent: "space-between" }}
                >
                  <Typography>
                    {r.nome} · {r.relacao}
                  </Typography>
                  {acaoInativar}
                </Stack>
              );
            })}
            {podeGerirCadastro ? (
              <Stack direction={{ xs: "column", sm: "row" }} spacing={2}>
                <TextField
                  select
                  fullWidth
                  label="Fiel relacionado"
                  value={destino}
                  onChange={(e) => setDestino(e.target.value)}
                >
                  {(fieis.data ?? [])
                    .filter((f) => (f.fielId ?? f.id) !== id)
                    .map((f) => (
                      <MenuItem key={f.fielId ?? f.id} value={f.fielId ?? f.id}>
                        {f.nome}
                      </MenuItem>
                    ))}
                </TextField>
                <TextField
                  select
                  label="Relação"
                  value={tipo}
                  onChange={(e) =>
                    setTipo(e.target.value as TipoRelacionamentoFamiliar)
                  }
                >
                  <MenuItem value="CONJUGE">Cônjuge</MenuItem>
                  <MenuItem value="GENITOR">Genitor</MenuItem>
                  <MenuItem value="FILHO">Filho(a)</MenuItem>
                  <MenuItem value="IRMAO">Irmão(ã)</MenuItem>
                </TextField>
                <Button
                  color="primary"
                  variant="contained"
                  startIcon={<AddIcon />}
                  disabled={!destino || criarFamiliar.isPending}
                  onClick={() => criarFamiliar.mutate()}
                >
                  Vincular
                </Button>
              </Stack>
            ) : null}
          </Stack>
        </DialogContent>
        <DialogActions>
          <Button
            color="secondary"
            startIcon={<BlockIcon />}
            onClick={() => setSelecionado(null)}
          >
            Fechar
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
};
export default Fieis;
