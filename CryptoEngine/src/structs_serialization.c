#define _POSIX_C_SOURCE 200809L
#include <string.h>
#include <stdlib.h>
#include <stdio.h>
#include <arpa/inet.h>
#include "tc_internal.h"

static const uint16_t version = 1;

#define SERIALIZE_VARIABLE(dst, x) \
    do { memcpy((dst), &x, sizeof x); (dst) += sizeof x; } while(0)
#define SERIALIZE_BYTES(dst, bs) \
    do { \
        bytes_t *__b = (bs); \
        uint32_t __net_len = htonl(__b->data_len); \
        SERIALIZE_VARIABLE((dst), __net_len); \
        memcpy((dst), __b->data, __b->data_len); \
        (dst) += __b->data_len; \
    } while(0)

char *tc_serialize_key_share(const key_share_t *ks) {
    /*
     * We are going to write each struct component directly in memory, and then write that as a base64 string.
     */

    /* First we get the size of each component */
    uint16_t net_version = htons(version);
    uint16_t net_id = htons(ks->id);

    /* We prepare the buffer */
    size_t buffer_size = sizeof(net_version) + sizeof(net_id) + sizeof(ks->n->data_len) +
                         sizeof(ks->s_i->data_len) + ks->n->data_len + ks->s_i->data_len;
    uint8_t *buffer = malloc(buffer_size);

    /* Copy each field to the buffer */
    uint8_t *p = buffer;

    SERIALIZE_VARIABLE(p, net_version);
    SERIALIZE_VARIABLE(p, net_id);
    SERIALIZE_BYTES(p, ks->n);
    SERIALIZE_BYTES(p, ks->s_i);

    bytes_t bs = {buffer, (uint32_t) buffer_size};
    char *b64 = tc_bytes_b64(&bs);

    free(buffer);

    return b64;
}

char *tc_serialize_signature_share(const signature_share_t *ss) {
    uint16_t net_version = htons(version);
    uint16_t net_id = htons(ss->id);

    size_t buffer_size = sizeof(net_version) + sizeof(net_id) + sizeof(ss->x_i->data_len) +
                         sizeof(ss->c->data_len) + sizeof(ss->z->data_len) + ss->x_i->data_len + ss->c->data_len +
                         ss->z->data_len;
    uint8_t *buffer = malloc(buffer_size);

    uint8_t *p = buffer;
    SERIALIZE_VARIABLE(p, net_version);
    SERIALIZE_VARIABLE(p, net_id);
    SERIALIZE_BYTES(p, ss->x_i);
    SERIALIZE_BYTES(p, ss->c);
    SERIALIZE_BYTES(p, ss->z);

    bytes_t bs = {buffer, (uint32_t) buffer_size};
    char *b64 = tc_bytes_b64(&bs);
    free(buffer);
    return b64;
}

static bytes_t *serialize_public_key(const public_key_t *pk) {
    size_t buffer_size = sizeof(pk->n->data_len) + sizeof(pk->e->data_len) +
                         pk->n->data_len + pk->e->data_len;
    uint8_t *buffer = malloc(buffer_size);
    uint8_t *p = buffer;
    SERIALIZE_BYTES(p, pk->n);
    SERIALIZE_BYTES(p, pk->e);

    bytes_t *bs = tc_init_bytes(buffer, buffer_size);

    return bs;
}

char *tc_serialize_key_metainfo(const key_metainfo_t *kmi) {
    size_t buffer_size = 0;

    uint16_t net_version = htons(version);
    buffer_size += sizeof net_version;

    bytes_t *pk = serialize_public_key(kmi->public_key);
    buffer_size += sizeof pk->data_len;
    buffer_size += pk->data_len;

    uint16_t net_k = htons(kmi->k);
    buffer_size += sizeof net_k;

    uint16_t net_l = htons(kmi->l);
    buffer_size += sizeof net_l;

    bytes_t *vk_v = kmi->vk_v;
    bytes_t *vk_u = kmi->vk_u;

    buffer_size += sizeof vk_v->data_len;
    buffer_size += vk_v->data_len;

    buffer_size += sizeof vk_u->data_len;
    buffer_size += vk_u->data_len;


    for (int i = 0; i < kmi->l; i++) {
        buffer_size += sizeof(kmi->vk_i[i].data_len);
        buffer_size += kmi->vk_i[i].data_len;
    }

    uint8_t *buffer = malloc(buffer_size);
    uint8_t *p = buffer;

    SERIALIZE_VARIABLE(p, net_version);
    SERIALIZE_BYTES(p, pk);
    SERIALIZE_VARIABLE(p, net_k);
    SERIALIZE_VARIABLE(p, net_l);
    SERIALIZE_BYTES(p, vk_v);
    SERIALIZE_BYTES(p, vk_u);
    for (int i = 0; i < kmi->l; i++) {
        bytes_t *v = kmi->vk_i + i;
        SERIALIZE_BYTES(p, v);
    }

    bytes_t bs = {buffer, buffer_size};
    char *b64 = tc_bytes_b64(&bs);
    tc_clear_bytes(pk);
    free(buffer);

    return b64;
}

