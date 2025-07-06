package com.example.auto_ecole.DTO;

public class AuthRequest {
    private String username;
    private String password;

    private String phone;
    private String address;
    private String drivingSchoolName;

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getDrivingSchoolName() { return drivingSchoolName; }
    public void setDrivingSchoolName(String drivingSchoolName) { this.drivingSchoolName = drivingSchoolName; }


    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
