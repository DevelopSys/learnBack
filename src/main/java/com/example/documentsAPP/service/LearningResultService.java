package com.example.documentsAPP.service;

import com.example.documentsAPP.dto.LearningResultCreateRequest;
import com.example.documentsAPP.dto.LearningResultUpdateRequest;
import com.example.documentsAPP.model.Course;
import com.example.documentsAPP.model.LearningResult;
import com.example.documentsAPP.repository.CourseRepository;
import com.example.documentsAPP.repository.LearningResultRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import java.util.List;

@Service
public class LearningResultService {

    @Autowired
    private LearningResultRepository learningResultRepository;

    @Autowired
    private CourseRepository courseRepository;

    public List<LearningResult> findAll() {
        return learningResultRepository.findAll();
    }

    public LearningResult findById(Long id) {
        return learningResultRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Resultado de aprendizaje no encontrado"));
    }

    public List<LearningResult> findByCourseId(Long courseId) {
        return learningResultRepository.findByCourseIdOrderByNumberAsc(courseId);
    }

    public List<LearningResult> findBySubjectCode(String subjectCode) {
        return learningResultRepository.findBySubjectCodeOrderByNumberAsc(subjectCode);
    }

    public LearningResult create(LearningResultCreateRequest request) {
        validarCreate(request);

        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new RuntimeException("Curso no encontrado"));

        LearningResult lr = new LearningResult();
        lr.setSubjectCode(request.getSubjectCode().trim());
        lr.setSubjectName(request.getSubjectName().trim());
        lr.setNumber(request.getNumber());
        lr.setDescription(request.getDescription().trim());
        lr.setCourse(course);

        return learningResultRepository.save(lr);
    }

    public LearningResult update(Long id, LearningResultUpdateRequest request) {
        LearningResult lr = learningResultRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Resultado de aprendizaje no encontrado"));

        if (request.getSubjectCode() != null && !request.getSubjectCode().trim().isEmpty()) {
            lr.setSubjectCode(request.getSubjectCode().trim());
        }

        if (request.getSubjectName() != null && !request.getSubjectName().trim().isEmpty()) {
            lr.setSubjectName(request.getSubjectName().trim());
        }

        if (request.getNumber() != null) {
            lr.setNumber(request.getNumber());
        }

        if (request.getDescription() != null && !request.getDescription().trim().isEmpty()) {
            lr.setDescription(request.getDescription().trim());
        }

        if (request.getCourseId() != null) {
            Course course = courseRepository.findById(request.getCourseId())
                    .orElseThrow(() -> new RuntimeException("Curso no encontrado"));
            lr.setCourse(course);
        }

        return learningResultRepository.save(lr);
    }

    public void delete(Long id) {
        LearningResult lr = learningResultRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Resultado de aprendizaje no encontrado"));

        learningResultRepository.delete(lr);
    }

    private void validarCreate(LearningResultCreateRequest request) {
        if (request.getSubjectCode() == null || request.getSubjectCode().trim().isEmpty()) {
            throw new RuntimeException("El código de módulo es obligatorio");
        }

        if (request.getSubjectName() == null || request.getSubjectName().trim().isEmpty()) {
            throw new RuntimeException("El nombre de módulo es obligatorio");
        }

        if (request.getNumber() == null) {
            throw new RuntimeException("El número del resultado de aprendizaje es obligatorio");
        }

        if (request.getDescription() == null || request.getDescription().trim().isEmpty()) {
            throw new RuntimeException("La descripción es obligatoria");
        }

        if (request.getCourseId() == null) {
            throw new RuntimeException("El curso es obligatorio");
        }
    }
    public int importFromCsv(MultipartFile file) {
        List<LearningResult> resultsToSave = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {

            String line = null;
            boolean firstLine = true;

            while ((line = reader.readLine()) != null) {
                if (firstLine) {
                    firstLine = false;
                    continue;
                }

                if (line.trim().isEmpty()) {
                    continue;
                }

                String[] columns = line.split(",", -1);

                if (columns.length < 5) {
                    throw new RuntimeException(
                            "Línea inválida, se esperaban 5 columnas: " + line);
                }

                String subjectCode = columns[0].trim();
                String subjectName = columns[1].trim();
                String numberText = columns[2].trim();
                String description = columns[3].trim();
                String courseIdText = columns[4].trim();

                if (subjectCode.isEmpty() || subjectName.isEmpty()
                        || numberText.isEmpty() || description.isEmpty()
                        || courseIdText.isEmpty()) {
                    throw new RuntimeException(
                            "Campos obligatorios vacíos en línea: " + line);
                }

                Integer number;
                Long courseId;

                try {
                    number = Integer.parseInt(numberText);
                } catch (Exception e) {
                    throw new RuntimeException(
                            "Número de resultado inválido en línea: " + line);
                }

                try {
                    courseId = Long.parseLong(courseIdText);
                } catch (Exception e) {
                    throw new RuntimeException(
                            "courseId inválido en línea: " + line);
                }

                String finalLine = line;
                Course course = courseRepository.findById(courseId)
                        .orElseThrow(() -> new RuntimeException(
                                "Curso no encontrado para courseId " + courseId + " en línea: " + finalLine));

                LearningResult lr = new LearningResult();
                lr.setSubjectCode(subjectCode);
                lr.setSubjectName(subjectName);
                lr.setNumber(number);
                lr.setDescription(description);
                lr.setCourse(course);

                resultsToSave.add(lr);
            }

        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Error leyendo el fichero CSV: " + e.getMessage());
        }

        learningResultRepository.saveAll(resultsToSave);
        return resultsToSave.size();
    }

    public String generateCsvTemplate() {
        StringBuilder sb = new StringBuilder();
        sb.append("subjectCode,subjectName,number,description,courseId\n");
        sb.append("0485,Programacion,1,Reconoce la estructura de un programa,1\n");
        return sb.toString();
    }
}