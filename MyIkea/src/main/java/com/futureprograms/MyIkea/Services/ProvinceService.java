package com.futureprograms.MyIkea.Services;

import org.springframework.stereotype.Service;
import com.futureprograms.MyIkea.Models.Province;
import com.futureprograms.MyIkea.Repositories.ProvinceRepository;
import java.util.List;
import org.springframework.lang.NonNull;

@Service
public class ProvinceService {
    private final ProvinceRepository provinceRepository;

    public ProvinceService(ProvinceRepository provinceRepository) {
        this.provinceRepository = provinceRepository;
    }

    public List<Province> getAllProvinces() {
        return provinceRepository.findAll();
    }

    public Province getProvinceById(Integer id) {
        if (id == null) return null;
        return provinceRepository.findById(id).orElse(null);
    }

    public Province saveProvince(@NonNull Province province) {
        return provinceRepository.save(province);
    }

    public void deleteProvince(Integer id) {
        if (id != null) {
            provinceRepository.deleteById(id);
        }
    }
}
