/**

SIMD memchr implementation using C-intrinsics

Usage:
  ./simd_memchr <HAYSTACK> <NEEDLE>

*/
#include <immintrin.h>
#include <stdio.h>
#include <string.h>

static size_t VECTOR_SIZE = sizeof(__m256i);

void *memchr_simd(const uint8_t *haystack, uint8_t needle, size_t hlen) {

  __m256i vneedle = _mm256_set1_epi8(needle);

  size_t idx = 0;
  if (hlen > VECTOR_SIZE) {
    // SIMD LOOP
    while (idx < (hlen - VECTOR_SIZE)) {
      // LOAD DATA FROM MEMORY INTO SIMD REGISTER
      __m256i chunk = _mm256_loadu_si256((const __m256i *)(haystack + idx));
      // DO THE MAGIC
      __m256i eq_res = _mm256_cmpeq_epi8(chunk, vneedle);
      // LOAD DATA FROM SIMD REGISTERS BACK TO MEM
      int mask = _mm256_movemask_epi8(eq_res);
      if (mask != 0) {
        return (void *)haystack + idx + __builtin_ctz(mask);
      }
      idx += VECTOR_SIZE;
    }
  }
  // SCALAR TAIL
  for (; idx < hlen; idx++) {
    if (haystack[idx] == needle) {
      return (void *)haystack + idx;
    }
  }
  return NULL;
}

int main(int argc, char **argv) {
#if defined(__AVX2__)
  if (argc < 3) {
    return 1;
  }
  const char *haystack = argv[1];
  size_t hlen = strlen(haystack);
  uint8_t needle = *argv[2];

  void *res = memchr_simd((const uint8_t *)haystack, needle, hlen);
  if (res == NULL) {
    printf("not found\n");
  } else {
    size_t idx = (size_t)((char *)res - haystack);
    printf("%zu: %c\n", idx, haystack[idx]);
  }
  return 0;
#else
  printf("AVX2 not available.");
  return 1;
#endif
}
