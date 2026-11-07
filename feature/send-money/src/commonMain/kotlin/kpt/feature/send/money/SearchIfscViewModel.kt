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

import kotlinx.coroutines.flow.update
import kpt.core.ui.utils.BaseViewModel
// TODO replace dummy data with actual data or call API
/** ViewModel for the IFSC search screen; filters banks and branches and emits the chosen IFSC code. */
class SearchIfscViewModel : BaseViewModel<SearchIfscState, SearchIfscEvent, SearchIfscAction>(
    initialState = SearchIfscState(),
) {

    override fun handleAction(action: SearchIfscAction) {
        when (action) {
            is SearchIfscAction.NavigateBack -> {
                sendEvent(SearchIfscEvent.NavigateBack)
            }
            is SearchIfscAction.UpdateBankName -> {
                mutableStateFlow.update { it.copy(bankName = action.bankName) }
            }
            is SearchIfscAction.UpdateSearchQuery -> {
                mutableStateFlow.update { it.copy(searchQuery = action.query) }
                performSearch(action.query)
            }
            is SearchIfscAction.SelectIfscCode -> {
                sendEvent(SearchIfscEvent.IfscSelected(action.ifscCode))
            }
            is SearchIfscAction.SelectBank -> {
                val selectedBank = if (action.bank.name.isEmpty()) null else action.bank
                mutableStateFlow.update {
                    it.copy(
                        bankName = "",
                        selectedBank = selectedBank,
                        bankBranch = "",
                        selectedBranch = null,
                        filteredBranches = if (selectedBank != null) getBranchesForBank(selectedBank.name) else emptyList(),
                    )
                }
            }
            is SearchIfscAction.UpdateBankBranch -> {
                mutableStateFlow.update {
                    it.copy(
                        bankBranch = action.bankBranch,
                        filteredBranches = getFilteredBranches(action.bankBranch),
                    )
                }
            }
            is SearchIfscAction.ClearBankSelection -> {
                mutableStateFlow.update {
                    it.copy(
                        bankName = "",
                        selectedBank = null,
                        bankBranch = "",
                        selectedBranch = null,
                        selectedIfscCode = null,
                        filteredBranches = emptyList(),
                    )
                }
            }
            is SearchIfscAction.SelectBranch -> {
                val ifscCode = generateIfscCodeForBranch(action.branch, mutableStateFlow.value.selectedBank?.name ?: "")
                mutableStateFlow.update {
                    it.copy(
                        selectedBranch = action.branch,
                        selectedIfscCode = ifscCode,
                    )
                }
            }
            is SearchIfscAction.ClearBranchSelection -> {
                mutableStateFlow.update { it.copy(selectedBranch = null, selectedIfscCode = null) }
            }
        }
    }

    private fun performSearch(query: String) {
        if (query.length < 3) {
            mutableStateFlow.update {
                it.copy(searchState = SearchIfscState.SearchState.Empty)
            }
            return
        }

        mutableStateFlow.update {
            it.copy(searchState = SearchIfscState.SearchState.Loading)
        }

        // Simulate search delay and results
        // In a real implementation, this would call an API
        val mockResults = getMockIfscResults(query)

        mutableStateFlow.update {
            it.copy(
                searchState = if (mockResults.isEmpty()) {
                    SearchIfscState.SearchState.Empty
                } else {
                    SearchIfscState.SearchState.Success(mockResults)
                },
            )
        }
    }

    private fun getMockIfscResults(query: String): List<IfscCode> {
        val allIfscCodes = listOf(
            IfscCode(
                code = "SBIN0001234",
                bankName = "State Bank of India",
                branch = "Mumbai Main Branch",
                address = "Mumbai, Maharashtra",
                city = "Mumbai",
                state = "Maharashtra",
            ),
            IfscCode(
                code = "HDFC0001234",
                bankName = "HDFC Bank",
                branch = "Delhi Main Branch",
                address = "Delhi, Delhi",
                city = "Delhi",
                state = "Delhi",
            ),
            IfscCode(
                code = "ICIC0001234",
                bankName = "ICICI Bank",
                branch = "Bangalore Main Branch",
                address = "Bangalore, Karnataka",
                city = "Bangalore",
                state = "Karnataka",
            ),
            IfscCode(
                code = "AXIS0001234",
                bankName = "Axis Bank",
                branch = "Chennai Main Branch",
                address = "Chennai, Tamil Nadu",
                city = "Chennai",
                state = "Tamil Nadu",
            ),
            IfscCode(
                code = "KOTAK0001234",
                bankName = "Kotak Mahindra Bank",
                branch = "Pune Main Branch",
                address = "Pune, Maharashtra",
                city = "Pune",
                state = "Maharashtra",
            ),
        )

        return allIfscCodes.filter { ifscCode ->
            ifscCode.code.contains(query, ignoreCase = true) ||
                ifscCode.bankName.contains(query, ignoreCase = true) ||
                ifscCode.branch.contains(query, ignoreCase = true) ||
                ifscCode.city.contains(query, ignoreCase = true)
        }
    }

    private fun getFilteredBranches(query: String): List<DummyBankBranch> {
        val currentState = mutableStateFlow.value
        val selectedBank = currentState.selectedBank ?: return emptyList()

        if (query.isEmpty()) {
            return getBranchesForBank(selectedBank.name)
        }

        return getBranchesForBank(selectedBank.name).filter { branch ->
            branch.name.contains(query, ignoreCase = true) ||
                branch.state.contains(query, ignoreCase = true)
        }
    }

    private fun getBranchesForBank(bankName: String): List<DummyBankBranch> {
        return when (bankName) {
            "State Bank of India" -> sbiBranches
            "HDFC Bank" -> hdfcBranches
            "ICICI Bank" -> iciciBranches
            "Punjab National Bank" -> pnbBranches
            "Bank of Baroda" -> bobBranches
            "Canara Bank" -> canaraBranches
            "Union Bank of India" -> unionBranches
            "Axis Bank" -> axisBranches
            "Kotak Mahindra Bank" -> kotakBranches
            "Yes Bank" -> yesBranches
            else -> emptyList()
        }
    }

    private fun generateIfscCodeForBranch(branch: DummyBankBranch, bankName: String): String {
        val bankCode = when (bankName) {
            "State Bank of India" -> "SBIN"
            "HDFC Bank" -> "HDFC"
            "ICICI Bank" -> "ICIC"
            "Punjab National Bank" -> "PNBN"
            "Bank of Baroda" -> "BARB"
            "Canara Bank" -> "CANA"
            "Union Bank of India" -> "UBIN"
            "Axis Bank" -> "AXIS"
            "Kotak Mahindra Bank" -> "KOTAK"
            "Yes Bank" -> "YESB"
            else -> "XXXX"
        }

        val branchCode = branch.name.replace(" ", "").take(4).uppercase()
        val cityCode = branch.state.take(2).uppercase()
        val sequenceNumber = "0001"

        return "$bankCode$branchCode$cityCode$sequenceNumber"
    }
}

