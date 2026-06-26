package com.ferbotz.aurapix.ui.viewmodel

import com.ferbotz.aurapix.data.common.DataState
import com.ferbotz.aurapix.data.model.FeedTray
import com.ferbotz.aurapix.data.model.TrayType
import com.ferbotz.aurapix.data.repository.FeedRepository
import com.ferbotz.aurapix.data.repository.ProfileRepository
import com.ferbotz.aurapix.ui.screens.CategoryItem
import com.ferbotz.aurapix.ui.screens.FeedSection
import com.ferbotz.aurapix.ui.screens.FeedSectionKind
import com.ferbotz.aurapix.ui.screens.TemplateItem
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

@OptIn(ExperimentalCoroutinesApi::class)
class HomeFeedViewModel(
    feedRepository: FeedRepository,
    private val profileRepository: ProfileRepository,
) : AuraViewModel() {

    private val refreshTrigger = MutableStateFlow(0)

    /** The full feed as ordered, typed sections — Loading/Success/Error for the screen. */
    val feedState: StateFlow<UiState<List<FeedSection>>> =
        refreshTrigger
            .flatMapLatest { feedRepository.getFeed() }
            .map { state -> state.toUiState { trays -> trays.mapNotNull { it.toSectionOrNull() } } }
            .stateIn(scope, SharingStarted.WhileSubscribed(5_000), UiState.Loading)

    /** Credits badge in the top bar. Falls back to the cached value when offline / not signed in. */
    val credits: StateFlow<Int> =
        refreshTrigger
            .flatMapLatest { profileRepository.getCredits() }
            .map { state -> (state as? DataState.Success)?.data?.totalCredits ?: profileRepository.cachedCredits }
            .stateIn(scope, SharingStarted.WhileSubscribed(5_000), profileRepository.cachedCredits)

    fun refresh() {
        refreshTrigger.value += 1
    }
}

/** Maps a decoded [FeedTray] to a presentation [FeedSection]; drops unknown/empty trays. */
private fun FeedTray.toSectionOrNull(): FeedSection? = when (type) {
    TrayType.TEMPLATE -> templates
        .takeIf { it.isNotEmpty() }
        ?.let { list ->
            FeedSection(
                id = id,
                title = title,
                kind = FeedSectionKind.TEMPLATES,
                templates = list.map {
                    TemplateItem(name = it.title, id = it.id, thumbnailUrl = it.thumbnailImageUrl, trending = it.isTrending)
                },
            )
        }

    TrayType.CATEGORY -> categories
        .takeIf { it.isNotEmpty() }
        ?.let { list ->
            FeedSection(
                id = id,
                title = title,
                kind = FeedSectionKind.CATEGORIES,
                categories = list.map { CategoryItem(id = it.id, name = it.name, iconUrl = it.iconUrl) },
            )
        }

    TrayType.UNKNOWN -> null
}
