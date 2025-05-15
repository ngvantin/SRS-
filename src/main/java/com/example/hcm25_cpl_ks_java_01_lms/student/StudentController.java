package com.example.hcm25_cpl_ks_java_01_lms.student;

import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.time.format.DateTimeFormatter;

import java.io.*;
import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Stream;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import org.apache.poi.ss.usermodel.Cell;
import com.itextpdf.layout.property.TextAlignment;
import com.itextpdf.layout.property.UnitValue;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/students")
public class StudentController {
    @Autowired
    private StudentService studentService;

    // api list table student
    @GetMapping
    public ResponseEntity<Page<StudentDTO>> getAllStudentsWithCard(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(name = "search", defaultValue = "") String searchTerm,
            @RequestParam(name = "sort", required = false) String sort ) {

        Pageable pageable = createPageable(page, size, sort);

        Page<Student> studentPage = (searchTerm != null && !searchTerm.trim().isEmpty())
                ? studentService.searchStudents(searchTerm, pageable)
                : studentService.findAllStudents(pageable);

        Page<StudentDTO> dtoPage = studentPage.map(student -> {
            Optional<StudentCard> cardOptional = studentService.findByUserId(student.getId());
            StudentCard card = cardOptional.orElse(null);
            return studentService.toDTO(student, card);
        });

        return ResponseEntity.ok(dtoPage);
    }

    private Pageable createPageable(int page, int size, String sortParam) {
        if (sortParam != null && !sortParam.isBlank()) {
            String[] parts = sortParam.split(",");
            if (parts.length == 2) {
                String sortField = parts[0];
                Sort.Direction direction = parts[1].equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
                return PageRequest.of(page, size, Sort.by(direction, sortField));
            }
        }
        return PageRequest.of(page, size); // no sort
    }

