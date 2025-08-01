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

type AuthStackParamList = {
  Login: undefined;
};

type SignUpScreenNavigationProp = StackNavigationProp<AuthStackParamList, 'Login'>;

const SignUpScreen = ({ navigation }: { navigation: SignUpScreenNavigationProp }) => {
  const [name, setName] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);

  const handleSignUp = async () => {
    if (!name || !email || !password) {
      Alert.alert('Error', 'Please fill in all fields.');
      return;
    }
    setLoading(true);
    try {
      await signUp(name, email, password);
      console.log('User account created & signed in!');
    } catch (error: any) {
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

          <TextInput
            style={[globalStyles.input, styles.input]}
            placeholder="Name"
            value={name}
            onChangeText={setName}
            autoCapitalize="words"
            placeholderTextColor={colors.textLight}
          />
          <TextInput
            style={[globalStyles.input, styles.input]}
            placeholder="Email"
            value={email}
            onChangeText={setEmail}
            keyboardType="email-address"
            autoCapitalize="none"
            placeholderTextColor={colors.textLight}
          />
          <TextInput
            style={[globalStyles.input, styles.input]}
            placeholder="Password"
            value={password}
            onChangeText={setPassword}
            secureTextEntry
            placeholderTextColor={colors.textLight}
          />

          <TouchableOpacity
            style={[globalStyles.button, styles.signUpButton]}
            onPress={handleSignUp}
            disabled={loading}
          >
            <Text style={globalStyles.buttonText}>{loading ? 'Creating Account...' : 'Sign Up'}</Text>
          </TouchableOpacity>

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