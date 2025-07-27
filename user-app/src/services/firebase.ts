import { initializeApp, getApp, getApps } from 'firebase/app';
import { initializeAuth, getReactNativePersistence } from 'firebase/auth';
import { getDatabase } from 'firebase/database';
import ReactNativeAsyncStorage from '@react-native-async-storage/async-storage';

const firebaseConfig = {
  apiKey: "AIzaSyAb21mpled_3NJiQqmN1wgIYwQnWgPCGs8",
  authDomain: "universal-yoga-3e267.firebaseapp.com",
  databaseURL: "https://universal-yoga-3e267-default-rtdb.asia-southeast1.firebasedatabase.app",
  projectId: "universal-yoga-3e267",
  storageBucket: "universal-yoga-3e267.firebasestorage.app",
  messagingSenderId: "284957734317",
  appId: "1:284957734317:web:cdc0a2c091a1a4dd4c1d24"
};

// Initialize Firebase
const app = !getApps().length ? initializeApp(firebaseConfig) : getApp();

const auth = initializeAuth(app, {
  persistence: getReactNativePersistence(ReactNativeAsyncStorage),
});
const database = getDatabase(app);

export { app, auth, database }; 