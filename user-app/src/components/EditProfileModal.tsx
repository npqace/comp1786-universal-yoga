/**
 * @file EditProfileModal.tsx
 * @description A modal component for editing user profile information.
 */
import React, { useState, useEffect } from 'react';
import { View, Text, TextInput, TouchableOpacity, StyleSheet, Alert, Modal, KeyboardAvoidingView, Platform } from 'react-native';
import { auth } from '../services/firebase';
import { 
  updateProfile, 
  updatePassword, 
  reauthenticateWithCredential, 
  EmailAuthProvider 
} from 'firebase/auth';
import { YogaService } from '../services/yogaService';
import { globalStyles, colors, spacing, typography, borderRadius } from '../styles/globalStyles';
import { Ionicons } from '@expo/vector-icons';
import { useToast } from '../context/ToastContext';

/**
 * @interface EditProfileModalProps
 * @description Props for the EditProfileModal component.
 * @property {boolean} visible - Whether the modal is visible.
 * @property {() => void} onClose - Function to call when the modal is closed.
 */
interface EditProfileModalProps {
  visible: boolean;
  onClose: () => void;
}

/**
 * @component EditProfileModal
 * @description A modal that allows users to update their display name and password.
 * It handles re-authentication for sensitive operations like changing a password.
 * @param {EditProfileModalProps} props - The props for the component.
 */
