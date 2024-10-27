package com.gempukku.server.chat

import com.gempukku.context.resolver.expose.Exposes
import org.commonmark.Extension
import org.commonmark.ext.autolink.AutolinkExtension
import org.commonmark.ext.gfm.strikethrough.StrikethroughExtension
import org.commonmark.node.Image
import org.commonmark.node.Link
import org.commonmark.node.Node
import org.commonmark.node.Text
import org.commonmark.parser.Parser
import org.commonmark.renderer.NodeRenderer
import org.commonmark.renderer.html.HtmlNodeRendererContext
import org.commonmark.renderer.html.HtmlRenderer
import org.commonmark.renderer.html.HtmlWriter
import java.util.*
import java.util.regex.Pattern

@Exposes(ChatMessageProcessor::class)
class MarkdownChatMessageProcessorSystem : ChatMessageProcessor {
    private val adminExt: List<Extension> = listOf(StrikethroughExtension.create(), AutolinkExtension.create())
    private val markdownParser: Parser =
        Parser.builder()
            .extensions(adminExt)
            .build()
    private val markdownRenderer: HtmlRenderer =
        HtmlRenderer.builder()
            .nodeRendererFactory { htmlContext ->
                LinkShredder(
                    htmlContext
                )
            }
            .extensions(adminExt)
            .escapeHtml(true)
            .sanitizeUrls(true)
            .softbreak("<br />")
            .build()
    private val quoteExtender: Pattern = Pattern.compile("^([ \t]*>[ \t]*.+)(?=\n[ \t]*[^>])", Pattern.MULTILINE)

    override fun processMessage(message: String): String {
        var newMsg: String
        newMsg = message.trim { it <= ' ' }.replace("\n\n\n+".toRegex(), "\n\n\n")
        newMsg = quoteExtender.matcher(newMsg).replaceAll("$1\n")

        //Escaping underscores so that URLs with lots of underscores (i.e. wiki links) aren't mangled
        // Besides, who uses _this_ instead of *this*?
        newMsg = newMsg.replace("_", "\\_")

        //Need to preserve any commands being made
        if (!newMsg.startsWith("/")) {
            newMsg = markdownRenderer.render(markdownParser.parse(newMsg))
            // Prevent quotes with newlines from displaying side-by-side
            newMsg =
                newMsg.replace("</blockquote>[\n \t]*<blockquote>".toRegex(), "</blockquote><br /><blockquote>")
            //Make all links open in a new tab
            newMsg = newMsg.replace("<(a href=\".*?\")>".toRegex(), "<$1 target=\"blank\">")
        }

        return newMsg
    }

    //Processing to implement:
    // + quotes restricted to one line
    // - triple quote to avoid this??
    // + remove url text processing
    // + remove image processing
    // - re-enable bare url linking
    private inner class LinkShredder(context: HtmlNodeRendererContext) : NodeRenderer {
        private val html: HtmlWriter = context.writer

        override fun getNodeTypes(): Set<Class<out Node>> {
            // Return the node types we want to use this renderer for.
            return HashSet(
                Arrays.asList(
                    Link::class.java,
                    Image::class.java
                )
            )
        }

        override fun render(node: Node) {
            when (node) {
                is Link -> {
                    if (node.title != null) {
                        html.text(node.title + ": " + node.destination)
                    } else {
                        node.firstChild?.takeIf { it is Text && it.literal != node.destination }?.let {
                            html.text((node.firstChild as Text).literal + ": " + node.destination)
                        } ?: run {
                            html.tag("a", Collections.singletonMap("href", node.destination))
                            html.text(node.destination)
                            html.tag("/a")
                        }
                    }
                }

                is Image -> {
                    html.text(node.title + ": " + node.destination)
                }
            }
        }
    }
}
