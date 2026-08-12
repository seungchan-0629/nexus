package com.example.pos.event;

public final class KafkaTopics {
    public static final String POS_PAYMENT_CREATED = "pos.payment.created";
    public static final String STORE_CREATED = "store.created";
    public static final String STORE_UPDATED = "store.updated";
    public static final String MENU_CREATED = "menu.created";
    public static final String MENU_UPDATED = "menu.updated";
    public static final String MENU_DELETED = "menu.deleted";
    public static final String PRODUCT_CREATED = "product.created";
    public static final String PRODUCT_UPDATED = "product.updated";
    public static final String PRODUCT_DELETED = "product.deleted";
    public static final String POS_CLOSE_COMPLETED = "pos.close.completed";

    private KafkaTopics() {}
}