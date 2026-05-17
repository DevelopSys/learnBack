package com.example.documentsAPP.service;

import com.example.documentsAPP.model.Course;
import com.example.documentsAPP.model.Student;
import com.example.documentsAPP.repository.CourseRepository;
import com.example.documentsAPP.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class StudentService {

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private CourseRepository courseRepository;

    public Student save(Student student) {
        return studentRepository.save(student);
    }

    public Student update(Long id, Student studentData) {
        return studentRepository.findById(id).map(student -> {
            student.setFirstName(studentData.getFirstName());
            student.setLastName(studentData.getLastName());
            student.setEmail(studentData.getEmail());
            student.setDni(studentData.getDni());
            student.setBirthDate(studentData.getBirthDate());
            student.setAddress(studentData.getAddress());
            student.setCourse(studentData.getCourse());
            return studentRepository.save(student);
        }).orElse(null);
    }

    public List<Student> findAll() {
        return studentRepository.findAll();
    }

    public Student findById(Long id) {
        return studentRepository.findById(id).orElse(null);
    }

    public boolean deleteById(Long id) {
        if (!studentRepository.existsById(id)) {
            return false;
        }

        studentRepository.deleteById(id);
        return true;
    }

    public void deleteAll() {
        studentRepository.deleteAll();
    }

    // ---------- NUEVO: importar alumnos desde CSV ----------

    public int importFromCsv(MultipartFile file) {
        List<Student> studentsToSave = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {

            String line;
            boolean firstLine = true;

            while ((line = reader.readLine()) != null) {
                if (firstLine) {
                    // saltar cabecera
                    firstLine = false;
                    continue;
                }

                if (line.trim().isEmpty()) {
                    continue;
                }

                String[] columns = line.split(",");

                if (columns.length < 7) {
                    throw new RuntimeException("Línea inválida, se esperaban 7 columnas: " + line);
                }

                String firstName = columns[0].trim();
                String lastName = columns[1].trim();
                String email = columns[2].trim();
                String dni = columns[3].trim();
                String birthDateStr = columns[4].trim();
                String address = columns[5].trim();
                String courseIdStr = columns[6].trim();

                if (firstName.isEmpty() || lastName.isEmpty() ||
                        email.isEmpty() || dni.isEmpty() || courseIdStr.isEmpty()) {
                    throw new RuntimeException("Campos obligatorios vacíos en línea: " + line);
                }

                Long courseId;
                try {
                    courseId = Long.parseLong(courseIdStr);
                } catch (NumberFormatException e) {
                    throw new RuntimeException("courseId no numérico en línea: " + line);
                }

                Optional<Course> courseOpt = courseRepository.findById(courseId);
                if (courseOpt.isEmpty()) {
                    throw new RuntimeException("Curso no encontrado con id: " + courseId + " en línea: " + line);
                }

                if (studentRepository.existsByDni(dni)) {
                    throw new RuntimeException("Ya existe un alumno con DNI: " + dni);
                }

                if (studentRepository.existsByEmail(email)) {
                    throw new RuntimeException("Ya existe un alumno con email: " + email);
                }

                Student student = new Student();
                student.setFirstName(firstName);
                student.setLastName(lastName);
                student.setEmail(email);
                student.setDni(dni);
                student.setAddress(address.isEmpty() ? null : address);
                student.setCourse(courseOpt.get());

                if (!birthDateStr.isEmpty()) {
                    try {
                        LocalDate birthDate = LocalDate.parse(birthDateStr);
                        student.setBirthDate(birthDate);
                    } catch (Exception e) {
                        throw new RuntimeException("Fecha de nacimiento inválida en línea: " + line);
                    }
                }

                studentsToSave.add(student);
            }

        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Error leyendo el fichero CSV: " + e.getMessage());
        }

        studentRepository.saveAll(studentsToSave);
        return studentsToSave.size();
    }

    // ---------- NUEVO: plantilla CSV ----------

    public String generateCsvTemplate() {
        StringBuilder sb = new StringBuilder();

        sb.append("firstName,lastName,email,dni,birthDate,address,courseId\n");
        sb.append("Juan,Perez,juan.perez@example.com,12345678A,2000-01-15,Calle Falsa 123,1\n");

        return sb.toString();
    }
}