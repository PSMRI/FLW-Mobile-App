# Form & card content audit (design vs code)

> Generated 2026-11-04 by `design/audit/extract_forms.py` + `extract_cards.py` from `configuration/*Dataset.kt` and `res/layout/rv_item_*.xml`. Re-run both scripts + rebuild the plugin after any form/card change in code.

## 1. Result

- **Hand-drawn module screens (stages 2.2–2.13)** contained **327 of 1147 form fields (28%)** — they were representative examples, so most forms were incomplete.
- **Fixed:** Figma now has **complete form specs for all 49 forms / 1147 fields** (sections *2.15* A–M and *2.16* N–Z), generated directly from the datasets: every field in code order, real input type, required `*`, all options, and conditional fields grouped in blue blocks.
- **Cards:** the redesigned cards showed only name / meta / tag / one action, while the code cards show up to 15 labelled values. **Fixed:** section *2.17* has a complete card spec for every list-card layout with labels (42 layouts): all labels as a 2-column grid, flags as tags, same buttons.
- **Design pass:** the complete forms and cards are drawn in the same visual language as the module screens (section cards with icons, Yes/No buttons, chips, steppers, leading icons, auto-filled tiles), not as plain field lists.
- **Rule for development:** the complete specs (2.15–2.17) are the source of truth for *what* is shown; the module screens (2.2–2.13) are the source of truth for *how* it looks.

Notes: "shown on condition" is detected statically (fields not in the first page list of `setUpPage`) and is approximate; flavour-specific fields (Mitanin etc.) are included in the spec.

## 2. Forms summary

| Form (dataset) | Fields | Always | On condition | In hand-drawn screen | Figma spec frame |
|---|---|---|---|---|---|
| AES / JE form (`AESJEFormDataset`) | 11 | 6 | 5 | 7 | `F-AESJEFormDataset` |
| AHD meeting (`AHDDataset`) | 5 | 3 | 2 | 3 | `F-AHDDataset` |
| Adolescent health (`AdolescentHealthFormDataset`) | 15 | 15 | 0 | 6 | `F-AdolescentHealthFormDataset` |
| ASHA profile (`AshaProfileDataset`) | 31 | 29 | 2 | 11 | `F-AshaProfileDataset` |
| General registration (15+) (`BenGenRegFormDataset`) | 28 | 28 | 0 | 13 | `F-BenGenRegFormDataset` |
| CHO beneficiary registration (`BenRegCHODataset`) | 8 | 8 | 0 | 8 | `F-BenRegCHODataset` |
| Beneficiary registration (`BenRegFormDataset`) | 70 | 70 | 0 | 26 | `F-BenRegFormDataset` |
| Child death report (CDR) (`CDRFormDataset`) | 27 | 24 | 3 | 15 | `F-CDRFormDataset` |
| Child registration (`ChildRegistrationDataset`) | 17 | 17 | 0 | 8 | `F-ChildRegistrationDataset` |
| Delivery outcome (`DeliveryOutcomeDataset`) | 21 | 18 | 3 | 5 | `F-DeliveryOutcomeDataset` |
| Deworming day (NDD) (`DewormingDataset`) | 6 | 4 | 2 | 2 | `F-DewormingDataset` |
| EC registration (`EligibleCoupleRegistrationDataset`) | 80 | 74 | 6 | 6 | `F-EligibleCoupleRegistrationDataset` |
| EC tracking (`EligibleCoupleTrackingDataset`) | 19 | 11 | 8 | 9 | `F-EligibleCoupleTrackingDataset` |
| FPOT form (`FPOTFormDataset`) | 25 | 25 | 0 | 1 | `F-FPOTFormDataset` |
| Filaria (`FilariaFormDataset`) | 7 | 2 | 5 | 3 | `F-FilariaFormDataset` |
| HBNC visit (`HBNCFormDataset`) | 92 | 92 | 0 | 11 | `F-HBNCFormDataset` |
| HBYC visit (`HBYCFormDataset`) | 27 | 27 | 0 | 6 | `F-HBYCFormDataset` |
| Micro birth plan (`HRPMicroBirthPlanDataset`) | 19 | 18 | 1 | 8 | `F-HRPMicroBirthPlanDataset` |
| HR non-pregnant · assess (`HRPNonPregnantAssessDataset`) | 12 | 12 | 0 | 1 | `F-HRPNonPregnantAssessDataset` |
| HR non-pregnant · follow-up (`HRPNonPregnantTrackDataset`) | 23 | 19 | 4 | 10 | `F-HRPNonPregnantTrackDataset` |
| HRP pregnant · assess (`HRPPregnantAssessDataset`) | 14 | 14 | 0 | 6 | `F-HRPPregnantAssessDataset` |
| HRP pregnant · follow-up (`HRPPregnantTrackDataset`) | 29 | 28 | 1 | 8 | `F-HRPPregnantTrackDataset` |
| Household registration (`HouseholdFormDataset`) | 28 | 28 | 0 | 5 | `F-HouseholdFormDataset` |
| Immunization record (`ImmunizationDataset`) | 15 | 15 | 0 | 8 | `F-ImmunizationDataset` |
| Infant registration (`InfantRegistrationDataset`) | 21 | 13 | 8 | 0 | `F-InfantRegistrationDataset` |
| Kala-azar (`KalaAzarFormDataset`) | 13 | 7 | 6 | 9 | `F-KalaAzarFormDataset` |
| Leprosy confirmed (`LeprosyConfirmedDataset`) | 14 | 11 | 3 | 8 | `F-LeprosyConfirmedDataset` |
| Leprosy screening (`LeprosyFormDataset`) | 27 | 24 | 3 | 7 | `F-LeprosyFormDataset` |
| Leprosy suspected (`LeprosySuspectedDataset`) | 12 | 6 | 6 | 6 | `F-LeprosySuspectedDataset` |
| Maternal death report (MDSR) (`MDSRFormDataset`) | 33 | 28 | 5 | 14 | `F-MDSRFormDataset` |
| Maa meeting (`MaaMeetingDataset`) | 8 | 7 | 1 | 0 | `F-MaaMeetingDataset` |
| Malaria confirmed · treatment (`MalariaConfirmCasesDataset`) | 6 | 6 | 0 | 3 | `F-MalariaConfirmCasesDataset` |
| Malaria screening (`MalariaFormDataset`) | 30 | 19 | 11 | 13 | `F-MalariaFormDataset` |
| Child / adolescent registration (0–14) (`NewChildBenRegDataset`) | 61 | 4 | 57 | 3 | `F-NewChildBenRegDataset` |
| PHC review meeting (`PHCReviewDataset`) | 8 | 8 | 0 | 2 | `F-PHCReviewDataset` |
| PMJAY form (`PMJAYFormDataset`) | 13 | 13 | 0 | 0 | `F-PMJAYFormDataset` |
| PMSMA visit (`PMSMAFormDataset`) | 45 | 45 | 0 | 13 | `F-PMSMAFormDataset` |
| PNC visit (`PncFormDataset`) | 23 | 15 | 8 | 8 | `F-PncFormDataset` |
| ANC · abortion (`PregnantWomanAncAbortionDataset`) | 15 | 13 | 2 | 4 | `F-PregnantWomanAncAbortionDataset` |
| ANC visit (`PregnantWomanAncVisitDataset`) | 39 | 25 | 14 | 12 | `F-PregnantWomanAncVisitDataset` |
| Pregnancy registration (`PregnantWomanRegistrationDataset`) | 38 | 28 | 10 | 10 | `F-PregnantWomanRegistrationDataset` |
| Saas Bahu Sammelan (`SaasBahuSamelanDataset`) | 4 | 3 | 1 | 1 | `F-SaasBahuSamelanDataset` |
| Survey register (`SurveyRegisterFormDataset`) | 17 | 17 | 0 | 2 | `F-SurveyRegisterFormDataset` |
| Suspected TB (`SuspectedTBDataset`) | 15 | 9 | 6 | 6 | `F-SuspectedTBDataset` |
| TB confirmed · treatment (`TBConfirmedDataset`) | 14 | 14 | 0 | 4 | `F-TBConfirmedDataset` |
| TB screening (`TBScreeningDataset`) | 30 | 11 | 19 | 10 | `F-TBScreeningDataset` |
| U-WIN session (`UWINDataset`) | 5 | 5 | 0 | 1 | `F-UWINDataset` |
| VHSNC meeting (`VHNCDataset`) | 12 | 12 | 0 | 0 | `F-VHNCDataset` |
| VHND meeting (`VHNDDataset`) | 15 | 15 | 0 | 5 | `F-VHNDDataset` |

## 3. Cards summary

| Card layout | Labels | Buttons | Labels (code order) |
|---|---|---|---|
| `rv_item_abortion` | 9 | 0 | Husband Name · Age · Beneficiary ID · Mobile Number · RCH ID · Weeks Of Pregnancy · LMP · EDD · Abortion Date |
| `rv_item_adolscent_health_list` | 9 | 1 | Husband Name · Father Name · Spouse Name · Age · Gender · Registration Date · Beneficiary ID · Mobile Number · RCH ID — buttons: Add Beneficiary |
| `rv_item_aes_members_list` | 8 | 0 | Husband Name · Father Name · Spouse Name · Age · Gender · Beneficiary ID · Mobile Number · RCH ID |
| `rv_item_ben` | 15 | 2 | HOF · Head of Family: · Husband Name · Father Name · Spouse Name · Age · Gender · HOF · Registration Date · Beneficiary ID · Mobile Number · RCH ID · Date of Death · ABHA Number : · Deleted Record — buttons: Register Children · Register Wife |
| `rv_item_ben_child_care_child` | 8 | 2 | Mother Name · Age · Gender · Father Name · Beneficiary ID · Mobile Number · Date of Birth · RCH ID — buttons: Add Beneficiary · HBYC |
| `rv_item_ben_child_care_infant` | 8 | 2 | Mother Name · Age · Gender · Father Name · Beneficiary ID · Mobile Number · Date of Birth · RCH ID — buttons: Add Beneficiary · HBNC |
| `rv_item_ben_child_immunization` | 9 | 0 | Age · Gender · Date of Birth · Father Name · Mother Name · Beneficiary ID · Child RCH ID · Mother RCH ID · Mobile Number |
| `rv_item_ben_child_reg` | 6 | 0 | Mother Name · Age · Husband Name · Beneficiary ID · Mobile Number · RCH ID |
| `rv_item_ben_ec_with_form` | 9 | 0 | Husband Name · Age · Beneficiary ID · Mobile Number · RCH ID · LMP Date · Status · No of children · RCH ID |
| `rv_item_ben_get_data` | 0 | 1 |  — buttons: SYNC TO LOCAL |
| `rv_item_ben_infant_reg` | 6 | 0 | Mother Name · Age · Husband Name · Beneficiary ID · Mobile Number · RCH ID |
| `rv_item_ben_pw_registration_list_with_form` | 5 | 0 | Husband Name · Age · Beneficiary ID · Mobile Number · RCH ID |
| `rv_item_ben_with_hrnpa_form` | 5 | 0 | Husband Name · Age · Beneficiary ID · Mobile Number · RCH ID |
| `rv_item_ben_with_hrpa_form` | 8 | 0 | Husband Name · Age · Beneficiary ID · Mobile Number · RCH ID · Weeks Of Pregnancy · LMP · Expected Delivery |
| `rv_item_children_care_bottom_sheet` | 0 | 2 |  — buttons: ADD · VIEW |
| `rv_item_ec_track` | 0 | 1 |  — buttons: VIEW |
| `rv_item_ec_tracking_list` | 5 | 0 | Age · Total No.of Children · LMP Date · Status · Last Visit Date |
| `rv_item_eye_surgery_visit` | 0 | 1 |  — buttons: VIEW |
| `rv_item_filaria_member_list` | 8 | 0 | Husband Name · Father Name · Spouse Name · Age · Gender · Beneficiary ID · Mobile Number · RCH ID |
| `rv_item_general_checkup` | 5 | 1 | Age · Gender · Beneficiary ID · Mobile Number · Earlier check-ups — buttons: Add Check-up |
| `rv_item_household` | 3 | 2 | Household ID · Contact Number · Deleted Record — buttons: Verify OTP · VIEW |
| `rv_item_hrnp_tracking_list` | 5 | 0 | Husband Name · Age · Beneficiary ID · Mobile Number · RCH ID |
| `rv_item_hrp_track_ben` | 0 | 1 |  — buttons: VIEW |
| `rv_item_hrp_tracking_list` | 8 | 0 | Husband Name · Age · Beneficiary ID · Mobile Number · RCH ID · Weeks Of Pregnancy · LMP · Expected Delivery |
| `rv_item_imm_vaccine` | 0 | 2 |  — buttons: KCUF · KCUF |
| `rv_item_kala_azar_member_list` | 8 | 0 | Husband Name · Father Name · Spouse Name · Age · Gender · Beneficiary ID · Mobile Number · RCH ID |
| `rv_item_leprosy_member_list` | 8 | 0 | Husband Name · Father Name · Spouse Name · Age · Gender · Beneficiary ID · Mobile Number · RCH ID |
| `rv_item_malaria_cases_confirmed_list` | 9 | 0 | Husband Name · Father Name · Spouse Name · Age · Gender · Beneficiary ID · Mobile Number · RCH ID · Date of Diagnosis |
| `rv_item_malaria_members_list` | 8 | 0 | Husband Name · Father Name · Spouse Name · Age · Gender · Beneficiary ID · Mobile Number · RCH ID |
| `rv_item_ncd_cbac` | 8 | 0 | Husband Name · Father Name · Spouse Name · Age · Gender · Beneficiary ID · Mobile Number · RCH ID |
| `rv_item_ncd_cbac_element` | 0 | 1 |  — buttons: VIEW |
| `rv_item_ncd_refer` | 11 | 1 | Husband Name · Father Name · Spouse Name · Age · Gender · Beneficiary ID · Mobile Number · Reason for Referral · Referred From · Referred To · Referral Date — buttons: Follow Up |
| `rv_item_pnc_visit` | 9 | 0 | Husband Name · Age · Date of Delivery · Beneficiary ID · Mobile Number · RCH ID · Last Visit Date · Status · Last Visit Date |
| `rv_item_pregnancy_anc` | 0 | 1 |  — buttons: VIEW |
| `rv_item_pregnancy_anc_pmsma` | 0 | 1 |  — buttons: VIEW |
| `rv_item_pregnancy_visit` | 12 | 0 | Husband Name · Age · Beneficiary ID · Mobile Number · RCH ID · Weeks Of Pregnancy · LMP · EDD · Last Visit Date · Status · Last Visit Date · Delivery Status |
| `rv_item_tb_confirmed_list` | 9 | 2 | Husband Name · Father Name · Spouse Name · Age · Gender · Beneficiary ID · Mobile Number · RCH ID · Nikshay ID — buttons: Family Members · Follow Up |
| `rv_item_tb_screening_list` | 9 | 0 | Husband Name · Father Name · Spouse Name · Age · Gender · Beneficiary ID · Mobile Number · RCH ID · Nikshay ID |
| `rv_item_tb_suspected_list` | 9 | 0 | Husband Name · Father Name · Spouse Name · Age · Gender · Beneficiary ID · Mobile Number · RCH ID · Nikshay ID |
| `rv_item_tpt_followup_list` | 9 | 1 | Husband Name · Father Name · Spouse Name · Age · Gender · Beneficiary ID · Mobile Number · RCH ID · Nikshay ID — buttons: START TPT |
| `rv_item_velunaribility_list` | 9 | 0 | Husband Name · Father Name · Spouse Name · Age · Gender · Beneficiary ID · Mobile Number · RCH ID · Nikshay ID |
| `rv_item_visits` | 3 | 0 | Visit 1 · 2023-11-15 · Treatment Status |

