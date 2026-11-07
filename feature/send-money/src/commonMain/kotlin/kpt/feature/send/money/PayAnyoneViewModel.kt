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

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kpt.core.common.getSerialized
import kpt.core.common.setSerialized
import kpt.core.ui.fork.utils.BaseViewModel
/** ViewModel for the pay-anyone screen; handles input, contact search and invite sharing. */
class PayAnyoneViewModel(
    savedStateHandle: SavedStateHandle,
) : BaseViewModel<PayAnyoneState, PayAnyoneEvent, PayAnyoneAction>(
    initialState = savedStateHandle.getSerialized(KEY_STATE) ?: PayAnyoneState(),
) {

    /** Holds the saved-state key, search constants and the list of UPI handles. */
    companion object {
        private const val KEY_STATE = "pay_anyone_state"
        private const val SEARCH_DEBOUNCE_DELAY = 300L
        private const val MIN_SEARCH_LENGTH = 1

        /** UPI handle suffixes suggested after the user types a name or number. */
        val UPI_HANDLES = listOf(
            "@ybl", "@axl", "@ptsbi", "@oksbi", "@pthdfc", "@icici", "@paytm", "@phonepe", "@upi", "@okicici",
            "@kotak", "@hdfc", "@sbi", "@axis", "@unionbank", "@canara", "@pnb", "@yesbank",
        )
    }

    init {
        stateFlow
            .onEach { savedStateHandle.setSerialized(key = KEY_STATE, value = it) }
            .launchIn(viewModelScope)

        // Handle selected contact phone from navigation arguments
        val selectedContactPhone = savedStateHandle.get<String>("selectedContact")
        if (selectedContactPhone != null) {
            mutableStateFlow.update {
                it.copy(
                    inputValue = selectedContactPhone,
                    showClearIcon = true,
                    isSearching = selectedContactPhone.length >= MIN_SEARCH_LENGTH,
                    showPartialNumberNote = false,
                    searchResults = if (selectedContactPhone.isEmpty()) ContactSearchResult() else it.searchResults,
                )
            }

            // Trigger search if the phone number meets minimum length
            if (selectedContactPhone.length >= MIN_SEARCH_LENGTH) {
                viewModelScope.launch {
                    val searchResult = searchContacts(selectedContactPhone)
                    val isPartialNumber = isPartialPhoneNumber(selectedContactPhone)
                    val hasNoResults = searchResult.people.isEmpty() && searchResult.others.isEmpty() && searchResult.businesses.isEmpty()

                    mutableStateFlow.update {
                        it.copy(
                            isSearching = false,
                            searchResults = searchResult,
                            showPartialNumberNote = isPartialNumber && hasNoResults,
                        )
                    }
                }
            }
        }

        // Setup search functionality
        setupSearchFlow()
    }

    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    private fun setupSearchFlow() {
        stateFlow
            .map { it.inputValue }
            .distinctUntilChanged()
            .debounce(SEARCH_DEBOUNCE_DELAY)
            .filter { it.length >= MIN_SEARCH_LENGTH }
            .flatMapLatest { searchQuery ->
                performSearch(searchQuery)
            }
            .launchIn(viewModelScope)
    }

    private fun performSearch(query: String): Flow<ContactSearchResult> {
        return kotlinx.coroutines.flow.flow {
            // Show loading state
            mutableStateFlow.update { it.copy(isSearching = true) }

            // Simulate network delay
            delay(500)

            val searchResult = searchContacts(query)
            val isPartialNumber = isPartialPhoneNumber(query)
            val hasNoResults = searchResult.people.isEmpty() && searchResult.others.isEmpty() && searchResult.businesses.isEmpty()

            // Update state with search results
            mutableStateFlow.update {
                it.copy(
                    isSearching = false,
                    searchResults = searchResult,
                    showPartialNumberNote = isPartialNumber && hasNoResults,
                )
            }

            // Note: Phone numbers not found on UPI will now appear in the "Others" section
            // instead of triggering the contact not found dialog

            emit(searchResult)
        }
    }

    private fun searchContacts(query: String): ContactSearchResult {
        val allContacts = sampleAllContacts + sampleRecentContacts + sampleBusinessContacts
        val normalizedQuery = query.lowercase().trim()

        val filteredContacts = allContacts.filter { contact ->
            contact.name.lowercase().contains(normalizedQuery) ||
                contact.phoneNumber.replace(" ", "").startsWith(normalizedQuery.replace(" ", "")) ||
                (contact.upiId?.lowercase()?.contains(normalizedQuery) == true)
        }

        // If no contacts found but query looks like a phone number, create a contact for "Others" section
        val contactsToShow = if (filteredContacts.isEmpty() && isFullPhoneNumber(query)) {
            listOf(
                Contact(
                    id = "not_found_${query.hashCode()}",
                    name = "Unknown Contact",
                    phoneNumber = query,
                    upiId = null,
                    type = ContactType.PERSON,
                ),
            )
        } else {
            filteredContacts
        }

        val people = contactsToShow.filter { it.upiId != null && it.type == ContactType.PERSON }
        val others = contactsToShow.filter { it.upiId == null && it.type == ContactType.PERSON }
        val businesses = contactsToShow.filter { it.type == ContactType.BUSINESS }

        return ContactSearchResult(
            people = people,
            others = others,
            businesses = businesses,
        )
    }

    private fun isPartialPhoneNumber(query: String): Boolean {
        val cleanQuery = query.replace(" ", "").replace("+", "").replace("-", "")
        return cleanQuery.length in 1..9 && cleanQuery.all { it.isDigit() }
    }

    private fun isFullPhoneNumber(query: String): Boolean {
        val cleanQuery = query.replace(" ", "").replace("+", "").replace("-", "")
        return cleanQuery.length >= 10 && cleanQuery.all { it.isDigit() }
    }

    private fun shareInviteMessage(sharingOption: SharingOption, phoneNumber: String) {
        val inviteMessage = createInviteMessage(phoneNumber)

        // Use the platform-specific sharing helper
        val sharingHelper = getSharingHelper()
        sharingHelper.shareInviteMessage(sharingOption, phoneNumber, inviteMessage)
    }

    private fun createInviteMessage(phoneNumber: String): String {
        return buildString {
            appendLine("Hi! I'm trying to send you money via UPI, but I couldn't find you on any UPI app.")
            appendLine()
            appendLine("Please download Mifos Pay to receive payments instantly:")
            appendLine("https://play.google.com/store/apps/details?id=org.mifospay&hl=en_IN")
            appendLine()
            appendLine("Once you install the app, I'll be able to send you money directly!")
            appendLine()
            appendLine("Thanks!")
        }
    }

    private fun showContactNotFoundFlow(phoneNumber: String) {
        viewModelScope.launch {
            // Show loading state
            mutableStateFlow.update {
                it.copy(
                    showContactNotFoundLoading = true,
                    showContactNotFoundMessage = false,
                    showContactNotFoundDialog = false,
                    inputValue = phoneNumber,
                )
            }

            // Simulate delay
            delay(2000)

            // Show message
            mutableStateFlow.update {
                it.copy(
                    showContactNotFoundLoading = false,
                    showContactNotFoundMessage = true,
                    showContactNotFoundDialog = false,
                )
            }

            // Show dialog after another delay
            delay(1500)

            mutableStateFlow.update {
                it.copy(
                    showContactNotFoundLoading = false,
                    showContactNotFoundMessage = false,
                    showContactNotFoundDialog = true,
                )
            }
        }
    }

    /**
     * Determines if UPI handle suggestions should be shown based on user input
     * Shows suggestions when user types '@' followed by 0 or 1 character
     */
    private fun shouldShowUpiHandleSuggestions(input: String): Boolean {
        val lastAtSymbolIndex = input.lastIndexOf('@')
        if (lastAtSymbolIndex == -1) return false

        val textAfterAt = input.substring(lastAtSymbolIndex + 1)
        // Show suggestions if there's no text after @ or just 1 character
        return textAfterAt.isEmpty() || (textAfterAt.length == 1 && !textAfterAt.contains(' '))
    }

    override fun handleAction(action: PayAnyoneAction) {
        when (action) {
            is PayAnyoneAction.InputValueChanged -> {
                val newValue = action.value
                val shouldShowSuggestions = shouldShowUpiHandleSuggestions(newValue)

                mutableStateFlow.update {
                    it.copy(
                        inputValue = newValue,
                        showClearIcon = newValue.isNotEmpty(),
                        isSearching = newValue.length >= MIN_SEARCH_LENGTH && !shouldShowSuggestions,
                        showPartialNumberNote = false,
                        showUpiHandleSuggestions = shouldShowSuggestions,
                        searchResults = if (newValue.isEmpty()) ContactSearchResult() else it.searchResults,
                    )
                }
            }

            is PayAnyoneAction.PhoneNumberSelected -> {
                val phoneNumber = action.phoneNumber
                mutableStateFlow.update {
                    it.copy(
                        inputValue = phoneNumber,
                        showClearIcon = true,
                        isSearching = phoneNumber.length >= MIN_SEARCH_LENGTH,
                        showPartialNumberNote = false,
                        showUpiHandleSuggestions = false,
                        searchResults = if (phoneNumber.isEmpty()) ContactSearchResult() else it.searchResults,
                    )
                }

                // Trigger search if the phone number meets minimum length
                if (phoneNumber.length >= MIN_SEARCH_LENGTH) {
                    viewModelScope.launch {
                        val searchResult = searchContacts(phoneNumber)
                        val isPartialNumber = isPartialPhoneNumber(phoneNumber)
                        val hasNoResults = searchResult.people.isEmpty() && searchResult.others.isEmpty() && searchResult.businesses.isEmpty()

                        mutableStateFlow.update {
                            it.copy(
                                isSearching = false,
                                searchResults = searchResult,
                                showPartialNumberNote = isPartialNumber && hasNoResults,
                            )
                        }
                    }
                }
            }

            is PayAnyoneAction.UpiHandleSelected -> {
                val currentInput = stateFlow.value.inputValue
                val lastAtSymbolIndex = currentInput.lastIndexOf('@')
                val newValue = if (lastAtSymbolIndex != -1) {
                    // Replace everything from @ onwards with the selected handle
                    currentInput.substring(0, lastAtSymbolIndex) + action.handle
                } else {
                    currentInput + action.handle
                }

                mutableStateFlow.update {
                    it.copy(
                        inputValue = newValue,
                        showClearIcon = newValue.isNotEmpty(),
                        isSearching = false,
                        showPartialNumberNote = false,
                        showUpiHandleSuggestions = false,
                        searchResults = ContactSearchResult(),
                    )
                }
            }

            PayAnyoneAction.ClearInput -> {
                mutableStateFlow.update {
                    it.copy(
                        inputValue = "",
                        showClearIcon = false,
                        isSearching = false,
                        showPartialNumberNote = false,
                        showUpiHandleSuggestions = false,
                        searchResults = ContactSearchResult(),
                    )
                }
            }

            PayAnyoneAction.ToggleKeyboardType -> {
                mutableStateFlow.update {
                    it.copy(
                        isKeyboardNumeric = !it.isKeyboardNumeric,
                    )
                }
            }

            PayAnyoneAction.ShowContactNotFoundDialog -> {
                mutableStateFlow.update {
                    it.copy(
                        showContactNotFoundDialog = true,
                        showContactNotFoundMessage = true,
                    )
                }
            }

            PayAnyoneAction.HideContactNotFoundDialog -> {
                mutableStateFlow.update {
                    it.copy(
                        showContactNotFoundDialog = false,
                    )
                }
            }

            is PayAnyoneAction.SharingOptionSelected -> {
                mutableStateFlow.update {
                    it.copy(
                        selectedSharingOption = action.option,
                    )
                }
            }

            PayAnyoneAction.SetAsDefaultSharingOption -> {
                // Save the selected sharing option as default
                val selectedOption = stateFlow.value.selectedSharingOption
                shareInviteMessage(selectedOption, stateFlow.value.inputValue)
                mutableStateFlow.update {
                    it.copy(
                        showContactNotFoundDialog = false,
                        defaultSharingOption = selectedOption,
                    )
                }
            }

            PayAnyoneAction.NotNowSharingOption -> {
                // Share the invite message without setting as default
                shareInviteMessage(stateFlow.value.selectedSharingOption, stateFlow.value.inputValue)
                mutableStateFlow.update {
                    it.copy(
                        showContactNotFoundDialog = false,
                    )
                }
            }

            PayAnyoneAction.ShareInviteMessage -> {
                shareInviteMessage(stateFlow.value.selectedSharingOption, stateFlow.value.inputValue)
            }

            is PayAnyoneAction.ContactSelected -> {
                val contact = action.contact

                if (contact.upiId == null) {
                    val defaultOption = stateFlow.value.defaultSharingOption

                    if (defaultOption != null) {
                        shareInviteMessage(defaultOption, contact.phoneNumber)
                    } else {
                        showContactNotFoundFlow(contact.phoneNumber)
                    }
                }
            }
        }
    }
}

