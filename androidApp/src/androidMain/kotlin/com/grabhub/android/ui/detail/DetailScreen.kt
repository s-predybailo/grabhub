package com.grabhub.android.ui.detail

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.SubcomposeAsyncImage
import com.grabhub.android.R
import com.grabhub.android.ui.components.GrabHubLoadingState
import com.grabhub.android.ui.components.ModelImagePlaceholderIcon
import com.grabhub.android.ui.components.SourceBadge
import com.grabhub.android.ui.theme.GrabHubShapes
import com.grabhub.domain.ModelDetail
import com.grabhub.domain.carouselImages
import com.grabhub.util.formatCount
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalFoundationApi::class, ExperimentalLayoutApi::class)
@Composable
fun DetailScreen(
    modelId: String,
    onBack: () -> Unit,
    viewModel: DetailViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var galleryOpen by remember { mutableStateOf(false) }
    var galleryStartPage by remember { mutableIntStateOf(0) }

    LaunchedEffect(modelId) {
        viewModel.load(modelId)
    }

    when {
        uiState.isLoading -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding(),
                contentAlignment = Alignment.Center,
            ) {
                GrabHubLoadingState(message = stringResource(R.string.detail_loading))
            }
        }

        uiState.error != null -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .padding(24.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = uiState.error ?: stringResource(R.string.detail_error),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        }

        uiState.detail != null -> {
            val detail = uiState.detail!!
            val images = detail.carouselImages()

            if (galleryOpen && images.isNotEmpty()) {
                FullScreenImageViewer(
                    images = images,
                    initialPage = galleryStartPage,
                    title = detail.item.title,
                    onDismiss = { galleryOpen = false },
                )
            }

            DetailContent(
                detail = detail,
                isFavorite = uiState.isFavorite,
                onBack = onBack,
                onToggleFavorite = viewModel::toggleFavorite,
                onOpenUrl = { url ->
                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                },
                onShare = { title, url ->
                    context.startActivity(
                        Intent.createChooser(
                            Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, "$title\n$url")
                            },
                            null,
                        ),
                    )
                },
                onImageClick = { page ->
                    galleryStartPage = page
                    galleryOpen = true
                },
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalLayoutApi::class)
@Composable
private fun DetailContent(
    detail: ModelDetail,
    isFavorite: Boolean,
    onBack: () -> Unit,
    onToggleFavorite: () -> Unit,
    onOpenUrl: (String) -> Unit,
    onShare: (String, String) -> Unit,
    onImageClick: (Int) -> Unit,
) {
    val item = detail.item
    val images = detail.carouselImages()
    val scrollState = rememberScrollState()
    var descriptionExpanded by remember(detail.item.id) { mutableStateOf(false) }
    var currentImagePage by remember(detail.item.id) { mutableIntStateOf(0) }

    Column(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(scrollState),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(360.dp),
            ) {
                if (images.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                        contentAlignment = Alignment.Center,
                    ) {
                        ModelImagePlaceholderIcon()
                    }
                } else {
                    val pagerState = rememberPagerState(pageCount = { images.size })
                    LaunchedEffect(pagerState.currentPage) {
                        currentImagePage = pagerState.currentPage
                    }
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxSize(),
                        beyondViewportPageCount = 1,
                    ) { page ->
                        SubcomposeAsyncImage(
                            model = images[page],
                            contentDescription = "${item.title} ${page + 1}",
                            modifier = Modifier
                                .fillMaxSize()
                                .clickable { onImageClick(page) },
                            contentScale = ContentScale.Crop,
                            loading = {
                                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    CircularProgressIndicator(modifier = Modifier.size(28.dp), strokeWidth = 2.dp)
                                }
                            },
                            error = {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    ModelImagePlaceholderIcon()
                                }
                            },
                        )
                    }

                    if (images.size > 1) {
                        Row(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            repeat(images.size) { index ->
                                val selected = pagerState.currentPage == index
                                Box(
                                    modifier = Modifier
                                        .size(if (selected) 9.dp else 7.dp)
                                        .background(
                                            if (selected) Color.White else Color.White.copy(alpha = 0.45f),
                                            CircleShape,
                                        ),
                                )
                            }
                        }
                        Text(
                            text = stringResource(R.string.detail_image_counter, pagerState.currentPage + 1, images.size),
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(top = 72.dp, end = 16.dp)
                                .background(Color.Black.copy(alpha = 0.45f), GrabHubShapes.small)
                                .padding(horizontal = 10.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White,
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Black.copy(alpha = 0.45f),
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.2f),
                                ),
                            ),
                        ),
                )

                FilledTonalIconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .statusBarsPadding()
                        .padding(8.dp),
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.88f),
                    ),
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.detail_back))
                }
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = GrabHubShapes.large,
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp,
                shadowElevation = 6.dp,
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Text(item.title, style = MaterialTheme.typography.headlineSmall)

                    DetailAuthorRow(
                        author = item.author,
                        onOpen = { onOpenUrl(item.modelUrl) },
                    )

                    DetailStatsPills(detail = detail)

                    detail.description?.let { description ->
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(stringResource(R.string.detail_description), style = MaterialTheme.typography.titleMedium)
                            val preview = if (descriptionExpanded || description.length <= 420) {
                                description
                            } else {
                                description.take(420).trimEnd() + "…"
                            }
                            Text(
                                text = preview,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            if (description.length > 420) {
                                TextButton(onClick = { descriptionExpanded = !descriptionExpanded }) {
                                    Text(
                                        stringResource(
                                            if (descriptionExpanded) R.string.detail_show_less else R.string.detail_show_more,
                                        ),
                                    )
                                }
                            }
                        }
                    }

                    detail.license?.let { license ->
                        DetailInfoCard(
                            title = stringResource(R.string.detail_license),
                            value = license,
                        )
                    }

                    val tags = item.tags
                    if (!tags.isNullOrEmpty()) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(stringResource(R.string.detail_tags), style = MaterialTheme.typography.titleMedium)
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                tags.forEach { tag ->
                                    Surface(
                                        shape = GrabHubShapes.small,
                                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                                    ) {
                                        Text(
                                            text = tag,
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                            style = MaterialTheme.typography.labelMedium,
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }

        DetailBottomBar(
            isFavorite = isFavorite,
            hasGallery = images.isNotEmpty(),
            imageCounter = if (images.size > 1) {
                stringResource(R.string.detail_image_counter, currentImagePage + 1, images.size)
            } else {
                null
            },
            onShare = { onShare(item.title, item.modelUrl) },
            onToggleFavorite = onToggleFavorite,
            onOpen = { onOpenUrl(item.modelUrl) },
            onGallery = { onImageClick(currentImagePage) },
        )
    }
}