## 4. Every field, per form

### AES / JE form — `AESJEFormDataset.kt` (11 fields)

| # | Field | Type | Req | Shown | Options |
|---|---|---|---|---|---|
| 1 | Visit Date | DATE_PICKER | * | always |  |
| 2 | Beneficiary Status | DROPDOWN | * | always |  |
| 3 | Date of death | DATE_PICKER | * | conditional |  |
| 4 | Place of Death | DROPDOWN | * | conditional |  |
| 5 | Other Place of Death | EDIT_TEXT | * | conditional |  |
| 6 | Reason for Death | DROPDOWN | * | always |  |
| 7 | Other Reason of Death | EDIT_TEXT | * | conditional |  |
| 8 | AES / JE Case Status Date | DROPDOWN |  | always |  |
| 9 | Follow Up | DROPDOWN |  | always | 1 · 2 · 3 · 4 · 5 · 6 |
| 10 | Referred To | DROPDOWN |  | always | Primary Health Centre · Community Health Centre · District Hospital · Medical College and Hospital · Referral Hospital · Other Private Hospital · Other |
| 11 | Other | EDIT_TEXT | * | conditional |  |

### AHD meeting — `AHDDataset.kt` (5 fields)

| # | Field | Type | Req | Shown | Options |
|---|---|---|---|---|---|
| 1 | Mobilized for AHD | RADIO | * | always | Yes · No |
| 2 | AHD Place | DROPDOWN | * | conditional | School · Anganwadi Centre · Community center |
| 3 | AHD Date | DATE_PICKER | * | conditional |  |
| 4 | Photo | FILE_UPLOAD |  | always |  |
| 5 | Photo | FILE_UPLOAD |  | always |  |

### Adolescent health — `AdolescentHealthFormDataset.kt` (15 fields)

| # | Field | Type | Req | Shown | Options |
|---|---|---|---|---|---|
| 1 | Visit Date | DATE_PICKER | * | always |  |
| 2 | Health Status | DROPDOWN | * | always | Healthy · Anemic · Malnourished |
| 3 | IFA Tablet Distribution | RADIO | * | always | Yes · No |
| 4 | Quantity of IFA Tablets | EDIT_TEXT | * | always |  |
| 5 | Menstrual Hygiene Awareness | RADIO |  | always | Yes · No |
| 6 | Sanitary Napkin Distributed | RADIO | * | always | Yes · No |
| 7 | Is sanitary pad used | RADIO | * | always | Yes · No |
| 8 | No. of Packets Distributed | EDIT_TEXT | * | always |  |
| 9 | Place | DROPDOWN | * | always | Home · Community center · School · Subcenter |
| 10 | Referred to Health Facility | EDIT_TEXT |  | always |  |
| 11 | Counseling Provided | RADIO | * | always | Yes · No |
| 12 | Counseling Type | DROPDOWN |  | always | Individual · Group |
| 13 | Follow-up Date | DATE_PICKER |  | always |  |
| 14 | Referral Status | DROPDOWN |  | always | Pending · Completed |
| 15 | Date | DATE_PICKER |  | always |  |

### ASHA profile — `AshaProfileDataset.kt` (31 fields)

| # | Field | Type | Req | Shown | Options |
|---|---|---|---|---|---|
| 1 | Photo | IMAGE_VIEW |  | always |  |
| 2 | ASHA Name | TEXT_VIEW | * | always |  |
| 3 | Village | TEXT_VIEW |  | always |  |
| 4 | Location Type | TEXT_VIEW |  | always |  |
| 5 | Login Username | TEXT_VIEW |  | always |  |
| 6 | ASHA Emp ID | TEXT_VIEW |  | always |  |
| 7 | Date of Birth | TEXT_VIEW |  | always |  |
| 8 | Age | TEXT_VIEW |  | always |  |
| 9 | Mobile Number | TEXT_VIEW | * | always |  |
| 10 | Alternate Mobile No. | EDIT_TEXT |  | always |  |
| 11 | Father or Spouse | RADIO |  | always | Father · Spouse |
| 12 | Father or Spouse | EDIT_TEXT |  | always | Father · Spouse |
| 13 | Date of Joining | TEXT_VIEW |  | always |  |
| 14 | Bank Account | EDIT_TEXT |  | always |  |
| 15 | IFSC | EDIT_TEXT |  | always |  |
| 16 | Population Covered under ASHA | EDIT_TEXT |  | always |  |
| 17 | ASHA Supervisor Name | EDIT_TEXT |  | always |  |
| 18 | ASHA Supervisor Contact No. | EDIT_TEXT |  | always |  |
| 19 | CHO Name | EDIT_TEXT |  | always |  |
| 20 | Mobile No. of CHO | EDIT_TEXT |  | always |  |
| 21 | Name of AWW | EDIT_TEXT |  | always |  |
| 22 | Mobile No. AWW | EDIT_TEXT |  | always |  |
| 23 | Name of ANM1 | EDIT_TEXT |  | always |  |
| 24 | Mobile Number of ANM1 | EDIT_TEXT |  | always |  |
| 25 | Name of ANM2 | EDIT_TEXT |  | always |  |
| 26 | Mobile Number of ANM2 | EDIT_TEXT |  | always |  |
| 27 | ABHA Number : | EDIT_TEXT |  | always |  |
| 28 | Facility | TEXT_VIEW |  | always |  |
| 29 | Facility ID | TEXT_VIEW |  | always |  |
| 30 | Asha Household registration | TEXT_VIEW |  | conditional |  |
| 31 | Asha Family member | EDIT_TEXT |  | conditional |  |

### General registration (15+) — `BenGenRegFormDataset.kt` (28 fields)

| # | Field | Type | Req | Shown | Options |
|---|---|---|---|---|---|
| 1 | Photo | IMAGE_VIEW |  | always |  |
| 2 | Date of Registration | DATE_PICKER | * | always |  |
| 3 | First Name | EDIT_TEXT | * | always |  |
| 4 | Last Name / Surname | EDIT_TEXT | * | always |  |
| 5 | Age | EDIT_TEXT | * | always |  |
| 6 | Date of Birth | DATE_PICKER | * | always |  |
| 7 | Gender | RADIO | * | always |  |
| 8 | Marital Status | DROPDOWN | * | always | Unmarried · Married · Divorced · Separated · Widower |
| 9 | Husband's Name | EDIT_TEXT | * | always |  |
| 10 | Wife's Name | EDIT_TEXT | * | always |  |
| 11 | Spouse's Name | EDIT_TEXT | * | always |  |
| 12 | Age At Marriage | EDIT_TEXT | * | always |  |
| 13 | Date of Marriage | DATE_PICKER | * | always |  |
| 14 | Father's Name | EDIT_TEXT |  | always |  |
| 15 | Mother's Name | EDIT_TEXT |  | always |  |
| 16 | Mobile Number Of | DROPDOWN | * | always | Self · Husband · Mother · Father · Family Head · Other |
| 17 | Other - Mobile Number of | EDIT_TEXT | * | always |  |
| 18 | Contact Number | EDIT_TEXT | * | always |  |
| 19 | Contact Number | TEXT_VIEW | * | always |  |
| 20 | Relation with Family Head | DROPDOWN | * | always | Mother · Father · Brother · Sister · Wife · Husband · Nephew · Niece · Son · Daughter · Grand Father · Grand Mother · Father in Law · Mother in Law · Grand Son · Grand Daughter · Son in Law · Daughter in Law · Brother in Law · Other |
| 21 | Other - Enter relation to head | EDIT_TEXT | * | always |  |
| 22 | Community | DROPDOWN | * | always | General · SC · ST · BC · OBC · OC · PVTG – Primitive Vulnerable Tribal Groups · Not given |
| 23 | Religion | DROPDOWN | * | always | Hindu · Muslim · Christian · Sikhism · Buddhism · Jainism · Parsi · Other · Not disclosed |
| 24 | Other - Enter Religion | EDIT_TEXT | * | always |  |
| 25 | Has Aadhaar Number | RADIO |  | always | Yes · No |
| 26 | Enter Aadhaar Number | EDIT_TEXT | * | always |  |
| 27 | RCH ID | EDIT_TEXT |  | always |  |
| 28 | Status Of Women | DROPDOWN | * | always | Pregnant Woman · Postnatal Mother · Permanently Sterilised |

### CHO beneficiary registration — `BenRegCHODataset.kt` (8 fields)

| # | Field | Type | Req | Shown | Options |
|---|---|---|---|---|---|
| 1 | Date of Registration | DATE_PICKER | * | always |  |
| 2 | Name of Beneficiary | EDIT_TEXT | * | always |  |
| 3 | Date of Birth | DATE_PICKER | * | always |  |
| 4 | Husband's Name | EDIT_TEXT | * | always |  |
| 5 | Contact Number | EDIT_TEXT | * | always |  |
| 6 | RCH ID | EDIT_TEXT |  | always |  |
| 7 | Is Pregnant | RADIO | * | always | Yes · No |
| 8 | Age In Years | EDIT_TEXT | * | always |  |

### Beneficiary registration — `BenRegFormDataset.kt` (70 fields)

| # | Field | Type | Req | Shown | Options |
|---|---|---|---|---|---|
| 1 | Photo | IMAGE_VIEW |  | always |  |
| 2 | Date of Registration | DATE_PICKER | * | always |  |
| 3 | Do you have ABHA ID / Ayushman ID? | RADIO |  | always | Yes · No |
| 4 | Enter Abha Id / Ayushman Card Number | EDIT_TEXT |  | always |  |
| 5 | Submit | org |  | always |  |
| 6 | First Name | EDIT_TEXT | * | always |  |
| 7 | Last Name / Surname | EDIT_TEXT | * | always |  |
| 8 | Age | org | * | always |  |
| 9 | Date of Birth | TEXT_VIEW |  | always |  |
| 10 | Gender | RADIO | * | always |  |
| 11 | Marital Status | DROPDOWN | * | always | Unmarried · Married · Divorced · Separated · Widower |
| 12 | Husband's Name | EDIT_TEXT | * | always |  |
| 13 | Wife's Name | EDIT_TEXT | * | always |  |
| 14 | Spouse's Name | EDIT_TEXT | * | always |  |
| 15 | Your Age at the time of marriage | EDIT_TEXT | * | always |  |
| 16 | Date of Marriage | DATE_PICKER | * | always |  |
| 17 | Father's Name | EDIT_TEXT |  | always |  |
| 18 | Mother's Name | EDIT_TEXT |  | always |  |
| 19 | Mobile Number belongs to | DROPDOWN | * | always | Self · Husband · Mother · Father · Family Head · Other |
| 20 | Other - Mobile Number of | EDIT_TEXT | * | always |  |
| 21 | Contact Number | EDIT_TEXT | * | always |  |
| 22 | Place of Current Living * | TEXT_VIEW |  | always |  |
| 23 | Name of Institution * | TEXT_VIEW |  | always |  |
| 24 | Relation with head of family | TEXT_VIEW | * | always | Mother · Father · Brother · Sister · Wife · Husband · Nephew · Niece · Son · Daughter · Grand Father · Grand Mother · Father in Law · Mother in Law · Grand Son · Grand Daughter · Son in Law · Daughter in Law · Self · Sister in Law · Other |
| 25 | Other - Enter relation to head | EDIT_TEXT | * | always |  |
| 26 | Community | DROPDOWN | * | always | General · SC · ST · BC · OBC · OC · PVTG – Primitive Vulnerable Tribal Groups · Not given |
| 27 | Religion | DROPDOWN | * | always | Hindu · Muslim · Christian · Sikhism · Buddhism · Jainism · Parsi · Other · Not disclosed |
| 28 | Other - Enter Religion | EDIT_TEXT | * | always |  |
| 29 | Is the Child registered at AWC | DROPDOWN | * | always | Yes · No |
| 30 | Is the Child registered at School | DROPDOWN | * | always | Yes · No |
| 31 | Type of School/Institute | DROPDOWN | * | always | Anganwadi · Primary · Secondary · Private |
| 32 | RCH ID | EDIT_TEXT |  | always |  |
| 33 | Has Aadhaar Number | RADIO |  | always | Yes · No |
| 34 | Enter Aadhaar Number | EDIT_TEXT | * | always |  |
| 35 | Status Of Women | TEXT_VIEW | * | always | Pregnant Woman · Postnatal Mother · Permanently Sterilised |
| 36 | Birth Certificate No. | EDIT_TEXT |  | always |  |
| 37 | Send OTP | org |  | always |  |
| 38 | Enter OTP | EDIT_TEXT |  | always |  |
| 39 | Contact Number | EDIT_TEXT |  | always |  |
| 40 | Mobile Number belongs to | DROPDOWN |  | always | Family Head · Self · Spouse · Mother · Father · Neighbor · Friend/Relative |
| 41 | Birth Certificate Uploads | org |  | always |  |
| 42 | Front Side | FILE_UPLOAD |  | always |  |
| 43 | Back Side | FILE_UPLOAD |  | always |  |
| 44 | Do you have children? | RADIO | * | always | Yes · No |
| 45 | Place of birth | DROPDOWN | * | always | Home · Sub-Centre · PHC · CHC · Sub-District Hospital · District Hospital · Medical College Hospital · In Transit · Private Hospital · Accredited Private Hospital · Other |
| 46 | Health Facility | DROPDOWN | * | always | Sub Centre · PHC · CHC · Sub District Hospital · District Hospital · Medical College Hospital · In Transit · Private Hospital |
| 47 | Other Facility | EDIT_TEXT | * | always |  |
| 48 | Other Place of Birth | EDIT_TEXT | * | always |  |
| 49 | Who Conducted Delivery | DROPDOWN | * | always | ANM · LHV · Doctor · Staff Nurse · Relative · TBA(Non-Skilled Birth Attendant) · Other |
| 50 | Other - Enter who Conducted Delivery | EDIT_TEXT | * | always |  |
| 51 | Type of Delivery | DROPDOWN | * | always | Normal Delivery · C - Section · Assisted |
| 52 | Complications during delivery | DROPDOWN | * | always | PPH · Retained Placenta · Obstructed Delivery · Prolapsed cord · Death · None |
| 53 | Breast feeding started within 1 hr of birth | DROPDOWN | * | always | Yes · No · Don't Know |
| 54 | Birth Dose | DROPDOWN | * | always | Given · Not Given · Don't Know |
| 55 | Given birth dose details | CHECKBOXES | * | always | BCG · Hepatitis · OPV |
| 56 | Term | DROPDOWN | * | always | Full-Term · Pre-Term · Don't Know |
| 57 | Gestational Age | RADIO | * | always | 24-34 Weeks · 34-36 Weeks · 36-38 Weeks |
| 58 | Corticosteroid given to mother at labor | DROPDOWN | * | always | Yes · No · Don't Know |
| 59 | Baby cried immediately at birth | DROPDOWN | * | always | Yes · No · Don't Know |
| 60 | Any Defect Seen at Birth | DROPDOWN | * | always | Cleft Lip / Cleft Palate · Neural Tube defect(Spinal Bifida) · Club Foot · Hydrocephalus · Imperforate Anus · Down's Syndrome · None |
| 61 | Mother Unselected | CHECKBOXES |  | always | Yes |
| 62 | Mother of the child | DROPDOWN | * | always |  |
| 63 | Height at birth ( cm ) | EDIT_TEXT |  | always |  |
| 64 | Weight at birth ( Kg ) | EDIT_TEXT |  | always |  |
| 65 | Beneficiary Status | RADIO |  | always | Alive · Death |
| 66 | Date of Death | DATE_PICKER | * | always |  |
| 67 | Time of Death | org |  | always |  |
| 68 | Reason for Death | DROPDOWN | * | always | Natural Death · Accident · Infectious Disease · Animal Bite Death · Suicide · Undetermined |
| 69 | Place of Death | DROPDOWN | * | always | Home · Subcenter · PHC · CHC · District Hospital · Medical College Hospital · Private Hospital · In Transit · Other Place of Death |
| 70 | Other Place of Death | EDIT_TEXT | * | always |  |

