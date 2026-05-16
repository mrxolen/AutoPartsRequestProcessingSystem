package com.autoparts.application.service;

import com.autoparts.domain.SupplierOffer;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;

@Getter
public class SupplierOfferGroup {

    private final String partCode;
    private final String partName;
    private final List<SupplierOffer> offers = new ArrayList<>();

    public SupplierOfferGroup(String partCode, String partName) {
        this.partCode = partCode;
        this.partName = partName;
    }

    public void addOffer(SupplierOffer offer) {
        offers.add(offer);
    }
}
