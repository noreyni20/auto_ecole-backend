package com.example.auto_ecole.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;


    public void sendEmail(String to, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);
        mailSender.send(message);
    }


    public void sendCredentials(String to, String username, String password) {
        String body = "Bienvenue !\n\nVoici vos identifiants :\n" +
                "Email : " + username + "\n" +
                "Mot de passe : " + password + "\n\n" +
                "Vous devez le modifier à la première connexion.";
        sendEmail(to, "🛡 Vos Identifiants Admin", body);
    }
}
