/**
 * @file Toast.tsx
 * @description A component to display a temporary, non-intrusive notification message (toast).
 */
import React, { useEffect, useRef } from 'react';
import { Text, StyleSheet, Animated } from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { colors, spacing, typography, borderRadius } from '../styles/globalStyles';
import { useSafeAreaInsets } from 'react-native-safe-area-context';

/**
 * @type ToastType
 * @description Defines the style of the toast message.
 */
type ToastType = 'success' | 'error' | 'info';

/**
 * @interface ToastProps
 * @description Props for the Toast component.
 * @property {string} message - The message to display in the toast.
 * @property {ToastType} type - The type of toast, which determines its color and icon.
 * @property {boolean} visible - Whether the toast is currently visible.
 * @property {() => void} onHide - Callback function to call when the toast finishes hiding.
 */
interface ToastProps {
  message: string;
  type: ToastType;
  visible: boolean;
  onHide: () => void;
}

/**
 * @component Toast
 * @description A toast notification component that slides in from the top, displays a message, and then slides out.
 * @param {ToastProps} props - The props for the component.
 */
const Toast = ({ message, type, visible, onHide }: ToastProps) => {
  // Animated value for the slide-in/out animation, starting off-screen
  const slideAnim = useRef(new Animated.Value(-100)).current;
  const insets = useSafeAreaInsets();

  /**
   * @effect
   * @description Manages the animation and visibility of the toast.
   */
  useEffect(() => {
    if (visible) {
      // Animate the toast sliding into view
      Animated.timing(slideAnim, {
        toValue: 0,
        duration: 300,
        useNativeDriver: true,
      }).start();

      // Set a timer to automatically hide the toast after 3 seconds
      const timer = setTimeout(() => {
        // Animate the toast sliding out of view
        Animated.timing(slideAnim, {
          toValue: -100, // Animate back to off-screen top
          duration: 300,
          useNativeDriver: true,
        }).start(() => {
          onHide(); // Notify the parent component that the toast is hidden
        });
      }, 3000);

      // Cleanup the timer if the component unmounts or visibility changes
      return () => clearTimeout(timer);
    }
  }, [visible, slideAnim, onHide]);

  // Determine the background color and icon based on the toast type
  const { backgroundColor, icon } = {
    success: { backgroundColor: colors.success, icon: 'checkmark-circle-outline' as const },
    error: { backgroundColor: colors.error, icon: 'alert-circle-outline' as const },
    info: { backgroundColor: colors.info, icon: 'information-circle-outline' as const },
  }[type];

  return (
    <Animated.View
      style={[
        styles.container,
        { backgroundColor, top: insets.top || spacing.md }, // Position below the status bar
        { transform: [{ translateY: slideAnim }] }, // Apply the slide animation
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