### Child death report (CDR) — `CDRFormDataset.kt` (27 fields)

| # | Field | Type | Req | Shown | Options |
|---|---|---|---|---|---|
| 1 | Name of the Child | TEXT_VIEW |  | always |  |
| 2 | Date of Birth | TEXT_VIEW |  | always |  |
| 3 | Age | TEXT_VIEW |  | always |  |
| 4 | Visit Date | DATE_PICKER | * | always |  |
| 5 | Gender | TEXT_VIEW |  | always |  |
| 6 | Mother's Name | TEXT_VIEW |  | always |  |
| 7 | Father's Name | TEXT_VIEW |  | always |  |
| 8 | Address | TEXT_VIEW |  | always |  |
| 9 | House Number | TEXT_VIEW |  | always |  |
| 10 | Mohalla/Colony | TEXT_VIEW |  | always |  |
| 11 | Landmarks, if any | TEXT_VIEW |  | conditional |  |
| 12 | Pincode | TEXT_VIEW |  | conditional |  |
| 13 | Landline | EDIT_TEXT |  | always |  |
| 14 | Mobile Number | TEXT_VIEW |  | always |  |
| 15 | Date of Death | TEXT_VIEW | * | always |  |
| 16 | Time | TIME_PICKER |  | always |  |
| 17 | Place of Death | TEXT_VIEW | * | always |  |
| 18 | Name of First Informant | TEXT_VIEW |  | always |  |
| 19 | Signature/Name of ASHA | EDIT_TEXT |  | conditional |  |
| 20 | Date of Notification | DATE_PICKER |  | always |  |
| 21 | CDR form from ANM 1 | FILE_UPLOAD |  | always |  |
| 22 | CDR form from ANM 2 | FILE_UPLOAD |  | always |  |
| 23 | Death Certificate | FILE_UPLOAD |  | always |  |
| 24 | State | TEXT_VIEW |  | always |  |
| 25 | District/Tehsil | TEXT_VIEW |  | always |  |
| 26 | Block | TEXT_VIEW |  | always |  |
| 27 | Village/Town/City | TEXT_VIEW |  | always |  |

### Child registration — `ChildRegistrationDataset.kt` (17 fields)

| # | Field | Type | Req | Shown | Options |
|---|---|---|---|---|---|
| 1 | Date of Registration | DATE_PICKER | * | always |  |
| 2 | Name of Child | EDIT_TEXT |  | always |  |
| 3 | RCH ID No. of Child | EDIT_TEXT |  | always |  |
| 4 | Date of Birth | DATE_PICKER | * | always |  |
| 5 | Sex of Child | RADIO |  | always | Male |
| 6 | Mother's Name | EDIT_TEXT | * | always |  |
| 7 | Father's Name | EDIT_TEXT | * | always |  |
| 8 | Whose Mobile Number | RADIO |  | always | Mother · Father · Others |
| 9 | Mobile No. | EDIT_TEXT | * | always |  |
| 10 | RCH ID No. of Mother | EDIT_TEXT |  | always |  |
| 11 | Birth Certificate Number | EDIT_TEXT |  | always |  |
| 12 | Weight at Birth ( grams ) | EDIT_TEXT |  | always |  |
| 13 | Weight at Birth (kg) | EDIT_TEXT |  | always |  |
| 14 | Place of Birth | DROPDOWN |  | always | District Hospital · Community Health Centre · Primary Health Centre · Sub Centre · Other Public Facility · Accredited Private Hospital · Other Private Hospital · Home · Sub District Hospital · Medical College Hospital · In Transit |
| 15 | Birth Certificate Uploads | HEADLINE |  | always |  |
| 16 | Front Side | FILE_UPLOAD |  | always |  |
| 17 | Back Side | FILE_UPLOAD |  | always |  |

### Delivery outcome — `DeliveryOutcomeDataset.kt` (21 fields)

| # | Field | Type | Req | Shown | Options |
|---|---|---|---|---|---|
| 1 | Date of Delivery | DATE_PICKER | * | always |  |
| 2 | Time of Delivery | TIME_PICKER |  | always |  |
| 3 | Place of Delivery | DROPDOWN | * | always | District Hospital · Community Health Centre · Primary Health Centre · Sub Centre · Other Public Facility · Accredited Private Hospital · Other Private Hospital · Home · Sub District Hospital · Medical College Hospital · In Transit |
| 4 | Type of Delivery | RADIO | * | always | Normal · Caesarean |
| 5 | Complications during Delivery? | RADIO |  | always | Yes · No |
| 6 | Delivery Complication | DROPDOWN | * | always | PPH · RETAINED PLACENTA · OBSTRUCTED DELIVERY · PROLAPSED CORD · TWINS PREGNANCY · CONVULSIONS · DEATH · ANY OTHER (SPECIFY) · DON'T KNOW |
| 7 | Probable Cause of Death | DROPDOWN | * | always | Eclampcia · Haemorrahge · Obstructed Labour · Prolonged Labour · Other · High Fever |
| 8 | Other Cause of Death | EDIT_TEXT | * | conditional |  |
| 9 | Date of Death | DATE_PICKER | * | always |  |
| 10 | Place of Death | DROPDOWN | * | always | Home · Subcenter · PHC · CHC · District Hospital · Medical College Hospital · Private Hospital · In Transit · Other Place of Death |
| 11 | Other Place of Death | EDIT_TEXT | * | conditional |  |
| 12 | Other Delivery Complication | EDIT_TEXT | * | always |  |
| 13 | Total births (Total number of Babies born) | EDIT_TEXT | * | always |  |
| 14 | Number of Live Births | EDIT_TEXT | * | always |  |
| 15 | Number of Stillbirths | EDIT_TEXT | * | always |  |
| 16 | Date of Discharge | DATE_PICKER |  | always |  |
| 17 | Time of Discharge | TIME_PICKER |  | always |  |
| 18 | Is the Pregnant Woman a JSY Beneficiary? | RADIO |  | always | Yes · No |
| 19 | MCP Card 1 | FILE_UPLOAD |  | always |  |
| 20 | MCP Card 2 | FILE_UPLOAD |  | always |  |
| 21 | JSY payment voucher | FILE_UPLOAD |  | conditional |  |

### Deworming day (NDD) — `DewormingDataset.kt` (6 fields)

| # | Field | Type | Req | Shown | Options |
|---|---|---|---|---|---|
| 1 | Was the deworming round conducted? | RADIO | * | always | Yes · No |
| 2 | Date of NDD | DATE_PICKER | * | conditional |  |
| 3 | Place of NDD | DROPDOWN | * | conditional | School · Anganwadi Centre · Community center · Home Visit |
| 4 | Number of Out-of-school Children sent to Anganwad | EDIT_TEXT | * | always |  |
| 5 | Upload Image | org |  | always |  |
| 6 | Upload Image | org |  | always |  |

### EC registration — `EligibleCoupleRegistrationDataset.kt` (80 fields)

| # | Field | Type | Req | Shown | Options |
|---|---|---|---|---|---|
| 1 | Date of Registration | DATE_PICKER | * | always |  |
| 2 | RCH ID No. of Woman | EDIT_TEXT |  | always |  |
| 3 | Name of Woman | TEXT_VIEW |  | conditional |  |
| 4 | Name of Husband | TEXT_VIEW |  | conditional |  |
| 5 | Current Age of Woman | TEXT_VIEW |  | conditional |  |
| 6 | Age of Woman at Marriage | TEXT_VIEW |  | conditional |  |
| 7 | LMP Date | DATE_PICKER | * | always |  |
| 8 | Is Nayi Pahal kit handed over to couple? | RADIO | * | always | Yes · No |
| 9 | Nayi Pahal kit handover Date | DATE_PICKER | * | always |  |
| 10 | ASHA Photo with couple while handing over Nayi Pahal kit | HEADLINE |  | always |  |
| 11 | ASHA Photo One | org |  | always |  |
| 12 | ASHA Photo Two | org |  | always |  |
| 13 | Children Details | HEADLINE |  | always |  |
| 14 | Aadhaar Number of Woman | EDIT_TEXT |  | conditional |  |
| 15 | Bank Details | HEADLINE |  | always |  |
| 16 | Bank AC No or Post Office AC No | EDIT_TEXT |  | always |  |
| 17 | Bank Name | EDIT_TEXT |  | always |  |
| 18 | Branch Name | EDIT_TEXT |  | always |  |
| 19 | IFSC Code | EDIT_TEXT |  | always |  |
| 20 | Total No. Live Children Born | EDIT_TEXT |  | conditional |  |
| 21 | No. of Live Children | EDIT_TEXT |  | always |  |
| 22 | Male | TEXT_VIEW |  | always |  |
| 23 | Female | TEXT_VIEW |  | always |  |
| 24 | Details of 1st Child | HEADLINE |  | always |  |
| 25 | 1st Child Date of Birth | DATE_PICKER | * | always |  |
| 26 | 1st Child Age ( in Years) | TEXT_VIEW | * | always |  |
| 27 | 1st Child Sex | RADIO | * | always |  |
| 28 | Gap between Date of marriage and 1st child | TEXT_VIEW | * | always |  |
| 29 | Details of 2nd Child | HEADLINE |  | always |  |
| 30 | 2nd Child Date of Birth | DATE_PICKER | * | always |  |
| 31 | 2nd Child Age ( in Years) | TEXT_VIEW | * | always |  |
| 32 | 2nd Child Sex | RADIO | * | always |  |
| 33 | Gap between 1st child and 2nd Child | TEXT_VIEW | * | always |  |
| 34 | Details of 3rd Child | HEADLINE |  | always |  |
| 35 | 3rd Child Date of Birth | DATE_PICKER | * | always |  |
| 36 | 3rd Child Age ( in Years) | TEXT_VIEW | * | always |  |
| 37 | 3rd Child Sex | RADIO | * | always |  |
| 38 | Gap between 2nd child and 3rd Child | TEXT_VIEW | * | always |  |
| 39 | Details of 4th Child | HEADLINE |  | always |  |
| 40 | 4th Child Date of Birth | DATE_PICKER | * | always |  |
| 41 | 4th Child Age ( in Years) | TEXT_VIEW | * | always |  |
| 42 | 4th Child Sex | RADIO | * | always |  |
| 43 | Gap between 3rd Child and 4th Child | TEXT_VIEW | * | always |  |
| 44 | Details of 5th Child | HEADLINE |  | always |  |
| 45 | 5th Child Date of Birth | DATE_PICKER | * | always |  |
| 46 | 5th Child Age ( in Years) | TEXT_VIEW | * | always |  |
| 47 | 5th Child Sex | RADIO | * | always |  |
| 48 | Gap between 4th child and 5th Child | TEXT_VIEW | * | always |  |
| 49 | Details of 6th Child | HEADLINE |  | always |  |
| 50 | 6th Child Date of Birth | DATE_PICKER | * | always |  |
| 51 | 6th Child Age ( in Years) | TEXT_VIEW | * | always |  |
| 52 | 6th Child Sex | RADIO | * | always |  |
| 53 | Gap between 5th child and 6th Child | TEXT_VIEW | * | always |  |
| 54 | Details of 7th Child | HEADLINE |  | always |  |
| 55 | 7th Child Date of Birth | DATE_PICKER | * | always |  |
| 56 | 7th Child Age ( in Years) | TEXT_VIEW | * | always |  |
| 57 | 7th Child Sex | RADIO | * | always |  |
| 58 | Gap between 6th child and 7th Child | TEXT_VIEW | * | always |  |
| 59 | Details of 8th Child | HEADLINE |  | always |  |
| 60 | 8th Child Date of Birth | DATE_PICKER | * | always |  |
| 61 | 8th Child Age ( in Years) | TEXT_VIEW | * | always |  |
| 62 | 8th Child Sex | RADIO | * | always |  |
| 63 | Gap between 7th child and 8th Child | TEXT_VIEW | * | always |  |
| 64 | Details of 9th Child | HEADLINE |  | always |  |
| 65 | 9th Child Date of Birth | DATE_PICKER | * | always |  |
| 66 | 9th Child Age ( in Years) | TEXT_VIEW | * | always |  |
| 67 | 9th Child Sex | RADIO | * | always |  |
| 68 | Gap between 8th child and 9th Child | TEXT_VIEW | * | always |  |
| 69 | No. of Deliveries is more than 3 | org |  | always | Yes · No |
| 70 | Time from last delivery is less than 18 months | org |  | always | Yes · No |
| 71 | Height is very short or less than 140 cms | org |  | always | Yes · No |
| 72 | Age is less than 18 or more than 35 years | org |  | always | Yes · No |
| 73 | Miscarriage/abortion | org |  | always | Yes · No |
| 74 | Home delivery of previous pregnancy | org |  | always | Yes · No |
| 75 | During pregnancy or delivery you faced any medical issues | org |  | always | Yes · No |
| 76 | Past C –section (CS) | org |  | always | Yes · No |
| 77 | Information on Children | org |  | always |  |
| 78 | Physical Observation | org |  | always |  |
| 79 | Obstetric History | org |  | always |  |
| 80 | Assess for High Risk Conditions in the Non-Pregnant Women | org |  | always |  |

### EC tracking — `EligibleCoupleTrackingDataset.kt` (19 fields)

| # | Field | Type | Req | Shown | Options |
|---|---|---|---|---|---|
| 1 | Date of Visit | DATE_PICKER | * | always |  |
| 2 | Date of Sterilisation | DATE_PICKER | * | conditional |  |
| 3 | LMP Date | DATE_PICKER | * | always |  |
| 4 | Financial Year | TEXT_VIEW |  | always |  |
| 5 | Month | TEXT_VIEW |  | always | Jan · Feb · Mar · Apr · May · Jun · Jul · Aug · Sep · Oct · Nov · Dec |
| 6 | Is Pregnancy Test done? | RADIO |  | always | Yes · No |
| 7 | Pregnancy Test Result | RADIO | * | always | Positive · Negative |
| 8 | Is the woman pregnant? | RADIO |  | always | Yes · No |
| 9 | Are you using Family Planning Method? or Do you want to use any Planning Method | RADIO |  | conditional | Yes · No |
| 10 | Are you using any Family Planning Method? | RADIO |  | conditional | Yes · No |
| 11 | Do you want to use any Planning Method? | RADIO |  | conditional | Yes · No |
| 12 | FP Methods | DROPDOWN |  | conditional | Self · ANTRA Injection · Copper T (IUCD) · Condom · Mala N · Chaya · ECP · MALE STERILIZATION · FEMALE STERILIZATION · MiniLap · Any Other Method |
| 13 | Antra injection | DROPDOWN | * | conditional | Dose-1 · Dose-2 · Dose-3 · Dose-4 · Dose-5 · Dose-6 · Dose-7 · Dose-8 · Dose-9 · Dose-10 |
| 14 | Any Other Method | EDIT_TEXT | * | always |  |
| 15 | Date of ANTRA Injection | DATE_PICKER | * | conditional |  |
| 16 | Due date of next Injection | TEXT_VIEW |  | conditional |  |
| 17 | MPA Card | FILE_UPLOAD |  | always |  |
| 18 | Discharge Summary Applicable for Sterilisation Method 1 | FILE_UPLOAD |  | always |  |
| 19 | Discharge Summary Applicable for Sterilisation Method 2 | FILE_UPLOAD |  | always |  |

### FPOT form — `FPOTFormDataset.kt` (25 fields)

