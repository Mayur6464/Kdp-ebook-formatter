package com.example.data

object DefaultBookData {

    val defaultBook = BookEntity(
        id = 1,
        title = "Glowlist: The Modern Path to Wellness",
        subtitle = "11 Essential Pillars for Mind, Body & Radiant Energy",
        author = "SAMNJ | Glowlist",
        publisher = "Glowlist Publishing & Media",
        isbn = "978-1-987654-32-1",
        publicationYear = 2026,
        trimWidthInches = 6.0f,
        trimHeightInches = 9.0f,
        marginTopInches = 1.0f,
        marginBottomInches = 1.0f,
        marginLeftInches = 1.0f,
        marginRightInches = 1.0f,
        gutterInches = 0.125f,
        bodyFontFamily = "Georgia",
        bodyFontSizePt = 12,
        chapterTitleSizePt = 20,
        heading2SizePt = 15,
        lineSpacingMultiplier = 1.15f,
        enableRunningHeaders = true,
        enablePageNumbers = true,
        startPageNumbersAfterFrontMatter = true,
        widowOrphanControl = true,
        cleanPageBreaksBeforeChapters = true
    )

    fun getDefaultSections(bookId: Long = 1): List<SectionEntity> {
        var order = 0
        val sections = mutableListOf<SectionEntity>()

        // FRONT MATTER
        sections.add(
            SectionEntity(
                bookId = bookId,
                sectionType = SectionType.COPYRIGHT,
                orderIndex = order++,
                title = "Copyright Page",
                contentText = """
Glowlist: The Modern Path to Wellness
Copyright © 2026 SAMNJ | Glowlist. All rights reserved.

Published by Glowlist Publishing & Media
www.glowlist.co

No part of this publication may be reproduced, stored in a retrieval system, or transmitted in any form or by any means, electronic, mechanical, photocopying, recording, scanning, or otherwise, except as permitted under Section 107 or 108 of the 1976 United States Copyright Act, without prior written permission of the author or publisher.

Publisher's Note: This book is designed to provide information on personal development and wellness. It is sold with the understanding that the publisher and author are not engaged in rendering legal, medical, or other professional services.

ISBN-13: 978-1-987654-32-1
First Paperback Edition: August 2026
Printed in the United States of America
                """.trimIndent()
            )
        )

        sections.add(
            SectionEntity(
                bookId = bookId,
                sectionType = SectionType.MEDICAL_DISCLAIMER,
                orderIndex = order++,
                title = "Medical Disclaimer",
                contentText = """
The information provided in this book is for educational and informational purposes only and is not intended as medical, health, or nutritional advice.

Always consult with a qualified physician or healthcare professional before beginning any new diet, exercise, or health regimen, or making changes to existing treatment plans.

The author (SAMNJ | Glowlist) and publisher disclaim all liability for any direct or indirect consequences resulting from the application of concepts, strategies, or recommendations contained within this publication.
                """.trimIndent()
            )
        )

        sections.add(
            SectionEntity(
                bookId = bookId,
                sectionType = SectionType.AFFILIATE_DISCLOSURE,
                orderIndex = order++,
                title = "Affiliate Disclosure",
                contentText = """
In compliance with Federal Trade Commission (FTC) guidelines, please note that certain links, tools, or resources recommended in this book, on our website, or in supplemental materials may contain affiliate links.

This means SAMNJ | Glowlist may earn a small commission if you make a purchase through those links, at no extra cost to you. We only recommend products, tools, and services that we personally test, trust, and believe will add genuine value to your personal journey.
                """.trimIndent()
            )
        )

        sections.add(
            SectionEntity(
                bookId = bookId,
                sectionType = SectionType.TABLE_OF_CONTENTS,
                orderIndex = order++,
                title = "Table of Contents",
                contentText = "[Automatic Table of Contents generated dynamically upon PDF & DOCX compile]"
            )
        )

        sections.add(
            SectionEntity(
                bookId = bookId,
                sectionType = SectionType.INTRODUCTION,
                orderIndex = order++,
                title = "Introduction",
                subtitle = "Welcome to Your Glow Journey",
                epigraph = "\"The journey of a thousand miles begins with a single intentional breath.\"",
                contentText = """
Welcome to Glowlist. If you are reading these words, you have made a conscious choice to prioritize your vitality, clarity, and personal growth.

In today's fast-paced digital world, true wellness is no longer just about sporadic gym visits or occasional green juices. It is a cohesive lifestyle system built on intentional daily habits.

## Why This Book Was Created
We created this 11-chapter blueprint to replace overwhelm with structured simplicity. Each chapter provides actionable protocols, backed by research and practical experience, designed to help you build sustainable daily routines.

## How to Use This Book
Read sequentially or jump directly to the pillar you need most today. Apply the core protocols at the end of each chapter, track your progress, and embrace the transformational journey ahead.
                """.trimIndent()
            )
        )

        // MAIN CONTENT - 11 CHAPTERS
        val chapterTitles = listOf(
            "Foundations of Daily Vitality" to "Establishing Core Morning Routines",
            "The Science of Circadian Rhythm" to "Optimizing Sleep Architecture & Natural Energy",
            "Mindful Nutrition & Gut Health" to "Fueling Your Body for Peak Mental Clarity",
            "Hydration Protocols & Cellular Energy" to "Unlocking Vitality Through Hydration",
            "Movement as Medicine" to "Daily Physical Workouts and Mobility Habits",
            "Mastering Stress & Cortisol Balance" to "Breathing Techniques & Nervous System Recovery",
            "Digital Detox & Mental Space" to "Reclaiming Focus in an Overstimulated World",
            "Emotional Renewal & Gratitude Practice" to "Cultivating Inner Peace & Resilience",
            "Sleep Hygiene & Evening Wind-Down" to "Designing the Ultimate Restful Sanctuary",
            "Habit Stacking & Sustainable Consistency" to "Building Routines That Last a Lifetime",
            "The Radiant Mindset Shift" to "Embodying Your Highest Potential Daily"
        )

        for (i in 1..11) {
            val (chapterTitle, subtitle) = chapterTitles[i - 1]
            sections.add(
                SectionEntity(
                    bookId = bookId,
                    sectionType = SectionType.CHAPTER,
                    chapterNumber = i,
                    orderIndex = order++,
                    title = "Chapter $i: $chapterTitle",
                    subtitle = subtitle,
                    epigraph = "\"Consistency is the key that unlocks extraordinary transformations in quiet moments.\"",
                    contentText = """
Chapter $i explores one of the foundational pillars of the Glowlist framework. Understanding this principle empowers you to make effortless daily adjustments that compound into long-term vitality.

## Core Science & Principles
Modern lifestyle demands require tailored protocols rather than generic advice. When you align your daily environment with biological rhythms, energy levels naturally stabilize and mental focus reaches new heights.

## Step-by-Step Action Plan
1. Start with one simple anchor habit each morning.
2. Track your energy responses across 7 consecutive days.
3. Eliminate friction by preparing your environment the evening before.

## Chapter Summary & Takeaways
Implementing the tools in Chapter $i$ builds sustainable momentum. Small, repeatable actions lead to remarkable transformations over time.
                    """.trimIndent()
                )
            )
        }

        // CONCLUSION & EXTRAS
        sections.add(
            SectionEntity(
                bookId = bookId,
                sectionType = SectionType.CONCLUSION,
                orderIndex = order++,
                title = "Conclusion",
                subtitle = "Your Ongoing Transformation",
                contentText = """
Congratulations on completing the 11 pillars of the Glowlist wellness blueprint.

Remember that wellness is not a static destination, but an evolving practice. Revisit these chapters whenever you need to realign your daily habits or recalibrate your goals.
                """.trimIndent()
            )
        )

        sections.add(
            SectionEntity(
                bookId = bookId,
                sectionType = SectionType.RECOMMENDED_RESOURCES,
                orderIndex = order++,
                title = "Recommended Resources",
                contentText = """
1. Glowlist Daily Habit Tracker App & Printable Worksheets
2. Recommended Circadian Lighting & Sleep Tools
3. Essential Reading List on Biohacking & Behavioral Psychology
4. Community Discussion Forums at www.glowlist.co
                """.trimIndent()
            )
        )

        sections.add(
            SectionEntity(
                bookId = bookId,
                sectionType = SectionType.APPENDIX,
                orderIndex = order++,
                title = "Appendix: Daily Protocols Quick-Reference",
                contentText = """
Table A.1: Morning Light Exposure Protocol (15 mins upon waking)
Table A.2: Hydration Formula (0.5 oz water per pound of body weight)
Table A.3: Evening Screen Shutdown Checklist (60 mins before bed)
                """.trimIndent()
            )
        )

        // BACK MATTER
        sections.add(
            SectionEntity(
                bookId = bookId,
                sectionType = SectionType.ABOUT_AUTHOR,
                orderIndex = order++,
                title = "About the Author",
                contentText = """
SAMNJ | Glowlist is a wellness brand, publisher, and research team dedicated to creating actionable, science-backed personal development guides and daily habit tools.

With a mission to empower individuals worldwide to unlock radiant vitality, SAMNJ combines modern behavior design with practical wellness principles.
                """.trimIndent()
            )
        )

        sections.add(
            SectionEntity(
                bookId = bookId,
                sectionType = SectionType.THANK_YOU,
                orderIndex = order++,
                title = "Thank You",
                contentText = """
Thank you for investing your time and trust in this book. Your dedication to self-improvement inspires our entire team. We wish you endless vitality, clarity, and joy on your ongoing glow journey.
                """.trimIndent()
            )
        )

        sections.add(
            SectionEntity(
                bookId = bookId,
                sectionType = SectionType.AMAZON_REVIEW,
                orderIndex = order++,
                title = "Invitation to Leave an Amazon Review",
                contentText = """
If this book provided value, clarity, or inspiration on your path, please consider leaving an honest review on Amazon.

Independent authors depend on reader feedback to reach new readers. Your review takes less than two minutes, but makes a monumental difference!
                """.trimIndent()
            )
        )

        sections.add(
            SectionEntity(
                bookId = bookId,
                sectionType = SectionType.OTHER_BOOKS,
                orderIndex = order++,
                title = "Other Books by SAMNJ | Glowlist",
                contentText = """
• The Glowlist Evening Reset: 30 Days to Restorative Sleep
• Mindful Focus: Elimination of Brain Fog in 14 Days
• The Glowlist Recipe Vault: Nutrient-Dense Meals for Busy Professionals
                """.trimIndent()
            )
        )

        sections.add(
            SectionEntity(
                bookId = bookId,
                sectionType = SectionType.WEBSITE_SOCIAL,
                orderIndex = order++,
                title = "Website and Social Media",
                contentText = """
Website: https://www.glowlist.co
Instagram: @glowlist.official
YouTube: @glowlist
Community & Newsletter: https://www.glowlist.co/newsletter
                """.trimIndent()
            )
        )

        return sections
    }
}
