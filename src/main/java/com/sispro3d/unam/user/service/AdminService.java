package com.sispro3d.unam.user.service;

import com.sispro3d.unam.core.service.CrudService;
import com.sispro3d.unam.user.dto.AdminRequest;
import com.sispro3d.unam.user.dto.AdminResponse;

public interface AdminService extends CrudService<AdminRequest, AdminResponse, Long> {
}
