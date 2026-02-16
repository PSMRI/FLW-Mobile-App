package org.piramalswasthya.sakhi.work

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.Operation
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkContinuation
import androidx.work.WorkManager
import org.piramalswasthya.sakhi.work.dynamicWoker.AncHomeVisitPushWorker
import org.piramalswasthya.sakhi.work.dynamicWoker.AncHomeVisitSyncWorker
import org.piramalswasthya.sakhi.work.dynamicWoker.BenIfaFormSyncWorker
import org.piramalswasthya.sakhi.work.dynamicWoker.CUFYIFAFormSyncWorker
import org.piramalswasthya.sakhi.work.dynamicWoker.CUFYORSFormSyncWorker
import org.piramalswasthya.sakhi.work.dynamicWoker.CUFYSAMFormSyncWorker
import org.piramalswasthya.sakhi.work.dynamicWoker.ORSCampaignFormSyncWorker
import org.piramalswasthya.sakhi.work.dynamicWoker.ORSCampaignPushWorker
import org.piramalswasthya.sakhi.work.dynamicWoker.PulsePolioCampaignFormSyncWorker
import org.piramalswasthya.sakhi.work.dynamicWoker.PulsePolioCampaignPushWorker
import org.piramalswasthya.sakhi.work.dynamicWoker.EyeSurgeryFormSyncWorker
import org.piramalswasthya.sakhi.work.dynamicWoker.FilariaMDAFormSyncWorker
import org.piramalswasthya.sakhi.work.dynamicWoker.FilariaMdaCampaignFormSyncWorker
import org.piramalswasthya.sakhi.work.dynamicWoker.FilariaMdaCampaignPushWorker
import org.piramalswasthya.sakhi.work.dynamicWoker.FormSyncWorker
import org.piramalswasthya.sakhi.work.dynamicWoker.MosquitoNetFormSyncWorker
import org.piramalswasthya.sakhi.work.dynamicWoker.NCDFollowUpSyncWorker
import java.util.concurrent.TimeUnit

object WorkerUtils {

    const val syncWorkerUniqueName = "SYNC-WITH-AMRIT"

    private val networkOnlyConstraint = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()

