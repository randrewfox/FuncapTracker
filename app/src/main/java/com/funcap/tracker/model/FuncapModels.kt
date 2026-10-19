package com.funcap.tracker.model

import kotlin.math.roundToInt

/**
 * The two validated FUNCAP questionnaire variants (Sommerfelt et al., 2024):
 * - FUNCAP55: Full 55-item questionnaire for comprehensive diagnostic/disability assessment
 * - FUNCAP27: Shortened 27-item subset designed for clinical follow-up and longitudinal tracking
 */
enum class TestType(
    val displayName: String,
    val questionCount: Int,
    val subtitle: String
) {
    FUNCAP55(
        displayName = "FUNCAP55",
        questionCount = 55,
        subtitle = "Comprehensive 55-item functional capacity evaluation"
    ),
    FUNCAP27(
        displayName = "FUNCAP27",
        questionCount = 27,
        subtitle = "Short 27-item monthly check-in & follow-up questionnaire"
    )
}

/**
 * Theme preference modes with explicit Light and Dark toggle options in User Settings.
 */
enum class ThemeMode(val label: String) {
    LIGHT("Light Mode"),
    DARK("Dark Mode"),
    SYSTEM("System Default")
}

/**
 * Official 8 functional capacity subscales (Domains A through H) in FUNCAP55 and FUNCAP27.
 * Includes validation study reference means from Sommerfelt et al. (2024), J. Clin. Med. 13(12):3486.
 */
enum class FuncapDomain(
    val code: String,
    val title: String,
    val shortTitle: String,
    val mecfsStudyMean: Double,
    val healthyControlMean: Double
) {
    A(
        code = "A",
        title = "Personal hygiene / basic functions",
        shortTitle = "Hygiene & Basic",
        mecfsStudyMean = 4.5,
        healthyControlMean = 6.0
    ),
    B(
        code = "B",
        title = "Walking – moving around",
        shortTitle = "Walking & Moving",
        mecfsStudyMean = 3.1,
        healthyControlMean = 5.9
    ),
    C(
        code = "C",
        title = "Being upright",
        shortTitle = "Being Upright",
        mecfsStudyMean = 4.1,
        healthyControlMean = 5.9
    ),
    D(
        code = "D",
        title = "Activities in the home",
        shortTitle = "Home Activities",
        mecfsStudyMean = 2.2,
        healthyControlMean = 5.7
    ),
    E(
        code = "E",
        title = "Communication",
        shortTitle = "Communication",
        mecfsStudyMean = 3.3,
        healthyControlMean = 5.8
    ),
    F(
        code = "F",
        title = "Activities outside your home",
        shortTitle = "Outside Home",
        mecfsStudyMean = 2.7,
        healthyControlMean = 5.8
    ),
    G(
        code = "G",
        title = "Reactions to light and sound",
        shortTitle = "Light & Sound",
        mecfsStudyMean = 3.4,
        healthyControlMean = 5.8
    ),
    H(
        code = "H",
        title = "Concentration",
        shortTitle = "Concentration",
        mecfsStudyMean = 3.2,
        healthyControlMean = 5.8
    )
}

/**
 * Official 0–6 activity consequence rating scale for every FUNCAP item.
 */
data class ScoreOption(
    val value: Int,
    val shortLabel: String,
    val fullDescription: String
)

/**
 * Represents a single question in the FUNCAP questionnaires.
 * Because all 27 items in FUNCAP27 are a subset of FUNCAP55, each question stores its
 * canonical FUNCAP55 number (1..55) and its optional FUNCAP27 number (1..27).
 */
data class FuncapQuestion(
    val id: Int, // Canonical 1..55 ID
    val domain: FuncapDomain,
    val funcap55Number: Int,
    val funcap27Number: Int?, // Non-null if part of FUNCAP27
    val text: String
) {
    fun displayNumber(testType: TestType): Int =
        if (testType == TestType.FUNCAP27) (funcap27Number ?: funcap55Number) else funcap55Number
}

/**
 * Complete catalog of all 55 official FUNCAP55 questions and all 27 official FUNCAP27 questions,
 * along with the official scoring engine.
 */
object FuncapCatalog {

