import type { StatusOperacional } from '@/types/api';
import { infoStatus } from '@/utils/status';

import { Badge } from './ui/Badge';

export function StatusBadge({ status }: { status: StatusOperacional }) {
  const info = infoStatus(status);
  return <Badge rotulo={info.rotulo} tom={info.tom} />;
}
