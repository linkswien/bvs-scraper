package at.links_wien.bcs_scraper.model

import java.time.Instant

data class Meeting(val district: Int, val date: Instant, val address: String, val additionalInfo: String?)