const EditProfileModal = ({ visible, onClose }: EditProfileModalProps) => {
  const user = auth.currentUser;
  const yogaService = YogaService.getInstance();
  const { showToast } = useToast();

  const [displayName, setDisplayName] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [currentPassword, setCurrentPassword] = useState('');
  const [loading, setLoading] = useState(false);
  const [reauthNeeded, setReauthNeeded] = useState(false);

  /**
   * @effect
   * @description Resets the form state whenever the modal becomes visible.
   */
  useEffect(() => {
    if (visible && user) {
      setDisplayName(user.displayName || '');
      setPassword('');
      setConfirmPassword('');
      setCurrentPassword('');
      setReauthNeeded(false);
      setLoading(false);
    }
  }, [visible, user]);

  /**
   * @method handleUpdate
   * @description Handles the initial profile update request.
   * Updates the display name immediately and triggers the re-authentication flow if a new password is entered.
   */
  const handleUpdate = async () => {
    if (!user) return;

    if (password && password !== confirmPassword) {
      Alert.alert('Error', 'Passwords do not match.');
      return;
    }

    setLoading(true);

    try {
      // Update display name if it has changed
      if (displayName !== user.displayName) {
        await updateProfile(user, { displayName });
        await yogaService.updateDenormalizedUserData(user.uid, displayName);
      }

      // If a new password is set, require re-authentication
      if (password) {
        setReauthNeeded(true);
        setLoading(false);
        return;
      }

      showToast('Your display name has been updated.', 'success');
      onClose();
    } catch (error: any) {
      Alert.alert('Error', error.message);
    } finally {
      if (!reauthNeeded) {
        setLoading(false);
      }
    }
  };

  /**
   * @method handleReauthenticate
   * @description Handles the re-authentication process and updates the password.
   */
  const handleReauthenticate = async () => {
    if (!user || !user.email) {
        Alert.alert('Error', 'No user is signed in.');
        return;
    }

    if (!currentPassword) {
      Alert.alert('Error', 'Please enter your current password.');
      return;
    }

    setLoading(true);
    try {
      // Re-authenticate the user with their current password
      const credential = EmailAuthProvider.credential(user.email, currentPassword);
      await reauthenticateWithCredential(user, credential);
      
      // If re-authentication is successful, update the password
      await updatePassword(user, password);
      
      showToast('Your profile has been updated successfully.', 'success');
      onClose();

    } catch (error: any) {
      Alert.alert('Error', `An error occurred: ${error.message}`);
    } finally {
      setLoading(false);
    }
  };

  /**
   * @method renderReAuthForm
   * @description Renders the form for re-authenticating the user.
   * @returns {React.ReactElement} The re-authentication form.
   */
  const renderReAuthForm = () => (
    <>
      <Text style={styles.label}>For security, please enter your current password to change it.</Text>
      <TextInput
        style={globalStyles.input}
        value={currentPassword}
        onChangeText={setCurrentPassword}
        placeholder="Current Password"
        secureTextEntry
      />
      <TouchableOpacity
        style={[globalStyles.button, styles.updateButton]}
        onPress={handleReauthenticate}
        disabled={loading}
      >
        <Text style={globalStyles.buttonText}>
          {loading ? 'Verifying...' : 'Confirm and Update'}
        </Text>
      </TouchableOpacity>
    </>
  );

  /**
   * @method renderEditForm
   * @description Renders the main form for editing profile details.
   * @returns {React.ReactElement} The edit profile form.
   */
  const renderEditForm = () => (
    <>
      <Text style={styles.label}>Display Name</Text>
      <TextInput
        style={globalStyles.input}
        value={displayName}
        onChangeText={setDisplayName}
        placeholder="Enter your display name"
      />
      <Text style={styles.label}>Email</Text>
      <TextInput
        style={[globalStyles.input, styles.disabledInput]}
        value={user?.email || ''}
        editable={false}
      />
      <Text style={styles.label}>New Password</Text>
      <TextInput
        style={globalStyles.input}
        value={password}
        onChangeText={setPassword}
        placeholder="Leave blank to keep current"
        secureTextEntry
      />
      <Text style={styles.label}>Confirm New Password</Text>
      <TextInput
        style={globalStyles.input}
        value={confirmPassword}
        onChangeText={setConfirmPassword}
        placeholder="Confirm your new password"
        secureTextEntry
      />
      <TouchableOpacity
        style={[globalStyles.button, styles.updateButton]}
        onPress={handleUpdate}
        disabled={loading}
      >
        <Text style={globalStyles.buttonText}>
          {loading ? 'Updating...' : 'Update Profile'}
        </Text>
      </TouchableOpacity>
    </>
  );

  return (
    <Modal
      transparent={true}
      visible={visible}
      animationType="fade"
      onRequestClose={onClose}
    >
      <KeyboardAvoidingView 
        behavior={Platform.OS === "ios" ? "padding" : "height"}
        style={styles.modalBackdrop}
      >
        <View style={styles.modalContainer}>
          <View style={styles.header}>
            <Text style={styles.headerTitle}>Edit Profile</Text>
            <TouchableOpacity onPress={onClose}>
              <Ionicons name="close-circle-outline" size={28} color={colors.textLight} />
            </TouchableOpacity>
          </View>
          <View style={styles.form}>
            {/* Conditionally render the re-auth form or the edit form */}
            {reauthNeeded ? renderReAuthForm() : renderEditForm()}
          </View>
        </View>
      </KeyboardAvoidingView>
    </Modal>
  );
};

const styles = StyleSheet.create({
  modalBackdrop: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    backgroundColor: 'rgba(0, 0, 0, 0.5)',
  },
  modalContainer: {
    width: '90%',
    backgroundColor: colors.surface,
    borderRadius: borderRadius.large,
    padding: spacing.md,
    elevation: 10,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.25,
    shadowRadius: 4,
  },
  header: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    borderBottomWidth: 1,
    borderBottomColor: colors.border,
    paddingBottom: spacing.sm,
    marginBottom: spacing.sm,
  },
  headerTitle: {
    ...typography.h3,
    color: colors.textDark,
  },
  form: {
    paddingVertical: spacing.sm,
  },
  label: {
    ...typography.body,
    color: colors.text,
    marginBottom: spacing.sm,
    marginTop: spacing.md,
    fontWeight: '600',
  },
  updateButton: {
    marginTop: spacing.lg,
  },
  disabledInput: {
    backgroundColor: colors.disabled,
    color: colors.textLight,
  }
});

export default EditProfileModal;