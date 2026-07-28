package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface BookDao {
    @Query("SELECT * FROM books WHERE id = :bookId LIMIT 1")
    fun getBookById(bookId: Long): Flow<BookEntity?>

    @Query("SELECT * FROM books LIMIT 1")
    suspend fun getFirstBookSync(): BookEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBook(book: BookEntity): Long

    @Update
    suspend fun updateBook(book: BookEntity)

    @Query("SELECT * FROM sections WHERE bookId = :bookId ORDER BY orderIndex ASC")
    fun getSectionsForBook(bookId: Long): Flow<List<SectionEntity>>

    @Query("SELECT * FROM sections WHERE bookId = :bookId ORDER BY orderIndex ASC")
    suspend fun getSectionsForBookSync(bookId: Long): List<SectionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSections(sections: List<SectionEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSection(section: SectionEntity): Long

    @Update
    suspend fun updateSection(section: SectionEntity)

    @Delete
    suspend fun deleteSection(section: SectionEntity)

    @Query("DELETE FROM sections WHERE bookId = :bookId")
    suspend fun deleteAllSectionsForBook(bookId: Long)
}
