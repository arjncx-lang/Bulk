package com.example.bulk.data

import androidx.compose.ui.graphics.Color

enum class MuscleKey { CHEST, BACK, BICEPS, TRICEPS, SHOULDERS, LEGS, ABS, GLUTES, CHEST_TRICEPS }

data class ExerciseItem(
    val name: String,
    val target: Int,
    val key: MuscleKey,
    val accent: Color,
    val videoQuery: String,
    val achieveMsg: String,
    val tip: String,
    val title: String,
    val steps: List<String>,
    val barPlacement: String,
    val formCheck: String
)

data class ExerciseGroup(val label: String, val items: List<ExerciseItem>)

val exerciseGroups = listOf(
    ExerciseGroup(
        label = "INCLINE PUSH-UPS",
        items = listOf(
            ExerciseItem(
                name = "Normal Hands", target = 15, key = MuscleKey.CHEST_TRICEPS,
                accent = Color(0xFFef9a7d),
                videoQuery = "incline push up parallette bars proper form",
                achieveMsg = "Great job! Chest and arms activated.",
                tip = "Hands under shoulders",
                title = "Incline Push-Up (Normal Hands)",
                steps = listOf(
                    "Put both bars on something raised, like a stair or low ledge.",
                    "Place hands under shoulders. Keep a straight line from head to feet.",
                    "Bend arms and lower until chest nears the bars. Elbows close to body.",
                    "Push back up with power. Don't let hips drop."
                ),
                barPlacement = "Bars about shoulder width apart (~50 cm). Handles pointing forward, under shoulders.",
                formCheck = "Feel chest and triceps working together. If front shoulder hurts, use a higher surface."
            ),
            ExerciseItem(
                name = "Wide Hands", target = 12, key = MuscleKey.CHEST,
                accent = Color(0xFFf4889a),
                videoQuery = "wide grip push up chest proper form",
                achieveMsg = "Great job! Chest activated.",
                tip = "Hands wider than shoulders",
                title = "Wide Incline Push-Up",
                steps = listOf(
                    "Put bars on a raised surface, wider than shoulders.",
                    "Lower slowly. Let chest drop between bars for a good stretch.",
                    "Push up. Squeeze chest at top.",
                    "Keep body tight. No hip drop."
                ),
                barPlacement = "Bars about 70 cm apart. Turn handles slightly outward for comfortable wrists.",
                formCheck = "Strong stretch in outer chest at bottom. Arms work less than chest. If shoulders dominate, bring hands slightly closer."
            ),
            ExerciseItem(
                name = "Close Hands", target = 12, key = MuscleKey.TRICEPS,
                accent = Color(0xFFf59e6c),
                videoQuery = "close grip push up triceps proper form",
                achieveMsg = "Great job! Triceps activated.",
                tip = "Hands close together",
                title = "Close Hands Incline Push-Up",
                steps = listOf(
                    "Put bars close together near mid-chest.",
                    "Keep elbows close as you lower, touching your sides.",
                    "Push up fully. Feel the triceps engage.",
                    "Go slow. Controlled is better than fast."
                ),
                barPlacement = "Bars about 15 to 20 cm apart, under mid-chest.",
                formCheck = "Feel it in triceps. If mostly chest or elbows flare, pull elbows closer to body."
            )
        )
    ),
    ExerciseGroup(
        label = "OTHER MUSCLES",
        items = listOf(
            ExerciseItem(
                name = "Chest", target = 12, key = MuscleKey.CHEST,
                accent = Color(0xFFf4889a),
                videoQuery = "push up proper form beginner",
                achieveMsg = "Great job! Chest activated.",
                tip = "",
                title = "Push-Up",
                steps = listOf(
                    "Hands on bars, slightly wider than shoulders.",
                    "Lower. Let chest drop between bars. Elbows part way out.",
                    "Push up, squeeze chest at top.",
                    "Too hard? Higher surface. Too easy? Slow the lowering to 3 seconds."
                ),
                barPlacement = "Bars slightly wider than shoulders (~60 cm). Handles forward.",
                formCheck = "Chest does the work and feels tight. Keep hips aligned. If hips drop, engage core more."
            ),
            ExerciseItem(
                name = "Back", target = 12, key = MuscleKey.BACK,
                accent = Color(0xFF5fc9b0),
                videoQuery = "superman exercise back proper form",
                achieveMsg = "Great job! Back activated.",
                tip = "",
                title = "Superman & Pull Back",
                steps = listOf(
                    "Lie on stomach on the floor. Arms straight out front.",
                    "Lift chest, arms, and legs off floor. Hold 2 seconds.",
                    "Bend arms, pull elbows to hips. Squeeze back.",
                    "Reach forward again. One squeeze equals one rep."
                ),
                barPlacement = "No bars needed. Just the floor.",
                formCheck = "Feel the squeeze between shoulder blades. No lower back pain. Keep neck relaxed, look down."
            ),
            ExerciseItem(
                name = "Biceps", target = 10, key = MuscleKey.BICEPS,
                accent = Color(0xFFf0b35e),
                videoQuery = "self resistance bicep curl no equipment",
                achieveMsg = "Great job! Biceps activated.",
                tip = "",
                title = "Self-Resistance Curl",
                steps = listOf(
                    "Stand tall. Make a fist with your right hand, palm up.",
                    "Press your left palm down on the right fist.",
                    "Curl the right arm up slowly while resisting hard with the left. Take 3 seconds up.",
                    "Resist on the way down too. Do all reps, then switch arms."
                ),
                barPlacement = "No bars needed. Your other arm is the resistance.",
                formCheck = "Front of upper arm should burn by the last reps. Keep the elbow pinned to your side. Press harder if it feels easy."
            ),
            ExerciseItem(
                name = "Triceps", target = 12, key = MuscleKey.TRICEPS,
                accent = Color(0xFFf59e6c),
                videoQuery = "bodyweight tricep extension push up bars form",
                achieveMsg = "Great job! Triceps activated.",
                tip = "",
                title = "Bar Triceps Extension",
                steps = listOf(
                    "Kneel with bars on the floor a little in front of you.",
                    "Grip the bars, lean forward so elbows bend and head goes past your hands.",
                    "Keep elbows still and press through the hands to straighten arms.",
                    "Move further forward over time to make it harder."
                ),
                barPlacement = "Bars about 15 to 20 cm apart, handles forward, slightly ahead of shoulders.",
                formCheck = "Feel triceps working. If shoulders take over, keep elbows pointing down and body straighter."
            ),
            ExerciseItem(
                name = "Shoulders", target = 15, key = MuscleKey.SHOULDERS,
                accent = Color(0xFF6db9f0),
                videoQuery = "pike push up shoulders proper form",
                achieveMsg = "Great job! Shoulders activated.",
                tip = "",
                title = "Pike Push-Up",
                steps = listOf(
                    "Start in push-up on bars. Walk feet in, lift hips high into an inverted V.",
                    "Bend arms, lower top of head toward floor.",
                    "Push up until arms are straight.",
                    "Higher hips means harder for shoulders."
                ),
                barPlacement = "Bars at shoulder width. They give room for your head to go below your hands.",
                formCheck = "Front and sides of shoulders should tire first. Keep hips over hands. If you feel chest, lift hips higher."
            ),
            ExerciseItem(
                name = "Legs", target = 15, key = MuscleKey.LEGS,
                accent = Color(0xFF9a8cf0),
                videoQuery = "bodyweight squat proper form beginner",
                achieveMsg = "Great job! Legs activated.",
                tip = "",
                title = "Squats & Lunges",
                steps = listOf(
                    "Squat: feet shoulder width. Sit back and down until thighs are parallel. Stand up.",
                    "Lunge: step one foot back, drop back knee. Rise. Alternate legs.",
                    "Calf raise: rise on toes. Hold, then lower slowly.",
                    "One rep equals one squat, one lunge each leg, or one calf raise."
                ),
                barPlacement = "No bars needed. Hold them for balance during lunges if you like.",
                formCheck = "Thighs should feel warm and tired. Knees over toes. Heels flat during squats."
            ),
            ExerciseItem(
                name = "Abs", target = 20, key = MuscleKey.ABS,
                accent = Color(0xFF5cccdd),
                videoQuery = "plank leg raise core workout form",
                achieveMsg = "Great job! Core activated.",
                tip = "",
                title = "Plank & Leg Raise",
                steps = listOf(
                    "Hold bars, body in a straight line. Squeeze core. This is a plank.",
                    "For reps: lie on back. Lift straight legs up, lower slowly. Don't touch floor.",
                    "Want more? Add mountain climbers on the bars.",
                    "Pull stomach in tight throughout."
                ),
                barPlacement = "Bars under shoulders for plank (easier on wrists). Floor is fine for leg raises.",
                formCheck = "Stomach should shake and tire, not lower back. If back lifts during leg raises, reduce range and press back to floor."
            ),
            ExerciseItem(
                name = "Glutes", target = 15, key = MuscleKey.GLUTES,
                accent = Color(0xFFd98ce0),
                videoQuery = "glute bridge proper form",
                achieveMsg = "Great job! Glutes activated.",
                tip = "",
                title = "Glute Bridge",
                steps = listOf(
                    "Lie on back on the floor. Bend knees. Feet flat, close to hips.",
                    "Push through heels, lift hips. Straight line from shoulders to knees.",
                    "Squeeze glutes hard at top for 1 second. Lower slowly.",
                    "Want more? Try the single-leg version, one foot in the air."
                ),
                barPlacement = "No bars needed. This move is all about hips and heels.",
                formCheck = "Feel the squeeze in glutes, not lower back. If back takes over, tuck hips and push through heels."
            )
        )
    )
)
