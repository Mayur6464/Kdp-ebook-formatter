package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.BookEntity
import com.example.data.SectionEntity
import com.example.data.SectionType
import com.example.ui.theme.PaperCream
import com.example.ui.theme.PaperText

@Composable
fun KdpPagePaperView(
    book: BookEntity,
    section: SectionEntity,
    pageIndex: Int,
    showMarginsOverlay: Boolean = true,
    paperStockBgColor: Color = PaperCream,
    paperStockTextColor: Color = PaperText,
    modifier: Modifier = Modifier
) {
    // 6" x 9" aspect ratio is 2:3
    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(2f / 3f)
            .shadow(8.dp, shape = RoundedCornerShape(4.dp))
            .clip(RoundedCornerShape(4.dp))
            .background(paperStockBgColor)
            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(4.dp))
            .padding(
                top = (book.marginTopInches * 18).dp,
                bottom = (book.marginBottomInches * 18).dp,
                start = ((book.marginLeftInches + if (pageIndex % 2 == 1) book.gutterInches else 0f) * 18).dp,
                end = ((book.marginRightInches + if (pageIndex % 2 == 0) book.gutterInches else 0f) * 18).dp
            )
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Running Header
            if (book.enableRunningHeaders && section.sectionType != SectionType.COPYRIGHT && section.sectionType != SectionType.TABLE_OF_CONTENTS) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (pageIndex % 2 == 0) Arrangement.Start else Arrangement.End
                ) {
                    Text(
                        text = if (pageIndex % 2 == 0) book.title.uppercase() else section.title.uppercase(),
                        fontSize = 8.sp,
                        fontFamily = FontFamily.Serif,
                        color = paperStockTextColor.copy(alpha = 0.6f),
                        letterSpacing = 1.sp
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                HorizontalDivider(color = paperStockTextColor.copy(alpha = 0.2f), thickness = 0.5.dp)
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Page Content Body
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                // Chapter Title
                if (pageIndex == 0 || section.sectionType == SectionType.CHAPTER) {
                    Text(
                        text = section.title,
                        fontSize = (book.chapterTitleSizePt - 2).sp,
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                        color = paperStockTextColor,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 6.dp)
                    )
                    if (section.subtitle.isNotBlank()) {
                        Text(
                            text = section.subtitle,
                            fontSize = (book.heading2SizePt - 2).sp,
                            fontFamily = FontFamily.Serif,
                            fontStyle = FontStyle.Italic,
                            color = paperStockTextColor.copy(alpha = 0.75f),
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp)
                        )
                    }
                    if (section.epigraph.isNotBlank()) {
                        Text(
                            text = section.epigraph,
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Serif,
                            fontStyle = FontStyle.Italic,
                            color = paperStockTextColor.copy(alpha = 0.65f),
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Section Body Text Preview
                val textLines = section.contentText.take(450)
                Text(
                    text = textLines + if (section.contentText.length > 450) "..." else "",
                    fontSize = (book.bodyFontSizePt - 2).sp,
                    fontFamily = FontFamily.Serif,
                    color = paperStockTextColor,
                    lineHeight = 16.sp,
                    textAlign = TextAlign.Justify,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Running Footer with Page Number
            if (book.enablePageNumbers && section.sectionType != SectionType.COPYRIGHT) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "${pageIndex + 1}",
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Serif,
                    color = paperStockTextColor,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Optional Gutter Line Visual
        if (showMarginsOverlay) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(1.dp)
                    .background(Color(0x33D97706))
                    .align(if (pageIndex % 2 == 1) Alignment.CenterStart else Alignment.CenterEnd)
            )
        }
    }
}
