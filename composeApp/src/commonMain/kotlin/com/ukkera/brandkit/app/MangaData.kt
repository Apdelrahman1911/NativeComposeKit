package com.ukkera.brandkit.app

/**
 * App-level sample data for the manga flow (library grid → detail → reader). Deterministic and stable so the
 * UI looks identical on every run and on both platforms; titles/authors are **fictional** to avoid
 * copyright-sensitive assets, and covers/pages use seeded HTTPS placeholder images (swap [Manga.coverUrl] /
 * [Manga.pageUrl] for a real CDN later). This is *app* data — the component library never sees it.
 */

enum class MangaStatus(val label: String) { Ongoing("Ongoing"), Completed("Completed"), Hiatus("Hiatus") }

data class Chapter(
    val id: String,
    val number: Int,
    val title: String,
    val date: String,
    /** Default read state; the detail screen tracks live changes on top of this. */
    val read: Boolean,
)

data class Manga(
    val id: String,
    val title: String,
    val author: String,
    val status: MangaStatus,
    val genres: List<String>,
    val rating: Float,
    val synopsis: String,
    val unread: Int,
    val chapters: List<Chapter>,
) {
    /** Stable, reliable HTTPS cover (2:3). Seeded so the same manga always gets the same image. */
    val coverUrl: String get() = "https://picsum.photos/seed/manga-$id/300/450"

    /** A page image for [chapterId] (1-based [page]). Seeded for stability. */
    fun pageUrl(chapterId: String, page: Int): String =
        "https://picsum.photos/seed/$id-$chapterId-$page/800/1200"
}

/** The in-memory sample library. */
object MangaLibrary {

    val all: List<Manga> = listOf(
        manga(
            id = "crimson-vanguard",
            title = "Crimson Vanguard",
            author = "Reina Sato",
            status = MangaStatus.Ongoing,
            genres = listOf("Action", "Fantasy"),
            rating = 4.5f,
            unread = 3,
            chapterCount = 18,
            synopsis = "A disgraced knight bound to a dying ember-spirit must reclaim the seven banners of " +
                "the fallen capital before the long winter swallows the last free city.",
        ),
        manga(
            id = "lanterns-of-yumegawa",
            title = "Lanterns of Yumegawa",
            author = "Haruki Mizuno",
            status = MangaStatus.Completed,
            genres = listOf("Slice of Life", "Drama"),
            rating = 4.0f,
            unread = 0,
            chapterCount = 12,
            synopsis = "Three siblings reopen their grandmother's riverside lantern shop and slowly mend the " +
                "town — and themselves — one summer festival at a time.",
        ),
        manga(
            id = "steelbound-saga",
            title = "Steelbound Saga",
            author = "Kenji Aoki",
            status = MangaStatus.Ongoing,
            genres = listOf("Action", "Adventure"),
            rating = 4.5f,
            unread = 5,
            chapterCount = 24,
            synopsis = "On a continent where memories are forged into weapons, a young smith hunts the blade " +
                "that stole her brother's name.",
        ),
        manga(
            id = "petals-and-static",
            title = "Petals & Static",
            author = "Mei Tanaka",
            status = MangaStatus.Hiatus,
            genres = listOf("Romance", "Sci-Fi"),
            rating = 3.5f,
            unread = 1,
            chapterCount = 9,
            synopsis = "A radio engineer keeps picking up love letters broadcast from a city that was " +
                "decommissioned forty years ago.",
        ),
        manga(
            id = "the-last-cartographer",
            title = "The Last Cartographer",
            author = "Daichi Kuroda",
            status = MangaStatus.Completed,
            genres = listOf("Adventure", "Mystery"),
            rating = 5.0f,
            unread = 0,
            chapterCount = 15,
            synopsis = "The maps are always right, even when the world is wrong. One mapmaker walks off the " +
                "edge of the known coast to find out why.",
        ),
        manga(
            id = "neon-ronin",
            title = "Neon Ronin",
            author = "Yui Hoshino",
            status = MangaStatus.Ongoing,
            genres = listOf("Action", "Sci-Fi"),
            rating = 4.0f,
            unread = 8,
            chapterCount = 30,
            synopsis = "A masterless courier with an outlawed reflex-chip takes one last job across the " +
                "vertical slums of New Sera.",
        ),
        manga(
            id = "garden-of-echoes",
            title = "Garden of Echoes",
            author = "Sora Fujimoto",
            status = MangaStatus.Ongoing,
            genres = listOf("Fantasy", "Romance"),
            rating = 4.5f,
            unread = 2,
            chapterCount = 14,
            synopsis = "Every flower in the walled garden repeats a sentence its planter never got to finish. " +
                "A mute gardener decides to answer them.",
        ),
        manga(
            id = "ironpeak-academy",
            title = "Ironpeak Academy",
            author = "Takumi Ishida",
            status = MangaStatus.Hiatus,
            genres = listOf("Comedy", "Adventure"),
            rating = 3.5f,
            unread = 0,
            chapterCount = 20,
            synopsis = "At the worst-ranked dungeon-delver school on the mountain, the least talented class " +
                "keeps accidentally saving the kingdom.",
        ),
        manga(
            id = "drift-signal",
            title = "Drift Signal",
            author = "Nao Komatsu",
            status = MangaStatus.Ongoing,
            genres = listOf("Sci-Fi", "Mystery"),
            rating = 4.0f,
            unread = 4,
            chapterCount = 11,
            synopsis = "A deep-space salvage crew answers a distress beacon that is broadcasting their own " +
                "voices, three days from now.",
        ),
    )

    fun byId(id: String): Manga? = all.firstOrNull { it.id == id }

    /** Resolve a (manga, chapter) pair from their ids, or null if either is unknown. */
    fun chapter(mangaId: String, chapterId: String): Pair<Manga, Chapter>? {
        val m = byId(mangaId) ?: return null
        val c = m.chapters.firstOrNull { it.id == chapterId } ?: return null
        return m to c
    }

    /** Deterministic page count for a chapter (5..8). */
    fun pageCount(chapter: Chapter): Int = 5 + (chapter.number % 4)
}

private val MONTHS = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")

/** Builds a manga whose last [unread] chapters are unread; chapter dates descend from newest. */
private fun manga(
    id: String,
    title: String,
    author: String,
    status: MangaStatus,
    genres: List<String>,
    rating: Float,
    unread: Int,
    chapterCount: Int,
    synopsis: String,
): Manga {
    val chapters = (1..chapterCount).map { n ->
        Chapter(
            id = "$id-ch$n",
            number = n,
            title = "Chapter $n",
            date = chapterDate(n),
            read = n <= chapterCount - unread,
        )
    }
    return Manga(id, title, author, status, genres, rating, synopsis, unread, chapters)
}

/** A stable, plausible-looking release date derived from the chapter number (no clock access). */
private fun chapterDate(n: Int): String {
    val month = MONTHS[(n * 5) % 12]
    val day = (n * 7) % 27 + 1
    val year = 2023 + (n / 7)
    return "$month $day, $year"
}
