import { Ban } from 'lucide-react-native';
import { useState } from 'react';
import { KeyboardAvoidingView, Platform, ScrollView, Switch, Text, View } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';

import { AlertaErro } from '@/components/ui/AlertaErro';
import { Button } from '@/components/ui/Button';
import { Card } from '@/components/ui/Card';
import { TextField } from '@/components/ui/TextField';
import { colors } from '@/theme/colors';
import type { EstacaoRequest, EstacaoResumo } from '@/types/api';
import { formatarCoordenada, lerCoordenada } from '@/utils/admin';

import { SeletorPosicao } from './SeletorPosicao';

interface FormularioEstacaoProps {
  /** Ausente na criação. */
  existente?: EstacaoResumo;
  /** Linhas que atendem a estação (só na edição). */
  linhas?: React.ReactNode;
  salvando: boolean;
  erroApi: string | null;
  aoSalvar: (dados: EstacaoRequest) => void;
  aoDesativar?: () => void;
  desativando?: boolean;
}

/** Nome + posição (campos ou toque no mapa) + ativa. Usado em "Nova estação" e na edição. */
export function FormularioEstacao({
  existente,
  linhas,
  salvando,
  erroApi,
  aoSalvar,
  aoDesativar,
  desativando = false,
}: FormularioEstacaoProps) {
  const [nome, setNome] = useState(existente?.nome ?? '');
  const [latitude, setLatitude] = useState(formatarCoordenada(existente?.latitude));
  const [longitude, setLongitude] = useState(formatarCoordenada(existente?.longitude));
  const [ativa, setAtiva] = useState(existente?.ativa ?? true);
  const [tentou, setTentou] = useState(false);

  const lat = lerCoordenada(latitude, 90);
  const lon = lerCoordenada(longitude, 180);
  const semPosicao = latitude.trim() === '' && longitude.trim() === '';
  const erros = {
    nome: nome.trim() ? null : 'Informe o nome da estação.',
    latitude: semPosicao || lat !== null ? null : 'Latitude entre -90 e 90, ex.: -23.5505.',
    longitude: semPosicao || lon !== null ? null : 'Longitude entre -180 e 180, ex.: -46.6333.',
  };
  const valido = Object.values(erros).every((e) => e === null);

  const escolherNoMapa = (novaLat: number, novaLon: number) => {
    setLatitude(formatarCoordenada(novaLat));
    setLongitude(formatarCoordenada(novaLon));
  };

  const salvar = () => {
    setTentou(true);
    if (!valido) return;
    aoSalvar({
      nome: nome.trim(),
      ...(lat !== null && lon !== null ? { latitude: lat, longitude: lon } : {}),
      ...(existente ? { ativa } : {}),
    });
  };

  return (
    <SafeAreaView edges={['bottom']} className="flex-1 bg-background">
      <KeyboardAvoidingView
        behavior={Platform.OS === 'ios' ? 'padding' : undefined}
        className="flex-1"
      >
        <ScrollView contentContainerClassName="gap-4 p-4 pb-8" keyboardShouldPersistTaps="handled">
          <Card className="gap-4">
            <TextField
              rotulo="Nome"
              value={nome}
              onChangeText={setNome}
              placeholder="Higienópolis-Mackenzie"
              maxLength={150}
              erro={tentou ? erros.nome : null}
            />
            {linhas}
          </Card>

          <Card className="gap-3">
            <View>
              <Text className="text-base font-semibold text-ink">Posição</Text>
              <Text className="text-sm text-ink-muted">Define onde a estação aparece no mapa.</Text>
            </View>
            <SeletorPosicao
              latitude={lat !== null && lon !== null ? lat : null}
              longitude={lat !== null && lon !== null ? lon : null}
              aoEscolher={escolherNoMapa}
            />
            <View className="flex-row gap-3">
              <TextField
                rotulo="Latitude"
                value={latitude}
                onChangeText={setLatitude}
                placeholder="-23.5505"
                keyboardType="numbers-and-punctuation"
                erro={tentou ? erros.latitude : null}
                className="flex-1"
              />
              <TextField
                rotulo="Longitude"
                value={longitude}
                onChangeText={setLongitude}
                placeholder="-46.6333"
                keyboardType="numbers-and-punctuation"
                erro={tentou ? erros.longitude : null}
                className="flex-1"
              />
            </View>
          </Card>

          {existente && (
            <Card className="flex-row items-center gap-3">
              <View className="flex-1">
                <Text className="text-base font-medium text-ink">Estação ativa</Text>
                <Text className="text-sm text-ink-muted">
                  {ativa ? 'Usada no cálculo de rotas.' : 'Fora do cálculo de rotas.'}
                </Text>
              </View>
              <Switch
                value={ativa}
                onValueChange={setAtiva}
                trackColor={{ true: colors.primary.DEFAULT, false: colors.border }}
                accessibilityLabel="Estação ativa"
              />
            </Card>
          )}

          {erroApi && <AlertaErro mensagem={erroApi} />}

          {existente?.ativa && aoDesativar && (
            <Button
              rotulo="Desativar estação"
              variante="danger"
              icone={Ban}
              carregando={desativando}
              onPress={aoDesativar}
            />
          )}
        </ScrollView>
        <View className="border-t border-border bg-surface p-4">
          <Button
            rotulo={existente ? 'Salvar alterações' : 'Criar estação'}
            onPress={salvar}
            carregando={salvando}
          />
        </View>
      </KeyboardAvoidingView>
    </SafeAreaView>
  );
}
