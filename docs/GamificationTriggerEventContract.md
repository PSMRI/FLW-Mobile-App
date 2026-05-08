# Gamification Trigger Event Contract

## Purpose

The gamification module should receive workflow events from existing ASHA health flows and apply reward logic separately. This keeps points, streaks, badges, and progress tracking outside core data-entry screens.

## Why This Is Needed

ASHA workflows already cover registration, ANC/PNC visits, child health, NCD screening, immunization, and follow-up activities. Gamification should encourage timely and consistent use of these workflows without rewarding unnecessary raw entries.

A common trigger event contract allows the app to capture local progress offline and reconcile rewards later after sync or validation.

## Event Model

Each workflow event should carry enough context for the gamification layer to decide rewards independently.

| Field | Purpose |
| --- | --- |
| `workflowType` | Source workflow, such as registration, ANC/PNC, immunization, NCD, or child health. |
| `eventAction` | Action completed, such as created, completed, synced, or validated. |
| `localTimestamp` | Time when the event happened on device. |
| `syncStatus` | Local, pending sync, synced, failed, or reconciled state. |
| `recordId` | Beneficiary or workflow record reference. |
| `ashaId` | ASHA or user reference. |
| `validationStatus` | Optional supervisor/CHO validation status when available. |

## Reward Flow

1. Existing workflow completes a meaningful action.
2. Workflow emits a gamification trigger event.
3. Event is stored locally for offline support.
4. Gamification logic calculates provisional progress.
5. Sync or validation updates the event status.
6. Final rewards are reconciled from accepted events.

## Expected Benefits

- Keeps health workflows focused on data entry.
- Keeps gamification modular and testable.
- Supports offline-first reward tracking.
- Reduces risk of rewarding only raw submissions.
- Gives future mechanics one shared integration point.

Related issue: PSMRI/AMRIT#150
