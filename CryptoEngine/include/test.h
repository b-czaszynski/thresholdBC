#include <tc.h>
#include "mathutils.h"
#include <gmp.h>
#include <check.h>
#include <string.h>
#include <stdlib.h>
#include <stdio.h>
#include <arpa/inet.h>

/**
 * @brief Structure that's stores a pointer that points to data_len bytes, data is big-endian.
 */
struct bytes {
    void *data; /**< Pointer to data */
    uint32_t data_len; /**< Size in bytes of data */
};
typedef struct bytes bytes_t;


bytes_t *serialize_public_key(const public_key_t *pk);