/**
 * State of the pay-anyone screen.
 * @property inputValue Text typed in the search field.
 * @property isKeyboardNumeric Whether the numeric keyboard is shown.
 * @property showClearIcon Whether the clear icon is visible.
 * @property isSearching Whether a search is in progress.
 * @property showPartialNumberNote Whether to show the partial number note.
 * @property showUpiHandleSuggestions Whether UPI handle suggestions are shown.
 * @property recentContacts Recently used contacts.
 * @property allContacts All contacts.
 * @property searchResults Contacts matching the current search.
 * @property showContactNotFoundDialog Whether the contact-not-found dialog is visible.
 * @property showContactNotFoundMessage Whether the contact-not-found message is visible.
 * @property showContactNotFoundLoading Whether the contact-not-found lookup is loading.
 * @property selectedSharingOption Currently selected invite sharing channel.
 * @property defaultSharingOption Saved default sharing channel, if any.
 * @property inviteMessage Invite message to share.
 */
@Serializable
data class PayAnyoneState(
    val inputValue: String = "",
    val isKeyboardNumeric: Boolean = false,
    val showClearIcon: Boolean = false,
    val isSearching: Boolean = false,
    val showPartialNumberNote: Boolean = false,
    val showUpiHandleSuggestions: Boolean = false,
    val recentContacts: List<Contact> = sampleRecentContacts,
    val allContacts: List<Contact> = sampleAllContacts,
    val searchResults: ContactSearchResult = ContactSearchResult(),
    val showContactNotFoundDialog: Boolean = false,
    val showContactNotFoundMessage: Boolean = false,
    val showContactNotFoundLoading: Boolean = false,
    val selectedSharingOption: SharingOption = SharingOption.WHATSAPP,
    val defaultSharingOption: SharingOption? = null,
    val inviteMessage: String = "",
)

