package com.example.hcm25_cpl_ks_java_01_lms.progress_management;

import com.example.hcm25_cpl_ks_java_01_lms.achievement.AchievementService;
import com.example.hcm25_cpl_ks_java_01_lms.progress_management.material_progress.MaterialProgress;
import com.example.hcm25_cpl_ks_java_01_lms.progress_management.material_progress.MaterialProgressRepository;
import com.example.hcm25_cpl_ks_java_01_lms.user.User;
import com.example.hcm25_cpl_ks_java_01_lms.user.UserRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ProgressManagementService {

    private final ProgressManagementRepository progressManagementRepository;
    private final MaterialProgressRepository materialProgressRepository;
    private final UserRepository userRepository;
    private final AchievementService achievementService;

    public ProgressManagementService(UserRepository userRepository, MaterialProgressRepository materialProgressRepository,
                                     ProgressManagementRepository progressManagementRepository, AchievementService achievementService) {
        this.userRepository = userRepository;
        this.materialProgressRepository = materialProgressRepository;
        this.progressManagementRepository = progressManagementRepository;
        this.achievementService = achievementService;
    }

    public Page<User> getAllUsersProgress(Pageable pageable) {
        return userRepository.findAll(pageable); // Trả về danh sách User trực tiếp
    }

    public Page<ProgressManagement> getAllUserProgress(Long userId, Pageable pageable) {
        return progressManagementRepository.findByUserId(userId, pageable); // Trả về danh sách ProgressManagement trực tiếp
    }

    public Page<User> searchUserProgress(String searchTerm, Pageable pageable) {
        return userRepository.findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCase(searchTerm, searchTerm, pageable);
    }

    public Page<ProgressManagement> searchUserCourses(Long userId, String searchTerm, Pageable pageable) {
        Page<ProgressManagement> progressPage;
        try {
            Long courseId = Long.parseLong(searchTerm);
            progressPage = progressManagementRepository.findByUserIdAndCourseId(userId, courseId, pageable);
        } catch (NumberFormatException e) {
            progressPage = progressManagementRepository.findByUserId(userId, pageable);
        }
        return progressPage;
    }

    public Page<MaterialProgress> searchMaterialsByUserAndCourse(Long userId, Long courseId, String searchTerm, Pageable pageable) {
        try {
            Long materialId = Long.parseLong(searchTerm);
            return materialProgressRepository.findByUserIdAndCourseIdAndMaterialId(userId, courseId, materialId, pageable);
        } catch (NumberFormatException e) {
            return materialProgressRepository.findByUserIdAndCourseId(userId, courseId, pageable);
        }
    }

    public List<ProgressManagement> getUserProgress(Long userId, Long courseId) {
        return progressManagementRepository.findByUserIdAndCourseId(userId, courseId);
    }

    @Transactional
    public void completeMaterial(Long userId, Long courseId, Long sessionId, Long materialId) {
        MaterialProgress materialProgress = materialProgressRepository
                .findByUserIdAndCourseIdAndSessionIdAndMaterialId(userId, courseId, sessionId, materialId)
                .orElseThrow(() -> new RuntimeException("Material not found"));

        if (!materialProgress.getIsCompleted()) {
            materialProgress.setIsCompleted(true);
            materialProgress.setCompletedAt(LocalDateTime.now());
            materialProgressRepository.save(materialProgress);
        }

        updateProgressManagement(userId, courseId);
    }

    private void updateProgressManagement(Long userId, Long courseId) {
        Page<MaterialProgress> allMaterialsPage = materialProgressRepository.findByUserIdAndCourseId(userId, courseId, Pageable.unpaged());
        List<MaterialProgress> allMaterials = allMaterialsPage.getContent();

        if (allMaterials.isEmpty()) return;

        long completedCount = allMaterials.stream().filter(MaterialProgress::getIsCompleted).count();
        float progressPercent = (float) completedCount / allMaterials.size() * 100;

        List<ProgressManagement> progressManagementList = progressManagementRepository.findByUserIdAndCourseId(userId, courseId);

        ProgressManagement progressManagement;
        if (!progressManagementList.isEmpty()) {
            progressManagement = progressManagementList.get(0);
        } else {
            progressManagement = new ProgressManagement(null, userId, courseId, 0f, "not_started", LocalDateTime.now());
        }

        if (progressManagement.getProgressPercent() != progressPercent) {
            progressManagement.setProgressPercent(progressPercent);
            progressManagement.setLastUpdated(LocalDateTime.now());

            if (progressPercent == 100) {
                progressManagement.setStatus("completed");
                achievementService.createAchievementFromCourseCompletion(progressManagement);

            } else if (progressPercent > 0) {
                progressManagement.setStatus("in_progress");
            }

            progressManagementRepository.save(progressManagement);
        }
    }

    public Page<MaterialProgress> getMaterialsByUserAndCourse(Long userId, Long courseId, Pageable pageable) {
        return materialProgressRepository.findByUserIdAndCourseId(userId, courseId, pageable);
    }

    @Transactional
    public ProgressManagement updateProgress(ProgressManagement progress) {
        List<ProgressManagement> progressList = progressManagementRepository.findByUserIdAndCourseId(progress.getUserId(), progress.getCourseId());

        if (progressList.isEmpty()) {
            throw new RuntimeException("Không tìm thấy tiến trình học tập cho userId: " + progress.getUserId() + " và courseId: " + progress.getCourseId());
        }

        ProgressManagement progressManagement = progressList.get(0); // Lấy tiến trình đầu tiên
        progressManagement.setProgressPercent(progress.getProgressPercent());
        progressManagement.setStatus(progress.getStatus());
        progressManagement.setLastUpdated(LocalDateTime.now());

        return progressManagementRepository.save(progressManagement);
    }

    public ByteArrayResource exportUserProgressToExcel() {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("User Progress");
            
            // Create header style
            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            
            // Create headers
            Row headerRow = sheet.createRow(0);
            String[] headers = {"User ID", "Username", "Email", "Total Courses", "Completed Courses", "Last Updated"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }
            
            // Add data
            List<User> users = userRepository.findAll();
            int rowNum = 1;
            for (User user : users) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(user.getId());
                row.createCell(1).setCellValue(user.getUsername());
                row.createCell(2).setCellValue(user.getEmail());
                
                // Get progress data
                Page<ProgressManagement> progressPage = progressManagementRepository.findByUserId(user.getId(), Pageable.unpaged());
                long totalCourses = progressPage.getTotalElements();
                long completedCourses = progressPage.getContent().stream()
                        .filter(course -> "completed".equalsIgnoreCase(course.getStatus()))
                        .count();
                
                row.createCell(3).setCellValue(totalCourses);
                row.createCell(4).setCellValue(completedCourses);
                
                // Get last updated date
                LocalDateTime lastUpdated = progressPage.getContent().stream()
                        .map(ProgressManagement::getLastUpdated)
                        .max(LocalDateTime::compareTo)
                        .orElse(LocalDateTime.now());
                row.createCell(5).setCellValue(lastUpdated.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
            }
            
            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            
            // Write to byte array
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return new ByteArrayResource(outputStream.toByteArray());
        } catch (Exception e) {
            throw new RuntimeException("Failed to export user progress", e);
        }
    }

    public ByteArrayResource exportUserCourseProgressToExcel(Long userId) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Course Progress");
            
            // Create header style
            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            
            // Create headers
            Row headerRow = sheet.createRow(0);
            String[] headers = {"Course ID", "Progress", "Status", "Completed Lessons", "Last Updated"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }
            
            // Add data
            Page<ProgressManagement> progressPage = progressManagementRepository.findByUserId(userId, Pageable.unpaged());
            int rowNum = 1;
            for (ProgressManagement progress : progressPage.getContent()) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(progress.getCourseId());
                row.createCell(1).setCellValue(String.format("%.1f%%", progress.getProgressPercent()));
                row.createCell(2).setCellValue(progress.getStatus());
                
                // Get material progress
                Page<MaterialProgress> materialsPage = materialProgressRepository.findByUserIdAndCourseId(userId, progress.getCourseId(), Pageable.unpaged());
                long totalMaterials = materialsPage.getTotalElements();
                long completedMaterials = materialsPage.getContent().stream()
                        .filter(MaterialProgress::getIsCompleted)
                        .count();
                
                row.createCell(3).setCellValue(completedMaterials + "/" + totalMaterials);
                row.createCell(4).setCellValue(progress.getLastUpdated().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
            }
            
            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            
            // Write to byte array
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return new ByteArrayResource(outputStream.toByteArray());
        } catch (Exception e) {
            throw new RuntimeException("Failed to export course progress", e);
        }
    }

    public ByteArrayResource exportMaterialProgressToExcel(Long userId, Long courseId) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Material Progress");
            
            // Create header style
            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            
            // Create headers
            Row headerRow = sheet.createRow(0);
            String[] headers = {"Material ID", "Material Name", "Status", "Completion Date"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }
            
            // Add data
            Page<MaterialProgress> materialsPage = materialProgressRepository.findByUserIdAndCourseId(userId, courseId, Pageable.unpaged());
            int rowNum = 1;
            for (MaterialProgress material : materialsPage.getContent()) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(material.getMaterialId());
                row.createCell(1).setCellValue("Material " + material.getMaterialId());
                row.createCell(2).setCellValue(material.getIsCompleted() ? "Completed" : "Not Completed");
                row.createCell(3).setCellValue(material.getIsCompleted() && material.getCompletedAt() != null ? 
                    material.getCompletedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : "N/A");
            }
            
            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            
            // Write to byte array
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return new ByteArrayResource(outputStream.toByteArray());
        } catch (Exception e) {
            throw new RuntimeException("Failed to export material progress", e);
        }
    }
}