package org.piramalswasthya.sakhi.badges.domain

import android.content.Context
import io.mockk.every
import io.mockk.mockk
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.database.room.InAppDb
import org.piramalswasthya.sakhi.testutil.JdbcSupportDb
import java.util.Calendar
import java.util.concurrent.TimeUnit

class BadgeFactsReaderTest {

    private lateinit var jdbc: JdbcSupportDb
    private lateinit var reader: BadgeFactsReader
    private val context = mockk<Context>()

    private val day = TimeUnit.DAYS.toMillis(1)
    private val now = System.currentTimeMillis()

    @Before
    fun setUp() {
        jdbc = JdbcSupportDb()
        val inAppDb = mockk<InAppDb>()
        every { inAppDb.openHelper.readableDatabase } returns jdbc.db
        every { context.getString(R.string.is_child_referred_nrc) } returns "nrc_referred"
        reader = BadgeFactsReader(inAppDb, context)
    }

    @After
    fun tearDown() = jdbc.close()

    // ── empty / missing schema: every fact degrades to zero ──

    @Test
    fun `missing tables contribute nothing`() {
        assertEquals(emptySet<String>(), reader.onTimeIncentiveMonths())
        assertEquals(0L, reader.activeDomainsSince(0))
        assertEquals(0L, reader.meetingTypesSince(0))
        assertEquals(emptySet<String>(), reader.activityWeeks())
        assertEquals(emptyList<String>(), reader.completedMaternalJourneys())
        assertEquals(emptyList<String>(), reader.fullyImmunizedChildren())
        assertEquals(emptyList<String>(), reader.abhaGeneratedBens())
        assertEquals(emptyList<String>(), reader.vulnerableBabiesCaredFor())
        assertEquals(emptyList<String>(), reader.nrcReferredChildren())
    }

    @Test
    fun `database failure is swallowed`() {
        val broken = mockk<InAppDb>()
        every { broken.openHelper.readableDatabase } throws IllegalStateException("closed")
        val r = BadgeFactsReader(broken, context)
        assertEquals(0L, r.activeDomainsSince(0))
        assertEquals(emptySet<String>(), r.onTimeIncentiveMonths())
        assertEquals(emptyList<String>(), r.nrcReferredChildren())
    }

    @Test
    fun `mapped tables include domain, meeting and extra tables`() {
        val tables = BadgeFactsReader.MAPPED_TABLES.toSet()
        assertTrue("INCENTIVE_RECORD" in tables)
        assertTrue("all_visit_history" in tables)
        assertTrue("MAA_MEETING" in tables)
        assertEquals(tables.size, BadgeFactsReader.MAPPED_TABLES.size)
    }

    // ── Timely Reporter ──

    @Test
    fun `on-time incentive months are keyed by end date`() {
        jdbc.exec(
            "CREATE TABLE INCENTIVE_RECORD (createdDate INTEGER, endDate INTEGER)",
            "INSERT INTO INCENTIVE_RECORD VALUES (${now - day}, $now)",
            "INSERT INTO INCENTIVE_RECORD VALUES (${now + day}, $now)" // late — excluded
        )
        assertEquals(setOf(BadgeDates.monthKey(now)), reader.onTimeIncentiveMonths())
    }

    // ── Complete Worker / Community Voice ──

    @Test
    fun `active domains counts each domain once and honours drafts and windows`() {
        jdbc.exec(
            "CREATE TABLE INFANT_REG (createdDate INTEGER, isDraft INTEGER)",
            "CREATE TABLE CHILD_REG (createdDate INTEGER)",
            "CREATE TABLE IMMUNIZATION (createdDate INTEGER)",
            "CREATE TABLE TB_SCREENING (createdDate TEXT)", // TEXT dates are ignored
            "CREATE TABLE PMSMA (updatedDate INTEGER)",
            "CREATE TABLE FPOT (someColumn INTEGER)", // no date column
            "INSERT INTO INFANT_REG VALUES ($now, 0)",
            "INSERT INTO CHILD_REG VALUES ($now)",
            "INSERT INTO IMMUNIZATION VALUES ($now)",
            "INSERT INTO TB_SCREENING VALUES ('$now')",
            "INSERT INTO PMSMA VALUES (${now - 200 * day})",
            "INSERT INTO FPOT VALUES (1)"
        )
        // child + immunization
        assertEquals(2L, reader.activeDomainsSince(now - day))
        // maternal via PMSMA when the window reaches back
        assertEquals(3L, reader.activeDomainsSince(now - 300 * day))
        // bounded window that excludes everything recent
        assertEquals(1L, reader.activeDomainsSince(now - 300 * day, now - 100 * day))
    }

