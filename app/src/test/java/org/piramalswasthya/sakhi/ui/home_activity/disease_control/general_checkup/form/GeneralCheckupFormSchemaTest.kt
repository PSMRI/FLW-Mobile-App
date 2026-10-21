package org.piramalswasthya.sakhi.ui.home_activity.disease_control.general_checkup.form

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.piramalswasthya.sakhi.model.HealthProblemOwnerModule
import org.piramalswasthya.sakhi.model.dynamicEntity.FormSchemaDto

class GeneralCheckupFormSchemaTest {

    private fun fixture(): FormSchemaDto = FormSchemaDto.fromJson(
        javaClass.classLoader!!.getResource("general_check_up_schema.json").readText()
    )

    private fun schema(json: String) = FormSchemaDto.fromJson(json)

    @Test
    fun `sample schema gives every type in server order with its problems`() {
        val form = GeneralCheckupFormSchema.parse(fixture())!!
        assertEquals(
            listOf("g11", "g6", "g13", "g14", "g3", "g4", "g8", "g9", "g10", "g12", "g99"),
            form.problemTypes.map { it.typeId }
        )
        assertEquals("problems_g6", form.problemType("g6")!!.problemFieldId)
        assertEquals(1, form.version)
    }

    @Test
    fun `extra option keys survive the engine's label-value options`() {
        val form = GeneralCheckupFormSchema.parse(fixture())!!
        val emergency = form.problemType("g11")!!.problems.first()
        assertEquals("e_chest", emergency.problemId)
        assertTrue(emergency.emergency)

        val womens = form.problemType("g4")!!.problems.associateBy { it.problemId }
        assertEquals(HealthProblemOwnerModule.ECL, womens["contra"]!!.ownerModule)
        assertEquals(HealthProblemOwnerModule.MATERNAL, womens["preg"]!!.ownerModule)
        assertNull(womens["g4_mens"]!!.ownerModule)

        val other = form.problemType("g6")!!.problems.last()
        assertEquals("g6_other", other.problemId)
        assertTrue(other.needsText)
        assertFalse(form.problemType("g6")!!.problems.first().needsText)
    }

    @Test
    fun `type with only Other is kept`() {
        val type = GeneralCheckupFormSchema.parse(fixture())!!.problemType("g99")!!
        assertEquals(listOf("g99_other"), type.problems.map { it.problemId })
    }

    @Test
    fun `labelEn falls back to the label`() {
        val form = GeneralCheckupFormSchema.parse(
            schema(
                """{"formId":"general_check_up","formName":"GC","version":3,"sections":[{"fields":[
                {"fieldId":"problem_type","type":"dropdown","options":[{"value":"t1","label":"बुखार"}]},
                {"fieldId":"problems_t1","type":"multicheckbox","conditional":{"dependsOn":"problem_type","expectedValue":"t1"},
                 "options":[{"value":"p1","label":"सिरदर्द"}]}]}]}"""
            )
        )!!
        assertEquals("बुखार", form.problemType("t1")!!.labelEn)
        assertEquals("सिरदर्द", form.problemType("t1")!!.problems.single().labelEn)
        assertEquals(3, form.version)
    }

    @Test
    fun `numeric values and string flags are read`() {
        val form = GeneralCheckupFormSchema.parse(
            schema(
                """{"formId":"general_check_up","formName":"GC","sections":[{"fields":[
                {"fieldId":"problem_type","type":"dropdown","options":[{"value":5,"label":"Type"}]},
                {"fieldId":"problems_5","type":"multicheckbox","conditional":{"dependsOn":"problem_type","expectedValue":"5"},
                 "options":[{"value":12,"label":"P","isEmergency":"true","requiresText":1,"ownerModule":"cataract"}]}]}]}"""
            )
        )!!
        val problem = form.problemType("5")!!.problems.single()
        assertEquals("12", problem.problemId)
        assertTrue(problem.emergency)
        assertTrue(problem.needsText)
        assertEquals(HealthProblemOwnerModule.CATARACT, problem.ownerModule)
    }

    @Test
    fun `hidden types and problems are left out of new check-ups`() {
        val form = GeneralCheckupFormSchema.parse(
            schema(
                """{"formId":"general_check_up","formName":"GC","sections":[{"fields":[
                {"fieldId":"problem_type","type":"dropdown","options":[
                  {"value":"a","label":"A"},{"value":"b","label":"B","isActive":false}]},
                {"fieldId":"problems_a","type":"multicheckbox","conditional":{"dependsOn":"problem_type","expectedValue":"a"},
                 "options":[{"value":"p1","label":"P1","isActive":true},{"value":"p2","label":"P2","isActive":false},{"value":"p3","label":"P3"}]},
                {"fieldId":"problems_b","type":"multicheckbox","conditional":{"dependsOn":"problem_type","expectedValue":"b"},
                 "options":[{"value":"q","label":"Q"}]}]}]}"""
            )
        )!!
        assertEquals(listOf("a"), form.problemTypes.map { it.typeId })
        assertEquals(listOf("p1", "p3"), form.problemType("a")!!.problems.map { it.problemId })
    }

