/**
 * Espelho dos DTOs do backend (backend/src/main/java/com/bytestorm/waygo/application/dto).
 * Ao alterar um record no backend, atualize o tipo correspondente aqui.
 * BigDecimal chega como number; LocalDateTime chega como string ISO sem fuso.
 */

export type StatusOperacional =
  | 'NORMAL'
  | 'VELOCIDADE_REDUZIDA'
  | 'PARALISADA'
  | 'OPERACAO_ENCERRADA'
  | 'OPERACAO_PARCIAL'
  | 'OPERACAO_TRANSITORIA'
  | 'ATIVIDADE_PROGRAMADA'
  | 'DESCONHECIDO';

export type CriterioRota = 'MENOR_TEMPO' | 'MENOS_BALDEACOES' | 'EQUILIBRADO';

export type TipoTransporte = 'METRO' | 'TREM_METROPOLITANO' | 'MONOTRILHO';

export type OrigemStatus = 'ADMINISTRADOR' | 'API';

export interface ErroResponse {
  status: number;
  message: string;
  data: string;
}

export interface EstacaoResumo {
  id: number;
  nome: string;
  latitude: number | null;
  longitude: number | null;
  ativa: boolean;
}

/** Resposta paginada do backend (PaginaResponse). `pagina` começa em 0. */
export interface Pagina<T> {
  conteudo: T[];
  pagina: number;
  tamanho: number;
  totalItens: number;
  totalPaginas: number;
  primeira: boolean;
  ultima: boolean;
}

export interface EstacoesParams {
  pagina: number;
  tamanho?: number;
  busca?: string;
  /** Só estações atendidas por mais de uma linha. */
  integracoes?: boolean;
}

export interface LinhaDaEstacao {
  id: number;
  nome: string;
  codigo: string;
  cor: string;
}

export interface EstacaoDetalhe extends EstacaoResumo {
  linhas: LinhaDaEstacao[];
}

export interface LinhaResumo {
  id: number;
  nome: string;
  codigo: string;
  cor: string;
  operadora: string;
  tipoTransporte: TipoTransporte;
  ativa: boolean;
}

export interface EstacaoDaLinha extends EstacaoResumo {
  sequencia: number;
}

export interface LinhaDetalhe extends LinhaResumo {
  estacoes: EstacaoDaLinha[];
}

export interface LinhaStatus {
  linhaId: number;
  status: StatusOperacional;
  descricao: string | null;
  origem: OrigemStatus;
  atualizadoEm: string | null;
}

export interface EstacaoRede {
  id: number;
  nome: string;
  latitude: number | null;
  longitude: number | null;
  linhaIds: number[];
}

export interface LinhaRede {
  id: number;
  nome: string;
  codigo: string;
  cor: string;
  operadora: string;
  tipoTransporte: TipoTransporte;
  status: StatusOperacional;
  statusDescricao: string | null;
  disponivel: boolean;
  estacaoIds: number[];
}

export interface ConexaoRede {
  linhaId: number;
  estacaoOrigemId: number;
  estacaoDestinoId: number;
  tempoSegundos: number;
  distanciaMetros: number;
}

export interface BaldeacaoRede {
  estacaoOrigemId: number;
  linhaOrigemId: number;
  estacaoDestinoId: number;
  linhaDestinoId: number;
  tempoSegundos: number;
}

export interface Rede {
  estacoes: EstacaoRede[];
  linhas: LinhaRede[];
  conexoes: ConexaoRede[];
  baldeacoes: BaldeacaoRede[];
}

export interface TrechoRota {
  ordem: number;
  linha: LinhaResumo;
  statusLinha: StatusOperacional;
  embarque: EstacaoResumo;
  desembarque: EstacaoResumo;
  sentido: EstacaoResumo;
  estacoes: EstacaoResumo[];
  quantidadeParadas: number;
  tempoSegundos: number;
  distanciaMetros: number;
}

