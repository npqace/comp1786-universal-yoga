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
import { colors } from '../styles/globalStyles';

// Import screens
import ClassListScreen from '../screens/ClassListScreen';
import SearchScreen from '../screens/SearchScreen';
import ClassDetailScreen from '../screens/ClassDetailScreen';
import LoginScreen from '../screens/auth/LoginScreen';
import SignUpScreen from '../screens/auth/SignUpScreen';
import ProfileScreen from '../screens/ProfileScreen';
import MyBookingsScreen from '../screens/MyBookingsScreen';

// Define Param Lists
type AuthStackParamList = {
  Login: undefined;
  SignUp: undefined;
};

type TabParamList = {
  Classes: undefined;
  Search: undefined;
  MyBookings: undefined;
  Profile: undefined;
};

type RootStackParamList = {
  Home: undefined;
  ClassDetail: { classId: string; courseFirebaseKey?: string };
};

const Tab = createBottomTabNavigator<TabParamList>();
const Stack = createStackNavigator<RootStackParamList>();
const AuthStack = createStackNavigator<AuthStackParamList>();

// Auth Navigator
function AuthNavigator() {
  return (
    <AuthStack.Navigator id={undefined}>
      <AuthStack.Screen name="Login" component={LoginScreen} options={{ headerShown: false }} />
      <AuthStack.Screen name="SignUp" component={SignUpScreen} options={{ headerShown: false }} />
    </AuthStack.Navigator>
  );
}

// Tab Navigator
function TabNavigator() {
  return (
    <Tab.Navigator
      id={undefined}
      screenOptions={({ route }) => ({
        tabBarIcon: ({ focused, color, size }) => {
          let iconName: keyof typeof Ionicons.glyphMap;

          if (route.name === 'Classes') {
            iconName = focused ? 'list' : 'list-outline';
          } else if (route.name === 'Search') {
            iconName = focused ? 'search' : 'search-outline';
          } else if (route.name === 'MyBookings') {
            iconName = focused ? 'calendar' : 'calendar-outline';
          } else if (route.name === 'Profile') {
            iconName = focused ? 'person' : 'person-outline';
          } else {
            iconName = 'help-outline';
          }

          return <Ionicons name={iconName} size={size} color={color} />;
        },
        tabBarActiveTintColor: '#2196F3',
        tabBarInactiveTintColor: 'gray',
        headerStyle: {
          backgroundColor: '#2196F3',
        },
        headerTintColor: '#ffffff',
        headerTitleStyle: {
          fontWeight: 'bold',
        },
        headerStatusBarHeight: 0,
      })}
    >
      <Tab.Screen 
        name="Classes" 
        component={ClassListScreen}
        options={{
          title: 'Yoga Classes',
          headerTitle: 'Available Classes'
        }}
      />
      <Tab.Screen 
        name="Search" 
        component={SearchScreen}
        options={{
          title: 'Search',
          headerTitle: 'Search Classes'
        }}
      />
      <Tab.Screen
        name="MyBookings"
        component={MyBookingsScreen}
        options={{
          title: 'My Bookings',
          headerTitle: 'My Bookings'
        }}
      />
      <Tab.Screen
        name="Profile"
        component={ProfileScreen}
        options={{
          title: 'Profile',
          headerTitle: 'My Profile'
        }}
      />
    </Tab.Navigator>
  );
}

// Main App Navigator
function MainStackNavigator() {
  return (
    <Stack.Navigator id={undefined}>
      <Stack.Screen 
        name="Home" 
        component={TabNavigator}
        options={{ headerShown: false }}
      />
      <Stack.Screen 
        name="ClassDetail" 
        component={ClassDetailScreen}
        options={{
          title: 'Class Details',
          headerStatusBarHeight: 0,
          headerStyle: {
            backgroundColor: '#2196F3',
          },
          headerTintColor: '#ffffff',
          headerTitleStyle: {
            fontWeight: 'bold',
          },
        }}
      />
    </Stack.Navigator>
  );
}

export default function AppNavigator() {
  const [user, setUser] = useState<User | null>(null);

  useEffect(() => {
    const unsubscribe = onAuthStateChanged(auth, userState => {
      if (userState) {
        setUser({
          uid: userState.uid,
          email: userState.email,
          displayName: userState.displayName,
        });
      } else {
        setUser(null);
      }
    });
    return unsubscribe; // unsubscribe on unmount
  }, []);

  return (
    <SafeAreaView style={styles.container}>
      <View style={styles.content}>
        <StatusBar backgroundColor="transparent" barStyle="light-content" translucent={true} />
        <OfflineBanner />
        <NavigationContainer>
          {user ? <MainStackNavigator /> : <AuthNavigator />}
        </NavigationContainer>
      </View>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: "black",
    paddingTop: StatusBar.currentHeight || 0,
  },
  content: {
    flex: 1,
    backgroundColor: colors.primary,
  },
}); 