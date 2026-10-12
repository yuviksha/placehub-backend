package com.placementhub.placementhub;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = {
    "http://localhost:5173",
    "http://127.0.0.1:5173"
})
public class StudentController {

    private final StudentRepository studentRepository;
    private final PasswordEncoder passwordEncoder;

    public StudentController(
            StudentRepository studentRepository,
            PasswordEncoder passwordEncoder) {
        this.studentRepository = studentRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> data) {

        String name = data.get("name");
        String email = data.get("email");
        String password = data.get("password");

        if (name == null || name.isBlank()
                || email == null || email.isBlank()
                || password == null || password.length() < 8) {
            return ResponseEntity.badRequest().body(
                Map.of("message",
                    "Enter your name and email, and a password of at least 8 characters.")
            );
        }

        email = email.trim().toLowerCase();

        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            return ResponseEntity.badRequest().body(
                Map.of("message", "Enter a valid email address.")
            );
        }

        if (studentRepository.existsByEmailIgnoreCase(email)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(
                Map.of("message", "An account with this email already exists.")
            );
        }

        Student student = new Student(
            name.trim(),
            email,
            passwordEncoder.encode(password)
        );

        Student savedStudent = studentRepository.save(student);

        return ResponseEntity.status(HttpStatus.CREATED).body(
            Map.of(
                "message", "Registration successful!",
                "id", savedStudent.getId(),
                "name", savedStudent.getName(),
                "email", savedStudent.getEmail()
            )
        );
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> data) {

        String email = data.get("email");
        String password = data.get("password");

        if (email == null || email.isBlank()
                || password == null || password.isBlank()) {
            return ResponseEntity.badRequest().body(
                Map.of("message", "Email and password are required.")
            );
        }

        Optional<Student> result =
            studentRepository.findByEmailIgnoreCase(email.trim());

        if (result.isEmpty()
                || !passwordEncoder.matches(
                    password, result.get().getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                Map.of("message", "Invalid email or password.")
            );
        }

        Student student = result.get();

        return ResponseEntity.ok(
            Map.of(
                "message", "Login successful!",
                "id", student.getId(),
                "name", student.getName(),
                "email", student.getEmail()
            )
        );
    }
}