    @Test
    fun `draft records do not count`() {
        jdbc.exec(
            "CREATE TABLE INFANT_REG (createdDate INTEGER, isDraft INTEGER)",
            "INSERT INTO INFANT_REG VALUES ($now, 1)"
        )
        assertEquals(0L, reader.activeDomainsSince(now - day))
    }

    @Test
    fun `meeting types counts distinct meeting tables`() {
        jdbc.exec(
            "CREATE TABLE MAA_MEETING (createdDate INTEGER)",
            "CREATE TABLE AHDMeeting (date INTEGER)",
            "CREATE TABLE UWIN_SESSION (visitDate INTEGER)",
            "INSERT INTO MAA_MEETING VALUES ($now)",
            "INSERT INTO MAA_MEETING VALUES ($now)",
            "INSERT INTO AHDMeeting VALUES ($now)",
            "INSERT INTO UWIN_SESSION VALUES (${now - 400 * day})"
        )
        assertEquals(2L, reader.meetingTypesSince(now - day))
        assertEquals(3L, reader.meetingTypesSince(0))
    }

    // ── Steady Syncer activity weeks ──

    @Test
    fun `activity weeks are derived from record dates across tables`() {
        val lastWeek = now - 7 * day
        jdbc.exec(
            "CREATE TABLE HBNC (createdDate INTEGER)",
            "CREATE TABLE IMMUNIZATION (createdDate INTEGER, isDraft INTEGER)",
            "CREATE TABLE CHILD_REG (name TEXT)", // no date column — skipped
            "INSERT INTO HBNC VALUES ($now)",
            "INSERT INTO HBNC VALUES (0)", // zero dates ignored
            "INSERT INTO IMMUNIZATION VALUES ($lastWeek, 0)",
            "INSERT INTO IMMUNIZATION VALUES (${now - 70 * day}, 1)" // draft
        )
        assertEquals(
            setOf(BadgeDates.weekKey(now), BadgeDates.weekKey(lastWeek)),
            reader.activityWeeks()
        )
    }

    @Test
    fun `activity week uses the local day even just after midnight`() {
        val earlyMorning = Calendar.getInstance().apply {
            timeInMillis = now
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 30)
            set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        jdbc.exec(
            "CREATE TABLE HBNC (createdDate INTEGER)",
            "INSERT INTO HBNC VALUES ($earlyMorning)"
        )
        assertEquals(setOf(BadgeDates.weekKey(earlyMorning)), reader.activityWeeks())
    }

    // ── Maternal Journey ──

    private fun maternalSchema(withTrimesterColumns: Boolean = false) {
        val lmp = if (withTrimesterColumns) ", lmp INTEGER" else ""
        val ancDate = if (withTrimesterColumns) ", ancDate INTEGER" else ""
        jdbc.exec(
            "CREATE TABLE PREGNANCY_REGISTER (benId INTEGER, isDraft INTEGER$lmp)",
            "CREATE TABLE DELIVERY_OUTCOME (benId INTEGER, placeOfDelivery TEXT, isDraft INTEGER)",
            "CREATE TABLE PREGNANCY_ANC (benId INTEGER, isDraft INTEGER$ancDate)",
            "CREATE TABLE ALL_VISIT_HISTORY_ANC (benId INTEGER, visitDay TEXT)"
        )
    }

    @Test
    fun `maternal journey requires institutional delivery and four ANC visits`() {
        maternalSchema()
        jdbc.exec(
            // ben 1: 3 structured + 1 dynamic, district hospital -> counts
            "INSERT INTO PREGNANCY_REGISTER VALUES (1, 0)",
            "INSERT INTO DELIVERY_OUTCOME VALUES (1, ' District Hospital ', 0)",
            "INSERT INTO PREGNANCY_ANC VALUES (1, 0)",
            "INSERT INTO PREGNANCY_ANC VALUES (1, 0)",
            "INSERT INTO PREGNANCY_ANC VALUES (1, 0)",
            "INSERT INTO ALL_VISIT_HISTORY_ANC VALUES (1, 'v4')",
            // ben 2: home delivery -> excluded
            "INSERT INTO PREGNANCY_REGISTER VALUES (2, 0)",
            "INSERT INTO DELIVERY_OUTCOME VALUES (2, 'Home', 0)",
            "INSERT INTO ALL_VISIT_HISTORY_ANC VALUES (2, 'a')",
            "INSERT INTO ALL_VISIT_HISTORY_ANC VALUES (2, 'b')",
            "INSERT INTO ALL_VISIT_HISTORY_ANC VALUES (2, 'c')",
            "INSERT INTO ALL_VISIT_HISTORY_ANC VALUES (2, 'd')",
            // ben 3: institutional but only 2 visits (one draft) -> excluded
            "INSERT INTO PREGNANCY_REGISTER VALUES (3, 0)",
            "INSERT INTO DELIVERY_OUTCOME VALUES (3, 'Sub Centre', 0)",
            "INSERT INTO PREGNANCY_ANC VALUES (3, 0)",
            "INSERT INTO PREGNANCY_ANC VALUES (3, 1)",
            "INSERT INTO ALL_VISIT_HISTORY_ANC VALUES (3, 'x')"
        )
        assertEquals(listOf("1"), reader.completedMaternalJourneys())
    }

