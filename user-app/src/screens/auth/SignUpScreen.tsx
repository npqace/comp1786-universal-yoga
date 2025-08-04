/**
 * @file SignUpScreen.tsx
 * @description Screen for new user registration.
 */
import React, { useState } from 'react';
import {
  View,
  Text,
  TextInput,
  StyleSheet,
  Alert,
  TouchableOpacity,
  Image,
  KeyboardAvoidingView,
  Platform,
  SafeAreaView,
} from 'react-native';
import { signUp } from '../../services/firebase';
import { StackNavigationProp } from '@react-navigation/stack';
import { colors, globalStyles, spacing, typography } from '../../styles/globalStyles';
import Logo from '../../../assets/yoga-logo.svg';

// Type definitions for navigation
type AuthStackParamList = {
  Login: undefined;
};

type SignUpScreenNavigationProp = StackNavigationProp<AuthStackParamList, 'Login'>;

/**
 * @screen SignUpScreen
 * @description Allows a new user to create an account with their name, email, and password.
 * @param {{ navigation: SignUpScreenNavigationProp }} props - Navigation props.
 */
const SignUpScreen = ({ navigation }: { navigation: SignUpScreenNavigationProp }) => {
  const [name, setName] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);

  /**
   * @method handleSignUp
   * @description Handles the user sign-up process using the custom `signUp` service function.
   */
  const handleSignUp = async () => {
    // Basic validation
    if (!name || !email || !password) {
      Alert.alert('Error', 'Please fill in all fields.');
      return;
    }
    setLoading(true);
    try {
      await signUp(name, email, password);
      // On success, the onAuthStateChanged listener in AppNavigator will handle navigation
      console.log('User account created & signed in!');
    } catch (error: any) {
      // Display an error message on failure
      Alert.alert('Sign Up Failed', error.message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <SafeAreaView style={globalStyles.safeArea}>
      <KeyboardAvoidingView
        behavior={Platform.OS === 'ios' ? 'padding' : 'height'}
        style={styles.container}
      >
        <View style={styles.content}>
          <Logo width={100} height={100} style={styles.logo} />
          <Text style={styles.title}>Create an Account</Text>
          <Text style={styles.subtitle}>Start your yoga journey with us</Text>

          {/* Name Input */}
          <TextInput
            style={[globalStyles.input, styles.input]}
            placeholder="Name"
            value={name}
            onChangeText={setName}
            autoCapitalize="words"
            placeholderTextColor={colors.textLight}
          />
          {/* Email Input */}
          <TextInput
            style={[globalStyles.input, styles.input]}
            placeholder="Email"
            value={email}
            onChangeText={setEmail}
            keyboardType="email-address"
            autoCapitalize="none"
            placeholderTextColor={colors.textLight}
          />
          {/* Password Input */}
          <TextInput
            style={[globalStyles.input, styles.input]}
            placeholder="Password"
            value={password}
            onChangeText={setPassword}
            secureTextEntry
            placeholderTextColor={colors.textLight}
          />

          {/* Sign Up Button */}
          <TouchableOpacity
            style={[globalStyles.button, styles.signUpButton]}
            onPress={handleSignUp}
            disabled={loading}
          >
            <Text style={globalStyles.buttonText}>{loading ? 'Creating Account...' : 'Sign Up'}</Text>
          </TouchableOpacity>

          {/* Link to Login Screen */}
          <TouchableOpacity
            style={styles.loginLink}
            onPress={() => navigation.navigate('Login')}
          >
            <Text style={styles.loginText}>
              Already have an account? <Text style={styles.loginLinkText}>Log In</Text>
            </Text>
          </TouchableOpacity>
        </View>
      </KeyboardAvoidingView>
    </SafeAreaView>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    justifyContent: 'center',
  },
  content: {
    paddingHorizontal: spacing.lg,
  },
  logo: {
    width: 100,
    height: 100,
    alignSelf: 'center',
    marginBottom: spacing.xl,
  },
  title: {
    ...typography.h2,
    color: colors.textDark,
    textAlign: 'center',
    marginBottom: spacing.sm,
  },
  subtitle: {
    ...typography.body,
    color: colors.textLight,
    textAlign: 'center',
    marginBottom: spacing.xl,
  },
  input: {
    paddingVertical: spacing.md,
    marginBottom: spacing.md,
  },
  signUpButton: {
    marginTop: spacing.md,
  },
  loginLink: {
    marginTop: spacing.lg,
    alignItems: 'center',
  },
  loginText: {
    ...typography.body,
    color: colors.textLight,
  },
  loginLinkText: {
    color: colors.primary,
    fontWeight: 'bold',
  },
});

export default SignUpScreen;
