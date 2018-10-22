package info.bitrich.xchangestream.bitfinex.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.math.BigDecimal;

@JsonFormat(shape = JsonFormat.Shape.ARRAY)
public class BitfinexWebSocketOrder {

    private Long id;
    private Long gid;
    private Long cid;
    private String symbol;
    private Long mtsCreate;
    private Long mtsUpdate;
    private BigDecimal amount;
    private BigDecimal amountOrig;
    private String type;
    private String typePrev;
    private Object unused1;
    private Object unused2;
    private Integer flags;
    private String orderStatus;
    private Object unused4;
    private Object unused5;
    private BigDecimal price;
    private BigDecimal priceAvg;
    private BigDecimal priceTrailing;
    private BigDecimal priceAuxLimit;
    private Object unused6;
    private Object unused7;
    private String notify;
    private Object unused8;
    private String placedId;
    private Object unused9;
    private Object unused10;
    private Object unused11;
    private String comment;
    private Object unused12;
    private Object unused13;
    private Object unused14;

    public Long getId() {
        return id;
    }

    public Long getGid() {
        return gid;
    }

    public Long getCid() {
        return cid;
    }

    public String getSymbol() {
        return symbol;
    }

    public Long getMtsCreate() {
        return mtsCreate;
    }

    public Long getMtsUpdate() {
        return mtsUpdate;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public BigDecimal getAmountOrig() {
        return amountOrig;
    }

    public String getType() {
        return type;
    }

    public String getTypePrev() {
        return typePrev;
    }

    public Object getUnused1() {
        return unused1;
    }

    public Object getUnused2() {
        return unused2;
    }

    public Integer getFlags() {
        return flags;
    }

    public String getOrderStatus() {
        return orderStatus;
    }

    public Object getUnused4() {
        return unused4;
    }

    public Object getUnused5() {
        return unused5;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public BigDecimal getPriceAvg() {
        return priceAvg;
    }

    public BigDecimal getPriceTrailing() {
        return priceTrailing;
    }

    public BigDecimal getPriceAuxLimit() {
        return priceAuxLimit;
    }

    public Object getUnused6() {
        return unused6;
    }

    public Object getUnused7() {
        return unused7;
    }

    public String getNotify() {
        return notify;
    }

    public Object getUnused8() {
        return unused8;
    }

    public String getPlacedId() {
        return placedId;
    }

    public Object getUnused9() {
        return unused9;
    }

    public Object getUnused10() {
        return unused10;
    }

    public Object getUnused11() {
        return unused11;
    }

    public String getComment() {
        return comment;
    }

    public Object getUnused12() {
        return unused12;
    }

    public Object getUnused13() {
        return unused13;
    }

    public Object getUnused14() {
        return unused14;
    }
}
