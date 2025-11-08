package eu.kanade.tachiyomi.extension.en.novelbuddy

import eu.kanade.tachiyomi.source.model.*
import eu.kanade.tachiyomi.source.online.ParsedHttpSource
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import eu.kanade.tachiyomi.network.GET

class NovelBuddy : ParsedHttpSource() {

    override val name = "NovelBuddy"
    override val baseUrl = "https://novelbuddy.com"
    override val lang = "en"
    override val supportsLatest = true

    override fun popularMangaRequest(page: Int) =
        GET("$baseUrl/novel-list?page=$page", headers)

    override fun popularMangaSelector() = "div.list-item"
    override fun popularMangaFromElement(element: Element): SManga {
        val novel = SManga.create()
        novel.title = element.select("h3.title a").text()
        novel.setUrlWithoutDomain(element.select("h3.title a").attr("href"))
        novel.thumbnail_url = element.select("img").attr("data-src")
        return novel
    }

    override fun popularMangaNextPageSelector() = "a.next"

    override fun latestUpdatesRequest(page: Int) =
        GET("$baseUrl/novel-list?page=$page&sort=latest", headers)
    override fun latestUpdatesSelector() = popularMangaSelector()
    override fun latestUpdatesFromElement(element: Element) = popularMangaFromElement(element)
    override fun latestUpdatesNextPageSelector() = popularMangaNextPageSelector()

    override fun mangaDetailsParse(document: Document): SManga {
        val info = SManga.create()
        info.title = document.select("h1.title").text()
        info.author = document.select("a[href*=/author/]").text()
        info.genre = document.select("div.genres a").joinToString { it.text() }
        info.description = document.select("div.summary").text()
        info.thumbnail_url = document.select("div.cover img").attr("src")
        return info
    }

    override fun chapterListSelector() = "ul.chapter-list li a"
    override fun chapterFromElement(element: Element): SChapter {
        val chapter = SChapter.create()
        chapter.name = element.text()
        chapter.setUrlWithoutDomain(element.attr("href"))
        return chapter
    }

    override fun pageListParse(document: Document): List<Page> {
        val paragraphs = document.select("div#chr-content p")
        val pages = mutableListOf<Page>()
        paragraphs.forEachIndexed { index, element ->
            val text = element.text()
            val dataUrl = "data:text/plain;charset=utf-8," + java.net.URLEncoder.encode(text, "UTF-8")
            pages.add(Page(index, document.location(), dataUrl))
        }
        return pages
    }

    override fun imageUrlParse(document: Document) = ""
}
