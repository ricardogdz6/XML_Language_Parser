package SpecificationManual

import org.w3c.dom.Document
import org.w3c.dom.NodeList
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory
import org.w3c.dom.Element
import org.w3c.dom.Node
import java.io.BufferedWriter
import java.io.FileWriter
import javax.xml.transform.OutputKeys
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult

fun readXmlSpecification(xmlPath:String, datos:List<ExcelEntry>){

    val xmlFile = File(xmlPath)
    val dbFactory = DocumentBuilderFactory.newInstance()
    val dBuilder = dbFactory.newDocumentBuilder()
    val doc = dBuilder.parse(xmlFile)

    datos.forEach {
        findNode(it.identificador,doc,it)
    }

    // Remove empty text nodes to avoid vertical spacing
    removeEmptyTextNodes(doc)

    // Write the updated document back to the file
    val transformerFactory = TransformerFactory.newInstance()
    val transformer = transformerFactory.newTransformer()
    val source = DOMSource(doc)
    val result = StreamResult(xmlFile)
    transformer.setOutputProperty(OutputKeys.INDENT, "yes")
    transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2")

    transformer.transform(source, result)

    println("XML file updated successfully.")


}

fun findNode(codeToFind:String,doc: Document, datos: ExcelEntry){

    val nodeList: NodeList = doc.getElementsByTagName("Item")
    for (i in 0 until nodeList.length) {
        val node = nodeList.item(i)
        if (node.nodeType == Element.ELEMENT_NODE) {
            val element = node as Element
            val code = element.getAttribute("code")
            if (code == codeToFind) {

                añadirNameDetails(element,datos,doc)
                addTextToDefinition(element, doc, datos)

            }
        }
    }

}

fun addTextToDefinition(element: Element, doc: Document, datos: ExcelEntry) {
    val definitionList = element.getElementsByTagName("Definition")
    if (definitionList.length > 0) {
        val definitionNode = definitionList.item(0) as Element
        val existingTextNodes = definitionNode.getElementsByTagName("Text")
        var textNodePresent = false
        for (i in 0 until existingTextNodes.length) {
            val node = existingTextNodes.item(i)
            val langAttr = node.attributes.getNamedItem("lang").nodeValue
            if (langAttr == languageFormatter(datos.idioma)) {
                textNodePresent = true
                break
            }
        }
        if (!textNodePresent) {
            val textElement = doc.createElement("Text")
            textElement.setAttribute("lang", languageFormatter(datos.idioma))
            textElement.appendChild(doc.createTextNode(datos.textoAyuda))
            definitionNode.appendChild(textElement)
        }
    }
}


fun añadirNameDetails(element:Element, datos: ExcelEntry, doc: Document){
    val name = element.getElementsByTagName("Name").item(0) as Element
    var nameDetail: Element? = null
    val existingNameDetails = name.getElementsByTagName("NameDetail")
    for (j in 0 until existingNameDetails.length) {
        val detail = existingNameDetails.item(j) as Element
        if (detail.getAttribute("lang") == languageFormatter(datos.idioma)) {
            nameDetail = detail
            break
        }
    }
    if (nameDetail == null) {
        nameDetail = doc.createElement("NameDetail")
        nameDetail.setAttribute("lang", languageFormatter(datos.idioma))
        name.appendChild(nameDetail)

        val nameElement = doc.createElement("Name")
        nameElement.appendChild(doc.createTextNode(datos.nombreCampo))
        nameDetail?.appendChild(nameElement)

        val shortNameElement = doc.createElement("ShortName")
        shortNameElement.appendChild(doc.createTextNode(datos.nombreCampo))
        nameDetail?.appendChild(shortNameElement)
    }
}

fun languageFormatter(lang:String):String{

    when(lang.lowercase()){
        "español" -> return "spa"
        "catalán" -> return "cat"
        "euskera" -> return "eus"
        "francés" -> return "fra"
        "gallego" -> return "glg"
        "inglés" -> return "eng"
        else -> return "error"
    }
}

fun removeEmptyTextNodes(node: Node) {
    val childNodes = node.childNodes
    var i = 0
    while (i < childNodes.length) {
        val child = childNodes.item(i)
        if (child.nodeType == Node.TEXT_NODE && child.nodeValue.trim { it <= ' ' }.isEmpty()) {
            node.removeChild(child)
        } else {
            removeEmptyTextNodes(child)
            i++
        }
    }
}

fun readRestantLanguages(xmlPath: String) {
    try {
        val desktopPath = System.getProperty("user.home") + "/Desktop/"
        val outputFile = File(desktopPath, "SpecificationManual Languages Faltantes.txt")
        val xmlFile = File(xmlPath)
        val dbFactory = DocumentBuilderFactory.newInstance()
        val dBuilder = dbFactory.newDocumentBuilder()
        val doc = dBuilder.parse(xmlFile)

        val writer = BufferedWriter(FileWriter(outputFile))

        // Obtener cada elemento 'Item'
        val itemList: NodeList = doc.getElementsByTagName("Item")
        for (i in 0 until itemList.length) {
            val itemNode = itemList.item(i)
            if (itemNode.nodeType == Element.ELEMENT_NODE) {
                val itemElement = itemNode as Element

                // Obtener el elemento 'Name' dentro de 'Item'
                val nameElement = itemElement.getElementsByTagName("Name").item(0) as Element

                // Contar la cantidad de 'NameDetail' dentro de 'Name'
                val nameDetailList: NodeList = nameElement.getElementsByTagName("NameDetail")
                val nameDetailCount = nameDetailList.length

                // Escribir en el archivo si no hay exactamente 6 'NameDetails'
                if (nameDetailCount != 6) {
                    val content = "Item code: ${itemElement.getAttribute("code")}, NameDetails count: $nameDetailCount\n"
                    writer.write(content)
                }
            }
        }

        writer.close()
        println("Se ha escrito el archivo exitosamente en el escritorio.")

    } catch (e: Exception) {
        e.printStackTrace()
        println("Error al procesar el archivo XML.")
    }
}