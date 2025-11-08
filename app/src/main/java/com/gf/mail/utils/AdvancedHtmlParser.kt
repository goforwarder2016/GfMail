package com.gf.mail.utils

import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.nodes.Node
import org.jsoup.nodes.TextNode
import org.jsoup.select.NodeTraversor
import org.jsoup.select.NodeVisitor
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.nio.charset.Charset
import java.util.regex.Pattern

/**
 * Provides comprehensive HTML to text conversion with proper formatting
 */
object AdvancedHtmlParser {
    
    // Special characters handling
    private const val PREVIEW_OBJECT_CHARACTER = 0xfffc.toChar()
    private const val PREVIEW_OBJECT_REPLACEMENT = 0x20.toChar() // space
    private const val NBSP_CHARACTER = 0x00a0.toChar() // utf-8 non-breaking space
    private const val NBSP_REPLACEMENT = 0x20.toChar() // space
    
    /**
     * Convert HTML to plain text with proper formatting
     */
    fun htmlToText(html: String): String {
        if (html.isBlank()) return ""
        
        try {
            println("🔍 [AdvancedHtmlParser] htmlToText called with HTML length: ${html.length}")
            println("🔍 [AdvancedHtmlParser] HTML preview: ${html.take(200)}")
            
            val document = Jsoup.parse(html)
            val formatter = FormattingVisitor()
            NodeTraversor.traverse(formatter, document.body())
            
            val result = formatter.toString()
                .replace(PREVIEW_OBJECT_CHARACTER, PREVIEW_OBJECT_REPLACEMENT)
                .replace(NBSP_CHARACTER, NBSP_REPLACEMENT)
                .trim()
            
            println("🔍 [AdvancedHtmlParser] htmlToText result length: ${result.length}")
            println("🔍 [AdvancedHtmlParser] Result preview: ${result.take(200)}")
            
            return result
        } catch (e: Exception) {
            println("❌ [AdvancedHtmlParser] Error parsing HTML: ${e.message}")
            e.printStackTrace()
            return fallbackHtmlToText(html)
        }
    }
    
    /**
     * Extract text content from HTML with charset detection
     */
    fun extractTextFromHtml(html: String, charset: String? = null): String {
        if (html.isBlank()) return ""
        
        try {
            println("🔍 [AdvancedHtmlParser] extractTextFromHtml called with HTML length: ${html.length}")
            println("🔍 [AdvancedHtmlParser] HTML preview: ${html.take(200)}")
            println("🔍 [AdvancedHtmlParser] Charset: $charset")
            
            // Detect charset from HTML if not provided
            val detectedCharset = charset ?: detectCharsetFromHtml(html)
            println("🔍 [AdvancedHtmlParser] Detected charset: $detectedCharset")
            
            // Parse with detected charset
            val document = if (detectedCharset != null && detectedCharset != "UTF-8") {
                try {
                    val bytes = html.toByteArray(Charset.forName(detectedCharset))
                    val inputStream = ByteArrayInputStream(bytes)
                    Jsoup.parse(inputStream, detectedCharset, "")
                } catch (e: Exception) {
                    println("⚠️ [AdvancedHtmlParser] Failed to parse with charset $detectedCharset, using default: ${e.message}")
                    Jsoup.parse(html)
                }
            } else {
                Jsoup.parse(html)
            }
            
            val formatter = FormattingVisitor()
            NodeTraversor.traverse(formatter, document.body())
            
            val result = formatter.toString()
                .replace(PREVIEW_OBJECT_CHARACTER, PREVIEW_OBJECT_REPLACEMENT)
                .replace(NBSP_CHARACTER, NBSP_REPLACEMENT)
                .trim()
            
            println("🔍 [AdvancedHtmlParser] extractTextFromHtml result length: ${result.length}")
            println("🔍 [AdvancedHtmlParser] Result preview: ${result.take(200)}")
            
            return result
        } catch (e: Exception) {
            println("❌ [AdvancedHtmlParser] Error extracting text: ${e.message}")
            e.printStackTrace()
            return fallbackHtmlToText(html)
        }
    }
    
