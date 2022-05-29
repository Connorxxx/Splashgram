package com.connor.unsplashgram.logic.model


data class SearchResponse(
    val total: Int,
    val total_pages: Int,
    val results: List<UnsplashPhoto>
)


data class UnsplashPhoto(
    val id: String,
    val created_at: String,
    val updated_at: String?,
    val width: Int,
    val height: Int,
    val color: String? = "#000000",
    val views: Int?,
    val downloads: Int?,
    val likes: Int?,
    val description: String? = "The artist did not add a description...",
    val urls: UnsplashUrls,
    val links: UnsplashLinks,
    val user: UnsplashUser,
    val exif: UnsplashExif?,
    val location: UnsplashLocation?,
    val tags: List<Tag>?,
)

data class UnsplashUrls(
    val thumb: String?,
    val small: String,
    val medium: String?,
    val regular: String?,
    val large: String?,
    val full: String?,
    val raw: String?
)

data class UnsplashLinks(
    val self: String,
    val html: String,
    val photos: String?,
    val likes: String?,
    val portfolio: String?,
    val download: String?,
    val download_location: String?
)

data class UnsplashUser(
    val id: String,
    val username: String,
    val name: String,
    val portfolio_url: String?,
    val bio: String?,
    val location: String?,
    val total_likes: Int,
    val total_photos: Int,
    val total_collection: Int,
    val profile_image: UnsplashUrls,
    val links: UnsplashLinks
)

data class UnsplashExif(
    val model: String?,
    val exposure_time: String?,
    val aperture: String?,
    val focal_length: String?,
    val iso: Int?
)

data class UnsplashLocation(
    val city: String?,
    val country: String?,
    val position: Position?
)

data class Position(
    val latitude: Double?,
    val longitude: Double?
)

data class Tag(
    val type: String?,
    val title: String?
)



