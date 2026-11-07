/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.faq

import kotlinx.coroutines.flow.update
import kpt.core.ui.utils.BaseViewModel
import kpt.feature.faq.generated.resources.Res
import kpt.feature.faq.generated.resources.feature_faq_answer1
import kpt.feature.faq.generated.resources.feature_faq_answer2
import kpt.feature.faq.generated.resources.feature_faq_answer3
import kpt.feature.faq.generated.resources.feature_faq_answer4
import kpt.feature.faq.generated.resources.feature_faq_question1
import kpt.feature.faq.generated.resources.feature_faq_question2
import kpt.feature.faq.generated.resources.feature_faq_question3
import kpt.feature.faq.generated.resources.feature_faq_question4
internal class FAQViewModel : BaseViewModel<FaqState, FaqEvent, FaqAction>(
    initialState = FaqState(sampleFaqList),
) {
    override fun handleAction(action: FaqAction) {
        when (action) {
            is FaqAction.ExpandFaq -> {
                mutableStateFlow.update {
                    if (it.expandedFaq == action.faqId) {
                        it.copy(expandedFaq = null)
                    } else {
                        it.copy(expandedFaq = action.faqId)
                    }
                }
            }

            is FaqAction.NavigateBack -> {
                sendEvent(FaqEvent.OnNavigateBack)
            }
        }
    }
}

internal data class FaqState(
    val faqList: List<FAQ>,
    val expandedFaq: Int? = null,
)

internal sealed interface FaqEvent {
    data object OnNavigateBack : FaqEvent
}

internal sealed interface FaqAction {
    data class ExpandFaq(val faqId: Int) : FaqAction
    data object NavigateBack : FaqAction
}

/**
 * Retrieves a list of Frequently Asked Questions (FAQs).
 *
 * Currently, the FAQs are statically defined within this function. This is a temporary
 * implementation for demonstration or testing purposes. In the future, this method will
 * be updated to fetch the FAQ data from a backend service once the backend functionality
 * is implemented. This will allow for dynamic and up-to-date FAQ content.
 *
 * @return A list of [FAQ] objects containing the questions and answers.
 */
internal val sampleFaqList = listOf(
    FAQ(1, Res.string.feature_faq_question1, Res.string.feature_faq_answer1),
    FAQ(2, Res.string.feature_faq_question2, Res.string.feature_faq_answer2),
    FAQ(3, Res.string.feature_faq_question3, Res.string.feature_faq_answer3),
    FAQ(4, Res.string.feature_faq_question4, Res.string.feature_faq_answer4),
)
