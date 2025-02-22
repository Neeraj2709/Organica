package com.organica.services.impl;

import com.organica.entities.Cart;
import com.organica.entities.CartDetalis;
import com.organica.entities.Product;
import com.organica.entities.User;
import com.organica.payload.*;
import com.organica.repositories.CartDetailsRepo;
import com.organica.repositories.CartRepo;
import com.organica.repositories.ProductRepo;
import com.organica.repositories.UserRepo;
import com.organica.services.CartService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private ProductRepo productRepo;

    @Autowired
    private CartRepo cartRepo;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private CartDetailsRepo cartDetailsRepo;

    @Autowired
    private ModelMapper modelMapper;

    @Override
    public CartDto addProductToCart(CartHelp cartHelp) {
        int productId = cartHelp.getProductId();
        int quantity = cartHelp.getQuantity();
        String userEmail = cartHelp.getUserEmail();

        AtomicReference<Integer> totalAmount = new AtomicReference<>(0);

        User user = userRepo.findByEmail(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        Product product = productRepo.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        // Get or create cart
        Cart cart = user.getCart();
        if (cart == null) {
            cart = new Cart();
            cart.setUser(user);
        }

        // Create CartDetails
        CartDetalis cartDetalis = new CartDetalis();
        cartDetalis.setProducts(product);
        cartDetalis.setQuantity(quantity);
        cartDetalis.setAmount((int) (product.getPrice() * quantity));
        cartDetalis.setCart(cart);

        List<CartDetalis> list = cart.getCartDetalis();

        AtomicReference<Boolean> flag = new AtomicReference<>(false);

        List<CartDetalis> updatedList = list.stream().map((i) -> {
            if (i.getProducts().getProductId() == productId) {
                i.setQuantity(quantity);
                i.setAmount((int) (i.getQuantity() * product.getPrice()));
                flag.set(true);
            }
            totalAmount.set(totalP(i.getAmount(), totalAmount.get()));
            return i;
        }).collect(Collectors.toList());

        if (flag.get()) {
            list.clear();
            list.addAll(updatedList);
        } else {
            totalAmount.set(totalAmount.get() + (int) (quantity * product.getPrice()));
            list.add(cartDetalis);
        }

        cart.setCartDetalis(list);
        cart.setTotalAmount(totalAmount.get());
        Cart savedCart = cartRepo.save(cart);

        // Convert to DTO
        CartDto cartDto = modelMapper.map(savedCart, CartDto.class);
        cartDto.getCartDetalis().forEach(i -> i.getProducts().setImg(decompressBytes(i.getProducts().getImg())));
        return cartDto;
    }

    @Override
    public CartDto GetCart(String userEmail) {
        User user = userRepo.findByEmail(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        Cart cart = cartRepo.findByUser(user);
        if (cart == null) {
            throw new RuntimeException("Cart not found for user: " + userEmail);
        }

        CartDto cartDto = modelMapper.map(cart, CartDto.class);
        cartDto.getCartDetalis().forEach(i -> i.getProducts().setImg(decompressBytes(i.getProducts().getImg())));
        return cartDto;
    }

    @Override
    public void RemoveById(Integer productId, String userEmail) {
        User user = userRepo.findByEmail(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        Product product = productRepo.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        Cart cart = cartRepo.findByUser(user);
        if (cart == null) {
            throw new RuntimeException("Cart not found for user: " + userEmail);
        }

        CartDetalis cartDetails = cartDetailsRepo.findByProductsAndCart(product, cart);
        if (cartDetails == null) {
            throw new RuntimeException("Product not found in cart");
        }

        cart.setTotalAmount(cart.getTotalAmount() - cartDetails.getAmount());
        cartRepo.save(cart);
        cartDetailsRepo.delete(cartDetails);
    }

    public int totalP(int t1, int total) {
        return total + t1;
    }

    public static byte[] decompressBytes(byte[] data) {
        Inflater inflater = new Inflater();
        inflater.setInput(data);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(data.length);
        byte[] buffer = new byte[1024];
        try {
            while (!inflater.finished()) {
                int count = inflater.inflate(buffer);
                outputStream.write(buffer, 0, count);
            }
            outputStream.close();
        } catch (IOException | DataFormatException e) {
            throw new RuntimeException("Error decompressing image", e);
        }
        return outputStream.toByteArray();
    }
}