#define DESERIALIZE_SHORT(dst, buf) \
    do { \
        memcpy(&dst, buf, sizeof dst); \
        dst = ntohs(dst); \
        (buf) += sizeof dst; \
    } while (0)

#define DESERIALIZE_LEN(dst, buf) \
    do { \
        bytes_t *__b = (dst); \
        uint32_t len; \
        memcpy(&len, (buf), sizeof(len)); \
        len = ntohl(len); \
        (buf) += sizeof(len); \
        __b->data_len = len; \
    } while(0)

#define DESERIALIZE_DATA(dst, buf, len) \
    do { \
        bytes_t *__b = (dst); \
        __b->data = alloc(len); \
        memcpy(__b->data, (buf), len); \
        (buf) += len; \
    } while(0)

key_share_t *tc_deserialize_key_share(const char *b64) {
    bytes_t *buffer = tc_b64_bytes(b64);
    uint8_t *p = buffer->data;
    uint32_t rem_bytes = buffer->data_len;
    uint16_t message_version;
    if (rem_bytes < 2 * 2 + 2 * 4) { /* We must have at least enough bytes for 2 uint16_t and 2 uint32_t*/
        fprintf(stderr, "Message wasn't long enough to deserialize key share\n");
        tc_clear_bytes(buffer);
        return NULL;
    }

    DESERIALIZE_SHORT(message_version, p);
    rem_bytes -= sizeof message_version;

    if (message_version != version) {
        fprintf(stderr, "KeyShare, Version mismatch: (Message=%x) != (Library=%x)\n", message_version, version);
        tc_clear_bytes(buffer);
        return NULL;
    }

    key_share_t *ks = tc_init_key_share();

    DESERIALIZE_SHORT(ks->id, p);
    rem_bytes -= sizeof ks->id;

    DESERIALIZE_LEN(ks->n, p);
    rem_bytes -= sizeof ks->n->data_len;
    if (rem_bytes < ks->n->data_len) {
        fprintf(stderr, "Message wasn't long enough to deserialize key share\n");
        goto err;
    }
    DESERIALIZE_DATA(ks->n, p, ks->n->data_len);
    rem_bytes -=  ks->n->data_len;

    DESERIALIZE_LEN(ks->s_i, p);
    rem_bytes -= sizeof ks->s_i->data_len;

    if (rem_bytes < ks->s_i->data_len) {
        fprintf(stderr, "Message wasn't long enough to deserialize key share\n");
        goto err;
    }
    DESERIALIZE_DATA(ks->s_i, p, ks->s_i->data_len);
    rem_bytes -=  ks->s_i->data_len;

    tc_clear_bytes(buffer);

    return ks;

err:
    tc_clear_key_share(ks);
    tc_clear_bytes(buffer);
    return NULL;
}

signature_share_t *tc_deserialize_signature_share(const char *b64) {
    bytes_t *buffer = tc_b64_bytes(b64);
    uint8_t *p = buffer->data;
    uint32_t rem_bytes = buffer->data_len;
    uint16_t message_version;
    if (rem_bytes < 2 * 2 + 3 * 4) {/* We must have at least enough bytes for 2 uint16_t and 3 uint32_t*/
        fprintf(stderr, "Message wasn't long enough to deserialize signature share\n");
        goto err;
    }

    DESERIALIZE_SHORT(message_version, p);
    rem_bytes -= sizeof message_version;
    if (message_version != version) {
        fprintf(stderr, "SignatureShare, Version mismatch: (Message=%d) != (Library=%d)\n", message_version, version);
        tc_clear_bytes(buffer);
        return NULL;
    }

    signature_share_t *ss = tc_init_signature_share();

    DESERIALIZE_SHORT(ss->id, p);
    rem_bytes -= sizeof ss->id;

    DESERIALIZE_LEN(ss->x_i, p);
    rem_bytes -= sizeof ss->x_i->data_len;

    if (rem_bytes < ss->x_i->data_len) {
        fprintf(stderr, "Message wasn't long enough to deserialize signature share\n");
        goto err;
    }
    DESERIALIZE_DATA(ss->x_i, p, ss->x_i->data_len);
    rem_bytes -= ss->x_i->data_len;

    DESERIALIZE_LEN(ss->c, p);
    rem_bytes -= sizeof ss->c->data_len;

    if (rem_bytes < ss->c->data_len) {
        fprintf(stderr, "Message wasn't long enough to deserialize signature share\n");
        goto err;
    }
    DESERIALIZE_DATA(ss->c, p, ss->c->data_len);
    rem_bytes -= ss->c->data_len;

    DESERIALIZE_LEN(ss->z, p);
    rem_bytes -= sizeof ss->z->data_len;

    if (rem_bytes < ss->z->data_len) {
        fprintf(stderr, "Message wasn't long enough to deserialize signature share\n");
        goto err;
    }
    DESERIALIZE_DATA(ss->z, p, ss->z->data_len);
    rem_bytes -= ss->z->data_len;

    tc_clear_bytes(buffer);

    return ss;

err:
    tc_clear_signature_share(ss);
    tc_clear_bytes(buffer);
    return NULL;
}

