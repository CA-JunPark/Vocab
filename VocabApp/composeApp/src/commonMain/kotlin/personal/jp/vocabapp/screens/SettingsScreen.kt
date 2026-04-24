package personal.jp.vocabapp.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.*
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.window.DialogProperties
import db.Tag
import kotlinx.coroutines.launch
import personal.jp.vocabapp.sql.WordServiceImpl

data class UserProfile(
    val name: String,
    val email: String,
    val profileImageUrl: String? = null
)

@Composable
fun SettingsScreen(
    userProfile: UserProfile?,
    isLoginInProgress: Boolean,
    hasPendingChanges: Boolean,
    isSyncing: Boolean,
    wordService: WordServiceImpl,
    onBackClick: () -> Unit,
    onLoginClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onSyncClick: () -> Unit,
    onCancelLogin: () -> Unit,
    onDeleteTagsComplete: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var showDeleteTagsDialog by remember { mutableStateOf(false) }
    var unusedTags by remember { mutableStateOf(emptyList<Tag>()) }

    Scaffold(
        topBar = {
            SettingsTopBar(onBackClick)
        },
        containerColor = Color(0xFF0F1219)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // --- ACCOUNT SECTION ---
            SettingsSection(title = "ACCOUNT") {
                when {
                    isLoginInProgress -> {
                        SignInInProgressCard(onCancelClick = onCancelLogin)
                    }
                    userProfile != null -> {
                        LoggedInAccountCard(userProfile, onLogoutClick)
                    }
                    else -> {
                        LoggedOutAccountCard(onLoginClick)
                    }
                }
            }

            // --- DATA MANAGEMENT SECTION ---
            SettingsSection(title = "DATA MANAGEMENT") {
                SyncDataCard(
                    isLoggedIn = userProfile != null,
                    hasPendingChanges = hasPendingChanges,
                    onSyncClick = onSyncClick
                )
            }

            // --- TAG MANAGEMENT SECTION ---
            SettingsSection(title = "TAG MANAGEMENT") {
                DeleteUnusedTagsCard(
                    onDeleteClick = {
                        scope.launch {
                            unusedTags = wordService.getUnusedTags()
                            showDeleteTagsDialog = true
                        }
                    }
                )
            }

            // Dialogs
            if (isSyncing) {
                SyncInProgressDialog()
            }

            if (showDeleteTagsDialog) {
                DeleteUnusedTagsDialog(
                    tags = unusedTags,
                    onDismiss = { showDeleteTagsDialog = false },
                    onConfirm = {
                        scope.launch {
                            wordService.deleteUnusedTags()
                            showDeleteTagsDialog = false
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun SettingsTopBar(onBackClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBackClick) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = Color.White
            )
        }
        Text(
            text = "Settings",
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun SettingsSection(title: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = title,
            color = Color(0xFF626978),
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        )
        content()
    }
}

@Composable
fun SignInInProgressCard(onCancelClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1B202D))
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(32.dp),
                color = Color(0xFF2D65FF),
                strokeWidth = 3.dp
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Sign in in progress",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Please complete the login in your browser",
                color = Color(0xFF9BA1B0),
                fontSize = 13.sp,
                modifier = Modifier.padding(top = 4.dp)
            )
            Spacer(modifier = Modifier.height(20.dp))
            // Cancel Button
            Button(
                onClick = onCancelClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF2B313E),
                    contentColor = Color.White
                )
            ) {
                Text("Cancel Login", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
fun LoggedInAccountCard(user: UserProfile, onLogoutClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1B202D))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Profile image
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE5D1C1)),
                    contentAlignment = Alignment.Center
                ) {
                    if (user.profileImageUrl != null) {
                        AsyncImage(
                            model = user.profileImageUrl,
                            contentDescription = "Profile Image",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = null,
                            modifier = Modifier.size(40.dp),
                            tint = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                // name and email
                Column {
                    Text(
                        text = user.name,
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = user.email,
                        color = Color(0xFF9BA1B0),
                        fontSize = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Logout button
            Button(
                onClick = onLogoutClick,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2B313E))
            ) {
                Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Logout", color = Color.White)
            }
        }
    }
}

