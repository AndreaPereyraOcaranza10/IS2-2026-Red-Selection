package redselection.ejercicio_c.services;

import jakarta.transaction.Transactional;

import org.springframework.data.domain.Page;
import redselection.ejercicio_c.dto.BaseDTO;
import redselection.ejercicio_c.entities.Base;

import redselection.ejercicio_c.mappers.BaseMapper;
import redselection.ejercicio_c.repositories.BaseRepository;


import org.springframework.data.domain.Pageable;
import java.io.Serializable;
import java.util.List;
import java.util.Optional;

public abstract class BaseServiceImpl<E extends Base, D extends BaseDTO, ID extends Serializable> implements BaseService<D, ID> {
    protected BaseRepository<E, ID> baseRepository;
    protected BaseMapper<E, D> baseMapper;

    public BaseServiceImpl(BaseRepository<E, ID> baseRepository, BaseMapper<E, D> baseMapper){
        this.baseRepository = baseRepository;
        this.baseMapper = baseMapper;
    }

    @Override
    @Transactional
    public List<D> findAll() throws Exception {
        try{
            List<E> entities = baseRepository.findAll();
            return baseMapper.toDTOList(entities);
        } catch (Exception e){
            throw new Exception(e.getMessage());
        }
    }

    @Override
    @Transactional
    public D findById(ID id) throws Exception {
        try{
            Optional<E> entityOptional = baseRepository.findById(id);
            return baseMapper.toDTO(entityOptional.get());
        } catch (Exception e){
            throw new Exception(e.getMessage());
        }
    }

    @Override
    @Transactional
    public D save(D dto) throws Exception {
        try{
            E entity = baseMapper.toEntity(dto);
            entity = baseRepository.save(entity);
            return baseMapper.toDTO(entity);
        } catch (Exception e){
            throw new Exception(e.getMessage());
        }
    }

    @Override
    @Transactional
    public D update(ID id, D dto) throws Exception {
        try{
            if (!baseRepository.existsById(id)){
                throw new Exception("Entidad no encontrada");
            }
            E entity = baseMapper.toEntity(dto);
            entity.setId(((Number) id).longValue());
            entity = baseRepository.save(entity);
            return baseMapper.toDTO(entity);
        } catch (Exception e){
            throw new Exception(e.getMessage());
        }
    }

    @Override
    @Transactional
    public boolean delete(ID id) throws Exception {
        try{
            if (baseRepository.existsById(id)){
                baseRepository.deleteById(id);
                return true;
            } else {
                throw new Exception();
            }
        } catch (Exception e){
            throw new Exception(e.getMessage());
        }
    }

    @Override
    @Transactional
    public Page<D> findAll(Pageable pageable) throws Exception {
        try{
            Page<E> entities = baseRepository.findAll(pageable);
            return entities.map(baseMapper::toDTO);
        } catch (Exception e){
            throw new Exception(e.getMessage());
        }

    }
}
