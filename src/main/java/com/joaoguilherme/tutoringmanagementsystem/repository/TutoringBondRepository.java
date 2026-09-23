package com.joaoguilherme.tutoringmanagementsystem.repository;

import com.joaoguilherme.tutoringmanagementsystem.model.Subject;
import com.joaoguilherme.tutoringmanagementsystem.model.TutoringBond;
import com.joaoguilherme.tutoringmanagementsystem.model.User;
import com.joaoguilherme.tutoringmanagementsystem.model.enums.BondStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TutoringBondRepository extends JpaRepository<TutoringBond, UUID> {

    public List<TutoringBond> findByRequesterAndSubjectAndStatusIn(User requester, Subject subject, List<BondStatus> statusIn);


}
