package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        MemberEntity::class,
        PtSessionEntity::class,
        DietPlanEntity::class,
        WorkoutPlanEntity::class,
        LeadEntity::class,
        AuditLogEntity::class,
        AttendanceEntity::class,
        WeightLogEntity::class,
        LoggedWorkoutSessionEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class GymDatabase : RoomDatabase() {
    abstract fun memberDao(): MemberDao
    abstract fun ptSessionDao(): PtSessionDao
    abstract fun dietPlanDao(): DietPlanDao
    abstract fun workoutPlanDao(): WorkoutPlanDao
    abstract fun leadDao(): LeadDao
    abstract fun auditLogDao(): AuditLogDao
    abstract fun attendanceDao(): AttendanceDao
    abstract fun weightLogDao(): WeightLogDao
    abstract fun loggedWorkoutDao(): LoggedWorkoutDao

    companion object {
        @Volatile
        private var INSTANCE: GymDatabase? = null

        fun getDatabase(context: Context): GymDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    GymDatabase::class.java,
                    "gymai_pro_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
