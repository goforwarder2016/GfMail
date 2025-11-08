package com.gf.mail.utils

import javax.mail.internet.MimeUtility

/**
 * 统一的HTML解析工具类
 * 用于将HTML内容转换为格式化的纯文本，保持原有格式结构
 */
object HtmlParser {
    
    /**
     * 将HTML内容转换为格式化的纯文本
     * 使用正确的HTML解析方法，保持原有格式结构
     * @param html HTML内容字符串
     * @return 格式化后的纯文本
     */
    fun parseHtmlToText(html: String): String {
        return try {
            println("🔍 [HTML_PARSER] Starting HTML parsing, input length: ${html.length}")
            
            // 首先清理不需要的内容
            val cleanedHtml = cleanUnwantedContent(html)
            
            // 使用正确的HTML解析方法
            val parsedText = parseHtmlStructure(cleanedHtml)
            
            // 最后清理空白字符，但保持格式结构
            val finalText = cleanWhitespace(parsedText)
            
            println("🔍 [HTML_PARSER] HTML parsing completed, output length: ${finalText.length}")
            finalText
        } catch (e: Exception) {
            println("⚠️ [HTML_PARSER] HTML parsing failed: ${e.message}")
            // 如果解析失败，返回原始内容
            html
        }
    }
    
    /**
     * 清理不需要的HTML内容（脚本、样式等）
     */
    private fun cleanUnwantedContent(html: String): String {
        var result = html
        
        // 移除脚本和样式标签及其内容
        result = result.replace(Regex("(?i)<script[^>]*>.*?</script>", RegexOption.DOT_MATCHES_ALL), "")
        result = result.replace(Regex("(?i)<style[^>]*>.*?</style>", RegexOption.DOT_MATCHES_ALL), "")
        result = result.replace(Regex("(?i)<noscript[^>]*>.*?</noscript>", RegexOption.DOT_MATCHES_ALL), "")
        result = result.replace(Regex("(?i)<iframe[^>]*>.*?</iframe>", RegexOption.DOT_MATCHES_ALL), "")
        result = result.replace(Regex("(?i)<object[^>]*>.*?</object>", RegexOption.DOT_MATCHES_ALL), "")
        result = result.replace(Regex("(?i)<embed[^>]*>.*?</embed>", RegexOption.DOT_MATCHES_ALL), "")
        result = result.replace(Regex("(?i)<applet[^>]*>.*?</applet>", RegexOption.DOT_MATCHES_ALL), "")
        
        // 移除HTML注释
        result = result.replace(Regex("<!--.*?-->", RegexOption.DOT_MATCHES_ALL), "")
        
        // 移除DOCTYPE和XML声明
        result = result.replace(Regex("(?i)<!DOCTYPE[^>]*>", RegexOption.DOT_MATCHES_ALL), "")
        result = result.replace(Regex("(?i)<\\?xml[^>]*\\?>", RegexOption.DOT_MATCHES_ALL), "")
        
        return result
    }
    
    /**
     * 正确解析HTML结构，保持格式信息
     */
    private fun parseHtmlStructure(html: String): String {
        var result = html
        
        // 先处理HTML实体，避免在标签处理时被破坏
        result = decodeHtmlEntities(result)
        
        // 处理块级元素，保持结构层次
        result = parseBlockElements(result)
        
        // 处理内联元素，保持文本格式
        result = parseInlineElements(result)
        
        // 处理特殊元素（链接、图片等）
        result = parseSpecialElements(result)
        
        // 最后移除剩余的HTML标签
        result = result.replace(Regex("<[^>]+>"), "")
        
        // 处理数字和十六进制实体
        result = decodeNumericEntities(result)
        
        return result
    }
    
