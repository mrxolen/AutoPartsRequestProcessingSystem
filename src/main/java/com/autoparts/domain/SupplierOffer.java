package com.autoparts.domain;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "supplier_offers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SupplierOffer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String partCode;

    @Column(nullable = false)
    private String partName;

    @Column(nullable = false)
    private Integer quantity;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "amount", column = @Column(name = "purchase_price_amount", nullable = false, precision = 10, scale = 2)),
            @AttributeOverride(name = "currency", column = @Column(name = "purchase_price_currency", nullable = false, length = 3))
    })
    private Money purchasePrice;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "amount", column = @Column(name = "selling_price_amount", nullable = false, precision = 10, scale = 2)),
            @AttributeOverride(name = "currency", column = @Column(name = "selling_price_currency", nullable = false, length = 3))
    })
    private Money sellingPrice;

    @Column(nullable = false)
    private String supplierName;

    @Column(nullable = false)
    private String brandName;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "request_case_id", nullable = false)
    private RequestCase requestCase;
}
