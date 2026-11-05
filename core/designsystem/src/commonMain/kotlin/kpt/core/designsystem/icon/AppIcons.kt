/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.designsystem.icon

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
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
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.DoneAll
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material.icons.outlined.Wallet
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material.icons.rounded.AccountBalance
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Contacts
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.QrCode
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material.icons.rounded.Wallet
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * The app's icon set, named by ROLE rather than by glyph.
 *
 * One indirection so a fork re-skins every icon in one place, and so a screen never imports a
 * Material icon directly — which is how two screens end up using different glyphs for the same
 * action.
 */
object AppIcons {
    /** `language` role — Material default `ArrowOutward`. */
    val Language: ImageVector = Icons.Default.ArrowOutward

    /** `check circle` role — Material filled `CheckCircle`. */
    val CheckCircle: ImageVector = Icons.Filled.CheckCircle

    /** `outlined info` role — Material outlined `Info`. */
    val OutlinedInfo = Icons.Outlined.Info

    /** `outlined lock` role — Material outlined `Lock`. */
    val OutlinedLock = Icons.Outlined.Lock

    /** `outlined notifications` role — Material outlined `Notifications`. */
    val OutlinedNotifications = Icons.Outlined.Notifications

    /** `chevron right` role — Material filled `ChevronRight`. */
    val ChevronRight: ImageVector = Icons.Filled.ChevronRight

    /** `qr code` role — Material filled `QrCode`. */
    val QrCode: ImageVector = Icons.Filled.QrCode

    /** `close` role — Material filled `Close`. */
    val Close: ImageVector = Icons.Filled.Close

    /** `attach money` role — Material filled `AttachMoney`. */
    val AttachMoney: ImageVector = Icons.Filled.AttachMoney

    /** `outlined visibility off` role — Material outlined `VisibilityOff`. */
    val OutlinedVisibilityOff: ImageVector = Icons.Outlined.VisibilityOff

    /** `outlined visibility` role — Material outlined `Visibility`. */
    val OutlinedVisibility: ImageVector = Icons.Outlined.Visibility

    /** `visibility off` role — Material filled `VisibilityOff`. */
    val VisibilityOff: ImageVector = Icons.Filled.VisibilityOff

    /** `visibility` role — Material filled `Visibility`. */
    val Visibility: ImageVector = Icons.Filled.Visibility

    /** `check` role — Material default `Check`. */
    val Check: ImageVector = Icons.Default.Check

    /** `keyboard arrow down` role — Material default `KeyboardArrowDown`. */
    val KeyboardArrowDown: ImageVector = Icons.Default.KeyboardArrowDown

    /** `home` role — Material outlined `Home`. */
    val Home = Icons.Outlined.Home

    /** `home boarder` role — Material rounded `Home`. */
    val HomeBoarder = Icons.Rounded.Home

    /** `payment` role — Material rounded `SwapHoriz`. */
    val Payment = Icons.Rounded.SwapHoriz

    /** `finance` role — Material outlined `Wallet`. */
    val Finance = Icons.Outlined.Wallet

    /** `finance boarder` role — Material rounded `Wallet`. */
    val FinanceBoarder = Icons.Rounded.Wallet

    /** `profile` role — Material outlined `AccountCircle`. */
    val Profile = Icons.Outlined.AccountCircle

    /** `profile boarder` role — Material rounded `AccountCircle`. */
    val ProfileBoarder = Icons.Rounded.AccountCircle

    /** `more vert` role — Material rounded `MoreVert`. */
    val MoreVert = Icons.Rounded.MoreVert

    /** `search` role — Material rounded `Search`. */
    val Search = Icons.Rounded.Search

    /** `add` role — Material rounded `Add`. */
    val Add = Icons.Rounded.Add

    /** `back` role — Material automirrored `Outlined`. */
    val Back = Icons.AutoMirrored.Outlined.ArrowBack

    /** `copy` role — Material filled `ContentCopy`. */
    val Copy = Icons.Filled.ContentCopy

    /** `share` role — Material filled `Share`. */
    val Share = Icons.Filled.Share