key_metainfo_t *tc_deserialize_key_metainfo(const char *b64) {
    bytes_t *buffer = tc_b64_bytes(b64);
    uint8_t *p = buffer->data;
    uint32_t rem_bytes = buffer->data_len;
    uint16_t message_version;
    if (rem_bytes < 2 * 3 + 3 * 4) {/* We must have at least enough bytes for 3 uint16_t and 5 uint32_t*/
        fprintf(stderr, "Message wasn't long enough to deserialize key metainfo\n");
        goto err0;
    }

    DESERIALIZE_SHORT(message_version, p);
    rem_bytes -= sizeof message_version;

    if (message_version != version) {
        fprintf(stderr,
                "KeyMetaInfo, Version mismatch: (Message=%d) != (Library=%d)\n",
                message_version, version);
        goto err0;
    }

    bytes_t *pk = tc_init_bytes(NULL, 0);

    DESERIALIZE_LEN(pk, p);
    rem_bytes -= sizeof pk->data_len;
    if (rem_bytes < pk->data_len) {
        fprintf(stderr, "Message wasn't long enough to deserialize key metainfo\n");
        goto err1;
    }
    DESERIALIZE_DATA(pk, p, pk->data_len);
    rem_bytes -=  pk->data_len;

    uint16_t k;
    DESERIALIZE_SHORT(k, p);
    rem_bytes -= sizeof k;

    uint16_t l;
    DESERIALIZE_SHORT(l, p);
    rem_bytes -= sizeof l;

    key_metainfo_t *kmi = tc_init_key_metainfo(k, l);

    DESERIALIZE_LEN(kmi->vk_v, p);
    rem_bytes -= sizeof kmi->vk_v->data_len;
    if (rem_bytes < kmi->vk_v->data_len) {
        fprintf(stderr, "Message wasn't long enough to deserialize key metainfo\n");
        goto err2;
    }
    DESERIALIZE_DATA(kmi->vk_v, p, kmi->vk_v->data_len);
    rem_bytes -=  kmi->vk_v->data_len;

    DESERIALIZE_LEN(kmi->vk_u, p);
    rem_bytes -= sizeof kmi->vk_u->data_len;
    if (rem_bytes < kmi->vk_u->data_len) {
        fprintf(stderr, "Message wasn't long enough to deserialize key metainfo\n");
        goto err2;
    }
    DESERIALIZE_DATA(kmi->vk_u, p, kmi->vk_u->data_len);
    rem_bytes -=  kmi->vk_u->data_len;

    if (rem_bytes < 4 * l) {/* We must have at least enough bytes for 3 uint16_t and 5 uint32_t*/
        fprintf(stderr, "Message wasn't long enough to deserialize key metainfo\n");
        goto err2;
    }
    // We have to do this here, because init_key_meta_info initializes the vk_i array.
    for (int i = 0; i < l; i++) {
        bytes_t *v = kmi->vk_i + i;
        DESERIALIZE_LEN(v, p);
        rem_bytes -= sizeof v->data_len;
        if (rem_bytes < v->data_len) {
                fprintf(stderr, "Message wasn't long enough to deserialize key metainfo\n");
                goto err2;
            }
        DESERIALIZE_DATA(v, p, v->data_len);
        rem_bytes -=  v->data_len;
    }
    p = pk->data;
    rem_bytes = pk->data_len;
    if (rem_bytes < 2 * 2) {/* We must have at least enough bytes for 2 uint16_t */
        fprintf(stderr, "Message wasn't long enough to deserialize key metainfo\n");
        goto err2;
    }

    DESERIALIZE_LEN(kmi->public_key->n, p);
    rem_bytes -= sizeof kmi->public_key->n->data_len;
    if (rem_bytes < kmi->public_key->n->data_len){
        fprintf(stderr, "Message wasn't long enough to deserialize key metainfo\n");
        goto err2;
    }
    DESERIALIZE_DATA(kmi->public_key->n, p, kmi->public_key->n->data_len);
    rem_bytes -=  kmi->public_key->n->data_len;

    DESERIALIZE_LEN(kmi->public_key->e, p);
    rem_bytes -= sizeof kmi->public_key->e->data_len;
    if (rem_bytes < kmi->public_key->e->data_len) {
        fprintf(stderr, "Message wasn't long enough to deserialize key metainfo\n");
        goto err2;
    }
    DESERIALIZE_DATA(kmi->public_key->e, p, kmi->public_key->e->data_len);
    rem_bytes -=  kmi->public_key->e->data_len;

    tc_clear_bytes(buffer);
    tc_clear_bytes(pk);
    return kmi;

err2:
    tc_clear_key_metainfo(kmi);
err1:
    tc_clear_bytes(pk);
err0:
    tc_clear_bytes(buffer);
    return NULL;
}
