package com.example.auto_ecole.Util;



import com.example.auto_ecole.Entity.Role;
import com.example.auto_ecole.Entity.User;
import com.example.auto_ecole.Repository.RoleRepository;
import com.example.auto_ecole.Repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.Set;

@Component
public class DataInitializer implements CommandLineRunner {
    private final RoleRepository roleRepo;
    private final UserRepository userRepo;
    private final PasswordEncoder encoder;

    public DataInitializer(RoleRepository roleRepo, UserRepository userRepo, PasswordEncoder encoder) {
        this.roleRepo = roleRepo;
        this.userRepo = userRepo;
        this.encoder = encoder;
    }

    @Override
    public void run(String... args) {
        Optional<Role> optionalRole = roleRepo.findByName("ROLE_SUPER_ADMIN");
        Role superAdminRole = optionalRole.orElseGet(() -> roleRepo.save(new Role("ROLE_SUPER_ADMIN")));

        if (superAdminRole == null) {
            superAdminRole = roleRepo.save(new Role("ROLE_SUPER_ADMIN"));
        }

        if (userRepo.findByUsername("superadmin").isEmpty()) {
            User user = new User();
            user.setUsername("superadmin");
            user.setPassword(encoder.encode("password"));
            user.setPhone("772004305");
            user.setRoles(Set.of(superAdminRole));
            userRepo.save(user);
            System.out.println("✔ Superadmin créé avec ROLE_SUPER_ADMIN");
        }
    }
}
