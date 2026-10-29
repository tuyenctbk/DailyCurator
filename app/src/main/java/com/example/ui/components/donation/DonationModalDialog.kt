package com.example.ui.components.donation

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.R
import com.example.ui.theme.GalleryBlack
import com.example.ui.theme.GalleryDark
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldMuted
import com.example.ui.theme.OchreDark
import com.example.ui.theme.ParchmentMuted
import com.example.ui.theme.ParchmentWhite
import com.example.ui.theme.SurfaceContainerDark
import com.example.ui.theme.SurfaceContainerHighDark

data class SupporterTier(
    val titleRes: Int,
    val price: String,
    val descRes: Int
)

@Composable
fun DonationModalDialog(
    isSupporter: Boolean,
    onDismiss: () -> Unit,
    onSimulatePlayPurchase: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) }

    val paypalLink = "https://www.paypal.me/dailycurator"
    val kofiLink = "https://ko-fi.com/dailycurator"

    val tiers = remember {
        listOf(
            SupporterTier(R.string.tier_1_title, "$1.99", R.string.tier_1_desc),
            SupporterTier(R.string.tier_2_title, "$4.99", R.string.tier_2_desc),
            SupporterTier(R.string.tier_3_title, "$9.99", R.string.tier_3_desc),
            SupporterTier(R.string.tier_4_title, "$19.99", R.string.tier_4_desc)
        )
    }

    val copyToClipboard: (String) -> Unit = { text ->
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(context.getString(R.string.donation_title), text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, context.getString(R.string.link_copied_toast), Toast.LENGTH_SHORT).show()
    }

    val openUrl: (String) -> Unit = { url ->
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            context.startActivity(intent)
        } catch (e: Exception) {
            copyToClipboard(url)
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = modifier
                .fillMaxWidth(0.92f)
                .clip(RoundedCornerShape(24.dp))
                .border(1.dp, OchreDark, RoundedCornerShape(24.dp)),
            colors = CardDefaults.cardColors(containerColor = GalleryDark)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(SurfaceContainerHighDark, shape = CircleShape)
                                .border(1.dp, GoldAccent, shape = CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.VolunteerActivism,
                                contentDescription = null,
                                tint = GoldAccent,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = stringResource(R.string.donation_title),
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontFamily = FontFamily.Serif,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = ParchmentWhite
                            )
                            Text(
                                text = stringResource(R.string.donation_subtitle),
                                style = MaterialTheme.typography.bodySmall,
                                color = ParchmentMuted
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = stringResource(R.string.close_preview),
                            tint = ParchmentMuted
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (isSupporter) {
                    // Supporter Recognition Card
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, GoldAccent, RoundedCornerShape(16.dp)),
                        colors = CardDefaults.cardColors(containerColor = SurfaceContainerDark),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = GoldAccent,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = stringResource(R.string.thank_you_title),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontFamily = FontFamily.Serif,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = GoldAccent,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = stringResource(R.string.thank_you_body),
                                style = MaterialTheme.typography.bodyMedium,
                                color = ParchmentMuted,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    // Navigation Tabs
                    TabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = SurfaceContainerDark,
                        contentColor = GoldAccent,
                        indicator = { tabPositions ->
                            TabRowDefaults.SecondaryIndicator(
                                Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                                color = GoldAccent
                            )
                        },
                        modifier = Modifier.clip(RoundedCornerShape(12.dp))
                    ) {
                        Tab(
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            text = {
                                Text(
                                    text = stringResource(R.string.google_pay_tab),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (selectedTab == 0) GoldAccent else ParchmentMuted
                                )
                            }
                        )
                        Tab(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            text = {
                                Text(
                                    text = stringResource(R.string.paypal_tab),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (selectedTab == 1) GoldAccent else ParchmentMuted
                                )
                            }
                        )
                        Tab(
                            selected = selectedTab == 2,
                            onClick = { selectedTab = 2 },
                            text = {
                                Text(
                                    text = stringResource(R.string.kofi_tab),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (selectedTab == 2) GoldAccent else ParchmentMuted
                                )
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    when (selectedTab) {
                        0 -> {
                            // Method 1: Google Play In-App Purchase Tiers
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                tiers.forEach { tier ->
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                onSimulatePlayPurchase()
                                                Toast.makeText(
                                                    context,
                                                    context.getString(R.string.thank_you_toast),
                                                    Toast.LENGTH_LONG
                                                ).show()
                                            }
                                            .border(1.dp, OchreDark.copy(alpha = 0.4f), RoundedCornerShape(12.dp)),
                                        colors = CardDefaults.cardColors(containerColor = SurfaceContainerDark),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(14.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = stringResource(tier.titleRes),
                                                    style = MaterialTheme.typography.titleSmall.copy(
                                                        fontFamily = FontFamily.Serif,
                                                        fontWeight = FontWeight.Bold
                                                    ),
                                                    color = ParchmentWhite
                                                )
                                                Text(
                                                    text = stringResource(tier.descRes),
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = ParchmentMuted
                                                )
                                            }

                                            Button(
                                                onClick = {
                                                    onSimulatePlayPurchase()
                                                    Toast.makeText(
                                                        context,
                                                        context.getString(R.string.thank_you_toast),
                                                        Toast.LENGTH_LONG
                                                    ).show()
                                                },
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = GoldAccent,
                                                    contentColor = GalleryBlack
                                                ),
                                                shape = RoundedCornerShape(8.dp)
                                            ) {
                                                Text(
                                                    text = tier.price,
                                                    style = MaterialTheme.typography.labelLarge.copy(
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        1 -> {
                            // Method 2: PayPal & Live QR Code
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                PayPalQRCodeView(
                                    qrContent = paypalLink,
                                    sizeDp = 170.dp
                                )

                                Spacer(modifier = Modifier.height(14.dp))

                                Text(
                                    text = stringResource(R.string.paypal_instructions),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = ParchmentMuted,
                                    textAlign = TextAlign.Center
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = { copyToClipboard(paypalLink) },
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ContentCopy,
                                            contentDescription = null,
                                            tint = GoldAccent,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = stringResource(R.string.copy_paypal_link),
                                            style = MaterialTheme.typography.labelMedium,
                                            color = GoldAccent
                                        )
                                    }

                                    Button(
                                        onClick = { openUrl(paypalLink) },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = GoldAccent,
                                            contentColor = GalleryBlack
                                        ),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.OpenInNew,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = stringResource(R.string.paypal_me),
                                            style = MaterialTheme.typography.labelMedium.copy(
                                                fontWeight = FontWeight.Bold
                                            )
                                        )
                                    }
                                }
                            }
                        }

                        2 -> {
                            // Method 3: Ko-fi Tip Jar
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(1.dp, OchreDark.copy(alpha = 0.5f), RoundedCornerShape(16.dp)),
                                    colors = CardDefaults.cardColors(containerColor = SurfaceContainerDark),
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(20.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CardGiftcard,
                                            contentDescription = null,
                                            tint = GoldAccent,
                                            modifier = Modifier.size(40.dp)
                                        )
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Text(
                                            text = stringResource(R.string.kofi_title),
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontFamily = FontFamily.Serif,
                                                fontWeight = FontWeight.Bold
                                            ),
                                            color = ParchmentWhite
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = stringResource(R.string.kofi_description),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = ParchmentMuted,
                                            textAlign = TextAlign.Center
                                        )
                                        Spacer(modifier = Modifier.height(16.dp))

                                        Button(
                                            onClick = { openUrl(kofiLink) },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = GoldAccent,
                                                contentColor = GalleryBlack
                                            ),
                                            shape = RoundedCornerShape(12.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.OpenInNew,
                                                contentDescription = null,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = stringResource(R.string.open_kofi),
                                                style = MaterialTheme.typography.labelLarge.copy(
                                                    fontWeight = FontWeight.Bold
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