    fun triggerAmritSyncWorker(context: Context) {
        val CUFYORSFormSyncWorker = OneTimeWorkRequestBuilder<CUFYORSFormSyncWorker>()
            .setConstraints(networkOnlyConstraint)
            .build()
        val hbncAndHbyceWorker = OneTimeWorkRequestBuilder<FormSyncWorker>()
            .setConstraints(networkOnlyConstraint)
            .build()
        val CUFYIFAFormSyncWorker = OneTimeWorkRequestBuilder<CUFYIFAFormSyncWorker>()
            .setConstraints(networkOnlyConstraint)
            .build()
        val orsCampaignPushWorker = OneTimeWorkRequestBuilder<ORSCampaignPushWorker>()
            .setConstraints(networkOnlyConstraint)
            .build()
        val pulsePolioCampaignPushWorker = OneTimeWorkRequestBuilder<PulsePolioCampaignPushWorker>()
            .setConstraints(networkOnlyConstraint)
            .build()
        val pushFilariaMdaCampaignFormSyncWorker = OneTimeWorkRequestBuilder<FilariaMdaCampaignPushWorker>()
            .setConstraints(networkOnlyConstraint)
            .build()
        val pullEyeSurgeryFormSyncWorker = OneTimeWorkRequestBuilder<EyeSurgeryFormSyncWorker>()
            .setConstraints(networkOnlyConstraint)
            .build()
        val pullFilariaMDAFormSyncWorker = OneTimeWorkRequestBuilder<FilariaMDAFormSyncWorker>()
            .setConstraints(networkOnlyConstraint)
            .build()
        val pullMosquitoNetFormSyncWorker = OneTimeWorkRequestBuilder<MosquitoNetFormSyncWorker>()
            .setConstraints(networkOnlyConstraint)
            .build()
        val pullBenIfaFormSyncWorker = OneTimeWorkRequestBuilder<BenIfaFormSyncWorker>()
            .setConstraints(networkOnlyConstraint)
            .build()
        val CUFYSAMFormSyncWorker = OneTimeWorkRequestBuilder<CUFYSAMFormSyncWorker>()
            .setConstraints(networkOnlyConstraint)
            .build()

        val NCDFollowupFormSyncWorker = OneTimeWorkRequestBuilder<NCDFollowUpSyncWorker>()
            .setConstraints(networkOnlyConstraint)
            .build()
        val pullWorkRequest = OneTimeWorkRequestBuilder<PullFromAmritWorker>()
            .setConstraints(networkOnlyConstraint)
            .build()
        val generalOpdPullFromAmritWorker = OneTimeWorkRequestBuilder<GeneralOpdPullFromAmritWorker>()
            .setConstraints(networkOnlyConstraint)
            .build()
        val pullAdolescentWorkRequest = OneTimeWorkRequestBuilder<PullAdolescentFromWorker>()
            .setConstraints(networkOnlyConstraint)
            .build()
        val pullIncentiveActivityWorkRequest =
            OneTimeWorkRequestBuilder<PullIncentiveWorker>()
                .setConstraints(networkOnlyConstraint)
                .build()
        val pullCbacWorkRequest = OneTimeWorkRequestBuilder<CbacPullFromAmritWorker>()
            .setConstraints(networkOnlyConstraint)
            .build()

        val pullReferWorkRequest = OneTimeWorkRequestBuilder<ReferPullFromAmritWorker>()
            .setConstraints(networkOnlyConstraint)
            .build()
        val pullHRPWorkRequest = OneTimeWorkRequestBuilder<PullHRPFromAmritWorker>()
            .setConstraints(networkOnlyConstraint)
            .build()

        val pullVLFWorkRequest = OneTimeWorkRequestBuilder<PullVLFFromAmritWorker>()
            .setConstraints(networkOnlyConstraint)
            .build()

        val pullTBWorkRequest = OneTimeWorkRequestBuilder<PullTBFromAmritWorker>()
            .setConstraints(networkOnlyConstraint)
            .build()
        val pullMalariaWorkRequest = OneTimeWorkRequestBuilder<PullMalariaFromAmritWorker>()
            .setConstraints(networkOnlyConstraint)
            .build()
        val pullECWorkRequest = OneTimeWorkRequestBuilder<PullECFromAmritWorker>()
            .setConstraints(networkOnlyConstraint)
            .build()
        val pullImmunizationWorkRequest =
            OneTimeWorkRequestBuilder<PullChildImmunizatonFromAmritWorker>()
                .setConstraints(networkOnlyConstraint)
                .build()
        val pullHBYCFromAmritWorker =
            OneTimeWorkRequestBuilder<PullHBYCFromAmritWorker>()
                .setConstraints(networkOnlyConstraint)
                .build()
        val pullHBNCFromAmritWorker =
            OneTimeWorkRequestBuilder<PullChildHBNCFromAmritWorker>()
                .setConstraints(networkOnlyConstraint)
                .build()
        val pushWorkRequest = OneTimeWorkRequestBuilder<PushToAmritWorker>()
            .setConstraints(networkOnlyConstraint)
            .build()
        val pushCbacWorkRequest = OneTimeWorkRequestBuilder<CbacPushToAmritWorker>()
            .setConstraints(networkOnlyConstraint)
            .build()
        val pushImmunizationWorkRequest =
            OneTimeWorkRequestBuilder<PushChildImmunizationToAmritWorker>()
                .setConstraints(networkOnlyConstraint)
                .build()
        val pushHRPToAmritWorker = OneTimeWorkRequestBuilder<PushHRPToAmritWorker>()
            .setConstraints(networkOnlyConstraint)
            .build()
        val pushVLFToAmritWorker = OneTimeWorkRequestBuilder<PushVLFToAmritWorker>()
            .setConstraints(networkOnlyConstraint)
            .build()


        val pushTBWorkRequest = OneTimeWorkRequestBuilder<PushTBToAmritWorker>()
            .setConstraints(networkOnlyConstraint)
            .build()
        val pushECWorkRequest = OneTimeWorkRequestBuilder<PushECToAmritWorker>()
            .setConstraints(networkOnlyConstraint)
            .build()
        val pushChildHBYCToAmritWorker = OneTimeWorkRequestBuilder<PushChildHBYCToAmritWorker>()
            .setConstraints(networkOnlyConstraint)
            .build()
        val pushChildHBNCToAmritWorker = OneTimeWorkRequestBuilder<PushChildHBNCFromAmritWorker>()
            .setConstraints(networkOnlyConstraint)
            .build()
        val pullAESToAmritWorker = OneTimeWorkRequestBuilder<pullAesFormAmritWorker>()
            .setConstraints(networkOnlyConstraint)
            .build()
        val pullkalaAzarToAmritWorker = OneTimeWorkRequestBuilder<PullKalaAzarFormAmritWorker>()
            .setConstraints(networkOnlyConstraint)
            .build()
        val pullLeprosyToAmritWorker = OneTimeWorkRequestBuilder<PullLeprosyFormAmritWorker>()
            .setConstraints(networkOnlyConstraint)
            .build()
        val pullFilariaToAmritWorker = OneTimeWorkRequestBuilder<PullFilariaFromAmritWorker>()
            .setConstraints(networkOnlyConstraint)
            .build()
        val pushAbhaWorkRequest = OneTimeWorkRequestBuilder<PushMapAbhatoBenficiaryWorker>()
            .setConstraints(networkOnlyConstraint) // if you have constraints
            .build()

        val formSyncWorkerRequest = OneTimeWorkRequestBuilder<FormSyncWorker>()
            .setConstraints(networkOnlyConstraint)
            .build()
        val ancFormSyncWorker = OneTimeWorkRequestBuilder<AncHomeVisitSyncWorker>()
            .setConstraints(networkOnlyConstraint)
            .build()

        val maaMeetingFormSyncWorkerRequest = OneTimeWorkRequestBuilder<MaaMeetingDownsyncWorker>()
            .setConstraints(networkOnlyConstraint)
            .build()

        val pullSaasBahuSamelanAmritWorker = OneTimeWorkRequestBuilder<SaasBahuSammelanPullWorker>()
            .setConstraints(networkOnlyConstraint).build()

        val workManager = WorkManager.getInstance(context)
        workManager
            .beginUniqueWork(
                syncWorkerUniqueName,
                ExistingWorkPolicy.APPEND_OR_REPLACE,
                pullWorkRequest
            )
            .then(ancFormSyncWorker)
            .then(pushFilariaMdaCampaignFormSyncWorker)
            .then(CUFYORSFormSyncWorker)
            .then(hbncAndHbyceWorker)
            .then(CUFYIFAFormSyncWorker)
            .then(orsCampaignPushWorker)
            .then(pulsePolioCampaignPushWorker)
            .then(CUFYSAMFormSyncWorker)
            .then(NCDFollowupFormSyncWorker)
            .then(pullVLFWorkRequest)

            .then(pullEyeSurgeryFormSyncWorker)
            .then(pullFilariaMDAFormSyncWorker)
            .then(pullMosquitoNetFormSyncWorker)
            .then(pullBenIfaFormSyncWorker)
            .then(maaMeetingFormSyncWorkerRequest)
            .then(pullSaasBahuSamelanAmritWorker)
            .then(pullIncentiveActivityWorkRequest)
            .then(pullCbacWorkRequest)
            .then(pullReferWorkRequest)
            .then(pullTBWorkRequest)
            .then(pullECWorkRequest)
            .then(pullImmunizationWorkRequest)
            .then(generalOpdPullFromAmritWorker)
            .then(pullLeprosyToAmritWorker)
//            .then(pullHBYCFromAmritWorker)
            .then(pullHBNCFromAmritWorker)
            .then(pullHRPWorkRequest)
            .then(pullAdolescentWorkRequest)
            .then(pullMalariaWorkRequest)
            .then(pullAESToAmritWorker)
            .then(pullkalaAzarToAmritWorker)
            .then(pullFilariaToAmritWorker)
            .then(pushWorkRequest)
            .then(pushCbacWorkRequest)
            .then(pushImmunizationWorkRequest)
            .then(pushHRPToAmritWorker)
            .then(formSyncWorkerRequest)
            .then(pushTBWorkRequest)
            .then(pushECWorkRequest)
//            .then(pushChildHBYCToAmritWorker)
            .then(pushChildHBNCToAmritWorker)
            .then(pushAbhaWorkRequest)
            .then(pushVLFToAmritWorker)
            .enqueue()
    }