| # | Field | Type | Req | Shown | Options |
|---|---|---|---|---|---|
| 1 | Monthly serial number | EDIT_TEXT |  | always |  |
| 2 | Annual serial number | EDIT_TEXT |  | always |  |
| 3 | Husband/Wife’s Name | EDIT_TEXT |  | always |  |
| 4 | Category | DROPDOWN |  | always |  |
| 5 | Beneficiary Address | EDIT_TEXT |  | always |  |
| 6 | Telephone Number/Mobile Number | EDIT_TEXT |  | always |  |
| 7 | Educational Qualification | EDIT_TEXT |  | always |  |
| 8 | Total number of surviving children | EDIT_TEXT |  | always |  |
| 9 | Age of Youngest child | EDIT_TEXT |  | always |  |
| 10 | Sterilization/ Sterilization Consent Form Filled | RADIO |  | always | Yes · No |
| 11 | Medical Record Checklist Filled  | RADIO |  | always | Yes · No |
| 12 | Date of operation done  | EDIT_TEXT |  | always |  |
| 13 | Female Sterilization  | EDIT_TEXT |  | always |  |
| 14 | Expected date of follow up | DATE_PICKER |  | always |  |
| 15 | Actual date of follow up | DATE_PICKER |  | always |  |
| 16 | Details of follow up  | EDIT_TEXT |  | always |  |
| 17 | Counselling post follow up  | EDIT_TEXT |  | always |  |
| 18 | Expected date of Follow-up | DATE_PICKER |  | always |  |
| 19 | Menstruation stared after female sterilization | RADIO |  | always | Yes · No |
| 20 | Spermatozoa found in semen | RADIO |  | always | Yes · No |
| 21 | Counselling post follow up  | EDIT_TEXT |  | always |  |
| 22 | Date of issue of female sterilization / vasectomy certificate | EDIT_TEXT |  | always |  |
| 23 | If not issued, please mention reasons | EDIT_TEXT |  | always |  |
| 24 | Photocopy of female sterilization / vasectomy certificate kept in Health Facility | RADIO |  | always | Yes · No |
| 25 | Remarks, if any  | EDIT_TEXT |  | always |  |

### Filaria — `FilariaFormDataset.kt` (7 fields)

| # | Field | Type | Req | Shown | Options |
|---|---|---|---|---|---|
| 1 | Home Visit Date | TEXT_VIEW | * | always |  |
| 2 | Suffering from Filariasis | DROPDOWN |  | always | Yes · No |
| 3 | Which part of Body | CHECKBOXES |  | conditional | Right Hand · Left Hand · Right Leg · Left Leg · Hydrocele · Other |
| 4 | Which part of Body | CHECKBOXES |  | conditional | Right Hand · Left Hand · Right Leg · Left Leg · Right Side Breast · Left Side Breast · Other |
| 5 | Is medicine Distributed | RADIO |  | conditional | Yes · No |
| 6 | Medicine Side Effect | DROPDOWN |  | conditional | F (Fever) · H (Headache) · B (Body Pain) · V (Vomiting) · N (Najala) · O (Other) · None |
| 7 | Other | EDIT_TEXT | * | conditional |  |

### HBNC visit — `HBNCFormDataset.kt` (92 fields)

| # | Field | Type | Req | Shown | Options |
|---|---|---|---|---|---|
| 1 | Health Subcenter Name | EDIT_TEXT |  | always |  |
| 2 | Mother Name | TEXT_VIEW |  | always |  |
| 3 | Father Name | TEXT_VIEW |  | always |  |
| 4 | Date of Delivery | DATE_PICKER |  | always |  |
| 5 | Place of Delivery | DROPDOWN |  | always |  |
| 6 | Baby Gender | RADIO |  | always |  |
| 7 | Type of Delivery | RADIO |  | always |  |
| 8 | Started Breastfeeding | DROPDOWN |  | always |  |
| 9 | Weight at Birth ( grams ) | EDIT_TEXT |  | always |  |
| 10 | Discharge Date of Mother | DATE_PICKER |  | always |  |
| 11 | Discharge Date of Baby | DATE_PICKER |  | always |  |
| 12 | Registration Of Birth | RADIO |  | always | Yes · No |
| 13 | Child Immunization Status | CHECKBOXES |  | always |  |
| 14 | What was the baby fed after birth | DROPDOWN |  | always |  |
| 15 | How did the baby breastfeed? | DROPDOWN |  | always |  |
| 16 | Baby eye condition | RADIO |  | always |  |
| 17 | Is there bleeding from the baby umbilical cord | RADIO |  | always | Yes · No |
| 18 | Weighing machine scale color | RADIO |  | always |  |
| 19 | Enter the child physical condition | HEADLINE |  | always |  |
| 20 | All limbs limp | RADIO |  | always | Yes · No |
| 21 | Feeding less/stop | RADIO |  | always | Yes · No |
| 22 | The child is wrapped in cloth and kept to the mother | RADIO |  | always | Yes · No |
| 23 | Started breastfeeding only/ only given breast milk | RADIO |  | always | Yes · No |
| 24 | Newborn first training (A) Ask mother | HEADLINE |  | always |  |
| 25 | How many times the mother feeds her stomach in 24 hours. Action – If the mother does not eat full stomach or eat less than 4 times, advise mother to do so | EDIT_TEXT |  | always |  |
| 26 | How many pads have been changed in a day for bleeding? Action – If more than 2 pad, refer the mother to the hospital. | EDIT_TEXT |  | always |  |
| 27 | During the winter season, is the baby kept warm? (Closer to the mother, dressed well and wrapped). - If it is not being done, ask the mother to do it. | RADIO |  | always | Yes · No |
| 28 | Is the child breastfed properly? (Whenever feeling hungry or breastfeeding at least 7 – 8 times in 24 hours). Action – if it is not being done then ask the mother to do it.  | RADIO |  | always | Yes · No |
| 29 | Does the child cry continuously or urinate less than 6 times a day? Action – Advice the mother for breast-feeding | RADIO |  | always | Yes · No |
| 30 | measure and check the temperature. Action – Give the patient paracetamol tablet if the temperature is 102°F (38.9°C) and refer to the hospital if the temperature is higher than this. | EDIT_TEXT |  | always |  |
| 31 | Water discharge with foul smell and fever 102 degree Fahrenheit (38.9 degree C). | RADIO |  | always | Yes · No |
| 32 | Is mother speaking abnormally or having fits? | RADIO |  | always | Yes · No |
| 33 | Mothers milk is not being produced after delivery or she thinks less milk is being produced. | RADIO |  | always | Yes · No |
| 34 | Does the mother have cracked nipple / pain and / or hard breasts | RADIO |  | always | Yes · No |
| 35 | Are the eyes swollen / Are there pus from the eyes? | RADIO |  | always | Yes · No |
| 36 | ${resources.getString(R.string.str_weight_on_day)} ${if (nthDay > 0) nthDay else 1} | EDIT_TEXT |  | always |  |
| 37 | Yellowing of the eye/palm/sole/skin (jaundice) | RADIO |  | always | Yes · No |
| 38 | Respiratory rate more than 60 per minute | RADIO |  | always | Yes · No |
| 39 | (D) Sepsis | HEADLINE |  | always |  |
| 40 | Distended abdomen or mother says baby vomits often | RADIO |  | always | Yes · No |
| 41 | The mother tells that the child feels cold when touching or the temperature of the child is more than 89 degrees Fahrenheit (37.5 degrees C) | RADIO |  | always | Yes · No |
| 42 | and the chest is pulled inward while breathing. | RADIO |  | always | Yes · No |
| 43 | Pus in the navel | EDIT_TEXT |  | always |  |
| 44 | ASHA NAME | TEXT_VIEW |  | always |  |
| 45 | Village Name | TEXT_VIEW |  | always |  |
| 46 | Block Name | TEXT_VIEW |  | always |  |
| 47 | Still Birth | RADIO |  | always | Yes · No |
| 48 | Supervisors Remark | EDIT_TEXT |  | always |  |
| 49 | Asha Supervisor | DROPDOWN |  | always |  |
| 50 | Supervisor name | EDIT_TEXT |  | always |  |
| 51 | Signature with Date of Supervisor | DATE_PICKER |  | always |  |
| 52 | Mother-Newborn Home Visit Card | HEADLINE |  | always |  |
| 53 | Discharge of Institutional Delivery | HEADLINE |  | always |  |
| 54 | Date of Home Visit | TEXT_VIEW |  | always |  |
| 55 | Is the baby alive? | RADIO |  | always | Yes · No |
| 56 | Date of death of baby | DATE_PICKER |  | always |  |
| 57 | Time of death of baby | TIME_PICKER |  | always |  |
| 58 | Place of Baby Death | DROPDOWN |  | always |  |
| 59 | Other place of Baby Death | EDIT_TEXT |  | always |  |
| 60 | Is the baby preterm? | RADIO |  | always | Yes · No |
| 61 | How many weeks has it been since baby born (Gestational Age) | RADIO | * | always |  |
| 62 | Date of First examination of baby | DATE_PICKER |  | always |  |
| 63 | Time of First examination of baby | TIME_PICKER |  | always |  |
| 64 | Is the mother alive? | RADIO |  | always | Yes · No |
| 65 | Date of death of mother | DATE_PICKER |  | always |  |
| 66 | Time of death of mother | TIME_PICKER |  | always |  |
| 67 | Place of mother Death | DROPDOWN |  | always |  |
| 68 | Other place of mother Death | EDIT_TEXT |  | always |  |
| 69 | Does Mother have any problems | CHECKBOXES |  | always |  |
| 70 | Other - What was given as the first feed to baby after birth? | EDIT_TEXT |  | always |  |
| 71 | When was the baby first fed | TIME_PICKER |  | always |  |
| 72 | Does the mother have breastfeeding problem? | RADIO |  | always | Yes · No |
| 73 | Write the problem, if there is any problem in breast feeding, help the mother to overcome it | EDIT_TEXT |  | always |  |
| 74 | Part 2: Baby first health check-up | HEADLINE |  | always |  |
| 75 | Temperature of the baby | EDIT_TEXT |  | always |  |
| 76 | If yes, either ASHA, ANM/MPW or TBA can tie again with a clean thread. Action taken? | RADIO |  | always | Yes · No |
| 77 | Weighing matches with the colour? | RADIO |  | always | Yes · No |
| 78 | Routine Newborn Care: whether the task was performed | HEADLINE |  | always |  |
| 79 | Dry the baby | RADIO |  | always | Yes · No |
| 80 | Cry weak/ stopped | RADIO |  | always | Yes · No |
| 81 | Keep the cord clean and dry | RADIO |  | always | Yes · No |
| 82 | Was there anything unusual with the baby? | RADIO |  | always |  |
| 83 | Other - unusual with the baby | EDIT_TEXT |  | always |  |
| 84 | ASHA should wash hands with soap and water before touching the baby during each visit | HEADLINE |  | always |  |
| 85 | Baby referred for any reason? | RADIO |  | always | Yes · No |
| 86 | Date of baby referral | DATE_PICKER |  | always |  |
| 87 | Place of baby referral | DROPDOWN |  | always |  |
| 88 | Other -Place of baby referral | EDIT_TEXT |  | always |  |
| 89 | Mother referred for any reason? | RADIO |  | always | Yes · No |
| 90 | Date of mother referral | DATE_PICKER |  | always |  |
| 91 | Place of mother referral | DROPDOWN |  | always |  |
| 92 | Other -Place of mother referral | EDIT_TEXT |  | always |  |

### HBYC visit — `HBYCFormDataset.kt` (27 fields)

| # | Field | Type | Req | Shown | Options |
|---|---|---|---|---|---|
| 1 | Month | EDIT_TEXT |  | always |  |
| 2 | Sub Centre | EDIT_TEXT |  | always |  |
| 3 | Year | EDIT_TEXT |  | always |  |
| 4 | Primary Health Center Name | EDIT_TEXT |  | always |  |
| 5 | Village Population | EDIT_TEXT |  | always |  |
| 6 | Total number of children aged 3 to 15 months in villages | EDIT_TEXT |  | always |  |
| 7 | Visit Date | DATE_PICKER |  | always |  |
| 8 | H.B.Y.C by age Tour | DROPDOWN |  | always | 3 · 6 · 9 · 12 · 15 |
| 9 | O.R.S. Packet delivered | RADIO |  | always | Yes · No |
| 10 | Iron folic acid syrup given (after six months of childbirth) | RADIO |  | always | Yes · No |
| 11 | Child vaccinated by age | RADIO |  | always | Yes · No |
| 12 | The child was ill | RADIO |  | always | Yes · No |
| 13 | If yes refer hospital | RADIO |  | always | Yes · No |
| 14 | Supplements given to the child according to age | RADIO |  | always | Yes · No |
| 15 | By height / length (color inscribed in mcp card) | RADIO |  | always | Yes · No |
| 16 | Number of children weighing less than –3SD orange color indicated in MCCP card referred to VHSND/PHC/NRC | RADIO |  | always | Yes · No |
| 17 | Weight according to child’s age (color inscribed in MCP card) | RADIO |  | always | Yes · No |
| 18 | Delay / constraint found in the physical and mental development of the child according to age | RADIO |  | always | Yes · No |
| 19 | If yes then Health Institute R.B.S.K. Team / A.N.M. referred to | RADIO |  | always | Yes · No |
| 20 | Vitamin – A supplements given to the child according to age | RADIO |  | always | Yes · No |
| 21 | Mark age (in month) at the time of death | DROPDOWN |  | always |  |
| 22 | Cause of death | EDIT_TEXT |  | always |  |
| 23 | Q.M/A.N.M was informed | RADIO |  | always | Yes · No |
| 24 | Place of Death | DROPDOWN |  | always | Eclampcia · Haemorrahge · Obstructed Labour · Prolonged Labour · Other · High Fever |
| 25 | Supervisor from block/district/state level in last on | RADIO |  | always | Yes · No |
| 26 | ORS in last one month Packet shortage | RADIO |  | always | Yes · No |
| 27 | IFA in last one month Decreased syrup | RADIO |  | always | Yes · No |

### Micro birth plan — `HRPMicroBirthPlanDataset.kt` (19 fields)

| # | Field | Type | Req | Shown | Options |
|---|---|---|---|---|---|
| 1 | Name of the PW | EDIT_TEXT | * | conditional |  |
| 2 | Nearest SC/HWC | EDIT_TEXT |  | always |  |
| 3 | Blood Group | DROPDOWN | * | always | A +Ve · A -Ve · B +Ve · B -Ve · AB +Ve · AB -Ve · O +Ve · O -Ve |
| 4 | Contact Number 1 | TEXT_VIEW | * | always |  |
| 5 | Contact Number 2 | EDIT_TEXT |  | always |  |
| 6 | SC/HWC/TG Hosp | EDIT_TEXT |  | always |  |
| 7 | Block | EDIT_TEXT |  | always |  |
| 8 | Nearest 24x7 PHC | EDIT_TEXT |  | always |  |
| 9 | Nearest FRU | EDIT_TEXT |  | always |  |
| 10 | Blood donors identified 1 | EDIT_TEXT |  | always |  |
| 11 | Blood donors identified 2 | EDIT_TEXT |  | always |  |
| 12 | Birth Companion | EDIT_TEXT |  | always |  |
| 13 | Person who will take care of children, if any when the PW is admitted for delivery | EDIT_TEXT |  | always |  |
| 14 | Name of VHSND/Community member for support during emergency | EDIT_TEXT |  | always |  |
| 15 | Contact Number of VHSND/Community member for support during emergency | EDIT_TEXT |  | always |  |
| 16 | Mode of transportation in case of labour pain | EDIT_TEXT |  | always |  |
| 17 | Nearest USG centre | EDIT_TEXT |  | always |  |
| 18 | Micro Birth Plan (For All Pregnant Women) | HEADLINE |  | always |  |
| 19 | Husband's Name | TEXT_VIEW | * | always |  |

