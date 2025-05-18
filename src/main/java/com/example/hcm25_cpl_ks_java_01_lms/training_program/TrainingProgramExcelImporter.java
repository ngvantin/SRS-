package com.example.hcm25_cpl_ks_java_01_lms.training_program;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.StreamSupport;

public class TrainingProgramExcelImporter {

    private static final Logger LOGGER = LoggerFactory.getLogger(TrainingProgramExcelImporter.class);

    public static List<TrainingProgram> importTrainingPrograms(InputStream inputStream) {
        List<TrainingProgram> trainingPrograms = new ArrayList<>();

        try (Workbook workbook = new XSSFWorkbook(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);
            StreamSupport.stream(sheet.spliterator(), false)
                    .skip(1)
                    .map(TrainingProgramExcelImporter::parseRow)
                    .filter(Objects::nonNull)
                    .forEach(trainingPrograms::add);
        } catch (IOException e) {
            LOGGER.error("Lỗi khi đọc file Excel: {}", e.getMessage());
        }
        return trainingPrograms;
    }

    private static TrainingProgram parseRow(Row row) {
        try {
            String programName = getStringValue(row.getCell(1));
            String programCode = getStringValue(row.getCell(2));
            String description = getStringValue(row.getCell(3));
            String contentLink = getStringValue(row.getCell(5));
            String versionString = getStringValue(row.getCell(4));


            double version = 0.0;
            try {
                version = versionString.isEmpty() ? 0.0 : Double.parseDouble(versionString);
            } catch (NumberFormatException e) {
                LOGGER.warn("Lỗi định dạng version ở dòng {}: {}", row.getRowNum(), e.getMessage());
            }

            ValidationResult validation = validateRow(programName, programCode);
            if (!validation.isValid) {
                LOGGER.warn("Bỏ qua dòng {}: {}", row.getRowNum(), validation.message);
                return null;
            }

            return TrainingProgram.builder()
                    .programName(programName)
                    .programCode(programCode)
                    .description(description)
                    .version(version)
                    .contentLink(contentLink)
                    .build();
        } catch (Exception e) {
            LOGGER.warn("Lỗi khi đọc hàng {}: {}", row.getRowNum(), e.getMessage());
            return null;
        }
    }

    private static String getStringValue(Cell cell) {
        if (cell == null) return "";
        try {
            return switch (cell.getCellType()) {
                case STRING -> cell.getStringCellValue().trim();
                case NUMERIC -> String.valueOf((int) cell.getNumericCellValue());
                case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
                default -> "";
            };
        } catch (Exception e) {
            LOGGER.warn("Lỗi khi lấy giá trị chuỗi: {}", e.getMessage());
            return "";
        }
    }

    private static ValidationResult validateRow(String programName, String programCode) {
        if (programName.isEmpty() || programCode.isEmpty()) {
            return new ValidationResult(false, "Tên hoặc mã chương trình không được để trống");
        }
        return new ValidationResult(true, "Hợp lệ");
    }

    private static class ValidationResult {
        private final boolean isValid;
        private final String message;

        public ValidationResult(boolean isValid, String message) {
            this.isValid = isValid;
            this.message = message;
        }
    }
}