# FLW Mobile App

## Overview

<ul>
<li>The FLW Mobile App is designed for Healthcare programs and consultation services render by ASHAs’ to serve pregnant women, mothers and newborns in India. This
ultimately eliminates pen and paper work by ASHAs and carry out entering beneficiaries’ data in a digitalized
process with increased ease and accuracy of data. We named it as SAKHI for Bihar State and Utprerona for
Assam State.</li>

<li>This application provides below functional modules.</li>
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
</ul>

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

1. Clone the repository to your local machine using `git clone https://github.com/PSMRI/FLW-Mobile-App`.
2. Open Android Studio.
3. Click on 'Open an existing Android Studio project'.
4. Navigate to the directory where you cloned the project and select the root folder.
5. Wait for Android Studio to sync the project and download the dependencies.
6. Once the sync is done, you can run the project on an emulator or a physical device.

### Prerequisites
<ul>
<li>**Secrets:**
    - set ENCRYPTED_PASS_KEY, ABHA_CLIENT_ID, ABHA_CLIENT_SECRET values in secrets.properties file.</li>
<li>**google-json**
    - set Google JSON file of required variant ie. UAT, STAGING or PRODUCTION.
</li>**Environments**:
    -We can generate the application for below environments based on requirements
        - Amrit Demo
        - UAT
        - Production
</ul>

**Configurations:**

    - In AppModule class there is a variable declared for base URL, set the value to amrit demo or uat or prod values based on requirement

        `object AppModule {

            private const val baseTmcUrl = "<add base url>"
        `
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
