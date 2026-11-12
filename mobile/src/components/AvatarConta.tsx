import { useState } from 'react';
import { Pressable } from 'react-native';

import { useEmailDaSessao } from '@/store/sessao';

import { MenuConta } from './MenuConta';
import { Avatar } from './ui/Avatar';

/** Avatar tocável do canto das telas: abre o pop-up da conta. */
export function AvatarConta() {
  const email = useEmailDaSessao();
  const [menuAberto, setMenuAberto] = useState(false);

  return (
    <>
      <Pressable
        onPress={() => setMenuAberto(true)}
        accessibilityRole="button"
        accessibilityLabel={email ? `Conta de ${email}` : 'Entrar ou criar conta'}
        accessibilityHint="Abre as opções da conta"
        hitSlop={4}
        className="rounded-full active:opacity-70"
      >
        <Avatar email={email} />
      </Pressable>
      <MenuConta visivel={menuAberto} aoFechar={() => setMenuAberto(false)} />
    </>
  );
}
