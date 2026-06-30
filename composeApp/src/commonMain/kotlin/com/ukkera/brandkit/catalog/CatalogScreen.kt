package com.ukkera.brandkit.catalog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ukkera.brandkit.components.BrandAvatar
import com.ukkera.brandkit.components.BrandAvatarShape
import com.ukkera.brandkit.components.BrandAvatarSize
import com.ukkera.brandkit.components.BrandBadge
import com.ukkera.brandkit.components.BrandBadgedBox
import com.ukkera.brandkit.components.BrandButton
import com.ukkera.brandkit.components.BrandCard
import com.ukkera.brandkit.components.BrandCardVariant
import com.ukkera.brandkit.components.BrandCheckbox
import com.ukkera.brandkit.components.BrandColorWell
import com.ukkera.brandkit.components.BrandDatePicker
import com.ukkera.brandkit.components.BrandOtpField
import com.ukkera.brandkit.components.BrandPopover
import com.ukkera.brandkit.components.BrandContentState
import com.ukkera.brandkit.components.BrandLoadState
import com.ukkera.brandkit.components.BrandSheet
import com.ukkera.brandkit.components.BrandSheetDetent
import com.ukkera.brandkit.components.BrandDialog
import com.ukkera.brandkit.components.BrandTopBar
import com.ukkera.brandkit.components.brandHeading
import com.ukkera.brandkit.components.BrandSwipeAction
import com.ukkera.brandkit.components.rememberBrandShare
import com.ukkera.brandkit.components.BrandChip
import com.ukkera.brandkit.components.BrandChipStyle
import com.ukkera.brandkit.components.BrandDivider
import com.ukkera.brandkit.components.BrandEmptyState
import com.ukkera.brandkit.components.BrandIconButton
import com.ukkera.brandkit.components.BrandListItem
import com.ukkera.brandkit.components.BrandListSection
import com.ukkera.brandkit.components.BrandPageControl
import com.ukkera.brandkit.components.BrandProgressIndicator
import com.ukkera.brandkit.components.BrandProgressKind
import com.ukkera.brandkit.components.BrandRadioGroup
import com.ukkera.brandkit.components.BrandRating
import com.ukkera.brandkit.components.BrandSearchBar
import com.ukkera.brandkit.components.BrandSearchBarIosOptions
import com.ukkera.brandkit.components.BrandSelectionStyle
import com.ukkera.brandkit.components.BrandSkeleton
import com.ukkera.brandkit.components.BrandSegmentedControl
import com.ukkera.brandkit.components.BrandSlider
import com.ukkera.brandkit.components.BrandSplitButton
import com.ukkera.brandkit.components.BrandStepper
import com.ukkera.brandkit.components.BrandText
import com.ukkera.brandkit.components.BrandTextField
import com.ukkera.brandkit.components.BrandToggle
import com.ukkera.brandkit.components.feedback.BrandAlertAction
import com.ukkera.brandkit.components.feedback.BrandAlertActionRole
import com.ukkera.brandkit.components.feedback.BrandAlertIosOptions
import com.ukkera.brandkit.components.feedback.BrandFeedbackPosition
import com.ukkera.brandkit.components.feedback.BrandFeedbackStatus
import com.ukkera.brandkit.components.feedback.BrandInlineStatus
import com.ukkera.brandkit.components.feedback.BrandPresentation
import com.ukkera.brandkit.components.feedback.BrandConfirmationSheetIosOptions
import com.ukkera.brandkit.components.feedback.BrandSheetAction
import com.ukkera.brandkit.components.feedback.BrandToastAndroidOptions
import com.ukkera.brandkit.components.feedback.LocalBrandFeedbackController
import com.ukkera.brandkit.components.model.BrandButtonShape
import com.ukkera.brandkit.components.model.BrandButtonSize
import com.ukkera.brandkit.components.model.BrandButtonVariant
import com.ukkera.brandkit.components.model.BrandCapitalization
import com.ukkera.brandkit.components.model.BrandCharacterLimit
import com.ukkera.brandkit.components.model.BrandCharacterLimitBehavior
import com.ukkera.brandkit.components.model.BrandClearButtonMode
import com.ukkera.brandkit.components.model.BrandFieldFocus
import com.ukkera.brandkit.components.model.BrandFieldInput
import com.ukkera.brandkit.components.model.BrandIcon
import com.ukkera.brandkit.components.model.BrandImeAction
import com.ukkera.brandkit.components.model.BrandKeyboardAccessory
import com.ukkera.brandkit.components.model.BrandKeyboardType
import com.ukkera.brandkit.components.model.BrandMenu
import com.ukkera.brandkit.components.model.BrandMenuItem
import com.ukkera.brandkit.components.model.BrandMenuItemRole
import com.ukkera.brandkit.components.model.BrandTextContentType
import com.ukkera.brandkit.components.model.BrandTextFieldIosOptions
import com.ukkera.brandkit.components.model.BrandTextStyle
import com.ukkera.brandkit.platform.platformName

