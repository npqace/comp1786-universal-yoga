import React from 'react';
import { StatusBar } from 'expo-status-bar';
import { SafeAreaProvider } from 'react-native-safe-area-context';
import AppNavigator from './src/navigation/AppNavigator';
import { ToastProvider } from './src/context/ToastContext';

export default function App() {
  return (
    <SafeAreaProvider>
      <ToastProvider>
        <AppNavigator />
      </ToastProvider>
      <StatusBar style="light" backgroundColor="#6366f1" />
    </SafeAreaProvider>
  );
}