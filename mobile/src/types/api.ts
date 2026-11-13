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

/** Resposta de GET /usuarios/me (assumida com o mesmo formato do RegistroResponse). */
export type UsuarioAtual = RegistroResponse;

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
