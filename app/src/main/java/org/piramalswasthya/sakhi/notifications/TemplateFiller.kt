package org.piramalswasthya.sakhi.notifications

import javax.inject.Inject
import javax.inject.Singleton

/** Runtime personalization (Notification LLD §6): fills {slot} placeholders. */
@Singleton
class TemplateFiller @Inject constructor() {

    fun fill(template: String, name: String?): String =
        template.replace("{name}", name?.trim().takeUnless { it.isNullOrEmpty() } ?: "ASHA")
}