    /**
     * 解析块级元素，保持结构层次
     */
    private fun parseBlockElements(html: String): String {
        var result = html
        
        // 处理标题 - 保持层级结构
        result = result.replace(Regex("(?i)<h1[^>]*>"), "\n\n=== ")
        result = result.replace(Regex("(?i)</h1>"), " ===\n\n")
        result = result.replace(Regex("(?i)<h2[^>]*>"), "\n\n== ")
        result = result.replace(Regex("(?i)</h2>"), " ==\n\n")
        result = result.replace(Regex("(?i)<h3[^>]*>"), "\n\n= ")
        result = result.replace(Regex("(?i)</h3>"), " =\n\n")
        result = result.replace(Regex("(?i)<h[4-6][^>]*>"), "\n\n")
        result = result.replace(Regex("(?i)</h[4-6]>"), "\n\n")
        
        // 处理段落 - 保持段落分隔
        result = result.replace(Regex("(?i)<p[^>]*>"), "\n\n")
        result = result.replace(Regex("(?i)</p>"), "\n\n")
        
        // 处理换行和分隔线
        result = result.replace(Regex("(?i)<br[^>]*/?>"), "\n")
        result = result.replace(Regex("(?i)<hr[^>]*/?>"), "\n---\n")
        
        // 处理列表 - 保持列表结构
        result = result.replace(Regex("(?i)<ul[^>]*>"), "\n")
        result = result.replace(Regex("(?i)</ul>"), "\n")
        result = result.replace(Regex("(?i)<ol[^>]*>"), "\n")
        result = result.replace(Regex("(?i)</ol>"), "\n")
        result = result.replace(Regex("(?i)<li[^>]*>"), "\n• ")
        result = result.replace(Regex("(?i)</li>"), "\n")
        
        // 处理表格 - 改进的表格结构解析
        result = result.replace(Regex("(?i)<table[^>]*>"), "\n")
        result = result.replace(Regex("(?i)</table>"), "\n")
        result = result.replace(Regex("(?i)<tbody[^>]*>"), "")
        result = result.replace(Regex("(?i)</tbody>"), "")
        result = result.replace(Regex("(?i)<thead[^>]*>"), "")
        result = result.replace(Regex("(?i)</thead>"), "")
        result = result.replace(Regex("(?i)<tfoot[^>]*>"), "")
        result = result.replace(Regex("(?i)</tfoot>"), "")
        result = result.replace(Regex("(?i)<tr[^>]*>"), "\n")
        result = result.replace(Regex("(?i)</tr>"), "\n")
        result = result.replace(Regex("(?i)<td[^>]*>"), " ")
        result = result.replace(Regex("(?i)</td>"), " ")
        result = result.replace(Regex("(?i)<th[^>]*>"), " ")
        result = result.replace(Regex("(?i)</th>"), " ")
        
        // 处理其他块级元素
        result = result.replace(Regex("(?i)<div[^>]*>"), "\n")
        result = result.replace(Regex("(?i)</div>"), "\n")
        result = result.replace(Regex("(?i)<section[^>]*>"), "\n")
        result = result.replace(Regex("(?i)</section>"), "\n")
        result = result.replace(Regex("(?i)<article[^>]*>"), "\n")
        result = result.replace(Regex("(?i)</article>"), "\n")
        result = result.replace(Regex("(?i)<header[^>]*>"), "\n")
        result = result.replace(Regex("(?i)</header>"), "\n")
        result = result.replace(Regex("(?i)<footer[^>]*>"), "\n")
        result = result.replace(Regex("(?i)</footer>"), "\n")
        result = result.replace(Regex("(?i)<nav[^>]*>"), "\n")
        result = result.replace(Regex("(?i)</nav>"), "\n")
        result = result.replace(Regex("(?i)<aside[^>]*>"), "\n")
        result = result.replace(Regex("(?i)</aside>"), "\n")
        
        // 处理引用块
        result = result.replace(Regex("(?i)<blockquote[^>]*>"), "\n> ")
        result = result.replace(Regex("(?i)</blockquote>"), "\n")
        
        // 处理代码块
        result = result.replace(Regex("(?i)<pre[^>]*>"), "\n```\n")
        result = result.replace(Regex("(?i)</pre>"), "\n```\n")
        
        return result
    }
    