/** Channel used to share an invite to a contact who is not on the app. */
@Serializable
enum class SharingOption {
    /** Share through WhatsApp. */
    WHATSAPP,

    /** Share through SMS. */
    SMS,
}

/**
 * Contact search results grouped by kind.
 * @property people Matching people.
 * @property others Other matching contacts.
 * @property businesses Matching businesses.
 */
@Serializable
data class ContactSearchResult(
    val people: List<Contact> = emptyList(),
    val others: List<Contact> = emptyList(),
    val businesses: List<Contact> = emptyList(),
)

private val sampleRecentContacts = listOf(
    Contact(
        id = "1",
        name = "John Doe",
        phoneNumber = "+91 98765 43210",
        upiId = "john.doe@upi",
        type = ContactType.PERSON,
    ),
    Contact(
        id = "2",
        name = "Jane Smith",
        phoneNumber = "+91 98765 43211",
        upiId = "jane.smith@okicici",
        type = ContactType.PERSON,
    ),
    Contact(
        id = "3",
        name = "Mike Johnson",
        phoneNumber = "+91 98765 43212",
        upiId = "mike.johnson@paytm",
        type = ContactType.PERSON,
    ),
    Contact(
        id = "4",
        name = "Sarah Wilson",
        phoneNumber = "+91 98765 43213",
        upiId = "sarah.wilson@phonepe",
        type = ContactType.PERSON,
    ),
)

