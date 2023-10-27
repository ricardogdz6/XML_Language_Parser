import ReferenceTables.readExcelReferenced
import ReferenceTables.readXmlReferenced
import ReferenceTables.reordenarAtributosDeTabla
import SpecificationManual.readExcelSpecification
import SpecificationManual.readRestantLanguages
import SpecificationManual.readXmlSpecification

// Este SCRIPT está escrito en Kotlin. Tiene un IDE gratuito llamado IntelliJ o también puedes ejecutarlo
// con un plugin tanto en VSCode como Eclipse

fun main() {

    //Descomenta la función que quieras usar

    //parseSpecificationManual()
    parseReferenceTables()
    //countRestantSpecification()
    //countRestantReference()

}


//Añadir idiomas de un archivo al XML de Specification
fun parseSpecificationManual(){

    val rutaXmlInput = "C:\\Users\\Ricar\\Desktop\\SpecificationManual.xml"
    val rutaLanguages = "C:\\Users\\Ricar\\Desktop\\items_curriculares\\060.000.000.000.xlsx"

    val datos = readExcelSpecification(rutaLanguages)
    readXmlSpecification(rutaXmlInput, datos)

}

//Añadir idiomas de una carpeta con archivos EXCEL para el XML de Reference (Este sustituye campos)
fun parseReferenceTables(){

    val rutaXmlInput = "C:\\Users\\Ricar\\Desktop\\ReferenceTables.xml"
    val rutaCarpetaExcels = "C:\\Users\\Ricar\\Desktop\\tablas_referencia"

    val datos = readExcelReferenced(rutaCarpetaExcels)

    readXmlReferenced(rutaXmlInput,datos)

}

fun countRestantSpecification(){
    val rutaXmlInput = "C:\\Users\\Ricar\\Desktop\\SpecificationManual.xml"
    readRestantLanguages(rutaXmlInput)
}

fun countRestantReference(){

    val rutaXmlInput = "C:\\Users\\Ricar\\Desktop\\ReferenceTables.xml"
    ReferenceTables.readRestantLanguages(rutaXmlInput)

}


