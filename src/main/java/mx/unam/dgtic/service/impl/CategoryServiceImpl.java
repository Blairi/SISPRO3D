package mx.unam.dgtic.service.impl;

import mx.unam.dgtic.dto.CategoryDTO;
import mx.unam.dgtic.entities.CategoryEntity;
import mx.unam.dgtic.mapper.CategoryMapper;
import mx.unam.dgtic.repository.ICategoryRepository;
import mx.unam.dgtic.repository.impl.CategoryRepository;
import mx.unam.dgtic.service.CategoryService;

import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

public class CategoryServiceImpl implements CategoryService {

    private final ICategoryRepository categoryRepository;

    public CategoryServiceImpl() {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("micursojpa");
        EntityManager em = emf.createEntityManager();
        this.categoryRepository = new CategoryRepository(em);
    }

    @Override
    public List<CategoryDTO> findAll() {
        return CategoryMapper.toDtoList(categoryRepository.findAll());
    }

    @Override
    public CategoryDTO findById(Integer id) {
        return CategoryMapper.toDTO(categoryRepository.findById(id));
    }

    @Override
    public CategoryDTO create(CategoryDTO dto) {
        CategoryEntity entity = CategoryMapper.toEntity(dto);
        categoryRepository.save(entity);
        return CategoryMapper.toDTO(entity);
    }

    @Override
    public CategoryDTO update(Integer id, CategoryDTO dto) {
        CategoryEntity existingCategory = categoryRepository.findById(id);
        if (existingCategory == null) {
            throw new RuntimeException("Category no encontrada con id: " + id);
        }

        CategoryEntity categoryEntity = CategoryMapper.toEntity(dto);
        categoryEntity.setId(id);
        categoryRepository.update(categoryEntity);
        return CategoryMapper.toDTO(categoryEntity);
    }

    @Override
    public void delete(Integer id) {
        CategoryEntity existingCategory = categoryRepository.findById(id);
        if (existingCategory == null) {
            throw new RuntimeException("Category no encontrada con id: " + id);
        }
        categoryRepository.delete(existingCategory);
    }
}