/**
 * One shared catalog screen. Running it on Android shows Compose Material controls; running it on
 * iOS shows real UIKit controls — same code, native rendering per platform.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CatalogScreen(
    dark: Boolean,
    onToggleDark: (Boolean) -> Unit,
    rtl: Boolean,
    onToggleRtl: (Boolean) -> Unit,
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("secret123") }
    var badEmail by remember { mutableStateOf("not-an-email") }
    var search by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var limited by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }
    var wifiOn by remember { mutableStateOf(true) }
    var period by remember { mutableStateOf(0) }
    var volume by remember { mutableStateOf(0.4f) }
    var quantity by remember { mutableStateOf(2) }
    var showInlineError by remember { mutableStateOf(true) }
    // The shared feedback controller (mounted by BrandFeedbackHost in App). The existing button demos
    // keep their terse string feedback by routing this helper through controller.toast(...).
    val feedback = LocalBrandFeedbackController.current
    val toast: (String) -> Unit = { feedback.toast(it) }

    // Hosted in BrandShell's NavigationStack, which already provides the native "Catalog" nav bar — so NO
    // Compose topBar here (it would be a redundant second bar + a top gap). Zero content insets so the content
    // fills the screen and scrolls under the floating tab bar (the host applies `ignoresSafeArea(.bottom)`).
    Scaffold(contentWindowInsets = WindowInsets(0, 0, 0, 0)) { inner ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
                .imePadding() // keyboard avoidance: scroll viewport ends above the keyboard
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Text("NativeComposeKit — $platformName", style = MaterialTheme.typography.titleLarge)
            // ----- Theme / direction controls -----
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text("Dark")
                Switch(checked = dark, onCheckedChange = onToggleDark)
                Spacer(Modifier.width(16.dp))
                Text("RTL")
                Switch(checked = rtl, onCheckedChange = onToggleRtl)
            }

            // ----- BrandTextField -----
            Section("Text fields") {
                BrandTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = "Notes (multiline)",
                    placeholder = "Type a few lines…",
                    input = BrandFieldInput(singleLine = false, minLines = 3, maxLines = 6),
                    ios = BrandTextFieldIosOptions(keyboardAccessory = BrandKeyboardAccessory(doneButton = true)),
                    modifier = Modifier.fillMaxWidth(),
                )
                BrandTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = "Name",
                    placeholder = "Jane Doe",
                    input = BrandFieldInput(capitalization = BrandCapitalization.Words),
                    modifier = Modifier.fillMaxWidth(),
                )
                BrandTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = "Email",
                    placeholder = "you@example.com",
                    helperText = "We'll never share it.",
                    leadingIcon = BrandIcon(Icons.Default.Email, sfSymbolName = "envelope"),
                    input = BrandFieldInput(keyboardType = BrandKeyboardType.Email, imeAction = BrandImeAction.Done, autoCorrect = false),
                    focus = BrandFieldFocus(onSubmit = { /* dismiss / submit */ }),
                    contentType = BrandTextContentType.EmailAddress,
                    ios = BrandTextFieldIosOptions(
                        clearButton = BrandClearButtonMode.WhileEditing,
                        keyboardAccessory = BrandKeyboardAccessory(doneButton = true),
                    ),
                    testTag = "email",
                    modifier = Modifier.fillMaxWidth(),
                )
                BrandTextField(
                    value = badEmail,
                    onValueChange = { badEmail = it },
                    label = "Email",
                    errorText = "Invalid email format",
                    isError = true,
                    input = BrandFieldInput(keyboardType = BrandKeyboardType.Email),
                    modifier = Modifier.fillMaxWidth(),
                )
                BrandTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = "Password",
                    leadingIcon = BrandIcon(Icons.Default.Lock, sfSymbolName = "lock"),
                    input = BrandFieldInput(secure = true, imeAction = BrandImeAction.Done),
                    contentType = BrandTextContentType.Password,
                    modifier = Modifier.fillMaxWidth(),
                )
                BrandTextField(
                    value = limited,
                    onValueChange = { limited = it },
                    label = "Short code (enforce)",
                    placeholder = "max 10 chars",
                    helperText = "${limited.length}/10",
                    input = BrandFieldInput(
                        characterLimit = BrandCharacterLimit(max = 10, behavior = BrandCharacterLimitBehavior.Enforce),
                        capitalization = BrandCapitalization.Characters,
                        autoCorrect = false,
                    ),
                    modifier = Modifier.fillMaxWidth(),
                )
                BrandTextField(
                    value = bio,
                    onValueChange = { bio = it },
                    label = "Bio (warn only)",
                    placeholder = "A short bio",
                    helperText = "${bio.length}/20",
                    errorText = if (bio.length > 20) "Too long by ${bio.length - 20}" else null,
                    isError = bio.length > 20,
                    input = BrandFieldInput(
                        characterLimit = BrandCharacterLimit(max = 20, behavior = BrandCharacterLimitBehavior.WarnOnly),
                        singleLine = false,
                        minLines = 2,
                        maxLines = 4,
                    ),
                    ios = BrandTextFieldIosOptions(keyboardAccessory = BrandKeyboardAccessory(doneButton = true)),
                    modifier = Modifier.fillMaxWidth(),
                )
                BrandTextField(
                    value = "Read-only / disabled",
                    onValueChange = {},
                    label = "Disabled",
                    enabled = false,
                    modifier = Modifier.fillMaxWidth(),
                )
                BrandTextField(
                    value = search,
                    onValueChange = { search = it },
                    placeholder = "Search…",
                    leadingIcon = BrandIcon(Icons.Default.Search, sfSymbolName = "magnifyingglass"),
                    input = BrandFieldInput(imeAction = BrandImeAction.Search),
                    ios = BrandTextFieldIosOptions(clearButton = BrandClearButtonMode.WhileEditing),
                    testTag = "search",
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            // ----- BrandToggle -----
            Section("Toggle") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    BrandText("Wi-Fi", style = BrandTextStyle.Body)
                    BrandToggle(checked = wifiOn, onCheckedChange = { wifiOn = it })
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    BrandText("Disabled", style = BrandTextStyle.Body)
                    BrandToggle(checked = false, onCheckedChange = {}, enabled = false)
                }
            }

            // ----- BrandSegmentedControl -----
            Section("Segmented control") {
                val periods = listOf("Day", "Week", "Month")
                BrandSegmentedControl(
                    options = periods,
                    selectedIndex = period,
                    onSelectedIndexChange = { period = it },
                    modifier = Modifier.fillMaxWidth(),
                    contentDescription = "Time period",
                )
                BrandText("Selected: ${periods[period]}", style = BrandTextStyle.Label)
            }

            // ----- BrandSlider -----
            Section("Slider") {
                BrandSlider(
                    value = volume,
                    onValueChange = { volume = it },
                    modifier = Modifier.fillMaxWidth(),
                    contentDescription = "Volume",
                )
                BrandText("Value: ${(volume * 100).toInt()}%", style = BrandTextStyle.Label)
            }

            // ----- BrandStepper -----
            Section("Stepper") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    BrandText("Quantity: $quantity", style = BrandTextStyle.Body)
                    BrandStepper(value = quantity, onValueChange = { quantity = it }, min = 0, max = 10, contentDescription = "Quantity")
                }
            }

            // ----- BrandText -----
            Section("Text — roles") {
                BrandText("Display style", style = BrandTextStyle.Display)
                BrandText("Title style", style = BrandTextStyle.Title)
                BrandText(
                    "Body style — the quick brown fox jumps over the lazy dog.",
                    style = BrandTextStyle.Body,
                )
                BrandText("LABEL STYLE", style = BrandTextStyle.Label)
                BrandText(
                    "Colored from the theme (primary)",
                    style = BrandTextStyle.Body,
                    color = MaterialTheme.colorScheme.primary,
                )
                BrandText(
                    "Truncated to a single line with an ellipsis when it is too long to fit.",
                    style = BrandTextStyle.Body,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            // ----- BrandButton -----
            Section("Buttons — variants") {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    BrandButton("Primary", { toast("Clicked Primary") }, variant = BrandButtonVariant.Primary)
                    BrandButton("Secondary", { toast("Clicked Secondary") }, variant = BrandButtonVariant.Secondary)
                    BrandButton("Tertiary", { toast("Clicked Tertiary") }, variant = BrandButtonVariant.Tertiary)
                    BrandButton("Outline", { toast("Clicked Outline") }, variant = BrandButtonVariant.Outline)
                    BrandButton("Destructive", { toast("Clicked Destructive") }, variant = BrandButtonVariant.Destructive)
                }
            }

            Section("Buttons — sizes") {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    BrandButton("Small", { toast("Clicked Small") }, size = BrandButtonSize.Small)
                    BrandButton("Medium", { toast("Clicked Medium") }, size = BrandButtonSize.Medium)
                    BrandButton("Large", { toast("Clicked Large") }, size = BrandButtonSize.Large)
                }
            }

            Section("Buttons — states & icons") {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    BrandButton("Disabled", { toast("Clicked Disabled") }, enabled = false)
                    BrandButton("Loading", { toast("Clicked Loading") }, loading = true)
                    BrandButton(
                        "Leading",
                        { toast("Clicked Leading") },
                        leadingIcon = BrandIcon(Icons.Default.Add, sfSymbolName = "plus", contentDescription = "Add"),
                    )
                    BrandButton(
                        "Trailing",
                        { toast("Clicked Trailing") },
                        trailingIcon = BrandIcon(Icons.AutoMirrored.Filled.ArrowForward, sfSymbolName = "arrow.right"),
                    )
                    BrandButton(
                        "Both",
                        { toast("Clicked Both") },
                        leadingIcon = BrandIcon(Icons.Default.Add, sfSymbolName = "plus"),
                        trailingIcon = BrandIcon(Icons.AutoMirrored.Filled.ArrowForward, sfSymbolName = "arrow.right"),
                    )
                    BrandButton("Pill", { toast("Clicked Pill") }, shape = BrandButtonShape.Pill, variant = BrandButtonVariant.Secondary)
                }
                BrandButton("Full width", { toast("Clicked Full width") }, fullWidth = true)
            }

            // ----- BrandIconButton / pull-down menu / BrandSplitButton -----
            Section("Buttons — icon, menu & split") {
                val sampleMenu = BrandMenu(
                    items = listOf(
                        BrandMenuItem("Edit", { toast("Selected Edit") }, icon = BrandIcon(Icons.Default.Edit, sfSymbolName = "pencil")),
                        // selected = true shows a native checkmark (iOS UIMenu .on / Android trailing check).
                        BrandMenuItem("Share", { toast("Selected Share") }, icon = BrandIcon(Icons.Default.Share, sfSymbolName = "square.and.arrow.up"), selected = true),
                        BrandMenuItem(
                            "Delete",
                            { toast("Selected Delete") },
                            icon = BrandIcon(Icons.Default.Delete, sfSymbolName = "trash"),
                            role = BrandMenuItemRole.Destructive,
                        ),
                    ),
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    BrandIconButton(
                        BrandIcon(Icons.Default.Add, sfSymbolName = "plus"),
                        { toast("Clicked Add") },
                        contentDescription = "Add",
                        variant = BrandButtonVariant.Primary,
                    )
                    BrandIconButton(
                        BrandIcon(Icons.Default.Edit, sfSymbolName = "pencil"),
                        { toast("Clicked Edit") },
                        contentDescription = "Edit",
                        variant = BrandButtonVariant.Outline,
                    )
                    BrandIconButton(
                        BrandIcon(Icons.Default.MoreVert, sfSymbolName = "ellipsis"),
                        { toast("Opened more") },
                        contentDescription = "More",
                        menu = sampleMenu,
                    )
                }
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    BrandButton("Export", { toast("Opened Export") }, menu = sampleMenu)
                    BrandSplitButton(
                        "Save",
                        onPrimaryClick = { toast("Split primary clicked") },
                        menu = sampleMenu,
                        leadingIcon = BrandIcon(Icons.Default.Add, sfSymbolName = "plus"),
                    )
                }
            }

            // ----- BrandCard / BrandListSection / BrandListItem / BrandDivider -----
            Section("Cards & lists") {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    BrandCard(
                        variant = BrandCardVariant.Elevated,
                        onClick = { toast("Open One Piece") },
                        modifier = Modifier.width(150.dp),
                    ) {
                        BrandText("One Piece", style = BrandTextStyle.Title)
                        BrandText("Eiichiro Oda · Ch. 1042", style = BrandTextStyle.Label)
                    }
                    BrandCard(variant = BrandCardVariant.Outlined, modifier = Modifier.width(150.dp)) {
                        BrandText("Outlined", style = BrandTextStyle.Body)
                        BrandText("Quiet separation", style = BrandTextStyle.Label)
                    }
                }
                BrandListSection(
                    header = "Reader settings",
                    rows = listOf(
                        {
                            BrandListItem(
                                "Reading direction",
                                trailingText = "Left → Right",
                                onClick = { toast("Reading direction") },
                            )
                        },
                        {
                            BrandListItem(
                                "New-chapter alerts",
                                supporting = "Notify when a followed series updates",
                                trailing = { BrandToggle(checked = wifiOn, onCheckedChange = { wifiOn = it }) },
                            )
                        },
                        {
                            BrandListItem(
                                "Download quality",
                                trailingText = "High",
                                onClick = { toast("Download quality") },
                            )
                        },
                    ),
                )
                BrandDivider()
                BrandListItem("About", supporting = "Version 1.0.0", onClick = { toast("About") })
            }

            // ----- BrandChip / BrandAvatar / BrandBadge / BrandRating -----
            Section("Chips, avatars, badges & rating") {
                val genres = listOf("Action", "Romance", "Comedy", "Horror")
                var selectedGenres by remember { mutableStateOf(setOf("Action")) }
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    genres.forEach { g ->
                        BrandChip(
                            label = g,
                            style = BrandChipStyle.Filter,
                            selected = g in selectedGenres,
                            onClick = {
                                selectedGenres = if (g in selectedGenres) selectedGenres - g else selectedGenres + g
                            },
                        )
                    }
                    BrandChip(
                        label = "Ongoing",
                        style = BrandChipStyle.Input,
                        selected = true,
                        trailingIcon = Icons.Default.Close,
                        onTrailingClick = { toast("Removed filter") },
                    )
                }
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    BrandAvatar(initials = "JD")
                    BrandAvatar(initials = "AK", size = BrandAvatarSize.Large)
                    BrandAvatar(icon = Icons.Default.Edit, shape = BrandAvatarShape.Rounded)
                    BrandBadgedBox(badge = { BrandBadge(count = 5) }) {
                        BrandAvatar(initials = "MX")
                    }
                }
                var stars by remember { mutableStateOf(3f) }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    BrandText("Score 4.5", style = BrandTextStyle.Label)
                    BrandRating(4.5f)
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    BrandText("Rate it", style = BrandTextStyle.Label)
                    BrandRating(stars, onRatingChange = { stars = it })
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    BrandText("Disabled", style = BrandTextStyle.Label)
                    BrandRating(3.5f, onRatingChange = { stars = it }, enabled = false) // dimmed + read-only
                }
            }

            // ----- BrandCheckbox / BrandRadioGroup / BrandSkeleton / BrandEmptyState -----
            Section("Selection & states") {
                var downloadHd by remember { mutableStateOf(true) }
                var hideSpoilers by remember { mutableStateOf(false) }
                BrandCheckbox(checked = downloadHd, onCheckedChange = { downloadHd = it }, label = "Download in HD")
                BrandCheckbox(checked = hideSpoilers, onCheckedChange = { hideSpoilers = it }, label = "Hide spoilers in comments")

                BrandText("Image quality", style = BrandTextStyle.Label)
                val qualities = listOf("Data saver", "Standard", "High")
                var quality by remember { mutableStateOf("Standard") }
                BrandRadioGroup(options = qualities, selected = quality, onSelectedChange = { quality = it })

                BrandText("Sort by (iOS checkmark style)", style = BrandTextStyle.Label)
                val sorts = listOf("Latest", "A–Z", "Popularity")
                var sort by remember { mutableStateOf("Latest") }
                BrandRadioGroup(
                    options = sorts,
                    selected = sort,
                    onSelectedChange = { sort = it },
                    style = BrandSelectionStyle.Checkmark,
                )

                BrandText("Loading (skeletons)", style = BrandTextStyle.Label)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    repeat(3) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            BrandSkeleton(Modifier.size(96.dp, 132.dp))
                            BrandSkeleton(Modifier.width(96.dp).height(12.dp))
                        }
                    }
                }

                BrandEmptyState(
                    title = "Your library is empty",
                    message = "Browse and add series to see them here.",
                    icon = Icons.Default.Search,
                    actionLabel = "Browse",
                    onAction = { toast("Browse") },
                )
            }

            // ----- BrandSearchBar / BrandProgressIndicator / BrandPageControl (native-backed) -----
            Section("Progress, search & paging") {
                var query by remember { mutableStateOf("") }
                BrandSearchBar(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = "Search manga…",
                    onSearch = { toast("Search: $query") },
                    ios = BrandSearchBarIosOptions(showCancelButton = true),
                    modifier = Modifier.fillMaxWidth(),
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    BrandProgressIndicator()
                    BrandText("Loading…", style = BrandTextStyle.Label)
                }
                var downloaded by remember { mutableStateOf(0.4f) }
                BrandText("Download ${(downloaded * 100).toInt()}%", style = BrandTextStyle.Label)
                BrandProgressIndicator(
                    kind = BrandProgressKind.Linear,
                    progress = downloaded,
                    modifier = Modifier.fillMaxWidth(),
                )
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    BrandButton("-10%", { downloaded = (downloaded - 0.1f).coerceIn(0f, 1f) }, variant = BrandButtonVariant.Outline, size = BrandButtonSize.Small)
                    BrandButton("+10%", { downloaded = (downloaded + 0.1f).coerceIn(0f, 1f) }, variant = BrandButtonVariant.Outline, size = BrandButtonSize.Small)
                }
                // iOS Compose-fallback combos (no native control): determinate circular + indeterminate linear.
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    BrandProgressIndicator(kind = BrandProgressKind.Circular, progress = downloaded)
                    BrandProgressIndicator(kind = BrandProgressKind.Linear, modifier = Modifier.width(120.dp))
                    BrandText("ring + looping bar", style = BrandTextStyle.Label)
                }
                var page by remember { mutableStateOf(0) }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    BrandPageControl(pageCount = 5, currentPage = page, onCurrentPageChange = { page = it })
                    BrandButton("Next", { page = (page + 1) % 5 }, variant = BrandButtonVariant.Tertiary, size = BrandButtonSize.Small)
                }
            }

            // ----- BrandDatePicker / ColorWell / Otp / Image / Share / Sheet / Popover / list swipe -----
            Section("Pickers, sheets, share & media") {
                var date by remember { mutableStateOf<Long?>(null) }
                BrandText("Date picker", style = BrandTextStyle.Label)
                BrandDatePicker(selectedMillis = date, onSelectedMillisChange = { date = it })

                var highlight by remember { mutableStateOf(Color(0xFF1E88E5)) }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    BrandText("Highlight color", style = BrandTextStyle.Label)
                    BrandColorWell(color = highlight, onColorChange = { highlight = it })
                }

                var otp by remember { mutableStateOf("") }
                BrandText("Verification code", style = BrandTextStyle.Label)
                BrandOtpField(value = otp, onValueChange = { otp = it }, length = 6, onFilled = { toast("Code: $it") })

                val share = rememberBrandShare()
                var sheetOpen by remember { mutableStateOf(false) }
                var popoverOpen by remember { mutableStateOf(false) }
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    BrandButton(
                        "Share",
                        { share.share(text = "Check out One Piece", url = "https://example.com/one-piece") },
                        variant = BrandButtonVariant.Outline,
                    )
                    BrandButton("Open sheet", { sheetOpen = true }, variant = BrandButtonVariant.Secondary)
                    BrandPopover(
                        visible = popoverOpen,
                        onDismissRequest = { popoverOpen = false },
                        anchor = {
                            BrandButton("Popover", { popoverOpen = true }, variant = BrandButtonVariant.Tertiary)
                        },
                    ) {
                        BrandText("Quick info", style = BrandTextStyle.Body)
                        BrandText("Native UIPopoverPresentationController on iOS; a Compose Popup on Android.", style = BrandTextStyle.Label)
                    }
                }

                BrandListItem(
                    "Chapter 1043",
                    supporting = "Swipe ← to archive · long-press for options",
                    onClick = { toast("Open chapter 1043") },
                    onLongClick = { toast("Long-pressed Chapter 1043") },
                    swipeAction = BrandSwipeAction(
                        label = "Archive",
                        onAction = { toast("Archived 1043") },
                        icon = Icons.Default.Delete,
                    ),
                )

                BrandSheet(
                    visible = sheetOpen,
                    onDismissRequest = { sheetOpen = false },
                    detents = listOf(BrandSheetDetent.Medium, BrandSheetDetent.Large),
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        BrandText("Chapter options", style = BrandTextStyle.Title)
                        BrandListItem("Download", onClick = { toast("Download"); sheetOpen = false })
                        BrandListItem("Mark as read", onClick = { toast("Marked read"); sheetOpen = false })
                        BrandButton("Close", { sheetOpen = false }, fullWidth = true)
                    }
                }
            }

            // ----- BrandFeedback: transient messages (toast / snackbar) -----
            Section("Feedback — toast & snackbar") {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    BrandButton("Toast", { feedback.toast("Saved") })
                    BrandButton(
                        "Success",
                        { feedback.toast("Uploaded", status = BrandFeedbackStatus.Success) },
                        variant = BrandButtonVariant.Secondary,
                    )
                    BrandButton(
                        "Error",
                        { feedback.toast("Upload failed", status = BrandFeedbackStatus.Error) },
                        variant = BrandButtonVariant.Destructive,
                    )
                    BrandButton(
                        "System toast",
                        { feedback.toast("Native system toast", android = BrandToastAndroidOptions(useSystemToast = true)) },
                        variant = BrandButtonVariant.Outline,
                    )
                    BrandButton(
                        "Snackbar",
                        { feedback.snackbar("Message sent") },
                        variant = BrandButtonVariant.Outline,
                    )
                    BrandButton(
                        "Undo snackbar",
                        {
                            feedback.snackbar(
                                "Item deleted",
                                actionLabel = "Undo",
                                onAction = { feedback.toast("Restored", status = BrandFeedbackStatus.Success) },
                            )
                        },
                        variant = BrandButtonVariant.Outline,
                    )
                    BrandButton(
                        "Fire 3 (FIFO)",
                        {
                            feedback.toast("First")
                            feedback.toast("Second", status = BrandFeedbackStatus.Success)
                            feedback.toast("Third", status = BrandFeedbackStatus.Warning)
                        },
                        variant = BrandButtonVariant.Tertiary,
                    )
                }
            }

            // ----- BrandFeedback: banners -----
            Section("Feedback — banners") {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    BrandButton("Top info", {
                        feedback.banner(
                            "Syncing your data…",
                            title = "Sync",
                            status = BrandFeedbackStatus.Info,
                            position = BrandFeedbackPosition.Top,
                        )
                    })
                    BrandButton(
                        "Success + action",
                        {
                            feedback.banner(
                                "Account updated",
                                status = BrandFeedbackStatus.Success,
                                position = BrandFeedbackPosition.Top,
                                actionLabel = "View",
                                onAction = { feedback.toast("Viewing account") },
                            )
                        },
                        variant = BrandButtonVariant.Secondary,
                    )
                    BrandButton(
                        "Bottom warning",
                        {
                            feedback.banner(
                                "You are offline",
                                status = BrandFeedbackStatus.Warning,
                                position = BrandFeedbackPosition.Bottom,
                            )
                        },
                        variant = BrandButtonVariant.Outline,
                    )
                }
            }

            // ----- BrandFeedback: alert & confirmation sheet (native by default, branded opt-in) -----
            Section("Feedback — alert & sheet") {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    BrandButton("Alert", {
                        feedback.alert(
                            title = "Unsaved changes",
                            message = "Discard your edits?",
                            actions = listOf(
                                BrandAlertAction("Discard", { toast("Discarded") }, role = BrandAlertActionRole.Destructive),
                                BrandAlertAction("Keep editing", role = BrandAlertActionRole.Cancel),
                            ),
                        )
                    })
                    BrandButton(
                        "Branded alert",
                        {
                            feedback.alert(
                                title = "Delete item?",
                                message = "This cannot be undone.",
                                actions = listOf(
                                    BrandAlertAction("Delete", { toast("Deleted") }, role = BrandAlertActionRole.Destructive),
                                    BrandAlertAction("Cancel", role = BrandAlertActionRole.Cancel),
                                ),
                                ios = BrandAlertIosOptions(presentation = BrandPresentation.Branded),
                            )
                        },
                        variant = BrandButtonVariant.Outline,
                    )
                    BrandButton(
                        "Action sheet",
                        {
                            feedback.confirmationSheet(
                                title = "Export as",
                                actions = listOf(
                                    BrandSheetAction("PDF", { toast("Export PDF") }, icon = BrandIcon(Icons.Default.Share, sfSymbolName = "doc")),
                                    BrandSheetAction("CSV", { toast("Export CSV") }, icon = BrandIcon(Icons.Default.Edit, sfSymbolName = "tablecells")),
                                    BrandSheetAction("Delete", { toast("Deleted") }, role = BrandAlertActionRole.Destructive, icon = BrandIcon(Icons.Default.Delete, sfSymbolName = "trash")),
                                    BrandSheetAction("Cancel", role = BrandAlertActionRole.Cancel),
                                ),
                            )
                        },
                        variant = BrandButtonVariant.Secondary,
                    )
                    BrandButton(
                        "Branded sheet",
                        {
                            feedback.confirmationSheet(
                                title = "Choose source",
                                message = "Where should we get the image?",
                                actions = listOf(
                                    BrandSheetAction("Camera", { toast("Camera") }),
                                    BrandSheetAction("Photo Library", { toast("Library") }),
                                    BrandSheetAction("Cancel", role = BrandAlertActionRole.Cancel),
                                ),
                                ios = BrandConfirmationSheetIosOptions(presentation = BrandPresentation.Branded),
                            )
                        },
                        variant = BrandButtonVariant.Outline,
                    )
                }
            }

            // ----- BrandInlineStatus: in-flow (not a floating overlay) -----
            Section("Feedback — inline status") {
                BrandInlineStatus("Saved locally — will sync when you're back online.", status = BrandFeedbackStatus.Info)
                BrandInlineStatus("Profile updated successfully.", title = "Done", status = BrandFeedbackStatus.Success)
                BrandInlineStatus(
                    "Your free trial ends in 3 days.",
                    status = BrandFeedbackStatus.Warning,
                    filled = false,
                    actionLabel = "Upgrade",
                    onAction = { feedback.toast("Upgrade") },
                )
                if (showInlineError) {
                    BrandInlineStatus(
                        "Couldn't load comments.",
                        status = BrandFeedbackStatus.Error,
                        actionLabel = "Retry",
                        onAction = { feedback.toast("Retrying…", status = BrandFeedbackStatus.Info) },
                        onDismiss = { showInlineError = false },
                    )
                } else {
                    BrandButton("Reset inline error", { showInlineError = true }, variant = BrandButtonVariant.Tertiary)
                }
            }

            // ----- Structural components (T7): standalone top bar + content-state lifecycle -----
            Section("Structural — top bar & content state") {
                BrandTopBar(
                    title = "Library",
                    subtitle = "Standalone BrandTopBar (decoupled from nav)",
                    actions = {
                        BrandIconButton(
                            BrandIcon(Icons.Default.Add, sfSymbolName = "plus"),
                            { toast("Add") },
                            contentDescription = "Add",
                        )
                    },
                )
                var phase by remember { mutableStateOf(0) }
                val loadState: BrandLoadState<String> = when (phase % 4) {
                    0 -> BrandLoadState.Loading
                    1 -> BrandLoadState.Empty
                    2 -> BrandLoadState.Error("Couldn't load the library.")
                    else -> BrandLoadState.Content("Loaded 42 series")
                }
                BrandButton(
                    "Cycle content state",
                    { phase++ },
                    variant = BrandButtonVariant.Outline,
                    size = BrandButtonSize.Small,
                )
                BrandContentState(
                    state = loadState,
                    modifier = Modifier.fillMaxWidth().height(160.dp),
                    onRetry = { phase = 3 },
                    emptyTitle = "Your library is empty",
                    emptyMessage = "Add a series to see it here.",
                ) { msg ->
                    BrandText(msg, style = BrandTextStyle.Body)
                }
            }

            // ----- In-content selection (native segmented) + custom-content dialog -----
            // BrandTabBar and BrandTooltip were deprecated (kit thesis: native-per-platform). In-content tabs
            // now use the native BrandSegmentedControl (a real UISegmentedControl on iOS); iOS has no tooltip idiom.
            Section("Segmented tabs & custom-content dialog") {
                var tab by remember { mutableStateOf(0) }
                BrandSegmentedControl(
                    options = listOf("Overview", "Chapters", "Comments"),
                    selectedIndex = tab,
                    onSelectedIndexChange = { tab = it },
                    modifier = Modifier.fillMaxWidth(),
                    contentDescription = "Section tabs",
                )
                BrandText("Selected tab ${tab + 1}", style = BrandTextStyle.Label, modifier = Modifier.brandHeading())

                var dialogOpen by remember { mutableStateOf(false) }
                BrandButton("Open custom dialog", { dialogOpen = true }, variant = BrandButtonVariant.Outline)
                if (dialogOpen) {
                    BrandDialog(
                        onDismissRequest = { dialogOpen = false },
                        title = "Custom dialog",
                        actions = {
                            BrandButton("Cancel", { dialogOpen = false }, variant = BrandButtonVariant.Tertiary, size = BrandButtonSize.Small)
                            BrandButton("OK", { dialogOpen = false }, size = BrandButtonSize.Small)
                        },
                    ) {
                        BrandText("Arbitrary content lives here — a form, a list, anything you compose.", style = BrandTextStyle.Body)
                    }
                }
            }
        }
    }
}

@Composable
private fun Section(title: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        BrandText(title, style = BrandTextStyle.Title)
        content()
    }
}
