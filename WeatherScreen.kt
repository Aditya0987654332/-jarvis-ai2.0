package com.example.ui.screens

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Compress
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
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
fun WeatherScreen(
  viewModel: JarvisViewModel,
  modifier: Modifier = Modifier
) {
  val language by viewModel.language.collectAsState()
  val isTtsEnabled by viewModel.isTtsEnabled.collectAsState()
  val weatherList by viewModel.weatherList.collectAsState()
  val selectedIndex by viewModel.selectedWeatherIndex.collectAsState()

  val isHindi = language == AssistantLanguage.HINDI
  var isFahrenheit by remember { mutableStateOf(false) }

  val currentStation = weatherList.getOrElse(selectedIndex) { weatherList[0] }

  val tempDisplay = if (isFahrenheit) {
    val f = (currentStation.tempC * 9 / 5) + 32
    "$f°F"
  } else {
    "${currentStation.tempC}°C"
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(VoidBlack)
  ) {
    HudHeader(
      title = if (isHindi) stringResource(R.string.weather_hi) else stringResource(R.string.weather),
      subtitle = "ORBITAL ATMOSPHERIC SENSORS",
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
      Spacer(modifier = Modifier.height(6.dp))

      // Global Station Selector Bar
      Text(
        text = if (isHindi) "// वैश्विक मौसम स्टेशन चुनें:" else "// SELECT ORBITAL MONITOR STATION:",
        fontFamily = FontFamily.Monospace,
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.sp,
        color = NeonCyanDim,
        modifier = Modifier.fillMaxWidth()
      )

      Spacer(modifier = Modifier.height(8.dp))

      LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        itemsIndexed(weatherList) { idx, item ->
          val isSelected = idx == selectedIndex
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(6.dp))
              .background(if (isSelected) NeonCyan else CyberSlate)
              .border(
                width = 1.dp,
                color = if (isSelected) NeonCyanBright else CyberBorder,
                shape = RoundedCornerShape(6.dp)
              )
              .clickable { viewModel.setSelectedWeatherIndex(idx) }
              .padding(horizontal = 10.dp, vertical = 6.dp)
              .testTag("station_pill_$idx")
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = null,
                tint = if (isSelected) VoidBlack else NeonCyan,
                modifier = Modifier.size(12.dp)
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = item.cityName,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                color = if (isSelected) VoidBlack else TextCyan
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // Main Atmospheric Sensor Hero Card
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(12.dp))
          .background(
            Brush.verticalGradient(
              listOf(CyberPlate, CyberDark)
            )
          )
          .border(1.dp, CyberBorderBright, RoundedCornerShape(12.dp))
          .hudBorder(bracketColor = NeonCyan, bracketLength = 16.dp)
          .padding(20.dp)
      ) {
        Column {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
          ) {
            Column {
              Text(
                text = currentStation.cityName.uppercase(),
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                letterSpacing = 1.sp,
                color = TextCyan
              )
              Text(
                text = currentStation.satelliteOrbitalStatus,
                fontFamily = FontFamily.Monospace,
                fontSize = 9.sp,
                color = StatusGreen
              )
            }

            // Temperature Unit Toggle (°C / °F)
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(CyberSlate)
                .border(1.dp, CyberBorder, RoundedCornerShape(6.dp))
                .clickable { isFahrenheit = !isFahrenheit }
                .padding(horizontal = 8.dp, vertical = 4.dp)
                .testTag("temp_unit_toggle")
            ) {
              Text(
                text = if (isFahrenheit) "°F" else "°C",
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = NeonCyanBright
              )
            }
          }

          Spacer(modifier = Modifier.height(20.dp))

          // Big Temperature Readout & Condition
          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Column {
              Text(
                text = tempDisplay,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 44.sp,
                color = NeonCyanBright
              )
              Text(
                text = if (isHindi) currentStation.conditionHi else currentStation.condition,
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp,
                color = TextCyan
              )
            }

            Box(
              modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(CyberSlate)
                .border(1.dp, CyberBorderBright, RoundedCornerShape(12.dp)),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.WbSunny,
                contentDescription = "Weather Icon",
                tint = NeonCyan,
                modifier = Modifier.size(36.dp)
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // 4 Telemetry Metrics (Humidity, Wind Velocity, Pressure, UV Index)
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        WeatherMetricCard(
          title = if (isHindi) "आर्द्रता" else "HUMIDITY",
          value = "${currentStation.humidity}%",
          icon = Icons.Default.WaterDrop,
          modifier = Modifier.weight(1f)
        )
        WeatherMetricCard(
          title = if (isHindi) "वायु वेग" else "WIND SPEED",
          value = "${currentStation.windKmh} km/h",
          icon = Icons.Default.Air,
          modifier = Modifier.weight(1f)
        )
      }

      Spacer(modifier = Modifier.height(10.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        WeatherMetricCard(
          title = if (isHindi) "वायुमंडलीय दबाव" else "PRESSURE",
          value = "${currentStation.pressureHpa} hPa",
          icon = Icons.Default.Compress,
          modifier = Modifier.weight(1f)
        )
        WeatherMetricCard(
          title = if (isHindi) "यूवी सूचकांक" else "UV INDEX",
          value = "${currentStation.uvIndex} / 12",
          icon = Icons.Default.WbSunny,
          modifier = Modifier.weight(1f)
        )
      }

      Spacer(modifier = Modifier.height(20.dp))
    }
  }
}

@Composable
private fun WeatherMetricCard(
  title: String,
  value: String,
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  modifier: Modifier = Modifier
) {
  Box(
    modifier = modifier
      .clip(RoundedCornerShape(8.dp))
      .background(CyberSlate)
      .border(1.dp, CyberBorder, RoundedCornerShape(8.dp))
      .padding(12.dp)
  ) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      Box(
        modifier = Modifier
          .size(34.dp)
          .clip(RoundedCornerShape(6.dp))
          .background(CyberPlate),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = icon,
          contentDescription = title,
          tint = NeonCyan,
          modifier = Modifier.size(18.dp)
        )
      }
      Spacer(modifier = Modifier.width(8.dp))
      Column {
        Text(
          text = title,
          fontFamily = FontFamily.Monospace,
          fontSize = 9.sp,
          color = TextMuted,
          letterSpacing = 0.5.sp
        )
        Text(
          text = value,
          fontFamily = FontFamily.Monospace,
          fontWeight = FontWeight.Bold,
          fontSize = 13.sp,
          color = TextCyan
        )
      }
    }
  }
}