    /**
     * Detect charset from HTML meta tag
     */
    private fun detectCharsetFromHtml(html: String): String? {
        try {
            val pattern = Pattern.compile(
                "<meta\\s+http-equiv=\"?Content-Type\"?\\s+content=\"text/html;\\s*charset=([^\"]+)\"",
                Pattern.CASE_INSENSITIVE
            )
            val matcher = pattern.matcher(html)
            if (matcher.find()) {
                return matcher.group(1)
            }
            
            // Try alternative pattern
            val pattern2 = Pattern.compile(
                "<meta\\s+charset=\"([^\"]+)\"",
                Pattern.CASE_INSENSITIVE
            )
            val matcher2 = pattern2.matcher(html)
            if (matcher2.find()) {
                return matcher2.group(1)
            }
        } catch (e: Exception) {
            println("❌ [AdvancedHtmlParser] Error detecting charset: ${e.message}")
        }
        return null
    }
    
    /**
     * Fallback HTML to text conversion using regex
     */
    private fun fallbackHtmlToText(html: String): String {
        println("🔍 [AdvancedHtmlParser] Using fallback HTML to text conversion")
        
        var result = html
            .replace(Regex("<script[^>]*>.*?</script>", RegexOption.DOT_MATCHES_ALL), "")
            .replace(Regex("<style[^>]*>.*?</style>", RegexOption.DOT_MATCHES_ALL), "")
            .replace(Regex("<!--.*?-->", RegexOption.DOT_MATCHES_ALL), "")
        
        // Process links - extract link text and URL
        result = result.replace(Regex("<a[^>]*href=\"([^\"]*)\"[^>]*>([^<]*)</a>", RegexOption.IGNORE_CASE)) { matchResult ->
            val url = matchResult.groupValues[1]
            val linkText = matchResult.groupValues[2].trim()
            
            when {
                // Image link
                url.contains("img") || url.contains("image") || 
                url.endsWith(".jpg") || url.endsWith(".jpeg") || 
                url.endsWith(".png") || url.endsWith(".gif") || 
                url.endsWith(".webp") -> {
                    if (linkText.isNotEmpty()) {
                        "🖼️ [$linkText]"
                    } else {
                        "🖼️ [Image Link]"
                    }
                }
                // Email link
                url.startsWith("mailto:") -> {
                    val email = url.removePrefix("mailto:")
                    if (linkText.isNotEmpty() && linkText != email) {
                        "📧 $linkText ($email)"
                    } else {
                        "📧 $email"
                    }
                }
                // Phone link
                url.startsWith("tel:") -> {
                    val phone = url.removePrefix("tel:")
                    if (linkText.isNotEmpty() && linkText != phone) {
                        "📞 $linkText ($phone)"
                    } else {
                        "📞 $phone"
                    }
                }
                // Regular link
                else -> {
                    val shortUrl = if (url.length > 50) {
                        url.take(47) + "..."
                    } else {
                        url
                    }
                    
                    if (linkText.isNotEmpty() && linkText != url) {
                        "$linkText 🔗"
                    } else {
                        "🔗 $shortUrl"
                    }
                }
            }
        }
        
        // Process image tags
        result = result.replace(Regex("<img[^>]*alt=\"([^\"]*)\"[^>]*>", RegexOption.IGNORE_CASE)) { matchResult ->
            val altText = matchResult.groupValues[1]
            if (altText.isNotEmpty()) {
                "🖼️ [$altText]"
            } else {
                "🖼️ [Image]"
            }
        }
        
        // Remove remaining HTML tags
        result = result.replace(Regex("<[^>]+>"), "")
        
        // Process HTML entities
        result = result
            .replace("&nbsp;", " ")
            .replace("&amp;", "&")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&quot;", "\"")
            .replace("&#39;", "'")
            .replace(Regex("\\s+"), " ")
            .trim()
        
        println("🔍 [AdvancedHtmlParser] Fallback result length: ${result.length}")
        println("🔍 [AdvancedHtmlParser] Fallback result preview: ${result.take(200)}")
        
        return result
    }
}

/**
 * Node visitor for formatting HTML to text
 */
private class FormattingVisitor : NodeVisitor {
    private val output = StringBuilder()
    private var collectLinkText = false
    private var linkText = StringBuilder()
    private var inPre = false
    private var inCode = false
    