export interface BaldeacaoRota {
  estacaoOrigem: EstacaoResumo;
  estacaoDestino: EstacaoResumo;
  linhaOrigem: LinhaResumo;
  linhaDestino: LinhaResumo;
  tempoSegundos: number;
}

export interface Rota {
  origem: EstacaoResumo;
  destino: EstacaoResumo;
  criterio: CriterioRota;
  /** true quando calculada com ?alternativa=true (só linhas em operação normal). */
  alternativa: boolean;
  tempoEstimadoSegundos: number;
  tempoEstimadoMinutos: number;
  distanciaMetros: number;
  quantidadeBaldeacoes: number;
  avisos: string[];
  trechos: TrechoRota[];
  baldeacoes: BaldeacaoRota[];
}

export interface RotaParams {
  origem: string;
  destino: string;
  criterio?: CriterioRota;
  /** Evita linhas fora da operação normal (status desconhecido conta como normal). */
  alternativa?: boolean;
}

// --- Conta e acesso ---

export type Role = 'USUARIO' | 'MODERADOR' | 'ADMINISTRADOR';

export interface LoginRequest {
  email: string;
  senha: string;
}

export interface LoginResponse {
  token: string;
  tipo: string;
  /** Instant ISO-8601 em UTC (com "Z"). */
  expiraEm: string;
  expiraEmSegundos: number;
}

export type RegistroRequest = LoginRequest;

export interface RegistroResponse {
  id: string;
  email: string;
  roles: Role[];
  criadoEm: string;
}

/** Uma conta: GET /usuarios/me (a do usuário logado) e GET /usuarios (admin). */
export interface Usuario {
  id: string;
  email: string;
  roles: Role[];
  criadoEm: string;
}

export type UsuarioAtual = Usuario;

// --- Administração (exigem a role ADMINISTRADOR) ---

export interface LinhaRequest {
  nome: string;
  codigo: string;
  cor?: string;
  operadora: string;
  tipoTransporte: TipoTransporte;
}

/** Campos omitidos mantêm o valor atual. Código e tipo não mudam. */
export interface LinhaAtualizacaoRequest {
  nome?: string;
  cor?: string;
  operadora?: string;
  ativa?: boolean;
}

export interface StatusLinhaRequest {
  status: StatusOperacional;
  descricao?: string;
}

/** Latitude e longitude andam juntas. Na atualização, campos omitidos mantêm o valor atual. */
export interface EstacaoRequest {
  nome?: string;
  latitude?: number;
  longitude?: number;
  ativa?: boolean;
}

// --- Rotas salvas (US08-US10) ---

export interface RotaSalvaRequest {
  nome: string;
  idEstacaoOrigem: number;
  idEstacaoDestino: number;
  criterio?: CriterioRota;
}

export interface RotaSalvaResponse {
  id: number;
  nome: string;
  origem: EstacaoResumo;
  destino: EstacaoResumo;
  criterio: CriterioRota;
  criadoEm: string;
}

// --- Agendamentos das rotas salvas (vários por rota) ---

export type DiaSemana = 'DOMINGO' | 'SEGUNDA' | 'TERCA' | 'QUARTA' | 'QUINTA' | 'SEXTA' | 'SABADO';

export interface RotaAgendadaRequest {
  /** "HH:mm" */
  horario: string;
  /** 0 a 1440. Na criação, o padrão do backend é 5; no PUT, omitido mantém o atual. */
  antecedenciaMinutos?: number;
  diasSemana: DiaSemana[];
  /** Na criação, o padrão é true; no PUT, omitido mantém o atual. */
  ativa?: boolean;
}

export interface RotaAgendada {
  id: number;
  idRotaSalva: number;
  /** "HH:mm" */
  horario: string;
  horarioPartidaMinutos: number;
  antecedenciaMinutos: number;
  /** Ordenados de domingo a sábado. */
  diasSemana: DiaSemana[];
  ativa: boolean;
  criadoEm: string;
}
