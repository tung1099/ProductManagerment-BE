package com.example.productmanagement.controller;

import com.example.productmanagement.model.Category;
import com.example.productmanagement.model.Product;
import com.example.productmanagement.model.ProductForm;
import com.example.productmanagement.service.category.ICategoryService;
import com.example.productmanagement.service.product.IProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

@RestController
@CrossOrigin("*")
@RequestMapping("/products")
public class ProductController {
    @Autowired
    private IProductService productService;

    @Autowired
    private ICategoryService categoryService;

    @Autowired
    private Environment env;

    @ModelAttribute("category")
    public Iterable<Category> categories(){
        return categoryService.findAll();
    }

    @GetMapping
    public ResponseEntity<Iterable<Product>> findAll() {
        Iterable<Product> products = productService.findAll();
        return new ResponseEntity<>(products, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> findById(@PathVariable Long id) {
        Optional<Product> productOptional = productService.findById(id);
        if (!productOptional.isPresent()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(productOptional.get(), HttpStatus.OK);
    }

    @PostMapping("/{id}")
    public ResponseEntity<Product> updateBook(@PathVariable Long id, @ModelAttribute ProductForm productForm) {
        Optional<Product> productOptional = productService.findById(id);
        productForm.setId(productOptional.get().getId());
        MultipartFile multipartFile = productForm.getImage();
        String fileName = multipartFile.getOriginalFilename();
        String fileUpload = env.getProperty("upload.path");
        Product existProduct = new Product(id, productForm.getName(), productForm.getPrice(), productForm.getDescription(), fileName, productForm.getCategory());
        try {
            FileCopyUtils.copy(multipartFile.getBytes(), new File(fileUpload + fileName));
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (existProduct.getImage().equals("filename.jpg")){
            existProduct.setImage(productOptional.get().getImage());
        }
        productService.save(existProduct);
        return new ResponseEntity<>(existProduct, HttpStatus.OK);
    }


    @PostMapping
    public ResponseEntity<Product> createProduct(@ModelAttribute ProductForm productForm) {
        MultipartFile multipartFile = productForm.getImage();
        String fileName = multipartFile.getOriginalFilename();
        String fileUpload = env.getProperty("upload.path");
        try {
            FileCopyUtils.copy(multipartFile.getBytes(), new File(fileUpload + fileName));
        } catch (IOException e) {
            e.printStackTrace();
        }
        Product product = new Product(productForm.getName(), productForm.getPrice(), productForm.getDescription(), fileName, productForm.getCategory());

        productService.save(product);
        return new ResponseEntity<>(HttpStatus.ACCEPTED);
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Product> deleteProduct(@PathVariable Long id) {
        Optional<Product> productOptional = productService.findById(id);
        if (!productOptional.isPresent()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        productService.removeById(id);
        return new ResponseEntity<>(productOptional.get(), HttpStatus.OK);
    }
}