### HR non-pregnant · assess — `HRPNonPregnantAssessDataset.kt` (12 fields)

| # | Field | Type | Req | Shown | Options |
|---|---|---|---|---|---|
| 1 | No. of Deliveries is more than 3 | RADIO | * | always | Yes · No |
| 2 | Time from last delivery is less than 18 months | RADIO | * | always | Yes · No |
| 3 | Height is very short or less than 140 cms | RADIO | * | always | Yes · No |
| 4 | Age is less than 18 or more than 35 years | RADIO | * | always | Yes · No |
| 5 | Miscarriage/abortion | RADIO | * | always | Yes · No |
| 6 | Home delivery of previous pregnancy | RADIO | * | always | Yes · No |
| 7 | During pregnancy or delivery you faced any medical issues | RADIO | * | always | Yes · No |
| 8 | Past C –section (CS) | RADIO | * | always | Yes · No |
| 9 | Information on Children | HEADLINE |  | always |  |
| 10 | Physical Observation | HEADLINE |  | always |  |
| 11 | Obstetric History | HEADLINE |  | always |  |
| 12 | Assess for High Risk Conditions in the Non-Pregnant Women | HEADLINE |  | always |  |

### HR non-pregnant · follow-up — `HRPNonPregnantTrackDataset.kt` (23 fields)

| # | Field | Type | Req | Shown | Options |
|---|---|---|---|---|---|
| 1 | Date of Visit | DATE_PICKER | * | always |  |
| 2 | Visible signs of Anemia as per appearance | RADIO | * | always | Yes · No |
| 3 | For Clinical Assessment | HEADLINE |  | always |  |
| 4 | Hypertension | RADIO | * | always | Yes · No |
| 5 | Diabetes | RADIO | * | always | Yes · No |
| 6 | Severe Anemia | RADIO | * | always | Yes · No |
| 7 | CURRENT FP/ PREGNANCY STATUS | HEADLINE |  | always |  |
| 8 | Adoption of Family Planning | RADIO | * | always | Yes · No |
| 9 | LMP | DATE_PICKER | * | always |  |
| 10 | Missed Period | RADIO | * | always | Yes · No |
| 11 | Is Pregnant | RADIO | * | always | Yes · No |
| 12 | RISK STATUS | HEADLINE |  | always |  |
| 13 | Follow-up for High Risk Conditions in the Non-Pregnant Women | HEADLINE |  | always |  |
| 14 | Blood Pressure (BP) | HEADLINE |  | always |  |
| 15 | Systolic (mm Hg) | EDIT_TEXT | * | always |  |
| 16 | Diastolic (mm Hg) | EDIT_TEXT | * | always |  |
| 17 | Blood Glucose (Sugar) Test | RADIO | * | always | RBS · FBS & PPBS |
| 18 | Random blood Sugar (RBS) (mg/dL) | EDIT_TEXT | * | conditional |  |
| 19 | Fasting Blood Sugar Test (FBS) (mg/dL) | EDIT_TEXT | * | conditional |  |
| 20 | Post-Prandial Blood Sugar Test (PPBS) (mg/dL) | EDIT_TEXT | * | conditional |  |
| 21 | Hemoglobin (Hb) Test (g/dl) | EDIT_TEXT | * | always |  |
| 22 | Whether IFA supplement is provided ? | RADIO | * | always | Yes · No |
| 23 | Issued quantity of IFA supplement | EDIT_TEXT | * | conditional |  |

### HRP pregnant · assess — `HRPPregnantAssessDataset.kt` (14 fields)

| # | Field | Type | Req | Shown | Options |
|---|---|---|---|---|---|
| 1 | No. of Deliveries is more than 3 | RADIO | * | always | Yes · No |
| 2 | Time from last delivery is less than 18 months | RADIO | * | always | Yes · No |
| 3 | Height is very short or less than 140 cms | RADIO | * | always | Yes · No |
| 4 | Age is less than 18 or more than 35 years | RADIO | * | always | Yes · No |
| 5 | Rh Negative | RADIO | * | always | Yes · No |
| 6 | Home delivery of previous pregnancy | RADIO | * | always | Yes · No |
| 7 | Bad obstetric history | RADIO | * | always | Yes · No |
| 8 | Multiple Pregnancy | RADIO | * | always | Yes · No |
| 9 | LMP Date | DATE_PICKER | * | always |  |
| 10 | EDD | DATE_PICKER | * | always |  |
| 11 | Information on Children | HEADLINE |  | always |  |
| 12 | Physical Observation | HEADLINE |  | always |  |
| 13 | Obstetric History | HEADLINE |  | always |  |
| 14 | Assess for High Risk Conditions in the Pregnant Women | HEADLINE |  | always |  |

### HRP pregnant · follow-up — `HRPPregnantTrackDataset.kt` (29 fields)

| # | Field | Type | Req | Shown | Options |
|---|---|---|---|---|---|
| 1 | RD Malaria | RADIO | * | always | Yes · No |
| 2 | Severe Anemia | RADIO | * | always | Yes · No |
| 3 | Pregnancy Induced Hypertension | RADIO | * | always | Yes · No |
| 4 | Gestational Diabetes Mellitus | RADIO | * | always | Yes · No |
| 5 | Hypothyrodism | RADIO | * | always | Yes · No |
| 6 | Polyhydromnios | RADIO | * | always | Yes · No |
| 7 | Oligohydromnios | RADIO | * | always | Yes · No |
| 8 | Antepartum Hemorrhage | RADIO | * | always | Yes · No |
| 9 | Mal Presentation | RADIO | * | always | Yes · No |
| 10 | HIV/Syphilis/Hep B | RADIO | * | always | Yes · No |
| 11 | Date of Visit | DATE_PICKER | * | always |  |
| 12 | Follow-up for High Risk Conditions in the Pregnant Women | HEADLINE |  | always |  |
| 13 | RD Dengue | RADIO | * | always | Yes · No |
| 14 | RD Filaria | RADIO | * | always | Yes · No |
| 15 | Blood Pressure (BP) | HEADLINE |  | always |  |
| 16 | Systolic (mm Hg) | EDIT_TEXT | * | always |  |
| 17 | Diastolic (mm Hg) | EDIT_TEXT | * | always |  |
| 18 | Blood Glucose (Sugar) Test | RADIO | * | always | RBS · FBS & PPBS · OGTT |
| 19 | Random blood Sugar (RBS) (mg/dL) | EDIT_TEXT | * | always |  |
| 20 | Fasting Blood Sugar Test (FBS) (mg/dL) | EDIT_TEXT | * | always |  |
| 21 | Post-Prandial Blood Sugar Test (PPBS) (mg/dL) | EDIT_TEXT | * | always |  |
| 22 | Using 75 gm OGTT | HEADLINE | * | always |  |
| 23 | Fasting Blood Sugar Test (FBS) (mg/dL) | EDIT_TEXT | * | always |  |
| 24 | After 2 Hours (mg/dL) | EDIT_TEXT | * | always |  |
| 25 | Hemoglobin (Hb) Test (g/dl) | EDIT_TEXT | * | always |  |
| 26 | Whether IFA or Folic Acid supplement is provided? | RADIO | * | always | Yes · No |
| 27 | Issued quantity of IFA or Folic Acid supplement | EDIT_TEXT | * | conditional |  |
| 28 | High risk conditions | HEADLINE |  | always |  |
| 29 | visit | HEADLINE |  | always |  |

### Household registration — `HouseholdFormDataset.kt` (28 fields)

| # | Field | Type | Req | Shown | Options |
|---|---|---|---|---|---|
| 1 | Family Details | HEADLINE |  | always |  |
| 2 | First Name of Head of the family | EDIT_TEXT | * | always |  |
| 3 | Last Name / Surname of head of the family | EDIT_TEXT |  | always |  |
| 4 | Mobile No of Head of Family | EDIT_TEXT | * | always |  |
| 5 | House No | EDIT_TEXT |  | always |  |
| 6 | Ward No | EDIT_TEXT |  | always |  |
| 7 | Ward Name | EDIT_TEXT |  | always |  |
| 8 | Mohalla Name | EDIT_TEXT |  | always |  |
| 9 | Economic Status | RADIO | * | always | APL · BPL · Don't Know |
| 10 | Do you have ABHA ID / Ayushman ID? | RADIO |  | always | Yes · No |
| 11 | Enter Abha Id / Ayushman Card Number | EDIT_TEXT |  | always |  |
| 12 | Submit | BUTTON |  | always |  |
| 13 | Village | DROPDOWN | * | always |  |
| 14 | Household Details | HEADLINE |  | always |  |
| 15 | Type of residential area | DROPDOWN |  | always | Rural · Urban · Tribal · Other · Tea Garden |
| 16 | Other type of residential area | EDIT_TEXT | * | always |  |
| 17 | Type of house | DROPDOWN | * | always | None · Kuchha · Pucca · Other |
| 18 | House ownership | RADIO | * | always | Yes · No |
| 19 | Household Amenities | HEADLINE |  | always |  |
| 20 | Separate Kitchen | RADIO | * | always | Yes · No |
| 21 | Type of fuel used for Cooking | DROPDOWN | * | always | Firewood · Crop Residue · Cow dung cake · Coal · Kerosene · LPG · Induction · Other |
| 22 | Other Type of fuel used for Cooking | EDIT_TEXT | * | always |  |
| 23 | Primary Source of Water | DROPDOWN | * | always | Tap Water · Hand pump inside house · Hand pump outside of house · Well · Tank · River · Pond · Other |
| 24 | Other Primary Source of Water | EDIT_TEXT | * | always |  |
| 25 | Availability of Electricity | DROPDOWN | * | always | Electricity Supply · Generator · Solar Power · Kerosene Lamp · Other |
| 26 | Other availability of Electricity | EDIT_TEXT | * | always |  |
| 27 | Availability of Toilet | DROPDOWN | * | always | Flush toilet with running water · Flush toilet without water · Pit toilet with running water supply · Pit toilet without water supply · Other · None |
| 28 | Other Availability of Toilet | EDIT_TEXT | * | always |  |

### Immunization record — `ImmunizationDataset.kt` (15 fields)

| # | Field | Type | Req | Shown | Options |
|---|---|---|---|---|---|
| 1 | Name | TEXT_VIEW |  | always |  |
| 2 | Mother's Name | TEXT_VIEW |  | always |  |
| 3 | Date of Birth | TEXT_VIEW |  | always |  |
| 4 | Date of vaccination | EDIT_TEXT |  | always |  |
| 5 | No. of Doses Taken | EDIT_TEXT |  | always |  |
| 6 | Vaccine Name | TEXT_VIEW |  | always |  |
| 7 | Dose Number | TEXT_VIEW |  | always |  |
| 8 | Expected Date | TEXT_VIEW |  | always |  |
| 9 | Date of Vaccination | DATE_PICKER | * | always |  |
| 10 | Vaccinated Place | DROPDOWN |  | always | Sub-Centre · PHC · CHC · Sub-District Hospital · District Hospital · Medical College Hospital · Private Hospital · Accredited Private Hospital · VHND VHSND U-WIN Session · Anganwadi Centre (AWC) |
| 11 | Vaccinated By | DROPDOWN |  | always | ANM · CHO · MO |
| 12 | Dose Name | TEXT_VIEW |  | always |  |
| 13 | Vaccination Due Date | TEXT_VIEW |  | always |  |
| 14 | MCP Card 1 | FILE_UPLOAD |  | always |  |
| 15 | MCP Card 2 | FILE_UPLOAD |  | always |  |

### Infant registration — `InfantRegistrationDataset.kt` (21 fields)

| # | Field | Type | Req | Shown | Options |
|---|---|---|---|---|---|
| 1 | Name of Baby | TEXT_VIEW |  | always |  |
| 2 | Infant Term | TEXT_VIEW |  | always | Full Term · Pre Term |
| 3 | Was Corticosteroid Inj. given? | RADIO |  | always | Yes · No · Don't Know |
| 4 | Sex of Infant | RADIO | * | always |  |
| 5 | Baby Cried Immediately at Birth | RADIO |  | always | Yes · No |
| 6 | If No, Resuscitation Done | RADIO | * | conditional | Yes · No |
| 7 | Referred to higher facility for further management | RADIO |  | conditional | Yes · No · NA |
| 8 | Any birth defect seen in at birth? | RADIO |  | always | Yes · No · NA |
| 9 | Defect seen at birth | DROPDOWN |  | conditional | Cleft Lip / Cleft Palate · Club Foot · Down's Syndrome · Hydrocephalus · Imperforate Anus · Neural Tube Defect (Spinal Bifida) · Other |
| 10 | Other defect seen at Birth | EDIT_TEXT |  | conditional |  |
| 11 | Weight at Birth ( grams ) | EDIT_TEXT | * | always |  |
| 12 | Breast feeding started within one hour of birth | RADIO | * | always | Yes · No |
| 13 | Is the Baby admitted to the SNCU? | RADIO |  | always | Yes · No |
| 14 | Delivery Discharge Summary 1 | FILE_UPLOAD |  | always |  |
| 15 | Delivery Discharge Summary 2 | FILE_UPLOAD |  | always |  |
| 16 | Delivery Discharge Summary 3 | FILE_UPLOAD |  | always |  |
| 17 | Delivery Discharge Summary 4 | FILE_UPLOAD |  | always |  |
| 18 | OPV0 Dose | DATE_PICKER |  | conditional |  |
| 19 | BCG Dose | DATE_PICKER |  | conditional |  |
| 20 | HEP B-0 Dose | DATE_PICKER |  | conditional |  |
| 21 | VITK Dose | DATE_PICKER |  | conditional |  |

### Kala-azar — `KalaAzarFormDataset.kt` (13 fields)

| # | Field | Type | Req | Shown | Options |
|---|---|---|---|---|---|
| 1 | Visit Date | DATE_PICKER | * | always |  |
| 2 | Beneficiary Status | DROPDOWN | * | always |  |
| 3 | Date of death | DATE_PICKER | * | conditional |  |
| 4 | Place of Death | DROPDOWN | * | conditional |  |
| 5 | Other Place of Death | EDIT_TEXT | * | conditional |  |
| 6 | Reason for Death | DROPDOWN | * | always |  |
| 7 | Other Reason of Death | EDIT_TEXT | * | conditional |  |
| 8 | Case Status | DROPDOWN |  | always |  |
| 9 | Kala Azar Rapid Diagnostic Test (RDT) | RADIO |  | always | Positive · Negative · Not Performed |
| 10 | Date of Test | DATE_PICKER | * | conditional |  |
| 11 | Follow Up | DROPDOWN |  | always | 1 · 2 · 3 · 4 · 5 · 6 |
| 12 | Referred To | DROPDOWN |  | always | Primary Health Centre · Community Health Centre · District Hospital · Medical College and Hospital · Referral Hospital · Other Private Hospital · Other |
| 13 | Other | EDIT_TEXT | * | conditional |  |

### Leprosy confirmed — `LeprosyConfirmedDataset.kt` (14 fields)

