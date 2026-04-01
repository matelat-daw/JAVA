package com.futureprograms.MyIkea.Services;

import org.springframework.stereotype.Service;
import org.springframework.lang.NonNull;
import com.futureprograms.MyIkea.Models.Municipality;
import com.futureprograms.MyIkea.Models.Province;
import com.futureprograms.MyIkea.Repositories.MunicipalityRepository;
import java.util.List;

@Service
public class LocationService {
    private final MunicipalityRepository municipalityRepository;
    private final ProvinceService provinceService;

    public LocationService(MunicipalityRepository municipalityRepository, ProvinceService provinceService) {
        this.municipalityRepository = municipalityRepository;
        this.provinceService = provinceService;
    }

    public List<Municipality> getAllMunicipalities() {
        return municipalityRepository.findAll();
    }

    public Municipality getMunicipalityById(Integer id) {
        if (id == null) return null;
        return municipalityRepository.findById(id).orElse(null);
    }

    public List<Municipality> getMunicipalitiesByProvince(Province province) {
        if (province == null) return List.of();
        return municipalityRepository.findByProvince(province);
    }

    public List<Municipality> getMunicipalitiesByProvinceId(Integer provinceId) {
        if (provinceId == null) return List.of();
        return municipalityRepository.findByProvinceProvinceId(provinceId);
    }

    public Municipality saveMunicipality(Municipality municipality) {
        if (municipality == null) throw new IllegalArgumentException("Municipality cannot be null");
        return municipalityRepository.save(municipality);
    }

    public void deleteMunicipality(Integer id) {
        if (id != null) {
            municipalityRepository.deleteById(id);
        }
    }

    public List<Province> getAllProvinces() {
        return provinceService.getAllProvinces();
    }

    public Province getProvinceById(Integer id) {
        return provinceService.getProvinceById(id);
    }

    public Province saveProvince(@NonNull Province province) {
        return provinceService.saveProvince(province);
    }

    public void deleteProvince(Integer id) {
        provinceService.deleteProvince(id);
    }
}