package org.piramalswasthya.sakhi.testutil

import android.database.Cursor
import androidx.sqlite.db.SupportSQLiteDatabase
import io.mockk.every
import io.mockk.mockk
import java.sql.Connection
import java.sql.DriverManager

/**
 * An in-memory SQLite database (sqlite-jdbc) exposed as a [SupportSQLiteDatabase] for the
 * read paths that classes such as BadgeFactsReader and FormSaveInterceptor use:
 * `query(String)` and `query(String, Array<Any>)`, returning a [Cursor] over the
 * materialised result. Use [exec] to build the schema and seed rows.
 */
class JdbcSupportDb : AutoCloseable {

    val connection: Connection = DriverManager.getConnection("jdbc:sqlite::memory:")

    fun exec(vararg sql: String) {
        connection.createStatement().use { st -> sql.forEach { st.execute(it) } }
    }

    val db: SupportSQLiteDatabase = mockk<SupportSQLiteDatabase>(relaxed = true).also { db ->
        every { db.query(any<String>()) } answers { cursorFor(firstArg(), emptyArray()) }
        every { db.query(any<String>(), any<Array<Any?>>()) } answers {
            cursorFor(firstArg(), secondArg())
        }
    }

    private fun cursorFor(sql: String, args: Array<out Any?>): Cursor {
        val names = mutableListOf<String>()
        val rows = mutableListOf<List<Any?>>()
        connection.prepareStatement(sql).use { ps ->
            args.forEachIndexed { i, a -> ps.setObject(i + 1, a) }
            ps.executeQuery().use { rs ->
                val md = rs.metaData
                for (i in 1..md.columnCount) names += md.getColumnLabel(i)
                while (rs.next()) rows += (1..md.columnCount).map { rs.getObject(it) }
            }
        }
        return listCursor(names, rows)
    }

    override fun close() = connection.close()

    companion object {
        fun listCursor(names: List<String>, rows: List<List<Any?>>): Cursor {
            val cursor = mockk<Cursor>(relaxed = true)
            var index = -1
            every { cursor.moveToNext() } answers { index += 1; index < rows.size }
            every { cursor.moveToFirst() } answers { index = 0; rows.isNotEmpty() }
            every { cursor.count } returns rows.size
            every { cursor.getColumnIndex(any()) } answers { names.indexOf(firstArg<String>()) }
            every { cursor.getColumnIndexOrThrow(any()) } answers { names.indexOf(firstArg<String>()) }
            every { cursor.getString(any()) } answers { rows[index][firstArg<Int>()]?.toString() }
            every { cursor.getLong(any()) } answers {
                when (val v = rows[index][firstArg<Int>()]) {
                    null -> 0L
                    is Number -> v.toLong()
                    else -> v.toString().toLong()
                }
            }
            every { cursor.getInt(any()) } answers {
                when (val v = rows[index][firstArg<Int>()]) {
                    null -> 0
                    is Number -> v.toInt()
                    else -> v.toString().toInt()
                }
            }
            every { cursor.isNull(any()) } answers { rows[index][firstArg<Int>()] == null }
            return cursor
        }
    }
}
