# Project Summary & Context

This document summarizes the work done on the Universal Yoga application to provide context for future development sessions.

## Initial Analysis (Summary)

The project was analyzed and several weaknesses were identified:
- **Insufficient User Data:** The `User` model only contained `uid`, `email`, and `displayName`, lacking a proper `name` field.
- **Uninformative Bookings:** The `Booking` model was lean, only storing IDs (`userId`, `classId`), which required multiple inefficient queries to get meaningful data.
- **Data Model Inconsistencies:** The `user-app` and `admin-app` had different and inconsistent data models, especially regarding IDs (`id` vs. `firebaseKey`).
- **Inefficient Data Fetching:** The `user-app` was fetching all classes to display a user's bookings.
- **Admin App Complexity:** The `admin-app` used a local Room database, adding complexity and potential synchronization issues with Firebase.

## Phased Development Plan

A four-phase plan was established to address these issues:

- **Phase 1: Foundational Data Model Improvements:** Enhance the `User` and `Booking` models at the source.
- **Phase 2: `user-app` Refactoring:** Align the `user-app` with the new data models.
- **Phase 3: `admin-app` Refactoring:** Align the `admin-app` with the new data models.
- **Phase 4: Code Health and Optimization:** Clean up the codebase, remove inconsistencies, and optimize queries.

## Work Completed

### Phase 1: Data Model Improvements

- **Status:** Completed.
- **Changes:**
    - Modified `user-app/src/screens/auth/SignUpScreen.tsx` to include a "Name" input field.
    - Created a new `signUp` function in `user-app/src/services/firebase.ts` to handle user creation and profile updates.
    - Updated the `user-app/src/types/Booking.ts` interface to include denormalized data fields (`userName`, `className`, etc.).

### Phase 2: `user-app` Refactoring

- **Status:** Completed.
- **Changes:**
    - Refactored the `bookClass` method in `user-app/src/services/yogaService.ts` to create the new, rich booking objects.
    - Refactored the `MyBookingsScreen.tsx` to fetch and display the new booking objects directly, improving performance and UI.
    - Updated the `getUserBookings` method in `yogaService.ts` to fetch the denormalized booking data.

### Detour: Database Reset Function

- **Status:** Completed and Deployed.
- **Purpose:** To create a secure and convenient way to wipe the Firebase Realtime Database.
- **Implementation:**
    - A Firebase Cloud Function (`resetDatabase`) was created in the `functions` directory.
    - The function is triggered by an HTTP request to a specific URL.
    - It is secured with a secret key, which must be passed as a query parameter (`?key=YOUR_SECRET_KEY`).
    - The function was successfully deployed to Firebase.

## Next Step

The next step is to begin **Phase 3: `admin-app` Refactoring**.

### Phase 3:

1.  **Update Data Models:** Modify `Booking.java` in the `admin-app` to include the new denormalized fields (`userName`, `className`, etc.).
2.  **Create Booking Repository:** Create a new `BookingRepository.java` responsible for fetching the list of rich booking objects for a specific class directly from Firebase.
3.  **Create Booking ViewModel:** Create a `BookingViewModel.java` to connect the repository to the UI.
4.  **Update `ClassDetailsActivity`:**
    *   Modify the `activity_class_details.xml` layout to add a `RecyclerView` for displaying the list of bookings.
    *   Update `ClassDetailsActivity.java` to use the new `BookingViewModel` to fetch and display the bookings.

### Upcoming Plan: ###
Wow, that's a lot of matches. Thank you for running that search. It's clear that
  the numeric courseId is deeply integrated into the admin-app, especially in the
  database (Room) and ViewModel layers.

  This is a more significant undertaking than I initially thought. Simply
  replacing courseId with courseFirebaseKey will not work, as it will break the
  local database schema and all the queries that depend on it.

  The New Plan: A Hybrid Approach

  Instead of ripping out the numeric courseId, we will adopt a hybrid approach.
  We will keep the courseId for the internal workings of the admin-app's local
  database, but we will also add the courseFirebaseKey and ensure it is saved to
  Firebase.

  Here is the revised plan:

   1. Re-add `courseId` to `YogaClass.java`: I will add the courseId field back to
      the YogaClass.java model, but we will mark it as @Ignore for Firebase
      serialization. This means it will be used by the local Room database but not
      saved to Firebase.
   2. Update `CreateClassActivity`: I will modify the createClasses method to set
      both the courseId (for local use) and the courseFirebaseKey (for Firebase).
   3. Fix `YogaClassDao`: The ClassWithCourseInfo query is now incorrect because we
      changed the foreign key. I will fix the @Relation annotation to use the
      correct columns.
   4. Fix `user-app`: The search functionality in the user-app is still using
      courseId. I will update it to use courseFirebaseKey.

  This is a more surgical approach that will fix the bug without requiring a
  major rewrite of the admin-app's database layer.

  I will start by re-adding the courseId to YogaClass.java. Shall I proceed?