private val sbiBranches = listOf(
    DummyBankBranch("Mumbai Main Branch", "Maharashtra"),
    DummyBankBranch("Delhi Main Branch", "Delhi"),
    DummyBankBranch("Bangalore Main Branch", "Karnataka"),
    DummyBankBranch("Chennai Main Branch", "Tamil Nadu"),
    DummyBankBranch("Kolkata Main Branch", "West Bengal"),
    DummyBankBranch("Hyderabad Main Branch", "Telangana"),
    DummyBankBranch("Ahmedabad Main Branch", "Gujarat"),
    DummyBankBranch("Pune Main Branch", "Maharashtra"),
    DummyBankBranch("Jaipur Main Branch", "Rajasthan"),
    DummyBankBranch("Lucknow Main Branch", "Uttar Pradesh"),
    DummyBankBranch("Chandigarh Main Branch", "Chandigarh"),
    DummyBankBranch("Indore Main Branch", "Madhya Pradesh"),
    DummyBankBranch("Bhopal Main Branch", "Madhya Pradesh"),
    DummyBankBranch("Patna Main Branch", "Bihar"),
    DummyBankBranch("Bhubaneswar Main Branch", "Odisha"),
)

private val hdfcBranches = listOf(
    DummyBankBranch("Andheri West Branch", "Maharashtra"),
    DummyBankBranch("Bandra Kurla Complex", "Maharashtra"),
    DummyBankBranch("Connaught Place", "Delhi"),
    DummyBankBranch("Koramangala", "Karnataka"),
    DummyBankBranch("T Nagar", "Tamil Nadu"),
    DummyBankBranch("Koregaon Park", "Maharashtra"),
    DummyBankBranch("Banjara Hills", "Telangana"),
    DummyBankBranch("Salt Lake City", "West Bengal"),
    DummyBankBranch("Satellite", "Gujarat"),
    DummyBankBranch("Malviya Nagar", "Rajasthan"),
    DummyBankBranch("Gomti Nagar", "Uttar Pradesh"),
    DummyBankBranch("Sector 17", "Chandigarh"),
    DummyBankBranch("Vijay Nagar", "Madhya Pradesh"),
    DummyBankBranch("Arera Colony", "Madhya Pradesh"),
    DummyBankBranch("Boring Road", "Bihar"),
    DummyBankBranch("Nayapalli", "Odisha"),
)