private val sampleAllContacts = listOf(
    Contact(
        id = "5",
        name = "Alice Brown",
        phoneNumber = "+91 98765 43214",
        upiId = "alice.brown@upi",
        type = ContactType.PERSON,
    ),
    Contact(
        id = "6",
        name = "Bob Davis",
        phoneNumber = "+91 98765 43215",
        upiId = "bob.davis@okicici",
        type = ContactType.PERSON,
    ),
    Contact(
        id = "7",
        name = "Carol Miller",
        phoneNumber = "+91 98765 43216",
        upiId = "carol.miller@paytm",
        type = ContactType.PERSON,
    ),
    Contact(
        id = "8",
        name = "David Garcia",
        phoneNumber = "+91 98765 43217",
        upiId = "david.garcia@phonepe",
        type = ContactType.PERSON,
    ),
    Contact(
        id = "9",
        name = "Emma Rodriguez",
        phoneNumber = "+91 98765 43218",
        upiId = "emma.rodriguez@upi",
        type = ContactType.PERSON,
    ),
    Contact(
        id = "10",
        name = "Frank Martinez",
        phoneNumber = "+91 98765 43219",
        upiId = "frank.martinez@okicici",
        type = ContactType.PERSON,
    ),
    Contact(
        id = "11",
        name = "Grace Lee",
        phoneNumber = "+91 98765 43220",
        upiId = "grace.lee@paytm",
        type = ContactType.PERSON,
    ),
    Contact(
        id = "12",
        name = "Henry Taylor",
        phoneNumber = "+91 98765 43221",
        upiId = "henry.taylor@phonepe",
        type = ContactType.PERSON,
    ),
    // Contacts without UPI ID (Others section)
    Contact(
        id = "13",
        name = "Tom Wilson",
        phoneNumber = "+91 98765 43222",
        type = ContactType.PERSON,
    ),
    Contact(
        id = "14",
        name = "Lisa Anderson",
        phoneNumber = "+91 98765 43223",
        type = ContactType.PERSON,
    ),
    Contact(
        id = "15",
        name = "Mark Thompson",
        phoneNumber = "+91 98765 43224",
        type = ContactType.PERSON,
    ),
)

