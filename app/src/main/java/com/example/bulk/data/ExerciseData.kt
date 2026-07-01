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
    val formCheck: String,
    val emoji: String = "🏋️"
)

data class ExerciseGroup(val label: String, val emoji: String = "⚡", val items: List<ExerciseItem>)

val exerciseGroups = listOf(
    ExerciseGroup(
        label = "INCLINE PUSH-UPS", emoji = "🤸",
        items = listOf(
            ExerciseItem(
                name = "Normal Hands", target = 15, key = MuscleKey.CHEST_TRICEPS,
                accent = Color(0xFFef9a7d), emoji = "🤸",
                videoQuery = "incline push up parallette bars proper form",
                achieveMsg = "Great job! Chest and arms activated.",
                tip = "Hands under shoulders",
                title = "Incline Push-Up (Normal Hands)",
                steps = listOf(
                    "Put both bars on something raised — a table, sofa arm, or stair.",
                    "Place hands under shoulders. Keep a straight line from head to feet.",
                    "Bend arms and lower until chest nears the bars. Elbows close to body.",
                    "Push back up with power. Don't let hips drop."
                ),
                barPlacement = "Bars about shoulder width apart (~50 cm). Handles pointing forward, under shoulders.",
                formCheck = "Feel chest and triceps working together. If front shoulder hurts, use a higher surface."
            ),
            ExerciseItem(
                name = "Wide Hands", target = 12, key = MuscleKey.CHEST,
                accent = Color(0xFFf4889a), emoji = "↔️",
                videoQuery = "wide grip push up chest proper form",
                achieveMsg = "Great job! Chest activated.",
                tip = "Hands wider than shoulders",
                title = "Wide Incline Push-Up",
                steps = listOf(
                    "Put bars on a raised surface, wider than shoulders.",
                    "Lower slowly. Let chest drop between bars for a good stretch.",
                    "Push up. Squeeze chest at top.",
                    "Keep body tight — no hip drop."
                ),
                barPlacement = "Bars about 70 cm apart. Turn handles slightly outward for comfortable wrists.",
                formCheck = "Strong stretch in outer chest at bottom. Arms work less than chest. If shoulders dominate, bring hands slightly closer."
            ),
            ExerciseItem(
                name = "Close Hands", target = 12, key = MuscleKey.TRICEPS,
                accent = Color(0xFFf59e6c), emoji = "🙏",
                videoQuery = "close grip push up triceps proper form",
                achieveMsg = "Great job! Triceps activated.",
                tip = "Hands close together",
                title = "Close Hands Incline Push-Up",
                steps = listOf(
                    "Put bars close together near mid-chest.",
                    "Keep elbows close as you lower — touching your sides.",
                    "Push up fully. Feel the triceps engage.",
                    "Go slow. Controlled is better than fast."
                ),
                barPlacement = "Bars about 15–20 cm apart, under mid-chest.",
                formCheck = "Feel it in triceps. If mostly chest or elbows flare, pull elbows closer to body."
            )
        )
    ),
    ExerciseGroup(
        label = "OTHER MUSCLES", emoji = "💪",
        items = listOf(
            ExerciseItem(
                name = "Chest", target = 12, key = MuscleKey.CHEST,
                accent = Color(0xFFf4889a), emoji = "🫀",
                videoQuery = "push up proper form beginner",
                achieveMsg = "Great job! Chest activated.",
                tip = "",
                title = "Push-Up",
                steps = listOf(
                    "Hands on bars, slightly wider than shoulders.",
                    "Lower. Let chest drop between bars. Elbows part way out.",
                    "Push up, squeeze chest at top.",
                    "Too hard? Higher surface. Too easy? Feet on a chair."
                ),
                barPlacement = "Bars slightly wider than shoulders (~60 cm). Handles forward.",
                formCheck = "Chest does the work and feels tight. Keep hips aligned. If hips drop, engage core more."
            ),
            ExerciseItem(
                name = "Back", target = 12, key = MuscleKey.BACK,
                accent = Color(0xFF5fc9b0), emoji = "🦅",
                videoQuery = "superman exercise back proper form",
                achieveMsg = "Great job! Back activated.",
                tip = "",
                title = "Superman & Pull Back",
                steps = listOf(
                    "Lie on stomach. Arms straight out front.",
                    "Lift chest, arms, and legs off floor. Hold 2 seconds.",
                    "Bend arms, pull elbows to hips. Squeeze back.",
                    "Reach forward again. One squeeze = one rep."
                ),
                barPlacement = "No bars needed. If you have a sturdy low table, lie under and pull your chest up to it.",
                formCheck = "Feel the squeeze between shoulder blades. No lower back pain. Keep neck relaxed, look down."
            ),
            ExerciseItem(
                name = "Biceps", target = 10, key = MuscleKey.BICEPS,
                accent = Color(0xFFf0b35e), emoji = "💪",
                videoQuery = "backpack bicep curl home workout",
                achieveMsg = "Great job! Biceps activated.",
                tip = "",
                title = "Bag Curl",
                steps = listOf(
                    "Load a backpack with books for weight.",
                    "Hold bag with both hands, palms up. Elbows close to sides.",
                    "Curl up to chest. Squeeze, then lower slowly.",
                    "No bag? Loop a towel under one foot and pull up against it."
                ),
                barPlacement = "Push-up bars don't target biceps well. Use a heavy bag or towel instead.",
                formCheck = "Front of upper arm should feel tired. Keep elbows still. If they swing, you're using momentum."
            ),
            ExerciseItem(
                name = "Triceps", target = 12, key = MuscleKey.TRICEPS,
                accent = Color(0xFFf59e6c), emoji = "🏆",
                videoQuery = "chair tricep dips proper form",
                achieveMsg = "Great job! Triceps activated.",
                tip = "",
                title = "Chair Dip",
                steps = listOf(
                    "Sit on edge of a sturdy chair. Hold front edge.",
                    "Move hips off chair. Bend arms and lower.",
                    "Push up until arms are straight. Elbows point back.",
                    "Want more? Add close-hand push-ups on bars."
                ),
                barPlacement = "Close-hand push-ups: bars 15–20 cm apart under chest. Chair dips need no bars.",
                formCheck = "Feel triceps working. If shoulders roll forward or hurt, keep chest up and elbows close."
            ),
            ExerciseItem(
                name = "Shoulders", target = 15, key = MuscleKey.SHOULDERS,
                accent = Color(0xFF6db9f0), emoji = "🛡️",
                videoQuery = "pike push up shoulders proper form",
                achieveMsg = "Great job! Shoulders activated.",
                tip = "",
                title = "Pike Push-Up",
                steps = listOf(
                    "Start in push-up on bars. Walk feet in, lift hips high into an inverted V.",
                    "Bend arms, lower top of head toward floor.",
                    "Push up until arms are straight.",
                    "Higher hips = harder for shoulders."
                ),
                barPlacement = "Bars at shoulder width. They give room for your head to go below your hands.",
                formCheck = "Front and sides of shoulders should tire first. Keep hips over hands. If you feel chest, lift hips higher."
            ),
            ExerciseItem(
                name = "Legs", target = 15, key = MuscleKey.LEGS,
                accent = Color(0xFF9a8cf0), emoji = "🦵",
                videoQuery = "bodyweight squat proper form beginner",
                achieveMsg = "Great job! Legs activated.",
                tip = "",
                title = "Squats & Lunges",
                steps = listOf(
                    "Squat: feet shoulder width. Sit back and down until thighs are parallel. Stand up.",
                    "Lunge: step one foot back, drop back knee. Rise. Alternate legs.",
                    "Calf raise: rise on toes. Hold, then lower slowly.",
                    "One rep = one squat, one lunge each leg, or one calf raise."
                ),
                barPlacement = "No bars needed. Hold them for balance during lunges if you like.",
                formCheck = "Thighs should feel warm and tired. Knees over toes. Heels flat during squats."
            ),
            ExerciseItem(
                name = "Abs", target = 20, key = MuscleKey.ABS,
                accent = Color(0xFF5cccdd), emoji = "⚡",
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
                formCheck = "Stomach should shake and tire — not lower back. If back lifts during leg raises, reduce range and press back to floor."
            ),
            ExerciseItem(
                name = "Glutes", target = 15, key = MuscleKey.GLUTES,
                accent = Color(0xFFd98ce0), emoji = "🍑",
                videoQuery = "glute bridge proper form",
                achieveMsg = "Great job! Glutes activated.",
                tip = "",
                title = "Glute Bridge",
                steps = listOf(
                    "Lie on back. Bend knees. Feet flat, close to hips.",
                    "Push through heels, lift hips. Straight line from shoulders to knees.",
                    "Squeeze glutes hard at top for 1 second. Lower slowly.",
                    "Want more? Upper back on a sofa, or single-leg variation."
                ),
                barPlacement = "No bars needed. This move is all about hips and heels.",
                formCheck = "Feel the squeeze in glutes, not lower back. If back takes over, tuck hips and push through heels."
            )
        )
    )
)