    // api  detail student
    @GetMapping("/{id}")
    public ResponseEntity<StudentDTO> getStudentDetail(@PathVariable Long id) {
        Optional<Student> optionalStudent = studentService.findStudentById(id);
        if (optionalStudent.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Student student = optionalStudent.get();
        Optional<StudentCard> optionalCard = studentService.findByUserId(student.getId());

        StudentCard card = optionalCard.orElse(null);
        StudentDTO dto = studentService.toDTO(student, card); // card có thể null, không sao

        return ResponseEntity.ok(dto);
    }

    // api create new student
    @PostMapping
    public ResponseEntity<?> createStudent(@RequestBody Student student) {
        try {
            Student saved = studentService.createStudent(student);
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Student already exists");
        }
    }

    // api edit student
    @PutMapping("/{id}")
    public ResponseEntity<?> updateStudent(@PathVariable Long id, @RequestBody StudentDTO student) {
        try {
            Student updated = studentService.updateStudent(id, student);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Failed to update student: " + e.getMessage());
        }
    }

    // api edit card
    @PutMapping("/card/{id}")
    public ResponseEntity<?> updateStudentCard(@PathVariable Long id, @RequestBody CreateCardRequest request) {
        try {
            StudentCard updated = studentService.updateStudentCard(id, request);

            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Failed to update student card: " + e.getMessage());
        }
    }

    // api delete student
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteStudent(@PathVariable Long id) {
        try {
            studentService.deleteStudent(id);
            return ResponseEntity.ok("Student deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to delete student");
        }
    }

    // api delete card
    @DeleteMapping("/card/{userId}")
    public ResponseEntity<?> deleteStudentCard(@PathVariable Long userId) {
        try {
            studentService.deleteStudentCardByUserId(userId);
            return ResponseEntity.ok("Student Card deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to delete student card");
        }
    }

    // api delete student
    @PostMapping("/delete-all")
    @Transactional
    public ResponseEntity<?> deleteSelectedStudents(@RequestBody DeleteRequest deleteRequest) {
        try {
            List<Long> ids = deleteRequest.getIds();
            if (ids == null || ids.isEmpty()) {
                return ResponseEntity.badRequest().body("No students selected for deletion");
            }
            for (Long id : ids) {
                studentService.deleteStudent(id);
            }
            return ResponseEntity.ok("Selected students deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to delete students");
        }
    }


    @PostMapping("/import")
    public ResponseEntity<?> importFromJson(@RequestBody List<Map<String, String>> studentsJson) {
        try {
            List<Student> students = new ArrayList<>();
            Map<String, StudentCard> studentCards = new HashMap<>();

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            List<String> requiredFields = Arrays.asList("Username", "Password", "First Name", "Last Name", "Email", "2FA Enabled", "Locked");

            for (Map<String, String> map : studentsJson) {
                // Check missing required fields
                for (String field : requiredFields) {
                    String value = map.get(field);
                    if (value == null || value.trim().isEmpty()) {
                        return ResponseEntity.badRequest().body("Missing required field: " + field);
                    }
                }

                Student student = new Student();
                student.setUsername(map.get("Username"));
                student.setPassword(map.get("Password"));
                student.setFirstName(map.get("First Name"));
                student.setLastName(map.get("Last Name"));
                student.setEmail(map.get("Email"));
                student.setPhoneNumber(map.getOrDefault("Phone Number", null));
                student.setAddress(map.getOrDefault("Address", null));
                student.setStudentCode(map.getOrDefault("Student Code", null));
                student.setIs2faEnabled(Boolean.parseBoolean(map.get("2FA Enabled")));
                student.setIsLocked(Boolean.parseBoolean(map.get("Locked")));

                students.add(student);

                // Nếu có Card Number thì tạo StudentCard
                String cardNumber = map.get("Card Number");
                if (cardNumber != null && !cardNumber.trim().isEmpty()) {
                    StudentCard card = StudentCard.builder()
                            .cardNumber(cardNumber)
                            .badgeHolderTitle(map.getOrDefault("Badge Holder Title", null))
                            .active(Boolean.parseBoolean(map.getOrDefault("Active", "false")))
                            .build();

                    // Parse Issued At nếu có
                    String issuedAtStr = map.get("Issued At");
                    if (issuedAtStr != null && !issuedAtStr.trim().isEmpty()) {
                        try {
                            card.setIssuedAt(LocalDate.parse(issuedAtStr, formatter));
                        } catch (Exception e) {
                            return ResponseEntity.badRequest().body("Invalid date format for Issued At: " + issuedAtStr);
                        }
                    }

                    studentCards.put(student.getUsername(), card);
                }
            }

            List<DuplicateInfo> duplicates = studentService.saveAllStudents(students, studentCards);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Imported successfully!");
            response.put("duplicates", duplicates);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Import failed: " + e.getMessage());
        }
    }

    @PostMapping("/import-card")
    public ResponseEntity<?> importCardFromJson(@RequestBody List<Map<String, String>> cardsJson) {
        try {
            List<StudentCard> cards = new ArrayList<>();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

            for (Map<String, String> map : cardsJson) {
                String cardNumber = map.get("Card Number");
                String badgeHolderTitle = map.get("Badge Holder Title");
                String activeStr = map.get("Active");
                String issuedAtStr = map.get("Issued At");
                String studentCodeStr = map.get("Student Code");

                if (cardNumber == null || badgeHolderTitle == null || activeStr == null || issuedAtStr == null || studentCodeStr == null) {
                    return ResponseEntity.badRequest().body("Missing required fields: Card Number, Badge Holder Title, Active, Issued At, or Student Code.");
                }

                // Parse boolean Active
                boolean active = Boolean.parseBoolean(activeStr.trim());

                // Parse issuedAt date
                LocalDate issuedAt;
                try {
                    issuedAt = LocalDate.parse(issuedAtStr.trim(), formatter);
                } catch (Exception e) {
                    return ResponseEntity.badRequest().body("Invalid Issued At format. Required format: dd/MM/yyyy");
                }

                // Find Student by studentCode
                Student student = studentService.findStudentByStudentCode(studentCodeStr.trim());
                if (student == null) {
                    return ResponseEntity.badRequest().body("Student ID not found: " + studentCodeStr);
                }

                // Build StudentCard
                StudentCard card = StudentCard.builder()
                        .cardNumber(cardNumber.trim())
                        .badgeHolderTitle(badgeHolderTitle.trim())
                        .active(active)
                        .issuedAt(issuedAt)
                        .student(student)
                        .build();

                cards.add(card);
            }

            studentService.saveAllStudentCards(cards);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Imported cards successfully!");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Import failed: " + e.getMessage());
        }
    }

    @GetMapping("/download-template")
    public ResponseEntity<Resource> downloadTemplate() throws IOException {
        String fileName = "student_template.xlsx";
        Path filePath = Paths.get("data-excel/" + fileName);

        if (!Files.exists(filePath)) {
            return ResponseEntity.notFound().build();
        }

        Resource resource = new UrlResource(filePath.toUri());

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .body(resource);
    }