private val sampleBusinessContacts = listOf(
    Contact(
        id = "16",
        name = "Amazon India",
        phoneNumber = "+91 1800 3000 9009",
        upiId = "amazon@icici",
        type = ContactType.BUSINESS,
    ),
    Contact(
        id = "17",
        name = "Flipkart",
        phoneNumber = "+91 1800 202 9898",
        upiId = "flipkart@paytm",
        type = ContactType.BUSINESS,
    ),
    Contact(
        id = "18",
        name = "Swiggy",
        phoneNumber = "+91 1800 208 2088",
        upiId = "swiggy@phonepe",
        type = ContactType.BUSINESS,
    ),
    Contact(
        id = "19",
        name = "Zomato",
        phoneNumber = "+91 1800 208 1222",
        upiId = "zomato@upi",
        type = ContactType.BUSINESS,
    ),
    Contact(
        id = "20",
        name = "Uber",
        phoneNumber = "+91 1800 102 3837",
        upiId = "uber@okicici",
        type = ContactType.BUSINESS,
    ),
    Contact(
        id = "21",
        name = "Netflix",
        phoneNumber = "+91 1800 102 1234",
        upiId = "netflix@paytm",
        type = ContactType.BUSINESS,
    ),
    Contact(
        id = "22",
        name = "Spotify",
        phoneNumber = "+91 1800 102 5678",
        upiId = "spotify@phonepe",
        type = ContactType.BUSINESS,
    ),
)

/** One-off events emitted by the pay-anyone screen. */
sealed interface PayAnyoneEvent {
    /** Navigate back. */
    data object NavigateBack : PayAnyoneEvent

    /** Navigate to the contact picker. */
    data object NavigateToContactPicker : PayAnyoneEvent
}

/** User actions on the pay-anyone screen. */
sealed interface PayAnyoneAction {
    /**
     * The search text changed.
     * @property value New search text.
     */
    data class InputValueChanged(val value: String) : PayAnyoneAction

    /**
     * The user selected a phone number.
     * @property phoneNumber The selected number.
     */
    data class PhoneNumberSelected(val phoneNumber: String) : PayAnyoneAction

    /** The user cleared the input. */
    data object ClearInput : PayAnyoneAction

    /** The user toggled between text and numeric keyboards. */
    data object ToggleKeyboardType : PayAnyoneAction

    /**
     * The user picked a UPI handle suggestion.
     * @property handle The selected handle.
     */
    data class UpiHandleSelected(val handle: String) : PayAnyoneAction

    /** Show the contact-not-found dialog. */
    data object ShowContactNotFoundDialog : PayAnyoneAction

    /** Hide the contact-not-found dialog. */
    data object HideContactNotFoundDialog : PayAnyoneAction

    /**
     * The user picked a sharing channel.
     * @property option The selected channel.
     */
    data class SharingOptionSelected(val option: SharingOption) : PayAnyoneAction

    /** Save the selected channel as the default. */
    data object SetAsDefaultSharingOption : PayAnyoneAction

    /** The user declined to save a default channel. */
    data object NotNowSharingOption : PayAnyoneAction

    /** Share the invite message. */
    data object ShareInviteMessage : PayAnyoneAction

    /**
     * The user selected a contact.
     * @property contact The selected contact.
     */
    data class ContactSelected(val contact: Contact) : PayAnyoneAction
}
