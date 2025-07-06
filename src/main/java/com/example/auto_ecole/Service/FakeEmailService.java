package com.example.auto_ecole.Service;


import org.springframework.stereotype.Service;

@Service
public class FakeEmailService {

    public void sendCredentials(String to, String username, String password) {
        System.out.println("------ 📧 SIMULATION EMAIL (ADMIN) ------");
        System.out.println("To: " + to);
        System.out.println("Subject: 🛡 Vos Identifiants Admin");
        System.out.println("Body:\nBienvenue !\n\nVoici vos identifiants :\n" +
                "Email : " + username + "\nMot de passe : " + password + "\n\n" +
                "Vous devez le modifier à la première connexion.");
        System.out.println("----------------------------------------");
    }

    public void sendEmail(String to, String subject, String body) {
        System.out.println("------ 📧 SIMULATION EMAIL ------");
        System.out.println("To: " + to);
        System.out.println("Subject: " + subject);
        System.out.println("Body:\n" + body);
        System.out.println("--------------------------------");
    }
}