    @GetMapping("/export")
    public ResponseEntity<InputStreamResource> exportAll(
            @RequestParam(defaultValue = "student") String type,    // student hoặc studentCard
            @RequestParam(defaultValue = "excel") String format      // excel, csv, pdf
    ) throws IOException {
        if (type.equalsIgnoreCase("student")) {
            List<Student> students = studentService.findAll();
            return generateExportResponseForStudents(students, format, "students_all");
        } else if (type.equalsIgnoreCase("studentCard")) {
            List<StudentCard> cards = studentService.findAllCards();
            return generateExportResponseForStudentCards(cards, format, "student_cards_all");
        } else {
            throw new IllegalArgumentException("Unsupported export type: " + type);
        }
    }

    @PostMapping("/export-selected")
    public ResponseEntity<InputStreamResource> exportSelected(
            @RequestBody List<Long> ids,
            @RequestParam(defaultValue = "student") String type,
            @RequestParam(defaultValue = "excel") String format
    ) throws IOException {
        if (type.equalsIgnoreCase("student")) {
            List<Student> selectedStudents = studentService.findByIds(ids);
            return generateExportResponseForStudents(selectedStudents, format, "students_selected");
        } else if (type.equalsIgnoreCase("studentCard")) {
            List<StudentCard> selectedCards = studentService.findCardsByUserIds(ids);
            return generateExportResponseForStudentCards(selectedCards, format, "student_cards_selected");
        } else {
            throw new IllegalArgumentException("Unsupported export type: " + type);
        }
    }

    private ResponseEntity<InputStreamResource> generateExportResponseForStudents(List<Student> students, String format, String filename) throws IOException {
        ByteArrayInputStream stream;
        String extension;
        String contentType;

        switch (format.toLowerCase()) {
            case "excel":
                stream = generateStudentExcel(students);
                extension = "xlsx";
                contentType = "application/vnd.ms-excel";
                break;
            case "csv":
                stream = generateStudentCsv(students);
                extension = "csv";
                contentType = "text/csv";
                break;
            case "pdf":
                stream = generateStudentPdf(students);
                extension = "pdf";
                contentType = "application/pdf";
                break;
            default:
                throw new IllegalArgumentException("Unsupported export format: " + format);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=" + filename + "." + extension);

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType(contentType))
                .body(new InputStreamResource(stream));
    }

    private ResponseEntity<InputStreamResource> generateExportResponseForStudentCards(List<StudentCard> cards, String format, String filename) throws IOException {
        ByteArrayInputStream stream;
        String extension;
        String contentType;

        switch (format.toLowerCase()) {
            case "excel":
                stream = generateStudentCardExcel(cards);
                extension = "xlsx";
                contentType = "application/vnd.ms-excel";
                break;
            case "csv":
                stream = generateStudentCardCsv(cards);
                extension = "csv";
                contentType = "text/csv";
                break;
            case "pdf":
                stream = generateStudentCardPdf(cards);
                extension = "pdf";
                contentType = "application/pdf";
                break;
            default:
                throw new IllegalArgumentException("Unsupported export format: " + format);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=" + filename + "." + extension);

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType(contentType))
                .body(new InputStreamResource(stream));
    }