    @Test
    fun `type without a problem list is dropped`() {
        val form = GeneralCheckupFormSchema.parse(
            schema(
                """{"formId":"general_check_up","formName":"GC","sections":[{"fields":[
                {"fieldId":"problem_type","type":"dropdown","options":[{"value":"a","label":"A"},{"value":"b","label":"B"}]},
                {"fieldId":"problems_a","type":"multicheckbox","conditional":{"dependsOn":"problem_type","expectedValue":"a"},
                 "options":[{"value":"p","label":"P"}]}]}]}"""
            )
        )!!
        assertEquals(listOf("a"), form.problemTypes.map { it.typeId })
        assertNotNull(form.typeOfProblemField("problems_a"))
        assertNull(form.typeOfProblemField("problem_type"))
    }

    @Test
    fun `schema without problem_type is not usable`() {
        assertNull(
            GeneralCheckupFormSchema.parse(
                schema("""{"formId":"general_check_up","formName":"GC","sections":[{"fields":[{"fieldId":"checkup_date","type":"date"}]}]}""")
            )
        )
    }

    @Test
    fun `schema with types but no problems is not usable`() {
        assertNull(
            GeneralCheckupFormSchema.parse(
                schema(
                    """{"formId":"general_check_up","formName":"GC","sections":[{"fields":[
                    {"fieldId":"problem_type","type":"dropdown","options":[{"value":"a","label":"A"}]}]}]}"""
                )
            )
        )
    }

    @Test
    fun `schema survives the form_schema save and reload round trip`() {
        val reloaded = FormSchemaDto.fromJson(fixture().toJson())
        val form = GeneralCheckupFormSchema.parse(reloaded)!!
        assertTrue(form.problemType("g11")!!.problems.filterNot { it.needsText }.all { it.emergency })
        assertEquals(HealthProblemOwnerModule.CATARACT, form.problemType("g8")!!.problems.first { it.problemId == "cat" }.ownerModule)
    }

    // Shape the UAT server sent on 22 Oct 2026: BRD rows as fields, no options.
    private val serverDraft = """{"formId":"general_check_up","formName":"General Check-up","version":1,"sections":[{"fields":[
        {"fieldId":"check_up_date","type":"date"},
        {"fieldId":"type_of_problem","type":"dropdown","options":null},
        {"fieldId":"problem","type":"multi-select","options":null}]}]}"""

    @Test
    fun `server draft is not usable and every problem is named`() {
        val draft = schema(serverDraft)
        assertNull(GeneralCheckupFormSchema.parse(draft))
        val issues = GeneralCheckupFormSchema.issues(draft)
        assertTrue(issues.any { it.startsWith("Field \"checkup_date\" is missing") })
        assertTrue(issues.any { it.startsWith("Field \"problem_type\" is missing") })
        assertTrue(issues.any { it.startsWith("Field \"other_text\" is missing") })
        assertTrue(issues.any { it.startsWith("No problem lists found") })
        assertTrue(issues.any { it.startsWith("Field \"problem\" has type \"multi-select\"") && it.contains("Use \"multicheckbox\"") })
    }

    @Test
    fun `report numbers the problems and lists the fields received`() {
        val report = GeneralCheckupFormSchema.issueReport(schema(serverDraft))
        assertTrue(report, report.startsWith("Form \"general_check_up\" version 1: 5 problem(s)\n1. "))
        assertTrue(report, report.endsWith("Fields received: check_up_date (date), type_of_problem (dropdown), problem (multi-select)"))
    }

    @Test
    fun `problem_type without options is reported`() {
        val issues = GeneralCheckupFormSchema.issues(
            schema("""{"formId":"general_check_up","formName":"GC","sections":[{"fields":[{"fieldId":"problem_type","type":"dropdown"}]}]}""")
        )
        assertTrue(issues.any { it.startsWith("Field \"problem_type\" has no options") })
    }

    @Test
    fun `type without a list and list without Other are reported`() {
        val issues = GeneralCheckupFormSchema.issues(
            schema(
                """{"formId":"general_check_up","formName":"GC","sections":[{"fields":[
                {"fieldId":"checkup_date","type":"date"},{"fieldId":"other_text","type":"text"},
                {"fieldId":"problem_type","type":"dropdown","options":[{"value":"a","label":"A"},{"value":"b","label":"B"}]},
                {"fieldId":"problems_a","type":"multicheckbox","conditional":{"dependsOn":"problem_type","expectedValue":"a"},
                 "options":[{"value":"p","label":"P","labelEn":"P"}]}]}]}"""
            )
        )
        assertEquals(
            listOf(
                "Problem types \"b\" have no problem list. Add a \"multicheckbox\" field with conditional.expectedValue = that type ID.",
                "Field \"problems_a\" has no \"Other\" option. Add {\"value\": \"<typeId>_other\", \"label\": \"Other\", \"labelEn\": \"Other\", \"requiresText\": true}."
            ),
            issues
        )
    }

    @Test
    fun `agreed sample has no problems`() {
        assertEquals(emptyList<String>(), GeneralCheckupFormSchema.issues(fixture()))
    }

    @Test
    fun `real UAT response of 22 Oct is reported`() {
        val uat = javaClass.classLoader!!.getResource("general_check_up_uat_2026-10-22.json").readText()
        val data = com.google.gson.JsonParser.parseString(uat).asJsonObject["data"].toString()
        val issues = GeneralCheckupFormSchema.issues(FormSchemaDto.fromJson(data))
        assertTrue(issues.joinToString("\n"), issues.any { it.startsWith("Field \"problem_type\" is missing") })
        assertTrue(issues.any { it.startsWith("Field \"priority\" has type \"auto\"") })
        assertTrue(issues.any { it.startsWith("Field \"refer_to_facility\" has type \"button\"") })
    }
}
