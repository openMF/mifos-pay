/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.send.money

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kpt.core.base.designsystem.theme.KptTheme
import kpt.core.common.CurrencyFormatter
import kpt.core.designsystem.component.MifosScaffold
import kpt.core.designsystem.icon.MifosIcons
import kpt.core.ui.AvatarBox
import org.koin.compose.viewmodel.koinViewModel
// TODO make the topbar back button consistent with other screens
/** Chat-style screen showing the payment history with a payee, grouped by date. */
@Composable
fun PaymentChatHistoryScreen(
    onBackClick: () -> Unit,
    onPaymentClick: () -> Unit,
    onTransactionClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PaymentChatHistoryViewModel = koinViewModel(),
) {
    val state by viewModel.stateFlow.collectAsStateWithLifecycle()

    MifosScaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            UserProfileTopBar(
                userType = state.userType,
                businessName = state.businessName,
                bankingName = state.bankingName,
                upiId = state.upiId,
                profileImageUrl = state.profileImageUrl,
                onBackClick = onBackClick,
            )
        },
        containerColor = KptTheme.colorScheme.background,
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            PaymentHistoryContent(
                paymentHistory = state.paymentHistory,
                onTransactionClick = onTransactionClick,
                modifier = Modifier.weight(1f),
            )

            PaymentActionBar(
                onPaymentClick = onPaymentClick,
                onMessageSend = viewModel::sendMessage,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun UserProfileTopBar(
    userType: UserType,
    businessName: String,
    bankingName: String,
    upiId: String,
    profileImageUrl: String?,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        IconButton(
            onClick = onBackClick,
            modifier = Modifier.size(40.dp),
        ) {
            Icon(
                imageVector = MifosIcons.Back,
                contentDescription = "Back",
                tint = KptTheme.colorScheme.onSurface,
                modifier = Modifier.size(24.dp),
            )
        }

        AvatarBox(
            name = if (userType == UserType.BUSINESS) businessName else bankingName,
            size = 40,
        )

        Column(
            modifier = Modifier.weight(1f),
        ) {
            Text(
                text = if (userType == UserType.BUSINESS) businessName else bankingName,
                style = KptTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                color = KptTheme.colorScheme.onSurface,
            )

            if (userType == UserType.INDIVIDUAL && upiId.isNotEmpty()) {
                Text(
                    text = upiId,
                    style = KptTheme.typography.bodySmall,
                    color = KptTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun PaymentHistoryContent(
    paymentHistory: List<PaymentHistoryGroup>,
    onTransactionClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        items(paymentHistory) { group ->
            PaymentHistoryGroup(
                group = group,
                onTransactionClick = onTransactionClick,
            )
        }
    }
}

@Composable
private fun PaymentHistoryGroup(
    group: PaymentHistoryGroup,
    onTransactionClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(1.dp)
                    .background(KptTheme.colorScheme.outline.copy(alpha = 0.3f)),
            )

            Text(
                text = group.date,
                style = KptTheme.typography.labelLarge,
                color = KptTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp),
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(1.dp)
                    .background(KptTheme.colorScheme.outline.copy(alpha = 0.3f)),
            )
        }

        group.transactions.forEach { transaction ->
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = if (transaction.isSent) Alignment.CenterEnd else Alignment.CenterStart,
            ) {
                PaymentCard(
                    transaction = transaction,
                    onClick = { onTransactionClick(transaction.transactionId) },
                    modifier = Modifier.width(224.dp),
                )
            }

            if (transaction != group.transactions.last()) {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun PaymentCard(
    transaction: PaymentTransaction,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (transaction.isSent) {
                KptTheme.colorScheme.primaryContainer
            } else {
                KptTheme.colorScheme.secondaryContainer
            },
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp,
        ),
        shape = RoundedCornerShape(12.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            Text(
                text = "Payment to ${getFirstName(transaction.recipientName)}",
                style = KptTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                color = KptTheme.colorScheme.onSurface,
            )

            if (transaction.note.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = transaction.note,
                    style = KptTheme.typography.bodyMedium,
                    color = KptTheme.colorScheme.onSurfaceVariant,
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = formatAmount(transaction.amount),
                style = KptTheme.typography.headlineSmall,
                fontWeight = FontWeight.Medium,
                color = KptTheme.colorScheme.onSurface,
            )

            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Icon(
                        imageVector = MifosIcons.CheckCircle,
                        contentDescription = "Paid",
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier.size(16.dp),
                    )
                    Text(
                        text = "Paid - ${formatPaymentDate(transaction.paymentDate)}",
                        style = KptTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = KptTheme.colorScheme.onSurfaceVariant,
                    )
                }

                Icon(
                    imageVector = MifosIcons.ChevronRight,
                    contentDescription = "View Details",
                    tint = KptTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
    }
}

@Composable
private fun PaymentActionBar(
    onPaymentClick: () -> Unit,
    onMessageSend: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        color = KptTheme.colorScheme.surface,
        shadowElevation = 8.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Button(
                onClick = onPaymentClick,
                modifier = Modifier.weight(0.3f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = KptTheme.colorScheme.primary,
                    contentColor = KptTheme.colorScheme.onPrimary,
                ),
                shape = RoundedCornerShape(24.dp),
            ) {
                Text(
                    text = "Pay",
                    style = KptTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                )
            }

            MessageInput(
                onMessageSend = onMessageSend,
                modifier = Modifier.weight(0.7f),
            )
        }
    }
}