    @Test
    fun `maternal journey applies first trimester check when dates are integers`() {
        maternalSchema(withTrimesterColumns = true)
        val lmp = now - 300 * day
        jdbc.exec(
            "INSERT INTO PREGNANCY_REGISTER VALUES (1, 0, $lmp)",
            "INSERT INTO DELIVERY_OUTCOME VALUES (1, 'Primary Health Centre', 0)",
            "INSERT INTO PREGNANCY_ANC VALUES (1, 0, ${lmp + 30 * day})",
            "INSERT INTO PREGNANCY_ANC VALUES (1, 0, ${lmp + 120 * day})",
            "INSERT INTO PREGNANCY_ANC VALUES (1, 0, ${lmp + 180 * day})",
            "INSERT INTO PREGNANCY_ANC VALUES (1, 0, ${lmp + 240 * day})",
            // ben 2: four visits but none in the first trimester
            "INSERT INTO PREGNANCY_REGISTER VALUES (2, 0, $lmp)",
            "INSERT INTO DELIVERY_OUTCOME VALUES (2, 'Primary Health Centre', 0)",
            "INSERT INTO PREGNANCY_ANC VALUES (2, 0, ${lmp + 120 * day})",
            "INSERT INTO PREGNANCY_ANC VALUES (2, 0, ${lmp + 150 * day})",
            "INSERT INTO PREGNANCY_ANC VALUES (2, 0, ${lmp + 180 * day})",
            "INSERT INTO PREGNANCY_ANC VALUES (2, 0, ${lmp + 240 * day})"
        )
        assertEquals(listOf("1"), reader.completedMaternalJourneys())
    }

    @Test
    fun `maternal journey works without any ANC tables`() {
        jdbc.exec(
            "CREATE TABLE PREGNANCY_REGISTER (benId INTEGER)",
            "CREATE TABLE DELIVERY_OUTCOME (benId INTEGER, placeOfDelivery TEXT)",
            "INSERT INTO PREGNANCY_REGISTER VALUES (1)",
            "INSERT INTO DELIVERY_OUTCOME VALUES (1, 'District Hospital')"
        )
        assertEquals(emptyList<String>(), reader.completedMaternalJourneys())
    }

    // ── Child Fully Protected ──

    @Test
    fun `fully immunized children cover every first-year child vaccine`() {
        jdbc.exec(
            "CREATE TABLE VACCINE (vaccineId INTEGER, category, immunizationService)",
            "CREATE TABLE IMMUNIZATION (beneficiaryId INTEGER, vaccineId INTEGER, date INTEGER)",
            "INSERT INTO VACCINE VALUES (1, 0, 0)",
            "INSERT INTO VACCINE VALUES (2, 'CHILD', 'WEEK_6')",
            "INSERT INTO VACCINE VALUES (3, 0, 7)", // later stage — not required
            "INSERT INTO VACCINE VALUES (4, 1, 0)", // mother vaccine — not required
            // child 10 complete, child 11 missing a dose, child 12 dose without date
            "INSERT INTO IMMUNIZATION VALUES (10, 1, $now)",
            "INSERT INTO IMMUNIZATION VALUES (10, 2, $now)",
            "INSERT INTO IMMUNIZATION VALUES (11, 1, $now)",
            "INSERT INTO IMMUNIZATION VALUES (12, 1, $now)",
            "INSERT INTO IMMUNIZATION VALUES (12, 2, NULL)"
        )
        assertEquals(listOf("10"), reader.fullyImmunizedChildren())
    }

    @Test
    fun `fully immunized is empty when no vaccine stages resolve`() {
        jdbc.exec(
            "CREATE TABLE VACCINE (vaccineId INTEGER, category, immunizationService)",
            "CREATE TABLE IMMUNIZATION (beneficiaryId INTEGER, vaccineId INTEGER, date INTEGER)",
            "INSERT INTO VACCINE VALUES (1, 1, 9)",
            "INSERT INTO IMMUNIZATION VALUES (10, 1, $now)"
        )
        assertEquals(emptyList<String>(), reader.fullyImmunizedChildren())
    }

