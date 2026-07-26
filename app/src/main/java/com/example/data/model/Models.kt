package com.example.data.model

import java.util.UUID

enum class UserRole(val displayName: String) {
    RECEPTIONIST("Receptionist"),
    TRAINER("Gym Trainer"),
    GYM_OWNER("Gym Owner")
}

enum class MembershipStatus(val displayName: String) {
    ACTIVE("Active"),
    EXPIRING_SOON("Expiring Soon"),
    EXPIRED("Expired"),
    CANCELLED("Cancelled")
}

enum class PtSessionStatus(val displayName: String) {
    PENDING_APPROVAL("Pending Approval"),
    APPROVED("Approved"),
    REJECTED("Rejected")
}

enum class LeadStatus(val displayName: String) {
    NEW("New Lead"),
    CONTACTED("Contacted"),
    TRIAL_SCHEDULED("Trial Scheduled"),
    CONVERTED("Converted"),
    LOST("Lost")
}

data class GymMembershipPackage(
    val id: String = UUID.randomUUID().toString(),
    val packageName: String,
    val durationMonths: Int,
    val price: Double,
    val description: String = ""
)

data class UserSession(
    val userId: String = "user_owner_1",
    val name: String = "Alex Vance",
    val email: String = "alex.vance@gymai.pro",
    val role: UserRole = UserRole.GYM_OWNER,
    val gymId: String = "gym_metro_01",
    val gymName: String = "Metro Fitness Club",
    val photoUrl: String = "",
    val phone: String = "+91 98765 43210",
    val isBiometricEnabled: Boolean = true,
    val token: String = "jwt_sec_token_sample"
)

data class Member(
    val id: String = UUID.randomUUID().toString(),
    val gymId: String = "gym_metro_01",
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
    val medicalConditions: String = "None",
    val membershipPlan: String = "Gold Annual",
    val status: MembershipStatus = MembershipStatus.ACTIVE,
    val trainerId: String = "trainer_1",
    val trainerName: String = "Coach Marcus",
    val ptPackageName: String = "12 Session PT Boost",
    val paymentStatus: String = "Paid",
    val joiningDate: String = "2026-01-10",
    val expiryDate: String = "2026-08-10",
    val attendanceCount: Int = 42,
    val qrId: String = "QR-${UUID.randomUUID().toString().take(8).uppercase()}",
    val photoUrl: String = "",
    val notes: String = "Prefers morning workouts."
)

