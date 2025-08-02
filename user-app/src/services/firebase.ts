import { initializeApp, getApp, getApps, FirebaseApp } from 'firebase/app';
import { 
  getAuth,
  initializeAuth, 
  getReactNativePersistence, 
  createUserWithEmailAndPassword,
  updateProfile,
  Auth
} from 'firebase/auth';
import { getDatabase, ref, set, Database } from 'firebase/database';
import ReactNativeAsyncStorage from '@react-native-async-storage/async-storage';
import { User } from '../types';

const firebaseConfig = {
  apiKey: "AIzaSyAb21mpled_3NJiQqmN1wgIYwQnWgPCGs8",
  authDomain: "universal-yoga-3e267.firebaseapp.com",
  databaseURL: "https://universal-yoga-3e267-default-rtdb.asia-southeast1.firebasedatabase.app",
  projectId: "universal-yoga-3e267",
  storageBucket: "universal-yoga-3e267.firebasestorage.app",
  messagingSenderId: "284957734317",
  appId: "1:284957734317:web:cdc0a2c091a1a4dd4c1d24"
};

let app: FirebaseApp;
let auth: Auth;
let database: Database;

if (!getApps().length) {
  try {
    app = initializeApp(firebaseConfig);
    auth = initializeAuth(app, {
      persistence: getReactNativePersistence(ReactNativeAsyncStorage),
    });
  } catch (error) {
    // This can happen in certain edge cases with hot-reloading
    console.error("Firebase initialization error", error);
    app = getApp();
    auth = getAuth(app);
  }
} else {
  app = getApp();
  auth = getAuth(app);
}

database = getDatabase(app);


// New Sign-Up Function
export const signUp = async (name: string, email: string, password: string) => {
  const userCredential = await createUserWithEmailAndPassword(auth, email, password);
  const user = userCredential.user;

  // Update the user's profile with their name
  await updateProfile(user, { displayName: name });

  // Also, save the user's public data to the Realtime Database
  const userRef = ref(database, `users/${user.uid}`);
  const userData: User = {
    uid: user.uid,
    email: user.email,
    displayName: name,
  };
  await set(userRef, userData);

  return userCredential;
};


export { app, auth, database };