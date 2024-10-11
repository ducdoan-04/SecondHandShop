/**
 * Import function triggers from their respective submodules:
 *
 * const {onCall} = require("firebase-functions/v2/https");
 * const {onDocumentWritten} = require("firebase-functions/v2/firestore");
 *
 * See a full list of supported triggers at https://firebase.google.com/docs/functions
 */

// Xóa các biến không sử dụng
// const { onRequest } = require("firebase-functions/v2/https");
// const logger = require("firebase-functions/logger");

const functions = require("firebase-functions");
const admin = require("firebase-admin");
admin.initializeApp();

// Function to get user UID by email
exports.getUserUID = functions.https.onCall(async (data, context) => {
  const email = data.email;
  try {
    const userRecord = await admin.auth().getUserByEmail(email);
    return {uid: userRecord.uid};
  } catch (error) {
    throw new functions.https.HttpsError("not-found", "User not found");
  }
});

// Create and deploy your first functions
// https://firebase.google.com/docs/functions/get-started

// Chia nhỏ dòng để không vượt quá 80 ký tự
// exports.helloWorld = onRequest((request, response) => {
//   logger.info("Hello logs!", { structuredData: true });
//   response.send("Hello from Firebase!");
// });
