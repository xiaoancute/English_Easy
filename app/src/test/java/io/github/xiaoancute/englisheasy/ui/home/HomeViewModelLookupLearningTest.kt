package io.github.xiaoancute.englisheasy.ui.home

import io.github.xiaoancute.englisheasy.data.learning.WordLearningStateRepository
import io.github.xiaoancute.englisheasy.data.llm.ConceptRepository
import io.github.xiaoancute.englisheasy.data.llm.ExampleFeedback
import io.github.xiaoancute.englisheasy.data.model.ConceptCard
import io.github.xiaoancute.englisheasy.data.model.CoreConcept
import io.github.xiaoancute.englisheasy.data.model.EntryType
import io.github.xiaoancute.englisheasy.data.settings.ProviderConfig
import io.github.xiaoancute.englisheasy.data.settings.SettingsRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelLookupLearningTest {

    @Test
    fun studyLookupSuccessStartsLearningAfterCardOpens() = runHomeViewModelTest {
        val fixture = Fixture()
        val viewModel = fixture.viewModel()

        viewModel.lookup(" Spring ", markLearningOnSuccess = true)
        advanceUntilIdle()

        coVerify(exactly = 1) { fixture.learning.startLearning("spring") }
    }

    @Test
    fun studyLookupFailureDoesNotStartLearning() = runHomeViewModelTest {
        val fixture = Fixture()
        coEvery {
            fixture.repo.lookup("Spring", contextSentence = "", forceRefresh = false)
        } returns Result.failure(IllegalStateException("network failed"))
        val viewModel = fixture.viewModel()

        viewModel.lookup(" Spring ", markLearningOnSuccess = true)
        advanceUntilIdle()

        coVerify(exactly = 0) { fixture.learning.startLearning(any()) }
    }

    @Test
    fun lookupPassesContextSentenceToRepository() = runHomeViewModelTest {
        val fixture = Fixture()
        val viewModel = fixture.viewModel()

        viewModel.lookup(
            word = " run out of ",
            contextSentence = "I ran out of time before the exam.",
        )
        advanceUntilIdle()

        coVerify(exactly = 1) {
            fixture.repo.lookup(
                "run out of",
                contextSentence = "I ran out of time before the exam.",
                forceRefresh = false,
            )
        }
    }

    @Test
    fun reviewExampleSuccessUpdatesCurrentCardState() = runHomeViewModelTest {
        val fixture = Fixture()
        val viewModel = fixture.viewModel()
        viewModel.lookup(" Spring ")
        advanceUntilIdle()
        viewModel.setExample("Spring is coming.")

        viewModel.reviewExample()
        advanceUntilIdle()

        val state = assertIs<HomeUiState.Success>(viewModel.state.value)
        val feedbackState = assertIs<ExampleFeedbackUiState.Success>(state.exampleFeedbackState)
        assertEquals("自然", feedbackState.feedback.verdict)
        assertEquals("Spring is coming.", feedbackState.feedback.improvedExample)
    }

    @Test
    fun normalLookupSuccessDoesNotStartLearning() = runHomeViewModelTest {
        val fixture = Fixture()
        val viewModel = fixture.viewModel()

        viewModel.lookup(" Spring ")
        advanceUntilIdle()

        coVerify(exactly = 0) { fixture.learning.startLearning(any()) }
    }

    private class Fixture {
        val repo = mockk<ConceptRepository>()
        val settings = mockk<SettingsRepository>()
        val learning = mockk<WordLearningStateRepository>(relaxed = true)
        private val card = ConceptCard(
            word = "spring",
            entryType = EntryType.WORD,
            coreConcept = CoreConcept(
                picture = "something jumps up or starts from a source",
                anchorWord = "source",
            ),
            chineseApproximation = "弹起；泉源；春天",
            scenarios = emptyList(),
            misconceptions = emptyList(),
            branches = null,
            promptVersion = 3,
        )

        init {
            every { settings.configFlow } returns flowOf(
                ProviderConfig(
                    apiKey = "test-key",
                    baseUrl = "https://example.test/v1",
                    model = "test-model",
                )
            )
            coEvery { repo.lookup("Spring", forceRefresh = false) } returns Result.success(card)
            coEvery {
                repo.lookup("Spring", contextSentence = "", forceRefresh = false)
            } returns Result.success(card)
            coEvery {
                repo.lookup(
                    "run out of",
                    contextSentence = "I ran out of time before the exam.",
                    forceRefresh = false,
                )
            } returns Result.success(card.copy(word = "run out of"))
            coEvery {
                repo.reviewExample(
                    word = "spring",
                    userExample = "Spring is coming.",
                    contextSentence = "",
                )
            } returns Result.success(
                ExampleFeedback(
                    verdict = "自然",
                    improvedExample = "Spring is coming.",
                    reason = "spring 在这里表示春天到来，很自然。",
                )
            )
            coEvery { repo.setExample("spring", "Spring is coming.") } returns Unit
            every { repo.observeFavorite("spring") } returns flowOf(false)
            every { repo.observeNote("spring") } returns flowOf("")
            every { repo.observeExample("spring") } returns flowOf("")
            every { repo.observeSourceSentence("spring") } returns flowOf("")
            every { repo.observeFavorite("run out of") } returns flowOf(false)
            every { repo.observeNote("run out of") } returns flowOf("")
            every { repo.observeExample("run out of") } returns flowOf("")
            every {
                repo.observeSourceSentence("run out of")
            } returns flowOf("I ran out of time before the exam.")
        }

        fun viewModel(): HomeViewModel {
            return HomeViewModel(
                repo = repo,
                settings = settings,
                wordLearningStateRepository = learning,
            )
        }
    }

    private fun runHomeViewModelTest(
        testBody: suspend TestScope.() -> Unit,
    ) = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        Dispatchers.setMain(dispatcher)
        try {
            testBody()
        } finally {
            Dispatchers.resetMain()
        }
    }
}
