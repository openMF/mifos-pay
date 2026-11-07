/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.designsystem.icon

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.automirrored.filled.Rule
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.ArrowOutward
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CollectionsBookmark
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Power
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.DoneAll
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material.icons.outlined.Wallet
import androidx.compose.material.icons.rounded.AccountBalance
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Contacts
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Money
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.QrCode
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material.icons.rounded.Wallet
import androidx.compose.ui.graphics.vector.ImageVector
import fluent.ui.system.icons.FluentIcons
import fluent.ui.system.icons.filled.CoinMultiple
import fluent.ui.system.icons.filled.PersonAccounts
import fluent.ui.system.icons.filled.Wallet

/**
 * Mifos icons. Material icons are [ImageVector]s, custom icons are drawable resource IDs.
 */
object MifosIcons {
    /** Fingerprint icon used for biometric authentication prompts. */
    val Fingerprint = Icons.Default.Fingerprint

    /** Outlined information icon. */
    val OutlinedInfo = Icons.Outlined.Info

    /** Outlined padlock icon, used for security and passcode settings. */
    val OutlinedLock = Icons.Outlined.Lock

    /** Outlined notification bell icon. */
    val OutlinedNotifications = Icons.Outlined.Notifications

    /** Filled right-pointing chevron, typically a list-row navigation affordance. */
    val ChevronRight: ImageVector = Icons.Filled.ChevronRight

    /** Filled QR code icon. */
    val QrCode: ImageVector = Icons.Filled.QrCode

    /** Close (X) icon. */
    val Close: ImageVector = Icons.Filled.Close

    /** Filled error icon. */
    val Error: ImageVector = Icons.Filled.Error

    /** Dollar/money icon. */
    val AttachMoney: ImageVector = Icons.Filled.AttachMoney

    /** Outlined "hide" eye icon, used to mask password text. */
    val OutlinedVisibilityOff: ImageVector = Icons.Outlined.VisibilityOff

    /** Outlined "show" eye icon, used to reveal password text. */
    val OutlinedVisibility: ImageVector = Icons.Outlined.Visibility

    /** Filled "hide" eye icon. */
    val VisibilityOff: ImageVector = Icons.Filled.VisibilityOff

    /** Filled "show" eye icon. */
    val Visibility: ImageVector = Icons.Filled.Visibility

    /** Check mark icon. */
    val Check: ImageVector = Icons.Default.Check

    /** Downward keyboard arrow icon. */
    val KeyboardArrowDown: ImageVector = Icons.Default.KeyboardArrowDown

    /** Upward keyboard arrow icon. */
    val KeyboardArrowUp: ImageVector = Icons.Default.KeyboardArrowUp

    /** Expand-more chevron shown on dropdown fields. */
    val DropDown: ImageVector = Icons.Default.ExpandMore

    /** Expand-less chevron shown on an opened dropdown. */
    val DropUp: ImageVector = Icons.Default.ExpandLess

    /** Outlined home icon, used for the Home navigation item. */
    val Home = Icons.Outlined.Home

    /** Rounded home icon, used as the selected Home navigation item. */
    val HomeBoarder = Icons.Rounded.Home

    /** Rounded swap icon, used for the Payments navigation item. */
    val Payment = Icons.Rounded.SwapHoriz

    /** Outlined wallet icon, used for the Finance navigation item. */
    val Finance = Icons.Outlined.Wallet

    /** Rounded wallet icon, used as the selected Finance navigation item. */
    val FinanceBoarder = Icons.Rounded.Wallet

    /** Outlined account icon, used for the Profile navigation item. */
    val Profile = Icons.Outlined.AccountCircle

    /** Rounded account icon, used as the selected Profile navigation item. */
    val ProfileBoarder = Icons.Rounded.AccountCircle

    /** Vertical three-dot overflow menu icon. */
    val MoreVert = Icons.Rounded.MoreVert

    /** Search magnifier icon. */
    val Search = Icons.Rounded.Search

    /** Add (plus) icon. */
    val Add = Icons.Rounded.Add

    /** Outlined auto-mirrored back arrow. */
    val Back = Icons.AutoMirrored.Outlined.ArrowBack

    /** Copy-to-clipboard icon. */
    val Copy = Icons.Filled.ContentCopy

    /** Filled share icon. */
    val Share = Icons.Filled.Share

    /** Outlined share icon. */
    val OutlinedShare = Icons.Outlined.Share

    /** Filled auto-mirrored back arrow. */
    val ArrowBack = Icons.AutoMirrored.Filled.ArrowBack

    /** Left-pointing chevron used as an alternative back icon. */
    val ArrowBack2 = Icons.Filled.ChevronLeft

    /** Outlined cancel icon. */
    val Cancel = Icons.Outlined.Cancel

    /** Outlined account circle icon. */
    val AccountCircle = Icons.Outlined.AccountCircle

    /** Arrow pointing up and to the right, used for send/outgoing actions. */
    val SendRightTilted = Icons.Default.ArrowOutward

    /** Filled information icon. */
    val Info = Icons.Default.Info

    /** Camera icon. */
    val Camera = Icons.Filled.Camera

    /** Photo library icon, used to pick an image from the gallery. */
    val PhotoLibrary = Icons.Filled.PhotoLibrary

    /** Filled delete (trash) icon. */
    val Delete = Icons.Filled.Delete

