import { Tabs } from 'expo-router/js-tabs';
import { Activity, Bookmark, Map, TrainFront } from 'lucide-react-native';

import { colors } from '@/theme/colors';

// O Mapa é a primeira tela, embora apareça na 3ª posição da barra (como no protótipo).
export const unstable_settings = { initialRouteName: 'index' };

export default function TabsLayout() {
  return (
    <Tabs
      screenOptions={{
        headerShown: false,
        tabBarActiveTintColor: colors.primary.DEFAULT,
        tabBarInactiveTintColor: colors.ink.subtle,
        tabBarStyle: { backgroundColor: colors.surface, borderTopColor: colors.border },
        tabBarLabelStyle: { fontSize: 12, fontWeight: '500' },
      }}
    >
      <Tabs.Screen
        name="status"
        options={{
          title: 'Status',
          tabBarIcon: ({ color, size }) => <Activity color={color} size={size} />,
        }}
      />
      <Tabs.Screen
        name="linhas"
        options={{
          title: 'Linhas',
          tabBarIcon: ({ color, size }) => <TrainFront color={color} size={size} />,
        }}
      />
      <Tabs.Screen
        name="index"
        options={{
          title: 'Mapa',
          tabBarIcon: ({ color, size }) => <Map color={color} size={size} />,
        }}
      />
      <Tabs.Screen
        name="rotas"
        options={{
          title: 'Rotas',
          tabBarIcon: ({ color, size }) => <Bookmark color={color} size={size} />,
        }}
      />
    </Tabs>
  );
}
