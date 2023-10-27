package ReferenceTables

import org.apache.poi.ss.usermodel.DataFormatter
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileInputStream

fun readExcelReferenced(path: String): List<ReferencedEntry> {

    val result = mutableListOf<ReferencedEntry>()
    val files = mutableListOf<File>()

    val directory = File(path)

    if (directory.isDirectory) {
        directory.listFiles()?.let { fileList ->
            files.addAll(fileList)
        }
    }

    files.forEach { file ->
        val fis = FileInputStream(file)
        val workbook = XSSFWorkbook(fis)
        val dataFormatter = DataFormatter()

        for (i in 0 until workbook.numberOfSheets) {
            val sheet = workbook.getSheetAt(i)
            var firstRowSkipped = false

            for (row in sheet) {
                if (!firstRowSkipped) {
                    firstRowSkipped = true
                    continue
                }

                val entries = mutableListOf<String>()
                for (cell in row) {
                    val cellValue = dataFormatter.formatCellValue(cell)
                    if (cellValue.isNotEmpty()) {
                        entries.add(cellValue)
                        if (entries.size == 4) break
                    }
                }
                if (entries.size >= 4) {
                    val referencedEntry = ReferencedEntry(
                        entries[0],
                        entries[1],
                        entries[2],
                        entries[3]
                    )
                    result.add(referencedEntry)
                }
            }
        }
        workbook.close()
        fis.close()
    }

    return result
}