private val iciciBranches = listOf(
    DummyBankBranch("Marine Drive", "Maharashtra"),
    DummyBankBranch("Lajpat Nagar", "Delhi"),
    DummyBankBranch("Indiranagar", "Karnataka"),
    DummyBankBranch("Anna Nagar", "Tamil Nadu"),
    DummyBankBranch("Kalyani Nagar", "Maharashtra"),
    DummyBankBranch("Jubilee Hills", "Telangana"),
    DummyBankBranch("Park Street", "West Bengal"),
    DummyBankBranch("Vastrapur", "Gujarat"),
    DummyBankBranch("C Scheme", "Rajasthan"),
    DummyBankBranch("Hazratganj", "Uttar Pradesh"),
)

private val pnbBranches = listOf(
    DummyBankBranch("Connaught Place", "Delhi"),
    DummyBankBranch("Mumbai Central", "Maharashtra"),
    DummyBankBranch("Koramangala", "Karnataka"),
    DummyBankBranch("T Nagar", "Tamil Nadu"),
    DummyBankBranch("Salt Lake City", "West Bengal"),
    DummyBankBranch("Satellite", "Gujarat"),
    DummyBankBranch("Malviya Nagar", "Rajasthan"),
    DummyBankBranch("Gomti Nagar", "Uttar Pradesh"),
)

private val bobBranches = listOf(
    DummyBankBranch("Mumbai Main Branch", "Maharashtra"),
    DummyBankBranch("Delhi Main Branch", "Delhi"),
    DummyBankBranch("Bangalore Main Branch", "Karnataka"),
    DummyBankBranch("Chennai Main Branch", "Tamil Nadu"),
    DummyBankBranch("Kolkata Main Branch", "West Bengal"),
    DummyBankBranch("Ahmedabad Main Branch", "Gujarat"),
    DummyBankBranch("Pune Main Branch", "Maharashtra"),
    DummyBankBranch("Jaipur Main Branch", "Rajasthan"),
)

private val canaraBranches = listOf(
    DummyBankBranch("Mumbai Main Branch", "Maharashtra"),
    DummyBankBranch("Delhi Main Branch", "Delhi"),
    DummyBankBranch("Bangalore Main Branch", "Karnataka"),
    DummyBankBranch("Chennai Main Branch", "Tamil Nadu"),
    DummyBankBranch("Kolkata Main Branch", "West Bengal"),
    DummyBankBranch("Hyderabad Main Branch", "Telangana"),
    DummyBankBranch("Ahmedabad Main Branch", "Gujarat"),
    DummyBankBranch("Pune Main Branch", "Maharashtra"),
)

private val unionBranches = listOf(
    DummyBankBranch("Mumbai Main Branch", "Maharashtra"),
    DummyBankBranch("Delhi Main Branch", "Delhi"),
    DummyBankBranch("Bangalore Main Branch", "Karnataka"),
    DummyBankBranch("Chennai Main Branch", "Tamil Nadu"),
    DummyBankBranch("Kolkata Main Branch", "West Bengal"),
    DummyBankBranch("Hyderabad Main Branch", "Telangana"),
    DummyBankBranch("Ahmedabad Main Branch", "Gujarat"),
    DummyBankBranch("Pune Main Branch", "Maharashtra"),
)

private val axisBranches = listOf(
    DummyBankBranch("Andheri West Branch", "Maharashtra"),
    DummyBankBranch("Bandra Kurla Complex", "Maharashtra"),
    DummyBankBranch("Connaught Place", "Delhi"),
    DummyBankBranch("Koramangala", "Karnataka"),
    DummyBankBranch("T Nagar", "Tamil Nadu"),
    DummyBankBranch("Banjara Hills", "Telangana"),
    DummyBankBranch("Salt Lake City", "West Bengal"),
    DummyBankBranch("Satellite", "Gujarat"),
)

private val kotakBranches = listOf(
    DummyBankBranch("Andheri West Branch", "Maharashtra"),
    DummyBankBranch("Bandra Kurla Complex", "Maharashtra"),
    DummyBankBranch("Connaught Place", "Delhi"),
    DummyBankBranch("Koramangala", "Karnataka"),
    DummyBankBranch("T Nagar", "Tamil Nadu"),
    DummyBankBranch("Banjara Hills", "Telangana"),
    DummyBankBranch("Salt Lake City", "West Bengal"),
    DummyBankBranch("Satellite", "Gujarat"),
)

