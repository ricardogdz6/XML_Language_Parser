package ReferenceTables

import SpecificationManual.*
import SpecificationManual.findNode
import org.jdom2.input.SAXBuilder
import org.jdom2.output.Format
import org.jdom2.output.XMLOutputter
import org.jsoup.Jsoup
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import org.xml.sax.InputSource
import java.io.*
import java.nio.charset.StandardCharsets
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.OutputKeys
import javax.xml.transform.Transformer
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult


fun readXmlReferenced(xmlPath:String, datos:List<ReferencedEntry>){

    val xmlFile = File(xmlPath)
    val dbFactory = DocumentBuilderFactory.newInstance()
    val dBuilder = dbFactory.newDocumentBuilder()
    val doc = dBuilder.parse(xmlFile)

    // Process the XML document as needed
    datos.forEach {
        findNode(doc, it)
    }

    // Remove empty text nodes to avoid vertical spacing
    removeEmptyTextNodes(doc)

    // Write the updated document back to the file without reordering attributes
    val transformerFactory = TransformerFactory.newInstance()
    val transformer = transformerFactory.newTransformer()
    transformer.setOutputProperty(OutputKeys.INDENT, "yes")
    transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2")
    transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes")
    transformer.setOutputProperty(OutputKeys.METHOD, "xml")
    transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8")

    val source = DOMSource(doc)
    val result = StreamResult(OutputStreamWriter(FileOutputStream(xmlFile), StandardCharsets.UTF_8))
    transformer.transform(source, result)

    reordenarAtributosDeTabla(xmlPath)

    println("XML file updated successfully.")

}


// Función para ordenar los atributos
fun orderAttributes(doc: Document) {
    val nodeList = doc.getElementsByTagName("Table")
    for (i in 0 until nodeList.length) {
        val element = nodeList.item(i) as Element
        val attributes = element.attributes
        val orderedAttributes = linkedMapOf<String, String>()
        val attributeNames = listOf("name", "version", "source", "XMLDataType", "XMLProperty", "XMLIndicator")
        for (name in attributeNames) {
            val item = attributes.getNamedItem(name)
            if (item != null) {
                orderedAttributes[name] = item.nodeValue
            }
        }
        element.removeAttribute("name")
        element.removeAttribute("version")
        element.removeAttribute("source")
        element.removeAttribute("XMLDataType")
        element.removeAttribute("XMLProperty")
        element.removeAttribute("XMLIndicator")
        for ((key, value) in orderedAttributes) {
            element.setAttribute(key, value)
        }
    }
}



fun findNode(doc: Document, datos: ReferencedEntry) {
    val nodeList: NodeList = doc.getElementsByTagName("Table")
    for (i in 0 until nodeList.length) {
        val node = nodeList.item(i)
        if (node.nodeType == Element.ELEMENT_NODE) {
            val element = node as Element
            val name = element.getAttribute("name")
            if (name == datos.tabla) {
                val itemNodes = element.getElementsByTagName("Item")
                for (j in 0 until itemNodes.length) {
                    val itemNode = itemNodes.item(j)
                    if (itemNode.nodeType == Element.ELEMENT_NODE) {
                        val itemElement = itemNode as Element
                        val codeNodes = itemElement.getElementsByTagName("Code")
                        for (k in 0 until codeNodes.length) {
                            val codeNode = codeNodes.item(k)
                            if (codeNode.nodeType == Element.ELEMENT_NODE) {
                                val codeElement = codeNode as Element
                                val codeValue = codeElement.textContent.trim()
                                if (parseCode(codeValue) == datos.code) {

                                    val itemElement = itemNode as Element
                                    val nameElement = itemElement.getElementsByTagName("Name").item(0) as Element

                                    val nameDetailNodes = nameElement.getElementsByTagName("NameDetail")
                                    for (m in 0 until nameDetailNodes.length) {
                                        val currentNameDetail = nameDetailNodes.item(m) as Element
                                        if (currentNameDetail.getAttribute("lang") == languageFormatter(datos.idioma)) {
                                            nameElement.removeChild(currentNameDetail)
                                            break
                                        }
                                    }

                                    // Crear el nuevo elemento <NameDetail>
                                    val nameDetailElement = doc.createElement("NameDetail")
                                    // Establecer el atributo "lang" en el elemento <NameDetail>
                                    nameDetailElement.setAttribute("lang", languageFormatter(datos.idioma))
                                    // Crear el elemento <Name> dentro de <NameDetail> con el texto especificado
                                    val nameElementChild = doc.createElement("Name")
                                    nameElementChild.textContent = datos.traduccion
                                    // Crear el elemento <ShortName> dentro de <NameDetail> con el texto especificado
                                    val shortNameElement = doc.createElement("ShortName")
                                    shortNameElement.textContent = datos.traduccion
                                    // Agregar <NameDetail> como hijo de <Name>
                                    nameElement.appendChild(nameDetailElement)
                                    // Agregar <Name> como hijo de <NameDetail>
                                    nameDetailElement.appendChild(nameElementChild)
                                    // Agregar <ShortName> como hijo de <NameDetail>
                                    nameDetailElement.appendChild(shortNameElement)

                                }
                            }
                        }
                    }
                }
            }
        }
    }
}


