package com.sispro3d.unam.workorder.repository.impl;

import com.sispro3d.unam.workorder.dao.WorkOrderJdbcDAO;
import com.sispro3d.unam.workorder.domain.WorkOrder;
import com.sispro3d.unam.workorder.repository.WorkOrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class WorkOrderRepositoryJdbc implements WorkOrderRepository {

    @Autowired
    private WorkOrderJdbcDAO dao;

    @Override
    public WorkOrder save(WorkOrder workOrder) {
        int generatedId = dao.insert(workOrder);
        workOrder.setId(generatedId);
        return workOrder;
    }

    @Override
    public List<WorkOrder> saveAll(Iterable<WorkOrder> entities) {
        List<WorkOrder> result = new ArrayList<>();
        for (WorkOrder entity : entities) {
            result.add(save(entity));
        }
        return result;
    }

    @Override
    public Optional<WorkOrder> findById(Integer id) {
        return dao.findById(id);
    }

    @Override
    public List<WorkOrder> findAll() {
        return dao.findAll();
    }

    @Override
    public WorkOrder update(WorkOrder workOrder) {
        dao.update(workOrder);
        return workOrder;
    }

    @Override
    public void delete(WorkOrder workOrder) {
        dao.delete(workOrder.getId());
    }

    @Override
    public void deleteById(Integer id) {
        dao.delete(id);
    }

    @Override
    public boolean existsById(Integer id) {
        return dao.findById(id).isPresent();
    }

    @Override
    public long count() {
        return dao.findAll().size();
    }
}