| # | Field | Type | Req | Shown | Options |
|---|---|---|---|---|---|
| 1 | Home Visit Date | TEXT_VIEW |  | always |  |
| 2 | Leprosy Status | DROPDOWN |  | always | Screening · Denied · Not Screened · Suspected · Confirmed · Not Confirmed |
| 3 | Referred To | DROPDOWN |  | always | Primary Health Centre · Community Health Centre · District Hospital · Medical College and Hospital · Referral Hospital · Other Private Hospital · Other |
| 4 | Other | EDIT_TEXT |  | conditional |  |
| 5 | Follow Up Dates | DATE_PICKER | * | always |  |
| 6 | Remarks | EDIT_TEXT |  | conditional |  |
| 7 | Any Leprosy Symptoms Present? | RADIO |  | always | Yes · No |
| 8 | Visit | TEXT_VIEW |  | always |  |
| 9 | Type of leprosy confirmed | RADIO |  | always |  |
| 10 | Treatment Start Date | DATE_PICKER | * | always |  |
| 11 | MDT/BLISTER PACK RECEIVED | RADIO |  | always | Yes · No |
| 12 | Treatment Status | DROPDOWN | * | always | Recovering · Cured · Recurrence of Symptoms · Regularly taking medicine · Not regularly taking medicine |
| 13 | Actual Treatment Completion Date | DATE_PICKER | * | conditional |  |
| 14 | Expected Treatment Completion Date | DATE_PICKER |  | always |  |

### Leprosy screening — `LeprosyFormDataset.kt` (27 fields)

| # | Field | Type | Req | Shown | Options |
|---|---|---|---|---|---|
| 1 | Check if the person has any of these symptoms | CHECKBOXES |  | always |  |
| 2 | Home Visit Date (Screening Date) | DATE_PICKER | * | always |  |
| 3 | Beneficiary Status | DROPDOWN | * | always |  |
| 4 | Date of death | DATE_PICKER | * | always |  |
| 5 | Place of Death | DROPDOWN | * | always |  |
| 6 | Other Place of Death | EDIT_TEXT | * | conditional |  |
| 7 | Reason for Death | DROPDOWN | * | always |  |
| 8 | Other Reason of Death | EDIT_TEXT | * | conditional |  |
| 9 | Leprosy Status | DROPDOWN |  | always | Screening · Denied · Not Screened · Suspected · Confirmed · Not Confirmed |
| 10 | Referred To | DROPDOWN |  | always | Primary Health Centre · Community Health Centre · District Hospital · Medical College and Hospital · Referral Hospital · Other Private Hospital · Other |
| 11 | Other | EDIT_TEXT | * | conditional |  |
| 12 | Type of leprosy confirmed | DROPDOWN |  | always |  |
| 13 | Follow Up Dates | DATE_PICKER |  | always |  |
| 14 | Remarks | EDIT_TEXT |  | always |  |
| 15 | Recurrent ulceration on palm or sole | RADIO | * | always | Yes · No |
| 16 | Recurrent tingling on palm(s) or sole (s) | RADIO | * | always | Yes · No |
| 17 | Any hyper pigmented patch or discoloration on skin with loss of sensation | RADIO | * | always | Yes · No |
| 18 | Any thickend skin | RADIO | * | always | Yes · No |
| 19 | Any nodules on skin | RADIO | * | always | Yes · No |
| 20 | Recurrent numbness on palm(s) or sole(s) | RADIO | * | always | Yes · No |
| 21 | Clawing of fingers in hand(s) or Feet | RADIO | * | always | Yes · No |
| 22 | Tingling or Numbness in hands and or Feet | RADIO | * | always | Yes · No |
| 23 | Inability to close eyelid | RADIO | * | always | Yes · No |
| 24 | Difficulty in holding objects with fingers | RADIO | * | always | Yes · No |
| 25 | Weakness in feet that causes difficulty in walking | RADIO | * | always | Yes · No |
| 26 | Any Leprosy Symptoms Present? | RADIO | * | always | Yes · No |
| 27 | Visit | TEXT_VIEW | * | always |  |

### Leprosy suspected — `LeprosySuspectedDataset.kt` (12 fields)

| # | Field | Type | Req | Shown | Options |
|---|---|---|---|---|---|
| 1 | Home Visit Date | TEXT_VIEW | * | always |  |
| 2 | Leprosy Status | DROPDOWN |  | always | Screening · Denied · Not Screened · Suspected · Confirmed · Not Confirmed |
| 3 | Referred To | DROPDOWN |  | always | Primary Health Centre · Community Health Centre · District Hospital · Medical College and Hospital · Referral Hospital · Other Private Hospital · Other |
| 4 | Other | EDIT_TEXT | * | conditional |  |
| 5 | Type of leprosy confirmed | DROPDOWN |  | conditional |  |
| 6 | Follow Up Dates | DATE_PICKER |  | conditional |  |
| 7 | Remarks | EDIT_TEXT |  | conditional |  |
| 8 | Any Leprosy Symptoms Present? | RADIO | * | always | Yes · No |
| 9 | Visit | TEXT_VIEW | * | always |  |
| 10 | Type of leprosy confirmed | RADIO | * | conditional |  |
| 11 | Treatment Start Date | DATE_PICKER |  | conditional |  |
| 12 | Has leprosy been confirmed? | RADIO | * | always | Yes · No |

### Maternal death report (MDSR) — `MDSRFormDataset.kt` (33 fields)

| # | Field | Type | Req | Shown | Options |
|---|---|---|---|---|---|
| 1 | Date of Death | TEXT_VIEW | * | always |  |
| 2 | Address | TEXT_VIEW |  | conditional |  |
| 3 | Husband’s name | TEXT_VIEW |  | always |  |
| 4 | Cause of death | RADIO | * | always | Suspected Maternal death · Non-maternal death |
| 5 | specify reasons | EDIT_TEXT | * | conditional |  |
| 6 | Date of field investigation | DATE_PICKER |  | conditional |  |
| 7 | Action taken | RADIO |  | conditional | Yes · No |
| 8 | Signature of MO I/C of the block: | EDIT_TEXT |  | conditional |  |
| 9 | Date | DATE_PICKER |  | always |  |
| 10 | a. During pregnancy | RADIO |  | always | Yes · No |
| 11 | b. During delivery | RADIO |  | always | Yes · No |
| 12 | c. Within 42 days after delivery | RADIO |  | always | Yes · No |
| 13 | d. During abortion or within 6 weeks after abortion | RADIO |  | always | Yes · No |
| 14 | Name of reporting Person  | TEXT_VIEW |  | always |  |
| 15 | Designation | TEXT_VIEW |  | always |  |
| 16 | Verification by ANM of the respective Sub-center that death of women occurred during pregnancy or within 42 days of delivery/abortion: | HEADLINE |  | always |  |
| 17 | Name of ANM | TEXT_VIEW |  | always |  |
| 18 | Name of the sub center | TEXT_VIEW |  | always |  |
| 19 | MDSR form from ANM 1 | FILE_UPLOAD |  | always |  |
| 20 | MDSR form from ANM 2 | FILE_UPLOAD |  | always |  |
| 21 | Death Certificate | FILE_UPLOAD |  | always |  |
| 22 | State | TEXT_VIEW |  | always |  |
| 23 | District | TEXT_VIEW |  | always |  |
| 24 | Block | TEXT_VIEW |  | always |  |
| 25 | Village/Town/City | TEXT_VIEW |  | always |  |
| 26 | Father Name | TEXT_VIEW |  | always |  |
| 27 | Age Of Woman | TEXT_VIEW |  | always |  |
| 28 | RCH ID | TEXT_VIEW |  | always |  |
| 29 | Mobile Number | TEXT_VIEW |  | always |  |
| 30 | Time of Death | TEXT_VIEW |  | always |  |
| 31 | Place of Death | TEXT_VIEW |  | always |  |
| 32 | When did death occur | HEADLINE |  | always |  |
| 33 | Name of the deceased woman | TEXT_VIEW |  | always |  |

### Maa meeting — `MaaMeetingDataset.kt` (8 fields)

| # | Field | Type | Req | Shown | Options |
|---|---|---|---|---|---|
| 1 | MAA Meeting Date | DATE_PICKER | * | always |  |
| 2 | Place of MAA Meeting | DROPDOWN | * | always | Anganwadi Centre · HWC · School · Community Center |
| 3 | Village Name | EDIT_TEXT |  | always |  |
| 4 | Number of Pregnant Woman Attended | EDIT_TEXT |  | always |  |
| 5 | Number of Lactating Mothers attended | EDIT_TEXT |  | always |  |
| 6 | During Breastfeeding Counseling check list | CHECKBOXES |  | always | Select All · The baby should be breastfed immediately within the first hour of birth · The baby should be given only mother’s milk for the first 6 months. · After 6 months, along with mother’s milk, nutritious complementary food should be given. · Breastfeeding should be continued for 2 years or more. · The family should support and encourage the mother. · For the first 6 months, the baby should not be given honey, water, or ghutti (herbal concoctions). · Breastfeeding protects the baby from many diseases. · Mother’s milk is the best for the complete development of the baby. · Information was given about KMC (Kangaroo Mother Care). |
| 7 | Total No. of Beneficiaries Attended | EDIT_TEXT | * | always |  |
| 8 | Meeting Photos / MoM (%1$d) | FILE_UPLOAD |  | conditional |  |

### Malaria confirmed · treatment — `MalariaConfirmCasesDataset.kt` (6 fields)

| # | Field | Type | Req | Shown | Options |
|---|---|---|---|---|---|
| 1 | Date of Starting Treatment | DATE_PICKER | * | always |  |
| 2 | Treatment Given | RADIO |  | always | Pf · Pv |
| 3 | Pf - Day-wise Tracking | DROPDOWN |  | always | Day 1 · Day 2 · Day 3 |
| 4 | PV - Day-wise Tracking | DROPDOWN |  | always |  |
| 5 | Date of Completion of Treatment | DATE_PICKER | * | always |  |
| 6 | Date of Referral | DATE_PICKER |  | always |  |

### Malaria screening — `MalariaFormDataset.kt` (30 fields)

| # | Field | Type | Req | Shown | Options |
|---|---|---|---|---|---|
| 1 | Maleria Screening Date | DATE_PICKER | * | always |  |
| 2 | Beneficiary Status | DROPDOWN | * | always |  |
| 3 | Date of death | DATE_PICKER | * | conditional |  |
| 4 | Place of Death | DROPDOWN | * | conditional |  |
| 5 | Other Place of Death | EDIT_TEXT | * | conditional |  |
| 6 | Reason for Death | DROPDOWN | * | conditional |  |
| 7 | Other Reason of Death | EDIT_TEXT | * | conditional |  |
| 8 | Check if the person has any of these Symptoms: | HEADLINE |  | always |  |
| 9 | Fever for > 2 weeks | RADIO | * | always | Yes · No |
| 10 | Flu-like illness | RADIO | * | always | Yes · No |
| 11 | Shaking chills | RADIO | * | always | Yes · No |
| 12 | Headache | RADIO | * | always | Yes · No |
| 13 | Muscle aches | RADIO | * | always | Yes · No |
| 14 | Tiredness | RADIO | * | always | Yes · No |
| 15 | Nausea | RADIO | * | always | Yes · No |
| 16 | Vomiting | RADIO | * | always | Yes · No |
| 17 | Diarrhea | RADIO | * | always | Yes · No |
| 18 | Case Status | DROPDOWN |  | always |  |
| 19 | Malaria Test Type | RADIO |  | always |  |
| 20 | Rapid Diagnostic Test (RDT) | RADIO |  | always | Positive · Negative · Not Performed |
| 21 | Date of Test | DATE_PICKER | * | conditional |  |
| 22 | Malaria Slide Test | RADIO |  | always | Pf · Pv |
| 23 | Malaria Slide Test Pf | RADIO |  | conditional | Positive · Negative · Not Performed |
| 24 | Malaria Slide Test Pv | RADIO |  | conditional | Positive · Negative · Not Performed |
| 25 | Date of Test | DATE_PICKER | * | conditional |  |
| 26 | Referred To | DROPDOWN |  | always | Primary Health Centre · Community Health Centre · District Hospital · Medical College and Hospital · Referral Hospital · Other Private Hospital · Other · None |
| 27 | Other | EDIT_TEXT | * | conditional |  |
| 28 | Follow Up Dates | DATE_PICKER | * | conditional |  |
| 29 | Remarks | EDIT_TEXT |  | always |  |
| 30 | Date of Visit by Supervisor | DATE_PICKER |  | always |  |

### Child / adolescent registration (0–14) — `NewChildBenRegDataset.kt` (61 fields)

| # | Field | Type | Req | Shown | Options |
|---|---|---|---|---|---|
| 1 | Date of Registration | DATE_PICKER | * | always |  |
| 2 | Number of Adult Children (Age Above 15 Years) | org | * | always |  |
| 3 | Registration for Children Under 15 Years of Age | org |  | always |  |
| 4 | No. of Live Children | org |  | always |  |
| 5 | Male | TEXT_VIEW |  | conditional |  |
| 6 | Female | TEXT_VIEW |  | conditional |  |
| 7 | Age of Woman at Marriage | TEXT_VIEW |  | conditional |  |
| 8 | Details of 1st Child | HEADLINE |  | conditional |  |
| 9 | Child Name | EDIT_TEXT | * | conditional |  |
| 10 | 1st Child Date of Birth | DATE_PICKER | * | conditional |  |
| 11 | 1st Child Age ( in Years) | TEXT_VIEW | * | conditional |  |
| 12 | 1st Child Sex | RADIO | * | conditional |  |
| 13 | Gap between Date of marriage and 1st child | TEXT_VIEW | * | conditional |  |
| 14 | Details of 2nd Child | HEADLINE |  | conditional |  |
| 15 | Child Name | EDIT_TEXT | * | conditional |  |
| 16 | 2nd Child Date of Birth | DATE_PICKER | * | conditional |  |
| 17 | 2nd Child Age ( in Years) | TEXT_VIEW | * | conditional |  |
| 18 | 2nd Child Sex | RADIO | * | conditional |  |
| 19 | Gap between 1st child and 2nd Child | TEXT_VIEW | * | conditional |  |
| 20 | Details of 3rd Child | HEADLINE |  | conditional |  |
| 21 | Child Name | EDIT_TEXT | * | conditional |  |
| 22 | 3rd Child Date of Birth | DATE_PICKER | * | conditional |  |
| 23 | 3rd Child Age ( in Years) | TEXT_VIEW | * | conditional |  |
| 24 | 3rd Child Sex | RADIO | * | conditional |  |
| 25 | Gap between 2nd child and 3rd Child | TEXT_VIEW | * | conditional |  |
| 26 | Details of 4th Child | HEADLINE |  | conditional |  |
| 27 | Child Name | EDIT_TEXT | * | conditional |  |
| 28 | 4th Child Date of Birth | DATE_PICKER | * | conditional |  |
| 29 | 4th Child Age ( in Years) | TEXT_VIEW | * | conditional |  |
| 30 | 4th Child Sex | RADIO | * | conditional |  |
| 31 | Gap between 3rd Child and 4th Child | TEXT_VIEW | * | conditional |  |
| 32 | Details of 5th Child | HEADLINE |  | conditional |  |
| 33 | Child Name | EDIT_TEXT | * | conditional |  |
| 34 | 5th Child Date of Birth | DATE_PICKER | * | conditional |  |
| 35 | 5th Child Age ( in Years) | TEXT_VIEW | * | conditional |  |
| 36 | 5th Child Sex | RADIO | * | conditional |  |
| 37 | Gap between 4th child and 5th Child | TEXT_VIEW | * | conditional |  |
| 38 | Details of 6th Child | HEADLINE |  | conditional |  |
| 39 | Child Name | EDIT_TEXT | * | conditional |  |
| 40 | 6th Child Date of Birth | DATE_PICKER | * | conditional |  |
| 41 | 6th Child Age ( in Years) | TEXT_VIEW | * | conditional |  |
| 42 | 6th Child Sex | RADIO | * | conditional |  |
| 43 | Gap between 5th child and 6th Child | TEXT_VIEW | * | conditional |  |
| 44 | Details of 7th Child | HEADLINE |  | conditional |  |
| 45 | Child Name | EDIT_TEXT | * | conditional |  |
| 46 | 7th Child Date of Birth | DATE_PICKER | * | conditional |  |
| 47 | 7th Child Age ( in Years) | TEXT_VIEW | * | conditional |  |
| 48 | 7th Child Sex | RADIO | * | conditional |  |
| 49 | Gap between 6th child and 7th Child | TEXT_VIEW | * | conditional |  |
| 50 | Details of 8th Child | HEADLINE |  | conditional |  |
| 51 | Child Name | EDIT_TEXT | * | conditional |  |
| 52 | 8th Child Date of Birth | DATE_PICKER | * | conditional |  |
| 53 | 8th Child Age ( in Years) | TEXT_VIEW | * | conditional |  |
| 54 | 8th Child Sex | RADIO | * | conditional |  |
| 55 | Gap between 7th child and 8th Child | TEXT_VIEW | * | conditional |  |
| 56 | Details of 9th Child | HEADLINE |  | conditional |  |
| 57 | Child Name | EDIT_TEXT | * | conditional |  |
| 58 | 9th Child Date of Birth | DATE_PICKER | * | conditional |  |
| 59 | 9th Child Age ( in Years) | TEXT_VIEW | * | conditional |  |
| 60 | 9th Child Sex | RADIO | * | conditional |  |
| 61 | Gap between 8th child and 9th Child | TEXT_VIEW | * | conditional |  |

