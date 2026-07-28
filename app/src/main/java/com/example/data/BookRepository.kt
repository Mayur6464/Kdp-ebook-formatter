package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

class BookRepository(private val bookDao: BookDao) {

    val currentBook: Flow<BookEntity?> = bookDao.getBookById(1)
    val currentSections: Flow<List<SectionEntity>> = bookDao.getSectionsForBook(1)

    suspend fun initializeDefaultBookIfEmpty() {
        val existingBook = bookDao.getFirstBookSync()
        if (existingBook == null) {
            val bookId = bookDao.insertBook(DefaultBookData.defaultBook)
            val defaultSections = DefaultBookData.getDefaultSections(bookId)
            bookDao.insertSections(defaultSections)
        }
    }

    suspend fun updateBook(book: BookEntity) {
        bookDao.updateBook(book)
    }

    suspend fun updateSection(section: SectionEntity) {
        bookDao.updateSection(section)
    }

    suspend fun insertSection(section: SectionEntity): Long {
        return bookDao.insertSection(section)
    }

    suspend fun deleteSection(section: SectionEntity) {
        bookDao.deleteSection(section)
    }

    suspend fun resetToDefaultBook() {
        bookDao.deleteAllSectionsForBook(1)
        bookDao.insertBook(DefaultBookData.defaultBook)
        bookDao.insertSections(DefaultBookData.getDefaultSections(1))
    }

    suspend fun getSectionsSync(bookId: Long = 1): List<SectionEntity> {
        return bookDao.getSectionsForBookSync(bookId)
    }

    suspend fun getBookSync(bookId: Long = 1): BookEntity? {
        return bookDao.getFirstBookSync()
    }
}