    override fun head(node: Node, depth: Int) {
        val name = node.nodeName()
        when {
            node is TextNode -> {
                val text = node.text()
                if (inPre || inCode) {
                    // Preserve whitespace in <pre> and <code> tags
                    append(text)
                } else {
                    append(text)
                }
                
                if (collectLinkText) {
                    linkText.append(text)
                    println("🔗 [FormattingVisitor] Collecting link text: '$text', total: '${linkText.toString()}'")
                }
            }
            name == "li" -> {
                startNewLine()
                append("* ")
            }
            name == "a" && node.hasAttr("href") -> {
                println("🔗 [FormattingVisitor] Found link: ${node.attr("href")}")
                collectLinkText = true
                linkText.clear()
            }
            name == "br" -> {
                append("\n")
            }
            name == "p" -> {
                startNewLine()
            }
            name == "div" -> {
                startNewLine()
            }
            name == "h1" || name == "h2" || name == "h3" || name == "h4" || name == "h5" || name == "h6" -> {
                startNewLine()
                append("\n")
            }
            name == "pre" -> {
                inPre = true
                startNewLine()
            }
            name == "code" -> {
                inCode = true
            }
            name == "blockquote" -> {
                startNewLine()
                append("> ")
            }
            name == "hr" -> {
                startNewLine()
                append("---\n")
            }
            name == "table" -> {
                startNewLine()
            }
            name == "tr" -> {
                startNewLine()
            }
            name == "td" || name == "th" -> {
                // Separate table cells with tabs for clarity
                append("\t")
            }
            name == "strong" || name == "b" -> {
                // Bold text, no special formatting, just process content
            }
            name == "em" || name == "i" -> {
                // Italic text, no special formatting, just process content
            }
            name == "u" -> {
                // Underlined text, no special formatting, just process content
            }
            name == "span" -> {
                // span tag, no special formatting, just process content
            }
            name == "font" -> {
                // font tag, no special formatting, just process content
            }
            name == "img" -> {
                // Image tag, display alt text or placeholder
                val altText = node.attr("alt")
                val src = node.attr("src")
                if (altText.isNotEmpty()) {
                    append("🖼️ [$altText]")
                } else if (src.isNotEmpty()) {
                    append("🖼️ [图片]")
                } else {
                    append("🖼️ [图片]")
                }
            }
            name == "ul" || name == "ol" -> {
                startNewLine()
            }
            name == "li" -> {
                startNewLine()
                append("* ")
            }
            // Handle special tags like <symbol(me)></symbol(me)>
            name.startsWith("symbol") -> {
                // Skip these special tags, output nothing
            }
            node is Element && node.isBlock && !inPre -> {
                startNewLine()
            }
        }
    }
    
    override fun tail(node: Node, depth: Int) {
        val name = node.nodeName()
        when {
            name == "li" -> append("\n")
            name == "p" -> {
                if (node is Element && node.text().isNotEmpty()) {
                    addEmptyLine()
                }
            }
            name == "div" -> {
                if (node is Element && node.text().isNotEmpty()) {
                    addEmptyLine()
                }
            }
            name == "h1" || name == "h2" || name == "h3" || name == "h4" || name == "h5" || name == "h6" -> {
                if (node is Element && node.text().isNotEmpty()) {
                    addEmptyLine()
                }
            }
            name == "pre" -> {
                inPre = false
                if (node is Element && node.text().isNotEmpty()) {
                    addEmptyLine()
                }
            }
            name == "code" -> {
                inCode = false
            }
            name == "blockquote" -> {
                if (node is Element && node.text().isNotEmpty()) {
                    addEmptyLine()
                }
            }
            name == "table" -> {
                if (node is Element && node.text().isNotEmpty()) {
                    addEmptyLine()
                }
            }
            name == "tr" -> {
                // New line after table row ends
                append("\n")
            }
            name == "td" || name == "th" -> {
                // No additional formatting needed after table cell ends
            }
            name == "strong" || name == "b" -> {
                // No additional formatting needed after bold text ends
            }
            name == "em" || name == "i" -> {
                // No additional formatting needed after italic text ends
            }
            name == "u" -> {
                // No additional formatting needed after underlined text ends
            }
            name == "span" -> {
                // No additional formatting needed after span tag ends
            }
            name == "font" -> {
                // No additional formatting needed after font tag ends
            }
            name == "img" -> {
                // No additional formatting needed after image tag ends
            }
            name == "ul" || name == "ol" -> {
                // New line after list ends
                if (node is Element && node.text().isNotEmpty()) {
                    addEmptyLine()
                }
            }
            name == "li" -> {
                // New line after list item ends
                append("\n")
            }
            // Handle special tags like <symbol(me)></symbol(me)>
            name.startsWith("symbol") -> {
                // Skip these special tags, output nothing
            }
            node is Element && node.isBlock && !inPre -> {
                if (node is Element && node.text().isNotEmpty()) {
                    addEmptyLine()
                }
            }
            name == "a" && node.hasAttr("href") -> {
                collectLinkText = false
                
                val href = node.attr("href")
                val linkTextStr = linkText.toString().trim()
                println("🔗 [FormattingVisitor] Processing link: href='$href', text='$linkTextStr'")
                
                // Process links directly, don't rely on absUrl
                when {
                    // Image link
                    href.contains("img") || href.contains("image") || 
                    href.endsWith(".jpg") || href.endsWith(".jpeg") || 
                    href.endsWith(".png") || href.endsWith(".gif") || 
                    href.endsWith(".webp") -> {
                        if (linkTextStr.isNotEmpty()) {
                            append("🖼️ [$linkTextStr]")
                        } else {
                            append("🖼️ [图片链接]")
                        }
                    }
                    // Email link
                    href.startsWith("mailto:") -> {
                        val email = href.removePrefix("mailto:")
                        if (linkTextStr.isNotEmpty() && linkTextStr != email) {
                            append("📧 $linkTextStr ($email)")
                        } else {
                            append("📧 $email")
                        }
                    }
                    // Phone link
                    href.startsWith("tel:") -> {
                        val phone = href.removePrefix("tel:")
                        if (linkTextStr.isNotEmpty() && linkTextStr != phone) {
                            append("📞 $linkTextStr ($phone)")
                        } else {
                            append("📞 $phone")
                        }
                    }
                    // Regular link
                    else -> {
                        val shortUrl = if (href.length > 50) {
                            href.take(47) + "..."
                        } else {
                            href
                        }
                        
                        if (linkTextStr.isNotEmpty() && linkTextStr != href) {
                            // If link text and URL are different, display as "link text 🔗"
                            append(" $linkTextStr 🔗")
                        } else if (linkTextStr.isNotEmpty()) {
                            // If link text and URL are the same, only display text
                            append(" $linkTextStr")
                        } else {
                            // If no link text, display URL
                            append(" 🔗 $shortUrl")
                        }
                    }
                }
            }
        }
    }
    
