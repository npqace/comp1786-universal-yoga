/**
 * @file AppNavigator.tsx
 * @description The main navigator for the application, handling authentication flow and the main tab/stack navigation.
 */
import React, { useState, useEffect } from 'react';
import { StatusBar, StyleSheet, View, SafeAreaView } from 'react-native';
import { NavigationContainer } from '@react-navigation/native';
import { createBottomTabNavigator } from '@react-navigation/bottom-tabs';
import { createStackNavigator } from '@react-navigation/stack';
import { Ionicons } from '@expo/vector-icons';
import { auth } from '../services/firebase';
import { onAuthStateChanged } from 'firebase/auth';
import { User } from '../types';
import { OfflineBanner } from '../components';
import CustomHeader from '../components/CustomHeader';
import { colors } from '../styles/globalStyles';

// Import screens
import ClassListScreen from '../screens/ClassListScreen';
import ClassDetailScreen from '../screens/ClassDetailScreen';
import LoginScreen from '../screens/auth/LoginScreen';
import SignUpScreen from '../screens/auth/SignUpScreen';
import ProfileScreen from '../screens/ProfileScreen';
import MyBookingsScreen from '../screens/MyBookingsScreen';
import DetailHeader from '../components/DetailHeader';

// Define Param Lists for type safety with React Navigation
type AuthStackParamList = {
  Login: undefined;
  SignUp: undefined;
};

type TabParamList = {
  Classes: undefined;
  MyBookings: undefined;
  Profile: undefined;
};

type RootStackParamList = {
  Home: undefined;
  ClassDetail: { classId: string; courseFirebaseKey?: string };
};

// Create navigators
const Tab = createBottomTabNavigator<TabParamList>();
const Stack = createStackNavigator<RootStackParamList>();
const AuthStack = createStackNavigator<AuthStackParamList>();

/**
 * @navigator AuthNavigator
 * @description Navigator for the authentication flow (Login, Sign Up).
 * This is shown when the user is not logged in.
 */
function AuthNavigator() {
  return (
    <AuthStack.Navigator id={undefined}>
      <AuthStack.Screen name="Login" component={LoginScreen} options={{ headerShown: false }} />
      <AuthStack.Screen name="SignUp" component={SignUpScreen} options={{ headerShown: false }} />
    </AuthStack.Navigator>
  );
}

/**
 * @navigator TabNavigator
 * @description The main bottom tab navigator for the authenticated user.
 * It includes screens for Classes, My Bookings, and Profile.
 */
function TabNavigator() {
  return (
    <Tab.Navigator
      id={undefined}
      screenOptions={({ route }) => ({
        // Configure tab bar icons
        tabBarIcon: ({ focused, color, size }) => {
          let iconName: keyof typeof Ionicons.glyphMap = 'help-outline';

          if (route.name === 'Classes') {
            iconName = focused ? 'list' : 'list-outline';
          } else if (route.name === 'MyBookings') {
            iconName = focused ? 'calendar' : 'calendar-outline';
          } else if (route.name === 'Profile') {
            iconName = focused ? 'person' : 'person-outline';
          }

          return <Ionicons name={iconName} size={size} color={color} />;
        },
        tabBarActiveTintColor: colors.primary,
        tabBarInactiveTintColor: 'gray',
        // Use a custom header for all tab screens
        header: ({ options }) => <CustomHeader title={options.title} />,
      })}
    >
      <Tab.Screen name="Classes" component={ClassListScreen} options={{ title: 'Yoga Classes' }} />
      <Tab.Screen name="MyBookings" component={MyBookingsScreen} options={{ title: 'My Bookings' }} />
      <Tab.Screen name="Profile" component={ProfileScreen} options={{ title: 'Profile' }} />
    </Tab.Navigator>
  );
}

/**
 * @navigator MainStackNavigator
 * @description The root stack navigator for the authenticated part of the app.
 * It contains the TabNavigator and any other screens that should be presented
 * on top of the tabs (e.g., ClassDetailScreen).
 */
function MainStackNavigator() {
  return (
    <Stack.Navigator id={undefined}>
      <Stack.Screen name="Home" component={TabNavigator} options={{ headerShown: false }} />
      <Stack.Screen
        name="ClassDetail"
        component={ClassDetailScreen}
        options={() => ({
          // Use a custom header for the detail screen
          header: () => <DetailHeader title="Class Details" />,
        })}
      />
    </Stack.Navigator>
  );
}

/**
 * @component AppNavigator
 * @description The main entry point for the app's navigation. It listens to the
 * authentication state and renders either the AuthNavigator or the MainStackNavigator.
 */
export default function AppNavigator() {
  const [user, setUser] = useState<User | null>(null);

  /**
   * @effect
   * @description Subscribes to Firebase authentication state changes to determine
   * which navigator to show.
   */
  useEffect(() => {
    const unsubscribe = onAuthStateChanged(auth, (userState) => {
      if (userState) {
        // User is signed in
        setUser({
          uid: userState.uid,
          email: userState.email,
          displayName: userState.displayName,
        });
      } else {
        // User is signed out
        setUser(null);
      }
    });
    // Unsubscribe from the listener on cleanup
    return unsubscribe;
  }, []);

  return (
    <SafeAreaView style={styles.container}>
      <StatusBar backgroundColor="transparent" barStyle="light-content" />
      <OfflineBanner />
      <NavigationContainer>
        {/* Conditionally render the navigator based on user auth state */}
        {user ? <MainStackNavigator /> : <AuthNavigator />}
      </NavigationContainer>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    paddingTop: 10,
  },
  content: {
    flex: 1,
    backgroundColor: colors.background,
  },
});