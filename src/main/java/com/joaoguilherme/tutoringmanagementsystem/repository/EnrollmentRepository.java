package com.joaoguilherme.tutoringmanagementsystem.repository;

import com.joaoguilherme.tutoringmanagementsystem.model.Enrollment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface EnrollmentRepository extends JpaRepository<Enrollment, UUID> {
}