    /**
     * 解析内联元素，保持文本格式
     */
    private fun parseInlineElements(html: String): String {
        var result = html
        
        // 处理文本格式
        result = result.replace(Regex("(?i)<(strong|b)[^>]*>"), "**")
        result = result.replace(Regex("(?i)</(strong|b)>"), "**")
        result = result.replace(Regex("(?i)<(em|i)[^>]*>"), "*")
        result = result.replace(Regex("(?i)</(em|i)>"), "*")
        result = result.replace(Regex("(?i)<u[^>]*>"), "_")
        result = result.replace(Regex("(?i)</u>"), "_")
        result = result.replace(Regex("(?i)<s[^>]*>"), "~~")
        result = result.replace(Regex("(?i)</s>"), "~~")
        
        // 处理代码
        result = result.replace(Regex("(?i)<code[^>]*>"), "`")
        result = result.replace(Regex("(?i)</code>"), "`")
        
        // 处理引用
        result = result.replace(Regex("(?i)<q[^>]*>"), "\"")
        result = result.replace(Regex("(?i)</q>"), "\"")
        
        return result
    }
    
    /**
     * 解析特殊元素（链接、图片等）
     */
    private fun parseSpecialElements(html: String): String {
        var result = html
        
        // 处理链接 - 保持链接信息
        result = result.replace(Regex("(?i)<a[^>]*href\\s*=\\s*[\"']([^\"']*)[\"'][^>]*>([^<]*)</a>"), "$2 ($1)")
        result = result.replace(Regex("(?i)<a[^>]*>([^<]*)</a>"), "$1")
        
        // 处理图片 - 保持图片信息
        result = result.replace(Regex("(?i)<img[^>]*alt\\s*=\\s*[\"']([^\"']*)[\"'][^>]*>"), "[图片: $1]")
        result = result.replace(Regex("(?i)<img[^>]*>"), "[图片]")
        
        return result
    }
    
    /**
     * 清理空白字符，但保持格式结构
     */
    private fun cleanWhitespace(text: String): String {
        var result = text
        
        // 首先处理重复内容 - 检测并移除重复的段落
        result = removeDuplicateContent(result)
        
        // 清理混乱的格式化标记 - 更精确的处理
        result = result.replace(Regex("\\*([^*\\s][^*]*?)\\s*~~"), "**$1**") // *标题 ~~ → **标题**
        result = result.replace(Regex("\\*\\s*\\*\\s*~~"), "**") // * * ~~ → **
        result = result.replace(Regex("\\*\\*\\s*\\*\\s*\\*"), "**") // ** * * → **
        result = result.replace(Regex("\\*\\s*\\*\\s*\\*"), "*") // * * * → *
        result = result.replace(Regex("~~\\s*~~\\s*~~"), "~~") // ~~ ~~ ~~ → ~~
        result = result.replace(Regex("\\*\\*\\s*\\*\\*"), "**") // 清理重复的 **
        result = result.replace(Regex("\\*\\s*\\*"), "*") // 清理重复的 *
        result = result.replace(Regex("~~\\s*~~"), "~~") // 清理重复的 ~~
        result = result.replace(Regex("_\\s*_"), "_") // 清理重复的 _
        
        // 清理多余的删除线标记（在非删除线内容中）
        result = result.replace(Regex("~~([^~]*?)\\s*~~"), "$1") // ~~内容~~ → 内容（如果不是真正的删除线）
        result = result.replace(Regex("~~\\s*([^~\\s][^~]*?)\\s*~~"), "$1") // ~~ 内容 ~~ → 内容
        
        // 清理孤立的格式化标记
        result = result.replace(Regex("\\s+\\*\\s+"), " ") // 清理孤立的 *
        result = result.replace(Regex("\\s+~~\\s+"), " ") // 清理孤立的 ~~
        result = result.replace(Regex("\\s+\\*\\*\\s+"), " ") // 清理孤立的 **
        
        // 清理表格中的多余分隔符
        result = result.replace(Regex("\\|\\s*\\|\\s*\\|"), "|") // | | | → |
        result = result.replace(Regex("\\s*\\|\\s*\\|\\s*"), " | ") // 规范化表格分隔符
        
        // 合并多个空格
        result = result.replace(Regex("\\s+"), " ")
        
        // 移除换行前后的空格
        result = result.replace(Regex("\\n\\s+"), "\n")
        result = result.replace(Regex("\\s+\\n"), "\n")
        
        // 最多保留两个连续换行
        result = result.replace(Regex("\\n{3,}"), "\n\n")
        
        // 移除首尾空白
        result = result.trim()
        
        return result
    }
    
