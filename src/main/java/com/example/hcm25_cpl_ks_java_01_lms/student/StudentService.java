package com.example.hcm25_cpl_ks_java_01_lms.student;

import com.example.hcm25_cpl_ks_java_01_lms.role.Role;
import com.example.hcm25_cpl_ks_java_01_lms.role.RoleRepository;
import com.example.hcm25_cpl_ks_java_01_lms.user.User;
import com.example.hcm25_cpl_ks_java_01_lms.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Map;
import java.util.HashMap;

@Service
public class StudentService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private StudentCardRepository studentCardRepository;

    public Page<Student> findAllStudents(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return studentRepository.findAll(pageable);
    }

    public List<Student> findAll() {
        return studentRepository.findAll();
    }

    public Page<Student> searchStudents(String keyword, int page, int size) {
        return studentRepository.searchStudents(keyword, PageRequest.of(page, size));
    }

    // find all student
    public Page<Student> findAllStudents(Pageable pageable) {
        return studentRepository.findAll(pageable);
    }

    // search student
    public Page<Student> searchStudents(String keyword, Pageable pageable) {
        return studentRepository.searchStudents(keyword, pageable);
    }

    // create new student
    public Student createStudent(Student student) {
        if (userRepository.existsByUsername(student.getUsername())) {
            throw new IllegalArgumentException("Username already exists.");
        }

        Role role = roleRepository.findByName("Student")
                .orElseGet(() -> roleRepository.save(Role.builder().name("Student").build()));

        student.setPassword(passwordEncoder.encode(student.getPassword()));
        student.setRoles(Collections.singletonList(role));

        return studentRepository.save(student);
    }

    // update student
    public Student updateStudent(Long id, StudentDTO updatedStudentDTO) {
        Student existing = studentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        existing.setUsername(updatedStudentDTO.getUsername());
        existing.setEmail(updatedStudentDTO.getEmail());
        existing.setFirstName(updatedStudentDTO.getFirstName());
        existing.setLastName(updatedStudentDTO.getLastName());
        existing.setPhoneNumber(updatedStudentDTO.getPhoneNumber());
        existing.setAddress(updatedStudentDTO.getAddress());
        existing.setStudentCode(updatedStudentDTO.getStudentCode());
        existing.setIs2faEnabled(updatedStudentDTO.getIs2faEnabled());
        existing.setIsLocked(updatedStudentDTO.getIsLocked());

        if (updatedStudentDTO.getPassword() != null && !updatedStudentDTO.getPassword().isBlank()) {
            existing.setPassword(passwordEncoder.encode(updatedStudentDTO.getPassword()));
        }

        return studentRepository.save(existing);
    }

    // detele student
    public void deleteStudent(Long id) {
        studentRepository.deleteById(id);
    }

    // delete card
    public void deleteStudentCardByUserId(Long userId) {
        StudentCard card = studentCardRepository.findByStudent_Id(userId)
                .orElseThrow(() -> new RuntimeException("Student Card not found for student ID: " + userId));
        studentCardRepository.delete(card);
    }

    // find student by id (userid)
    public Optional<Student> findStudentById(Long id) {
        return studentRepository.findById(id);
    }

    public Student findStudentByStudentCode(String studentCode) {
        return studentRepository.findByStudentCode(studentCode)
                .orElse(null);
    }

    public Optional<StudentDTO> getStudentByUserId(Long userid) {
        return studentRepository.findStudentDTOByUserId(userid);
    }

    public List<Student> findByIds(List<Long> ids) {
        return studentRepository.findAllById(ids);
    }

    public List<DuplicateInfo> saveAllStudents(List<Student> students, Map<String, StudentCard> studentCards) {
        List<DuplicateInfo> duplicates = new ArrayList<>();

        Role studentRole = roleRepository.findByName("Student")
                .orElseGet(() -> roleRepository.save(Role.builder().name("Student").build()));

        for (Student student : students) {
            List<String> duplicateFields = new ArrayList<>();

            if (student.getUsername() != null && userRepository.existsByUsername(student.getUsername())) {
                duplicateFields.add("Username");
            }
            if (student.getPhoneNumber() != null && studentRepository.existsByPhoneNumber(student.getPhoneNumber())) {
                duplicateFields.add("Phone Number");
            }
            if (student.getStudentCode() != null && studentRepository.existsByStudentCode(student.getStudentCode())) {
                duplicateFields.add("Student ID");
            }
            if (student.getEmail() != null && studentRepository.existsByEmail(student.getEmail())) {
                duplicateFields.add("Email");
            }

            StudentCard cardInfo = studentCards.get(student.getUsername());
            if (cardInfo != null && cardInfo.getCardNumber() != null
                    && studentCardRepository.existsByCardNumber(cardInfo.getCardNumber())) {
                duplicateFields.add("Card Number");
            }

            if (!duplicateFields.isEmpty()) {
                duplicates.add(new DuplicateInfo(student, duplicateFields));
            } else {
                // Encode password + set role như createStudent
                student.setPassword(passwordEncoder.encode(student.getPassword()));
                student.setRoles(Collections.singletonList(studentRole));

                Student savedStudent = studentRepository.save(student);

                // Nếu có cardNumber thì lưu StudentCard
                if (cardInfo != null) {
                    cardInfo.setStudent(savedStudent);
                    studentCardRepository.save(cardInfo);
                }
            }
        }

        return duplicates;
    }

    public long getTotalStudents() {
        return studentRepository.count();
    }

    // detail student
    public StudentDTO toDTO(Student student, StudentCard card) {
        return StudentDTO.builder()
                .id(student.getId())
                .username(student.getUsername())
                .password(student.getPassword())
                .email(student.getEmail())
                .studentCode(student.getStudentCode())
                .firstName(student.getFirstName())
                .lastName(student.getLastName())
                .phoneNumber(student.getPhoneNumber())
                .address(student.getAddress())
                .is2faEnabled(student.getIs2faEnabled())
                .isLocked(student.getIsLocked())
                .cardNumber(card != null ? card.getCardNumber() : null)
                .badgeHolderTitle(card != null ? card.getBadgeHolderTitle() : null)
                .issuedAt(card != null ? card.getIssuedAt() : null)
                .build();
    }

    //create card
    public StudentCard createStudentCard(StudentCard card) {
        return studentCardRepository.save(card);
    }

    // find student card by id of card
    public Optional<StudentCard> findStudentCardById(Long id) {
        return studentCardRepository.findById(id);
    }

    public List<StudentCard> saveAllStudentCards(List<StudentCard> cards) {
        return studentCardRepository.saveAll(cards);
    }

    public List<StudentCard> findAllCards() {
        return studentCardRepository.findAll();
    }

    public List<StudentCard> findCardsByUserIds(List<Long> userIds) {
        return studentCardRepository.findByStudentIdIn(userIds);
    }

    public StudentCard updateStudentCard(Long studentId, StudentController.CreateCardRequest dto) {
        StudentCard studentCard = studentCardRepository.findByStudent_Id(studentId)
                .orElseThrow(() -> new RuntimeException("StudentCard not found for student id: " + studentId));

        studentCard.setCardNumber(dto.getCardNumber());
        studentCard.setBadgeHolderTitle(dto.getBadgeHolderTitle());
        studentCard.setIssuedAt(dto.getIssuedAt());

        return studentCardRepository.save(studentCard);
    }

    // find student card by id (user id)
    public Optional<StudentCard> findByUserId(Long id) {
        return studentCardRepository.findByStudent_Id(id);
    }
}
