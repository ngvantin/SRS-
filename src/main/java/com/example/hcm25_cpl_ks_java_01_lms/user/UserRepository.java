package com.example.hcm25_cpl_ks_java_01_lms.user;

import com.example.hcm25_cpl_ks_java_01_lms.course.Course;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsernameAndPassword(String username, String password);

    Optional<List<User>> findByRoles_Name(String role);

    Optional<User> findByUsername(String userName);

    boolean existsByUsernameOrEmail(String username, String email);

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);

    User findByUsernameOrEmail(String username, String email);

    Page<User> findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCase(String searchTerm, String searchTerm1, Pageable pageable);

    List<User> findByUsernameContainingIgnoreCase(String username);

    @Query("SELECT COUNT(DISTINCT a.student.id) FROM Attendance a")
    int countStudentsWithAttendance();

    @Query(value = "SELECT u.* FROM app_user u " +
            "JOIN group_users gu ON u.id = gu.user_id " +
            "WHERE gu.group_id = :groupId", nativeQuery = true)
    Page<User> findUsersByGroupId(@Param("groupId") Long groupId, Pageable pageable);

    @Query(value = "SELECT u.* FROM app_user u " +
            "LEFT JOIN group_users gu ON u.id = gu.user_id AND gu.group_id = :groupId " +
            "WHERE gu.user_id IS NULL", nativeQuery = true)
    List<User> findUsersNotInGroup(@Param("groupId") Long groupId);

    @Query(value = "SELECT u.* FROM app_user u " +
            "LEFT JOIN class_students cs ON u.id = cs.user_id AND cs.class_id = :classId " +
            "WHERE cs.user_id IS NULL " +
            "AND (LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(u.first_name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(u.last_name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')))",
            countQuery = "SELECT COUNT(*) FROM app_user u " +
                    "LEFT JOIN class_students cs ON u.id = cs.user_id AND cs.class_id = :classId " +
                    "WHERE cs.user_id IS NULL " +
                    "AND (LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
                    "OR LOWER(u.first_name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
                    "OR LOWER(u.last_name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
                    "OR LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')))",
            nativeQuery = true)
    Page<User> findUsersNotInClass(@Param("classId") Long classId,
                                   @Param("keyword") String keyword,
                                   Pageable pageable);


    @Query(value = "SELECT u.* FROM app_user u " +
            "JOIN group_users gu ON u.id = gu.user_id " +
            "WHERE gu.group_id = :groupId " +
            "AND (LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')))",
            countQuery = "SELECT COUNT(*) FROM app_user u " +
                    "JOIN group_users gu ON u.id = gu.user_id " +
                    "WHERE gu.group_id = :groupId " +
                    "AND (LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
                    "OR LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')))",
            nativeQuery = true)
    Page<User> searchUsersByGroupId(@Param("keyword") String keyword,
                                    @Param("groupId") Long groupId,
                                    Pageable pageable);

    User findByEmail(String email);

    @Transactional
    @Modifying
    @Query("DELETE FROM User u WHERE u.id = :id")
    void deleteByUserId(@Param("id") Long id);

    @Query("SELECT c FROM User u JOIN u.learningPaths lp JOIN lp.courses c WHERE u.id = :studentId")
    List<Course> findEnrolledCoursesByUserId(@Param("studentId") Long userId);

    @Query(value = "SELECT COUNT(u) > 0 FROM User u JOIN u.learningPaths lp WHERE u.id = :userId AND lp.id = :learningPathId")
    boolean isUserEnrolledInLearningPath(@Param("userId") Long userId, @Param("learningPathId") Long learningPathId);

    Page<User> findByRoles_Name(String role, Pageable pageable);

    @Query("SELECT u FROM User u JOIN u.roles ur " +
            "WHERE (LOWER(u.username) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
            "OR LOWER(u.email) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) " +
            "AND ur.name = :instructor")
    Page<User> searchUsers(String searchTerm, String instructor, Pageable pageable);

    @Query("SELECT u FROM User u " +
            "WHERE u.id != :currentUserId " +
            "AND (LOWER(u.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
            "OR LOWER(u.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) " +
            "ORDER BY u.lastName, u.firstName")
    List<User> searchUsersByName(
            @Param("currentUserId") Long currentUserId,
            @Param("searchTerm") String searchTerm,
            Pageable pageable);

    List<User> findAllByRoles_Id(Long roleId);
}