    private ByteArrayInputStream generateStudentExcel(List<Student> students) throws IOException {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Students");

            Row headerRow = sheet.createRow(0);
            CellStyle headerCellStyle = workbook.createCellStyle();
            Font font = workbook.createFont();
            font.setBold(true);
            headerCellStyle.setFont(font);

            String[] headers = {"ID", "Username", "Student Code", "Last Name", "First Name", "Email", "Phone Number", "Address"};
            for (int col = 0; col < headers.length; col++) {
                Cell cell = headerRow.createCell(col);
                cell.setCellValue(headers[col]);
                cell.setCellStyle(headerCellStyle);
            }

            int rowIdx = 1;
            for (Student student : students) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(student.getId());
                row.createCell(1).setCellValue(student.getUsername());
                row.createCell(2).setCellValue(student.getStudentCode());
                row.createCell(3).setCellValue(student.getLastName());
                row.createCell(4).setCellValue(student.getFirstName());
                row.createCell(5).setCellValue(student.getEmail());
                row.createCell(6).setCellValue(student.getPhoneNumber());
                row.createCell(7).setCellValue(student.getAddress());
            }

            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException("Error generating Excel file: " + e.getMessage());
        }
    }

    private ByteArrayInputStream generateStudentCsv(List<Student> students) {
        final CSVFormat format = CSVFormat.Builder.create()
                .setHeader("ID", "Username", "Student Code", "Last Name", "First Name", "Email", "Phone Number", "Address")
                .setSkipHeaderRecord(false)
                .build();

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            // UTF-8 BOM
            out.write(0xEF);
            out.write(0xBB);
            out.write(0xBF);

            try (CSVPrinter csvPrinter = new CSVPrinter(
                    new PrintWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8), true),
                    format)) {

                for (Student student : students) {
                    String phone = "=\"" + student.getPhoneNumber() + "\""; // để tránh excel tự convert

                    csvPrinter.printRecord(
                            student.getId(),
                            student.getUsername(),
                            student.getStudentCode(),
                            student.getLastName(),
                            student.getFirstName(),
                            student.getEmail(),
                            phone,
                            student.getAddress()
                    );
                }

                csvPrinter.flush();
                return new ByteArrayInputStream(out.toByteArray());
            }
        } catch (IOException e) {
            throw new RuntimeException("Error generating CSV file: " + e.getMessage());
        }
    }

    private ByteArrayInputStream generateStudentPdf(List<Student> students) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter writer = new PdfWriter(out);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf, PageSize.A4);
            document.setMargins(20, 20, 20, 20);

            String fontPath = "src/main/resources/static/font/Roboto-Regular.ttf";
            PdfFont font = PdfFontFactory.createFont(fontPath, PdfEncodings.IDENTITY_H, true);
            document.setFont(font);

            Paragraph title = new Paragraph("Student List")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(16)
                    .setBold()
                    .setMarginBottom(20);
            document.add(title);

            Table table = new Table(UnitValue.createPercentArray(new float[]{1, 2, 2, 2, 2, 3, 3, 3}))
                    .setWidth(UnitValue.createPercentValue(100));

            Stream.of("ID", "Username", "Student Code", "Last Name", "First Name", "Email", "Phone Number", "Address")
                    .forEach(header -> {
                        table.addHeaderCell(new com.itextpdf.layout.element.Cell()
                                .add(new Paragraph(header).setFont(font).setBold()));
                    });

            for (Student student : students) {
                table.addCell(new com.itextpdf.layout.element.Cell().add(new Paragraph(String.valueOf(student.getId())).setFont(font)));
                table.addCell(new com.itextpdf.layout.element.Cell().add(new Paragraph(student.getUsername()).setFont(font)));
                table.addCell(new com.itextpdf.layout.element.Cell().add(new Paragraph(student.getStudentCode()).setFont(font)));
                table.addCell(new com.itextpdf.layout.element.Cell().add(new Paragraph(student.getLastName()).setFont(font)));
                table.addCell(new com.itextpdf.layout.element.Cell().add(new Paragraph(student.getFirstName()).setFont(font)));
                table.addCell(new com.itextpdf.layout.element.Cell().add(new Paragraph(student.getEmail()).setFont(font)));
                table.addCell(new com.itextpdf.layout.element.Cell().add(new Paragraph(student.getPhoneNumber()).setFont(font)));
                table.addCell(new com.itextpdf.layout.element.Cell().add(new Paragraph(student.getAddress()).setFont(font)));
            }

            document.add(table);
            document.close();
        } catch (Exception e) {
            throw new IOException("Error generating PDF file: " + e.getMessage(), e);
        }

        return new ByteArrayInputStream(out.toByteArray());
    }

    private ByteArrayInputStream generateStudentCardExcel(List<StudentCard> cards) throws IOException {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Student Cards");

            Row headerRow = sheet.createRow(0);
            CellStyle headerCellStyle = workbook.createCellStyle();
            Font font = workbook.createFont();
            font.setBold(true);
            headerCellStyle.setFont(font);

            String[] headers = {"ID", "Student Name", "Student Code", "Card Number", "Badge Holder Title", "Issued Date", "Revoked Date"};
            for (int col = 0; col < headers.length; col++) {
                Cell cell = headerRow.createCell(col);
                cell.setCellValue(headers[col]);
                cell.setCellStyle(headerCellStyle);
            }

            int rowIdx = 1;
            for (StudentCard card : cards) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(card.getId());
                row.createCell(1).setCellValue(card.getStudent().getFirstName() + " " + card.getStudent().getLastName());
                row.createCell(2).setCellValue(card.getStudent().getStudentCode());
                row.createCell(3).setCellValue(card.getCardNumber());
                row.createCell(4).setCellValue(card.getBadgeHolderTitle());
                row.createCell(5).setCellValue(card.getIssuedAt() != null ? card.getIssuedAt().toString() : "");
                row.createCell(6).setCellValue(card.getRevokedAt() != null ? card.getRevokedAt().toString() : "");
            }

            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException("Error generating StudentCard Excel file: " + e.getMessage());
        }
    }

    private ByteArrayInputStream generateStudentCardCsv(List<StudentCard> cards) {
        final CSVFormat format = CSVFormat.Builder.create()
                .setHeader("ID", "Student Name", "Student Code", "Card Number", "Badge Holder Title", "Issued Date", "Revoked Date")
                .setSkipHeaderRecord(false)
                .build();

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            // UTF-8 BOM
            out.write(0xEF);
            out.write(0xBB);
            out.write(0xBF);

            try (CSVPrinter csvPrinter = new CSVPrinter(
                    new PrintWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8), true),
                    format)) {

                for (StudentCard card : cards) {
                    csvPrinter.printRecord(
                            card.getId(),
                            card.getStudent().getFirstName() + " " + card.getStudent().getLastName(),
                            card.getStudent().getStudentCode(),
                            card.getCardNumber(),
                            card.getBadgeHolderTitle(),
                            card.getIssuedAt() != null ? card.getIssuedAt().toString() : "",
                            card.getRevokedAt() != null ? card.getRevokedAt().toString() : ""
                    );
                }

                csvPrinter.flush();
                return new ByteArrayInputStream(out.toByteArray());
            }
        } catch (IOException e) {
            throw new RuntimeException("Error generating StudentCard CSV file: " + e.getMessage());
        }
    }

    private ByteArrayInputStream generateStudentCardPdf(List<StudentCard> cards) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter writer = new PdfWriter(out);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf, PageSize.A4.rotate()); // để rộng ra cho đủ cột
            document.setMargins(20, 20, 20, 20);

            String fontPath = "src/main/resources/static/font/Roboto-Regular.ttf";
            PdfFont font = PdfFontFactory.createFont(fontPath, PdfEncodings.IDENTITY_H, true);
            document.setFont(font);

            Paragraph title = new Paragraph("Student Card List")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(16)
                    .setBold()
                    .setMarginBottom(20);
            document.add(title);

            Table table = new Table(UnitValue.createPercentArray(new float[]{1, 3, 2, 2, 2, 2, 2}))
                    .setWidth(UnitValue.createPercentValue(100));

            Stream.of("ID", "Student Name", "Student Code", "Card Number", "Badge Holder Title", "Assigned Date", "Revoked Date")
                    .forEach(header -> {
                        table.addHeaderCell(new com.itextpdf.layout.element.Cell()
                                .add(new Paragraph(header).setFont(font).setBold()));
                    });

            for (StudentCard card : cards) {
                table.addCell(new com.itextpdf.layout.element.Cell().add(new Paragraph(String.valueOf(card.getId())).setFont(font)));
                table.addCell(new com.itextpdf.layout.element.Cell().add(new Paragraph(card.getStudent().getFirstName() + " " + card.getStudent().getLastName()).setFont(font)));
                table.addCell(new com.itextpdf.layout.element.Cell().add(new Paragraph(card.getStudent().getStudentCode()).setFont(font)));
                table.addCell(new com.itextpdf.layout.element.Cell().add(new Paragraph(card.getCardNumber()).setFont(font)));
                table.addCell(new com.itextpdf.layout.element.Cell().add(new Paragraph(card.getBadgeHolderTitle()).setFont(font)));
                table.addCell(new com.itextpdf.layout.element.Cell().add(new Paragraph(card.getIssuedAt() != null ? card.getIssuedAt().toString() : "").setFont(font)));
                table.addCell(new com.itextpdf.layout.element.Cell().add(new Paragraph(card.getRevokedAt() != null ? card.getRevokedAt().toString() : "").setFont(font)));
            }

            document.add(table);
            document.close();
        } catch (Exception e) {
            throw new IOException("Error generating StudentCard PDF file: " + e.getMessage(), e);
        }

        return new ByteArrayInputStream(out.toByteArray());
    }

    public static class DeleteRequest {
        private List<Long> ids;

        public List<Long> getIds() {
            return ids;
        }

        public void setIds(List<Long> ids) {
            this.ids = ids;
        }
    }

    // api create card
    @PostMapping("/{id}/create-card")
    public ResponseEntity<?> createStudentCard(@PathVariable Long id, @RequestBody CreateCardRequest request) {
        try {
            Student student = studentService.findStudentById(id)
                    .orElseThrow(() -> new RuntimeException("Student not found"));

            StudentCard card = StudentCard.builder()
                    .cardNumber(request.getCardNumber())
                    .badgeHolderTitle(request.getBadgeHolderTitle())
                    .issuedAt(request.getIssuedAt())
                    .active(true)
                    .student(student)
                    .build();

            StudentCard savedCard = studentService.createStudentCard(card);

            return ResponseEntity.ok(savedCard);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Failed to create card: " + e.getMessage());
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateCardRequest {
        private String cardNumber;
        private String badgeHolderTitle;
        private LocalDate issuedAt;
    }
}
