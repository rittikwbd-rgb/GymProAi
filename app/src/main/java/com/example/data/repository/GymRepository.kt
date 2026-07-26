package com.example.data.repository

import com.example.data.local.AttendanceEntity
import com.example.data.local.AuditLogEntity
import com.example.data.local.DietPlanEntity
import com.example.data.local.GymDatabase
import com.example.data.local.LeadEntity
import com.example.data.local.MemberEntity
import com.example.data.local.PtSessionEntity
import com.example.data.local.WorkoutPlanEntity
import com.example.data.local.WeightLogEntity
import com.example.data.local.LoggedWorkoutSessionEntity
import com.example.data.model.AnalyticsSummary
import com.example.data.model.AuditLog
import com.example.data.model.DietPlan
import com.example.data.model.ExerciseDetail
import com.example.data.model.ExerciseLog
import com.example.data.model.DailyRoutine
import com.example.data.model.Lead
import com.example.data.model.LeadStatus
import com.example.data.model.MealItem
import com.example.data.model.Member
import com.example.data.model.MembershipStatus
import com.example.data.model.PtSession
import com.example.data.model.PtSessionStatus
import com.example.data.model.RenewalItem
import com.example.data.model.UserRole
import com.example.data.model.UserSession
import com.example.data.model.WeightLog
import com.example.data.model.WorkoutPlan
import com.example.data.model.LoggedWorkoutSession
import com.example.data.model.LoggedExercise
import com.example.data.model.LoggedSet
import com.example.data.remote.SupabaseCloudBackendImpl
import org.json.JSONArray
import org.json.JSONObject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