    private fun append(text: String) {
        if (text.isEmpty()) return
        
        // If text is a single space, check if it needs to be added
        if (text == " ") {
            if (output.isEmpty() || output.last() in listOf(' ', '\n')) {
                return
            }
        }
        
        // If text starts with space, check previous character
        if (text.startsWith(" ") && output.isNotEmpty() && output.last() == ' ') {
            output.append(text.substring(1))
        } else {
            output.append(text)
        }
        
        // Debug info: record link-related append calls
        if (text.contains("🔗") || text.contains("首页") || text.contains("家用电器") || text.contains("手机")) {
            println("🔗 [FormattingVisitor] Appending link content: '$text', current output length: ${output.length}")
        }
    }
    
    private fun startNewLine() {
        if (output.isEmpty() || output.last() == '\n') {
            return
        }
        
        append("\n")
    }
    
    private fun addEmptyLine() {
        if (output.isEmpty() || output.endsWith("\n\n")) {
            return
        }
        
        startNewLine()
        append("\n")
    }
    
    override fun toString(): String {
        if (output.isEmpty()) {
            return ""
        }
        
        var result = output.toString()
        println("🔍 [FormattingVisitor] Final output before cleanup: '${result.take(200)}...'")
        
        // Clean up extra spaces, but preserve newlines and link content
        result = result.replace(Regex("[ \t]+"), " ") // Merge multiple spaces and tabs into single space
        result = result.replace(Regex("\\n[ \t]+"), "\n") // Remove spaces after newlines
        result = result.replace(Regex("[ \t]+\\n"), "\n") // Remove spaces before newlines
        result = result.replace(Regex("\\n{3,}"), "\n\n") // Keep at most two consecutive newlines
        
        // Protect link content from over-cleaning
        // If result only contains image placeholder, it means over-cleaning occurred, need to restore original content
        if (result.trim() == "🖼️ [图片]" && output.length > 50) {
            println("🔍 [FormattingVisitor] Detected over-cleaning, restoring original content")
            result = output.toString()
                .replace(Regex("[ \t]+"), " ")
                .replace(Regex("\\n[ \t]+"), "\n")
                .replace(Regex("[ \t]+\\n"), "\n")
                .replace(Regex("\\n{3,}"), "\n\n")
                .trim()
        }
        
        // Clean special characters
        result = result.replace(Regex("[\\u200B-\\u200D\\uFEFF]"), "") // Remove zero-width characters
        result = result.replace(Regex("[\\u00A0]"), " ") // Convert non-breaking spaces to regular spaces
        
        // Remove leading and trailing whitespace
        result = result.trim()
        
        println("🔍 [FormattingVisitor] Final result after cleanup: '${result.take(200)}...'")
        
        return result
    }
}
