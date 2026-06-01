package io.github.xiaoancute.englisheasy.data.learning

enum class LearningState(val storageValue: String) {
    UNLEARNED("UNLEARNED"),
    LEARNING("LEARNING"),
    MASTERED("MASTERED"),
    SKIPPED("SKIPPED"),
    ;

    companion object {
        val progressStates = listOf(LEARNING, MASTERED)
        val blockingStates = listOf(LEARNING, MASTERED, SKIPPED)

        fun storageValues(states: Iterable<LearningState>): List<String> {
            return states.map { it.storageValue }
        }

        fun fromStorageValue(value: String): LearningState {
            return values().firstOrNull { it.storageValue == value } ?: UNLEARNED
        }
    }
}