    const val INSTRUCTIONS_HEADING =
        "What are the consequences for you if you perform the activities described below? " +
        "To what extent does this affect how much else you can do?"

    const val INSTRUCTIONS_BODY =
        "No days are the same. Base your response on an average day during the last month – not the worst nor the best. " +
        "If a question concerns an activity that you have not performed (such as showering while seated because you always shower standing up), " +
        "score as you think this activity would have affected you. Items described include necessary activities to perform them " +
        "(for example, 'Going to a shop for groceries' includes getting dressed and travelling)."

    val SCORE_OPTIONS: List<ScoreOption> = listOf(
        ScoreOption(
            value = 0,
            shortLabel = "Cannot do",
            fullDescription = "I cannot do this."
        ),
        ScoreOption(
            value = 1,
            shortLabel = "≥3 days severe",
            fullDescription = "My capacity will be severely reduced for at least three days."
        ),
        ScoreOption(
            value = 2,
            shortLabel = "Same + 1–2 days",
            fullDescription = "I can do little else on the same day and for one to two days afterwards."
        ),
        ScoreOption(
            value = 3,
            shortLabel = "Little else same day",
            fullDescription = "I can do little else on the same day."
        ),
        ScoreOption(
            value = 4,
            shortLabel = "Limit same day",
            fullDescription = "I must limit other activities on the same day."
        ),
        ScoreOption(
            value = 5,
            shortLabel = "Rarely affects",
            fullDescription = "This rarely affects other activities."
        ),
        ScoreOption(
            value = 6,
            shortLabel = "Unproblematic",
            fullDescription = "Unproblematic – does not affect other activities."
        )
    )

