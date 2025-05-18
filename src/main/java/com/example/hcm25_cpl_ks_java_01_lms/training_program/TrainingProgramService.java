package com.example.hcm25_cpl_ks_java_01_lms.training_program;

import com.example.hcm25_cpl_ks_java_01_lms.syllabus.Syllabus;
import jakarta.transaction.Transactional;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@Service
public class TrainingProgramService {
    private final TrainingProgramRepository trainingProgramRepository;

    @Autowired
    public TrainingProgramService(TrainingProgramRepository trainingProgramRepository) {
        this.trainingProgramRepository = trainingProgramRepository;
    }

    // Lấy danh sách chương trình đào tạo với tìm kiếm theo tên hoặc mã chương trình
    public Page<TrainingProgram> getAllTrainingPrograms(String searchTerm, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        if (searchTerm != null && !searchTerm.isEmpty()) {
            return trainingProgramRepository.findByProgramNameContainingIgnoreCase(searchTerm, pageable);
        }
        return trainingProgramRepository.findAll(pageable);
    }

    public List<TrainingProgram> searchTrainingPrograms(String searchTerm) {
        if (searchTerm != null && !searchTerm.trim().isEmpty()) {
            return trainingProgramRepository.findByProgramNameContainingIgnoreCaseOrProgramCodeContainingIgnoreCase(
                    searchTerm, searchTerm
            );
        }
        return trainingProgramRepository.findAll();
    }


    // Tạo mới chương trình đào tạo
    public void createTrainingProgram(TrainingProgram trainingProgram) {
        trainingProgramRepository.save(trainingProgram);
    }

    // Lấy chương trình đào tạo theo ID
    public TrainingProgram getTrainingProgramById(Long id) {
        return trainingProgramRepository.findById(id).orElse(null);
    }

    // Cập nhật chương trình đào tạo
    public void updateTrainingProgram(TrainingProgram trainingProgramDetails) {
        trainingProgramRepository.save(trainingProgramDetails);
    }

    // Xóa chương trình đào tạo theo ID
    public void deleteTrainingProgram(Long id) {
        trainingProgramRepository.deleteById(id);
    }

    // Xuất dữ liệu chương trình đào tạo ra file Excel
    public ByteArrayInputStream exportToExcel(List<TrainingProgram> trainingPrograms) throws IOException {
        return generateExcel(trainingPrograms);
    }

    private ByteArrayInputStream generateExcel(List<TrainingProgram> trainingPrograms) throws IOException {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Training Programs");

            // Header
            Row headerRow = sheet.createRow(0);
            CellStyle headerCellStyle = workbook.createCellStyle();
            Font font = workbook.createFont();
            font.setBold(true);
            headerCellStyle.setFont(font);

            String[] headers = {"ID", "Program Name", "Program Code", "Description", "Version", "Content Link", "Syllabuses"};
            for (int col = 0; col < headers.length; col++) {
                Cell cell = headerRow.createCell(col);
                cell.setCellValue(headers[col]);
                cell.setCellStyle(headerCellStyle);
            }

            // Data
            int rowIdx = 1;
            for (TrainingProgram trainingProgram : trainingPrograms) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(trainingProgram.getId());
                row.createCell(1).setCellValue(trainingProgram.getProgramName());
                row.createCell(2).setCellValue(trainingProgram.getProgramCode());
                row.createCell(3).setCellValue(trainingProgram.getDescription() != null ? trainingProgram.getDescription() : "");
                row.createCell(4).setCellValue(trainingProgram.getVersion());
                row.createCell(5).setCellValue(trainingProgram.getContentLink());

                // Syllabuses
                StringBuilder syllabusesBuilder = new StringBuilder();
                for (Syllabus syllabus : trainingProgram.getSyllabuses()) {
                    if (syllabusesBuilder.length() > 0) syllabusesBuilder.append(", ");
                    syllabusesBuilder.append(syllabus.getSyllabusCode());
                }
                row.createCell(4).setCellValue(syllabusesBuilder.toString());
            }

            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        }
    }

    // Lưu tất cả chương trình đào tạo từ Excel
    public void saveAllFromExcel(List<TrainingProgram> trainingPrograms) {
        trainingProgramRepository.saveAll(trainingPrograms);
    }

    @Transactional
    public void deleteTrainingProgramsByIds(List<Long> ids) {
        trainingProgramRepository.deleteAllById(ids);
    }
}
