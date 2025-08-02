import React, { useEffect, useRef } from 'react';
import { Text, StyleSheet, Animated } from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { colors, spacing, typography, borderRadius } from '../styles/globalStyles';
import { useSafeAreaInsets } from 'react-native-safe-area-context';

type ToastType = 'success' | 'error' | 'info';

interface ToastProps {
  message: string;
  type: ToastType;
  visible: boolean;
  onHide: () => void;
}

const Toast = ({ message, type, visible, onHide }: ToastProps) => {
  // Start from off-screen top
  const slideAnim = useRef(new Animated.Value(-100)).current;
  const insets = useSafeAreaInsets();

  useEffect(() => {
    if (visible) {
      // Slide in
      Animated.timing(slideAnim, {
        toValue: 0,
        duration: 300,
        useNativeDriver: true,
      }).start();

      const timer = setTimeout(() => {
        // Slide out
        Animated.timing(slideAnim, {
          toValue: -100, // Animate back to off-screen top
          duration: 300,
          useNativeDriver: true,
        }).start(() => {
          onHide();
        });
      }, 3000);

      return () => clearTimeout(timer);
    }
  }, [visible, slideAnim, onHide]);

  const { backgroundColor, icon } = {
    success: { backgroundColor: colors.success, icon: 'checkmark-circle-outline' as const },
    error: { backgroundColor: colors.error, icon: 'alert-circle-outline' as const },
    info: { backgroundColor: colors.info, icon: 'information-circle-outline' as const },
  }[type];

  return (
    <Animated.View
      style={[
        styles.container,
        { backgroundColor, top: insets.top || spacing.md }, // Position at the top
        { transform: [{ translateY: slideAnim }] },
      ]}
    >
      <Ionicons name={icon} size={24} color="white" />
      <Text style={styles.message}>{message}</Text>
    </Animated.View>
  );
};

const styles = StyleSheet.create({
  container: {
    position: 'absolute',
    left: spacing.md,
    right: spacing.md,
    padding: spacing.md,
    borderRadius: borderRadius.medium,
    flexDirection: 'row',
    alignItems: 'center',
    elevation: 10,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.2,
    shadowRadius: 4,
  },
  message: {
    ...typography.body,
    color: 'white',
    fontWeight: '600',
    marginLeft: spacing.md,
    flex: 1,
  },
});

export default Toast;