package com.example.hcm25_cpl_ks_java_01_lms.student;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class StudentExcelImporter {
    public static List<Student> importStudents(InputStream inputStream) throws IOException {
        List<Student> students = new ArrayList<>();
        Workbook workbook = new XSSFWorkbook(inputStream);

        Sheet sheet = workbook.getSheetAt(0);
        Iterator<Row> rows = sheet.iterator();

        int rowNumber = 0;
        while (rows.hasNext()) {
            Row currentRow = rows.next();
            // Skip header
            if (rowNumber == 0) {
                rowNumber++;
                continue;
            }

            Iterator<Cell> cellsInRow = currentRow.iterator();

            Student student = new Student();
            int cellIdx = 0;
            while (cellsInRow.hasNext()) {
                Cell currentCell = cellsInRow.next();
                switch (cellIdx) {
                    case 0:
                        student.setStudentCode(currentCell.getStringCellValue());
                        break;
                    case 1:
                        student.setLastName(currentCell.getStringCellValue());
                        break;
                    case 2:
                        student.setFirstName(currentCell.getStringCellValue());
                        break;
                    case 3:
                        student.setEmail(currentCell.getStringCellValue());
                        break;
                    case 4:
                        student.setPhoneNumber(currentCell.getStringCellValue());
                        break;
                    case 5:
                        student.setAddress(currentCell.getStringCellValue());
                        break;
                    default:
                        break;
                }
                cellIdx++;
            }

            students.add(student);
        }

        workbook.close();
        return students;
    }
}
