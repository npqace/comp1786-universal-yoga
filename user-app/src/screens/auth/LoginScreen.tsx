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
import { signInWithEmailAndPassword } from 'firebase/auth';
import { auth } from '../../services/firebase';
import { StackNavigationProp } from '@react-navigation/stack';
import { colors, globalStyles, spacing, typography } from '../../styles/globalStyles';
import Logo from '../../../assets/yoga-logo.svg';

type AuthStackParamList = {
  SignUp: undefined;
};

type LoginScreenNavigationProp = StackNavigationProp<AuthStackParamList, 'SignUp'>;

const LoginScreen = ({ navigation }: { navigation: LoginScreenNavigationProp }) => {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);

  const handleLogin = () => {
    if (!email || !password) {
      Alert.alert('Error', 'Please enter both email and password.');
      return;
    }
    setLoading(true);
    signInWithEmailAndPassword(auth, email, password)
      .then(() => {
        console.log('User signed in!');
      })
      .catch(error => {
        Alert.alert('Login Failed', error.message);
      })
      .finally(() => {
        setLoading(false);
      });
  };

  return (
    <SafeAreaView style={globalStyles.safeArea}>
      <KeyboardAvoidingView
        behavior={Platform.OS === 'ios' ? 'padding' : 'height'}
        style={styles.container}
      >
        <View style={styles.content}>
          <Logo width={100} height={100} style={styles.logo} />
          <Text style={styles.title}>Welcome Back!</Text>
          <Text style={styles.subtitle}>Log in to your account</Text>

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
            style={[globalStyles.button, styles.loginButton]}
            onPress={handleLogin}
            disabled={loading}
          >
            <Text style={globalStyles.buttonText}>{loading ? 'Logging in...' : 'Login'}</Text>
          </TouchableOpacity>

          <TouchableOpacity
            style={styles.signUpLink}
            onPress={() => navigation.navigate('SignUp')}
          >
            <Text style={styles.signUpText}>
              Don't have an account? <Text style={styles.signUpLinkText}>Sign Up</Text>
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
  loginButton: {
    marginTop: spacing.md,
  },
  signUpLink: {
    marginTop: spacing.lg,
    alignItems: 'center',
  },
  signUpText: {
    ...typography.body,
    color: colors.textLight,
  },
  signUpLinkText: {
    color: colors.primary,
    fontWeight: 'bold',
  },
});

export default LoginScreen;