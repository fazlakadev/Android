package com.fazlaka.app.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.fazlaka.app.ui.components.GlassCard
import com.fazlaka.app.ui.components.HeroAccents
import com.fazlaka.app.ui.components.HeroSection

private data class LegalSection(val title: String, val points: List<String>)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyPolicyScreen(onBack: () -> Unit) {
    LegalScreen(
        title = stringResource(com.fazlaka.app.R.string.more_privacy),
        subtitle = "نتعامل مع بياناتك بشفافية واحترام كامل",
        badge = "🛡️ خصوصيتك أولاً",
        onBack = onBack,
        sections = listOf(
            LegalSection(
                "ما نجمعه ولماذا",
                listOf(
                    "بيانات الحساب: الاسم واسم المستخدم والبريد الإلكتروني لإنشاء وإدارة حسابك.",
                    "بيانات الجهاز: نوع الجهاز ونظام التشغيل وإصدار التطبيق لتأمين جلساتك وعرض التنبيهات الأمنية.",
                    "الموقع التقريبي: يُستخدم فقط في تنبيهات تسجيل الدخول لحماية حسابك، ويمكنك رفض صلاحية الموقع في أي وقت.",
                    "سجل النشاط: عمليات الدخول والرسائل والرفعات لتوثيق الأمان ومساعدتك في مراجعة حسابك.",
                ),
            ),
            LegalSection(
                "كيف نحمي بياناتك",
                listOf(
                    "كلمات المرور تُخزَّن مشفّرة بالكامل ولا يمكن لأي أحد — بما فيهم نحن — قراءتها.",
                    "رموز الجلسات تُجزَّل (hash) في قاعدة البيانات وتُدار بنظام تدوير وإلغاء فوري.",
                    "التحقق بخطوتين (TOTP وOTP) متاح لتأمين إضافي لحسابك.",
                    "تصلك رسالة فورية عند كل تسجيل دخول جديدة تتضمن الجهاز والموقع والوقت.",
                ),
            ),
            LegalSection(
                "ما لا نفعله أبداً",
                listOf(
                    "لا نبيع بياناتك لأي طرف ثالث.",
                    "لا نشارك بريدك إلا مع خدمات التشغيل الأساسية (قاعدة البيانات والبريد والتخزين).",
                    "لا نستخدم الموقع الجغرافي للإعلانات أو التتبع الدائم.",
                ),
            ),
            LegalSection(
                "حقوقك",
                listOf(
                    "يمكنك تصدير كل بياناتك في أي وقت (تصدير البيانات).",
                    "يمكنك تعطيل حسابك وحذف بياناتك نهائياً من الإعدادات.",
                    "يمكنك سحب أي صلاحية (الموقع، الإشعارات) من إعدادات النظام.",
                ),
            ),
            LegalSection(
                "التواصل",
                listOf(
                    "لأي استفسار حول الخصوصية تواصل معنا من صفحة الدعم الفني داخل التطبيق.",
                ),
            ),
        ),
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TermsScreen(onBack: () -> Unit) {
    LegalScreen(
        title = stringResource(com.fazlaka.app.R.string.more_terms),
        subtitle = "القواعد البسيطة التي تحافظ على تجربة آمنة للجميع",
        badge = "📜 آخر تحديث 2026",
        onBack = onBack,
        sections = listOf(
            LegalSection(
                "الحساب",
                listOf(
                    "أنت مسؤول عن الحفاظ على سرية كلمة مرورك وتفعيل التحقق بخطوتين عند الإمكان.",
                    "يجب أن تكون بياناتك الحقيقية وصحيحة، ويُمنع انتحال هوية الآخرين.",
                    "الحساب الواحد لكل شخص، ويُمنع إنشاء حسابات وهمية أو مؤقتة.",
                ),
            ),
            LegalSection(
                "المحتوى والاستخدام",
                listOf(
                    "المحتوى المعروض مخصص للاستخدام الشخصي فقط دون إعادة نشر أو بيع.",
                    "يُمنع إساءة استخدام خاصية الرسائل (إزعاج، سبام، محتوى غير قانوني).",
                    "يُمنع محاولة اختراق النظام أو تجاوز حدود الاستخدام الآلي (API).",
                ),
            ),
            LegalSection(
                "المجتمع",
                listOf(
                    "احترم جميع المستخدمين — التشهير أو التنمر يؤدي لتعليق الحساب.",
                    "الإبلاغ عن أي محتوى مخالف متاح من زر الإبلاغ في كل محتوى.",
                ),
            ),
            LegalSection(
                "تعديلات الشروط",
                listOf(
                    "قد نحدّث هذه الشروط، وسيصلك إشعار داخل التطبيق عند أي تغيير جوهري.",
                    "استمرارك في استخدام التطبيق بعد التحديث يعني موافقتك على النسخة الجديدة.",
                ),
            ),
        ),
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LegalScreen(
    title: String,
    subtitle: String,
    badge: String,
    onBack: () -> Unit,
    sections: List<LegalSection>,
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(title, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                ),
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
        ) {
            HeroSection(
                title = title,
                subtitle = subtitle,
                badge = badge,
                accent = HeroAccents.Settings,
                minHeight = 120.dp,
            )
            Spacer(Modifier.height(16.dp))
            sections.forEach { section ->
                GlassCard {
                    Column(Modifier.padding(16.dp)) {
                        Text(
                            text = section.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                        )
                        Spacer(Modifier.height(8.dp))
                        section.points.forEach { point ->
                            Row(
                                modifier = Modifier.padding(vertical = 5.dp),
                                verticalAlignment = Alignment.Top,
                            ) {
                                Text(
                                    text = "•",
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(start = 4.dp, end = 8.dp),
                                )
                                Text(
                                    text = point,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.fillMaxWidth(),
                                )
                            }
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
            }
            Spacer(Modifier.height(28.dp))
        }
    }
}
