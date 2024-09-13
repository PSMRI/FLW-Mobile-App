# **Generating APK:**

    - Information for the steps followed during the generation of APK’s for Sakhi Application from code

**Pre-Requesties:**
    - Clone the code of updated repo into android studio and build the application.
**Secrets:**
    - set ENCRYPTED_PASS_KEY, ABHA_CLIENT_ID, ABHA_CLIENT_SECRET values in secrets.properties file.
**google-json**
    - set google json file of required variant ie. uat, staging or production.
**Environments**:  We can generate the application for below environments based on requirements
    - Amrit Demo
    - UAT
    - Production

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

 

 

 

 

 
