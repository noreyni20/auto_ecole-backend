package com.example.auto_ecole.Entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.*;

@Entity
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Identifiants d’authentification
    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(unique = true, nullable = false)
    private String phone;

    private boolean enabled = true;

    // Rôles : SUPER_ADMIN, ADMIN, CANDIDAT
    @ManyToMany(fetch = FetchType.EAGER)
    private Set<Role> roles = new HashSet<>();
    private String role;

    // Infos spécifiques à l’admin
    private String address;
    private String drivingSchoolName;
    private Integer candidateQuota;


    // Infos spécifiques aux candidats
    private LocalDate registrationDate;
    private LocalDate expirationDate;

    // L’admin ayant créé ce candidat (nullable)
    @ManyToOne
    private User createdBy;

    // ===========================
    //        GETTERS / SETTERS
    // ===========================
    public String getRole() {
        return role;
    }
    public void setRole(String role) {
        this.role = role;
    }

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }

    public String getPhone() {
        return phone;
    }
    public void setPhone(String phone) {
        this.phone = phone;
    }

    public boolean isEnabled() {
        return enabled;
    }
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Set<Role> getRoles() {
        return roles;
    }
    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }

    public String getAddress() {
        return address;
    }
    public void setAddress(String address) {
        this.address = address;
    }

    public String getDrivingSchoolName() {
        return drivingSchoolName;
    }
    public void setDrivingSchoolName(String drivingSchoolName) {
        this.drivingSchoolName = drivingSchoolName;
    }

    public Integer getCandidateQuota() {
        return candidateQuota;
    }
    public void setCandidateQuota(Integer candidateQuota) {
        this.candidateQuota = candidateQuota;
    }

    public LocalDate getRegistrationDate() {
        return registrationDate;
    }
    public void setRegistrationDate(LocalDate registrationDate) {
        this.registrationDate = registrationDate;
    }

    public LocalDate getExpirationDate() {
        return expirationDate;
    }
    public void setExpirationDate(LocalDate expirationDate) {
        this.expirationDate = expirationDate;
    }

    public User getCreatedBy() {
        return createdBy;
    }
    public void setCreatedBy(User createdBy) {
        this.createdBy = createdBy;
    }
}
