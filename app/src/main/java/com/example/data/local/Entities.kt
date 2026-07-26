package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "members")
data class MemberEntity(
    @PrimaryKey val id: String,
    val gymId: String,
    val name: String,
    val phone: String,
    val whatsapp: String,
    val email: String,
    val dob: String,
    val gender: String,
    val heightCm: Float,
    val weightKg: Float,
    val address: String,
    val emergencyContact: String,
    val medicalConditions: String,
    val membershipPlan: String,
    val status: String,
    val trainerId: String,
    val trainerName: String,
    val ptPackageName: String,
    val paymentStatus: String,
    val joiningDate: String,
    val expiryDate: String,
    val attendanceCount: Int,
    val qrId: String,
    val photoUrl: String,
    val notes: String
)

@Entity(tableName = "pt_sessions")
data class PtSessionEntity(
    @PrimaryKey val id: String,
    val memberId: String,
    val memberName: String,
    val trainerId: String,
    val trainerName: String,
    val startTime: String,
    val endTime: String,
    val exercisesJson: String,
    val workoutNotes: String,
    val trainerNotes: String,
    val voiceNotePath: String,
    val photoUrl: String,
    val status: String,
    val rejectionReason: String,
    val isSuspicious: Boolean,
    val timestamp: Long
)

@Entity(tableName = "diet_plans")
data class DietPlanEntity(
    @PrimaryKey val id: String,
    val memberId: String,
    val memberName: String,
    val age: Int,
    val gender: String,
    val heightCm: Float,
    val weightKg: Float,
    val targetWeightKg: Float,
    val activityLevel: String,
    val goal: String,
    val regionFoodPref: String,
    val bmi: Float,
    val bmr: Int,
    val maintenanceCalories: Int,
    val targetCalories: Int,
    val proteinGrams: Int,
    val carbsGrams: Int,
    val fatGrams: Int,
    val waterLiters: Float,
    val mealsJson: String,
    val explanationText: String,
    val createdAt: Long
)

@Entity(tableName = "workout_plans")
data class WorkoutPlanEntity(
    @PrimaryKey val id: String,
    val memberId: String,
    val memberName: String,
    val goal: String,
    val daysPerWeek: Int,
    val routinesJson: String,
    val createdAt: Long
)

@Entity(tableName = "leads")
data class LeadEntity(
    @PrimaryKey val id: String,
    val name: String,
    val phone: String,
    val email: String,
    val source: String,
    val status: String,
    val followUpDate: String,
    val notes: String
)

@Entity(tableName = "audit_logs")
data class AuditLogEntity(
    @PrimaryKey val id: String,
    val timestamp: Long,
    val userId: String,
    val userName: String,
    val userRole: String,
    val action: String,
    val details: String
)

@Entity(tableName = "attendances")
data class AttendanceEntity(
    @PrimaryKey val id: String,
    val memberId: String,
    val memberName: String,
    val timestamp: Long,
    val dateString: String,
    val checkInType: String // e.g. "QR", "Manual", "Biometric"
)

@Entity(tableName = "weight_logs")
data class WeightLogEntity(
    @PrimaryKey val id: String,
    val memberId: String,
    val date: String,
    val weightKg: Float,
    val note: String,
    val timestamp: Long
)

@Entity(tableName = "logged_workout_sessions")
data class LoggedWorkoutSessionEntity(
    @PrimaryKey val id: String,
    val memberId: String,
    val memberName: String,
    val trainerName: String,
    val date: String,
    val workoutTitle: String,
    val exercisesJson: String,
    val notes: String,
    val timestamp: Long
)

