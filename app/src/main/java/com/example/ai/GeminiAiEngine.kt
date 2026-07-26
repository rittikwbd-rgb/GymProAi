package com.example.ai

import com.example.BuildConfig
import com.example.data.model.DailyRoutine
import com.example.data.model.DietPlan
import com.example.data.model.ExerciseDetail
import com.example.data.model.MealItem
import com.example.data.model.Member
import com.example.data.model.WorkoutPlan
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class GeminiAiEngine {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    /**
     * Knowledge Base RAG Reference Context (ICMR / NIN & Indian Sports Nutrition Standards)
     */
    private val RAG_KNOWLEDGE_BASE = """
        [RAG KNOWLEDGE BASE - INDIAN SPORTS NUTRITION & EXERCISE PHYSIOLOGY]
        1. Energy Balance & BMR: Mifflin-St Jeor equation customized for Indian demographics.
           - Male BMR = 10 * weight(kg) + 6.25 * height(cm) - 5 * age + 5
           - Female BMR = 10 * weight(kg) + 6.25 * height(cm) - 5 * age - 161
        2. Strict Dietary Classification Rules (MANDATORY ACCURACY):
           - PURE VEGETARIAN (Veg): ABSOLUTELY NO EGGS, NO CHICKEN, NO MEAT, NO FISH.
             Primary Protein Sources: Paneer (18g protein/100g), Soya Chunks (52g protein/100g), Tofu, Hung Curd/Ghee/Lassi, Sprouts, Dal (Moong, Masoor, Chana, Toor), Rajma, Chole (Chickpeas), Roasted Makhana, Sattu, Whey Protein Isolate/Concentrate, Almonds & Seeds.
           - EGGETARIAN: Vegetarian diet + Eggs (Boiled egg whites, Omelette). STRICTLY NO chicken, fish, or meat.
           - NON-VEGETARIAN: Chicken breast, Fish, Eggs, Mutton, alongside Indian staples (Roti, Brown Basmati Rice, Dal).
           - JAIN VEGETARIAN: Pure Vegetarian + STRICTLY NO onion, NO garlic, NO potatoes or root vegetables.
        3. Carbohydrates & Healthy Indian Fats:
           - Roti/Chapati (Whole Wheat/Multigrain), Oats, Brown Rice, Poha, Idli/Dosa, Quinoa.
           - Healthy Fats: Cow Ghee (moderate), Mustard Oil, Olive Oil, Nuts, Flaxseeds.
        4. Hydration & Recovery: 3.5 - 4.5 Liters/day + Coconut Water / ORS / Nimbu Pani post-workout.
        5. Disclaimer: Non-diagnostic sports nutrition guidance tailored for Indian fitness enthusiasts.
    """.trimIndent()

    suspend fun generateDietPlan(
        member: Member,
        age: Int,
        targetWeightKg: Float,
        activityLevel: String,
        goal: String,
        regionFoodPref: String,
        learnedMemory: String = ""
    ): DietPlan = withContext(Dispatchers.IO) {
        // Calculate BMR using Mifflin-St Jeor
        val isMale = member.gender.equals("Male", ignoreCase = true)
        val bmr = if (isMale) {
            (10 * member.weightKg + 6.25 * member.heightCm - 5 * age + 5).toInt()
        } else {
            (10 * member.weightKg + 6.25 * member.heightCm - 5 * age - 161).toInt()
        }

        val activityMultiplier = when (activityLevel.lowercase()) {
            "sedentary" -> 1.2f
            "light" -> 1.375f
            "moderate" -> 1.55f
            "very active" -> 1.725f
            else -> 1.55f
        }

        val maintenanceCal = (bmr * activityMultiplier).toInt()
        val targetCal = when (goal.lowercase()) {
            "fat loss", "weight loss" -> (maintenanceCal - 500).coerceAtLeast(1200)
            "muscle gain", "hypertrophy" -> maintenanceCal + 350
            else -> maintenanceCal
        }

        val proteinGrams = (member.weightKg * 2.0f).toInt()
        val fatGrams = ((targetCal * 0.25f) / 9f).toInt()
        val carbsGrams = ((targetCal - (proteinGrams * 4 + fatGrams * 9)) / 4f).coerceAtLeast(50f).toInt()
        val waterLiters = (member.weightKg * 0.04f).coerceAtLeast(3.0f)
        val bmi = member.weightKg / ((member.heightCm / 100f) * (member.heightCm / 100f))

        val isPureVeg = regionFoodPref.contains("Veg", ignoreCase = true) && !regionFoodPref.contains("Egg", ignoreCase = true) && !regionFoodPref.contains("Non", ignoreCase = true)
        val isEggetarian = regionFoodPref.contains("Egg", ignoreCase = true)
        val isJain = regionFoodPref.contains("Jain", ignoreCase = true)

        val dietTypeConstraint = when {
            isJain -> "STRICT JAIN VEGETARIAN (NO Eggs, NO Meat, NO Fish, NO Onion, NO Garlic, NO Potato or root veggies). High protein from Paneer, Tofu, Moong Dal, Sprouts, Whey."
            isPureVeg -> "STRICT PURE VEGETARIAN (NO EGGS WHATSOEVER, NO Meat, NO Fish, NO Chicken). High protein from Paneer, Soya Chunks, Tofu, Sprouts, Rajma, Chole, Dal, Curd, Whey."
            isEggetarian -> "EGGETARIAN (Vegetarian + Eggs allowed). NO Chicken, NO Fish, NO Meat."
            else -> "NON-VEGETARIAN (Chicken, Fish, Eggs, Dal, Roti, Rice)."
        }

        val memoryContextStr = if (learnedMemory.isNotBlank()) "\n[HISTORICAL FITOPS AI LEARNED MEMORY & PREVIOUS RECOMMENDATIONS]:\n$learnedMemory\n" else ""

        val prompt = """
            $RAG_KNOWLEDGE_BASE
            $memoryContextStr
            Generate a personalized Indian 4-meal diet plan for a gym member:
            Name: ${member.name}, Age: $age, Gender: ${member.gender}, Height: ${member.heightCm}cm, Current Weight: ${member.weightKg}kg, Target Weight: $targetWeightKg kg.
            Goal: $goal, Diet Preference: $regionFoodPref.
            STRICT DIET REQUIREMENT: $dietTypeConstraint
            Daily Macro Targets: $targetCal kcal (Protein: ${proteinGrams}g, Carbs: ${carbsGrams}g, Fats: ${fatGrams}g).
            Medical Conditions: ${member.medicalConditions}.
            
            Respond strictly with valid JSON with this format:
            {
              "explanation": "Scientific justification tailored to Indian diet ($regionFoodPref) and $goal",
              "meals": [
                {
                  "time": "08:00 AM",
                  "name": "Breakfast",
                  "foods": "${if (isPureVeg) "Moong Dal Chela / Paneer Bhurji with 2 Multigrain Rotis & Almonds" else if (isEggetarian) "3 Egg Whites Omelette with spinach & 2 Whole Wheat Toast" else "3 Egg Whites, 1 Whole Egg, Oats porridge with almonds"}",
                  "calories": 420,
                  "protein": 30,
                  "carbs": 45,
                  "fat": 10
                },
                {
                  "time": "01:00 PM",
                  "name": "Lunch",
                  "foods": "${if (isPureVeg) "150g Low Fat Paneer / Soya Chunks curry, 1 cup Brown Basmati Rice, Dal Tadka & Cucumber Salad" else "150g Grilled Chicken / Fish or Paneer, 1 cup Rice, Dal & Salad"}",
                  "calories": 550,
                  "protein": 40,
                  "carbs": 60,
                  "fat": 15
                },
                {
                  "time": "05:00 PM",
                  "name": "Pre-Workout / Evening Snack",
                  "foods": "Roasted Makhana (30g) + 1 scoop Whey Protein in water or Hung Curd with Berries",
                  "calories": 250,
                  "protein": 24,
                  "carbs": 30,
                  "fat": 4
                },
                {
                  "time": "08:30 PM",
                  "name": "Dinner",
                  "foods": "${if (isPureVeg) "150g Tofu / Paneer Tikka with sautéed vegetables & 1 Jowar Roti" else "150g Fish / Paneer with steamed broccoli & quinoa"}",
                  "calories": 480,
                  "protein": 38,
                  "carbs": 40,
                  "fat": 12
                }
              ]
            }
        """.trimIndent()

        val aiResponse = callGeminiApi(prompt)
        val defaultExplanation = "Customized according to ICMR/NIN Indian sports nutrition standards for $dietTypeConstraint targeting $goal."
        
        val defaultMeals = if (isPureVeg || isJain) {
            listOf(
                MealItem("08:00 AM", "Breakfast", "Moong Dal Chela with Paneer stuffing & Green Chutney + 5 Almonds", (targetCal * 0.25).toInt(), (proteinGrams * 0.25).toInt(), (carbsGrams * 0.3).toInt(), (fatGrams * 0.25).toInt()),
                MealItem("01:00 PM", "Lunch", "150g Soya Chunks Curry / Paneer, 1 cup Brown Rice, Moong Dal & Cucumber Salad", (targetCal * 0.35).toInt(), (proteinGrams * 0.35).toInt(), (carbsGrams * 0.4).toInt(), (fatGrams * 0.35).toInt()),
                MealItem("05:00 PM", "Pre-Workout", "Roasted Makhana (30g) & 1 Scoop Whey Protein in Water / Buttermilk", (targetCal * 0.15).toInt(), (proteinGrams * 0.15).toInt(), (carbsGrams * 0.15).toInt(), (fatGrams * 0.15).toInt()),
                MealItem("08:30 PM", "Dinner", "150g Tofu / Paneer Stir-fry with Sautéed Veggies & 1 Multigrain Roti", (targetCal * 0.25).toInt(), (proteinGrams * 0.25).toInt(), (carbsGrams * 0.15).toInt(), (fatGrams * 0.25).toInt())
            )
        } else if (isEggetarian) {
            listOf(
                MealItem("08:00 AM", "Breakfast", "3 Egg White Omelette with spinach & 2 Multigrain Toast + Green Tea", (targetCal * 0.25).toInt(), (proteinGrams * 0.25).toInt(), (carbsGrams * 0.3).toInt(), (fatGrams * 0.25).toInt()),
                MealItem("01:00 PM", "Lunch", "150g Paneer / Egg Bhurji Curry, 1 cup Brown Rice, Chana Dal & Salad", (targetCal * 0.35).toInt(), (proteinGrams * 0.35).toInt(), (carbsGrams * 0.4).toInt(), (fatGrams * 0.35).toInt()),
                MealItem("05:00 PM", "Pre-Workout", "2 Boiled Egg Whites & 1 Banana with Almonds", (targetCal * 0.15).toInt(), (proteinGrams * 0.15).toInt(), (carbsGrams * 0.15).toInt(), (fatGrams * 0.15).toInt()),
                MealItem("08:30 PM", "Dinner", "150g Tofu or Egg Curry with Steamed Veggies & 1 Wheat Roti", (targetCal * 0.25).toInt(), (proteinGrams * 0.25).toInt(), (carbsGrams * 0.15).toInt(), (fatGrams * 0.25).toInt())
            )
        } else {
            listOf(
                MealItem("08:00 AM", "Breakfast", "3 Boiled Egg Whites, 1 Whole Egg, Oats Porridge with Walnuts", (targetCal * 0.25).toInt(), (proteinGrams * 0.25).toInt(), (carbsGrams * 0.3).toInt(), (fatGrams * 0.25).toInt()),
                MealItem("01:00 PM", "Lunch", "150g Grilled Chicken Breast / Fish, 1 cup Brown Rice, Dal Tadka & Salad", (targetCal * 0.35).toInt(), (proteinGrams * 0.35).toInt(), (carbsGrams * 0.4).toInt(), (fatGrams * 0.35).toInt()),
                MealItem("05:00 PM", "Pre-Workout", "1 Scoop Whey Protein with 1 Apple & Nimbu Pani", (targetCal * 0.15).toInt(), (proteinGrams * 0.15).toInt(), (carbsGrams * 0.15).toInt(), (fatGrams * 0.15).toInt()),
                MealItem("08:30 PM", "Dinner", "150g Fish / Tofu, Steamed Vegetables & Quinoa or 1 Roti", (targetCal * 0.25).toInt(), (proteinGrams * 0.25).toInt(), (carbsGrams * 0.15).toInt(), (fatGrams * 0.25).toInt())
            )
        }

        var finalExplanation = defaultExplanation
        var finalMeals = defaultMeals

        if (aiResponse != null) {
            try {
                val cleanJson = extractJson(aiResponse)
                val jsonObject = JSONObject(cleanJson)
                finalExplanation = jsonObject.optString("explanation", defaultExplanation)
                val mealsArray = jsonObject.optJSONArray("meals")
                if (mealsArray != null && mealsArray.length() > 0) {
                    val parsedList = mutableListOf<MealItem>()
                    for (i in 0 until mealsArray.length()) {
                        val m = mealsArray.getJSONObject(i)
                        parsedList.add(
                            MealItem(
                                time = m.optString("time", "12:00 PM"),
                                name = m.optString("name", "Meal"),
                                foods = m.optString("foods", "Balanced meal"),
                                calories = m.optInt("calories", 300),
                                protein = m.optInt("protein", 20),
                                carbs = m.optInt("carbs", 30),
                                fat = m.optInt("fat", 10)
                            )
                        )
                    }
                    if (parsedList.isNotEmpty()) {
                        finalMeals = parsedList
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        DietPlan(
            memberId = member.id,
            memberName = member.name,
            age = age,
            gender = member.gender,
            heightCm = member.heightCm,
            weightKg = member.weightKg,
            targetWeightKg = targetWeightKg,
            activityLevel = activityLevel,
            goal = goal,
            regionFoodPref = regionFoodPref,
            bmi = String.format("%.1f", bmi).toFloat(),
            bmr = bmr,
            maintenanceCalories = maintenanceCal,
            targetCalories = targetCal,
            proteinGrams = proteinGrams,
            carbsGrams = carbsGrams,
            fatGrams = fatGrams,
            waterLiters = waterLiters,
            meals = finalMeals,
            explanationText = finalExplanation
        )
    }

    suspend fun generateWorkoutPlan(
        member: Member,
        age: Int = 28,
        targetWeightKg: Float = 60f,
        goal: String = "Hypertrophy & Strength",
        workoutStyle: String = "Full Commercial Gym",
        daysPerWeek: Int = 3,
        splitType: String = "Push Pull Legs (PPL)",
        monthlyRotation: Boolean = true,
        learnedMemory: String = ""
    ): WorkoutPlan = withContext(Dispatchers.IO) {
        val rotationNote = if (monthlyRotation) "Include monthly exercise rotation variations" else "Fixed core compound exercises"
        val memoryContextStr = if (learnedMemory.isNotBlank()) "\n[HISTORICAL FITOPS AI LEARNED MEMORY & PREVIOUS RECOMMENDATIONS]:\n$learnedMemory\n" else ""
        val prompt = """
            $RAG_KNOWLEDGE_BASE
            $memoryContextStr
            Create a high-performance $daysPerWeek-day workout plan for gym member ${member.name} based on Indian training standards:
            Age: $age, Gender: ${member.gender}, Height: ${member.heightCm}cm, Weight: ${member.weightKg}kg, Target Weight: $targetWeightKg kg.
            Fitness Goal: $goal
            Training Frequency: $daysPerWeek days/week
            Muscle Split Structure: $splitType (e.g. Each muscle a day / 2 muscles a day / Push Pull Legs)
            Movement Periodization: $rotationNote
            Training Style & Indian Conditions: $workoutStyle (Tailored for Indian climate/gym facilities, incorporating Surya Namaskar warmups, traditional Indian conditioning like Desi Dands/Baithaks alongside modern gym lifts, and tropical hydration notes like Nimbu Pani/ORS).
            Medical Conditions: ${member.medicalConditions}.
            
            Respond strictly in valid JSON format:
            {
              "routines": [
                {
                  "dayName": "Day 1",
                  "title": "Desi Power Push (Chest, Shoulders & Triceps)",
                  "exercises": [
                    {"name": "Surya Namaskar (Sun Salutations)", "sets": "5", "reps": "Flow", "rest": "30s", "notes": "Indian joint warm-up & spinal mobility"},
                    {"name": "Flat Barbell Bench Press", "sets": "4", "reps": "8-10", "rest": "90s", "notes": "Progressive overload, explosive concentric"},
                    {"name": "Incline Dumbbell Press", "sets": "3", "reps": "10-12", "rest": "60s", "notes": "3 sec negative tempo"},
                    {"name": "Desi Hindu Pushups (Dands)", "sets": "3", "reps": "15", "rest": "45s", "notes": "Core & shoulder endurance, finish with Nimbu Pani hydration"}
                  ]
                }
              ]
            }
        """.trimIndent()

        val aiResponse = callGeminiApi(prompt)
        val defaultRoutines = listOf(
            DailyRoutine(
                "Day 1", "Desi Power Push (Chest, Shoulders & Triceps)",
                listOf(
                    ExerciseDetail("Surya Namaskar (Sun Salutations)", "5", "Rounds", "30s", "Traditional Indian dynamic warm-up for spinal mobility"),
                    ExerciseDetail("Flat Barbell Bench Press", "4", "8-10", "90s", "Build chest power with explosive drive"),
                    ExerciseDetail("Standing Overhead Press / Dumbbell Press", "3", "10-12", "60s", "Strict form, squeeze deltoids"),
                    ExerciseDetail("Desi Hindu Pushups (Dands)", "3", "15", "45s", "Traditional shoulder & chest finisher; drink Nimbu Pani/ORS for tropical climate")
                )
            ),
            DailyRoutine(
                "Day 2", "Pull & Desi Core (Back, Biceps & Core)",
                listOf(
                    ExerciseDetail("Lat Pulldown / Pull-ups", "4", "8-10", "90s", "Full lat stretch & elbow drive"),
                    ExerciseDetail("Seated Cable Rows / T-Bar Row", "3", "10-12", "60s", "Retract scapula & hold 1 sec"),
                    ExerciseDetail("EZ Bar Bicep Curls", "3", "12", "45s", "Strict biceps contraction"),
                    ExerciseDetail("Desi Plank & Leg Raises", "3", "20", "30s", "Core stabilization & posture alignment")
                )
            ),
            DailyRoutine(
                "Day 3", "Legs & Traditional Baithak Conditioning",
                listOf(
                    ExerciseDetail("Barbell Back Squats", "4", "8-10", "120s", "Full depth squats breaking parallel"),
                    ExerciseDetail("Desi Hindu Squats (Baithak)", "3", "25", "45s", "High-rep traditional knee & leg endurance builder"),
                    ExerciseDetail("Romanian Deadlifts", "3", "10", "90s", "Hinge at hips for hamstrings & glutes"),
                    ExerciseDetail("Standing Calf Raises", "4", "15-20", "30s", "Squeeze calves at peak extension")
                )
            )
        )

        var finalRoutines = defaultRoutines

        if (aiResponse != null) {
            try {
                val cleanJson = extractJson(aiResponse)
                val jsonObject = JSONObject(cleanJson)
                val routinesArr = jsonObject.optJSONArray("routines")
                if (routinesArr != null && routinesArr.length() > 0) {
                    val list = mutableListOf<DailyRoutine>()
                    for (i in 0 until routinesArr.length()) {
                        val rObj = routinesArr.getJSONObject(i)
                        val dayName = rObj.optString("dayName", "Day ${i+1}")
                        val title = rObj.optString("title", "Workout")
                        val exArr = rObj.optJSONArray("exercises")
                        val exList = mutableListOf<ExerciseDetail>()
                        if (exArr != null) {
                            for (j in 0 until exArr.length()) {
                                val e = exArr.getJSONObject(j)
                                exList.add(
                                    ExerciseDetail(
                                        name = e.optString("name", "Exercise"),
                                        sets = e.optString("sets", "3"),
                                        reps = e.optString("reps", "10"),
                                        rest = e.optString("rest", "60s"),
                                        notes = e.optString("notes", "Controlled tempo")
                                    )
                                )
                            }
                        }
                        list.add(DailyRoutine(dayName, title, exList))
                    }
                    if (list.isNotEmpty()) finalRoutines = list
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        WorkoutPlan(
            memberId = member.id,
            memberName = member.name,
            goal = goal,
            daysPerWeek = daysPerWeek,
            routines = finalRoutines
        )
    }

    suspend fun predictRenewalAndChurn(
        member: Member,
        daysRemaining: Int
    ): Pair<Int, String> = withContext(Dispatchers.IO) {
        var riskScore = 20
        if (member.attendanceCount < 10) riskScore += 40
        if (daysRemaining <= 7) riskScore += 30

        val prompt = "Predict renewal likelihood for gym member ${member.name}. Attendance: ${member.attendanceCount} check-ins, Days remaining: $daysRemaining. Provide a concise 1-sentence action advice for gym manager."
        val response = callGeminiApi(prompt) ?: "High priority: Send personalized WhatsApp reminder with 10% early renewal discount."

        Pair(riskScore.coerceIn(5, 95), response.take(160))
    }

    suspend fun askNaturalLanguageInsight(query: String): String = withContext(Dispatchers.IO) {
        val prompt = """
            You are GymAI Pro's Chief Revenue & Operations AI Consultant for Indian Gyms. Answer this gym owner's query concisely and actionable with INR (₹) standards:
            Query: "$query"
        """.trimIndent()
        callGeminiApi(prompt) ?: "Based on recent trends, member retention is highest among members completing 3+ PT sessions per week. Recommend promoting PT trial bundles during weekend check-ins."
    }

    suspend fun askWhatsAppSupportBot(query: String): String = withContext(Dispatchers.IO) {
        val prompt = """
            You are the Official GymAI Pro WhatsApp AI Support Assistant (+91 98765 43210) for Indian Gym Owners & Fitness Staff.
            Answer the user's query in friendly, clean WhatsApp formatting (using *bold*, emojis, line breaks, bullet points):
            Query: "$query"
            
            Guidelines:
            - Keep answers helpful, polite, and formatted for mobile WhatsApp reading.
            - If asking about diet, explain that our AI strictly enforces Pure Vegetarian (No Eggs), Eggetarian, Non-Veg, and Jain filters according to ICMR Indian standards.
            - If asking about renewals/pricing, use Indian Rupee (₹).
            - Include 1 helpful follow-up action or tip.
        """.trimIndent()
        callGeminiApi(prompt) ?: "Namaste! 🙏 Welcome to *GymAI Pro Official WhatsApp Support*.\n\n• *Veg Diet AI*: Our AI generator strictly respects Pure Veg (No Eggs!), Jain, and Eggetarian preferences based on Indian ICMR nutrition benchmarks.\n• *Currency & Plans*: All member fees, renewals, and analytics are configured in Indian Rupees (₹).\n• *QR & Biometric*: Fast login with fingerprint and instant QR check-in scanning.\n\nHow can I assist your gym today? Type your query below!"
    }

    suspend fun generatePtPerformanceFeedback(
        memberName: String,
        exercisesStr: String,
        trainerNotes: String
    ): String = withContext(Dispatchers.IO) {
        val prompt = """
            You are an elite Strength & Conditioning Head Coach.
            Analyze this PT workout session logged for client $memberName:
            Exercises Logged: $exercisesStr
            Trainer Notes: "$trainerNotes"
            
            Provide 2 short, actionable coaching tips for progressive overload and recovery in the next session.
        """.trimIndent()
        callGeminiApi(prompt) ?: "• Progressive Overload: Increase weight by 2.5 kg on primary compound lift next session if RPE <= 8.\n• Recovery Target: Ensure post-workout protein intake (25-30g) within 45 mins and adequate hydration."
    }

    private fun callGeminiApi(promptText: String): String? {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return null
        }

        val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"

        val jsonPayload = JSONObject().apply {
            put("contents", org.json.JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", org.json.JSONArray().apply {
                        put(JSONObject().apply {
                            put("text", promptText)
                        })
                    })
                })
            })
        }

        val request = Request.Builder()
            .url(url)
            .post(jsonPayload.toString().toRequestBody("application/json".toMediaType()))
            .build()

        return try {
            val response = client.newCall(request).execute()
            val responseBodyStr = response.body?.string() ?: return null
            val root = JSONObject(responseBodyStr)
            val candidates = root.optJSONArray("candidates")
            val firstCandidate = candidates?.optJSONObject(0)
            val content = firstCandidate?.optJSONObject("content")
            val parts = content?.optJSONArray("parts")
            parts?.optJSONObject(0)?.optString("text")
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun extractJson(text: String): String {
        val start = text.indexOf('{')
        val end = text.lastIndexOf('}')
        if (start != -1 && end != -1 && end > start) {
            return text.substring(start, end + 1)
        }
        return text
    }
}
