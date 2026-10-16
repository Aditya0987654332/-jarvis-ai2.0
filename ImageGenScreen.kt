package com.example.ui.screens

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.AppScreen
import com.example.data.AssistantLanguage
import com.example.data.GeneratedSchematic
import com.example.data.ImageGenStyle
import com.example.ui.components.HudHeader
import com.example.ui.components.hudBorder
import com.example.ui.theme.*
import com.example.viewmodel.JarvisViewModel
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun ImageGenScreen(
  viewModel: JarvisViewModel,
  modifier: Modifier = Modifier
) {
  val language by viewModel.language.collectAsState()
  val isTtsEnabled by viewModel.isTtsEnabled.collectAsState()
  val schematics by viewModel.schematics.collectAsState()
  val imagePrompt by viewModel.imagePrompt.collectAsState()
  val selectedStyle by viewModel.selectedImageStyle.collectAsState()
  val isGenerating by viewModel.isGeneratingImage.collectAsState()

  val isHindi = language == AssistantLanguage.HINDI

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(VoidBlack)
  ) {
    HudHeader(
      title = if (isHindi) stringResource(R.string.image_generation_hi) else stringResource(R.string.image_generation),
      subtitle = "HOLOGRAPHIC SCHEMATIC SYNTHESIZER",
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
      // Free Prototype Notice Banner
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(8.dp))
          .background(CyberPlate.copy(alpha = 0.8f))
          .border(1.dp, CyberBorder, RoundedCornerShape(8.dp))
          .padding(10.dp)
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Default.Info,
            contentDescription = "Notice",
            tint = NeonCyan,
            modifier = Modifier.size(18.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = if (isHindi)
              "प्रोटोटाइप मोड: ऑन-डिवाइस 2D/3D होलोग्राफिक सीएडी स्कीमेटिक जनरेटर सक्रिय है (बिना किसी सशुल्क सेवा के)।"
            else
              "Prototype HUD Mode: Generates vector CAD holograms on-device without requiring paid cloud image services.",
            fontFamily = FontFamily.SansSerif,
            fontSize = 11.sp,
            color = TextCyanDim
          )
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Input Prompt Box
      OutlinedTextField(
        value = imagePrompt,
        onValueChange = { viewModel.setImagePrompt(it) },
        modifier = Modifier
          .fillMaxWidth()
          .testTag("image_prompt_input"),
        label = {
          Text(
            text = if (isHindi) "स्कीमेटिक या खाके का विवरण दर्ज करें..." else "Describe schematic (e.g. Arc Reactor Core)",
            color = TextMuted,
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace
          )
        },
        colors = OutlinedTextFieldDefaults.colors(
          focusedBorderColor = NeonCyan,
          unfocusedBorderColor = CyberBorder,
          focusedContainerColor = CyberSlate,
          unfocusedContainerColor = CyberDark,
          cursorColor = NeonCyanBright
        ),
        shape = RoundedCornerShape(10.dp),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = { viewModel.generateSchematic() })
      )

      Spacer(modifier = Modifier.height(10.dp))

      // Style Selector Chips
      Text(
        text = if (isHindi) "// स्कीमेटिक शैली चुनें:" else "// SELECT SCHEMATIC STYLE:",
        fontFamily = FontFamily.Monospace,
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.sp,
        color = NeonCyanDim,
        modifier = Modifier.fillMaxWidth()
      )

      Spacer(modifier = Modifier.height(6.dp))

      LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        items(ImageGenStyle.entries) { style ->
          val isSelected = style == selectedStyle
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(6.dp))
              .background(if (isSelected) NeonCyan else CyberSlate)
              .border(
                width = 1.dp,
                color = if (isSelected) NeonCyanBright else CyberBorder,
                shape = RoundedCornerShape(6.dp)
              )
              .clickable { viewModel.setImageStyle(style) }
              .padding(horizontal = 12.dp, vertical = 6.dp)
              .testTag("style_chip_${style.name}")
          ) {
            Text(
              text = style.title,
              fontFamily = FontFamily.Monospace,
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              color = if (isSelected) VoidBlack else TextCyan
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Generate Button
      Button(
        onClick = { viewModel.generateSchematic() },
        enabled = !isGenerating,
        modifier = Modifier
          .fillMaxWidth()
          .height(48.dp)
          .testTag("synthesize_schematic_button"),
        colors = ButtonDefaults.buttonColors(
          containerColor = NeonCyan,
          disabledContainerColor = CyberSlate
        ),
        shape = RoundedCornerShape(8.dp)
      ) {
        if (isGenerating) {
          CircularProgressIndicator(
            modifier = Modifier.size(20.dp),
            color = VoidBlack,
            strokeWidth = 2.dp
          )
          Spacer(modifier = Modifier.width(10.dp))
          Text(
            text = if (isHindi) "होलोग्राम संकलित हो रहा है..." else "SYNTHESIZING SCHEMATIC...",
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            color = VoidBlack
          )
        } else {
          Icon(
            imageVector = Icons.Default.AutoAwesome,
            contentDescription = "Synthesize",
            tint = VoidBlack,
            modifier = Modifier.size(18.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = if (isHindi) "होलोग्राफिक खाका बनाएं" else "SYNTHESIZE HOLOGRAPHIC HUD",
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            color = VoidBlack
          )
        }
      }

      Spacer(modifier = Modifier.height(20.dp))

      // Generated Schematics Display
      Text(
        text = if (isHindi) "// हाल के होलोग्राफिक स्कीमेटिक्स" else "// RECENT SCHEMATIC ARCHIVES",
        fontFamily = FontFamily.Monospace,
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.sp,
        color = NeonCyanDim,
        modifier = Modifier.fillMaxWidth()
      )

      Spacer(modifier = Modifier.height(10.dp))

      schematics.forEach { schematic ->
        SchematicDisplayCard(schematic = schematic)
        Spacer(modifier = Modifier.height(14.dp))
      }

      Spacer(modifier = Modifier.height(20.dp))
    }
  }
}

