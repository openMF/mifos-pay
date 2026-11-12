/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.user

/**
 * Every language the app can be switched to, in the user's OWN language.
 *
 * GENERATED from core/registries/LOCALE_REGISTRY.yaml by
 * `core/scripts/language-picker-sync.sh --write` — DO NOT HAND-EDIT. The selectable set is
 * held EQUAL to the shipped locale set by LC-14 (RULE-IMPL-LOCALE-COVERAGE-001): a locale
 * that has translated strings but no picker entry is unreachable, and a picker entry with
 * no strings shows English. Add or remove languages in the registry, then re-run the sync.
 *
 * Enabled for mifos-x/kmp-project-template: 20 languages + system default.
 * Resolved by core/scripts/locale-resolve.sh from locales.languages[].
 *
 * @property localeName platform locale tag (BCP-47) passed to the platform locale switcher;
 *   `null` means "follow the system".
 * @property text the language endonym — a picker must name a language in that language.
 */
enum class LanguageConfig(
    val localeName: String?,
    val text: String,
) {
    /** Follow the system locale. */
    DEFAULT(
        localeName = null,
        text = "System Default",
    ),

    /** English — locale tag `en`. */
    ENGLISH(
        localeName = "en",
        text = "English",
    ),

    /** Español — locale tag `es`. */
    SPANISH(
        localeName = "es",
        text = "Español",
    ),

    /** 简体中文 — locale tag `zh-CN`. */
    CHINESE_SIMPLIFIED(
        localeName = "zh-CN",
        text = "简体中文",
    ),

    /** 繁體中文 — locale tag `zh-TW`. */
    CHINESE_TRADITIONAL(
        localeName = "zh-TW",
        text = "繁體中文",
    ),

    /** हिन्दी — locale tag `hi`. */
    HINDI(
        localeName = "hi",
        text = "हिन्दी",
    ),

    /** العربية — locale tag `ar`. */
    ARABIC(
        localeName = "ar",
        text = "العربية",
    ),

    /** Português (Brasil) — locale tag `pt-BR`. */
    PORTUGUESE_BRAZIL(
        localeName = "pt-BR",
        text = "Português (Brasil)",
    ),

    /** Русский — locale tag `ru`. */
    RUSSIAN(
        localeName = "ru",
        text = "Русский",
    ),

    /** 日本語 — locale tag `ja`. */
    JAPANESE(
        localeName = "ja",
        text = "日本語",
    ),

    /** Deutsch — locale tag `de`. */
    GERMAN(
        localeName = "de",
        text = "Deutsch",
    ),

    /** Français — locale tag `fr`. */
    FRENCH(
        localeName = "fr",
        text = "Français",
    ),

    /** 한국어 — locale tag `ko`. */
    KOREAN(
        localeName = "ko",
        text = "한국어",
    ),

    /** Italiano — locale tag `it`. */
    ITALIAN(
        localeName = "it",
        text = "Italiano",
    ),

    /** Türkçe — locale tag `tr`. */
    TURKISH(
        localeName = "tr",
        text = "Türkçe",
    ),

    /** Bahasa Indonesia — locale tag `in`. */
    INDONESIAN(
        localeName = "in",
        text = "Bahasa Indonesia",
    ),

    /** Tiếng Việt — locale tag `vi`. */
    VIETNAMESE(
        localeName = "vi",
        text = "Tiếng Việt",
    ),

    /** ไทย — locale tag `th`. */
    THAI(
        localeName = "th",
        text = "ไทย",
    ),

    /** Polski — locale tag `pl`. */
    POLISH(
        localeName = "pl",
        text = "Polski",
    ),

    /** Nederlands — locale tag `nl`. */
    DUTCH(
        localeName = "nl",
        text = "Nederlands",
    ),

    /** Українська — locale tag `uk`. */
    UKRAINIAN(
        localeName = "uk",
        text = "Українська",
    ),
}