data class PtSession(
    val id: String = UUID.randomUUID().toString(),
    val memberId: String,
    val memberName: String,
    val trainerId: String,
    val trainerName: String,
    val startTime: String,
    val endTime: String,
    val exercises: List<ExerciseLog> = emptyList(),
    val workoutNotes: String = "",
    val trainerNotes: String = "",
    val voiceNotePath: String = "",
    val photoUrl: String = "",
    val status: PtSessionStatus = PtSessionStatus.PENDING_APPROVAL,
    val rejectionReason: String = "",
    val isSuspicious: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

data class ExerciseLog(
    val name: String,
    val sets: Int,
    val reps: Int,
    val weightKg: Float,
    val caloriesBurned: Int = 0
)

data class DietPlan(
    val id: String = UUID.randomUUID().toString(),
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
    val meals: List<MealItem>,
    val explanationText: String,
    val createdAt: Long = System.currentTimeMillis()
)

data class MealItem(
    val time: String,
    val name: String,
    val foods: String,
    val calories: Int,
    val protein: Int,
    val carbs: Int,
    val fat: Int
)

data class WorkoutPlan(
    val id: String = UUID.randomUUID().toString(),
    val memberId: String,
    val memberName: String,
    val goal: String,
    val daysPerWeek: Int,
    val routines: List<DailyRoutine>,
    val createdAt: Long = System.currentTimeMillis()
)

data class DailyRoutine(
    val dayName: String,
    val title: String,
    val exercises: List<ExerciseDetail>
)

data class ExerciseDetail(
    val name: String,
    val sets: String,
    val reps: String,
    val rest: String,
    val notes: String
)

data class Lead(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val phone: String,
    val email: String,
    val source: String,
    val status: LeadStatus = LeadStatus.NEW,
    val followUpDate: String,
    val notes: String
)

data class RenewalItem(
    val memberId: String,
    val memberName: String,
    val phone: String,
    val planName: String,
    val expiryDate: String,
    val daysRemaining: Int,
    val amountDue: Double,
    val churnRiskScore: Int, // 0 to 100
    val aiPredictionNote: String
)

data class AnalyticsSummary(
    val todayRevenue: Double = 3450.0,
    val monthlyRevenue: Double = 42800.0,
    val membershipRevenue: Double = 31200.0,
    val renewalsCountToday: Int = 4,
    val upcomingRenewalsThisWeek: Int = 18,
    val pendingPaymentsCount: Int = 3,
    val pendingPaymentsAmount: Double = 1250.0,
    val todayCheckIns: Int = 128,
    val todayPtSessions: Int = 14,
    val trainerPerformanceRating: Float = 4.8f,
    val retentionRatePercent: Int = 88,
    val renewalRatePercent: Int = 82,
    val cancellationRatePercent: Int = 4,
    val projectedMonthlyRevenue: Double = 49200.0,
    val revenueTrend: List<Pair<String, Double>> = listOf(
        "Mon" to 4200.0,
        "Tue" to 3800.0,
        "Wed" to 5100.0,
        "Thu" to 4600.0,
        "Fri" to 6200.0,
        "Sat" to 7800.0,
        "Sun" to 5400.0
    )
)

data class AuditLog(
    val id: String = UUID.randomUUID().toString(),
    val timestamp: Long = System.currentTimeMillis(),
    val userId: String,
    val userName: String,
    val userRole: String,
    val action: String,
    val details: String
)

data class WeightLog(
    val id: String = UUID.randomUUID().toString(),
    val memberId: String,
    val date: String,
    val weightKg: Float,
    val note: String = ""
)

data class LoggedWorkoutSession(
    val id: String = UUID.randomUUID().toString(),
    val memberId: String,
    val memberName: String,
    val trainerName: String,
    val date: String,
    val workoutTitle: String,
    val exercises: List<LoggedExercise>,
    val notes: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

data class LoggedExercise(
    val exerciseName: String,
    val sets: List<LoggedSet>
)

data class LoggedSet(
    val setNumber: Int,
    val reps: Int,
    val weightKg: Float,
    val isCompleted: Boolean = true
)

data class KnowledgeArticle(
    val id: String,
    val title: String,
    val category: String,
    val summary: String,
    val source: String
)

data class DailyQuest(
    val id: String,
    val title: String,
    val description: String,
    val xpReward: Int,
    val isCompleted: Boolean = false,
    val category: String = "Fitness"
)

data class GamificationBadge(
    val id: String,
    val title: String,
    val iconEmoji: String,
    val description: String,
    val isUnlocked: Boolean = true
)

data class LeaderboardUser(
    val rank: Int,
    val name: String,
    val xp: Int,
    val levelName: String,
    val streakDays: Int,
    val isCurrentUser: Boolean = false
)

data class MemberGamificationState(
    val level: Int = 5,
    val levelTitle: String = "Gym Titan 🏆",
    val currentXp: Int = 1850,
    val xpForNextLevel: Int = 2200,
    val streakDays: Int = 7,
    val totalWorkoutsThisMonth: Int = 18,
    val quests: List<DailyQuest> = listOf(
        DailyQuest("q1", "Morning QR Check-In", "Scan your QR pass at gym entrance before 10 AM", 50, true, "Attendance"),
        DailyQuest("q2", "Hydration Goal", "Drink 8+ glasses (2.0L) of water today", 50, true, "Nutrition"),
        DailyQuest("q3", "Log 4 Daily Meals", "Track Breakfast, Lunch, Snack & Dinner in AI Planner", 75, false, "Nutrition"),
        DailyQuest("q4", "Complete PT / Heavy Lift", "Complete all assigned exercise sets with Coach Marcus", 100, false, "Workout")
    ),
    val badges: List<GamificationBadge> = listOf(
        GamificationBadge("b1", "7-Day Streak", "🔥", "Logged in 7 days in a row"),
        GamificationBadge("b2", "Hydration Hero", "💧", "Hit 2.5L water target 5 days in a row"),
        GamificationBadge("b3", "Iron Lifter", "🏋️‍♂️", "Completed 15 workout routines"),
        GamificationBadge("b4", "Macro Master", "🥗", "Followed AI Diet Plan perfectly")
    ),
    val leaderboard: List<LeaderboardUser> = listOf(
        LeaderboardUser(1, "Sarah Jenkins", 2150, "Level 6 Titan", 12, true),
        LeaderboardUser(2, "Marcus Brody", 1920, "Level 5 Crusher", 9),
        LeaderboardUser(3, "Elena Rostova", 1750, "Level 5 Shredder", 7),
        LeaderboardUser(4, "Michael Chang", 1420, "Level 4 Athlete", 5),
        LeaderboardUser(5, "Jordan Lee", 1180, "Level 3 Lifter", 4)
    )
)

enum class RoomSyncState(val label: String) {
    SYNCED("Synced"),
    SYNCING("Syncing..."),
    OFFLINE_CACHED("Offline Cached"),
    PENDING_PUSH("Pending Cloud Push"),
    SYNC_ERROR("Sync Error")
}

data class SyncStatusInfo(
    val state: RoomSyncState = RoomSyncState.SYNCED,
    val isOnline: Boolean = true,
    val lastSyncedTime: String = "Just now",
    val pendingMutationsCount: Int = 0,
    val pendingDetails: List<String> = emptyList(),
    val dbFileName: String = "gym_database.db",
    val localRecordCount: Int = 142
)

data class GymInvoice(
    val id: String = "INV-${System.currentTimeMillis().toString().takeLast(6)}",
    val memberId: String,
    val memberName: String,
    val memberPhone: String = "",
    val packageName: String,
    val amount: Double,
    val discount: Double = 0.0,
    val taxAmount: Double = 0.0,
    val totalAmount: Double = amount - discount + taxAmount,
    val date: String = "2026-07-25",
    val paymentStatus: String = "Paid", // Paid, Pending
    val paymentMode: String = "UPI", // UPI, Cash, Card, Bank Transfer
    val notes: String = "",
    val createdByRole: String = "Gym Owner",
    val timestamp: Long = System.currentTimeMillis()
)

data class AiChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val isUser: Boolean,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val actionExecutedText: String? = null,
    val actionType: String? = null
)

data class AiFitnessMemory(
    val id: String = UUID.randomUUID().toString(),
    val memberId: String,
    val memberName: String,
    val type: String, // "DIET" or "WORKOUT" or "FEEDBACK"
    val summaryText: String,
    val preferencesLearned: String,
    val timestamp: Long = System.currentTimeMillis()
)


