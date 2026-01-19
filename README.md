# FLW Mobile App

[![DeepWiki](https://img.shields.io/badge/DeepWiki-PSMRI%2FFLW--Mobile--App-blue.svg?...)](https://deepwiki.com/PSMRI/FLW-Mobile-App)

## Overview

The FLW Mobile App is designed for healthcare programs and consultation services rendered by ASHAs’ to serve pregnant women, mothers, and newborns in India. This eliminates pen and paperwork by ASHAs and allows them to enter beneficiaries data in a digital
process with increased ease and accuracy of data. We named it as SAKHI for Bihar State and Utprerona for
Assam State.


## Functional Modules


1. Household List

2. New Household Registration

3. All Beneficiaries List

4. Beneficiary Registration

5. Eligible Couple List

6. Mother Care

    • Pregnancy List (PMSMA Form: Pradhan Mantri Surakshit Matritva Abhiyan)

    • Delivery stage List

    • PNC Mother List

    • Reproductive Age List

7. Child Care

    • Infant List

    • Child List

    • Adolescent List

8. NCD

    • NCD List

    • NCD Eligible List

    • NCD Priority List

    • NCD Non-Eligible List

9. Immunization due List

10. HRP Cases

11. General OP Care List

12. Menopause Stage List

13. Death Reports

14. Village Level Forms

15. ASHA Dashboard

    • Total ANC Women

    • Delivery due list

    • Total Delivery Women

    • Total PNC Women

16. ASHA To-do List (Scheduler)


## Features

- **User Authentication**: Secure login.
- **Real-time Data**: Access to up-to-date information about beneficiaries.
- **User-Friendly Interface**: Intuitive design for easy navigation.
- **Offline Access**: Ability to use the app without an internet connection.
- **Multilingual Support**: Ability to use app in different languages like English, Hindi, Assamese.

## Technologies & Tools Used

- **IDE**: Android Studio.
- **Database**: Room
- **Languages**: XML, Kotlin, SQL
- **Architecture & Architectural Components**: MVVM, Android Architectural Components
- **SDK**: Android SDK 23-34

## Installation

Make sure you have the following installed:

- [Android Studio](https://developer.android.com/studio)

To run this project, Follow these steps:

1. Clone the repository to your local machine,
   using: `git clone https://github.com/PSMRI/FLW-Mobile-App`.
2. Open Android Studio.
3. Click on 'Open an existing Android Studio project'.
4. Navigate to the directory where you cloned the project and select the root folder.
5. Wait for Android Studio to sync the project and download the dependencies.
6. Once the sync is done, select build variant you want to work on like uatDebug, statingDebug or productionDebug
7. create folder in \app\src named production, uat or staging as per build variant you want to work and add google JSON file in it.
7. Clean Project and Rebuild and run project 
8. you can run the project on an emulator or a physical device.
9. Try to login with valid Credentials if everything is fine you able to login successfully

### Prerequisites

- **Secrets**: Set the following secrets in your GitHub repository:
    - `ENCODED_AES_KEY`
    - `ENCODED_AES_IV`
    - `GOOGLE_SERVICES_JSON_NIRAMAY_PRODUCTION`
    - `GOOGLE_SERVICES_JSON_XUSHRUKHA_PRODUCTION`
    - `GOOGLE_SERVICES_JSON_GENERIC`
    - `FIREBASE_CREDENTIALS_JSON_SAKSHAM_ASSAM`
    - `FIREBASE_CREDENTIALS_JSON_UTPRERONA_NIRAMAY`
    - `FIREBASE_CREDENTIALS_JSON_UTPRERONA_XUSHRUKHA`
    - `GOOGLE_PLAY_JSON_KEY`
    - `KEYSTORE_FILE`
    - `KEYSTORE_PASSWORD`
    - `KEY_ALIAS`
    - `KEY_PASSWORD`
    - `FIREBASE_APP_ID`
    - `BASE_TMC_URL`
    - `ABHA_CLIENT_ID`
    - `ABHA_CLIENT_SECRET`
    - `BASE_ABHA_URL`
    - `ABHA_TOKEN_URL`
    - `ABHA_AUTH_URL`
    - `CHAT_URL`

- **Environment Variables**:
    - `environment` (e.g., `NIRAMAY_PRODUCTION`, `XUSHRUKHA_PRODUCTION`, or other environments)

- **Build Configuration**:
    - `variant` (e.g., `Saksham`, `Niramay`, `Xushrukha`)
    - `build_type` (e.g., `debug`, `release`)

- **Files**:
    - `google-services.json` for various environments
    - `firebase_credentials.json` for different variants
    - `google_play_service_account.json` for release builds
    - `keystore.jks`




**Configurations:**

    - In App level build.gradle file productFlavors added. Based on which varient you want to work select build varient 

    - In IconDataset class we have the list of modules that we show on UI, we can comment, or un-comment modules based on requirement

    - In AllBenFragment class we have a Boolean `showAbha` which can be used to toggle the visibility of abha button on beneficiary cards

**APK Variants:** We can generate signed and un-signed apk’s for the application

    - For amritdemo/UAT we generate unsigned apk’s

        + In Android Studio menu we have `Build` section 
            -> under this we have `Build Bundle(s) or APK(s)` 
                -> click on `Build APK(s)`

        + Once the APK is generated android studio will notify with the location of the APK,
            like ../app/build/outputs/apk/debug

    - For Production signed APK is generated

        + In Android Studio menu we have `Build` section 
            -> under this we have `Generate Signed Bundle/APK`

        + We can create a signing key or use existing key, once you enter the key details Signed APK is generated

        + https://developer.android.com/studio/publish/app-signing

        + Once the APK is generated android studio will notify with the location of APK, 
             generally in release folder 

## Filing Issues

If you encounter any issues, bugs, or have feature requests, please file them in the [main AMRIT repository](https://github.com/PSMRI/AMRIT/issues). Centralizing all feedback helps us streamline improvements and address concerns efficiently.  

## Join Our Community

We’d love to have you join our community discussions and get real-time support!  
Join our [Discord server](https://discord.gg/FVQWsf5ENS) to connect with contributors, ask questions, and stay updated.  
