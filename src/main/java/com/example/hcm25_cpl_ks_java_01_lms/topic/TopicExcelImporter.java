package com.example.hcm25_cpl_ks_java_01_lms.topic;

import com.example.hcm25_cpl_ks_java_01_lms.course.Course;
import com.example.hcm25_cpl_ks_java_01_lms.course.CourseRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class TopicExcelImporter {
    public static List<Topic> importTopics(InputStream inputStream, CourseRepository courseRepository) throws IOException {
        List<Topic> topicsToImport = new ArrayList<>();
        Workbook workbook = new XSSFWorkbook(inputStream);

        Sheet sheet = workbook.getSheetAt(0);
        Iterator<Row> rows = sheet.iterator();

        int rowNumber = 0;
        while (rows.hasNext()) {
            Row currentRow = rows.next();
            if (rowNumber == 0) { // Bỏ qua hàng tiêu đề
                rowNumber++;
                continue;
            }

            Iterator<Cell> cellsInRow = currentRow.iterator();
            Topic topic = new Topic();
            int cellIdx = 0;

            while (cellsInRow.hasNext()) {
                Cell currentCell = cellsInRow.next();
                switch (cellIdx) {
                    case 0: // Bỏ qua ID (nếu có)
                        break;
                    case 1: // Tên topic
                        if (currentCell.getCellType() == CellType.STRING) {
                            topic.setTopicName(currentCell.getStringCellValue().trim());
                        }
                        break;
                    case 2: // Course ID
                        if (currentCell.getCellType() == CellType.NUMERIC) {
                            Long courseId = (long) currentCell.getNumericCellValue();
                            Course course = courseRepository.findById(courseId).orElse(null);
                            topic.setCourse(course);
                        }
                        break;
                    default:
                        break;
                }
                cellIdx++;
            }

            // Chỉ thêm nếu topic có tên và có course hợp lệ
            if (topic.getTopicName() != null && !topic.getTopicName().isEmpty() && topic.getCourse() != null) {
                topicsToImport.add(topic);
            }
        }

        workbook.close();
        return topicsToImport;
    }
}