    // ═══════════════════════════════════════════════════════════════════
    // ARCHITECTURE: Grouped Parallel Push Execution (Task-07)
    // ═══════════════════════════════════════════════════════════════════
    //
    // PROBLEM (old approach):
    //   All 42 push workers were chained sequentially via .then(). If ANY
    //   worker returned Result.failure(), the entire chain stopped — all
    //   subsequent workers never executed. One failing entity type (e.g.,
    //   CUFY IFA forms) blocked ALL other entity types from syncing.
    //
    // SOLUTION (new approach):
    //   Workers are organized into 9 dependency-based groups. Groups that
    //   are independent of each other run in PARALLEL via
    //   WorkContinuation.combine(). Within each group, workers chain
    //   sequentially only where entity relationships require it.
    //
    // DEPENDENCY GRAPH:
    //   Group 1 (Registration) — MUST complete first (creates beneficiaryId)
    //     ├── Group 2 (Screening & NCD)       ── parallel after Group 1
    //     ├── Group 3 (Maternal Health)        ── parallel after Group 1
    //     │     └── Group 4 (Child Health)     ── after Group 3 (needs delivery)
    //     │           └── Group 8 (CUFY/U5)    ── after Group 4 (needs child)
    //     ├── Group 5 (Communicable Disease)   ── parallel after Group 1
    //     ├── Group 6 (Community Programs)     ── parallel after Group 1
    //     ├── Group 7 (Campaigns & Forms)      ── parallel after Group 1
    //     └── Group 9 (Digital Health)         ── parallel after Group 1
    //
    // PERFORMANCE:
    //   Before: 42 workers × ~5s each = ~210s sequential (one failure blocks all)
    //   After:  ~4 phases × ~35s each = ~60-90s (groups B-F finish in parallel)
    //   One group's failure does NOT block other groups from syncing.
    //
    // CLEANUP from old code:
    //   - PullIncentiveWorker REMOVED (was incorrectly in push chain)
    //   - Duplicate FormSyncWorker CONSOLIDATED into single instance in Group 4
    //   - PushChildHBYCToAmritWorker remains disabled (was already commented out)
    // ═══════════════════════════════════════════════════════════════════
    fun triggerAmritPushWorker(context: Context) {
        val workManager = WorkManager.getInstance(context)

        // ─────────────────────────────────────────────────────────────
        // GROUP 1: Registration (Foundation)
        // Creates beneficiaryId and beneficiaryRegID that ALL other
        // entities reference. This MUST complete before any other group
        // can start pushing data.
        // ─────────────────────────────────────────────────────────────
        val registration = OneTimeWorkRequestBuilder<PushToAmritWorker>()
            .setConstraints(networkOnlyConstraint)
            .addTag("push_group1_registration")
            .build()

        // Start unique work chain with registration as the anchor.
        // All subsequent chains fan out from this single entry point.
        val afterRegistration = workManager
            .beginUniqueWork(
                syncWorkerUniqueName,
                ExistingWorkPolicy.APPEND_OR_REPLACE,
                registration
            )

        // ─────────────────────────────────────────────────────────────
        // GROUP 2: Screening & NCD
        // All need beneficiary only — no internal dependencies between
        // these workers. Passed as a list to .then() so they all run
        // in PARALLEL within this group.
        // ─────────────────────────────────────────────────────────────
        val group2Screening = listOf(
            OneTimeWorkRequestBuilder<CbacPushToAmritWorker>()
                .setConstraints(networkOnlyConstraint)
                .addTag("push_group2_screening").build(),
            OneTimeWorkRequestBuilder<PushHRPToAmritWorker>()
                .setConstraints(networkOnlyConstraint)
                .addTag("push_group2_screening").build(),
            OneTimeWorkRequestBuilder<PushVLFToAmritWorker>()
                .setConstraints(networkOnlyConstraint)
                .addTag("push_group2_screening").build(),
            OneTimeWorkRequestBuilder<PushAdolescentAmritWorker>()
                .setConstraints(networkOnlyConstraint)
                .addTag("push_group2_screening").build(),
            OneTimeWorkRequestBuilder<NCDReferPushtoAmritWorker>()
                .setConstraints(networkOnlyConstraint)
                .addTag("push_group2_screening").build(),
            OneTimeWorkRequestBuilder<NCDFollowUpSyncWorker>()
                .setConstraints(networkOnlyConstraint)
                .addTag("push_group2_screening").build(),
        )

        // ─────────────────────────────────────────────────────────────
        // GROUP 3: Maternal Health Lifecycle (STRICT sequential ordering)
        // Follows pregnancy lifecycle:
        //   EC Registration → PWR/ANC Registration → ANC Home Visits →
        //   PMSMA Visits → Delivery Outcome → Post-Natal Care → MDSR
        // Each step depends on the previous step's data existing on
        // the server. These MUST run in order.
        // ─────────────────────────────────────────────────────────────
        val group3Ec = OneTimeWorkRequestBuilder<PushECToAmritWorker>()
            .setConstraints(networkOnlyConstraint)
            .addTag("push_group3_maternal").build()
        val group3Pwr = OneTimeWorkRequestBuilder<PushPWRToAmritWorker>()
            .setConstraints(networkOnlyConstraint)
            .addTag("push_group3_maternal").build()
        val group3AncVisit = OneTimeWorkRequestBuilder<AncHomeVisitPushWorker>()
            .setConstraints(networkOnlyConstraint)
            .addTag("push_group3_maternal").build()
        val group3Pmsma = OneTimeWorkRequestBuilder<PushPmsmaToAmritWorker>()
            .setConstraints(networkOnlyConstraint)
            .addTag("push_group3_maternal").build()
        val group3Delivery = OneTimeWorkRequestBuilder<PushDeliveryOutcomeToAmritWorker>()
            .setConstraints(networkOnlyConstraint)
            .addTag("push_group3_maternal").build()
        val group3Pnc = OneTimeWorkRequestBuilder<PushPNCToAmritWorker>()
            .setConstraints(networkOnlyConstraint)
            .addTag("push_group3_maternal").build()
        val group3Mdsr = OneTimeWorkRequestBuilder<PushMdsrToAmritWorker>()
            .setConstraints(networkOnlyConstraint)
            .addTag("push_group3_maternal").build()

        // ─────────────────────────────────────────────────────────────
        // GROUP 4: Child Health (depends on Group 3)
        // Needs delivery outcome from Group 3 to create infant records.
        // Internal ordering: InfantReg must come first, then others
        // chain sequentially as they depend on child records.
        // ─────────────────────────────────────────────────────────────
        val group4InfantReg = OneTimeWorkRequestBuilder<PushInfantRegisterToAmritWorker>()
            .setConstraints(networkOnlyConstraint)
            .addTag("push_group4_child").build()
        val group4Immunization = OneTimeWorkRequestBuilder<PushChildImmunizationToAmritWorker>()
            .setConstraints(networkOnlyConstraint)
            .addTag("push_group4_child").build()
        val group4Uwin = OneTimeWorkRequestBuilder<PushUwinToAmritWorker>()
            .setConstraints(networkOnlyConstraint)
            .addTag("push_group4_child").build()
        val group4Hbnc = OneTimeWorkRequestBuilder<PushChildHBNCFromAmritWorker>()
            .setConstraints(networkOnlyConstraint)
            .addTag("push_group4_child").build()
        // Single FormSyncWorker instance (was duplicated in old code as
        // formSyncWorkerRequest + hbncAndHbyceWorkerrRequest)
        val group4Forms = OneTimeWorkRequestBuilder<FormSyncWorker>()
            .setConstraints(networkOnlyConstraint)
            .addTag("push_group4_child").build()
        val group4Cdr = OneTimeWorkRequestBuilder<PushCdrToAmritWorker>()
            .setConstraints(networkOnlyConstraint)
            .addTag("push_group4_child").build()

        // ─────────────────────────────────────────────────────────────
        // GROUP 5: Communicable Diseases
        // All need beneficiary only — no internal dependencies.
        // Passed as a list → all run in PARALLEL within group.
        // ─────────────────────────────────────────────────────────────
        val group5CommDisease = listOf(
            OneTimeWorkRequestBuilder<PushTBToAmritWorker>()
                .setConstraints(networkOnlyConstraint)
                .addTag("push_group5_comm_disease").build(),
            OneTimeWorkRequestBuilder<PushMalariaAmritWorker>()
                .setConstraints(networkOnlyConstraint)
                .addTag("push_group5_comm_disease").build(),
            OneTimeWorkRequestBuilder<pushAesAmritWorker>()
                .setConstraints(networkOnlyConstraint)
                .addTag("push_group5_comm_disease").build(),
            OneTimeWorkRequestBuilder<pushKalaAzarAmritWorker>()
                .setConstraints(networkOnlyConstraint)
                .addTag("push_group5_comm_disease").build(),
            OneTimeWorkRequestBuilder<pushLeprosyAmritWorker>()
                .setConstraints(networkOnlyConstraint)
                .addTag("push_group5_comm_disease").build(),
            OneTimeWorkRequestBuilder<PushLeprosyFollowUpAmritWorker>()
                .setConstraints(networkOnlyConstraint)
                .addTag("push_group5_comm_disease").build(),
            OneTimeWorkRequestBuilder<PushFilariaAmritWorker>()
                .setConstraints(networkOnlyConstraint)
                .addTag("push_group5_comm_disease").build(),
        )

        // ─────────────────────────────────────────────────────────────
        // GROUP 6: Community Programs
        // Meeting/event data that may not require beneficiary registration.
        // Passed as a list → all run in PARALLEL within group.
        // ─────────────────────────────────────────────────────────────
        val group6Community = listOf(
            OneTimeWorkRequestBuilder<MaaMeetingsPushWorker>()
                .setConstraints(networkOnlyConstraint)
                .addTag("push_group6_community").build(),
            OneTimeWorkRequestBuilder<PushSaasBahuSamelanAmritWorker>()
                .setConstraints(networkOnlyConstraint)
                .addTag("push_group6_community").build(),
        )

        // ─────────────────────────────────────────────────────────────
        // GROUP 7: Campaigns & Dynamic Forms
        // Campaign-specific data collected during health drives.
        // Passed as a list → all run in PARALLEL within group.
        // ─────────────────────────────────────────────────────────────
        val group7Campaigns = listOf(
            OneTimeWorkRequestBuilder<FilariaMdaCampaignPushWorker>()
                .setConstraints(networkOnlyConstraint)
                .addTag("push_group7_campaigns").build(),
            OneTimeWorkRequestBuilder<ORSCampaignPushWorker>()
                .setConstraints(networkOnlyConstraint)
                .addTag("push_group7_campaigns").build(),
            OneTimeWorkRequestBuilder<PulsePolioCampaignPushWorker>()
                .setConstraints(networkOnlyConstraint)
                .addTag("push_group7_campaigns").build(),
            OneTimeWorkRequestBuilder<EyeSurgeryFormSyncWorker>()
                .setConstraints(networkOnlyConstraint)
                .addTag("push_group7_campaigns").build(),
            OneTimeWorkRequestBuilder<FilariaMDAFormSyncWorker>()
                .setConstraints(networkOnlyConstraint)
                .addTag("push_group7_campaigns").build(),
            OneTimeWorkRequestBuilder<MosquitoNetFormSyncWorker>()
                .setConstraints(networkOnlyConstraint)
                .addTag("push_group7_campaigns").build(),
            OneTimeWorkRequestBuilder<BenIfaFormSyncWorker>()
                .setConstraints(networkOnlyConstraint)
                .addTag("push_group7_campaigns").build(),
        )

        // ─────────────────────────────────────────────────────────────
        // GROUP 8: CUFY / Under-5 Nutrition (depends on Group 4)
        // CUFY forms reference child records created in Group 4.
        // Passed as a list → all run in PARALLEL within group.
        // ─────────────────────────────────────────────────────────────
        val group8Cufy = listOf(
            OneTimeWorkRequestBuilder<CUFYORSFormSyncWorker>()
                .setConstraints(networkOnlyConstraint)
                .addTag("push_group8_cufy").build(),
            OneTimeWorkRequestBuilder<CUFYIFAFormSyncWorker>()
                .setConstraints(networkOnlyConstraint)
                .addTag("push_group8_cufy").build(),
            OneTimeWorkRequestBuilder<CUFYSAMFormSyncWorker>()
                .setConstraints(networkOnlyConstraint)
                .addTag("push_group8_cufy").build(),
        )

        // ─────────────────────────────────────────────────────────────
        // GROUP 9: Digital Health & Misc
        // ABHA ID mapping — independent of other entity types.
        // ─────────────────────────────────────────────────────────────
        val group9Abha = OneTimeWorkRequestBuilder<PushMapAbhatoBenficiaryWorker>()
            .setConstraints(networkOnlyConstraint)
            .addTag("push_group9_digital_health").build()

        // ═══════════════════════════════════════════════════════════════
        // ORCHESTRATION: Fan-out after registration
        // ═══════════════════════════════════════════════════════════════
        //
        // Chain A: Registration → Maternal (sequential) → Child (sequential) → CUFY (parallel)
        //   This is the longest chain due to strict lifecycle dependencies.
        //
        // Chains B-F: Registration → [parallel group]
        //   These fan out immediately after registration completes.
        //
        // WorkContinuation.combine() merges all chains — they run in
        // parallel after the shared registration step, and one chain's
        // failure does NOT block the others.
        //
        // TIME FLOW:
        //   Phase 1: Registration
        //   Phase 2: Chains B-F (parallel) + Chain A starts maternal sequence
        //   Phase 3: Chain A continues → Child Health (after maternal done)
        //   Phase 4: Chain A finishes → CUFY (after child done)
        // ═══════════════════════════════════════════════════════════════

        // Chain A: Maternal Health → Child Health → CUFY
        // (sequential lifecycle — each step depends on the previous)
        val chainA = afterRegistration
            .then(group3Ec)
            .then(group3Pwr)
            .then(group3AncVisit)
            .then(group3Pmsma)
            .then(group3Delivery)
            .then(group3Pnc)
            .then(group3Mdsr)
            .then(group4InfantReg)
            .then(group4Immunization)
            .then(group4Uwin)
            .then(group4Hbnc)
            .then(group4Forms)
            .then(group4Cdr)
            .then(group8Cufy) // CUFY workers run in parallel after child health

        // Chain B: Screening & NCD (all workers run in parallel within)
        val chainB = afterRegistration.then(group2Screening)

        // Chain C: Communicable Diseases (all workers run in parallel within)
        val chainC = afterRegistration.then(group5CommDisease)

        // Chain D: Community Programs (all workers run in parallel within)
        val chainD = afterRegistration.then(group6Community)

        // Chain E: Campaigns & Dynamic Forms (all workers run in parallel within)
        val chainE = afterRegistration.then(group7Campaigns)

        // Chain F: Digital Health / ABHA
        val chainF = afterRegistration.then(group9Abha)

        // ─────────────────────────────────────────────────────────────
        // COMBINE & ENQUEUE: All chains fan out in parallel after
        // registration. If Chain C (Communicable Diseases) fails,
        // Chains A/B/D/E/F continue unaffected.
        // ─────────────────────────────────────────────────────────────
        WorkContinuation.combine(listOf(chainA, chainB, chainC, chainD, chainE, chainF))
            .enqueue()

        // NOTE: PullIncentiveWorker was previously mixed into this push
        // chain (old code). It has been REMOVED — pull operations belong
        // in triggerAmritPullWorker() or should be triggered independently.
        //
        // NOTE: Duplicate FormSyncWorker has been CONSOLIDATED into a
        // single invocation in Group 4 (Child Health).
        //
        // NOTE: PushChildHBYCToAmritWorker remains disabled (was already
        // commented out in the original code). Re-enable in Group 4 if needed.
    }

