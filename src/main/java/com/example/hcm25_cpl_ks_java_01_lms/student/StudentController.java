package com.example.hcm25_cpl_ks_java_01_lms.student;

import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.property.TextAlignment;
import com.itextpdf.layout.property.UnitValue;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
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

import java.io.*;
import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@RestController
@RequestMapping("/api/students")
public class StudentController {
    @Autowired
    private StudentService studentService;

    @GetMapping
    public ResponseEntity<Page<Student>> getAllStudents(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(name = "search", defaultValue = "") String searchTerm,
            @RequestParam(name = "sort", required = false) String sort ) {
        Pageable pageable = createPageable(page, size, sort);

        Page<Student> students = (searchTerm != null && !searchTerm.trim().isEmpty())
                ? studentService.searchStudents(searchTerm, pageable)
                : studentService.findAllStudents(pageable);

        return ResponseEntity.ok(students);
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

    @GetMapping("/{id}")
    public ResponseEntity<Student> getStudentById(@PathVariable Long id) {
        return studentService.findStudentById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> createStudent(@RequestBody Student student) {
        try {
            Student saved = studentService.createStudent(student);
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Student already exists");
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateStudent(@PathVariable Long id, @RequestBody Student student) {
        try {
            student.setId(id);
            Student updated = studentService.createStudent(student);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Failed to update student");
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteStudent(@PathVariable Long id) {
        try {
            studentService.deleteStudent(id);
            return ResponseEntity.ok("Student deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to delete student");
        }
    }

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
            for (Map<String, String> map : studentsJson) {
                Student student = new Student();
                student.setFirstName(map.get("First Name"));
                student.setLastName(map.get("Last Name"));
                student.setEmail(map.get("Email"));
                student.setPhoneNumber(map.get("Phone Number"));
                student.setAddress(map.get("Address"));
                student.setStudentID(map.get("Student ID"));

                if (student.getFirstName() == null || student.getStudentID() == null) {
                    return ResponseEntity.badRequest().body("Missing required fields: First Name or Student ID.");
                }

                students.add(student);
            }

            List<DuplicateInfo> duplicates = studentService.saveAllStudents(students);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Imported successfully!");
            response.put("duplicates", duplicates);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Import failed: " + e.getMessage());
        }
    }

    @GetMapping("/template")
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
    public ResponseEntity<InputStreamResource> exportAll(@RequestParam(defaultValue = "excel") String type) throws IOException {
        List<Student> students = studentService.findAll();
        return generateExportResponse(students, type, "students_all");
    }

    @PostMapping("/export-selected")
    public ResponseEntity<InputStreamResource> exportSelected(@RequestBody List<Long> ids,
                                                              @RequestParam(defaultValue = "excel") String type) throws IOException {
        List<Student> selectedStudents = studentService.findByIds(ids);
        return generateExportResponse(selectedStudents, type, "students_selected");
    }

    private ResponseEntity<InputStreamResource> generateExportResponse(List<Student> students, String type, String filename) throws IOException {
        ByteArrayInputStream stream;
        String extension;
        String contentType;

        switch (type.toLowerCase()) {
            case "excel":
                stream = generateExcel(students);
                extension = "xlsx";
                contentType = "application/vnd.ms-excel";
                break;
            case "csv":
                stream = generateCsv(students);
                extension = "csv";
                contentType = "text/csv";
                break;
            case "pdf":
                stream = generatePdf(students);
                extension = "pdf";
                contentType = "application/pdf";
                break;
            default:
                throw new IllegalArgumentException("Unsupported export type: " + type);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=" + filename + "." + extension);

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType(contentType))
                .body(new InputStreamResource(stream));
    }

    private ByteArrayInputStream generatePdf(List<Student> students) throws IOException {
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

            Table table = new Table(UnitValue.createPercentArray(new float[]{1, 2, 3, 3, 4, 3, 3}))
                    .setWidth(UnitValue.createPercentValue(100));

            Stream.of("ID", "Student ID", "Last Name", "First Name", "Email", "Phone", "Address").forEach(header -> {
                table.addHeaderCell(new com.itextpdf.layout.element.Cell(1, 1)
                        .add(new Paragraph(header).setFont(font).setBold()));
            });

            for (Student student : students) {
                table.addCell(new com.itextpdf.layout.element.Cell().add(new Paragraph(String.valueOf(student.getId())).setFont(font)));
                table.addCell(new com.itextpdf.layout.element.Cell().add(new Paragraph(student.getStudentID()).setFont(font)));
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

    private ByteArrayInputStream generateCsv(List<Student> students) {
        final CSVFormat format = CSVFormat.Builder.create()
                .setHeader("ID", "Student ID", "Last Name", "First Name", "Email", "Phone", "Address")
                .setSkipHeaderRecord(false)
                .build();

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            out.write(0xEF);
            out.write(0xBB);
            out.write(0xBF);

            try (CSVPrinter csvPrinter = new CSVPrinter(
                    new PrintWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8), true),
                    format)) {

                for (Student student : students) {
                    String phone = "=\"" + student.getPhoneNumber() + "\"";

                    csvPrinter.printRecord(
                            student.getId(),
                            student.getStudentID(),
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

    private ByteArrayInputStream generateExcel(List<Student> students) throws IOException {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Students");

            Row headerRow = sheet.createRow(0);
            CellStyle headerCellStyle = workbook.createCellStyle();
            Font font = workbook.createFont();
            font.setBold(true);
            headerCellStyle.setFont(font);

            String[] headers = {"ID", "Student ID", "Last Name", "First Name", "Email", "Phone Number", "Address"};
            for (int col = 0; col < headers.length; col++) {
                Cell cell = (Cell) headerRow.createCell(col);
                cell.setCellValue(headers[col]);
                cell.setCellStyle(headerCellStyle);
            }

            int rowIdx = 1;
            for (Student student : students) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(student.getId());
                row.createCell(1).setCellValue(student.getStudentID());
                row.createCell(2).setCellValue(student.getLastName());
                row.createCell(3).setCellValue(student.getFirstName());
                row.createCell(4).setCellValue(student.getEmail());
                row.createCell(5).setCellValue(student.getPhoneNumber());
                row.createCell(6).setCellValue(student.getAddress());
            }

            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException("Error generating excel file: " + e.getMessage());
        }
    }

    @GetMapping("/download-template")
    public ResponseEntity<Resource> downloadSampleFile() {
        String fileName = "student_template.xlsx";
        Path filePath = Paths.get("data-excel/" + fileName);

        try {
            Resource resource = new UrlResource(filePath.toUri());
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName)
                    .body(resource);
        } catch (MalformedURLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
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

    @GetMapping("/all")
    public ResponseEntity<List<Student>> getAllStudents() {
        Page<Student> studentPage = studentService.findAllStudents(0, Integer.MAX_VALUE);
        List<Student> students = studentPage.getContent();
        return ResponseEntity.ok(students);
    }
}
