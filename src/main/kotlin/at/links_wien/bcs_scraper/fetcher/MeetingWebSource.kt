package at.links_wien.bcs_scraper.fetcher

import at.links_wien.bcs_scraper.model.Meeting
import kotlinx.coroutines.future.await
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatterBuilder
import java.time.temporal.ChronoField

class MeetingWebSource(private val district: Int) {

    companion object {
        private val HTTP_CLIENT: HttpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(15))
            .build()

        private const val URI_TEMPLATE =
            "https://www.wien.gv.at/bvt/internet/AdvPrSrv.asp?Layout=sSitzungstermine&Type=R&Bezirk=%d"

        private val DATE_FORMATTER = DateTimeFormatterBuilder()
            .appendPattern("d. ")
            .appendText(
                ChronoField.MONTH_OF_YEAR, mapOf(
                    1L to "Januar",
                    2L to "Februar",
                    3L to "März",
                    4L to "April",
                    5L to "Mai",
                    6L to "Juni",
                    7L to "Juli",
                    8L to "August",
                    9L to "September",
                    10L to "Oktober",
                    11L to "November",
                    12L to "Dezember"
                )
            )
            .appendPattern(" yyyy HH:mm")
            .appendLiteral(" Uhr")
            .toFormatter()
            .withZone(ZoneId.of("Europe/Vienna"))
    }

    init {
        require(district in 1..23) { "District $district doesn't exist" }
    }

    suspend fun get(): List<Meeting> {
        val document = fetchPage()

        return document.select("#vie_main table:first-of-type tr")
            .drop(1) // Skip header
            .mapNotNull { parseMeeting(it) }
            .toList()
    }

    private fun parseMeeting(row: Element): Meeting {
        return row.children().let { cells ->
            val infoSplit = cells[1].text().trim().split(";").map { it.trim() }
            Meeting(
                district,
                date = parseDate(cells[0].text().trim()),
                address = infoSplit[0],
                additionalInfo = infoSplit.getOrNull(1)
            )
        }
    }

    private fun parseDate(input: String): Instant {
        return DATE_FORMATTER
            .parse(input)
            .let { Instant.from(it) }
    }

    private suspend fun fetchPage(): Document {
        return HttpRequest.newBuilder()
            .uri(URI(URI_TEMPLATE.format(district))).build()
            .let { HTTP_CLIENT.sendAsync(it, HttpResponse.BodyHandlers.ofString()) }
            .await().body()
            .let { Jsoup.parse(it) }
    }

}