    /**
     * 移除重复的内容段落
     */
    private fun removeDuplicateContent(text: String): String {
        val lines = text.split("\n")
        val seen = mutableSetOf<String>()
        val result = mutableListOf<String>()
        
        for (line in lines) {
            val trimmedLine = line.trim()
            if (trimmedLine.isNotEmpty() && !seen.contains(trimmedLine)) {
                seen.add(trimmedLine)
                result.add(line)
            }
        }
        
        return result.joinToString("\n")
    }
    
    /**
     * 解码HTML实体
     */
    private fun decodeHtmlEntities(text: String): String {
        var result = text
        
        // 基础HTML实体
        result = result.replace("&nbsp;", " ")
        result = result.replace("&amp;", "&")
        result = result.replace("&lt;", "<")
        result = result.replace("&gt;", ">")
        result = result.replace("&quot;", "\"")
        result = result.replace("&#39;", "'")
        result = result.replace("&apos;", "'")
        
        // 版权和商标符号
        result = result.replace("&copy;", "©")
        result = result.replace("&reg;", "®")
        result = result.replace("&trade;", "™")
        
        // 标点符号
        result = result.replace("&hellip;", "...")
        result = result.replace("&mdash;", "—")
        result = result.replace("&ndash;", "–")
        result = result.replace("&lsquo;", "'")
        result = result.replace("&rsquo;", "'")
        result = result.replace("&ldquo;", """)
        result = result.replace("&rdquo;", """)
        result = result.replace("&bull;", "•")
        result = result.replace("&middot;", "·")
        
        // 数学符号
        result = result.replace("&deg;", "°")
        result = result.replace("&plusmn;", "±")
        result = result.replace("&times;", "×")
        result = result.replace("&divide;", "÷")
        result = result.replace("&infin;", "∞")
        result = result.replace("&sum;", "∑")
        result = result.replace("&prod;", "∏")
        result = result.replace("&int;", "∫")
        result = result.replace("&part;", "∂")
        result = result.replace("&nabla;", "∇")
        result = result.replace("&radic;", "√")
        result = result.replace("&prop;", "∝")
        result = result.replace("&in;", "∈")
        result = result.replace("&notin;", "∉")
        result = result.replace("&ni;", "∋")
        result = result.replace("&cap;", "∩")
        result = result.replace("&cup;", "∪")
        result = result.replace("&sub;", "⊂")
        result = result.replace("&sup;", "⊃")
        result = result.replace("&sube;", "⊆")
        result = result.replace("&supe;", "⊇")
        result = result.replace("&oplus;", "⊕")
        result = result.replace("&otimes;", "⊗")
        result = result.replace("&perp;", "⊥")
        result = result.replace("&sdot;", "⋅")
        
        // 货币符号
        result = result.replace("&euro;", "€")
        result = result.replace("&pound;", "£")
        result = result.replace("&yen;", "¥")
        result = result.replace("&cent;", "¢")
        result = result.replace("&curren;", "¤")
        
        // 希腊字母
        result = result.replace("&Alpha;", "Α")
        result = result.replace("&Beta;", "Β")
        result = result.replace("&Gamma;", "Γ")
        result = result.replace("&Delta;", "Δ")
        result = result.replace("&Epsilon;", "Ε")
        result = result.replace("&Zeta;", "Ζ")
        result = result.replace("&Eta;", "Η")
        result = result.replace("&Theta;", "Θ")
        result = result.replace("&Iota;", "Ι")
        result = result.replace("&Kappa;", "Κ")
        result = result.replace("&Lambda;", "Λ")
        result = result.replace("&Mu;", "Μ")
        result = result.replace("&Nu;", "Ν")
        result = result.replace("&Xi;", "Ξ")
        result = result.replace("&Omicron;", "Ο")
        result = result.replace("&Pi;", "Π")
        result = result.replace("&Rho;", "Ρ")
        result = result.replace("&Sigma;", "Σ")
        result = result.replace("&Tau;", "Τ")
        result = result.replace("&Upsilon;", "Υ")
        result = result.replace("&Phi;", "Φ")
        result = result.replace("&Chi;", "Χ")
        result = result.replace("&Psi;", "Ψ")
        result = result.replace("&Omega;", "Ω")
        
        // 小写希腊字母
        result = result.replace("&alpha;", "α")
        result = result.replace("&beta;", "β")
        result = result.replace("&gamma;", "γ")
        result = result.replace("&delta;", "δ")
        result = result.replace("&epsilon;", "ε")
        result = result.replace("&zeta;", "ζ")
        result = result.replace("&eta;", "η")
        result = result.replace("&theta;", "θ")
        result = result.replace("&iota;", "ι")
        result = result.replace("&kappa;", "κ")
        result = result.replace("&lambda;", "λ")
        result = result.replace("&mu;", "μ")
        result = result.replace("&nu;", "ν")
        result = result.replace("&xi;", "ξ")
        result = result.replace("&omicron;", "ο")
        result = result.replace("&pi;", "π")
        result = result.replace("&rho;", "ρ")
        result = result.replace("&sigma;", "σ")
        result = result.replace("&tau;", "τ")
        result = result.replace("&upsilon;", "υ")
        result = result.replace("&phi;", "φ")
        result = result.replace("&chi;", "χ")
        result = result.replace("&psi;", "ψ")
        result = result.replace("&omega;", "ω")
        
        // 其他常用符号
        result = result.replace("&spades;", "♠")
        result = result.replace("&clubs;", "♣")
        result = result.replace("&hearts;", "♥")
        result = result.replace("&diams;", "♦")
        result = result.replace("&loz;", "◊")
        result = result.replace("&weierp;", "℘")
        result = result.replace("&image;", "ℑ")
        result = result.replace("&real;", "ℜ")
        result = result.replace("&alefsym;", "ℵ")
        result = result.replace("&larr;", "←")
        result = result.replace("&uarr;", "↑")
        result = result.replace("&rarr;", "→")
        result = result.replace("&darr;", "↓")
        result = result.replace("&harr;", "↔")
        result = result.replace("&crarr;", "↵")
        result = result.replace("&lArr;", "⇐")
        result = result.replace("&uArr;", "⇑")
        result = result.replace("&rArr;", "⇒")
        result = result.replace("&dArr;", "⇓")
        result = result.replace("&hArr;", "⇔")
        
        return result
    }
    
    /**
     * 解码数字和十六进制HTML实体
     */
    private fun decodeNumericEntities(text: String): String {
        var result = text
        
        // 处理数字实体 &#123;
        result = result.replace(Regex("&#(\\d+);")) { matchResult ->
            val code = matchResult.groupValues[1].toIntOrNull()
            if (code != null && code in 32..126) {
                code.toChar().toString()
            } else {
                matchResult.value
            }
        }
        
        // 处理十六进制实体 &#x1A;
        result = result.replace(Regex("&#x([0-9a-fA-F]+);")) { matchResult ->
            val code = matchResult.groupValues[1].toIntOrNull(16)
            if (code != null && code in 32..126) {
                code.toChar().toString()
            } else {
                matchResult.value
            }
        }
        
        return result
    }
}
