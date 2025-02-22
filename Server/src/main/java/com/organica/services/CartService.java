package com.organica.services;

import com.organica.payload.CartDto;
import com.organica.payload.CartHelp;

public interface CartService {
    CartDto addProductToCart(CartHelp cartHelp);
    CartDto GetCart(String userEmail);
    void RemoveById(Integer productId, String userEmail);
}
