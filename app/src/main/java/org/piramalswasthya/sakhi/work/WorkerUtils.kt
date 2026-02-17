package org.piramalswasthya.sakhi.work

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.work.BackoffPolicy
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
import org.piramalswasthya.sakhi.work.dynamicWoker.CUFYIFAPushWorker
import org.piramalswasthya.sakhi.work.dynamicWoker.CUFYORSPushWorker
import org.piramalswasthya.sakhi.work.dynamicWoker.CUFYSAMPushWorker
import java.util.concurrent.TimeUnit

object WorkerUtils {

    const val syncWorkerUniqueName = "SYNC-WITH-AMRIT"  // Legacy (triggerAmritSyncWorker)
    const val pushWorkerUniqueName = "PUSH-TO-AMRIT"
    const val pullWorkerUniqueName = "PULL-FROM-AMRIT"

    private val networkOnlyConstraint = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()

    /** Standard builder: network constraint + exponential backoff (30s → 60s → 120s …). */
    private inline fun <reified W : androidx.work.ListenableWorker> syncRequestBuilder(
    ) = OneTimeWorkRequestBuilder<W>()
        .setConstraints(networkOnlyConstraint)
        .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)

    fun triggerAmritSyncWorker(context: Context) {
        val CUFYORSFormSyncWorker = syncRequestBuilder<CUFYORSFormSyncWorker>()
            .build()
        val hbncAndHbyceWorker = syncRequestBuilder<FormSyncWorker>()
            .build()
        val CUFYIFAFormSyncWorker = syncRequestBuilder<CUFYIFAFormSyncWorker>()
            .build()
        val orsCampaignPushWorker = syncRequestBuilder<ORSCampaignPushWorker>()
            .build()
        val pulsePolioCampaignPushWorker = syncRequestBuilder<PulsePolioCampaignPushWorker>()
            .build()
        val pushFilariaMdaCampaignFormSyncWorker = syncRequestBuilder<FilariaMdaCampaignPushWorker>()
            .build()
        val pullEyeSurgeryFormSyncWorker = syncRequestBuilder<EyeSurgeryFormSyncWorker>()
            .build()
        val pullFilariaMDAFormSyncWorker = syncRequestBuilder<FilariaMDAFormSyncWorker>()
            .build()
        val pullMosquitoNetFormSyncWorker = syncRequestBuilder<MosquitoNetFormSyncWorker>()
            .build()
        val pullBenIfaFormSyncWorker = syncRequestBuilder<BenIfaFormSyncWorker>()
            .build()
        val CUFYSAMFormSyncWorker = syncRequestBuilder<CUFYSAMFormSyncWorker>()
            .build()

        val NCDFollowupFormSyncWorker = syncRequestBuilder<NCDFollowUpSyncWorker>()
            .build()
        val pullWorkRequest = syncRequestBuilder<PullFromAmritWorker>()
            .build()
        val generalOpdPullFromAmritWorker = syncRequestBuilder<GeneralOpdPullFromAmritWorker>()
            .build()
        val pullAdolescentWorkRequest = syncRequestBuilder<PullAdolescentFromWorker>()
            .build()
        val pullIncentiveActivityWorkRequest =
            syncRequestBuilder<PullIncentiveWorker>()
                .build()
        val pullCbacWorkRequest = syncRequestBuilder<CbacPullFromAmritWorker>()
            .build()

        val pullReferWorkRequest = syncRequestBuilder<ReferPullFromAmritWorker>()
            .build()
        val pullHRPWorkRequest = syncRequestBuilder<PullHRPFromAmritWorker>()
            .build()

        val pullVLFWorkRequest = syncRequestBuilder<PullVLFFromAmritWorker>()
            .build()

        val pullTBWorkRequest = syncRequestBuilder<PullTBFromAmritWorker>()
            .build()
        val pullMalariaWorkRequest = syncRequestBuilder<PullMalariaFromAmritWorker>()
            .build()
        val pullECWorkRequest = syncRequestBuilder<PullECFromAmritWorker>()
            .build()
        val pullImmunizationWorkRequest =
            syncRequestBuilder<PullChildImmunizatonFromAmritWorker>()
                .build()
        val pullHBYCFromAmritWorker =
            syncRequestBuilder<PullHBYCFromAmritWorker>()
                .build()
        val pullHBNCFromAmritWorker =
            syncRequestBuilder<PullChildHBNCFromAmritWorker>()
                .build()
        val pushWorkRequest = syncRequestBuilder<PushToAmritWorker>()
            .build()
        val pushCbacWorkRequest = syncRequestBuilder<CbacPushToAmritWorker>()
            .build()
        val pushImmunizationWorkRequest =
            syncRequestBuilder<PushChildImmunizationToAmritWorker>()
                .build()
        val pushHRPToAmritWorker = syncRequestBuilder<PushHRPToAmritWorker>()
            .build()
        val pushVLFToAmritWorker = syncRequestBuilder<PushVLFToAmritWorker>()
            .build()


        val pushTBWorkRequest = syncRequestBuilder<PushTBToAmritWorker>()
            .build()
        val pushECWorkRequest = syncRequestBuilder<PushECToAmritWorker>()
            .build()
        val pushChildHBYCToAmritWorker = syncRequestBuilder<PushChildHBYCToAmritWorker>()
            .build()
        val pushChildHBNCToAmritWorker = syncRequestBuilder<PushChildHBNCFromAmritWorker>()
            .build()
        val pullAESToAmritWorker = syncRequestBuilder<pullAesFormAmritWorker>()
            .build()
        val pullkalaAzarToAmritWorker = syncRequestBuilder<PullKalaAzarFormAmritWorker>()
            .build()
        val pullLeprosyToAmritWorker = syncRequestBuilder<PullLeprosyFormAmritWorker>()
            .build()
        val pullFilariaToAmritWorker = syncRequestBuilder<PullFilariaFromAmritWorker>()
            .build()
        val pushAbhaWorkRequest = syncRequestBuilder<PushMapAbhatoBenficiaryWorker>()
            .build()

        val formSyncWorkerRequest = syncRequestBuilder<FormSyncWorker>()
            .build()
        val ancFormSyncWorker = syncRequestBuilder<AncHomeVisitSyncWorker>()
            .build()

        val maaMeetingFormSyncWorkerRequest = syncRequestBuilder<MaaMeetingDownsyncWorker>()
            .build()

        val pullSaasBahuSamelanAmritWorker = syncRequestBuilder<SaasBahuSammelanPullWorker>().build()

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
        val registration = syncRequestBuilder<PushToAmritWorker>()
            .addTag("push_group1_registration")
            .build()

        // Start unique work chain with registration as the anchor.
        // All subsequent chains fan out from this single entry point.
        // KEEP: If a push cycle is already running/enqueued, skip this request.
        // The running cycle will pick up any new data. This prevents the
        // feedback loop where each DB change re-triggers the entire chain.
        val afterRegistration = workManager
            .beginUniqueWork(
                pushWorkerUniqueName,
                ExistingWorkPolicy.KEEP,
                registration
            )

        // ─────────────────────────────────────────────────────────────
        // GROUP 2: Screening & NCD
        // All need beneficiary only — no internal dependencies between
        // these workers. Passed as a list to .then() so they all run
        // in PARALLEL within this group.
        // ─────────────────────────────────────────────────────────────
        val group2Screening = listOf(
            syncRequestBuilder<CbacPushToAmritWorker>()
                .addTag("push_group2_screening").build(),
            syncRequestBuilder<PushHRPToAmritWorker>()
                .addTag("push_group2_screening").build(),
            syncRequestBuilder<PushVLFToAmritWorker>()
                .addTag("push_group2_screening").build(),
            syncRequestBuilder<PushAdolescentAmritWorker>()
                .addTag("push_group2_screening").build(),
            syncRequestBuilder<NCDReferPushtoAmritWorker>()
                .addTag("push_group2_screening").build(),
            syncRequestBuilder<NCDFollowUpSyncWorker>()
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
        val group3Ec = syncRequestBuilder<PushECToAmritWorker>()
            .addTag("push_group3_maternal").build()
        val group3Pwr = syncRequestBuilder<PushPWRToAmritWorker>()
            .addTag("push_group3_maternal").build()
        val group3AncVisit = syncRequestBuilder<AncHomeVisitPushWorker>()
            .addTag("push_group3_maternal").build()
        val group3Pmsma = syncRequestBuilder<PushPmsmaToAmritWorker>()
            .addTag("push_group3_maternal").build()
        val group3Delivery = syncRequestBuilder<PushDeliveryOutcomeToAmritWorker>()
            .addTag("push_group3_maternal").build()
        val group3Pnc = syncRequestBuilder<PushPNCToAmritWorker>()
            .addTag("push_group3_maternal").build()
        val group3Mdsr = syncRequestBuilder<PushMdsrToAmritWorker>()
            .addTag("push_group3_maternal").build()

        // ─────────────────────────────────────────────────────────────
        // GROUP 4: Child Health (depends on Group 3)
        // Needs delivery outcome from Group 3 to create infant records.
        // Internal ordering: InfantReg must come first, then others
        // chain sequentially as they depend on child records.
        // ─────────────────────────────────────────────────────────────
        val group4InfantReg = syncRequestBuilder<PushInfantRegisterToAmritWorker>()
            .addTag("push_group4_child").build()
        val group4Immunization = syncRequestBuilder<PushChildImmunizationToAmritWorker>()
            .addTag("push_group4_child").build()
        val group4Uwin = syncRequestBuilder<PushUwinToAmritWorker>()
            .addTag("push_group4_child").build()
        val group4Hbnc = syncRequestBuilder<PushChildHBNCFromAmritWorker>()
            .addTag("push_group4_child").build()
        // Single FormSyncWorker instance (was duplicated in old code as
        // formSyncWorkerRequest + hbncAndHbyceWorkerrRequest)
        val group4Forms = syncRequestBuilder<FormSyncWorker>()
            .addTag("push_group4_child").build()
        val group4Cdr = syncRequestBuilder<PushCdrToAmritWorker>()
            .addTag("push_group4_child").build()

        // ─────────────────────────────────────────────────────────────
        // GROUP 5: Communicable Diseases
        // All need beneficiary only — no internal dependencies.
        // Passed as a list → all run in PARALLEL within group.
        // ─────────────────────────────────────────────────────────────
        val group5CommDisease = listOf(
            syncRequestBuilder<PushTBToAmritWorker>()
                .addTag("push_group5_comm_disease").build(),
            syncRequestBuilder<PushMalariaAmritWorker>()
                .addTag("push_group5_comm_disease").build(),
            syncRequestBuilder<pushAesAmritWorker>()
                .addTag("push_group5_comm_disease").build(),
            syncRequestBuilder<pushKalaAzarAmritWorker>()
                .addTag("push_group5_comm_disease").build(),
            syncRequestBuilder<pushLeprosyAmritWorker>()
                .addTag("push_group5_comm_disease").build(),
            syncRequestBuilder<PushLeprosyFollowUpAmritWorker>()
                .addTag("push_group5_comm_disease").build(),
            syncRequestBuilder<PushFilariaAmritWorker>()
                .addTag("push_group5_comm_disease").build(),
        )

        // ─────────────────────────────────────────────────────────────
        // GROUP 6: Community Programs
        // Meeting/event data that may not require beneficiary registration.
        // Passed as a list → all run in PARALLEL within group.
        // ─────────────────────────────────────────────────────────────
        val group6Community = listOf(
            syncRequestBuilder<MaaMeetingsPushWorker>()
                .addTag("push_group6_community").build(),
            syncRequestBuilder<PushSaasBahuSamelanAmritWorker>()
                .addTag("push_group6_community").build(),
        )

        // ─────────────────────────────────────────────────────────────
        // GROUP 7: Campaigns & Dynamic Forms
        // Campaign-specific data collected during health drives.
        // Passed as a list → all run in PARALLEL within group.
        // ─────────────────────────────────────────────────────────────
        val group7Campaigns = listOf(
            syncRequestBuilder<FilariaMdaCampaignPushWorker>()
                .addTag("push_group7_campaigns").build(),
            syncRequestBuilder<ORSCampaignPushWorker>()
                .addTag("push_group7_campaigns").build(),
            syncRequestBuilder<PulsePolioCampaignPushWorker>()
                .addTag("push_group7_campaigns").build(),
            syncRequestBuilder<EyeSurgeryFormSyncWorker>()
                .addTag("push_group7_campaigns").build(),
            syncRequestBuilder<FilariaMDAFormSyncWorker>()
                .addTag("push_group7_campaigns").build(),
            syncRequestBuilder<MosquitoNetFormSyncWorker>()
                .addTag("push_group7_campaigns").build(),
            syncRequestBuilder<BenIfaFormSyncWorker>()
                .addTag("push_group7_campaigns").build(),
        )

        // ─────────────────────────────────────────────────────────────
        // GROUP 8: CUFY / Under-5 Nutrition (depends on Group 4)
        // CUFY forms reference child records created in Group 4.
        // Passed as a list → all run in PARALLEL within group.
        // ─────────────────────────────────────────────────────────────
        val group8Cufy = listOf(
            syncRequestBuilder<CUFYORSPushWorker>()
                .addTag("push_group8_cufy").build(),
            syncRequestBuilder<CUFYIFAPushWorker>()
                .addTag("push_group8_cufy").build(),
            syncRequestBuilder<CUFYSAMPushWorker>()
                .addTag("push_group8_cufy").build(),
        )

        // ─────────────────────────────────────────────────────────────
        // GROUP 9: Digital Health & Misc
        // ABHA ID mapping — independent of other entity types.
        // ─────────────────────────────────────────────────────────────
        val group9Abha = syncRequestBuilder<PushMapAbhatoBenficiaryWorker>()
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
        // FINAL: Pull Incentive (runs AFTER all push groups complete)
        // Fetches incentive/activity data once all entities have been
        // pushed. Placed here because the server calculates incentives
        // based on pushed data — must run after all pushes finish.
        // ─────────────────────────────────────────────────────────────
        val pullIncentive = syncRequestBuilder<PullIncentiveWorker>()
            .addTag("push_final_incentive").build()

        // ─────────────────────────────────────────────────────────────
        // COMBINE & ENQUEUE: All chains fan out in parallel after
        // registration. If Chain C (Communicable Diseases) fails,
        // Chains A/B/D/E/F continue unaffected. PullIncentiveWorker
        // runs after ALL chains complete.
        // ─────────────────────────────────────────────────────────────
        WorkContinuation.combine(listOf(chainA, chainB, chainC, chainD, chainE, chainF))
            .then(pullIncentive)
            .enqueue()
        //
        // NOTE: Duplicate FormSyncWorker has been CONSOLIDATED into a
        // single invocation in Group 4 (Child Health).
        //
        // NOTE: PushChildHBYCToAmritWorker remains disabled (was already
        // commented out in the original code). Re-enable in Group 4 if needed.
    }

    fun triggerAmritPullWorker(context: Context) {
        val CUFYORSFormSyncWorker = syncRequestBuilder<CUFYORSFormSyncWorker>()
            .build()
        val ancPullWorkRequest = syncRequestBuilder<AncHomeVisitSyncWorker>()
            .build()
        val hbncAndHbyceWorker = syncRequestBuilder<FormSyncWorker>()
            .build()
        val CUFYIFAFormSyncWorker = syncRequestBuilder<CUFYIFAFormSyncWorker>()
            .build()
        val orsCampaignFormSyncWorker = syncRequestBuilder<ORSCampaignFormSyncWorker>()
            .build()
        val pulsePolioCampaignFormSyncWorker = syncRequestBuilder<PulsePolioCampaignFormSyncWorker>()
            .build()
        val CUFYSAMFormSyncWorker = syncRequestBuilder<CUFYSAMFormSyncWorker>()
            .build()
            val NCDFollowupFormSyncWorker = syncRequestBuilder<NCDFollowUpSyncWorker>()
            .build()

        val pullWorkRequest = syncRequestBuilder<PullFromAmritWorker>()
            .build()
        val pullEyeSurgeryFormSyncWorker = syncRequestBuilder<EyeSurgeryFormSyncWorker>()
            .build()
        val pullFilariaMDAFormSyncWorker = syncRequestBuilder<FilariaMDAFormSyncWorker>()
            .build()
        val pullMosquitoNetFormSyncWorker = syncRequestBuilder<MosquitoNetFormSyncWorker>()
            .build()
        val pullBenIfaFormSyncWorker = syncRequestBuilder<BenIfaFormSyncWorker>()
            .build()
        val generalOpdPullFromAmritWorker = syncRequestBuilder<GeneralOpdPullFromAmritWorker>()
            .build()
        val pullIncentiveActivityWorkRequest =
            syncRequestBuilder<PullIncentiveWorker>()
                .build()
        val pullCbacWorkRequest = syncRequestBuilder<CbacPullFromAmritWorker>()
            .build()

        val pullReferWorkRequest = syncRequestBuilder<ReferPullFromAmritWorker>()
            .build()

        val pullVaccineWorkRequest = syncRequestBuilder<PullVaccinesWorker>()
            .build()
        val pullHRPWorkRequest = syncRequestBuilder<PullHRPFromAmritWorker>()
            .build()
        val pullVLFWorkRequest = syncRequestBuilder<PullVLFFromAmritWorker>()
            .build()
        val maaMeetingFormSyncWorkerRequest = syncRequestBuilder<MaaMeetingDownsyncWorker>()
            .build()
        val pullTBWorkRequest = syncRequestBuilder<PullTBFromAmritWorker>()
            .build()
        val pullECWorkRequest = syncRequestBuilder<PullECFromAmritWorker>()
            .build()
        val pullPWWorkRequest = syncRequestBuilder<PullPWRFromAmritWorker>()
            .build()
        val pullPMSMAWorkRequest = syncRequestBuilder<PullPmsmaFromAmritWorker>()
            .build()
        val pullPNCWorkRequest = syncRequestBuilder<PullPNCFromAmritWorker>()
            .build()
        val pullDeliveryOutcomeWorkRequest =
            syncRequestBuilder<PullDeliveryOutcomeFromAmritWorker>()
                .build()
        val pullInfantRegWorkRequest = syncRequestBuilder<PullInfantRegFromAmritWorker>()
            .build()
        val pullImmunizationWorkRequest =
            syncRequestBuilder<PullChildImmunizatonFromAmritWorker>()
                .build()
        val pullMdsrWorkRequest =
            syncRequestBuilder<PullMdsrFromAmritWorker>()
                .build()
        val pullCdrWorkRequest =
            syncRequestBuilder<PullCdrFromAmritWorker>()
                .build()
        val pullHBYCFromAmritWorker =
            syncRequestBuilder<PullHBYCFromAmritWorker>()
                .build()
        val pullHBNCFromAmritWorker =
            syncRequestBuilder<PullChildHBNCFromAmritWorker>()
                .build()
        val pullUwinWorkerRequest = syncRequestBuilder<PullUwinFromAmritWorker>()
            .build()
        val setSyncCompleteWorker = OneTimeWorkRequestBuilder<UpdatePrefForPullCompleteWorker>()
            .build()
        val pullMalariaWorkRequest = syncRequestBuilder<PullMalariaFromAmritWorker>()
            .build()
        val pullAESToAmritWorker = syncRequestBuilder<pullAesFormAmritWorker>()
            .build()
        val pullkalaAzarToAmritWorker = syncRequestBuilder<PullKalaAzarFormAmritWorker>()
            .build()
        val pullLeprosyToAmritWorker = syncRequestBuilder<PullLeprosyFormAmritWorker>()
            .build()
        val pullFilariaToAmritWorker = syncRequestBuilder<PullFilariaFromAmritWorker>()
            .build()

        val pullFilariaMDACampaignToAmritWorker = syncRequestBuilder<FilariaMdaCampaignFormSyncWorker>()
            .build()


        val pullSaasBahuSamelanAmritWorker = syncRequestBuilder<SaasBahuSammelanPullWorker>().build()
        val workManager = WorkManager.getInstance(context)
        // KEEP: If a pull cycle is already running/enqueued, skip this request.
        // Separate name from push so push/pull don't cancel each other.
        workManager
            .beginUniqueWork(
                pullWorkerUniqueName,
                ExistingWorkPolicy.KEEP,
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
        val workRequest = syncRequestBuilder<CbacPullFromAmritWorker>()
            .build()
        WorkManager.getInstance(context)
            .enqueueUniqueWork(
                CbacPullFromAmritWorker.name,
                ExistingWorkPolicy.APPEND_OR_REPLACE,
                workRequest
            )
    }

    fun triggerCbacPushWorker(context: Context) {
        val workRequest = syncRequestBuilder<CbacPushToAmritWorker>()
            .build()
        WorkManager.getInstance(context)
            .enqueueUniqueWork(
                CbacPushToAmritWorker.name,
                ExistingWorkPolicy.APPEND_OR_REPLACE,
                workRequest
            )
    }

    fun triggerECPushWorker(context: Context) {
        val workRequest = syncRequestBuilder<PushECToAmritWorker>()
            .build()
        WorkManager.getInstance(context)
            .enqueueUniqueWork(
                PushECToAmritWorker.name,
                ExistingWorkPolicy.APPEND_OR_REPLACE,
                workRequest
            )
    }

    fun triggerPMSMAPushWorker(context: Context) {
        val workRequest = syncRequestBuilder<PushPmsmaToAmritWorker>()
            .build()
        WorkManager.getInstance(context)
            .enqueueUniqueWork(
                PushPmsmaToAmritWorker.name,
                ExistingWorkPolicy.APPEND_OR_REPLACE,
                workRequest
            )
    }

    fun triggerDeliveryOutcomePushWorker(context: Context) {
        val workRequest = syncRequestBuilder<PushDeliveryOutcomeToAmritWorker>()
            .build()
        WorkManager.getInstance(context)
            .enqueueUniqueWork(
                PushDeliveryOutcomeToAmritWorker.name,
                ExistingWorkPolicy.APPEND_OR_REPLACE,
                workRequest
            )
    }

    fun triggerInfantRegPushWorker(context: Context) {
        val workRequest = syncRequestBuilder<PushInfantRegisterToAmritWorker>()
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

        val workRequest = syncRequestBuilder<DownloadCardWorker>()
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