fun parseCode(code: String): String {
    val trimmedCode = code.trimStart('0')
    return if (trimmedCode.isEmpty()) "0" else trimmedCode
}


fun languageFormatter(lang:String):String{

    when(lang.lowercase()){
        "español" -> return "spa"
        "catalán" -> return "cat"
        "espanyol" -> return "cat"
        "euskera" -> return "eus"
        "gaztelania" -> return "eus"
        "francés" -> return "fra"
        "français" -> return "fra"
        "galego" -> return "glg"
        "english" -> return "eng"
        "inglés" -> return "eng"
        else -> return "error"
    }
}

fun nodeToString(node: Node): String {
    val sw = StringWriter()
    try {
        val tf = TransformerFactory.newInstance()
        val transformer = tf.newTransformer()
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes")
        transformer.transform(DOMSource(node), StreamResult(sw))
    } catch (ex: Exception) {
        println("Error al convertir el nodo a String: $ex")
    }
    return sw.toString()
}

fun readRestantLanguages(xmlPath: String) {
    try {
        val desktopPath = System.getProperty("user.home") + "/Desktop/"
        val outputFile = File(desktopPath, "Referenced Languages Faltantes.txt")
        val xmlFile = File(xmlPath)
        val dbFactory = DocumentBuilderFactory.newInstance()
        val dBuilder = dbFactory.newDocumentBuilder()
        val doc = dBuilder.parse(xmlFile)

        val writer = BufferedWriter(FileWriter(outputFile))

        // Obtener cada elemento 'Table'
        val tableList: NodeList = doc.getElementsByTagName("Table")
        for (i in 0 until tableList.length) {
            val tableNode = tableList.item(i)
            if (tableNode.nodeType == Element.ELEMENT_NODE) {
                val tableElement = tableNode as Element
                val tableName = tableElement.getAttribute("name")

                // Obtener el elemento 'Name' dentro de 'Item' dentro de 'Table'
                val itemList: NodeList = tableElement.getElementsByTagName("Item")
                for (j in 0 until itemList.length) {
                    val itemNode = itemList.item(j)
                    if (itemNode.nodeType == Element.ELEMENT_NODE) {
                        val itemElement = itemNode as Element

                        // Obtener el elemento 'Name' dentro de 'Item'
                        val nameElement = itemElement.getElementsByTagName("Name").item(0) as Element

                        // Contar la cantidad de 'NameDetail' dentro de 'Name'
                        val nameDetailList: NodeList = nameElement.getElementsByTagName("NameDetail")
                        val nameDetailCount = nameDetailList.length

                        // Escribir en el archivo si no hay exactamente 6 'NameDetails'
                        if (nameDetailCount != 6) {
                            val code = itemElement.getElementsByTagName("Code").item(0).textContent
                            val content = "Table name: $tableName, Item code: $code, NameDetails count: $nameDetailCount\n"
                            writer.write(content)
                        }
                    }
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

fun reordenarAtributosDeTabla(filePath: String) {
    val file = File(filePath)
    val builder = SAXBuilder()
    val document: org.jdom2.Document? = builder.build(file)
    val rootElement: org.jdom2.Element? = document!!.rootElement

    val tableList = rootElement!!.getChildren("Table")
    for (table in tableList) {
        table.attributes.sortBy {
            when (it.name) {
                "name" -> 1
                "version" -> 2
                "source" -> 3
                "XMLDataType" -> 4
                "XMLProperty" -> 5
                "XMLIndicator" -> 6
                else -> 7
            }
        }
    }

    val xmlOutputter = XMLOutputter()
    xmlOutputter.format = Format.getPrettyFormat()
    val writer = FileWriter(file)
    xmlOutputter.output(document, writer)
    writer.close()
}
