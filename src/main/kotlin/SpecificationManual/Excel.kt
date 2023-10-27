package SpecificationManual

import org.apache.poi.ss.usermodel.WorkbookFactory
import java.io.File

fun readExcelSpecification(rutaLanguages:String):List<ExcelEntry>{

    val entryList = mutableListOf<ExcelEntry>()

    val file = File(rutaLanguages)
    val workbook = WorkbookFactory.create(file)

    for (i in 0 until workbook.numberOfSheets){

        val sheet = workbook.getSheetAt(i)

        //Obtengo el idioma
        val row = sheet.getRow(1)
        val cellB2 = row.getCell(1)

        when {
            cellB2.toString().lowercase() == "español" -> {
                //Español ya está añadido, lo ignoro
            }

            else -> {

                for (i in 1..sheet.lastRowNum) {
                    val row = sheet.getRow(i)
                    if (row?.cellIterator()?.asSequence()?.any { it?.stringCellValue?.isNotBlank() == true } == true) {
                        val identificador = row.getCell(0)?.toString() ?: ""
                        val idioma = row.getCell(1)?.toString() ?: ""
                        val nombreCampo = row.getCell(2)?.toString() ?: ""
                        val textoAyuda = row.getCell(3)?.toString() ?: ""

                        if (identificador.isNotBlank() || idioma.isNotBlank() || nombreCampo.isNotBlank() || textoAyuda.isNotBlank()) {
                            entryList.add(ExcelEntry(identificador,idioma,nombreCampo,textoAyuda))
                        }
                    }
                }
            }
        }

    }

    workbook.close()
    return entryList

}