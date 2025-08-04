/**
 * @file ProfileScreen.tsx
 * @description Screen for displaying user profile information and actions like editing and logging out.
 */
import React, { useState, useEffect } from 'react';
import { View, Text, StyleSheet, Alert, TouchableOpacity, SafeAreaView, Image } from 'react-native';
import { auth } from '../services/firebase';
import { signOut, onAuthStateChanged } from 'firebase/auth';
import { globalStyles, colors, spacing, typography } from '../styles/globalStyles';
import { Ionicons } from '@expo/vector-icons';
import EditProfileModal from '../components/EditProfileModal';

/**
 * @screen ProfileScreen
 * @description Displays the current user's profile information, including their avatar,
 * display name, and email. It provides options to edit the profile or log out.
 */
const ProfileScreen = () => {
  const [modalVisible, setModalVisible] = useState(false);
  // We need a state to force re-render when user profile is updated via the modal
  const [user, setUser] = useState(auth.currentUser);

  /**
   * @effect
   * @description Subscribes to authentication state changes to keep the user
   * information up-to-date in real-time (e.g., after a profile edit).
   */
  useEffect(() => {
    const unsubscribe = onAuthStateChanged(auth, (user) => {
      setUser(user);
    });
    return unsubscribe;
  }, []);

  /**
   * @method handleLogout
   * @description Signs the user out of their Firebase account.
   */
  const handleLogout = () => {
    signOut(auth).catch(error => {
      Alert.alert('Logout Failed', error.message);
    });
  };

  /**
   * @method handleModalClose
   * @description Closes the edit profile modal.
   */
  const handleModalClose = () => {
    setModalVisible(false);
    // The onAuthStateChanged listener will handle the UI update automatically if the profile was changed.
  };

  return (
    <SafeAreaView style={globalStyles.safeArea}>
      <View style={styles.container}>
        <View style={styles.profileHeader}>
          {/* Avatar is generated using an external service based on user's name or email */}
          <Image
            source={{ uri: `https://ui-avatars.com/api/?name=${user?.displayName || user?.email}&background=random&color=fff&size=128` }}
            style={styles.avatar}
          />
          <Text style={styles.displayName}>{user?.displayName || 'Anonymous User'}</Text>
          {user?.email && <Text style={styles.email}>{user.email}</Text>}
        </View>

        <View style={styles.buttonContainer}>
          {/* Button to open the Edit Profile modal */}
          <TouchableOpacity 
            style={[globalStyles.button, styles.editButton]}
            onPress={() => setModalVisible(true)}
          >
            <Ionicons name="create-outline" size={20} color="white" />
            <Text style={styles.buttonText}>Edit Profile</Text>
          </TouchableOpacity>

          {/* Button to log out */}
          <TouchableOpacity 
            style={[globalStyles.button, styles.logoutButton]}
            onPress={handleLogout}
          >
            <Ionicons name="log-out-outline" size={20} color="white" />
            <Text style={styles.buttonText}>Logout</Text>
          </TouchableOpacity>
        </View>
      </View>
      {/* The EditProfileModal is rendered here but only visible when modalVisible is true */}
      {user && <EditProfileModal visible={modalVisible} onClose={handleModalClose} />}
    </SafeAreaView>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: colors.background,
  },
  profileHeader: {
    alignItems: 'center',
    paddingVertical: spacing.lg,
    paddingHorizontal: spacing.md,
    backgroundColor: colors.surface,
    borderBottomWidth: 1,
    borderBottomColor: colors.border,
    marginBottom: spacing.lg,
  },
  avatar: {
    width: 120,
    height: 120,
    borderRadius: 60,
    marginBottom: spacing.md,
    borderWidth: 4,
    borderColor: colors.primary,
  },
  displayName: {
    ...typography.h2,
    color: colors.textDark,
    marginBottom: spacing.xs,
  },
  email: {
    ...typography.body,
    color: colors.textLight,
  },
  buttonContainer: {
    paddingHorizontal: spacing.md,
  },
  editButton: {
    backgroundColor: colors.primary,
    marginBottom: spacing.md,
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
  },
  logoutButton: {
    backgroundColor: colors.error,
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
  },
  buttonText: {
    ...globalStyles.buttonText,
    marginLeft: spacing.sm,
  }
});

export default ProfileScreen;
