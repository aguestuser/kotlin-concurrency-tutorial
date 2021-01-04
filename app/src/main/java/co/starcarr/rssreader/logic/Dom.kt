package co.starcarr.rssreader.logic

import org.w3c.dom.Element
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import javax.xml.parsers.DocumentBuilderFactory

object Dom {

    private const val DEFAULT_TAG = "channel"

    fun parseElementTree(factory: DocumentBuilderFactory, url: String, tagName: String = DEFAULT_TAG): Node =
        factory.newDocumentBuilder()
            .parse(url)
            .getElementsByTagName(tagName)
            .item(0)

    fun Node.getLeafElements(): List<Element> =
        (0 until this.childNodes.length)
            .map { this.childNodes.item(it) }
            .filter { it.nodeType == Node.ELEMENT_NODE }
            .map { it as Element }
            .filter { it.tagName == "item" }

    fun Element.getTextContentByTag(tagName: String): String =
        this.getElementsByTagName(tagName).item(0).textContent

    fun String.sanitize(): String {
        if (!startsWith("div") && contains("<div"))
            return substring(0, indexOf(("<div")))
        if (contains("<img"))
            return substring(0, indexOf(("<img")))
        return this
    }
}