    /** `outlined share` role — Material outlined `Share`. */
    val OutlinedShare = Icons.Outlined.Share

    /** `arrow back` role — Material automirrored `Filled`. */
    val ArrowBack = Icons.AutoMirrored.Filled.ArrowBack

    /** `arrow back2` role — Material filled `ChevronLeft`. */
    val ArrowBack2 = Icons.Filled.ChevronLeft

    /** `arrow right` role — Material automirrored `Filled`. */
    val ArrowRight = Icons.AutoMirrored.Filled.KeyboardArrowRight

    /** `cancel` role — Material outlined `Cancel`. */
    val Cancel = Icons.Outlined.Cancel

    /** `account circle` role — Material outlined `AccountCircle`. */
    val AccountCircle = Icons.Outlined.AccountCircle

    /** `send right tilted` role — Material default `ArrowOutward`. */
    val SendRightTilted = Icons.Default.ArrowOutward

    /** `info` role — Material default `Info`. */
    val Info = Icons.Default.Info

    /** `camera` role — Material filled `Camera`. */
    val Camera = Icons.Filled.Camera

    /** `photo library` role — Material filled `PhotoLibrary`. */
    val PhotoLibrary = Icons.Filled.PhotoLibrary

    /** `delete` role — Material filled `Delete`. */
    val Delete = Icons.Filled.Delete

    /** `outlined delete` role — Material outlined `DeleteOutline`. */
    val OutlinedDelete = Icons.Outlined.DeleteOutline

    /** `rounded info` role — Material rounded `Info`. */
    val RoundedInfo = Icons.Rounded.Info

    /** `contact` role — Material rounded `Contacts`. */
    val Contact = Icons.Rounded.Contacts

    /** `settings` role — Material rounded `Settings`. */
    val Settings = Icons.Rounded.Settings

    /** `settings outlined` role — Material outlined `Settings`. */
    val SettingsOutlined = Icons.Outlined.Settings

    /** `q r` role — Material rounded `QrCode`. */
    val QR = Icons.Rounded.QrCode

    /** `bank` role — Material rounded `AccountBalance`. */
    val Bank = Icons.Rounded.AccountBalance

    /** `photo` role — Material default `Photo`. */
    val Photo = Icons.Default.Photo

    /** `flash on` role — Material default `FlashOn`. */
    val FlashOn = Icons.Default.FlashOn

    /** `flash off` role — Material default `FlashOff`. */
    val FlashOff = Icons.Default.FlashOff

    /** `qr code2` role — Material filled `QrCode2`. */
    val QrCode2 = Icons.Filled.QrCode2

    /** `edit` role — Material filled `Edit`. */
    val Edit = Icons.Filled.Edit

    /** `edit2` role — Material outlined `Edit`. */
    val Edit2 = Icons.Outlined.Edit

    /** `calender month` role — Material filled `CalendarMonth`. */
    val CalenderMonth = Icons.Filled.CalendarMonth

    /** `outlined done all` role — Material outlined `DoneAll`. */
    val OutlinedDoneAll = Icons.Outlined.DoneAll

    /** `person` role — Material filled `Person`. */
    val Person = Icons.Filled.Person

    /** `badge` role — Material filled `Badge`. */
    val Badge = Icons.Filled.Badge

    /** `data info` role — Material filled `Description`. */
    val DataInfo = Icons.Filled.Description

    /** `scan` role — Material outlined `QrCodeScanner`. */
    val Scan = Icons.Outlined.QrCodeScanner

    /** `radio button unchecked` role — Material default `RadioButtonUnchecked`. */
    val RadioButtonUnchecked = Icons.Default.RadioButtonUnchecked

    /** `radio button checked` role — Material filled `RadioButtonChecked`. */
    val RadioButtonChecked = Icons.Filled.RadioButtonChecked

//    val Theme = Icons.Filled.WbSunny
    /** `sun` role — Material outlined `WbSunny`. */
    val Sun = Icons.Outlined.WbSunny

    /** "Rate this app" affordance (settings). */
    val Star = Icons.Outlined.StarOutline
}