    val ALL_QUESTIONS: List<FuncapQuestion> = listOf(
        // Domain A: Personal hygiene / basic functions (Items 1-7 in FUNCAP55; 1, 5, 7 in FUNCAP27)
        FuncapQuestion(1, FuncapDomain.A, 1, 1, "Using the toilet (not bedpan or bedside commode)"),
        FuncapQuestion(2, FuncapDomain.A, 2, null, "Brushing your teeth without assistance"),
        FuncapQuestion(3, FuncapDomain.A, 3, null, "Showering seated, with assistance"),
        FuncapQuestion(4, FuncapDomain.A, 4, null, "Showering seated, without assistance"),
        FuncapQuestion(5, FuncapDomain.A, 5, 2, "Showering standing up"),
        FuncapQuestion(6, FuncapDomain.A, 6, null, "Getting up and staying out of bed for approx. 1 hour"),
        FuncapQuestion(7, FuncapDomain.A, 7, 3, "Getting dressed in regular clothes"),

        // Domain B: Walking – moving around (Items 8-14 in FUNCAP55; 8, 10, 13 in FUNCAP27)
        FuncapQuestion(8, FuncapDomain.B, 8, 4, "Walking a short distance indoors, from one room to another"),
        FuncapQuestion(9, FuncapDomain.B, 9, null, "Walking a short continuous distance, approx. 100 m (length of a football field), in or outdoors"),
        FuncapQuestion(10, FuncapDomain.B, 10, 5, "Walking between approx. 100 m and 1 km on level ground (length of 1 to 10 football fields)"),
        FuncapQuestion(11, FuncapDomain.B, 11, null, "Going for a longer walk, approx. 1 km (0.6 miles), mostly level ground"),
        FuncapQuestion(12, FuncapDomain.B, 12, null, "Going for a longer walk, approx. 1 km (0.6 miles), hilly or varied terrain"),
        FuncapQuestion(13, FuncapDomain.B, 13, 6, "Physical activity with increased heart rate, for approx. 15 min"),
        FuncapQuestion(14, FuncapDomain.B, 14, null, "Physical activity with increased heart rate, for approx. ½ hour"),

        // Domain C: Being upright (Items 15-19 in FUNCAP55; 15, 17, 18 in FUNCAP27)
        FuncapQuestion(15, FuncapDomain.C, 15, 7, "Sitting in bed for approx. ½ hour"),
        FuncapQuestion(16, FuncapDomain.C, 16, null, "Sitting in an upright chair (dining chair) with feet on floor for approx. 10 minutes"),
        FuncapQuestion(17, FuncapDomain.C, 17, 8, "Sitting in an upright chair (dining chair) with feet on floor for approx. 2 hours"),
        FuncapQuestion(18, FuncapDomain.C, 18, 9, "Standing up for approx. 5 minutes, e.g. while queuing or while cooking"),
        FuncapQuestion(19, FuncapDomain.C, 19, null, "Standing up for a long time, approx. ½ hour"),

        // Domain D: Activities in the home (Items 20-25 in FUNCAP55; 21, 25 in FUNCAP27)
        FuncapQuestion(20, FuncapDomain.D, 20, null, "Light housework (dusting, tidying etc.) for approx. ½ hour continuously"),
        FuncapQuestion(21, FuncapDomain.D, 21, 10, "Heavier housework (washing floors, vacuuming etc.) for approx. ½ hour continuously"),
        FuncapQuestion(22, FuncapDomain.D, 22, null, "Laundry (sorting, hanging up to dry and folding)"),
        FuncapQuestion(23, FuncapDomain.D, 23, null, "Making a simple cold meal, such as a sandwich or cereal"),
        FuncapQuestion(24, FuncapDomain.D, 24, null, "Cooking a simple hot meal"),
        FuncapQuestion(25, FuncapDomain.D, 25, 11, "Cooking a complicated meal from scratch, approx. 1 hour of preparation"),

        // Domain E: Communication (Items 26-32 in FUNCAP55; 27, 30, 32 in FUNCAP27)
        FuncapQuestion(26, FuncapDomain.E, 26, null, "Speaking a few words"),
        FuncapQuestion(27, FuncapDomain.E, 27, 12, "Having a conversation for approx. 5 minutes"),
        FuncapQuestion(28, FuncapDomain.E, 28, null, "Having a conversation for approx. ½ hour"),
        FuncapQuestion(29, FuncapDomain.E, 29, null, "Writing a short message by hand"),
        FuncapQuestion(30, FuncapDomain.E, 30, 13, "Participating in a conversation with three people for approx. ½ hour"),
        FuncapQuestion(31, FuncapDomain.E, 31, null, "Socializing with friends for approx. 1 hour"),
        FuncapQuestion(32, FuncapDomain.E, 32, 14, "Participating in a dinner party, party or family event"),

        // Domain F: Activities outside your home (Items 33-39 in FUNCAP55; 33, 35, 38, 39 in FUNCAP27)
        FuncapQuestion(33, FuncapDomain.F, 33, 15, "Stepping right outside your home"),
        FuncapQuestion(34, FuncapDomain.F, 34, null, "Going on a necessary errand, such as a doctor's appointment"),
        FuncapQuestion(35, FuncapDomain.F, 35, 16, "Going to a shop for groceries"),
        FuncapQuestion(36, FuncapDomain.F, 36, null, "Doing enjoyable leisure activities, such as going to a cafe, non-essential shopping etc."),
        FuncapQuestion(37, FuncapDomain.F, 37, null, "Riding as a passenger in a car for approx. 15 minutes"),
        FuncapQuestion(38, FuncapDomain.F, 38, 17, "Using public transport (bus or train)"),
        FuncapQuestion(39, FuncapDomain.F, 39, 18, "Participating in organized leisure activities such as classes, sports etc."),

        // Domain G: Reactions to light and sound (Items 40-45 in FUNCAP55; 41, 42, 44 in FUNCAP27)
        FuncapQuestion(40, FuncapDomain.G, 40, null, "Staying in a room with dim lighting for approx. ½ hour"),
        FuncapQuestion(41, FuncapDomain.G, 41, 19, "Staying in a room with normal lighting, without sunglasses, for approx. 1 hour"),
        FuncapQuestion(42, FuncapDomain.G, 42, 20, "Staying outdoors in daylight without sunglasses for approx. 2 hours"),
        FuncapQuestion(43, FuncapDomain.G, 43, null, "Staying in an environment with the sound of a few people in quiet conversation for approx. 1 hour"),
        FuncapQuestion(44, FuncapDomain.G, 44, 21, "Staying in a noisy environment (shopping mall, café or open plan office) for approx. 1 hour"),
        FuncapQuestion(45, FuncapDomain.G, 45, null, "Going to a cinema, concert etc. with high noise levels"),

        // Domain H: Concentration (Items 46-55 in FUNCAP55; 46, 48, 51, 53, 54, 55 in FUNCAP27)
        FuncapQuestion(46, FuncapDomain.H, 46, 22, "Reading a short text, such as a mobile phone text message"),
        FuncapQuestion(47, FuncapDomain.H, 47, null, "Reading fiction / light reading"),
        FuncapQuestion(48, FuncapDomain.H, 48, 23, "Reading and understanding a non-fiction text, such as an official document one A4 page long"),
        FuncapQuestion(49, FuncapDomain.H, 49, null, "Performing simple mental arithmetic"),
        FuncapQuestion(50, FuncapDomain.H, 50, null, "Writing short messages on a smartphone or tablet"),
        FuncapQuestion(51, FuncapDomain.H, 51, 24, "Using social media to stay in touch with others"),
        FuncapQuestion(52, FuncapDomain.H, 52, null, "Watching TV (series, news)"),
        FuncapQuestion(53, FuncapDomain.H, 53, 25, "Focusing on a task for approx. 10 minutes continuously"),
        FuncapQuestion(54, FuncapDomain.H, 54, 26, "Focusing on a task for approx. 2 hours continuously"),
        FuncapQuestion(55, FuncapDomain.H, 55, 27, "Managing a full working day (non-physical work such as office work, classes or lectures)")
    )

