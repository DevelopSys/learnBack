package com.example.documentsAPP.service;

import com.example.documentsAPP.model.Agreement;
import com.example.documentsAPP.model.Company;
import com.example.documentsAPP.model.LegalRepresentative;
import com.example.documentsAPP.repository.AgreementRepository;
import com.example.documentsAPP.repository.CompanyRepository;
import com.example.documentsAPP.repository.LegalRepresentativeRepository;
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
public class CompanyService {

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private LegalRepresentativeRepository legalRepresentativeRepository;

    @Autowired
    private AgreementRepository agreementRepository;

    public Company save(Company company) {
        if (companyRepository.existsByNif(company.getNif())) {
            throw new RuntimeException("Ya existe una empresa con ese NIF");
        }

        if (company.getRepresentatives() != null) {
            for (LegalRepresentative representative : company.getRepresentatives()) {
                representative.setCompany(company);
            }
        }

        Agreement agreement = new Agreement();
        agreement.setNumber(generateNextAgreementNumber());
        agreement.setSignDate(LocalDate.now());
        agreement.setCompany(company);
        company.setAgreement(agreement);

        return companyRepository.save(company);
    }

    public List<Company> findAll() {
        return companyRepository.findAll();
    }

    public Optional<Company> findById(Long id) {
        return companyRepository.findById(id);
    }

    public List<LegalRepresentative> getRepresentativesByCompanyId(Long companyId) {
        return legalRepresentativeRepository.findByCompanyId(companyId);
    }

    public Company update(Long id, Company companyData) {
        Optional<Company> optionalCompany = companyRepository.findById(id);

        if (optionalCompany.isEmpty()) {
            return null;
        }

        if (companyRepository.existsByNifAndIdNot(companyData.getNif(), id)) {
            throw new RuntimeException("Ya existe otra empresa con ese NIF");
        }

        Company company = optionalCompany.get();

        company.setNif(companyData.getNif());
        company.setLegalName(companyData.getLegalName());
        company.setActivity(companyData.getActivity());
        company.setStreet(companyData.getStreet());
        company.setPostalCode(companyData.getPostalCode());
        company.setCity(companyData.getCity());
        company.setProvince(companyData.getProvince());   // NUEVO
        company.setCountry(companyData.getCountry());     // NUEVO
        company.setPhone(companyData.getPhone());

        return companyRepository.save(company);
    }

    public boolean deleteById(Long id) {
        if (!companyRepository.existsById(id)) {
            return false;
        }
        companyRepository.deleteById(id);
        return true;
    }

    private String generateNextAgreementNumber() {
        Optional<Agreement> lastAgreement = agreementRepository.findTopByOrderByNumberDesc();

        int nextNumber = 1;
        if (lastAgreement.isPresent()) {
            nextNumber = Integer.parseInt(lastAgreement.get().getNumber()) + 1;
        }

        return String.format("%03d", nextNumber);
    }

    // ---------- importar empresas desde CSV ----------

    public int importFromCsv(MultipartFile file) {
        List<Company> companiesToSave = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {

            String line;
            boolean firstLine = true;

            while ((line = reader.readLine()) != null) {
                if (firstLine) {
                    firstLine = false;
                    continue;
                }

                if (line.trim().isEmpty()) {
                    continue;
                }

                String[] columns = line.split(",");

                // ahora son 9 campos obligatorios (añadimos province y country)
                if (columns.length < 9) {
                    throw new RuntimeException("Línea inválida, se esperaban al menos 9 columnas: " + line);
                }

                String nif        = columns[0].trim();
                String legalName  = columns[1].trim();
                String activity   = columns[2].trim();
                String street     = columns[3].trim();
                String postalCode = columns[4].trim();
                String city       = columns[5].trim();
                String province   = columns[6].trim(); // NUEVO
                String country    = columns[7].trim(); // NUEVO
                String phone      = columns[8].trim();

                String agreementNumberCsv   = columns.length > 9  ? columns[9].trim()  : "";
                String agreementSignDateCsv = columns.length > 10 ? columns[10].trim() : "";
                String repFullName          = columns.length > 11 ? columns[11].trim() : "";
                String repDni               = columns.length > 12 ? columns[12].trim() : "";

                if (nif.isEmpty() || legalName.isEmpty() || activity.isEmpty() ||
                        street.isEmpty() || postalCode.isEmpty() || city.isEmpty() ||
                        phone.isEmpty()) {
                    throw new RuntimeException("Campos obligatorios vacíos en línea: " + line);
                }

                if (companyRepository.existsByNif(nif)) {
                    throw new RuntimeException("Ya existe una empresa con NIF: " + nif);
                }

                Company company = new Company();
                company.setNif(nif);
                company.setLegalName(legalName);
                company.setActivity(activity);
                company.setStreet(street);
                company.setPostalCode(postalCode);
                company.setCity(city);
                company.setProvince(province.isEmpty() ? null : province); // NUEVO
                company.setCountry(country.isEmpty() ? null : country);    // NUEVO
                company.setPhone(phone);

                Agreement agreement = new Agreement();
                agreement.setNumber(
                        agreementNumberCsv.isEmpty()
                                ? generateNextAgreementNumber()
                                : agreementNumberCsv
                );

                if (!agreementSignDateCsv.isEmpty()) {
                    try {
                        agreement.setSignDate(LocalDate.parse(agreementSignDateCsv));
                    } catch (Exception e) {
                        throw new RuntimeException("Fecha de convenio inválida en línea: " + line);
                    }
                } else {
                    agreement.setSignDate(LocalDate.now());
                }

                agreement.setCompany(company);
                company.setAgreement(agreement);

                if (!repFullName.isEmpty() && !repDni.isEmpty()) {
                    LegalRepresentative rep = new LegalRepresentative();
                    rep.setFullName(repFullName);
                    rep.setDni(repDni);
                    rep.setCompany(company);
                    company.setRepresentatives(List.of(rep));
                }

                companiesToSave.add(company);
            }

        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Error leyendo el fichero CSV: " + e.getMessage());
        }

        companyRepository.saveAll(companiesToSave);
        return companiesToSave.size();
    }

    // ---------- plantilla CSV ----------

    public String generateCompaniesCsvTemplate() {
        StringBuilder sb = new StringBuilder();

        // NUEVO: province y country añadidos entre city y phone
        sb.append("nif,legalName,activity,street,postalCode,city,province,country,phone,agreementNumber,agreementSignDate,repFullName,repDni\n");
        sb.append("B12345678,Empresa Uno,Desarrollo de software,Calle Falsa 123,28001,Madrid,Madrid,España,600123123,001,2024-09-15,Juan Perez,12345678A\n");

        return sb.toString();
    }
}