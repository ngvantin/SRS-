package com.example.hcm25_cpl_ks_java_01_lms.learningpath;

import com.example.hcm25_cpl_ks_java_01_lms.course.Course;
import com.example.hcm25_cpl_ks_java_01_lms.course.CourseRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Component // Đánh dấu là một Spring Component để có thể Autowired
public class LearningPathExcelImporter {

    private final CourseRepository courseRepository;

    @Autowired
    public LearningPathExcelImporter(CourseRepository courseRepository) {
        this.courseRepository = courseRepository;
    }

    public List<LearningPathWithOrder> importLearningPathsWithOrder(InputStream inputStream) throws IOException {
        List<LearningPathWithOrder> learningPathsWithOrderList = new ArrayList<>();
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
            LearningPath learningPath = new LearningPath();
            List<CourseWithOrderInfo> courseOrderInfos = new ArrayList<>();
            int cellIdx = 0;
            while (cellsInRow.hasNext()) {
                Cell currentCell = cellsInRow.next();
                switch (cellIdx) {
                    case 0:
                        // Bỏ qua cột id
                        break;
                    case 1:
                        learningPath.setName(currentCell.getStringCellValue().trim());
                        break;
                    case 2:
                        learningPath.setDescription(currentCell.getStringCellValue().trim());
                        break;
                    case 3:
                        String courseIdsStr = "";
                        if (currentCell.getCellType() == CellType.NUMERIC) {
                            courseIdsStr = String.valueOf((long) currentCell.getNumericCellValue());
                        } else if (currentCell.getCellType() == CellType.STRING) {
                            courseIdsStr = currentCell.getStringCellValue();
                        }

                        if (courseIdsStr != null && !courseIdsStr.isEmpty()) {
                            String[] courseIdArray = courseIdsStr.split(",");
                            for (int i = 0; i < courseIdArray.length; i++) {
                                String courseIdStr = courseIdArray[i].trim();
                                try {
                                    Long courseId = Long.parseLong(courseIdStr);
                                    Course course = courseRepository.findById(courseId).orElse(null);
                                    if (course != null) {
                                        courseOrderInfos.add(new CourseWithOrderInfo(course, (long) (i + 1)));
                                    } else {
                                        System.err.println("Course not found with ID: " + courseId);
                                        // Xử lý trường hợp không tìm thấy Course
                                    }
                                } catch (NumberFormatException e) {
                                    System.err.println("Invalid Course ID format: " + courseIdStr);
                                    // Xử lý trường hợp ID không phải là số
                                }
                            }
                        }
                        break;
                    default:
                        break;
                }
                cellIdx++;
            }
            learningPathsWithOrderList.add(new LearningPathWithOrder(learningPath, courseOrderInfos));
        }

        workbook.close();
        return learningPathsWithOrderList;
    }

    public static class LearningPathWithOrder {
        private LearningPath learningPath;
        private List<CourseWithOrderInfo> courseOrderInfos;

        public LearningPathWithOrder(LearningPath learningPath, List<CourseWithOrderInfo> courseOrderInfos) {
            this.learningPath = learningPath;
            this.courseOrderInfos = courseOrderInfos;
        }

        public LearningPath getLearningPath() {
            return learningPath;
        }

        public List<CourseWithOrderInfo> getCourseOrderInfos() {
            return courseOrderInfos;
        }
    }

    public static class CourseWithOrderInfo {
        private Course course;
        private Long orderNumber;

        public CourseWithOrderInfo(Course course, Long orderNumber) {
            this.course = course;
            this.orderNumber = orderNumber;
        }

        public Course getCourse() {
            return course;
        }

        public Long getOrderNumber() {
            return orderNumber;
        }
    }
}
//
//public class LearningPathExcelImporter {
//
//    public static List<LearningPath> importLearningPaths(InputStream inputStream, CourseRepository courseRepository) throws IOException {
//        List<LearningPath> learningPaths = new ArrayList<>();
//        Workbook workbook = new XSSFWorkbook(inputStream);
//
//        Sheet sheet = workbook.getSheetAt(0);
//        Iterator<Row> rows = sheet.iterator();
//
//        int rowNumber = 0;
//        while (rows.hasNext()) {
//            Row currentRow = rows.next();
//            if (rowNumber == 0) {
//                rowNumber++;
//                continue;
//            }
//
//            Iterator<Cell> cellsInRow = currentRow.iterator();
//
//            LearningPath learningPath = new LearningPath();
//            List<Course> courses = new ArrayList<>();
//            int cellIdx = 0;
//            while (cellsInRow.hasNext()) {
//                Cell currentCell = cellsInRow.next();
//                switch (cellIdx) {
//                    case 0:
//                        // Bỏ qua cột id
//                        break;
//                    case 1:
//                        learningPath.setName(currentCell.getStringCellValue().trim());
//                        break;
//                    case 2:
//                        learningPath.setDescription(currentCell.getStringCellValue().trim());
//                        break;
//                    case 3:
//                        String courseIds = "";
//                        if (currentCell.getCellType() == CellType.NUMERIC) {
//                            courseIds = String.valueOf((long) currentCell.getNumericCellValue());
//                        } else if (currentCell.getCellType() == CellType.STRING) {
//                            courseIds = currentCell.getStringCellValue();
//                        }
//                        if (courseIds != null && !courseIds.isEmpty()) {
//                            courses = java.util.Arrays.stream(courseIds.split(","))
//                                    .map(String::trim)
//                                    .map(Long::parseLong)
//                                    .map(courseRepository::findById)
//                                    .filter(java.util.Optional::isPresent)
//                                    .map(java.util.Optional::get)
//                                    .collect(Collectors.toList());
//                        }
//                        learningPath.setCourses(courses);
//                        break;
//                    default:
//                        break;
//                }
//                cellIdx++;
//            }
//            learningPaths.add(learningPath);
//        }
//
//        workbook.close();
//        return learningPaths;
//    }
//}

