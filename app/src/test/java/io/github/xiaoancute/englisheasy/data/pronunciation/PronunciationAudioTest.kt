package io.github.xiaoancute.englisheasy.data.pronunciation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class PronunciationAudioTest {

    @Test
    fun picksFirstNonBlankAudioUrl() {
        val json = """
            [
              {
                "phonetics": [
                  {"text": "/x/", "audio": ""},
                  {"text": "/x/", "audio": "//api.dictionaryapi.dev/media/pronunciations/en/test-us.mp3"}
                ]
              }
            ]
        """.trimIndent()

        assertEquals(
            "https://api.dictionaryapi.dev/media/pronunciations/en/test-us.mp3",
            firstDictionaryAudioUrl(json),
        )
    }

    @Test
    fun returnsNullWhenNoAudioExists() {
        assertNull(firstDictionaryAudioUrl("""[{"phonetics":[{"text":"/x/"}]}]"""))
    }

    @Test
    fun returnsNullForUnexpectedJsonShape() {
        assertNull(firstDictionaryAudioUrl("""[1, {"phonetics": [1, {"audio": 2}]}]"""))
    }
}
