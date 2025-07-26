import { initializeApp } from 'firebase/app';
import { getDatabase, Database } from 'firebase/database';

// TODO: Replace with your web app's Firebase configuration
// Go to Firebase Console → Project Settings → Add Web App
// Copy the config object from there
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
const app = initializeApp(firebaseConfig);

// Initialize Realtime Database and get a reference to the service
export const database: Database = getDatabase(app);

export default app; 