    fun triggerAmritPullWorker(context: Context) {
        val CUFYORSFormSyncWorker = OneTimeWorkRequestBuilder<CUFYORSFormSyncWorker>()
            .setConstraints(networkOnlyConstraint)
            .build()
        val ancPullWorkRequest = OneTimeWorkRequestBuilder<AncHomeVisitSyncWorker>()
            .setConstraints(networkOnlyConstraint)
            .build()
        val hbncAndHbyceWorker = OneTimeWorkRequestBuilder<FormSyncWorker>()
            .setConstraints(networkOnlyConstraint)
            .build()
        val CUFYIFAFormSyncWorker = OneTimeWorkRequestBuilder<CUFYIFAFormSyncWorker>()
            .setConstraints(networkOnlyConstraint)
            .build()
        val orsCampaignFormSyncWorker = OneTimeWorkRequestBuilder<ORSCampaignFormSyncWorker>()
            .setConstraints(networkOnlyConstraint)
            .build()
        val pulsePolioCampaignFormSyncWorker = OneTimeWorkRequestBuilder<PulsePolioCampaignFormSyncWorker>()
            .setConstraints(networkOnlyConstraint)
            .build()
        val CUFYSAMFormSyncWorker = OneTimeWorkRequestBuilder<CUFYSAMFormSyncWorker>()
            .setConstraints(networkOnlyConstraint)
            .build()
            val NCDFollowupFormSyncWorker = OneTimeWorkRequestBuilder<NCDFollowUpSyncWorker>()
            .setConstraints(networkOnlyConstraint)
            .build()

        val pullWorkRequest = OneTimeWorkRequestBuilder<PullFromAmritWorker>()
            .setConstraints(networkOnlyConstraint)
            .build()
        val pullEyeSurgeryFormSyncWorker = OneTimeWorkRequestBuilder<EyeSurgeryFormSyncWorker>()
            .setConstraints(networkOnlyConstraint)
            .build()
        val pullFilariaMDAFormSyncWorker = OneTimeWorkRequestBuilder<FilariaMDAFormSyncWorker>()
            .setConstraints(networkOnlyConstraint)
            .build()
        val pullMosquitoNetFormSyncWorker = OneTimeWorkRequestBuilder<MosquitoNetFormSyncWorker>()
            .setConstraints(networkOnlyConstraint)
            .build()
        val pullBenIfaFormSyncWorker = OneTimeWorkRequestBuilder<BenIfaFormSyncWorker>()
            .setConstraints(networkOnlyConstraint)
            .build()
        val pulladolescentWorkRequest = OneTimeWorkRequestBuilder<PushAdolescentAmritWorker>()
            .setConstraints(networkOnlyConstraint)
            .build()

        val generalOpdPullFromAmritWorker = OneTimeWorkRequestBuilder<GeneralOpdPullFromAmritWorker>()
            .setConstraints(networkOnlyConstraint)
            .build()
        val pullIncentiveActivityWorkRequest =
            OneTimeWorkRequestBuilder<PullIncentiveWorker>()
                .setConstraints(networkOnlyConstraint)
                .build()
        val pullCbacWorkRequest = OneTimeWorkRequestBuilder<CbacPullFromAmritWorker>()
            .setConstraints(networkOnlyConstraint)
            .build()

        val pullReferWorkRequest = OneTimeWorkRequestBuilder<ReferPullFromAmritWorker>()
            .setConstraints(networkOnlyConstraint)
            .build()

        val pullVaccineWorkRequest = OneTimeWorkRequestBuilder<PullVaccinesWorker>()
            .setConstraints(networkOnlyConstraint)
            .build()
        val pullHRPWorkRequest = OneTimeWorkRequestBuilder<PullHRPFromAmritWorker>()
            .setConstraints(networkOnlyConstraint)
            .build()
        val pullVLFWorkRequest = OneTimeWorkRequestBuilder<PullVLFFromAmritWorker>()
            .setConstraints(networkOnlyConstraint)
            .build()
        val maaMeetingFormSyncWorkerRequest = OneTimeWorkRequestBuilder<MaaMeetingDownsyncWorker>()
            .setConstraints(networkOnlyConstraint)
            .build()
        val pullTBWorkRequest = OneTimeWorkRequestBuilder<PullTBFromAmritWorker>()
            .setConstraints(networkOnlyConstraint)
            .build()
        val pullECWorkRequest = OneTimeWorkRequestBuilder<PullECFromAmritWorker>()
            .setConstraints(networkOnlyConstraint)
            .build()
        val pullPWWorkRequest = OneTimeWorkRequestBuilder<PullPWRFromAmritWorker>()
            .setConstraints(networkOnlyConstraint)
            .build()
        val pullPMSMAWorkRequest = OneTimeWorkRequestBuilder<PullPmsmaFromAmritWorker>()
            .setConstraints(networkOnlyConstraint)
            .build()
        val pullPNCWorkRequest = OneTimeWorkRequestBuilder<PullPNCFromAmritWorker>()
            .setConstraints(networkOnlyConstraint)
            .build()
        val pullDeliveryOutcomeWorkRequest =
            OneTimeWorkRequestBuilder<PullDeliveryOutcomeFromAmritWorker>()
                .setConstraints(networkOnlyConstraint)
                .build()
        val pullInfantRegWorkRequest = OneTimeWorkRequestBuilder<PullInfantRegFromAmritWorker>()
            .setConstraints(networkOnlyConstraint)
            .build()
        val pullImmunizationWorkRequest =
            OneTimeWorkRequestBuilder<PullChildImmunizatonFromAmritWorker>()
                .setConstraints(networkOnlyConstraint)
                .build()
        val pullMdsrWorkRequest =
            OneTimeWorkRequestBuilder<PullMdsrFromAmritWorker>()
                .setConstraints(networkOnlyConstraint)
                .build()
        val pullCdrWorkRequest =
            OneTimeWorkRequestBuilder<PullCdrFromAmritWorker>()
                .setConstraints(networkOnlyConstraint)
                .build()
        val pullHBYCFromAmritWorker =
            OneTimeWorkRequestBuilder<PullHBYCFromAmritWorker>()
                .setConstraints(networkOnlyConstraint)
                .build()
        val pullHBNCFromAmritWorker =
            OneTimeWorkRequestBuilder<PullChildHBNCFromAmritWorker>()
                .setConstraints(networkOnlyConstraint)
                .build()
        val pullUwinWorkerRequest = OneTimeWorkRequestBuilder<PullUwinFromAmritWorker>()
            .setConstraints(networkOnlyConstraint)
            .build()
        val pullAshaWorkRequest =
            OneTimeWorkRequestBuilder<PullIncentiveWorker>()
                .setConstraints(networkOnlyConstraint)
                .build()
        val setSyncCompleteWorker = OneTimeWorkRequestBuilder<UpdatePrefForPullCompleteWorker>()
            .build()
        val pullMalariaWorkRequest = OneTimeWorkRequestBuilder<PullMalariaFromAmritWorker>()
            .setConstraints(networkOnlyConstraint)
            .build()
        val pullAESToAmritWorker = OneTimeWorkRequestBuilder<pullAesFormAmritWorker>()
            .setConstraints(networkOnlyConstraint)
            .build()
        val pullkalaAzarToAmritWorker = OneTimeWorkRequestBuilder<PullKalaAzarFormAmritWorker>()
            .setConstraints(networkOnlyConstraint)
            .build()
        val pullLeprosyToAmritWorker = OneTimeWorkRequestBuilder<PullLeprosyFormAmritWorker>()
            .setConstraints(networkOnlyConstraint)
            .build()
        val pullFilariaToAmritWorker = OneTimeWorkRequestBuilder<PullFilariaFromAmritWorker>()
            .setConstraints(networkOnlyConstraint)
            .build()

        val pullFilariaMDACampaignToAmritWorker = OneTimeWorkRequestBuilder<FilariaMdaCampaignFormSyncWorker>()
            .setConstraints(networkOnlyConstraint)
            .build()


        val pullSaasBahuSamelanAmritWorker = OneTimeWorkRequestBuilder<SaasBahuSammelanPullWorker>()
            .setConstraints(networkOnlyConstraint).build()
        val workManager = WorkManager.getInstance(context)
        workManager
            .beginUniqueWork(
                syncWorkerUniqueName,
                ExistingWorkPolicy.APPEND_OR_REPLACE,
                pullWorkRequest
            )
            .then(ancPullWorkRequest)
            .then(pullVLFWorkRequest)
            .then(pullFilariaMDACampaignToAmritWorker)
            .then(maaMeetingFormSyncWorkerRequest)
            .then(pullEyeSurgeryFormSyncWorker)
            .then(pullFilariaMDAFormSyncWorker)
            .then(pullMosquitoNetFormSyncWorker)
            .then(pullBenIfaFormSyncWorker)
            .then(pullSaasBahuSamelanAmritWorker)
            .then(pullCbacWorkRequest)
            .then(pullReferWorkRequest)
            .then(pullIncentiveActivityWorkRequest)
            .then(CUFYORSFormSyncWorker)
            .then(hbncAndHbyceWorker)
            .then(CUFYIFAFormSyncWorker)
            .then(orsCampaignFormSyncWorker)
            .then(pulsePolioCampaignFormSyncWorker)
            .then(CUFYSAMFormSyncWorker)
            .then(NCDFollowupFormSyncWorker)
            .then(pullUwinWorkerRequest)
            .then(pullVaccineWorkRequest)
            .then(pullTBWorkRequest)
            .then(pullECWorkRequest)
            .then(pullPWWorkRequest)
            .then(pullPMSMAWorkRequest)
            .then(pullPNCWorkRequest)
            .then(pullDeliveryOutcomeWorkRequest)
            .then(pullInfantRegWorkRequest)
            .then(pullImmunizationWorkRequest)
            .then(generalOpdPullFromAmritWorker)
            .then(pullMdsrWorkRequest)
            .then(pullCdrWorkRequest)
//            .then(pullHBYCFromAmritWorker)
            .then(pullHBNCFromAmritWorker)
            .then(pullHRPWorkRequest)
            .then(pulladolescentWorkRequest)
            .then(pullMalariaWorkRequest)
            .then(pullAESToAmritWorker)
            .then(pullkalaAzarToAmritWorker)
            .then(pullFilariaToAmritWorker)
            .then(pullLeprosyToAmritWorker)
            .then(setSyncCompleteWorker)
            .enqueue()
    }

