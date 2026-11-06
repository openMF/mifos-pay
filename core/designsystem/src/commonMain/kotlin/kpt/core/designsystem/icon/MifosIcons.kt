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
    val Fingerprint = Icons.Default.Fingerprint
    val OutlinedInfo = Icons.Outlined.Info
    val OutlinedLock = Icons.Outlined.Lock
    val OutlinedNotifications = Icons.Outlined.Notifications
    val ChevronRight: ImageVector = Icons.Filled.ChevronRight
    val QrCode: ImageVector = Icons.Filled.QrCode
    val Close: ImageVector = Icons.Filled.Close
    val Error: ImageVector = Icons.Filled.Error
    val AttachMoney: ImageVector = Icons.Filled.AttachMoney
    val OutlinedVisibilityOff: ImageVector = Icons.Outlined.VisibilityOff
    val OutlinedVisibility: ImageVector = Icons.Outlined.Visibility
    val VisibilityOff: ImageVector = Icons.Filled.VisibilityOff
    val Visibility: ImageVector = Icons.Filled.Visibility
    val Check: ImageVector = Icons.Default.Check
    val KeyboardArrowDown: ImageVector = Icons.Default.KeyboardArrowDown
    val KeyboardArrowUp: ImageVector = Icons.Default.KeyboardArrowUp
    val DropDown: ImageVector = Icons.Default.ExpandMore
    val DropUp: ImageVector = Icons.Default.ExpandLess
    val Home = Icons.Outlined.Home
    val HomeBoarder = Icons.Rounded.Home
    val Payment = Icons.Rounded.SwapHoriz
    val Finance = Icons.Outlined.Wallet
    val FinanceBoarder = Icons.Rounded.Wallet
    val Profile = Icons.Outlined.AccountCircle
    val ProfileBoarder = Icons.Rounded.AccountCircle
    val MoreVert = Icons.Rounded.MoreVert
    val Search = Icons.Rounded.Search
    val Add = Icons.Rounded.Add
    val Back = Icons.AutoMirrored.Outlined.ArrowBack
    val Copy = Icons.Filled.ContentCopy
    val Share = Icons.Filled.Share
    val OutlinedShare = Icons.Outlined.Share
    val ArrowBack = Icons.AutoMirrored.Filled.ArrowBack
    val ArrowBack2 = Icons.Filled.ChevronLeft
    val Cancel = Icons.Outlined.Cancel
    val AccountCircle = Icons.Outlined.AccountCircle
    val SendRightTilted = Icons.Default.ArrowOutward
    val Info = Icons.Default.Info
    val Camera = Icons.Filled.Camera
    val PhotoLibrary = Icons.Filled.PhotoLibrary
    val Delete = Icons.Filled.Delete
    val OutlinedDelete = Icons.Outlined.DeleteOutline
    val RoundedInfo = Icons.Rounded.Info
    val Contact = Icons.Rounded.Contacts
    val Settings = Icons.Rounded.Settings
    val SettingsOutlined = Icons.Outlined.Settings
    val QR = Icons.Rounded.QrCode
    val Bank = Icons.Rounded.AccountBalance
    val Photo = Icons.Default.Photo
    val FlashOn = Icons.Default.FlashOn
    val FlashOff = Icons.Default.FlashOff
    val QrCode2 = Icons.Filled.QrCode2
    val Edit = Icons.Filled.Edit
    val Edit2 = Icons.Outlined.Edit
    val CalenderMonth = Icons.Filled.CalendarMonth
    val OutlinedDoneAll = Icons.Outlined.DoneAll
    val Person = Icons.Filled.Person
    val PersonAdd = Icons.Filled.PersonAdd
    val Badge = Icons.Filled.Badge
    val DataInfo = Icons.Filled.Description
    val Scan = Icons.Outlined.QrCodeScanner
    val RadioButtonUnchecked = Icons.Default.RadioButtonUnchecked
    val RadioButtonChecked = Icons.Filled.RadioButtonChecked

    val ArrowForward = Icons.AutoMirrored.Filled.ArrowForward

    val CurrencyRupee = Icons.Filled.CurrencyRupee
    val CheckRounded = Icons.Rounded.Check

    val Send = Icons.AutoMirrored.Filled.Send
    val History = Icons.Default.History
    val HistoryBoarder = Icons.Rounded.History
    val Filter = Icons.Default.FilterList
    val OpenInNew = Icons.AutoMirrored.Filled.OpenInNew
    val Warning = Icons.Default.Warning
    val Location = Icons.Filled.LocationOn

    val Transfer = Icons.Rounded.Money
    val CheckCircle = Icons.Outlined.CheckCircle
    val ArrowRight = Icons.AutoMirrored.Filled.KeyboardArrowRight
    val Download = Icons.Filled.Download

    // AutoPay specific icons
    val Schedule = Icons.Filled.Schedule
    val Security = Icons.Filled.Security
    val Power = Icons.Filled.Power
    val CreditCard = Icons.Filled.CreditCard
    val Rule = Icons.AutoMirrored.Filled.Rule
    val Receipt = Icons.Filled.Receipt
    val List = Icons.AutoMirrored.Filled.List

    val Email = Icons.Outlined.Email

    val Repeat = Icons.Filled.Repeat

    val Refresh = Icons.Filled.Refresh

    val ExpandLess = Icons.Filled.ExpandLess
    val ExpandMore = Icons.Filled.ExpandMore

    val Pocket = Icons.Default.CollectionsBookmark
    val PersonAccounts = FluentIcons.Filled.PersonAccounts
    val CoinMultiple = FluentIcons.Filled.CoinMultiple
    val Savings = Icons.Rounded.AccountBalanceWallet

    val Language = Icons.Default.Language
}
