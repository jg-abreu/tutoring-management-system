package com.joaoguilherme.tutoringmanagementsystem.repository;

import com.joaoguilherme.tutoringmanagementsystem.model.TutoringBond;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TutoringBondRepository extends JpaRepository<TutoringBond, UUID> {
}
