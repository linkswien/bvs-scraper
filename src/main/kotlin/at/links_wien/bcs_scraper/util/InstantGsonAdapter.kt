package at.links_wien.bcs_scraper.util

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import java.time.Instant

object InstantGsonAdapter : TypeAdapter<Instant>() {
    override fun write(writer: JsonWriter?, value: Instant?) {
        writer?.value(value?.toEpochMilli())
    }

    override fun read(reader: JsonReader?): Instant = Instant.ofEpochMilli(reader?.nextLong()!!)
}