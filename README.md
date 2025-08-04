# Universal Yoga

Welcome to Universal Yoga, a comprehensive class management system featuring a dedicated admin application and a user-facing mobile app. This solution is designed to streamline the process of managing yoga classes, schedules, and bookings.

---

## Project Structure

The project is a monorepo containing two main applications:

-   **`admin-app/`**: An Android application built with Java and Android Studio for administrators to manage courses, classes, and view bookings.
-   **`user-app/`**: A React Native mobile application for users to browse, book, and manage their yoga classes.

---

## 🚀 Getting Started

### Prerequisites

-   **For the Admin App:**
    -   Android Studio
    -   Java Development Kit (JDK)
    -   Android SDK
-   **For the User App:**
    -   Node.js and npm
    -   Expo CLI
    -   React Native development environment

### Installation & Setup

1.  **Clone the repository:**
    ```bash
    git clone [https://github.com/your-username/comp1786-universal-yoga.git](https://github.com/your-username/comp1786-universal-yoga.git)
    ```
2.  **Set up the Admin App:**
    -   Open the `admin-app` directory in Android Studio.
    -   Sync the project with Gradle files.
    -   Run the application on an Android emulator or a physical device.
3.  **Set up the User App:**
    -   Navigate to the `user-app` directory:
        ```bash
        cd user-app
        ```
    -   Install the dependencies:
        ```bash
        npm install
        ```
    -   Start the development server:
        ```bash
        npm start
        ```

---

## ✨ Key Features

### 🧘 Admin App (Android)

-   **Course Management**: Create, edit, and delete yoga course templates with details like class type, duration, price, and capacity.
-   **Class Scheduling**: Schedule individual classes based on course templates, assign instructors, and set specific dates.
-   **Booking Overview**: View a list of bookings for each class to track attendance.
-   **Real-time Sync**: Data is synchronized in real-time with Firebase, ensuring consistency across the platform.

### 🤸 User App (React Native)

-   **Class Browse and Searching**: Users can easily browse and search for available yoga classes.
-   **Class Booking**: A simple and intuitive booking system allows users to reserve their spot in a class.
-   **User Profiles**: Users can create and manage their profiles.
-   **Booking Management**: Users can view their upcoming and past bookings.

---

## 🤝 Contributing

Contributions are welcome! If you'd like to contribute to the project, please follow these steps:

1.  Fork the repository.
2.  Create a new branch for your feature or bug fix.
3.  Make your changes and commit them with descriptive messages.
4.  Push your changes to your forked repository.
5.  Create a pull request with a detailed explanation of your changes.

We appreciate your help in making Universal Yoga even better!