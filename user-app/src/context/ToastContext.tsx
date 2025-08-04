/**
 * @file ToastContext.tsx
 * @description Provides a context for showing toast notifications throughout the application.
 */
import React, { createContext, useContext, useState, ReactNode } from 'react';
import Toast from '../components/Toast';

/**
 * @type ToastType
 * @description Defines the style of the toast message.
 */
type ToastType = 'success' | 'error' | 'info';

/**
 * @interface ToastContextData
 * @description Defines the shape of the Toast context.
 */
interface ToastContextData {
  showToast: (message: string, type?: ToastType) => void;
}

// Create the context with an undefined default value.
const ToastContext = createContext<ToastContextData | undefined>(undefined);

/**
 * @hook useToast
 * @description A custom hook to easily access the toast context.
 * @throws {Error} If used outside of a ToastProvider.
 * @returns {ToastContextData} The toast context data.
 */
export const useToast = () => {
  const context = useContext(ToastContext);
  if (!context) {
    throw new Error('useToast must be used within a ToastProvider');
  }
  return context;
};

/**
 * @interface ToastProviderProps
 * @description Props for the ToastProvider component.
 */
interface ToastProviderProps {
  children: ReactNode;
}

/**
 * @component ToastProvider
 * @description A provider component that wraps the application and provides a global toast notification system.
 * @param {ToastProviderProps} props - The props for the component.
 */
export const ToastProvider = ({ children }: ToastProviderProps) => {
  const [toast, setToast] = useState<{ message: string; type: ToastType; visible: boolean } | null>(null);

  /**
   * @method showToast
   * @description Displays a toast message.
   * @param {string} message - The message to show.
   * @param {ToastType} [type='info'] - The type of the toast.
   */
  const showToast = (message: string, type: ToastType = 'info') => {
    setToast({ message, type, visible: true });
  };

  /**
   * @method hideToast
   * @description Hides the currently visible toast.
   */
  const hideToast = () => {
    if (toast) {
      setToast({ ...toast, visible: false });
    }
  };

  return (
    <ToastContext.Provider value={{ showToast }}>
      {children}
      {/* Render the Toast component if there is a toast to display */}
      {toast && (
        <Toast
          message={toast.message}
          type={toast.type}
          visible={toast.visible}
          onHide={hideToast}
        />
      )}
    </ToastContext.Provider>
  );
};