package com.sispro3d.unam.api.service;

import com.sispro3d.unam.core.exception.ForbiddenException;
import com.sispro3d.unam.security.ApiCurrentUser;
import com.sispro3d.unam.workorder.domain.WorkOrder;
import org.springframework.stereotype.Component;

/**
 * Resuelve la identidad de la llamada API y la compara con las partes de la
 * orden. Sustituye a las verificaciones que antes recibían un id de actor en el
 * cuerpo de la petición.
 */
@Component
public class ApiOrderAccess {

    public Long currentAccountId() {
        return ApiCurrentUser.requireAccountId();
    }

    public void requireParticipant(WorkOrder order) {
        if (!isParticipant(order, currentAccountId())) {
            throw new ForbiddenException("Only the order's client or expert can access this resource");
        }
    }

    public void requireExpertOwner(WorkOrder order) {
        if (!isExpertOwner(order, currentAccountId())) {
            throw new ForbiddenException("Only the expert owning the order can perform this action");
        }
    }

    public boolean isParticipant(WorkOrder order, Long accountId) {
        return matches(order, accountId, false) || matches(order, accountId, true);
    }

    public boolean isExpertOwner(WorkOrder order, Long accountId) {
        return matches(order, accountId, true);
    }

    private boolean matches(WorkOrder order, Long accountId, boolean expert) {
        if (order == null || order.getQuote() == null || accountId == null) {
            return false;
        }

        var account = expert
                ? order.getQuote().getOfferedService().getExpert()
                : order.getQuote().getClient();

        return account != null && accountId.equals(account.getIdUser());
    }
}
