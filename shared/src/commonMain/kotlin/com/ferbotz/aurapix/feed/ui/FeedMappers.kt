package com.ferbotz.aurapix.feed.ui

import com.ferbotz.aurapix.core.data.remote.dto.CategorySummaryDto
import com.ferbotz.aurapix.core.data.remote.dto.TemplateSummaryDto

/**
 * DTO → presentation mappers shared by [HomeFeedViewModel] and [TrayListingViewModel],
 * so the feed and its "See all" screens surface the same fields.
 */

fun TemplateSummaryDto.toTemplateItem(): TemplateItem = TemplateItem(
    name = title,
    id = id,
    thumbnailUrl = thumbnailImageUrl,
    trending = isTrending,
    description = shortDescription,
)

fun CategorySummaryDto.toCategoryItem(): CategoryItem = CategoryItem(
    id = id,
    name = name,
    iconUrl = iconUrl,
    bannerUrl = bannerUrl,
)
