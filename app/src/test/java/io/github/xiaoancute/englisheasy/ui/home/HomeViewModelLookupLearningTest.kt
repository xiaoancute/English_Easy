package io.github.xiaoancute.englisheasy.ui.home

import io.github.xiaoancute.englisheasy.data.learning.WordLearningStateRepository
import io.github.xiaoancute.englisheasy.data.llm.ConceptRepository
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
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.Test

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
            fixture.repo.lookup("spring", forceRefresh = false)
        } returns Result.failure(IllegalStateException("network failed"))
        val viewModel = fixture.viewModel()

        viewModel.lookup(" Spring ", markLearningOnSuccess = true)
        advanceUntilIdle()

        coVerify(exactly = 0) { fixture.learning.startLearning(any()) }
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
            coEvery { repo.lookup("spring", forceRefresh = false) } returns Result.success(card)
            every { repo.observeFavorite("spring") } returns flowOf(false)
            every { repo.observeNote("spring") } returns flowOf("")
            every { repo.observeExample("spring") } returns flowOf("")
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
        dispatcher: TestDispatcher = StandardTestDispatcher(),
        testBody: suspend TestScope.() -> Unit,
    ) {
        Dispatchers.setMain(dispatcher)
        try {
            runTest(dispatcher) {
                testBody()
            }
        } finally {
            Dispatchers.resetMain()
        }
    }
}