    fun triggerD2dSyncWorker(context: Context) {
    }

    fun triggerCbacPullWorker(context: Context) {
        val workRequest = OneTimeWorkRequestBuilder<CbacPullFromAmritWorker>()
            .setConstraints(networkOnlyConstraint)
            .build()
        WorkManager.getInstance(context)
            .enqueueUniqueWork(
                CbacPullFromAmritWorker.name,
                ExistingWorkPolicy.APPEND_OR_REPLACE,
                workRequest
            )
    }

    fun triggerCbacPushWorker(context: Context) {
        val workRequest = OneTimeWorkRequestBuilder<CbacPushToAmritWorker>()
            .setConstraints(networkOnlyConstraint)
            .build()
        WorkManager.getInstance(context)
            .enqueueUniqueWork(
                CbacPushToAmritWorker.name,
                ExistingWorkPolicy.APPEND_OR_REPLACE,
                workRequest
            )
    }

    fun triggerECPushWorker(context: Context) {
        val workRequest = OneTimeWorkRequestBuilder<PushECToAmritWorker>()
            .setConstraints(networkOnlyConstraint)
            .build()
        WorkManager.getInstance(context)
            .enqueueUniqueWork(
                PushECToAmritWorker.name,
                ExistingWorkPolicy.APPEND_OR_REPLACE,
                workRequest
            )
    }

