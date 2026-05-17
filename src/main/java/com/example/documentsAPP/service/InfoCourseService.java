package com.example.documentsAPP.service;

import com.example.documentsAPP.model.InfoCourse;
import com.example.documentsAPP.repository.InfoCourseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class InfoCourseService {

    @Autowired
    private InfoCourseRepository infoCourseRepository;


    public List<InfoCourse> findAll() {
        return infoCourseRepository.findAll();
    }

    public InfoCourse findById(Long id) {
        return infoCourseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("InfoCourse no encontrado con id " + id));
    }

    public InfoCourse save(InfoCourse infoCourse) {
        validar(infoCourse);
        return infoCourseRepository.save(infoCourse);
    }

    public InfoCourse update(Long id, InfoCourse newData) {
        InfoCourse infoCourse = infoCourseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("InfoCourse no encontrado con id " + id));

        validar(newData);

        infoCourse.setDirectorName(newData.getDirectorName());
        infoCourse.setDirectorLastName(newData.getDirectorLastName());
        infoCourse.setSchoolNumber(newData.getSchoolNumber());
        infoCourse.setSchoolName(newData.getSchoolName());
        infoCourse.setSchoolEmail(newData.getSchoolEmail());
        infoCourse.setSchoolPhone(newData.getSchoolPhone());
        infoCourse.setSchoolAddress(newData.getSchoolAddress());
        infoCourse.setSchoolYear(newData.getSchoolYear());

        return infoCourseRepository.save(infoCourse);
    }

    public void delete(Long id) {
        InfoCourse infoCourse = infoCourseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("InfoCourse no encontrado con id " + id));

        infoCourseRepository.delete(infoCourse);
    }

    private void validar(InfoCourse infoCourse) {
        if (infoCourse.getDirectorName() == null || infoCourse.getDirectorName().trim().isEmpty()) {
            throw new RuntimeException("El nombre del director es obligatorio");
        }
        if (infoCourse.getDirectorLastName() == null || infoCourse.getDirectorLastName().trim().isEmpty()) {
            throw new RuntimeException("El apellido del director es obligatorio");
        }
        if (infoCourse.getSchoolNumber() == null || infoCourse.getSchoolNumber().trim().isEmpty()) {
            throw new RuntimeException("El número de centro es obligatorio");
        }
        if (infoCourse.getSchoolName() == null || infoCourse.getSchoolName().trim().isEmpty()) {
            throw new RuntimeException("El nombre del centro es obligatorio");
        }
        if (infoCourse.getSchoolEmail() == null || infoCourse.getSchoolEmail().trim().isEmpty()) {
            throw new RuntimeException("El correo del centro es obligatorio");
        }
        if (infoCourse.getSchoolPhone() == null || infoCourse.getSchoolPhone().trim().isEmpty()) {
            throw new RuntimeException("El teléfono del centro es obligatorio");
        }
        if (infoCourse.getSchoolAddress() == null || infoCourse.getSchoolAddress().trim().isEmpty()) {
            throw new RuntimeException("La dirección del centro es obligatoria");
        }
        if (infoCourse.getSchoolYear() == null || infoCourse.getSchoolYear().trim().isEmpty()) {
            throw new RuntimeException("El año escolar es obligatorio");
        }
    }
}