    // ── Digital Identity ──

    @Test
    fun `abha generated beneficiaries need a health id number`() {
        jdbc.exec(
            "CREATE TABLE ABHA_GENERATED (beneficiaryID INTEGER, healthIdNumber TEXT)",
            "INSERT INTO ABHA_GENERATED VALUES (1, '12-3456')",
            "INSERT INTO ABHA_GENERATED VALUES (1, '12-3456')",
            "INSERT INTO ABHA_GENERATED VALUES (2, '')",
            "INSERT INTO ABHA_GENERATED VALUES (3, NULL)"
        )
        assertEquals(listOf("1"), reader.abhaGeneratedBens())
    }

    // ── Vulnerable Baby ──

    @Test
    fun `vulnerable babies need all seven HBNC visits across both sources`() {
        jdbc.exec(
            "CREATE TABLE INFANT_REG (childBenId INTEGER, isSNCU TEXT, weight REAL)",
            "CREATE TABLE HBNC (benId INTEGER, homeVisitDate INTEGER)",
            "CREATE TABLE all_visit_history (benId INTEGER, formId TEXT, visitDay TEXT)",
            "INSERT INTO INFANT_REG VALUES (1, 'No', 2200)", // LBW
            "INSERT INTO INFANT_REG VALUES (2, 'Yes', 0)", // SNCU, weight unknown
            "INSERT INTO INFANT_REG VALUES (3, 'No', 0)", // neither — excluded
            "INSERT INTO INFANT_REG VALUES (4, 'No', 3000)" // normal weight — excluded
        )
        // baby 1: days 1,3,7 legacy + 14..42 dynamic
        listOf(1, 3, 7, 0, -1).forEach { jdbc.exec("INSERT INTO HBNC VALUES (1, $it)") }
        listOf("14th Day", "21st Day", "28th Day", "42nd Day").forEach {
            jdbc.exec("INSERT INTO all_visit_history VALUES (1, 'hbnc_form_001', '$it')")
        }
        // baby 2: all seven via the dynamic form, plus a row from another form
        listOf("1st Day", "3rd Day", "7th Day", "14th Day", "21st Day", "28th Day", "42nd Day")
            .forEach { jdbc.exec("INSERT INTO all_visit_history VALUES (2, 'hbnc_form_001', '$it')") }
        jdbc.exec("INSERT INTO all_visit_history VALUES (2, 'other_form', '1st Day')")
        // babies 3 and 4: all seven legacy visits but not vulnerable
        listOf(3, 4).forEach { ben ->
            listOf(1, 3, 7, 14, 21, 28, 42).forEach { jdbc.exec("INSERT INTO HBNC VALUES ($ben, $it)") }
        }
        assertEquals(listOf("1", "2"), reader.vulnerableBabiesCaredFor().sorted())
    }

    @Test
    fun `vulnerable babies works with only the legacy table`() {
        jdbc.exec(
            "CREATE TABLE INFANT_REG (childBenId INTEGER, isSNCU TEXT, weight REAL)",
            "CREATE TABLE HBNC (benId INTEGER, homeVisitDate INTEGER)",
            "INSERT INTO INFANT_REG VALUES (5, 'Yes', NULL)"
        )
        listOf(1, 3, 7, 14, 21, 28, 42).forEach { jdbc.exec("INSERT INTO HBNC VALUES (5, $it)") }
        assertEquals(listOf("5"), reader.vulnerableBabiesCaredFor())
    }

    @Test
    fun `vulnerable babies is empty without any visit source`() {
        jdbc.exec("CREATE TABLE INFANT_REG (childBenId INTEGER, isSNCU TEXT, weight REAL)")
        assertEquals(emptyList<String>(), reader.vulnerableBabiesCaredFor())
    }

    // ── Critical Referral ──

    @Test
    fun `nrc referred children parsed from form json and malformed rows skipped`() {
        jdbc.exec(
            "CREATE TABLE children_under_five_all_visit (benId INTEGER, formDataJson TEXT)",
            """INSERT INTO children_under_five_all_visit VALUES (1, '{"fields":{"nrc_referred":"Yes"}}')""",
            """INSERT INTO children_under_five_all_visit VALUES (1, '{"fields":{"nrc_referred":"Yes"}}')""",
            """INSERT INTO children_under_five_all_visit VALUES (2, '{"fields":{"nrc_referred":"No"}}')""",
            """INSERT INTO children_under_five_all_visit VALUES (3, '{"other":{}}')""",
            "INSERT INTO children_under_five_all_visit VALUES (4, 'not json')"
        )
        assertEquals(listOf("1"), reader.nrcReferredChildren())
    }
}