@Composable
fun LoggedOutAccountCard(onLoginClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1B202D))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF2B313E)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = null,
                        modifier = Modifier.size(40.dp),
                        tint = Color.White
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text("Not Logged In", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text("Log in to save your progress", color = Color(0xFF9BA1B0), fontSize = 14.sp)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Login with Google button
            Button(
                onClick = onLoginClick,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2D65FF))
            ) {
                // TODO replace to Goolge logo
                Text("G", fontWeight = FontWeight.Black, modifier = Modifier.padding(end = 8.dp))
                Text("Login with Google", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun SyncDataCard(
    isLoggedIn: Boolean,
    hasPendingChanges: Boolean,
    onSyncClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1B202D))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF232938)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isLoggedIn) Icons.Default.Sync else Icons.Default.CloudOff,
                    contentDescription = null,
                    tint = if (isLoggedIn) Color(0xFF2D65FF) else Color(0xFF626978)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isLoggedIn) "Sync" else "Sync Disabled",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = when {
                        !isLoggedIn -> "Please login to sync your data"
                        hasPendingChanges -> "Pending changes detected"
                        else -> "All data is up to date"
                    },
                    color = Color(0xFF9BA1B0),
                    fontSize = 13.sp
                )
            }

            TextButton(
                onClick = onSyncClick,
                enabled = isLoggedIn && hasPendingChanges,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = Color(0xFF2D65FF),
                    disabledContentColor = Color(0xFF626978)
                )
            ) {
                Text(
                    text = "Sync Now",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun DeleteUnusedTagsCard(onDeleteClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1B202D))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Delete Unused Tags", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text("Remove tags not assigned to any words", color = Color(0xFF9BA1B0), fontSize = 13.sp)
            }
            IconButton(
                onClick = onDeleteClick,
                colors = IconButtonDefaults.iconButtonColors(containerColor = Color(0xFF2B313E))
            ) {
                Icon(Icons.Default.DeleteOutline, contentDescription = null, tint = Color(0xFFFF5252))
            }
        }
    }
}

@Composable
fun DeleteUnusedTagsDialog(
    tags: List<Tag>,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        tonalElevation = 0.dp,
        containerColor = Color(0xFF1B202D),
        shape = RoundedCornerShape(28.dp),
        icon = {
            Box(
                modifier = Modifier.size(56.dp).clip(CircleShape).background(Color(0xFF2B313E)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.DeleteOutline, contentDescription = null, tint = Color(0xFFFF5252), modifier = Modifier.size(28.dp))
            }
        },
        title = {
            Text("Delete Unused Tags?", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 22.sp)
        },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "The following tags are not assigned to any words and will be permanently removed.",
                    color = Color(0xFF9BA1B0),
                    fontSize = 14.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                Spacer(modifier = Modifier.height(20.dp))
                // 태그 칩 리스트
                FlowRow(
                    horizontalArrangement = Arrangement.Center,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    tags.forEach { tag ->
                        Surface(
                            color = Color(0xFF2B313E),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.padding(horizontal = 4.dp)
                        ) {
                            Text(
                                text = tag.tagName,
                                color = Color(0xFF9BA1B0),
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5252)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Delete Tags", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                Text("Cancel", color = Color(0xFF9BA1B0), fontWeight = FontWeight.Medium)
            }
        }
    )
}

@Composable
fun SyncInProgressDialog() {
    AlertDialog(
        onDismissRequest = { },
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false),
        containerColor = Color(0xFF1B202D),
        shape = RoundedCornerShape(28.dp),
        confirmButton = { },
        icon = {
            CircularProgressIndicator(
                modifier = Modifier.size(48.dp),
                color = Color(0xFF2D65FF),
                strokeWidth = 4.dp
            )
        },
        title = {
            Text(
                "Syncing Data...",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        },
        text = {
            Text(
                "Please wait while we synchronize your vocabulary with the server.",
                color = Color(0xFF9BA1B0),
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    )
}