    fun triggerPMSMAPushWorker(context: Context) {
        val workRequest = OneTimeWorkRequestBuilder<PushPmsmaToAmritWorker>()
            .setConstraints(networkOnlyConstraint)
            .build()
        WorkManager.getInstance(context)
            .enqueueUniqueWork(
                PushPmsmaToAmritWorker.name,
                ExistingWorkPolicy.APPEND_OR_REPLACE,
                workRequest
            )
    }

    fun triggerDeliveryOutcomePushWorker(context: Context) {
        val workRequest = OneTimeWorkRequestBuilder<PushDeliveryOutcomeToAmritWorker>()
            .setConstraints(networkOnlyConstraint)
            .build()
        WorkManager.getInstance(context)
            .enqueueUniqueWork(
                PushDeliveryOutcomeToAmritWorker.name,
                ExistingWorkPolicy.APPEND_OR_REPLACE,
                workRequest
            )
    }

    fun triggerInfantRegPushWorker(context: Context) {
        val workRequest = OneTimeWorkRequestBuilder<PushInfantRegisterToAmritWorker>()
            .setConstraints(networkOnlyConstraint)
            .build()
        WorkManager.getInstance(context)
            .enqueueUniqueWork(
                PushInfantRegisterToAmritWorker.name,
                ExistingWorkPolicy.APPEND_OR_REPLACE,
                workRequest
            )
    }