private val yesBranches = listOf(
    DummyBankBranch("Andheri West Branch", "Maharashtra"),
    DummyBankBranch("Bandra Kurla Complex", "Maharashtra"),
    DummyBankBranch("Connaught Place", "Delhi"),
    DummyBankBranch("Koramangala", "Karnataka"),
    DummyBankBranch("T Nagar", "Tamil Nadu"),
    DummyBankBranch("Banjara Hills", "Telangana"),
    DummyBankBranch("Salt Lake City", "West Bengal"),
    DummyBankBranch("Satellite", "Gujarat"),
)

/**
 * A bank branch IFSC record.
 * @property code IFSC code.
 * @property bankName Name of the bank.
 * @property branch Branch name.
 * @property address Branch address.
 * @property city City of the branch.
 * @property state State of the branch.
 */
data class IfscCode(
    val code: String,
    val bankName: String,
    val branch: String,
    val address: String,
    val city: String,
    val state: String,
)

/**
 * Placeholder bank entry used by the IFSC search.
 * @property name Bank name.
 */
data class DummyBank(
    val name: String,
)

/**
 * Placeholder bank branch entry used by the IFSC search.
 * @property name Branch name.
 * @property state State where the branch is located.
 */
data class DummyBankBranch(
    val name: String,
    val state: String,
)

/**
 * State of the IFSC search screen.
 * @property bankName Bank name typed by the user.
 * @property searchQuery Search text.
 * @property searchState Result state of the search.
 * @property selectedBank Bank chosen from the list.
 * @property bankBranch Branch text typed by the user.
 * @property selectedBranch Branch chosen from the list.
 * @property filteredBranches Branches matching the current input.
 * @property selectedIfscCode IFSC code chosen by the user.
 */
data class SearchIfscState(
    val bankName: String = "",
    val searchQuery: String = "",
    val searchState: SearchState = SearchState.Empty,
    val selectedBank: DummyBank? = null,
    val bankBranch: String = "",
    val selectedBranch: DummyBankBranch? = null,
    val filteredBranches: List<DummyBankBranch> = emptyList(),
    val selectedIfscCode: String? = null,
) {
    /** Result state of an IFSC search. */
    sealed interface SearchState {
        /** Search is in progress. */
        data object Loading : SearchState

        /** No search has been run or no query was given. */
        data object Empty : SearchState

        /**
         * Search succeeded.
         * @property results Matching IFSC records.
         */
        data class Success(val results: List<IfscCode>) : SearchState

        /**
         * Search failed.
         * @property message Error message.
         */
        data class Error(val message: String) : SearchState
    }
}

/** One-off events emitted by the IFSC search screen. */
sealed interface SearchIfscEvent {
    /** Navigate back. */
    data object NavigateBack : SearchIfscEvent

    /**
     * An IFSC code was chosen.
     * @property ifscCode The chosen record.
     */
    data class IfscSelected(val ifscCode: IfscCode) : SearchIfscEvent
}

/** User actions on the IFSC search screen. */
sealed interface SearchIfscAction {
    /** The user tapped back. */
    data object NavigateBack : SearchIfscAction

    /**
     * The bank name text changed.
     * @property bankName New bank name text.
     */
    data class UpdateBankName(val bankName: String) : SearchIfscAction

    /**
     * The search text changed.
     * @property query New search text.
     */
    data class UpdateSearchQuery(val query: String) : SearchIfscAction

    /**
     * The user picked an IFSC record.
     * @property ifscCode The picked record.
     */
    data class SelectIfscCode(val ifscCode: IfscCode) : SearchIfscAction

    /**
     * The user picked a bank.
     * @property bank The picked bank.
     */
    data class SelectBank(val bank: DummyBank) : SearchIfscAction

    /**
     * The branch text changed.
     * @property bankBranch New branch text.
     */
    data class UpdateBankBranch(val bankBranch: String) : SearchIfscAction

    /** The user cleared the selected bank. */
    data object ClearBankSelection : SearchIfscAction

    /**
     * The user picked a branch.
     * @property branch The picked branch.
     */
    data class SelectBranch(val branch: DummyBankBranch) : SearchIfscAction

    /** The user cleared the selected branch. */
    data object ClearBranchSelection : SearchIfscAction
}
