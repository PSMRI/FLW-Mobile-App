package org.piramalswasthya.sakhi.database.room

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Upgrades real on-disk databases created with the exact schema of earlier builds
 * (assets/db_upgrade/<name>.sql, captured from Room's generated createAllTables) through
 * the production migration chain, letting Room validate the result against the current
 * entities. Every table is seeded with one row first so data loss is caught too.
 */
@RunWith(AndroidJUnit4::class)
class InAppDbUpgradeTest {

    private val targetContext: Context =
        InstrumentationRegistry.getInstrumentation().targetContext
    private val assets = InstrumentationRegistry.getInstrumentation().context.assets

    private fun productionMigrations(): Array<Migration> =
        (FIRST_REGISTERED_VERSION until LATEST_VERSION).flatMap { from ->
            (from + 1..minOf(from + 2, LATEST_VERSION)).mapNotNull { to ->
                val clazz = try {
                    Class.forName(
                        "org.piramalswasthya.sakhi.database.room.InAppDb\$Companion\$getInstance\$MIGRATION_${from}_${to}\$1"
                    )
                } catch (_: ClassNotFoundException) {
                    return@mapNotNull null
                }
                val constructor = clazz.declaredConstructors.first()
                constructor.isAccessible = true
                when (val instance = constructor.newInstance()) {
                    is Migration -> instance
                    // Migration(from, to) { ... } compiles the lambda body to this class, not a Migration.
                    is Function1<*, *> -> {
                        @Suppress("UNCHECKED_CAST")
                        val body = instance as (SupportSQLiteDatabase) -> Unit
                        Migration(from, to, body)
                    }
                    else -> error("MIGRATION_${from}_${to} is neither a Migration nor a lambda")
                }
            }
        }.toTypedArray()

    private fun seedEveryTable(db: SQLiteDatabase): Map<String, Long> {
        val tables = db.rawQuery(
            "SELECT name FROM sqlite_master WHERE type='table' AND name NOT LIKE 'sqlite_%' " +
                    "AND name NOT IN ('room_master_table','android_metadata')",
            null
        ).use { c -> generateSequence { if (c.moveToNext()) c.getString(0) else null }.toList() }

        tables.forEach { table ->
            val values = db.rawQuery("PRAGMA table_info(`$table`)", null).use { c ->
                generateSequence {
                    if (!c.moveToNext()) return@generateSequence null
                    val name = c.getString(c.getColumnIndexOrThrow("name"))
                    val type = c.getString(c.getColumnIndexOrThrow("type")).uppercase()
                    val value = when {
                        type.contains("TEXT") -> "'1'"
                        type.contains("BLOB") -> "x'01'"
                        else -> "1"
                    }
                    "`$name`" to value
                }.toList()
            }
            db.execSQL(
                "INSERT INTO `$table` (${values.joinToString { it.first }}) " +
                        "VALUES (${values.joinToString { it.second }})"
            )
        }
        return tables.associateWith { 1L }
    }

    private fun createFromFixture(fixture: String, dbName: String): Map<String, Long> {
        val lines = assets.open("db_upgrade/$fixture.sql").bufferedReader().readLines()
            .filter { it.isNotBlank() }
        val version = lines.first().removePrefix("-- version ").trim().toInt()

        targetContext.deleteDatabase(dbName)
        val db = SQLiteDatabase.openOrCreateDatabase(targetContext.getDatabasePath(dbName), null)
        try {
            lines.drop(1).forEach { db.execSQL(it) }
            val seeded = seedEveryTable(db)
            db.version = version
            return seeded
        } finally {
            db.close()
        }
    }

    private fun upgradeAndVerify(fixture: String) {
        val dbName = "upgrade_test_$fixture"
        val seeded = createFromFixture(fixture, dbName)

        val room = Room.databaseBuilder(targetContext, InAppDb::class.java, dbName)
            .addMigrations(*productionMigrations())
            .build()
        try {
            // Opening runs the migrations and Room's schema validation; either throws on mismatch.
            val db = room.openHelper.writableDatabase
            assertEquals(LATEST_VERSION, db.version)

            seeded.keys.forEach { table ->
                val exists = db.query(
                    "SELECT 1 FROM sqlite_master WHERE type='table' AND name='$table'"
                ).use { it.moveToFirst() }
                if (exists) {
                    val count = db.query("SELECT COUNT(*) FROM `$table`").use {
                        it.moveToFirst(); it.getLong(0)
                    }
                    assertTrue("$fixture: rows lost in $table", count >= 1)
                }
            }
            db.query("SELECT COUNT(*) FROM BEN_BASIC_CACHE").use { assertTrue(it.moveToFirst()) }
        } finally {
            room.close()
            targetContext.deleteDatabase(dbName)
        }
    }

    @Test
    fun freshInstallCreatesLatestSchema() {
        val dbName = "upgrade_test_fresh"
        targetContext.deleteDatabase(dbName)
        val room = Room.databaseBuilder(targetContext, InAppDb::class.java, dbName)
            .addMigrations(*productionMigrations())
            .build()
        try {
            assertEquals(LATEST_VERSION, room.openHelper.writableDatabase.version)
        } finally {
            room.close()
            targetContext.deleteDatabase(dbName)
        }
    }

    @Test
    fun upgradesEveryCapturedPreviousSchema() {
        val fixtures = assets.list("db_upgrade").orEmpty()
            .filter { it.endsWith(".sql") }
            // The gamification build is not shipping on this release line; its v66 schema is not supported.
            .filterNot { it.startsWith("v66_gamification") }
            .map { it.removeSuffix(".sql") }
        assertTrue("no fixtures found", fixtures.isNotEmpty())
        val failures = fixtures.mapNotNull { fixture ->
            runCatching { upgradeAndVerify(fixture) }.exceptionOrNull()?.let { "$fixture -> $it" }
        }
        assertTrue(failures.joinToString("\n"), failures.isEmpty())
    }

    private companion object {
        const val LATEST_VERSION = 67
        const val FIRST_REGISTERED_VERSION = 13 // getInstance registers MIGRATION_13_14 onwards
    }
}
