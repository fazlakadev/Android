package com.fazlaka.app.core.common

import com.fazlaka.app.core.network.ApiResult

/**
 * Resolves backend i18n message keys (e.g. "auth.emailInvalid") to a human
 * readable Arabic string. Falls back to the raw key/message when unknown.
 */
object ErrorMessages {

    private val arabic = mapOf(
        "auth.emailInvalid" to "البريد الإلكتروني غير صحيح",
        "auth.passwordTooShort" to "كلمة المرور قصيرة جدًا (8 أحرف على الأقل)",
        "auth.passwordStrength" to "كلمة المرور يجب أن تحتوي على أحرف وأرقام",
        "auth.usernameInvalid" to "اسم المستخدم غير صالح (أحرف وأرقام ونقاط فقط)",
        "auth.emailExists" to "هذا البريد الإلكتروني مسجل بالفعل",
        "auth.usernameTaken" to "اسم المستخدم محجوز بالفعل",
        "auth.invalidCredentials" to "بيانات الدخول غير صحيحة",
        "auth.invalidToken" to "الرابط غير صالح أو منتهي الصلاحية",
        "auth.tokenMissing" to "انتهت الجلسة، سجّل الدخول مجددًا",
        "auth.accountLocked" to "الحساب مقفل مؤقتًا، حاول لاحقًا",
        "auth.otpInvalid" to "رمز التحقق غير صحيح",
        "auth.otpExpired" to "رمز التحقق منتهي الصلاحية",
        "auth.twoFactorRequired" to "يلزم التحقق بخطوتين",
        "auth.emailNotVerified" to "يرجى تفعيل البريد الإلكتروني أولًا",
        "auth.accountSuspended" to "الحساب موقوف مؤقتًا",
        "auth.accountBanned" to "الحساب محظور",
        "auth.wrongPassword" to "كلمة المرور الحالية غير صحيحة",
        "auth.newPasswordSame" to "كلمة المرور الجديدة مطابقة للحالية",
        "auth.tooManyAttempts" to "محاولات كثيرة، حاول لاحقًا",
        "errors.phoneInvalid" to "رقم الهاتف غير صحيح",
        "errors.invalidProvider" to "مزود غير معروف",
        "errors.notFound" to "غير موجود",
        "errors.forbidden" to "ليس لديك صلاحية لهذا الإجراء",
        "common.loggedOut" to "تم تسجيل الخروج",
        "common.passwordResetEmailSent" to "تم إرسال رابط استعادة كلمة المرور إلى بريدك",
        "network.error" to "تعذّر الاتصال بالخادم، تحقق من الإنترنت",
        "network.timeout" to "انتهت مهلة الاتصال، حاول مجددًا",
        "network.parseError" to "حدث خطأ في معالجة البيانات",
    )

    fun resolve(key: String?, fallback: String? = null): String {
        if (key.isNullOrEmpty()) return fallback ?: "حدث خطأ ما"
        return arabic[key] ?: fallback ?: key
    }

    fun ApiResult.Failure.localized(): String = resolve(message, "حدث خطأ ما")
}