    /** Outlined delete icon. */
    val OutlinedDelete = Icons.Outlined.DeleteOutline

    /** Rounded information icon. */
    val RoundedInfo = Icons.Rounded.Info

    /** Rounded contacts icon. */
    val Contact = Icons.Rounded.Contacts

    /** Rounded settings (gear) icon. */
    val Settings = Icons.Rounded.Settings

    /** Outlined settings (gear) icon. */
    val SettingsOutlined = Icons.Outlined.Settings

    /** Rounded QR code icon. */
    val QR = Icons.Rounded.QrCode

    /** Rounded bank building icon, used for bank accounts. */
    val Bank = Icons.Rounded.AccountBalance

    /** Photo icon. */
    val Photo = Icons.Default.Photo

    /** Camera flash on icon, used in the QR scanner. */
    val FlashOn = Icons.Default.FlashOn

    /** Camera flash off icon, used in the QR scanner. */
    val FlashOff = Icons.Default.FlashOff

    /** Alternative filled QR code icon. */
    val QrCode2 = Icons.Filled.QrCode2

    /** Filled edit (pencil) icon. */
    val Edit = Icons.Filled.Edit

    /** Outlined edit (pencil) icon. */
    val Edit2 = Icons.Outlined.Edit

    /** Calendar month icon, used for date selection. */
    val CalenderMonth = Icons.Filled.CalendarMonth

    /** Outlined double-check icon. */
    val OutlinedDoneAll = Icons.Outlined.DoneAll

    /** Person icon. */
    val Person = Icons.Filled.Person

    /** Add-person icon. */
    val PersonAdd = Icons.Filled.PersonAdd

    /** Badge (ID card) icon. */
    val Badge = Icons.Filled.Badge

    /** Description (document) icon, used for data/detail rows. */
    val DataInfo = Icons.Filled.Description

    /** QR code scanner icon. */
    val Scan = Icons.Outlined.QrCodeScanner

    /** Unselected radio button icon. */
    val RadioButtonUnchecked = Icons.Default.RadioButtonUnchecked

    /** Selected radio button icon. */
    val RadioButtonChecked = Icons.Filled.RadioButtonChecked

    /** Auto-mirrored forward arrow. */
    val ArrowForward = Icons.AutoMirrored.Filled.ArrowForward

    /** Indian rupee currency icon. */
    val CurrencyRupee = Icons.Filled.CurrencyRupee

    /** Rounded check mark icon. */
    val CheckRounded = Icons.Rounded.Check

    /** Auto-mirrored send icon. */
    val Send = Icons.AutoMirrored.Filled.Send

    /** History (clock) icon. */
    val History = Icons.Default.History

    /** Rounded history icon, used as the selected History navigation item. */
    val HistoryBoarder = Icons.Rounded.History

    /** Filter list icon. */
    val Filter = Icons.Default.FilterList

    /** Auto-mirrored "open in new" icon. */
    val OpenInNew = Icons.AutoMirrored.Filled.OpenInNew

    /** Warning icon. */
    val Warning = Icons.Default.Warning

    /** Location pin icon. */
    val Location = Icons.Filled.LocationOn

    /** Rounded money icon, used for transfers. */
    val Transfer = Icons.Rounded.Money

    /** Outlined check-in-circle icon, used for success states. */
    val CheckCircle = Icons.Outlined.CheckCircle

    /** Auto-mirrored right keyboard arrow. */
    val ArrowRight = Icons.AutoMirrored.Filled.KeyboardArrowRight

    /** Download icon. */
    val Download = Icons.Filled.Download

    // AutoPay specific icons
    /** Schedule (clock) icon used by AutoPay. */
    val Schedule = Icons.Filled.Schedule

    /** Security shield icon used by AutoPay. */
    val Security = Icons.Filled.Security

    /** Power icon used by AutoPay. */
    val Power = Icons.Filled.Power

    /** Credit card icon used by AutoPay. */
    val CreditCard = Icons.Filled.CreditCard

    /** Auto-mirrored rule icon used by AutoPay. */
    val Rule = Icons.AutoMirrored.Filled.Rule

    /** Receipt icon used by AutoPay. */
    val Receipt = Icons.Filled.Receipt

    /** Auto-mirrored list icon used by AutoPay. */
    val List = Icons.AutoMirrored.Filled.List

    /** Outlined email icon. */
    val Email = Icons.Outlined.Email

    /** Repeat icon, used for recurring payments. */
    val Repeat = Icons.Filled.Repeat

    /** Refresh icon. */
    val Refresh = Icons.Filled.Refresh

    /** Expand-less chevron. */
    val ExpandLess = Icons.Filled.ExpandLess

    /** Expand-more chevron. */
    val ExpandMore = Icons.Filled.ExpandMore

    /** Collections-bookmark icon, used for pockets (sub-wallets). */
    val Pocket = Icons.Default.CollectionsBookmark

    /** Fluent person-with-accounts icon. */
    val PersonAccounts = FluentIcons.Filled.PersonAccounts

    /** Fluent stacked coins icon. */
    val CoinMultiple = FluentIcons.Filled.CoinMultiple

    /** Rounded wallet icon used for savings accounts. */
    val Savings = Icons.Rounded.AccountBalanceWallet

    /** Language (globe) icon. */
    val Language = Icons.Default.Language
}
