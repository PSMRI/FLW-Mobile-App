package org.piramalswasthya.sakhi.notifications

import org.piramalswasthya.sakhi.model.NotifTemplateCache

/**
 * Bundled fallback template library (Notification LLD §7 offline resilience):
 * works from day one, before any sync. Server templates (libraryVersion ≥ 1)
 * replace these wholesale when they arrive.
 *
 * Buckets: ZERO = gratitude / quiet day, ONE = active day, TWO_PLUS = busy day.
 * Languages: en, hi, as, bn. Translations pending native-speaker review.
 */
object BundledTemplates {

    private fun t(id: String, bucket: String, lang: String, body: String) =
        NotifTemplateCache(id, bucket, lang, body, libraryVersion = 0)

    val ALL: List<NotifTemplateCache> = listOf(
        // ─── ZERO / quiet day — gratitude, never framed as failure ───
        t("zero_en_1", "ZERO", "en", "Good evening, {name}. Thank you for your continued service to your community. Wishing you a peaceful evening."),
        t("zero_en_2", "ZERO", "en", "Good evening, {name}. Your village is stronger because of you. Rest well tonight."),
        t("zero_en_3", "ZERO", "en", "Good evening, {name}. Every day of your service matters. Have a restful evening."),
        t("zero_hi_1", "ZERO", "hi", "शुभ संध्या, {name} जी। समुदाय की निरंतर सेवा के लिए धन्यवाद। आपकी शाम मंगलमय हो।"),
        t("zero_hi_2", "ZERO", "hi", "शुभ संध्या, {name} जी। आपके गाँव की ताक़त आप हैं। आज आराम कीजिए।"),
        t("zero_hi_3", "ZERO", "hi", "शुभ संध्या, {name} जी। आपकी सेवा का हर दिन मायने रखता है। शुभ रात्रि।"),
        t("zero_as_1", "ZERO", "as", "শুভ সন্ধিয়া, {name}। সমাজলৈ আপোনাৰ নিৰন্তৰ সেৱাৰ বাবে ধন্যবাদ। আপোনাৰ সন্ধিয়া শুভ হওক।"),
        t("zero_as_2", "ZERO", "as", "শুভ সন্ধিয়া, {name}। আপোনাৰ বাবেই আপোনাৰ গাঁও শক্তিশালী। আজি জিৰণি লওক।"),
        t("zero_as_3", "ZERO", "as", "শুভ সন্ধিয়া, {name}। আপোনাৰ সেৱাৰ প্ৰতিটো দিন মূল্যৱান। শুভ ৰাত্ৰি।"),
        t("zero_bn_1", "ZERO", "bn", "শুভ সন্ধ্যা, {name}। সমাজের প্রতি আপনার নিরন্তর সেবার জন্য ধন্যবাদ। আপনার সন্ধ্যা শুভ হোক।"),
        t("zero_bn_2", "ZERO", "bn", "শুভ সন্ধ্যা, {name}। আপনার জন্যই আপনার গ্রাম শক্তিশালী। আজ বিশ্রাম নিন।"),
        t("zero_bn_3", "ZERO", "bn", "শুভ সন্ধ্যা, {name}। আপনার সেবার প্রতিটি দিন মূল্যবান। শুভ রাত্রি।"),

        // ─── ONE / active day — affirmation ───
        t("one_en_1", "ONE", "en", "Good evening, {name}. You made a positive difference in your community today. Thank you for your dedication."),
        t("one_en_2", "ONE", "en", "Good evening, {name}. A family is safer tonight because of your visit today. Thank you."),
        t("one_en_3", "ONE", "en", "Good evening, {name}. Your work today touched a life. Thank you for showing up."),
        t("one_hi_1", "ONE", "hi", "शुभ संध्या, {name} जी। आज आपने अपने समुदाय में सकारात्मक बदलाव लाया। आपके समर्पण के लिए धन्यवाद।"),
        t("one_hi_2", "ONE", "hi", "शुभ संध्या, {name} जी। आज आपकी सेवा से एक परिवार अधिक सुरक्षित है। धन्यवाद।"),
        t("one_hi_3", "ONE", "hi", "शुभ संध्या, {name} जी। आज आपके काम ने किसी की ज़िंदगी को छुआ। धन्यवाद।"),
        t("one_as_1", "ONE", "as", "শুভ সন্ধিয়া, {name}। আজি আপুনি আপোনাৰ সমাজত ইতিবাচক পৰিৱৰ্তন আনিলে। আপোনাৰ নিষ্ঠাৰ বাবে ধন্যবাদ।"),
        t("one_as_2", "ONE", "as", "শুভ সন্ধিয়া, {name}। আপোনাৰ আজিৰ সেৱাৰ বাবে এটা পৰিয়াল আজি অধিক সুৰক্ষিত। ধন্যবাদ।"),
        t("one_as_3", "ONE", "as", "শুভ সন্ধিয়া, {name}। আজি আপোনাৰ কামে এটা জীৱন স্পৰ্শ কৰিলে। ধন্যবাদ।"),
        t("one_bn_1", "ONE", "bn", "শুভ সন্ধ্যা, {name}। আজ আপনি আপনার সমাজে ইতিবাচক পরিবর্তন এনেছেন। আপনার নিষ্ঠার জন্য ধন্যবাদ।"),
        t("one_bn_2", "ONE", "bn", "শুভ সন্ধ্যা, {name}। আপনার আজকের সেবার জন্য একটি পরিবার আজ বেশি সুরক্ষিত। ধন্যবাদ।"),
        t("one_bn_3", "ONE", "bn", "শুভ সন্ধ্যা, {name}। আজ আপনার কাজ একটি জীবন স্পর্শ করেছে। ধন্যবাদ।"),

        // ─── TWO_PLUS / busy day — celebration ───
        t("two_en_1", "TWO_PLUS", "en", "Good evening, {name}. You had a busy day supporting families today. Thank you for your commitment."),
        t("two_en_2", "TWO_PLUS", "en", "Good evening, {name}. Many families felt your care today. Your community is lucky to have you."),
        t("two_en_3", "TWO_PLUS", "en", "Good evening, {name}. What a day of service! Rest well — you have earned it."),
        t("two_hi_1", "TWO_PLUS", "hi", "शुभ संध्या, {name} जी। आज आपने कई परिवारों का सहारा बनकर व्यस्त दिन बिताया। आपकी प्रतिबद्धता के लिए धन्यवाद।"),
        t("two_hi_2", "TWO_PLUS", "hi", "शुभ संध्या, {name} जी। आज कई परिवारों ने आपकी देखभाल महसूस की। आपका समुदाय भाग्यशाली है।"),
        t("two_hi_3", "TWO_PLUS", "hi", "शुभ संध्या, {name} जी। सेवा से भरा कैसा दिन! आराम कीजिए — आप इसके हक़दार हैं।"),
        t("two_as_1", "TWO_PLUS", "as", "শুভ সন্ধিয়া, {name}। আজি আপুনি বহু পৰিয়ালক সহায় কৰি ব্যস্ত দিন কটালে। আপোনাৰ দায়বদ্ধতাৰ বাবে ধন্যবাদ।"),
        t("two_as_2", "TWO_PLUS", "as", "শুভ সন্ধিয়া, {name}। আজি বহু পৰিয়ালে আপোনাৰ যত্ন অনুভৱ কৰিলে। আপোনাৰ সমাজ ভাগ্যৱান।"),
        t("two_as_3", "TWO_PLUS", "as", "শুভ সন্ধিয়া, {name}। সেৱাৰে ভৰা এটা দিন! জিৰণি লওক — আপুনি ইয়াৰ যোগ্য।"),
        t("two_bn_1", "TWO_PLUS", "bn", "শুভ সন্ধ্যা, {name}। আজ আপনি বহু পরিবারকে সাহায্য করে ব্যস্ত দিন কাটিয়েছেন। আপনার দায়বদ্ধতার জন্য ধন্যবাদ।"),
        t("two_bn_2", "TWO_PLUS", "bn", "শুভ সন্ধ্যা, {name}। আজ বহু পরিবার আপনার যত্ন অনুভব করেছে। আপনার সমাজ ভাগ্যবান।"),
        t("two_bn_3", "TWO_PLUS", "bn", "শুভ সন্ধ্যা, {name}। সেবায় ভরা একটি দিন! বিশ্রাম নিন — আপনি এর যোগ্য।")
    )
}