class GymRepository(
    private val db: GymDatabase,
    private val cloudBackend: SupabaseCloudBackendImpl = SupabaseCloudBackendImpl()
) {

    private val memberDao = db.memberDao()
    private val ptSessionDao = db.ptSessionDao()
    private val dietPlanDao = db.dietPlanDao()
    private val workoutPlanDao = db.workoutPlanDao()
    private val leadDao = db.leadDao()
    private val auditLogDao = db.auditLogDao()

    val allMembers: Flow<List<Member>> = memberDao.getAllMembers().map { entities ->
        entities.map { it.toDomain() }
    }

    val allPtSessions: Flow<List<PtSession>> = ptSessionDao.getAllSessions().map { entities ->
        entities.map { it.toDomain() }
    }

    val allLeads: Flow<List<Lead>> = leadDao.getAllLeads().map { entities ->
        entities.map { it.toDomain() }
    }

    val allAuditLogs: Flow<List<AuditLog>> = auditLogDao.getAllAuditLogs().map { entities ->
        entities.map { it.toDomain() }
    }

    suspend fun getMemberById(id: String): Member? {
        return memberDao.getMemberById(id)?.toDomain()
    }

    suspend fun getMemberByQrId(qrId: String): Member? {
        return memberDao.getMemberByQrId(qrId)?.toDomain()
    }

    suspend fun saveMember(member: Member) {
        memberDao.insertMember(member.toEntity())
        cloudBackend.syncMembers(listOf(member))
    }

    suspend fun deleteMember(id: String) {
        memberDao.deleteMemberById(id)
    }

    suspend fun savePtSession(session: PtSession) {
        ptSessionDao.insertSession(session.toEntity())
        cloudBackend.syncPtSessions(listOf(session))
    }

    suspend fun updatePtSessionStatus(id: String, status: PtSessionStatus, reason: String = "") {
        ptSessionDao.updateStatus(id, status.name, reason)
    }

    fun getLatestDietForMember(memberId: String): Flow<DietPlan?> {
        return dietPlanDao.getLatestDietForMember(memberId).map { it?.toDomain() }
    }

    suspend fun saveDietPlan(dietPlan: DietPlan) {
        dietPlanDao.insertDietPlan(dietPlan.toEntity())
        cloudBackend.syncDietPlan(dietPlan)
    }

    fun getLatestWorkoutForMember(memberId: String): Flow<WorkoutPlan?> {
        return workoutPlanDao.getLatestWorkoutForMember(memberId).map { it?.toDomain() }
    }

    suspend fun saveWorkoutPlan(workoutPlan: WorkoutPlan) {
        workoutPlanDao.insertWorkoutPlan(workoutPlan.toEntity())
        cloudBackend.syncWorkoutPlan(workoutPlan)
    }

    suspend fun saveLead(lead: Lead) {
        leadDao.insertLead(lead.toEntity())
    }

    suspend fun updateLeadStatus(id: String, status: LeadStatus) {
        leadDao.updateLeadStatus(id, status.name)
    }

    suspend fun logAudit(user: UserSession, action: String, details: String) {
        val audit = AuditLog(
            userId = user.userId,
            userName = user.name,
            userRole = user.role.displayName,
            action = action,
            details = details
        )
        auditLogDao.insertAuditLog(audit.toEntity())
    }

    suspend fun recordAttendanceCheckIn(member: Member, checkInType: String = "QR Scan"): Boolean {
        val attendance = AttendanceEntity(
            id = UUID.randomUUID().toString(),
            memberId = member.id,
            memberName = member.name,
            timestamp = System.currentTimeMillis(),
            dateString = "2026-07-23",
            checkInType = checkInType
        )
        db.attendanceDao().insertAttendance(attendance)
        val updatedMember = member.copy(
            attendanceCount = member.attendanceCount + 1
        )
        saveMember(updatedMember)
        return true
    }

    fun getWeightLogsForMember(memberId: String): Flow<List<WeightLog>> {
        return db.weightLogDao().getWeightLogsForMember(memberId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    suspend fun saveWeightLog(weightLog: WeightLog) {
        db.weightLogDao().insertWeightLog(weightLog.toEntity())
        // Also update the member's current weight in database
        val member = getMemberById(weightLog.memberId)
        if (member != null) {
            saveMember(member.copy(weightKg = weightLog.weightKg))
        }
    }

    suspend fun deleteWeightLog(id: String) {
        db.weightLogDao().deleteWeightLogById(id)
    }

    fun getLoggedWorkoutsForMember(memberId: String): Flow<List<LoggedWorkoutSession>> {
        return db.loggedWorkoutDao().getSessionsForMember(memberId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    fun getAllLoggedWorkouts(): Flow<List<LoggedWorkoutSession>> {
        return db.loggedWorkoutDao().getAllSessions().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    suspend fun saveLoggedWorkoutSession(session: LoggedWorkoutSession) {
        db.loggedWorkoutDao().insertSession(session.toEntity())
        logAudit(
            user = UserSession(userId = session.trainerName, name = session.trainerName, role = UserRole.TRAINER),
            action = "PT_WORKOUT_LOGGED",
            details = "Logged session '${session.workoutTitle}' for ${session.memberName} with ${session.exercises.size} exercises"
        )
    }

    suspend fun deleteLoggedWorkoutSession(id: String) {
        db.loggedWorkoutDao().deleteSessionById(id)
    }

    /**
     * Seed Initial Commercial Data if DB is freshly created
     */
    suspend fun seedInitialDataIfEmpty() {
        // We do a quick check
        val existing = memberDao.getMemberById("mem_1")
        if (existing != null) return

        val sampleMembers = listOf(
            Member(
                id = "mem_1",
                name = "Sarah Jenkins",
                phone = "+91 98765 43210",
                whatsapp = "919876543210",
                email = "sarah.j@gmail.com",
                dob = "1994-05-12",
                gender = "Female",
                heightCm = 168f,
                weightKg = 64f,
                address = "BandrA West, Mumbai, Maharashtra",
                emergencyContact = "Rohan Jenkins (+91 98123 45678)",
                medicalConditions = "Mild Asthma",
                membershipPlan = "Gold Annual Membership",
                status = MembershipStatus.ACTIVE,
                trainerId = "trainer_1",
                trainerName = "Coach Marcus",
                ptPackageName = "24 Session PT Sculpt",
                paymentStatus = "Paid",
                joiningDate = "2026-01-15",
                expiryDate = "2026-08-15",
                attendanceCount = 48,
                qrId = "QR-SARAH01",
                notes = "Goal: Pure Veg Diet & Strength build. Prefers 7 AM sessions."
            ),
            Member(
                id = "mem_2",
                name = "David Miller",
                phone = "+91 98234 56789",
                whatsapp = "919823456789",
                email = "david.m@outlook.com",
                dob = "1988-11-20",
                gender = "Male",
                heightCm = 182f,
                weightKg = 88f,
                address = "Indiranagar, Bengaluru, Karnataka",
                emergencyContact = "Elena Miller (+91 98345 67890)",
                medicalConditions = "None",
                membershipPlan = "Quarterly Elite Pro",
                status = MembershipStatus.EXPIRING_SOON,
                trainerId = "trainer_2",
                trainerName = "Coach Elena",
                ptPackageName = "12 Session Hypertrophy",
                paymentStatus = "Renewal Pending",
                joiningDate = "2026-04-25",
                expiryDate = "2026-07-28", // Expiring in 5 days
                attendanceCount = 32,
                qrId = "QR-DAVID02",
                notes = "Eggetarian diet. Wants to renew annual tier with discount."
            ),
            Member(
                id = "mem_3",
                name = "Emily Rodriguez",
                phone = "+91 98456 78901",
                whatsapp = "919845678901",
                email = "emily.r@techcorp.com",
                dob = "1996-03-08",
                gender = "Female",
                heightCm = 162f,
                weightKg = 58f,
                address = "Connaught Place, New Delhi",
                emergencyContact = "Carlos Rodriguez (+91 98567 89012)",
                medicalConditions = "Previous Knee Injury",
                membershipPlan = "Monthly Flex Pass",
                status = MembershipStatus.EXPIRED,
                trainerId = "trainer_1",
                trainerName = "Coach Marcus",
                ptPackageName = "6 Session Rehab Focus",
                paymentStatus = "Due",
                joiningDate = "2026-05-01",
                expiryDate = "2026-07-01",
                attendanceCount = 14,
                qrId = "QR-EMILY03",
                notes = "Inactive for 3 weeks. High churn risk."
            ),
            Member(
                id = "mem_4",
                name = "Michael Vance",
                phone = "+91 98678 90123",
                whatsapp = "919867890123",
                email = "mvance@finance.org",
                dob = "1982-08-30",
                gender = "Male",
                heightCm = 178f,
                weightKg = 82f,
                address = "Jubilee Hills, Hyderabad, Telangana",
                emergencyContact = "Claire Vance (+91 98789 01234)",
                medicalConditions = "Hypertension",
                membershipPlan = "Platinum VIP Lifetime",
                status = MembershipStatus.ACTIVE,
                trainerId = "trainer_2",
                trainerName = "Coach Elena",
                ptPackageName = "VIP Unlimited PT",
                paymentStatus = "Paid",
                joiningDate = "2025-10-10",
                expiryDate = "2027-10-10",
                attendanceCount = 94,
                qrId = "QR-MIKE04",
                notes = "Requires quiet locker and steam room access."
            )
        )

        memberDao.insertAll(sampleMembers.map { it.toEntity() })

        val sampleSessions = listOf(
            PtSession(
                id = "pts_1",
                memberId = "mem_1",
                memberName = "Sarah Jenkins",
                trainerId = "trainer_1",
                trainerName = "Coach Marcus",
                startTime = "07:00 AM",
                endTime = "08:00 AM",
                exercises = listOf(
                    ExerciseLog("Barbell Squats", 4, 10, 60f, 180),
                    ExerciseLog("Romanian Deadlifts", 3, 12, 50f, 140),
                    ExerciseLog("Walking Lunges", 3, 15, 20f, 110)
                ),
                workoutNotes = "Pushed hard on squat depth today. Form was immaculate.",
                trainerNotes = "Sarah achieved PR on Barbell Squats at 60kg.",
                voiceNotePath = "simulated_audio_note_sarah_01.mp3",
                status = PtSessionStatus.PENDING_APPROVAL,
                timestamp = System.currentTimeMillis() - 3600000
            ),
            PtSession(
                id = "pts_2",
                memberId = "mem_2",
                memberName = "David Miller",
                trainerId = "trainer_2",
                trainerName = "Coach Elena",
                startTime = "06:00 PM",
                endTime = "07:00 PM",
                exercises = listOf(
                    ExerciseLog("Bench Press", 4, 8, 80f, 210),
                    ExerciseLog("Incline Dumbbell Press", 3, 10, 30f, 160)
                ),
                workoutNotes = "Upper body push focus.",
                trainerNotes = "David completed session smoothly.",
                status = PtSessionStatus.APPROVED,
                timestamp = System.currentTimeMillis() - 86400000
            )
        )

        sampleSessions.forEach { ptSessionDao.insertSession(it.toEntity()) }

        val sampleLeads = listOf(
            Lead(
                id = "lead_1",
                name = "Jonathan Croft",
                phone = "+1 (555) 901-2345",
                email = "jcroft@adventure.com",
                source = "Website Form",
                status = LeadStatus.NEW,
                followUpDate = "Today 4:00 PM",
                notes = "Inquired about 1-on-1 PT transformation package."
            ),
            Lead(
                id = "lead_2",
                name = "Amanda Smith",
                phone = "+1 (555) 012-3456",
                email = "amanda.s@design.co",
                source = "Instagram Ad",
                status = LeadStatus.TRIAL_SCHEDULED,
                followUpDate = "Tomorrow 10:00 AM",
                notes = "Free trial pass scheduled with Coach Elena."
            )
        )

        sampleLeads.forEach { leadDao.insertLead(it.toEntity()) }

        val sampleWeightLogs = listOf(
            // Sarah Jenkins (mem_1): Starting 68.5kg -> Current 64kg
            WeightLogEntity("wl_1_1", "mem_1", "01 Jun", 68.5f, "Initial join weigh-in", System.currentTimeMillis() - 40 * 86400000L),
            WeightLogEntity("wl_1_2", "mem_1", "10 Jun", 67.2f, "End of week 1 high intensity", System.currentTimeMillis() - 31 * 86400000L),
            WeightLogEntity("wl_1_3", "mem_1", "20 Jun", 66.0f, "Switched to Pure Veg high protein plan", System.currentTimeMillis() - 21 * 86400000L),
            WeightLogEntity("wl_1_4", "mem_1", "01 Jul", 65.1f, "Consistently hitting 10k steps daily", System.currentTimeMillis() - 10 * 86400000L),
            WeightLogEntity("wl_1_5", "mem_1", "15 Jul", 64.0f, "Current weigh-in - feeling energetic!", System.currentTimeMillis() - 2 * 86400000L),

            // David Miller (mem_2): Starting 92kg -> Current 88kg
            WeightLogEntity("wl_2_1", "mem_2", "01 Jun", 92.0f, "Pre-bulk weigh in", System.currentTimeMillis() - 40 * 86400000L),
            WeightLogEntity("wl_2_2", "mem_2", "15 Jun", 90.5f, "Cut phase week 2", System.currentTimeMillis() - 26 * 86400000L),
            WeightLogEntity("wl_2_3", "mem_2", "01 Jul", 89.2f, "Strength progressing well", System.currentTimeMillis() - 10 * 86400000L),
            WeightLogEntity("wl_2_4", "mem_2", "20 Jul", 88.0f, "Reached quarterly milestone", System.currentTimeMillis() - 1 * 86400000L)
        )

        db.weightLogDao().insertAll(sampleWeightLogs)

        val sampleLoggedWorkouts = listOf(
            LoggedWorkoutSessionEntity(
                id = "log_sess_1",
                memberId = "mem_1",
                memberName = "Sarah Jenkins",
                trainerName = "Coach Alex Vance",
                date = "22 Jul",
                workoutTitle = "Hypertrophy Upper Body & Core",
                exercisesJson = JSONArray().apply {
                    put(JSONObject().apply {
                        put("exerciseName", "Barbell Bench Press")
                        put("sets", JSONArray().apply {
                            put(JSONObject().put("setNumber", 1).put("reps", 12).put("weightKg", 40.0).put("isCompleted", true))
                            put(JSONObject().put("setNumber", 2).put("reps", 10).put("weightKg", 45.0).put("isCompleted", true))
                            put(JSONObject().put("setNumber", 3).put("reps", 8).put("weightKg", 50.0).put("isCompleted", true))
                        })
                    })
                    put(JSONObject().apply {
                        put("exerciseName", "Lat Pulldown")
                        put("sets", JSONArray().apply {
                            put(JSONObject().put("setNumber", 1).put("reps", 12).put("weightKg", 35.0).put("isCompleted", true))
                            put(JSONObject().put("setNumber", 2).put("reps", 10).put("weightKg", 40.0).put("isCompleted", true))
                            put(JSONObject().put("setNumber", 3).put("reps", 10).put("weightKg", 40.0).put("isCompleted", true))
                        })
                    })
                    put(JSONObject().apply {
                        put("exerciseName", "Dumbbell Shoulder Press")
                        put("sets", JSONArray().apply {
                            put(JSONObject().put("setNumber", 1).put("reps", 12).put("weightKg", 12.0).put("isCompleted", true))
                            put(JSONObject().put("setNumber", 2).put("reps", 10).put("weightKg", 14.0).put("isCompleted", true))
                            put(JSONObject().put("setNumber", 3).put("reps", 8).put("weightKg", 14.0).put("isCompleted", true))
                        })
                    })
                }.toString(),
                notes = "Increased bench press top set weight to 50kg. Great scapular control.",
                timestamp = System.currentTimeMillis() - 2 * 86400000L
            ),
            LoggedWorkoutSessionEntity(
                id = "log_sess_2",
                memberId = "mem_2",
                memberName = "David Miller",
                trainerName = "Coach Alex Vance",
                date = "23 Jul",
                workoutTitle = "Legs & Lower Body Power",
                exercisesJson = JSONArray().apply {
                    put(JSONObject().apply {
                        put("exerciseName", "Barbell Back Squat")
                        put("sets", JSONArray().apply {
                            put(JSONObject().put("setNumber", 1).put("reps", 10).put("weightKg", 80.0).put("isCompleted", true))
                            put(JSONObject().put("setNumber", 2).put("reps", 8).put("weightKg", 90.0).put("isCompleted", true))
                            put(JSONObject().put("setNumber", 3).put("reps", 6).put("weightKg", 100.0).put("isCompleted", true))
                            put(JSONObject().put("setNumber", 4).put("reps", 6).put("weightKg", 100.0).put("isCompleted", true))
                        })
                    })
                    put(JSONObject().apply {
                        put("exerciseName", "Romanian Deadlift")
                        put("sets", JSONArray().apply {
                            put(JSONObject().put("setNumber", 1).put("reps", 10).put("weightKg", 70.0).put("isCompleted", true))
                            put(JSONObject().put("setNumber", 2).put("reps", 10).put("weightKg", 75.0).put("isCompleted", true))
                            put(JSONObject().put("setNumber", 3).put("reps", 8).put("weightKg", 80.0).put("isCompleted", true))
                        })
                    })
                }.toString(),
                notes = "New PR on squat 100kg for 6 reps. Controlled tempo throughout.",
                timestamp = System.currentTimeMillis() - 1 * 86400000L
            )
        )

        db.loggedWorkoutDao().insertAll(sampleLoggedWorkouts)
    }

    // Entity mapping helpers
    private fun LoggedWorkoutSessionEntity.toDomain(): LoggedWorkoutSession {
        val exercisesList = mutableListOf<LoggedExercise>()
        try {
            val array = JSONArray(exercisesJson)
            for (i in 0 until array.length()) {
                val exObj = array.getJSONObject(i)
                val exName = exObj.optString("exerciseName", "")
                val setsArray = exObj.optJSONArray("sets") ?: JSONArray()
                val setsList = mutableListOf<LoggedSet>()
                for (j in 0 until setsArray.length()) {
                    val setObj = setsArray.getJSONObject(j)
                    setsList.add(
                        LoggedSet(
                            setNumber = setObj.optInt("setNumber", j + 1),
                            reps = setObj.optInt("reps", 10),
                            weightKg = setObj.optDouble("weightKg", 0.0).toFloat(),
                            isCompleted = setObj.optBoolean("isCompleted", true)
                        )
                    )
                }
                exercisesList.add(LoggedExercise(exerciseName = exName, sets = setsList))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return LoggedWorkoutSession(
            id = id,
            memberId = memberId,
            memberName = memberName,
            trainerName = trainerName,
            date = date,
            workoutTitle = workoutTitle,
            exercises = exercisesList,
            notes = notes,
            timestamp = timestamp
        )
    }

    private fun LoggedWorkoutSession.toEntity(): LoggedWorkoutSessionEntity {
        val array = JSONArray()
        exercises.forEach { ex ->
            val exObj = JSONObject()
            exObj.put("exerciseName", ex.exerciseName)
            val setsArray = JSONArray()
            ex.sets.forEach { set ->
                val setObj = JSONObject()
                setObj.put("setNumber", set.setNumber)
                setObj.put("reps", set.reps)
                setObj.put("weightKg", set.weightKg)
                setObj.put("isCompleted", set.isCompleted)
                setsArray.put(setObj)
            }
            exObj.put("sets", setsArray)
            array.put(exObj)
        }
        return LoggedWorkoutSessionEntity(
            id = id,
            memberId = memberId,
            memberName = memberName,
            trainerName = trainerName,
            date = date,
            workoutTitle = workoutTitle,
            exercisesJson = array.toString(),
            notes = notes,
            timestamp = timestamp
        )
    }

    private fun WeightLogEntity.toDomain() = WeightLog(
        id = id, memberId = memberId, date = date, weightKg = weightKg, note = note
    )

    private fun WeightLog.toEntity() = WeightLogEntity(
        id = id, memberId = memberId, date = date, weightKg = weightKg, note = note, timestamp = System.currentTimeMillis()
    )
    private fun MemberEntity.toDomain() = Member(
        id = id, gymId = gymId, name = name, phone = phone, whatsapp = whatsapp,
        email = email, dob = dob, gender = gender, heightCm = heightCm, weightKg = weightKg,
        address = address, emergencyContact = emergencyContact, medicalConditions = medicalConditions,
        membershipPlan = membershipPlan, status = MembershipStatus.valueOf(status),
        trainerId = trainerId, trainerName = trainerName, ptPackageName = ptPackageName,
        paymentStatus = paymentStatus, joiningDate = joiningDate, expiryDate = expiryDate,
        attendanceCount = attendanceCount, qrId = qrId, photoUrl = photoUrl, notes = notes
    )

    private fun Member.toEntity() = MemberEntity(
        id = id, gymId = gymId, name = name, phone = phone, whatsapp = whatsapp,
        email = email, dob = dob, gender = gender, heightCm = heightCm, weightKg = weightKg,
        address = address, emergencyContact = emergencyContact, medicalConditions = medicalConditions,
        membershipPlan = membershipPlan, status = status.name, trainerId = trainerId,
        trainerName = trainerName, ptPackageName = ptPackageName, paymentStatus = paymentStatus,
        joiningDate = joiningDate, expiryDate = expiryDate, attendanceCount = attendanceCount,
        qrId = qrId, photoUrl = photoUrl, notes = notes
    )

    private fun PtSessionEntity.toDomain(): PtSession {
        val exList = mutableListOf<ExerciseLog>()
        try {
            val arr = JSONArray(exercisesJson)
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                exList.add(
                    ExerciseLog(
                        name = obj.getString("name"),
                        sets = obj.getInt("sets"),
                        reps = obj.getInt("reps"),
                        weightKg = obj.getDouble("weightKg").toFloat(),
                        caloriesBurned = obj.optInt("caloriesBurned", 0)
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return PtSession(
            id = id, memberId = memberId, memberName = memberName, trainerId = trainerId,
            trainerName = trainerName, startTime = startTime, endTime = endTime,
            exercises = exList, workoutNotes = workoutNotes, trainerNotes = trainerNotes,
            voiceNotePath = voiceNotePath, photoUrl = photoUrl, status = PtSessionStatus.valueOf(status),
            rejectionReason = rejectionReason, isSuspicious = isSuspicious, timestamp = timestamp
        )
    }

    private fun PtSession.toEntity(): PtSessionEntity {
        val arr = JSONArray()
        exercises.forEach { ex ->
            val obj = JSONObject().apply {
                put("name", ex.name)
                put("sets", ex.sets)
                put("reps", ex.reps)
                put("weightKg", ex.weightKg)
                put("caloriesBurned", ex.caloriesBurned)
            }
            arr.put(obj)
        }
        return PtSessionEntity(
            id = id, memberId = memberId, memberName = memberName, trainerId = trainerId,
            trainerName = trainerName, startTime = startTime, endTime = endTime,
            exercisesJson = arr.toString(), workoutNotes = workoutNotes, trainerNotes = trainerNotes,
            voiceNotePath = voiceNotePath, photoUrl = photoUrl, status = status.name,
            rejectionReason = rejectionReason, isSuspicious = isSuspicious, timestamp = timestamp
        )
    }

    private fun DietPlanEntity.toDomain(): DietPlan {
        val mealList = mutableListOf<MealItem>()
        try {
            val arr = JSONArray(mealsJson)
            for (i in 0 until arr.length()) {
                val m = arr.getJSONObject(i)
                mealList.add(
                    MealItem(
                        time = m.getString("time"),
                        name = m.getString("name"),
                        foods = m.getString("foods"),
                        calories = m.getInt("calories"),
                        protein = m.getInt("protein"),
                        carbs = m.getInt("carbs"),
                        fat = m.getInt("fat")
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return DietPlan(
            id = id, memberId = memberId, memberName = memberName, age = age, gender = gender,
            heightCm = heightCm, weightKg = weightKg, targetWeightKg = targetWeightKg,
            activityLevel = activityLevel, goal = goal, regionFoodPref = regionFoodPref,
            bmi = bmi, bmr = bmr, maintenanceCalories = maintenanceCalories,
            targetCalories = targetCalories, proteinGrams = proteinGrams, carbsGrams = carbsGrams,
            fatGrams = fatGrams, waterLiters = waterLiters, meals = mealList,
            explanationText = explanationText, createdAt = createdAt
        )
    }

    private fun DietPlan.toEntity(): DietPlanEntity {
        val arr = JSONArray()
        meals.forEach { meal ->
            val obj = JSONObject().apply {
                put("time", meal.time)
                put("name", meal.name)
                put("foods", meal.foods)
                put("calories", meal.calories)
                put("protein", meal.protein)
                put("carbs", meal.carbs)
                put("fat", meal.fat)
            }
            arr.put(obj)
        }
        return DietPlanEntity(
            id = id, memberId = memberId, memberName = memberName, age = age, gender = gender,
            heightCm = heightCm, weightKg = weightKg, targetWeightKg = targetWeightKg,
            activityLevel = activityLevel, goal = goal, regionFoodPref = regionFoodPref,
            bmi = bmi, bmr = bmr, maintenanceCalories = maintenanceCalories,
            targetCalories = targetCalories, proteinGrams = proteinGrams, carbsGrams = carbsGrams,
            fatGrams = fatGrams, waterLiters = waterLiters, mealsJson = arr.toString(),
            explanationText = explanationText, createdAt = createdAt
        )
    }

    private fun WorkoutPlanEntity.toDomain(): WorkoutPlan {
        val routineList = mutableListOf<DailyRoutine>()
        try {
            val arr = JSONArray(routinesJson)
            for (i in 0 until arr.length()) {
                val rObj = arr.getJSONObject(i)
                val dayName = rObj.getString("dayName")
                val title = rObj.getString("title")
                val exArr = rObj.getJSONArray("exercises")
                val exList = mutableListOf<ExerciseDetail>()
                for (j in 0 until exArr.length()) {
                    val e = exArr.getJSONObject(j)
                    exList.add(
                        ExerciseDetail(
                            name = e.getString("name"),
                            sets = e.getString("sets"),
                            reps = e.getString("reps"),
                            rest = e.getString("rest"),
                            notes = e.optString("notes", "")
                        )
                    )
                }
                routineList.add(DailyRoutine(dayName, title, exList))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return WorkoutPlan(
            id = id, memberId = memberId, memberName = memberName, goal = goal,
            daysPerWeek = daysPerWeek, routines = routineList, createdAt = createdAt
        )
    }

    private fun WorkoutPlan.toEntity(): WorkoutPlanEntity {
        val arr = JSONArray()
        routines.forEach { r ->
            val exArr = JSONArray()
            r.exercises.forEach { e ->
                exArr.put(JSONObject().apply {
                    put("name", e.name)
                    put("sets", e.sets)
                    put("reps", e.reps)
                    put("rest", e.rest)
                    put("notes", e.notes)
                })
            }
            val rObj = JSONObject().apply {
                put("dayName", r.dayName)
                put("title", r.title)
                put("exercises", exArr)
            }
            arr.put(rObj)
        }
        return WorkoutPlanEntity(
            id = id, memberId = memberId, memberName = memberName, goal = goal,
            daysPerWeek = daysPerWeek, routinesJson = arr.toString(), createdAt = createdAt
        )
    }

    private fun LeadEntity.toDomain() = Lead(
        id = id, name = name, phone = phone, email = email, source = source,
        status = LeadStatus.valueOf(status), followUpDate = followUpDate, notes = notes
    )

    private fun Lead.toEntity() = LeadEntity(
        id = id, name = name, phone = phone, email = email, source = source,
        status = status.name, followUpDate = followUpDate, notes = notes
    )

    private fun AuditLogEntity.toDomain() = AuditLog(
        id = id, timestamp = timestamp, userId = userId, userName = userName,
        userRole = userRole, action = action, details = details
    )

    private fun AuditLog.toEntity() = AuditLogEntity(
        id = id, timestamp = timestamp, userId = userId, userName = userName,
        userRole = userRole, action = action, details = details
    )
}