### PHC review meeting — `PHCReviewDataset.kt` (8 fields)

| # | Field | Type | Req | Shown | Options |
|---|---|---|---|---|---|
| 1 | Meeting Date | DATE_PICKER | * | always |  |
| 2 | Place of meeting | DROPDOWN | * | always | Anganwadi Centre · HWC · School · Community center |
| 3 | Village Name | EDIT_TEXT |  | always |  |
| 4 | Total No. of Participants | EDIT_TEXT |  | always |  |
| 5 | Participants Attended | CHECKBOXES |  | always | Mitanins · MT · ANM/ RHO |
| 6 | Mitanin Activity check list | CHECKBOXES |  | always | Select All · Information was shared about new schemes or guidelines issued by the Health Department. · Data registers and records were checked. · Training and capacity building were discussed. · Review of Dava Peti (Medicine Kit). · Supply distribution and related activities were carried out. · Progress of pregnant and breastfeeding Mothers was discussed. · Referral of special cases (serious patients) was discussed. · Updates were given on Child Immunization. |
| 7 | Upload Image | FILE_UPLOAD |  | always |  |
| 8 | Upload Image | FILE_UPLOAD |  | always |  |

### PMJAY form — `PMJAYFormDataset.kt` (13 fields)

| # | Field | Type | Req | Shown | Options |
|---|---|---|---|---|---|
| 1 | Id Number | EDIT_TEXT |  | always |  |
| 2 | Registration Date | DATE_PICKER |  | always |  |
| 3 | Registered Hospital | EDIT_TEXT |  | always |  |
| 4 | Contact No | EDIT_TEXT |  | always |  |
| 5 | Communication Contact No | EDIT_TEXT |  | always |  |
| 6 | Patient Address | EDIT_TEXT |  | always |  |
| 7 | Communication Address | EDIT_TEXT |  | always |  |
| 8 | Hospital Address | EDIT_TEXT |  | always |  |
| 9 | Family Id | EDIT_TEXT |  | always |  |
| 10 | Is Aadhaar Beneficiary | EDIT_TEXT |  | always |  |
| 11 | Member Type | EDIT_TEXT |  | always |  |
| 12 | Patient Type | DROPDOWN |  | always | Select Type · General OP · IP |
| 13 | Scheme | DROPDOWN |  | always | Select Scheme |

### PMSMA visit — `PMSMAFormDataset.kt` (45 fields)

| # | Field | Type | Req | Shown | Options |
|---|---|---|---|---|---|
| 1 | PMSMA Visit Date | org | * | always |  |
| 2 | PMSMA Visit | org | * | always |  |
| 3 | Details of Pregnant women | HEADLINE |  | always |  |
| 4 | if yes, select high-risk condition | org | * | always | HIGH BP (SYSTOLIC >=140 AND OR DIASTOLIC >=90 mmHg) · CONVULSIONS · VAGINAL BLEEDING · FOUL SMELLING DISCHARGE · SEVERE ANAEMIA (HB <7 gm/dl) · DIABETES · TWINS · OTHER |
| 5 | Any other High Risk conditions | EDIT_TEXT | * | always |  |
| 6 | MCTS Number/RCH Number | TEXT_VIEW |  | always |  |
| 7 | Does the beneficiary have an MCP card | RADIO |  | always | Yes · No |
| 8 | MCP card is given | RADIO |  | always | Yes · No |
| 9 | Husband’s Name | TEXT_VIEW |  | always |  |
| 10 | Address | EDIT_TEXT |  | always |  |
| 11 | Mobile number | TEXT_VIEW |  | always |  |
| 12 | Number of ANCs done before delivery | TEXT_VIEW |  | always |  |
| 13 | Status of Mother | HEADLINE |  | always |  |
| 14 | Weight (in Kg) | EDIT_TEXT |  | always |  |
| 15 | BP of PW – Systolic/ Diastolic (mm Hg) | EDIT_TEXT |  | always |  |
| 16 | Abdominal Check-up | TEXT_VIEW |  | always |  |
| 17 | Fetal Status | HEADLINE |  | always |  |
| 18 | Fetal Heart Rate per minute | EDIT_TEXT |  | always |  |
| 19 | Twins pregnancy | RADIO |  | always | Yes · No |
| 20 | Investigations | HEADLINE |  | always |  |
| 21 | Urine albumin | RADIO |  | always | Yes · No |
| 22 | Haemoglobin | EDIT_TEXT |  | always |  |
| 23 | HIV | TEXT_VIEW |  | always |  |
| 24 | Vdrl | TEXT_VIEW |  | always |  |
| 25 | HBSG (Hepatitis B) | TEXT_VIEW |  | always |  |
| 26 | Malaria | RADIO |  | always | Negative · Positive · Test Not Done |
| 27 | Was HIV test done during ANC Check up? | RADIO |  | always | Yes · No |
| 28 | Swollen condition | RADIO |  | always | Yes · No |
| 29 | Blood sugar test | RADIO |  | always | Yes · No |
| 30 | Ultrasound | RADIO |  | always | Yes · No |
| 31 | Intervention Details | HEADLINE |  | always |  |
| 32 | Iron Folic Acid | RADIO |  | always | Yes · No |
| 33 | Calcium Supplementation | RADIO |  | always | Yes · No |
| 34 | Tetanus toxoid | RADIO |  | always | 1st Dose · 2nd Dose · Booster Dose |
| 35 | LMP | HEADLINE |  | always |  |
| 36 | Last Menstrual Period | TEXT_VIEW |  | always |  |
| 37 | Expected Date of Delivery | TEXT_VIEW |  | always |  |
| 38 | Identification of high-risk factors | HEADLINE |  | always |  |
| 39 | Identification of high risk symbols | RADIO |  | always | Yes · No |
| 40 | If yes, select the reason give below and write | EDIT_TEXT |  | always |  |
| 41 | Pregnant in high risk category treated | RADIO |  | always | Yes · No |
| 42 | Pregnant in high risk category was referred | RADIO |  | always | Yes · No |
| 43 | Advice | HEADLINE |  | always |  |
| 44 | Preparation for birth and complications, nutrition and family planning | RADIO |  | always | Yes · No |
| 45 | Signature of medical officer in charge | EDIT_TEXT |  | always |  |

### PNC visit — `PncFormDataset.kt` (23 fields)

| # | Field | Type | Req | Shown | Options |
|---|---|---|---|---|---|
| 1 | Date of Delivery | DATE_PICKER | * | always |  |
| 2 | PNC Period | DROPDOWN | * | always | 1st Day · 3rd Day · 7th Day · 14th Day · 21st Day · 28th Day · 42nd Day |
| 3 | PNC Visit Date | DATE_PICKER | * | always |  |
| 4 | No. of IFA Tablets given | NUMBER_PICKER |  | always |  |
| 5 | Has the couple started any family planning method after delivery?\nAny Postpartum Contraception (PPC) | RADIO |  | always | Yes · No |
| 6 | Method of Contraception | DROPDOWN |  | always | POST PARTUM IUCD (PPIUCD) · CONDOM · MALE STERILIZATION · FEMALE STERILIZATION · POST PARTUM STERILIZATION (PPS) · MiniLap · ANY OTHER (SPECIFY) |
| 7 | Other PPC Method | EDIT_TEXT | * | conditional |  |
| 8 | Mother Danger Sign | DROPDOWN | * | conditional | PPH - Excessive bleeding · FEVER · SEPSIS · SEVERE ABDOMINAL PAIN · SEVERE HEADACHE OR BLURRED VISION · DIFFICULT BREATHING · Yellowness of Urine, Skin or Eyes · Pale Skin or Eyes · Swelling on face, hands and legs · Abnormal behaviour · Any Other |
| 9 | Other Danger Sign | EDIT_TEXT | * | conditional |  |
| 10 | Referral Facility | DROPDOWN |  | always | Primary Health Centre · Community Health Centre · District Hospital · Other Private Hospital |
| 11 | Is the Mother alive? | RADIO | * | always | Yes · No |
| 12 | Date of Death | DATE_PICKER | * | conditional |  |
| 13 | Reason for Death | DROPDOWN | * | conditional | ECLAMPSIA · HAEMORRHAGE (PPH) · ANAEMIA · HIGH FEVER · Sepsis · Accident · Other Maternal Death |
| 14 | Other Death Cause | EDIT_TEXT | * | conditional |  |
| 15 | Place of Death | DROPDOWN | * | conditional | Home · Subcenter · PHC · CHC · District Hospital · Medical College Hospital · Private Hospital · In Transit · Other Place of Death |
| 16 | Remarks | EDIT_TEXT |  | always |  |
| 17 | Other Place of Death | EDIT_TEXT | * | conditional |  |
| 18 | Date of Sterilisation | DATE_PICKER | * | always |  |
| 19 | Any Danger Signs?\n | RADIO |  | always | Yes · No |
| 20 | Delivery Discharge Summary 1 | FILE_UPLOAD |  | always |  |
| 21 | Delivery Discharge Summary 2 | FILE_UPLOAD |  | always |  |
| 22 | Delivery Discharge Summary 3 | FILE_UPLOAD |  | always |  |
| 23 | Delivery Discharge Summary 4 | FILE_UPLOAD |  | always |  |

### ANC · abortion — `PregnantWomanAncAbortionDataset.kt` (15 fields)

| # | Field | Type | Req | Shown | Options |
|---|---|---|---|---|---|
| 1 | Visit Date | DATE_PICKER | * | always |  |
| 2 | Weeks Of Pregnancy | TEXT_VIEW |  | always |  |
| 3 | Abortion Date | TEXT_VIEW |  | always |  |
| 4 | Abortion Type | TEXT_VIEW |  | always |  |
| 5 | Facility (Place of Abortion) | TEXT_VIEW |  | always |  |
| 6 | Serial no as per Admission/ Evacuation register | EDIT_TEXT | * | always |  |
| 7 | Method of Termination | DROPDOWN |  | always | MVA · EVA · MMA · Others |
| 8 | Termination done by | DROPDOWN |  | always | Doctor · Nurse |
| 9 | What family planning method has been chosen after the abortion? | HEADLINE |  | always |  |
| 10 |   | RADIO |  | always | Has a PAIUCD been inserted?(Copper-T)\n or · Has the woman opted for permanent sterilization (Tubectomy)? |
| 11 |   | RADIO | * | conditional | Yes · No |
| 12 | Date of Sterilisation | DATE_PICKER | * | conditional |  |
| 13 | Remarks | EDIT_TEXT |  | always |  |
| 14 | Abortion Discharge\n Summary 1 | FILE_UPLOAD |  | always |  |
| 15 | Abortion Discharge\n Summary 2 | FILE_UPLOAD |  | always |  |

### ANC visit — `PregnantWomanAncVisitDataset.kt` (39 fields)

| # | Field | Type | Req | Shown | Options |
|---|---|---|---|---|---|
| 1 | ANC Date | DATE_PICKER | * | always |  |
| 2 | Place of ANC | DROPDOWN | * | always | Sub-Centre · VHND/VHSND · PHC · PMSMA Visit · CHC · District Hospital · Medical College Hospital |
| 3 | Weeks Of Pregnancy | TEXT_VIEW_PAIR |  | always |  |
| 4 | ANC Period | DROPDOWN | * | always |  |
| 5 | Abortion If Any | RADIO |  | always | No · Yes |
| 6 | Abortion Type | DROPDOWN | * | always | Induced · Spontaneous · Incomplete |
| 7 | Place of Death | DROPDOWN | * | conditional | Home · Subcenter · PHC · CHC · District Hospital · Medical College Hospital · Private Hospital · In Transit · Other Place of Death |
| 8 | Other Place of Death | EDIT_TEXT | * | conditional |  |
| 9 | Facility (Place of Abortion) | DROPDOWN | * | always | Home · Subcenter · PHC · CHC · District Hospital · Medical College Hospital · Private Hospital · In Transit · Other Place of Abortion |
| 10 | Abortion Date | DATE_PICKER | * | always |  |
| 11 | Weight of PW (Kg) at time Registration | EDIT_TEXT |  | always |  |
| 12 | BP of PW – Systolic/ Diastolic (mm Hg) | EDIT_TEXT | * | always |  |
| 13 | BP of PW (mm Hg) – Diastolic | EDIT_TEXT |  | conditional |  |
| 14 | BP of PW (mm Hg) – Systolic | EDIT_TEXT | * | conditional |  |
| 15 | BP of PW (mm Hg) – Diastolic | EDIT_TEXT | * | conditional |  |
| 16 | Pulse Rate (BPM) | EDIT_TEXT |  | conditional |  |
| 17 | HB (gm/dl) | EDIT_TEXT | * | always |  |
| 18 | Fundal Height / Size of the Uterus weeks | EDIT_TEXT |  | conditional |  |
| 19 | Urine Albumin | RADIO |  | conditional | Absent · Present |
| 20 | Random Blood Sugar Test | RADIO |  | conditional | Not Done · Done |
| 21 | Date of Td TT (1st Dose) | DATE_PICKER |  | always |  |
| 22 | Date of Td TT (2nd Dose) | DATE_PICKER |  | always |  |
| 23 | Date of Td TT (Booster Dose) | DATE_PICKER |  | always |  |
| 24 | No. of Folic Acid Tabs given | EDIT_TEXT |  | always |  |
| 25 | No. of IFA Tabs given | EDIT_TEXT |  | always |  |
| 26 | Any High Risk conditions | RADIO |  | always | No · Yes |
| 27 | High risk conditions | DROPDOWN |  | conditional | NONE · HIGH BP (SYSTOLIC>=140 AND OR DIASTOLIC >=90mmHg) · CONVULSIONS · VAGINAL BLEEDING · FOUL SMELLING DISCHARGE · SEVERE ANAEMIA (HB less than 7 gm/dl) · DIABETES · TWINS · OTHER |
| 28 | Any other High Risk conditions | EDIT_TEXT | * | conditional |  |
| 29 | Referral Facility | DROPDOWN |  | always | Primary Health Centre · Community Health Centre · District Hospital · Other Private Hospital |
| 30 | Is HRP Confirmed? | RADIO |  | always | No · Yes |
| 31 | Who had identified as HRP? | DROPDOWN | * | conditional | ANM · CHO · PHC – MO · Specialist at Higher Facility |
| 32 | Is the Pregnant Woman alive? | RADIO |  | always | Yes · No |
| 33 | Reason for Death | DROPDOWN | * | always | ECLAMPSIA · HAEMORRHAGE · HIGH FEVER · ABORTION · Accident · Other Maternal Death |
| 34 | Other Death Cause | EDIT_TEXT | * | conditional |  |
| 35 | Date of death | DATE_PICKER | * | always |  |
| 36 | Has the pregnant woman delivered? | RADIO |  | conditional | Yes · No |
| 37 | MCP Cards | HEADLINE |  | always |  |
| 38 | Front Side | FILE_UPLOAD |  | always |  |
| 39 | Back Side | FILE_UPLOAD |  | always |  |

