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

interface EditProfileModalProps {
  visible: boolean;
  onClose: () => void;
}

const EditProfileModal = ({ visible, onClose }: EditProfileModalProps) => {
  const user = auth.currentUser;
  const yogaService = YogaService.getInstance();

  const [displayName, setDisplayName] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [currentPassword, setCurrentPassword] = useState('');
  const [loading, setLoading] = useState(false);
  const [reauthNeeded, setReauthNeeded] = useState(false);

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

  const handleUpdate = async () => {
    if (!user) return;

    if (password && password !== confirmPassword) {
      Alert.alert('Error', 'Passwords do not match.');
      return;
    }

    setLoading(true);

    try {
      if (displayName !== user.displayName) {
        await updateProfile(user, { displayName });
        await yogaService.updateDenormalizedUserData(user.uid, displayName);
      }

      if (password) {
        setReauthNeeded(true);
        setLoading(false);
        return;
      }

      Alert.alert('Success', 'Your display name has been updated.');
      onClose();
    } catch (error: any) {
      Alert.alert('Error', error.message);
    } finally {
      if (!reauthNeeded) {
        setLoading(false);
      }
    }
  };

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
      const credential = EmailAuthProvider.credential(user.email, currentPassword);
      await reauthenticateWithCredential(user, credential);
      
      await updatePassword(user, password);
      
      Alert.alert('Success', 'Your profile has been updated successfully.', [{ text: 'OK', onPress: onClose }]);

    } catch (error: any) {
      Alert.alert('Error', `An error occurred: ${error.message}`);
    } finally {
      setLoading(false);
    }
  };

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
