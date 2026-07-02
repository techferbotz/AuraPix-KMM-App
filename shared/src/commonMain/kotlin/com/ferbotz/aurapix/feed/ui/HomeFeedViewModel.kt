package com.ferbotz.aurapix.feed.ui

import com.ferbotz.aurapix.core.session.UserState
import com.ferbotz.aurapix.core.ui.base.AuraViewModel
import com.ferbotz.aurapix.core.ui.base.UiState
import com.ferbotz.aurapix.core.ui.base.toUiState
import com.ferbotz.aurapix.feed.data.FeedRepository
import com.ferbotz.aurapix.feed.data.model.FeedTray
import com.ferbotz.aurapix.feed.data.model.TrayType
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
    userState: StateFlow<UserState>,
) : AuraViewModel() {

    private val refreshTrigger = MutableStateFlow(0)

    /** The full feed as ordered, typed sections — Loading/Success/Error for the screen. */
    val feedState: StateFlow<UiState<List<FeedSection>>> =
        refreshTrigger
            .flatMapLatest { feedRepository.getFeed() }
            .map { state -> state.toUiState { trays -> trays.mapNotNull { it.toSectionOrNull() } } }
            .stateIn(scope, SharingStarted.WhileSubscribed(5_000), UiState.Loading)

    /** Gem badge in the top bar — reflects the shared session (UserManager). */
    val credits: StateFlow<Int> =
        userState
            .map { it.credits }
            .stateIn(scope, SharingStarted.WhileSubscribed(5_000), userState.value.credits)

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
