package io.github.xiaoancute.englisheasy.data.model

import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class ConceptCardValidationTest {

    @Test
    fun singleCardRequiresCoreConceptPictureAndAnchor() {
        val card = ConceptCard(
            word = "spring",
            entryType = EntryType.WORD,
            coreConcept = CoreConcept(
                picture = "突然向上释放的力量",
                anchorWord = "burst",
            ),
            chineseApproximation = "中文没有完全对应词",
            scenarios = emptyList(),
            misconceptions = emptyList(),
            branches = null,
            promptVersion = 3,
        )

        card.validateStructure()
    }

    @Test
    fun singleCardWithoutCoreConceptFails() {
        val card = ConceptCard(
            word = "spring",
            entryType = EntryType.WORD,
            coreConcept = null,
            branches = null,
            promptVersion = 3,
        )

        val error = assertFailsWith<IllegalArgumentException> {
            card.validateStructure()
        }
        assertTrue(error.message!!.contains("coreConcept"))
    }

    @Test
    fun blankPictureFails() {
        val card = ConceptCard(
            word = "spring",
            coreConcept = CoreConcept(picture = "  ", anchorWord = "burst"),
            promptVersion = 3,
        )

        val error = assertFailsWith<IllegalArgumentException> {
            card.validateStructure()
        }
        assertTrue(error.message!!.contains("picture"))
    }

    @Test
    fun branchCardValidatesChildren() {
        val leaf = ConceptCard(
            word = "bank",
            coreConcept = CoreConcept(picture = "河边的陆地", anchorWord = "side"),
            promptVersion = 3,
        )
        val card = ConceptCard(
            word = "bank",
            coreConcept = null,
            branches = listOf(
                Branch(type = BranchType.HOMONYM, card = leaf),
            ),
            promptVersion = 3,
        )

        card.validateStructure()
    }

    @Test
    fun branchWithInvalidChildFails() {
        val badLeaf = ConceptCard(
            word = "bank",
            coreConcept = null,
            branches = null,
            promptVersion = 3,
        )
        val card = ConceptCard(
            word = "bank",
            branches = listOf(
                Branch(type = BranchType.HOMONYM, card = badLeaf),
            ),
            promptVersion = 3,
        )

        val error = assertFailsWith<IllegalArgumentException> {
            card.validateStructure()
        }
        assertTrue(error.message!!.contains("branches[0]"))
    }
}
