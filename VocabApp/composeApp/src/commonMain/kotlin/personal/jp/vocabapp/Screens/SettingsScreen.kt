package personal.jp.vocabapp.Screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.PersonOff
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.*
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import personal.jp.vocabapp.sql.sync

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
    onBackClick: () -> Unit,
    onLoginClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onSyncClick: () -> Unit,
    onCancelLogin: () -> Unit
) {
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