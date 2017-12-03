#ifndef MATHUTILS_H
#define MATHUTILS_H

#include <gmp.h>

typedef void (*random_fn)(mpz_t rop, int bit_len);

void random_dev(mpz_t rop, int bit_len);
void random_prime(mpz_t rop, int bit_len, random_fn random);

typedef struct poly {
  mpz_t * coeff;
  int size;
} poly_t;

poly_t * create_random_poly(mpz_t d, size_t size, mpz_t m);
void clear_poly(poly_t * poly);
void poly_eval(mpz_t rop, poly_t * poly, mpz_t op);
void poly_eval_ui(mpz_t rop, poly_t * poly, unsigned long op);
#endif
