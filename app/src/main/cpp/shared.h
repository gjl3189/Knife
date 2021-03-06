#ifndef SHARED_H__
#define SHARED_H__

#include <string.h>

#define maximum_unsigned_value_of_type(a) \
    (UINTMAX_MAX >> (bitsizeof(uintmax_t) - bitsizeof(a)))

/*
 * Returns true if the multiplication of "a" and "b" will
 * overflow. The types of "a" and "b" must match and must be unsigned.
 * Note that this macro evaluates "a" twice!
 */
#define unsigned_mult_overflows(a, b) \
    ((a) && (b) > maximum_unsigned_value_of_type(a) / (a))

static inline size_t st_mult(size_t a, size_t b)
{
    if (unsigned_mult_overflows(a, b))
        die("size_t overflow: %"PRIuMAX" * %"PRIuMAX,
            (uintmax_t)a, (uintmax_t)b);
    return a * b;
}

#define MOVE_ARRAY(dst, src, n) move_array((dst), (src), (n), sizeof(*(dst)) + \
    BUILD_ASSERT_OR_ZERO(sizeof(*(dst)) == sizeof(*(src))))

static inline void move_array(void *dst, const void *src, size_t n, size_t size) {
    if (n)
        memmove(dst, src, st_mult(size, n));
}

#endif