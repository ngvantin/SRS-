package com.example.hcm25_cpl_ks_java_01_lms.tag;

import com.example.hcm25_cpl_ks_java_01_lms.topic.Topic;
import com.example.hcm25_cpl_ks_java_01_lms.topic.TopicRepository;
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
public class TagExcelImporter {

    public static List<Tag> importTags(InputStream inputStream, TopicRepository topicRepository, TagRepository tagRepository) throws IOException {
        List<Tag> tagsToImport = new ArrayList<>();
        Workbook workbook = new XSSFWorkbook(inputStream);

        Sheet sheet = workbook.getSheetAt(0);
        Iterator<Row> rows = sheet.iterator();

        int rowNumber = 0;
        while (rows.hasNext()) {
            Row currentRow = rows.next();
            if (rowNumber == 0) {
                rowNumber++;
                continue;
            }

            Iterator<Cell> cellsInRow = currentRow.iterator();

            Tag tag = new Tag();
            int cellIdx = 0;
            while (cellsInRow.hasNext()) {
                Cell currentCell = cellsInRow.next();
                switch (cellIdx) {
                    case 0:
                        break;
                    case 1:
                        tag.setTagName(currentCell.getStringCellValue().trim());
                        break;
                    case 2:
                        Integer topicId = (int) currentCell.getNumericCellValue();
                        Topic topic = topicRepository.findById(topicId).orElse(null);
                        tag.setTopic(topic);
                        break;
                    default:
                        break;
                }
                cellIdx++;
            }
            tagsToImport.add(tag);
        }
        workbook.close();
        return tagsToImport;
    }
}