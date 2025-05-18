package com.example.hcm25_cpl_ks_java_01_lms.training_program;

import com.example.hcm25_cpl_ks_java_01_lms.syllabus.Syllabus;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import lombok.*;
import net.minidev.json.annotate.JsonIgnore;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Builder
public class TrainingProgram {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String programName;

    @Column(nullable = false, unique = true)
    private String programCode;

    @Column(nullable = true)
    private String description;

    @ManyToMany
    @JoinTable(
            name = "program_syllabuses",
            joinColumns = @JoinColumn(name = "training_program_id"),
            inverseJoinColumns = @JoinColumn(name = "syllabus_id")
    )
    @JsonIgnore
    private List<Syllabus> syllabuses;

    @DecimalMin(value = "0.0", message = "Number of version must be at least 0")
    @DecimalMax(value = "7.0", message = "Number of version cannot exceed 7.")
    @Column(nullable = true)
    private Double version;

    @Column(nullable = true)
    private String contentLink;

    @Override
    public String toString() {
        return programName;
    }
}
