const functions = require("firebase-functions");
const admin = require("firebase-admin");

admin.initializeApp();

const RESET_KEY = "6LYY6i6S0vUXNelGzy6wJoXN0xoNKhxu";

exports.resetDatabase = functions.https.onRequest(async (req, res) => {
    if (req.query.key !== RESET_KEY) {
        return res.status(401).send("Unauthorized");
    }

    try {
        await admin.database().ref().set(null);
        functions.logger.info("Realtime Database has been successfully reset.");
        return res.status(200).send("Realtime Database has been reset.");
    } catch (error) {
        functions.logger.error("Error resetting database:", error);
        return res.status(500).send("An error occurred while resetting the database.");
    }
});

// // Create and Deploy Your First Cloud Functions
// // https://firebase.google.com/docs/functions/write-firebase-functions
//
// exports.helloWorld = functions.https.onRequest((request, response) => {
//   functions.logger.info("Hello logs!", {structuredData: true});
//   response.send("Hello from Firebase!");
// });