@Composable
private fun SchematicDisplayCard(schematic: GeneratedSchematic) {
  Box(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(10.dp))
      .background(CyberDark)
      .border(1.dp, CyberBorderBright, RoundedCornerShape(10.dp))
      .hudBorder(bracketColor = NeonCyan, bracketLength = 12.dp)
      .padding(14.dp)
  ) {
    Column {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = schematic.prompt.uppercase(),
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            color = TextCyan
          )
          Text(
            text = "STYLE: ${schematic.style.title.uppercase()} // CAD VER 4.2",
            fontFamily = FontFamily.Monospace,
            fontSize = 9.sp,
            color = NeonCyanDim
          )
        }
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(CyberSlate)
            .border(1.dp, NeonCyan.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
          Text(
            text = "HUD RENDERED",
            fontFamily = FontFamily.Monospace,
            fontSize = 8.sp,
            color = NeonCyanBright
          )
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Dynamic Holographic Canvas Preview
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(180.dp)
          .clip(RoundedCornerShape(8.dp))
          .background(VoidBlack)
          .border(1.dp, CyberBorder, RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center
      ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
          val center = Offset(size.width / 2f, size.height / 2f)
          val w = size.width
          val h = size.height

          // Cyber Grid Background
          val gridStep = 24.dp.toPx()
          var x = 0f
          while (x < w) {
            drawLine(
              color = CyberBorder.copy(alpha = 0.2f),
              start = Offset(x, 0f),
              end = Offset(x, h),
              strokeWidth = 0.8f
            )
            x += gridStep
          }
          var y = 0f
          while (y < h) {
            drawLine(
              color = CyberBorder.copy(alpha = 0.2f),
              start = Offset(0f, y),
              end = Offset(w, y),
              strokeWidth = 0.8f
            )
            y += gridStep
          }

          // Center Crosshairs
          drawLine(NeonCyan.copy(alpha = 0.4f), Offset(center.x, 10f), Offset(center.x, h - 10f), 1f)
          drawLine(NeonCyan.copy(alpha = 0.4f), Offset(10f, center.y), Offset(w - 10f, center.y), 1f)

          when (schematic.style) {
            ImageGenStyle.ARC_REACTOR, ImageGenStyle.NANOTECH -> {
              // Arc Reactor Circular Schematics
              drawCircle(NeonCyan, radius = 55.dp.toPx(), center = center, style = Stroke(2.dp.toPx()))
              drawCircle(NeonBlue, radius = 40.dp.toPx(), center = center, style = Stroke(1.5f.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f))))
              drawCircle(NeonCyanBright, radius = 25.dp.toPx(), center = center, style = Stroke(2.dp.toPx()))
              drawCircle(Color.White, radius = 8.dp.toPx(), center = center)

              // Radial rays
              for (i in 0 until 12) {
                val rad = (i * 30) * (PI / 180f)
                val p1 = Offset(center.x + 25.dp.toPx() * cos(rad).toFloat(), center.y + 25.dp.toPx() * sin(rad).toFloat())
                val p2 = Offset(center.x + 55.dp.toPx() * cos(rad).toFloat(), center.y + 55.dp.toPx() * sin(rad).toFloat())
                drawLine(NeonCyanBright, p1, p2, 1.8f.dp.toPx())
              }
            }
            ImageGenStyle.BLUEPRINT, ImageGenStyle.SUIT_ARMOR -> {
              // Armor Chestplate CAD Schematic
              val chestWidth = 70.dp.toPx()
              val chestHeight = 50.dp.toPx()

              // Hexagonal chestplate outline
              val path = androidx.compose.ui.graphics.Path().apply {
                moveTo(center.x - chestWidth, center.y - chestHeight * 0.5f)
                lineTo(center.x + chestWidth, center.y - chestHeight * 0.5f)
                lineTo(center.x + chestWidth * 0.7f, center.y + chestHeight)
                lineTo(center.x, center.y + chestHeight * 1.3f)
                lineTo(center.x - chestWidth * 0.7f, center.y + chestHeight)
                close()
              }
              drawPath(path, NeonCyanBright, style = Stroke(2.dp.toPx()))

              // Central Arc Triangle
              val triPath = androidx.compose.ui.graphics.Path().apply {
                moveTo(center.x, center.y - 15.dp.toPx())
                lineTo(center.x + 20.dp.toPx(), center.y + 15.dp.toPx())
                lineTo(center.x - 20.dp.toPx(), center.y + 15.dp.toPx())
                close()
              }
              drawPath(triPath, ElectricBlue, style = Stroke(2.dp.toPx()))
              drawCircle(NeonCyanBright, radius = 6.dp.toPx(), center = center)
            }
            ImageGenStyle.CYBERPUNK -> {
              // Futuristic Cyber Matrix Vector Waves
              val wavePoints = 30
              val amp = 30.dp.toPx()
              for (i in 0 until wavePoints - 1) {
                val x1 = (w / wavePoints) * i
                val x2 = (w / wavePoints) * (i + 1)
                val y1 = center.y + sin(i * 0.5f) * amp
                val y2 = center.y + sin((i + 1) * 0.5f) * amp
                drawLine(NeonCyanBright, Offset(x1, y1), Offset(x2, y2), 2.5f.dp.toPx())

                val y1B = center.y - cos(i * 0.6f) * (amp * 0.7f)
                val y2B = center.y - cos((i + 1) * 0.6f) * (amp * 0.7f)
                drawLine(ElectricBlue, Offset(x1, y1B), Offset(x2, y2B), 1.8f.dp.toPx())
              }
            }
            else -> {
              drawCircle(NeonCyan, radius = 50.dp.toPx(), center = center, style = Stroke(2.dp.toPx()))
            }
          }
        }
      }
    }
  }
}
