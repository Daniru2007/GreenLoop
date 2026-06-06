package org.example.controller;


import org.example.Repository.Impl.ProductRepositoryImpl;
import org.example.Repository.ProductRepository;

public class ProductController {
    /***
     * Use this object to call the methods in the product repository
     * consideration - Don't use this object in static variables
     */
     private final ProductRepository productRepo = new ProductRepositoryImpl();



}