@Composable
private fun MessageInput(
    onMessageSend: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var messageText by remember { mutableStateOf(TextFieldValue("")) }
    val focusRequester = remember { FocusRequester() }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .background(
                    color = KptTheme.colorScheme.surfaceContainerLow,
                    shape = RoundedCornerShape(24.dp),
                )
                .padding(horizontal = 16.dp, vertical = 12.dp),
        ) {
            BasicTextField(
                value = messageText,
                onValueChange = { messageText = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester),
                textStyle = TextStyle(
                    fontSize = 14.sp,
                    color = KptTheme.colorScheme.onSurface,
                ),
                decorationBox = { innerTextField ->
                    if (messageText.text.isEmpty()) {
                        Text(
                            text = "Message...",
                            style = TextStyle(
                                fontSize = 14.sp,
                                color = KptTheme.colorScheme.onSurfaceVariant,
                            ),
                        )
                    }
                    innerTextField()
                },
                singleLine = true,
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        IconButton(
            onClick = {
                if (messageText.text.isNotEmpty()) {
                    onMessageSend(messageText.text)
                    messageText = TextFieldValue("")
                }
            },
            modifier = Modifier
                .size(48.dp)
                .background(
                    color = KptTheme.colorScheme.primary,
                    shape = CircleShape,
                ),
        ) {
            Icon(
                imageVector = MifosIcons.Send,
                contentDescription = "Send Message",
                tint = KptTheme.colorScheme.onPrimary,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

private fun formatPaymentDate(dateString: String): String {
    return try {
        // Extract day, month, and year from the date string (e.g., "25 Sep 2023")
        val parts = dateString.split(" ")
        if (parts.size == 3) {
            val day = parts[0]
            val month = parts[1]
            val year = parts[2]
            "$day $month $year"
        } else {
            dateString
        }
    } catch (e: Exception) {
        dateString
    }
}

private fun getFirstName(fullName: String): String {
    return fullName.trim().split(" ").firstOrNull() ?: fullName
}

private fun formatAmount(amount: String): String {
    return try {
        val amountValue = amount.replace(",", "").toDoubleOrNull() ?: 0.0
        CurrencyFormatter.format(
            balance = amountValue,
            currencyCode = "INR",
            maximumFractionDigits = 2,
        )
    } catch (e: Exception) {
        "₹$amount"
    }
}

/** Kind of payee shown in the payment chat history. */
enum class UserType {
    /** A business payee. */
    BUSINESS,

    /** An individual payee. */
    INDIVIDUAL,
}

/**
 * A single payment shown as a chat bubble.
 * @property transactionId Identifier of the transaction.
 * @property recipientName Name of the recipient.
 * @property amount Formatted payment amount.
 * @property note Note attached to the payment.
 * @property paymentDate Formatted date of the payment.
 * @property timestamp Payment time as epoch milliseconds.
 * @property isSent Whether the payment was sent (true) or received (false).
 */
data class PaymentTransaction(
    val transactionId: String,
    val recipientName: String,
    val amount: String,
    val note: String,
    val paymentDate: String,
    val timestamp: Long,
    val isSent: Boolean,
)

/**
 * Payments that share the same date.
 * @property date Formatted date heading of the group.
 * @property transactions Payments made on that date.
 */
data class PaymentHistoryGroup(
    val date: String,
    val transactions: List<PaymentTransaction>,
)

/**
 * State of the payment chat history screen.
 * @property userType Whether the payee is a business or an individual.
 * @property businessName Name of the business payee.
 * @property bankingName Registered banking name of the payee.
 * @property upiId UPI ID of the payee.
 * @property profileImageUrl URL of the payee's profile image, if any.
 * @property paymentHistory Payments grouped by date.
 * @property isLoading Whether history is loading.
 * @property error Error message, if loading failed.
 */
data class PaymentChatHistoryState(
    val userType: UserType = UserType.INDIVIDUAL,
    val businessName: String = "",
    val bankingName: String = "",
    val upiId: String = "",
    val profileImageUrl: String? = null,
    val paymentHistory: List<PaymentHistoryGroup> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
)