@Composable
private fun DetailAuthorRow(
    author: String?,
    onOpen: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            val initial = author?.firstOrNull()?.uppercaseChar()?.toString() ?: "?"
            Surface(
                modifier = Modifier.size(40.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = initial,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
            }
            Text(
                text = author ?: stringResource(R.string.detail_unknown_author),
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Button(onClick = onOpen, shape = RoundedCornerShape(999.dp)) {
            Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(stringResource(R.string.detail_open))
        }
    }
}

@Composable
private fun DetailStatsPills(detail: ModelDetail) {
    val item = detail.item
    val likesLabel = stringResource(R.string.stat_likes)
    val downloadsLabel = stringResource(R.string.stat_downloads)
    val commentsLabel = stringResource(R.string.stat_comments)
    val makesLabel = stringResource(R.string.stat_makes)
    val viewsLabel = stringResource(R.string.stat_views)
    val filesLabel = stringResource(R.string.stat_files)

    val stats = buildList {
        item.likes?.let { add(Triple(Icons.Default.ThumbUp, formatCount(it) ?: it.toString(), likesLabel)) }
        item.downloads?.let { add(Triple(Icons.Default.Download, formatCount(it) ?: it.toString(), downloadsLabel)) }
        detail.commentCount?.let { add(Triple(Icons.Default.ChatBubbleOutline, formatCount(it) ?: it.toString(), commentsLabel)) }
        detail.makeCount?.let { add(Triple(Icons.Default.Print, formatCount(it) ?: it.toString(), makesLabel)) }
        detail.viewCount?.let { add(Triple(Icons.Default.Visibility, formatCount(it) ?: it.toString(), viewsLabel)) }
        detail.fileCount?.let { add(Triple(Icons.Default.FolderOpen, it.toString(), filesLabel)) }
    }

    if (stats.isEmpty()) {
        SourceBadge(source = item.source)
        return
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SourceBadge(source = item.source)
        stats.forEach { (icon, value, label) ->
            DetailStatPill(icon = icon, value = value, label = label)
        }
    }
}

@Composable
private fun DetailStatPill(
    icon: ImageVector,
    value: String,
    label: String,
) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        tonalElevation = 1.dp,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.labelLarge,
                maxLines = 1,
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun DetailBottomBar(
    isFavorite: Boolean,
    hasGallery: Boolean,
    imageCounter: String?,
    onShare: () -> Unit,
    onToggleFavorite: () -> Unit,
    onOpen: () -> Unit,
    onGallery: () -> Unit,
) {
    val favoriteTint by animateColorAsState(
        targetValue = if (isFavorite) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "favoriteTint",
    )

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.98f),
        shadowElevation = 12.dp,
        tonalElevation = 4.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onShare) {
                Icon(Icons.Default.Share, contentDescription = stringResource(R.string.detail_share))
            }
            IconButton(onClick = onToggleFavorite) {
                Icon(
                    imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = stringResource(
                        if (isFavorite) R.string.detail_favorite_remove else R.string.detail_favorite_add,
                    ),
                    tint = favoriteTint,
                )
            }
            if (imageCounter != null && hasGallery) {
                Text(
                    text = imageCounter,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            IconButton(onClick = onOpen) {
                Icon(Icons.Default.OpenInNew, contentDescription = stringResource(R.string.detail_open))
            }
            if (hasGallery) {
                IconButton(onClick = onGallery) {
                    Icon(Icons.Default.Fullscreen, contentDescription = stringResource(R.string.detail_open_gallery))
                }
            }
        }
    }
}

@Composable
private fun DetailInfoCard(title: String, value: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = GrabHubShapes.medium,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(title, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
