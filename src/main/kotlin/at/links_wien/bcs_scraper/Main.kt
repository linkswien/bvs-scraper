package at.links_wien.bcs_scraper

import at.links_wien.bcs_scraper.fetcher.MeetingWebSource
import at.links_wien.bcs_scraper.model.Meeting
import at.links_wien.bcs_scraper.util.InstantGsonAdapter
import com.google.gson.GsonBuilder
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import java.io.FileWriter
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets.UTF_8
import java.time.Instant


object Main {

    private val GSON = GsonBuilder()
        .registerTypeAdapter(Instant::class.java, InstantGsonAdapter)
        .setPrettyPrinting()
        .create()

    @JvmStatic
    fun main(args: Array<String>) = runBlocking {
        val writer = if (args.isNotEmpty()) {
            FileWriter(args[0], UTF_8)
        } else {
            OutputStreamWriter(System.out, UTF_8)
        }

        writer.use { GSON.toJson(getAllMeetings(), it) }
    }

    private fun getAllMeetings(): List<Meeting> = runBlocking {
        IntRange(1, 23).map {
            async { MeetingWebSource(it).get() }
        }.awaitAll().flatten()
    }

}

