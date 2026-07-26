package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface MemberDao {
    @Query("SELECT * FROM members ORDER BY name ASC")
    fun getAllMembers(): Flow<List<MemberEntity>>

    @Query("SELECT * FROM members WHERE id = :id LIMIT 1")
    suspend fun getMemberById(id: String): MemberEntity?

    @Query("SELECT * FROM members WHERE qrId = :qrId LIMIT 1")
    suspend fun getMemberByQrId(qrId: String): MemberEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMember(member: MemberEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(members: List<MemberEntity>)

    @Update
    suspend fun updateMember(member: MemberEntity)

    @Query("DELETE FROM members WHERE id = :id")
    suspend fun deleteMemberById(id: String)
}

@Dao
interface PtSessionDao {
    @Query("SELECT * FROM pt_sessions ORDER BY timestamp DESC")
    fun getAllSessions(): Flow<List<PtSessionEntity>>

    @Query("SELECT * FROM pt_sessions WHERE memberId = :memberId ORDER BY timestamp DESC")
    fun getSessionsForMember(memberId: String): Flow<List<PtSessionEntity>>

    @Query("SELECT * FROM pt_sessions WHERE trainerId = :trainerId ORDER BY timestamp DESC")
    fun getSessionsForTrainer(trainerId: String): Flow<List<PtSessionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: PtSessionEntity)

    @Query("UPDATE pt_sessions SET status = :status, rejectionReason = :reason WHERE id = :id")
    suspend fun updateStatus(id: String, status: String, reason: String)
}

@Dao
interface DietPlanDao {
    @Query("SELECT * FROM diet_plans WHERE memberId = :memberId ORDER BY createdAt DESC LIMIT 1")
    fun getLatestDietForMember(memberId: String): Flow<DietPlanEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDietPlan(dietPlan: DietPlanEntity)
}

@Dao
interface WorkoutPlanDao {
    @Query("SELECT * FROM workout_plans WHERE memberId = :memberId ORDER BY createdAt DESC LIMIT 1")
    fun getLatestWorkoutForMember(memberId: String): Flow<WorkoutPlanEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkoutPlan(workoutPlan: WorkoutPlanEntity)
}

@Dao
interface LeadDao {
    @Query("SELECT * FROM leads ORDER BY followUpDate ASC")
    fun getAllLeads(): Flow<List<LeadEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLead(lead: LeadEntity)

    @Query("UPDATE leads SET status = :status WHERE id = :id")
    suspend fun updateLeadStatus(id: String, status: String)
}

@Dao
interface AuditLogDao {
    @Query("SELECT * FROM audit_logs ORDER BY timestamp DESC")
    fun getAllAuditLogs(): Flow<List<AuditLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAuditLog(log: AuditLogEntity)
}

@Dao
interface AttendanceDao {
    @Query("SELECT * FROM attendances ORDER BY timestamp DESC")
    fun getAllAttendances(): Flow<List<AttendanceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendance(attendance: AttendanceEntity)
}

@Dao
interface WeightLogDao {
    @Query("SELECT * FROM weight_logs WHERE memberId = :memberId ORDER BY timestamp ASC")
    fun getWeightLogsForMember(memberId: String): Flow<List<WeightLogEntity>>

    @Query("SELECT * FROM weight_logs ORDER BY timestamp ASC")
    fun getAllWeightLogs(): Flow<List<WeightLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeightLog(weightLog: WeightLogEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(weightLogs: List<WeightLogEntity>)

    @Query("DELETE FROM weight_logs WHERE id = :id")
    suspend fun deleteWeightLogById(id: String)
}

@Dao
interface LoggedWorkoutDao {
    @Query("SELECT * FROM logged_workout_sessions WHERE memberId = :memberId ORDER BY timestamp DESC")
    fun getSessionsForMember(memberId: String): Flow<List<LoggedWorkoutSessionEntity>>

    @Query("SELECT * FROM logged_workout_sessions ORDER BY timestamp DESC")
    fun getAllSessions(): Flow<List<LoggedWorkoutSessionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: LoggedWorkoutSessionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(sessions: List<LoggedWorkoutSessionEntity>)

    @Query("DELETE FROM logged_workout_sessions WHERE id = :id")
    suspend fun deleteSessionById(id: String)
}