    fun triggerGenBenIdWorker(context: Context) {
        val workRequest = OneTimeWorkRequestBuilder<GenerateBenIdsWorker>()
            .setConstraints(GenerateBenIdsWorker.constraint)
            .build()
        WorkManager.getInstance(context)
            .enqueueUniqueWork(GenerateBenIdsWorker.name, ExistingWorkPolicy.KEEP, workRequest)
    }


    fun triggerUwinWorker(context: Context) {
        val workRequest = OneTimeWorkRequestBuilder<PullUwinFromAmritWorker>()
            .setConstraints(PullUwinFromAmritWorker.constraint)
            .build()
        WorkManager.getInstance(context)
            .enqueueUniqueWork(PullUwinFromAmritWorker.name, ExistingWorkPolicy.KEEP, workRequest)
    }

    fun triggerPeriodicPncEcUpdateWorker(context: Context) {
        val workRequest =
            PeriodicWorkRequest.Builder(UpdatePNCToECWorker::class.java, 1, TimeUnit.DAYS)
                .setConstraints(Constraints.Builder().setRequiresDeviceIdle(true).build())
                .build()
        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(
                UpdatePNCToECWorker.periodicName,
                ExistingPeriodicWorkPolicy.KEEP,
                workRequest
            )
    }

    fun triggerAdHocPncEcUpdateWorker(context: Context) {
        val workRequest = OneTimeWorkRequest.Builder(UpdatePNCToECWorker::class.java).build()
        WorkManager.getInstance(context)
            .enqueueUniqueWork(
                UpdatePNCToECWorker.oneShotName,
                ExistingWorkPolicy.KEEP,
                workRequest
            )
    }

    fun triggerDownloadCardWorker(
        context: Context,
        fileName: String,
        otpTxnID: MutableLiveData<String?>
    ): LiveData<Operation.State> {

        val workRequest = OneTimeWorkRequestBuilder<DownloadCardWorker>()
            .setConstraints(networkOnlyConstraint)
            .setInputData(Data.Builder().apply { putString(DownloadCardWorker.file_name, fileName) }
                .build())
            .build()

        return WorkManager.getInstance(context)
            .enqueueUniqueWork(
                DownloadCardWorker.name,
                ExistingWorkPolicy.REPLACE,
                workRequest
            ).state
    }

    fun cancelAllWork(context: Context) {
        val workManager = WorkManager.getInstance(context)
        workManager.cancelAllWork()
    }

}