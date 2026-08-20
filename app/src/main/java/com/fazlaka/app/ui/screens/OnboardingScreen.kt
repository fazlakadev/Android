package com.fazlaka.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.fazlaka.app.ui.components.AuthBackground
import com.fazlaka.app.ui.components.AuthButton
import com.fazlaka.app.ui.components.BrandLogo
import com.fazlaka.app.ui.components.OrDivider
import com.fazlaka.app.ui.components.SocialAuthButton
import com.fazlaka.app.ui.components.SocialProvider
import com.fazlaka.app.ui.navigation.Routes

@Composable
fun OnboardingScreen(
    onNavigate: (String) -> Unit,
    onDone: () -> Unit,
) {
    AuthBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.weight(1f))
            BrandLogo(size = 40.dp)
            Spacer(Modifier.height(16.dp))
            Text(
                text = stringResource(com.fazlaka.app.R.string.onboarding_subtitle),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = Color.White.copy(alpha = 0.72f),
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
            )
            Spacer(Modifier.weight(1f))
            SocialAuthButton(
                provider = SocialProvider.Google,
                onClick = { onNavigate(Routes.LOGIN) },
            )
            Spacer(Modifier.height(10.dp))
            SocialAuthButton(
                provider = SocialProvider.GitHub,
                onClick = { onNavigate(Routes.oauth("github")) },
            )
            Spacer(Modifier.height(10.dp))
            SocialAuthButton(
                provider = SocialProvider.Facebook,
                onClick = { onNavigate(Routes.oauth("facebook")) },
            )
            Spacer(Modifier.height(20.dp))
            OrDivider()
            Spacer(Modifier.height(20.dp))
            AuthButton(
                text = stringResource(com.fazlaka.app.R.string.onboarding_login),
                onClick = { onNavigate(Routes.LOGIN) },
            )
            Spacer(Modifier.height(12.dp))
            OutlinedButton(
                onClick = { onNavigate(Routes.REGISTER) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = MaterialTheme.shapes.medium,
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.35f)),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color.Transparent,
                    contentColor = Color.White,
                ),
            ) {
                Text(
                    text = stringResource(com.fazlaka.app.R.string.onboarding_register),
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium,
                )
            }
            Spacer(Modifier.height(10.dp))
            Text(
                text = stringResource(com.fazlaka.app.R.string.onboarding_phone),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = Color.White.copy(alpha = 0.85f),
                modifier = Modifier
                    .padding(8.dp)
                    .clickable(onClick = { onNavigate(Routes.PHONE_AUTH) }),
            )
            Spacer(Modifier.height(10.dp))
            Text(
                text = stringResource(com.fazlaka.app.R.string.onboarding_guest),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .padding(8.dp)
                    .clickable(onClick = onDone),
            )
            Spacer(Modifier.weight(0.6f))
        }
    }
}