### Pregnancy registration — `PregnantWomanRegistrationDataset.kt` (38 fields)

| # | Field | Type | Req | Shown | Options |
|---|---|---|---|---|---|
| 1 | Date of Registration | DATE_PICKER | * | always |  |
| 2 | RCH ID | EDIT_TEXT |  | always |  |
| 3 | MCP Card No | EDIT_TEXT |  | conditional |  |
| 4 | Name of Pregnant Women | TEXT_VIEW |  | always |  |
| 5 | Name of Husband | TEXT_VIEW |  | always |  |
| 6 | Current Age of Woman | TEXT_VIEW |  | always |  |
| 7 | LMP Date | DATE_PICKER | * | always |  |
| 8 | Weeks of Pregnancy at the time of Registration | TEXT_VIEW |  | always |  |
| 9 | EDD | TEXT_VIEW |  | always |  |
| 10 | Blood Group | DROPDOWN |  | always | A +Ve · A -Ve · B +Ve · B -Ve · AB +Ve · AB -Ve · O +Ve · O -Ve |
| 11 | Weight of PW (Kg) at time Registration | EDIT_TEXT |  | always |  |
| 12 | Height of PW (Cm) | EDIT_TEXT |  | always |  |
| 13 | VDRL/RPR Test result | DROPDOWN |  | conditional | Reactive · Non - Reactive · Test Not Done |
| 14 | Date of VDRL Test done | DATE_PICKER |  | conditional |  |
| 15 | HIV Test result | DROPDOWN |  | conditional | Reactive · Non - Reactive · Test Not Done |
| 16 | Date of HIV Test done | DATE_PICKER |  | conditional |  |
| 17 | HBsAg Test result | DROPDOWN |  | conditional | Positive · Negative · Test Not Done |
| 18 | Date of HbsAg Test done | DATE_PICKER |  | conditional |  |
| 19 | Past Illness | CHECKBOXES | * | always | None · TB · Diabetes · Hypertension · Heart Disease · Epileptic · STI/RTI · Hepatites-B · Asthama · Sickle cell · Any Other |
| 20 | other | EDIT_TEXT | * | conditional |  |
| 21 | Is this your 1st pregnancy? | RADIO | * | always | Yes · No |
| 22 | Total no. of previous Pregnancy | EDIT_TEXT |  | always |  |
| 23 | Any complications in Last Pregnancy | DROPDOWN |  | always | None · CONVULSIONS · APH · PREGNANCY INDUCED HYPERTENSION (PIH) · REPEATED ABORTION · STILLBIRTH · CONGENITAL ANOMALY · CAESAREAN SECTION · BLOOD TRANSFUSION · TWINS · OBSTRUCTED LABOUR · PPH · Any Other |
| 24 | Any other Complication | EDIT_TEXT | * | conditional |  |
| 25 | Is Identified as HRP cases? | RADIO |  | always | Yes · No |
| 26 | Who had identified as HRP? | DROPDOWN | * | conditional | ANM · CHO · PHC – MO · Specialist at Higher Facility |
| 27 | No. of Deliveries is more than 3 | RADIO |  | always | Yes · No |
| 28 | Time from last delivery is less than 18 months | RADIO |  | always | Yes · No |
| 29 | Height is very short or less than 140 cms | RADIO |  | always | Yes · No |
| 30 | Age is less than 18 or more than 35 years | RADIO |  | always | Yes · No |
| 31 | Rh Negative | RADIO |  | always | Yes · No |
| 32 | Home delivery of previous pregnancy | RADIO |  | always | Yes · No |
| 33 | Bad obstetric history | RADIO |  | always | Yes · No |
| 34 | Multiple Pregnancy | RADIO |  | always | Yes · No |
| 35 | Information on Children | HEADLINE |  | always |  |
| 36 | Physical Observation | HEADLINE |  | always |  |
| 37 | Obstetric History | HEADLINE |  | always |  |
| 38 | Assess for High Risk Conditions in the Pregnant Women | HEADLINE |  | always |  |

### Saas Bahu Sammelan — `SaasBahuSamelanDataset.kt` (4 fields)

| # | Field | Type | Req | Shown | Options |
|---|---|---|---|---|---|
| 1 | Saas Bahu Sammelan Date | DATE_PICKER | * | always |  |
| 2 | Place | DROPDOWN |  | always | HWC · Anganwadi Centre · Community center |
| 3 | No. of Participants Attended | EDIT_TEXT | * | always |  |
| 4 | Sammelan Photos / MoM (1) | FILE_UPLOAD |  | conditional |  |

### Survey register — `SurveyRegisterFormDataset.kt` (17 fields)

| # | Field | Type | Req | Shown | Options |
|---|---|---|---|---|---|
| 1 | Name of Facilitator Supervisor | EDIT_TEXT |  | always |  |
| 2 | From | EDIT_TEXT |  | always |  |
| 3 | Till | EDIT_TEXT |  | always |  |
| 4 | Name of the landlord of First House | EDIT_TEXT |  | always |  |
| 5 | Home identification Symbol | EDIT_TEXT |  | always |  |
| 6 | Name of the landlord of Middle House | EDIT_TEXT |  | always |  |
| 7 | Home Identification Symbol | EDIT_TEXT |  | always |  |
| 8 | Name of the landlord of Last House | EDIT_TEXT |  | always |  |
| 9 | Home Identification Symbol | EDIT_TEXT |  | always |  |
| 10 | Household number | EDIT_TEXT |  | always |  |
| 11 | Name of Head of House | EDIT_TEXT |  | always |  |
| 12 | Total members in the family | EDIT_TEXT |  | always |  |
| 13 | Total number of eligible couples in the family | EDIT_TEXT |  | always |  |
| 14 | Total number of pregnant women in the house | EDIT_TEXT |  | always |  |
| 15 | Total number of children (0 – 1 month) | EDIT_TEXT |  | always |  |
| 16 | Total no. of children (1 month – 1 year) | EDIT_TEXT |  | always |  |
| 17 | Total no. of children (2 yr – 5 yr) | EDIT_TEXT |  | always |  |

### Suspected TB — `SuspectedTBDataset.kt` (15 fields)

| # | Field | Type | Req | Shown | Options |
|---|---|---|---|---|---|
| 1 | Date of Visit | DATE_PICKER | * | always |  |
| 2 | Visit | TEXT_VIEW |  | always |  |
| 3 | Type of TB case? | RADIO | * | always | New case of TB · Previously treated TB case · DR-TB case |
| 4 | Reason for suspicion | DROPDOWN | * | always | Treatment failure · TB Relapse/Recurring symptoms · Contact with DR-TB case · Treatment after LFU (Lost to Follow-up) · Other |
| 5 | Does the patient have symptoms? | RADIO | * | conditional | Yes · No |
| 6 | Is the TB Chest X-Ray Test done? | RADIO |  | always | Yes · No |
| 7 | Chest X-Ray Test Result | RADIO |  | conditional | Clinically Diagnosed TB · TB Not suspected |
| 8 | Is Sputum sample collected? | RADIO | * | always | Yes · No |
| 9 | Sputum sample submitted at | DROPDOWN |  | always | HWC · PHC · CHC · District Hospital · Govt. Medical College |
| 10 | Nikshay ID | EDIT_TEXT |  | always |  |
| 11 | Sputum Test Result | RADIO | * | always | Positive · Negative |
| 12 | Is TB confirmed? | RADIO | * | conditional | Yes · No |
| 13 | Has the diagnosis of DR-TB been confirmed? | RADIO | * | conditional | Yes · No |
| 14 | Recommended for TPT? | RADIO | * | conditional | Yes · No |
| 15 | Facility Referral follow-ups | EDIT_TEXT |  | conditional | Yes · No |

### TB confirmed · treatment — `TBConfirmedDataset.kt` (14 fields)

| # | Field | Type | Req | Shown | Options |
|---|---|---|---|---|---|
| 1 | Regimen Type | DROPDOWN | * | always | DS-TB (6 Months) · Shorter Regimen (9–12 Months) · Longer Regimen (18–24 Months) · BPaL Regimen (6 Months) · INH Mono (6 Month) |
| 2 | Treatment Start Date | DATE_PICKER | * | always |  |
| 3 | Expected Treatment Completion Date | TEXT_VIEW |  | always |  |
| 4 | Follow Up Dates | DATE_PICKER | * | always |  |
| 5 | Monthly follow up done | TEXT_VIEW |  | always |  |
| 6 | Adherence to Medicines | RADIO | * | always | Regular · Irregular |
| 7 | Any discomfort | RADIO | * | always | Yes · No |
| 8 | Did the patient complete the full course of Treatment? | RADIO |  | always | Yes · No |
| 9 | Actual Treatment Completion Date | DATE_PICKER |  | always |  |
| 10 | TB Treatment outcomes | DROPDOWN |  | always | Cured · Failed · Lost to Follow-up (LFU) · Death |
| 11 | Date of Death | DATE_PICKER |  | always |  |
| 12 | Place of Death | DROPDOWN |  | always | Home · Subcenter · PHC · CHC · District Hospital · Medical College Hospital · Private Hospital · Other Place |
| 13 | Reason for Death | TEXT_VIEW |  | always |  |
| 14 | Reason for not completing treatment | EDIT_TEXT |  | always |  |

### TB screening — `TBScreeningDataset.kt` (30 fields)

| # | Field | Type | Req | Shown | Options |
|---|---|---|---|---|---|
| 1 | Symptomatic TB Screening | HEADLINE |  | conditional |  |
| 2 | Check if the person has any of these Symptoms: | HEADLINE |  | conditional |  |
| 3 | Date of Visit | DATE_PICKER | * | always |  |
| 4 | Coughing more than 2 weeks | RADIO | * | always | Yes · No |
| 5 | Blood in sputum | RADIO | * | always | Yes · No |
| 6 | Fever for > 2 weeks | RADIO | * | always | Yes · No |
| 7 | Loss of weight | RADIO | * | always | Yes · No |
| 8 | Night Sweats | RADIO | * | always | Yes · No |
| 9 | History of TB | RADIO | * | conditional | Yes · No |
| 10 | Are you currently taking anti-TB drugs | RADIO | * | conditional | Yes · No |
| 11 | Anyone in family currently suffering from TB | RADIO | * | conditional | Yes · No |
| 12 | Rise of fever in evening | RADIO | * | always | Yes · No |
| 13 | Loss of Appetite | RADIO | * | always | Yes · No |
| 14 | History of TB | HEADLINE |  | conditional |  |
| 15 | Beneficiary is Asymptomatic | RADIO |  | conditional | Yes · No |
| 16 | Check if the person has any of these Symptoms: | HEADLINE |  | conditional |  |
| 17 | Age more than 60 years | RADIO | * | conditional | Yes · No |
| 18 | Diabetic | RADIO | * | conditional | Yes · No |
| 19 | Tobacco user | RADIO | * | conditional | Yes · No |
| 20 | BMI < 18.5 | RADIO | * | conditional | Yes · No |
| 21 | Contact with TB patient on treatment | RADIO | * | conditional | Yes · No |
| 22 | Last 5 years history of TB | RADIO | * | conditional | Yes · No |
| 23 | Shortness of breath | RADIO | * | always | Yes · No |
| 24 | Fatigue | RADIO | * | always | Yes · No |
| 25 | Chest pain | RADIO | * | always | Yes · No |
| 26 | Failure to gain weight (in children) | RADIO | * | conditional | Yes · No |
| 27 | Decreased activity or playfulness (in children) | RADIO | * | conditional | Yes · No |
| 28 | Risk Factors | HEADLINE |  | conditional |  |
| 29 | Key Population / Risk Factors | CHECKBOXES | * | conditional |  |
| 30 | HIV Status | DROPDOWN | * | conditional |  |

### U-WIN session — `UWINDataset.kt` (5 fields)

| # | Field | Type | Req | Shown | Options |
|---|---|---|---|---|---|
| 1 | U-WIN Session Date | DATE_PICKER | * | always |  |
| 2 | Place | DROPDOWN | * | always | HWC · School · Anganwadi Centre · Community Center |
| 3 | No. of Participants Attended | EDIT_TEXT | * | always |  |
| 4 | File Upload %1$d | FILE_UPLOAD |  | always |  |
| 5 | File Upload %1$d | FILE_UPLOAD |  | always |  |

### VHSNC meeting — `VHNCDataset.kt` (12 fields)

| # | Field | Type | Req | Shown | Options |
|---|---|---|---|---|---|
| 1 | VHSNC meeting Date | DATE_PICKER | * | always |  |
| 2 | Place of VHSNC meeting | DROPDOWN | * | always | Anganwadi Centre · HWC · School · Community center |
| 3 | Village Name | EDIT_TEXT |  | always |  |
| 4 | No. of Beneficiaries Attended | EDIT_TEXT |  | always |  |
| 5 | Number of ANM/ MPW attended | EDIT_TEXT |  | always |  |
| 6 | Number of AWW attended | EDIT_TEXT |  | always |  |
| 7 | Number of Pregnant Woman | EDIT_TEXT |  | always |  |
| 8 | Number of Lactating Mothers | EDIT_TEXT |  | always |  |
| 9 | Number of Committee members present | EDIT_TEXT |  | always |  |
| 10 | Follow-up the previous meeting minutes | RADIO |  | always | Yes · No |
| 11 | Upload Image | FILE_UPLOAD |  | always |  |
| 12 | Upload Image | FILE_UPLOAD |  | always |  |

### VHND meeting — `VHNDDataset.kt` (15 fields)

| # | Field | Type | Req | Shown | Options |
|---|---|---|---|---|---|
| 1 | VHND Date | DATE_PICKER | * | always |  |
| 2 | Place of VHND session | DROPDOWN | * | always | Anganwadi Centre · HWC · School · Community center |
| 3 | Total No. of Beneficiaries Attended | EDIT_TEXT | * | always |  |
| 4 | Number of Pregnant Woman attended for ANC check up | EDIT_TEXT |  | always |  |
| 5 | No. of Lactating Mothers attended for PNC check up | EDIT_TEXT |  | always |  |
| 6 | Number of Children attended for Immunization | EDIT_TEXT |  | always |  |
| 7 | Health and Nutrition Education (Focus for Pregnant Women, Lactating Mothers, Adolescent girls) | HEADLINE |  | always |  |
| 8 | Knowledge of balanced diet | CHECKBOXES |  | always | What to eat, what to avoid, importance of dietary diversity. |
| 9 | Care during pregnancy and delivery | CHECKBOXES |  | always | Prevention of Anemia, weakness, importance of rest and nutrition. |
| 10 | Importance of breastfeeding | CHECKBOXES |  | always | Early initiation after birth, exclusive breastfeeding for 6 months. |
| 11 | Complementary feeding | CHECKBOXES |  | always | Starting home-based nutritious food after 6 months along with breastfeeding |
| 12 | Hygiene and sanitation | CHECKBOXES |  | always | Maintaining cleanliness while preparing and feeding food |
| 13 | Family planning and health care | CHECKBOXES |  | always | Importance of small family and regular health check-ups |
| 14 | Upload Image | FILE_UPLOAD |  | always |  |
| 15 | Upload Image | FILE_UPLOAD |  | always |  |
