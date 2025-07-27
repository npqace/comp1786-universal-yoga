import React from 'react';
import { View, Text, Button, StyleSheet, Alert } from 'react-native';
import { auth } from '../services/firebase';
import { signOut } from 'firebase/auth';
import { globalStyles, colors } from '../styles/globalStyles';

const ProfileScreen = () => {
  const handleLogout = () => {
    signOut(auth)
      .then(() => console.log('User signed out!'))
      .catch(error => {
        Alert.alert('Logout Failed', error.message);
      });
  };

  const user = auth.currentUser;

  return (
    <View style={[globalStyles.container, styles.container]}>
      <Text style={styles.title}>Profile</Text>
      {user && <Text style={styles.email}>{user.email}</Text>}
      <Button title="Logout" onPress={handleLogout} color={colors.error} />
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    justifyContent: 'center',
    alignItems: 'center',
    padding: 16,
  },
  title: {
    fontSize: 24,
    marginBottom: 16,
  },
  email: {
    fontSize: 18,
    marginBottom: 24,
  },
});

export default ProfileScreen;