    fun questionsFor(testType: TestType): List<FuncapQuestion> =
        when (testType) {
            TestType.FUNCAP55 -> ALL_QUESTIONS
            TestType.FUNCAP27 -> ALL_QUESTIONS.filter { it.funcap27Number != null }
                .sortedBy { it.funcap27Number }
        }

    /**
     * Extracts only the responses for questions that map directly to FUNCAP27.
     */
    fun getMappedFuncap27Responses(itemResponses: Map<Int, Int>): Map<Int, Int> {
        val funcap27QuestionIds = questionsFor(TestType.FUNCAP27).map { it.id }.toSet()
        return itemResponses.filterKeys { it in funcap27QuestionIds }
    }

    /**
     * Computes each domain mean (A..H) and the overall score (mean of the 8 domain means).
     * When a FUNCAP55 test is taken, also computes the equivalent FUNCAP27 score from the 27 subset items.
     */
    fun calculateScores(
        testType: TestType,
        itemResponses: Map<Int, Int>
    ): CalculatedFuncapResult {
        val activeQuestions = questionsFor(testType)
        val domainScores = mutableMapOf<FuncapDomain, Double>()

        for (domain in FuncapDomain.entries) {
            val domainQuestions = activeQuestions.filter { it.domain == domain }
            val values = domainQuestions.mapNotNull { itemResponses[it.id] }
            val avg = if (values.isNotEmpty()) values.average() else 0.0
            domainScores[domain] = avg.roundToTwoDecimals()
        }

        val totalScore = domainScores.values.average().roundToTwoDecimals()

        // Also compute the 27-item subset score so FUNCAP55 and FUNCAP27 logs can be compared side-by-side
        val funcap27Questions = questionsFor(TestType.FUNCAP27)
        val funcap27DomainMeans = FuncapDomain.entries.map { domain ->
            val dq = funcap27Questions.filter { it.domain == domain }
            val vals = dq.mapNotNull { itemResponses[it.id] }
            if (vals.isNotEmpty()) vals.average() else 0.0
        }
        val equivalent27Score = funcap27DomainMeans.average().roundToTwoDecimals()

        return CalculatedFuncapResult(
            testType = testType,
            totalScore = totalScore,
            equivalentFuncap27Score = equivalent27Score,
            domainScores = domainScores
        )
    }

    fun Double.roundToTwoDecimals(): Double =
        (this * 100.0).roundToInt() / 100.0
}

data class CalculatedFuncapResult(
    val testType: TestType,
    val totalScore: Double,
    val equivalentFuncap27Score: Double,
    val domainScores: Map<FuncapDomain, Double>
)
