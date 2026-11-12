import AsyncStorage from '@react-native-async-storage/async-storage';
import * as SecureStore from 'expo-secure-store';
import { Platform } from 'react-native';

/**
 * Guarda dados sensíveis (token) no Keychain/Keystore do aparelho.
 * O expo-secure-store não funciona na web; lá cai no AsyncStorage (localStorage).
 */
const naWeb = Platform.OS === 'web';

export const armazenamentoSeguro = {
  ler: (chave: string): Promise<string | null> =>
    naWeb ? AsyncStorage.getItem(chave) : SecureStore.getItemAsync(chave),
  gravar: (chave: string, valor: string): Promise<void> =>
    naWeb ? AsyncStorage.setItem(chave, valor) : SecureStore.setItemAsync(chave, valor),
  apagar: (chave: string): Promise<void> =>
    naWeb ? AsyncStorage.removeItem(chave) : SecureStore.deleteItemAsync(chave),
};
