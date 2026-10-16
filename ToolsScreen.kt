package com.example.ui.screens

import android.content.Context
import android.hardware.camera2.CameraManager
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.FlashlightOff
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.AppScreen
import com.example.data.AssistantLanguage
import com.example.ui.components.HudHeader
import com.example.ui.components.hudBorder
import com.example.ui.theme.*
import com.example.viewmodel.JarvisViewModel

@Composable
fun ToolsScreen(
  viewModel: JarvisViewModel,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val language by viewModel.language.collectAsState()
  val isTtsEnabled by viewModel.isTtsEnabled.collectAsState()
  val telemetry by viewModel.telemetry.collectAsState()
  val isTorchOn by viewModel.isTorchOn.collectAsState()
  val scanProgress by viewModel.diagnosticScanProgress.collectAsState()

  val isHindi = language == AssistantLanguage.HINDI

  fun toggleTorchHardware() {
    viewModel.toggleTorch()
    try {
      val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager
      val cameraId = cameraManager?.cameraIdList?.firstOrNull()
      if (cameraId != null) {
        cameraManager.setTorchMode(cameraId, !isTorchOn)
      }
    } catch (_: Exception) {}
  }

  fun playFrequencyPulse() {
    try {
      val toneGen = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 90)
      toneGen.startTone(ToneGenerator.TONE_PROP_BEEP2, 350)
    } catch (_: Exception) {}
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(VoidBlack)
  ) {
    HudHeader(
      title = if (isHindi) stringResource(R.string.tools_hi) else stringResource(R.string.tools),
      subtitle = "SYSTEM DIAGNOSTICS // HARDWARE BUS",
      language = language,
      isTtsEnabled = isTtsEnabled,
      onLanguageToggle = { viewModel.toggleLanguage() },
      onTtsToggle = { viewModel.toggleTts() },
      showBackButton = true,
      onBackClick = { viewModel.setScreen(AppScreen.DASHBOARD) }
    )

    Column(
      modifier = Modifier
        .weight(1f)
        .verticalScroll(rememberScrollState())
        .padding(horizontal = 16.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Spacer(modifier = Modifier.height(8.dp))

      // Diagnostic Scan Runner Banner
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(10.dp))
          .background(CyberPlate)
          .border(1.dp, CyberBorderBright, RoundedCornerShape(10.dp))
          .hudBorder(bracketColor = NeonCyan, bracketLength = 12.dp)
          .padding(16.dp)
      ) {
        Column {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text(
                text = if (isHindi) "पूर्ण सिस्टम डायग्नोस्टिक" else "FULL SYSTEM SCAN",
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = TextCyan
              )
              Text(
                text = "STATUS: ALL SYSTEMS NOMINAL",
                fontFamily = FontFamily.Monospace,
                fontSize = 9.sp,
                color = StatusGreen
              )
            }
            Button(
              onClick = { viewModel.runSystemDiagnostics() },
              enabled = scanProgress == null,
              colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
              shape = RoundedCornerShape(6.dp),
              modifier = Modifier.testTag("run_diagnostic_button")
            ) {
              Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = null,
                tint = VoidBlack,
                modifier = Modifier.size(16.dp)
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = if (isHindi) "स्कैन करें" else "Scan",
                color = VoidBlack,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp
              )
            }
          }

          if (scanProgress != null) {
            Spacer(modifier = Modifier.height(12.dp))
            LinearProgressIndicator(
              progress = { scanProgress ?: 0f },
              modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
              color = NeonCyanBright,
              trackColor = CyberSlate
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = "SCANNING SUBSYSTEMS: ${(scanProgress!! * 100).toInt()}%",
              fontFamily = FontFamily.Monospace,
              fontSize = 9.sp,
              color = NeonCyanBright
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // Section: Hardware Telemetry Readouts
      Text(
        text = if (isHindi) "// हार्डवेयर टेलीमेट्री" else "// HARDWARE TELEMETRY NODES",
        fontFamily = FontFamily.Monospace,
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.sp,
        color = NeonCyanDim,
        modifier = Modifier.fillMaxWidth()
      )

      Spacer(modifier = Modifier.height(8.dp))

      // Row 1: Battery & Memory
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        HardwareTelemetryCard(
          title = if (isHindi) "बैटरी रिज़र्व" else "BATTERY POWER",
          value = "${telemetry.batteryPercent}%",
          subtitle = if (telemetry.isCharging) "Charging (AC Lock)" else "Discharging",
          icon = Icons.Default.BatteryChargingFull,
          statusColor = if (telemetry.batteryPercent > 20) StatusGreen else StatusWarning,
          modifier = Modifier.weight(1f)
        )
        HardwareTelemetryCard(
          title = if (isHindi) "सिस्टम मेमोरी" else "RAM CONSUMPTION",
          value = "${telemetry.ramUsedMb} MB",
          subtitle = "Max ${telemetry.ramTotalMb} MB",
          icon = Icons.Default.Memory,
          statusColor = NeonBlue,
          modifier = Modifier.weight(1f)
        )
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Row 2: Arc Reactor Core & Security Level
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        HardwareTelemetryCard(
          title = if (isHindi) "कोर ऊर्जा" else "ARC REACTOR",
          value = "${telemetry.arcReactorEnergy}%",
          subtitle = "Temp: ${telemetry.batteryTempC}°C",
          icon = Icons.Default.Security,
          statusColor = NeonCyanBright,
          modifier = Modifier.weight(1f)
        )
        HardwareTelemetryCard(
          title = if (isHindi) "प्रोटोकॉल स्तर" else "SECURITY MESH",
          value = telemetry.protocolVersion,
          subtitle = telemetry.securityLevel,
          icon = Icons.Default.Security,
          statusColor = StatusGreen,
          modifier = Modifier.weight(1f)
        )
      }

      Spacer(modifier = Modifier.height(20.dp))

      // Section: Quick Hardware Controls (Torch / Flashlight & Audio Frequency Pulse)
      Text(
        text = if (isHindi) "// उपकरण नियंत्रण" else "// HARDWARE TOOL CONTROLS",
        fontFamily = FontFamily.Monospace,
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.sp,
        color = NeonCyanDim,
        modifier = Modifier.fillMaxWidth()
      )

      Spacer(modifier = Modifier.height(10.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        // Torch / Flashlight Toggle Button
        Box(
          modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(10.dp))
            .background(if (isTorchOn) NeonCyan.copy(alpha = 0.2f) else CyberSlate)
            .border(
              width = 1.dp,
              color = if (isTorchOn) NeonCyanBright else CyberBorder,
              shape = RoundedCornerShape(10.dp)
            )
            .clickable { toggleTorchHardware() }
            .padding(14.dp)
            .testTag("torch_toggle_tool")
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(if (isTorchOn) NeonCyan else CyberPlate),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = if (isTorchOn) Icons.Default.FlashlightOn else Icons.Default.FlashlightOff,
                contentDescription = "Flashlight Toggle",
                tint = if (isTorchOn) VoidBlack else TextCyan,
                modifier = Modifier.size(20.dp)
              )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
              Text(
                text = if (isHindi) "टॉर्च / लाइट" else "PHOTON TORCH",
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                color = TextCyan
              )
              Text(
                text = if (isTorchOn) "ACTIVE" else "STANDBY",
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp,
                color = if (isTorchOn) StatusGreen else TextMuted
              )
            }
          }
        }

        // Sound Frequency Generator / Audio Beep Pulse
        Box(
          modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(10.dp))
            .background(CyberSlate)
            .border(1.dp, CyberBorder, RoundedCornerShape(10.dp))
            .clickable { playFrequencyPulse() }
            .padding(14.dp)
            .testTag("frequency_pulse_tool")
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(CyberPlate),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                contentDescription = "Frequency Pulse",
                tint = NeonCyan,
                modifier = Modifier.size(20.dp)
              )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
              Text(
                text = if (isHindi) "ध्वनि पल्स" else "AUDIO PULSE",
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                color = TextCyan
              )
              Text(
                text = "TEST 440Hz",
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp,
                color = NeonCyanDim
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(24.dp))
    }
  }
}

@Composable
private fun HardwareTelemetryCard(
  title: String,
  value: String,
  subtitle: String,
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  statusColor: androidx.compose.ui.graphics.Color,
  modifier: Modifier = Modifier
) {
  Box(
    modifier = modifier
      .clip(RoundedCornerShape(8.dp))
      .background(CyberSlate)
      .border(1.dp, CyberBorder, RoundedCornerShape(8.dp))
      .padding(12.dp)
  ) {
    Column {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = title,
          fontFamily = FontFamily.Monospace,
          fontSize = 9.sp,
          color = TextMuted,
          letterSpacing = 0.5.sp
        )
        Icon(
          imageVector = icon,
          contentDescription = null,
          tint = statusColor,
          modifier = Modifier.size(16.dp)
        )
      }
      Spacer(modifier = Modifier.height(4.dp))
      Text(
        text = value,
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        color = statusColor
      )
      Text(
        text = subtitle,
        fontFamily = FontFamily.Monospace,
        fontSize = 9.sp,
        color = TextCyanDim
      )
    }
  }
}
