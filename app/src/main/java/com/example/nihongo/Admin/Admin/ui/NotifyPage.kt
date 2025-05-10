package com.example.nihongo.Admin.ui

import Campaign
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.work.*
import com.example.nihongo.Admin.viewmodel.AdminNotifyPageViewModel
import com.example.nihongo.Admin.viewmodel.CampaignWorker
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotifyPage(viewModel: AdminNotifyPageViewModel = AdminNotifyPageViewModel()) {
    var title by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var imageUrl by remember { mutableStateOf("") }
    var isScheduled by remember { mutableStateOf(false) }
    var isDaily by remember { mutableStateOf(false) }
    var scheduledDate by remember { mutableStateOf<Date?>(null) }
    var dailyHour by remember { mutableStateOf(9) } // Default 9 AM
    var dailyMinute by remember { mutableStateOf(0) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var showDailyTimePicker by remember { mutableStateOf(false) }
    var activeTab by remember { mutableStateOf(0) }

    val campaigns by viewModel.campaigns.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    val context = LocalContext.current
    val calendar = Calendar.getInstance()

    val dateFormatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    val timeFormatter = SimpleDateFormat("HH:mm", Locale.getDefault())

    // Time pickers
    if (showDatePicker) {
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                calendar.set(year, month, dayOfMonth)
                scheduledDate = calendar.time
                showDatePicker = false
                showTimePicker = true
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    if (showTimePicker) {
        TimePickerDialog(
            context,
            { _, hourOfDay, minute ->
                calendar.apply {
                    set(Calendar.HOUR_OF_DAY, hourOfDay)
                    set(Calendar.MINUTE, minute)
                    set(Calendar.SECOND, 0)
                }
                scheduledDate = calendar.time
                showTimePicker = false
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            true
        ).show()
    }

    if (showDailyTimePicker) {
        TimePickerDialog(
            context,
            { _, hourOfDay, minute ->
                dailyHour = hourOfDay
                dailyMinute = minute
                showDailyTimePicker = false
            },
            dailyHour,
            dailyMinute,
            true
        ).show()
    }

    Scaffold(
        topBar = {
            TabRow(
                selectedTabIndex = activeTab,
                modifier = Modifier.padding(top = 46.dp)
            ) {
                Tab(
                    selected = activeTab == 0,
                    onClick = { activeTab = 0 },
                    text = { Text("Create Notification") }
                )
                Tab(
                    selected = activeTab == 1,
                    onClick = { activeTab = 1 },
                    text = { Text("Scheduled Campaigns") }
                )
            }
        }
    ) { paddingValues ->
        when (activeTab) {
            0 -> {
                // Create Notification Tab
                Column(
                    modifier = Modifier
                        .padding(paddingValues)
                        .padding(16.dp)
                        .fillMaxSize()
                ) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Title") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = message,
                        onValueChange = { message = it },
                        label = { Text("Message") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = imageUrl,
                        onValueChange = { imageUrl = it },
                        label = { Text("Image URL (optional)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // Notification Type Selection
                    Text(
                        "Notification Type",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = !isScheduled && !isDaily,
                            onClick = {
                                isScheduled = false
                                isDaily = false
                            }
                        )
                        Text("Send immediately")
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = isScheduled,
                            onClick = {
                                isScheduled = true
                                isDaily = false
                                if (scheduledDate == null) {
                                    showDatePicker = true
                                }
                            }
                        )
                        Text("Schedule for later")
                        Spacer(modifier = Modifier.width(8.dp))
                        if (isScheduled) {
                            OutlinedButton(onClick = { showDatePicker = true }) {
                                Text(scheduledDate?.let { dateFormatter.format(it) + " " + timeFormatter.format(it) } ?: "Select date & time")
                            }
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = isDaily,
                            onClick = {
                                isDaily = true
                                isScheduled = false
                                showDailyTimePicker = true
                            }
                        )
                        Text("Send daily")
                        Spacer(modifier = Modifier.width(8.dp))
                        if (isDaily) {
                            OutlinedButton(onClick = { showDailyTimePicker = true }) {
                                Text("${String.format("%02d", dailyHour)}:${String.format("%02d", dailyMinute)}")
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            val campaign = Campaign(
                                title = title,
                                message = message,
                                imageUrl = if (imageUrl.isBlank()) null else imageUrl,
                                isScheduled = isScheduled,
                                isDaily = isDaily,
                                scheduledFor = scheduledDate?.let { Timestamp(it) },
                                dailyHour = dailyHour,
                                dailyMinute = dailyMinute
                            )

                            if (isScheduled && scheduledDate != null) {
                                // Save to Firebase first
                                viewModel.saveCampaign(context, campaign)

                                // Schedule the work
                                val now = System.currentTimeMillis()
                                val scheduledTime = scheduledDate!!.time
                                val delay = scheduledTime - now

                                if (delay > 0) {
                                    val workRequest = OneTimeWorkRequestBuilder<CampaignWorker>()
                                        .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                                        .setInputData(workDataOf("campaignId" to campaign.id))
                                        .build()

                                    WorkManager.getInstance(context)
                                        .enqueueUniqueWork(
                                            campaign.id,
                                            ExistingWorkPolicy.REPLACE,
                                            workRequest
                                        )
                                }
                            } else if (isDaily) {
                                // Save to Firebase first
                                viewModel.saveCampaign(context, campaign)

                                // Calculate initial delay to the next occurrence of the specified time
                                val nowCalendar = Calendar.getInstance()
                                val targetCalendar = Calendar.getInstance()
                                targetCalendar.set(Calendar.HOUR_OF_DAY, dailyHour)
                                targetCalendar.set(Calendar.MINUTE, dailyMinute)
                                targetCalendar.set(Calendar.SECOND, 0)

                                if (targetCalendar.before(nowCalendar)) {
                                    // If the target time is before current time, add a day
                                    targetCalendar.add(Calendar.DAY_OF_MONTH, 1)
                                }

                                val initialDelay = targetCalendar.timeInMillis - nowCalendar.timeInMillis

                                val dailyWork = PeriodicWorkRequestBuilder<CampaignWorker>(24, TimeUnit.HOURS)
                                    .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
                                    .setInputData(workDataOf("campaignId" to campaign.id))
                                    .build()

                                WorkManager.getInstance(context)
                                    .enqueueUniquePeriodicWork(
                                        campaign.id,
                                        ExistingPeriodicWorkPolicy.REPLACE,
                                        dailyWork
                                    )
                            } else {
                                // Send immediately
                                viewModel.sendCampaign(campaign, context)
                            }

                            // Clear fields after sending
                            title = ""
                            message = ""
                            imageUrl = ""
                            isScheduled = false
                            isDaily = false
                            scheduledDate = null
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = title.isNotBlank() && message.isNotBlank() &&
                                (!isScheduled || scheduledDate != null)
                    ) {
                        if (!isScheduled && !isDaily) {
                            Text("Send Now")
                        } else if (isScheduled) {
                            Text("Schedule Notification")
                        } else {
                            Text("Set Up Daily Notification")
                        }
                    }
                }
            }
            1 -> {
                // Campaigns List Tab
                if (isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .padding(paddingValues)
                            .fillMaxSize()
                            .padding(horizontal = 16.dp)
                    ) {
                        item {
                            Text(
                                "Scheduled Campaigns",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                modifier = Modifier.padding(vertical = 16.dp)
                            )
                        }

                        if (campaigns.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("No scheduled campaigns")
                                }
                            }
                        } else {
                            items(campaigns) { campaign ->
                                CampaignItem(
                                    campaign = campaign,
                                    onDelete = { viewModel.deleteCampaign(campaign.id) },
                                    onSendNow = { viewModel.sendCampaign(campaign, context) }
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CampaignItem(
    campaign: Campaign,
    onDelete: () -> Unit,
    onSendNow: () -> Unit
) {
    val dateFormatter = SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault())
    val timeFormatter = SimpleDateFormat("HH:mm", Locale.getDefault())

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = campaign.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )

                Row {
                    IconButton(onClick = onSendNow) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Send Now"
                        )
                    }

                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete"
                        )
                    }
                }
            }

            Text(
                text = campaign.message,
                maxLines = 2,
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Status indicator
            Row(verticalAlignment = Alignment.CenterVertically) {
                when {
                    campaign.isDaily -> {
                        Icon(
                            imageVector = Icons.Default.Repeat,
                            contentDescription = "Daily",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Daily at ${String.format("%02d", campaign.dailyHour)}:${String.format("%02d", campaign.dailyMinute)}",
                            fontSize = 12.sp
                        )
                    }
                    campaign.isScheduled -> {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = "Scheduled",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = campaign.scheduledFor?.toDate()?.let { dateFormatter.format(it) } ?: "Scheduled",
                            fontSize = 12.sp
                        )
                    }
                    else -> {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Sent",
                            tint = Color.Green,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Sent",
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Last sent indicator if applicable
                campaign.lastSent?.toDate()?.let { lastSentDate ->
                    Text(
                        text = "Last sent: ${dateFormatter.format(lastSentDate)}",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}