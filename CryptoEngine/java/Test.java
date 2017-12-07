/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.codahale.shamir;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author tim
 */
public class Test {

    public static void main(String[] args) {
        doIt();
    }

    static void doIt() {
        final Scheme scheme = new Scheme(5, 3);
        final byte[] secret = "hello there".getBytes(StandardCharsets.UTF_8);
        final Map<Integer, byte[]> parts = scheme.split(secret);

        int counter = 0;
        Map<Integer, byte[]> p = new HashMap<>();
        for (Integer i : parts.keySet()) {
            p.put(i, parts.get(i));
            if (counter > 1) {
                break;
            }
            counter++;
        }

        for (Integer i : p.keySet()) {

            StringBuilder sb = new StringBuilder();
            for (byte b : p.get(i)) {
                sb.append(String.format("%02X ", b));
            }
            System.out.println(sb.toString());
            System.out.println("");
        }

        final byte[] recovered = scheme.join(p);
        System.out.println(new String(recovered, StandardCharsets.UTF_8));
    }
}
