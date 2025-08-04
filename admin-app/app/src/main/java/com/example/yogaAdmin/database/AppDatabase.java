package com.example.yogaAdmin.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.example.yogaAdmin.dao.YogaClassDao;
import com.example.yogaAdmin.dao.YogaCourseDao;
import com.example.yogaAdmin.models.YogaClass;
import com.example.yogaAdmin.models.YogaCourse;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * The main database class for the application, built using Room Persistence Library.
 * It defines the database configuration, serves as the main access point to the persisted data,
 * and provides access to the DAOs.
 *
 * @version 10
 */
@Database(entities = {YogaCourse.class, YogaClass.class}, version = 10, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    /**
     * Provides access to the {@link YogaCourseDao}.
     * @return The Data Access Object for yoga courses.
     */
    public abstract YogaCourseDao yogaCourseDao();

    /**
     * Provides access to the {@link YogaClassDao}.
     * @return The Data Access Object for yoga classes.
     */
    public abstract YogaClassDao yogaClassDao();

    // Singleton instance of the AppDatabase to prevent having multiple instances of the database opened at the same time.
    private static volatile AppDatabase INSTANCE;
    // Number of threads for the database write executor.
    private static final int NUMBER_OF_THREADS = 4;
    // Executor service to run database operations asynchronously on a background thread.
    public static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    /**
     * A sample migration object. Although fallbackToDestructiveMigration is used,
     * this shows how a migration would be implemented.
     */
    static final Migration MIGRATION_4_5 = new Migration(4, 5) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            // Example migration: Add 'firebaseKey' columns to the tables.
            database.execSQL("ALTER TABLE yoga_courses ADD COLUMN firebaseKey TEXT");
            database.execSQL("ALTER TABLE yoga_classes ADD COLUMN firebaseKey TEXT");
        }
    };

    /**
     * Returns the singleton instance of the AppDatabase.
     * If the instance is not null, it returns the existing instance.
     * Otherwise, it creates a new database instance.
     *
     * @param context The application context.
     * @return The singleton AppDatabase instance.
     */
    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    // Create the Room database instance.
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "yoga_database")
                            // Wipes and rebuilds the database instead of migrating if no Migration object is provided.
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
