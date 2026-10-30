package com.example

import com.example.data.local.ArtworkEntity
import com.example.data.local.CachedArtworkEntity
import com.example.data.model.ArticArtworkData
import com.example.data.model.ArticSearchResponse
import com.example.data.model.Artwork
import com.example.data.model.MetObjectResponse
import com.example.data.remote.NetworkClient
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun `test Met Museum response parsing and artwork mapping`() {
        val json = """
            {
                "objectID": 436535,
                "isHighlight": true,
                "isPublicDomain": true,
                "primaryImage": "https://images.metmuseum.org/CRDImages/ep/original/DP-42549-001.jpg",
                "primaryImageSmall": "https://images.metmuseum.org/CRDImages/ep/web-large/DP-42549-001.jpg",
                "department": "European Paintings",
                "title": "Wheat Field with Cypresses",
                "period": "19th Century",
                "artistDisplayName": "Vincent van Gogh",
                "artistDisplayBio": "Dutch, 1853–1890",
                "objectDate": "1889",
                "medium": "Oil on canvas",
                "dimensions": "73.2 × 93.4 cm",
                "creditLine": "Purchase, The Annenberg Foundation Gift, 1993"
            }
        """.trimIndent()

        val adapter = NetworkClient.moshi.adapter(MetObjectResponse::class.java)
        val metObj = adapter.fromJson(json)

        assertNotNull(metObj)
        assertEquals(436535L, metObj?.objectID)
        assertEquals("Wheat Field with Cypresses", metObj?.title)
        assertEquals("Vincent van Gogh", metObj?.artistDisplayName)

        // Test mapper
        val artwork = metObj!!.toArtwork()
        assertEquals("met-436535", artwork.id)
        assertEquals("Vincent van Gogh", artwork.artistDisplayName)
        assertEquals("The Metropolitan Museum of Art", artwork.sourceMuseum)
        assertTrue(artwork.isPublicDomain)
        assertEquals("https://images.metmuseum.org/CRDImages/ep/original/DP-42549-001.jpg", artwork.primaryImage)
    }

    @Test
    fun `test Art Institute of Chicago response parsing and artwork mapping`() {
        val json = """
            {
                "data": [
                    {
                        "id": 27992,
                        "title": "A Sunday on La Grande Jatte",
                        "artist_display": "Georges Seurat\nFrench, 1859–1891",
                        "artist_title": "Georges Seurat",
                        "date_display": "1884–1886",
                        "medium_display": "Oil on canvas",
                        "dimensions": "207.5 × 308.1 cm",
                        "credit_line": "Helen Birch Bartlett Memorial Collection",
                        "image_id": "1adf2696-8489-499b-cad2-821d7fde4b33",
                        "is_public_domain": true,
                        "department_title": "Painting and Sculpture of Europe",
                        "style_title": "Pointillism"
                    }
                ],
                "config": {
                    "iiif_url": "https://www.artic.edu/iiif/2"
                }
            }
        """.trimIndent()

        val adapter = NetworkClient.moshi.adapter(ArticSearchResponse::class.java)
        val response = adapter.fromJson(json)

        assertNotNull(response)
        assertEquals(1, response?.data?.size)

        val articItem = response?.data?.first()!!
        assertEquals(27992L, articItem.id)
        assertEquals("A Sunday on La Grande Jatte", articItem.title)
        assertEquals("Georges Seurat", articItem.artistTitle)

        // Test IIIF image URL generation
        val fullImage = articItem.getFullImageUrl()
        assertEquals("https://www.artic.edu/iiif/2/1adf2696-8489-499b-cad2-821d7fde4b33/full/1686,/0/default.jpg", fullImage)

        // Test mapper
        val artwork = articItem.toArtwork()
        assertEquals("artic-27992", artwork.id)
        assertEquals("Georges Seurat", artwork.artistDisplayName)
        assertEquals("Art Institute of Chicago", artwork.sourceMuseum)
        assertEquals("Pointillism", artwork.movement)
    }

    @Test
    fun `test Room entity cache conversion`() {
        val artwork = Artwork(
            id = "met-438012",
            title = "The Great Wave off Kanagawa",
            artistDisplayName = "Katsushika Hokusai",
            objectDate = "ca. 1830–32",
            medium = "Woodblock print",
            movement = "Ukiyo-e",
            sourceMuseum = "The Metropolitan Museum of Art",
            primaryImage = "https://images.metmuseum.org/CRDImages/as/original/DP130155.jpg"
        )

        // Test CachedArtworkEntity
        val cachedEntity = CachedArtworkEntity.fromArtwork(artwork)
        assertEquals("met-438012", cachedEntity.id)
        assertEquals("The Great Wave off Kanagawa", cachedEntity.title)

        val restoredFromCache = cachedEntity.toArtwork(isFavorite = false)
        assertEquals(artwork.id, restoredFromCache.id)
        assertEquals(artwork.title, restoredFromCache.title)
        assertEquals(artwork.artistDisplayName, restoredFromCache.artistDisplayName)

        // Test ArtworkEntity (Favorites)
        val favoriteEntity = ArtworkEntity.fromArtwork(artwork)
        assertEquals("met-438012", favoriteEntity.id)
        assertEquals("Katsushika Hokusai", favoriteEntity.artist)
        assertEquals("https://images.metmuseum.org/CRDImages/as/original/DP130155.jpg", favoriteEntity.imageUrl)
        assertTrue(favoriteEntity.dateDiscovered > 0L)

        val restoredFavorite = favoriteEntity.toArtwork()
        assertTrue(restoredFavorite.isFavorite)
        assertEquals("Katsushika Hokusai", restoredFavorite.artist)
        assertEquals("https://images.metmuseum.org/CRDImages/as/original/DP130155.jpg", restoredFavorite.imageUrl)
        assertEquals(favoriteEntity.dateDiscovered, restoredFavorite.dateDiscovered)
    }

    @Test
    fun `test ArtworkApiService instance availability`() {
        val service = NetworkClient.artworkApiService
        assertNotNull(service)
    }
}
