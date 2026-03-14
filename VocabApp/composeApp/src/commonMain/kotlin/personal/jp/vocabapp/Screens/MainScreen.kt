package personal.jp.vocabapp.Screens

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SortByAlpha
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import personal.jp.vocabapp.viewmodels.WordScreen
import personal.jp.vocabapp.viewmodels.WordWithTags
import co.touchlab.kermit.Logger

@Composable
fun MainScreen(wordsList: List<WordWithTags>){
    var searchQuery by remember { mutableStateOf("") }
    var selectedMode by remember { mutableStateOf(SearchMode.Content) }
    val filteredList = remember(searchQuery, wordsList, selectedMode) {
        if (searchQuery.isBlank()) wordsList
        else {
            wordsList.filter { item ->
                when (selectedMode) {
                    // Content Mode
                    SearchMode.Content -> {
                        item.word.name.contains(searchQuery, ignoreCase = true) ||
                                item.word.meaningKr.contains(searchQuery)
                    }
                    // Tag Mode 
                    SearchMode.Tags -> {
                        item.tags.any { tag -> tag.tagName.contains(searchQuery, ignoreCase = true) }
                    }
                }
            }
        }
    }
    Scaffold(
        topBar = {
            Column(modifier = Modifier.background(MaterialTheme.colorScheme.background)) {
                VocabTopBar()
                SearchBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                )
                FilterSection(
                    selectedMode = selectedMode,
                    onModeChange = { selectedMode = it }
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { /* TODO */ },
                containerColor = Color(0xFF2D65FF),
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.padding(16.dp).size(60.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add", modifier = Modifier.size(30.dp))
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(filteredList, key = { it.word.name }) { item ->
                WordScreen(item.word.name)
            }
        }
    }
}

@Composable
fun VocabTopBar() {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // TODO Change Profile Icon if Signed in
            Icon(Icons.Default.AccountCircle, "Profile", tint = Color.White, modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Text("Vocab", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
        Icon(Icons.Default.Settings, "Settings", tint = Color.Gray)
    }
}

@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
) {
    TextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).clip(RoundedCornerShape(12.dp)),
        placeholder = { Text("Search words...", color = Color.Gray) },
        leadingIcon = { Icon(Icons.Default.Search, null, tint = Color.Gray) },
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color(0xFF1B202D),
            unfocusedContainerColor = Color(0xFF1B202D),
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            unfocusedTextColor = Color.White,
            focusedTextColor = Color.White
        ),
        singleLine = true
    )
}

enum class SearchMode { Content, Tags }
@Composable
fun FilterSection(
    selectedMode: SearchMode,
    onModeChange: (SearchMode) -> Unit
){
    // Animation Setting
    val tabWidth = 80.dp
    val indicatorOffset by animateDpAsState(
        targetValue = if (selectedMode == SearchMode.Content) 0.dp else tabWidth,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
    )

    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFF1B202D))
                .padding(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Search By: ",
                fontSize = 13.sp,
                modifier = Modifier.padding(start = 8.dp, end = 4.dp)
            )

            // Sliding Area
            Box {
                // Moving Box
                Box(
                    modifier = Modifier
                        .offset(x = indicatorOffset) // 애니메이션 좌표 적용
                        .size(width = tabWidth, height = 36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF2D65FF))
                )

                // Actual Buttons for Tab
                Row {
                    TabItem(
                        text = "Content",
                        isSelected = selectedMode == SearchMode.Content,
                        width = tabWidth,
                        onClick = { onModeChange(SearchMode.Content) }
                    )
                    TabItem(
                        text = "Tags",
                        isSelected = selectedMode == SearchMode.Tags,
                        width = tabWidth,
                        onClick = { onModeChange(SearchMode.Tags) }
                    )
                }
            }
        }
    }
}

@Composable
fun TabItem(text: String, isSelected: Boolean, width: Dp, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(width = width, height = 36.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (isSelected) Color.White else Color.Gray,
            fontSize = 14.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
        )
    }
}
