package com.tapattend.nfc.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.tapattend.nfc.data.ParsedCardData

@Composable
fun CardInspectorDialog(
    inspectedCard: ParsedCardData?,
    onSimulateLocalCard: () -> Unit,
    onClear: () -> Unit,
    onSendToRegistration: (ParsedCardData) -> Unit,
    onDismiss: () -> Unit
) {
    val clipboardManager = LocalClipboardManager.current
    var copied by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .padding(18.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Memory,
                            contentDescription = null,
                            tint = Color(0xFF7C3AED),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("Local NFC Card Inspector", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text(
                                "Reads NDEF, MIFARE & IsoDep without any DB",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (inspectedCard == null) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFF3E8FF),
                        border = BorderStroke(1.5.dp, Color(0xFF7C3AED)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.Contactless,
                                contentDescription = null,
                                tint = Color(0xFF7C3AED),
                                modifier = Modifier.size(44.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Tap Student ID Card to Test Local Read",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color(0xFF4C1D95)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "This scanner probes NDEF records, MIFARE Classic default-key sectors, MIFARE Ultralight pages, and IsoDep SmartCard bytes to check if Name, Reg No, Course & Department are readable directly from the card.",
                                fontSize = 11.sp,
                                color = Color(0xFF5B21B6)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            OutlinedButton(
                                onClick = onSimulateLocalCard,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.TouchApp, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Simulate Local Card Payload", fontSize = 12.sp)
                            }
                        }
                    }
                } else {
                    val hasText = !inspectedCard.ndefText.isNullOrBlank()
                    val statusBg = when {
                        inspectedCard.hasLocalStudentData -> Color(0xFFECFDF5)
                        hasText -> Color(0xFFFFFBEB)
                        else -> Color(0xFFFEF2F2)
                    }
                    val statusBorder = when {
                        inspectedCard.hasLocalStudentData -> Color(0xFF10B981)
                        hasText -> Color(0xFFF59E0B)
                        else -> Color(0xFFEF4444)
                    }
                    val statusTitle = when {
                        inspectedCard.hasLocalStudentData -> "Student Details Read Locally!"
                        hasText -> "Readable Text Found (Partial Parse)"
                        else -> "UID Only — Card Memory Locked / Empty"
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = statusBg,
                        border = BorderStroke(1.5.dp, statusBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(statusTitle, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF0F172A))
                                TextButton(onClick = onClear, contentPadding = PaddingValues(0.dp)) {
                                    Text("Scan Again", fontSize = 11.sp)
                                }
                            }
                            Text("UID: ${inspectedCard.cardUid}", fontFamily = FontFamily.Monospace, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            if (inspectedCard.techList.isNotEmpty()) {
                                Text("Chips: ${inspectedCard.techList.joinToString(", ")}", fontSize = 11.sp, color = Color(0xFF475569))
                            }
                            Text("Source: ${inspectedCard.memorySource}", fontSize = 11.sp, color = Color(0xFF475569))
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Parsed Student Fields
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Extracted Student Fields", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            InspectorFieldRow("Full Name", inspectedCard.suggestedName ?: "Not found on card")
                            InspectorFieldRow("Reg Number", inspectedCard.suggestedRegNumber ?: "Not found on card")
                            InspectorFieldRow("Department", inspectedCard.suggestedDepartment ?: "Not found on card")
                            InspectorFieldRow("Course", inspectedCard.suggestedCourse ?: "Not found on card")
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Raw Diagnostic Memory Dump
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Raw Chip Memory Dump", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        TextButton(
                            onClick = {
                                clipboardManager.setText(AnnotatedString(inspectedCard.rawDump))
                                copied = true
                            },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (copied) "Copied!" else "Copy Dump", fontSize = 11.sp)
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF0F172A),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 90.dp, max = 190.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(10.dp)
                                .verticalScroll(rememberScrollState())
                        ) {
                            Text(
                                text = inspectedCard.rawDump.ifBlank { "No raw bytes read." },
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp,
                                color = Color(0xFFE2E8F0)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = onDismiss) { Text("Close") }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(onClick = { onSendToRegistration(inspectedCard) }) {
                            Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Register / Save", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InspectorFieldRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text("$label:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.width(90.dp))
        Text(value, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
    }
}
